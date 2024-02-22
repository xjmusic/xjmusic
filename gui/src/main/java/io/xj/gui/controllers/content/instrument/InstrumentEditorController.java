// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.project.ProjectPathUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentEditorController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentEditorController.class);
  private final ObjectProperty<UUID> instrumentId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final ObjectProperty<InstrumentType> type = new SimpleObjectProperty<>(null);
  private final ObjectProperty<InstrumentMode> mode = new SimpleObjectProperty<>(null);
  private final ObjectProperty<InstrumentState> state = new SimpleObjectProperty<>(null);
  private final FloatProperty volume = new SimpleFloatProperty(0);
  private final StringProperty config = new SimpleStringProperty("");
  private final ObservableList<InstrumentAudio> audios = FXCollections.observableList(new ArrayList<>());
  private final StringProperty initialImportAudioDirectory = new SimpleStringProperty();

  @Value("classpath:/views/content/common/entity-memes.fxml")
  private Resource entityMemesFxml;

  @FXML
  protected SplitPane container;

  @FXML
  protected VBox fieldsContainer;

  @FXML
  public StackPane instrumentMemeContainer;

  @FXML
  protected TextField fieldName;

  @FXML
  protected TextField fieldVolume;

  @FXML
  protected TextArea fieldConfig;

  @FXML
  ChoiceBox<InstrumentType> choiceType;

  @FXML
  ChoiceBox<InstrumentMode> choiceMode;

  @FXML
  ChoiceBox<InstrumentState> choiceState;

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
    fieldVolume.textProperty().bindBidirectional(volume, new NumberStringConverter());
    fieldConfig.textProperty().bindBidirectional(config);
    fieldConfig.prefHeightProperty().bind(fieldsContainer.heightProperty().subtract(100));
    choiceType.setItems(FXCollections.observableArrayList(InstrumentType.values()));
    choiceType.valueProperty().bindBidirectional(type);
    choiceMode.setItems(FXCollections.observableArrayList(InstrumentMode.values()));
    choiceMode.valueProperty().bindBidirectional(mode);
    choiceState.setItems(FXCollections.observableArrayList(InstrumentState.values()));
    choiceState.valueProperty().bindBidirectional(state);

    fieldName.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("name", name.get());
      }
    });
    fieldConfig.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        try {
          config.set(new InstrumentConfig(config.get()).toString());
          update("config", config.get());
        } catch (Exception e) {
          LOG.error("Could not parse Instrument Config because {}", e.getMessage());
        }
      }
    });
    fieldVolume.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("volume", volume.get());
      }
    });
    choiceType.valueProperty().addListener((o, ov, v) -> update("type", type.get()));
    choiceMode.valueProperty().addListener((o, ov, v) -> update("mode", mode.get()));
    choiceState.valueProperty().addListener((o, ov, v) -> update("state", state.get()));

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
            projectService.deleteContent(audio);
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
    var audioFilePath = ProjectUtils.chooseAudioFile(container.getScene().getWindow(), "Choose audio file", initialImportAudioDirectory.get());
    if (Objects.isNull(audioFilePath)) return;
    initialImportAudioDirectory.set(ProjectPathUtils.getPrefix(audioFilePath));
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
    this.type.set(instrument.getType());
    this.mode.set(instrument.getMode());
    this.state.set(instrument.getState());
    this.volume.set(instrument.getVolume());
    setupAudiosTable();
    setupInstrumentMemeContainer();
  }

  /**
   Update an attribute of the current instrument record with the given value

   @param attribute of instrument
   @param value     to set
   */
  private void update(String attribute, Object value) {
    if (Objects.nonNull(instrumentId.get())) {
      try {
        projectService.update(Instrument.class, instrumentId.get(), attribute, value);
      } catch (Exception e) {
        LOG.error("Could not update Instrument " + attribute, StringUtils.formatStackTrace(e));
      }
    }
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

  /**
   Set up the instrument meme container FXML and its controller
   */
  private void setupInstrumentMemeContainer() {
    try {
      FXMLLoader loader = new FXMLLoader(entityMemesFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      instrumentMemeContainer.getChildren().clear();
      instrumentMemeContainer.getChildren().add(root);
      EntityMemesController entityMemesController = loader.getController();
      entityMemesController.setup(
        () -> projectService.getContent().getMemesOfInstrument(instrumentId.get()),
        () -> projectService.createInstrumentMeme(instrumentId.get()),
        (Object meme) -> {
          try {
            projectService.update(meme);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      );
    } catch (IOException e) {
      LOG.error("Error loading Entity Memes window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }
}
