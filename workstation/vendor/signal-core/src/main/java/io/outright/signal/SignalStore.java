package io.outright.signal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the client keeps on the device, and when it may (README §7.1).
 *
 * Two values:
 *
 * <pre>
 *     state   the person's answer, the ledger, and the small counters the
 *             client paces itself by, as JSON
 *     spool   summaries waiting to be sent: length-prefixed, at most 32 of
 *             them and 8 KB, the oldest dropped first
 * </pre>
 *
 * Storing on a device is what consent law is about, whatever is stored, so
 * the client writes nothing until the person has answered. The one thing
 * written on a "no" is the no itself, so the question is never asked again.
 * The Flutter package's {@code store.dart}.
 */
public interface SignalStore {
  /** Read what was kept, once, before anything else. Throws when there is no storage to be had. */
  void open() throws IOException;

  /** The stored state, or an empty one. */
  Map<String, Object> loadState();

  void saveState(Map<String, Object> state);

  /** The summaries waiting to be sent, oldest first. */
  List<byte[]> loadSpool();

  /** Write the spool, within its caps: the oldest are dropped first. Returns what was kept. */
  List<byte[]> saveSpool(List<byte[]> payloads);

  /** Take everything the client ever wrote off the device. */
  void erase();

  /**
   * The store as two files in a folder of the app's own: {@code state.json}
   * and {@code spool.bin}. The folder is made at the first write, never
   * before: a person who never answers finds nothing on their disk.
   */
  final class Folder implements SignalStore {
    private final Path folder;

    public Folder(Path folder) {
      this.folder = folder;
    }

    public Path folder() {
      return folder;
    }

    private Path state() {
      return folder.resolve("state.json");
    }

    private Path spool() {
      return folder.resolve("spool.bin");
    }

    @Override
    public void open() {
      // Nothing to open: reads are of what is there, and writes make the folder.
    }

    @Override
    public Map<String, Object> loadState() {
      try {
        if (!Files.isRegularFile(state())) return new LinkedHashMap<>();
        return Json.parseObject(Files.readString(state(), StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException e) {
        return new LinkedHashMap<>();
      }
    }

    @Override
    public void saveState(Map<String, Object> state) {
      write(state(), Json.write(state).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<byte[]> loadSpool() {
      try {
        if (!Files.isRegularFile(spool())) return new ArrayList<>();
        return SignalSpool.unpack(Files.readAllBytes(spool()));
      } catch (IOException | RuntimeException e) {
        return new ArrayList<>();
      }
    }

    @Override
    public List<byte[]> saveSpool(List<byte[]> payloads) {
      List<byte[]> kept = SignalSpool.capped(payloads);
      write(spool(), SignalSpool.pack(kept));
      return kept;
    }

    @Override
    public void erase() {
      try {
        Files.deleteIfExists(state());
        Files.deleteIfExists(spool());
        Files.deleteIfExists(folder);
      } catch (IOException | RuntimeException e) {
        // A folder with something else in it stays; the client's own files are gone.
      }
    }

    /** Written beside, then moved into place: a crash mid-write never leaves half a file. */
    private void write(Path file, byte[] bytes) {
      try {
        Files.createDirectories(folder);
        Path partial = folder.resolve(file.getFileName() + ".partial");
        Files.write(partial, bytes);
        try {
          Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicNotSupported) {
          Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException | RuntimeException e) {
        // Failure is silent: the app is never slowed or stopped by it.
      }
    }
  }

  /** A store that lasts as long as the process: for tests. */
  final class Memory implements SignalStore {
    private Map<String, Object> state;
    private List<byte[]> spool = new ArrayList<>();
    /** How many times anything was written: before an answer, it must stay 0. */
    public int writes = 0;

    @Override
    public void open() {
    }

    @Override
    public Map<String, Object> loadState() {
      return state == null ? new LinkedHashMap<>() : Json.parseObject(Json.write(state));
    }

    @Override
    public void saveState(Map<String, Object> state) {
      writes += 1;
      this.state = Json.parseObject(Json.write(state));
    }

    @Override
    public List<byte[]> loadSpool() {
      return new ArrayList<>(spool);
    }

    @Override
    public List<byte[]> saveSpool(List<byte[]> payloads) {
      writes += 1;
      spool = SignalSpool.capped(payloads);
      return new ArrayList<>(spool);
    }

    @Override
    public void erase() {
      state = null;
      spool = new ArrayList<>();
    }

    /** Whether nothing at all is kept. */
    public boolean isEmpty() {
      return state == null && spool.isEmpty();
    }
  }
}
