// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.ActionEvent;
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

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
  private final Integer segmentDisplayHashRecheckLimit;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final long refreshTimelineMillis;
  final long refreshTimelineMicros;
  final SimpleFloatProperty microsPerPixel = new SimpleFloatProperty(0);
  final Timeline scrollPaneAnimationTimeline = new Timeline();
  final SimpleDoubleProperty demoImageWidth = new SimpleDoubleProperty();
  final SimpleDoubleProperty demoImageHeight = new SimpleDoubleProperty();

  // Keep track of Displayed Segments (DS) so we can update them in place
  final Map<Integer, DisplayedSegment> ds = new ConcurrentHashMap<>();

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
    @Value("${gui.timeline.segment.hash.recheck.limit}") Integer segmentDisplayHashRecheckLimit,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
    @Value("${gui.timeline.auto.scroll.behind.pixels}") Integer autoScrollBehindPixels,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService,
    MainTimelineSegmentFactory segmentFactory
  ) {
    this.ac = ac;
    this.autoScrollBehindPixels = autoScrollBehindPixels;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshTimelineMicros = refreshTimelineMillis * MILLIS_PER_MICRO;
    this.refreshTimelineMillis = refreshTimelineMillis;
    this.segmentDisplayHashRecheckLimit = segmentDisplayHashRecheckLimit;
    this.segmentFactory = segmentFactory;
    this.segmentHorizontalSpacing = segmentSpacingHorizontal;
    this.segmentMinWidth = segmentWidthMin;
  }

  @Override
  public void onStageReady() {
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

    fabricationService.statusProperty().addListener((ignored1, ignored2, status) -> handleUpdateFabricationStatus(status));
  }

  private void handleUpdateFabricationStatus(FabricationStatus status) {
    if (Objects.equals(FabricationStatus.Active, status)) {
      startTimelineAnimation();

    } else if (Objects.equals(FabricationStatus.Standby, status)) {
      stopTimelineAnimation();
      ds.clear();
      segmentListView.getChildren().clear();
      timelineRegion1Past.setWidth(0);
      timelineRegion2Ship.setWidth(0);
      timelineRegion3Dub.setWidth(0);
      timelineRegion4Craft.setWidth(0);
      scrollPane.setHvalue(0);
      segmentListView.layout();
      scrollPane.layout();

    } else {
      stopTimelineAnimation();
    }
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
  void updateTimeline(ActionEvent ignored) {
    if (fabricationService.isEmpty()) {
      ds.clear();
      segmentListView.getChildren().clear();
      return;
    }

    // If we aren't active, none of the rest of this matters
    if (!fabricationService.isStatusActive().getValue()) {
      return;
    }

    // get the current first index of the view
    var viewStartIndex = Math.max(0, fabricationService.getSegmentAtShipOutput().map((s) -> s.getId() - 1).orElse(NO_ID));

    // get updated segments and compute updated first id (to clean up segments before that id)
    var freshSegments = fabricationService.getSegments(viewStartIndex);

    // determine if the segment pixels-per-micro has changed, and we will re-render the whole list and return
    long updatedDurationMinMicros = SegmentUtils.getDurationMinMicros(freshSegments);
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
      ds.clear();
      segmentListView.getChildren().clear();
      for (Segment freshSegment : freshSegments) {
        ds.put(freshSegment.getId(), new DisplayedSegment(freshSegment));
        segmentListView.getChildren().add(segmentFactory.create(freshSegment, updatedMicrosPerPixel, segmentMinWidth, segmentHorizontalSpacing));
      }

    } else {
      // if the micros per pixel has not changed, we will update the segments in place as efficiently as possible
      // get the fresh first and last ids of the current and fresh segments
      int freshFirstId = freshSegments.stream().min(Comparator.comparing(Segment::getId)).map(Segment::getId).orElse(NO_ID);

      // remove segments from the beginning of the list if their id is less than the updated first id
      int firstId;
      while (ds.size() > 0) {
        firstId = ds.keySet().stream().min(Comparator.comparingInt((id) -> id)).orElse(NO_ID);
        if (NO_ID == firstId || firstId >= freshFirstId)
          break;
        ds.remove(firstId);
        segmentListView.getChildren().remove(0);
      }

      // add current segments to end of list if their id is greater than the existing last id
      int currentLastId = ds.keySet().stream().max(Comparator.comparingInt((i) -> i)).orElse(NO_ID);
      for (Segment freshSegment : freshSegments) {
        if (freshSegment.getId() > currentLastId) {
          ds.put(freshSegment.getId(), new DisplayedSegment(freshSegment));
          segmentListView.getChildren().add(segmentFactory.create(freshSegment, microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
        }
      }

      // iterate through all in segments, and update if the updated at time has changed from the source matching that id
      var limit = Math.min(ds.size(), freshSegments.size());
      for (var i = 0; i < limit; i++)
        if (ds.get(freshSegments.get(i).getId()).isSameButUpdated(freshSegments.get(i))) {
          ds.get(freshSegments.get(i).getId()).update(freshSegments.get(i));
          segmentListView.getChildren().set(i, segmentFactory.create(freshSegments.get(i), microsPerPixel.get(), segmentMinWidth, segmentHorizontalSpacing));
        }
    }

    // Recompute the width of the timeline
    segmentListView.layout();
    scrollPane.layout();

    // Reset the animation timeline
    scrollPaneAnimationTimeline.stop();
    scrollPaneAnimationTimeline.getKeyFrames().clear();

    // marker 0 is the beginAtChainMicros of the first displayed segment
    var m0 = ds.isEmpty() ? 0 :
      ds.keySet().stream().min(Comparator.comparingInt((id) -> id))
        .map(id -> ds.get(id).getBeginAtChainMicros()).orElse(0L) - segmentHorizontalSpacing;

    // other markers continue increasing from there
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

  private void startTimelineAnimation() {
    refreshTimeline = new Timeline(
      new KeyFrame(
        Duration.millis(refreshTimelineMillis),
        this::updateTimeline
      )
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
    refreshTimeline.setRate(1.0);
    refreshTimeline.play();
  }

  private void stopTimelineAnimation() {
    if (Objects.nonNull(refreshTimeline)) {
      refreshTimeline.stop();
    }
  }

  private class DisplayedSegment {
    private final AtomicReference<Segment> segment = new AtomicReference<>();
    private final AtomicReference<String> choiceHash = new AtomicReference<>();
    private final AtomicInteger recheckCount = new AtomicInteger(0);

    DisplayedSegment(Segment segment) {
      this.segment.set(segment);
      this.choiceHash.set(fabricationService.getChoiceHash(segment));
    }

    public Segment getSegment() {
      return segment.get();
    }

    public boolean isSameButUpdated(Segment segment) {
      if (!(recheckCount.getAndIncrement() < segmentDisplayHashRecheckLimit))
        return false;

      if (SegmentUtils.isSameButUpdated(this.segment.get(), segment))
        return true;

      return
        !Objects.equals(this.choiceHash.get(), fabricationService.getChoiceHash(segment));
    }

    public void update(Segment segment) {
      this.segment.set(segment);
      this.choiceHash.set(fabricationService.getChoiceHash(segment));
    }

    public Long getBeginAtChainMicros() {
      return segment.get().getBeginAtChainMicros();
    }
  }
}
