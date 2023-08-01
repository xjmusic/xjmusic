// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.common.base.Strings;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.xj.lib.util.Text.SPACE;
import static io.xj.lib.util.Text.UNDERSCORE;

/**
 * Send telemetry to GCP https://www.pivotaltracker.com/story/show/180741969
 * <p>
 * SEE: https://cloud.google.com/monitoring/custom-metrics/open-census
 * <p>
 * NOTE: This provider automatically prefixes all telemetry:
 * - names with "ship_app_" e.g. "coolair_nexus_xyz"
 * - descriptions with "Ship App" e.g. "Coolair Nexus Xyz"
 */
@Service
class TelemetryProviderImpl implements TelemetryProvider {
  static final Logger LOG = LoggerFactory.getLogger(TelemetryProviderImpl.class);
  static final String DEFAULT_SHIP_KEY = "lab";
  final String prefixA;
  final String prefixB;
  final boolean enabled;

  @Autowired
  public TelemetryProviderImpl(
    AppConfiguration config,
    @Value("${input.template.key:}")
    String inputTemplateKey,
    @Value("${telemetry.enabled:false}")
    Boolean isTelemetryEnabled
  ) {
    prefixA = Strings.isNullOrEmpty(inputTemplateKey) ? DEFAULT_SHIP_KEY : inputTemplateKey;
    prefixB = config.getName();

    // Globally enable or disable telemetry recording
    enabled = isTelemetryEnabled;
    if (!enabled) {
      LOG.info("Will not send telemetry.");
      return;
    }

    // FUTURE: initialize telemetry sending mechanism
  }

  @Override
  public void put(TelemetryMeasureCount measure, Long value) {
    if (!enabled) return;
    // FUTURE: send telemetry e.g. Math.max(0, value)
  }

  @Override
  public void put(TelemetryMeasureGauge measure, Double value) {
    if (!enabled) return;
    // FUTURE: send telemetry e.g. Math.max(0, value)
  }

  @Override
  public TelemetryMeasureCount count(String name, String desc, String unit) {
    // FUTURE: create a count measure in our telemetry sending paradigm
    return new TelemetryMeasureCount();
  }

  @Override
  public TelemetryMeasureGauge gauge(String name, String desc, String unit) {
    // FUTURE: create a gauge measure in our telemetry sending paradigm
    return new TelemetryMeasureGauge();
  }

  @Override
  public String prefixedLowerSnake(String name) {
    return String.join(UNDERSCORE,
      Text.toLowerScored(prefixA),
      Text.toLowerScored(prefixB),
      Text.toLowerScored(name)
    );
  }

  @Override
  public String prefixedProperSpace(String desc) {
    return String.join(SPACE,
      Text.toProper(prefixA),
      Text.toProper(prefixB),
      Text.toProper(desc)
    );
  }

}
