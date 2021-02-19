// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramVoice;
import org.junit.Before;
import org.junit.Test;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramVoice;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProgramVoiceIsometryTest {
  private ProgramVoice programVoice1a;
  private ProgramVoice programVoice1b;

  @Before
  public void setUp() {
    Program program1 = Program.newBuilder().build();
    programVoice1a = buildProgramVoice(program1, Instrument.Type.Pad, "Super Cool");
    programVoice1b = buildProgramVoice(program1, Instrument.Type.Pad, "Very Interesting");
  }

  @Test
  public void of() {
    VoiceIsometry result = VoiceIsometry.ofVoices(ImmutableList.of(programVoice1a, programVoice1b));

    assertNotNull(result);
  }

  @Test
  public void find() {
    VoiceIsometry result = VoiceIsometry.ofVoices(ImmutableList.of(programVoice1a, programVoice1b));

    var find1 = result.find(ProgramVoice.newBuilder().setName("Sooper Kewl").build());
    assertNotNull(find1);
    assertEquals(programVoice1a.getId(), find1.getId());

    var find2 = result.find(ProgramVoice.newBuilder().setName("Vury Anterestin").build());
    assertNotNull(find2);
    assertEquals(programVoice1b.getId(), find2.getId());
  }


}
