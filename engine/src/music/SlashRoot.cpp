// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/music/SlashRoot.h"


using namespace XJ;


std::regex SlashRoot::rgxSlashPost("[^/]*/([A-G♯#♭b]+)$");
std::regex SlashRoot::rgxSlashNote("/([A-G])$");
std::regex SlashRoot::rgxSlashNoteModified("/([A-G][♯#♭b])$");
std::regex SlashRoot::rgxSlashPre("^([^/]*)/");


SlashRoot::SlashRoot(const std::string &name) {
  post = StringUtils::match(rgxSlashPost, name).value_or("");
  pre = post.empty() ? name : StringUtils::match(rgxSlashPre, name).value_or("");

  const auto modifiedPitchClass = StringUtils::match(rgxSlashNoteModified, name);
  const auto slashNotePitchClass = StringUtils::match(rgxSlashNote, name);
  if (modifiedPitchClass.has_value()) {
    pitchClass = pitchClassOf(modifiedPitchClass.value());
  } else if (slashNotePitchClass.has_value()) {
    pitchClass = pitchClassOf(slashNotePitchClass.value());
  } else {
    pitchClass = std::nullopt;
  }
}


SlashRoot SlashRoot::of(const std::string &name) {
  return SlashRoot(name);
}


SlashRoot SlashRoot::none() {
  return SlashRoot("");
}


PitchClass SlashRoot::orDefault(PitchClass dpc) const {
  if (pitchClass == Atonal) return dpc;
  return pitchClass.value_or(dpc);
}


std::string SlashRoot::computePre(const std::string &description) {
  if (description.empty()) return "";
  if (description[0] == '/') return "";
  return StringUtils::match(rgxSlashPre, description).value_or(description);
}


bool SlashRoot::has_value(const std::string &name) {
  return std::regex_search(name, rgxSlashPost);
}


bool SlashRoot::has_value() const {
  return !post.empty();
}


std::string SlashRoot::display(const Accidental withOptional) const {
  if (pitchClass.has_value() && Atonal != pitchClass) {
    std::ostringstream oss;
    oss << "/" << stringOf(pitchClass.value(), withOptional);
    return oss.str();
  }

  if (!post.empty()) {
    std::ostringstream oss;
    oss << "/" << post;
    return oss.str();
  }

  return "";
}


bool SlashRoot::operator==(const SlashRoot &other) const {
  return ((!post.empty() && !other.post.empty() && post == other.post) || pitchClass == other.pitchClass);
}
