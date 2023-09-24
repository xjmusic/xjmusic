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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MainTimelineController extends ScrollPane implements ReadyAfterBootController {
  private static final Long MILLIS_PER_MICRO = 1000L;
  private static final Integer NO_ID = -1;
  private static final Double DEMO_BUTTON_HEIGHT_LEFTOVER = 168.0;
  private static final Double DEMO_BUTTON_SPACING = 10.0;
  private static final Double DEMO_BUTTON_MARGIN = 30.0;
  private final Integer segmentMinWidth;
  private final Integer segmentHorizontalSpacing;
  private final Integer autoScrollBehindPixels;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final List<Segment> segments = new ArrayList<>();
  final double refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleFloatProperty microsPerPixel = new SimpleFloatProperty(0);
  final Timeline scrollPaneAnimationTimeline = new Timeline();
  final SimpleDoubleProperty demoImageWidth = new SimpleDoubleProperty();
  final SimpleDoubleProperty demoImageHeight = new SimpleDoubleProperty();

  @FXML
  ImageView demoSelectionBump;

  @FXML
  ImageView demoSelectionSlaps;

  @FXML
  ImageView demoSelectionSpace;

  @FXML
  ScrollPane scrollPane;

  @FXML
  VBox demoContainer;

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

  public MainTimelineController(
    @Value("${gui.timeline.refresh.millis}") Integer refreshTimelineMillis,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
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
        event -> updateTimeline()
      )
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
    refreshTimeline.setRate(1.0);
    refreshTimeline.play();

    segmentListView.setSpacing(segmentHorizontalSpacing);
    segmentListView.setPadding(new Insets(0, segmentMinWidth * 3, 0, segmentHorizontalSpacing));

    scrollPane.hbarPolicyProperty().bind(fabricationService.followPlaybackProperty().map(followPlayback -> followPlayback ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED));

    demoContainer.visibleProperty().bind(fabricationService.isStatusStandby());
    demoContainer.managedProperty().bind(fabricationService.isStatusStandby());
    demoContainer.maxWidthProperty().bind(scrollPane.widthProperty());
    demoImageWidth.bind(scrollPane.heightProperty().subtract(DEMO_BUTTON_HEIGHT_LEFTOVER));
    demoImageHeight.bind(scrollPane.widthProperty().subtract(DEMO_BUTTON_SPACING * 2 + DEMO_BUTTON_MARGIN * 2).divide(3));
    demoSelectionBump.fitHeightProperty().bind(demoImageHeight);
    demoSelectionBump.fitWidthProperty().bind(demoImageWidth);
    demoSelectionSlaps.fitHeightProperty().bind(demoImageHeight);
    demoSelectionSlaps.fitWidthProperty().bind(demoImageWidth);
    demoSelectionSpace.fitHeightProperty().bind(demoImageHeight);
    demoSelectionSpace.fitWidthProperty().bind(demoImageWidth);
  }

  @Override
  public void onStageClose() {
    stopTimelineAnimation();
  }

  @FXML
  public void handleDemoPlayBump(MouseEvent ignored) {
    fabricationService.handleDemoPlay("bump_deep", 60);
  }

  @FXML
  public void handleDemoPlaySlaps(MouseEvent ignored) {
    fabricationService.handleDemoPlay("slaps_lofi", 60);
  }

  @FXML
  public void handleDemoPlaySpace(MouseEvent ignored) {
    fabricationService.handleDemoPlay("space_flow", 300);
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

    // If we aren't active, none of the rest of this matters
    if (!fabricationService.isStatusActive().getValue()) {
      return;
    }

    // get updated segments and compute updated first id (to clean up segments before that id)
    var latestSegments = fabricationService.getSegments(null);

    // determine if the segment pixels-per-micro has changed, and we will re-render the whole list and return
    long updatedDurationMinMicros = SegmentUtils.getDurationMinMicros(latestSegments);
    if (updatedDurationMinMicros == 0) {
      return;
    }

    // update the micros per pixel if it has changed
    float updatedMicrosPerPixel = (float) updatedDurationMinMicros / segmentMinWidth;
    if (updatedMicrosPerPixel == 0) {
      return;
    }
    if (updatedMicrosPerPixel != microsPerPixel.get()) {
      microsPerPixel.set(updatedMicrosPerPixel);
      // clear the segments and segment list view -- see note below about why we are using this inefficient code
      segments.clear();
      segmentListView.getChildren().clear();
      for (Segment s : latestSegments) {
        segments.add(s);
        segmentListView.getChildren().add(segmentFactory.create(s, updatedMicrosPerPixel, segmentMinWidth, segmentHorizontalSpacing));
      }

    } else {
      // if the micros per pixel has not changed, we will update the segments in place as efficiently as possible
      // get the latest first and last ids of the current and latest segments
      int latestFirstId = latestSegments.stream().min(Comparator.comparing(Segment::getId)).map(Segment::getId).orElse(NO_ID);

      // remove segments from the beginning of the list if their id is less than the updated first id
      while (segments.size() > 0 && segments.get(0).getId() < latestFirstId) {
        segments.remove(0);
        segmentListView.getChildren().remove(0);
      }

      // add current segments to end of list if their id is greater than the existing last id
      int currentLastId = segments.stream().max(Comparator.comparing(Segment::getId)).map(Segment::getId).orElse(NO_ID);
      for (Segment latestSegment : latestSegments) {
        if (latestSegment.getId() > currentLastId) {
          segments.add(latestSegment);
          segmentListView.getChildren().add(segmentFactory.create(latestSegment, microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
        }
      }

      // iterate through all in segments, and update if the updated at time has changed from the source matching that id
      var limit = Math.min(segments.size(), latestSegments.size());
      for (var i = 0; i < limit; i++)
        if (SegmentUtils.isSameButUpdated(segments.get(i), latestSegments.get(i))) {
          segments.set(i, latestSegments.get(i));
          segmentListView.getChildren().set(i, segmentFactory.create(latestSegments.get(i), microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
          var hello = 123;// todo remove
        }
    }

    // Recompute the width of the timeline
    segmentListView.layout();
    scrollPane.layout();

    // Reset the animation timeline
    scrollPaneAnimationTimeline.stop();
    scrollPaneAnimationTimeline.getKeyFrames().clear();

    var m0 = Math.max(0, segments.stream().findFirst().map(Segment::getBeginAtChainMicros).orElse(0L)- segmentHorizontalSpacing);
    var m1Past = fabricationService.getWorkFactory().getShippedToChainMicros().orElse(m0);
    var m2Ship = fabricationService.getWorkFactory().getShipTargetChainMicros().orElse(m1Past);
    var m3Dub = fabricationService.getWorkFactory().getDubbedToChainMicros().orElse(m2Ship);
    var m4Craft = fabricationService.getWorkFactory().getCraftedToChainMicros().orElse(m3Dub);

    // This gets re-used for the follow position as well as past timeline width
    var pastTimelineWidth = (m1Past - m0) / microsPerPixel.get();

    // like the scroll pane target position, the past region is always moving at a predictable rate,
    // so we set its initial position as well as animation its target, which smooths over some
    // jumpiness caused by adding or removing segments to the list.
    timelineRegion1Past.setWidth(pastTimelineWidth - MILLIS_PER_MICRO * refreshTimelineMillis / microsPerPixel.get());
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion1Past.widthProperty(), pastTimelineWidth)));

    // the rest of these widths are always animated
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion2Ship.widthProperty(), (m2Ship - m1Past) / microsPerPixel.get())));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion3Dub.widthProperty(), (m3Dub - m2Ship) / microsPerPixel.get())));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion4Craft.widthProperty(), (m4Craft - m3Dub) / microsPerPixel.get())));

    // auto-scroll if enabled, animating to the scroll pane position
    if (fabricationService.followPlaybackProperty().getValue() && 0 < segmentListView.getWidth()) {
      var extraHorizontalPixels = Math.max(0, segmentListView.getWidth() - scrollPane.getWidth());
      var targetOffsetHorizontalPixels = Math.max(0, pastTimelineWidth - autoScrollBehindPixels);

      if (fabricationService.isOutputModeSync().getValue()) {
        // the scroll pane is always moving at a predictable rate,
        // so we set its initial position as well as animation its target, which smooths over some
        // jumpiness caused by adding or removing segments to the list.
        scrollPane.setHvalue((targetOffsetHorizontalPixels - ((MILLIS_PER_MICRO * refreshTimelineMillis) / microsPerPixel.get())) / extraHorizontalPixels);
        scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
          new KeyValue(scrollPane.hvalueProperty(), targetOffsetHorizontalPixels / extraHorizontalPixels)));
      } else {
        scrollPane.setHvalue(targetOffsetHorizontalPixels / extraHorizontalPixels);
      }
    }

    // play the next leg of the animation timeline
    scrollPaneAnimationTimeline.play();
  }

  private void stopTimelineAnimation() {
    if (Objects.nonNull(refreshTimeline)) {
      refreshTimeline.stop();
    }
  }
}
