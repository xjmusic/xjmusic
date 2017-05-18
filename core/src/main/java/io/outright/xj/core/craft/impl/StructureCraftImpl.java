// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.craft.Basis;
import io.outright.xj.core.craft.StructureCraft;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.model.choice.Chance;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.choice.Chooser;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.meme.MemeIsometry;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.util.Value;
import io.outright.xj.music.Key;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Objects;

/**
 Structure craft for the current link includes rhythm and support
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class StructureCraftImpl implements StructureCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_RHTYHM = 10;
  //  private final Logger log = LoggerFactory.getLogger(StructureCraftImpl.class);
  private final Basis basis;
  private final ChoiceDAO choiceDAO;
  private final IdeaDAO ideaDAO;
  private Idea _rhythmIdea;
  private ULong _rhythmPhaseOffset;

  @Inject
  public StructureCraftImpl(
    @Assisted("basis") Basis basis,
    ChoiceDAO choiceDAO,
    IdeaDAO ideaDAO
  /*-*/) throws BusinessException {
    this.basis = basis;
    this.choiceDAO = choiceDAO;
    this.ideaDAO = ideaDAO;
  }

  @Override
  public void craft() throws BusinessException {
    try {
      craftRhythm();
      craftSupport();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do Structure %s-type craft for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   craft link rhythm
   */
  private void craftRhythm() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(basis.linkId().toBigInteger())
        .setType(Choice.RHYTHM)
        .setIdeaId(rhythmIdea().getId().toBigInteger())
        .setTranspose(rhythmTranspose())
        .setPhaseOffset(rhythmPhaseOffset().toBigInteger()));
  }

  /**
   craft link support
   */
  private void craftSupport() throws Exception {
    // TODO craft support
  }

  /**
   compute (and cache) the mainIdea

   @return mainIdea
   */
  private Idea rhythmIdea() throws Exception {
    if (Objects.isNull(_rhythmIdea))
      switch (basis.type()) {

        case Continue:
          LinkChoice previousChoice = basis.previousRhythmChoice();
          if (Objects.isNull(previousChoice))
            throw new BusinessException("No rhythm-type idea chosen in previous link!");
          _rhythmIdea = basis.idea(previousChoice.getIdeaId());
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmIdea = chooseRhythm(basis.chainId(), basis.currentMacroChoice(), basis.currentMainChoice());
      }

    return _rhythmIdea;
  }

  /**
   Phase offset for rhythm-type idea choice for link
   if continues past available rhythm-type idea phases, loops back to beginning of idea phases

   @return offset of rhythm-type idea choice
   <p>
   TODO actually compute rhythm idea phase offset
   */
  private ULong rhythmPhaseOffset() throws Exception {
    if (Objects.isNull(_rhythmPhaseOffset))
      switch (basis.type()) {

        case Continue:
          _rhythmPhaseOffset = basis.previousRhythmChoice().nextPhaseOffset();
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPhaseOffset = ULong.valueOf(0);
      }

    return _rhythmPhaseOffset;
  }

  /**
   Fetch current phase of rhythm-type idea

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord rhythmPhase() throws Exception {
    PhaseRecord phase = basis.phaseByOffset(rhythmIdea().getId(), rhythmPhaseOffset());

    if (Objects.isNull(phase))
      throw new BusinessException("rhythm-phase does not exist!");

    return phase;
  }

  /**
   Transposition for rhythm-type idea choice for link

   @return +/- semitones transposition of rhythm-type idea choice
   */
  private Integer rhythmTranspose() throws Exception {
    return Key.delta(
      Value.eitherOr(rhythmPhase().getKey(), rhythmIdea().getKey())
      , basis.link().getKey(), 0);
  }

  /**
   Choose rhythm idea

   @param chainId     chain to choose idea for
   @param macroChoice macro idea chosen
   @param mainChoice  macro idea chosen
   @return rhythm-type Idea
   @throws Exception on failure
   <p>
   TODO: actually choose rhythm idea
   */
  private Idea chooseRhythm(ULong chainId, LinkChoice macroChoice, LinkChoice mainChoice) throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Idea> chooser = new Chooser<>();

    // TODO: only choose major ideas for major keys, minor for minor! [#223] Key of first Phase of chosen Rhythm-Idea must match the `minor` or `major` with the Key of the current Link.

    // (1) retrieve memes of macro idea, for use as a meme isometry comparison
    MemeIsometry memeIsometry = MemeIsometry.of(basis.linkMemes());

    // (2a) retrieve ideas bound directly to chain
    sourceRecords = ideaDAO.readAllBoundToChain(Access.internal(), chainId, Idea.RHYTHM);

    // (2b) only if none were found in the previous step, retrieve ideas bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = ideaDAO.readAllBoundToChainLibrary(Access.internal(), chainId, Idea.RHYTHM);

    // (3) score each source record based on meme isometry
    sourceRecords.forEach((record ->
      chooser.add(new Idea().setFromRecord(record),
        Chance.normallyAround(
          memeIsometry.scoreCSV(String.valueOf(record.get(Meme.KEY_MANY))),
          0.5))));

    // (3b) Avoid previous rhythm idea
    if (!basis.isInitialLink())
      chooser.score(basis.previousRhythmChoice().getIdeaId(), -SCORE_AVOID_CHOOSING_PREVIOUS_RHTYHM);

    // report
    basis.report("rhythmChoice", chooser.report());

    // (4) return the top choice
    Idea idea = chooser.getTop();
    if (Objects.nonNull(idea))
      return idea;
    else
      throw new BusinessException("Found no rhythm-type idea bound to Chain!");
  }

  /**
   Report
   */
  private void report() {
    // TODO basis.report() anything else interesting from the craft operation
  }

}
