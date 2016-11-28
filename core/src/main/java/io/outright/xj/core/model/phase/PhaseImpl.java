// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.voice.Voice;

public class PhaseImpl implements Phase {
  private String name;
  private int offset;
  private float total;
  private float density;
  private String key;
  private float tempo;
  private Meme[] memes = new Meme[0];
  private Chord[] chords = new Chord[0];
  private Voice[] voices = new Voice[0];

  public PhaseImpl(
    String _name,
    int _offset,
    float _total,
    float _density,
    String _key,
    float _tempo
  ) {
    name = _name;
    offset = _offset;
    total = _total;
    density = _density;
    key = _key;
    tempo = _tempo;
  }

  public String Name() {
    return name;
  }

  public int Offset() {
    return offset;
  }

  public float Total() {
    return total;
  }

  public float Density() {
    return density;
  }

  public String Key() {
    return key;
  }

  public float Tempo() {
    return tempo;
  }

  @Override
  public Meme[] Memes() {
    return memes;
  }

  @Override
  public Chord[] Chords() {
    return chords;
  }

  @Override
  public Voice[] Voices() {
    return voices;
  }
}
