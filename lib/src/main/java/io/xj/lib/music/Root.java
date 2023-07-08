// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Root can be the root of a Chord, Key or Scale.
 */
public class Root {
  static final Pattern rgxNote = Pattern.compile("^([ABCDEFG]).*");
  static final Pattern rgxNoteModified = Pattern.compile("^([ABCDEFG][♯#♭b]).*");
  PitchClass pitchClass;
  String remainingText;

  /**
   Parse root and remaining string, using regular expressions

   @param name to parse root and remaining of
   */
  Root(String name) {
    // as a default, the whole thing is remaining text, and pitch class is None
    this.pitchClass = PitchClass.None;
    this.remainingText = name;

    evaluate(rgxNote, name);
    evaluate(rgxNoteModified, name);
  }

  /**
   Instantiate a Root by name
   <p>
   XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338

   @param name of root
   @return root
   */
  public static Root of(String name) {
    return new Root(name);
  }

  /**
   First group matching pattern in text, else null@param pattern to in@param text              to search
   */
  void evaluate(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find())
      return;

    String match = matcher.group(1);
    if (Objects.isNull(match) || match.length() == 0)
      return;

    this.pitchClass = PitchClass.of(match);
    this.remainingText = text.substring(match.length()).trim();
  }

  /**
   Get pitch class of root

   @return root pitch class
   */
  public PitchClass getPitchClass() {
    return pitchClass;
  }

  /**
   Remaining text after root has been extracted

   @return remaining text
   */
  public String getRemainingText() {
    return remainingText;
  }

}
