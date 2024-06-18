// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/util/StringUtils.h"
#include "xjmusic/music/Chord.h"
#include "xjmusic/music/Step.h"


using namespace XJ;


ChordSynonym::ChordSynonym(const std::string &match, bool caseSensitive) {
  this->match = match;
  this->caseSensitive = caseSensitive;
}


bool ChordSynonym::operator==(const ChordSynonym &other) const {
  return this->match == other.match && this->caseSensitive == other.caseSensitive;
}


bool ChordSynonym::matches(const std::string &input) const {
  return caseSensitive ? match == input : StringUtils::toLowerCase(match) ==
                                          StringUtils::toLowerCase(input);
}


ChordForm::ChordForm(const std::string &description, const std::vector<ChordSynonym> &synonyms) {
  this->description = description;
  this->synonyms = synonyms;
}


bool ChordForm::operator==(const ChordForm &other) const {
  if (this == &other) return true;
  return this->description == other.description && this->synonyms.size() == other.synonyms.size()
         && std::equal(this->synonyms.begin(), this->synonyms.end(), other.synonyms.begin());
}


bool ChordForm::matches(
    const std::string &input) const { // NOLINT(*-convert-member-functions-to-static) because it uses the synonyms member
  if (input.empty()) return false;
  return std::any_of(synonyms.begin(), synonyms.end(), [&input](const ChordSynonym &synonym) {
    return synonym.matches(input);
  });
}


std::size_t ChordForm::hashCode() {
  std::size_t h1 = std::hash<std::string>{}(description);
  std::size_t h2 = 0;
  for (const auto &synonym: synonyms) {
    h2 ^= std::hash<std::string>{}(synonym.match) + 0x9e3779b9 + (h2 << 6) + (h2 >> 2);
  }
  return h1 ^ (h2 << 1);
}


std::vector<ChordForm> Chord::forms = {
    ChordForm("", {ChordSynonym("maj"), ChordSynonym("major"), ChordSynonym("M", true)}),
    ChordForm("-", {ChordSynonym("min"), ChordSynonym("minor"), ChordSynonym("m", true), ChordSynonym("mi")}),
    ChordForm("maj7", {ChordSynonym("major7"), ChordSynonym("M7", true), ChordSynonym("∆")}),
    ChordForm("-7",
              {ChordSynonym("min7"), ChordSynonym("minor7"), ChordSynonym("m7", true), ChordSynonym("mi7")}),
    ChordForm("dim", {ChordSynonym("diminished"), ChordSynonym("°")}),
    ChordForm("dim7", {ChordSynonym("diminished7"), ChordSynonym("°7")}),
    ChordForm("-7b5",
              {ChordSynonym("min7b5"), ChordSynonym("minor7b5"), ChordSynonym("m7b5", true), ChordSynonym("Ø"),
               ChordSynonym("half dim"), ChordSynonym("half diminished")}),
    ChordForm("+", {ChordSynonym("aug"), ChordSynonym("augmented"), ChordSynonym("#5")}),
    ChordForm("+7", {ChordSynonym("aug7"), ChordSynonym("augmented7"), ChordSynonym("7#5")}),
    ChordForm("7", {ChordSynonym("dom7"), ChordSynonym("dominant 7"), ChordSynonym("dominant")}),
    ChordForm("7/9",
              {ChordSynonym("dom7/9"), ChordSynonym("dominant 7/9"), ChordSynonym("9"), ChordSynonym("7add9")}),
    ChordForm("7/9/13", {ChordSynonym("dom 7/9/13"), ChordSynonym("dominant 7/9/13"), ChordSynonym("7/6/9"),
                         ChordSynonym("dom7 6/9"), ChordSynonym("dominant 7 6/9")}),
    ChordForm("7/13", {ChordSynonym("dom 7/13"), ChordSynonym("dominant 7/13"), ChordSynonym("7/6"),
                       ChordSynonym("dom7/6"), ChordSynonym("dominant 7/6"), ChordSynonym("13")}),
    ChordForm("7b9", {ChordSynonym("dom7b9"), ChordSynonym("dominant 7b9")}),
    ChordForm("7b9b13", {ChordSynonym("dom7b9b13"), ChordSynonym("dominant 7b9b13")}),
    ChordForm("7b9/13", {ChordSynonym("dom7b9/13"), ChordSynonym("dominant 7 b9/13")}),
    ChordForm("7#9", {ChordSynonym("dom7#9"), ChordSynonym("dominant 7#9")}),
    ChordForm("7#9b13", {ChordSynonym("dom7#9b13"), ChordSynonym("dominant 7#9b13")}),
    ChordForm("7/9b13", {ChordSynonym("dom7/9b13"), ChordSynonym("dominant 7/9b13")}),
    ChordForm("7#11", {ChordSynonym("dom7#11"), ChordSynonym("dominant 7#11"), ChordSynonym("7b5"),
                       ChordSynonym("dom7b5"), ChordSynonym("dominant 7b5")}),
    ChordForm("7add4",
              {ChordSynonym("dom7add4"), ChordSynonym("dominant 7add4"), ChordSynonym("7/11"), ChordSynonym("11"),
               ChordSynonym("dom7/11"), ChordSynonym("dominant7/11")}),
    ChordForm("7sus4", {ChordSynonym("dom7sus4"), ChordSynonym("dominant 7sus4"), ChordSynonym("7sus")}),
    ChordForm("sus4", {ChordSynonym("sus")}),
    ChordForm("sus2", {ChordSynonym("5add9"), ChordSynonym("5add2")}),
    ChordForm("5", {}),
    ChordForm("add9", {
        ChordSynonym("maj add9"), ChordSynonym("major add9"), ChordSynonym("Madd2", true),
        ChordSynonym("Madd9", true), ChordSynonym("add2"), ChordSynonym("maj add2"),
        ChordSynonym("major add2")
    }),
    ChordForm("-7/9", {
        ChordSynonym("min7/9"), ChordSynonym("minor7/9"), ChordSynonym("m7/9", true),
        ChordSynonym("m7add9", true), ChordSynonym("min7add9"), ChordSynonym("minor7add9"),
        ChordSynonym("mi7/9"), ChordSynonym("mi7add9"), ChordSynonym("-7add9")
    }),
    ChordForm("maj7/9", {
        ChordSynonym("major7/9"), ChordSynonym("M7/9", true), ChordSynonym("M7add9", true),
        ChordSynonym("∆9"), ChordSynonym("maj7add9"), ChordSynonym("major7add9"),
        ChordSynonym("maj7add2"), ChordSynonym("major7add2")
    }),
    ChordForm("6", {ChordSynonym("maj6"), ChordSynonym("major6"), ChordSynonym("M6", true)}),
    ChordForm("6/9", {
        ChordSynonym("maj6/9"), ChordSynonym("major6/9"), ChordSynonym("M6/9", true),
        ChordSynonym("M6add9", true), ChordSynonym("6add9"), ChordSynonym("maj6add9"),
        ChordSynonym("major6add9")
    }),
    ChordForm("-6",
              {ChordSynonym("min6"), ChordSynonym("minor6"), ChordSynonym("m6", true), ChordSynonym("mi6")}),
    ChordForm("add4", {
        ChordSynonym("maj add4"), ChordSynonym("major add4"), ChordSynonym("Madd4", true),
        ChordSynonym("Madd11", true), ChordSynonym("add11"), ChordSynonym("maj add11"),
        ChordSynonym("major add11")
    }),
    ChordForm("-7/11", {
        ChordSynonym("min7/11"), ChordSynonym("minor7/11"), ChordSynonym("m7/11", true),
        ChordSynonym("m11", true), ChordSynonym("-11"), ChordSynonym("min11"),
        ChordSynonym("minor11"), ChordSynonym("mi11"), ChordSynonym("mi7/11")
    }),
    ChordForm("-7b5/11", {
        ChordSynonym("-7b5/11"), ChordSynonym("minor7b5/11"), ChordSynonym("m7b5/11", true),
        ChordSynonym("Ø11"), ChordSynonym("half dim 11"), ChordSynonym("half diminished 11")
    }),
    ChordForm("maj7#11", {ChordSynonym("major7#11"), ChordSynonym("M7#11", true), ChordSynonym("∆#11")}),
    ChordForm("maj7/13", {ChordSynonym("major7/13"), ChordSynonym("M7/13", true), ChordSynonym("∆13")}),
    ChordForm("dim maj7",
              {
                  ChordSynonym("diminished major 7"), ChordSynonym("dimM7", true), ChordSynonym("dim♮7"),
                  ChordSynonym("°M7")
              }),
    ChordForm("min/maj7",
              {
                  ChordSynonym("-maj7"), ChordSynonym("minor/maj7"), ChordSynonym("mM7", true), ChordSynonym("min∆"),
                  ChordSynonym("-∆"), ChordSynonym("mi/maj7"), ChordSynonym("minor/major7"),
                  ChordSynonym("minor major 7")
              }),
    ChordForm("aug maj7", {
        ChordSynonym("+Maj7"), ChordSynonym("augmented major 7"), ChordSynonym("+M7", true),
        ChordSynonym("augM7", true), ChordSynonym("aug∆"), ChordSynonym("+∆"),
        ChordSynonym("maj7#5"), ChordSynonym("major7#5"), ChordSynonym("+♮7"),
        ChordSynonym("∆#5")
    }),
    ChordForm("-b6", {ChordSynonym("minb6"), ChordSynonym("minor b6"), ChordSynonym("mb6", true)}),
    ChordForm("-13", {
        ChordSynonym("min13"), ChordSynonym("minor 13"), ChordSynonym("m13", true), ChordSynonym("-7/13"),
        ChordSynonym("min7/13"), ChordSynonym("minor7/13")
    })
};


bool Chord::operator<(const Chord &other) const {
  if (root == other.root) {
    return description < other.description;
  }
  return root < other.root;
}


bool Chord::operator==(const Chord &other) const {
  return root == other.root && description == other.description && slashRoot == other.slashRoot;
}


Chord::Chord(const std::string &input) : slashRoot(SlashRoot::none()) {

  // Don't set values if there's nothing to set
  if (input.empty()) {
    description = "";
    root = PitchClass::Atonal;
    slashRoot = SlashRoot::none();
    accidental = Accidental::Natural;
    return;
  }

  // store original name
  auto name = StringUtils::stripExtraSpaces(input);

  // determine whether the name is "sharps" or "flats"
  accidental = accidentalOf(name);

  // Root utility separates root from remaining text
  Root rooter = Root::of(name);

  // parse the root, and keep the remaining string
  root = rooter.pitchClass;

  // parse the description all together, before removing the slash root
  auto raw = StringUtils::stripExtraSpaces(rooter.remainingText);
  auto normalized = normalize(raw);

  // parse the slash root
  slashRoot = SlashRoot::of(normalized);

  // save the description without the slash root, normalizing again in case we missed one because of the slash
  // but if we did make a substitution the first time (raw==normalized) then do not normalize again (this is wrong)
  description = raw == normalized ? normalize(slashRoot.pre) : slashRoot.pre;
}


int Chord::delta(const Chord &target) const {
  return Step::delta(root, target.root);
}


std::string Chord::getName() const {
  std::stringstream ss;
  ss << stringOf(root, accidental);
  if (!description.empty()) {
    ss << " " << description;
  }
  ss << slashRoot.display(accidental);
  return ss.str();
}


std::string Chord::toString() const {
  return getName();
}


PitchClass Chord::slashRootPitchClass() {
  return slashRoot.pitchClass.value_or(root);
}


std::string Chord::normalize(const std::string &input) {
  auto it = std::find_if(forms.begin(), forms.end(), [&input](const ChordForm &form) {
    return form.matches(input);
  });

  if (it != forms.end()) {
    return it->description;
  } else {
    return input;
  }
}


Chord Chord::of(const std::string &name) {
  return Chord(name);
}


bool Chord::isNoChord() const {
  return root == PitchClass::Atonal;
}


bool Chord::isAcceptable(const Chord &other) const {
  return root == other.root && description == other.description;
}


bool Chord::has_value() const {
  return root != PitchClass::Atonal;
}
