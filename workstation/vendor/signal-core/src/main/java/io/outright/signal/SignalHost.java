package io.outright.signal;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What the client knows of the computer and the build, read once at start.
 *
 * Every field is plain data, so a test makes one by hand; {@link #detect}
 * reads the real ones. Nothing here is an identifier: the device is a class
 * of four, never a model, and the language is the one the computer is set
 * to. The Flutter package's {@code host.dart}.
 */
public final class SignalHost {
  /** One of {@link Rules.Platform}. */
  public final int platform;
  public final int cores;
  /** Where the platform says how much memory there is; null elsewhere. */
  public final Long memoryBytes;
  /** A BCP 47 tag, such as {@code en-US}. */
  public final String locale;
  /** The channel this build is, as one name of the catalog's. Empty is the catalog's first. */
  public final String channel;
  /** A kill switch is on (README §5.4): the client is off before it does anything. */
  public final boolean killed;

  public SignalHost(int platform, int cores, Long memoryBytes, String locale, String channel, boolean killed) {
    this.platform = platform;
    this.cores = cores;
    this.memoryBytes = memoryBytes;
    this.locale = locale;
    this.channel = channel;
    this.killed = killed;
  }

  /** The four device classes from cores and memory; from cores alone where memory cannot be read. */
  public int deviceClass() {
    if (memoryBytes != null) return Rules.deviceClass(cores, memoryBytes);
    if (cores >= 12) return 3;
    if (cores >= 6) return 2;
    if (cores >= 4) return 1;
    return 0;
  }

  public SignalHost withKilled(boolean killed) {
    return new SignalHost(platform, cores, memoryBytes, locale, channel, killed);
  }

  /**
   * The real computer and build. {@code arguments} are the app's own, where
   * {@code --no-telemetry} is looked for; {@code channel} names where this
   * build was got, as the app knows it.
   */
  public static SignalHost detect(String[] arguments, String channel) {
    Map<String, String> environment = System.getenv();
    return new SignalHost(
      platform(),
      Runtime.getRuntime().availableProcessors(),
      memory(),
      Locale.getDefault().toLanguageTag(),
      channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT),
      killSwitch(arguments, environment)
    );
  }

  static int platform() {
    String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    if (os.contains("win")) return Rules.Platform.WINDOWS;
    if (os.contains("mac") || os.contains("darwin")) return Rules.Platform.MACOS;
    if (os.contains("nux") || os.contains("nix")) return Rules.Platform.LINUX;
    return Rules.Platform.UNKNOWN;
  }

  /** The total memory, where the JDK's management module is in the runtime; null where it is not. */
  static Long memory() {
    try {
      if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) {
        long total = os.getTotalMemorySize();
        return total > 0 ? total : null;
      }
    } catch (Throwable e) {
      // A trimmed runtime without jdk.management: the class is from cores alone.
    }
    return null;
  }

  /**
   * The switches that turn the client off before it has done anything
   * (README §5.4): a flag, an environment variable or system property, and a
   * {@code signal-disable} file in the machine's or the person's own configuration.
   */
  public static boolean killSwitch(String[] arguments, Map<String, String> environment) {
    if (arguments != null) {
      for (String argument : arguments) if ("--no-telemetry".equals(argument)) return true;
    }
    if ("1".equals(environment.get("SIGNAL_DISABLE")) || "1".equals(System.getProperty("signal.disable"))) return true;
    List<String> config = new ArrayList<>();
    if (environment.get("APPDATA") != null) config.add(environment.get("APPDATA"));
    if (environment.get("XDG_CONFIG_HOME") != null) config.add(environment.get("XDG_CONFIG_HOME"));
    if (environment.get("HOME") != null) {
      config.add(environment.get("HOME") + "/.config");
      config.add(environment.get("HOME") + "/Library/Application Support");
    }
    config.add("/etc");
    for (String dir : config) {
      try {
        if (Files.exists(Path.of(dir, "signal-disable"))) return true;
      } catch (RuntimeException e) {
        // A folder the app may not look in: nothing there.
      }
    }
    return false;
  }
}
