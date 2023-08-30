// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 NOT a Spring component/service -- this gets created by a custom controller factory in
 {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
public class MainTimelineSegmentController extends VBox implements ReadyAfterBootController {
  private static final double CHORD_POSITION_WIDTH = 32;
  final SimpleObjectProperty<Segment> segment = new SimpleObjectProperty<>();

  final ObservableList<SegmentMeme> memes = FXCollections.observableArrayList();
  final ObservableList<SegmentChord> chords = FXCollections.observableArrayList();
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
