// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class SequenceTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Library ID is required");

    new Sequence()
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User ID is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'funk' is not a valid type");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("funk")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutKey() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Key is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setDensity(0.75)
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Density is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Tempo is required");

    new Sequence()
      .setLibraryId(BigInteger.valueOf(23L))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setUserId(BigInteger.valueOf(987L))
      .validate();
  }

}
