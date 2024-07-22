package io.xj.engine;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
  value = @Platform(
    includepath = {"../engine/include/xjmusic"},
    preloadpath = {"../engine/build/_deps"},
    linkpath = {"../engine/build/src/libxjmusic.a"},
    include = {"Engine.h"},
    // preload = {"DependentLib"},
    link = {"Engine"}
  ),
  target = "Engine"
)
public class Engine implements InfoMapper {
  public void map(InfoMap infoMap) {
  }
}

/*

TODO: sort out use of first manually created version of preset for Engine.cpp - whether we need this at all

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Platform(include="xjmusic/Engine.h")
@Namespace("XJ")
public class Engine extends Pointer {
  static { Loader.load(); }

  public Engine(String pathToProjectFile, @Cast("XJ::Fabricator::ControlMode") int controlMode,
                @Cast("std::optional<int>") Integer craftAheadSeconds,
                @Cast("std::optional<int>") Integer dubAheadSeconds,
                @Cast("std::optional<int>") Integer persistenceWindowSeconds) {
    allocate(pathToProjectFile, controlMode, craftAheadSeconds, dubAheadSeconds, persistenceWindowSeconds);
  }
  private native void allocate(String pathToProjectFile, int controlMode, Integer craftAheadSeconds,
                               Integer dubAheadSeconds, Integer persistenceWindowSeconds);

  public native void start(@StdString String templateIdentifier);
  public native void finish(@Cast("bool") boolean cancelled) const;
  // Add other methods as needed, simplifying complex types or using custom mappings.
}*/
