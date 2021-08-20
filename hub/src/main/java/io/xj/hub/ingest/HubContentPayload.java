package io.xj.hub.ingest;

import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentMeme;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.Template;
import io.xj.api.TemplateBinding;

import java.util.List;

public class HubContentPayload {
  private List<Template> templates;
  private List<TemplateBinding> templateBindings;
  private List<Instrument> instruments;
  private List<InstrumentAudio> instrumentAudios;
  private List<InstrumentMeme> instrumentMemes;
  private List<Program> programs;
  private List<ProgramMeme> programMemes;
  private List<ProgramSequence> programSequences;
  private List<ProgramSequenceBinding> programSequenceBindings;
  private List<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  private List<ProgramSequenceChord> programSequenceChords;
  private List<ProgramSequenceChordVoicing> programSequenceChordVoicings;
  private List<ProgramSequencePattern> programSequencePatterns;
  private List<ProgramSequencePatternEvent> programSequencePatternEvents;
  private List<ProgramVoice> programVoices;
  private List<ProgramVoiceTrack> programVoiceTracks;


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
}
