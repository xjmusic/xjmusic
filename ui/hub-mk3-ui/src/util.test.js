// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {
  belongsTo, computeBarSize,
  computeMax,
  computeMin,
  computeSeriesFromZeroTo,
  isSuccessful,
  optional, rgbGray,
  toEntityName,
  toEntityType,
  visibleIf,
  wrapSvgInCssImageDataUrl,
} from "./util";
import React from "react";

test('converts a string to an entity name', () => {
  expect(toEntityName("libraries")).toEqual("Library");
  expect(toEntityName("library")).toEqual("Library");
  expect(toEntityName("program sequence")).toEqual("ProgramSequence");
  expect(toEntityName("program-sequences")).toEqual("ProgramSequence");
});

test('converts a string to an entity type', () => {
  expect(toEntityType("Library")).toEqual("libraries");
  expect(toEntityType("Libraries")).toEqual("libraries");
  expect(toEntityType("ProgramSequence")).toEqual("program-sequences");
  expect(toEntityType("Program Sequence")).toEqual("program-sequences");
});

test('convert a type and id to a belongs-to object', () => {
  expect(belongsTo("program sequences", "xyz"))
    .toEqual({programSequence: {data: {type: "program-sequences", id: "xyz"}}});
});

test('test whether a JSONAPI response is successful', () => {
  expect(isSuccessful(null)).toEqual(false);
  expect(isSuccessful({payload: "x"})).toEqual(false);
  expect(isSuccessful({payload: {status: 200}})).toEqual(true);
  expect(isSuccessful({payload: {status: 204}})).toEqual(true);
  expect(isSuccessful({payload: {status: 400}})).toEqual(false);
  expect(isSuccessful({payload: {status: 500}})).toEqual(false);
  expect(isSuccessful({type: "API_ORM_SUCCESS"})).toEqual(true);
});

test('minimum and maximum values from array', () => {
  expect(computeMin([4, 64, 234, 636, 79, 234, 57, 23])).toEqual(4);
  expect(computeMax([4, 64, 234, 636, 79, 234, 57, 23])).toEqual(636);
});

test('compute series inclusive from zero to maximum value', () => {
  expect(computeSeriesFromZeroTo(7)).toEqual([0, 1, 2, 3, 4, 5, 6, 7]);
});

test('visible only if condition is true', () => {
  expect(visibleIf(false, "party")).toEqual("");
  expect(visibleIf(true, "party")).toEqual("party");
  expect(visibleIf(true, "party", "x")).toEqual("party");
  expect(visibleIf(false, "party", "x")).toEqual("x");
});

test('optional prop', () => {
  const props = {one: 1};
  expect(optional(props.one)).toEqual(1);
  expect(optional(props.two)).toEqual(null);
  expect(optional(props.two, 2)).toEqual(2);
});

test('wrap SVG in CSS background-image data-image url(...)', () => {
  expect(wrapSvgInCssImageDataUrl(
    <svg xmlns="http://www.w3.org/2000/svg" width={10} height={10}>
      <rect fill="green" x="0" y="0" width={10} height={10}/>
    </svg>
  )).toEqual(`url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='10' height='10'><rect fill='green' x='0' y='0' width='10' height='10'></rect></svg>")`);
});


test('rgb string of grayscale value', () => {
  expect(rgbGray(25)).toEqual("rgb(25,25,25)")
});
