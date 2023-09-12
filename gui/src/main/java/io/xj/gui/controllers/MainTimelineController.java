// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MainTimelineController extends ScrollPane implements ReadyAfterBootController {
  private static final int NO_ID = -1;
  private static final Long MILLIS_PER_MICRO = 1000L;
  private final Integer refreshSyncMillis;
  private final Integer showMaxSegments;
  private final Integer segmentMinWidth;
  private final Integer segmentHorizontalSpacing;
  private final Integer timelinePositionActiveRegionWidth;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final ObservableList<Segment> segments = FXCollections.observableArrayList();
  final double refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleFloatProperty microsPerPixel = new SimpleFloatProperty(0);

  @FXML
  HBox segmentPositionRow;

  @FXML
  Rectangle timelinePastRegion;

  @FXML
  Rectangle timelineActiveRegion;

  @FXML
  Rectangle timelineCraftedRegion;

  @FXML
  Rectangle timelineDubbedRegion;

  @FXML
  HBox segmentListView;

  @Nullable
  Timeline refreshTimeline;

  @Nullable
  Timeline refreshSync;

  public MainTimelineController(
    @Value("${gui.timeline.max.segments}") Integer showMaxSegments,
    @Value("${gui.timeline.refresh.millis}") Integer refreshTimelineMillis,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
    @Value("${gui.timeline.sync.refresh.millis}") Integer refreshSyncMillis,
    @Value("${gui.timeline.position.active.region.width}") Integer timelinePositionActiveRegionWidth,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService,
    MainTimelineSegmentFactory segmentFactory
  ) {
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.segmentFactory = segmentFactory;
    this.refreshSyncMillis = refreshSyncMillis;
    this.refreshTimelineMicros = refreshTimelineMillis * MILLIS_PER_MICRO;
    this.refreshTimelineMillis = refreshTimelineMillis;
    this.segmentHorizontalSpacing = segmentSpacingHorizontal;
    this.segmentMinWidth = segmentWidthMin;
    this.showMaxSegments = showMaxSegments;
    this.timelinePositionActiveRegionWidth = timelinePositionActiveRegionWidth;
  }

  @Override
  public void onStageReady() {
    refreshTimeline = new Timeline(
      new KeyFrame(
        Duration.millis(refreshTimelineMillis),
        event -> {
          updateTimeline();
          updateSync();
        }
      )
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
    refreshTimeline.setRate(1.0);
    refreshTimeline.play();

    refreshSync = new Timeline(
      new KeyFrame(
        Duration.millis(refreshSyncMillis),
        event -> updateSync()
      )
    );
    refreshSync.setCycleCount(Timeline.INDEFINITE);
    refreshSync.setRate(1.0);
    refreshSync.play();

    timelineActiveRegion.setWidth(timelinePositionActiveRegionWidth);
    segmentListView.setSpacing(segmentHorizontalSpacing);
    segmentListView.setPadding(new Insets(0, segmentMinWidth, 0, segmentHorizontalSpacing));
  }

  @Override
  public void onStageClose() {
    if (Objects.nonNull(refreshTimeline)) {
      refreshTimeline.stop();
    }
    if (Objects.nonNull(refreshSync)) {
      refreshSync.stop();
    }
  }

  /**
   Called to update the timeline (segment list).
   */
  void updateTimeline() {
    if (Objects.isNull(fabricationService.getWorkFactory().getCraftWork())) {
      segments.clear();
      segmentListView.getChildren().clear();
      return;
    }

    // get updated segments and compute updated first id (to clean up segments before that id)
    var updatedSegmentsById = fabricationService.getSegments(showMaxSegments, null).stream().collect(Collectors.toMap(Segment::getId, s -> s));
    int updatedFirstId = NO_ID;
    for (Segment s : updatedSegmentsById.values()) {
      if (updatedFirstId == NO_ID || s.getId() < updatedFirstId) updatedFirstId = s.getId();
    }

    // determine if the segment pixels-per-micro has changed, and we will re-render the whole list and return
    long updatedDurationMinMicros = SegmentUtils.getDurationMinMicros(updatedSegmentsById.values());
    if (updatedDurationMinMicros > 0) {
      float updatedMicrosPerPixel = (float) updatedDurationMinMicros / segmentMinWidth;
      if (updatedMicrosPerPixel != microsPerPixel.get()) {
        microsPerPixel.set(updatedMicrosPerPixel);
        segmentListView.getChildren().clear();
        for (Segment s : updatedSegmentsById.values()) {
          segments.add(s);
          segmentListView.getChildren().add(segmentFactory.create(s, updatedMicrosPerPixel, segmentMinWidth, segmentHorizontalSpacing));
        }
        return;
      }
    }

    // compute current first and last id
    int firstId = NO_ID;
    int lastId = NO_ID;
    for (Segment s : segments) {
      if (firstId == NO_ID || s.getId() < firstId) firstId = s.getId();
      if (lastId == NO_ID || s.getId() > lastId) lastId = s.getId();
    }

    // Add current segments to end of list if their id is greater than the existing last id
    for (Segment s : updatedSegmentsById.values())
      if (firstId == NO_ID || lastId == NO_ID || s.getId() > lastId) {
        if (firstId == NO_ID || s.getId() < firstId) firstId = s.getId();
        if (lastId == NO_ID || s.getId() > lastId) lastId = s.getId();
        segments.add(s);
        segmentListView.getChildren().add(segmentFactory.create(s, microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
      }

    // remove segments from the beginning of the list if their id is less than the updated first id
    for (var i = 0; i < segments.size(); i++)
      if (segments.get(i).getId() < updatedFirstId) {
        segments.remove(i);
        segmentListView.getChildren().remove(i);
        i--;
      }

    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    for (var i = 0; i < segments.size(); i++)
      if (updatedSegmentsById.containsKey(segments.get(i).getId()) && SegmentUtils.isSameButUpdated(segments.get(i), updatedSegmentsById.get(segments.get(i).getId()))) {
        segments.set(i, updatedSegmentsById.get(segments.get(i).getId()));
        segmentListView.getChildren().set(i, segmentFactory.create(updatedSegmentsById.get(segments.get(i).getId()), microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
      }
  }

  /**
   Called frequently to update the sync (playback position indicator).
   */
  void updateSync() {
    var viewFromChainMicros = segments.isEmpty() ? 0 : segments.get(0).getBeginAtChainMicros();
    var shippedToChainMicros = fabricationService.getWorkFactory().getShippedToChainMicros();
    var dubbedToChainMicros = fabricationService.getWorkFactory().getDubbedToChainMicros();
    var craftedToChainMicros = fabricationService.getWorkFactory().getCraftedToChainMicros();

    if (shippedToChainMicros.isPresent() && 0 < shippedToChainMicros.get()) {
      timelinePastRegion.setWidth((shippedToChainMicros.get() - viewFromChainMicros) / microsPerPixel.get());
      timelinePastRegion.setVisible(true);
      timelineActiveRegion.setVisible(true);
    } else {
      timelinePastRegion.setVisible(false);
      timelineActiveRegion.setVisible(false);
    }

    if (dubbedToChainMicros.isPresent() && 0 < dubbedToChainMicros.get()) {
      timelineDubbedRegion.setWidth(
        (dubbedToChainMicros.get() - shippedToChainMicros.orElse(viewFromChainMicros)) / microsPerPixel.get());
    } else {
      timelineDubbedRegion.setWidth(0);
    }

    if (craftedToChainMicros.isPresent() && 0 < craftedToChainMicros.get()) {
      timelineCraftedRegion.setWidth(
        (craftedToChainMicros.get() - dubbedToChainMicros.orElse(viewFromChainMicros)) / microsPerPixel.get());
    } else {
      timelineCraftedRegion.setWidth(0);
    }
  }
}
