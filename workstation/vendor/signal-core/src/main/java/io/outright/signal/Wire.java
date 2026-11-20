package io.outright.signal;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Wire protocol v1 (README §8): a summary as bytes.
 *
 * <pre>
 *     0xA1 | flags | 8B catalog digest | 8B nonce
 *     varint local day (since 2026-01-01) | varint install age
 *     5B dims: channel, platform, device, lang, traits
 *     records: varint metric id + a body its metric's type decides
 * </pre>
 *
 * The encoding is canonical: records ascend by metric id, entries ascend by
 * their keys, and nothing that is zero is written. The aggregator hands over
 * records already in that order. The Dart package's {@code encoder.dart}.
 */
public final class Wire {
  private Wire() {
  }

  private static final int MAGIC = 0xA1;

  /** Every v1 flag but deflate: this encoder never compresses. */
  private static final int FLAG_MASK = 0x5F;

  /** Header flags, one bit each (README §8.2). */
  public static final class Flag {
    private Flag() {
    }

    public static final int FIRST_TODAY = 1;
    public static final int FIRST_WEEK = 2;
    public static final int FIRST_MONTH = 4;
    public static final int NEW_INSTALL = 8;
  }

  /**
   * One record of a summary: a metric, and either a single number (a
   * counter's value, a milestone's newly reached bits, a node) or entries,
   * each a list of keys ending in a count.
   */
  public static final class Record {
    public final int id;
    public final int type;
    public final long value;
    /** Null for a single number. */
    public final long[][] entries;

    private Record(int id, int type, long value, long[][] entries) {
      this.id = id;
      this.type = type;
      this.value = value;
      this.entries = entries;
    }

    public static Record value(int id, int type, long value) {
      return new Record(id, type, value, null);
    }

    public static Record entries(int id, int type, long[][] entries) {
      return new Record(id, type, 0, entries);
    }
  }

  /** What one payload says, with the names taken out. */
  public static final class Summary {
    public final int flags;
    public final String digest;
    public final String nonce;
    public final int day;
    public final int age;
    public final int channel;
    public final int platform;
    public final int device;
    public final int lang;
    public final int traits;
    /** In ascending metric id. */
    public final List<Record> records;

    public Summary(int flags, String digest, String nonce, int day, int age, int channel, int platform, int device, int lang, int traits, List<Record> records) {
      this.flags = flags;
      this.digest = digest;
      this.nonce = nonce;
      this.day = day;
      this.age = age;
      this.channel = channel;
      this.platform = platform;
      this.device = device;
      this.lang = lang;
      this.traits = traits;
      this.records = records;
    }
  }

  /** Append an unsigned LEB128 number: seven bits a byte, low bits first. */
  public static void writeVarint(ByteArrayOutputStream out, long value) {
    long rest = value;
    while (rest >= 0x80) {
      out.write((int) ((rest & 0x7F) | 0x80));
      rest >>>= 7;
    }
    out.write((int) rest);
  }

  /** Hex digits as bytes. */
  public static byte[] hexToBytes(String hex) {
    byte[] out = new byte[hex.length() / 2];
    for (int i = 0; i < out.length; i++) {
      out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
    }
    return out;
  }

  public static String toHex(byte[] bytes) {
    StringBuilder out = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      int v = b & 0xFF;
      out.append(Character.forDigit(v >> 4, 16)).append(Character.forDigit(v & 0xF, 16));
    }
    return out.toString();
  }

  public static byte[] encode(Summary summary) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(64);
    out.write(MAGIC);
    out.write(summary.flags & FLAG_MASK);
    out.writeBytes(hexToBytes(summary.digest));
    out.writeBytes(hexToBytes(summary.nonce));
    writeVarint(out, summary.day);
    writeVarint(out, summary.age);
    out.write(summary.channel & 0xFF);
    out.write(summary.platform & 0xFF);
    out.write(summary.device & 0xFF);
    out.write(summary.lang & 0xFF);
    out.write(summary.traits & 0xFF);
    for (Record record : summary.records) {
      writeVarint(out, record.id);
      if (record.entries == null) {
        writeVarint(out, record.value);
      } else {
        writeVarint(out, record.entries.length);
        for (long[] entry : record.entries) {
          for (long number : entry) writeVarint(out, number);
        }
      }
    }
    return out.toByteArray();
  }

  /** Whether a record of this type is a single number rather than entries. */
  public static boolean isSingleValue(int type) {
    return type == SignalCatalog.Type.COUNTER || type == SignalCatalog.Type.MILESTONE
      || type == SignalCatalog.Type.EXIT_NODE || type == SignalCatalog.Type.RETURN_NODE;
  }
}
