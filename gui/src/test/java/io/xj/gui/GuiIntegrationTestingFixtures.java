// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;

import java.util.Objects;
import java.util.UUID;


/**
 Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class GuiIntegrationTestingFixtures {
  public static Chain buildChain(Template template) {
    return buildChain(template, ChainState.FABRICATE);
  }

  public static Chain buildChain(Template template, ChainState state) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setProjectId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setName("Test Chain");
    chain.setType(ChainType.PRODUCTION);
    chain.setTemplateConfig(template.getConfig());
    chain.setState(state);
    return chain;
  }

  public static Chain buildChain(Project project, String name, ChainType type, ChainState state, Template template) {
    return buildChain(project, name, type, state, template, StringUtils.toShipKey(name));
  }

  public static Chain buildChain(Project project, Template template, String name, ChainType type, ChainState state) {
    return buildChain(project, name, type, state, template, StringUtils.toShipKey(name));
  }

  public static Chain buildChain(Project project, String name, ChainType type, ChainState state, Template template, /*@Nullable*/ String shipKey) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setProjectId(project.getId());
    chain.setName(name);
    chain.setType(type);
    chain.setState(state);
    chain.setTemplateConfig(GuiHubIntegrationTestingFixtures.TEST_TEMPLATE_CONFIG);
    if (Objects.nonNull(shipKey))
      chain.shipKey(shipKey);
    return chain;
  }

  public static Segment buildSegment() {
    var seg = new Segment();
    seg.setId(123);
    return seg;
  }

  public static Segment buildSegment(Chain chain, int offset, SegmentState state, String key, int total, float density, float tempo, String storageKey) {
    return buildSegment(chain,
      0 < offset ? SegmentType.CONTINUE : SegmentType.INITIAL,
      offset, 0, state, key, total, density, tempo, storageKey, state == SegmentState.CRAFTED);
  }


  public static Segment buildSegment(Chain chain, SegmentType type, int id, int delta, SegmentState state, String key, int total, float density, float tempo, String storageKey, boolean hasEndSet) {
    var segment = new Segment();
    segment.setChainId(chain.getId());
    segment.setType(type);
    segment.setId(id);
    segment.setDelta(delta);
    segment.setState(state);
    segment.setBeginAtChainMicros((long) (id * ValueUtils.MICROS_PER_SECOND * total * ValueUtils.SECONDS_PER_MINUTE / tempo));
    segment.setKey(key);
    segment.setTotal(total);
    segment.setDensity((double) density);
    segment.setTempo((double) tempo);
    segment.setStorageKey(storageKey);
    segment.setWaveformPreroll(0.0);
    segment.setWaveformPostroll(0.0);

    var durationMicros = (long) (ValueUtils.MICROS_PER_SECOND * total * ValueUtils.SECONDS_PER_MINUTE / tempo);
    if (hasEndSet)
      segment.setDurationMicros(durationMicros);

    return segment;
  }

  public static Segment buildSegment(Chain chain, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      0,
      SegmentState.CRAFTING,
      key, total, density, tempo, "segment123");
  }

  public static Segment buildSegment(Chain chain, int offset, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      offset,
      SegmentState.CRAFTING,
      key, total, density, tempo, "segment123");
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequenceBinding programSequenceBinding) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(programSequenceBinding.getProgramId());
    segmentChoice.setProgramSequenceBindingId(programSequenceBinding.getId());
    segmentChoice.setProgramType(programType);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequence programSequence) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(programSequence.getProgramId());
    segmentChoice.setProgramSequenceId(programSequence.getId());
    segmentChoice.setProgramType(programType);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, InstrumentType instrumentType, InstrumentMode instrumentMode) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(deltaIn);
    segmentChoice.setDeltaOut(deltaOut);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType());
    segmentChoice.setMute(false);
    segmentChoice.setInstrumentType(instrumentType);
    segmentChoice.setInstrumentMode(instrumentMode);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program, ProgramSequence programSequence, ProgramVoice voice, Instrument instrument) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setProgramVoiceId(voice.getId());
    segmentChoice.setInstrumentId(instrument.getId());
    segmentChoice.setInstrumentType(instrument.getType());
    segmentChoice.setMute(false);
    segmentChoice.setInstrumentMode(instrument.getMode());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramSequenceId(programSequence.getId());
    segmentChoice.setProgramType(program.getType());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, ProgramSequenceBinding programSequenceBinding) {
    var choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
    choice.setProgramSequenceBindingId(programSequenceBinding.getId());
    return choice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, ProgramVoice voice, Instrument instrument) {
    var choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
    choice.setProgramVoiceId(voice.getId());
    choice.setInstrumentId(instrument.getId());
    choice.setInstrumentType(instrument.getType());
    choice.setMute(false);
    choice.setInstrumentMode(instrument.getMode());
    return choice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program) {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(segment.getId());
    choice.setDeltaIn(deltaIn);
    choice.setDeltaOut(deltaOut);
    choice.setProgramId(program.getId());
    choice.setProgramType(program.getType());
    return choice;
  }
}
