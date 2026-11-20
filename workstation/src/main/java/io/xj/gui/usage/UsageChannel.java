// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

/**
 Where this build of the workstation was got: one of the channels in .signal/stream.yaml.
 <ul>
 <li>{@code download}: installed by the installer, from GitHub Releases or xjmusic.com. Those are the same file,
 so a build cannot tell which. An installed workstation is launched by jpackage's launcher, which sets
 {@code jpackage.app-path}.</li>
 <li>{@code source}: run from a checkout, by {@code ./gradlew bootRun} or the jar, with no installer.</li>
 </ul>
 */
public final class UsageChannel {
  public static final String DOWNLOAD = "download";
  public static final String SOURCE = "source";

  private UsageChannel() {
  }

  /**
   @return this build's channel
   */
  public static String resolve() {
    return resolve(System.getProperty("jpackage.app-path"));
  }

  static String resolve(String jpackageAppPath) {
    return jpackageAppPath != null && !jpackageAppPath.isBlank() ? DOWNLOAD : SOURCE;
  }
}
