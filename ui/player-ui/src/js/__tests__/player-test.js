// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

jest.mock('segment-audio');

import {Player} from "../player";

window.AudioContext = jest.fn().mockImplementation(() => (jest.mock('audio-context')));

it('can be instantiated with no options', ()=> {
  new Player({});
});

it('takes an chain id configuration parameter', ()=> {
  let subject = new Player({
    play: 'coolambience'
  });
  expect(subject.chainId).toBe('coolambience');
});

it('takes a start at time configuration parameter', ()=> {
  let subject = new Player({
    startAtMillisUTC: 1528750454027
  });
  expect(subject.offsetMillis).toBeLessThan(0);
});
