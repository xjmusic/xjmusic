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
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 Usage reporting: what the workstation's own state says about how it is used, read from the properties the UI
 is already bound to. It counts which view is open, how long each mode is used, when music starts and stops
 playing, and when something fails. It reads no content: never a project, a file, a template, a program, an
 instrument, a meme or a name.
 <p>
 Every call ends in {@link UsageService}, which does nothing unless usage reporting is on.
 */
@Component
public class UsageObserver {
  /** A preview that played at least this long is a completed core loop. */
  static final int PREVIEW_COMPLETE_SECONDS = 60;
  private static final int NO_MODE = -1;

  private final UsageService usage;
  private final ProjectService projectService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;
  private final LongSupplier nanos;

  private int mode = NO_MODE;
  private long modeSince = 0;
  private final double[] modeSeconds = new double[3];
  private boolean playing = false;
  private long playingSince = 0;
  private boolean playedThisSession = false;
  private boolean observing = false;

  @Autowired
  public UsageObserver(
    UsageService usage,
    ProjectService projectService,
    FabricationService fabricationService,
    UIStateService uiStateService
  ) {
    this(usage, projectService, fabricationService, uiStateService, System::nanoTime);
  }

  UsageObserver(
    UsageService usage,
    ProjectService projectService,
    FabricationService fabricationService,
    UIStateService uiStateService,
    LongSupplier nanos
  ) {
    this.usage = usage;
    this.projectService = projectService;
    this.fabricationService = fabricationService;
    this.uiStateService = uiStateService;
    this.nanos = nanos;
  }

  /**
   Start observing, once the main window is up. The main window is the workstation's "menu": the point the
   first-run question is asked from.

   @param scene the main window's scene, where a click or a key press is the first input
   */
  public void onStageReady(Scene scene) {
    if (observing) return;
    observing = true;
    usage.beforeEnd(this::onEnd);

    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> usage.input());
    scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> usage.input());

    projectService.stateProperty().addListener((o, ov, value) -> onProjectState(value));
    projectService.buildStateProperty().addListener((o, ov, value) -> onBuildState(ov, value));
    projectService.isModifiedProperty().addListener((o, ov, value) -> {
      if (Boolean.TRUE.equals(value)) usage.milestone(MS.CONTENT_EDITED);
    });
    uiStateService.navStateProperty().addListener((o, ov, value) -> onRoute(value));
    uiStateService.logsVisibleProperty().addListener((o, ov, value) -> {
      if (Boolean.TRUE.equals(value)) usage.feature(L.FEATURES_LOGS_SHOWN);
    });
    uiStateService.programEditorModeProperty().addListener((o, ov, value) -> onProgramEditorMode(value));
    fabricationService.stateProperty().addListener((o, ov, value) -> onFabricationState(value));
    fabricationService.intensityOverrideActiveProperty().addListener((o, ov, value) -> {
      if (Boolean.TRUE.equals(value)) usage.feature(L.FEATURES_INTENSITY_OVERRIDE);
    });

    usage.milestone(MS.MENU);
    onProjectState(projectService.stateProperty().get());
  }

  // ── What is observed ───────────────────────────────────────────────────────────────────────────────────

  void onProjectState(ProjectState state) {
    if (state == ProjectState.Standby) {
      usage.node(N.START);
      setMode(NO_MODE);
    } else if (state == ProjectState.Ready) {
      onRoute(uiStateService.navStateProperty().get());
    } else if (state == ProjectState.Failed) {
      usage.failure(L.FAILURES_PROJECT);
    }
  }

  void onBuildState(ProjectBuildState before, ProjectBuildState state) {
    if (state == ProjectBuildState.Failed) {
      usage.failure(L.FAILURES_BUILD);
    } else if (state == ProjectBuildState.Clean && before == ProjectBuildState.Building) {
      usage.milestone(MS.PROJECT_BUILT);
    }
  }

  void onRoute(Route route) {
    if (route == null || !Objects.equals(projectService.stateProperty().get(), ProjectState.Ready)) return;
    usage.node(nodeOf(route));
    setMode(route.isContent() ? L.TIME_IN_MODE_CONTENT : route.isTemplate() ? L.TIME_IN_MODE_TEMPLATES : route.isFabrication() ? L.TIME_IN_MODE_PREVIEW : NO_MODE);
  }

  void onProgramEditorMode(ProgramEditorMode value) {
    if (value == ProgramEditorMode.Bind) usage.countLabel(M.PROGRAM_EDITOR_MODE, L.PROGRAM_EDITOR_MODE_BIND);
    else if (value == ProgramEditorMode.Edit) usage.countLabel(M.PROGRAM_EDITOR_MODE, L.PROGRAM_EDITOR_MODE_EDIT);
  }

  void onFabricationState(FabricationState state) {
    if (state == FabricationState.Starting) {
      usage.feature(L.FEATURES_PREVIEW_START);
      usage.milestone(MS.PREVIEW_STARTED);
      ControlMode controlMode = fabricationService.controlModeProperty().get();
      if (controlMode != null) {
        usage.countLabel(M.PREVIEW_CONTROL_MODE, switch (controlMode) {
          case AUTO -> L.AUTO;
          case MACRO -> L.MACRO;
          case TAXONOMY -> L.TAXONOMY;
        });
      }
    }
    if (state == FabricationState.Active) {
      if (!playing) {
        playing = true;
        playingSince = nanos.getAsLong();
        if (!playedThisSession) {
          playedThisSession = true;
          usage.time(M.TIME_TO_MUSIC, usage.elapsedSeconds(), -1);
          usage.milestone(MS.MUSIC_PLAYING);
        }
        usage.milestone(MS.CORE_LOOP_START);
      }
    } else {
      stopPlaying();
    }
    if (state == FabricationState.Failed) usage.failure(L.FAILURES_PREVIEW);
  }

  /** At quit: the time in the last mode, and the preview still playing. */
  void onEnd() {
    setMode(NO_MODE);
    stopPlaying();
    for (int label = 0; label < modeSeconds.length; label++) {
      if (modeSeconds[label] >= 1) usage.time(M.TIME_IN_MODE, modeSeconds[label], label);
      modeSeconds[label] = 0;
    }
  }

  // ── Inside ─────────────────────────────────────────────────────────────────────────────────────────────

  private void setMode(int next) {
    long now = nanos.getAsLong();
    if (mode != NO_MODE) modeSeconds[mode] += (now - modeSince) / 1e9;
    mode = next;
    modeSince = now;
  }

  private void stopPlaying() {
    if (!playing) return;
    playing = false;
    double seconds = (nanos.getAsLong() - playingSince) / 1e9;
    usage.time(M.PREVIEW_LENGTH, seconds, -1);
    if (seconds >= PREVIEW_COMPLETE_SECONDS) usage.milestone(MS.CORE_LOOP_COMPLETE);
  }

  /**
   @return the path node of a view: one for one with {@link Route}
   */
  static int nodeOf(Route route) {
    return switch (route) {
      case ContentLibraryBrowser -> N.CONTENT_LIBRARY_BROWSER;
      case ContentLibraryEditor -> N.CONTENT_LIBRARY_EDITOR;
      case ContentProgramBrowser -> N.CONTENT_PROGRAM_BROWSER;
      case ContentProgramEditor -> N.CONTENT_PROGRAM_EDITOR;
      case ContentInstrumentBrowser -> N.CONTENT_INSTRUMENT_BROWSER;
      case ContentInstrumentEditor -> N.CONTENT_INSTRUMENT_EDITOR;
      case ContentInstrumentAudioEditor -> N.CONTENT_INSTRUMENT_AUDIO_EDITOR;
      case TemplateBrowser -> N.TEMPLATE_BROWSER;
      case TemplateEditor -> N.TEMPLATE_EDITOR;
      case FabricationTimeline -> N.FABRICATION_TIMELINE;
      case FabricationSegment -> N.FABRICATION_SEGMENT;
    };
  }
}
