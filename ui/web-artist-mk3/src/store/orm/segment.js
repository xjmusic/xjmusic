/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

const ROW_HEIGHT_PIXELS_PER_BEAT = 9;

class Segment extends Model {
  toString() {
    return `Segment@${this.offset}`;
  }

  rowHeight() {
    let pixels = this.total * ROW_HEIGHT_PIXELS_PER_BEAT;
    return `${pixels}px`;
  }

  // Declare any static or instance methods you need.
}

Segment.modelName = 'Segment';

// Declare your related fields.
Segment.fields = {
  id: attr(),
  offset: attr(),
  state: attr(),
  beginAt: attr(),
  endAt: attr(),
  total: attr(),
  density: attr(),
  key: attr(),
  tempo: attr(),
  waveformKey: attr(),
  chainId: fk({
    to: 'Chain',
    as: 'chain',
    relatedName: 'segments',
  }),
  /*
    segmentMessages: many('segment-message'),
    segmentMemes: many('segment-meme'),
    segmentChoices: many('segment-choice'),
    segmentChoiceArrangements: many('segment-choice-arrangement'),
    segmentChords: many('segment-chord'),
  */
};

export default Segment;

