// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model;

import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.event.Event;

public interface Audio {
  /**
   * Audio Waveform (path in filesystem)
   *
   * @return
   * TODO replace with Waveform (String, filesystem) type
   */
  String Waveform();

  /**
   * Audio Length (seconds)
   *
   * @return
   * TODO replace with Length (seconds) type
   */
  float Length();

  /**
   * Audio Start (seconds from beginning of waveform)
   *
   * @return
   * TODO replace with Time (float, seconds) type
   */
  float Start();

  /**
   * Audio Tempo (beats per minute)
   *
   * @return
   * TODO replace with Tempo (float) type
   */
  float Tempo();

  /**
   * Audio Pitch (Hz)
   *
   * @return float
   * TODO replace with Pitch (float, Hz) type
   */
  float Pitch();

  /**
   * Audio Chords
   *
   * @return Chord[]
   */
  Chord[] Chords();

  /**
   * Audio Events
   *
   * @return Event[]
   */
  Event[] Events();
}
