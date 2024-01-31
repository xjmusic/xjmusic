// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus;

import io.xj.hub.entity.EntityFactory;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMeta;

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
      .withAttribute("type")
      .withAttribute("shipKey")
      .withAttribute("templateConfig")
      .belongsTo(Project.class)
      .belongsTo(Template.class);

    // Segment
    entityFactory.register(Segment.class)
      .createdBy(Segment::new)
      .withAttribute("state")
      .withAttribute("beginAtChainMicros")
      .withAttribute("durationMicros")
      .withAttribute("key")
      .withAttribute("total")
      .withAttribute("offset")
      .withAttribute("delta")
      .withAttribute("intensity")
      .withAttribute("tempo")
      .withAttribute("shipKey")
      .withAttribute("waveformPreroll")
      .withAttribute("waveformPostroll")
      .withAttribute("type")
      .belongsTo(Chain.class)
      .hasMany(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangement.class)
      .hasMany(SegmentChoiceArrangementPick.class)
      .hasMany(SegmentChord.class)
      .hasMany(SegmentChordVoicing.class)
      .hasMany(SegmentMeme.class)
      .hasMany(SegmentMessage.class)
      .hasMany(SegmentMeta.class);

    // SegmentChoice
    entityFactory.register(SegmentChoice.class)
      .createdBy(SegmentChoice::new)
      .withAttribute("deltaIn")
      .withAttribute("deltaOut")
      .withAttribute("instrumentMode")
      .withAttribute("instrumentType")
      .withAttribute("mute")
      .withAttribute("programType")
      .withAttribute("segmentType")
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
      .withAttribute("startAtSegmentMicros")
      .withAttribute("lengthMicros")
      .withAttribute("amplitude")
      .withAttribute("name")
      .withAttribute("tones")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChordVoicing.class)
      .belongsTo(SegmentChoiceArrangement.class)
      .belongsTo(InstrumentAudio.class)
      .belongsTo(ProgramSequencePatternEvent.class);

    // SegmentChord
    entityFactory.register(SegmentChord.class)
      .createdBy(SegmentChord::new)
      .withAttribute("name")
      .withAttribute("position") // atMicros or position?
      .hasMany(SegmentChordVoicing.class)
      .belongsTo(ProgramSequenceChord.class)
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

    // SegmentMeta
    entityFactory.register(SegmentMeta.class)
      .createdBy(SegmentMeta::new)
      .withAttribute("key")
      .withAttribute("value")
      .belongsTo(Segment.class);
  }
}
