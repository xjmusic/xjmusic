// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.model.*;
import jakarta.annotation.Nullable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
 Used as a JavaFX node factory by {@link io.xj.gui.controllers.MainTimelineController}
 */
@Service
public class MainTimelineSegmentFactory {
  static final int SEGMENT_PROPERTY_ROW_MIN_HEIGHT = 60;
  static final int SEGMENT_CONTAINER_PADDING_VERTICAL = 10;
  static final int SEGMENT_CONTAINER_PADDING_HORIZONTAL = 10;
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

   @param segment        from which to compute JavaFX node
   @param pixelsPerMicro pixels per microsecond
   @param minWidth       minimum width of the segment
   @return JavaFX node
   */
  public Node create(Segment segment, float pixelsPerMicro, int minWidth) {
    // todo this should only get called when the segment needs to be completely re-rendered
    try {
      int width = Objects.nonNull(segment.getDurationMicros()) ? (int) (pixelsPerMicro * segment.getDurationMicros()) : minWidth;
      var box = new VBox();
      box.setMinWidth(width);
      box.setMaxWidth(width);
      box.getStyleClass().add("main-timeline-segment");
      box.setMaxHeight(Double.MAX_VALUE);
      box.setPadding(new Insets(SEGMENT_CONTAINER_PADDING_VERTICAL, SEGMENT_CONTAINER_PADDING_HORIZONTAL, SEGMENT_CONTAINER_PADDING_VERTICAL, SEGMENT_CONTAINER_PADDING_HORIZONTAL));
      VBox.setVgrow(box, Priority.ALWAYS);
      int innerWidth = width - SEGMENT_CONTAINER_PADDING_HORIZONTAL * 2;
      box.getChildren().addAll(
        computeSegmentSectionHeaderNode(segment, innerWidth),
        computeSegmentSectionPropertiesNode(segment, innerWidth),
        computeSegmentSectionMemesChordsNode(segment, innerWidth),
        computeSegmentSectionChoicesNode(segment, innerWidth),
        computeSegmentSectionMessageListNode(segment, innerWidth),
        computeSegmentSectionMetasNode(segment, innerWidth));
      return box;

    } catch (Exception e) {
      LOG.error("Failed to update choices because {}!\n\n{}", e.getMessage(), formatStackTrace(e));
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
    return computeLabeledPropertyNode("Messages", col, width, SEGMENT_SECTION_VERTICAL_MARGIN * 2);
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
    return computeLabeledPropertyNode("Metas", col, width, SEGMENT_SECTION_VERTICAL_MARGIN);
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
    var box = new VBox();
    box.getStyleClass().add("segment-meta");
    box.getChildren().add(key);
    box.getChildren().add(value);
    return box;
  }

  /**
   Segment section: Choices
   */
  private Node computeSegmentSectionChoicesNode(Segment segment, int width) {
    var p4 = new VBox();
    p4.setPrefWidth(width);
    p4.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    p4.setMaxHeight(Double.MAX_VALUE);
    p4.setPadding(new Insets(20, 0, 0, 0));
    VBox.setVgrow(p4, Priority.ALWAYS);
    var choices = fabricationService.getSegmentChoices(segment);
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
  private Node computeSegmentSectionMemesChordsNode(Segment segment, int width) {
    var row = new HBox();
    row.setPrefWidth(width);
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode("Memes", computeMemeListNode(segment), width / 2, 0));
    row.getChildren().add(computeLabeledPropertyNode("Chords", computeChordListNode(segment), width / 2, 0));
    return row;
  }

  /**
   Segment section: Beats, Density, Tempo, Key
   */
  private Node computeSegmentSectionPropertiesNode(Segment segment, int width) {
    var row = new HBox();
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(String.format("%d beats", segment.getTotal()), formatTimeFromMicros(segment.getDurationMicros()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Density", String.format("%.2f", segment.getDensity()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Tempo", formatMinDecimal(segment.getTempo()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Key", segment.getKey(), width / 4));
    return row;
  }

  /**
   Segment section: offset, begin-at micros, delta, type
   */
  Node computeSegmentSectionHeaderNode(Segment segment, int width) {
    var row = new HBox();
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(String.format("[%d]", segment.getId()), formatTimeFromMicros(segment.getBeginAtChainMicros()), width / 2));
    row.getChildren().add(computeLabeledPropertyNode(String.format("+%d", segment.getDelta()), segment.getType().toString(), width / 2));
    return row;
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

  Node computeChoiceListNodes(Segment segment, String layerName, Collection<? extends SegmentChoice> choices, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
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

