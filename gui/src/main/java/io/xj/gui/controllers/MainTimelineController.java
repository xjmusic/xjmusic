// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.listeners.NoSelectionModel;
import io.xj.gui.models.SegmentOnTimeline;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MainTimelineController extends ScrollPane implements ReadyAfterBootController {
  private static final int SHOW_LAST_N_SEGMENTS = 20;
  private static final Long MILLIS_PER_MICRO = 1000L;
  private final Integer refreshSyncMillis;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final ObservableList<SegmentOnTimeline> segments = FXCollections.observableArrayList();
  final double refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleLongProperty outputSyncChainMicros = new SimpleLongProperty(0L);

  @Nullable
  Timeline refreshTimeline;

  @Nullable
  Timeline refreshSync;

  @FXML
  protected ListView<SegmentOnTimeline> segmentListView;

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

  final Callback<ListView<SegmentOnTimeline>, ListCell<SegmentOnTimeline>> cellFactory = new Callback<>() {
    @Override
    public ListCell<SegmentOnTimeline> call(ListView<SegmentOnTimeline> param) {
      return new ListCell<>() {
        @Override
        protected void updateItem(SegmentOnTimeline item, boolean empty) {
          super.updateItem(item, empty);

          if (empty || item == null) {
            setGraphic(null);
          } else {
            setGraphic(segmentFactory.computeSegmentNode(item, outputSyncChainMicros));
          }
        }
      };
    }
  };

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

    segmentListView.setSelectionModel(new NoSelectionModel<>());
    segmentListView.setCellFactory(cellFactory);
    segmentListView.setItems(segments);
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

    var currentSegments = fabricationService.getSegmentsOnTimeline(SHOW_LAST_N_SEGMENTS, null, outputSyncChainMicros.get(), refreshTimelineMicros * 2);

    segments.removeIf(segment -> currentSegments.stream().noneMatch(source -> Objects.equals(source.getId(), segment.getId())));
    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    for (var i = 0; i < segments.size(); i++) {
      var segment = segments.get(i);
      var source = currentSegments.stream().filter(s -> s.isSameButUpdated(segment)).findFirst();
      if (source.isPresent()) {
        segments.set(i, source.get());
      }
    }
    currentSegments.forEach(source -> {
      if (segments.stream().noneMatch(segment -> Objects.equals(segment.getId(), source.getId()))) {
        segments.add(source);
      }
    });
  }

  /**
   Called frequently to update the sync (playback position indicator).
   */
  void updateSync() {
    fabricationService.getWorkFactory().getOutputSyncChainMicros().ifPresent(outputSyncChainMicros::set);
  }

}
