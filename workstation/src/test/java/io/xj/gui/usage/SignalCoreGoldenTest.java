// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.outright.signal.Json;
import io.outright.signal.SignalCatalog;
import io.outright.signal.SignalCore;
import io.outright.signal.Wire;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 The vendored Signal core (vendor/signal-core) is the one Signal holds to its golden fixtures: played through it,
 every fixture gives the reference implementation's bytes and ledger, byte for byte. The fixtures are vendored
 beside it, pinned by golden.sha256.
 */
class SignalCoreGoldenTest {
  private double clock;

  static Path golden() {
    return Path.of(System.getProperty("signal.golden", "vendor/signal-core/fixtures/golden.json"));
  }

  @Test
  void theFixturesAreTheOnesTheirDigestPins() throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(golden()));
    String pinned = Files.readString(golden().resolveSibling("golden.sha256")).trim().split("\\s+")[0];
    assertEquals(pinned, Wire.toHex(digest));
  }

  @Test
  void everyFixtureGivesTheReferenceBytesAndLedger() throws Exception {
    Map<String, Object> fixtures = Json.parseObject(Files.readString(golden(), StandardCharsets.UTF_8));
    Map<String, Object> catalogs = Json.asMap(fixtures.get("catalogs"));
    int payloads = 0;
    for (Object raw : Json.asList(fixtures.get("fixtures"))) {
      Map<String, Object> fixture = Json.asMap(raw);
      String name = (String) fixture.get("name");
      SignalCatalog catalog = SignalCatalog.fromCompiled(Json.asMap(catalogs.get((String) fixture.get("catalog"))));
      clock = 0;
      SignalCore core = new SignalCore(catalog, Json.asMap(fixture.get("ledger")), () -> clock);
      int index = 0;
      for (Object rawSession : Json.asList(fixture.get("sessions"))) {
        Map<String, Object> session = Json.asMap(rawSession);
        index += 1;
        double start = session.get("start") instanceof Number s ? s.doubleValue() : index * 100000.0;
        clock = start;
        List<Object> dims = Json.asList(session.get("dims"));
        core.begin(n(session.get("day")), n(session.get("month")), new int[]{n(dims.get(0)), n(dims.get(1)), n(dims.get(2)), n(dims.get(3))});
        play(core, Json.asList(session.get("ops")), start);
        if ("none".equals(session.get("end"))) {
          core.abandon();
          continue;
        }
        String nonce = (String) session.get("nonce");
        String previewed = Wire.toHex(core.preview(nonce).bytes);
        String sent = Wire.toHex(core.end(nonce).bytes);
        payloads += 1;
        assertEquals(sent, previewed, name + " session " + index + ": the preview is what is sent");
        assertEquals(session.get("hex"), sent, name + " session " + index);
      }
      assertEquals(Json.asMap(fixture.get("ledger_after")), core.ledger.toJson(), name + ": the ledger left");
    }
    assertTrue(payloads >= 25, payloads + " payloads played");
  }

  private void play(SignalCore core, List<Object> ops, double start) {
    for (Object raw : ops) {
      List<Object> op = Json.asList(raw);
      switch ((String) op.get(0)) {
        case "t" -> clock = start + d(op.get(1));
        case "count" -> core.count(n(op.get(1)), op.size() > 2 ? n(op.get(2)) : 1, op.size() > 3 ? n(op.get(3)) : -1);
        case "observe" -> core.observe(n(op.get(1)), d(op.get(2)), op.size() > 3 ? n(op.get(3)) : -1);
        case "milestone" -> core.milestone(n(op.get(1)), n(op.get(2)));
        case "node" -> core.node(n(op.get(1)));
        case "trait" -> core.setTrait(n(op.get(1)));
        case "outcome" -> core.outcome(n(op.get(1)));
        case "input" -> core.input(n(op.get(1)));
        case "fps" -> core.fps(d(op.get(1)));
        case "pause" -> core.pause();
        case "resume" -> core.resume();
        case "repeat" -> {
          for (int i = 0; i < n(op.get(1)); i++) play(core, Json.asList(op.get(2)), start);
        }
        default -> throw new IllegalStateException("no such op: " + op.get(0));
      }
    }
  }

  private static int n(Object value) {
    return Json.asInt(value, 0);
  }

  private static double d(Object value) {
    return ((Number) value).doubleValue();
  }
}
