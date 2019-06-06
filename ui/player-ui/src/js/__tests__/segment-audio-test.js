// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

jest.mock('binary-resource');

import {SegmentAudio} from "../segment-audio";

it('can decode option key-values from URL', () => {
  expect(new SegmentAudio({}, 1529168557494, {
    offset: 23,
    waveformKey: 'audio-123.ogg',
    beginAt: '2018-07-01T06:10:31.924434Z'
  }, 'http://ship.dev.xj.io/')).not.toBeNull();
});

