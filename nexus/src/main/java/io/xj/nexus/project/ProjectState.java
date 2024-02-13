package io.xj.nexus.project;

public enum ProjectState {
  Standby,
  CreatingFolder,
  CreatedFolder,
  LoadingContent,
  LoadedContent,
  LoadingAudio,
  LoadedAudio,
  Ready,
  PushingContent,
  PushingAudio,
  Saving,
  Cancelled,
  Failed,
}
