package io.xj.gui.services.impl;

import io.xj.gui.WorkstationLogAppender;
import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.nav.NavState;
import io.xj.gui.nav.Route;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.GridChoice;
import io.xj.gui.types.ProgramEditorMode;
import io.xj.gui.types.ViewContentMode;
import io.xj.gui.types.ViewMode;
import io.xj.gui.types.ViewStatusMode;
import io.xj.gui.types.ViewTemplateMode;
import io.xj.gui.types.ZoomChoice;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.ControlMode;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.work.FabricationState;
import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

@Service
public class UIStateServiceImpl implements UIStateService {
  static final Logger LOG = LoggerFactory.getLogger(UIStateServiceImpl.class);
  private final Preferences prefs = Preferences.userNodeForPackage(UIStateServiceImpl.class);
  private final int programEditorBaseSizePerBeat;
  private final BooleanBinding hasCurrentProject;
  private final BooleanBinding isManualFabricationActive;
  private final BooleanProperty isLabFeatureEnabled = new SimpleBooleanProperty(false);
  private final ProjectService projectService;
  private final BooleanBinding isManualFabricationMode;
  private final BooleanBinding isProgressBarVisible;
  private static final Collection<ViewContentMode> CONTENT_MODES_WITH_PARENT = Set.of(
    ViewContentMode.ProgramBrowser,
    ViewContentMode.ProgramEditor,
    ViewContentMode.InstrumentBrowser,
    ViewContentMode.InstrumentEditor,
    ViewContentMode.InstrumentAudioEditor,
    ViewContentMode.LibraryEditor
  );
  private final StringBinding windowTitle;
  private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.Content);
  private final ObjectProperty<NavState> navRoute = new SimpleObjectProperty<>(new NavState(Route.LibraryBrowser, null));
  private final ObservableList<NavState> navHistory = FXCollections.observableArrayList();
  private final ObjectProperty<Library> currentLibrary = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Program> currentProgram = new SimpleObjectProperty<>(null);
  private final ObservableList<ProgramSequence> sequencesOfCurrentProgram = FXCollections.observableArrayList();
  private final ObjectProperty<Instrument> currentInstrument = new SimpleObjectProperty<>(null);
  private final ObjectProperty<InstrumentAudio> currentInstrumentAudio = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Template> currentTemplate = new SimpleObjectProperty<>(null);
  private final ObjectProperty<ViewContentMode> contentMode = new SimpleObjectProperty<>(ViewContentMode.LibraryBrowser);
  private final ObjectProperty<ViewTemplateMode> templateMode = new SimpleObjectProperty<>(ViewTemplateMode.TemplateBrowser);
  private final BooleanBinding isContentLevelUpPossible = Bindings.createBooleanBinding(
    () -> (Objects.equals(viewMode.get(), ViewMode.Content) && CONTENT_MODES_WITH_PARENT.contains(contentMode.get()))
      || (Objects.equals(viewMode.get(), ViewMode.Templates) && Objects.equals(templateMode.get(), ViewTemplateMode.TemplateEditor)),
    viewMode, contentMode, templateMode);

  private final BooleanBinding isViewProgressStatusMode;
  private final BooleanBinding isViewContentNavigationStatusMode;
  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  private final BooleanBinding isFabricationSettingsDisabled;
  private final BooleanBinding isMainActionButtonDisabled;
  private final DoubleBinding progress;
  private final StringBinding stateText;
  private final StringProperty logLevel = new SimpleStringProperty(WorkstationLogAppender.LEVEL.get().toString());
  private final BooleanBinding isViewModeFabrication = viewMode.isEqualTo(ViewMode.Fabrication);
  private final BooleanBinding isViewingEntity;
  private final StringBinding currentParentName;
  private final StringBinding currentEntityName;
  private final BooleanBinding isCreateEntityButtonVisible;

  private final Resource popupSelectorMenuFxml;
  private final Resource popupActionMenuFxml;
  private final String defaultIsLabFeatureEnabled;
  private final ObjectProperty<ProgramSequence> currentProgramSequence = new SimpleObjectProperty<>();
  private final ObservableList<GridChoice> programEditorGridChoices;
  private final ApplicationContext ac;
  private final ThemeService themeService;
  private final ObservableList<ZoomChoice> programEditorZoomChoices;
  private final ObjectProperty<GridChoice> programEditorGrid = new SimpleObjectProperty<>();
  private final ObjectProperty<ZoomChoice> programEditorZoom = new SimpleObjectProperty<>();
  private final BooleanProperty programEditorSnap = new SimpleBooleanProperty(false);
  private final ObjectProperty<ProgramEditorMode> programEditorMode = new SimpleObjectProperty<>();
  private final int navHistoryMaxSize;

  public UIStateServiceImpl(
    @Value("classpath:/views/content/common/popup-selector-menu.fxml") Resource popupSelectorMenuFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    @Value("${lab.feature.enabled}") String defaultIsLabFeatureEnabled,
    @Value("${programEditor.baseSizePerBeat}") int programEditorBaseSizePerBeat,
    @Value("#{'${programEditor.gridChoices}'.split(',')}") List<Double> programEditorGridChoices,
    @Value("${programEditor.gridChoiceDefault}") Double programEditorGridChoiceDefault,
    @Value("#{'${programEditor.zoomChoices}'.split(',')}") List<Double> programEditorZoomChoices,
    @Value("${programEditor.zoomChoiceDefault}") Double programEditorZoomChoiceDefault,
    @Value("${navHistory.maxSize}") int navHistoryMaxSize,
    ApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    ProjectService projectService
  ) {
    this.popupSelectorMenuFxml = popupSelectorMenuFxml;
    this.popupActionMenuFxml = popupActionMenuFxml;
    this.defaultIsLabFeatureEnabled = defaultIsLabFeatureEnabled;
    this.programEditorBaseSizePerBeat = programEditorBaseSizePerBeat;
    this.programEditorGridChoices = FXCollections.observableArrayList(programEditorGridChoices.stream().map(GridChoice::new).toList());
    this.navHistoryMaxSize = navHistoryMaxSize;
    this.ac = ac;
    this.themeService = themeService;
    programEditorGrid.set(new GridChoice(programEditorGridChoiceDefault));
    this.programEditorZoomChoices = FXCollections.observableArrayList(programEditorZoomChoices.stream().map(ZoomChoice::new).toList());
    programEditorZoom.set(new ZoomChoice(programEditorZoomChoiceDefault));

    // Has a current project?
    hasCurrentProject = Bindings.createBooleanBinding(
      () -> Objects.nonNull(projectService.currentProjectProperty().get())
        && projectService.stateProperty().isEqualTo(ProjectState.Ready).get(),
      projectService.currentProjectProperty(), projectService.stateProperty());

    // Is the fabrication settings button disabled?
    isFabricationSettingsDisabled = fabricationService.isStateActiveProperty();

    // Is the main action button disabled?
    isMainActionButtonDisabled = fabricationService.inputTemplateProperty().isNull();

    // Is the workstation in a manual fabrication mode?
    isManualFabricationMode = fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO);

    // Is manual fabrication active?
    isManualFabricationActive =
      fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO)
        .and(fabricationService.stateProperty().isEqualTo(FabricationState.Active));
    this.projectService = projectService;

    // Is the progress bar visible?
    isProgressBarVisible =
      projectService.isStateLoadingProperty().or(fabricationService.isStateLoadingProperty());

    // Progress
    progress =
      Bindings.createDoubleBinding(
        () ->
          projectService.isStateLoadingProperty().get() ?
            projectService.progressProperty().get() :
            fabricationService.isStateLoadingProperty().get() ?
              fabricationService.progressProperty().get() :
              0.0,
        projectService.isStateLoadingProperty(),
        projectService.progressProperty(),
        fabricationService.isStateLoadingProperty(),
        fabricationService.progressProperty());

    // State Text
    stateText = Bindings.createStringBinding(
      () -> projectService.isStateReadyProperty().get()
        ? fabricationService.stateTextProperty().getValue() : projectService.stateTextProperty().getValue(),
      projectService.stateProperty(),
      projectService.stateTextProperty(),
      fabricationService.stateTextProperty());

    progress.addListener((o, ov, value) -> UiUtils.setTaskbarProgress(value.floatValue()));

    projectService.stateProperty().addListener((o, ov, value) -> {
      if (Objects.equals(value, ProjectState.Standby)) {
        navigateTo(Route.LibraryBrowser, null);
      }
    });

    windowTitle = Bindings.createStringBinding(
      () -> Objects.nonNull(projectService.currentProjectProperty().get())
        ? String.format("%s%s - XJ music workstation",
        projectService.isModifiedProperty().get() ? "* " : "",
        projectService.currentProjectProperty().get().getName())
        : "XJ music workstation",
      projectService.isModifiedProperty(),
      projectService.currentProjectProperty()
    );

    currentParentName = Bindings.createStringBinding(
      () -> switch (viewMode.get()) {
        case Content -> switch (contentMode.get()) {
          case LibraryBrowser, LibraryEditor -> "Libraries";
          case InstrumentAudioEditor -> currentInstrument.isNotNull().get() ? currentInstrument.get().getName() : "";
          case ProgramBrowser, InstrumentBrowser, ProgramEditor, InstrumentEditor ->
            currentLibrary.isNotNull().get() ? currentLibrary.get().getName() : "";
        };
        case Templates -> switch (templateMode.get()) {
          case TemplateBrowser -> "Templates";
          case TemplateEditor -> currentTemplate.isNotNull().get() ? currentTemplate.get().getName() : "";
        };
        case Fabrication -> "";
      },
      viewMode, contentMode, currentLibrary
    );

    isViewingEntity = Bindings.createBooleanBinding(
      () -> switch (viewMode.get()) {
        case Content -> switch (contentMode.get()) {
          case LibraryEditor, ProgramEditor, InstrumentEditor, InstrumentAudioEditor -> true;
          default -> false;
        };
        case Templates -> Objects.equals(ViewTemplateMode.TemplateEditor, templateMode.get());
        default -> false;
      },
      viewMode, contentMode, templateMode
    );

    currentEntityName = Bindings.createStringBinding(
      () -> switch (viewMode.get()) {
        case Content -> switch (contentMode.get()) {
          case LibraryEditor -> currentLibrary.isNotNull().get() ? currentLibrary.get().getName() : "";
          case ProgramEditor -> currentProgram.isNotNull().get() ? currentProgram.get().getName() : "";
          case InstrumentEditor -> currentInstrument.isNotNull().get() ? currentInstrument.get().getName() : "";
          case InstrumentAudioEditor ->
            currentInstrumentAudio.isNotNull().get() ? currentInstrumentAudio.get().getName() : "";
          default -> "";
        };
        case Templates ->
          Objects.equals(ViewTemplateMode.TemplateEditor, templateMode.get()) && currentTemplate.isNotNull().get() ? currentTemplate.get().getName() : "";
        default -> "";
      },
      viewMode, contentMode, templateMode, currentProgram, currentInstrument, currentInstrumentAudio, currentTemplate);

    isCreateEntityButtonVisible = Bindings.createBooleanBinding(
      () ->
        projectService.isStateReadyProperty().get() &&
          switch (viewMode.get()) {
            case Content -> switch (contentMode.get()) {
              case LibraryBrowser, ProgramBrowser, InstrumentBrowser -> true;
              default -> false;
            };
            case Templates -> Objects.equals(ViewTemplateMode.TemplateBrowser, templateMode.get());
            default -> false;
          },
      viewMode, contentMode, templateMode, projectService.isStateReadyProperty()
    );

    ObjectBinding<ViewStatusMode> viewStatusMode = Bindings.createObjectBinding(
      () -> {
        if (projectService.isStateReadyProperty().not().get()) return ViewStatusMode.ProjectProgress;
        if (viewMode.isEqualTo(ViewMode.Fabrication).get()) return ViewStatusMode.FabricationProgress;
        return ViewStatusMode.ContentNavigation;
      },
      projectService.isStateReadyProperty(), viewMode);

    isViewProgressStatusMode = viewStatusMode.isEqualTo(ViewStatusMode.FabricationProgress).or(viewStatusMode.isEqualTo(ViewStatusMode.ProjectProgress));
    isViewContentNavigationStatusMode = viewStatusMode.isEqualTo(ViewStatusMode.ContentNavigation);

    // Update sequences of current program on a change in program or any sequence
    currentProgram.addListener((o, ov, v) -> updateSequencesOfCurrentProgram());
    projectService.addProjectUpdateListener(ProgramSequence.class, this::updateSequencesOfCurrentProgram);

    // Refresh the current program property when the program type is modified
    projectService.addProjectUpdateListener(Program.class, () -> {
      if (currentProgram.isNull().get()) return;
      var program = projectService.getContent().getProgram(currentProgram.get().getId());
      if (program.isEmpty()) {
        // program was deleted; this listener was latent
        currentProgram.set(null);
        return;
      }
      if (!Objects.equals(program.get().getType(), currentProgram.get().getType()))
        currentProgram.set(program.get());
    });

    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
  }

  @Override
  public void onStageReady() {
    logLevel.addListener((o, ov, value) -> WorkstationLogAppender.setLevel(value));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  @Override
  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }

  @Override
  public BooleanBinding isFabricationSettingsDisabledProperty() {
    return isFabricationSettingsDisabled;
  }

  @Override
  public BooleanBinding isViewProgressStatusModeProperty() {
    return isViewProgressStatusMode;
  }

  @Override
  public BooleanBinding isViewContentNavigationStatusModeProperty() {
    return isViewContentNavigationStatusMode;
  }

  @Override
  public StringBinding stateTextProperty() {
    return stateText;
  }

  @Override
  public BooleanBinding isProgressBarVisibleProperty() {
    return isProgressBarVisible;
  }

  @Override
  public DoubleBinding progressProperty() {
    return progress;
  }

  @Override
  public StringProperty logLevelProperty() {
    return logLevel;
  }

  @Override
  public BooleanBinding isManualFabricationModeProperty() {
    return isManualFabricationMode;
  }

  @Override
  public BooleanBinding isManualFabricationActiveProperty() {
    return isManualFabricationActive;
  }

  @Override
  public BooleanBinding hasCurrentProjectProperty() {
    return hasCurrentProject;
  }

  @Override
  public BooleanBinding isMainActionButtonDisabledProperty() {
    return isMainActionButtonDisabled;
  }

  @Override
  public ObjectProperty<ViewMode> viewModeProperty() {
    return viewMode;
  }

  @Override
  public ObjectProperty<NavState> navStateProperty() {
    return navRoute;
  }

  @Override
  public void navigateTo(Route route, @Nullable UUID id) {
    var state = new NavState(route, id);
    navRoute.set(state);
    navHistory.add(state);
    while (navHistory.size() > navHistoryMaxSize) navHistory.remove(0);
  }

  @Override
  public void navigateBack() {
    if (navHistory.size() > 1) {
      navHistory.remove(navHistory.size() - 1);
      navRoute.set(navHistory.get(navHistory.size() - 1));
    }
  }

  @Override
  public BooleanBinding isViewModeFabricationProperty() {
    return isViewModeFabrication;
  }

  @Override
  public ObservableStringValue windowTitleProperty() {
    return windowTitle;
  }

  @Override
  public void goUpContentLevel() {
    switch (viewMode.get()) {
      case Content -> {
        switch (contentMode.get()) {
          case ProgramBrowser, InstrumentBrowser, LibraryEditor -> {
            currentLibrary.set(null);
            contentMode.set(ViewContentMode.LibraryBrowser);
          }
          case ProgramEditor -> {
            currentProgram.set(null);
            contentMode.set(ViewContentMode.ProgramBrowser);
          }
          case InstrumentEditor -> {
            currentInstrument.set(null);
            contentMode.set(ViewContentMode.InstrumentBrowser);
          }
          case InstrumentAudioEditor -> {
            currentInstrumentAudio.set(null);
            contentMode.set(ViewContentMode.InstrumentEditor);
          }
        }
      }
      case Templates -> {
        if (Objects.equals(templateMode.get(), ViewTemplateMode.TemplateEditor)) {
          currentTemplate.set(null);
          templateMode.set(ViewTemplateMode.TemplateBrowser);
        }
      }
    }
  }

  @Override
  public BooleanBinding isContentLevelUpPossibleProperty() {
    return isContentLevelUpPossible;
  }

  @Override
  public ObjectProperty<ViewContentMode> contentModeProperty() {
    return contentMode;
  }

  @Override
  public ObjectProperty<ViewTemplateMode> templateModeProperty() {
    return templateMode;
  }

  @Override
  public ObjectProperty<Library> currentLibraryProperty() {
    return currentLibrary;
  }

  @Override
  public ObjectProperty<Program> currentProgramProperty() {
    return currentProgram;
  }

  @Override
  public ObservableList<ProgramSequence> sequencesOfCurrentProgramProperty() {
    return sequencesOfCurrentProgram;
  }

  @Override
  public ObjectProperty<Instrument> currentInstrumentProperty() {
    return currentInstrument;
  }

  @Override
  public ObjectProperty<InstrumentAudio> currentInstrumentAudioProperty() {
    return currentInstrumentAudio;
  }

  @Override
  public ObjectProperty<Template> currentTemplateProperty() {
    return currentTemplate;
  }

  @Override
  public void viewLibraries() {
    viewMode.set(ViewMode.Content);
    contentMode.set(ViewContentMode.LibraryBrowser);
  }

  @Override
  public void viewLibrary(UUID libraryId) {
    var library = projectService.getContent().getLibrary(libraryId)
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentLibrary.set(library);
    if (Objects.nonNull(library))
      if (projectService.getContent().getInstruments().stream()
        .anyMatch(instrument -> Objects.equals(instrument.getLibraryId(), library.getId()))) {
        contentMode.set(ViewContentMode.InstrumentBrowser);
      } else {
        contentMode.set(ViewContentMode.ProgramBrowser);
      }
  }

  @Override
  public void editLibrary(UUID libraryId) {
    var library = projectService.getContent().getLibrary(libraryId)
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentLibrary.set(library);
    contentMode.set(ViewContentMode.LibraryEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editProgram(UUID programId) {
    var program = projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Could not find Program!"));
    var library = projectService.getContent().getLibrary(program.getLibraryId())
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentProgram.set(program);
    currentLibrary.set(library);
    contentMode.set(ViewContentMode.ProgramEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editInstrument(UUID instrumentId) {
    var instrument = projectService.getContent().getInstrument(instrumentId)
      .orElseThrow(() -> new RuntimeException("Could not find Instrument!"));
    var library = projectService.getContent().getLibrary(instrument.getLibraryId())
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentInstrument.set(instrument);
    currentInstrumentAudio.set(null);
    currentLibrary.set(library);
    contentMode.set(ViewContentMode.InstrumentEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editInstrumentAudio(UUID instrumentAudioId) {
    var instrumentAudio = projectService.getContent().getInstrumentAudio(instrumentAudioId)
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio!"));
    var instrument = projectService.getContent().getInstrument(instrumentAudio.getInstrumentId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument!"));
    var library = projectService.getContent().getLibrary(instrument.getLibraryId())
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentInstrumentAudio.set(instrumentAudio);
    currentInstrument.set(instrument);
    currentLibrary.set(library);
    contentMode.set(ViewContentMode.InstrumentAudioEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editTemplate(UUID templateId) {
    var template = projectService.getContent().getTemplate(templateId)
      .orElseThrow(() -> new RuntimeException("Could not find Template!"));
    currentTemplate.set(template);
    templateMode.set(ViewTemplateMode.TemplateEditor);
    viewMode.set(ViewMode.Templates);
  }

  @Override
  public BooleanBinding isViewingEntityProperty() {
    return isViewingEntity;
  }

  @Override
  public StringBinding currentParentNameProperty() {
    return currentParentName;
  }

  @Override
  public StringBinding currentEntityNameProperty() {
    return currentEntityName;
  }

  @Override
  public BooleanBinding isCreateEntityButtonVisibleProperty() {
    return isCreateEntityButtonVisible;
  }

  @Override
  public BooleanProperty isLabFeatureEnabledProperty() {
    return isLabFeatureEnabled;
  }

  @Override
  public ObjectProperty<ProgramEditorMode> programEditorModeProperty() {
    return programEditorMode;
  }

  @Override
  public ObjectProperty<ProgramSequence> currentProgramSequenceProperty() {
    return currentProgramSequence;
  }

  @Override
  public int getProgramEditorBaseSizePerBeat() {
    return programEditorBaseSizePerBeat;
  }

  @Override
  public ObservableList<GridChoice> getProgramEditorGridChoices() {
    return programEditorGridChoices;
  }

  @Override
  public ObservableList<ZoomChoice> getProgramEditorZoomChoices() {
    return programEditorZoomChoices;
  }

  @Override
  public ObjectProperty<GridChoice> programEditorGridProperty() {
    return programEditorGrid;
  }

  @Override
  public ObjectProperty<ZoomChoice> programEditorZoomProperty() {
    return programEditorZoom;
  }

  @Override
  public BooleanProperty programEditorSnapProperty() {
    return programEditorSnap;
  }

  @Override
  public <T> void launchModalMenu(
    Resource fxml, Node launcher,
    Consumer<T> setupController,
    LaunchMenuPosition position,
    boolean darkenBackground,
    @Nullable Runnable onClose
  ) {
    try {
      launcher.setDisable(true);
      launcher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(fxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      T controller = loader.getController();
      setupController.accept(controller);
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();

      Runnable onHidden = () -> {
        launcher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false);
        launcher.setDisable(false);
        if (Objects.nonNull(onClose)) onClose.run();
      };
      if (darkenBackground) {
        UiUtils.darkenBackgroundUntilClosed(stage, launcher.getScene(), onHidden);
      } else {
        stage.setOnHidden(e -> onHidden.run());
      }

      UiUtils.closeWindowOnClickingAway(stage);
      position.move(launcher.getScene().getWindow(), stage);

    } catch (
      IOException e) {
      LOG.error("Failed to launch menu from {}! {}\n{}", fxml.getFilename(), e, StringUtils.formatStackTrace(e));
    }

  }

  @Override
  public void launchPopupSelectorMenu(Node launcher, Consumer<PopupSelectorMenuController> setupController) {
    launchModalMenu(popupSelectorMenuFxml, launcher, setupController, LaunchMenuPosition.from(launcher), false, null);
  }

  @Override
  public void launchPopupActionMenu(Node launcher, Consumer<PopupActionMenuController> setupController) {
    launchModalMenu(popupActionMenuFxml, launcher, setupController, LaunchMenuPosition.from(launcher), false, null);
  }

  @Override
  public void launchQuickActionMenu(Node launcher, MouseEvent mouseEvent, Consumer<PopupActionMenuController> setupController) {
    launchModalMenu(popupActionMenuFxml, launcher, setupController, LaunchMenuPosition.from(mouseEvent), false, null);
  }

  @Override
  public void setupTimelineBackground(Pane timeline, int timelineHeight) {
    // clear background items
    timeline.getChildren().clear();

    // if there's no sequence, don't draw the timeline
    if (currentProgramSequenceProperty().isNull().get()) {
      timeline.setMinWidth(0);
      timeline.setMaxWidth(0);
      return;
    }

    // variables
    int sequenceTotal = currentProgramSequenceProperty().get().getTotal();
    double beatWidth = getProgramEditorBaseSizePerBeat() * programEditorZoomProperty().get().value();
    double grid = programEditorGridProperty().get().value();

    // compute the total width
    var width = sequenceTotal * beatWidth;
    timeline.setMinWidth(width);
    timeline.setMaxWidth(width);

    // draw vertical grid lines
    double x;
    for (double b = grid; b < sequenceTotal; b += grid) {
      x = b * beatWidth;
      Line gridLine = new Line();
      gridLine.setStroke(b % 1 == 0 ? Color.valueOf("#505050") : Color.valueOf("#3d3d3d"));
      gridLine.setStrokeWidth(2);
      gridLine.setStartX(x);
      gridLine.setStartY(1);
      gridLine.setEndX(x);
      gridLine.setEndY(timelineHeight - 1);
      timeline.getChildren().add(gridLine);
    }

    // draw horizontal dotted line from x=0 to x=width at y = timelineHeight /2
    Line dottedLine = new Line();
    dottedLine.setStroke(Color.valueOf("#585858"));
    dottedLine.setStrokeWidth(2);
    dottedLine.setStartX(1);
    dottedLine.setStartY(timelineHeight / 2.0);
    dottedLine.setEndX(width - 1);
    dottedLine.setEndY(timelineHeight / 2.0);
    dottedLine.getStrokeDashArray().addAll(2d, 4d);
    timeline.getChildren().add(dottedLine);
  }

  @Override
  public void setupTimelineActiveRegion(Pane timeline, @Nullable ProgramSequencePattern pattern) {
    // if there's no sequence, don't draw the timeline
    if (currentProgramSequenceProperty().isNull().get()) {
      timeline.setMinWidth(0);
      timeline.setMaxWidth(0);
      return;
    }

    double beatWidth = getProgramEditorBaseSizePerBeat() * programEditorZoomProperty().get().value();

    // draw active region for the current pattern total
    if (Objects.nonNull(pattern)) {
      timeline.setMinWidth(beatWidth * pattern.getTotal());
      timeline.setMaxWidth(beatWidth * pattern.getTotal());
    } else {
      timeline.setMinWidth(0);
      timeline.setMaxWidth(0);
    }
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    isLabFeatureEnabled.addListener((o, ov, value) -> prefs.put("isLabFeatureEnabled", value.toString()));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    isLabFeatureEnabled.set(Boolean.parseBoolean(prefs.get("isLabFeatureEnabled", defaultIsLabFeatureEnabled)));
  }

  /**
   Update the observable list of sequences of the current program.
   */
  private void updateSequencesOfCurrentProgram() {
    if (Objects.nonNull(currentProgram.get())) {
      sequencesOfCurrentProgram.setAll(projectService.getContent().getSequencesOfProgram(currentProgram.get().getId()));
    } else {
      sequencesOfCurrentProgram.clear();
    }
  }
}
