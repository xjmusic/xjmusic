// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
  private final Integer segmentMinWidth;
  private final Integer segmentHorizontalSpacing;
  private final Integer autoScrollBehindPixels;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final ObservableList<Segment> segments = FXCollections.observableArrayList();
  final double refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleFloatProperty microsPerPixel = new SimpleFloatProperty(0);
  final Timeline scrollPaneAnimationTimeline = new Timeline();

  @FXML
  public ScrollPane scrollpane;

  @FXML
  HBox segmentPositionRow;

  @FXML
  Rectangle timelineRegion1Past;

  @FXML
  Rectangle timelineRegion2Ship;

  @FXML
  Rectangle timelineRegion4Craft;

  @FXML
  Rectangle timelineRegion3Dub;

  @FXML
  HBox segmentListView;

  @Nullable
  Timeline refreshTimeline;

  @Nullable
  Timeline refreshSync;

  public MainTimelineController(
    @Value("${gui.timeline.refresh.millis}") Integer refreshTimelineMillis,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
    @Value("${gui.timeline.sync.refresh.millis}") Integer refreshSyncMillis,
    @Value("${gui.timeline.auto.scroll.behind.pixels}") Integer autoScrollBehindPixels,
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
    this.autoScrollBehindPixels = autoScrollBehindPixels;
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
    if (fabricationService.isEmpty()) {
      segments.clear();
      segmentListView.getChildren().clear();
      return;
    }

    // keep track of whether we added or removed any segments
    boolean added = false;
    boolean removed = false;

    // get updated segments and compute updated first id (to clean up segments before that id)
    var updatedSegmentsById = fabricationService.getSegments(null).stream().collect(Collectors.toMap(Segment::getId, s -> s));
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
        added = true;
      }

    // remove segments from the beginning of the list if their id is less than the updated first id
    for (var i = 0; i < segments.size(); i++)
      if (segments.get(i).getId() < updatedFirstId) {
        segments.remove(i);
        segmentListView.getChildren().remove(i);
        i--;
        removed = true;
      }

    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    for (var i = 0; i < segments.size(); i++)
      if (updatedSegmentsById.containsKey(segments.get(i).getId()) && SegmentUtils.isSameButUpdated(segments.get(i), updatedSegmentsById.get(segments.get(i).getId()))) {
        segments.set(i, updatedSegmentsById.get(segments.get(i).getId()));
        segmentListView.getChildren().set(i, segmentFactory.create(updatedSegmentsById.get(segments.get(i).getId()), microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
      }

    // update the sync position before scrolling
    updateSync();

    // auto-scroll if enabled, animating to the scroll pane position
    if (fabricationService.followPlaybackProperty().getValue() && 0 < segmentListView.getWidth() && 0 < microsPerPixel.get()) {
      scrollPaneAnimationTimeline.stop();
      scrollPaneAnimationTimeline.getKeyFrames().clear();

      segmentListView.layout();
      scrollpane.layout();
      var extraHorizontalPixels = Math.max(0, segmentListView.getWidth() - scrollpane.getWidth());
      var targetOffsetHorizontalPixels = Math.max(0, timelineRegion1Past.getWidth() - autoScrollBehindPixels);

      if (fabricationService.isOutputModeSync().getValue()) {
        // if we added or removed, jump to the target position minus our current velocity
        if (added || removed) {
          scrollpane.setHvalue((targetOffsetHorizontalPixels - ((MILLIS_PER_MICRO * refreshTimelineMillis) / microsPerPixel.get())) / extraHorizontalPixels);
        }
        KeyValue kv = new KeyValue(scrollpane.hvalueProperty(), targetOffsetHorizontalPixels / extraHorizontalPixels);
        KeyFrame kf = new KeyFrame(Duration.millis(refreshTimelineMillis), kv);
        scrollPaneAnimationTimeline.getKeyFrames().add(kf);
        scrollPaneAnimationTimeline.play();
      } else {
        scrollpane.setHvalue(targetOffsetHorizontalPixels / extraHorizontalPixels);
      }
    }
  }

  /**
   Called frequently to update the sync (playback position indicator).
   */
  void updateSync() {
    if (!fabricationService.isStatusActive().get() || 0 == microsPerPixel.get()) {
      return;
    }
    var firstVisibleSegment = fabricationService.getSegments(null).stream().findFirst();
    var m0 = firstVisibleSegment.map(Segment::getBeginAtChainMicros).orElse(0L);
    var m1Past = fabricationService.getWorkFactory().getShippedToChainMicros().orElse(m0);
    var m2Ship = fabricationService.getWorkFactory().getShipTargetChainMicros().orElse(m1Past);
    var m3Dub = fabricationService.getWorkFactory().getDubbedToChainMicros().orElse(m2Ship);
    var m4Craft = fabricationService.getWorkFactory().getCraftedToChainMicros().orElse(m3Dub);
    timelineRegion1Past.setWidth((m1Past - m0) / microsPerPixel.get());
    timelineRegion2Ship.setWidth((m2Ship - m1Past) / microsPerPixel.get());
    timelineRegion3Dub.setWidth((m3Dub - m2Ship) / microsPerPixel.get());
    timelineRegion4Craft.setWidth((m4Craft - m3Dub) / microsPerPixel.get());
  }
}
