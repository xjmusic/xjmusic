import EmberRouter from '@ember/routing/router';
import config from "./config/environment";
import googlePageView from "./mixins/google-pageview";

const Router = EmberRouter.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, userIndex);
  this.route('accounts', {path: '/a'}, accountIndex);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
  this.route('platform', platform);
  this.route('go', go);
});

function userIndex() {
  this.route('one', {path: '/:user_id'});
}

function accountIndex() {
  this.route('new');
  this.route('one', {path: '/:account_id'}, accountOne);
}

function accountOne() {
  this.route('edit');
  this.route('destroy');
  this.route('users');
  this.route('libraries', {path: '/lib'}, accountLibraryIndex);
  this.route('chains', accountChainIndex);
}

function accountLibraryIndex() {
  this.route('new');
  this.route('one', {path: '/:library_id'}, accountLibraryOne);
}

function accountLibraryOne() {
  this.route('edit');
  this.route('destroy');
  this.route('patterns', accountLibraryPatternIndex);
  this.route('instruments', accountLibraryInstrumentIndex);
  this.route('digest');
}

function accountLibraryPatternIndex() {
  this.route('new');
  this.route('one', {path: '/:pattern_id'}, accountLibraryPatternOne);
}

function accountLibraryPatternOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('phases', accountLibraryPatternPhaseIndex);
  this.route('voices', accountLibraryPatternVoiceIndex);
}

function accountLibraryPatternVoiceIndex() {
  this.route('new');
  this.route('one', {path: '/:voice_id'}, accountLibraryPatternVoiceOne);
}

function accountLibraryPatternVoiceOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryPatternPhaseIndex() {
  this.route('new');
  this.route('one', {path: '/:phase_id'}, accountLibraryPatternPhaseOne);
}

function accountLibraryPatternPhaseOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('chords', accountLibraryPatternPhaseChordIndex);
  this.route('events', accountLibraryPatternPhaseEventIndex);
}

function accountLibraryPatternPhaseChordIndex() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibraryPatternPhaseChordOne);
}

function accountLibraryPatternPhaseChordOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryPatternPhaseEventIndex() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibraryPatternPhaseEventOne);
}

function accountLibraryPatternPhaseEventOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
}

function accountLibraryInstrumentIndex() {
  this.route('new');
  this.route('one', {path: '/:instrument_id'}, accountLibraryInstrumentOne);
}

function accountLibraryInstrumentOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('audios', accountLibraryInstrumentAudioIndex);
}

function accountLibraryInstrumentAudioIndex() {
  this.route('new');
  this.route('one', {path: '/:audio_id'}, accountLibraryInstrumentAudioOne);
}

function accountLibraryInstrumentAudioOne() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('events', accountLibraryInstrumentAudioEventIndex);
  this.route('chords', accountLibraryInstrumentAudioChordIndex);
}

function accountLibraryInstrumentAudioEventIndex() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibraryInstrumentAudioEventOne);
}

function accountLibraryInstrumentAudioEventOne() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryInstrumentAudioChordIndex() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibraryInstrumentAudioChordOne);
}

function accountLibraryInstrumentAudioChordOne() {
  this.route('edit');
  this.route('destroy');
}

function accountChainIndex() {
  this.route('new');
  this.route('one', {path: '/:chain_id'}, accountChainOne);
}

function accountChainOne() {
  this.route('edit');
  this.route('destroy');
  this.route('libraries');
  this.route('configs');
  this.route('patterns');
  this.route('instruments');
  this.route('links', accountChainLinkIndex);
}

function accountChainLinkIndex() {
  this.route('one', {path: '/:link_id'}, accountChainLinkOne);
}

function accountChainLinkOne() {
}

function platform() {
  this.route('messages', function () {
    this.route('new');
  });
  this.route('works');
}

function go() {
  this.route('audio', {path: '/audio/:audio_id'});
  this.route('phase', {path: '/phase/:phase_id'});
  this.route('pattern', {path: '/pattern/:pattern_id'});
  this.route('instrument', {path: '/instrument/:instrument_id'});
}

export default Router;
