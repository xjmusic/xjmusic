package io.xj.hub.analysis.util;

import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramVoice;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChord;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChordVoicing;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static org.junit.Assert.assertEquals;

/**
 Chord Search while composing a main program
 https://www.pivotaltracker.com/story/show/178921705
 <p>
 Chord search results (backend) must include all unique combinations of chord & voicing
 */
@SuppressWarnings("FieldCanBeLocal")
public class ChordVoicingDeduperTest {
  private Account account1;
  private Library library1;
  private Program program1;
  private ProgramVoice voiceBass;
  private ProgramSequence program1_sequence1;
  private ProgramSequenceChord chord1;
  private ProgramSequenceChord chord2;
  private ProgramSequenceChord chord3;
  private ProgramSequenceChord chord4;
  private ProgramSequenceChord chord5;
  private ProgramSequenceChord chord6;
  private ProgramSequenceChordVoicing chord1_voicing1;
  private ProgramSequenceChordVoicing chord2_voicing1;
  private ProgramSequenceChordVoicing chord3_voicing1;
  private ProgramSequenceChordVoicing chord4_voicing1;
  private ProgramSequenceChordVoicing chord5_voicing1;
  private ProgramSequenceChordVoicing chord6_voicing1;

  private ChordVoicingDeduper subject;

  @Before
  public void setUp() throws Exception {
    account1 = buildAccount("bananas");
    library1 = buildLibrary(account1, "palm tree");
    program1 = buildProgram(library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f);
    voiceBass = buildProgramVoice(program1, InstrumentType.Bass,"Bass");
    program1_sequence1 = buildProgramSequence(program1, 4, "Ants", 0.583f, "D minor");

    chord1 = buildProgramSequenceChord(program1_sequence1, 0.0f, "C major");
    chord1_voicing1 = buildProgramSequenceChordVoicing(chord1, voiceBass, "C3, E3, G3");

    chord2 = buildProgramSequenceChord(program1_sequence1, 0.0f, "C major");
    chord2_voicing1 = buildProgramSequenceChordVoicing(chord2, voiceBass, "C3, E3, G3");

    chord3 = buildProgramSequenceChord(program1_sequence1, 0.0f, "C major");
    chord3_voicing1 = buildProgramSequenceChordVoicing(chord3, voiceBass, "C3, E3, G3, C4, E4, G4");

    chord4 = buildProgramSequenceChord(program1_sequence1, 0.0f, "C minor");
    chord4_voicing1 = buildProgramSequenceChordVoicing(chord4, voiceBass, "C3, Eb3, G3, C4, Eb4, G4");

    chord5 = buildProgramSequenceChord(program1_sequence1, 0.0f, "C minor");
    chord5_voicing1 = buildProgramSequenceChordVoicing(chord5, voiceBass, "C3, Eb3, G3");

    chord6 = buildProgramSequenceChord(program1_sequence1, 0.0f, "D sus");
    chord6_voicing1 = buildProgramSequenceChordVoicing(chord6, voiceBass, "D3, G3");

    subject = new ChordVoicingDeduper(
      List.of(
        voiceBass
      ),
      List.of(
        chord1,
        chord2,
        chord3,
        chord4,
        chord5,
        chord6
      ),
      List.of(
        chord1_voicing1,
        chord2_voicing1,
        chord3_voicing1,
        chord4_voicing1,
        chord5_voicing1,
        chord6_voicing1
      )
    );
  }

  @Test
  public void dedupe_uniqueChordAndVoicings() {
    assertEquals(5, subject.getChords().size());
    assertEquals(5, subject.getVoicings().size());
  }
}
