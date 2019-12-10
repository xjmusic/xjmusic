// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {Application} from "../application";

jest.mock('dom');
jest.mock('player');

it('can decode option key-values from URL', () => {
  expect(new Application("http://localhost/#one=1&two=2&word=def").options).toEqual({one: 1, two: 2, word: "def"});
});

