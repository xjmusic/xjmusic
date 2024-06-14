// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.meme;

import java.util.Collections;
import java.util.HashSet;
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
    sources = new HashSet<>();
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
   Add a String for isometry comparison

   @param input to add
   */
  public void add(String input) {
    sources.add(input);
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
   for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/xjmusic/issues/208

   @return unique constellation for this set of strings.
   */
  public String getConstellation() {
    return MemeConstellation.fromNames(sources);
  }

}
