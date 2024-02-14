package io.xj.nexus.project;

import io.xj.hub.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

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
    return String.format("Synchronized %s", StringUtils.toProperCsvAnd(Stream.of(
        templates > 0 ? describeCount("template", templates):null,
        libraries > 0 ? describeCount("library", libraries):null,
        programs > 0 ? describeCount("program", programs):null,
        instruments > 0 ? describeCount("instrument", instruments):null,
        audios > 0 ? describeCount("audio", audios):null,
        audiosUploaded > 0 ? String.format("%s uploaded", describeCount("audio", audiosUploaded)):null
      ).filter(Objects::nonNull).toList())
        + (errors.isEmpty() ? "":String.format(" with %s: %s", describeCount("error", errors.size()), StringUtils.toProperCsvAnd(errors.stream().sorted().toList())))
    );
  }

  /**
   Describe a count of something, the name pluralized if necessary

   @param name  of the thing
   @param count of the thing
   @return description of the count
   */
  private String describeCount(String name, long count) {
    return String.format("%d %s", count, count > 1 ? StringUtils.toPlural(name):name);
  }

  /**
   Check if there are any errors

   @return true if there are errors
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}
