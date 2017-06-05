// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.craft.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.basis.Basis;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.isometry.MemeIsometry;
import io.outright.xj.core.model.MemeEntity;
import io.outright.xj.core.model.choice.Chance;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.choice.Chooser;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.util.Value;
import io.outright.xj.craftworker.craft.FoundationCraft;
import io.outright.xj.music.BPM;
import io.outright.xj.music.Chord;
import io.outright.xj.music.Key;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.tables.Idea.IDEA;

/**
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class FoundationCraftImpl implements FoundationCraft {
  private static final double SCORE_MATCHED_KEY_MODE = 10;
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS = 10;
  private final Logger log = LoggerFactory.getLogger(FoundationCraftImpl.class);
  private final ChoiceDAO choiceDAO;
  private final IdeaDAO ideaDAO;
  private final IdeaMemeDAO ideaMemeDAO;
  private final LinkChordDAO linkChordDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final PhaseChordDAO phaseChordDAO;
  private final Basis basis;
  private Idea _macroIdea;
  private Idea _mainIdea;
  private ULong _macroPhaseOffset;
  private ULong _mainPhaseOffset;

  @Inject
  public FoundationCraftImpl(
    @Assisted("basis") Basis basis,
    ChoiceDAO choiceDAO,
    IdeaDAO ideaDAO,
    IdeaMemeDAO ideaMemeDAO,
    LinkChordDAO linkChordDAO,
    LinkMemeDAO linkMemeDAO,
    PhaseChordDAO phaseChordDAO
  /*-*/) throws BusinessException {
    this.basis = basis;
    this.choiceDAO = choiceDAO;
    this.ideaDAO = ideaDAO;
    this.ideaMemeDAO = ideaMemeDAO;
    this.linkChordDAO = linkChordDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.phaseChordDAO = phaseChordDAO;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftMacro();
      craftMain();
      craftMemes();
      craftChords();
      basis.updateLink(basis.link()
        .setDensity(linkDensity())
        .setTempo(linkTempo())
        .setKey(linkKey())
        .setTotal(linkTotal())
        .setEndAtTimestamp(linkEndTimestamp()));
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type FoundationCraft for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   Report
   */
  private void report() {
    // TODO basis.report() anything else interesting from the craft operation
  }

  /**
   Make Macro-type Idea Choice
   add macro-idea choice to link

   @throws Exception on any failure
   */
  private void craftMacro() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(basis.linkId().toBigInteger())
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
  private void craftMain() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(basis.linkId().toBigInteger())
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
  private void craftMemes() throws Exception {
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
  private void craftChords() throws Exception {
    phaseChordDAO.readAll(Access.internal(), mainPhase().getId())
      .forEach(phaseChordRecord -> {
        String name = "NaN";
        try {
          // delta the chord name
          name = Chord.of(phaseChordRecord.getName()).transpose(mainTranspose()).getFullDescription();
          // create the transposed chord
          linkChordDAO.create(Access.internal(),
            new LinkChord()
              .setLinkId(basis.linkId().toBigInteger())
              .setName(name)
              .setPosition(phaseChordRecord.getPosition()));

        } catch (Exception e) {
          log.warn("failed to create transposed link chord '" +
            String.valueOf(name) + "'@" + phaseChordRecord.getPosition(), e);
        }
      });
  }

  /**
   compute (and cache) the chosen macro idea

   @return macro-type idea
   @throws Exception on failure
   */
  private Idea macroIdea() throws Exception {
    if (Objects.isNull(_macroIdea))
      switch (basis.type()) {

        case Initial:
          _macroIdea = chooseMacro();
          break;

        case Continue:
        case NextMain:
          Choice previousChoice = basis.previousMacroChoice();
          if (Objects.isNull(previousChoice))
            throw new BusinessException("No macro-type idea chosen in previous link!");
          _macroIdea = basis.idea(previousChoice.getIdeaId());
          break;

        case NextMacro:
          _macroIdea = chooseMacro();
      }
    return _macroIdea;
  }

  /**
   compute (and cache) the mainIdea

   @return mainIdea
   */
  private Idea mainIdea() throws Exception {
    if (Objects.isNull(_mainIdea))
      switch (basis.type()) {

        case Continue:
          Choice previousChoice = basis.previousMainChoice();
          if (Objects.isNull(previousChoice))
            throw new BusinessException("No main-type idea chosen in previous link!");
          _mainIdea = basis.idea(previousChoice.getIdeaId());
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _mainIdea = chooseMain();
      }


    return _mainIdea;
  }

  /**
   compute (and cache) the macroTranspose

   @return macroTranspose
   */
  private Integer macroTranspose() throws Exception {
    switch (basis.type()) {

      case Initial:
        return 0;

      case Continue:
      case NextMain:
        return basis.previousMacroChoice().getTranspose();

      case NextMacro:
        return Key.delta(macroIdea().getKey(),
          basis.previousMacroNextPhase().getKey(),
          basis.previousMacroChoice().getTranspose());

      default:
        throw new BusinessException("unable to determine macro-type idea transposition");
    }
  }

  /**
   compute (and cache) Transpose Main-Idea to the transposed key of the current macro phase

   @return mainTranspose
   */
  private Integer mainTranspose() throws Exception {
    return Key.delta(mainIdea().getKey(),
      Value.eitherOr(macroPhase().getKey(), macroIdea().getKey()),
      macroTranspose());
  }

  /**
   compute (and cache) the macroPhaseOffset

   @return macroPhaseOffset
   */
  private ULong macroPhaseOffset() throws Exception {
    if (Objects.isNull(_macroPhaseOffset))
      switch (basis.type()) {

        case Initial:
          _macroPhaseOffset = ULong.valueOf(0);
          break;

        case Continue:
          _macroPhaseOffset = basis.previousMacroChoice().getPhaseOffset();
          break;

        case NextMain:
          _macroPhaseOffset = basis.previousMacroChoice().nextPhaseOffset();
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
  private ULong mainPhaseOffset() throws Exception {
    if (Objects.isNull(_mainPhaseOffset))
      switch (basis.type()) {

        case Initial:
          _mainPhaseOffset = ULong.valueOf(0);
          break;

        case Continue:
          _mainPhaseOffset = basis.previousMainChoice().nextPhaseOffset();
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
   Fetch current phase of macro-type idea

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord macroPhase() throws Exception {
    PhaseRecord phase = basis.phaseByOffset(macroIdea().getId(), macroPhaseOffset());

    if (Objects.isNull(phase))
      throw new BusinessException("macro-phase does not exist!");

    return phase;
  }

  /**
   Fetch current phase of main-type idea

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord mainPhase() throws Exception {
    PhaseRecord phase = basis.phaseByOffset(mainIdea().getId(), mainPhaseOffset());

    if (Objects.isNull(phase))
      throw new BusinessException("main-phase does not exist!");

    return phase;
  }

  /**
   Choose macro idea

   @return macro-type idea
   @throws Exception on failure
   */
  private Idea chooseMacro() throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Idea> chooser = new Chooser<>();
    String key = basis.isInitialLink() ? null : basis.previousMacroPhase().getKey();

    // (1a) retrieve ideas bound directly to chain
    sourceRecords = ideaDAO.readAllBoundToChain(Access.internal(), basis.chainId(), Idea.MACRO);

    // (1b) only if none were found in the previous transpose, retrieve ideas bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = ideaDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), Idea.MACRO);

    // (2) score each source record
    sourceRecords.forEach((record ->
      chooser.add(new Idea().setFromRecord(record),
        Chance.normallyAround(
          Key.isSameMode(key, record.get(IDEA.KEY)) ? SCORE_MATCHED_KEY_MODE : 0,
          0.5))));

    // (2b) Avoid previous macro idea
    if (!basis.isInitialLink())
      chooser.score(basis.previousMacroChoice().getIdeaId(), -SCORE_AVOID_CHOOSING_PREVIOUS);

    // report
    basis.report("macroChoice", chooser.report());

    // (3) return the top choice
    Idea idea = chooser.getTop();
    if (Objects.nonNull(idea))
      return idea;
    else
      throw new BusinessException("Found no macro-type idea bound to Chain!");
  }

  /**
   Choose main idea

   @return main-type Idea
   @throws Exception on failure
   <p>
   TODO don't we need to pass in the current phase of the macro idea?
   */
  private Idea chooseMain() throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Idea> chooser = new Chooser<>();

    // TODO: only choose major ideas for major keys, minor for minor! [#223] Key of first Phase of chosen Main-Idea must match the `minor` or `major` with the Key of the current Link.

    // (1) retrieve memes of macro idea, for use as a meme isometry comparison
    MemeIsometry memeIsometry = MemeIsometry.of(ideaMemeDAO.readAll(Access.internal(), macroIdea().getId()));

    // (2a) retrieve ideas bound directly to chain
    sourceRecords = ideaDAO.readAllBoundToChain(Access.internal(), basis.chainId(), Idea.MAIN);

    // (2b) only if none were found in the previous transpose, retrieve ideas bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = ideaDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), Idea.MAIN);

    // (3) score each source record based on meme isometry
    sourceRecords.forEach((record ->
      chooser.add(new Idea().setFromRecord(record),
        Chance.normallyAround(
          memeIsometry.scoreCSV(String.valueOf(record.get(MemeEntity.KEY_MANY))),
          0.5))));

    // (3b) Avoid previous main idea
    if (!basis.isInitialLink())
      chooser.score(basis.previousMainChoice().getIdeaId(), -SCORE_AVOID_CHOOSING_PREVIOUS);

    // report
    basis.report("mainChoice", chooser.report());

    // (4) return the top choice
    Idea idea = chooser.getTop();
    if (Objects.nonNull(idea))
      return idea;
    else
      throw new BusinessException("Found no main-type idea bound to Chain!");
  }

  /**
   prepare map of final link memes

   @return map of meme name to LinkMeme entity
   */
  private Map<String, LinkMeme> linkMemes() throws Exception {
    Map<String, LinkMeme> out = Maps.newHashMap();

    basis.ideaMemes(macroIdea().getId())
      .forEach(meme -> out.put(
        meme.getName(), basis.linkMeme(basis.linkId(), meme.getName())));

    basis.phaseMemes(macroPhase().getId())
      .forEach(meme -> out.put(
        meme.getName(), basis.linkMeme(basis.linkId(), meme.getName())));

    basis.ideaMemes(mainIdea().getId())
      .forEach(meme -> out.put(
        meme.getName(), basis.linkMeme(basis.linkId(), meme.getName())));

    basis.phaseMemes(mainPhase().getId())
      .forEach(meme -> out.put(
        meme.getName(), basis.linkMeme(basis.linkId(), meme.getName())));

    return out;
  }

  /**
   Get Link length, in nanoseconds
   If a previous link exists, the tempo is averaged with its tempo, because the tempo will increase at a linear rate from start to finish.

   @return link length, in nanoseconds
   @throws Exception on failure
   */
  private long linkLengthNanos() throws Exception {
    if (!basis.isInitialLink())
      return BPM.beatsNanos(linkTotal(),
        (linkTempo() + basis.previousLink().getTempo()) / 2);
    else
      return BPM.beatsNanos(linkTotal(), linkTempo());
  }

  /**
   Get Link End Timestamp
   Link Length Time = Link Tempo (time per Beat) * Link Length (# Beats)

   @return end timestamp
   @throws BusinessException on failure
   */
  private Timestamp linkEndTimestamp() throws Exception {
    return Timestamp.from(basis.linkBeginAt().toInstant().plusNanos(linkLengthNanos()));
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
    return (Value.eitherOr(macroPhase().getTempo(), macroIdea().getTempo()) +
      Value.eitherOr(mainPhase().getTempo(), mainIdea().getTempo())) / 2;
  }

  /**
   Compute the final density of the current link
   TODO Link Density = average of macro and main-idea phases

   @return density
   @throws Exception on failure
   */
  private Double linkDensity() throws Exception {
    return (Value.eitherOr(macroPhase().getDensity(), macroIdea().getDensity()) +
      Value.eitherOr(mainPhase().getDensity(), mainIdea().getDensity())) / 2;
  }

}
