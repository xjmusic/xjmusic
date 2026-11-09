# Grounded map: engine segment store, messages/meta, build system, example app, docs, CI and tests

Scope: `engine/` at v1.9.6 (`engine/CMakeLists.txt:6`), as checked out at `/Users/nck/xj/xjmusic`. Every path below is relative to `engine/` unless it starts with `.github/`. Line numbers come from `grep -n` / `sed -n` on this checkout. Where something does not exist, it is stated as absent.

This map underpins V2 specs for: the C ABI build integration (`04-engine.md` §1 "Build integration"), CI additions (C-compiler header test, ASan, fuzz, timing harness), the snapshot JSON functions (`xj_engine_get_segments_json`, `xj_engine_get_messages_json`), the determinism work (§5), and the test plan (§11).

---

## 1. SegmentEntityStore: the in-memory segment database

Files: `include/xjmusic/segment/SegmentEntityStore.h` (284 lines), `src/segment/SegmentEntityStore.cpp` (422 lines). Owned by `Engine` as `std::unique_ptr<SegmentEntityStore> store` (`include/xjmusic/Engine.h:14`), handed as a raw pointer to `WorkManager` → `CraftWork`/`DubWork` → `Fabricator`/`SegmentRetrospective`, and exposed to hosts verbatim via `Engine::getSegmentStore()` (`Engine.h:63`).

### 1.1 Storage layout (`SegmentEntityStore.h:44-53`)

| Member | Type | Note |
|---|---|---|
| `chain` | `std::optional<Chain>` | Exactly one chain per store |
| `segments` | `std::map<int, Segment>` | Keyed by dense int `Segment::id`, 0-based (`CraftWork::buildSegmentInitial` sets `id = 0`, `src/work/CraftWork.cpp:405`) |
| `segmentChoices`, `segmentChoiceArrangements`, `segmentChoiceArrangementPicks`, `segmentChords`, `segmentChordVoicings`, `segmentMemes`, `segmentMessages`, `segmentMetas` | `std::map<int, std::map<UUID, T>>` | Partitioned by segment id, then by entity `UUID` (`using UUID = std::string;` at `include/xjmusic/util/EntityUtils.h:28`) |

There is no index other than these two levels. Every query that is not "by segment id" is a linear scan.

### 1.2 Generated CRUD API (macro, 8 sub-entity types)

`SEGMENT_STORE_CORE_HEADERS(ENTITY, ENTITIES)` (`SegmentEntityStore.h:25-30`) and `SEGMENT_STORE_CORE_METHODS(...)` (`SegmentEntityStore.cpp:11-55`) generate, for each of `SegmentChoice`, `SegmentChoiceArrangement`, `SegmentChoiceArrangementPick`, `SegmentChord`, `SegmentChordVoicing`, `SegmentMeme`, `SegmentMessage`, `SegmentMeta`:

| Signature | Semantics (from the .cpp) |
|---|---|
| `const T *put(const T &)` | `STORE[segmentId].emplace(id, entity)` (line 16): **does not overwrite** an existing id; returns pointer to whatever is stored. Silent no-op on duplicate id. |
| `std::optional<const T *> read##T(int segmentId, const UUID &id)` | `nullopt` if segment or id missing |
| `std::set<const T *> readAll##Ts(int segmentId)` | **`std::set` of pointers, ordered by pointer address** (line 28-37). Iteration order is allocation order, not insertion order. |
| `std::set<const T *> readAll##Ts(const std::set<int> &segmentIds)` | Union over segments, same pointer ordering |
| `void delete##T(int segmentId, const UUID &id)` | Erase; no-op if missing |

### 1.3 Hand-written API (chain, segments, cross-entity queries)

| Method | Lines (.cpp) | Behavior |
|---|---|---|
| `Chain *put(const Chain &)` | 74-77 | Replaces the single chain |
| `const Segment *put(const Segment &)` | 79-82 | `segments[id] = segment` — **overwrites** (unlike sub-entities) |
| `std::optional<Chain *> readChain()` | 93-95 | **Returns an engaged optional holding `nullptr` when there is no chain** (`chain.has_value() ? &chain.value() : nullptr`). `has_value()` is always true. |
| `std::optional<const Segment *> readSegment(int)` | 97-100 | By id |
| `readSegmentAtChainMicros(unsigned long long)` | 84-91 | Linear scan, `SegmentUtils::isSpanning(seg, t, t)`; header says "latter segment wins on the boundary" |
| `std::vector<const Segment *> readAllSegments()` | 102-112 | Sorted by id ascending |
| `readAllSegmentsInState(Segment::State)` | 115-126 | Filter; `std::sort` is inside the loop (O(n² log n)) |
| `readSegmentsFromToOffset(int from, int to)` | 128-136 | Inclusive both ends, by id |
| `std::set<const SegmentEntity *> readAllSegmentEntities(const std::set<int> &)` | 139-168 | All 8 sub-entity kinds for the given segments, as base-class pointers |
| `readAllSegmentsSpanning(unsigned long long from, unsigned long long to)` | 171-180 | Linear scan via `SegmentUtils::isSpanning` |
| `int readLastSegmentId() const` | 183-188 | `segments.rbegin()->first`, **returns 0 when empty** (indistinguishable from segment 0) |
| `readSegmentLast()` | 191-196 | Highest id |
| `readChoice(int segmentId, Program::Type)` | 199-210 | First choice of that program type (map order = UUID order) |
| `std::string readChoiceHash(const Segment &)` | 213-224 | Sorted ids of all sub-entities joined by `_` (not a hash, a concatenation) |
| `readAllSegmentChoiceArrangementPicks(const std::vector<const Segment *> &)` | 226-235 | Picks over many segments |
| `readAllSegmentChoiceArrangementPicks(const SegmentChoice *)` | 237-251 | Picks for one choice via its arrangements, O(arrangements × picks) |
| `std::vector<const SegmentChord *> readOrderedSegmentChords(int)` | 253-261 | Sorted by `position` |
| `int getSegmentCount() const`, `bool empty() const` | 264-271 | Segments only |
| `const Segment *updateSegment(Segment &)` | 274-302 | See §1.4 |
| `void deleteChain()` | 305-307 | |
| `void deleteSegment(int id)` | 310-320 | Erases the segment **and all 9 sub-entity partitions** for that id |
| `void deleteSegmentsBefore(int id)` | 385-395 | Strictly `<` |
| `void deleteSegmentsAfter(int id)` | 398-408 | Strictly `>` |
| `void clear()` | 411-422 | Everything, including the chain |
| `static protectSegmentStateTransition(from, to)`, `static onlyAllowSegmentStateTransitions(to, allowed)` | 323-372 | Throws `FabricationException("transition to X not in allowed (...)")` |

Private: `static void validate(SegmentMeme entity)` (375-377) and `static void validate(Segment entity)` (380-382) **take their argument by value**, so the `toMeme` normalization and `updatedAt` stamp they perform are discarded. `validate(Segment)` is called from `updateSegment` (line 276) and has no effect; `validate(SegmentMeme)` is never called.

### 1.4 `updateSegment` and the segment state machine

`updateSegment(Segment &segment)` (`SegmentEntityStore.cpp:274-302`): if the segment exists, `protectSegmentStateTransition(existing->state, toState)`; throws `FabricationException("cannot modify chainId of a Segment")` if `chainId` differs; forces `segment.id = existing->id`; always sets `segment.updatedAt = EntityUtils::currentTimeMillis()`; then `put`. Allowed transitions (`323-354`):

| From | To (allowed) |
|---|---|
| `Planned` | `Planned`, `Crafting` |
| `Crafting` | `Crafting`, `Crafted`, `Failed`, `Planned` |
| `Crafted` | `Crafted`, `Crafting` |
| `Failed` | `Failed` |
| (anything else) | `Planned` |

`Segment::State` = `Planned, Crafting, Crafted, Failed`; `Segment::Type` = `Pending, Initial, Continue, NextMain, NextMacro` (`include/xjmusic/segment/Segment.h:16-29`).

### 1.5 Who calls what (grep histogram of `store->X` across `src/`)

`put` 13, `readAllSegmentChoices` 10, `readAllSegmentChoiceArrangementPicks` 6, `readSegment` 5, `readChain` 5, `readAllSegmentChoiceArrangements` 4, `readChoice` 3, `updateSegment` 2, `readOrderedSegmentChords` 2, `readAllSegments` 2, `readAllSegmentMetas` 2, `readAllSegmentChordVoicings` 2, `deleteSegmentChoiceArrangementPick` 2, `readSegmentLast` 1, `readSegmentAtChainMicros` 1, `readAllSegmentsSpanning` 1, `readAllSegmentMemes` 1, `readAllSegmentChords` 1, `getSegmentCount` 1, `deleteSegmentsBefore` 1, `deleteSegmentsAfter` 1.

**Never called from `src/`:** `readAllSegmentMessages`, `readSegmentsFromToOffset`, `readAllSegmentsInState`, `readAllSegmentEntities`, `readChoiceHash`, `readLastSegmentId`, `deleteSegment` (directly), `deleteChain`, `clear`, `empty`, and every per-entity `delete*` except `deleteSegmentChoiceArrangementPick`. Messages are write-only inside the engine today.

Key mutation sites:

| Operation | Where |
|---|---|
| Chain created | `CraftWork::start`: `store->put(ChainUtils::fromTemplate(tmpl))` (`src/work/CraftWork.cpp:40`) |
| Initial segment | `buildSegmentInitial()` id 0, type `Pending`, `beginAtChainMicros 0` (`CraftWork.cpp:403-410`); `store->put` at 258 |
| Rewrite on override | `CraftWork::doFabricationRewrite`: cutoff math uses `mainProgramConfig.barBeats * cutoffMinimumBars` (298), `doCutoffLastSegment` (333+: `updateSegment` with new `total`/`durationMicros`, deletes picks starting past the cut, truncates overlapping ones), then **`store->deleteSegmentsAfter(lastSegment->id)` (319)**, builds a `NextMacro` segment, `store->put` (322) |
| Persistence window | `CraftWork::doSegmentCleanup`: `deleteSegmentsBefore(segmentAt(shippedTo − persistenceWindowMicros).id)` (396-401) |
| Dub reads | `CraftWork::getSegmentsIfReady` = `readAllSegmentsSpanning(from,to)` plus the previous/next segment if `Crafted` (58-77); `DubWork::computeActiveAudios` walks choices→arrangements→picks (`src/work/DubWork.cpp:96-140`) |
| State changes | `CraftWork::updateSegmentState` → `fabricator->updateSegment` (438-450); `Fabricator::updateSegment` swallows `FabricationException`, prints to `std::cerr`, returns `nullptr` (`src/fabricator/Fabricator.cpp:1013-1022`) |

**`Engine::start` does not clear the segment store.** `Engine.h:40` says "1. Clear the segment entity store", but `Engine::start` (`src/Engine.cpp:37-54`) only recreates `WorkManager`, clears `templateContent`, and calls `work->start()`. No `store->clear()` exists anywhere in `src/`.

### 1.6 Pointer and lifetime semantics

Every read returns raw pointers into `std::map` nodes. They are stable across inserts (map guarantee) but **dangle after `deleteSegment*`**. `ActiveAudio` (`include/xjmusic/audio/ActiveAudio.h:18-20`) carries `const SegmentChoiceArrangementPick*`, `const Instrument*`, `const InstrumentAudio*` raw pointers, and `AudioScheduleEvent` (`AudioScheduleEvent.h:19-20`) copies an `ActiveAudio` by value, so the events `Engine::RunCycle` returns point into the store. Nothing is reference counted.

---

## 2. Segment entity field lists (for the snapshot JSON spec)

All sub-entities derive from `SegmentEntity { UUID id; int segmentId; }` with `operator<` by `(segmentId, id)` (`SegmentEntity.h:15-34`). **No segment entity has any JSON (de)serialization**: `grep -rln json include/xjmusic/segment src/segment` is empty. nlohmann is used for content and for `StickyBun` only.

| Entity | Fields (type) | Header |
|---|---|---|
| `Chain` | `id`, `templateId` (UUID); `type` (`Preview`,`Production`); `state` (`Draft`,`Ready`,`Fabricate`,`Failed`); `shipKey`; `config` (`TemplateConfig`); `name`; `updatedAt` (long long, defaults to now) | `Chain.h:14-38` |
| `Segment` | `id` (int); `chainId` (UUID); `type`; `state`; `beginAtChainMicros` (**`long`**); `durationMicros` (`std::optional<long>`); `key`; `total` (int beats); `intensity`, `tempo` (float); `storageKey`; `waveformPreroll`, `waveformPostroll` (float); `delta` (int); `createdAt`, `updatedAt` (long long, default now) | `Segment.h:33-48` |
| `SegmentChoice` | `position` (float); `name`; `programId`, `programSequenceId`, `programSequenceBindingId`, `programVoiceId`, `instrumentId` (UUID); `deltaIn`, `deltaOut` (int, `DELTA_UNLIMITED = -1`); `mute` (bool); `instrumentType`, `instrumentMode`, `programType` | `SegmentChoice.h:19-33` |
| `SegmentChoiceArrangement` | `segmentChoiceId`, `programSequencePatternId` | `SegmentChoiceArrangement.h:19-20` |
| `SegmentChoiceArrangementPick` | `segmentChoiceArrangementId`, `segmentChordVoicingId`, `instrumentAudioId`, `programSequencePatternEventId` (UUID); `startAtSegmentMicros`, `lengthMicros` (**`long`**); `amplitude` (float); `tones`, `event` (string) | `SegmentChoiceArrangementPick.h:18-26` |
| `SegmentChord` | `position` (float); `name` | `SegmentChord.h:17-18` |
| `SegmentChordVoicing` | `segmentChordId`; `type` (`Instrument::Type`); `notes` (CSV string) | `SegmentChordVoicing.h:21-23` |
| `SegmentMeme` | `name` | `SegmentMeme.h:17` |
| `SegmentMessage` | `type` (`Debug`,`Info`,`Warning`,`Error`); `body` | `SegmentMessage.h:17-27` |
| `SegmentMeta` | `key`; `value` (both `std::string`) | `SegmentMeta.h:19-20` |

Every enum has `parseType`/`toString` pairs backed by static `std::map`s; unknown strings parse to the first enumerator (`SegmentMessage::parseType` → `Debug`, `SegmentMessage.cpp:18-23`; `Chain::parseType` → `Preview`, `Chain.cpp:26-31`).

`TemplateConfig` (what `Chain.config` holds; `include/xjmusic/content/TemplateConfig.h:26-55`): `detailLayerOrder`, `instrumentTypesForAudioLengthFinalization`, `instrumentTypesForInversionSeeking`, `eventNamesLarge/Medium/Small`, `memeTaxonomy`, `deltaArcBeatLayersToPrioritize`, `intensityAutoCrescendoEnabled`, `deltaArcEnabled`, `stickyBunEnabled`, `choiceMuteProbability`, `dubMasterVolume`, `intensityThreshold`, `intensityLayers` (per `Instrument::Type` maps), `intensityAutoCrescendoMaximum/Minimum`, `mixerCompress*` (5), `mixerNormalization*` (2), `deltaArcBeatLayersIncoming`, `deltaArcDetailLayersIncoming`, `mainProgramLengthMaxDelta`, `mixerDspBufferSize`, `mixerHighpassThresholdHz`, `mixerLowpassThresholdHz`. Defaults are a raw HOCON-style string `TemplateConfig::DEFAULT` (`src/content/TemplateConfig.cpp:12+`). There is **no `commitHorizonBeats`, `followHoldMaxBars`, tempo policy or seed** field today.

---

## 3. SegmentMessage and SegmentMeta in practice

### 3.1 Messages

Single producer path: `Fabricator::addMessage(SegmentMessage::Type, std::string body)` (`src/fabricator/Fabricator.cpp:58-73`) sets `id = EntityUtils::computeUniqueId()`, `segmentId = getSegment()->id`, and `put`s; it catches only `FabricationFatalException` and prints to `std::cerr`. Wrappers `addErrorMessage`/`addWarningMessage`/`addInfoMessage` (75-88). Declared `virtual` in `Fabricator.h:82-103`.

| Type | Writers (file:line) |
|---|---|
| `Error` | `MacroMainCraft.cpp:267,290,341,361,411` (no macro/main candidates etc.); `Fabricator.cpp:710,724,731,743,747` (StickyBun JSON failures); `Fabricator.cpp:1219` ("Refused to add Choice[...] because adding Memes[...] to MemeStack[...] would result in an invalid meme stack theorem!") |
| `Warning` | `DetailCraft.cpp:87`; `FabricationWrapper.cpp:26` ("EntityUtils not found! " + traces) |
| `Info` | `Craft.cpp:103` ("Prioritized " + CSV of layers) |
| `Debug` | **no writer anywhere** |

No reader: `readAllSegmentMessages` is not called in `src/` or `example/`. There is no timestamp, no severity ordering, no "since" cursor; the only ordering available is `(segmentId, id)` where `id` is a generated string. `xj_engine_get_messages_json(since)` has nothing to key `since` on today.

### 3.2 Meta: key/value shape

Only one writer: `Fabricator::putStickyBun(StickyBun bun)` (`Fabricator.cpp:684-693`):
- `key = bun.computeMetaKey()` = `"StickyBun_" + eventId` (`src/music/StickyBun.cpp:18` `META_KEY_TEMPLATE = "StickyBun_"`, `33-37`)
- `value = bun.serialize()` = nlohmann `{"eventId": "<uuid>", "values": [int,...]}` (`StickyBun.cpp:71-76`); `deserializeFrom` (79-85)

Readers: `Fabricator::getSegmentMeta(key)` linear scan over `readAllSegmentMetas(segmentId)` (`Fabricator.cpp:1068-1078`); `SegmentRetrospective::getPreviousMeta(key)` (`src/fabricator/SegmentRetrospective.cpp:164`). Gated by `chain->config.stickyBunEnabled` (`Fabricator.cpp:697`). Key uniqueness per segment is **not enforced**; with duplicate keys, "first match" is whichever pointer sorts lowest in the `std::set`. The proposal's choice diagnostics (`04-engine.md` §8, "SegmentMeta records keyed by choice") would be the second key family and needs a namespace convention; today the only convention is the `StickyBun_` prefix. Test fixture: `SegmentFixtures::buildSegmentMeta(const Segment*, std::string key, std::string value)` (`test/_helper/SegmentFixtures.h:175-178`).

---

## 4. Build system

### 4.1 Root `CMakeLists.txt` (89 lines)

| Fact | Line |
|---|---|
| `cmake_minimum_required(VERSION 3.24)` | 1 |
| `project(xjmusic VERSION 1.9.6 DESCRIPTION "The XJ music engine" LANGUAGES CXX)` — **C is not an enabled language** | 4-8 |
| `set(CMAKE_CXX_STANDARD 17)` | 11 |
| Top-level only: `CMAKE_CXX_EXTENSIONS OFF`, `USE_FOLDERS`, `include(CTest)`, `find_package(Doxygen)` → `add_subdirectory(doc)` | 14-37 |
| `add_vendor_external_project(PARENT NAME ARCHIVE HASH_MD5 BYPRODUCTS COPY_TO_RUNTIME)`: `ExternalProject_Add` from `vendor/<archive>` with MD5 check, installs to `${CMAKE_BINARY_DIR}/vendor/installed`, `EXCLUDE_FROM_ALL`, `add_dependencies`, links byproducts `PRIVATE`, post-build copies runtime libs next to the parent. `include(ExternalProject)` is **never called**; it works only because FetchContent pulls it in. | 40-72 |
| `add_subdirectory(src)`; **`add_subdirectory(example)` unconditional** → every build (including CI library builds) configures SDL2 from source | 78-81 |
| `add_subdirectory(test)` when top-level (or `MODERN_CMAKE_BUILD_TESTING`) and `BUILD_TESTING` | 85-88 |
| `CMAKE_POSITION_INDEPENDENT_CODE` mentioned only in a comment | 16 |

### 4.2 `src/CMakeLists.txt` (109 lines)

- `add_library(xjmusic <89 .cpp files>)` with **no `STATIC`/`SHARED` keyword and no `BUILD_SHARED_LIBS` anywhere → static archive** (`libxjmusic.a` / `xjmusic.lib`). CI's distro job is literally named `test_and_ship_static_library` and copies `src/libxjmusic.a` (`.github/workflows/engine_distro.yml:153`). `README.md:6,25` and `doc/index.md:7` both say "shared library"; they are wrong.
- `target_include_directories(xjmusic PUBLIC ../include)` (93); `target_compile_features(xjmusic PUBLIC cxx_std_17)` (96).
- nlohmann json 3.11.3 via `FetchContent_Declare(json URL vendor/nlohmann-json-3.11.3.tar.gz)` (99-100), linked `PRIVATE` but its include dir re-exported `PUBLIC` (101-103) because public headers use it (`EntityUtils.h:14` `using json = nlohmann::json;`).
- **No `install()`, `export()`, or package-config anywhere** (`grep -rn 'install(' --include=CMakeLists.txt` is empty). No version header is generated; the version lives only in `project(... VERSION 1.9.6)`.
- No `-fPIC`, no symbol visibility settings, no `__declspec`/`visibility` macros in any source (grep empty). No platform `#ifdef`s at all in `src/`, `include/`, `example/`, `test/`.

### 4.3 `test/CMakeLists.txt` (164 lines)

- `add_executable(xjmusic_test <5 helpers + 115 test files>)` (5-125); `cxx_std_17 PRIVATE` (128); links `xjmusic`, `gtest_main`, `gmock_main` (googletest 1.15.0 via vendor tarball, 134-137), `yaml-cpp::yaml-cpp` 0.8.0 (140-147, `FetchContent_Populate` + `add_subdirectory`).
- Quirks: line 152 `target_link_libraries(xjmusic PRIVATE nlohmann_json::nlohmann_json)` links the **library**, not the test; line 155 `set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)` runs **after** `FetchContent_MakeAvailable(googletest)` (135), too late to influence the first configure; line 161 `file(COPY ${CMAKE_SOURCE_DIR}/test/_data DESTINATION ${CMAKE_CURRENT_BINARY_DIR})` copies the 37 MB `_data` tree **at configure time**, so edited fixtures are stale until re-configure; line 164 `enable_testing()` duplicates root `include(CTest)`.
- `add_test(NAME xjmusic_test COMMAND xjmusic_test)` (158): **one CTest entry for all 596 `TEST`/`TEST_F` cases** (`grep -rh '^TEST' test | wc -l` = 596). CTest runs it with cwd `build/test`, which is what the relative `_data/...` paths require.

### 4.4 `example/CMakeLists.txt` (43 lines)

`add_executable(xj_player main.cpp XJPlayer.cpp EngineUiBase.cpp EngineScheduledAudio.cpp)`; SDL 2.30.5 via `add_vendor_external_project` with per-platform branches: `WIN32` links **debug** `SDL2d.lib;SDL2maind.lib` and copies `SDL2d.dll` (16); `APPLE` links `libSDL2.a;libSDL2main.a`, copies `libSDL2.dylib`, and links 12 frameworks (17-30); else Linux `libSDL2.a` + copies `libSDL2.so` (32). FTXUI 5.0.0 via FetchContent, links `ftxui::screen/dom/component` (36-42).

### 4.5 `doc/CMakeLists.txt` (20 lines) and `doc/`

Doxygen via CMake's `doxygen_add_docs(doc ...)` (14-20) with `DOXYGEN_EXTRACT_ALL YES`, `BUILTIN_STL_SUPPORT`, project name "XJ music engine", brief "In-game runtime engine for XJ music.", `USE_MDFILE_AS_MAINPAGE doc/index.md`, `EXCLUDE_SYMBOLS` for nlohmann/std, `SITEMAP_URL https://engine-docs.xjmusic.com/sitemap.xml`. Inputs: `${CMAKE_SOURCE_DIR}/LICENSE.md` (**does not exist**; `engine/` has no LICENSE file, the repo root has `LICENSE.txt`), `include`, `src`, `doc/index.md`, `doc/xj-music-logo.png`. `doc/` contains only `CMakeLists.txt`, `index.md` (9 lines), the logo, and `.gitignore` (`html/`, `latex/`). There is no hand-written Doxyfile; no page documents the C++ API beyond the Doxygen extraction.

### 4.6 `vendor/` (offline tarballs, 35 MB)

| Archive | Size | Consumer | Hash-checked |
|---|---|---|---|
| `nlohmann-json-3.11.3.tar.gz` | 8.1 MB | library (PUBLIC include) | no |
| `SDL-2.30.5.tar.gz` | 7.8 MB | example only | MD5 `e3b9c6faf8b28567e069588c8f8fecef` |
| `googletest-1.15.0.tar.gz` | 872 KB | tests | no |
| `yaml-cpp-0.8.0.tar.gz` | 1.0 MB | tests | no |
| `FTXUI-5.0.0.tar.gz` | 237 KB | example only | no |

### 4.7 Platform quirks observed

- `long` fields: `Segment::beginAtChainMicros`, `SegmentChoiceArrangementPick::startAtSegmentMicros/lengthMicros`, `CraftWork` cutoff math all use `long`, while store/dub APIs use `unsigned long long`. On MSVC `long` is 32-bit, so chain time in those fields saturates at 2^31 µs ≈ 35.8 min. `WorkSettings::craftAheadMicros/dubAheadMicros/deadlineMicros` are `int` (`include/xjmusic/work/WorkSettings.h:17-19`).
- `CMAKE_BUILD_TYPE` is never set: CI's `cmake ..` on Linux/macOS builds with no optimization flags; on Windows the Visual Studio multi-config generator builds `Debug` for `cmake --build .`, and the distro "Release" step still copies `src/Debug/xjmusic.lib` (`engine_distro.yml:159-161`), i.e. **the shipped Windows library is a Debug build linked against the debug CRT**.
- No `CMAKE_OBJECT_PATH_MAX` or short-path mitigation for Windows; FetchContent/ExternalProject nest under `build/_deps/` and `build/vendor/src/`. Not observed failing in CI.
- No platform `#ifdef` in sources; `std::filesystem` is used in `src/Engine.cpp` and `ActiveAudio.h`.

---

## 5. Example app `xj_player` (`example/`, 7 files, ~1,270 lines)

What it is: a terminal UI around one `Engine` that visualizes segments and the audio schedule. **It does not play audio.**

- `main.cpp:12-25` usage: `<pathToProjectFile> [-template <name>] [-mode Auto|Taxonomy|Macro] [-craft s] [-dub s] [-deadline s] [-persistence s]`. Fixed `SDL_AudioSpec`: 48000 Hz, `AUDIO_S16SYS`, 2 channels, 1024 samples, no callback (52-57).
- `XJPlayer::RunEngine` (`XJPlayer.cpp:39-95`): `SDL_Init(SDL_INIT_AUDIO)`, `SDL_OpenAudioDevice(nullptr, 0, &spec, nullptr, 0)`, `engine->start(templateId)`, spawns a **detached** `std::thread` that loops: poll SDL events, `AtChainMicros = (SDL_GetTicks() − start) * 1000`, `RunEngineCycle(deviceId)`, `screen.PostEvent(Event::Custom)`, `SDL_Delay(382)` (`CYCLE_MILLISECONDS = 382`, `EngineUiBase.h:23`). The FTXUI loop runs on the main thread.
- `AtChainMicros` is declared **`Uint32`** (`EngineUiBase.h:56`): wraps after 2^32 µs ≈ 71.6 minutes.
- `RunEngineCycle` (`XJPlayer.cpp:97-118`): for each `AudioScheduleEvent` from `engine->RunCycle(AtChainMicros)`: `Create` → constructs `EngineScheduledAudio` (which `SDL_LoadWAV`s `buildPath + waveformKey`, `EngineScheduledAudio.cpp:24-28`, throwing `std::runtime_error` if missing) into `std::map<std::string, EngineScheduledAudio> Schedule`; `Update` → `.Update()` copies fields; `Delete` → `erase`. **`EngineScheduledAudio::Play()` (`SDL_QueueAudio` + `SDL_PauseAudioDevice(id, 0)`) and `Stop()` are defined (`EngineScheduledAudio.cpp:31-53`) but never called** (`grep -n 'Play()\|Stop()' example/*` shows only the definitions). The device stays paused. `Stop()` would also `SDL_CloseAudioDevice` the shared device. Six `// NEXT:` comments mark the unimplemented playback (`grep -rn NEXT example | wc -l` = 6).
- UI (`EngineUiBase.cpp`): `ScreenInteractive::Fullscreen()` (45); tabs `Engine`, `Template`, `Content` (`EngineUiBase.h:38-42`) via `Toggle` + `Container::Tab` (201-212); header shows template name, control mode, `Deadline`, `Dub ahead`, `Craft ahead` from `engine->getSettings()` (215-239); timeline iterates `engine->getSegmentStore()->readAllSegments()` (158-170) and per segment reads `readChoice(id, Program::Main)`, `readAllSegmentMemes`, `readAllSegmentChoices`, `readAllSegmentChoiceArrangementPicks` (449-684); audio mixer table over `Schedule` colored by dubbed/active (556-585); Template tab builds one menu per `MemeTaxonomy` category and an "Engage" `Button` → `engageMemeOverride()` → `engine->doOverrideMemes(memes)` (587-600). `SelectTemplate()` (51-106) is a menu over `getProjectContent()->getTemplates()` when `-template` is absent. Messages are never displayed.
- Threading: the engine thread mutates the store while FTXUI renderers on the main thread read it through raw pointers; there is no mutex. This is a live data race in the only shipped host.

---

## 6. CI and distribution (`.github/`)

Files: `workflows/engine_ci.yaml` (133 lines), `engine_pr.yaml` (83), `engine_distro.yml` (241), plus `workstation_*.yml`, `nothing_left_todo.sh`, `ISSUE_TEMPLATE/bug_report.yml` (the only issue template; no feature/spec template).

| Workflow | Trigger | Jobs (order) | Matrix |
|---|---|---|---|
| Engine CI | push to `main`, paths `engine/**` | `nothing_left_todo` → `build_and_test` → `deploy_docs` (GitHub Pages from `engine/build/doc/html` after `cmake .. && make doc`) | `ubuntu-latest, macos-13, macos-14, windows-latest`, `fail-fast: false` |
| Engine PR | `pull_request`, paths `engine/**` | `build_and_test` → `nothing_left_todo` | same 4 |
| Engine Distro | push of **any** tag (`tags: ['*']`) | `nothing_left_todo` → `verify_version_number` → `test_and_ship_static_library` | same 4 |

Build/test steps are identical everywhere: install cmake (`choco`/`apt`/`brew`), `mkdir build && cd build && cmake .. && cmake --build .`; test = `ctest --verbose` from `build` on Unix, `.\Debug\xjmusic_test.exe` from `build/test` on Windows (`engine_ci.yaml:65-85`). Python 3 is set up but unused. `actions/checkout@v2` and `setup-python@v2` throughout.

`verify_version_number` (`engine_distro.yml:21-77`) requires: tag == `v` + `workstation/gradle.properties` `version=`, and that equals the engine `CMakeLists.txt` ` VERSION ` value. Engine and workstation versions are locked together.

Artifacts on tag (`engine_distro.yml:147-241`), each uploaded to the GitHub Release (`svenstaro/upload-release-action@v2`, `overwrite: true`) and to S3 (`aws s3 cp` to `${{ secrets.AWS_S3_BUCKET }}`):

| Runner | Produced file | Contents |
|---|---|---|
| macos-13 (X64) | `xjmusic-v<V>-macos-intel.lib.zip` | `libxjmusic.a` renamed `xjmusic-v<V>.lib` |
| macos-14 (ARM64) | `xjmusic-v<V>-macos-silicon.lib.zip` | same |
| windows-latest | `xjmusic-v<V>-windows.lib.zip` | `src/Debug/xjmusic.lib` (Debug CRT) |
| ubuntu-latest | `xjmusic-v<V>-linux.lib.tar.gz` | `libxjmusic.a` renamed `.lib` |

**No headers, no nlohmann headers, no example, no docs, no checksums are shipped.** A consumer must pair the archive with `include/` from the same git tag by hand.

`nothing_left_todo.sh`: `grep --recursive -Iie "\btodo\b"` over `<engine|workstation>/{src,include,test}`; fails CI on any match. `example/` is not scanned (it uses `NEXT` instead). The `#!/usr/bin/env bash` is on line 3, after a comment, so it is not an effective shebang.

Absent from CI: sanitizers, coverage, fuzzing, clang-format/tidy (a `CLionCodeStyle.xml` exists, unenforced), any C-compiler compile check, dependency caching (SDL is rebuilt from source on every run because `example/` is unconditional), timing or benchmark jobs, Linux ARM, iOS/Android, Emscripten.

---

## 7. Tests

### 7.1 Layout and counts (`test/`, 115 `*Test.cpp`, 596 test cases, one binary)

| Directory | Files | Notes |
|---|---|---|
| `.` | 1 test (`EngineTest.cpp`) + `CMakeLists.txt` | end-to-end over the test project |
| `_helper/` | 10 | fixtures, see §7.2 |
| `_mock/` | 2 | gmock subclasses, see §7.2 |
| `_data/` | see §7.4 | copied to `build/test/_data` at configure |
| `audio/` | 2 | `ActiveAudioTest`, `AudioMathUtilsTest` |
| `content/` | 22 | one per content entity + `ContentEntityStoreTest` |
| `craft/` | 2 + 7 subdirs (59 files) | see §7.3 |
| `fabricator/` | 5 | `FabricatorTest`, `MarbleBagTest`, `NotePickerTest` (YAML-driven), `SegmentRetrospectiveTest`, `SegmentUtilsTest` |
| `meme/` | 3 | isometry, stack, taxonomy |
| `music/` | 13 | theory primitives; `ChordTest` is YAML-driven |
| `segment/` | 2 | `SegmentEntityStoreTest` (25 tests, names at lines 128-614), `SegmentMemeTest` |
| `util/` | 4 | config parser, CSV, string, value utils |
| `work/` | 1 | `WorkManagerTest` (1 test: marathon to 50 segments, `MARATHON_NUMBER_OF_SEGMENTS = 50`, 500 s wall-clock limit, `generateFixtures(content, 3)`) |

`SegmentEntityStoreTest` covers: `Create`, `Create_Get_Segment`, `Create_Get_Chain`, `CreateAll_ReadAll`, `ReadSegment`, `ReadLastSegmentId`, `ReadSegmentsFromToOffset` (+4 boundary variants), `ReadAllSegments`, `ReadAllSegmentChoiceArrangementPicks` (+`_ForChoice`), `ReadOrderedSegmentChords`, `ReadAll_hasNoLimit` (20 segments), `UpdateSegment` (+`_PersistPriorSegmentContent`, `_FailsToTransitionFromPlannedToCrafted`, `_FailsToChangeChain`), `GetSegmentCount`, `IsSegmentsEmpty`, `DeleteSegment`, `DeleteSegmentsAfter` (ids 0..9, after 5 → 6 remain), `DeleteSegmentsBefore` (before 5 → 5 remain). Not covered: `readSegmentAtChainMicros`, `readAllSegmentsSpanning`, `readAllSegmentsInState`, `readAllSegmentEntities`, `readChoiceHash`, `clear`, the `put` no-overwrite semantics, messages or metas.

### 7.2 Helpers and mocks

| File | Size | Provides |
|---|---|---|
| `_helper/ContentFixtures.{h,cpp}` | 680 / 1,454 lines | Named canonical content (`program3/4/5/9/10/15/35`, `instrument8/9`, `library1/2`, `template1`, sequences, bindings, binding memes, chords, voicings, patterns, events, voices, tracks: `ContentFixtures.h:90-231`); static builders for every content entity (`buildProject`, `buildLibrary`, `buildTemplate` ×3, `buildTemplateBinding` ×3, `buildProgram`, `buildProgramSequence`, `buildProgramSequenceBinding(+Meme)`, `buildProgramSequenceChord(+Voicing)`, `buildProgramVoice(+Track)`, `buildProgramSequencePattern(+Event)`, `buildInstrument`, `buildInstrumentMeme`, `buildInstrumentAudio`, plus short-name variants `buildAudio/buildMeme/buildSequence/...`); scenario fixtures `setupFixtureB1(store, includeBeat=true)`, `setupFixtureB2`, `setupFixtureB3` (intro/loop/outro pattern types, issues #203/#204/#257/#283), `setupFixtureB4_DetailBass` (issue #284); `generateFixtures(store, N)` random library of magnitude N; `random(...)`, `listOfRandomValues`, `listOfUniqueRandom` using `RANDOM_VALUE_FROM 0.3f .. TO 0.8f` |
| `_helper/SegmentFixtures.{h,cpp}` | 270 / 372 | `buildChain` ×5, `buildSegment` ×5, `buildSegmentChoice` ×8, `buildSegmentMeta`, `buildSegmentMeme`, `buildSegmentChord`, `buildSegmentChordVoicing`, `buildSegmentChoiceArrangement`, `buildSegmentChoiceArrangementPick` ×2 (`SegmentFixtures.h:38-266`) |
| `_helper/LoremIpsum.{h,cpp}` | 61 / 459 | constant vectors `COLORS`, `VARIANTS`, `PERCUSSIVE_NAMES`, `MUSICAL_CHORDS`, `MUSICAL_KEYS`, `SEQUENCE_TOTALS`, `PATTERN_TOTALS`, `ELEMENTS` |
| `_helper/TestHelpers.{h,cpp}` | 41 / 13 | `EXPECT_THROW_WITH_MESSAGE(stmt, msg)` macro; `TestHelpers::assertNote(expect, Note)` |
| `_helper/YamlTest.{h,cpp}` | 113 / 110 | `class YamlTest : public testing::Test`; `static YAML::Node loadYaml(prefix, filename)` = `YAML::LoadFile(prefix + filename)` (`YamlTest.cpp:28-33`); `getStr/getInt/getFloat/getBool(node, key)`; `assertSame`, `assertSameNote`, `assertSameNotes` |
| `_mock/MockFabricator.h` | 33 | `final : public Fabricator`, `MOCK_METHOD` for `getRetrospective`, `getSourceMaterial`, `getSegment`, `getMemeIsometryOfSegment` |
| `_mock/MockSegmentRetrospective.h` | 40 | 16 `MOCK_METHOD`s over the retrospective API |

The mocks are used only by `craft/CraftTest.cpp` and `fabricator/FabricatorTest.cpp`. Everything else uses real `SegmentEntityStore` + real `Fabricator`.

### 7.3 The craft test matrix (`test/craft/`, 59 files, 108 tests)

Seven layer directories: `background/`, `beat/`, `detail/`, `detail_hook/`, `detail_perc_loop/`, `macro_main/`, `transition/`, plus `ArrangementTest.cpp` (13) and `CraftTest.cpp` (14) at the top.

File naming encodes three axes:

| Axis | Values | File pattern |
|---|---|---|
| Segment type | `Initial`, `Continue`, `NextMain`, `NextMacro` | `Craft<Layer><Type>Test.cpp` |
| Voice presence | default (program chosen by sequence) vs. `ProgramVoice` (fixture B2, instrument per program voice) | `Craft<Layer>ProgramVoice<Type>Test.cpp` |
| Layering | multiple instruments on one voice type | `Craft<Layer>_LayeredVoicesTest.cpp` (present for `background`, `beat`, `detail_perc_loop`, `transition`; **absent for `detail` and `detail_hook`**) |

Layer names in files: `CraftBackground*`, `CraftBeat*`, `CraftDetail*`, `CraftHook*` (dir `detail_hook`), `CraftPercLoop*` (dir `detail_perc_loop`), `CraftFoundation*` (dir `macro_main`, plus `CraftSegmentOutputEncoderTest`, `CraftSegmentPatternMemeTest`, `MacroFromOverlappingMemeSequencesTest`), `CraftTransition*`. Counts per dir: background 11, beat 16, detail 15, detail_hook 10, detail_perc_loop 11, macro_main 7, transition 11.

Every matrix file follows one template (e.g. `beat/CraftBeatContinueTest.cpp`): fresh `SegmentEntityStore`; `ContentFixtures` with `setupFixtureB1/B2/B3`; chain `"Test Print #1"` (`Production`, `Fabricate`); segments 0..3 built by hand with `SegmentFixtures::buildSegment(chain, type, id, delta, state, key, total, intensity, tempo, storageKey, true)`, choices, memes (`Cozy, Classic, Outlook, Rosy`), chords (`A minor`@0, `D Major`@8); then

```cpp
const auto retrospective = SegmentRetrospective(store.get(), segment4->id);
auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment4->id, std::nullopt);
BeatCraft(&fabricator).doWork();
ASSERT_EQ(SegmentUtils::findFirstOfType(store->readAllSegmentChoices(segment4->id), Program::Type::Beat).has_value(), true);
```

(`CraftBeatContinueTest.cpp:141-162`). The assertions are mostly **existence** of a choice of the right type, occasionally a count; they do not pin which program/audio was chosen, so they pass under any RNG. The content-level oracle is the YAML arrangement suite (§7.4).

### 7.4 `_data/` contents (37 MB)

| Path | What | Consumer |
|---|---|---|
| `arrangements/arrangement_{0_no_chord_sections,1..11,12_sticky_bun_basic}.yaml` (13) | Each defines `bassInstrument/padInstrument/stabInstrument` (notes, `isTonal`, `isMultiphonic`), `*DetailProgram` (key, `doPatternRestartOnChord`, sequence/pattern/events with position/duration/tones), a segment with chords, and expected picks; header comments cite the arrangement-test Google doc and issue #221 | `craft/ArrangementTest.cpp` (`TEST_PATH_PREFIX = "_data/arrangements/"`, `loadAndRunTest` at 345-378 runs `Craft::craftNoteEventArrangements` and asserts picks) |
| `picking/picking_{4_5_notes_one,4_5_notes_two,4_5_notes_three,6_notes}.yaml` (4) | `input: {instrumentType, chord, range{from,to}, voicingNotes, eventNotes}` → `assertion: {picks, range}` | `fabricator/NotePickerTest.cpp` |
| `music_chord_test.yaml` | chord-name synonym map (`"C #5": C +`, ...) | `music/ChordTest.cpp` |
| `content_store_test.json` | serialized content (instruments, ...) | `content/ContentEntityStoreTest.cpp` |
| `test_project/TestProject.xj` | `{"demo": false, "project": {id e4f06956-…, name "Engine Test Project", platformVersion "1.9.6"}}` | `EngineTest.cpp:23` |
| `test_project/build/testproject.json` (23.5 KB) | flattened content, `platformVersion "1.7.41"`: 2 libraries (Test Instruments, Test Programs), **8 programs = 4 macro (Earth/Fire/Water/Wind) + 4 main**, 3 instruments (Test-Basslines, Test-Pads, Test-Perc-Loops), 10 instrumentAudios, 1 template "Test Template", 2 templateBindings, 12 programSequences, 24 bindings, 6 binding memes, 12 program memes, 4 sequence chords. **No `programVoices`, `programSequencePatterns` or events: there are no beat or detail programs in the test project.** | `Engine::loadProjectContent` crawls `build/*.json` (`Engine.cpp:167-199`) |
| `test_project/build/*.wav` (10) | 768 KB–3 MB each, named `<instrumentAudioId>.wav`, 30 MB total | `EngineTest` asserts `exists(buildPath / waveformKey)` |
| `test_project/libraries/**`, `templates/Test-Template/Test-Template.json` | the per-entity source tree the workstation writes; template config string sets `memeTaxonomy = [COLOR{RED,GREEN,BLUE}, SEASON{WINTER,SPRING,SUMMER,FALL}]`, `stickyBunEnabled = true`, `deltaArcEnabled = false`, `intensityAutoCrescendoEnabled = true`, `detailLayerOrder = [Bass,Stripe,Pad,Sticky,Stab]` | not read by tests (only `build/` is) |
| `test_project/.gitignore` | `!build` (keep), `render` (ignore) | |

All test data paths are **relative** (`_data/...`); the binary must run with cwd `build/test`.

### 7.5 The two end-to-end tests (`test/EngineTest.cpp`)

- `ReadsAndRunsProjectFromDisk` (51-85): `Engine(path, Taxonomy, craft 15, dub 30, deadline 1, persistence 60)`; asserts 2 taxonomy categories; `start(firstTemplate.id)`; loops `RunCycle(t)` for `t` in `[0, 60 s)` step 1 s; asserts every `waveformKey` exists under `getBuildPath()` and `> 10` distinct events. With craft 15 s and dub 30 s, dub-ahead exceeds craft-ahead.
- `CorrectlyDubsAudio` (87-387): `Engine(std::nullopt, Auto, ...)` with content built in-memory via `ContentFixtures` (macro/main/beat programs, Percussion-Loop, Pad-Chord, Hook-Loop, Drum-Event instruments at 60 BPM), then **~60 exact `EXPECT_TRUE(keys.find(...))` on strings shaped** `"{atChainMicros:012}_{Create|Update|Delete}_{ProperSlug(audio.name)}_at[{start:012},{stop:012}]_vol[{from×100:03},{to×100:03}]"` (`computeKey`, 37-44). This is the only micros-exact golden oracle in the suite and the obvious seed for a snapshot format.

---

## 8. Implications for V2 specs

**C ABI / build integration (`04-engine.md` §1)**
1. There is no `install()`, no export set, no package config, no generated version header, and no PIC. The `xjmusic_c` target spec must add all of these; a Rust `cdylib`/Python/Godot host linking the static archive on Linux needs `CMAKE_POSITION_INDEPENDENT_CODE ON` or a shared build, which in turn needs an export-macro header that does not exist today.
2. `project(... LANGUAGES CXX)`: the C-compiler header test needs `enable_language(C)` (or `LANGUAGES C CXX`) and a separate C target. Nothing in the tree is currently compiled as C.
3. The library is static by default (no keyword) and CI ships it as `*.lib` on all platforms; the Windows artifact is a **Debug** build (`src/Debug/xjmusic.lib`) because the Release step never passes `--config Release`. Any spec that promises release binaries must fix `engine_distro.yml:155-161`.
4. Shipped artifacts contain no headers. The C ABI spec should define a distributable layout (header + archive + version/ABI manifest + checksums) rather than inherit "bare `.lib` in a zip".
5. `add_subdirectory(example)` is unconditional, so every library consumer also configures SDL2 and FTXUI from vendored tarballs. Gate it behind an option (`XJ_BUILD_EXAMPLE`) before adding more targets.
6. `UUID` is `std::string` (`EntityUtils.h:28`) and `Segment::id` is `int`. The proposed 16-byte `active_audio_id`/`instrument_audio_id` arrays require parsing string UUIDs; verify `EntityUtils::computeUniqueId()` emits canonical 36-char UUIDs before committing to a binary id.
7. `long` time fields (`Segment::beginAtChainMicros`, pick `startAtSegmentMicros`/`lengthMicros`) are 32-bit on MSVC. A C ABI that exposes `uint64_t` chain micros must either widen these or document the ~35-minute Windows ceiling; the example app has its own 71-minute `Uint32` ceiling.
8. `Engine::RunCycle` returns events holding raw pointers into both stores; a flat C struct must copy values at the boundary and must be produced before any `deleteSegmentsBefore` in the same cycle.

**Snapshot JSON (`xj_engine_get_segments_json`, `xj_engine_get_messages_json`)**
9. No segment entity has JSON serialization today; only content entities and `StickyBun` do. The spec must define schemas for all 10 types from the field lists in §2, including enum string forms (reuse the existing `toString` maps).
10. `readAll*` return `std::set<const T*>` ordered by **pointer address**, so JSON emitted from them is in nondeterministic order unless the serializer sorts (by `id`, by `position`, by `startAtSegmentMicros`). Specify the sort key per array.
11. Messages have no timestamp, sequence number or cursor, and no reader exists in `src/`. `get_messages_json(since)` needs a monotonically increasing message sequence added at `Fabricator::addMessage` (`Fabricator.cpp:58-73`), or `since` must be a segment id.
12. `Segment.createdAt/updatedAt` and `Chain.updatedAt` are wall-clock millis set in constructors and in `updateSegment`. They must be excluded from any "byte-identical store" determinism comparison.
13. `readChain()` returns `optional<Chain*>` that is engaged-but-null when absent; a snapshot function must check the pointer, not `has_value()`.

**Determinism (`04-engine.md` §5)**
14. The proposal says "four `std::random_device`-seeded generators". The grep finds **seven sites / six distinct generators**: statics `StickyBun::gen` (`StickyBun.cpp:14-15`) and `ValueUtils::gen` (`ValueUtils.cpp:33-34`); members `Craft::gen` (`Craft.h:417-418`) and `MarbleBag::gen` (`MarbleBag.h:33-34`); locals in `MarbleBag::quickPick` (`MarbleBag.cpp:167-168`), `MarbleBag::quickBooleanChanceOf` (176-177) and `Fabricator::chooseVoicing` (`Fabricator.cpp:874-875`). Also, `ContentFixtures::random*` in tests draw from their own RNG.
15. Seeding alone will not make craft deterministic: every loop over a `readAll*` set iterates in allocation-address order, which varies run to run. The determinism spec must either change the store's return types (e.g. `std::vector` sorted by `(segmentId,id)`) or sort at every consumer. This is a larger change than the RNG swap and should be its own issue.
16. `SegmentMeta` keys are not unique per segment; with duplicates `getSegmentMeta` returns an address-ordered "first". Choice diagnostics (§8 of the engine chapter) should use a documented key namespace (`StickyBun_` is the only existing one) and the spec should decide whether `put` of a duplicate key replaces (sub-entity `put` currently **never overwrites**, `SegmentEntityStore.cpp:16`).

**Cues, rewrite, follow mode**
17. Rewrite granularity is whole segments: `deleteSegmentsAfter(id)` then re-craft from a `NextMacro` segment (`CraftWork.cpp:319-322`). Cutoff is `doCutoffLastSegment` (`333+`) which rewrites `total`/`durationMicros` and trims picks. Preview (§7) can copy the store cheaply because it is nine `std::map`s with value semantics; there is no copy constructor hazard, but the raw pointers in `ActiveAudio` must not escape the scratch copy.
18. `Engine::start` does not clear the segment store (header comment at `Engine.h:40` notwithstanding). A session re-join or template switch on one handle inherits stale segments; the C ABI lifecycle spec must define `xj_engine_start` semantics explicitly and add the missing `store->clear()`.
19. The example app reads the store from a second thread with no lock; the "one handle, one thread" rule in the ABI spec is not merely stylistic, it is the only thing standing between a host and dangling pointers from `deleteSegmentsBefore`.

**CI additions (C header test, ASan, fuzz, timing)**
20. One CTest entry wraps 596 gtest cases; `--gtest_filter` and per-test timing are only reachable by invoking the binary directly. Use `gtest_discover_tests` or at least `--gtest_output=xml` before adding timing jobs.
21. Test data is copied at configure time (`file(COPY)`), paths are relative to cwd `build/test`, and the fixture tree is 37 MB with 30 MB of WAV. A fuzz or ASan job that runs from another cwd will fail on `_data/` lookups.
22. `nothing_left_todo.sh` fails CI on the word "todo" in `src/include/test`; new specs that add `TODO` markers in code will break the build. `example/` is exempt (uses `NEXT`).
23. No sanitizer/coverage/format job exists; `gtest_force_shared_crt` is set too late (`test/CMakeLists.txt:155`); the CI runner images are `macos-13` (Intel) and `macos-14` (ARM), `ubuntu-latest`, `windows-latest`, with `actions/checkout@v2`. SDL is rebuilt from source every run; caching `build/_deps` and `build/vendor` would cut CI time substantially.
24. `engine_distro.yml` fires on **every** tag and requires engine and workstation versions to match; a 2.0.0 engine tag scheme (and the separate ABI 1.0.0) needs this job rewritten.

**Test plan (`04-engine.md` §11)**
25. The existing matrix asserts existence of choices, not content; follow-mode "follower equals conductor" tests and determinism tests need a value-level comparison of the store, which the snapshot JSON (sorted) can provide. `EngineTest::computeKey` (`EngineTest.cpp:37-44`) is the existing micros-exact golden format for schedule events and should be preserved or formalized.
26. The test project contains only macro/main programs and three loop-mode instruments (no beat/detail programs, no event-mode instruments), so EngineTest never exercises `BeatCraft`/`DetailCraft` on disk content; `CorrectlyDubsAudio` builds that content in memory. Follow-mode tests over "the test project" (§11) need either a richer `_data/test_project` or the in-memory fixtures.
27. `MockFabricator`/`MockSegmentRetrospective` exist and the craft stages take a `Fabricator*`, so an `ExternalFoundationCraft` can be unit-tested with the same two-line harness as every matrix file; `SegmentFixtures::buildSegmentMeta/Chord/ChordVoicing/Meme` already construct the exact entities a foundation record carries.
28. Docs: `doc/CMakeLists.txt:15` lists a nonexistent `LICENSE.md`, and both READMEs call the engine a shared library. Any docs job added for the C header should fix these so Doxygen runs clean.
