// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.xj.engine.fabricator.ControlMode;
import io.xj.engine.work.FabricationState;
import io.xj.gui.project.ProjectBuildState;
import io.xj.gui.project.ProjectState;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.ProgramEditorMode;
import io.xj.gui.types.Route;
import io.xj.gui.usage.XjSignalCatalog.L;
import io.xj.gui.usage.XjSignalCatalog.M;
import io.xj.gui.usage.XjSignalCatalog.MS;
import io.xj.gui.usage.XjSignalCatalog.N;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 What the workstation's state is counted as: views, modes, music playing, failures. Never any content.
 */
@ExtendWith(MockitoExtension.class)
class UsageObserverTest {
  @Mock
  UsageService usage;

  @Mock
  ProjectService projectService;

  @Mock
  FabricationService fabricationService;

  @Mock
  UIStateService uiStateService;

  final SimpleObjectProperty<ProjectState> projectState = new SimpleObjectProperty<>(ProjectState.Standby);
  final SimpleObjectProperty<Route> route = new SimpleObjectProperty<>(Route.ContentLibraryBrowser);
  final SimpleObjectProperty<ControlMode> controlMode = new SimpleObjectProperty<>(ControlMode.TAXONOMY);
  final AtomicLong nanos = new AtomicLong(0);
  UsageObserver subject;

  @BeforeEach
  void setUp() {
    lenient().when(projectService.stateProperty()).thenReturn(projectState);
    lenient().when(uiStateService.navStateProperty()).thenReturn(route);
    lenient().when(fabricationService.controlModeProperty()).thenReturn(controlMode);
    subject = new UsageObserver(usage, projectService, fabricationService, uiStateService, nanos::get);
  }

  @Test
  void theWindowWithNoProjectIsTheStartNode_andAReadyProjectIsItsView() {
    subject.onProjectState(ProjectState.Standby);
    verify(usage).node(N.START);

    projectState.set(ProjectState.Ready);
    route.set(Route.TemplateEditor);
    subject.onProjectState(ProjectState.Ready);
    verify(usage).node(N.TEMPLATE_EDITOR);

    subject.onRoute(Route.FabricationTimeline);
    verify(usage).node(N.FABRICATION_TIMELINE);
  }

  @Test
  void aViewIsNotCountedWhileNoProjectIsReady() {
    projectState.set(ProjectState.LoadingContent);
    subject.onRoute(Route.ContentProgramEditor);
    verify(usage, never()).node(anyInt());
  }

  @Test
  void timeInEachModeIsAddedUpAndSentAtTheEnd() {
    projectState.set(ProjectState.Ready);
    subject.onRoute(Route.ContentLibraryBrowser);
    nanos.addAndGet(90_000_000_000L);
    subject.onRoute(Route.ContentProgramEditor);
    nanos.addAndGet(30_000_000_000L);
    subject.onRoute(Route.FabricationTimeline);
    nanos.addAndGet(600_000_000_000L);
    subject.onEnd();
    verify(usage).time(eq(M.TIME_IN_MODE), doubleThat(seconds -> Math.abs(seconds - 120) < 0.001), eq(L.TIME_IN_MODE_CONTENT));
    verify(usage).time(eq(M.TIME_IN_MODE), doubleThat(seconds -> Math.abs(seconds - 600) < 0.001), eq(L.TIME_IN_MODE_PREVIEW));
    verify(usage, never()).time(eq(M.TIME_IN_MODE), anyDouble(), eq(L.TIME_IN_MODE_TEMPLATES));
  }

  @Test
  void aPreviewIsStartedInAControlMode_andMusicPlayingIsTheCoreLoop() {
    when(usage.elapsedSeconds()).thenReturn(42);
    subject.onFabricationState(FabricationState.Starting);
    verify(usage).feature(L.FEATURES_PREVIEW_START);
    verify(usage).milestone(MS.PREVIEW_STARTED);
    verify(usage).countLabel(M.PREVIEW_CONTROL_MODE, L.TAXONOMY);

    subject.onFabricationState(FabricationState.Active);
    verify(usage).time(M.TIME_TO_MUSIC, 42, -1);
    verify(usage).milestone(MS.MUSIC_PLAYING);
    verify(usage).milestone(MS.CORE_LOOP_START);

    nanos.addAndGet(75_000_000_000L);
    subject.onFabricationState(FabricationState.Cancelled);
    verify(usage).time(eq(M.PREVIEW_LENGTH), doubleThat(seconds -> Math.abs(seconds - 75) < 0.001), eq(-1));
    verify(usage).milestone(MS.CORE_LOOP_COMPLETE);
  }

  @Test
  void aShortPreviewIsNotACompletedCoreLoop_andAFailureIsCounted() {
    subject.onFabricationState(FabricationState.Active);
    nanos.addAndGet(10_000_000_000L);
    subject.onFabricationState(FabricationState.Failed);
    verify(usage, never()).milestone(MS.CORE_LOOP_COMPLETE);
    verify(usage).failure(L.FAILURES_PREVIEW);
  }

  @Test
  void projectsAndBuildsThatFailAreCounted_andABuildThatFinishesIsAStep() {
    subject.onProjectState(ProjectState.Failed);
    verify(usage).failure(L.FAILURES_PROJECT);
    subject.onBuildState(ProjectBuildState.Building, ProjectBuildState.Failed);
    verify(usage).failure(L.FAILURES_BUILD);
    subject.onBuildState(ProjectBuildState.Building, ProjectBuildState.Clean);
    verify(usage).milestone(MS.PROJECT_BUILT);
  }

  @Test
  void theProgramEditorsModesAreCounted() {
    subject.onProgramEditorMode(ProgramEditorMode.Bind);
    subject.onProgramEditorMode(ProgramEditorMode.Edit);
    verify(usage).countLabel(M.PROGRAM_EDITOR_MODE, L.PROGRAM_EDITOR_MODE_BIND);
    verify(usage).countLabel(M.PROGRAM_EDITOR_MODE, L.PROGRAM_EDITOR_MODE_EDIT);
  }
}
