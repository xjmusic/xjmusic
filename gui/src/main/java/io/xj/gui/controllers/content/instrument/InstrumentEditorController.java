// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.nav.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.ViewContentMode;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.project.ProjectPathUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
  private final ObservableList<InstrumentAudio> audios = FXCollections.observableList(new ArrayList<>());
  private final Resource configFxml;
  private final Resource entityMemesFxml;
  private final ObjectProperty<UUID> instrumentId = new SimpleObjectProperty<>(null);
  private final StringProperty initialImportAudioDirectory = new SimpleStringProperty();
  private final CmdModalController cmdModalController;

  private final ChangeListener<? super ViewContentMode> onEditInstrument = (o, ov, v) -> {
    teardown();
    if (Objects.equals(uiStateService.contentModeProperty().get(), ViewContentMode.InstrumentEditor) && uiStateService.currentInstrumentProperty().isNotNull().get())
      setup();
  };
  private final Runnable updateInstrumentName;
  private final Runnable updateInstrumentType;
  private final Runnable updateInstrumentMode;
  private final Runnable updateInstrumentState;
  private final Runnable updateInstrumentVolume;

  @FXML
  StackPane instrumentMemeContainer;

  @FXML
  Button duplicateButton;

  @FXML
  Button configButton;

  @FXML
  TextField instrumentNameField;

  @FXML
  ComboBox<InstrumentType> instrumentTypeChooser;

  @FXML
  ComboBox<InstrumentMode> instrumentModeChooser;

  @FXML
  ComboBox<InstrumentState> instrumentStateChooser;

  @FXML
  TextField instrumentVolumeField;

  @FXML
  AnchorPane container;

  @FXML
  TableView<InstrumentAudio> audiosTable;

  @FXML
  Button buttonOpenAudioFolder;

  public InstrumentEditorController(
    @Value("classpath:/views/content/instrument/instrument-editor.fxml") Resource fxml,
    @Value("classpath:/views/content/instrument/instrument-config.fxml") Resource configFxml,
    @Value("classpath:/views/content/common/entity-memes.fxml") Resource entityMemesFxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.configFxml = configFxml;
    this.entityMemesFxml = entityMemesFxml;
    this.cmdModalController = cmdModalController;

    updateInstrumentName = () -> projectService.update(Instrument.class, instrumentId.get(), "name", instrumentNameField.getText());
    updateInstrumentType = () -> projectService.update(Instrument.class, instrumentId.get(), "type", instrumentTypeChooser.getValue());
    updateInstrumentMode = () -> projectService.update(Instrument.class, instrumentId.get(), "mode", instrumentModeChooser.getValue());
    updateInstrumentState = () -> projectService.update(Instrument.class, instrumentId.get(), "state", instrumentStateChooser.getValue());
    updateInstrumentVolume = () -> projectService.update(Instrument.class, instrumentId.get(), "volume", instrumentVolumeField.getText());
  }

  @Override
  public void onStageReady() {
    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get().route() == Route.ContentInstrumentEditor,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    uiStateService.contentModeProperty().addListener(onEditInstrument);
    instrumentTypeChooser.setItems(FXCollections.observableArrayList(InstrumentType.values()));
    instrumentModeChooser.setItems(FXCollections.observableArrayList(InstrumentMode.values()));
    instrumentStateChooser.setItems(FXCollections.observableArrayList(InstrumentState.values()));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    // Fields lose focus on Enter key press
    UiUtils.blurOnEnterKeyPress(instrumentNameField);
    UiUtils.blurOnEnterKeyPress(instrumentVolumeField);

    // On blur, update the underlying value
    UiUtils.onBlur(instrumentNameField, updateInstrumentName);
    UiUtils.onChange(instrumentTypeChooser.valueProperty(), updateInstrumentType);
    UiUtils.onChange(instrumentModeChooser.valueProperty(), updateInstrumentMode);
    UiUtils.onChange(instrumentStateChooser.valueProperty(), updateInstrumentState);
    UiUtils.onBlur(instrumentVolumeField, updateInstrumentVolume);

    container.maxWidthProperty().bind(container.getScene().getWindow().widthProperty());
    container.maxHeightProperty().bind(container.getScene().getWindow().heightProperty());

    buttonOpenAudioFolder.disableProperty().bind(Bindings.createBooleanBinding(audios::isEmpty, audios));

    audiosTable.setItems(audios);

    TableColumn<InstrumentAudio, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setPrefWidth(200);
    nameColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getName()));
    audiosTable.getColumns().add(nameColumn);

    TableColumn<InstrumentAudio, String> eventColumn = new TableColumn<>("Event");
    eventColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getEvent()));
    audiosTable.getColumns().add(eventColumn);

    TableColumn<InstrumentAudio, String> volumeColumn = new TableColumn<>("Volume");
    volumeColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getVolume())));
    audiosTable.getColumns().add(volumeColumn);

    TableColumn<InstrumentAudio, String> intensityColumn = new TableColumn<>("Intensity");
    intensityColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getIntensity())));
    audiosTable.getColumns().add(intensityColumn);

    javafx.scene.control.TableColumn<InstrumentAudio, String> tonesColumn = new TableColumn<>("Tones");
    tonesColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().getTones()));
    audiosTable.getColumns().add(tonesColumn);

    TableColumn<InstrumentAudio, String> tempoColumn = new TableColumn<>("Tempo");
    tempoColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTempo())));
    audiosTable.getColumns().add(tempoColumn);

    TableColumn<InstrumentAudio, String> transientSecondsColumn = new TableColumn<>("Transient");
    transientSecondsColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(Float.toString(row.getValue().getTransientSeconds())));
    audiosTable.getColumns().add(transientSecondsColumn);

    TableColumn<InstrumentAudio, String> loopBeatsColumn = new TableColumn<>("Loop Beats");
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
          if (projectService.showConfirmationDialog("Delete Audio?", "This action cannot be undone.", String.format("Are you sure you want to delete the Audio \"%s\"?", audio.getName())))
            projectService.deleteContent(audio);
        }
      });

    projectService.addProjectUpdateListener(InstrumentAudio.class, this::setupAudiosTable);

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ViewContentMode.InstrumentEditor))
        setup();
    });
  }

  @FXML
  void openCloneDialog() {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    cmdModalController.cloneInstrument(instrument);
  }

  @Override
  public void onStageClose() {
    LOG.info("Closed Instrument Editor");
  }

  @FXML
  void handleEditConfig() {
    uiStateService.launchModalMenu(
      configFxml,
      configButton,
      (InstrumentConfigController controller) -> controller.setup(instrumentId.get()),
      LaunchMenuPosition.from(configButton),
      true, null);
  }

  @FXML
  private void handlePressOpenAudioFolder() {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    var audioFolder = projectService.getPathPrefixToInstrumentAudio(instrument.getId());
    if (Objects.isNull(audioFolder)) return;
    ProjectUtils.openDesktopPath(audioFolder);
  }

  @FXML
  void handlePressImportAudio(ActionEvent ignored) {
    var instrument = projectService.getContent().getInstrument(instrumentId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    var audioFilePath = ProjectUtils.chooseAudioFile(container.getScene().getWindow(), "Choose audio file", initialImportAudioDirectory.get());
    if (Objects.isNull(audioFilePath)) return;
    initialImportAudioDirectory.set(ProjectPathUtils.getPrefix(audioFilePath));
    try {
      var audio = projectService.createInstrumentAudio(instrument, audioFilePath);
      uiStateService.editInstrumentAudio(audio.getId());
    } catch (Exception e) {
      LOG.error("Could not import audio! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
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
    instrumentNameField.setText(instrument.getName());
    instrumentTypeChooser.setValue(instrument.getType());
    instrumentModeChooser.setValue(instrument.getMode());
    instrumentStateChooser.setValue(instrument.getState());
    instrumentVolumeField.setText(instrument.getVolume().toString());

    setupAudiosTable();
    setupInstrumentMemeContainer();
  }

  /**
   Teardown the Instrument Editor
   */
  private void teardown() {
    // no op
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
   Set up the Instrument Meme Container FXML and its controller
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
        true, () -> projectService.getContent().getMemesOfInstrument(instrumentId.get()),
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
      LOG.error("Error loading Entity Memes window! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
