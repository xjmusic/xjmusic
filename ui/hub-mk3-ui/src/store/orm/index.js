// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import {ORM} from "redux-orm"
// app
import Account from "./account"
import AccountUser from "./account-user"
import Chain from "./chain"
import ChainBinding from "./chain-binding"
import ChainConfig from "./chain-config"
import Instrument from "./instrument"
import InstrumentAudio from "./instrument-audio"
import InstrumentAudioChord from "./instrument-audio-chord"
import InstrumentAudioEvent from "./instrument-audio-event"
import InstrumentMeme from "./instrument-meme"
import Library from "./library"
import PlatformMessage from "./platform-message"
import Program from "./program"
import ProgramMeme from "./program-meme"
import ProgramSequence from "./program-sequence"
import ProgramSequenceBinding from "./program-sequence-binding"
import ProgramSequenceBindingMeme from "./program-sequence-binding-meme"
import ProgramSequenceChord from "./program-sequence-chord"
import ProgramSequencePattern from "./program-sequence-pattern"
import ProgramSequencePatternEvent from "./program-sequence-pattern-event"
import ProgramVoice from "./program-voice"
import ProgramVoiceTrack from "./program-voice-track"
import Segment from "./segment"
import SegmentChoice from "./segment-choice"
import SegmentChoiceArrangement from "./segment-choice-arrangement"
import SegmentChoiceArrangementPick from "./segment-choice-arrangement-pick"
import SegmentChord from "./segment-chord"
import SegmentMeme from "./segment-meme"
import SegmentMessage from "./segment-message"
import User from "./user"
import Work from "./work"

const orm = new ORM({
  stateSelector: state => state.orm,
});

orm.register(
  Account,
  AccountUser,
  Chain,
  ChainBinding,
  ChainConfig,
  Instrument,
  InstrumentAudio,
  InstrumentAudioChord,
  InstrumentAudioEvent,
  InstrumentMeme,
  Library,
  PlatformMessage,
  Program,
  ProgramMeme,
  ProgramSequence,
  ProgramSequenceBinding,
  ProgramSequenceBindingMeme,
  ProgramSequenceChord,
  ProgramSequencePattern,
  ProgramSequencePatternEvent,
  ProgramVoice,
  ProgramVoiceTrack,
  Segment,
  SegmentChoice,
  SegmentChoiceArrangement,
  SegmentChoiceArrangementPick,
  SegmentChord,
  SegmentMeme,
  SegmentMessage,
  User,
  Work,
);

export default orm;
