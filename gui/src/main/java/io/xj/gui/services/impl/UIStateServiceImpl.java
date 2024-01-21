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
    ContentMode.LibraryEditor
  );
  private final StringBinding windowTitle;
  private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.Content);
  private final ObjectProperty<Library> currentLibrary = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Program> currentProgram = new SimpleObjectProperty<>(null);
  private final ObjectProperty<Instrument> currentInstrument = new SimpleObjectProperty<>(null);
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
  private final BooleanBinding isViewingLibrary;
  private final BooleanBinding isViewingEntity;
  private final StringBinding currentLibraryName;
  private final StringBinding currentEntityName;

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
      () -> Objects.equals(projectService.stateProperty().get(), ProjectState.Ready)
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
        ? String.format("%s - XJ music workstation", projectService.currentProjectProperty().get().getName())
        : "XJ music workstation",
      projectService.currentProjectProperty()
    );

    isViewingLibrary = currentLibrary.isNotNull();
    currentLibraryName = Bindings.createStringBinding(
      () -> Objects.nonNull(currentLibrary.get()) ? currentLibrary.get().getName() : "",
      currentLibrary
    );

    isViewingEntity = currentProgram.isNotNull().or(currentInstrument.isNotNull()).or(currentTemplate.isNotNull());
    currentEntityName = Bindings.createStringBinding(
      () -> {
        if (Objects.nonNull(currentProgram.get())) {
          return currentProgram.get().getName();
        } else if (Objects.nonNull(currentInstrument.get())) {
          return currentInstrument.get().getName();
        } else if (Objects.nonNull(currentTemplate.get())) {
          return currentTemplate.get().getName();
        } else {
          return "";
        }
      }, currentProgram, currentInstrument, currentTemplate);
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
        }
      }
      case Templates -> {
        if (Objects.equals(templateMode.get(), TemplateMode.TemplateEditor)) {
          currentTemplate.set(null);
          templateMode.set(TemplateMode.TemplateBrowser);
        }
      }
    }

    if (Objects.equals(viewMode.get(), ViewMode.Content)) {
      if (Objects.equals(contentMode.get(), ContentMode.ProgramEditor)) {
        contentMode.set(ContentMode.ProgramBrowser);
      } else if (Objects.equals(contentMode.get(), ContentMode.InstrumentEditor)) {
        contentMode.set(ContentMode.InstrumentBrowser);
      }
    } else if (Objects.equals(viewMode.get(), ViewMode.Templates)) {
      if (Objects.equals(templateMode.get(), TemplateMode.TemplateEditor)) {
        templateMode.set(TemplateMode.TemplateBrowser);
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
  public ObjectProperty<Template> currentTemplateProperty() {
    return currentTemplate;
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
    currentProgram.set(program);
    contentMode.set(ContentMode.ProgramEditor);
    viewMode.set(ViewMode.Content);
  }

  @Override
  public void editInstrument(UUID instrumentId) {
    var instrument = projectService.getContent().getInstrument(instrumentId)
      .orElseThrow(() -> new RuntimeException("Could not find Instrument!"));
    currentInstrument.set(instrument);
    contentMode.set(ContentMode.InstrumentEditor);
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
  public BooleanBinding isViewingLibraryProperty() {
    return isViewingLibrary;
  }

  @Override
  public BooleanBinding isViewingEntityProperty() {
    return isViewingEntity;
  }

  @Override
  public StringBinding currentLibraryNameProperty() {
    return currentLibraryName;
  }

  @Override
  public StringBinding currentEntityNameProperty() {
    return currentEntityName;
  }
}
