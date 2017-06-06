// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Octave models a musical octave
 <p>
 A perfect octave is the interval between one musical pitch and another with half or double its frequency.
 */
public interface Octave {

  /**
   Regular expression to in the octave of a note
   */
  Pattern rgxOctave = Pattern.compile("(-*[0-9]+)$");

  /**
   Octave value from note text

   @param text note
   @return octave
   */
  static Integer of(String text) {
    Matcher matcher = rgxOctave.matcher(text.trim());
    if (matcher.find())
      return Integer.valueOf(matcher.group(1));
    else
      return 0;
  }

}
