package io.xj.nexus.project;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectPathUtils {
  private static final String escapedFileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
  private static final Pattern projectPathAndFilenameRgx = Pattern.compile("(.*" + escapedFileSeparator + ")([^" + escapedFileSeparator + "]+)" + "\\.([^.]+)$");

  public static Matcher matchPrefixNameExtension(String fileName) {
    return projectPathAndFilenameRgx.matcher(fileName);
  }
}
