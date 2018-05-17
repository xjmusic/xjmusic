// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class InstrumentTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Library ID is required");

    new Instrument()
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User ID is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutDescription() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Description is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("Percussive")
      .setDensity(0.8)
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'butt' is not a valid type");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("butt")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Density is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("Percussive")
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743L))
      .validate();
  }

}
