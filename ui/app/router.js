// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';
import config from "./config/environment";
import googlePageView from './mixins/google-pageview';

const Router = Ember.Router.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, users);
  this.route('accounts', {path: '/a'}, accounts);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
});

function users() {
  this.route('one', {path: '/:user_id'});
}

function accounts() {
  this.route('new'); // New Account
  this.route('one', {path: '/:account_id'}, account); // One Account
}

function account() {
  this.route('edit'); // Edit Account
  this.route('users'); // Users in Account
  this.route('libraries', {path: '/lib'}, accountLibraries); // Libraries in Account
  this.route('chains', accountChains); // Chains in Account
}

function accountLibraries() {
  this.route('new'); // New Library
  this.route('one', {path: '/:library_id'}, accountLibrary); // One Library
}

function accountLibrary() {
  this.route('edit'); // Edit Library
  this.route('ideas', accountLibraryIdeas); // Ideas in Library
  this.route('instruments', accountLibraryInstruments); // Instruments in Library
}

function accountLibraryIdeas() {
  this.route('new'); // New Idea
  this.route('one', {path: '/:idea_id'}, accountLibraryIdea); // One Idea
}

function accountLibraryIdea() {
  this.route('edit'); // Edit Idea
  this.route('memes'); // Memes in Idea
  this.route('phases', accountLibraryIdeaPhases); // Phases in Idea
}

function accountLibraryIdeaPhases() {
  this.route('new'); // New Phase
  this.route('one', {path: '/:phase_id'}, accountLibraryIdeaPhase); // One Phase
}

function accountLibraryIdeaPhase() {
  this.route('edit'); // Edit Phase
  this.route('memes'); // Memes in Phase
  this.route('chords', accountLibraryIdeaPhaseChords); // Chords in Phase
  this.route('voices', accountLibraryIdeaPhaseVoices); // Voices in Phase
}

function accountLibraryIdeaPhaseChords() {
  this.route('new'); // New Chord
  this.route('one', {path: '/:chord_id'}, accountLibraryIdeaPhaseChord); // One Chord
}

function accountLibraryIdeaPhaseChord() {
  this.route('edit'); // Edit Phase
}

function accountLibraryIdeaPhaseVoices() {
  this.route('new'); // New Voice
  this.route('one', {path: '/:voice_id'}, accountLibraryIdeaPhaseVoice); // One Voice
}

function accountLibraryIdeaPhaseVoice() {
  this.route('edit'); // Edit Phase
  this.route('events', accountLibraryIdeaPhaseVoiceEvents); // Events in Voice
}

function accountLibraryIdeaPhaseVoiceEvents() {
  this.route('new'); // New Event
  this.route('one', {path: '/:event_id'}, accountLibraryIdeaPhaseVoiceEvent); // One Event
}

function accountLibraryIdeaPhaseVoiceEvent() {
  this.route('edit'); // Edit Event
}

function accountLibraryInstruments() {
  this.route('new'); // New Instrument
  this.route('one', {path: '/:instrument_id'}, accountLibraryInstrument); // One Instrument
}

function accountLibraryInstrument() {
  this.route('edit'); // Edit Instrument
  this.route('memes'); // Memes in Instrument
  this.route('audios', accountLibraryInstrumentAudios);
}

function accountLibraryInstrumentAudios() {
  this.route('new'); // New Audio
  this.route('one', {path: '/:audio_id'}, accountLibraryInstrumentAudio); // One Audio
}

function accountLibraryInstrumentAudio() {
  this.route('edit'); // Edit Audio
  this.route('events', accountLibraryInstrumentAudioEvents); // Events in Audio
  this.route('chords', accountLibraryInstrumentAudioChords); // Chords in Audio
}

function accountLibraryInstrumentAudioEvents() {
  this.route('new'); // New Event
  this.route('one', {path: '/:event_id'}, accountLibraryInstrumentAudioEvent); // One Event
}

function accountLibraryInstrumentAudioEvent() {
  this.route('edit'); // Edit Event
}

function accountLibraryInstrumentAudioChords() {
  this.route('new'); // New Chord
  this.route('one', {path: '/:chord_id'}, accountLibraryInstrumentAudioChord); // One Chord
}

function accountLibraryInstrumentAudioChord() {
  this.route('edit'); // Edit Chord
}

function accountChains() {
  this.route('new'); // New Chain
  this.route('one', {path: '/:chain_id'}, accountChain); // One Chain
}

function accountChain() {
  this.route('edit'); // Edit Chain
  this.route('libraries'); // Libraries in Chain
  this.route('links', accountChainLinks); // Links in Chain
}

function accountChainLinks() {
  this.route('one', {path: '/:link_id'}, accountChainLink); // One Link
}

function accountChainLink() {}

export default Router;
