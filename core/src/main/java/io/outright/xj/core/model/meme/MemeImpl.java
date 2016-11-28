// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.meme;

public class MemeImpl implements Meme {
  private String name;
  private int order;

  public MemeImpl(
    String _name,
    int _order
  ) {
    name = _name;
    order = _order;
  }

  @Override
  public String Name() {
    return name;
  }

  @Override
  public int Order() {
    return order;
  }
}
