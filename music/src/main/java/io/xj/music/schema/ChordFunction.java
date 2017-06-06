// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music.schema;

/**
 Chord functions (not yet implemented)
 <p>
 Chords have different Functions, such as Diatonic, Altered or Other.
 */
public enum ChordFunction {

  GenericFunction,

  // Diatonic
  TonicDiatonic,
  DominantDiatonic,
  SubdominantDiatonic,
  SupertonDiatonicicDiatonic,
  MediantDiatonic,
  SubmediantDiatonic,
  LeadingDiatonic,
  SubtonicDiatonic,

  // Altered
  ApproachAltered,
  BorrowedAltered,
  ChromaticMediantAltered,
  NeapolitanAltered,
  PassingAltered,
  SecondaryAltered,
  SecondaryDominantAltered,
  SecondaryLeadingToneAltered,
  SecondarySupertonicAltered,

  // Other
  CommonOther,
  ContrastOther,
  PrimaryTriadOther,
  SubsidiaryOther;

}
