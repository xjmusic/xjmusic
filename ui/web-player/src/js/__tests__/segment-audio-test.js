/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {SegmentAudio} from "../segment-audio";

jest.mock('dom');

it('can decode option key-values from URL', () => {
  expect(new SegmentAudio({}, 1529168557494, {
    offset: 23,
    waveformKey: 'audio-123.ogg',
    waveformPreroll: 0.0021,
    beginAt: '2018-07-01T06:10:31.924434Z'
  }, 'http://ship.dev.xj.io/')).not.toBeNull();
});

