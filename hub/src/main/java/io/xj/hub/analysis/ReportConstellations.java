package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import com.google.common.collect.Streams;
import io.xj.hub.TemplateConfig;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.meme.MemeConstellation;
import io.xj.lib.meme.MemeStack;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 Constellations report https://www.pivotaltracker.com/story/show/182861489
 */
public class ReportConstellations extends Report {
  private final Histogram macroHistogram;
  private final Histogram mainHistogram;
  private final Histogram beatProgramHistogram;
  private final Histogram detailProgramHistogram;
  private final Map<InstrumentType, Histogram> instrumentHistogram;
  private final Map<ProgramType, Set<Program>> programsByType;
  private final Map<InstrumentType, Set<Instrument>> instrumentsByType;
  private final MemeTaxonomy taxonomy;

  public ReportConstellations(HubContent content, Environment env) throws HubClientException, ValueException {
    super(content, env);
    Collection<String> memeNames;
    MemeStack stack;

    // Store the taxonomy
    taxonomy = new TemplateConfig(content.getTemplate()).getMemeTaxonomy();

    // 1. Compute Macro Constellations: for each macro program, for each macro sequence
    macroHistogram = computeMacroHistogram(taxonomy);

    // 2. Compute Main Constellations: for each macro constellation, for each possible main program, for each main sequence
    mainHistogram = computeMainHistogram(taxonomy, macroHistogram);

    // 3. For each main constellation, compute all possible programs and instruments that might be chosen to fulfill the meme stack and display them all in a table
    beatProgramHistogram = new Histogram();
    detailProgramHistogram = new Histogram();
    instrumentHistogram = Maps.newHashMap();
    for (var mainConstellation : mainHistogram.histogram.keySet()) {
      stack = MemeStack.from(taxonomy, MemeConstellation.toNames(mainConstellation));
      //
      for (var beatProgram : content.getPrograms(ProgramType.Beat)) {
        memeNames = Entities.namesOf(content.getProgramMemes(beatProgram.getId()));
        if (stack.isAllowed(memeNames))
          beatProgramHistogram.addId(stack.getConstellation(), beatProgram.getId());
      }
      //
      for (var detailProgram : content.getPrograms(ProgramType.Detail)) {
        memeNames = Entities.namesOf(content.getProgramMemes(detailProgram.getId()));
        if (stack.isAllowed(memeNames))
          detailProgramHistogram.addId(stack.getConstellation(), detailProgram.getId());
      }
      //
      for (var instrumentType : InstrumentType.values()) {
        if (!instrumentHistogram.containsKey(instrumentType))
          instrumentHistogram.put(instrumentType, new Histogram());
        for (var instrument : content.getInstruments().parallelStream().filter(instrument -> instrumentType.equals(instrument.getType())).collect(Collectors.toSet())) {
          memeNames = Entities.namesOf(content.getInstrumentMemes(instrument.getId()));
          if (stack.isAllowed(memeNames))
            instrumentHistogram.get(instrumentType).addId(stack.getConstellation(), instrument.getId());
        }
      }
    }

    // 4a. Stash all program ids by type
    programsByType = Maps.newHashMap();
    for (var program : content.getPrograms()) {
      if (!programsByType.containsKey(program.getType()))
        programsByType.put(program.getType(), Sets.newHashSet());
      programsByType.get(program.getType()).add(program);
    }

    // 4b. Stash all instrument ids by type
    instrumentsByType = Maps.newHashMap();
    for (var instrument : content.getInstruments()) {
      if (!instrumentsByType.containsKey(instrument.getType()))
        instrumentsByType.put(instrument.getType(), Sets.newHashSet());
      instrumentsByType.get(instrument.getType()).add(instrument);
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public List<Section> computeSections() {
    return Streams.concat(
      Stream.of(
        sectionTaxonomy(),
        sectionMacroSummary(),
        sectionMainSummary(),
        sectionBeatProgramCoverage()
      ),
      instrumentsByType.keySet().parallelStream().map(this::sectionInstrumentCoverage),
      instrumentsByType.keySet().parallelStream().map(this::sectionDetailProgramCoverage)
    ).toList();
  }

  private Section sectionTaxonomy() {
    return new Section("taxonomy", "Template Taxonomy",
      taxonomy.getCategories().parallelStream()
        .sorted(Comparator.comparing(MemeTaxonomy.Category::getName))
        .map(c -> List.of(
          c.getName(),
          c.getMemes().parallelStream()
            .map(Report::P)
            .collect(Collectors.joining("\n"))
        ))
        .toList(), List.of("Category", "Values"),
      List.of());
  }

  private Section sectionMacroSummary() {
    return new Section("macro_meme_summary", "Macro Summary",
      macroHistogram.histogram.entrySet().parallelStream()
        .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
        .map(e -> List.of(
          e.getKey(),
          e.getValue().ids.parallelStream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n"))
        ))
        .toList(), List.of("Memes", "Macro-Programs"),
      List.of());
  }

  private Section sectionMainSummary() {
    return new Section("main_meme_summary", "Main Summary",
      mainHistogram.histogram.entrySet().parallelStream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> List.of(
          c.getKey(),
          c.getValue().ids.parallelStream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n")),
          Values.emptyZero(beatProgramHistogram.getIds(c.getKey()).size()),
          Values.emptyZero(detailProgramHistogram.getIds(c.getKey()).size())
        ))
        .toList(), List.of("Memes", "Main-Programs", "# Beat-Programs", "# Detail-Programs"),
      List.of());
  }

  private Section sectionBeatProgramCoverage() {
    if (!programsByType.containsKey(ProgramType.Beat)) return Section.empty();
    var programs = programsByType.get(ProgramType.Beat).parallelStream()
      .sorted(Comparator.comparing(Program::getName))
      .toList();
    return new Section("beat_programs", "Beat-program Coverage",
      mainHistogram.histogram.entrySet().parallelStream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          programs.parallelStream().map(program ->
            Section.checkboxValue(beatProgramHistogram.getIds(c.getKey()).contains(program.getId()))
          )).toList())
        .toList(), Streams.concat(Stream.of("Memes"),
        programs.parallelStream().map(this::programRef)).toList(),
      List.of());
  }

  private Section sectionInstrumentCoverage(InstrumentType instrumentType) {
    if (!instrumentsByType.containsKey(instrumentType)) return Section.empty();
    var instruments = instrumentsByType.get(instrumentType).parallelStream()
      .sorted(Comparator.comparing(Instrument::getName))
      .toList();
    if (instruments.isEmpty()) return Section.empty();
    return new Section(String.format("%s_detail_instruments", instrumentType.toString().toLowerCase(Locale.ROOT)),
      String.format("%s Instrument coverage", instrumentType),
      mainHistogram.histogram.entrySet().parallelStream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          instruments.parallelStream().map(instrument ->
            Section.checkboxValue(instrumentHistogram.get(instrumentType).getIds(c.getKey()).contains(instrument.getId()))
          )).toList())
        .toList(), Streams.concat(Stream.of("Memes"),
        instruments.parallelStream().map(this::instrumentRef)).toList(),
      List.of());
  }

  private Section sectionDetailProgramCoverage(InstrumentType instrumentType) {
    if (!programsByType.containsKey(ProgramType.Detail)) return Section.empty();
    var programs = programsByType.get(ProgramType.Detail).parallelStream()
      .filter(p -> content.getVoices(p).parallelStream().anyMatch(v -> Objects.equals(instrumentType, v.getType())))
      .sorted(Comparator.comparing(Program::getName))
      .toList();
    if (programs.isEmpty()) return Section.empty();
    return new Section(String.format("%s_detail_programs", instrumentType.toString().toLowerCase(Locale.ROOT)),
      String.format("%s DP coverage", instrumentType),
      mainHistogram.histogram.entrySet().parallelStream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          programs.parallelStream().map(program ->
            Section.checkboxValue(detailProgramHistogram.getIds(c.getKey()).contains(program.getId()))
          )).toList())
        .toList(), Streams.concat(Stream.of("Memes"),
        programs.parallelStream().map(this::programRef)).toList(),
      List.of());
  }

  @Override
  public Type getType() {
    return Type.Constellations;
  }
}
