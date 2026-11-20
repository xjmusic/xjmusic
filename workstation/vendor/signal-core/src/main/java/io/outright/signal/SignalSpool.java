package io.outright.signal;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Where summaries wait to be sent (README §7.1): at most 32 of them and
 * 8 KB, the oldest dropped first. This is the shape of one, and one in
 * memory; {@link SignalClient} keeps one in a file. The Dart package's
 * {@code spool.dart}.
 */
public interface SignalSpool {
  int MAX_PAYLOADS = 32;
  int MAX_BYTES = 8192;

  /** The summaries waiting, oldest first. */
  List<byte[]> load();

  /** Keep these, within the caps. Returns what was kept. */
  List<byte[]> save(List<byte[]> payloads);

  /** What the caps keep of a list: the newest that fit. */
  static List<byte[]> capped(List<byte[]> payloads) {
    List<byte[]> kept = new ArrayList<>(payloads);
    while (kept.size() > MAX_PAYLOADS || bytes(kept) > MAX_BYTES) kept.remove(0);
    return kept;
  }

  private static int bytes(List<byte[]> payloads) {
    int sum = 0;
    for (byte[] payload : payloads) sum += payload.length + 2;
    return sum;
  }

  /** A spool as one length-prefixed blob, as the Godot addon writes its file. */
  static byte[] pack(List<byte[]> payloads) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (byte[] payload : payloads) {
      out.write(payload.length & 0xFF);
      out.write((payload.length >> 8) & 0xFF);
      out.writeBytes(payload);
    }
    return out.toByteArray();
  }

  static List<byte[]> unpack(byte[] blob) {
    List<byte[]> out = new ArrayList<>();
    int at = 0;
    while (at + 2 <= blob.length) {
      int size = (blob[at] & 0xFF) | ((blob[at + 1] & 0xFF) << 8);
      if (size <= 0 || at + 2 + size > blob.length) break;
      out.add(Arrays.copyOfRange(blob, at + 2, at + 2 + size));
      at += 2 + size;
    }
    return out;
  }

  /**
   * A spool that lasts as long as the process: for tests, and for a session
   * before the person has answered, when nothing may be written.
   */
  final class Memory implements SignalSpool {
    private List<byte[]> payloads = new ArrayList<>();

    @Override
    public List<byte[]> load() {
      return new ArrayList<>(payloads);
    }

    @Override
    public List<byte[]> save(List<byte[]> payloads) {
      this.payloads = capped(payloads);
      return load();
    }
  }
}
