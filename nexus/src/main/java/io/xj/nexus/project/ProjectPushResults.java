package io.xj.nexus.project;

import io.xj.hub.util.StringUtils;

import java.util.Objects;
import java.util.stream.Stream;

public class ProjectPushResults {
  int templates;
  int libraries;
  int programs;
  int instruments;
  int audiosDownloaded;
  int audiosUploaded;

  public ProjectPushResults(int templates, int libraries, int programs, int instruments, int audiosDownloaded, int audiosUploaded) {
    this.templates = templates;
    this.libraries = libraries;
    this.programs = programs;
    this.instruments = instruments;
    this.audiosDownloaded = audiosDownloaded;
    this.audiosUploaded = audiosUploaded;
  }

  public ProjectPushResults() {
    this.templates = 0;
    this.libraries = 0;
    this.programs = 0;
    this.instruments = 0;
    this.audiosDownloaded = 0;
    this.audiosUploaded = 0;
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

  public int getAudiosDownloaded() {
    return audiosDownloaded;
  }

  public int getAudiosUploaded() {
    return audiosUploaded;
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

  public void addAudiosDownloaded(int count) {
    this.audiosDownloaded += count;
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

  public void incrementAudiosDownloaded() {
    this.audiosDownloaded++;
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
        audiosDownloaded > 0 ? String.format("%s downloaded", describeCount("audio", audiosDownloaded)):null,
        audiosUploaded > 0 ? String.format("%s uploaded", describeCount("audio", audiosUploaded)):null
      ).filter(Objects::nonNull).toList())
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
}
