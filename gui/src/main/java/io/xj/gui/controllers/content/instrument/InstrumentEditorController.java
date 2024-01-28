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
import javafx.beans.binding.Bindings;
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

    name.addListener((o, ov, v) -> dirty.set(true));
    config.addListener((o, ov, v) -> dirty.set(true));

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

    TableColumn<InstrumentAudio, String> densityColumn = new TableColumn<>("Density");
    densityColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getDensity())));
    audiosTable.getColumns().add(densityColumn);

    TableColumn<InstrumentAudio, String> transientSecondsColumn = new TableColumn<>("Transient (Seconds)");
    transientSecondsColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTransientSeconds())));
    audiosTable.getColumns().add(transientSecondsColumn);

    TableColumn<InstrumentAudio, String> totalBeatsColumn = new TableColumn<>("Total (Beats)");
    totalBeatsColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTotalBeats())));
    audiosTable.getColumns().add(totalBeatsColumn);

    audiosTable.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
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
    if (projectService.updateInstrument(instrument)) dirty.set(false);
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
    ProjectUtils.openFolder(audioFolder);
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
   Update the instrument audios table data.
   */
  private void updateAudios() {
    if (uiStateService.currentInstrumentProperty().isNull().get())
      return;
    audios.setAll(projectService.getContent().getInstrumentAudios().stream()
      .filter(audio -> Objects.equals(uiStateService.currentInstrumentProperty().get().getId(), audio.getInstrumentId()))
      .toList());
  }
}
