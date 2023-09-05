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
 Used as a JavaFX node factory by {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
@Service
public class MainTimelineSegmentFactory {
  static final int SEGMENT_MIN_WIDTH = 240;
  static final int SEGMENT_PROPERTY_ROW_MIN_HEIGHT = 60;
  static final Logger LOG = LoggerFactory.getLogger(MainTimelineSegmentFactory.class);

  static final int CHORD_POSITION_WIDTH = 32;
  final FabricationService fabricationService;

  public MainTimelineSegmentFactory(
    FabricationService fabricationService
  ) {
    this.fabricationService = fabricationService;
  }

  /**
   Every time we need a segment, build it from scratch

   @param segment from which to compute JavaFX node
   @return JavaFX node
   */
  public Node computeSegmentNode(Segment segment) {
    try {
      //
      var p1 = new HBox();
      p1.setPrefWidth(SEGMENT_MIN_WIDTH);
      p1.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
      p1.getChildren().add(computeLabeledPropertyNode(String.format("[%d]", segment.getOffset()), formatTimeFromMicros(segment.getBeginAtChainMicros()), SEGMENT_MIN_WIDTH / 2));
      p1.getChildren().add(computeLabeledPropertyNode(String.format("+%d", segment.getDelta()), segment.getType().toString(), SEGMENT_MIN_WIDTH / 2));
      //
      var p2 = new HBox();
      p2.setPrefWidth(SEGMENT_MIN_WIDTH);
      p2.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
      p2.getChildren().add(computeLabeledPropertyNode(String.format("%d beats", segment.getTotal()), formatTimeFromMicros(segment.getDurationMicros()), SEGMENT_MIN_WIDTH / 4));
      p2.getChildren().add(computeLabeledPropertyNode("Density", String.format("%.2f", segment.getDensity()), SEGMENT_MIN_WIDTH / 4));
      p2.getChildren().add(computeLabeledPropertyNode("Tempo", formatMinDecimal(segment.getTempo()), SEGMENT_MIN_WIDTH / 4));
      p2.getChildren().add(computeLabeledPropertyNode("Key", segment.getKey(), SEGMENT_MIN_WIDTH / 4));
      //
      var p3 = new HBox();
      p3.setPrefWidth(SEGMENT_MIN_WIDTH);
      p3.setMinHeight(SEGMENT_PROPERTY_ROW_MIN_HEIGHT);
      p3.getChildren().add(computeLabeledPropertyNode("Memes", computeMemeListNode(segment), SEGMENT_MIN_WIDTH / 2));
      p3.getChildren().add(computeLabeledPropertyNode("Chords", computeChordListNode(segment), SEGMENT_MIN_WIDTH / 2));
      //
      var p4 = new VBox();
      p4.setPrefWidth(SEGMENT_MIN_WIDTH);
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
      //
      var box = new VBox();
      box.getStyleClass().add("main-timeline-segment");
      box.setMaxHeight(Double.MAX_VALUE);
      box.setPadding(new Insets(10, 10, 10, 10));
      VBox.setVgrow(box, Priority.ALWAYS);
      box.getChildren().add(p1);
      box.getChildren().add(p2);
      box.getChildren().add(p3);
      box.getChildren().add(p4);
      return box;

    } catch (Exception e) {
      LOG.error("Failed to update choices because {}!\n\n{}", e.getMessage(), formatStackTrace(e));
      return new VBox();
    }
  }

  @SuppressWarnings("SameParameterValue")
  Node computeLabeledPropertyNode(String label, String value, int minWidth) {
    return computeLabeledPropertyNode(label, computeValueNode(value), minWidth);
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
          position.setMinWidth(CHORD_POSITION_WIDTH);
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

  Node computeLabeledPropertyNode(Object label, Node child, int minWidth) {
    var labelNode = new Label();
    labelNode.setText(Objects.toString(label));
    labelNode.setMinWidth(minWidth);
    labelNode.getStyleClass().add("label");
    //
    var vbox = new VBox();
    vbox.getChildren().add(labelNode);
    vbox.getChildren().add(child);
    vbox.setPadding(new Insets(0, 10, 0, 0));
    return vbox;
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
      var choiceListItem = computeChoiceListItemNode(segment, choice, showProgram, showProgramVoice, showArrangementPicks);
      box.getChildren().add(choiceListItem);
    });
    return box;
  }

  Node computeChoiceListItemNode(Segment segment, SegmentChoice choice, boolean showProgram, boolean showProgramVoice, boolean showArrangementPicks) {
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

    var instrumentBox = new VBox();
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
      readableTime.append(seconds).append("s");
    }

    return readableTime.toString();
  }
}

/*

From the original Lab web UI

```javascript
render() {
    if ('past' === this.state.tense) return null

    const macroChoices = this.props.choices.filter((choice) => 'Macro' === choice.programType);
    const mainChoices = this.props.choices.filter((choice) => 'Main' === choice.programType);
    const beatChoices = this.props.choices.filter((choice) => 'Beat' === choice.programType);

    const detailChoices = this.props.choices.filter((choice) => 'Detail' === choice.programType);
    const percLoopChoices = this.props.choices.filter((choice) => 'Percussion' === choice.instrumentType && 'Loop' === choice.instrumentMode);
    const hookChoices = this.props.choices.filter((choice) => 'Hook' === choice.instrumentType);
    const transitionModeChoices = this.props.choices.filter((choice) => 'Transition' === choice.instrumentMode);
    const backgroundModeChoices = this.props.choices.filter((choice) => 'Background' === choice.instrumentMode);
    const chordModeChoices = this.props.choices.filter((choice) => 'Chord' === choice.instrumentMode);

    return (
      <div className={`chain-timeline-segment ${this.state.tense}-tense`}>


        <div className="property-row">
          <div className="time property">{this.state.beginAtText}</div>
          <div className="property">&nbsp;</div>
          <div className="property">
            <div className="key">Offset</div>
            <div className="value">
              {this.props.segment.offset}
            </div>
          </div>
          <div className="property">
            <div className="key">{this.props.segment.type}</div>
            <div className="value">
              {this.props.segment.delta}
            </div>
          </div>
          <div className="property">
            <div className="key">Audio</div>
            <div className="value">
              <a target="_blank"
                 href={`${this.props.baseUrl}${this.props.segment.storageKey}.${this.props.segment.outputEncoder.toLowerCase()}`}>
                <GetAppIcon/>
              </a>
            </div>
          </div>
        </div>


        <div className="property-row">
          <div className="property">
            <div className="key">Total</div>
            <div className="value">{this.props.segment.total}</div>
          </div>
          <div className="property">
            <div className="key">Density</div>
            <div className="value">{this.props.segment.density}</div>
          </div>
          <div className="property">
            <div className="key">Tempo</div>
            <div className="value">{this.props.segment.tempo}</div>
          </div>
          <div className="property">
            <div className="key">Key</div>
            <div className="value">{this.props.segment.key}</div>
          </div>
        </div>


        <div className="property-row">
          <div className="property">
            <div className="key">Memes</div>
            <div className="value">
              {this.props.memes.map(meme =>
                <div key={meme.id}
                     className="meme">
                  {meme.name}
                </div>
              )}
            </div>
          </div>
          <div className="property triple">
            <div className="key chord">
              <div className="position">Chords</div>
            </div>
            <div className="value">
              {this.props.chords.map(chord =>
                <div key={chord.id}
                     className="chord">
                  <EntityPosition className="position" position={chord.position}/>
                  <div className="name">{chord.name}</div>
                </div>
              )}
            </div>
          </div>
        </div>


        {this.renderChoices('Macro', macroChoices, true)}

        {this.renderChoices('Main', mainChoices, true)}

        <div className="property">
          <div className="key">
            <div className="position">Beat</div>
          </div>
          <div className="value">
            {this.renderBeatProgramReference()}
            {beatChoices.map(choice => {
              return ([
                <SegmentChoice segment={this.props.segment} showProgramVoice={true} key={choice.id} choice={choice}/>
              ])
            })}
          </div>
        </div>

        {this.renderChoices('Detail', detailChoices, true)}

        {this.renderChoices('PercLoop', percLoopChoices, false, true)}

        {this.renderChoices('Hook', hookChoices, false, true)}

        {this.renderChoices('Transition', transitionModeChoices, false, true)}

        {this.renderChoices('Background', backgroundModeChoices, false, true)}

        {this.renderChoices('Chord', chordModeChoices, false, true)}

        {visibleIf(0 < this.props.metadatas.length,
          () => (
            <div className="property">
              <div className="key">
                <div className="position">Metadata</div>
              </div>
              <div className="value">
                {this.props.metadatas
                  .filter(message => message.type === 'Info')
                  .map(message => <SegmentMessage key={message.id} message={message}/>)}
              </div>
            </div>
          ))}

        <div className="property">
          <div className="key">
            <div className="position">Messages</div>
          </div>
          <div className="value">
            {this.props.messages
              .filter(message => message.type === 'Info')
              .map(message => <SegmentMessage key={message.id} message={message}/>)}
            {this.props.messages
              .filter(message => message.type === 'Warning')
              .map(message => <SegmentMessage key={message.id} message={message}/>)}
            {this.props.messages
              .filter(message => message.type === 'Error')
              .map(message => <SegmentMessage key={message.id} message={message}/>)}
          </div>
        </div>


      </div>
    )
  }
```

 */
