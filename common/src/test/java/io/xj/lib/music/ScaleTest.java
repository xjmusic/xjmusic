// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ScaleTest {
  private static final String EXPECTED_SCALES_YAML = "/music/expect_scale.yaml";
  private static final String KEY_SCALES = "scales";
  private static final String KEY_ROOT_PITCH_CLASS = "root";
  private static final Object KEY_PITCHES = "pitches";

  @Test
  public void TestScaleExpectations() throws Exception {
    Yaml yaml = new Yaml();

    Map<?, ?> wrapper = (Map<?, ?>) yaml.load(getClass().getResourceAsStream(EXPECTED_SCALES_YAML));
    assertNotNull(wrapper);

    Map<?, ?> scales = (Map<?, ?>) wrapper.get(KEY_SCALES);
    assertNotNull(scales);

    scales.keySet().forEach((scaleName) -> {
      Map<?, ?> scale = (Map<?, ?>) scales.get(scaleName);
      PitchClass expectRootPitchClass = PitchClass.of(String.valueOf(scale.get(KEY_ROOT_PITCH_CLASS)));
      assertNotNull(expectRootPitchClass);

      Map<?, ?> rawPitches = (Map<?, ?>) scale.get(KEY_PITCHES);
      assertNotNull(rawPitches);

      Map<Interval, PitchClass> expectPitches = Maps.newHashMap();
      rawPitches.forEach((rawInterval, rawPitchClass) ->
        expectPitches.put(
          Interval.valueOf(Integer.valueOf(String.valueOf(rawInterval))),
          PitchClass.of(String.valueOf(rawPitchClass))));

      assertScaleExpectations(expectRootPitchClass, expectPitches, Scale.of(String.valueOf(scaleName)));
    });
  }

  private void assertScaleExpectations(PitchClass expectRootPitchClass, Map<Interval, PitchClass> expectPitchClasses, Scale scale) {
    System.out.println(
      "Expect pitch classes " + IntervalPitchGroup.detailsOf(expectPitchClasses, scale.getAdjSymbol()) + " for " +
        "Scale " + scale.details());
    Map<Interval, PitchClass> pitchClasses = scale.getPitchClasses();
    assertEquals(expectRootPitchClass, scale.getRoot());
    assertEquals(expectPitchClasses.size(), pitchClasses.size());
    expectPitchClasses.forEach((expectInterval, expectPitchClass) ->
      assertEquals(expectPitchClass, pitchClasses.get(expectInterval)));
  }

}
