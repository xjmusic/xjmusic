// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.Collection;
import java.util.Objects;

/**
 NOT a Spring component/service -- this gets created by a custom controller factory in
 {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
public class MainTimelineSegmentController extends VBox implements ReadyAfterBootController {
  final static int MEME_CELL_HEIGHT = 24;
  final static int CHORD_CELL_HEIGHT = 24;
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
  ListView<SegmentMeme> listMemes;

  final Callback<ListView<SegmentMeme>, ListCell<SegmentMeme>> listMemeCellFactory = new Callback<>() {
    @Override
    public ListCell<SegmentMeme> call(ListView<SegmentMeme> param) {
      return new ListCell<>() {
        @Override
        protected void updateItem(SegmentMeme item, boolean empty) {
          super.updateItem(item, empty);

          if (empty || item == null) {
            setGraphic(null);
          } else {
            // todo setGraphic(...);
          }
        }
      };
    }
  };

  @FXML
  ListView<SegmentChord> listChords;

  final Callback<ListView<SegmentChord>, ListCell<SegmentChord>> listChordCellFactory = new Callback<>() {
    @Override
    public ListCell<SegmentChord> call(ListView<SegmentChord> param) {
      return new ListCell<>() {
        @Override
        protected void updateItem(SegmentChord item, boolean empty) {
          super.updateItem(item, empty);

          if (empty || item == null) {
            setGraphic(null);
          } else {
            // todo setGraphic(...);
          }
        }
      };
    }
  };

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

    listMemes.setCellFactory(listMemeCellFactory);
    listMemes.setItems(memes);
    listMemes.setFixedCellSize(MEME_CELL_HEIGHT);  // Replace with the height of your custom cell
    listMemes.itemsProperty().addListener((observable, oldValue, newValue) ->
      listMemes.setMinHeight(newValue.size() * MEME_CELL_HEIGHT + 2));

    listChords.setCellFactory(listChordCellFactory);
    listChords.setItems(chords);
    listChords.setFixedCellSize(CHORD_CELL_HEIGHT);  // Replace with the height of your custom cell
    listChords.itemsProperty().addListener((observable, oldValue, newValue) ->
      listChords.setMinHeight(newValue.size() * CHORD_CELL_HEIGHT + 2));
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
