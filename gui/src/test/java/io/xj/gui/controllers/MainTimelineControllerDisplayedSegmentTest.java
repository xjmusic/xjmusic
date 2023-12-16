// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.nexus.model.Segment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MainTimelineControllerDisplayedSegmentTest {

  @Mock
  FabricationService fabricationService;

  @Test
  void isIntersecting() {
    when(fabricationService.computeChoiceHash(any())).thenReturn("123");
    MainTimelineController.DisplayedSegment subject = new MainTimelineController.DisplayedSegment(
      new Segment().beginAtChainMicros(1000L).durationMicros(1000L),
      fabricationService,
      3
    );

    assertFalse(subject.isIntersecting(500L));
    assertTrue(subject.isIntersecting(1000L));
    assertTrue(subject.isIntersecting(1500L));
    assertFalse(subject.isIntersecting(2000L));
    assertFalse(subject.isIntersecting(2500L));
  }

  @Test
  void isAfter() {
    when(fabricationService.computeChoiceHash(any())).thenReturn("123");
    MainTimelineController.DisplayedSegment subject = new MainTimelineController.DisplayedSegment(
      new Segment().beginAtChainMicros(1000L).durationMicros(1000L),
      fabricationService,
      3
    );

    assertTrue(subject.isAfter(500L));
    assertTrue(subject.isAfter(1000L));
    assertFalse(subject.isAfter(1500L));
    assertFalse(subject.isAfter(2000L));
    assertFalse(subject.isAfter(2500L));
  }

  @Test
  void isBefore() {
    when(fabricationService.computeChoiceHash(any())).thenReturn("123");
    MainTimelineController.DisplayedSegment subject = new MainTimelineController.DisplayedSegment(
      new Segment().beginAtChainMicros(1000L).durationMicros(1000L),
      fabricationService,
      3
    );

    assertFalse(subject.isBefore(500L));
    assertFalse(subject.isBefore(1000L));
    assertFalse(subject.isBefore(1500L));
    assertFalse(subject.isBefore(2000L));
    assertTrue(subject.isBefore(2500L));
  }

  @Test
  void computePositionRatio() {
    when(fabricationService.computeChoiceHash(any())).thenReturn("123");
    MainTimelineController.DisplayedSegment subject = new MainTimelineController.DisplayedSegment(
      new Segment().beginAtChainMicros(1000L).durationMicros(1000L),
      fabricationService,
      3
    );

    assertEquals(0.0, subject.computePositionRatio(1000L), 0.001);
    assertEquals(.5, subject.computePositionRatio(1500L), 0.001);
    assertEquals(1.0, subject.computePositionRatio(2000L), 0.001);
  }
}
