// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model;

import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.event.Event;

public class AudioImpl implements Audio {
  private String waveform;
  private float length;
  private float start;
  private float tempo;
  private float pitch;
  private Chord[] chords = new Chord[0];
  private Event[] events = new Event[0];

  public AudioImpl(
    String _waveform,
    float _length,
    float _start,
    float _tempo,
    float _pitch
  ) {
    waveform = _waveform;
    length = _length;
    start = _start;
    tempo = _tempo;
    pitch = _pitch;
  }

  @Override
  public String Waveform() {
    return waveform;
  }

  @Override
  public float Length() {
    return length;
  }

  @Override
  public float Start() {
    return start;
  }

  @Override
  public float Tempo() {
    return tempo;
  }

  @Override
  public float Pitch() {
    return pitch;
  }

  @Override
  public Chord[] Chords() {
    return chords;
  }

  @Override
  public Event[] Events() {
    return events;
  }
}
