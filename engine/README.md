
# XJ music engine

This is the playback engine for XJ music.

It's written in C++ as a shared library, and can be used in any language that can interface with C++.

The engine is designed to be as simple as possible, and to be able to Run on any platform.



## Building

To build the engine, you need to have CMake installed.

Then, you can Run the following commands:

```bash
mkdir build
cd build
cmake ..
cmake --build .
```

This will create a shared library in the `build` directory.



## Usage

The engine is designed to be as simple as possible.

All interaction is through an instance of an [XJ::Engine](https://engine-docs.xjmusic.com/d4/dde/classXJ_1_1Engine.html)

For an example of how to use it in C++ see [test/EngineTest.cpp](test/EngineTest.cpp)

Read the docs at [engine-docs.xjmusic.com](https://engine-docs.xjmusic.com)
