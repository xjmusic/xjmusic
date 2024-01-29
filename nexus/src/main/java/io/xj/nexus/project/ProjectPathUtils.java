package io.xj.nexus.project;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectPathUtils {
  private static final String escapedFileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
  private static final Pattern prefixNameExtensionRgx = Pattern.compile("(.*" + escapedFileSeparator + ")([^" + escapedFileSeparator + "]+)" + "\\.([^.]+)$");

  public static Matcher matchPrefixNameExtension(String fileName) {
    return prefixNameExtensionRgx.matcher(fileName);
  }

  public static String getExtension(String fromPath) {
    Matcher matcher = matchPrefixNameExtension(fromPath);
    if (matcher.matches()) {
      return matcher.group(3);
    }
    return "";
  }
}
