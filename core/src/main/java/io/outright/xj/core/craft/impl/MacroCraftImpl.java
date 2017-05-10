// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.craft.MacroCraft;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.model.choice.Chance;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.choice.Chooser;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.meme.MemeIsometry;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.util.Value;
import io.outright.xj.music.BPM;
import io.outright.xj.music.Chord;
import io.outright.xj.music.Key;
import io.outright.xj.music.schema.KeyMode;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.craft.impl.MacroCraftType.Continue;
import static io.outright.xj.core.craft.impl.MacroCraftType.Initial;
import static io.outright.xj.core.craft.impl.MacroCraftType.NextMacro;
import static io.outright.xj.core.craft.impl.MacroCraftType.NextMain;
import static io.outright.xj.core.tables.Idea.IDEA;

/**
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class MacroCraftImpl extends CraftImpl implements MacroCraft {
  private static final double SCORE_MATCHED_KEY_MODE = 10;
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS = 100;
  private final Logger log = LoggerFactory.getLogger(MacroCraftImpl.class);
  private final ChoiceDAO choiceDAO;
  private final IdeaDAO ideaDAO;
  private final IdeaMemeDAO ideaMemeDAO;
  private final Link link;
  private final LinkChordDAO linkChordDAO;
  private final LinkDAO linkDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final PhaseChordDAO phaseChordDAO;
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private MacroCraftType _type;
  private Map<String, Object> report = Maps.newHashMap();
  private Idea _macroIdea;
  private Idea _mainIdea;
  private Link _previousLink;
  private LinkChoice _previousMacroChoice;
  private LinkChoice _previousMainChoice;
  private ULong _macroPhaseOffset;
  private ULong _mainPhaseOffset;
  private Result<IdeaMemeRecord> _macroIdeaMemes;
  private Result<PhaseMemeRecord> _macroPhaseMemes;
  private PhaseRecord _macroPhase;
  private Result<IdeaMemeRecord> _mainIdeaMemes;
  private Result<PhaseMemeRecord> _mainPhaseMemes;
  private PhaseRecord _mainPhase;
  private PhaseRecord _previousMacroPhase;
  private PhaseRecord _previousMacroNextPhase;

  @Inject
  public MacroCraftImpl(
    @Assisted("link") Link link,
    ChoiceDAO choiceDAO,
    IdeaDAO ideaDAO,
    IdeaMemeDAO ideaMemeDAO,
    LinkChordDAO linkChordDAO,
    LinkDAO linkDAO,
    LinkMemeDAO linkMemeDAO,
    LinkMessageDAO linkMessageDAO,
    PhaseChordDAO phaseChordDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO
  /*-*/) throws BusinessException {
    this.link = link;
    this.choiceDAO = choiceDAO;
    this.ideaDAO = ideaDAO;
    this.ideaMemeDAO = ideaMemeDAO;
    this.linkChordDAO = linkChordDAO;
    this.linkDAO = linkDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.phaseChordDAO = phaseChordDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
  }

  @Override
  public void craft() throws BusinessException {
    try {
      makeMacroChoice();
      makeMainChoice();
      makeMemes();
      makeChords();
      updateLink();
      reportLink();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s macro-choice for link #%s",
          type(), linkId().toString()), e);
    }
  }

  /**
   Determine type of macro-craft this operation will be

   @return macro-craft type
   */
  private MacroCraftType type() throws BusinessException {
    if (Objects.isNull(_type))
      if (isInitialLink())
        _type = Initial;
      else if (previousMainChoice().hasOneMorePhase())
        _type = Continue;
      else if (previousMacroChoice().hasTwoMorePhases())
        _type = NextMain;
      else
        _type = NextMacro;

    return _type;
  }

  /**
   Make Macro-type Idea Choice
   add macro-idea choice to link

   @throws Exception on any failure
   */
  private void makeMacroChoice() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(linkId().toBigInteger())
        .setType(Choice.MACRO)
        .setIdeaId(macroIdea().getId().toBigInteger())
        .setTranspose(macroTranspose())
        .setPhaseOffset(macroPhaseOffset().toBigInteger()));
  }

  /**
   Make Main-type Idea Choice
   add macro-idea choice to link

   @throws Exception on any failure
   */
  private void makeMainChoice() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(linkId().toBigInteger())
        .setType(Choice.MAIN)
        .setIdeaId(mainIdea().getId().toBigInteger())
        .setTranspose(mainTranspose())
        .setPhaseOffset(mainPhaseOffset().toBigInteger()));
  }

  /**
   Make Memes
   add all memes to link

   @throws Exception on any failure
   */
  private void makeMemes() throws Exception {
    linkMemes().forEach((memeName, linkMeme) -> {
      try {
        linkMemeDAO.create(Access.internal(), linkMeme);

      } catch (Exception e) {
        log.warn("failed to create link meme '" + memeName + '"', e);
      }
    });
  }

  /**
   Make Chords
   Link Chords = Main Idea Phase Chords, transposed according to to main idea choice

   @throws Exception on any failure
   */
  private void makeChords() throws Exception {
    phaseChordDAO.readAll(Access.internal(), mainPhase().getId())
      .forEach(phaseChordRecord -> {
        String name = "NaN";
        try {
          // delta the chord name
          name = Chord.of(phaseChordRecord.getName()).transpose(mainTranspose()).getFullDescription();
          // create the transposed chord
          linkChordDAO.create(Access.internal(),
            new LinkChord()
              .setLinkId(linkId().toBigInteger())
              .setName(name)
              .setPosition(phaseChordRecord.getPosition()));

        } catch (Exception e) {
          log.warn("failed to create transposed link chord '" +
            String.valueOf(name) + "'@" + phaseChordRecord.getPosition(), e);
        }
      });
  }

  /**
   Update the Link with final values

   @throws Exception on any failure
   */
  private void updateLink() throws Exception {
    try {
      linkDAO.update(Access.internal(), linkId(),
        link
          .setDensity(linkDensity())
          .setTempo(linkTempo())
          .setKey(linkKey())
          .setTotal(linkTotal())
          .setEndAtTimestamp(linkEndTimestamp()));

    } catch (Exception e) {
      throw new BusinessException("CraftLinkWorkerOperation failed to update Link!", e);
    }
  }

  /**
   is initial link?
   @return whether this is the initial link in a chain
   */
  private Boolean isInitialLink() {
    return link.isInitial();
  }

  /**
   Compute the total # of beats of the current link
   Link Total (# Beats) = from current Phase of Main-Idea

   @return # beats total
   @throws Exception on failure
   */
  private Integer linkTotal() throws Exception {
    return mainPhase().getTotal().intValue();
  }

  /**
   Compute the final key of the current link
   Link Key is the transposed key of the current main phase

   @return key
   @throws Exception on failure
   */
  private String linkKey() throws Exception {
    return Key.of(mainPhase().getKey()).transpose(mainTranspose()).getFullDescription();
  }

  /**
   Compute the final tempo of the current link

   @return tempo
   @throws Exception on failure
   */
  private double linkTempo() throws Exception {
    return (available(macroPhase().getTempo(), macroIdea().getTempo()) +
      available(mainPhase().getTempo(), mainIdea().getTempo())) / 2;
  }

  /**
   Compute the final density of the current link
   TODO Link Density = average of macro and main-idea phases

   @return density
   @throws Exception on failure
   */
  private Double linkDensity() throws Exception {
    return (available(macroPhase().getDensity(), macroIdea().getDensity()) +
      available(mainPhase().getDensity(), mainIdea().getDensity())) / 2;
  }

  /**
   current link id

   @return id of current link
   */
  private ULong linkId() {
    return link.getId();
  }

  /**
   Chain id, from link
   @return chain id
   */
  private ULong chainId() {
    return link.getChainId();
  }

  /**
   prepare map of final link memes

   @return map of meme name to LinkMeme entity
   */
  private Map<String, LinkMeme> linkMemes() throws Exception {
    Map<String, LinkMeme> out = Maps.newHashMap();

    macroIdeaMemes().forEach(meme -> out.put(
      meme.getName(), linkMeme(linkId(), meme.getName())));

    macroPhaseMemes().forEach(meme -> out.put(
      meme.getName(), linkMeme(linkId(), meme.getName())));

    mainIdeaMemes().forEach(meme -> out.put(
      meme.getName(), linkMeme(linkId(), meme.getName())));

    mainPhaseMemes().forEach(meme -> out.put(
      meme.getName(), linkMeme(linkId(), meme.getName())));

    return out;
  }

  /**
   Add meme

   @param linkId id of link
   @param name   of meme to add
   */
  private LinkMeme linkMeme(ULong linkId, String name) {
    return
      new LinkMeme()
        .setLinkId(linkId.toBigInteger())
        .setName(name);
  }

  /**
   Get Link End Timestamp
   Link Length Time = Link Tempo (time per Beat) * Link Length (# Beats)

   @return end timestamp
   @throws BusinessException on failure
   */
  private Timestamp linkEndTimestamp() throws Exception {
    return Timestamp.from(linkBeginAt().toInstant().plusNanos(linkLengthNanos()));
  }

  /**
   Get Link length, in nanoseconds
   If a previous link exists, the tempo is averaged with its tempo, because the tempo will increase at a linear rate from start to finish.

   @return link length, in nanoseconds
   @throws Exception on failure
   */
  private long linkLengthNanos() throws Exception {
    if (Objects.nonNull(previousLink()))
      return BPM.beatsNanos(linkTotal(),
        (linkTempo() + previousLink().getTempo()) / 2);
    else
      return BPM.beatsNanos(linkTotal(), linkTempo());
  }

  /**
   Link begin-at timestamp

   @return begin at
   */
  private Timestamp linkBeginAt() {
    return link.getBeginAt();
  }

  /**
   Fetch all memes for the macro-type idea

   @return result of idea memes
   @throws Exception on failure
   */
  private Result<IdeaMemeRecord> macroIdeaMemes() throws Exception {
    if (Objects.isNull(_macroIdeaMemes))
      _macroIdeaMemes = ideaMemeDAO.readAll(Access.internal(), macroIdea().getId());

    return _macroIdeaMemes;
  }

  /**
   Fetch all memes for the macro-type idea phase

   @return result of phase memes
   @throws Exception on failure
   */
  private Result<PhaseMemeRecord> macroPhaseMemes() throws Exception {
    if (Objects.isNull(_macroPhaseMemes))
      _macroPhaseMemes = phaseMemeDAO.readAll(Access.internal(), macroPhase().getId());

    return _macroPhaseMemes;
  }

  /**
   Fetch current phase of macro-type idea

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord macroPhase() throws Exception {
    if (Objects.isNull(_macroPhase))
      if (Objects.isNull(_macroPhase =
        phaseDAO.readOneForIdea(Access.internal(),
          macroIdea().getId(), macroPhaseOffset())))
        throw new BusinessException("macro-phase does not exist!");

    return _macroPhase;
  }

  /**
   Fetch all memes for the main-type idea

   @return result of idea memes
   @throws Exception on failure
   */
  private Result<IdeaMemeRecord> mainIdeaMemes() throws Exception {
    if (Objects.isNull(_mainIdeaMemes))
      _mainIdeaMemes = ideaMemeDAO.readAll(Access.internal(), mainIdea().getId());

    return _mainIdeaMemes;
  }

  /**
   Fetch all memes for the main-type idea phase

   @return result of phase memes
   @throws Exception on failure
   */
  private Result<PhaseMemeRecord> mainPhaseMemes() throws Exception {
    if (Objects.isNull(_mainPhaseMemes))
      _mainPhaseMemes = phaseMemeDAO.readAll(Access.internal(), mainPhase().getId());

    return _mainPhaseMemes;
  }

  /**
   Fetch current phase of main-type idea

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord mainPhase() throws Exception {
    if (Objects.isNull(_mainPhase))
      if (Objects.isNull(_mainPhase =
        phaseDAO.readOneForIdea(Access.internal(),
          mainIdea().getId(), mainPhaseOffset())))
        throw new BusinessException("main-phase does not exist!");

    return _mainPhase;
  }

  /**
   compute (and cache) the chosen macro idea

   @return macro-type idea
   @throws Exception on failure
   */
  private Idea macroIdea() throws Exception {
    if (Objects.isNull(_macroIdea))
      switch (type()) {

        case Initial:
          _macroIdea = chooseMacro(chainId(), null);
          break;

        case Continue:
          _macroIdea = readIdea(previousMacroChoice());
          break;

        case NextMain:
          _macroIdea = readIdea(previousMacroChoice());
          break;

        case NextMacro:
          _macroIdea = chooseMacro(chainId(),
            Key.of(previousMacroPhase().getKey()).getMode());
      }
    return _macroIdea;
  }

  /**
   macro-type idea phase in previous link

   @return phase
   @throws Exception on failure
   */
  private PhaseRecord previousMacroPhase() throws Exception {
    if (Objects.isNull(_previousMacroPhase))
      _previousMacroPhase =
        phaseDAO.readOneForIdea(Access.internal(),
          previousMacroChoice().getIdeaId(),
          previousMacroChoice().getPhaseOffset());

    return _previousMacroPhase;
  }

  /**
   macro-type idea phase in previous link

   @return phase
   @throws Exception on failure
   */
  private PhaseRecord previousMacroNextPhase() throws Exception {
    if (Objects.isNull(_previousMacroNextPhase))
      _previousMacroNextPhase =
        phaseDAO.readOneForIdea(Access.internal(),
          previousMacroChoice().getIdeaId(),
          previousMacroChoice().nextPhaseOffset());

    return _previousMacroNextPhase;
  }

  /**
   compute (and cache) the mainIdea

   @return mainIdea
   */
  private Idea mainIdea() throws Exception {
    if (Objects.isNull(_mainIdea))
      switch (type()) {

        case Initial:
          _mainIdea = chooseMain(chainId(), macroIdea());
          break;

        case Continue:
          _mainIdea = readIdea(previousMainChoice());
          break;

        case NextMain:
          _mainIdea = chooseMain(chainId(), macroIdea());
          break;

        case NextMacro:
          _mainIdea = chooseMain(chainId(), macroIdea());
      }


    return _mainIdea;
  }

  /**
   compute (and cache) the macroTranspose

   @return macroTranspose
   */
  private Integer macroTranspose() throws Exception {
    switch (type()) {

      case Initial:
        return 0;

      case Continue:
      case NextMain:
        return previousMacroChoice().getTranspose();

      case NextMacro:
        return delta(macroIdea().getKey(),
          previousMacroNextPhase().getKey(),
          previousMacroChoice().getTranspose());

      default:
        throw new BusinessException("unable to determine macro-type idea transposition");
    }
  }

  /**
   delta +/- semitones from a Key (string) to another Key (string)

   @param fromKey to compute delta from
   @param toKey   to compute delta to
   @param adjust  +/- semitones adjustment
   @return delta from one key to another
   */
  private Integer delta(String fromKey, String toKey, int adjust) {
    return Key.of(fromKey).delta(Key.of(toKey).transpose(adjust));
  }

  /**
   compute (and cache) Transpose Main-Idea to the transposed key of the current macro phase

   @return mainTranspose
   */
  private Integer mainTranspose() throws Exception {
    return delta(mainIdea().getKey(),
      available(macroPhase().getKey(), macroIdea().getKey()),
      macroTranspose());
  }

  /**
   compute (and cache) the previousLink

   @return previousLink
   */
  private Link previousLink() throws BusinessException {
    if (Objects.isNull(_previousLink) && !isInitialLink())
      _previousLink = readLinkForChainOffset(chainId(),
        Value.inc(link.getOffset(), -1));

    return _previousLink;
  }

  /**
   compute (and cache) the previousMacroChoice

   @return previousMacroChoice
   */
  private LinkChoice previousMacroChoice() throws BusinessException {
    if (Objects.isNull(_previousMacroChoice) && !isInitialLink()) {
      _previousMacroChoice = readPreviousLinkChoice(previousLink().getId(), Idea.MACRO);
    }

    return _previousMacroChoice;
  }

  /**
   compute (and cache) the previousMainChoice

   @return previousMainChoice
   */
  private LinkChoice previousMainChoice() throws BusinessException {
    if (Objects.isNull(_previousMainChoice)&& !isInitialLink())
      _previousMainChoice = readPreviousLinkChoice(previousLink().getId(), Idea.MAIN);

    return _previousMainChoice;
  }

  /**
   compute (and cache) the macroPhaseOffset

   @return macroPhaseOffset
   */
  private ULong macroPhaseOffset() throws BusinessException {
    if (Objects.isNull(_macroPhaseOffset))
      switch (type()) {

        case Initial:
          _macroPhaseOffset = ULong.valueOf(0);
          break;

        case Continue:
          _macroPhaseOffset = previousMacroChoice().getPhaseOffset();
          break;

        case NextMain:
          _macroPhaseOffset = previousMacroChoice().nextPhaseOffset();
          break;

        case NextMacro:
          _macroPhaseOffset = ULong.valueOf(0);
      }

    return _macroPhaseOffset;
  }

  /**
   compute (and cache) the mainPhaseOffset

   @return mainPhaseOffset
   */
  private ULong mainPhaseOffset() throws BusinessException {
    if (Objects.isNull(_mainPhaseOffset))
      switch (type()) {

        case Initial:
          _mainPhaseOffset = ULong.valueOf(0);
          break;

        case Continue:
          _mainPhaseOffset = previousMainChoice().nextPhaseOffset();
          break;

        case NextMain:
          _mainPhaseOffset = ULong.valueOf(0);
          break;

        case NextMacro:
          _mainPhaseOffset = ULong.valueOf(0);
      }

    return _mainPhaseOffset;
  }

  /**
   Choose macro idea

   @param chainId chain to choose macro for
   @param keyMode major or minor
   @return macro-type idea
   @throws Exception on failure
   */
  private Idea chooseMacro(ULong chainId, KeyMode keyMode) throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Idea> chooser = new Chooser<>();

    // (1a) retrieve ideas bound directly to chain
    sourceRecords = ideaDAO.readAllBoundToChain(Access.internal(), chainId, Idea.MACRO);

    // (1b) only if none were found in the previous step, retrieve ideas bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = ideaDAO.readAllBoundToChainLibrary(Access.internal(), chainId, Idea.MACRO);

    // (2) score each source record
    sourceRecords.forEach((record ->
      chooser.add(new Idea().setFromRecord(record),
        Chance.normallyAround(matchKeyMode(keyMode, record.get(IDEA.KEY)), 0.5))));

    // (2b) Avoid previous macro idea
    if (Objects.nonNull(previousMacroChoice()))
      chooser.score(previousMacroChoice().getIdeaId(), -SCORE_AVOID_CHOOSING_PREVIOUS);

    // (3) return the top choice
    Idea idea = chooser.getTop();
    if (Objects.nonNull(idea))
      return idea;
    else
      throw new BusinessException("Found no macro-type idea bound to Chain!");
  }

  /**
   Increased score for a matching adjustment symbol,
   only if one is provided.

   @param keyMode to match, or null if none matters
   @param key     to match adjustment symbol of
   @return increased score if match, else 0
   */
  private double matchKeyMode(@Nullable KeyMode keyMode, String key) {
    if (Objects.isNull(keyMode))
      return 0;

    return Key.of(key).getMode().equals(keyMode) ? SCORE_MATCHED_KEY_MODE : 0;
  }

  /**
   Choose main idea

   @param chainId   chain to choose idea for
   @param macroIdea macro idea chosen
   @return main-type Idea
   @throws Exception on failure
   <p>
   TODO don't we need to pass in the current phase of the macro idea?
   */
  private Idea chooseMain(ULong chainId, Idea macroIdea) throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Idea> chooser = new Chooser<>();

    // TODO: only choose major ideas for major keys, minor for minor! [#223] Key of first Phase of chosen Main-Idea must match the `minor` or `major` with the Key of the current Link.

    // (1) retrieve memes of macro idea, for use as a meme isometry comparison
    MemeIsometry memeIsometry = MemeIsometry.of(ideaMemeDAO.readAll(Access.internal(), macroIdea.getId()));

    // (2a) retrieve ideas bound directly to chain
    sourceRecords = ideaDAO.readAllBoundToChain(Access.internal(), chainId, Idea.MAIN);

    // (2b) only if none were found in the previous step, retrieve ideas bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = ideaDAO.readAllBoundToChainLibrary(Access.internal(), chainId, Idea.MAIN);

    // (3) score each source record based on meme isometry
    sourceRecords.forEach((record ->
      chooser.add(new Idea().setFromRecord(record),
        Chance.normallyAround(
          memeIsometry.scoreCSV(String.valueOf(record.get(Meme.KEY_MANY))),
          0.5))));

    // (3b) Avoid previous main idea
    if (Objects.nonNull(previousMainChoice()))
      chooser.score(previousMainChoice().getIdeaId(), -SCORE_AVOID_CHOOSING_PREVIOUS);

    // (4) return the top choice
    Idea idea = chooser.getTop();
    if (Objects.nonNull(idea))
      return idea;
    else
      throw new BusinessException("Found no main-type idea bound to Chain!");
  }

  /**
   Read the id of a link at a given offset in a chain

   @param chainId of chain in which to read link
   @param offset  of link in chain
   @return link id
   */
  private Link readLinkForChainOffset(ULong chainId, ULong offset) throws BusinessException {
    try {
      LinkRecord linkRecord = linkDAO.readOneAtChainOffset(Access.internal(), chainId, offset);
      if (Objects.isNull(linkRecord))
        throw new Exception("empty result");
      return new Link().setFromRecord(linkRecord); // linkId;
    } catch (Exception e) {
      throw new BusinessException("no link found in Chain #" + chainId + " at Offset=" + offset + "! " + e.getMessage(), e);
    }
  }

  /**
   Read the current given type of idea for a given link

   @param linkId to read given type of idea for
   @return type of idea
   @throws BusinessException on failure.
   */
  private LinkChoice readPreviousLinkChoice(ULong linkId, String ideaType) throws BusinessException {
    try {
      LinkChoice linkChoice = linkDAO.readLinkChoice(Access.internal(), linkId, ideaType);
      if (Objects.isNull(linkChoice))
        throw new Exception("empty result");
      return linkChoice;
    } catch (Exception e) {
      throw new BusinessException("no " + ideaType + "-type idea found! " + e.getMessage(), e);
    }
  }

  /**
   Read the original idea for a given LinkChoice

   @param linkChoice to read original idea for
   @return idea
   @throws BusinessException on failure.
   */
  private Idea readIdea(LinkChoice linkChoice) throws BusinessException {
    try {
      Idea idea = new Idea().setFromRecord(ideaDAO.readOne(Access.internal(), linkChoice.getIdeaId()));
      if (Objects.isNull(idea))
        throw new Exception("empty result");
      return idea;
    } catch (Exception e) {
      throw new BusinessException("no Idea #" + linkChoice.getIdeaId() + " found! " + e.getMessage(), e);
    }
  }

  /**
   Send the final report of craft process, as a link message
   */
  private void reportLink() throws Exception {
    // Add internal fields to report
    if (Objects.nonNull(macroIdea()))
      report.put("macroIdea", macroIdea().asMap());
    if (Objects.nonNull(mainIdea()))
      report.put("mainIdea", mainIdea().asMap());
    report.put("macroTranspose", macroTranspose());
    report.put("mainTranspose", mainTranspose());
    if (Objects.nonNull(previousLink()))
      report.put("previousLink", previousLink().asMap());
    if (Objects.nonNull(previousMacroChoice()))
      report.put("previousMacroChoice", previousMacroChoice().asMap());
    if (Objects.nonNull(previousMainChoice()))
      report.put("previousMainChoice", previousMainChoice().asMap());
    report.put("macroPhaseOffset", macroPhaseOffset());
    report.put("mainPhaseOffset", mainPhaseOffset());

    // build YAML and create Link Message
    String body = new Yaml().dumpAsMap(report);
    try {
      linkMessageDAO.create(Access.internal(),
        new LinkMessage()
          .setLinkId(linkId().toBigInteger())
          .setType(Message.INFO)
          .setBody(body));

    } catch (Exception e) {
      log.warn("Failed to send final craft message for Link {} Message {}", link, body, e);
    }
  }

}
