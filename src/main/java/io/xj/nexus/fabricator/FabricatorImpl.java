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
import io.xj.SegmentMessage;
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
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.dao.Chains;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainBindingDAO;
import io.xj.nexus.dao.ChainConfig;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.SegmentDAO;
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
import java.util.stream.Collectors;

import static io.xj.Instrument.Type.UNRECOGNIZED;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private final int workBufferAheadSeconds;
  private Map<String, InstrumentAudio> previousInstrumentAudio;
  private Segment.Type type;
  private final Chain chain;
  private final ChainConfig chainConfig;
  private final Collection<ChainBinding> chainBindings;
  private final Config config;
  private final DecimalFormat segmentNameFormat;
  private final FabricatorFactory fabricatorFactory;
  private final FileStoreProvider fileStoreProvider;
  private final HubClientAccess access;
  private final HubContent sourceMaterial;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final Map<Double, Optional<SegmentChord>> chordAtPosition;
  private final Map<Instrument.Type, NoteRange> voicingNoteRange;
  private final Map<Program.Type, Map<Instrument.Type, List<String>>> previouslyChosenProgramIds;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final Map<String, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  private final Map<String, Integer> rangeShiftOctave;
  private final Map<String, Integer> targetShift;
  private final Map<String, NoteRange> rangeForChoice;
  private final Map<String, Optional<SegmentChordVoicing>> voicingForSegmentChordInstrumentType;
  private final Map<String, Set<String>> previouslyPickedNotes;
  private final SegmentDAO segmentDAO;
  private final SegmentRetrospective retrospective;
  private final SegmentWorkbench workbench;
  private final Set<String> boundInstrumentIds;
  private final Set<String> boundProgramIds;
  private final String workTempFilePathPrefix;
  private final long startTime;
  private static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  private static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  private static final String NAME_SEPARATOR = "-";
  private static final String UNKNOWN_KEY = "unknown";
  private static final double MICROS_PER_SECOND = 1000000.0F;
  private static final double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;

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
      log.debug("[segId={}] HubClientAccess {}", segment.getId(), access);

      this.fileStoreProvider = fileStoreProvider;
      this.fabricatorFactory = fabricatorFactory;
      this.sourceMaterial = sourceMaterial;

      this.config = config;
      workTempFilePathPrefix = env.getTempFilePathPrefix();
      segmentNameFormat = new DecimalFormat(config.getString("work.segmentNameFormat"));
      workBufferAheadSeconds = config.getInt("work.bufferAheadSeconds");

      // caches
      voicingForSegmentChordInstrumentType = Maps.newHashMap();
      voicingNoteRange = Maps.newHashMap();
      rangeForChoice = Maps.newHashMap();
      rangeShiftOctave = Maps.newHashMap();
      targetShift = Maps.newHashMap();
      chordAtPosition = Maps.newHashMap();
      completeChordsForProgramSequence = Maps.newHashMap();

      // time
      startTime = System.nanoTime();
      log.debug("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainDAO.readOne(access, segment.getChainId());
      chainConfig = new ChainConfig(chain, config);
      chainBindings = chainBindingDAO.readMany(access, ImmutableList.of(chain.getId()));
      boundProgramIds = Chains.targetIdsOfType(chainBindings, ChainBinding.Type.Program);
      boundInstrumentIds = Chains.targetIdsOfType(chainBindings, ChainBinding.Type.Instrument);
      log.debug("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        chainConfig,
        CSV.prettyFrom(chainBindings, "and"));

      // setup the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(access, segment, sourceMaterial);

      // digest previous choices
      previouslyChosenProgramIds = digestPreviouslyChosenProgramIds();

      // get the current segment on the workbench
      workbench = fabricatorFactory.setupWorkbench(access, chain, segment);

      // digest previous picks
      previouslyPickedNotes = digestPreviouslyPickedNotes();

      // final pre-flight check
      ensureStorageKey();

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | ValueException e) {
      throw new NexusException("Failed to instantiate Fabricator!", e);
    }
  }

  /**
   Digest all previously chosen programs for the same main program

   @return map of program types to instrument types to list of programs chosen
   */
  private Map<Program.Type, Map<Instrument.Type, List<String>>> digestPreviouslyChosenProgramIds() {
    Map<Program.Type, Map<Instrument.Type, List<String>>> programIds = Maps.newHashMap();
    getChoicesOfPreviousSegmentsWithSameMainProgram().forEach(choice -> {
      if (!programIds.containsKey(choice.getProgramType()))
        programIds.put(choice.getProgramType(), Maps.newHashMap());
      if (!programIds.get(choice.getProgramType()).containsKey(choice.getInstrumentType()))
        programIds.get(choice.getProgramType()).put(choice.getInstrumentType(), Lists.newArrayList());
      programIds.get(choice.getProgramType()).get(choice.getInstrumentType())
        .add(choice.getProgramId());
    });

    return programIds;
  }

  /**
   Digest all previously picked events for the same main program

   @return map of program types to instrument types to list of programs chosen
   */
  private Map<String, Set<String>> digestPreviouslyPickedNotes() {
    Map<String, Set<String>> notes = Maps.newHashMap();

    getPicksOfPreviousSegmentsWithSameMainProgram()
      .forEach(pick -> {
        try {
          var key = keyEventChord(pick.getProgramSequencePatternEventId(), pick.getSegmentChordVoicingId());
          if (!notes.containsKey(key))
            notes.put(key, Sets.newHashSet());
          notes.get(key).add(pick.getNote());

        } catch (NexusException | EntityStoreException e) {
          log.warn("Can't find chord of previous event and chord id", e);
        }
      });

    return notes;
  }

  @Override
  public <N extends MessageLite> N add(N entity) throws NexusException {
    return workbench.add(entity);
  }

  @Override
  public Double computeSecondsAtPosition(double p) throws NexusException {
    return getTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public HubClientAccess getAccess() {
    return access;
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
  public Collection<SegmentMessage> getSegmentMessages() {
    return workbench.getSegmentMessages();
  }

  @Override
  public Optional<SegmentChoice> getCurrentMacroChoice() {
    return workbench.getChoiceOfType(Program.Type.Macro);
  }

  @Override
  public Optional<SegmentChoice> getCurrentMainChoice() {
    return workbench.getChoiceOfType(Program.Type.Main);
  }

  @Override
  public Optional<SegmentChoice> getCurrentRhythmChoice() {
    return workbench.getChoiceOfType(Program.Type.Rhythm);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return workbench.getChoicesOfType(Program.Type.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
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
  public Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws NexusException {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      throw new NexusException("Cannot determine whether choice with no SequenceBinding has two more available Sequence Pattern offsets");
    var sequenceBinding = getSequenceBinding(choice);
    if (sequenceBinding.isEmpty()) return 0L;

    Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding.get()).stream().max(Long::compareTo);
    if (max.isEmpty()) throw new NexusException("Cannot determine max available sequence binding offset");
    return max.get();

  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() {
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
  public Collection<SegmentChoiceArrangement> getChoiceArrangementsOfPreviousSegments() {
    Collection<SegmentChoiceArrangement> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoiceArrangements(seg));
    return out;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getChoiceArrangementPicksOfPreviousSegments() throws NexusException {
    Collection<SegmentChoiceArrangementPick> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoiceArrangementPicks(seg));
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() {
    Map<String, Collection<SegmentChoice>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentChoices(seg));
    }
    return out;
  }

  @Override
  public Collection<SegmentChoice> getChoicesOfPreviousSegmentsWithSameMainProgram() {
    Collection<SegmentChoice> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoices(seg));
    return out;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicksOfPreviousSegmentsWithSameMainProgram() {
    Collection<SegmentChoiceArrangementPick> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoiceArrangementPicks(seg));
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() {
    Map<String, Collection<SegmentChoiceArrangementPick>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentChoiceArrangementPicks(seg));
    }
    return out;
  }

  @Override
  public Map<String, InstrumentAudio> getPreviousInstrumentAudio() {
    // this map is built once from the retrospective, and from then on is modified by its accessors--
    // FUTURE it's not great ^^ that we are modifying this map externally
    if (Objects.isNull(previousInstrumentAudio))
      try {
        previousInstrumentAudio = Maps.newHashMap();
        for (SegmentChoiceArrangementPick pick : getChoiceArrangementPicksOfPreviousSegments()) {
          getSourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
            .ifPresent(audio -> previousInstrumentAudio.put(keyByVoiceTrack(pick), audio));
        }

      } catch (NexusException e) {
        log.error("Unable to build map create previous instrument audio", e);
      }

    return previousInstrumentAudio;
  }


  @Override
  public String keyByVoiceTrack(SegmentChoiceArrangementPick pick) {
    String voiceId =
      getSourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId())
        .map(event -> getSourceMaterial().getTrack(event)
          .map(ProgramVoiceTrack::getProgramVoiceId)
          .orElse(UNKNOWN_KEY))
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, voiceId, pick.getName());
  }

  @Override
  public String keyByVoiceTrack(ProgramSequencePatternEvent event) {
    String voiceId =
      getSourceMaterial().getVoice(event)
        .map(ProgramVoice::getId)
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, voiceId, getTrackName(event));
  }

  @Override
  public String keyByTrackNote(String track, String note) {
    return String.format(KEY_VOICE_NOTE_TEMPLATE, track, note);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return getSourceMaterial().getTrack(event)
      .map(ProgramVoiceTrack::getName)
      .orElse(UNKNOWN_KEY);
  }

  @Override
  public Optional<String> getPreviousVoiceInstrumentId(String voiceId) {
    try {
      return getChoicesOfPreviousSegmentsWithSameMainProgram()
        .stream()
        .filter(choice -> voiceId.equals(choice.getProgramVoiceId()))
        .map(SegmentChoice::getInstrumentId)
        .findFirst();

    } catch (Exception e) {
      log.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceId=%s", voiceId)), e);
      return Optional.empty();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() {
    var macroChoice = getCurrentMacroChoice();
    if (macroChoice.isEmpty()) return MemeIsometry.none();
    return MemeIsometry.ofMemes(toStrings(getMemesOfChoice(macroChoice.get())));
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      var previousMacroChoice =
        getPreviousMacroChoice()
          .orElseThrow(NexusException::new);
      var previousSequenceBinding =
        getSourceMaterial()
          .getProgramSequenceBinding(previousMacroChoice.getProgramSequenceBindingId())
          .orElseThrow(NexusException::new);

      var nextSequenceBinding =
        getSourceMaterial().getSequenceBindingsAtProgramOffset(
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
      log.warn("Could not get meme isometry of next sequence in previous macro", e);
      return MemeIsometry.none();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(Entities.namesOf(workbench.getSegmentMemes()));
  }

  @Override
  public Collection<SegmentMeme> getMemesOfChoice(SegmentChoice choice) {
    Collection<SegmentMeme> result = Lists.newArrayList();
    var program = getProgram(choice);
    if (program.isEmpty())
      return ImmutableList.of();
    sourceMaterial.getMemes(program.get())
      .forEach(meme -> result.add(SegmentMeme.newBuilder()
        .setName(meme.getName())
        .setSegmentId(choice.getSegmentId())
        .build()));
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = getSequenceBinding(choice);
      sequenceBinding.ifPresent(programSequenceBinding -> sourceMaterial.getMemes(programSequenceBinding)
        .forEach(meme -> result.add(SegmentMeme.newBuilder()
          .setName(meme.getName())
          .setSegmentId(choice.getSegmentId())
          .build())));
    }
    return result;
  }

  @Override
  public Long getNextSequenceBindingOffset(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;

    var sequenceBinding = getSequenceBinding(choice);
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
  public Collection<Segment> getPreviousSegmentsWithSameMainProgram() {
    return retrospective.getSegments();
  }

  @Override
  public Optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.getProgramId());

  }

  @Override
  public Optional<Program> getProgram(SegmentChoiceArrangement arrangement) {
    var choice = getChoice(arrangement);
    return choice.isPresent() ? getProgram(choice.get()) : Optional.empty();
  }

  @Override
  public Optional<SegmentChoice> getPreviousMacroChoice() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Macro);
  }

  @Override
  public Optional<SegmentChoice> getPreviousMainChoice() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Main);
  }

  @Override
  public Segment getPreviousSegment() throws NexusException {
    if (isInitialSegment() || retrospective.getPreviousSegment().isEmpty())
      throw new NexusException("Initial Segment has no previous Segment");

    return retrospective.getPreviousSegment().orElseThrow();
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public void updateSegment(Segment segment) {
    try {
      segmentDAO.update(access, segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
      log.error("Failed to update Segment", e);
    }
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return getSegmentStorageKey(getSegment().getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  @Override
  public String getSegmentOutputMetadataKey() {
    return getSegmentStorageKey(FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getChainFullOutputMetadataKey() {
    return fileStoreProvider.getChainStorageKey(Chains.getFullKey(getChainBaseKey()), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getChainOutputMetadataKey() {
    return fileStoreProvider.getChainStorageKey(getChainBaseKey(), FileStoreProvider.EXTENSION_JSON);
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
      var sequenceBinding = getSequenceBinding(choice);
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().getProgramSequenceId());
    }

    if (!sequenceForChoice.containsKey(choice))
      randomlySelectSequence(program.get())
        .ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;
    var sequenceBinding = getSequenceBinding(choice);
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0L);
  }

  @Override
  public HubContent getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public Segment.Type getType() {
    if (Value.isEmpty(type))
      type = determineType();
    return type;
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
  public Boolean isInitialSegment() {
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
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
    switch (getType()) {
      case Continue:
        // transitions only once, of empty to non-empty
        log.debug("[segId={}] continues main sequence create previous segments: {}",
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
  public Optional<ProgramSequenceBinding> randomlySelectSequenceBindingAtOffset(Program program, Long offset) {
    EntityScorePicker<ProgramSequenceBinding> entityScorePicker = new EntityScorePicker<>();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramSequence> randomlySelectSequence(Program program) {
    EntityScorePicker<ProgramSequence> entityScorePicker = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> s.getProgramId().equals(program.getId()))
      .forEach(sequence -> entityScorePicker.add(sequence, Chance.normallyAround(0.0, 1.0)));
    return entityScorePicker.getTop();

  }

  @Override
  public Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(SegmentChoice choice, ProgramSequencePattern.Type patternType) {
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
  public String getChainFullMetadataJson() throws NexusException {
    try {
      return getChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainMetadataJson() throws NexusException {
    try {
      var threshold = Instant.now().plusSeconds(workBufferAheadSeconds);
      return getChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())).stream()
        .filter(segment -> Instant.parse(segment.getBeginAt()).isBefore(threshold))
        .collect(Collectors.toList()));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
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
  public ProgramConfig getProgramConfig(Program program) throws NexusException {
    try {
      return new ProgramConfig(program, config);
    } catch (ValueException e) {
      throw new NexusException(e);
    }
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
  public List<String> getPreviouslyChosenProgramIds(Program.Type programType, Instrument.Type instrumentType) {
    if (previouslyChosenProgramIds.containsKey(programType) &&
      previouslyChosenProgramIds.get(programType).containsKey(instrumentType))
      return previouslyChosenProgramIds.get(programType).get(instrumentType);

    else return ImmutableList.of();
  }

  @Override
  public Optional<Set<String>> getPreviouslyPickedNotes(String programSequencePatternEventId, String segmentChordName) {
    try {
      var key = keyEventChord(programSequencePatternEventId, segmentChordName);
      if (previouslyPickedNotes.containsKey(key))
        return Optional.of(new HashSet<>(previouslyPickedNotes.get(key)));

    } catch (NexusException | EntityStoreException e) {
      log.warn("Can't find chord of previous event and chord id", e);
    }

    return Optional.empty();
  }

  @Override
  public Set<String> rememberPickedNotes(String programSequencePatternEventId, String chordName, Set<String> notes) {
    try {
      previouslyPickedNotes.put(keyEventChord(programSequencePatternEventId, chordName),
        new HashSet<>(notes));
    } catch (NexusException | EntityStoreException e) {
      log.warn("Can't find chord of previous event and chord id", e);
    }

    return notes;
  }

  @Override
  public double getAmplitudeForInstrumentType(SegmentChoiceArrangementPick pick) {
    return switch (getInstrument(pick).map(Instrument::getType).orElse(UNRECOGNIZED)) {
      case Percussive -> chainConfig.getDubMasterVolumeInstrumentTypePercussive();
      case Bass -> chainConfig.getDubMasterVolumeInstrumentTypeBass();
      case Pad -> chainConfig.getDubMasterVolumeInstrumentTypePad();
      case Sticky -> chainConfig.getDubMasterVolumeInstrumentTypeSticky();
      case Stripe -> chainConfig.getDubMasterVolumeInstrumentTypeStripe();
      case Stab -> chainConfig.getDubMasterVolumeInstrumentTypeStab();
      default -> 1.0;
    };
  }

  @Override
  public NoteRange computeVoicingNoteRange(Instrument.Type type) {
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
  public Optional<Instrument> getInstrument(SegmentChoiceArrangementPick pick) {
    return sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
      .flatMap(audio -> sourceMaterial.getInstrument(audio.getInstrumentId()));
  }

  @Override
  public Optional<Instrument> getInstrument(SegmentChoiceArrangement arrangement) {
    return getChoice(arrangement).stream()
      .flatMap(choice -> sourceMaterial.getInstrument(choice.getInstrumentId()).stream())
      .findFirst();
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return workbench.getSegmentMemes();
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return workbench.getSegmentChoices();
  }

  @Override
  public Optional<SegmentChoice> getChoice(SegmentChoiceArrangement arrangement) {
    return workbench.getSegmentChoices().stream()
      .filter(choice -> arrangement.getSegmentChoiceId().equals(choice.getId()))
      .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return workbench.getSegmentArrangements();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) {
    Collection<String> choiceIds = Entities.idsOf(choices);
    return getArrangements().stream()
      .filter(arrangement -> choiceIds.contains(String.valueOf(arrangement.getSegmentChoiceId())))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() {
    return workbench.getSegmentChords();
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
  public Collection<String> getVoicingNotes(SegmentChord chord, Instrument.Type type) {
    var key = String.format("%s__%s", chord.getId(), type);

    if (!voicingForSegmentChordInstrumentType.containsKey(key)) {
      voicingForSegmentChordInstrumentType.put(key, workbench.getSegmentChordVoicings().stream()
        .filter(v -> type.equals(v.getType()))
        .filter(v -> Objects.equals(chord.getId(), v.getSegmentChordId()))
        .findAny());
    }

    return voicingForSegmentChordInstrumentType.get(key).stream()
      .flatMap(voicing -> CSV.split(voicing.getNotes()).stream())
      .collect(Collectors.toList());
  }

  @Override
  public Collection<String> getNotes(SegmentChordVoicing voicing) {
    return new ArrayList<>(CSV.split(voicing.getNotes()));
  }

  @Override
  public NoteRange computeProgramRange(String programId, Instrument.Type instrumentType) {
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
  public boolean continuesMacroProgram() {
    return
      Segment.Type.Continue.equals(getType()) ||
        Segment.Type.NextMain.equals(getType());
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return false;
    var sequenceBinding = getSequenceBinding(choice);

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
  public Segment.Type determineType() {
    if (isInitialSegment())
      return Segment.Type.Initial;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getPreviousMainChoice();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get()))
      return Segment.Type.Continue;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getPreviousMacroChoice();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return Segment.Type.NextMain;

    return Segment.Type.NextMacro;
  }

  @Override
  public int computeRangeShiftOctaves(Instrument.Type type, NoteRange sourceRange, NoteRange targetRange) {
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
  public int computeTargetShift(Key fromKey, Chord toChord) {
    if (!fromKey.isPresent()) return 0;
    var key = String.format("%s__%s", fromKey, toChord.toString());
    if (!targetShift.containsKey(key))
      targetShift.put(key, fromKey.getRoot().delta(toChord.getSlashRoot()));

    return targetShift.get(key);
  }

  @Override
  public double getAudioVolume(SegmentChoiceArrangementPick pick) {
    return getSourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
      .stream().map(InstrumentAudio::getVolume)
      .findAny()
      .orElse(1.0);
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

  /**
   @return Chain base key
   */
  private String getChainBaseKey() {
    return
      Strings.isNullOrEmpty(getChain().getEmbedKey()) ?
        String.format("chain-%s", getChainId())
        : getChain().getEmbedKey();
  }

  /**
   Compute the lowest optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return lowest optimal range shift octaves
   */
  private Integer computeLowestOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) {
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int d = targetRange.getLow().orElseThrow()
        .delta(sourceRange.getLow()
          .orElseThrow()
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
  private Integer computeMedianOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) {
    if (sourceRange.getLow().isEmpty() ||
      sourceRange.getHigh().isEmpty() ||
      targetRange.getLow().isEmpty() ||
      targetRange.getHigh().isEmpty())
      return 0;
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int dLow = targetRange.getLow().orElseThrow()
        .delta(sourceRange.getLow().orElseThrow().shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow()
        .delta(sourceRange.getHigh().orElseThrow().shiftOctave(o));
      if (0 <= dLow && 0 >= dHigh && Math.abs(o) < baselineDelta) {
        baselineDelta = Math.abs(o);
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   Collection Strings from collection of of Segment Memes

   @param memes to get strings of
   @return strings
   */
  private Collection<String> toStrings(Collection<SegmentMeme> memes) {
    return memes.stream().map(SegmentMeme::getName).collect(Collectors.toList());
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
  private Optional<ProgramSequenceBinding> getSequenceBinding(SegmentChoice choice) {
    return sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
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
  private String getChainMetadataJson(Collection<Segment> segments) throws NexusException {
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
  private String keyEventChord(String eventId, String chordName) throws NexusException, EntityStoreException {
    return String.format("%s__%s", chordName, eventId);
  }

  /**
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer getTimeComputer() throws NexusException {
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
          .setStorageKey(generateStorageKey(workbench.getChain(), workbench.getSegment()))
          .build());
      log.debug("[segId={}] Generated storage key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
    }
  }

}
