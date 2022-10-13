// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import io.xj.lib.util.Text;

import java.util.regex.Pattern;

/**
 Root can be the root of a Chord, Key or Scale.
 */
public class SlashRoot {
  private static final Pattern rgxSlashNote = Pattern.compile("/([ABCDEFG])$");
  private static final Pattern rgxSlashNoteModified = Pattern.compile("/([ABCDEFG][♯#♭b])$");
  private static final Pattern rgxSlashPre = Pattern.compile("^([^/]+)/");
  private PitchClass pitchClass;

  /**
   Parse slash root string, using regular expressions

   @param name to parse slash root
   */
  private SlashRoot(String name) {
    // as a default, the pitch class is None
    this.pitchClass = PitchClass.None;

    Text.match(rgxSlashNote, name).ifPresent(pc -> this.pitchClass = PitchClass.of(pc));
    Text.match(rgxSlashNoteModified, name).ifPresent(pc -> this.pitchClass = PitchClass.of(pc));
  }

  /**
   Instantiate a Root by name
   <p>
   https://www.pivotaltracker.com/story/show/176728338 XJ understands the root of a slash chord

   @param name of root
   @return root
   */
  public static SlashRoot of(String name) {
    return new SlashRoot(name);
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

  /**
   Returns the pre-slash content, or whole string if no slash is present

   @param name to search for pre-slash content
   */
  public static String pre(String name) {
    return Text.match(rgxSlashPre, name).orElse(name);
  }
}
