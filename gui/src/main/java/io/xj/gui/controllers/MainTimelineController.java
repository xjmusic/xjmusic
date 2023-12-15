// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;
import io.xj.nexus.work.WorkState;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class MainTimelineController extends ScrollPane implements ReadyAfterBootController {
  private static final Logger LOG = LoggerFactory.getLogger(MainTimelineController.class);
  private static final int NO_ID = -1;
  private static final double DEMO_BUTTON_HEIGHT_LEFTOVER = 168.0;
  private static final double DEMO_BUTTON_SPACING = 10.0;
  private static final double DEMO_BUTTON_MARGIN = 30.0;
  private static final double ACTIVE_SHIP_REGION_WIDTH = 5.0;
  private static final float DEFAULT_MICROS_PER_PIXEL = 25000;
  private final int segmentMinWidth;
  private final int segmentHorizontalSpacing;
  private final int segmentDisplayChoiceHashRecheckLimit;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final MainTimelineSegmentFactory segmentFactory;
  final long refreshTimelineMillis;
  final Timeline scrollPaneAnimationTimeline = new Timeline();
  final SimpleDoubleProperty demoImageWidth = new SimpleDoubleProperty();
  final SimpleDoubleProperty demoImageHeight = new SimpleDoubleProperty();

  // Keep track of Displayed Segments (DS), keyed by id, so we can update them in place
  final Map<Integer, DisplayedSegment> dsMap = new ConcurrentHashMap<>();

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
  Rectangle timelineRegion3Dub;

  @FXML
  Rectangle timelineRegion4Craft;

  @FXML
  HBox segmentListView;

  @Nullable
  Timeline refreshTimeline;

  public MainTimelineController(
    @Value("${gui.timeline.refresh.millis}") Integer refreshTimelineMillis,
    @Value("${gui.timeline.segment.hash.recheck.limit}") Integer segmentDisplayChoiceHashRecheckLimit,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService,
    MainTimelineSegmentFactory segmentFactory
  ) {
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshTimelineMillis = refreshTimelineMillis;
    this.segmentDisplayChoiceHashRecheckLimit = segmentDisplayChoiceHashRecheckLimit;
    this.segmentFactory = segmentFactory;
    this.segmentHorizontalSpacing = segmentSpacingHorizontal;
    this.segmentMinWidth = segmentWidthMin;
  }

  @Override
  public void onStageReady() {
    segmentListView.setSpacing(segmentHorizontalSpacing);
    segmentListView.setPadding(new Insets(0, segmentMinWidth * 3, 0, segmentHorizontalSpacing));

    segmentListView.paddingProperty().bind(scrollPane.widthProperty().map(width -> new Insets(0, width.doubleValue(), 0, segmentHorizontalSpacing)));

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

    resetTimeline();
  }

  @Override
  public void onStageClose() {
    stopTimelineAnimation();
  }

  @FXML
  public void handleDemoPlayBump(MouseEvent ignored) {
    fabricationService.handleDemoPlay("bump_deep");
  }

  @FXML
  public void handleDemoPlaySlaps(MouseEvent ignored) {
    fabricationService.handleDemoPlay("slaps_lofi");
  }

  @FXML
  public void handleDemoPlaySpace(MouseEvent ignored) {
    fabricationService.handleDemoPlay("space_flow");
  }

  /**
   Called when the fabrication status is updated.

   @param status the new status
   */
  private void handleUpdateFabricationStatus(WorkState status) {
    try {
      switch (status) {
        case Standby -> resetTimeline();
        case Active -> startTimelineAnimation();
        case Done, Cancelled, Failed -> stopTimelineAnimation();
      }
    } catch (Exception e) {
      LOG.error("Error handling fabrication status updated to {}", status, e);
    }
  }

  /**
   Called to reset the timeline (segment list).
   */
  private void resetTimeline() {
    final KeyFrame kf1 = new KeyFrame(Duration.millis(0), e -> {
      dsMap.clear();
      segmentListView.getChildren().clear();
      scrollPane.setHvalue(0);
      timelineRegion1Past.setWidth(0);
      timelineRegion2Ship.setWidth(ACTIVE_SHIP_REGION_WIDTH);
      timelineRegion3Dub.setWidth(0);
      timelineRegion4Craft.setWidth(0);
    });
    final KeyFrame kf2 = new KeyFrame(Duration.millis(200), e -> {
      segmentListView.layout();
      scrollPane.layout();
      segmentPositionRow.layout();
    });
    final Timeline timeline = new Timeline(kf1, kf2);
    Platform.runLater(timeline::play);
  }

  /**
   Called to start the timeline animation.
   */
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

  /**
   Called to stop the timeline animation.
   */
  private void stopTimelineAnimation() {
    if (Objects.nonNull(refreshTimeline)) {
      refreshTimeline.stop();
    }
  }

  /**
   Called to update the timeline (segment list).
   */
  private void updateTimeline(ActionEvent ignored) {
    if (fabricationService.isEmpty()) {
      dsMap.clear();
      segmentListView.getChildren().clear();
      return;
    }

    // If we aren't active, none of the rest of this matters
    if (!fabricationService.isStatusActive().getValue()) {
      return;
    }

    // get the current first index of the view
    var viewStartIndex = Math.max(0, fabricationService.getSegmentAtShipOutput().map((s) -> s.getId() - 1).orElse(NO_ID));

    // Fresh Segment (FS) map keyed by id
    // get updated segments and compute updated first id (to clean up segments before that id)
    var fsMap = fabricationService.getSegments(viewStartIndex).stream()
      .collect(Collectors.toMap(Segment::getId, (s) -> s, (s1, s2) -> s1, ConcurrentHashMap::new));

    // we will update the segments in place as efficiently as possible
    // get the fresh first and last ids of the current and fresh segments
    int freshFirstId = fsMap.values().stream().min(Comparator.comparing(Segment::getId)).map(Segment::getId).orElse(NO_ID);

    // remove segments from the beginning of the list if their id is less than the updated first id
    int firstId;
    while (!dsMap.isEmpty()) {
      firstId = dsMap.keySet().stream().min(Comparator.comparingInt((id) -> id)).orElse(NO_ID);
      if (NO_ID == firstId || firstId >= freshFirstId)
        break;
      dsMap.remove(firstId);
      segmentListView.getChildren().remove(0);
    }

    // add current segments to end of list if their id is greater than the existing last id
    int currentLastId = dsMap.keySet().stream().max(Comparator.comparingInt((i) -> i)).orElse(NO_ID);
    for (Segment freshSegment : fsMap.values()) {
      if (freshSegment.getId() > currentLastId) {
        dsMap.put(freshSegment.getId(), new DisplayedSegment(freshSegment));
        segmentListView.getChildren().add(segmentFactory.create(freshSegment, segmentMinWidth));
      }
    }

    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    var limit = Math.min(dsMap.size(), fsMap.size());
    for (var i = 0; i < limit; i++)
      if (Objects.nonNull(fsMap.get(i)) &&
        Objects.nonNull(dsMap.get(fsMap.get(i).getId())) &&
        dsMap.get(fsMap.get(i).getId()).isSameButUpdated(fsMap.get(i))) {
        dsMap.get(fsMap.get(i).getId()).update(fsMap.get(i));
        segmentListView.getChildren().set(i, segmentFactory.create(fsMap.get(i), segmentMinWidth));
      }


    // Recompute the width of the timeline
    segmentListView.layout();
    scrollPane.layout();

    // Reset the animation timeline
    scrollPaneAnimationTimeline.stop();
    scrollPaneAnimationTimeline.getKeyFrames().clear();

    // Build a list of displayed segments in order of their id
    var dsList = dsMap.keySet().stream().sorted().map(dsMap::get).toList();

    // marker 0 is the beginAtChainMicros of the first displayed segment
    var m0 = dsList.isEmpty() ? 0 : dsList.get(0).getBeginAtChainMicros();

    // markers 1-3 are the shippedToChainMicros, dubbedToChainMicros, and craftedToChainMicros
    var m1Past = fabricationService.getShippedToChainMicros().orElse(m0);
    var m3Dub = fabricationService.getDubbedToChainMicros().orElse(m1Past);
    var m4Craft = fabricationService.getCraftedToChainMicros().orElse(m3Dub);

    // TODO width of each region must be computed based on the actual displayed segments
    var w1Past = dsList.stream().filter((ds) -> ds.getBeginAtChainMicros() < m1Past).mapToDouble((ds) -> (ds.getBeginAtChainMicros() - m0) / DEFAULT_MICROS_PER_PIXEL).sum();
    var w2Ship = dsList.stream().filter((ds) -> ds.getBeginAtChainMicros() < m3Dub).mapToDouble((ds) -> (ds.getBeginAtChainMicros() - m1Past) / DEFAULT_MICROS_PER_PIXEL).sum();
    var w3Dub = dsList.stream().filter((ds) -> ds.getBeginAtChainMicros() < m4Craft).mapToDouble((ds) -> (ds.getBeginAtChainMicros() - m3Dub) / DEFAULT_MICROS_PER_PIXEL).sum();

    // In sync output, like the scroll pane target position, the past region is always moving at a predictable rate,
    // so we set its initial position as well as animation its target, which smooths over some
    // jumpiness caused by adding or removing segments to the list.
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion1Past.widthProperty(), w1Past)));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion3Dub.widthProperty(), w2Ship)));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion4Craft.widthProperty(), w3Dub)));

    // auto-scroll if enabled, animating to the scroll pane position
    if (fabricationService.followPlaybackProperty().getValue() && 0 < segmentListView.getWidth()) {
      var extraHorizontalPixels = Math.max(0, segmentListView.getWidth() - scrollPane.getWidth());
      var targetOffsetHorizontalPixels = Math.max(0, w1Past - segmentMinWidth);

      // in sync output, the scroll pane is always moving at a predictable rate,
      // so we set its initial position as well as animation its target, which smooths over some
      // jumpiness caused by adding or removing segments to the list.
      scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
        new KeyValue(scrollPane.hvalueProperty(), targetOffsetHorizontalPixels / extraHorizontalPixels)));
    }

    // play the next leg of the animation timeline
    scrollPaneAnimationTimeline.play();
  }

  private class DisplayedSegment {
    private final AtomicReference<Segment> segment = new AtomicReference<>();
    private final AtomicReference<String> choiceHash = new AtomicReference<>();
    private final AtomicInteger hashRecheckCount = new AtomicInteger(0);

    DisplayedSegment(Segment segment) {
      this.segment.set(segment);
      this.choiceHash.set(fabricationService.getChoiceHash(segment));
    }

    public boolean isSameButUpdated(Segment segment) {
      // Before worrying about the choice hash, test if the segment updated time is updated
      if (SegmentUtils.isSameButUpdated(this.segment.get(), segment))
        return true;

      // For performance, limit how many times we recheck the choice hash
      if (!(hashRecheckCount.get() < segmentDisplayChoiceHashRecheckLimit))
        return false;
      hashRecheckCount.incrementAndGet();
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
