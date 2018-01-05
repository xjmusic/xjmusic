import EmberRouter from '@ember/routing/router';
import config from "./config/environment";
import googlePageView from "./mixins/google-pageview";

const Router = EmberRouter.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, users);
  this.route('accounts', {path: '/a'}, accounts);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
  this.route('platform', platform);
});

function users() {
  this.route('one', {path: '/:user_id'});
}

function accounts() {
  this.route('new');
  this.route('one', {path: '/:account_id'}, account);
}

function platform() {
  this.route('messages', function () {
    this.route('new');
  });
  this.route('works');
}

function account() {
  this.route('edit');
  this.route('destroy');
  this.route('users');
  this.route('libraries', {path: '/lib'}, accountLibraries);
  this.route('chains', accountChains);
}

function accountLibraries() {
  this.route('new');
  this.route('one', {path: '/:library_id'}, accountLibrary);
}

function accountLibrary() {
  this.route('edit');
  this.route('destroy');
  this.route('patterns', accountLibraryPatterns);
  this.route('instruments', accountLibraryInstruments);
}

function accountLibraryPatterns() {
  this.route('new');
  this.route('one', {path: '/:pattern_id'}, accountLibraryPattern);
}

function accountLibraryPattern() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('phases', accountLibraryPatternPhases);
  this.route('voices', accountLibraryPatternVoices);
}

function accountLibraryPatternVoices() {
  this.route('new');
  this.route('one', {path: '/:voice_id'}, accountLibraryPatternVoice);
}

function accountLibraryPatternVoice() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryPatternPhases() {
  this.route('new');
  this.route('one', {path: '/:phase_id'}, accountLibraryPatternPhase);
}

function accountLibraryPatternPhase() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('chords', accountLibraryPatternPhaseChords);
  this.route('events', accountLibraryPatternPhaseEvents);
}

function accountLibraryPatternPhaseChords() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibraryPatternPhaseChord);
}

function accountLibraryPatternPhaseChord() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryPatternPhaseEvents() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibraryPatternPhaseEvent);
}

function accountLibraryPatternPhaseEvent() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
}

function accountLibraryInstruments() {
  this.route('new');
  this.route('one', {path: '/:instrument_id'}, accountLibraryInstrument);
}

function accountLibraryInstrument() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('memes');
  this.route('audios', accountLibraryInstrumentAudios);
}

function accountLibraryInstrumentAudios() {
  this.route('new');
  this.route('one', {path: '/:audio_id'}, accountLibraryInstrumentAudio);
}

function accountLibraryInstrumentAudio() {
  this.route('edit');
  this.route('clone');
  this.route('destroy');
  this.route('events', accountLibraryInstrumentAudioEvents);
  this.route('chords', accountLibraryInstrumentAudioChords);
}

function accountLibraryInstrumentAudioEvents() {
  this.route('new');
  this.route('one', {path: '/:event_id'}, accountLibraryInstrumentAudioEvent);
}

function accountLibraryInstrumentAudioEvent() {
  this.route('edit');
  this.route('destroy');
}

function accountLibraryInstrumentAudioChords() {
  this.route('new');
  this.route('one', {path: '/:chord_id'}, accountLibraryInstrumentAudioChord);
}

function accountLibraryInstrumentAudioChord() {
  this.route('edit');
  this.route('destroy');
}

function accountChains() {
  this.route('new');
  this.route('one', {path: '/:chain_id'}, accountChain);
}

function accountChain() {
  this.route('edit');
  this.route('destroy');
  this.route('libraries');
  this.route('configs');
  this.route('patterns');
  this.route('instruments');
  this.route('links', accountChainLinks);
}

function accountChainLinks() {
  this.route('one', {path: '/:link_id'}, accountChainLink);
}

function accountChainLink() {
}

export default Router;
