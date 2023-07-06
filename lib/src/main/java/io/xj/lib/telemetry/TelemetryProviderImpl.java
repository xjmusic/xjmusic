// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import com.google.api.client.util.Strings;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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
  private static final Logger LOG = LoggerFactory.getLogger(TelemetryProviderImpl.class);
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();
  private static final String DEFAULT_SHIP_KEY = "lab";
  private final String prefixA;
  private final String prefixB;
  private final boolean enabled;

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

    // Enable OpenCensus exporters to export metrics to Stackdriver Monitoring.
    // Exporters use Application Default Credentials to authenticate.
    // See https://developers.google.com/identity/protocols/application-default-credentials
    // for more details.
    try {
      StackdriverStatsExporter.createAndRegister();
    } catch (Exception e) {
      LOG.warn("Exception while registering stack driver stats exporter", e);
    }
  }

  @Override
  public void put(Measure.MeasureLong measure, Long value) {
    if (!enabled) return;
    STATS_RECORDER.newMeasureMap().put(measure, Math.max(0, value)).record();
  }

  @Override
  public void put(Measure.MeasureDouble measure, Double value) {
    if (!enabled) return;
    STATS_RECORDER.newMeasureMap().put(measure, Math.max(0, value)).record();
  }

  @Override
  public Measure.MeasureLong count(String name, String desc, String unit) {
    var measure = Measure.MeasureLong.create(prefixedLowerSnake(name), prefixedProperSpace(desc), unit);

    // built the view
    View view = View.create(View.Name.create(prefixedLowerSnake(name)),
      prefixedProperSpace(desc),
      measure,
      Aggregation.Count.create(),
      List.of());

    // Register the view. It is imperative that this step exists,
    // otherwise recorded metrics will be dropped and never exported.
    Stats.getViewManager().registerView(view);

    return measure;
  }

  @Override
  public Measure.MeasureDouble gauge(String name, String desc, String unit) {
    var measure = Measure.MeasureDouble.create(prefixedLowerSnake(name), prefixedProperSpace(desc), unit);

    // built the view
    View view = View.create(View.Name.create(prefixedLowerSnake(name)),
      prefixedProperSpace(desc),
      measure,
      Aggregation.LastValue.create(),
      List.of());

    // Register the view. It is imperative that this step exists,
    // otherwise recorded metrics will be dropped and never exported.
    Stats.getViewManager().registerView(view);

    return measure;
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
