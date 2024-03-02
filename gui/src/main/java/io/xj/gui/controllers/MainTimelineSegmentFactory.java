// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.util.FormatUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.AnchorPane;
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

  static final int CHORD_POSITION_WIDTH = 42;
  static final int SEGMENT_SECTION_VERTICAL_MARGIN = 30;
  final FabricationService fabricationService;

  public MainTimelineSegmentFactory(
    FabricationService fabricationService
  ) {
    this.fabricationService = fabricationService;
  }

  /**
   Every time we need a segment, build it from scratch

   @param segment from which to compute JavaFX node
   @param width   minimum width of the segment
   @return JavaFX node
   */
  public Node create(Segment segment, int width) {
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
        computeSegmentSectionChoicesNode(segment, innerFullWidth));
      return box;

    } catch (Exception e) {
      LOG.warn("Failed to update choices because {}!\n\n{}", e.getMessage(), formatStackTrace(e));
      return new VBox();
    }
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
    var segmentChoices = fabricationService.getSegmentChoices(segment);
    col.getChildren().add(computeChoicesListNode(segment, "Macro", segmentChoices.stream().filter((choice) -> ProgramType.Macro == choice.getProgramType()).toList(), true, false, false));
    col.getChildren().add(computeChoicesListNode(segment, "Main", segmentChoices.stream().filter((choice) -> ProgramType.Main == choice.getProgramType()).toList(), true, false, false));
    col.getChildren().add(computeChoicesListNode(segment, "Beat", segmentChoices.stream().filter((choice) -> ProgramType.Beat == choice.getProgramType()).toList(), false, true, false));
    for (var instrumentType : InstrumentType.values()) {
      var choices = segmentChoices.stream().filter((choice) -> instrumentType == choice.getInstrumentType()).toList();
      if (choices.isEmpty()) continue;
      col.getChildren().add(computeChoicesListNode(
        segment,
        instrumentType.toString(),
        choices,
        true,
        true,
        true
      ));
    }
    //
    var pane = new VBox();
    pane.getChildren().add(col);
    return pane;
  }

  Node computeChoicesListNode(Segment segment, String layerName, Collection<? extends SegmentChoice> choices, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
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
   Segment section: Beats, Intensity, Tempo, Key
   */
  private Node computeSegmentSectionPropertiesNode(Segment segment, int width) {
    var row = new HBox();
    row.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
    row.getChildren().add(computeLabeledPropertyNode(fabricationService.formatTotalBars(segment, segment.getTotal()), FormatUtils.formatTimeFromMicros(segment.getDurationMicros()), width / 4));
    row.getChildren().add(computeLabeledPropertyNode("Intensity", String.format("%.2f", segment.getIntensity()), width / 4));
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

  Node computeChoiceNode(Segment segment, SegmentChoice choice, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group-item");

    if ((Objects.nonNull(choice.getMute()) && choice.getMute()) ||
      (Objects.nonNull(choice.getDeltaIn()) && DELTA_UNLIMITED != choice.getDeltaIn() && segment.getDelta() < choice.getDeltaIn()) ||
      (Objects.nonNull(choice.getDeltaOut()) && DELTA_UNLIMITED != choice.getDeltaOut() && segment.getDelta() > choice.getDeltaOut()))
      box.getStyleClass().add("choice-group-item-muted");

    if (showProgram && Objects.nonNull(choice.getProgramId())) {
      box.getChildren().add(fabricationService.computeProgramReferenceNode(choice.getProgramId(), choice.getProgramSequenceBindingId()));
    }

    if (showProgramVoice && Objects.nonNull(choice.getProgramVoiceId())) {
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

