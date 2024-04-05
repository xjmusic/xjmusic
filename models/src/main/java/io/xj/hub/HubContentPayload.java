// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class HubContentPayload {
  Boolean demo;
  Collection<Template> templates;
  Collection<TemplateBinding> templateBindings;
  Collection<Instrument> instruments;
  Collection<Library> libraries;
  Collection<InstrumentAudio> instrumentAudios;
  Collection<InstrumentMeme> instrumentMemes;
  Collection<Program> programs;
  Collection<ProgramMeme> programMemes;
  Collection<ProgramSequence> programSequences;
  Collection<ProgramSequenceBinding> programSequenceBindings;
  Collection<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  Collection<ProgramSequenceChord> programSequenceChords;
  Collection<ProgramSequenceChordVoicing> programSequenceChordVoicings;
  Collection<ProgramSequencePattern> programSequencePatterns;
  Collection<ProgramSequencePatternEvent> programSequencePatternEvents;
  Collection<ProgramVoice> programVoices;
  Collection<ProgramVoiceTrack> programVoiceTracks;
  Collection<Project> projects;

  /**
   Create a HubContentPayload from a collection of entities

   @param entities from which to create the payload
   @return the payload
   */
  public static HubContentPayload from(Collection<Object> entities) {
    return new HubContentPayload()
      .setTemplates(entities.stream()
        .filter(ent -> Template.class.equals(ent.getClass()))
        .map(ent -> (Template) ent)
        .collect(Collectors.toList()))
      .setTemplateBindings(entities.stream()
        .filter(ent -> TemplateBinding.class.equals(ent.getClass()))
        .map(ent -> (TemplateBinding) ent)
        .collect(Collectors.toList()))
      .setInstruments(entities.stream()
        .filter(ent -> Instrument.class.equals(ent.getClass()))
        .map(ent -> (Instrument) ent)
        .collect(Collectors.toList()))
      .setInstrumentAudios(entities.stream()
        .filter(ent -> InstrumentAudio.class.equals(ent.getClass()))
        .map(ent -> (InstrumentAudio) ent)
        .collect(Collectors.toList()))
      .setInstrumentMemes(entities.stream()
        .filter(ent -> InstrumentMeme.class.equals(ent.getClass()))
        .map(ent -> (InstrumentMeme) ent)
        .collect(Collectors.toList()))
      .setPrograms(entities.stream()
        .filter(ent -> Program.class.equals(ent.getClass()))
        .map(ent -> (Program) ent)
        .collect(Collectors.toList()))
      .setProgramMemes(entities.stream()
        .filter(ent -> ProgramMeme.class.equals(ent.getClass()))
        .map(ent -> (ProgramMeme) ent)
        .collect(Collectors.toList()))
      .setProgramSequences(entities.stream()
        .filter(ent -> ProgramSequence.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequence) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceBindings(entities.stream()
        .filter(ent -> ProgramSequenceBinding.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceBinding) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceBindingMemes(entities.stream()
        .filter(ent -> ProgramSequenceBindingMeme.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceBindingMeme) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceChords(entities.stream()
        .filter(ent -> ProgramSequenceChord.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceChord) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceChordVoicings(entities.stream()
        .filter(ent -> ProgramSequenceChordVoicing.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceChordVoicing) ent)
        .collect(Collectors.toList()))
      .setProgramSequencePatterns(entities.stream()
        .filter(ent -> ProgramSequencePattern.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequencePattern) ent)
        .collect(Collectors.toList()))
      .setProgramSequencePatternEvents(entities.stream()
        .filter(ent -> ProgramSequencePatternEvent.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequencePatternEvent) ent)
        .collect(Collectors.toList()))
      .setProgramVoices(entities.stream()
        .filter(ent -> ProgramVoice.class.equals(ent.getClass()))
        .map(ent -> (ProgramVoice) ent)
        .collect(Collectors.toList()))
      .setProgramVoiceTracks(entities.stream()
        .filter(ent -> ProgramVoiceTrack.class.equals(ent.getClass()))
        .map(ent -> (ProgramVoiceTrack) ent)
        .collect(Collectors.toList()))
      .setLibraries(entities.stream()
        .filter(ent -> io.xj.hub.tables.pojos.Library.class.equals(ent.getClass()))
        .map(ent -> (io.xj.hub.tables.pojos.Library) ent)
        .collect(Collectors.toList()))
      .setProjects(entities.stream()
        .filter(ent -> io.xj.hub.tables.pojos.Project.class.equals(ent.getClass()))
        .map(ent -> (io.xj.hub.tables.pojos.Project) ent)
        .collect(Collectors.toList()));
  }

  /**
   Create an empty HubContentPayload
   */
  public HubContentPayload() {
    demo = false;
    templates = new ArrayList<>();
    templateBindings = new ArrayList<>();
    instruments = new ArrayList<>();
    libraries = new ArrayList<>();
    instrumentAudios = new ArrayList<>();
    instrumentMemes = new ArrayList<>();
    programs = new ArrayList<>();
    programMemes = new ArrayList<>();
    programSequences = new ArrayList<>();
    programSequenceBindings = new ArrayList<>();
    programSequenceBindingMemes = new ArrayList<>();
    programSequenceChords = new ArrayList<>();
    programSequenceChordVoicings = new ArrayList<>();
    programSequencePatterns = new ArrayList<>();
    programSequencePatternEvents = new ArrayList<>();
    programVoices = new ArrayList<>();
    programVoiceTracks = new ArrayList<>();
    projects = new ArrayList<>();
  }

  public Collection<Instrument> getInstruments() {
    return instruments;
  }

  public HubContentPayload setInstruments(Collection<Instrument> instruments) {
    this.instruments = instruments;
    return this;
  }

  public Collection<InstrumentAudio> getInstrumentAudios() {
    return instrumentAudios;
  }

  public HubContentPayload setInstrumentAudios(Collection<InstrumentAudio> instrumentAudios) {
    this.instrumentAudios = instrumentAudios;
    return this;
  }

  public Collection<InstrumentMeme> getInstrumentMemes() {
    return instrumentMemes;
  }

  public HubContentPayload setInstrumentMemes(Collection<InstrumentMeme> instrumentMemes) {
    this.instrumentMemes = instrumentMemes;
    return this;
  }

  public Collection<Program> getPrograms() {
    return programs;
  }

  public HubContentPayload setPrograms(Collection<Program> programs) {
    this.programs = programs;
    return this;
  }

  public Collection<ProgramMeme> getProgramMemes() {
    return programMemes;
  }

  public HubContentPayload setProgramMemes(Collection<ProgramMeme> programMemes) {
    this.programMemes = programMemes;
    return this;
  }

  public Collection<ProgramSequence> getProgramSequences() {
    return programSequences;
  }

  public HubContentPayload setProgramSequences(Collection<ProgramSequence> programSequences) {
    this.programSequences = programSequences;
    return this;
  }

  public Collection<ProgramSequenceBinding> getProgramSequenceBindings() {
    return programSequenceBindings;
  }

  public HubContentPayload setProgramSequenceBindings(Collection<ProgramSequenceBinding> programSequenceBindings) {
    this.programSequenceBindings = programSequenceBindings;
    return this;
  }

  public Collection<ProgramSequenceBindingMeme> getProgramSequenceBindingMemes() {
    return programSequenceBindingMemes;
  }

  public HubContentPayload setProgramSequenceBindingMemes(Collection<ProgramSequenceBindingMeme> programSequenceBindingMemes) {
    this.programSequenceBindingMemes = programSequenceBindingMemes;
    return this;
  }

  public Collection<ProgramSequenceChord> getProgramSequenceChords() {
    return programSequenceChords;
  }

  public HubContentPayload setProgramSequenceChords(Collection<ProgramSequenceChord> programSequenceChords) {
    this.programSequenceChords = programSequenceChords;
    return this;
  }

  public Collection<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings() {
    return programSequenceChordVoicings;
  }

  public HubContentPayload setProgramSequenceChordVoicings(Collection<ProgramSequenceChordVoicing> programSequenceChordVoicings) {
    this.programSequenceChordVoicings = programSequenceChordVoicings;
    return this;
  }

  public Collection<ProgramSequencePattern> getProgramSequencePatterns() {
    return programSequencePatterns;
  }

  public HubContentPayload setProgramSequencePatterns(Collection<ProgramSequencePattern> programSequencePatterns) {
    this.programSequencePatterns = programSequencePatterns;
    return this;
  }

  public Collection<ProgramSequencePatternEvent> getProgramSequencePatternEvents() {
    return programSequencePatternEvents;
  }

  public HubContentPayload setProgramSequencePatternEvents(Collection<ProgramSequencePatternEvent> programSequencePatternEvents) {
    this.programSequencePatternEvents = programSequencePatternEvents;
    return this;
  }

  public Collection<ProgramVoice> getProgramVoices() {
    return programVoices;
  }

  public HubContentPayload setProgramVoices(Collection<ProgramVoice> programVoices) {
    this.programVoices = programVoices;
    return this;
  }

  public Collection<ProgramVoiceTrack> getProgramVoiceTracks() {
    return programVoiceTracks;
  }

  public HubContentPayload setProgramVoiceTracks(Collection<ProgramVoiceTrack> programVoiceTracks) {
    this.programVoiceTracks = programVoiceTracks;
    return this;
  }

  public Collection<Template> getTemplates() {
    return templates;
  }

  public HubContentPayload setTemplates(Collection<Template> templates) {
    this.templates = templates;
    return this;
  }

  public Collection<TemplateBinding> getTemplateBindings() {
    return templateBindings;
  }

  public HubContentPayload setTemplateBindings(Collection<TemplateBinding> templateBindings) {
    this.templateBindings = templateBindings;
    return this;
  }

  public Collection<Library> getLibraries() {
    return libraries;
  }

  public HubContentPayload setLibraries(Collection<Library> libraries) {
    this.libraries = libraries;
    return this;
  }

  public Collection<Project> getProjects() {
    return projects;
  }

  public HubContentPayload setProjects(Collection<Project> projects) {
    this.projects = projects;
    return this;
  }

  public Boolean getDemo() {
    return demo;
  }

  public HubContentPayload setDemo(Boolean demo) {
    this.demo = demo;
    return this;
  }

  @JsonIgnore
  public Collection<Object> getAllEntities() {
    Collection<Object> entities = new ArrayList<>();
    entities.addAll(instrumentAudios);
    entities.addAll(instrumentMemes);
    entities.addAll(instruments);
    entities.addAll(libraries);
    entities.addAll(programMemes);
    entities.addAll(programSequenceBindingMemes);
    entities.addAll(programSequenceBindings);
    entities.addAll(programSequenceChordVoicings);
    entities.addAll(programSequenceChords);
    entities.addAll(programSequencePatternEvents);
    entities.addAll(programSequencePatterns);
    entities.addAll(programSequences);
    entities.addAll(programVoiceTracks);
    entities.addAll(programVoices);
    entities.addAll(programs);
    entities.addAll(projects);
    entities.addAll(templateBindings);
    entities.addAll(templates);
    return entities;
  }
}
