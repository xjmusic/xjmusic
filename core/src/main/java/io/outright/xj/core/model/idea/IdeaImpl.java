// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.phase.Phase;

public class IdeaImpl implements Idea {
  private String name;
  private Credit credit;
  private Type type;
  private float density;
  private String key;
  private float tempo;
  private Meme[] memes = new Meme[0];
  private Phase[] phases = new Phase[0];

  public IdeaImpl(
    String _name,
    Credit _credit,
    Type _type,
    float _density,
    String _key,
    float _tempo
  ) {
    name = _name;
    credit = _credit;
    type = _type;
    density = _density;
    key = _key;
    tempo = _tempo;
  }

  @Override
  public String Name() {
    return name;
  }

  @Override
  public Credit Credit() {
    return credit;
  }

  @Override
  public Type Type() {
    return type;
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
  public Phase[] Phases() {
    return phases;
  }
}
