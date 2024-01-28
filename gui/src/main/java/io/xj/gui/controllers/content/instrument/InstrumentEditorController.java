// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.tables.pojos.InstrumentAudio;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentEditorController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentEditorController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final InstrumentAddAudioController instrumentAddAudioController;
  private final ObjectProperty<UUID> instrumentId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty config = new SimpleStringProperty("");
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final ObservableList<InstrumentAudio> audios = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected SplitPane container;

  @FXML
  protected VBox fieldsContainer;

  @FXML
  protected TextField fieldName;

  @FXML
  protected TextArea fieldConfig;

  @FXML
  protected Button buttonSave;

  @FXML
  protected TableView<InstrumentAudio> audiosTable;

  public InstrumentEditorController(
    ProjectService projectService,
    UIStateService uiStateService,
    InstrumentAddAudioController instrumentAddAudioController
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
    this.instrumentAddAudioController = instrumentAddAudioController;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);
    fieldConfig.textProperty().bindBidirectional(config);
    fieldConfig.prefHeightProperty().bind(fieldsContainer.heightProperty().subtract(100));

    name.addListener((o, ov, v) -> dirty.set(true));
    config.addListener((o, ov, v) -> dirty.set(true));

    audiosTable.setItems(audios);

/*
TODO add other audio columns
    TableColumn<InstrumentAudio, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeColumn.setPrefWidth(100);
    audiosTable.getColumns().add(typeColumn);
*/

    TableColumn<InstrumentAudio, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getName()));
    nameColumn.setPrefWidth(300);
    audiosTable.getColumns().add(nameColumn);

    audiosTable.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
          Platform.runLater(() -> {
            var audio = audiosTable.getSelectionModel().getSelectedItem();
            // TODO open audio in audio editor
          });
        }
      });

    addActionsColumn(InstrumentAudio.class, audiosTable,
      (InstrumentAudio audio) -> {
        // TODO open audio in audio editor
      },
      null,
      null,
      audio -> {
        if (Objects.nonNull(audio)) {
          // TODO confirm before deleting audio
          projectService.deleteInstrumentAudio(audio);
        }
      });

    projectService.addProjectUpdateListener(InstrumentAudio.class, this::updateAudios);

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.InstrumentEditor))
        update();
    });

    buttonSave.disableProperty().bind(dirty.not());
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  protected void handlePressSave() {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    instrument.setName(name.get());
    try {
      instrument.setConfig(new InstrumentConfig(config.get()).toString());
    } catch (Exception e) {
      LOG.error("Could not parse Instrument config!", e);
      return;
    }
    if (projectService.updateInstrument(instrument))
      uiStateService.viewLibrary(instrument.getLibraryId());
  }

  @FXML
  private void handlePressAddAudio(ActionEvent ignored) {
    instrumentAddAudioController.addAudioToInstrument(instrumentId.get());
  }

  /**
   Update the Instrument Editor with the current Instrument.
   */
  private void update() {
    if (uiStateService.currentInstrumentProperty().isNull().get())
      return;
    var instrument = projectService.getContent().getInstrument(uiStateService.currentInstrumentProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    LOG.info("Will edit Instrument \"{}\"", instrument.getName());
    this.instrumentId.set(instrument.getId());
    this.name.set(instrument.getName());
    this.config.set(instrument.getConfig());
    this.dirty.set(false);
    updateAudios();
  }

  /**
   Update the libraries table data.
   */
  private void updateAudios() {
    if (uiStateService.currentInstrumentProperty().isNull().get())
      return;
    audios.setAll(projectService.getContent().getInstrumentAudios().stream()
      .filter(audio -> Objects.equals(uiStateService.currentInstrumentProperty().get().getId(), audio.getInstrumentId()))
      .toList());
  }
}
