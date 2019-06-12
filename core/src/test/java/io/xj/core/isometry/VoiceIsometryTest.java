// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.sub.Voice;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VoiceIsometryTest extends CoreTest {
  private final UUID id1 = UUID.randomUUID();
  private final UUID id2 = UUID.randomUUID();
  private final Collection<Voice> testVoicesA = ImmutableList.of(
    newVoice(id1, InstrumentType.Harmonic, "Super Cool"),
    newVoice(id2, InstrumentType.Harmonic, "Very Interesting")
  );

  @Test
  public void of() {
    VoiceIsometry result = VoiceIsometry.ofVoices(testVoicesA);

    assertNotNull(result);
  }

  @Test
  public void find() {
    VoiceIsometry result = VoiceIsometry.ofVoices(testVoicesA);

    Voice find1 = result.find(new Voice().setDescription("Sooper Kewl"));
    assertNotNull(find1);
    assertEquals(id1, find1.getId());

    Voice find2 = result.find(new Voice().setDescription("Vury Anterestin"));
    assertNotNull(find2);
    assertEquals(id2, find2.getId());
  }


}
