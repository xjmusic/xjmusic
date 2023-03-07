// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import io.xj.hub.InstrumentConfig;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.util.ValueException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 Artist saves Instrument config, validate & combine with defaults. https://www.pivotaltracker.com/story/show/177355683
 */
public class InstrumentConfigTest {

  @Test
  public void setFromInstrument() throws ValueException {
    var instrument = new Instrument();
    instrument.setConfig("isMultiphonic = true");

    var subject = new InstrumentConfig(instrument);

    assertTrue(subject.isMultiphonic());
  }

  @Test
  public void setFromDefaults() throws ValueException {
    var subject = new InstrumentConfig(InstrumentConfig.DEFAULT);

    assertFalse(subject.isMultiphonic());
  }

  @Test
  public void defaultsToString() throws ValueException {
    var subject = new InstrumentConfig(InstrumentConfig.DEFAULT);

    assertEquals(InstrumentConfig.DEFAULT, subject.toString());
  }

  /**
   Instruments have a config to play back as one-shots (ignore note-event lengths) https://www.pivotaltracker.com/story/show/178307432
   */
  @Test
  public void isOneShot() throws ValueException {
    var subject = new InstrumentConfig("isOneShot=true");

    assertTrue(subject.isOneShot());
  }

  /**
   Instruments have a config to play back as one-shots (ignore note-event lengths) https://www.pivotaltracker.com/story/show/178307432
   */
  @Test
  public void oneShotObserveLengthOfEvents() throws ValueException {
    var subject = new InstrumentConfig("oneShotObserveLengthOfEvents=[   bada ,     bIng, b    ooM ]");

    assertEquals(List.of("BADA", "BING", "BOOM"), subject.getOneShotObserveLengthOfEvents());
  }

  /**
   InstrumentConfig to disable note cutoffs for one-shot instruments https://www.pivotaltracker.com/story/show/181211927
   */
  @Test
  public void isOneShotCutoffEnabled() throws ValueException {
    var subject = new InstrumentConfig("");

    assertTrue(subject.isOneShotCutoffEnabled());
  }

  /**
   One-shot fadeout mode https://www.pivotaltracker.com/story/show/183385397
   */
  @Test
  public void releaseMillis() throws ValueException {
    var subject = new InstrumentConfig("");

    assertEquals((Integer) 5, subject.getReleaseMillis());
  }

}
