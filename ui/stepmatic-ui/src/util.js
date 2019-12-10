// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
export default {

  getKeyForVoiceSequence: (voice, sequence) => {
    return `${voice.id}_${sequence.id}`;
  }

}
