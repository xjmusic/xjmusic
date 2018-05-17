// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

jest.mock('binary-resource');

import {SegmentAudio} from "../segment-audio";

it('can decode option key-values from URL', () => {
  expect(new SegmentAudio({}, 1529168557494, {}, 'http://ship.dev.xj.io/')).not.toBeNull();
});

