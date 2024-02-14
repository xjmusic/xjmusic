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
  PushingAudio,
  PushedAudio,
  PushingContent,
  PushedContent,
  Saving,
  Cancelled,
  Failed,
}
