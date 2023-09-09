// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.listeners.NoSelectionModel;
import io.xj.gui.models.SegmentOnTimeline;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final Integer refreshRateSeconds;
  final MainTimelineSegmentFactory segmentFactory;
  final LabService labService;
  final ObservableList<SegmentOnTimeline> segments = FXCollections.observableArrayList();

  @Nullable
  Timeline refresh;

  @FXML
  protected ListView<SegmentOnTimeline> segmentListView;

  public MainTimelineController(
    @Value("${gui.timeline.refresh.seconds}") Integer refreshRateSeconds,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService,
    MainTimelineSegmentFactory segmentFactory
  ) {
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshRateSeconds = refreshRateSeconds;
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
            setGraphic(segmentFactory.computeSegmentNode(item));
          }
        }
      };
    }
  };

  @Override
  public void onStageReady() {
    refresh = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> updateSegmentList()
      )
    );
    refresh.setCycleCount(Timeline.INDEFINITE);
    refresh.setRate(refreshRateSeconds);
    refresh.play();

    segmentListView.setSelectionModel(new NoSelectionModel<>());
    segmentListView.setCellFactory(cellFactory);
    segmentListView.setItems(segments);
  }

  @Override
  public void onStageClose() {
    if (Objects.nonNull(refresh)) {
      refresh.stop();
    }
  }

  /**
   Called every second to update the segment list.
   */
  void updateSegmentList() {
    if (Objects.isNull(fabricationService.getWorkFactory().getCraftWork())) {
      segments.clear();
      return;
    }

    // TODO move the getOutputSyncChainMicros out of the segment factory loop and into its own more-frequent loop
    // TODO instead of writing new objects to represent active/inactive, update the master node and let displays be reactive
    var outputSyncChainMicros = fabricationService.getWorkFactory().getOutputSyncChainMicros();

    var currentSegments = fabricationService.getSegmentsOnTimeline(0, SHOW_LAST_N_SEGMENTS, outputSyncChainMicros.orElse(null));

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

}
