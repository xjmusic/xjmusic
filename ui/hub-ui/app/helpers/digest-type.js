//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { helper } from '@ember/component/helper';

/**
 Get the Digest type of a given aesthetically pleasing type.
 E.G. returns "DigestChordProgression" for value "Chord progression"
 * @param params[0] input raw string
 * @returns {string}
 */
export function digestType(params/*, hash*/) {
  return "Digest" + params[0].replace(/\s/g, '');
}

export default helper(digestType);
