package io.xj.gui.services.impl;

import io.xj.gui.WorkstationLogAppender;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.WindowUtils;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.ControlMode;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.work.FabricationState;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UIStateServiceImpl implements UIStateService {
  private final BooleanBinding hasCurrentProject;
  private final BooleanBinding isManualFabricationActive;
  private final ProjectService projectService;
  private final BooleanBinding isManualFabricationMode;
  private final BooleanBinding isProgressBarVisible;
  private static final Collection<ContentMode> CONTENT_MODES_WITH_PARENT = Set.of(
    ContentMode.ProgramBrowser,
    ContentMode.ProgramEditor,
    ContentMode.InstrumentBrowser,
    ContentMode.InstrumentEditor,
    ContentMode.InstrumentAudioEditor,
    ContentMode.LibraryEditor
  );
  private final StringBinding windowTitle;
  private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.Content);
  private final ObjectProperty<Library> currentLibrary = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Program> currentProgram = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Instrument> currentInstrument = new SimpleObjectProperty<>(null);
  private final ObjectProperty<InstrumentAudio> currentInstrumentAudio = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Template> currentTemplate = new SimpleObjectProperty<>(null);
  private final ObjectProperty<ContentMode> contentMode = new SimpleObjectProperty<>(ContentMode.LibraryBrowser);
  private final ObjectProperty<TemplateMode> templateMode = new SimpleObjectProperty<>(TemplateMode.TemplateBrowser);
  private final BooleanBinding isContentLevelUpPossible = Bindings.createBooleanBinding(
    () -> (Objects.equals(viewMode.get(), ViewMode.Content) && CONTENT_MODES_WITH_PARENT.contains(contentMode.get()))
      || (Objects.equals(viewMode.get(), ViewMode.Templates) && Objects.equals(templateMode.get(), TemplateMode.TemplateEditor)),
    viewMode, contentMode, templateMode);
  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  private final BooleanBinding isFabricationSettingsDisabled;
  private final BooleanBinding isMainActionButtonDisabled;
  private final DoubleBinding progress;
  private final StringBinding stateText;
  private final StringProperty logLevel = new SimpleStringProperty(WorkstationLogAppender.LEVEL.get().toString());
  private final BooleanBinding isStateTextVisible;
  private final BooleanBinding isViewModeFabrication = viewMode.isEqualTo(ViewMode.Fabrication);
  private final BooleanBinding isViewingEntity;
  private final StringBinding currentParentName;
  private final StringBinding currentEntityName;
  private final BooleanBinding isCreateEntityButtonVisible;
  private final StringBinding createEntityButtonText;
  private final BooleanBinding isLibraryContentBrowser;

  public UIStateServiceImpl(
    FabricationService fabricationService,
    ProjectService projectService
  ) {

    // Has a current project?
    hasCurrentProject = projectService.stateProperty().isNotEqualTo(ProjectState.Standby);

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

    // State text is only visible in fabrication view or if project is between standby or ready states
    isStateTextVisible = projectService.isStateReadyProperty().not()
      .and(projectService.isStateStandbyProperty().not())
      .or(isViewModeFabrication);

    progress.addListener((o, ov, value) -> WindowUtils.setTaskbarProgress(value.floatValue()));

    projectService.stateProperty().addListener((o, ov, value) -> {
      if (Objects.equals(value, ProjectState.Standby)) {
        viewMode.set(ViewMode.Content);
        contentMode.set(ContentMode.LibraryBrowser);
        templateMode.set(TemplateMode.TemplateBrowser);
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
        case Templates -> Objects.equals(TemplateMode.TemplateEditor, templateMode.get());
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
          Objects.equals(TemplateMode.TemplateEditor, templateMode.get()) && currentTemplate.isNotNull().get() ? currentTemplate.get().getName() : "";
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
            case Templates -> Objects.equals(TemplateMode.TemplateBrowser, templateMode.get());
            default -> false;
          },
      viewMode, contentMode, templateMode, projectService.isStateReadyProperty()
    );

    createEntityButtonText = Bindings.createStringBinding(
      () -> switch (viewMode.get()) {
        case Content -> switch (contentMode.get()) {
          case LibraryBrowser -> "New Library";
          case ProgramBrowser -> "New Program";
          case InstrumentBrowser -> "New Instrument";
          default -> "";
        };
        case Templates -> "New Template";
        default -> "";
      },
      viewMode, contentMode, templateMode
    );

    isLibraryContentBrowser = Bindings.createBooleanBinding(
      () -> Objects.equals(ViewMode.Content, viewMode.get()) &&
        switch (contentMode.get()) {
          case ProgramBrowser, InstrumentBrowser -> true;
          default -> false;
        },
      viewMode, contentMode
    );
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
  public BooleanBinding isStateTextVisibleProperty() {
    return isStateTextVisible;
  }

  @Override
  public ObjectProperty<ViewMode> viewModeProperty() {
    return viewMode;
  }

  @Override
  public BooleanBinding isViewModeContentProperty() {
    return viewMode.isEqualTo(ViewMode.Content);
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
            contentMode.set(ContentMode.LibraryBrowser);
          }
          case ProgramEditor -> {
            currentProgram.set(null);
            contentMode.set(ContentMode.ProgramBrowser);
          }
          case InstrumentEditor -> {
            currentInstrument.set(null);
            contentMode.set(ContentMode.InstrumentBrowser);
          }
          case InstrumentAudioEditor -> {
            currentInstrumentAudio.set(null);
            contentMode.set(ContentMode.InstrumentEditor);
          }
        }
      }
      case Templates -> {
        if (Objects.equals(templateMode.get(), TemplateMode.TemplateEditor)) {
          currentTemplate.set(null);
          templateMode.set(TemplateMode.TemplateBrowser);
        }
      }
    }
  }

  @Override
  public BooleanBinding isContentLevelUpPossibleProperty() {
    return isContentLevelUpPossible;
  }

  @Override
  public ObjectProperty<ContentMode> contentModeProperty() {
    return contentMode;
  }

  @Override
  public ObjectProperty<TemplateMode> templateModeProperty() {
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
  public void viewTemplates() {
    viewMode.set(ViewMode.Templates);
    templateMode.set(TemplateMode.TemplateBrowser);
  }

  @Override
  public void viewLibraries() {
    viewMode.set(ViewMode.Content);
    contentMode.set(ContentMode.LibraryBrowser);
  }

  @Override
  public void viewLibrary(UUID libraryId) {
    var library = projectService.getContent().getLibrary(libraryId)
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentLibrary.set(library);
    if (Objects.nonNull(library))
      if (projectService.getContent().getInstruments().stream()
        .anyMatch(instrument -> Objects.equals(instrument.getLibraryId(), library.getId()))) {
        contentMode.set(ContentMode.InstrumentBrowser);
      } else {
        contentMode.set(ContentMode.ProgramBrowser);
      }
  }

  @Override
  public void editLibrary(UUID libraryId) {
    var library = projectService.getContent().getLibrary(libraryId)
      .orElseThrow(() -> new RuntimeException("Could not find Library!"));
    currentLibrary.set(library);
    contentMode.set(ContentMode.LibraryEditor);
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
    contentMode.set(ContentMode.ProgramEditor);
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
    contentMode.set(ContentMode.InstrumentEditor);
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
    contentMode.set(ContentMode.InstrumentAudioEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editTemplate(UUID templateId) {
    var template = projectService.getContent().getTemplate(templateId)
      .orElseThrow(() -> new RuntimeException("Could not find Template!"));
    currentTemplate.set(template);
    templateMode.set(TemplateMode.TemplateEditor);
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
  public StringBinding createEntityButtonTextProperty() {
    return createEntityButtonText;
  }

  @Override
  public BooleanBinding isLibraryContentBrowserProperty() {
    return isLibraryContentBrowser;
  }

}
