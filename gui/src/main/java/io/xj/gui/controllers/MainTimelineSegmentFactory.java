// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.models.SegmentOnTimeline;
import io.xj.gui.services.FabricationService;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.SegmentUtils;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.StringUtils.formatStackTrace;
import static io.xj.nexus.model.Segment.DELTA_UNLIMITED;

/**
 Used as a JavaFX node factory by {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
@Service
public class MainTimelineSegmentFactory {
  static final int SEGMENT_MIN_WIDTH = 240;
  static final int SEGMENT_ACTIVE_INDICATOR_HEIGHT = 6;
  static final int SEGMENT_PROPERTY_ROW_MIN_HEIGHT = 60;
  static final Logger LOG = LoggerFactory.getLogger(MainTimelineSegmentFactory.class);

  static final int CHORD_POSITION_WIDTH = 32;
  static final int SEGMENT_SECTION_VERTICAL_MARGIN = 30;
  final FabricationService fabricationService;

  public MainTimelineSegmentFactory(
    FabricationService fabricationService
  ) {
    this.fabricationService = fabricationService;
  }

  /**
   Every time we need a segment, build it from scratch

   @param segment               from which to compute JavaFX node
   @param outputSyncChainMicros to which to bind the output sync chain micros
   @return JavaFX node
   */
  public Node computeSegmentNode(SegmentOnTimeline segment, SimpleLongProperty outputSyncChainMicros) {
    try {
      var box = new VBox();
      box.getStyleClass().add("main-timeline-segment");
      box.setMaxHeight(Double.MAX_VALUE);
      box.setPadding(new Insets(10, 10, 10, 10));
      VBox.setVgrow(box, Priority.ALWAYS);
      box.getChildren().addAll(
        computeSegmentSectionHeaderNode(segment, outputSyncChainMicros),
        computeSegmentSectionPropertiesNode(segment),
        computeSegmentSectionMemesChordsNode(segment),
        computeSegmentSectionChoicesNode(segment),
        computeSegmentSectionMessageListNode(segment),
        computeSegmentSectionMetasNode(segment));
      return box;

    } catch (Exception e) {
      LOG.error("Failed to update choices because {}!\n\n{}", e.getMessage(), formatStackTrace(e));
      return new VBox();
    }
  }

  /**
   Segment section: Messages
   */
  private Node computeSegmentSectionMessageListNode(SegmentOnTimeline segment) {
    var messages = fabricationService.getSegmentMessages(segment.getSegment());
    var col = new VBox();
    col.setPadding(new Insets(0, 0, 0, 10));
    // info
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.INFO)
      .map(this::computeSegmentSectionMessageNode)
      .toList());
    // warning
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.WARNING)
      .map(this::computeSegmentSectionMessageNode)
      .toList());
    // error
    col.getChildren().addAll(messages.stream()
      .filter(message -> message.getType() == SegmentMessageType.ERROR)
      .map(this::computeSegmentSectionMessageNode)
      .toList());
    return computeLabeledPropertyNode("Messages", col, SEGMENT_MIN_WIDTH, SEGMENT_SECTION_VERTICAL_MARGIN * 2);
  }

  private Node computeSegmentSectionMessageNode(SegmentMessage message) {
    // type
    var type = new Label();
    type.getStyleClass().add("segment-message-type");
    type.getStyleClass().add("segment-message-" + message.getType().toString().toLowerCase());
    type.setPadding(new Insets(5, 0, 0, 0));
    type.setWrapText(true);
    type.setMaxWidth(SEGMENT_MIN_WIDTH - 20);
    type.setText(message.getType().toString().toUpperCase());
    // body
    var body = new Text();
    body.getStyleClass().add("segment-message-body");
    body.setText(message.getBody());
    body.setWrappingWidth(SEGMENT_MIN_WIDTH - 20);
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
  private Node computeSegmentSectionMetasNode(SegmentOnTimeline segment) {
    var metas = fabricationService.getSegmentMetas(segment.getSegment());
    var col = new VBox();
    col.setPadding(new Insets(0, 0, 0, 10));
    col.getChildren().addAll(metas.stream()
      .map(this::computeSegmentSectionMetaNode)
      .toList());
    return computeLabeledPropertyNode("Metas", col, SEGMENT_MIN_WIDTH, SEGMENT_SECTION_VERTICAL_MARGIN);
  }

  private Node computeSegmentSectionMetaNode(SegmentMeta meta) {
    // key
    var key = new Label();
    key.getStyleClass().add("segment-meta-key");
    key.setPadding(new Insets(5, 0, 0, 0));
    key.setWrapText(true);
    key.setMaxWidth(SEGMENT_MIN_WIDTH - 20);
    key.setText(meta.getKey());
    // type
    var value = new Text();
    value.getStyleClass().add("segment-meta-value");
    value.setText(meta.getValue());
    value.setWrappingWidth(SEGMENT_MIN_WIDTH - 20);
    // meta
    var box = new VBox();
    box.getStyleClass().add("segment-meta");
    box.getChildren().add(key);
    box.getChildren().add(value);
    return box;
  }

  /**
   Segment section: Choices
   */
  private Node computeSegmentSectionChoicesNode(SegmentOnTimeline segment) {
    var p4 = new VBox();
    p4.setPrefWidth(SEGMENT_MIN_WIDTH);
    p4.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    p4.setMaxHeight(Double.MAX_VALUE);
    p4.setPadding(new Insets(20, 0, 0, 0));
    VBox.setVgrow(p4, Priority.ALWAYS);
    var choices = fabricationService.getSegmentChoices(segment.getSegment());
    p4.getChildren().add(computeChoiceListNodes(segment, "Macro", choices.stream().filter((choice) -> ProgramType.Macro == choice.getProgramType()).toList(), true, false, false));
    p4.getChildren().add(computeChoiceListNodes(segment, "Main", choices.stream().filter((choice) -> ProgramType.Main == choice.getProgramType()).toList(), true, false, false));
    p4.getChildren().add(computeChoiceListNodes(segment, "Beat", choices.stream().filter((choice) -> ProgramType.Beat == choice.getProgramType()).toList(), false, true, false));
    p4.getChildren().add(computeChoiceListNodes(segment, "Detail", choices.stream().filter((choice) -> ProgramType.Detail == choice.getProgramType()).toList(), true, false, false));
    p4.getChildren().add(computeChoiceListNodes(segment, "Perc Loop", choices.stream().filter((choice) ->
      InstrumentType.Percussion == choice.getInstrumentType() && InstrumentMode.Loop == choice.getInstrumentMode()).toList(), false, false, true));
    p4.getChildren().add(computeChoiceListNodes(segment, "Hook", choices.stream().filter((choice) -> InstrumentType.Hook == choice.getInstrumentType()).toList(), false, false, true));
    p4.getChildren().add(computeChoiceListNodes(segment, "Transition", choices.stream().filter((choice) -> InstrumentMode.Transition == choice.getInstrumentMode()).toList(), false, false, true));
    p4.getChildren().add(computeChoiceListNodes(segment, "Background", choices.stream().filter((choice) -> InstrumentMode.Background == choice.getInstrumentMode()).toList(), false, false, true));
    p4.getChildren().add(computeChoiceListNodes(segment, "Chord", choices.stream().filter((choice) -> InstrumentMode.Chord == choice.getInstrumentMode()).toList(), false, false, true));
    return p4;
  }

  /**
   Segment section: Memes, Chords
   */
  private Node computeSegmentSectionMemesChordsNode(SegmentOnTimeline segment) {
    var row = new HBox();
    row.setPrefWidth(SEGMENT_MIN_WIDTH);
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode("Memes", computeMemeListNode(segment), SEGMENT_MIN_WIDTH / 2, 0));
    row.getChildren().add(computeLabeledPropertyNode("Chords", computeChordListNode(segment), SEGMENT_MIN_WIDTH / 2, 0));
    return row;
  }

  /**
   Segment section: Beats, Density, Tempo, Key
   */
  private Node computeSegmentSectionPropertiesNode(SegmentOnTimeline segment) {
    var row = new HBox();
    row.setPrefWidth(SEGMENT_MIN_WIDTH);
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(String.format("%d beats", segment.getSegment().getTotal()), formatTimeFromMicros(segment.getSegment().getDurationMicros()), SEGMENT_MIN_WIDTH / 4));
    row.getChildren().add(computeLabeledPropertyNode("Density", String.format("%.2f", segment.getSegment().getDensity()), SEGMENT_MIN_WIDTH / 4));
    row.getChildren().add(computeLabeledPropertyNode("Tempo", formatMinDecimal(segment.getSegment().getTempo()), SEGMENT_MIN_WIDTH / 4));
    row.getChildren().add(computeLabeledPropertyNode("Key", segment.getSegment().getKey(), SEGMENT_MIN_WIDTH / 4));
    return row;
  }

  /**
   Segment section: offset, begin-at micros, delta, type
   */
  Node computeSegmentSectionHeaderNode(SegmentOnTimeline segment, SimpleLongProperty outputSyncChainMicros) {
    // position indicator
    var activeIndicator = new Pane();
    activeIndicator.setPrefWidth(SEGMENT_MIN_WIDTH);
    activeIndicator.setMinHeight(SEGMENT_ACTIVE_INDICATOR_HEIGHT);
    activeIndicator.setMaxHeight(SEGMENT_ACTIVE_INDICATOR_HEIGHT);
    activeIndicator.getStyleClass().add("segment-active-indicator");
    if (segment.isActive()) {
      var active = new SimpleBooleanProperty(true);
      active.bind(outputSyncChainMicros.map(sync -> SegmentUtils.isIntersecting(segment.getSegment(), sync.longValue(), 0L))); // why is this never triggering
      active.addListener((observable, oldValue, newValue) -> {
        if (newValue) {
          activeIndicator.getStyleClass().add("active");
        } else {
          activeIndicator.getStyleClass().remove("active");
        }
      });
    }
    var spacer = new Pane();
    spacer.setPrefWidth(SEGMENT_MIN_WIDTH);
    spacer.setMinHeight(SEGMENT_SECTION_VERTICAL_MARGIN);
    spacer.setMaxHeight(SEGMENT_SECTION_VERTICAL_MARGIN);
    // metadata
    var row = new HBox();
    row.setPrefWidth(SEGMENT_MIN_WIDTH);
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(String.format("[%d]", segment.getSegment().getId()), formatTimeFromMicros(segment.getSegment().getBeginAtChainMicros()), SEGMENT_MIN_WIDTH / 2));
    row.getChildren().add(computeLabeledPropertyNode(String.format("+%d", segment.getSegment().getDelta()), segment.getSegment().getType().toString(), SEGMENT_MIN_WIDTH / 2));
    // column
    var col = new VBox();
    col.getChildren().add(activeIndicator);
    col.getChildren().add(spacer);
    col.getChildren().add(row);
    return col;
  }

  @SuppressWarnings("SameParameterValue")
  Node computeLabeledPropertyNode(String label, String value, int minWidth) {
    return computeLabeledPropertyNode(label, computeValueNode(value), minWidth, 0);
  }

  Node computeChordListNode(SegmentOnTimeline segment) {
    var box = new VBox();
    box.getChildren().setAll(
      fabricationService.getSegmentChords(segment.getSegment())
        .stream()
        .sorted(Comparator.comparing(SegmentChord::getPosition))
        .map(chord -> {
          // position
          var position = new Label();
          position.setText(formatMinDecimal(chord.getPosition()));
          position.setPrefWidth(CHORD_POSITION_WIDTH);
          position.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
          position.getStyleClass().add("chord-position");
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

  Node computeMemeListNode(SegmentOnTimeline segment) {
    var box = new VBox();
    box.getChildren().setAll(
      fabricationService.getSegmentMemes(segment.getSegment())
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

  Node computeLabeledPropertyNode(Object label, Node child, int minWidth, int topPadding) {
    var labelNode = new Label();
    labelNode.setText(Objects.toString(label));
    labelNode.setMinWidth(minWidth);
    labelNode.getStyleClass().add("label");
    //
    var col = new VBox();
    col.setPadding(new Insets(topPadding, 10, 0, 0));
    col.getChildren().add(labelNode);
    col.getChildren().add(child);
    return col;
  }

  Node computeChoiceListNodes(SegmentOnTimeline segment, String layerName, Collection<? extends SegmentChoice> choices, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group");
    // layer name
    var layerNameLabel = new Label();
    layerNameLabel.setText(layerName);
    layerNameLabel.getStyleClass().add("choice-group-name");
    box.getChildren().add(layerNameLabel);
    // choices
    choices.forEach(choice -> {
      var choiceListItem = computeChoiceNode(segment, choice, showProgram, showProgramVoice, showArrangementPicks);
      box.getChildren().add(choiceListItem);
    });
    return box;
  }

  Node computeChoiceNode(SegmentOnTimeline segment, SegmentChoice choice, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group-item");

    if ((Objects.nonNull(choice.getMute()) && choice.getMute()) ||
      (Objects.nonNull(choice.getDeltaIn()) && DELTA_UNLIMITED != choice.getDeltaIn() && segment.getSegment().getDelta() < choice.getDeltaIn()) ||
      (Objects.nonNull(choice.getDeltaOut()) && DELTA_UNLIMITED != choice.getDeltaOut() && segment.getSegment().getDelta() > choice.getDeltaOut()))
      box.getStyleClass().add("choice-group-item-muted");

    if (showProgram) {
      box.getChildren().add(fabricationService.computeProgramReferenceNode(choice.getProgramId(), choice.getProgramSequenceBindingId()));
    }

    if (showProgramVoice) {
      box.getChildren().add(fabricationService.computeProgramVoiceReferenceNode(choice.getProgramVoiceId()));
    }

    var instrumentBox = new HBox();
    instrumentBox.getStyleClass().add("choice-instrument");
    computeShowDeltaNode(choice).ifPresent(instrumentBox.getChildren()::add);
    if (Objects.nonNull(choice.getInstrumentId())) {
      instrumentBox.getChildren().add(fabricationService.computeInstrumentReferenceNode(choice.getInstrumentId()));
    }
    box.getChildren().add(instrumentBox);

    if (showArrangementPicks) {
      box.getChildren().addAll(computeUniquePicks(choice).stream().flatMap(this::computeChoiceListItemPickNode).toList());
    }

    return box;
  }

  Optional<Node> computeShowDeltaNode(SegmentChoice choice) {
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
      deltaIn.setText(computeChoiceDeltaValue(choice.getDeltaIn()));
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
      deltaOut.setText(computeChoiceDeltaValue(choice.getDeltaOut()));
      deltaOut.getStyleClass().add("delta");
      deltaOut.getStyleClass().add("delta-out");
      box.getChildren().add(deltaOut);
    }
    return Optional.of(box);
  }

  String computeChoiceDeltaValue(Integer value) {
    if (-1 == value) return "∞";
    return Integer.toString(value);
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

  /**
   Format a number to a human-readable string

   @param number number to format
   @return human-readable string like "5", "4.5", or "1.27".
   */
  public static String formatMinDecimal(@Nullable Double number) {
    if (Objects.isNull(number)) {
      return "N/A";
    }
    if (Math.floor(number) == number) {
      return String.format("%.0f", number);  // No decimal places if it's an integer
    } else {
      String str = Double.toString(number);
      int decimalPlaces = str.length() - str.indexOf('.') - 1;

      // Remove trailing zeros
      for (int i = 0; i < decimalPlaces; i++) {
        if (str.endsWith("0")) {
          str = str.substring(0, str.length() - 1);
        } else {
          break;
        }
      }

      // Remove trailing decimal point if any
      if (str.endsWith(".")) {
        str = str.substring(0, str.length() - 1);
      }

      return str;
    }
  }

  /**
   Format a time in microseconds to a human-readable string

   @param microseconds time in microseconds
   @return human-readable string like "5s", "4m7s", or "1h27m4s".
   */
  String formatTimeFromMicros(@Nullable Long microseconds) {
    if (Objects.isNull(microseconds)) {
      return "N/A";
    }

    // Round up to the nearest second
    long totalSeconds = (microseconds + 999999) / 1000000;

    // Get fractional seconds
    double fractionalSeconds = (microseconds % 1000000) / 1000000.0;

    // Calculate hours, minutes, and remaining seconds
    long hours = totalSeconds / 3600;
    long remainingSeconds = totalSeconds % 3600;
    long minutes = remainingSeconds / 60;
    long seconds = remainingSeconds % 60;

    // Build the readable string
    StringBuilder readableTime = new StringBuilder();
    if (hours > 0) {
      readableTime.append(hours).append("h");
    }
    if (minutes > 0) {
      readableTime.append(minutes).append("m");
    }
    if (seconds > 0 || (hours == 0 && minutes == 0)) {
      if (hours == 0 && minutes == 0) {
        readableTime.append(String.format("%d.%d", seconds, (int) Math.floor(fractionalSeconds * 10))).append("s");
      } else {
        readableTime.append(seconds).append("s");
      }
    }

    return readableTime.toString();
  }
}

