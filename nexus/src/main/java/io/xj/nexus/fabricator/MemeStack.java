// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Objects;
import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 Concretely exclude meme combinations in violation of the given axioms:
 - Anti-Memes
 - Numeric Memes
 - Unique Memes
 */
public class MemeStack {
  private static final String NOT_PREFIX = "!";
  private static final String UNIQUE_PREFIX = "$";
  private final Set<String> memes;

  /**
   Constructor from memes

   @param from from which to create stack
   */
  private MemeStack(Collection<String> from) {
    memes = from.stream().map(Text::toMeme).collect(Collectors.toSet());
  }

  /**
   Instantiate a new MemeIsometry of a group of source Memes,
   as expressed in a Result of jOOQ records.

   @param memes to compare of
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeStack from(Collection<String> memes) {
    return new MemeStack(memes);
  }

  /**
   FUTURE: could be more efficient by skipping tests of the same meme pair in reverse order

   @param targets memes to test
   @return true if the specified set of memes is allowed into this meme stack
   */
  public Boolean isAllowed(Collection<String> targets) {
    for (var source : memes)
      for (var target : targets)
        if (
          violatesAntiMeme(source, target)
            || NumericMeme.isViolatedBy(source, target)
            || violatesUniqueMeme(source, target)
        ) return false;

    return true;
  }

  /**
   Anti-Memes
   <p>
   Artist can add !MEME values into Programs #176474073

   @param a meme to test
   @param b meme to test
   @return true if this pairing would violate the anti-meme axiom
   */
  private boolean violatesAntiMeme(String a, String b) {
    return
      (NOT_PREFIX.equals(a.substring(0, 1)) && a.substring(1).equals(b))
        || (NOT_PREFIX.equals(b.substring(0, 1)) && b.substring(1).equals(a));
  }

  /**
   Unique Memes
   <p>
   Artist can add `$MEME` so only one is chosen #179078760

   @param a meme to test
   @param b meme to test
   @return true if this pairing would violate the unique meme axiom
   */
  private boolean violatesUniqueMeme(String a, String b) {
    return Objects.equal(a, b) && UNIQUE_PREFIX.equals(a.substring(0, 1));
  }

  /**
   Parse any meme to test if it's a numeric meme, and extract its features
   <p>
   Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive #180125852
   */
  static class NumericMeme {
    private static final Pattern rgx = Pattern.compile("^([0-9]+)(.+)$");
    final String body;
    final Integer prefix;
    final boolean isValid;

    public NumericMeme(String raw) {
      Matcher matcher = rgx.matcher(raw);

      if (!matcher.find()) {
        prefix = null;
        body = null;
        isValid = false;
        return;
      }

      String pfx = matcher.group(1);
      if (java.util.Objects.isNull(pfx) || pfx.length() == 0) {
        prefix = null;
        body = null;
        isValid = false;
        return;
      }
      prefix = Integer.valueOf(pfx);

      body = matcher.group(2);
      if (java.util.Objects.isNull(body) || body.length() == 0) {
        isValid = false;
        return;
      }

      isValid = true;
    }

    /**
     Numeric Memes
     <p>
     Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive #180125852

     @param a meme to test
     @param b meme to test
     @return true if this pairing would violate the numeric meme axiom
     */
    static boolean isViolatedBy(String a, String b) {
      var mA = new NumericMeme(a);
      var mB = new NumericMeme(b);
      return
        mA.isValid && mB.isValid
          && Objects.equal(mA.body, mB.body)
          && !Objects.equal(mA.prefix, mB.prefix);
    }
  }
}
