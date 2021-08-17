package io.xj.nexus.hub_client.client;

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

import java.util.Collection;

public class HubContentPayload {
  private Collection<Instrument> instruments;
  private Collection<InstrumentAudio> instrumentAudios;
  private Collection<InstrumentMeme> instrumentMemes;
  private Collection<Program> programs;
  private Collection<ProgramMeme> programMemes;
  private Collection<ProgramSequence> programSequences;
  private Collection<ProgramSequenceBinding> programSequenceBindings;
  private Collection<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  private Collection<ProgramSequenceChord> programSequenceChords;
  private Collection<ProgramSequenceChordVoicing> programSequenceChordVoicings;
  private Collection<ProgramSequencePattern> programSequencePatterns;
  private Collection<ProgramSequencePatternEvent> programSequencePatternEvents;
  private Collection<ProgramVoice> programVoices;
  private Collection<ProgramVoiceTrack> programVoiceTracks;


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
}
