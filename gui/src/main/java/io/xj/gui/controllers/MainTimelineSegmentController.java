// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.nexus.model.Segment;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Objects;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

/**
 NOT a Spring component/service -- this gets created by a custom controller factory in
 {@link io.xj.gui.controllers.MainTimelineController#cellFactory}
 */
public class MainTimelineSegmentController extends VBox implements ReadyAfterBootController {
  final SimpleObjectProperty<Segment> segment = new SimpleObjectProperty<>();
  final FabricationService fabricationService;

  @FXML
  Label labelType;

  @FXML
  Text durationMicrosText;

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
  Text storageKeyText;

  @FXML
  Text waveformPrerollText;

  @FXML
  Text waveformPostrollText;

  @FXML
  Text deltaText;

  @FXML
  Text createdAtText;

  @FXML
  Text updatedAtText;

  public MainTimelineSegmentController(FabricationService fabricationService) {
    this.fabricationService = fabricationService;
  }

  private String formatDurationMicros(@Nullable Long micros) {
    return Objects.nonNull(micros) ? String.format("%.2fs", (float) micros / MICROS_PER_SECOND) : "N/A";
  }

  public void setSegment(Segment segment) {
    this.segment.set(segment);
  }

  @Override
  public void onStageReady() {
    createdAtText.textProperty().bind(segment.map(Segment::getCreatedAt));
    deltaText.textProperty().bind(segment.map(Segment::getDelta).map(Objects::toString));
    densityText.textProperty().bind(segment.map(s -> String.format("%.2f", s.getDensity())));
    durationMicrosText.textProperty().bind(segment.map(s -> formatDurationMicros(s.getDurationMicros())));
    keyText.textProperty().bind(segment.map(Segment::getKey));
    labelType.textProperty().bind(segment.map(Segment::getType).map(Objects::toString));
    offsetText.textProperty().bind(segment.map(Segment::getOffset).map(Objects::toString));
    storageKeyText.textProperty().bind(segment.map(Segment::getStorageKey));
    tempoText.textProperty().bind(segment.map(s -> String.format("%.2f", s.getDensity())));
    totalText.textProperty().bind(segment.map(s -> String.format("%d", s.getTotal())));
    updatedAtText.textProperty().bind(segment.map(Segment::getUpdatedAt));
    waveformPostrollText.textProperty().bind(segment.map(s -> String.format("%.2f", s.getDensity())));
    waveformPrerollText.textProperty().bind(segment.map(s -> String.format("%.2f", s.getDensity())));

    // todo: choices source content from fabricationService
  }

  @Override
  public void onStageClose() {
    // no op
  }
}
