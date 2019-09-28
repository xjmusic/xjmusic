export default {

  /**
   * Get Tracks for a given Voice
   * @param voice
   */
  getTracksForVoice: (state) => (voice) => {
    return state.program.tracks.filter(track => track.voice.id === voice.id)
  },

  /**
   * Get Patterns for a given Voice
   * @param voice
   */
  getPatternsForVoice: (state) => (voice) => {
    if (state.activeSequence != null)
      return state.program.patterns.filter(pattern => pattern.voice.id === voice.id && pattern.sequence.id === state.activeSequence.id);
    else return [];
  },

  /**
   * Get Events for a given Pattern & Track
   * @param pattern
   * @param track
   */
  getEventsForPatternTrack: (state) => (pattern, track) => {
    return state.program.events.filter(event => event.pattern.id === pattern.id && event.track.id === track.id);
  }

};
