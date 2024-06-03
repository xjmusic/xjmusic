// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.util;

import io.xj.model.util.StringUtils;
import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface FormatUtils {
  /**
   Format the suffix of a human-readable string with a number and fraction

   @param value number to format
   @return human-readable string like "½", "¼", or "⅔".
   */
  static String formatFractionalSuffix(Double value) {
    if (value <= 0 || value >= 1) {
      return "";
    }

    return switch ((int) (value * 100)) {
      case 10 -> "⅒";
      case 11 -> "⅑";
      case 12 -> "⅛";
      case 14 -> "⅐";
      case 16 -> "⅙";
      case 20 -> "⅕";
      case 25 -> "¼";
      case 33 -> "⅓";
      case 37 -> "⅜";
      case 40 -> "⅖";
      case 50 -> "½";
      case 60 -> "⅗";
      case 62 -> "⅝";
      case 66 -> "⅔";
      case 75 -> "¾";
      case 80 -> "⅘";
      case 83 -> "⅚";
      case 87 -> "⅞";
      default -> formatMinDecimal(value).substring(1);
    };
  }

  /**
   Format the decimal suffix portion of a human-readable string

   @param value number to format
   @return human-readable string like ".5", ".25", or ".333".
   */
  static String formatDecimalSuffix(Double value) {
    if (value <= 0 || value >= 1) {
      return "";
    }

    return formatMinDecimal(value).substring(1);
  }


  /**
   Format a number to a human-readable string

   @param number number to format
   @return human-readable string like "5", "4.5", or "1.27".
   */
  static String formatMinDecimal(@Nullable Double number) {
    if (Objects.isNull(number)) {
      return "N/A";
    }
    if (Math.floor(number) == number) {
      return String.format("%.0f", number);  // No decimal places if it's an integer
    } else {
      String str = Float.toString((float) (Math.round(number * 1000.0) / 1000.0));
      int decimalPlaces = str.length() - str.indexOf('.') - 1;

      // Remove trailing zeros
      for (int i = 0; i < decimalPlaces; i++) {
        if (str.endsWith("0")) {
          str = str.substring(0, str.length() - 1);
        } else {
          break;
        }
      }

      // Remove trailing decimal point if any
      if (str.endsWith(".")) {
        str = str.substring(0, str.length() - 1);
      }

      return str;
    }
  }

  /**
   Format a time in microseconds to a human-readable string

   @param microseconds time in microseconds
   @return human-readable string like "5s", "4m7s", or "1h27m4s".
   */
  static String formatTimeFromMicros(@Nullable Long microseconds) {
    if (Objects.isNull(microseconds)) {
      return "N/A";
    }

    // Round up to the nearest second
    long totalSeconds = (microseconds + 999999) / 1000000;

    // Get fractional seconds
    float fractionalSeconds = (float) ((microseconds % 1000000) / 1000000.0);

    // Calculate hours, minutes, and remaining seconds
    long hours = totalSeconds / 3600;
    long remainingSeconds = totalSeconds % 3600;
    long minutes = remainingSeconds / 60;
    long seconds = remainingSeconds % 60;

    // Build the readable string
    StringBuilder readableTime = new StringBuilder();
    if (hours > 0) {
      readableTime.append(hours).append("h");
    }
    if (minutes > 0) {
      readableTime.append(minutes).append("m");
    }
    if (seconds > 0 || (hours == 0 && minutes == 0)) {
      if (hours == 0 && minutes == 0) {
        readableTime.append(String.format("%d.%d", seconds, (int) Math.floor(fractionalSeconds * 10))).append("s");
      } else {
        readableTime.append(seconds).append("s");
      }
    }

    return readableTime.toString();
  }

  /**
   Create a sequence, increment a numerical suffix to make each sequence unique, e.g. "New Sequence 2" then "New Sequence 3"

   @param existing set of existing names
   @param name     to iterate if existing
   */
  static String iterateNumericalSuffixFromExisting(Set<String> existing, String name) {
    int num = 2;
    if (existing.stream().map(String::trim).noneMatch(s -> s.startsWith(name))) {
      return name;
    }
    for (String existingName : existing) {
      if (existingName.startsWith(name)) {
        try {
          int existingNum = Integer.parseInt(existingName.substring(name.length()).trim());
          if (existingNum >= num) {
            num = existingNum + 1;
          }
        } catch (NumberFormatException e) {
          // ignore
        }
      }
    }
    return String.format("%s %d", name, num);
  }


  /**
   Describe counts of items based on a map of item names to counts, sorted alphabetically, comma-separated, pluralized
   e.g. Map.of("peach", 1, "apple", 3, "banana", 2) -> "3 apples, 2 bananas, and 1 peach"

   @param items for which to describe counts
   @return description of the item counts
   */
  static String describeCounts(Map<String, Integer> items) {
    return StringUtils.toProperCsvAnd(items.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(entry -> describeCount(entry.getKey(), entry.getValue()))
      .filter(Objects::nonNull)
      .toList());
  }

  /**
   Describe a count of something, the name pluralized if necessary
   Return null if count is zero

   @param name  of the thing
   @param count of the thing
   @return description of the count
   */
  static @Nullable String describeCount(String name, long count) {
    if (count == 0) return null;
    return String.format("%d %s", count, count > 1 ? StringUtils.toPlural(name) : name);
  }
}
