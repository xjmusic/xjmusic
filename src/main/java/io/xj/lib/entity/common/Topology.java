// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.User;
import io.xj.*;
import io.xj.lib.entity.EntityFactory;

/**
 In the future, we will simplify JSON payload
 deprecating this complex abstraction of JSON:API
 in favor of plain POJO
 */
public enum Topology {
  ;

  /**
   Given a entity factory, build the Hub REST API entity topology

   @param entityFactory to build topology on
   */
  public static void buildHubApiTopology(EntityFactory entityFactory) {
    // Account
    entityFactory.register(Account.class)
      .createdBy(Account::getDefaultInstance)
      .withAttribute("name")
      .hasMany(Library.class)
      .hasMany(AccountUser.class);

    // AccountUser
    entityFactory.register(AccountUser.class)
      .createdBy(AccountUser::getDefaultInstance)
      .belongsTo(Account.class)
      .belongsTo(User.class);

    // Instrument
    entityFactory.register(Instrument.class)
      .createdBy(Instrument::getDefaultInstance)
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("density")
      .withAttribute("config")
      .belongsTo(User.class)
      .belongsTo(Library.class)
      .hasMany(InstrumentAudio.class)
      .hasMany(InstrumentMeme.class);

    // InstrumentAudio
    entityFactory.register(InstrumentAudio.class)
      .createdBy(InstrumentAudio::getDefaultInstance)
      .withAttribute("waveformKey")
      .withAttribute("name")
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("tempo")
      .withAttribute("density")
      .withAttribute("volume")
      .withAttribute("event")
      .withAttribute("note")
      .belongsTo(Instrument.class);

    // InstrumentMeme
    entityFactory.register(InstrumentMeme.class)
      .createdBy(InstrumentMeme::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Instrument.class);

    // Library
    entityFactory.register(Library.class)
      .createdBy(Library::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Account.class)
      .hasMany(Instrument.class)
      .hasMany(Program.class);

    // Program
    entityFactory.register(Program.class)
      .createdBy(Program::getDefaultInstance)
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
      .createdBy(ProgramMeme::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Program.class);

    // ProgramSequence
    entityFactory.register(ProgramSequence.class)
      .createdBy(ProgramSequence::getDefaultInstance)
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
      .createdBy(ProgramSequenceBinding::getDefaultInstance)
      .withAttribute("offset")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class)
      .hasMany(ProgramSequenceBindingMeme.class);

    // ProgramSequenceBindingMeme
    entityFactory.register(ProgramSequenceBindingMeme.class)
      .createdBy(ProgramSequenceBindingMeme::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceBinding.class);

    // ProgramSequenceChord
    entityFactory.register(ProgramSequenceChord.class)
      .createdBy(ProgramSequenceChord::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("position")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class);

    // ProgramSequenceChordVoicing
    entityFactory.register(ProgramSequenceChordVoicing.class)
      .createdBy(ProgramSequenceChordVoicing::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("notes")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceChord.class);

    // ProgramSequencePattern
    entityFactory.register(ProgramSequencePattern.class)
      .createdBy(ProgramSequencePattern::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("total")
      .withAttribute("name")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequence.class)
      .belongsTo(ProgramVoice.class)
      .hasMany(ProgramSequencePatternEvent.class);

    // ProgramSequencePatternEvent
    entityFactory.register(ProgramSequencePatternEvent.class)
      .createdBy(ProgramSequencePatternEvent::getDefaultInstance)
      .withAttribute("duration")
      .withAttribute("note")
      .withAttribute("position")
      .withAttribute("velocity")
      .belongsTo(Program.class)
      .belongsTo(ProgramSequencePattern.class)
      .belongsTo(ProgramVoiceTrack.class);

    // ProgramVoice
    entityFactory.register(ProgramVoice.class)
      .createdBy(ProgramVoice::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("order")
      .belongsTo(Program.class)
      .hasMany(ProgramSequencePattern.class);

    // ProgramVoiceTrack
    entityFactory.register(ProgramVoiceTrack.class)
      .createdBy(ProgramVoiceTrack::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("order")
      .belongsTo(Program.class)
      .belongsTo(ProgramVoice.class)
      .hasMany(ProgramSequencePatternEvent.class);

    // User
    entityFactory.register(User.class)
      .createdBy(User::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("roles")
      .withAttribute("email")
      .withAttribute("avatarUrl")
      .hasMany(UserAuth.class)
      .hasMany(UserAuthToken.class);

    // UserAuth
    entityFactory.register(UserAuth.class)
      .createdBy(UserAuth::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("externalAccessToken")
      .withAttribute("externalRefreshToken")
      .withAttribute("externalAccount")
      .belongsTo(User.class);

    // UserAuthToken
    entityFactory.register(UserAuthToken.class)
      .createdBy(UserAuthToken::getDefaultInstance)
      .withAttribute("accessToken")
      .belongsTo(User.class)
      .belongsTo(UserAuth.class);

    // UserRole
    entityFactory.register(UserRole.class)
      .createdBy(UserRole::getDefaultInstance)
      .withAttribute("type")
      .belongsTo(User.class);
  }

  /**
   Given a entity factory, build the Hub REST API entity topology

   @param entityFactory to build topology on
   */
  public static void buildNexusApiTopology(EntityFactory entityFactory) {
    // Chain
    entityFactory.register(Chain.class)
      .createdBy(Chain::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("config")
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("startAt")
      .withAttribute("stopAt")
      .withAttribute("embedKey")
      .withAttribute("fabricatedAheadSeconds")
      .belongsTo(Account.class)
      .hasMany(ChainBinding.class);

    // ChainBinding
    entityFactory.register(ChainBinding.class)
      .createdBy(ChainBinding::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("targetId")
      .belongsTo(Chain.class);

    // Segment
    entityFactory.register(Segment.class)
      .createdBy(Segment::getDefaultInstance)
      .withAttribute("state")
      .withAttribute("beginAt")
      .withAttribute("endAt")
      .withAttribute("key")
      .withAttribute("total")
      .withAttribute("offset")
      .withAttribute("offsetMain")
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
      .createdBy(SegmentChoice::getDefaultInstance)
      .withAttribute("programType")
      .withAttribute("segmentType")
      .withAttribute("isInertial")
      .belongsTo(Instrument.class)
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceBinding.class)
      .belongsTo(ProgramVoice.class)
      .belongsTo(Segment.class)
      .hasMany(SegmentChoiceArrangement.class);

    // SegmentChoiceArrangement
    entityFactory.register(SegmentChoiceArrangement.class)
      .createdBy(SegmentChoiceArrangement::getDefaultInstance)
      .belongsTo(ProgramSequencePattern.class)
      .belongsTo(Segment.class)
      .belongsTo(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangementPick.class);

    // SegmentChoiceArrangementPick
    entityFactory.register(SegmentChoiceArrangementPick.class)
      .createdBy(SegmentChoiceArrangementPick::getDefaultInstance)
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("amplitude")
      .withAttribute("name")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChordVoicing.class)
      .belongsTo(InstrumentAudio.class)
      .belongsTo(ProgramSequencePatternEvent.class);

    // SegmentChord
    entityFactory.register(SegmentChord.class)
      .createdBy(SegmentChord::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("position")
      .hasMany(SegmentChordVoicing.class)
      .belongsTo(Segment.class);

    // SegmentChordVoicing
    entityFactory.register(SegmentChordVoicing.class)
      .createdBy(SegmentChord::getDefaultInstance)
      .withAttribute("notes")
      .withAttribute("type")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChord.class);

    // SegmentMeme
    entityFactory.register(SegmentMeme.class)
      .createdBy(SegmentMeme::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Segment.class);

    // SegmentMessage
    entityFactory.register(SegmentMessage.class)
      .createdBy(SegmentMessage::getDefaultInstance)
      .withAttribute("body")
      .withAttribute("type")
      .belongsTo(Segment.class);
  }
}
