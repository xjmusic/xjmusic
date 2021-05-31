// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Root can be the root of a Chord, Key or Scale.
 */
public class SlashRoot {
  private static final Pattern rgxSlashNote = Pattern.compile("/([ABCDEFG])$");
  private static final Pattern rgxSlashNoteModified = Pattern.compile("/([ABCDEFG][♯#♭b])$");
  private PitchClass pitchClass;

  /**
   Parse root and remaining string, using regular expressions

   @param name to parse root and remaining of
   */
  private SlashRoot(String name) {
    // as a default, the whole thing is remaining text, and pitch class is None
    this.pitchClass = PitchClass.None;

    evaluate(rgxSlashNote, name);
    evaluate(rgxSlashNoteModified, name);
  }

  /**
   Instantiate a Root by name
   <p>
   [#176728338] XJ understands the root of a slash chord

   @param name of root
   @return root
   */
  public static SlashRoot of(String name) {
    return new SlashRoot(name);
  }

  /**
   First group matching pattern in text, else null@param pattern to in

   @param text to search
   */
  private void evaluate(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find())
      return;

    String match = matcher.group(1);
    if (Objects.isNull(match) || match.length() == 0)
      return;

    this.pitchClass = PitchClass.of(match);
  }

  /**
   Get pitch class of root

   @return root pitch class
   */
  public PitchClass getPitchClass() {
    return pitchClass;
  }

  public PitchClass orDefault(PitchClass dpc) {
    if (pitchClass.equals(PitchClass.None)) return dpc;
    return pitchClass;
  }
}
