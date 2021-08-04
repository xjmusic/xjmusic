// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.common.InstrumentConfig;
import io.xj.lib.entity.common.ProgramConfig;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainBindingDAO;
import io.xj.nexus.dao.ChainConfig;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.Chains;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.swing.text.html.Option;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final double MICROS_PER_SECOND = 1000000.0F;
  private static final double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;
  private static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  private static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  private static final String NAME_SEPARATOR = "-";
  private static final String UNKNOWN_KEY = "unknown";
  private final Logger LOG = LoggerFactory.getLogger(FabricatorImpl.class);
  private final Chain chain;
  private final ChainConfig chainConfig;
  private final Collection<ChainBinding> chainBindings;
  private final Config config;
  private final DecimalFormat segmentNameFormat;
  private final FabricatorFactory fabricatorFactory;
  private final FileStoreProvider fileStoreProvider;
  private final HubClientAccess access;
  private final HubContent sourceMaterial;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final long startTime;
  private final Map<Double, Optional<SegmentChord>> chordAtPosition;
  private final Map<Instrument.Type, NoteRange> voicingNoteRange;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final Map<String, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  private final Map<String, Integer> rangeShiftOctave;
  private final Map<String, Integer> targetShift;
  private final Map<String, NoteRange> rangeForChoice;
  private final Map<String, Set<String>> preferredNotes;
  private final SegmentDAO segmentDAO;
  private final SegmentRetrospective retrospective;
  private final SegmentWorkbench workbench;
  private final Set<String> boundInstrumentIds;
  private final Set<String> boundProgramIds;
  private final String workTempFilePathPrefix;
  private final Map<String, InstrumentAudio> preferredAudios;
  private Segment.Type type;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    @Assisted("segment") Segment segment,
    Config config,
    Environment env,
    ChainDAO chainDAO,
    ChainBindingDAO chainBindingDAO,
    FileStoreProvider fileStoreProvider,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) throws NexusException {
    this.segmentDAO = segmentDAO;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    try {
      // FUTURE: [#165815496] Chain fabrication access control
      this.access = access;
      LOG.debug("[segId={}] HubClientAccess {}", segment.getId(), access);

      this.fileStoreProvider = fileStoreProvider;
      this.fabricatorFactory = fabricatorFactory;
      this.sourceMaterial = sourceMaterial;

      this.config = config;
      workTempFilePathPrefix = env.getTempFilePathPrefix();
      segmentNameFormat = new DecimalFormat(config.getString("work.segmentNameFormat"));
      workBufferAheadSeconds = config.getInt("work.bufferAheadSeconds");
      workBufferBeforeSeconds = config.getInt("work.bufferBeforeSeconds");

      // caches
      voicingNoteRange = Maps.newHashMap();
      rangeForChoice = Maps.newHashMap();
      rangeShiftOctave = Maps.newHashMap();
      targetShift = Maps.newHashMap();
      chordAtPosition = Maps.newHashMap();
      completeChordsForProgramSequence = Maps.newHashMap();

      // time
      startTime = System.nanoTime();
      LOG.debug("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainDAO.readOne(access, segment.getChainId());
      chainConfig = new ChainConfig(chain, config);
      chainBindings = chainBindingDAO.readMany(access, ImmutableList.of(chain.getId()));
      boundProgramIds = Chains.targetIdsOfType(chainBindings, ChainBinding.Type.Program);
      boundInstrumentIds = Chains.targetIdsOfType(chainBindings, ChainBinding.Type.Instrument);
      LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        chainConfig,
        CSV.prettyFrom(chainBindings, "and"));

      // setup the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(access, segment, sourceMaterial);

      // digest previous picks
      preferredNotes = computePreferredNotes();

      // digest previous instrument audio
      preferredAudios = computePreferredInstrumentAudio();

      // get the current segment on the workbench
      workbench = fabricatorFactory.setupWorkbench(access, chain, segment);

      // final pre-flight check
      ensureStorageKey();

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | ValueException e) {
      throw new NexusException("Failed to instantiate Fabricator!", e);
    }
  }

  @Override
  public <N extends MessageLite> N add(N entity) throws NexusException {
    return workbench.add(entity);
  }

  @Override
  public Program addMemes(Program p) throws NexusException {
    for (ProgramMeme meme : sourceMaterial().getMemes(p))
      add(SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(getSegment().getId())
        .setName(Text.toMeme(meme.getName()))
        .build());
    return p;
  }

  @Override
  public ProgramSequenceBinding addMemes(ProgramSequenceBinding psb) throws NexusException {
    for (ProgramSequenceBindingMeme meme : sourceMaterial().getMemes(psb))
      add(SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(getSegment().getId())
        .setName(Text.toMeme(meme.getName()))
        .build());
    return psb;
  }

  @Override
  public void done() throws NexusException {
    try {
      workbench.setSegment(workbench.getSegment().toBuilder()
        .setType(getType())
        .build());
      workbench.done();
    } catch (JsonApiException | ValueException e) {
      throw new NexusException("Could not complete Segment fabrication", e);
    }
  }

  @Override
  public HubClientAccess getAccess() {
    return access;
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return workbench.getSegmentArrangements();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) {
    Collection<String> choiceIds = Entities.idsOf(choices);
    return getArrangements().stream()
      .filter(arrangement -> choiceIds.contains(String.valueOf(arrangement.getSegmentChoiceId())))
      .collect(Collectors.toList());
  }

  @Override
  public double getAudioVolume(SegmentChoiceArrangementPick pick) {
    return sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
      .stream().map(InstrumentAudio::getVolume)
      .findAny()
      .orElse(1.0);
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
  public String getChainMetadataFullJson() throws NexusException {
    try {
      return computeChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainMetadataFullKey() {
    return fileStoreProvider.getChainStorageKey(Chains.getFullKey(computeChainBaseKey()), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getChainMetadataJson() throws NexusException {
    try {
      var now = Instant.now();
      var beforeThreshold = now.plusSeconds(workBufferAheadSeconds);
      var afterThreshold = now.minusSeconds(workBufferBeforeSeconds);
      return computeChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())).stream()
        .filter(segment ->
          Instant.parse(segment.getBeginAt()).isBefore(beforeThreshold)
            && Instant.parse(segment.getEndAt()).isAfter(afterThreshold))
        .collect(Collectors.toList()));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainMetadataKey() {
    return fileStoreProvider.getChainStorageKey(computeChainBaseKey(), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return workbench.getSegmentChoices();
  }

  @Override
  public Optional<SegmentChord> getChordAt(double position) {
    if (!chordAtPosition.containsKey(position)) {
      Optional<SegmentChord> foundChord = Optional.empty();
      Double foundPosition = null;

      // we assume that these entities are in order of position ascending
      for (SegmentChord segmentChord : workbench.getSegmentChords()) {
        // if it's a better match (or no match has yet been found) then use it
        if (Objects.isNull(foundPosition) ||
          (segmentChord.getPosition() > foundPosition && segmentChord.getPosition() <= position)) {
          foundPosition = segmentChord.getPosition();
          foundChord = Optional.of(segmentChord);
        }
      }
      chordAtPosition.put(position, foundChord);
    }

    return chordAtPosition.get(position);
  }

  @Override
  public Optional<SegmentChoice> getCurrentMainChoice() {
    return workbench.getChoiceOfType(Program.Type.Main);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return workbench.getChoicesOfType(Program.Type.Rhythm);
  }

  @Override
  public Optional<SegmentChoice> getCurrentRhythmChoice() {
    return workbench.getChoiceOfType(Program.Type.Rhythm);
  }

  @Override
  public List<Instrument.Type> getDistinctChordVoicingTypes() {
    var mainChoice = getCurrentMainChoice();
    if (mainChoice.isEmpty()) return ImmutableList.of();
    var voicings = sourceMaterial
      .getProgramSequenceChordVoicings(mainChoice.get().getProgramId());
    return voicings.stream()
      .map(ProgramSequenceChordVoicing::getType)
      .distinct()
      .collect(Collectors.toList());
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public String getFullQualityAudioOutputFilePath() {
    return String.format("%s%s", workTempFilePathPrefix, getSegmentOutputWaveformKey());
  }

  @Override
  public Optional<Instrument> getInstrument(SegmentChoiceArrangementPick pick) {
    return sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
      .flatMap(audio -> sourceMaterial.getInstrument(audio.getInstrumentId()));
  }

  @Override
  public InstrumentConfig getInstrumentConfig(Instrument instrument) throws NexusException {
    try {
      return new InstrumentConfig(instrument, config);
    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceOfSameMainProgram(ProgramVoice voice) {
    try {
      return retrospective.getChoices()
        .stream()
        .filter(choice -> {
          var candidateVoice = sourceMaterial.getProgramVoice(choice.getProgramVoiceId());
          return candidateVoice.isPresent()
            && candidateVoice.get().getName().equals(voice.getName())
            && candidateVoice.get().getType().equals(voice.getType());
        })
        .findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceName=%s", voice.getName())), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceOfSameMainProgram(Instrument.Type instrumentType) {
    try {
      return switch (getSegment().getType()) {
        case Pending, UNRECOGNIZED -> Optional.empty();
        case Initial, NextMain, NextMacro -> retrospective.getChoices()
          .stream()
          .filter(choice ->
            instrumentType.equals(choice.getInstrumentType())
              && Segments.DELTA_UNLIMITED == choice.getDeltaOut())
          .findFirst();
        case Continue -> retrospective.getChoices()
          .stream()
          .filter(choice -> instrumentType.equals(choice.getInstrumentType()))
          .findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceOfSameMainProgram(Program.Type programType) {
    try {
      return switch (getSegment().getType()) {
        case Pending, UNRECOGNIZED, Initial, NextMain, NextMacro -> Optional.empty();
        case Continue -> retrospective.getChoices()
          .stream()
          .filter(choice -> programType.equals(choice.getProgramType()))
          .findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for programType=%s", programType)), e);
      return Optional.empty();
    }
  }

  @Override
  public String getKeyByVoiceTrack(SegmentChoiceArrangementPick pick) {
    String voiceId =
      sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId())
        .map(event -> sourceMaterial().getTrack(event)
          .map(ProgramVoiceTrack::getProgramVoiceId)
          .orElse(UNKNOWN_KEY))
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, voiceId, pick.getName());
  }

  @Override
  public Key getKeyForChoice(SegmentChoice choice) throws NexusException {
    Optional<Program> program = getProgram(choice);
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !Strings.isNullOrEmpty(sequence.get().getKey()))
        return Key.of(sequence.get().getKey());
    }

    return Key.of(program
      .orElseThrow(() -> new NexusException("Cannot get key for nonexistent choice!"))
      .getKey());
  }

  @Override
  public Optional<SegmentChoice> getMacroChoiceOfPreviousSegment() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Macro);
  }

  @Override
  public Optional<SegmentChoice> getMainChoiceOfPreviousSegment() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Main);
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      var previousMacroChoice =
        getMacroChoiceOfPreviousSegment()
          .orElseThrow(NexusException::new);
      var previousSequenceBinding =
        sourceMaterial()
          .getProgramSequenceBinding(previousMacroChoice.getProgramSequenceBindingId())
          .orElseThrow(NexusException::new);

      var nextSequenceBinding =
        sourceMaterial().getSequenceBindingsAtProgramOffset(
          previousMacroChoice.getProgramId(),
          previousSequenceBinding.getOffset() + 1);

      return MemeIsometry.ofMemes(Streams.concat(
        sourceMaterial.getProgramMemes(previousMacroChoice.getProgramId())
          .stream()
          .map(ProgramMeme::getName),
        nextSequenceBinding.stream()
          .flatMap(programSequenceBinding -> sourceMaterial.getMemes(programSequenceBinding)
            .stream()
            .map(ProgramSequenceBindingMeme::getName))
      ).collect(Collectors.toList()));

    } catch (NexusException e) {
      LOG.warn("Could not get meme isometry of next sequence in previous macro", e);
      return MemeIsometry.none();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(Entities.namesOf(workbench.getSegmentMemes()));
  }

  @Override
  public Long getNextSequenceBindingOffset(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;

    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Long offset = null;
    if (sequenceBinding.isEmpty()) return 0L;
    for (Long availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : Long.valueOf(0L);

  }

  @Override
  public Collection<String> getNotes(SegmentChordVoicing voicing) {
    return new ArrayList<>(CSV.split(voicing.getNotes()));
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
  public Collection<InstrumentAudio> getPickedAudios() {
    Collection<InstrumentAudio> audios = Lists.newArrayList();
    for (SegmentChoiceArrangementPick pick : workbench.getSegmentChoiceArrangementPicks())
      sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
        .ifPresent(audios::add);
    return audios;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public Optional<InstrumentAudio> getPreferredAudio(ProgramSequencePatternEvent event, String note) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, event.getProgramVoiceTrackId(), note);

    if (preferredAudios.containsKey(key)) return Optional.of(preferredAudios.get(key));

    return Optional.empty();
  }

  @Override
  public Optional<Set<String>> getPreferredNotes(String eventId, String chordName) {
    try {
      var key = computeEventChordKey(eventId, chordName);
      if (preferredNotes.containsKey(key))
        return Optional.of(new HashSet<>(preferredNotes.get(key)));

    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't find chord of previous event and chord id", e);
    }

    return Optional.empty();
  }

  @Override
  public Optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.getProgramId());
  }

  @Override
  public ProgramConfig getProgramConfig(Program program) throws NexusException {
    try {
      return new ProgramConfig(program, config);
    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Collection<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence) {
    if (!completeChordsForProgramSequence.containsKey(programSequence.getId())) {
      Map<Double, ProgramSequenceChord> chordForPosition = Maps.newHashMap();
      Map<Double, Integer> validVoicingsForPosition = Maps.newHashMap();
      for (ProgramSequenceChord chord : sourceMaterial.getChords(programSequence)) {
        int validVoicings = sourceMaterial.getVoicings(chord)
          .stream().map(V -> CSV.split(V.getNotes()).size()).reduce(0, Integer::sum);
        if (!validVoicingsForPosition.containsKey(chord.getPosition()) ||
          validVoicingsForPosition.get(chord.getPosition()) < validVoicings) {
          validVoicingsForPosition.put(chord.getPosition(), validVoicings);
          chordForPosition.put(chord.getPosition(), chord);
        }
      }
      completeChordsForProgramSequence.put(programSequence.getId(), chordForPosition.values());
    }

    return completeChordsForProgramSequence.get(programSequence.getId());
  }

  @Override
  public NoteRange getProgramRange(String programId, Instrument.Type instrumentType) {
    var key = String.format("%s__%s", programId, instrumentType);

    if (!rangeForChoice.containsKey(key)) {
      rangeForChoice.put(key,
        new NoteRange(
          sourceMaterial.getEvents(programId)
            .stream()
            .filter(event ->
              sourceMaterial.getVoice(event)
                .map(voice -> voice.getType().equals(instrumentType))
                .orElse(false) &&
                !Note.of(event.getNote()).getPitchClass().equals(PitchClass.None))
            .flatMap(programSequencePatternEvent ->
              CSV.split(programSequencePatternEvent.getNote())
                .stream())
            .collect(Collectors.toList())
        ));
    }

    return rangeForChoice.get(key);
  }

  @Override
  public int getProgramRangeShiftOctaves(Instrument.Type type, NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var key = String.format("%s__%s__%s", type,
      sourceRange.toString(AdjSymbol.None), targetRange.toString(AdjSymbol.None));

    if (!rangeShiftOctave.containsKey(key))
      switch (type) {

        case Bass:
          rangeShiftOctave.put(key, computeLowestOptimalRangeShiftOctaves(sourceRange, targetRange));
          break;

        case Percussive:
          return 0;

        case Pad:
        case Sticky:
        case Stripe:
        case Stab:
        default:
          rangeShiftOctave.put(key, computeMedianOptimalRangeShiftOctaves(sourceRange, targetRange));
          break;
      }

    return rangeShiftOctave.get(key);
  }

  @Override
  public int getProgramTargetShift(Key fromKey, Chord toChord) {
    if (!fromKey.isPresent()) return 0;
    var key = String.format("%s__%s", fromKey, toChord.toString());
    if (!targetShift.containsKey(key))
      targetShift.put(key, fromKey.getRoot().delta(toChord.getSlashRoot()));

    return targetShift.get(key);
  }

  @Override
  public Program.Type getProgramType(ProgramVoice voice) throws NexusException {
    return sourceMaterial.getProgram(voice.getProgramId())
      .orElseThrow(() -> new NexusException("Could not get program!"))
      .getType();
  }

  @Override
  public NoteRange getProgramVoicingNoteRange(Instrument.Type type) {
    if (!voicingNoteRange.containsKey(type)) {
      voicingNoteRange.put(type, new NoteRange(workbench.getSegmentChordVoicings()
        .stream()
        .filter(segmentChordVoicing -> segmentChordVoicing.getType().equals(type))
        .flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream())
        .collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    EntityScorePicker<ProgramSequence> entityScorePicker = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> s.getProgramId().equals(program.getId()))
      .forEach(sequence -> entityScorePicker.add(sequence, Chance.normallyAround(0.0, 1.0)));
    return entityScorePicker.getTop();

  }

  @Override
  public Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Long offset) {
    EntityScorePicker<ProgramSequenceBinding> entityScorePicker = new EntityScorePicker<>();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramVoice> getRandomlySelectedVoiceForProgramId(String programId, Collection<String> excludeVoiceIds) {
    EntityScorePicker<ProgramVoice> entityScorePicker = new EntityScorePicker<>();
    for (ProgramVoice sequenceBinding : sourceMaterial.getAllProgramVoices()
      .stream().filter(programVoice -> programId.equals(programVoice.getProgramId())
        && !excludeVoiceIds.contains(programVoice.getId()))
      .collect(Collectors.toList()))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice, ProgramSequencePattern.Type patternType) {
    EntityScorePicker<ProgramSequencePattern> rank = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequencePatterns().stream()
      .filter(pattern -> pattern.getProgramSequenceId().equals(choice.getProgramSequenceId()))
      .filter(pattern -> pattern.getProgramVoiceId().equals(choice.getProgramVoiceId()))
      .filter(pattern -> pattern.getType() == patternType)
      .forEach(pattern ->
        rank.add(pattern, Chance.normallyAround(0.0, 1.0)));
    if (Objects.equals(0, rank.size()))
      return Optional.empty();
    return rank.getTop();
  }

  @Override
  public Double getSecondsAtPosition(double p) throws NexusException {
    return buildTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() {
    return workbench.getSegmentChords();
  }

  @Override
  public String getSegmentMetadataJson() throws NexusException {
    try {
      return jsonapiPayloadFactory.serialize(jsonapiPayloadFactory.newJsonapiPayload()
        .setDataOne(jsonapiPayloadFactory.toPayloadObject(workbench.getSegment()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentArrangements()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChords()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMessages())));

    } catch (JsonApiException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getSegmentOutputMetadataKey() {
    return getSegmentStorageKey(FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return getSegmentStorageKey(getSegment().getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  @Override
  public String getSegmentStorageKey(String extension) {
    return fileStoreProvider.getSegmentStorageKey(getSegment().getStorageKey(), extension);
  }

  @Override
  public Optional<ProgramSequence> getSequence(SegmentChoice choice) {
    Optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return Optional.empty();
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().getProgramSequenceId());
    }

    if (!sequenceForChoice.containsKey(choice))
      getRandomlySelectedSequence(program.get())
        .ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0L);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrack(event)
      .map(ProgramVoiceTrack::getName)
      .orElse(UNKNOWN_KEY);
  }

  @Override
  public Segment.Type getType() throws NexusException {
    if (Value.isEmpty(type))
      type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, Instrument.Type type) {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream()
      .filter(voicing -> type.equals(voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .findAny();
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());

    if (sequenceBinding.isEmpty())
      return false;
    List<Long> avlOfs = ImmutableList.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

    // if we locate the target and still have two offsets remaining, result is true
    for (int i = 0; i < avlOfs.size(); i++)
      if (avlOfs.get(i).equals(sequenceBinding.get().getOffset()) && i < avlOfs.size() - N)
        return true;

    return false;
  }

  @Override
  public boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  @Override
  public boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  @Override
  public boolean isContinuationOfMacroProgram() throws NexusException {
    return
      Segment.Type.Continue.equals(getType()) ||
        Segment.Type.NextMain.equals(getType());
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
  public Boolean isInitialSegment() {
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public Set<String> rememberPickedNotes(String programSequencePatternEventId, String chordName, Set<String> notes) {
    try {
      preferredNotes.put(computeEventChordKey(programSequencePatternEventId, chordName),
        new HashSet<>(notes));
    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't find chord of previous event and chord id", e);
    }

    return notes;
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void updateSegment(Segment segment) {
    try {
      segmentDAO.update(access, segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
      LOG.error("Failed to update Segment", e);
    }
  }

  @Override
  public SegmentRetrospective retrospective() {
    return retrospective;
  }

  @Override
  public void setPreferredAudio(ProgramSequencePatternEvent event, String note, InstrumentAudio instrumentAudio) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, event.getProgramVoiceTrackId(), note);

    preferredAudios.put(key, instrumentAudio);
  }

  @Override
  public HubContent sourceMaterial() {
    return sourceMaterial;
  }

  /**
   @return Chain base key
   */
  private String computeChainBaseKey() {
    return
      Strings.isNullOrEmpty(getChain().getEmbedKey()) ?
        String.format("chain-%s", chain.getId())
        : getChain().getEmbedKey();
  }

  /**
   Compute the lowest optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return lowest optimal range shift octaves
   */
  private Integer computeLowestOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int d = targetRange.getLow().orElseThrow(() -> new NexusException("can't get low end of target range"))
        .delta(sourceRange.getLow()
          .orElse(Note.atonal())
          .shiftOctave(o));
      if (0 <= d && d < baselineDelta) {
        baselineDelta = d;
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   Compute the median optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return median optimal range shift octaves
   */
  private Integer computeMedianOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    if (sourceRange.getLow().isEmpty() ||
      sourceRange.getHigh().isEmpty() ||
      targetRange.getLow().isEmpty() ||
      targetRange.getHigh().isEmpty())
      return 0;
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int dLow = targetRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of target range"))
        .delta(sourceRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of source range")).shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of target range"))
        .delta(sourceRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of source range")).shiftOctave(o));
      if (0 <= dLow && 0 >= dHigh && Math.abs(o) < baselineDelta) {
        baselineDelta = Math.abs(o);
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   General a Segment URL

   @param chain   to generate URL for
   @param segment to generate URL for
   @return URL as string
   */
  private String computeStorageKey(Chain chain, Segment segment) {
    String chainName = Strings.isNullOrEmpty(chain.getEmbedKey()) ?
      "chain" + NAME_SEPARATOR + chain.getId() :
      chain.getEmbedKey();
    String segmentName = segmentNameFormat.format(Instant.parse(segment.getBeginAt()).toEpochMilli());
    return fileStoreProvider.generateKey(chainName + NAME_SEPARATOR + segmentName);
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
   Get the Chain Metadata JSON file from a set of segments

   @param segments to include in metadata JSON
   @return metadata JSON
   @throws NexusException on failure
   */
  private String computeChainMetadataJson(Collection<Segment> segments) throws NexusException {
    try {
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(jsonapiPayloadFactory.toPayloadObject(chain));
      for (ChainBinding binding : chainBindings)
        jsonapiPayload.addToIncluded(jsonapiPayloadFactory.toPayloadObject(binding));
      for (Segment segment : segments)
        jsonapiPayload.addToIncluded(jsonapiPayloadFactory.toPayloadObject(segment));
      return jsonapiPayloadFactory.serialize(jsonapiPayload);

    } catch (JsonApiException e) {
      throw new NexusException(e);
    }
  }

  /**
   Key for a chord + event pairing

   @param eventId   to get key for
   @param chordName to get key for
   @return key for chord + event
   */
  private String computeEventChordKey(String eventId, String chordName) throws NexusException, EntityStoreException {
    return String.format("%s__%s", chordName, eventId);
  }

  /**
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer buildTimeComputer() throws NexusException {
    double toTempo = workbench.getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo = retrospective.getPreviousSegment().isPresent() ? retrospective.getPreviousSegment().get().getTempo() : toTempo;
    double totalBeats = workbench.getSegment().getTotal();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    if (0 == totalBeats) throw new NexusException("Can't instantiate time computer with zero total beats!");
    if (0 == fromTempo) throw new NexusException("Can't instantiate time computer from zero tempo!");
    if (0 == toTempo) throw new NexusException("Can't instantiate time computer to zero tempo!");
    return fabricatorFactory.createTimeComputer(totalBeats, fromTempo, toTempo);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureStorageKey() {
    if (Value.isEmpty(workbench.getSegment().getStorageKey()) || workbench.getSegment().getStorageKey().isEmpty()) {
      workbench.setSegment(
        workbench.getSegment().toBuilder()
          .setStorageKey(computeStorageKey(workbench.getChain(), workbench.getSegment()))
          .build());
      LOG.debug("[segId={}] Generated storage key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
    }
  }

  /**
   Compute the type of the current segment

   @return type of the current segment
   */
  private Segment.Type computeType() throws NexusException {
    if (isInitialSegment())
      return Segment.Type.Initial;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getMainChoiceOfPreviousSegment();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get())
      && getChainConfig().getMainProgramLengthMaxDelta() > getPreviousSegmentDelta())
      return Segment.Type.Continue;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return Segment.Type.NextMain;

    return Segment.Type.NextMacro;
  }

  /**
   Get the delta of the previous segment

   @return delta from previous segment
   */
  private int getPreviousSegmentDelta() throws NexusException {
    return retrospective.getPreviousSegment()
      .orElseThrow(() -> new NexusException("Failed to get previous segment!"))
      .getDelta();
  }

  /**
   Digest all previously picked events for the same main program

   @return map of program types to instrument types to list of programs chosen
   */
  private Map<String, Set<String>> computePreferredNotes() {
    Map<String, Set<String>> notes = Maps.newHashMap();

    retrospective.getPicks()
      .forEach(pick -> {
        try {
          var key = computeEventChordKey(pick.getProgramSequencePatternEventId(), pick.getSegmentChordVoicingId());
          if (!notes.containsKey(key))
            notes.put(key, Sets.newHashSet());
          notes.get(key).add(pick.getNote());

        } catch (NexusException | EntityStoreException e) {
          LOG.warn("Can't find chord of previous event and chord id", e);
        }
      });

    return notes;
  }

  /**
   Compute the preferred instrument audio

   @return preferred instrument audio
   */
  private Map<String, InstrumentAudio> computePreferredInstrumentAudio() {
    Map<String, InstrumentAudio> audios = Maps.newHashMap();

    retrospective.getPicks()
      .forEach(pick ->
        sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
          .ifPresent(audio -> audios.put(getKeyByVoiceTrack(pick), audio)));

    return audios;
  }
}
