package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import com.google.common.collect.Streams;
import io.xj.hub.TemplateConfig;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.meme.MemeConstellation;
import io.xj.lib.meme.MemeStack;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Constellations report https://www.pivotaltracker.com/story/show/182861489
 */
public class ReportConstellations extends Report {
  private final Histogram macroHistogram;
  private final Histogram mainHistogram;
  private final Histogram beatProgramHistogram;
  private final Histogram detailProgramHistogram;
  private final Histogram instrumentHistogram;
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
    instrumentHistogram = new Histogram();
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
      for (var instrument : content.getInstruments()) {
        memeNames = Entities.namesOf(content.getInstrumentMemes(instrument.getId()));
        if (stack.isAllowed(memeNames))
          instrumentHistogram.addId(stack.getConstellation(), instrument.getId());
      }
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String renderContentHTML() {
    return
      renderTaxonomyHTML() +
        renderMacroContentHTML() +
        renderMainContentHTML();
  }

  private String renderTaxonomyHTML() {
    return H1("Taxonomy", "taxonomy") +
      TABLE(TR(true, TD("Category"), TD("Values")),
        taxonomy.getCategories().stream()
          .sorted(Comparator.comparing(MemeTaxonomy.Category::getName))
          .map(c -> TR(
            false, TD(c.getName()),
            TD(c.getMemes().stream()
              .map(Report::P)
              .collect(Collectors.joining("\n")))
          ))
          .collect(Collectors.joining()));
  }


  private String renderMacroContentHTML() {
    return H1("Macro", "macro_programs") +
      TABLE(TR(true, TD("Memes"), TD("Macro-Programs")),
        macroHistogram.histogram.entrySet().stream()
          .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
          .map(e -> TR(
            false, TD(e.getKey()),
            TD(e.getValue().ids.stream()
              .map(content::getProgram)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Program::getName))
              .map(this::programRef)
              .collect(Collectors.joining("\n")))
          ))
          .collect(Collectors.joining()));
  }

  private String renderMainContentHTML() {
    return H1("Main", "main_programs") +
      TABLE(TR(true, TD("Memes"), TD("Main-Programs"), TD("Beat-Programs"), TD("Detail-Programs"), TD("Instruments")),
        mainHistogram.histogram.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .map(c -> TR(
            false, TD(c.getKey()),
            TD(c.getValue().ids.stream()
              .map(content::getProgram)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Program::getName))
              .map(this::programRef)
              .collect(Collectors.joining("\n"))),
            TD(beatProgramHistogram.getIds(c.getKey()).stream()
              .map(content::getProgram)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Program::getName))
              .map(this::programRef)
              .collect(Collectors.joining("\n"))),
            TD(detailProgramHistogram.getIds(c.getKey()).stream()
              .map(content::getProgram)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Program::getName))
              .map(this::programRef)
              .collect(Collectors.joining("\n"))),
            TD(instrumentHistogram.getIds(c.getKey()).stream()
              .map(content::getInstrument)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Instrument::getName))
              .map(this::instrumentRef)
              .collect(Collectors.joining("\n")))
          ))
          .collect(Collectors.joining()));
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
