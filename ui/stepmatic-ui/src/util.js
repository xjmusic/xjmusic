export default {

  getKeyForVoiceSequence: (voice, sequence) => {
    return `${voice.id}_${sequence.id}`;
  }

}
