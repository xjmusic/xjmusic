// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import com.google.common.collect.Sets;
import org.apache.commons.codec.language.Metaphone;

import java.util.Collections;
import java.util.Set;

/**
 Determine the isometry between a source and target group of Memes
 */
public class Isometry {
  protected final Set<String> sources;

  /**
   Default constructor
   */
  public Isometry() {
    sources = Sets.newHashSet();
  }

  /**
   Instantiate a new Isometry of a group of source strings

   @param sources to compare of
   @return Isometry ready for comparison to targets
   */
  public static Isometry of(Iterable<String> sources) {
    Isometry result = new Isometry();
    sources.forEach(result::add);
    return result;
  }

  /**
   Instantiate a new Isometry of a group of source strings which will be phonetically reduced

   @param sources to compare of
   @return Isometry ready for comparison to targets
   */
  public static Isometry ofPhonetic(Iterable<String> sources) {
    Isometry result = new Isometry();
    sources.forEach(result::addPhonetic);
    return result;
  }

  /**
   Double metaphone phonetic of a particular word

   @param raw text to get phonetic of
   @return phonetic
   */
  protected static String phonetic(String raw) {
    Metaphone metaphone = new Metaphone();
    return metaphone.metaphone(raw);
  }

  /**
   Add a String for isometry comparison

   @param input to add
   */
  public void add(String input) {
    sources.add(input);
  }

  /**
   Add the phonetic reduction of a String for isometry comparison

   @param input to add
   */
  public void addPhonetic(String input) {
    sources.add(phonetic(input));
  }

  /**
   Get the source Memes

   @return source memes
   */
  public Set<String> getSources() {
    return Collections.unmodifiableSet(sources);
  }

  /**
   Compute normalized string representation of an unordered set of memes
   for the purpose of identifying unique constellations.
   <p>
   for each unique sequence-pattern-meme constellation within the main sequence https://www.pivotaltracker.com/story/show/161736024

   @return unique constellation for this set of strings.
   */
  public String getConstellation() {
    return MemeConstellation.fromNames(sources);
  }

}
