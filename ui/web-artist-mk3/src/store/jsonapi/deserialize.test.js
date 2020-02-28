/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import deserialize from "./deserialize"

test('deserializes payload', () => {
  const result = deserialize({
    "id": "20fd7452-16eb-11ea-8a37-a31487a7a82a",
    "type": "program-sequence-pattern-events",
    "relationships": {
      "programSequencePattern": {
        "data": {
          "id": "20ec10f4-16eb-11ea-8a37-139689e9e2d8",
          "type": "program-sequence-patterns"
        }
      },
      "programVoiceTrack": {
        "data": {
          "id": "20e1740a-16eb-11ea-8a37-e7330ee09af0",
          "type": "program-voice-tracks"
        }
      },
      "program": {
        "data": {
          "id": "20d07cc2-16eb-11ea-8a37-c32a94270435",
          "type": "programs"
        }
      }
    },
    "attributes": {
      "duration": 0.25,
      "note": "G12",
      "velocity": 0.1,
      "position": 1.25
    }
  });

  expect(result.id).toEqual("20fd7452-16eb-11ea-8a37-a31487a7a82a");
  expect(result.programSequencePatternId).toEqual("20ec10f4-16eb-11ea-8a37-139689e9e2d8");
  expect(result.programVoiceTrackId).toEqual("20e1740a-16eb-11ea-8a37-e7330ee09af0");
  expect(result.programId).toEqual("20d07cc2-16eb-11ea-8a37-c32a94270435");
  expect(result.duration).toEqual(0.25);
  expect(result.note).toEqual("G12");
  expect(result.velocity).toEqual(0.1);
  expect(result.position).toEqual(1.25);
});

