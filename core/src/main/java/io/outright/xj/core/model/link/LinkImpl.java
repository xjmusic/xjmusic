// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.meme.Meme;

public class LinkImpl implements Link {
  private int offset;
  private State state;
  private float start;
  private float finish;
  private float total;
  private float density;
  private String key;
  private float tempo;
  private Meme[] memes = new Meme[0];
  private Chord[] chords = new Chord[0];
  private Choice[] choices = new Choice[0];

  public LinkImpl(
    int _offset,
    State _state,
    float _start,
    float _finish,
    float _total,
    float _density,
    String _key,
    float _tempo
  ) {
    offset = _offset;
    state = _state;
    start = _start;
    finish = _finish;
    total = _total;
    density = _density;
    key = _key;
    tempo = _tempo;
  }

  @Override
  public int Offset() {
    return offset;
  }

  @Override
  public State State() {
    return state;
  }

  @Override
  public float Start() {
    return start;
  }

  @Override
  public float Finish() {
    return finish;
  }

  @Override
  public float Total() {
    return total;
  }

  @Override
  public float Density() {
    return density;
  }

  @Override
  public String Key() {
    return key;
  }

  @Override
  public float Tempo() {
    return tempo;
  }

  @Override
  public Meme[] Memes() {
    return memes;
  }

  @Override
  public Choice[] Choices() {
    return choices;
  }

  @Override
  public Chord[] Chords() {
    return chords;
  }

  // TODO LinkImplTest
}
