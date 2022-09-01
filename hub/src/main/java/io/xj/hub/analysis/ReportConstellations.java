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
import java.util.UUID;
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
    Collection<String> macroMemeNames;
    Collection<String> macroBindingMemeNames;
    Collection<String> memeNames;
    MemeStack stack;

    // Store the taxonomy
    taxonomy = new TemplateConfig(content.getTemplate()).getMemeTaxonomy();

    // 1. Compute Macro Constellations: for each macro program, for each macro sequence
    macroHistogram = new Histogram();
    for (var macroProgram : content.getPrograms(ProgramType.Macro)) {
      macroMemeNames = Entities.namesOf(content.getProgramMemes(macroProgram.getId()));
      for (var macroBinding : content.getSequenceBindingsForProgram(macroProgram.getId())) {
        macroBindingMemeNames = Entities.namesOf(content.getMemesForSequenceBinding(macroBinding.getId()));
        memeNames = Streams.concat(macroMemeNames.stream(), macroBindingMemeNames.stream()).collect(Collectors.toSet());
        macroHistogram.addId(MemeStack.from(taxonomy, memeNames).getConstellation(), macroProgram.getId());
      }
    }

    // 2. Compute Main Constellations: for each macro constellation, for each possible main program, for each main sequence
    mainHistogram = new Histogram();
    for (var macroConstellation : macroHistogram.histogram.keySet()) {
      stack = MemeStack.from(taxonomy, MemeConstellation.toNames(macroConstellation));
      for (var mainProgram : content.getPrograms(ProgramType.Main)) {
        var mainMemes = Entities.namesOf(content.getProgramMemes(mainProgram.getId()));
        if (stack.isAllowed(mainMemes))
          for (var mainBinding : content.getSequenceBindingsForProgram(mainProgram.getId())) {
            Collection<String> mainBindingMemeNames = Entities.namesOf(content.getMemesForSequenceBinding(mainBinding.getId()));
            memeNames = Streams.concat(mainMemes.stream(), mainBindingMemeNames.stream()).collect(Collectors.toSet());
            mainHistogram.addId(MemeStack.from(taxonomy, memeNames).getConstellation(), mainProgram.getId());
          }
      }
    }

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
        for (var instrument : content.getInstruments().stream().filter(instrument -> instrumentType.equals(instrument.getType())).collect(Collectors.toSet())) {
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
  public List<ReportSection> computeSections() {
    return Streams.concat(
      Stream.of(
        sectionTaxonomy(),
        sectionMacroSummary(),
        sectionMainSummary(),
        sectionBeatProgramCoverage()
      ),
      instrumentsByType.keySet().stream().map(this::sectionInstrumentCoverage),
      instrumentsByType.keySet().stream().map(this::sectionDetailProgramCoverage)
    ).toList();
  }

  private ReportSection sectionTaxonomy() {
    return new ReportSection("taxonomy", "Template Taxonomy",
      List.of("Category", "Values"),
      taxonomy.getCategories().stream()
        .sorted(Comparator.comparing(MemeTaxonomy.Category::getName))
        .map(c -> List.of(
          c.getName(),
          c.getMemes().stream()
            .map(Report::P)
            .collect(Collectors.joining("\n"))
        ))
        .toList());
  }

  private ReportSection sectionMacroSummary() {
    return new ReportSection("macro_meme_summary", "Macro Summary",
      List.of("Memes", "Macro-Programs"),
      macroHistogram.histogram.entrySet().stream()
        .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
        .map(e -> List.of(
          e.getKey(),
          e.getValue().ids.stream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n"))
        ))
        .toList());
  }

  private ReportSection sectionMainSummary() {
    return new ReportSection("main_meme_summary", "Main Summary",
      List.of("Memes", "Main-Programs", "# Beat-Programs", "# Detail-Programs"),
      mainHistogram.histogram.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> List.of(
          c.getKey(),
          c.getValue().ids.stream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n")),
          Values.emptyZero(beatProgramHistogram.getIds(c.getKey()).size()),
          Values.emptyZero(detailProgramHistogram.getIds(c.getKey()).size())
        ))
        .toList());
  }

  private ReportSection sectionBeatProgramCoverage() {
    return new ReportSection("beat_programs", "Beat-program Coverage",
      Streams.concat(Stream.of("Memes"),
        programsByType.get(ProgramType.Beat).stream().map(this::programRef)).toList(),
      mainHistogram.histogram.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          programsByType.get(ProgramType.Beat).stream().map(program ->
            ReportSection.checkboxValue(beatProgramHistogram.getIds(c.getKey()).contains(program.getId()))
          )).toList())
        .toList());
  }

  private ReportSection sectionInstrumentCoverage(InstrumentType instrumentType) {
    var instruments = instrumentsByType.get(instrumentType);
    if (instruments.isEmpty()) return ReportSection.empty();
    return new ReportSection(String.format("%s_detail_instruments", instrumentType.toString().toLowerCase(Locale.ROOT)),
      String.format("%s Instrument coverage", instrumentType),
      Streams.concat(Stream.of("Memes"),
        instruments.stream().map(this::instrumentRef)).toList(),
      mainHistogram.histogram.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          instruments.stream().map(instrument ->
            ReportSection.checkboxValue(instrumentHistogram.get(instrumentType).getIds(c.getKey()).contains(instrument.getId()))
          )).toList())
        .toList());
  }

  private ReportSection sectionDetailProgramCoverage(InstrumentType instrumentType) {
    var programs = programsByType.get(ProgramType.Detail).stream()
      .filter(p -> content.getVoices(p).stream().anyMatch(v -> Objects.equals(instrumentType, v.getType())))
      .toList();
    if (programs.isEmpty()) return ReportSection.empty();
    return new ReportSection(String.format("%s_detail_programs", instrumentType.toString().toLowerCase(Locale.ROOT)),
      String.format("%s DP coverage", instrumentType),
      Streams.concat(Stream.of("Memes"),
        programs.stream().map(this::programRef)).toList(),
      mainHistogram.histogram.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(c -> Streams.concat(Stream.of(c.getKey()),
          programs.stream().map(program ->
            ReportSection.checkboxValue(detailProgramHistogram.getIds(c.getKey()).contains(program.getId()))
          )).toList())
        .toList());
  }

  @Override
  public Type getType() {
    return Type.Constellations;
  }

  /**
   Representation of the construction of a histogram of usage of all constellations
   */
  private static class Histogram {
    Map<String, Count> histogram;

    public Histogram() {
      histogram = Maps.newHashMap();
    }

    public void addId(String key, UUID id) {
      if (!histogram.containsKey(key)) histogram.put(key, new Count());
      histogram.get(key).addId(id);
    }

    public Collection<UUID> getIds(String key) {
      if (histogram.containsKey(key)) return histogram.get(key).ids;
      return List.of();
    }

    private static class Count {
      Set<UUID> ids;
      Integer total;

      public Count() {
        total = 0;
        ids = Sets.newHashSet();
      }

      public void addId(UUID programId) {
        ids.add(programId);
        total++;
      }
    }
  }
}
