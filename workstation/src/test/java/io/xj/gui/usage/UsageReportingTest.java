// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.outright.signal.SignalClient;
import io.outright.signal.SignalHost;
import io.outright.signal.SignalStore;
import io.outright.signal.SignalTransport;
import io.xj.gui.types.Route;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 Usage reporting holds to what PRIVACY.md promises: the catalog is the one in .signal, the question is opt-in,
 nothing leaves or is written before a yes, and the hard off switch is off.
 */
class UsageReportingTest {

  @Test
  void theGeneratedCatalogIsTheWorkstationsStreamAndOptIn() throws IOException {
    var catalog = XjSignalCatalog.CATALOG;
    assertEquals("xjmusic-workstation", catalog.stream);
    assertEquals(16, catalog.digest.length());
    assertEquals("opt_in", catalog.consent.get("mode"), "opt-in until counsel signs off on anything else (.signal/consent.yaml)");
    assertEquals("https://xjmusic.com/privacy#workstation", catalog.consent.get("policy_url"));
    assertEquals(Boolean.FALSE, catalog.pulse.get("enabled"));
    assertEquals(Boolean.FALSE, catalog.review.get("enabled"));
    assertEquals(Boolean.FALSE, catalog.feedback.get("enabled"), "nothing typed is ever sent");
    assertEquals(java.util.List.of("download", "source"), catalog.channels);

    // The key that ships is this stream's
    Properties properties = new Properties();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
      properties.load(in);
    }
    assertTrue(properties.getProperty("signal.key").startsWith(catalog.stream + "."));
    assertEquals("https://in.signal.outright.io", properties.getProperty("signal.endpoint"));
  }

  @Test
  void everyViewIsANodeOfItsOwn() {
    Set<Integer> seen = new HashSet<>();
    for (Route route : Route.values()) {
      int node = UsageObserver.nodeOf(route);
      assertTrue(seen.add(node), route + " shares a node");
      String snake = route.name().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
      assertEquals(snake, XjSignalCatalog.CATALOG.nodeNames.get(node), route + " is its node");
    }
    assertFalse(seen.contains(XjSignalCatalog.N.START));
  }

  @Test
  void theHardOffSwitchIsXjSignalOff() {
    assertTrue(UsageService.isHardOff(Map.of("XJ_SIGNAL", "off"), null));
    assertTrue(UsageService.isHardOff(Map.of("XJ_SIGNAL", "OFF"), null));
    assertTrue(UsageService.isHardOff(Map.of("XJ_SIGNAL", "0"), null));
    assertTrue(UsageService.isHardOff(Map.of(), "off"));
    assertTrue(UsageService.isHardOff(Map.of(), "false"));
    assertFalse(UsageService.isHardOff(Map.of(), null));
    assertFalse(UsageService.isHardOff(Map.of("XJ_SIGNAL", "on"), null));
    // The tests themselves run with -Dxj.signal=off (build.gradle)
    assertTrue(UsageService.isHardOff(Map.of(), System.getProperty(UsageService.HARD_OFF_PROPERTY)));
  }

  @Test
  void theFilesAreKeptInTheWorkstationsOwnFolder() {
    assertEquals(Path.of("C:/Users/x/AppData/Roaming", "XJ Music", "workstation", "signal"),
      UsageService.dataFolder("Windows 11", "C:/Users/x", Map.of("APPDATA", "C:/Users/x/AppData/Roaming")));
    assertEquals(Path.of("/Users/x", "Library", "Application Support", "XJ Music", "workstation", "signal"),
      UsageService.dataFolder("Mac OS X", "/Users/x", Map.of()));
    assertEquals(Path.of("/home/x", ".local", "share", "xjmusic", "workstation", "signal"),
      UsageService.dataFolder("Linux", "/home/x", Map.of()));
  }

  @Test
  void anInstalledBuildIsADownloadAndACheckoutIsSource() {
    assertEquals("download", UsageChannel.resolve("C:/Program Files/XJ music workstation/XJ music workstation.exe"));
    assertEquals("source", UsageChannel.resolve(null));
  }

  @Test
  void beforeAYesNothingIsSentOrWritten_afterANoNothingEver() {
    var store = new SignalStore.Memory();
    var posts = new AtomicInteger();
    SignalTransport transport = (url, key, type, body) -> {
      posts.incrementAndGet();
      return CompletableFuture.completedFuture(new SignalTransport.Response(204, 0));
    };
    var host = new SignalHost(1, 8, 16L << 30, "en-US", "download", false);
    var client = new SignalClient(XjSignalCatalog.CATALOG, "xjmusic-workstation.test", "https://ingest.test", "1.9.6", store, transport, host, null, null, null);
    client.start();
    assertTrue(client.needsConsent());
    client.countLabel(XjSignalCatalog.M.FEATURES, XjSignalCatalog.L.FEATURES_PROJECT_OPEN);
    client.finish(Duration.ofSeconds(1));
    assertEquals(0, posts.get());
    assertEquals(0, store.writes);

    var again = new SignalClient(XjSignalCatalog.CATALOG, "xjmusic-workstation.test", "https://ingest.test", "1.9.6", store, transport, host, null, null, null);
    again.start();
    again.setConsent(false);
    again.countLabel(XjSignalCatalog.M.FEATURES, XjSignalCatalog.L.FEATURES_PROJECT_OPEN);
    again.finish(Duration.ofSeconds(1));
    assertEquals(0, posts.get());
    assertNull(again.previewNextPayload());
    assertFalse(again.needsConsent(), "a no is never asked again");
  }

  @Test
  void aYesSendsOneSmallSummaryAtQuit() {
    var store = new SignalStore.Memory();
    var posts = new AtomicInteger();
    var size = new AtomicInteger();
    SignalTransport transport = (url, key, type, body) -> {
      posts.incrementAndGet();
      size.set(body.length);
      assertEquals("https://ingest.test/v1/s", url);
      return CompletableFuture.completedFuture(new SignalTransport.Response(204, 0));
    };
    var host = new SignalHost(1, 8, 16L << 30, "en-US", "download", false);
    var client = new SignalClient(XjSignalCatalog.CATALOG, "xjmusic-workstation.test", "https://ingest.test", "1.9.6", store, transport, host, null, null, null);
    client.start();
    client.setConsent(true);
    client.node(XjSignalCatalog.N.START);
    client.node(XjSignalCatalog.N.FABRICATION_TIMELINE);
    client.countLabel(XjSignalCatalog.M.FEATURES, XjSignalCatalog.L.FEATURES_PREVIEW_START);
    client.milestone(XjSignalCatalog.MS.MUSIC_PLAYING);
    assertEquals(0, posts.get(), "nothing leaves before quit");
    client.finish(Duration.ofSeconds(1));
    assertEquals(1, posts.get());
    assertTrue(size.get() < 256, size.get() + " bytes");
    assertEquals(1, client.lastSent().size());
  }

  @Test
  void theHardOffSwitchLeavesTheClientOff() {
    var store = new SignalStore.Memory();
    var host = new SignalHost(1, 8, 16L << 30, "en-US", "download", true);
    var client = new SignalClient(XjSignalCatalog.CATALOG, "xjmusic-workstation.test", "https://ingest.test", "1.9.6", store, null, host, null, null, null);
    client.start();
    assertEquals(SignalClient.State.DISABLED, client.state());
    assertFalse(client.needsConsent(), "no question is asked");
    client.setConsent(true);
    assertFalse(client.hasConsent());
    assertEquals(0, store.writes);
  }
}
