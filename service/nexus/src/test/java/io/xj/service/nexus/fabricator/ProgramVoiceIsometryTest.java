// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramVoice;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProgramVoiceIsometryTest {
  private ProgramVoice programVoice1a;
  private ProgramVoice programVoice1b;

  @Before
  public void setUp() {
    Program program1 = Program.create();
    programVoice1a = ProgramVoice.create(program1, InstrumentType.Harmonic, "Super Cool");
    programVoice1b = ProgramVoice.create(program1, InstrumentType.Harmonic, "Very Interesting");
  }

  @Test
  public void of() {
    VoiceIsometry result = VoiceIsometry.ofVoices(ImmutableList.of(programVoice1a, programVoice1b));

    assertNotNull(result);
  }

  @Test
  public void find() {
    VoiceIsometry result = VoiceIsometry.ofVoices(ImmutableList.of(programVoice1a, programVoice1b));

    ProgramVoice find1 = result.find(new ProgramVoice().setName("Sooper Kewl"));
    assertNotNull(find1);
    assertEquals(programVoice1a.getId(), find1.getId());

    ProgramVoice find2 = result.find(new ProgramVoice().setName("Vury Anterestin"));
    assertNotNull(find2);
    assertEquals(programVoice1b.getId(), find2.getId());
  }


}
