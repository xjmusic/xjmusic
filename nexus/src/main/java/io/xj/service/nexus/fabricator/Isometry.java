// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.language.Metaphone;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 Determine the isometry between a source and target group of Memes
 */
public class Isometry {
  private static final String CONSTELLATION_DELIMITER = "_";
  private final List<String> sources;

  /**
   Default constructor
   */
  public Isometry() {
    sources = Lists.newArrayList();
  }

  /**
   Instantiate a new Isometry of a group of source strings which will be stemmed

   @param sources to compare of
   @return Isometry ready for comparison to targets
   */
  public static Isometry ofStemmed(Iterable<String> sources) {
    Isometry result = new Isometry();
    sources.forEach(result::addStem);
    return result;
  }

  /**
   Instantiate a new Isometry of a group of source strings which will be phentically reduced

   @param sources to compare of
   @return Isometry ready for comparison to targets
   */
  public static Isometry ofPhonetic(Iterable<String> sources) {
    Isometry result = new Isometry();
    sources.forEach(result::addPhonetic);
    return result;
  }

  /**
   Snowball stem of a particular word
   FUTURE implement other non-English languages for stemming and string isometry.

   @param raw text to get stem of
   @return stem
   */
  protected static String stem(String raw) {
    SnowballStemmer stemmer = new englishStemmer();
    stemmer.setCurrent(raw.toLowerCase(Locale.ENGLISH).trim());
    stemmer.stem();
    return stemmer.getCurrent();
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
   Add the stem of a String for isometry comparison

   @param input to add
   */
  public void addStem(String input) {
    sources.add(stem(input));
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

   @return source stems
   */
  List<String> getSources() {
    return Collections.unmodifiableList(sources);
  }

  /**
   Compute a unique constellation for any set of Memes,
   for the purpose of identifying unique constellations.
   <p>
   [#161736024] for each unique sequence-pattern-meme constellation within the main sequence

   @return unique constellation for this set of strings.
   */
  public String getConstellation() {
    Map<String, Boolean> uniqueStems = Maps.newHashMap();
    sources.forEach(stem -> uniqueStems.put(stem, true));
    Set<String> pieces = Sets.newTreeSet(uniqueStems.keySet());
    return String.join(CONSTELLATION_DELIMITER, pieces);
  }
}
