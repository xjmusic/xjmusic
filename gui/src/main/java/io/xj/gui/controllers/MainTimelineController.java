// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.gui.controllers.MainTimelineSegmentFactory.SEGMENT_WIDTH;

@Service
public class MainTimelineController extends ScrollPane implements ReadyAfterBootController {
  private static final int NO_ID = -1;
  private static final int SHOW_LAST_N_SEGMENTS = 20;
  private static final Long MILLIS_PER_MICRO = 1000L;
  private final Integer refreshSyncMillis;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final ObservableList<Segment> segments = FXCollections.observableArrayList();
  final SimpleIntegerProperty outputSyncChainIndex = new SimpleIntegerProperty(NO_ID);
  final double refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleLongProperty outputSyncChainMicros = new SimpleLongProperty(0L);

  @FXML
  HBox segmentPositionRow;

  @FXML
  Pane segmentPositionActiveRegion;

  @FXML
  Pane segmentPositionPastRegion;

  @FXML
  HBox segmentListView;

  @Nullable
  Timeline refreshTimeline;

  @Nullable
  Timeline refreshSync;

  public MainTimelineController(
    @Value("${gui.refresh.timeline.millis}") Integer refreshTimelineMillis,
    @Value("${gui.refresh.sync.millis}") Integer refreshSyncMillis,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService,
    MainTimelineSegmentFactory segmentFactory
  ) {
    this.refreshSyncMillis = refreshSyncMillis;
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshTimelineMicros = refreshTimelineMillis * MILLIS_PER_MICRO;
    this.refreshTimelineMillis = refreshTimelineMillis;
    this.segmentFactory = segmentFactory;
  }

  @Override
  public void onStageReady() {
    refreshTimeline = new Timeline(
      new KeyFrame(
        Duration.millis(refreshTimelineMillis),
        event -> updateTimeline()
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

    outputSyncChainIndex.bind(outputSyncChainMicros.map(micros -> {
      for (var i = 0; i < segments.size(); i++) {
        var segment = segments.get(i);
        if (SegmentUtils.isIntersecting(segment, micros.longValue(), 0L)) {
          return i;
        }
      }
      return NO_ID;
    }));

    segmentPositionPastRegion.prefWidthProperty().bind(outputSyncChainIndex.multiply(SEGMENT_WIDTH));
    segmentPositionPastRegion.visibleProperty().bind(outputSyncChainIndex.greaterThan(NO_ID));

    segmentPositionActiveRegion.setPrefWidth(SEGMENT_WIDTH);
    segmentPositionActiveRegion.visibleProperty().bind(outputSyncChainIndex.greaterThan(NO_ID));
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
      return;
    }

    // compute current first and last id
    int firstId = NO_ID;
    int lastId = NO_ID;
    for (Segment s : segments) {
      if (firstId == NO_ID || s.getId() < firstId) firstId = s.getId();
      if (lastId == NO_ID || s.getId() > lastId) lastId = s.getId();
    }

    // get updated segments and compute updated first and last id
    var updatedSegments = fabricationService.getSegments(SHOW_LAST_N_SEGMENTS, null, outputSyncChainMicros.get(), refreshTimelineMicros * 2).stream().collect(Collectors.toMap(Segment::getId, s -> s));
    int updatedFirstId = NO_ID;
    int currentLastId = NO_ID;
    for (Segment s : updatedSegments.values()) {
      if (updatedFirstId == NO_ID || s.getId() < updatedFirstId) updatedFirstId = s.getId();
      if (currentLastId == NO_ID || s.getId() > currentLastId) currentLastId = s.getId();
    }

    // Add current segments to end of list if their id is greater than the existing last id
    for (Segment s : updatedSegments.values())
      if (firstId == NO_ID || lastId == NO_ID || s.getId() > lastId) {
        if (firstId == NO_ID || s.getId() < firstId) firstId = s.getId();
        if (lastId == NO_ID || s.getId() > lastId) lastId = s.getId();
        segments.add(s);
        segmentListView.getChildren().add(segmentFactory.computeSegmentNode(s));
      }

    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    for (var i = 0; i < segments.size(); i++)
      if (updatedSegments.containsKey(i)) {
        segments.set(i, updatedSegments.get(i));
        segmentListView.getChildren().set(i, segmentFactory.computeSegmentNode(updatedSegments.get(i)));
      }

    // remove segments from the beginning of the list if their id is less than the updated first id
    for (var i = 0; i < segments.size(); i++)
      if (segments.get(i).getId() < updatedFirstId) {
        segments.remove(i);
        segmentListView.getChildren().remove(i);
        i--;
      }
  }

  /**
   Called frequently to update the sync (playback position indicator).
   */
  void updateSync() {
    fabricationService.getWorkFactory().getOutputSyncChainMicros().ifPresent(outputSyncChainMicros::set);
  }
}
