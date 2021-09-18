// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import io.xj.api.Chain;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentMessage;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;

/**
 In the future, we will simplify JSON payload
 deprecating this complex abstraction of JSON:API
 in favor of plain POJO
 */
public enum NexusTopology {
  ;

  /**
   Given an entity factory, build the Hub REST API entity topology

   @param entityFactory to build topology on
   */
  public static void buildNexusApiTopology(EntityFactory entityFactory) {
    // Chain
    entityFactory.register(Chain.class)
      .createdBy(Chain::new)
      .withAttribute("name")
      .withAttribute("state")
      .withAttribute("startAt")
      .withAttribute("stopAt")
      .withAttribute("fabricatedAheadSeconds")
      .belongsTo(Account.class)
      .belongsTo(Template.class);

    // Segment
    entityFactory.register(Segment.class)
      .createdBy(Segment::new)
      .withAttribute("state")
      .withAttribute("beginAt")
      .withAttribute("endAt")
      .withAttribute("key")
      .withAttribute("total")
      .withAttribute("offset")
      .withAttribute("delta")
      .withAttribute("density")
      .withAttribute("tempo")
      .withAttribute("storageKey")
      .withAttribute("outputEncoder")
      .withAttribute("waveformPreroll")
      .withAttribute("type")
      .belongsTo(Chain.class)
      .hasMany(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangement.class)
      .hasMany(SegmentChoiceArrangementPick.class)
      .hasMany(SegmentChord.class)
      .hasMany(SegmentChordVoicing.class)
      .hasMany(SegmentMeme.class)
      .hasMany(SegmentMessage.class);

    // SegmentChoice
    entityFactory.register(SegmentChoice.class)
      .createdBy(SegmentChoice::new)
      .withAttribute("programType")
      .withAttribute("segmentType")
      .withAttribute("deltaIn")
      .withAttribute("deltaOut")
      .belongsTo(Instrument.class)
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceBinding.class)
      .belongsTo(ProgramVoice.class)
      .belongsTo(Segment.class)
      .hasMany(SegmentChoiceArrangement.class);

    // SegmentChoiceArrangement
    entityFactory.register(SegmentChoiceArrangement.class)
      .createdBy(SegmentChoiceArrangement::new)
      .belongsTo(ProgramSequencePattern.class)
      .belongsTo(Segment.class)
      .belongsTo(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangementPick.class);

    // SegmentChoiceArrangementPick
    entityFactory.register(SegmentChoiceArrangementPick.class)
      .createdBy(SegmentChoiceArrangementPick::new)
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("amplitude")
      .withAttribute("name")
      .withAttribute("note")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChordVoicing.class)
      .belongsTo(SegmentChoiceArrangement.class)
      .belongsTo(InstrumentAudio.class)
      .belongsTo(ProgramSequencePatternEvent.class);

    // SegmentChord
    entityFactory.register(SegmentChord.class)
      .createdBy(SegmentChord::new)
      .withAttribute("name")
      .withAttribute("position")
      .hasMany(SegmentChordVoicing.class)
      .belongsTo(Segment.class);

    // SegmentChordVoicing
    entityFactory.register(SegmentChordVoicing.class)
      .createdBy(SegmentChordVoicing::new)
      .withAttribute("notes")
      .withAttribute("type")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChord.class);

    // SegmentMeme
    entityFactory.register(SegmentMeme.class)
      .createdBy(SegmentMeme::new)
      .withAttribute("name")
      .belongsTo(Segment.class);

    // SegmentMessage
    entityFactory.register(SegmentMessage.class)
      .createdBy(SegmentMessage::new)
      .withAttribute("body")
      .withAttribute("type")
      .belongsTo(Segment.class);
  }
}
