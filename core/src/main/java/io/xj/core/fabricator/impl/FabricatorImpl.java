// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.fabricator.TimeComputer;
import io.xj.core.fabricator.TimeComputerFactory;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.isometry.Isometry;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.entity.Meme;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.transport.CSV;
import io.xj.music.Chord;
import io.xj.music.MusicalException;
import io.xj.music.Note;
import io.xj.music.Tuning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class FabricatorImpl implements Fabricator {
  private static final double NANOS_PER_SECOND = 1000000000.0;
  private final Access access;
  private final AmazonProvider amazonProvider;
  private final Chain chain;
  private final Collection<Segment> cachedPreviousSegmentsWithThisMainSequence = Lists.newArrayList();

  private final Collection<Segment> previousSegmentsWithSameMainProgram;

  private final Ingest sourceMaterial;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final long startTime;
  private final Map<String, Segment> segmentByOffset = Maps.newHashMap();
  private final Map<UUID, BigInteger> instrumentIdForAudioUUID;
  private final Map<UUID, Collection<Arrangement>> presetChoiceArrangements = Maps.newHashMap();
  private final Map<Choice, Sequence> sequenceForChoice = Maps.newHashMap();
  private final Segment segment;
  private final SegmentDAO segmentDAO;
  private final TimeComputerFactory timeComputerFactory;
  private final Tuning tuning;
  private FabricatorType type;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("segment") Segment segment,
    IngestCacheProvider ingestProvider,
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    TimeComputerFactory timeComputerFactory,
    AmazonProvider amazonProvider
  ) throws CoreException {
    this.segment = segment;
    this.amazonProvider = amazonProvider;
    this.segmentDAO = segmentDAO;
    this.timeComputerFactory = timeComputerFactory;

    // FUTURE: [#165815496] Chain fabrication access control
    access = Access.internal();
    log.info("[segId={}] Access {}", segment.getId(), access);

    // tuning
    tuning = computeTuning();
    log.info("[segId={}] Tuning {}", segment.getId(), tuning);

    // time
    startTime = System.nanoTime();
    log.info("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

    // read the chain
    chain = chainDAO.readOne(access, segment.getChainId());
    log.info("[segId={}] Chain {}", segment.getId(), chain);

    // read the source material
    sourceMaterial = ingestProvider.ingest(access, chain.getBindings());
    log.info("[segId={}] SourceMaterial {}", segment.getId(), sourceMaterial);

    // cache additional knowledge
    instrumentIdForAudioUUID = buildInstrumentIdForAudioUUID();
    log.info("[segId={}] InstrumentIdForAudioUUID {}", segment.getId(), instrumentIdForAudioUUID);

    previousSegmentsWithSameMainProgram = buildPreviousSegmentsWithSameMainSequence();
    log.info("[segId={}] PreviousSegmentsWithSameMainProgram {}", segment.getId(), previousSegmentsWithSameMainProgram);

    // final pre-flight check
    ensureWaveformKey();
  }

  @Override
  public Arrangement add(Arrangement arrangement) {
    return getSegment().add(arrangement);
  }

  @Override
  public Choice add(Choice choice) {
    return getSegment().add(choice);
  }

  @Override
  public Pick add(Pick pick) {
    return getSegment().add(pick);
  }

  @Override
  public SegmentChord add(SegmentChord segmentChord) {
    return getSegment().add(segmentChord);
  }

  @Override
  public SegmentMeme add(SegmentMeme segmentMeme) {
    return getSegment().add(segmentMeme);
  }

  @Override
  public SegmentMessage add(SegmentMessage segmentMessage) {
    return getSegment().add(segmentMessage);
  }

  @Override
  public Double computeSecondsAtPosition(double p) {
    return getTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
  public Map<ChainConfigType, ChainConfig> getAllChainConfigs() {
    Map<ChainConfigType, ChainConfig> out = Maps.newHashMap();
    getChain().getConfigs().forEach(chainConfig -> out.put(chainConfig.getType(), chainConfig));
    return out;
  }

  @Override
  public Collection<Audio> getPickedAudios() throws CoreException {
    Collection<Audio> audios = Lists.newArrayList();
    for (Pick pick : getSegment().getPicks()) {
      audios.add(getAudio(pick));
    }
    return audios;
  }

  @Override
  public Audio getAudio(UUID id) throws CoreException {
    Optional<Audio> audio = sourceMaterial.getInstrument(instrumentIdForAudioUUID.get(id)).getAudios().stream()
      .filter(search -> search.getId().equals(id)).findAny();
    if (audio.isEmpty())
      throw new CoreException(String.format("Cannot find audio id=%s", id));
    return audio.get();
  }

  @Override
  public Audio getAudio(Pick pick) throws CoreException {
    return getAudio(pick.getAudioId());
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public ChainConfig getChainConfig(ChainConfigType chainConfigType) throws CoreException {
    if (getAllChainConfigs().containsKey(chainConfigType))
      return getAllChainConfigs().get(chainConfigType);

    try {
      return new ChainConfig()
        .setChainId(getChainId())
        .setTypeEnum(chainConfigType)
        .setValue(chainConfigType.defaultValue());
    } catch (CoreException e) {
      throw exception(String.format("No default value for chainConfigType=%s", chainConfigType), e);
    }
  }

  @Override
  public BigInteger getChainId() {
    return chain.getId();
  }

  @Override
  public Chord getChordAt(int position) {
    // default to returning a chord based on the segment key, if nothing else is found
    String foundChordText = getSegment().getKey();
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending
    for (SegmentChord segmentChord : getSegment().getChords()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        segmentChord.getPosition() > foundPosition && segmentChord.getPosition() < position) {
        foundPosition = segmentChord.getPosition();
        foundChordText = segmentChord.getName();
      }
    }

    return Chord.of(foundChordText);
  }

  @Override
  public Choice getCurrentMacroChoice() throws CoreException {
    return getSegment().getChoiceOfType(ProgramType.Macro);
  }

  @Override
  public Sequence getCurrentMacroSequence() throws CoreException {
    return getSequence(getCurrentMacroChoice());
  }

  @Override
  public Choice getCurrentMainChoice() throws CoreException {
    return getSegment().getChoiceOfType(ProgramType.Main);
  }

  @Override
  public Choice getCurrentRhythmChoice() throws CoreException {
    return getSegment().getChoiceOfType(ProgramType.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public Collection<AudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) {
    Map<String, AudioEvent> result = Maps.newHashMap();
    instrument.getAudios().forEach(audio ->
      instrument.getAudioEvents().stream().filter(search -> search.getAudioId().equals(audio.getId())).forEach(audioEvent -> {
        String key = audioEvent.getAudioId().toString();
        if (result.containsKey(key)) {
          if (audioEvent.getPosition() < result.get(key).getPosition()) {
            result.put(key, audioEvent);
          }
        } else {
          result.put(key, audioEvent);
        }
      }));
    return result.values();
  }

  @Override
  public String getKeyForChoice(Choice choice) throws CoreException {
    Program program = getProgram(choice);
    if (Objects.nonNull(choice.getSequenceBindingId())) {
      return getSequence(choice).getKey();
    }
    return program.getKey();
  }

  @Override
  public Long getMaxAvailableSequenceBindingOffset(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has two more available Sequence Pattern offsets");
    SequenceBinding sequenceBinding = getSequenceBinding(choice);

    Optional<Long> max = getProgram(choice).getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
    if (max.isEmpty()) throw exception("Cannot determine max available sequence binding offset");
    return max.get();
  }

  @Override
  public Map<String, Collection<Arrangement>> getMemeConstellationArrangementsOfPreviousSegment() {
    Map<String, Collection<Arrangement>> out = Maps.newHashMap();
    getMemeConstellationChoicesOfPreviousSegment().forEach((constellation, previousChoices) -> {
      out.put(constellation, Lists.newArrayList());
      previousChoices.forEach(choice -> getSegment().getArrangementsForChoice(choice).forEach(arrangement -> out.get(constellation).add(arrangement)));
    });
    return out;
  }

  @Override
  public Map<String, Collection<Choice>> getMemeConstellationChoicesOfPreviousSegment() {
    Map<String, Collection<Choice>> out = Maps.newHashMap();
    for (Segment seg : previousSegmentsWithSameMainProgram) {
      Isometry iso = MemeIsometry.ofMemes(seg.getMemes());
      String con = iso.getConstellation();
      out.put(con, seg.getChoices());
    }
    return out;
  }

  @Override
  public Map<String, Collection<Pick>> getMemeConstellationPicksOfPreviousSegment() {
    Map<String, Collection<Pick>> out = Maps.newHashMap();
    for (Segment seg : previousSegmentsWithSameMainProgram) {
      Isometry iso = MemeIsometry.ofMemes(seg.getMemes());
      String con = iso.getConstellation();
      out.put(con, seg.getPicks());
    }
    return out;
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() throws CoreException {
    return MemeIsometry.ofMemes(getProgram(getCurrentMacroChoice()).getMemesAtBeginning());
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      return MemeIsometry.ofMemes(getProgram(getPreviousMacroChoice()).getMemesAtBeginning());
    } catch (Exception ignored) {
      return new MemeIsometry();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(getSegment().getMemes());
  }

  @Override
  public Collection<Meme> getMemesOfChoice(Choice choice) throws CoreException {
    Collection<Meme> result = Lists.newArrayList();
    result.addAll(getProgram(choice).getMemes());
    if (Objects.nonNull(choice.getSequenceBindingId()))
      result.addAll(getProgram(choice).getMemes(getSequenceBinding(choice)));
    return result;
  }

  @Override
  public Long getNextSequenceBindingOffset(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequenceBindingId()))
      throw exception("Cannot determine next available SequenceBinding offset of choice with no SequenceBinding.");

    SequenceBinding sequenceBinding = getSequenceBinding(choice);
    Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Long offset = null;
    for (Long availableOffset : getProgram(choice).getAvailableOffsets(sequenceBinding))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : Long.valueOf(0L);
  }

  @Override
  public Note getNoteAtPitch(Double pitch) {
    return tuning.note(pitch);
  }

  @Override
  public AudioFormat getOutputAudioFormat() throws CoreException {
    return new AudioFormat(
      getOutputEncoding(),
      getOutputFrameRate(),
      getOutputSampleBits(),
      getOutputChannels(),
      getOutputChannels() * getOutputSampleBits() / 8,
      getOutputFrameRate(),
      false);
  }

  @Override
  public String getOutputFilePath() throws CoreException {
    if (Objects.isNull(getSegment().getWaveformKey()))
      throw exception("Segment has no waveform key!");

    return Config.getWorkTempFilePathPrefix() + getSegment().getWaveformKey();
  }

  @Override
  public Double getPitch(Note note) {
    return tuning.pitch(note);
  }

  @Override
  public Collection<Segment> getPreviousSegmentsWithSameMainProgram() {
    return previousSegmentsWithSameMainProgram;
  }

  @Override
  public Program getProgram(Choice choice) throws CoreException {
    return sourceMaterial.getProgram(choice.getProgramId());
  }

  @Override
  public Program getProgram(Sequence sequence) throws CoreException {
    return sourceMaterial.getProgram(sequence.getProgramId());
  }

  @Override
  public Map<UUID, Collection<Arrangement>> getPresetChoiceArrangements() {
    return presetChoiceArrangements;
  }

  @Override
  public Choice getPreviousMacroChoice() throws CoreException {
    return getPreviousSegment().getChoiceOfType(ProgramType.Macro);
  }

  @Override
  public Choice getPreviousMainChoice() throws CoreException {
    return getPreviousSegment().getChoiceOfType(ProgramType.Main);
  }

  @Override
  public Segment getPreviousSegment() throws CoreException {
    if (isInitialSegment()) throw exception("Initial Segment has no previous Segment");

    return getSegmentByOffset(getChainId(), getSegment().getOffset() - 1);
  }

  @Override
  public Segment getSegment() {
    return segment;
  }

  @Override
  public Segment getSegmentByOffset(BigInteger chainId, Long offset) throws CoreException {
    String key = String.format("%s_%s", chainId, offset);
    if (!segmentByOffset.containsKey(key)) {
      try {
        segmentByOffset.put(key, segmentDAO.readOneAtChainOffset(getAccess(), chainId, offset));
      } catch (CoreException e) {
        throw exception(String.format("Could not retrieve segment at chainId=%s, offset=%s", chainId, offset), e);
      }
    }

    return segmentByOffset.get(key);
  }

  @Override
  public Duration getSegmentTotalLength() throws CoreException {
    if (Objects.isNull(getSegment().getEndAt()))
      throw exception("Cannot compute total length of segment with no end!");

    return Duration.between(getSegment().getBeginAt(), getSegment().getEndAt());
  }

  @Override
  public Sequence getSequence(Choice choice) throws CoreException {
    Program program = getProgram(choice);
    if (Objects.nonNull(choice.getSequenceBindingId()))
      return program.getSequence(getSequenceBinding(choice).getSequenceId());

    if (!sequenceForChoice.containsKey(choice))
      sequenceForChoice.put(choice, program.randomlySelectSequence());

    return sequenceForChoice.get(choice);
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequenceBindingId()))
      throw exception("Cannot determine SequenceBinding offset of choice with no SequenceBinding.");
    return getSequenceBinding(choice).getOffset();
  }

  @Override
  public Ingest getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public FabricatorType getType() throws CoreException {
    if (Objects.isNull(type))
      type = determineType();
    return type;
  }

  @Override
  public boolean hasOneMoreSequenceBindingOffset(Choice choice) throws CoreException {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  @Override
  public boolean hasTwoMoreSequenceBindingOffsets(Choice choice) throws CoreException {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  @Override
  public Boolean isInitialSegment() {
    return getSegment().isInitial();
  }

  @Override
  public void putReport(String key, Object value) {
    getSegment().getReport().put(key, value);
  }

  @Override
  public void setPreArrangementsForChoice(Choice choice, Collection<Arrangement> arrangements) {
    presetChoiceArrangements.put(choice.getId(), arrangements);
  }

  @Override
  public void updateSegment() throws CoreException {
    Segment updatedSegment = getSegment();
    updatedSegment.setTypeEnum(getType());
    try {
      segmentDAO.update(getAccess(), getSegment().getId(), updatedSegment);
    } catch (CoreException e) {
      throw exception("Could not update Segment", e);
    }
    switch (getType()) {
      case Continue:
        // transitions only once, from empty to non-empty
        cachedPreviousSegmentsWithThisMainSequence.clear();
        cachedPreviousSegmentsWithThisMainSequence.addAll(previousSegmentsWithSameMainProgram);
        log.info("[segId={}] continues main sequence of previous segments: {}",
          segment.getId(),
          CSV.fromIdsOf(cachedPreviousSegmentsWithThisMainSequence));
        break;
      case Initial:
      case NextMain:
      case NextMacro:
        break;
    }
  }

  /**
   Does the program of the specified Choice have at least N more sequence binding offsets available?

   @param choice of which to check the program for next available sequence binding offsets
   @param N      more sequence offsets to check for
   @return true if N more sequence binding offsets are available
   @throws CoreException on failure
   */
  private boolean hasMoreSequenceBindingOffsets(Choice choice, int N) throws CoreException {
    if (Objects.isNull(choice.getSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has one more available Sequence Pattern offset");
    SequenceBinding sequenceBinding = getSequenceBinding(choice);

    Optional<Long> max = getProgram(choice).getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
    return max.filter(aLong -> 0 <= aLong.compareTo(sequenceBinding.getOffset() + N)).isPresent();
  }

  /**
   Determine the type of fabricator

   @return type of fabricator
   @throws CoreException on failure to determine
   */
  private FabricatorType determineType() throws CoreException {
    if (isInitialSegment())
      return FabricatorType.Initial;

    // previous main choice having at least one more pattern?
    Choice previousMainChoice;
    try {
      previousMainChoice = getPreviousMainChoice();
    } catch (CoreException e) {
      return FabricatorType.Initial;
    }

    if (Objects.nonNull(previousMainChoice) && hasOneMoreSequenceBindingOffset(previousMainChoice))
      return FabricatorType.Continue;

    // previous macro choice having at least two more patterns?
    Choice previousMacroChoice;
    try {
      previousMacroChoice = getPreviousMacroChoice();
    } catch (CoreException e) {
      return FabricatorType.Initial;
    }

    if (Objects.nonNull(previousMacroChoice) && hasTwoMoreSequenceBindingOffsets(previousMacroChoice))
      return FabricatorType.NextMain;

    return FabricatorType.NextMacro;
  }

  /**
   General a Segment URL

   @param chainId to generate URL for
   @return URL as string
   */
  private String generateWaveformKey(BigInteger chainId) throws CoreException {
    String prefix = String.format("chains-%s-segments", chainId);
    String extension = getChainConfig(ChainConfigType.OutputContainer).getValue().toLowerCase(Locale.ENGLISH);
    return amazonProvider.generateKey(prefix, extension);
  }

  /**
   Read all previous segments with the same main sequence as this one

   @return collection of segments
   */
  private Collection<Segment> buildPreviousSegmentsWithSameMainSequence() {
    try {
      // if there isn't a main choice yet, nothing to do
      Choice mainChoice = getCurrentMainChoice();
      if (Objects.isNull(mainChoice))
        return Lists.newArrayList();

      Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(mainChoice);
      if (0 < sequenceBindingOffset) {
        Long oF = getSegment().getOffset() - sequenceBindingOffset;
        Long oT = getSegment().getOffset() - 1;
        if (0 > oF || 0 > oT)
          return Lists.newArrayList();
        else
          return segmentDAO.readAllFromToOffset(getAccess(), getChainId(), oF, oT);

      } else return Lists.newArrayList();

    } catch (CoreException e) {
      return Lists.newArrayList();
    }
  }

  /**
   @return Map of the parent Instrument ID for all ingest Audio UUIDs
   */
  private Map<UUID, BigInteger> buildInstrumentIdForAudioUUID() {
    Map<UUID, BigInteger> map = Maps.newHashMap();
    sourceMaterial.getAllInstruments().forEach(instrument ->
      instrument.getAudios().forEach(audio ->
        map.put(audio.getId(), instrument.getId())
      ));
    return map;
  }

  /**
   Get a Sequence Binding for a given Choice

   @param choice to get sequence binding for
   @return Sequence Binding for the given Choice
   @throws CoreException on failure to locate the sequence binding for the specified choice
   */
  private SequenceBinding getSequenceBinding(Choice choice) throws CoreException {
    Optional<SequenceBinding> binding =
      sourceMaterial
        .getProgram(choice.getProgramId())
        .getSequenceBindings().stream()
        .filter(search -> search.getId().equals(choice.getSequenceBindingId()))
        .findAny();
    if (binding.isEmpty())
      throw exception(String.format("Found no SequenceBinding id=%s for choice!", choice.getSequenceBindingId()));
    return binding.get();
  }

  /**
   [#255] Tuning based on root note configured in environment parameters.
   */
  private Tuning computeTuning() throws CoreException {
    try {
      return Tuning.at(
        Note.of(Config.getTuningRootNote()),
        Config.getTuningRootPitch());
    } catch (MusicalException e) {
      throw exception("Could not tune XJ!", e);
    }
  }

  /**
   Create a new CoreException prefixed with a segment id

   @param message to include in exception
   @return CoreException to throw
   */
  public CoreException exception(String message) {
    return new CoreException(formatLog(message));
  }

  /**
   Create a new CoreException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CoreException to throw
   */
  public CoreException exception(String message, Exception e) {
    return new CoreException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[segId=%s] %s", getSegment().getId(), message);
  }

  /**
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer getTimeComputer() {
    double toTempo = getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo;
    try {
      Segment previous = getPreviousSegment();
      // velocity at previous segment tempo
      fromTempo = previous.getTempo();
    } catch (Exception ignored) {
      fromTempo = toTempo;
    }
    double totalBeats = getSegment().getTotal().doubleValue();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    return timeComputerFactory.create(totalBeats, fromTempo, toTempo);
  }

  /**
   real output channels based on chain configs

   @return output channels
   */
  private int getOutputChannels() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputChannels).getValue());
  }

  /**
   output encoding based on chain configs

   @return output encoding
   */
  private AudioFormat.Encoding getOutputEncoding() throws CoreException {
    return new AudioFormat.Encoding(getChainConfig(ChainConfigType.OutputEncoding).getValue());
  }

  /**
   real output frame rate based on chain configs

   @return output frame rate, per second
   */
  private float getOutputFrameRate() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputFrameRate).getValue());
  }

  /**
   real output sample bits based on chain configs

   @return output sample bits
   */
  private int getOutputSampleBits() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputSampleBits).getValue());
  }

  /**
   Ensure the current segment has a waveform key; if not, add a waveform key to this Segment

   @throws CoreException on failure to ensure generate waveform key
   */
  private void ensureWaveformKey() throws CoreException {
    if (Objects.isNull(segment.getWaveformKey()) || segment.getWaveformKey().isEmpty()) {
      segment.setWaveformKey(generateWaveformKey(segment.getChainId()));
      log.info("[segId={}] Generated Waveform Key {}", segment.getId(), segment.getWaveformKey());
    }
  }

}
