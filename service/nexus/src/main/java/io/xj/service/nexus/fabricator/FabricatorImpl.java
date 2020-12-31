// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.music.Key;
import io.xj.lib.music.MusicalException;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.dao.InstrumentConfig;
import io.xj.service.hub.dao.ProgramConfig;
import io.xj.service.nexus.dao.ChainBindingDAO;
import io.xj.service.nexus.dao.ChainConfig;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final double MICROS_PER_SECOND = 1000000.0F;
  private static final double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;
  private static final String EXTENSION_SEPARATOR = ".";
  private static final String EXTENSION_JSON = "json";
  private static final String NAME_SEPARATOR = "-";
  private final HubClientAccess access;
  private final FileStoreProvider fileStoreProvider;
  private final Chain chain;
  private final HubContent sourceMaterial;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final long startTime;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final SegmentWorkbench workbench;
  private final SegmentRetrospective retrospective;
  private final TimeComputerFactory timeComputerFactory;
  private final Tuning tuning;
  private final double tuningRootPitch;
  private final String tuningRootNote;
  private final Set<String> boundProgramIds;
  private final Set<String> boundInstrumentIds;
  private final Config config;
  private Segment.Type type;
  private final String workTempFilePathPrefix;
  private final DecimalFormat segmentNameFormat;
  private final PayloadFactory payloadFactory;
  private final ChainConfig chainConfig;
  private final SegmentDAO segmentDAO;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("segment") Segment segment,
    Config config,
    HubClient hubClient,
    ChainDAO chainDAO,
    ChainBindingDAO chainBindingDAO,
    TimeComputerFactory timeComputerFactory,
    SegmentWorkbenchFactory segmentWorkbenchFactory,
    FileStoreProvider fileStoreProvider,
    SegmentRetrospectiveFactory retrospectiveFactory,
    PayloadFactory payloadFactory,
    SegmentDAO segmentDAO
  ) throws FabricationException {
    this.segmentDAO = segmentDAO;
    try {
      // FUTURE: [#165815496] Chain fabrication access control
      this.access = access;
      log.info("[segId={}] HubClientAccess {}", segment.getId(), access);

      this.fileStoreProvider = fileStoreProvider;
      this.timeComputerFactory = timeComputerFactory;
      this.payloadFactory = payloadFactory;

      this.config = config;
      tuningRootPitch = config.getDouble("tuning.rootPitchHz");
      tuningRootNote = config.getString("tuning.rootNote");
      workTempFilePathPrefix = config.getString("work.tempFilePathPrefix");
      segmentNameFormat = new DecimalFormat(config.getString("work.segmentNameFormat"));

      // tuning
      tuning = computeTuning();
      log.info("[segId={}] Tuning {}", segment.getId(), tuning);

      // time
      startTime = System.nanoTime();
      log.info("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainDAO.readOne(access, segment.getChainId());
      chainConfig = new ChainConfig(chain, config);
      Collection<ChainBinding> chainBindings = chainBindingDAO.readMany(access, ImmutableList.of(chain.getId()));
      Set<String> boundLibraryIds = targetIdsOfType(chainBindings, ChainBinding.Type.Library);
      boundProgramIds = targetIdsOfType(chainBindings, ChainBinding.Type.Program);
      boundInstrumentIds = targetIdsOfType(chainBindings, ChainBinding.Type.Instrument);
      log.info("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        chainConfig,
        CSV.prettyFrom(chainBindings, "and"));

      // read the source material
      sourceMaterial = hubClient.ingest(access, boundLibraryIds, boundProgramIds, boundInstrumentIds);
      log.info("[segId={}] SourceMaterial loaded {} entities", segment.getId(), sourceMaterial.size());

      // setup the segment retrospective
      retrospective = retrospectiveFactory.workOn(access, segment, sourceMaterial);

      // get the current segment on the workbench
      workbench = segmentWorkbenchFactory.workOn(access, chain, segment);

      // final pre-flight check
      ensureStorageKey();

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | HubClientException | ValueException e) {
      throw new FabricationException("Failed to instantiate Fabricator!", e);
    }
  }

  @Override
  public <N extends MessageLite> N add(N entity) throws FabricationException {
    return workbench.add(entity);
  }

  @Override
  public Double computeSecondsAtPosition(double p) throws FabricationException {
    return getTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public HubClientAccess getAccess() {
    return access;
  }

  @Override
  public Collection<InstrumentAudio> getPickedAudios() throws FabricationException {
    Collection<InstrumentAudio> audios = Lists.newArrayList();
    for (SegmentChoiceArrangementPick pick : workbench.getSegmentChoiceArrangementPicks()) {
      try {
        audios.add(sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId()));
      } catch (HubClientException e) {
        throw new FabricationException(e);
      }
    }
    return audios;
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public ChainConfig getChainConfig() {
    return chainConfig;
  }

  @Override
  public String getChainId() {
    return chain.getId();
  }

  @Override
  public Optional<SegmentChord> getChordAt(int position) throws FabricationException {
    Optional<SegmentChord> foundChord = Optional.empty();
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending
    for (SegmentChord segmentChord : workbench.getSegmentChords()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        segmentChord.getPosition() > foundPosition && segmentChord.getPosition() < position) {
        foundPosition = segmentChord.getPosition();
        foundChord = Optional.of(segmentChord);
      }
    }

    return foundChord;
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages() throws FabricationException {
    return workbench.getSegmentMessages();
  }

  @Override
  public SegmentChoice getCurrentMacroChoice() throws FabricationException {
    return workbench.getChoiceOfType(Program.Type.Macro);
  }

  @Override
  public SegmentChoice getCurrentMainChoice() throws FabricationException {
    return workbench.getChoiceOfType(Program.Type.Main);
  }

  @Override
  public SegmentChoice getCurrentRhythmChoice() throws FabricationException {
    return workbench.getChoiceOfType(Program.Type.Rhythm);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() throws FabricationException {
    return workbench.getChoicesOfType(Program.Type.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public Key getKeyForChoice(SegmentChoice choice) throws FabricationException {
    Program program = getProgram(choice);
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      return Key.of(getSequence(choice).getKey());
    }
    return Key.of(program.getKey());
  }

  @Override
  public Key getKeyForArrangement(SegmentChoiceArrangement arrangement) throws FabricationException {
    return getKeyForChoice(getChoice(arrangement).orElseThrow(() ->
      new FabricationException(String.format("No key found for Arrangement[%s]", arrangement.getId()))));
  }

  @Override
  public Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws FabricationException {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has two more available Sequence Pattern offsets");
    var sequenceBinding = getSequenceBinding(choice);

    try {
      Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
      if (max.isEmpty()) throw exception("Cannot determine max available sequence binding offset");
      return max.get();

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() throws FabricationException {
    Map<String, Collection<SegmentChoiceArrangement>> out = Maps.newHashMap();
    for (Map.Entry<String, Collection<SegmentChoice>> entry : getMemeConstellationChoicesOfPreviousSegments().entrySet()) {
      String con = entry.getKey();
      Collection<SegmentChoice> previousChoices = entry.getValue();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());

      for (SegmentChoice choice : previousChoices) {
        for (SegmentChoiceArrangement arrangement : retrospective.getArrangements(choice)) {
          out.get(con).add(arrangement);
        }
      }
    }
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() throws FabricationException {
    try {
      Map<String, Collection<SegmentChoice>> out = Maps.newHashMap();
      for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
        Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
        String con = iso.getConstellation();
        if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
        out.get(con).addAll(retrospective.getSegmentChoices(seg));
      }
      return out;

    } catch (EntityException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() throws FabricationException {
    try {
      Map<String, Collection<SegmentChoiceArrangementPick>> out = Maps.newHashMap();
      for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
        Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
        String con = iso.getConstellation();
        if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
        out.get(con).addAll(retrospective.getSegmentPicks(seg));
      }
      return out;

    } catch (EntityException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() throws FabricationException {
    try {
      return MemeIsometry.ofMemes(sourceMaterial.getMemesAtBeginning(getProgram(getCurrentMacroChoice())));
    } catch (HubClientException | EntityException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      return MemeIsometry.ofMemes(
        sourceMaterial.getMemesAtBeginning(getProgram(getPreviousMacroChoice())));

    } catch (Exception ignored) {
      return new MemeIsometry();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    try {
      return MemeIsometry.ofMemes(Entities.namesOf(workbench.getSegmentMemes()));

    } catch (FabricationException | EntityException e) {
      return MemeIsometry.none();
    }
  }

  @Override
  public Collection<SegmentMeme> getMemesOfChoice(SegmentChoice choice) throws FabricationException {
    try {
      Collection<SegmentMeme> result = Lists.newArrayList();
      sourceMaterial.getMemes(getProgram(choice))
        .forEach(meme -> result.add(SegmentMeme.newBuilder()
          .setName(meme.getName())
          .setSegmentId(choice.getSegmentId())
          .build()));
      if (Value.isSet(choice.getProgramSequenceBindingId()))
        sourceMaterial.getMemes(getSequenceBinding(choice))
          .forEach(meme -> result.add(SegmentMeme.newBuilder()
            .setName(meme.getName())
            .setSegmentId(choice.getSegmentId())
            .build()));
      return result;

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Long getNextSequenceBindingOffset(SegmentChoice choice) throws FabricationException {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine next available SequenceBinding offset create choice with no SequenceBinding.");

    try {
      var sequenceBinding = getSequenceBinding(choice);
      Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
      Long offset = null;
      for (Long availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding))
        if (0 < availableOffset.compareTo(sequenceBindingOffset))
          if (Objects.isNull(offset) ||
            0 > availableOffset.compareTo(offset))
            offset = availableOffset;

      // if none found, loop back around to zero
      return Objects.nonNull(offset) ? offset : Long.valueOf(0L);

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public AudioFormat getOutputAudioFormat() {
    return new AudioFormat(
      chainConfig.getOutputEncoding(),
      chainConfig.getOutputFrameRate(),
      chainConfig.getOutputSampleBits(),
      chainConfig.getOutputChannels(),
      chainConfig.getOutputChannels() * chainConfig.getOutputSampleBits() / 8,
      chainConfig.getOutputFrameRate(),
      false);
  }

  @Override
  public String getFullQualityAudioOutputFilePath() {
    return String.format("%s%s", workTempFilePathPrefix, getSegmentOutputWaveformKey());
  }

  @Override
  public Double getPitch(Note note) {
    return tuning.pitch(note);
  }

  @Override
  public Collection<Segment> getPreviousSegmentsWithSameMainProgram() throws FabricationException {
    return retrospective.getSegments();
  }

  @Override
  public Program getProgram(SegmentChoice choice) throws FabricationException {
    try {
      return sourceMaterial.getProgram(choice.getProgramId());

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public SegmentChoice getPreviousMacroChoice() throws FabricationException {
    return retrospective.getPreviousChoiceOfType(Program.Type.Macro);
  }

  @Override
  public SegmentChoice getPreviousMainChoice() throws FabricationException {
    return retrospective.getPreviousChoiceOfType(Program.Type.Main);
  }

  @Override
  public Segment getPreviousSegment() throws FabricationException {
    if (isInitialSegment() || retrospective.getPreviousSegment().isEmpty())
      throw exception("Initial Segment has no previous Segment");

    return retrospective.getPreviousSegment().get();
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public void updateSegment(Segment segment) throws FabricationException {
    try {
      segmentDAO.update(access, segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return getStorageKey(getSegment().getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  @Override
  public String getSegmentOutputMetadataKey() {
    return getStorageKey(EXTENSION_JSON);
  }

  @Override
  public String getStorageKey(String extension) {
    return String.format("%s%s%s", getSegment().getStorageKey(), EXTENSION_SEPARATOR, extension);
  }

  @Override
  public Duration getSegmentTotalLength() throws FabricationException {
    if (Value.isEmpty(workbench.getSegment().getEndAt()))
      throw exception("Cannot compute total length create segment with no end!");

    return Duration.between(
      Instant.parse(workbench.getSegment().getBeginAt()),
      Instant.parse(workbench.getSegment().getEndAt()))
      .plusNanos((long) (workbench.getSegment().getWaveformPreroll() * NANOS_PER_SECOND));
  }

  @Override
  public ProgramSequence getSequence(SegmentChoice choice) throws FabricationException {
    try {
      Program program = getProgram(choice);
      if (Value.isSet(choice.getProgramSequenceBindingId()))
        return sourceMaterial.getProgramSequence(getSequenceBinding(choice).getProgramSequenceId());

      if (!sequenceForChoice.containsKey(choice))
        sequenceForChoice.put(choice, randomlySelectSequence(program));

      return sequenceForChoice.get(choice);

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(SegmentChoice choice) throws FabricationException {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine SequenceBinding offset create choice with no SequenceBinding.");
    return getSequenceBinding(choice).getOffset();
  }

  @Override
  public HubContent getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public Segment.Type getType() throws FabricationException {
    if (Value.isEmpty(type))
      type = determineType();
    return type;
  }

  @Override
  public boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) throws FabricationException {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  @Override
  public boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) throws FabricationException {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  @Override
  public Boolean isInitialSegment() {
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void done() throws FabricationException {
    try {
      workbench.setSegment(workbench.getSegment().toBuilder()
        .setType(getType())
        .build());
      workbench.done();
    } catch (FabricationException | JsonApiException | ValueException e) {
      throw exception("Could not complete Segment fabrication", e);
    }
    switch (getType()) {
      case Continue:
        // transitions only once, of empty to non-empty
        log.info("[segId={}] continues main sequence create previous segments: {}",
          workbench.getSegment().getId(),
          Entities.csvIdsOf(getPreviousSegmentsWithSameMainProgram()));
        break;
      case Initial:
      case NextMain:
      case NextMacro:
        break;
    }
  }

  @Override
  public ProgramSequenceBinding randomlySelectSequenceBindingAtOffset(Program program, Long offset) throws FabricationException {
    try {
      EntityScorePicker<ProgramSequenceBinding> entityScorePicker = new EntityScorePicker<>();
      for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset)) {
        entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));
      }
      return entityScorePicker.getTop();

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public ProgramSequence randomlySelectSequence(Program program) throws FabricationException {
    try {
      EntityScorePicker<ProgramSequence> entityScorePicker = new EntityScorePicker<>();
      sourceMaterial.getAllProgramSequences().stream()
        .filter(s -> s.getProgramId().equals(program.getId()))
        .forEach(sequence -> entityScorePicker.add(sequence, Chance.normallyAround(0.0, 1.0)));
      return entityScorePicker.getTop();

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(ProgramSequence sequence, ProgramVoice voice, ProgramSequencePattern.Type patternType) throws FabricationException {
    try {
      EntityScorePicker<ProgramSequencePattern> rank = new EntityScorePicker<>();
      sourceMaterial.getAllProgramSequencePatterns().stream()
        .filter(pattern -> pattern.getProgramSequenceId().equals(sequence.getId()))
        .filter(pattern -> pattern.getProgramVoiceId().equals(voice.getId()))
        .filter(pattern -> pattern.getType() == patternType)
        .forEach(pattern ->
          rank.add(pattern, Chance.normallyAround(0.0, 1.0)));
      if (Objects.equals(0, rank.size()))
        return Optional.empty();
      return Optional.of(rank.getTop());

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public String getResultMetadataJson() throws FabricationException {
    try {
      return payloadFactory.serialize(payloadFactory.newPayload()
        .setDataOne(payloadFactory.toPayloadObject(workbench.getSegment()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentArrangements()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChords()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentMessages()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangementPicks())));

    } catch (JsonApiException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.getId());
  }

  @Override
  public boolean isDirectlyBound(Instrument instrument) {
    return boundInstrumentIds.contains(instrument.getId());
  }

  @Override
  public ProgramConfig getProgramConfig(Program program) throws ValueException {
    return new ProgramConfig(program, config);
  }

  @Override
  public InstrumentConfig getInstrumentConfig(Instrument instrument) throws ValueException {
    return new InstrumentConfig(instrument, config);
  }

  @Override
  public List<Instrument.Type> getDistinctChordVoicingTypes() throws FabricationException {
    try {
      return getSourceMaterial().getProgramSequenceChordVoicings(getCurrentMainChoice().getProgramId()).stream()
        .map(ProgramSequenceChordVoicing::getType)
        .distinct()
        .collect(Collectors.toList());

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Tuning getTuning() {
    return tuning;
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() throws FabricationException {
    return workbench.getSegmentMemes();
  }

  @Override
  public Collection<SegmentChoice> getChoices() throws FabricationException {
    return workbench.getSegmentChoices();
  }

  @Override
  public Optional<SegmentChoice> getChoice(SegmentChoiceArrangement arrangement) throws FabricationException {
    return workbench.getSegmentChoices().stream()
      .filter(choice -> arrangement.getSegmentChoiceId().equals(choice.getId()))
      .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() throws FabricationException {
    return workbench.getSegmentArrangements();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() throws FabricationException {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) throws FabricationException {
    Collection<String> choiceIds = Entities.idsOf(choices);
    return getArrangements().stream()
      .filter(arrangement -> choiceIds.contains(String.valueOf(arrangement.getSegmentChoiceId())))
      .collect(Collectors.toList());
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, Instrument.Type type) throws FabricationException {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream()
      .filter(voicing -> type.equals(voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .findAny();
  }

  /**
   Filter and map target ids of a specified type from a set of chain bindings

   @param chainBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of chain binding targets
   */
  private Set<String> targetIdsOfType(Collection<ChainBinding> chainBindings, ChainBinding.Type type) {
    return chainBindings.stream().filter(chainBinding -> chainBinding.getType().equals(type))
      .map(ChainBinding::getTargetId).collect(Collectors.toSet());
  }

  /**
   Does the program of the specified Choice have at least N more sequence binding offsets available?

   @param choice of which to check the program for next available sequence binding offsets
   @param N      more sequence offsets to check for
   @return true if N more sequence binding offsets are available
   @throws FabricationException on failure
   */
  private boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) throws FabricationException {
    try {
      if (Value.isEmpty(choice.getProgramSequenceBindingId()))
        throw exception("Cannot determine whether choice with no SequenceBinding has one more available Sequence Pattern offset");
      var sequenceBinding = getSequenceBinding(choice);

      Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
      return max.filter(aLong -> 0 <= aLong.compareTo(sequenceBinding.getOffset() + N)).isPresent();

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  /**
   Determine the type of fabricator

   @return type of fabricator
   @throws FabricationException on failure to determine
   */
  private Segment.Type determineType() throws FabricationException {
    if (isInitialSegment())
      return Segment.Type.Initial;

    // previous main choice having at least one more pattern?
    SegmentChoice previousMainChoice;
    try {
      previousMainChoice = getPreviousMainChoice();
    } catch (FabricationException e) {
      return Segment.Type.Initial;
    }

    if (Value.isSet(previousMainChoice) && hasOneMoreSequenceBindingOffset(previousMainChoice))
      return Segment.Type.Continue;

    // previous macro choice having at least two more patterns?
    SegmentChoice previousMacroChoice;
    try {
      previousMacroChoice = getPreviousMacroChoice();
    } catch (FabricationException e) {
      return Segment.Type.Initial;
    }

    if (Value.isSet(previousMacroChoice) && hasTwoMoreSequenceBindingOffsets(previousMacroChoice))
      return Segment.Type.NextMain;

    return Segment.Type.NextMacro;
  }

  /**
   General a Segment URL

   @param chain   to generate URL for
   @param segment to generate URL for
   @return URL as string
   */
  private String generateStorageKey(Chain chain, Segment segment) {
    String chainName = Strings.isNullOrEmpty(chain.getEmbedKey()) ?
      "chain" + NAME_SEPARATOR + chain.getId() :
      chain.getEmbedKey();
    String segmentName = segmentNameFormat.format(Instant.parse(segment.getBeginAt()).toEpochMilli());
    return fileStoreProvider.generateKey(chainName + NAME_SEPARATOR + segmentName);
  }

  /**
   Get a Sequence Binding for a given Choice

   @param choice to get sequence binding for
   @return Sequence Binding for the given Choice
   */
  private ProgramSequenceBinding getSequenceBinding(SegmentChoice choice) throws FabricationException {
    try {
      return sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());

    } catch (HubClientException e) {
      throw new FabricationException(e);
    }
  }

  /**
   [#255] Tuning based on root note configured in environment parameters.
   */
  private Tuning computeTuning() throws FabricationException {
    try {
      return Tuning.at(
        Note.of(tuningRootNote),
        tuningRootPitch);
    } catch (MusicalException e) {
      throw exception("Could not tune XJ!", e);
    }
  }

  /**
   Create a new CoreException prefixed with a segment id

   @param message to include in exception
   @return CoreException to throw
   */
  private FabricationException exception(String message) {
    return new FabricationException(formatLog(message));
  }

  /**
   Create a new CoreException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CoreException to throw
   */
  private FabricationException exception(String message, Exception e) {
    return new FabricationException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[segId=%s] %s", workbench.getSegment().getId(), message);
  }

  /**
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer getTimeComputer() throws FabricationException {
    double toTempo = workbench.getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo;
    try {
      Segment previous = getPreviousSegment();
      // velocity at previous segment tempo
      fromTempo = previous.getTempo();
    } catch (Exception ignored) {
      fromTempo = toTempo;
    }
    double totalBeats = workbench.getSegment().getTotal();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    if (0 == totalBeats) throw new FabricationException("Can't instantiate time computer with zero total beats!");
    if (0 == fromTempo) throw new FabricationException("Can't instantiate time computer from zero tempo!");
    if (0 == toTempo) throw new FabricationException("Can't instantiate time computer to zero tempo!");
    return timeComputerFactory.create(totalBeats, fromTempo, toTempo);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureStorageKey() {
    if (Value.isEmpty(workbench.getSegment().getStorageKey()) || workbench.getSegment().getStorageKey().isEmpty()) {
      workbench.setSegment(
        workbench.getSegment().toBuilder()
          .setStorageKey(generateStorageKey(workbench.getChain(), workbench.getSegment()))
          .build());
      log.info("[segId={}] Generated storage key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
    }
  }

}
