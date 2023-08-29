// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class MainTimelineController extends VBox implements ReadyAfterBootController {
  final Resource timelineSegmentFxml;
  final FabricationService fabricationService;
  final LabService labService;
  final DoubleProperty refreshRate = new SimpleDoubleProperty(1);

  @Nullable
  Timeline refresh;

  @FXML
  protected ListView<Segment> segmentListView;

  final ObservableList<Segment> segments = FXCollections.observableArrayList();

  public MainTimelineController(
    @Value("classpath:/views/main-timeline-segment.fxml") Resource timelineSegmentFxml,
    FabricationService fabricationService,
    LabService labService
  ) {
    this.timelineSegmentFxml = timelineSegmentFxml;
    this.fabricationService = fabricationService;
    this.labService = labService;
  }

  @Override
  public void onStageReady() {
    refresh = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> updateSegmentList()
      )
    );
    refresh.setCycleCount(Timeline.INDEFINITE);
    refresh.rateProperty().bind(refreshRateProperty());
    refresh.play();

    segmentListView.setCellFactory(new Callback<>() {
      @Override
      public ListCell<Segment> call(ListView<Segment> param) {
        return new ListCell<>() {
          @Override
          protected void updateItem(Segment item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
              setText(null);
              setGraphic(null);
            } else {
              try {
                FXMLLoader loader = new FXMLLoader(timelineSegmentFxml.getURL());
                Node cellContent = loader.load();
                MainTimelineSegmentController cellController = loader.getController();
                cellController.onStageReady();
                cellController.setSegment(item);
                setGraphic(cellContent);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        };
      }
    });
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
  private void updateSegmentList() {
    if (Objects.isNull(fabricationService.getWorkFactory().getCraftWork())) {
      segments.clear();
      return;
    }
    var sources = fabricationService.getWorkFactory().getCraftWork().getAllSegments();
    segments.removeIf(segment -> sources.stream().noneMatch(source -> source.getId().equals(segment.getId())));
    sources.forEach(source -> {
      if (segments.stream().noneMatch(segment -> segment.getId().equals(source.getId()))) {
        segments.add(source);
      }
    });
  }

  public DoubleProperty refreshRateProperty() {
    return refreshRate;
  }

}
