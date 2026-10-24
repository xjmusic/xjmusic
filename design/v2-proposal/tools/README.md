# Tools used by the second pass

Three throwaway scripts that produced the measurements and content analysis in [11-resolutions.md](../11-resolutions.md). They are kept here so the numbers can be reproduced, not as engine code. If the craft-timing harness proves useful it belongs under `engine/test/` with a CMake target; see the Phase 0 notes in [09-roadmap.md](../09-roadmap.md).

## `craft_timing.cpp`

Drives `XJ::Engine::RunCycle` at a 100 ms tick, faster than real time, and reports cycle times split into idle, craft and rewrite cycles.

Build the engine library first (from `engine/`):

```bash
mkdir -p build && cd build && cmake .. -DBUILD_TESTING=OFF && cmake --build . --config Release --target xjmusic
```

Then compile the harness against it. With MSVC, from a Developer Command Prompt (the `/MD` flag matches the runtime CMake chose for the library):

```bash
cl /std:c++17 /O2 /MD /EHsc /DNDEBUG /I../engine/include /I../engine/build/_deps/json-src/include craft_timing.cpp /Fe:craft_timing.exe /link ../engine/build/src/Release/xjmusic.lib
```

With GCC or Clang:

```bash
c++ -std=c++17 -O2 -DNDEBUG -I../engine/include -I../engine/build/_deps/json-src/include craft_timing.cpp ../engine/build/src/libxjmusic.a -o craft_timing
```

Run it on a project. The engine loads every `.json` file under the project's `build/` folder, so a project exported from the workstation works as is. Arguments after the project path are: simulated seconds, craft-ahead seconds, dub-ahead seconds, and how often (in seconds) to fire a macro override, zero for never.

```bash
./craft_timing path/to/Project.xj 600 20 10 0
./craft_timing path/to/Project.xj 600 8 2 20
```

On Windows, keep project paths short: the engine opens files with standard streams and paths beyond 260 characters fail to open, which the engine reports as an empty-input JSON parse error.

## `analyze_demos.py`

Walks a folder of XJ projects and counts programs by type, instruments by type and mode, audios, distinct memes by kind, and main-sequence lengths in beats and seconds. Pass the folder containing the projects:

```bash
python -I analyze_demos.py path/to/xjmusic-demos
```

## `taxonomies.py`

For each project in a folder, prints the `memeTaxonomy` block of every template and the memes actually used on programs, instruments and sequence bindings, so declared palettes can be compared with real usage.

```bash
python -I taxonomies.py path/to/xjmusic-demos
```

Both Python scripts only read JSON; a blob-less sparse clone of the demos repository restricted to `*.json` and `*.xj` is enough and avoids downloading the audio:

```bash
git clone --depth 1 --filter=blob:none --no-checkout https://github.com/xjmusic/xjmusic-demos.git demos
cd demos && git config core.longpaths true && git sparse-checkout set --no-cone '*.json' '*.xj' && git checkout
```
