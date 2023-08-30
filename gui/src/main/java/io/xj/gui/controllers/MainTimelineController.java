// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class MainTimelineController extends VBox implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(MainTimelineController.class);
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final Integer refreshRateSeconds;
  final LabService labService;
  final Map<UUID, SegmentCell> cellCache = new HashMap<>();
  final Resource timelineSegmentFxml;
  final ObservableList<Segment> segments = FXCollections.observableArrayList();

  @Nullable
  Timeline refresh;

  @FXML
  protected ListView<Segment> segmentListView;

  final Callback<ListView<Segment>, ListCell<Segment>> cellFactory = new Callback<>() {
    @Override
    public ListCell<Segment> call(ListView<Segment> param) {
      return new ListCell<>() {
        @Override
        protected void updateItem(Segment item, boolean empty) {
          super.updateItem(item, empty);

          if (empty || item == null) {
            setGraphic(null);
          } else {
            var cell = getCachedCellContent(item);
            setGraphic(cell.content);
          }
        }
      };
    }
  };

  SegmentCell getCachedCellContent(Segment item) {
    try {
      if (!cellCache.containsKey(item.getId())) {
        LOG.info("Will load FXML for Segment@{}", item.getOffset());
        FXMLLoader loader = new FXMLLoader(timelineSegmentFxml.getURL());
        loader.setControllerFactory(this::createSegmentController);
        Node cellContent = loader.load();
        MainTimelineSegmentController cellController = loader.getController();
        cellController.onStageReady();
        cellController.setSegment(item);
        cellController.setMemes(fabricationService.getSegmentMemes(item));
        cellController.setChords(fabricationService.getSegmentChords(item));
        cellController.setChoices(fabricationService.getSegmentChoices(item));
        cellCache.put(item.getId(), new SegmentCell(cellController, cellContent, item));
      }
      return cellCache.get(item.getId());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public MainTimelineController(
    @Value("classpath:/views/main-timeline-segment.fxml") Resource timelineSegmentFxml,
    @Value("${gui.timeline.refresh.seconds}") Integer refreshRateSeconds,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService
  ) {
    this.timelineSegmentFxml = timelineSegmentFxml;
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshRateSeconds = refreshRateSeconds;
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
    refresh.setRate(refreshRateSeconds);
    refresh.play();

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
    var sources = fabricationService.getWorkFactory().getCraftWork().getAllSegments();
    segments.removeIf(segment -> sources.stream().noneMatch(source -> source.getId().equals(segment.getId())));
    sources.forEach(source -> {
      if (segments.stream().noneMatch(segment -> segment.getId().equals(source.getId()))) {
        segments.add(source);
      }
    });
  }

  MainTimelineSegmentController createSegmentController(Class<?> ignored) {
    return new MainTimelineSegmentController(fabricationService);
  }

  private static class SegmentCell {
    MainTimelineSegmentController controller;
    Node content;
    Segment segment;

    public SegmentCell(
      MainTimelineSegmentController controller,
      Node content,
      Segment segment
    ) {
      this.content = content;
      this.controller = controller;
      this.segment = segment;
    }
  }
}
