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
  PushedContent,
  PushingAudio,
  PushedAudio,
  Saving,
  Cancelled,
  Failed,
}
