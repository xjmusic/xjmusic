// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkChord extends Chord {
  public static final String KEY_ONE = "linkChord";
  public static final String KEY_MANY = "linkChords";

  private BigInteger linkId;

  public LinkChord setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getLinkId() {
    return linkId;
  }

  public LinkChord setLinkId(BigInteger linkId) {
    this.linkId = linkId;
    return this;
  }

  public LinkChord setPosition(Double position) {
    this.position = roundPosition(position);
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return linkId;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(linkId)) {
      throw new BusinessException("Link ID is required.");
    }
    super.validate();
  }

  @Override
  public LinkChord of(String name) {
    return new LinkChord().setName(name);
  }


}
