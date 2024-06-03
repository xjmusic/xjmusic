// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/music/Step.h"

namespace XJ {

  std::map<PitchClass, Step> Step::stepUpMap = {
      {Atonal, Step(Atonal, 0)},
      {C, Step(Cs, 0)},
      {Cs, Step(D, 0)},
      {D, Step(Ds, 0)},
      {Ds, Step(E, 0)},
      {E, Step(F, 0)},
      {F, Step(Fs, 0)},
      {Fs, Step(G, 0)},
      {G, Step(Gs, 0)},
      {Gs, Step(A, 0)},
      {A, Step(As, 0)},
      {As, Step(B, 0)},
      {B, Step(C, 1)},
  };

  std::map<PitchClass, Step> Step::stepDownMap = {
      {Atonal, Step(Atonal, 0)},
      {C, Step(B, -1)},
      {Cs, Step(C, 0)},
      {D, Step(Cs, 0)},
      {Ds, Step(D, 0)},
      {E, Step(Ds, 0)},
      {F, Step(E, 0)},
      {Fs, Step(F, 0)},
      {G, Step(Fs, 0)},
      {Gs, Step(G, 0)},
      {A, Step(Gs, 0)},
      {As, Step(A, 0)},
      {B, Step(As, 0)},
  };

  std::map<PitchClass, std::map<PitchClass, int>> Step::deltaMap = {
      {C, {
              {C, 0},
              {Cs, 1},
              {D, 2},
              {Ds, 3},
              {E, 4},
              {F, 5},
              {Fs, -6},
              {G, -5},
              {Gs, -4},
              {A, -3},
              {As, -2},
              {B, -1},
          }},
      {Cs, {
               {C, -1},
               {Cs, 0},
               {D, 1},
               {Ds, 2},
               {E, 3},
               {F, 4},
               {Fs, 5},
               {G, -6},
               {Gs, -5},
               {A, -4},
               {As, -3},
               {B, -2},
           }},
      {D, {
              {C, -2},
              {Cs, -1},
              {D, 0},
              {Ds, 1},
              {E, 2},
              {F, 3},
              {Fs, 4},
              {G, 5},
              {Gs, -6},
              {A, -5},
              {As, -4},
              {B, -3},
          }},
      {Ds, {
               {C, -3},
               {Cs, -2},
               {D, -1},
               {Ds, 0},
               {E, 1},
               {F, 2},
               {Fs, 3},
               {G, 4},
               {Gs, 5},
               {A, -6},
               {As, -5},
               {B, -4},
           }},
      {E, {
              {C, -4},
              {Cs, -3},
              {D, -2},
              {Ds, -1},
              {E, 0},
              {F, 1},
              {Fs, 2},
              {G, 3},
              {Gs, 4},
              {A, 5},
              {As, -6},
              {B, -5},
          }},
      {F, {
              {C, -5},
              {Cs, -4},
              {D, -3},
              {Ds, -2},
              {E, -1},
              {F, 0},
              {Fs, 1},
              {G, 2},
              {Gs, 3},
              {A, 4},
              {As, 5},
              {B, -6},
          }},
      {Fs, {
               {C, -6},
               {Cs, -5},
               {D, -4},
               {Ds, -3},
               {E, -2},
               {F, -1},
               {Fs, 0},
               {G, 1},
               {Gs, 2},
               {A, 3},
               {As, 4},
               {B, 5},
           }},
      {G, {
              {C, 5},
              {Cs, -6},
              {D, -5},
              {Ds, -4},
              {E, -3},
              {F, -2},
              {Fs, -1},
              {G, 0},
              {Gs, 1},
              {A, 2},
              {As, 3},
              {B, 4},
          }},
      {Gs, {
               {C, 4},
               {Cs, 5},
               {D, -6},
               {Ds, -5},
               {E, -4},
               {F, -3},
               {Fs, -2},
               {G, -1},
               {Gs, 0},
               {A, 1},
               {As, 2},
               {B, 3},
           }},
      {A, {
              {C, 3},
              {Cs, 4},
              {D, 5},
              {Ds, -6},
              {E, -5},
              {F, -4},
              {Fs, -3},
              {G, -2},
              {Gs, -1},
              {A, 0},
              {As, 1},
              {B, 2},
          }},
      {As, {
               {C, 2},
               {Cs, 3},
               {D, 4},
               {Ds, 5},
               {E, -6},
               {F, -5},
               {Fs, -4},
               {G, -3},
               {Gs, -2},
               {A, -1},
               {As, 0},
               {B, 1},
           }},
      {B, {
              {C, 1},
              {Cs, 2},
              {D, 3},
              {Ds, 4},
              {E, 5},
              {F, -6},
              {Fs, -5},
              {G, -4},
              {Gs, -3},
              {A, -2},
              {As, -1},
              {B, 0},
          }},
  };

  Step::Step(PitchClass pitchClass, int deltaOctave) {
    this->pitchClass = pitchClass;
    this->deltaOctave = deltaOctave;
  }

  Step Step::to(PitchClass pitchClass, int deltaOctave) {
    return {pitchClass, deltaOctave};
  }

  int Step::delta(PitchClass from, PitchClass to) {
    if (from == Atonal || to == Atonal) return 0;
    return deltaMap.at(from).at(to);
  }

  Step Step::step(PitchClass from, int inc) {
    if (0 < inc)
      return stepUp(from, inc);
    else if (0 > inc)
      return stepDown(from, -inc);
    else
      return {from, 0};
  }

  Step Step::stepUp(PitchClass from, int inc) {
    PitchClass to = from;
    int newOctave = 0;
    for (int i = 0; i < inc; i++) {
      Step step = stepUpMap.at(to);
      to = step.pitchClass;
      newOctave += step.deltaOctave;
    }
    return {to, newOctave};
  }

  Step Step::stepDown(PitchClass from, int inc) {
    PitchClass to = from;
    int newOctave = 0;
    for (int i = 0; i < inc; i++) {
      Step step = stepDownMap.at(to);
      to = step.pitchClass;
      newOctave += step.deltaOctave;
    }
    return {to, newOctave};
  }

}// namespace XJ