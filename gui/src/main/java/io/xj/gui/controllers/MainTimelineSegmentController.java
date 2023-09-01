// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.model.*;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 NOT a Spring component/service -- this gets created by a custom controller factory in
 {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
public class MainTimelineSegmentController extends VBox implements ReadyAfterBootController {
  private static final int CHORD_POSITION_WIDTH = 32;
  private static final int CHOICE_TYPE_WIDTH = 64;
  final SimpleObjectProperty<Segment> segment = new SimpleObjectProperty<>();
  final ObservableList<SegmentMeme> memes = FXCollections.observableArrayList();
  final ObservableList<SegmentChord> chords = FXCollections.observableArrayList();
  final ObservableList<SegmentChoice> choices = FXCollections.observableArrayList();
  final FabricationService fabricationService;

  @FXML
  Label labelType;

  @FXML
  Text keyText;

  @FXML
  Text totalText;

  @FXML
  Text offsetText;

  @FXML
  Text densityText;

  @FXML
  Text tempoText;

  @FXML
  Text deltaText;

  @FXML
  VBox listMemes;

  @FXML
  VBox listChords;

  @FXML
  VBox listChoices;

  public MainTimelineSegmentController(FabricationService fabricationService) {
    this.fabricationService = fabricationService;
  }

  public void setSegment(Segment segment) {
    this.segment.set(segment);
  }

  public void setMemes(Collection<SegmentMeme> memes) {
    this.memes.setAll(memes);
  }

  public void setChords(Collection<SegmentChord> chords) {
    this.chords.setAll(chords);
  }

  public void setChoices(Collection<SegmentChoice> choices) {
    this.choices.setAll(choices);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public void onStageReady() {
    deltaText.textProperty().bind(segment.map(Segment::getDelta).map(Objects::toString));
    densityText.textProperty().bind(segment.map(s -> String.format("%.2f", s.getDensity())));
    keyText.textProperty().bind(segment.map(Segment::getKey));
    labelType.textProperty().bind(segment.map(Segment::getType).map(Objects::toString));
    offsetText.textProperty().bind(segment.map(Segment::getOffset).map(Objects::toString));
    tempoText.textProperty().bind(segment.map(s -> formatMinDecimal(s.getTempo())));
    totalText.textProperty().bind(segment.map(s -> String.format("%d", s.getTotal())));

    memes.addListener((ListChangeListener<SegmentMeme>) c ->
      listMemes.getChildren().setAll(
        c.getList()
          .stream()
          .sorted(Comparator.comparing(SegmentMeme::getName))
          .map(meme -> {
            var text = new Text();
            text.setText(meme.getName());
            text.getStyleClass().add("meme");
            return text;
          })
          .toList()));

    chords.addListener((ListChangeListener<SegmentChord>) c ->
      listChords.getChildren().setAll(
        c.getList()
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
            var box = new HBox();
            box.getChildren().add(position);
            box.getChildren().add(name);
            return box;
          })
          .toList()));

    choices.addListener((ListChangeListener<SegmentChoice>) c -> {
        var macroChoices = c.getList().filtered((choice) -> ProgramType.Macro == choice.getProgramType());
        var mainChoices = c.getList().filtered((choice) -> ProgramType.Main == choice.getProgramType());
        var beatChoices = c.getList().filtered((choice) -> ProgramType.Beat == choice.getProgramType());
        var detailChoices = c.getList().filtered((choice) -> ProgramType.Detail == choice.getProgramType());
        var percLoopChoices = c.getList().filtered((choice) ->
          InstrumentType.Percussion == choice.getInstrumentType() && InstrumentMode.Loop == choice.getInstrumentMode());
        var hookChoices = c.getList().filtered((choice) -> InstrumentType.Hook == choice.getInstrumentType());
        var transitionModeChoices = c.getList().filtered((choice) -> InstrumentMode.Transition == choice.getInstrumentMode());
        var backgroundModeChoices = c.getList().filtered((choice) -> InstrumentMode.Background == choice.getInstrumentMode());
        var chordModeChoices = c.getList().filtered((choice) -> InstrumentMode.Chord == choice.getInstrumentMode());

        Collection<Node> nodes = new HashSet<>();
        nodes.add(computeChoiceListNodes("Macro", macroChoices, false, false));
        nodes.add(computeChoiceListNodes("Main", mainChoices, false, false));
        nodes.add(computeChoiceListNodes("Beat", beatChoices, false, false));
        nodes.add(computeChoiceListNodes("Detail", detailChoices, false, false));
        nodes.add(computeChoiceListNodes("Perc Loop", percLoopChoices, false, false));
        nodes.add(computeChoiceListNodes("Hook", hookChoices, false, false));
        nodes.add(computeChoiceListNodes("Transition", transitionModeChoices, false, false));
        nodes.add(computeChoiceListNodes("Background", backgroundModeChoices, false, false));
        nodes.add(computeChoiceListNodes("Chord", chordModeChoices, false, false));
        listChoices.getChildren().setAll(nodes);
      }
    );
  }

  private Node computeChoiceListNodes(String layerName, Collection<? extends SegmentChoice> choices, boolean showProgram, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group");
    // layer name
    var layerNameLabel = new Label();
    layerNameLabel.setText(layerName);
    layerNameLabel.getStyleClass().add("choice-group-name");
    box.getChildren().add(layerNameLabel);
    // choices
    choices.forEach(choice -> {
      var choiceListItem = computeChoiceListItem(choice, showProgram, showArrangementPicks);
      box.getChildren().add(choiceListItem);
    });
    return box;
  }

  private Node computeChoiceListItem(SegmentChoice choice, boolean showProgram, boolean showArrangementPicks) {
    var box = new VBox();
    box.getStyleClass().add("choice-group-item");


    /*
    const SegmentChoice = function ({
                                  showProgram,
                                  showProgramVoice,
                                  choice,
                                  segment,
                                }) {
  function renderOptionalProgramReference() {
    if (!showProgram) return [];
    return <ProgramReference programId={choice.programId}
                             programSequenceBindingId={choice.programSequenceBindingId}/>;
  }

  function renderOptionalInstrumentReference() {
    if (!choice.instrumentId) return [];
    return <InstrumentReference instrumentId={choice.instrumentId}/>;
  }

  function renderOptionalProgramVoiceReference() {
    if (!showProgramVoice) return [];
    if (!choice.programVoiceId) return [];
    return <ProgramVoiceReference programVoiceId={choice.programVoiceId}/>;
  }

  function renderDeltaValue(value) {
    if (-1 === value) return (<span>&infin;</span>);
    return value;
  }

  function renderDelta() {
    if ('Macro' === choice.programType || 'Main' === choice.programType) return null;
    if (!choice.deltaIn && !choice.deltaOut) return null;
    return (
      <div className="delta">
        <div className="delta-in">{renderDeltaValue(choice.deltaIn)}</div>
        <div className="connector">&mdash;</div>
        <div className="delta-out">{renderDeltaValue(choice.deltaOut)}</div>
      </div>
    );
  }

  function computeDeltaClass() {
    if (choice.mute)
      return 'partial-delta';
    if (DELTA_UNLIMITED !== choice.deltaIn && segment.delta < choice.deltaIn)
      return 'partial-delta';
    if (DELTA_UNLIMITED !== choice.deltaOut && segment.delta > choice.deltaOut)
      return 'partial-delta';
    return '';
  }

  return (
    <div key={choice.id} className={`segment-choice ${computeDeltaClass()}`}>
      {renderOptionalProgramReference()}
      {renderOptionalProgramVoiceReference()}
      <div className="segment-choice-instrument">
        {renderDelta()}
        {renderOptionalInstrumentReference()}
      </div>
    </div>
  );
}
     */


    if (showArrangementPicks) {
      box.getChildren().addAll(computeUniquePicks(choice).stream().map(this::computeChoiceListItemPick).toList());
    }
    
    /*

    if (Objects.nonNull(choice.getProgramType())) {
      var programType = new Label();
      programType.setText(choice.getProgramType().toString());
      programType.setMinWidth(CHOICE_TYPE_WIDTH);
      programType.getStyleClass().add("choice-program-type");
      var programName = new Text();
      programName.setText(fabricationService.getProgram(choice.getProgramId()).map(Program::getName).orElse("Unknown"));
      programName.getStyleClass().add("choice-program-name");
      var program = new HBox();
      program.getChildren().add(programType);
      program.getChildren().add(programName);
      box.getChildren().add(program);
    }
    if (Objects.nonNull(choice.getInstrumentType())) {
      var instrumentType = new Label();
      instrumentType.setText(choice.getInstrumentType().toString());
      instrumentType.setMinWidth(CHOICE_TYPE_WIDTH);
      instrumentType.getStyleClass().add("choice-instrument-type");
      var instrumentName = new Text();
      instrumentName.setText(fabricationService.getInstrument(choice.getInstrumentId()).map(Instrument::getName).orElse("Unknown"));
      instrumentName.getStyleClass().add("choice-instrument-name");
      var instrument = new HBox();
      instrument.getChildren().add(instrumentType);
      instrument.getChildren().add(instrumentName);
      box.getChildren().add(instrument);
    }
*/
    return box;
  }

  private Node computeChoiceListItemPick(SegmentChoiceArrangementPick pick) {
    return null;
  }

  /**
   Compute the unique (as in instrument audio reference) picks for the given choice

   @param choice for which to compute unique picks
   */
  private Collection<SegmentChoiceArrangementPick> computeUniquePicks(SegmentChoice choice) {
    return fabricationService.getArrangements(choice)
      .stream().flatMap(arrangement -> fabricationService.getPicks(arrangement).stream())
      .filter(pick -> Objects.nonNull(pick.getInstrumentAudioId()))
      .collect(Collectors.toMap(SegmentChoiceArrangementPick::getInstrumentAudioId, pick -> pick))
      .values();
  }

  @Override
  public void onStageClose() {
    // no op
  }

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
                 rel="noopener noreferrer"
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
