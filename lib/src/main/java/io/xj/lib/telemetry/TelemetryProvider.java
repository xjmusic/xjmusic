// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.telemetry;

import io.opencensus.stats.Measure;

/**
 Send telemetry to GCP https://www.pivotaltracker.com/story/show/180741969
 <p>
 SEE: https://cloud.google.com/monitoring/custom-metrics/open-census
 <p>
 NOTE: This provider automatically prefixes all telemetry:
 - names with "ship_app_" e.g. "coolair_nexus_xyz"
 - descriptions with "Ship App" e.g. "Coolair Nexus Xyz"
 */
public interface TelemetryProvider {
  /**
   Register a simple count aggregated measure

   @param name        of measure
   @param description of measure
   @param unit        of measure
   @return simple count view
   */
  Measure.MeasureLong count(
    String name,
    String description,
    String unit
  );

  /**
   Register a simple gauge aggregated measure

   @param name        of measure
   @param description of measure
   @param unit        of measure
   @return simple gauge view
   */
  Measure.MeasureDouble gauge(String name, String description, String unit);

  /**
   Put a measure value, with a minimum of 0

   @param measure to put
   @param value   to put
   */
  void put(Measure.MeasureLong measure, Long value);

  /**
   Put a measure value, with a minimum of 0

   @param measure to put
   @param value   to put
   */
  void put(Measure.MeasureDouble measure, Double value);

  /**
   Prefix a name with "ship_app_" e.g. "coolair_nexus_xyz"

   @return prefixed name, or original
   */
  String prefixedLowerSnake(String name);

  /**
   Prefix a description with "Ship App" e.g. "Coolair Nexus Xyz"

   @return prefixed name, or original
   */
  String prefixedProperSpace(String desc);
}
