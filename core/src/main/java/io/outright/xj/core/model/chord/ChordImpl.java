// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chord;

public class ChordImpl implements Chord {
  private String name = null;
  private float position = 0;

  public ChordImpl(String _name, float _position) {
    name = _name;
    position = _position;
  }

  @Override
  public String Name() {
    return name;
  }

  @Override
  public float Position() {
    return position;
  }
}
