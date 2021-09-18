// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

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
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentAuthorship;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.InstrumentMessage;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramAuthorship;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramMessage;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.lib.entity.EntityFactory;

/**
 In the future, we will simplify JSON payload
 deprecating this complex abstraction of JSON:API
 in favor of plain POJO
 */
public enum Topology {
  ;

  /**
   Given an entity factory, build the Hub REST API entity topology

   @param entityFactory to build topology on
   */
  public static void buildHubApiTopology(EntityFactory entityFactory) {
    // Account
    entityFactory.register(Account.class)
      .createdBy(Account::new)
      .withAttribute("name")
      .hasMany(Library.class)
      .hasMany(AccountUser.class);

    // AccountUser
    entityFactory.register(AccountUser.class)
      .createdBy(AccountUser::new)
      .belongsTo(Account.class)
      .belongsTo(User.class);

    // Instrument
    entityFactory.register(Instrument.class)
      .createdBy(Instrument::new)
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("density")
      .withAttribute("config")
      .belongsTo(User.class)
      .belongsTo(Library.class)
      .hasMany(InstrumentAudio.class)
      .hasMany(InstrumentAuthorship.class)
      .hasMany(InstrumentMeme.class)
      .hasMany(InstrumentMessage.class);

    // InstrumentAudio
    entityFactory.register(InstrumentAudio.class)
      .createdBy(InstrumentAudio::new)
      .withAttribute("waveformKey")
      .withAttribute("name")
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("tempo")
      .withAttribute("density")
      .withAttribute("volume")
      .withAttribute("note")
      .withAttribute("event")
      .belongsTo(Instrument.class);

    // InstrumentMeme
    entityFactory.register(InstrumentMeme.class)
      .createdBy(InstrumentMeme::new)
      .withAttribute("name")
      .belongsTo(Instrument.class);

    // InstrumentMessage
    entityFactory.register(InstrumentMessage.class)
      .createdBy(InstrumentMessage::new)
      .withAttribute("body")
      .belongsTo(User.class)
      .belongsTo(Instrument.class);

    // InstrumentAuthorship
    entityFactory.register(InstrumentAuthorship.class)
      .createdBy(InstrumentAuthorship::new)
      .withAttribute("hours")
      .withAttribute("description")
      .belongsTo(User.class)
      .belongsTo(Instrument.class);

    // Library
    entityFactory.register(Library.class)
      .createdBy(Library::new)
      .withAttribute("name")
      .belongsTo(Account.class)
      .hasMany(Instrument.class)
      .hasMany(Program.class);

    // Program
    entityFactory.register(Program.class)
      .createdBy(Program::new)
      .withAttribute("state")
      .withAttribute("key")
      .withAttribute("tempo")
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("density")
      .withAttribute("config")
      .belongsTo(User.class)
      .belongsTo(Library.class)
      .hasMany(ProgramMeme.class)
      .hasMany(ProgramSequence.class)
      .hasMany(ProgramSequenceChord.class)
      .hasMany(ProgramSequencePattern.class)
      .hasMany(ProgramVoiceTrack.class)
      .hasMany(ProgramSequencePatternEvent.class)
      .hasMany(ProgramSequenceBinding.class)
      .hasMany(ProgramSequenceBindingMeme.class)
      .hasMany(ProgramVoice.class);

    // ProgramMeme
    entityFactory.register(ProgramMeme.class)
      .createdBy(ProgramMeme::new)
      .withAttribute("name")
      .belongsTo(Program.class);

    // ProgramMessage
    entityFactory.register(ProgramMessage.class)
      .createdBy(ProgramMessage::new)
      .withAttribute("body")
      .belongsTo(User.class)
      .belongsTo(Program.class);

    // ProgramAuthorship
    entityFactory.register(ProgramAuthorship.class)
      .createdBy(ProgramAuthorship::new)
      .withAttribute("hours")
      .withAttribute("description")
      .belongsTo(User.class)
      .belongsTo(Program.class);

    // ProgramSequence
    entityFactory.register(ProgramSequence.class)
      .createdBy(ProgramSequence::new)
      .withAttribute("name")
      .withAttribute("key")
      .withAttribute("density")
      .withAttribute("total")
      .withAttribute("tempo")
      .belongsTo(Program.class)
      .hasMany(ProgramSequencePattern.class)
      .hasMany(ProgramSequenceBinding.class)
      .hasMany(ProgramSequenceChord.class);

    // ProgramSequenceBinding
    entityFactory.register(ProgramSequenceBinding.class)
      .createdBy(ProgramSequenceBinding::new)
      .withAttribute("offset")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class)
      .hasMany(ProgramSequenceBindingMeme.class);

    // ProgramSequenceBindingMeme
    entityFactory.register(ProgramSequenceBindingMeme.class)
      .createdBy(ProgramSequenceBindingMeme::new)
      .withAttribute("name")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceBinding.class);

    // ProgramSequenceChord
    entityFactory.register(ProgramSequenceChord.class)
      .createdBy(ProgramSequenceChord::new)
      .withAttribute("name")
      .withAttribute("position")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class);

    // ProgramSequenceChordVoicing
    entityFactory.register(ProgramSequenceChordVoicing.class)
      .createdBy(ProgramSequenceChordVoicing::new)
      .withAttribute("type")
      .withAttribute("notes")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceChord.class);

    // ProgramSequencePattern
    entityFactory.register(ProgramSequencePattern.class)
      .createdBy(ProgramSequencePattern::new)
      .withAttribute("type")
      .withAttribute("total")
      .withAttribute("name")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class)
      .belongsTo(ProgramVoice.class)
      .hasMany(ProgramSequencePatternEvent.class);

    // ProgramSequencePatternEvent
    entityFactory.register(ProgramSequencePatternEvent.class)
      .createdBy(ProgramSequencePatternEvent::new)
      .withAttribute("duration")
      .withAttribute("note")
      .withAttribute("position")
      .withAttribute("velocity")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequencePattern.class)
      .belongsTo(ProgramVoiceTrack.class);

    // ProgramVoice
    entityFactory.register(ProgramVoice.class)
      .createdBy(ProgramVoice::new)
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("order")
      .belongsTo(Program.class)
      .hasMany(ProgramSequencePattern.class);

    // ProgramVoiceTrack
    entityFactory.register(ProgramVoiceTrack.class)
      .createdBy(ProgramVoiceTrack::new)
      .withAttribute("name")
      .withAttribute("order")
      .belongsTo(Program.class)
      .belongsTo(ProgramVoice.class)
      .hasMany(ProgramSequencePatternEvent.class);

    // User
    entityFactory.register(User.class)
      .createdBy(User::new)
      .withAttribute("name")
      .withAttribute("roles")
      .withAttribute("email")
      .withAttribute("avatarUrl")
      .hasMany(UserAuth.class)
      .hasMany(UserAuthToken.class)
      .hasMany(InstrumentMessage.class)
      .hasMany(InstrumentAuthorship.class)
      .hasMany(ProgramMessage.class)
      .hasMany(ProgramAuthorship.class);

    // UserAuth
    entityFactory.register(UserAuth.class)
      .createdBy(UserAuth::new)
      .withAttribute("type")
      .withAttribute("externalAccessToken")
      .withAttribute("externalRefreshToken")
      .withAttribute("externalAccount")
      .belongsTo(User.class);

    // UserAuthToken
    entityFactory.register(UserAuthToken.class)
      .createdBy(UserAuthToken::new)
      .withAttribute("accessToken")
      .belongsTo(User.class)
      .belongsTo(UserAuth.class);

    // Template
    entityFactory.register(Template.class)
      .createdBy(Template::new)
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("config")
      .withAttribute("embedKey")
      .belongsTo(Account.class)
      .hasMany(TemplateBinding.class);

    // TemplateBinding
    entityFactory.register(TemplateBinding.class)
      .createdBy(TemplateBinding::new)
      .withAttribute("type")
      .withAttribute("targetId")
      .belongsTo(Template.class);

    // TemplateBinding
    entityFactory.register(TemplatePlayback.class)
      .createdBy(TemplatePlayback::new)
      .withAttribute("createdAt")
      .belongsTo(Template.class)
      .belongsTo(User.class);
  }


  /**
   Given a entity factory, build the Hub REST API entity topology

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
