// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.instrument;

import io.outright.xj.core.model.Audio;
import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;

public class InstrumentImpl implements Instrument {
  private Type type;
  private String description;
  private Credit credit;
  private float density;
  private Meme[] memes = new Meme[0];
  private Audio[] audios = new Audio[0];

  public InstrumentImpl(
    Type _type,
    String _description,
    Credit _credit,
    float _density
  ) {
    type = _type;
    description = _description;
    credit = _credit;
    density = _density;
  }

  @Override
  public Type Type() {
    return type;
  }

  @Override
  public String Description() {
    return description;
  }

  @Override
  public Credit Credit() {
    return credit;
  }

  @Override
  public float Density() {
    return density;
  }

  @Override
  public Meme[] Memes() {
    return memes;
  }

  @Override
  public Audio[] Audios() {
    return audios;
  }
}
