// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.primitive.density.Density;
import io.outright.xj.core.primitive.key.Key;
import io.outright.xj.core.primitive.name.Name;
import io.outright.xj.core.primitive.tempo.Tempo;

import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.phase.Phase;

public class IdeaImpl implements Idea {
  private Name name;
  private Credit credit;
  private Type type;
  private Density density;
  private Key key;
  private Tempo tempo;
  private Meme[] memes = new Meme[0];
  private Phase[] phases = new Phase[0];

  public IdeaImpl(
    Name name,
    Credit credit,
    Type type,
    Density density,
    Key key,
    Tempo tempo
  ) {
    this.name = name;
    this.credit = credit;
    this.type = type;
    this.density = density;
    this.key = key;
    this.tempo = tempo;
  }

  @Override
  public Name Name() {
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
  public Density Density() {
    return density;
  }

  @Override
  public Key Key() {
    return key;
  }

  @Override
  public Tempo Tempo() {
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
