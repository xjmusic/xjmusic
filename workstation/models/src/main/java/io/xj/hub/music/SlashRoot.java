// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.music;

import io.xj.hub.util.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 Root can be the root of a Chord, Key or Scale.
 */
public class SlashRoot {
  static final Pattern rgxSlashPost = Pattern.compile("[^/]*/([A-G♯#♭b]+)$");
  static final Pattern rgxSlashNote = Pattern.compile("/([A-G])$");
  static final Pattern rgxSlashNoteModified = Pattern.compile("/([A-G][♯#♭b])$");
  static final Pattern rgxSlashPre = Pattern.compile("^([^/]*)/");
  static final String SLASH = "/";
  static final String EMPTY = "";
  final PitchClass pitchClass;
  final String pre;
  final String post;

  /**
   Parse slash root string, using regular expressions

   @param name to parse slash root
   */
  SlashRoot(String name) {
    post = StringUtils.match(rgxSlashPost, name).orElse(EMPTY);
    pre = StringUtils.isNullOrEmpty(post) ? name : StringUtils.match(rgxSlashPre, name).orElse(EMPTY);
    pitchClass =
      StringUtils.match(rgxSlashNoteModified, name)
        .map(PitchClass::of)
        .orElse(StringUtils.match(rgxSlashNote, name)
          .map(PitchClass::of)
          .orElse(PitchClass.None));
  }

  /**
   Instantiate a Root by name
   <p>
   XJ understands the root of a slash chord https://github.com/xjmusic/workstation/issues/220

   @param name of root
   @return root
   */
  public static SlashRoot of(String name) {
    return new SlashRoot(name);
  }

  public static SlashRoot none() {
    return new SlashRoot("");
  }

  /**
   Returns the pre-slash content, or whole string if no slash is present

   @param description to search for pre-slash content
   */
  public static String pre(String description) {
    if (StringUtils.isNullOrEmpty(description)) return "";
    if (Objects.equals(SLASH, description.substring(0, 1))) return "";
    return StringUtils.match(rgxSlashPre, description).orElse(description);
  }

  /**
   Return true if a slash is present in the given chord name

   @param name to test for slash
   @return true if slash is found
   */
  public static boolean isPresent(String name) {
    return rgxSlashPost.matcher(name).find();
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
   @return true if any slash info is present
   */
  public boolean isPresent() {
    return !StringUtils.isNullOrEmpty(post);
  }

  /**
   @return entire text after the first slash
   */
  public String getPost() {
    return post;
  }

  /**
   @return entire text before the first slash
   */
  public String getPre() {
    return pre;
  }

  /**
   Display the slash root, with an adjustment symbol if it's a clean note, otherwise as-is

   @param withOptional adjustment symbol
   @return displayed slash root
   */
  public String display(Accidental withOptional) {
    if (PitchClass.None != pitchClass)
      return String.format("/%s", pitchClass.toString(withOptional));
    else if (!StringUtils.isNullOrEmpty(post))
      return String.format("/%s", post);
    else return EMPTY;
  }

  /**
   Whether this slash root is the same as another

   @param o to compare
   @return true if same
   */
  public boolean isSame(SlashRoot o) {
    return (Objects.nonNull(post) && Objects.nonNull(o.post) && post.equals(o.post))
      || pitchClass.equals(o.pitchClass);
  }
}
