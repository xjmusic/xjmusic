// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.music.Note;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.InstrumentMeme;
import io.xj.hub.pojos.Library;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramMeme;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceBinding;
import io.xj.hub.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.pojos.ProgramSequenceChord;
import io.xj.hub.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.pojos.ProgramSequencePattern;
import io.xj.hub.pojos.ProgramSequencePatternEvent;
import io.xj.hub.pojos.ProgramVoice;
import io.xj.hub.pojos.ProgramVoiceTrack;
import io.xj.hub.pojos.Project;
import io.xj.hub.pojos.ProjectUser;
import io.xj.hub.pojos.Template;
import io.xj.hub.pojos.TemplateBinding;
import io.xj.hub.pojos.User;
import io.xj.hub.util.Multiset;
import io.xj.hub.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments) https://www.pivotaltracker.com/story/show/154350346
 <p>
 Refactoring this class ala `HubContent` extends common `EntityStore` implementation https://www.pivotaltracker.com/story/show/173803936
 */
public class HubContent {
  final Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>> store = new ConcurrentHashMap<>();
  final Set<Error> errors = new HashSet<>();
  boolean demo = false;

  /**
   Create an empty hub content object
   */
  public HubContent() {
  }

  /**
   Create a hub content object from a collection of entities
   */
  public HubContent(Collection<?> entities) {
    try {
      putAll(entities);
      this.demo = false;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   Create a hub content object from a collection of entities and whether it's demo content

   @param entities from which to create content
   @param demo     whether to create demo content
   @throws RuntimeException on failure
   */
  public HubContent(
    Collection<?> entities,
    boolean demo
  ) throws RuntimeException {
    try {
      putAll(entities);
      this.demo = demo;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   Create a hub content object from a payload

   @param payload from which to get content object
   @return hub content
   @throws RuntimeException on failure
   */
  public static HubContent from(HubContentPayload payload) throws RuntimeException {
    return new HubContent(payload.getAllEntities(), payload.getDemo());
  }

  /**
   Get all errors

   @return all errors
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Error> getErrors() {
    return errors;
  }

  /**
   Add an error to the content

   @param errors to add
   */
  public void setErrors(List<Error> errors) {
    try {
      this.errors.clear();
      this.errors.addAll(errors);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   Get all Users

   @return Users
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<User> getUsers() {
    return getAll(User.class);
  }

  /**
   Set all users

   @param users to set
   */
  public void setUsers(Collection<User> users) throws Exception {
    setAll(User.class, users);
  }

  /**
   Get all Users

   @return Project Users
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProjectUser> getProjectUsers() {
    return getAll(ProjectUser.class);
  }

  /**
   Set all Project Users

   @param projectUsers to set
   */
  public void setProjectUsers(Collection<ProjectUser> projectUsers) throws Exception {
    setAll(ProjectUser.class, projectUsers);
  }

  /**
   Get all Instruments

   @return Instruments
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Instrument> getInstruments() {
    return getAll(Instrument.class);
  }

  /**
   Set all instruments

   @param instruments to set
   */
  public void setInstruments(Collection<Instrument> instruments) throws Exception {
    setAll(Instrument.class, instruments);
  }

  /**
   Get all InstrumentAudios

   @return InstrumentAudios
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<InstrumentAudio> getInstrumentAudios() {
    return getAll(InstrumentAudio.class);
  }

  /**
   Set all instrumentAudios

   @param instrumentAudios to set
   */
  public void setInstrumentAudios(Collection<InstrumentAudio> instrumentAudios) throws Exception {
    setAll(InstrumentAudio.class, instrumentAudios);
  }

  /**
   Get all InstrumentMemes

   @return InstrumentMemes
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<InstrumentMeme> getInstrumentMemes() {
    return getAll(InstrumentMeme.class);
  }

  /**
   Set all instrumentMemes

   @param instrumentMemes to set
   */
  public void setInstrumentMemes(Collection<InstrumentMeme> instrumentMemes) throws Exception {
    setAll(InstrumentMeme.class, instrumentMemes);
  }

  /**
   Get all Programs

   @return Programs
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Program> getPrograms() {
    return getAll(Program.class);
  }

  /**
   Set all programs

   @param programs to set
   */
  public void setPrograms(Collection<Program> programs) throws Exception {
    setAll(Program.class, programs);
  }

  /**
   Get all program memes

   @return memes of program
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramMeme> getProgramMemes() {
    return getAll(ProgramMeme.class);
  }

  /**
   Set all programMemes

   @param programMemes to set
   */
  public void setProgramMemes(Collection<ProgramMeme> programMemes) throws Exception {
    setAll(ProgramMeme.class, programMemes);
  }

  /**
   Get all ProgramSequences

   @return ProgramSequences
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequence> getProgramSequences() {
    return getAll(ProgramSequence.class);
  }

  /**
   Set all programSequences

   @param programSequences to set
   */
  public void setProgramSequences(Collection<ProgramSequence> programSequences) throws Exception {
    setAll(ProgramSequence.class, programSequences);
  }

  /**
   Get all ProgramSequenceBindings

   @return ProgramSequenceBindings
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequenceBinding> getProgramSequenceBindings() {
    return getAll(ProgramSequenceBinding.class);
  }

  /**
   Set all programSequenceBindings

   @param programSequenceBindings to set
   */
  public void setProgramSequenceBindings(Collection<ProgramSequenceBinding> programSequenceBindings) throws Exception {
    setAll(ProgramSequenceBinding.class, programSequenceBindings);
  }

  /**
   Get all ProgramSequenceBindingMemes

   @return ProgramSequenceBindingMemes
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequenceBindingMeme> getProgramSequenceBindingMemes() {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  /**
   Set all programSequenceBindingMemes

   @param programSequenceBindingMemes to set
   */
  public void setProgramSequenceBindingMemes(Collection<ProgramSequenceBindingMeme> programSequenceBindingMemes) throws Exception {
    setAll(ProgramSequenceBindingMeme.class, programSequenceBindingMemes);
  }

  /**
   Get all ProgramSequencePatterns

   @return ProgramSequencePatterns
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequencePattern> getProgramSequencePatterns() {
    return getAll(ProgramSequencePattern.class);
  }

  /**
   Set all programSequencePatterns

   @param programSequencePatterns to set
   */
  public void setProgramSequencePatterns(Collection<ProgramSequencePattern> programSequencePatterns) throws Exception {
    setAll(ProgramSequencePattern.class, programSequencePatterns);
  }

  /**
   Get all ProgramSequencePatterns for a given sequence and voice

   @return ProgramSequencePatterns for sequence and voice
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequencePattern> getPatternsOfSequenceAndVoice(UUID programSequenceId, UUID programVoiceId) {
    return getProgramSequencePatterns().stream()
      .filter(p -> programSequenceId.equals(p.getProgramSequenceId()) && programVoiceId.equals(p.getProgramVoiceId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramEvents

   @return ProgramEvents
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequencePatternEvent> getProgramSequencePatternEvents() {
    return getAll(ProgramSequencePatternEvent.class);
  }

  /**
   Set all programSequencePatternEvents

   @param programSequencePatternEvents to set
   */
  public void setProgramSequencePatternEvents(Collection<ProgramSequencePatternEvent> programSequencePatternEvents) throws Exception {
    setAll(ProgramSequencePatternEvent.class, programSequencePatternEvents);
  }

  /**
   Get all ProgramSequenceChords

   @return ProgramSequenceChords
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequenceChord> getProgramSequenceChords() {
    return getAll(ProgramSequenceChord.class);
  }

  /**
   Set all programSequenceChords

   @param programSequenceChords to set
   */
  public void setProgramSequenceChords(Collection<ProgramSequenceChord> programSequenceChords) throws Exception {
    setAll(ProgramSequenceChord.class, programSequenceChords);
  }

  /**
   Get all ProgramSequencePatternEvents

   @return ProgramSequencePatternEvents
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings() {
    return getAll(ProgramSequenceChordVoicing.class);
  }

  /**
   Set all programSequenceChordVoicings

   @param programSequenceChordVoicings to set
   */
  public void setProgramSequenceChordVoicings(Collection<ProgramSequenceChordVoicing> programSequenceChordVoicings) throws Exception {
    setAll(ProgramSequenceChordVoicing.class, programSequenceChordVoicings);
  }

  /**
   Get all ProgramVoices

   @return ProgramVoices
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramVoice> getProgramVoices() {
    return getAll(ProgramVoice.class);
  }

  /**
   Set all programVoices

   @param programVoices to set
   */
  public void setProgramVoices(Collection<ProgramVoice> programVoices) throws Exception {
    setAll(ProgramVoice.class, programVoices);
  }

  /**
   Get all program voice tracks

   @return tracks for program
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<ProgramVoiceTrack> getProgramVoiceTracks() {
    return getAll(ProgramVoiceTrack.class);
  }

  /**
   Set all programVoiceTracks

   @param programVoiceTracks to set
   */
  public void setProgramVoiceTracks(Collection<ProgramVoiceTrack> programVoiceTracks) throws Exception {
    setAll(ProgramVoiceTrack.class, programVoiceTracks);
  }

  /**
   Get all libraries

   @return libraries
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Library> getLibraries() {
    return getAll(Library.class);
  }

  /**
   Set all libraries

   @param libraries to set
   */
  public void setLibraries(Collection<Library> libraries) throws Exception {
    setAll(Library.class, libraries);
  }

  /**
   Get all templates

   @return templates
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Template> getTemplates() {
    return getAll(Template.class);
  }

  /**
   Set all templates

   @param templates to set
   */
  public void setTemplates(Collection<Template> templates) throws Exception {
    setAll(Template.class, templates);
  }

  /**
   @return all projects
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Project getProject() {
    return getAll(Project.class).stream().findFirst().orElse(null);
  }

  /**
   Set all projects

   @param project to set
   */
  public void setProject(Project project) {
    store.put(Project.class, new ConcurrentHashMap<>(Map.of(project.getId(), project)));
  }

  /**
   Get all template bindings
   <p>
   Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569

   @return all template bindings
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<TemplateBinding> getTemplateBindings() {
    return getAll(TemplateBinding.class);
  }

  /**
   Set all templateBindings

   @param templateBindings to set
   */
  public void setTemplateBindings(Collection<TemplateBinding> templateBindings) throws Exception {
    setAll(TemplateBinding.class, templateBindings);
  }

  /**
   Get all bindings for the given template id

   @return template bindings
   */
  public Collection<TemplateBinding> getBindingsOfTemplate(UUID templateId) {
    return getAll(TemplateBinding.class).stream()
      .filter(binding -> binding.getTemplateId().equals(templateId))
      .toList();
  }

  /**
   Get all available sequence pattern offsets of a given sequence, sorted of offset

   @param sequenceBinding for which to get available sequence pattern offsets
   @return collection of available sequence pattern offsets
   */
  public Collection<Integer> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) {
    return getProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(sequenceBinding.getProgramId()))
      .map(ProgramSequenceBinding::getOffset)
      .distinct()
      .sorted(Integer::compareTo)
      .collect(Collectors.toList());
  }

  /**
   Get all Audios for a given instrument id

   @param id of instrument for which to get audios
   @return audios of instrument id
   */
  public Collection<InstrumentAudio> getAudiosOfInstrument(UUID id) {
    return getInstrumentAudios().stream()
      .filter(a -> id.equals(a.getInstrumentId()))
      .collect(Collectors.toList());
  }

  /**
   Get all InstrumentAudios for a given Instrument

   @param instrument for which to get audios
   @return audios for instrument
   */
  public Collection<InstrumentAudio> getAudiosOfInstrument(Instrument instrument) {
    return getAudiosOfInstrument(instrument.getId());
  }

  /**
   Get the sequence bindings for a given sequence

   @param sequence for which to get bindings
   @return bindings of sequence
   */
  public Collection<ProgramSequenceBinding> getBindingsOfSequence(ProgramSequence sequence) {
    return getBindingsOfSequence(sequence.getId());
  }

  /**
   Get the sequence bindings for a given sequence id

   @param sequenceId for which to get bindings
   @return bindings of sequence
   */
  public Collection<ProgramSequenceBinding> getBindingsOfSequence(UUID sequenceId) {
    return getProgramSequenceBindings().stream()
      .filter(b -> sequenceId.equals(b.getProgramSequenceId()))
      .collect(Collectors.toList());
  }

  /**
   Get the sequence binding memes for a given program

   @param program for which to get sequence binding memes
   @return sequence binding memes of program
   */
  public Collection<ProgramSequenceBindingMeme> getSequenceBindingMemesOfProgram(Program program) {
    return getSequenceBindingMemesOfProgram(program.getId());
  }

  /**
   Get the sequence binding memes for a given program id

   @param programId for which to get sequence binding memes
   @return sequence binding memes of program
   */
  public Collection<ProgramSequenceBindingMeme> getSequenceBindingMemesOfProgram(UUID programId) {
    return getProgramSequenceBindingMemes().stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .collect(Collectors.toList());
  }

  /**
   Get sequence bindings at a specified offset.
   If the target offset is not found in the chosen Main Program,
   we'll find the nearest matching offset, and return all bindings at that offset.
   <p>
   Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278

   @param program        for which to get sequence bindings
   @param offset         to get sequence bindings at
   @param includeNearest whether to include the nearest offset if the target offset is not found
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getBindingsAtOffsetOfProgram(Program program, Integer offset, boolean includeNearest) {
    return getBindingsAtOffsetOfProgram(program.getId(), offset, includeNearest);
  }

  /**
   Get sequence bindings at a specified offset.
   If the target offset is not found in the chosen Main Program,
   we'll find the nearest matching offset, and return all bindings at that offset.
   <p>
   Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278

   @param programId      for which to get sequence bindings
   @param offset         to get sequence bindings at
   @param includeNearest whether to include the nearest offset if the target offset is not found
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getBindingsAtOffsetOfProgram(UUID programId, Integer offset, boolean includeNearest) {
    if (includeNearest) {
      var candidates = getProgramSequenceBindings().stream()
        .filter(psb -> Objects.equals(psb.getProgramId(), programId)).toList();
      var actualOffset = candidates.stream()
        .map(ProgramSequenceBinding::getOffset)
        .min(Comparator.comparing(psbOffset -> Math.abs(psbOffset - offset)));
      return actualOffset.map(integer -> getProgramSequenceBindings().stream()
        .filter(psb ->
          Objects.equals(psb.getProgramId(), programId) &&
            Objects.equals(psb.getOffset(), integer))
        .collect(Collectors.toList())).orElseGet(List::of);
    } else {
      return getProgramSequenceBindings().stream()
        .filter(psb -> Objects.equals(psb.getProgramId(), programId) && Objects.equals(psb.getOffset(), offset))
        .collect(Collectors.toList());
    }
  }

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param sequence for which to get chords
   @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChordsOfSequence(ProgramSequence sequence) {
    return getChordsOfSequence(sequence.getId());
  }

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param programSequenceId for which to get chords
   @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChordsOfSequence(UUID programSequenceId) {
    return getProgramSequenceChords().stream()
      .filter(e -> programSequenceId.equals(e.getProgramSequenceId()))
      .collect(Collectors.toList());
  }

  /**
   Get patterns for a given program

   @param programId for which to get patterns
   @return patterns for given program
   */
  public Collection<ProgramSequencePattern> getSequencePatternsOfProgram(UUID programId) {
    return getProgramSequencePatterns().stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .collect(Collectors.toList());
  }

  /**
   Get patterns for a given program pattern, sorted of position

   @param program for which to get patterns
   @return patterns for given program pattern
   */
  public Collection<ProgramSequencePattern> getSequencePatternsOfProgram(Program program) {
    return getSequencePatternsOfProgram(program.getId());
  }

  /**
   Get events for a given program

   @param programId for which to get events
   @return events for given program
   */
  public List<ProgramSequencePatternEvent> getSequencePatternEventsOfProgram(UUID programId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program pattern, sorted of position

   @param pattern for which to get events
   @return events for given program pattern
   */
  public List<ProgramSequencePatternEvent> getEventsOfPattern(ProgramSequencePattern pattern) {
    return getEventsOfPattern(pattern.getId());
  }

  /**
   Get events for a given program sequence pattern id, sorted of position

   @param patternId for which to get events
   @return events for given pattern id
   */
  public List<ProgramSequencePatternEvent> getEventsOfPattern(UUID patternId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> patternId.equals(m.getProgramSequencePatternId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program track, sorted of position

   @param track for which to get events
   @return events for given program track
   */
  public List<ProgramSequencePatternEvent> getEventsOfTrack(ProgramVoiceTrack track) {
    return getEventsOfTrack(track.getId());
  }

  /**
   Get events for a given program voice track id, sorted of position

   @param trackId for which to get events
   @return events for given track id
   */
  public List<ProgramSequencePatternEvent> getEventsOfTrack(UUID trackId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> trackId.equals(m.getProgramVoiceTrackId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program pattern and track, sorted of position

   @param pattern for which to get events
   @param track   for which to get events
   @return events for given program pattern
   */
  public List<ProgramSequencePatternEvent> getEventsOfPatternAndTrack(ProgramSequencePattern pattern, ProgramVoiceTrack track) {
    return getEventsOfPatternAndTrack(pattern.getId(), track.getId());
  }

  /**
   Get events for a given program sequence pattern id and track id, sorted of position

   @param patternId for which to get events
   @param trackId   for which to get events
   @return events for given pattern id
   */
  public List<ProgramSequencePatternEvent> getEventsOfPatternAndTrack(UUID patternId, UUID trackId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> patternId.equals(m.getProgramSequencePatternId()) && trackId.equals(m.getProgramVoiceTrackId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   get Instrument of id

   @param id of Instrument to get
   @return Instrument
   */
  public Optional<Instrument> getInstrument(UUID id) {
    return get(Instrument.class, id);
  }

  /**
   get Instrument Meme of id

   @param id of Instrument Meme to get
   @return InstrumentMeme
   */
  public Optional<InstrumentMeme> getInstrumentMeme(UUID id) {
    return get(InstrumentMeme.class, id);
  }

  /**
   get InstrumentAudio of id

   @param id of InstrumentAudio to get
   @return InstrumentAudio
   */
  public Optional<InstrumentAudio> getInstrumentAudio(UUID id) {
    return get(InstrumentAudio.class, id);
  }

  /**
   Get all instrument audios for the given instrument types and modes

   @param types of instrument
   @param modes of instrument
   @return all audios for instrument type
   */
  public Collection<InstrumentAudio> getAudiosOfInstrumentTypesAndModes(Collection<InstrumentType> types, Collection<InstrumentMode> modes) {
    return getInstrumentsOfTypesAndModes(types, modes).stream()
      .flatMap(instrument -> getAudiosOfInstrument(instrument.getId()).stream())
      .toList();
  }

  /**
   Get all instrument audios for the given instrument types

   @param types of instrument
   @return all audios for instrument type
   */
  public Collection<InstrumentAudio> getAudiosOfInstrumentTypes(Collection<InstrumentType> types) {
    return getInstrumentsOfTypes(types).stream()
      .flatMap(instrument -> getAudiosOfInstrument(instrument.getId()).stream())
      .toList();
  }

  /**
   Get a collection of all instruments of the given library

   @param library for which to get instruments
   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfLibrary(Library library) {
    return getInstrumentsOfLibrary(library.getId());
  }

  /**
   Get a collection of all instruments of the given library id

   @param libraryId for which to get instruments
   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfLibrary(UUID libraryId) {
    return getInstruments().stream()
      .filter(instrument -> libraryId.equals(instrument.getLibraryId()))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    return getInstruments().stream()
      .filter(instrument -> type.equals(instrument.getType()))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all instruments of particular types and modes

   @param types of instrument; empty list is a wildcard
   @param modes of instrument; empty list is a wildcard
   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfTypesAndModes(Collection<InstrumentType> types, Collection<InstrumentMode> modes) {
    return getInstruments().stream()
      .filter(instrument -> modes.isEmpty() || modes.contains(instrument.getMode()))
      .filter(instrument -> types.isEmpty() || types.contains(instrument.getType()))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all instruments of particular types

   @param types of instrument; empty list is a wildcard
   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfTypes(Collection<InstrumentType> types) {
    return getInstruments().stream()
      .filter(instrument -> types.isEmpty() || types.contains(instrument.getType()))
      .collect(Collectors.toList());
  }

  /**
   Get the instrument type for the given audio id

   @param instrumentAudioId for which to get instrument type
   @return instrument type
   @throws RuntimeException on failure
   */
  public InstrumentType getInstrumentTypeOfAudio(UUID instrumentAudioId) throws RuntimeException {
    return getInstrument(
      getInstrumentAudio(instrumentAudioId)
        .orElseThrow(() -> new RuntimeException("Can't get Instrument Audio!"))
        .getInstrumentId())
      .orElseThrow(() -> new RuntimeException("Can't get Instrument!"))
      .getType();
  }

  /**
   Get the instrument type for the given event

   @param event for which to get instrument type
   @return instrument type
   @throws RuntimeException on failure
   */
  public InstrumentType getInstrumentTypeOfEvent(ProgramSequencePatternEvent event) throws RuntimeException {
    return
      getVoiceOfEvent(event)
        .orElseThrow(() -> new RuntimeException("Can't get Program Voice!"))
        .getType();
  }

  /**
   Get memes of instrument

   @param instrumentId for which to get memes
   @return memes of instrument
   */
  public Collection<InstrumentMeme> getMemesOfInstrument(UUID instrumentId) {
    return getInstrumentMemes().stream()
      .filter(m -> instrumentId.equals(m.getInstrumentId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding for which to get memes
   @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemesOfSequenceBinding(ProgramSequenceBinding programSequenceBinding) {
    return getMemesOfSequenceBinding(programSequenceBinding.getId());
  }

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBindingId for which to get memes
   @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemesOfSequenceBinding(UUID programSequenceBindingId) {
    return getProgramSequenceBindingMemes().stream()
      .filter(m -> programSequenceBindingId.equals(m.getProgramSequenceBindingId()))
      .collect(Collectors.toList());
  }

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   */
  public Collection<String> getMemesAtBeginning(Program program) {
    Map<String, Boolean> memes = new HashMap<>();

    // add sequence memes
    getMemesOfProgram(program.getId()).forEach((meme ->
      memes.put(meme.getName(), true)));

    // add sequence binding memes
    for (ProgramSequenceBinding sequenceBinding : getBindingsAtOffsetOfProgram(program, 0, false))
      for (ProgramSequenceBindingMeme meme : getMemesOfSequenceBinding(sequenceBinding.getId()))
        memes.put(meme.getName(), true);

    return memes.keySet();
  }

  /**
   Get the pattern id for an event id

   @param eventId for which to get pattern
   @return pattern id
   */
  public UUID getPatternIdOfEvent(UUID eventId) throws RuntimeException {
    return getProgramSequencePatternEvent(eventId)
      .orElseThrow(() -> new RuntimeException(String.format("content does not content ProgramSequencePatternEvent[%s]", eventId)))
      .getProgramSequencePatternId();
  }

  /**
   Get all patterns for a sequence

   @param sequence for which to get patterns
   @return patterns of sequence
   */
  public Collection<ProgramSequencePattern> getPatternsOfSequence(ProgramSequence sequence) {
    return getPatternsOfSequence(sequence.getId());
  }

  /**
   Get all patterns for a sequence ID

   @param sequence for which to get patterns
   @return patterns of sequence
   */
  public Collection<ProgramSequencePattern> getPatternsOfSequence(UUID sequence) {
    return getProgramSequencePatterns().stream()
      .filter(p -> sequence.equals(p.getProgramSequenceId()))
      .collect(Collectors.toList());
  }

  /**
   Get all patterns for a voice

   @param voice for which to get patterns
   @return patterns of voice
   */
  public Collection<ProgramSequencePattern> getPatternsOfVoice(ProgramVoice voice) {
    return getPatternsOfVoice(voice.getId());
  }

  /**
   Get all patterns for a voice ID

   @param voice for which to get patterns
   @return patterns of voice
   */
  public Collection<ProgramSequencePattern> getPatternsOfVoice(UUID voice) {
    return getProgramSequencePatterns().stream()
      .filter(p -> voice.equals(p.getProgramVoiceId()))
      .collect(Collectors.toList());
  }

  /**
   get Program of id

   @param id of Program to get
   @return Program
   */
  public Optional<Program> getProgram(UUID id) {
    return get(Program.class, id);
  }

  /**
   get Program Meme of id

   @param id of Program Meme to get
   @return ProgramMeme
   */
  public Optional<ProgramMeme> getProgramMeme(UUID id) {
    return get(ProgramMeme.class, id);
  }

  /**
   Get a collection of all programs of the given library

   @param library for which to get programs
   @return collection of programs
   */
  public Collection<Program> getProgramsOfLibrary(Library library) {
    return getProgramsOfLibrary(library.getId());
  }

  /**
   Get a collection of all programs of the given library id

   @param libraryId for which to get programs
   @return collection of programs
   */
  public Collection<Program> getProgramsOfLibrary(UUID libraryId) {
    return getPrograms().stream()
      .filter(program -> libraryId.equals(program.getLibraryId()))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  public Collection<Program> getProgramsOfType(ProgramType type) {
    return getPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   Get memes of program

   @param programId for which to get memes
   @return memes of program
   */
  public Collection<ProgramMeme> getMemesOfProgram(UUID programId) {
    return getAll(ProgramMeme.class).stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .collect(Collectors.toList());
  }

  /**
   get ProgramSequence of id

   @param id of ProgramSequence to get
   @return ProgramSequence
   */
  public Optional<ProgramSequence> getProgramSequence(UUID id) {
    return get(ProgramSequence.class, id);
  }

  /**
   Get the program sequence for a given program sequence binding

   @param sequenceBinding for which to get program sequence
   @return program sequence for the given program sequence binding
   */
  public Optional<ProgramSequence> getSequenceOfBinding(ProgramSequenceBinding sequenceBinding) {
    return getProgramSequence(sequenceBinding.getProgramSequenceId());
  }

  /**
   Get all ProgramSequences

   @param programId to search for sequences
   @return ProgramSequences
   */
  public Collection<ProgramSequence> getSequencesOfProgram(UUID programId) {
    return getAll(ProgramSequence.class).stream().filter(s -> programId.equals(s.getProgramId())).toList();
  }

  /**
   get ProgramSequenceBinding of id

   @param id of ProgramSequenceBinding to get
   @return ProgramSequenceBinding
   */
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID id) {
    return get(ProgramSequenceBinding.class, id);
  }

  /**
   get ProgramSequenceBindingMeme of id

   @param id of ProgramSequenceBindingMeme to get
   @return ProgramSequenceBindingMeme
   */
  public Optional<ProgramSequenceBindingMeme> getProgramSequenceBindingMeme(UUID id) {
    return get(ProgramSequenceBindingMeme.class, id);
  }

  /**
   Get all sequence bindings for the given program

   @param programId for which to get bindings
   @return sequence bindings
   */
  public Collection<ProgramSequenceBinding> getSequenceBindingsOfProgram(UUID programId) {
    return getAll(ProgramSequenceBinding.class).stream().filter(b -> programId.equals(b.getProgramId())).collect(Collectors.toSet());
  }

  /**
   get ProgramSequencePattern of id

   @param id of ProgramSequencePattern to get
   @return ProgramSequencePattern
   */
  public Optional<ProgramSequencePattern> getProgramSequencePattern(UUID id) {
    return get(ProgramSequencePattern.class, id);
  }

  /**
   get ProgramSequencePatternEvent of id

   @param id of ProgramSequencePatternEvent to get
   @return ProgramSequencePatternEvent
   */
  public Optional<ProgramSequencePatternEvent> getProgramSequencePatternEvent(UUID id) {
    return get(ProgramSequencePatternEvent.class, id);
  }

  /**
   get ProgramSequenceChord of id

   @param id of ProgramSequenceChord to get
   @return ProgramSequenceChord
   */
  public Optional<ProgramSequenceChord> getProgramSequenceChord(UUID id) {
    return get(ProgramSequenceChord.class, id);
  }

  /**
   get ProgramSequenceChordVoicing of id

   @param id of ProgramSequenceChordVoicing to get
   @return ProgramSequenceChordVoicing
   */
  public Optional<ProgramSequenceChordVoicing> getProgramSequenceChordVoicing(UUID id) {
    return get(ProgramSequenceChordVoicing.class, id);
  }

  /**
   Get all ProgramSequenceChords

   @return ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getSequenceChordsOfProgram(UUID programId) {
    return getAll(ProgramSequenceChord.class).stream().filter(s -> programId.equals(s.getProgramId())).toList();
  }

  /**
   Get program sequence chord voicings

   @param programId to get sequence chord voicings of
   @return sequence chord voicings for program
   */
  public Collection<ProgramSequenceChordVoicing> getSequenceChordVoicingsOfProgram(UUID programId) {
    return getAll(ProgramSequenceChordVoicing.class).stream()
      .filter(v -> v.getProgramId().equals(programId))
      .filter(v -> Note.containsAnyValidNotes(v.getNotes()))
      .collect(Collectors.toList());
  }

  /**
   get ProgramVoice of id

   @param id of ProgramVoice to get
   @return ProgramVoice
   */
  public Optional<ProgramVoice> getProgramVoice(UUID id) {
    return get(ProgramVoice.class, id);
  }

  /**
   get ProgramVoiceTrack of id

   @param id of ProgramVoiceTrack to get
   @return ProgramVoiceTrack
   */
  public Optional<ProgramVoiceTrack> getProgramVoiceTrack(UUID id) {
    return get(ProgramVoiceTrack.class, id);
  }

  /**
   Get all program voice tracks for the given program id

   @param programId for which to get tracks
   @return tracks for program
   */
  public Collection<ProgramVoiceTrack> getTracksOfProgram(UUID programId) {
    return getAll(ProgramVoiceTrack.class).stream()
      .filter(track -> Objects.equals(programId, track.getProgramId()))
      .toList();
  }

  /**
   Get all program voice tracks for the given program type

   @param type of program
   @return all voice tracks for program type
   */
  public Collection<ProgramVoiceTrack> getTracksOfProgramType(ProgramType type) {
    return getProgramsOfType(type).stream()
      .flatMap(program -> getTracksOfProgram(program.getId()).stream())
      .toList();
  }

  /**
   Get all Program Voice Tracks for the given Voice

   @param voice for which to get tracks
   @return tracks for voice
   */
  public Collection<ProgramVoiceTrack> getTracksOfVoice(ProgramVoice voice) {
    return getTracksOfVoice(voice.getId());
  }

  /**
   Get all Program Voice Tracks for the given Voice ID

   @param voiceId for which to get tracks
   @return tracks for voice
   */
  public Collection<ProgramVoiceTrack> getTracksOfVoice(UUID voiceId) {
    return getAll(ProgramVoiceTrack.class).stream()
      .filter(track -> Objects.equals(voiceId, track.getProgramVoiceId()))
      .toList();
  }

  /**
   Get one Library of id

   @param id of library
   */
  public Optional<Library> getLibrary(UUID id) {
    return get(Library.class, id);
  }

  /**
   Get one Template of id

   @param id of template
   */
  public Optional<Template> getTemplate(UUID id) {
    return get(Template.class, id);
  }

  /**
   Get one Template Binding of id

   @param id of template binding
   */
  public Optional<TemplateBinding> getTemplateBinding(UUID id) {
    return get(TemplateBinding.class, id);
  }

  /**
   Get Program track for a given program event

   @param event to get program track of
   @return Program track for the given program event
   */
  public Optional<ProgramVoiceTrack> getTrackOfEvent(ProgramSequencePatternEvent event) {
    return getProgramVoiceTrack(event.getProgramVoiceTrackId());
  }

  /**
   Get all track names for a given program voice

   @param voice for which to get track names
   @return names of tracks for the given voice
   */
  public Collection<String> getTrackNamesOfVoice(ProgramVoice voice) {
    return getAll(ProgramVoiceTrack.class).stream()
      .filter(t -> voice.getId().equals(t.getProgramVoiceId()))
      .map(ProgramVoiceTrack::getName)
      .toList();
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord

   @param chord for which to get voicings
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicingsOfChord(ProgramSequenceChord chord) {
    return getVoicingsOfChord(chord.getId());
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord ID

   @param chordId for which to get voicings
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicingsOfChord(UUID chordId) {
    return getAll(ProgramSequenceChordVoicing.class).stream()
      .filter(e -> chordId.equals(e.getProgramSequenceChordId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord ID and Voice ID

   @param chord for which to get voicings
   @param voice for which to get voicings
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicingsOfChordAndVoice(ProgramSequenceChord chord, ProgramVoice voice) {
    return getVoicingsOfChordAndVoice(chord.getId(), voice.getId());
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord ID and Voice ID

   @param chordId for which to get voicings
   @param voiceId for which to get voicings
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicingsOfChordAndVoice(UUID chordId, UUID voiceId) {
    return getAll(ProgramSequenceChordVoicing.class).stream()
      .filter(e -> chordId.equals(e.getProgramSequenceChordId()) && voiceId.equals(e.getProgramVoiceId()))
      .collect(Collectors.toList());
  }

  /**
   Get Program voice for a given program event

   @param event to get program voice of
   @return Program voice for the given program event
   */
  public Optional<ProgramVoice> getVoiceOfEvent(ProgramSequencePatternEvent event) {
    var track = getTrackOfEvent(event);
    if (track.isEmpty()) return Optional.empty();
    return getProgramVoice(track.get().getProgramVoiceId());
  }

  /**
   Get all program voices for a given program

   @param program for which to get program voices
   @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoicesOfProgram(Program program) {
    return getVoicesOfProgram(program.getId());
  }

  /**
   Get all program voices for a given program

   @param programId for which to get program voices
   @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoicesOfProgram(UUID programId) {
    return getProgramVoices().stream()
      .filter(m -> m.getProgramId().equals(programId))
      .collect(Collectors.toList());
  }

  /**
   Update an attribute of an entity in the store

   @param type      of entity to update
   @param id        of entity to update
   @param attribute to update
   @param value     to set
   @throws RuntimeException on failure
   */
  public <N> boolean update(Class<N> type, UUID id, String attribute, Object value) throws Exception {
    var entity = get(type, id).orElseThrow(() -> new RuntimeException(String.format("%s[%s] not found", type.getSimpleName(), id)));
    var ov = EntityUtils.get(entity, attribute).orElseThrow(() -> new RuntimeException(String.format("%s[%s] does not have attribute %s", type.getSimpleName(), id, attribute)));
    EntityUtils.set(entity, attribute, value);
    put(entity);
    return !Objects.equals(ov, value);
  }

  /**
   Put an object to the store

   @param entity to store
   @throws RuntimeException on failure
   */
  public HubContent put(Object entity) {
    try {
      store.putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
      store.get(entity.getClass()).put(EntityUtils.getId(entity), entity);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   Delete a Hub Content entity from the store
   <p>
   HubContent has proper method to delete any entity of id
   https://www.pivotaltracker.com/story/show/186986806

   @param type of entity to delete
   @param uuid of entity to delete
   @return this content object, for chaining methods
   */
  public HubContent delete(Class<?> type, UUID uuid) {
    store.get(type).remove(uuid);
    return this;
  }

  /**
   Set all of a specific class of objects in the store

   @param type     of entity to store
   @param entities to store
   @throws RuntimeException on failure
   */
  public <N> void setAll(Class<N> type, Collection<N> entities) throws Exception {
    store.putIfAbsent(type, new ConcurrentHashMap<>());
    store.get(type).clear();
    for (N entity : entities)
      store.get(type).put(EntityUtils.getId(entity), entity);
  }

  /**
   Put all objects to the store

   @param entity to store
   @throws RuntimeException on failure
   */
  public void putAll(Collection<?> entity) {
    for (Object e : entity)
      put(e);
  }

  /**
   For reverse compatibility, HubContent can deserialize a payload with multiple projects- it takes the first one

   @param projects of which to set the first one as the project
   */
  public void setProjects(Collection<Project> projects) {
    var project = projects.stream().findFirst();
    project.ifPresent(this::setProject);
  }

  /**
   Get a count of total entities in this Hub Content

   @return total number of entities in this Hub Content
   <p>
   }
   */
  public int size() {
    return store.values().stream()
      .mapToInt(Map::size)
      .sum();
  }

  @Override
  public String toString() {
    Multiset<String> entityHistogram = new Multiset<>();
    store.values().stream()
      .flatMap(map -> map.values().stream()).toList()
      .forEach((Object obj) -> entityHistogram.add(StringUtils.getSimpleName(obj)));
    List<String> descriptors = new ArrayList<>();

    Collection<String> names = entityHistogram.elementSet().stream()
      .sorted(String.CASE_INSENSITIVE_ORDER)
      .toList();

    names.forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   Get an entity of a given type and id from the store, or throw an exception

   @param type to get
   @param id   to get
   @param <E>  class
   @return entity
   */
  public <E> Optional<E> get(Class<E> type, UUID id) {
    try {
      if (store.containsKey(type) && store.get(type).containsKey(id))
        //noinspection unchecked
        return Optional.of((E) store.get(type).get(id));
      // otherwise
      return Optional.empty();

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   Get all entities of a given type from the store, or throw an exception

   @param type to get
   @param <E>  class
   @return entity
   */
  public <E> Collection<E> getAll(Class<E> type) {
    if (store.containsKey(type))
      //noinspection unchecked
      return (Collection<E>) store.get(type).values();
    return List.of();
  }

  /**
   Whether the content contains instruments of the given type

   @param type of instrument for which to search
   @return true if present
   */
  public boolean hasInstrumentsOfType(InstrumentType type) {
    return getInstruments().stream()
      .anyMatch(instrument -> type.equals(instrument.getType()));
  }

  /**
   Whether the content contains instruments of the given mode

   @param mode of instrument for which to search
   @return true if present
   */
  public boolean hasInstrumentsOfMode(InstrumentMode mode) {
    return getInstruments().stream()
      .anyMatch(instrument -> mode.equals(instrument.getMode()));
  }

  /**
   Whether the content contains instruments of the given type

   @param type of instrument for which to search
   @param mode of instrument for which to search
   @return true if present
   */
  public boolean hasInstrumentsOfTypeAndMode(InstrumentType type, InstrumentMode mode) {
    return getInstruments().stream()
      .anyMatch(instrument -> type.equals(instrument.getType()) && mode.equals(instrument.getMode()));
  }

  /**
   Add an error to the content

   @param error to add
   @return this content object, for chaining
   */
  public HubContent addError(Error error) {
    errors.add(error);
    return this;
  }

  /**
   @return Whether this content is a demo
   */
  public boolean getDemo() {
    return demo;
  }

  /**
   Set whether this content is a demo

   @param demo whether this content is a demo
   */
  public void setDemo(boolean demo) {
    this.demo = demo;
  }

  /**
   Clear the content
   */
  public void clear() {
    store.clear();
    errors.clear();
    demo = false;
  }
}
