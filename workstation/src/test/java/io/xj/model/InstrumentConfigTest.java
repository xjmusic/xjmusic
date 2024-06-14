// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model;

import io.xj.model.pojos.Instrument;
import io.xj.model.util.ValueException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 Artist saves Instrument config, validate & combine with defaults. https://github.com/xjmusic/xjmusic/issues/206
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
  public void setFromInstrument_isAudioSelectionPersistent_false() throws ValueException {
    var instrument = new Instrument();
    instrument.setConfig("isAudioSelectionPersistent = false");

    var subject = new InstrumentConfig(instrument);

    assertFalse(subject.isAudioSelectionPersistent());
  }

  @Test
  public void setFromDefaults() throws ValueException {
    var subject = new InstrumentConfig(InstrumentConfig.DEFAULT);

    assertFalse(subject.isMultiphonic());
  }

  @Test
  public void defaultsToString() throws ValueException {
    var subject = new InstrumentConfig(InstrumentConfig.DEFAULT);

    assertArrayEquals(InstrumentConfig.DEFAULT.split("\n"), subject.toString().split(System.lineSeparator()));
  }

  /**
   Instruments have a config to play back as one-shots (ignore note-event lengths) https://github.com/xjmusic/xjmusic/issues/224
   */
  @Test
  public void isOneShot() throws ValueException {
    var subject = new InstrumentConfig("isOneShot=true");

    assertTrue(subject.isOneShot());
  }

  /**
   Instruments have a config to play back as one-shots (ignore note-event lengths) https://github.com/xjmusic/xjmusic/issues/224
   */
  @Test
  public void oneShotObserveLengthOfEvents() throws ValueException {
    var subject = new InstrumentConfig("oneShotObserveLengthOfEvents=[   bada ,     bIng, b    ooM ]");

    assertEquals(List.of("BADA", "BING", "BOOM"), subject.getOneShotObserveLengthOfEvents());
  }

  /**
   InstrumentConfig to disable note cutoffs for one-shot instruments https://github.com/xjmusic/xjmusic/issues/225
   */
  @Test
  public void isOneShotCutoffEnabled() throws ValueException {
    var subject = new InstrumentConfig("");

    assertTrue(subject.isOneShotCutoffEnabled());
  }

  /**
   One-shot fadeout mode https://github.com/xjmusic/xjmusic/issues/226
   */
  @Test
  public void releaseMillis() throws ValueException {
    var subject = new InstrumentConfig("");

    assertEquals((Integer) 5, subject.getReleaseMillis());
  }

}
