// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {rgbGray} from "./util";

export const
  BG_HEIGHT = 1000,
  BG_STROKE_WIDTH = 2,
  BG_STROKE_LINE_CAP = "square",
  BG_BAR_START_LINE_COLOR = rgbGray(50),
  BG_BAR_TICK_LINE_COLOR = rgbGray(30),
  BG_CENTER_LINE_COLOR = rgbGray(60),
  BG_CENTER_LINE_DASH_ARRAY = [2, 6];

export const
  BASE_PIXELS_PER_BEAT = 100;

// Workspace timeline grid options
export const TIMELINE_GRID_OPTIONS = [
  {
    label: "1/4",
    value: 0.25,
  },
  {
    label: "1/8",
    value: 0.125,
  },
  {
    label: "1/16",
    value: 0.0625,
  },
  {
    label: "1/32",
    value: 0.03125,
  },
];

// Workspace timeline grid options
export const ZOOM_LEVEL_OPTIONS = [
  {
    label: "x/4",
    value: 0.25,
  },
  {
    label: "x/3",
    value: 0.333,
  },
  {
    label: "x/2",
    value: 0.5,
  },
  {
    label: "1x",
    value: 1.0,
  },
  {
    label: "2x",
    value: 2.0,
  },
  {
    label: "3x",
    value: 3.0,
  },
  {
    label: "4x",
    value: 4.0,
  },
];

// New entity attributes
export const
  NEW_CHORD_ATTRIBUTES = {
    name: "New",
    position: 0,
  },
  NEW_EVENT_ATTRIBUTES = {
    note: "X",
    position: 0,
    velocity: 1.0,
    duration: 1.0,
  },
  NEW_PATTERN_ATTRIBUTES = {
    name: "New",
    type: "Loop",
    total: 4.0,
  },
  NEW_TRACK_ATTRIBUTES = {
    name: "New",
  },
  NEW_VOICE_ATTRIBUTES = {
    name: "New",
    type: "Percussive",
  };
