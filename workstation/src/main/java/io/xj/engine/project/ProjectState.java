package io.xj.engine.project;

public enum ProjectState {
  Standby,
  CreatingFolder,
  CreatedFolder,
  LoadingContent,
  LoadedContent,
  LoadingAudio,
  LoadedAudio,
  Ready,
  ExportingTemplate,
  Saving,
  Cancelled,
  Failed,
}
