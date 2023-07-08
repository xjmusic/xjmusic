// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.ingest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.Lists;
import io.xj.hub.tables.pojos.*;

import java.util.Collection;
import java.util.List;

public class HubContentPayload {
  List<Template> templates;
  List<TemplateBinding> templateBindings;
  List<Instrument> instruments;
  List<InstrumentAudio> instrumentAudios;
  List<InstrumentMeme> instrumentMemes;
  List<Program> programs;
  List<ProgramMeme> programMemes;
  List<ProgramSequence> programSequences;
  List<ProgramSequenceBinding> programSequenceBindings;
  List<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  List<ProgramSequenceChord> programSequenceChords;
  List<ProgramSequenceChordVoicing> programSequenceChordVoicings;
  List<ProgramSequencePattern> programSequencePatterns;
  List<ProgramSequencePatternEvent> programSequencePatternEvents;
  List<ProgramVoice> programVoices;
  List<ProgramVoiceTrack> programVoiceTracks;


  public List<Instrument> getInstruments() {
    return instruments;
  }

  public HubContentPayload setInstruments(List<Instrument> instruments) {
    this.instruments = instruments;
    return this;
  }

  public List<InstrumentAudio> getInstrumentAudios() {
    return instrumentAudios;
  }

  public HubContentPayload setInstrumentAudios(List<InstrumentAudio> instrumentAudios) {
    this.instrumentAudios = instrumentAudios;
    return this;
  }

  public List<InstrumentMeme> getInstrumentMemes() {
    return instrumentMemes;
  }

  public HubContentPayload setInstrumentMemes(List<InstrumentMeme> instrumentMemes) {
    this.instrumentMemes = instrumentMemes;
    return this;
  }

  public List<Program> getPrograms() {
    return programs;
  }

  public HubContentPayload setPrograms(List<Program> programs) {
    this.programs = programs;
    return this;
  }

  public List<ProgramMeme> getProgramMemes() {
    return programMemes;
  }

  public HubContentPayload setProgramMemes(List<ProgramMeme> programMemes) {
    this.programMemes = programMemes;
    return this;
  }

  public List<ProgramSequence> getProgramSequences() {
    return programSequences;
  }

  public HubContentPayload setProgramSequences(List<ProgramSequence> programSequences) {
    this.programSequences = programSequences;
    return this;
  }

  public List<ProgramSequenceBinding> getProgramSequenceBindings() {
    return programSequenceBindings;
  }

  public HubContentPayload setProgramSequenceBindings(List<ProgramSequenceBinding> programSequenceBindings) {
    this.programSequenceBindings = programSequenceBindings;
    return this;
  }

  public List<ProgramSequenceBindingMeme> getProgramSequenceBindingMemes() {
    return programSequenceBindingMemes;
  }

  public HubContentPayload setProgramSequenceBindingMemes(List<ProgramSequenceBindingMeme> programSequenceBindingMemes) {
    this.programSequenceBindingMemes = programSequenceBindingMemes;
    return this;
  }

  public List<ProgramSequenceChord> getProgramSequenceChords() {
    return programSequenceChords;
  }

  public HubContentPayload setProgramSequenceChords(List<ProgramSequenceChord> programSequenceChords) {
    this.programSequenceChords = programSequenceChords;
    return this;
  }

  public List<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings() {
    return programSequenceChordVoicings;
  }

  public HubContentPayload setProgramSequenceChordVoicings(List<ProgramSequenceChordVoicing> programSequenceChordVoicings) {
    this.programSequenceChordVoicings = programSequenceChordVoicings;
    return this;
  }

  public List<ProgramSequencePattern> getProgramSequencePatterns() {
    return programSequencePatterns;
  }

  public HubContentPayload setProgramSequencePatterns(List<ProgramSequencePattern> programSequencePatterns) {
    this.programSequencePatterns = programSequencePatterns;
    return this;
  }

  public List<ProgramSequencePatternEvent> getProgramSequencePatternEvents() {
    return programSequencePatternEvents;
  }

  public HubContentPayload setProgramSequencePatternEvents(List<ProgramSequencePatternEvent> programSequencePatternEvents) {
    this.programSequencePatternEvents = programSequencePatternEvents;
    return this;
  }

  public List<ProgramVoice> getProgramVoices() {
    return programVoices;
  }

  public HubContentPayload setProgramVoices(List<ProgramVoice> programVoices) {
    this.programVoices = programVoices;
    return this;
  }

  public List<ProgramVoiceTrack> getProgramVoiceTracks() {
    return programVoiceTracks;
  }

  public HubContentPayload setProgramVoiceTracks(List<ProgramVoiceTrack> programVoiceTracks) {
    this.programVoiceTracks = programVoiceTracks;
    return this;
  }

  public List<Template> getTemplates() {
    return templates;
  }

  public HubContentPayload setTemplates(List<Template> templates) {
    this.templates = templates;
    return this;
  }

  public List<TemplateBinding> getTemplateBindings() {
    return templateBindings;
  }

  public HubContentPayload setTemplateBindings(List<TemplateBinding> templateBindings) {
    this.templateBindings = templateBindings;
    return this;
  }

  @JsonIgnore
  public Collection<Object> getAllEntities() {
    List<Object> entities = Lists.newArrayList();
    entities.addAll(templates);
    entities.addAll(templateBindings);
    entities.addAll(instruments);
    entities.addAll(instrumentAudios);
    entities.addAll(instrumentMemes);
    entities.addAll(programs);
    entities.addAll(programMemes);
    entities.addAll(programSequences);
    entities.addAll(programSequenceBindings);
    entities.addAll(programSequenceBindingMemes);
    entities.addAll(programSequenceChords);
    entities.addAll(programSequenceChordVoicings);
    entities.addAll(programSequencePatterns);
    entities.addAll(programSequencePatternEvents);
    entities.addAll(programVoices);
    entities.addAll(programVoiceTracks);
    return entities;
  }
}
