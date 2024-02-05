// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentEditorController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentEditorController.class);
  private final ObjectProperty<UUID> instrumentId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty config = new SimpleStringProperty("");
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
  protected Button buttonOpenAudioFolder;

  @FXML
  protected TableView<InstrumentAudio> audiosTable;

  public InstrumentEditorController(
    @Value("classpath:/views/content/instrument/instrument-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
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

    fieldName.focusedProperty().addListener(this::onUnfocusedDoSave);
    fieldConfig.focusedProperty().addListener(this::onUnfocusedDoSave);

    buttonOpenAudioFolder.disableProperty().bind(Bindings.createBooleanBinding(audios::isEmpty, audios));

    audiosTable.setItems(audios);

    TableColumn<InstrumentAudio, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getName()));
    audiosTable.getColumns().add(nameColumn);

    TableColumn<InstrumentAudio, String> eventColumn = new TableColumn<>("Event");
    eventColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getEvent()));
    audiosTable.getColumns().add(eventColumn);

    TableColumn<InstrumentAudio, String> volumeColumn = new TableColumn<>("Volume");
    volumeColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getVolume())));
    audiosTable.getColumns().add(volumeColumn);

    javafx.scene.control.TableColumn<InstrumentAudio, String> tonesColumn = new TableColumn<>("Tones");
    tonesColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getTones()));
    audiosTable.getColumns().add(tonesColumn);

    TableColumn<InstrumentAudio, String> tempoColumn = new TableColumn<>("Tempo");
    tempoColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTempo())));
    audiosTable.getColumns().add(tempoColumn);

    TableColumn<InstrumentAudio, String> intensityColumn = new TableColumn<>("Intensity");
    intensityColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getIntensity())));
    audiosTable.getColumns().add(intensityColumn);

    TableColumn<InstrumentAudio, String> transientSecondsColumn = new TableColumn<>("Transient (Seconds)");
    transientSecondsColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTransientSeconds())));
    audiosTable.getColumns().add(transientSecondsColumn);

    TableColumn<InstrumentAudio, String> loopBeatsColumn = new TableColumn<>("Loop (Beats)");
    loopBeatsColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getLoopBeats())));
    audiosTable.getColumns().add(loopBeatsColumn);

    audiosTable.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
          if (Objects.nonNull(audiosTable.getSelectionModel().getSelectedItem()))
            Platform.runLater(() -> uiStateService.editInstrumentAudio(audiosTable.getSelectionModel().getSelectedItem().getId()));
        }
      });

    addActionsColumn(InstrumentAudio.class, audiosTable,
      (InstrumentAudio audio) -> uiStateService.editInstrumentAudio(audio.getId()),
      null,
      null,
      audio -> {
        if (Objects.nonNull(audio)) {
          if (showConfirmationDialog("Delete Audio?", "This action cannot be undone.", String.format("Are you sure you want to delete the Audio \"%s\"?", audio.getName())))
            projectService.deleteInstrumentAudio(audio);
        }
      });

    projectService.addProjectUpdateListener(InstrumentAudio.class, this::setupAudiosTable);

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.InstrumentEditor))
        setup();
    });
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  private void handlePressImportAudio(ActionEvent ignored) {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    var audioFilePath = ProjectUtils.chooseAudioFile(container.getScene().getWindow(), "Choose audio file");
    if (Objects.isNull(audioFilePath)) return;
    try {
      var audio = projectService.createInstrumentAudio(instrument, audioFilePath);
      uiStateService.editInstrumentAudio(audio.getId());
    } catch (Exception e) {
      LOG.error("Could not import audio!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
    }
  }

  @FXML
  private void handlePressOpenAudioFolder() {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    var audioFolder = projectService.getPathPrefixToInstrumentAudio(instrument.getId());
    if (Objects.isNull(audioFolder)) return;
    ProjectUtils.openDesktopPath(audioFolder);
  }

  /**
   When a field is unfocused, save

   @param o       observable
   @param ov      old value
   @param focused false if unfocused
   */
  private void onUnfocusedDoSave(Observable o, Boolean ov, Boolean focused) {
    if (!focused) {
      save();
    }
  }

  /**
   Save the Instrument
   */
  private void save() {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    instrument.setName(name.get());
    try {
      instrument.setConfig(new InstrumentConfig(config.get()).toString());
    } catch (Exception e) {
      LOG.error("Could not parse Instrument config!", e);
      return;
    }
    projectService.updateInstrument(instrument);
  }

  /**
   Update the Instrument Editor with the current Instrument.
   */
  private void setup() {
    if (uiStateService.currentInstrumentProperty().isNull().get())
      return;
    var instrument = projectService.getContent().getInstrument(uiStateService.currentInstrumentProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    LOG.info("Will edit Instrument \"{}\"", instrument.getName());
    this.instrumentId.set(instrument.getId());
    this.name.set(instrument.getName());
    this.config.set(instrument.getConfig());
    setupAudiosTable();
  }

  /**
   Update the instrument audios table data.
   */
  private void setupAudiosTable() {
    if (uiStateService.currentInstrumentProperty().isNull().get())
      return;
    audios.setAll(projectService.getContent().getInstrumentAudios().stream()
      .filter(audio -> Objects.equals(uiStateService.currentInstrumentProperty().get().getId(), audio.getInstrumentId()))
      .toList());
  }
}
