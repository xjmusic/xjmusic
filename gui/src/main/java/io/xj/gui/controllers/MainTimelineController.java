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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;

@Service
public class MainTimelineController extends VBox implements ReadyAfterBootController {
  final FabricationService fabricationService;
  final LabService labService;
  final DoubleProperty refreshRate = new SimpleDoubleProperty(1);

  @Nullable
  Timeline refresh;

  @FXML
  protected ListView<Segment> segmentListView;

  final ObservableList<Segment> segments = FXCollections.observableArrayList();

  public MainTimelineController(
    FabricationService fabricationService,
    LabService labService
  ) {
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
              GridPane grid = new GridPane();
              grid.setHgap(10);
              grid.setVgap(10);

              Label idLabel = new Label("ID: " + item.getId().toString());
              Label typeLabel = new Label("Type: " + item.getType().toString());
              Label stateLabel = new Label("State: " + item.getState().toString());
              Label beginAtLabel = new Label("Begin At: " + item.getBeginAtChainMicros());
              Label durationLabel = new Label("Duration: " + item.getDurationMicros());
              Label keyLabel = new Label("Key: " + item.getKey());
              Label totalLabel = new Label("Total: " + item.getTotal());
              Label offsetLabel = new Label("Offset: " + item.getOffset());
              Label densityLabel = new Label("Density: " + item.getDensity());
              Label tempoLabel = new Label("Tempo: " + item.getTempo());
              Label storageKeyLabel = new Label("Storage Key: " + item.getStorageKey());
              Label waveformPrerollLabel = new Label("Waveform Preroll: " + item.getWaveformPreroll());
              Label waveformPostrollLabel = new Label("Waveform Postroll: " + item.getWaveformPostroll());
              Label deltaLabel = new Label("Delta: " + item.getDelta());
              Label createdAtLabel = new Label("Created At: " + item.getCreatedAt());
              Label updatedAtLabel = new Label("Updated At: " + item.getUpdatedAt());

              // Arrange the labels in the grid; you can adjust as needed
              grid.add(idLabel, 0, 0);
              grid.add(typeLabel, 1, 0);
              grid.add(stateLabel, 2, 0);
              grid.add(beginAtLabel, 0, 1);
              grid.add(durationLabel, 1, 1);
              grid.add(keyLabel, 2, 1);
              grid.add(totalLabel, 0, 2);
              grid.add(offsetLabel, 1, 2);
              grid.add(densityLabel, 2, 2);
              grid.add(tempoLabel, 0, 3);
              grid.add(storageKeyLabel, 1, 3);
              grid.add(waveformPrerollLabel, 2, 3);
              grid.add(waveformPostrollLabel, 0, 4);
              grid.add(deltaLabel, 1, 4);
              grid.add(createdAtLabel, 2, 4);
              grid.add(updatedAtLabel, 0, 5);

              setGraphic(grid);
            }
          }
        };
      }
    });
    segmentListView.setItems(segments);
    // todo sorting segments.addListener((ListChangeListener<Segment>) c -> segments.sort(Comparator.comparing(Segment::getOffset)));
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
