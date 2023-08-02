package io.xj.lib.analysis;

import io.xj.hub.TemplateConfig;
import io.xj.hub.ingest.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.lib.entity.Entities;
import io.xj.lib.meme.MemeConstellation;
import io.xj.lib.meme.MemeStack;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.lib.music.Chord;
import io.xj.lib.util.ValueException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Constellations report https://www.pivotaltracker.com/story/show/182861489
 */
public class ReportChordInstruments extends Report {
  final Histogram mainHistogram;
  final MemeTaxonomy taxonomy;
  final List<Instrument> chordInstruments;

  public ReportChordInstruments(HubContent content) throws ValueException {
    super(content);

    // Store the taxonomy
    taxonomy = new TemplateConfig(content.getTemplate()).getMemeTaxonomy();

    // 1. Compute Macro Constellations: for each macro program, for each macro sequence
    Histogram macroHistogram = computeMacroHistogram(taxonomy);

    // 2. Compute Main Constellations: for each macro constellation, for each possible main program, for each main sequence
    mainHistogram = computeMainHistogram(taxonomy, macroHistogram);

    // 3.
    chordInstruments = content.getInstruments().parallelStream().filter(instrument -> InstrumentMode.Chord.equals(instrument.getMode())).toList();
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public List<Section> computeSections() {
    return mainHistogram.histogram.keySet().parallelStream().sorted().map(this::sectionInstrumentCoverage).toList();
  }


  Section sectionInstrumentCoverage(String mainConstellation) {
    // to in each main constellation section,
    List<Chord> chords = mainHistogram.histogram.get(mainConstellation).ids.parallelStream()
      .flatMap(mpId -> content.getProgramSequenceChords(mpId).parallelStream())
      .map(ProgramSequenceChord::getName).map(Chord::of) // instantiate chord by name
      .collect(Collectors.toMap(Chord::getName, a -> a, (a, b) -> a)).values().parallelStream() // map unique by chord name, discard duplicates
      .sorted()// sorted by chord name
      .toList();

    var stack = MemeStack.from(taxonomy, MemeConstellation.toNames(mainConstellation));
    var instruments = chordInstruments.parallelStream()
      .filter(instrument -> stack.isAllowed(Entities.namesOf(content.getInstrumentMemes(instrument.getId()))))
      .sorted(Comparator.comparing(Instrument::getName)).toList();

    if (instruments.isEmpty()) {
      return new Section(String.format("main-constellation-%s", mainConstellation),
        mainConstellation,
        List.of(chords.parallelStream().map(c -> CellSpecialValue.RED.toString()).toList()),
        chords.parallelStream().map(Chord::getName).toList(),
        List.of("Missing"));
    }

    String[][] rowCellData = new String[instruments.size()][chords.size()];

    Collection<Chord> instrumentChords;
    for (int i = 0; i < instruments.size(); i++) {
      instrumentChords = content.getInstrumentAudios(instruments.get(i).getId()).parallelStream().map(InstrumentAudio::getTones).map(Chord::of).toList();
      for (int c = 0; c < chords.size(); c++) {
        var chord = chords.get(c);
        if (instrumentChords.stream().anyMatch(instrumentChord -> instrumentChord.isSame(chord)))
          rowCellData[i][c] = CellSpecialValue.GREEN.toString();
        else if (instrumentChords.stream().anyMatch(instrumentChord -> instrumentChord.isAcceptable(chord)))
          rowCellData[i][c] = CellSpecialValue.YELLOW.toString();
        else
          rowCellData[i][c] = CellSpecialValue.RED.toString();
      }
    }

    return new Section(String.format("main-constellation-%s", mainConstellation),
      mainConstellation,
      Arrays.stream(rowCellData).map(cells -> Arrays.stream(cells).toList()).toList(),
      chords.parallelStream().map(Chord::getName).toList(),
      instruments.parallelStream().map(Instrument::getName).toList());
  }

  @Override
  public Type getType() {
    return Type.ChordInstruments;
  }
}
