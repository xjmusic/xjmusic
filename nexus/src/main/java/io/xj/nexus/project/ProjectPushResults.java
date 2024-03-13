package io.xj.nexus.project;

import io.xj.hub.util.StringUtils;
import io.xj.nexus.util.FormatUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectPushResults {
  Set<String> errors;
  int templates;
  int libraries;
  int programs;
  int instruments;
  int audios;
  int audiosUploaded;

  public ProjectPushResults() {
    this.templates = 0;
    this.libraries = 0;
    this.programs = 0;
    this.instruments = 0;
    this.audios = 0;
    this.audiosUploaded = 0;
    this.errors = new HashSet<>();
  }

  public Collection<String> getErrors() {
    return errors;
  }

  public int getTemplates() {
    return templates;
  }

  public int getLibraries() {
    return libraries;
  }

  public int getPrograms() {
    return programs;
  }

  public int getInstruments() {
    return instruments;
  }

  public int getAudios() {
    return audios;
  }

  public int getAudiosUploaded() {
    return audiosUploaded;
  }

  public void addError(String error) {
    this.errors.add(error);
  }

  public void addErrors(Collection<String> errors) {
    this.errors.addAll(errors);
  }

  public void addTemplates(int count) {
    this.templates += count;
  }

  public void addLibraries(int count) {
    this.libraries += count;
  }

  public void addPrograms(int count) {
    this.programs += count;
  }

  public void addInstruments(int count) {
    this.instruments += count;
  }

  public void addAudios(int i) {
    this.audios += i;
  }

  public void addAudiosUploaded(int count) {
    this.audiosUploaded += count;
  }

  public void incrementTemplates() {
    this.templates++;
  }

  public void incrementLibraries() {
    this.libraries++;
  }

  public void incrementPrograms() {
    this.programs++;
  }

  public void incrementInstruments() {
    this.instruments++;
  }

  public void incrementAudios() {
    this.audios++;
  }

  public void incrementAudiosUploaded() {
    this.audiosUploaded++;
  }

  @Override
  public String toString() {
    return String.format("Synchronized %s",
      FormatUtils.describeCounts(Map.of(
        "template", templates,
        "library", libraries,
        "program", programs,
        "instrument", instruments,
        "audio", audios,
        "uploaded audio", audiosUploaded
      ))
        + (errors.isEmpty() ? "" : String.format(" with %s: %s", FormatUtils.describeCount("error", errors.size()), StringUtils.toProperCsvAnd(errors.stream().sorted().toList())))
    );
  }

  /**
   Check if there are any errors

   @return true if there are errors
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}
