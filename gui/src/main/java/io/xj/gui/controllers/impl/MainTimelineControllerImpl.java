// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.impl;

import io.xj.gui.controllers.MainTimelineController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMessageType;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.persistence.SegmentUtils;
import io.xj.nexus.util.FormatUtils;
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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.StringUtils.formatStackTrace;
import static io.xj.nexus.model.Segment.DELTA_UNLIMITED;

@Service
public class MainTimelineControllerImpl extends ScrollPane implements ReadyAfterBootController, MainTimelineController {
  private static final Logger LOG = LoggerFactory.getLogger(MainTimelineControllerImpl.class);
  private static final int NO_ID = -1;
  private static final double DEMO_BUTTON_HEIGHT_LEFTOVER = 168.0;
  private static final double DEMO_BUTTON_SPACING = 10.0;
  private static final double DEMO_BUTTON_MARGIN = 30.0;
  private static final double ACTIVE_SHIP_REGION_WIDTH = 5.0;
  private static final long MILLIS_PER_MICRO = 1000L;
  static final int SEGMENT_PROPERTY_ROW_MIN_HEIGHT = 60;
  static final int SEGMENT_CONTAINER_PADDING_VERTICAL = 10;
  static final int SEGMENT_CONTAINER_PADDING_HORIZONTAL = 10;

  static final int CHORD_POSITION_WIDTH = 42;
  static final int SEGMENT_SECTION_VERTICAL_MARGIN = 30;
  private final int segmentWidth;
  private final int segmentGutter;
  private final int segmentDisplayChoiceHashRecheckLimit;
  private final int displaySegmentsBeforeShip;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final LabService labService;
  final long refreshTimelineMillis;
  final Timeline scrollPaneAnimationTimeline = new Timeline();
  final SimpleDoubleProperty demoImageWidth = new SimpleDoubleProperty();
  final SimpleDoubleProperty demoImageHeight = new SimpleDoubleProperty();

  // Keep track of Displayed Segments (DS), keyed by id, so we can update them in place
  final List<DisplayedSegment> ds = new ArrayList<>();

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

  public MainTimelineControllerImpl(
    @Value("${gui.timeline.refresh.millis}") Integer refreshTimelineMillis,
    @Value("${gui.timeline.segment.hash.recheck.limit}") Integer segmentDisplayChoiceHashRecheckLimit,
    @Value("${gui.timeline.segment.spacing.horizontal}") Integer segmentSpacingHorizontal,
    @Value("${gui.timeline.segment.width.min}") Integer segmentWidthMin,
    @Value("${gui.timeline.display.segments.before.now}") int displaySegmentsBeforeNow,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    LabService labService
  ) {
    this.displaySegmentsBeforeShip = displaySegmentsBeforeNow;
    this.ac = ac;
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.refreshTimelineMillis = refreshTimelineMillis;
    this.segmentDisplayChoiceHashRecheckLimit = segmentDisplayChoiceHashRecheckLimit;
    this.segmentGutter = segmentSpacingHorizontal;
    this.segmentWidth = segmentWidthMin;
  }

  @Override
  public void onStageReady() {
    segmentListView.setSpacing(segmentGutter);
    segmentListView.setPadding(new Insets(0, segmentWidth * 3, 0, segmentGutter));

    segmentListView.paddingProperty().bind(scrollPane.widthProperty().map(width -> new Insets(0, width.doubleValue(), 0, segmentGutter)));

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
    scrollPaneAnimationTimeline.stop();
    Platform.runLater(() -> {
      ds.clear();
      segmentListView.getChildren().clear();
      scrollPane.setHvalue(0);
      timelineRegion1Past.setWidth(0);
      timelineRegion2Ship.setWidth(ACTIVE_SHIP_REGION_WIDTH);
      timelineRegion3Dub.setWidth(0);
      timelineRegion4Craft.setWidth(0);
      segmentListView.layout();
      scrollPane.layout();
      segmentPositionRow.layout();
    });
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
      ds.clear();
      segmentListView.getChildren().clear();
      return;
    }

    // If we aren't active, none of the rest of this matters
    if (!fabricationService.isStatusActive().getValue()) {
      return;
    }

    // get the current first index of the view
    var viewStartIndex = Math.max(0, fabricationService.getSegmentAtShipOutput().map((s) -> s.getId() - displaySegmentsBeforeShip).orElse(NO_ID));

    // Fresh Segment (FS) list
    // get updated segments and compute updated first id (to clean up segments before that id)
    var fs = fabricationService.getSegments(viewStartIndex);

    // we will update the segments in place as efficiently as possible
    // get the fresh first and last ids of the current and fresh segments
    int freshFirstId = fs.isEmpty() ? NO_ID : fs.get(0).getId();

    // remove segments from the beginning of the list if their id is less than the updated first id
    int dsFirstId;
    while (!ds.isEmpty()) {
      dsFirstId = ds.get(0).getId();
      if (NO_ID == dsFirstId || dsFirstId >= freshFirstId)
        break;
      ds.remove(0);
      segmentListView.getChildren().remove(0);
    }

    // remove segments from the end of the displayed segment list if the fresh segment list is shorter
    while (ds.size() > fs.size()) {
      ds.remove(ds.size() - 1);
      segmentListView.getChildren().remove(segmentListView.getChildren().size() - 1);
    }

    // add current segments to end of list if their id is greater than the existing last id
    int dsLastId = ds.isEmpty() ? NO_ID : ds.get(ds.size() - 1).getId();
    for (Segment freshSegment : fs) {
      if (freshSegment.getId() > dsLastId) {
        ds.add(new DisplayedSegment(freshSegment));
        segmentListView.getChildren().add(buildSegment(freshSegment, segmentWidth));
      }
    }

    // iterate through all in segments, and update if the updated at time has changed from the source matching that id
    var didOverride = fabricationService.getAndResetDidOverride();
    var limit = Math.min(ds.size(), fs.size());
    for (var i = 0; i < limit; i++)
      if (didOverride || ds.get(i).isSameButUpdated(fs.get(i))) {
        ds.get(i).update(fs.get(i));
        segmentListView.getChildren().set(i, buildSegment(fs.get(i), segmentWidth));
      }

    // Recompute the width of the timeline
    segmentListView.layout();
    scrollPane.layout();

    // Reset the animation timeline
    scrollPaneAnimationTimeline.stop();
    scrollPaneAnimationTimeline.getKeyFrames().clear();

    // marker 0 is the beginAtChainMicros of the first displayed segment
    var m0 = ds.isEmpty() ? 0 : ds.get(0).getBeginAtChainMicros();

    // markers 1-3 are the shippedToChainMicros, dubbedToChainMicros, and craftedToChainMicros
    var m1Now = fabricationService.getShippedToChainMicros().orElse(m0);
    var m2Dub = fabricationService.getDubbedToChainMicros().orElse(m1Now);
    var m3Craft = fabricationService.getCraftedToChainMicros().orElse(m2Dub);

    // Compute the delta pixels per timeline refresh
    var deltaPixelsPerTimelineRefresh = computeCurrentDeltaPixelsPerTimelineRefresh(ds, m1Now);

    // position of each region must be computed based on the actual displayed segments
    // position of each region is the timeline X of the marker plus the delta pixels per timeline refresh
    var p1Now = computeTimelineX(ds, m1Now) + deltaPixelsPerTimelineRefresh;
    var p2Dub = computeTimelineX(ds, m2Dub) + deltaPixelsPerTimelineRefresh;
    var p3Craft = computeTimelineX(ds, m3Craft) + deltaPixelsPerTimelineRefresh;

    // width based on positions
    var w2Dub = p2Dub - p1Now;
    var w3Craft = p3Craft - p2Dub;

    // In sync output, like the scroll pane target position, the past region is always moving at a predictable rate,
    // so we set its initial position as well as animation its target, which smooths over some
    // jumpiness caused by adding or removing segments to the list.
    timelineRegion1Past.setWidth(p1Now - deltaPixelsPerTimelineRefresh);
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion1Past.widthProperty(), p1Now)));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion3Dub.widthProperty(), w2Dub)));
    scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
      new KeyValue(timelineRegion4Craft.widthProperty(), w3Craft)));

    // auto-scroll if enabled, animating to the scroll pane position
    if (fabricationService.followPlaybackProperty().getValue() && 0 < segmentListView.getWidth()) {
      var extraHorizontalPixels = Math.max(0, segmentListView.getWidth() - scrollPane.getWidth());
      var targetOffsetHorizontalPixels = Math.max(0, p1Now - segmentWidth);

      // in sync output, the scroll pane is always moving at a predictable rate,
      // so we set its initial position as well as animation its target, which smooths over some
      // jumpiness caused by adding or removing segments to the list.
      scrollPane.setHvalue((targetOffsetHorizontalPixels - deltaPixelsPerTimelineRefresh) / extraHorizontalPixels);
      scrollPaneAnimationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(refreshTimelineMillis),
        new KeyValue(scrollPane.hvalueProperty(), targetOffsetHorizontalPixels / extraHorizontalPixels)));
    }

    // play the next leg of the animation timeline
    scrollPaneAnimationTimeline.play();
  }

  /**
   Compute the delta pixels per timeline refresh based on the current displayed segments and the given chain micros.

   @param dsList      the list of displayed segments, in order of id
   @param chainMicros the chain micros for which compute the delta pixels per timeline refresh
   @return the delta pixels per timeline refresh
   */
  private long computeCurrentDeltaPixelsPerTimelineRefresh(List<DisplayedSegment> dsList, long chainMicros) {
    return dsList.stream().filter(ds -> ds.isIntersecting(chainMicros)).findFirst()
      .map(displayedSegment -> segmentWidth * refreshTimelineMillis / (displayedSegment.getDurationMicros() / MILLIS_PER_MICRO))
      .orElse(0L);
  }

  /**
   compute timeline X of any given chain micros based on the displayed segments

   @param dsList      the list of displayed segments, in order of id
   @param chainMicros the chain micros for which compute the timeline X
   @return the timeline X
   */
  private int computeTimelineX(List<DisplayedSegment> dsList, long chainMicros) {
    return
      segmentGutter +
        dsList.stream()
          .filter((ds) -> ds.isSegmentReady() && ds.getBeginAtChainMicros() < chainMicros)
          .mapToInt((ds) -> {
            if (ds.getEndAtChainMicros() < chainMicros)
              return segmentWidth + segmentGutter;
            else
              return (int) (ds.computePositionRatio(chainMicros) * segmentWidth);
          })
          .sum();
  }

  /**
   A displayed segment is a segment that is currently displayed in the timeline.
   It is identified by its id, and it is updated in place when the segment is updated.
   It is also updated in place when the segment is unchanged but its choice hash has changed.
   */
  private class DisplayedSegment {
    private final AtomicReference<Segment> segment = new AtomicReference<>();
    private final AtomicReference<String> choiceHash = new AtomicReference<>();
    private final AtomicInteger hashRecheckCount = new AtomicInteger(0);

    DisplayedSegment(Segment segment) {
      update(segment);
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
        !Objects.equals(this.choiceHash.get(), fabricationService.computeChoiceHash(segment));
    }

    public void update(Segment segment) {
      this.segment.set(segment);
      this.choiceHash.set(fabricationService.computeChoiceHash(segment));
      this.hashRecheckCount.set(0);
    }

    public long getBeginAtChainMicros() {
      if (!isSegmentReady()) return 0;
      return segment.get().getBeginAtChainMicros();
    }

    public long getEndAtChainMicros() {
      if (!isSegmentReady()) return 0;
      // noinspection DataFlowIssue
      return segment.get().getBeginAtChainMicros() + segment.get().getDurationMicros();
    }

    public boolean isIntersecting(long chainMicros) {
      if (!isSegmentReady()) return false;
      return segment.get().getBeginAtChainMicros() <= chainMicros && getEndAtChainMicros() > chainMicros;
    }

    public double computePositionRatio(long chainMicros) {
      if (!isSegmentReady()) return 0;
      //noinspection DataFlowIssue
      return (double) (chainMicros - segment.get().getBeginAtChainMicros()) / segment.get().getDurationMicros();
    }

    public long getDurationMicros() {
      if (!isSegmentReady())
        return 0;
      //noinspection DataFlowIssue
      return segment.get().getDurationMicros();
    }

    private boolean isSegmentReady() {
      return Objects.nonNull(segment.get()) && Objects.nonNull(segment.get().getBeginAtChainMicros()) && Objects.nonNull(segment.get().getDurationMicros());
    }

    public int getId() {
      return segment.get().getId();
    }
  }

  /**
   Every time we need a segment, build it from scratch

   @param segment from which to compute JavaFX node
   @param width   minimum width of the segment
   @return JavaFX node
   */
  public Node buildSegment(Segment segment, int width) {
    try {
      var box = new VBox();
      box.setMinWidth(width);
      box.setMaxWidth(width);
      box.getStyleClass().add("main-timeline-segment");
      box.setMaxHeight(Double.MAX_VALUE);
      box.setPadding(new Insets(SEGMENT_CONTAINER_PADDING_VERTICAL, SEGMENT_CONTAINER_PADDING_HORIZONTAL, SEGMENT_CONTAINER_PADDING_VERTICAL, SEGMENT_CONTAINER_PADDING_HORIZONTAL));
      VBox.setVgrow(box, Priority.ALWAYS);
      int innerMinWidth = width - SEGMENT_CONTAINER_PADDING_HORIZONTAL * 2;
      int innerFullWidth = width - SEGMENT_CONTAINER_PADDING_HORIZONTAL * 2;
      box.getChildren().addAll(
        computeSegmentSectionHeaderNode(segment, innerMinWidth),
        computeSegmentSectionPropertiesNode(segment, innerMinWidth),
        computeSegmentSectionMemesChordsNode(segment, innerMinWidth),
        computeSegmentSectionChoicesNode(segment, innerFullWidth),
        computeSegmentSectionMessageListNode(segment, innerFullWidth),
        computeSegmentSectionMetasNode(segment, innerFullWidth));
      return box;

    } catch (Exception e) {
      LOG.warn("Failed to update choices because {}!\n\n{}", e.getMessage(), formatStackTrace(e));
      return new VBox();
    }
  }

  /**
   Segment section: Messages
   */
  private Node computeSegmentSectionMessageListNode(Segment segment, int width) {
    var messages = fabricationService.getSegmentMessages(segment);
    var col = new VBox();
    col.setPadding(new Insets(0, 0, 0, 10));
    // info
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.INFO)
      .map(m -> computeSegmentSectionMessageNode(m, width))
      .toList());
    // warning
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.WARNING)
      .map(m -> computeSegmentSectionMessageNode(m, width))
      .toList());
    // error
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.ERROR)
      .map(m -> computeSegmentSectionMessageNode(m, width))
      .toList());
    //
    var pane = new AnchorPane();
    pane.getChildren().add(computeLabeledPropertyNode("Messages", col, width, SEGMENT_SECTION_VERTICAL_MARGIN * 2));
    return pane;
  }

  private Node computeSegmentSectionMessageNode(SegmentMessage message, int width) {
    // type
    var type = new Label();
    type.getStyleClass().add("segment-message-type");
    type.getStyleClass().add("segment-message-" + message.getType().toString().toLowerCase());
    type.setPadding(new Insets(5, 0, 0, 0));
    type.setWrapText(true);
    type.setMaxWidth(width - 20);
    type.setText(message.getType().toString().toUpperCase());
    // body
    var body = new Text();
    body.getStyleClass().add("segment-message-body");
    body.setText(message.getBody());
    body.setWrappingWidth(width - 20);
    // message
    var box = new VBox();
    box.getStyleClass().add("segment-message");
    box.getChildren().add(type);
    box.getChildren().add(body);
    return box;
  }

  /**
   Segment section: Metas
   */
  private Node computeSegmentSectionMetasNode(Segment segment, int width) {
    var metas = fabricationService.getSegmentMetas(segment);
    var col = new VBox();
    col.setPadding(new Insets(0, 0, 0, 10));
    col.getChildren().addAll(metas.stream()
      .map(m -> computeSegmentSectionMetaNode(m, width))
      .toList());
    //
    var pane = new AnchorPane();
    pane.getChildren().add(computeLabeledPropertyNode("Metas", col, width, SEGMENT_SECTION_VERTICAL_MARGIN));
    return pane;
  }

  private Node computeSegmentSectionMetaNode(SegmentMeta meta, int width) {
    // key
    var key = new Label();
    key.getStyleClass().add("segment-meta-key");
    key.setPadding(new Insets(5, 0, 0, 0));
    key.setWrapText(true);
    key.setMaxWidth(width - 20);
    key.setText(meta.getKey());
    // type
    var value = new Text();
    value.getStyleClass().add("segment-meta-value");
    value.setText(meta.getValue());
    value.setWrappingWidth(width - 20);
    // meta
    var col = new VBox();
    col.getStyleClass().add("segment-meta");
    col.getChildren().add(key);
    col.getChildren().add(value);
    //
    var pane = new AnchorPane();
    pane.getChildren().add(col);
    return pane;
  }

  /**
   Segment section: Choices
   */
  private Node computeSegmentSectionChoicesNode(Segment segment, int width) {
    var col = new VBox();
    col.setPrefWidth(width);
    col.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    col.setMaxHeight(Double.MAX_VALUE);
    col.setPadding(new Insets(20, 0, 0, 0));
    VBox.setVgrow(col, Priority.ALWAYS);
    var choices = fabricationService.getSegmentChoices(segment);
    col.getChildren().add(computeChoiceListNodes(segment, "Macro", choices.stream().filter((choice) -> ProgramType.Macro == choice.getProgramType()).toList(), true, false, false));
    col.getChildren().add(computeChoiceListNodes(segment, "Main", choices.stream().filter((choice) -> ProgramType.Main == choice.getProgramType()).toList(), true, false, false));
    col.getChildren().add(computeChoiceListNodes(segment, "Beat", choices.stream().filter((choice) -> ProgramType.Beat == choice.getProgramType()).toList(), false, true, false));
    col.getChildren().add(computeChoiceListNodes(segment, "Detail", choices.stream().filter((choice) -> ProgramType.Detail == choice.getProgramType()).toList(), true, false, false));
    col.getChildren().add(computeChoiceListNodes(segment, "Perc Loop", choices.stream().filter((choice) ->
      InstrumentType.Percussion == choice.getInstrumentType() && InstrumentMode.Loop == choice.getInstrumentMode()).toList(), false, false, true));
    col.getChildren().add(computeChoiceListNodes(segment, "Hook", choices.stream().filter((choice) -> InstrumentType.Hook == choice.getInstrumentType()).toList(), false, false, true));
    col.getChildren().add(computeChoiceListNodes(segment, "Transition", choices.stream().filter((choice) -> InstrumentMode.Transition == choice.getInstrumentMode()).toList(), false, false, true));
    col.getChildren().add(computeChoiceListNodes(segment, "Background", choices.stream().filter((choice) -> InstrumentMode.Background == choice.getInstrumentMode()).toList(), false, false, true));
    col.getChildren().add(computeChoiceListNodes(segment, "Chord", choices.stream().filter((choice) -> InstrumentMode.Chord == choice.getInstrumentMode()).toList(), false, false, true));
    //
    var pane = new AnchorPane();
    pane.getChildren().add(col);
    return pane;
  }

  /**
   Segment section: Memes, Chords
   */
  private Node computeSegmentSectionMemesChordsNode(Segment segment, int width) {
    var row = new HBox();
    row.setPrefWidth(width);
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode("Memes", computeMemeListNode(segment), (int) (width * 0.45), 0));
    row.getChildren().add(computeLabeledPropertyNode("Chords", computeChordListNode(segment), (int) (width * 0.55), 0));
    //
    var pane = new AnchorPane();
    pane.getChildren().add(row);
    return pane;
  }

  /**
   Segment section: Beats, Density, Tempo, Key
   */
  private Node computeSegmentSectionPropertiesNode(Segment segment, int width) {
    var row = new HBox();
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(fabricationService.formatTotalBars(segment, segment.getTotal()), FormatUtils.formatTimeFromMicros(segment.getDurationMicros()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Density", String.format("%.2f", segment.getDensity()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Tempo", FormatUtils.formatMinDecimal(segment.getTempo()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Key", segment.getKey(), width / 4));
    //
    var pane = new AnchorPane();
    pane.getChildren().add(row);
    return pane;
  }

  /**
   Segment section: offset, begin-at micros, delta, type
   */
  Node computeSegmentSectionHeaderNode(Segment segment, int width) {
    var row = new HBox();
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(String.format("[%d]", segment.getId()), FormatUtils.formatTimeFromMicros(segment.getBeginAtChainMicros()), width / 2));
    row.getChildren().add(computeLabeledPropertyNode(fabricationService.formatPositionBarBeats(segment, Double.valueOf(segment.getDelta())), segment.getType().toString(), width / 2));
    //
    var pane = new AnchorPane();
    pane.getChildren().add(row);
    return pane;
  }

  @SuppressWarnings("SameParameterValue")
  Node computeLabeledPropertyNode(String label, String value, int minWidth) {
    return computeLabeledPropertyNode(label, computeValueNode(value), minWidth, 0);
  }

  Node computeChordListNode(Segment segment) {
    var box = new VBox();
    box.getChildren().setAll(
      fabricationService.getSegmentChords(segment)
        .stream()
        .sorted(Comparator.comparing(SegmentChord::getPosition))
        .map(chord -> {
          // position
          var position = new Label();
          position.setText(fabricationService.formatPositionBarBeats(segment, chord.getPosition()));
          position.setPrefWidth(CHORD_POSITION_WIDTH);
          position.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
          position.getStyleClass().add("chord-position");
          position.setTextOverrun(OverrunStyle.CLIP);
          // name
          var name = new Text();
          name.setText(chord.getName());
          name.getStyleClass().add("chord-name");
          // horizontal box
          var row = new HBox();
          row.getChildren().add(position);
          row.getChildren().add(name);
          return row;
        })
        .toList());
    return box;
  }

  Node computeMemeListNode(Segment segment) {
    var box = new VBox();
    box.getChildren().setAll(
      fabricationService.getSegmentMemes(segment)
        .stream()
        .sorted(Comparator.comparing(SegmentMeme::getName))
        .map(meme -> {
          var text = new Text();
          text.setText(meme.getName());
          text.getStyleClass().add("meme");
          return text;
        })
        .toList());
    return box;
  }

  Node computeValueNode(Object value) {
    var node = new Text();
    node.setText(Objects.toString(value));
    node.getStyleClass().add("value");
    return node;
  }

  Node computeLabeledPropertyNode(Object label, Node child, int width, int topPadding) {
    var labelNode = new Label();
    labelNode.setText(Objects.toString(label));
    labelNode.setMinWidth(width);
    labelNode.setMaxWidth(width);
    labelNode.getStyleClass().add("label");
    //
    var col = new VBox();
    col.setPadding(new Insets(topPadding, 10, 0, 0));
    col.getChildren().add(labelNode);
    col.getChildren().add(child);
    return col;
  }

  Node computeChoiceListNodes(Segment segment, String layerName, Collection<? extends SegmentChoice> choices, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group");
    // layer name
    var layerNameLabel = new Label();
    layerNameLabel.setText(layerName);
    layerNameLabel.getStyleClass().add("choice-group-name");
    box.getChildren().add(layerNameLabel);
    // choices
    choices
      .stream()
      .sorted(Comparator.comparing((c) ->
        String.format("%s_%s",
          Objects.nonNull(c.getInstrumentType()) ? c.getInstrumentType() : "",
          Objects.nonNull(c.getProgramType()) ? c.getProgramType() : "")))
      .forEach(choice -> {
        var choiceListItem = computeChoiceNode(segment, choice, showProgram, showProgramVoice, showArrangementPicks);
        box.getChildren().add(choiceListItem);
      });
    return box;
  }

  Node computeChoiceNode(Segment segment, SegmentChoice choice, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group-item");

    if ((Objects.nonNull(choice.getMute()) && choice.getMute()) ||
      (Objects.nonNull(choice.getDeltaIn()) && DELTA_UNLIMITED != choice.getDeltaIn() && segment.getDelta() < choice.getDeltaIn()) ||
      (Objects.nonNull(choice.getDeltaOut()) && DELTA_UNLIMITED != choice.getDeltaOut() && segment.getDelta() > choice.getDeltaOut()))
      box.getStyleClass().add("choice-group-item-muted");

    if (showProgram) {
      box.getChildren().add(fabricationService.computeProgramReferenceNode(choice.getProgramId(), choice.getProgramSequenceBindingId()));
    }

    if (showProgramVoice) {
      box.getChildren().add(fabricationService.computeProgramVoiceReferenceNode(choice.getProgramVoiceId()));
    }

    var instrumentBox = new HBox();
    instrumentBox.getStyleClass().add("choice-instrument");
    computeShowDeltaNode(segment, choice).ifPresent(instrumentBox.getChildren()::add);
    if (Objects.nonNull(choice.getInstrumentId())) {
      instrumentBox.getChildren().add(fabricationService.computeInstrumentReferenceNode(choice.getInstrumentId()));
    }
    box.getChildren().add(instrumentBox);

    if (showArrangementPicks) {
      box.getChildren().addAll(computeUniquePicks(choice).stream().flatMap(this::computeChoiceListItemPickNode).toList());
    }

    return box;
  }

  Optional<Node> computeShowDeltaNode(Segment segment, SegmentChoice choice) {
    if (ProgramType.Macro == choice.getProgramType() || ProgramType.Main == choice.getProgramType()) {
      return Optional.empty();
    }

    if (Objects.isNull(choice.getDeltaIn()) && Objects.isNull(choice.getDeltaOut())) {
      return Optional.empty();
    }
    var box = new HBox();
    box.getStyleClass().add("delta-container");
    if (Objects.nonNull(choice.getDeltaIn())) {
      var deltaIn = new Text();
      deltaIn.setText(computeChoiceDeltaValue(segment, choice.getDeltaIn()));
      deltaIn.getStyleClass().add("delta");
      deltaIn.getStyleClass().add("delta-in");
      box.getChildren().add(deltaIn);
    }
    var connector = new Text();
    connector.setText("-");
    connector.getStyleClass().add("delta");
    connector.getStyleClass().add("delta-connector");
    box.getChildren().add(connector);
    if (Objects.nonNull(choice.getDeltaOut())) {
      var deltaOut = new Text();
      deltaOut.setText(computeChoiceDeltaValue(segment, choice.getDeltaOut()));
      deltaOut.getStyleClass().add("delta");
      deltaOut.getStyleClass().add("delta-out");
      box.getChildren().add(deltaOut);
    }
    return Optional.of(box);
  }

  String computeChoiceDeltaValue(Segment segment, Integer value) {
    if (-1 == value) return "∞";
    return fabricationService.formatPositionBarBeats(segment, Double.valueOf(value));
  }

  Stream<Node> computeChoiceListItemPickNode(SegmentChoiceArrangementPick pick) {
    if (Objects.isNull(pick.getInstrumentAudioId()))
      return Stream.empty();
    return Stream.of(fabricationService.computeInstrumentAudioReferenceNode(pick.getInstrumentAudioId()));
  }

  /**
   Compute the unique (as in instrument audio reference) picks for the given choice

   @param choice for which to compute unique picks
   */
  Collection<SegmentChoiceArrangementPick> computeUniquePicks(SegmentChoice choice) {
    return fabricationService.getArrangements(choice)
      .stream().flatMap(arrangement -> fabricationService.getPicks(arrangement).stream())
      .filter(pick -> Objects.nonNull(pick.getInstrumentAudioId()))
      .collect(Collectors.toMap(SegmentChoiceArrangementPick::getInstrumentAudioId, pick -> pick, (pick1, pick2) -> pick2))
      .values();
  }
}
