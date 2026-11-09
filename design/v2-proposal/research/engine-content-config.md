# Grounded map: engine content model and configuration (V1.9.6)

Scope: `engine/include/xjmusic/content/*.h`, `engine/src/content/*.cpp`, `engine/src/Engine.cpp`, `util/ConfigParser`, `util/EntityUtils`, `util/CsvUtils`, `meme/MemeTaxonomy` and the meme grammar parsers, plus `engine/test/_data`. Where the Java workstation (`workstation/src/main/java/io/xj/...`) is the *writer* of the format, it is cited so spec authors know what the engine is actually fed. All paths are relative to `/Users/nck/xj/xjmusic/` unless shown absolute. Line numbers are from `grep -n` against the working tree at commit `a94000b9a`.

This map underpins the additive-content-fields spec (`04-engine.md` section 9), the pack format (`06-commons.md`) and the `xj-content` crate (`03-architecture.md`, "xj-content (Rust)").

---

## 1. How a project is loaded

### 1.1 What the Java workstation writes (the format the engine consumes)

`workstation/src/main/java/io/xj/gui/project/ProjectManagerImpl.java`:

| Artifact | Path rule | Source |
|---|---|---|
| Project file | `<project dir>/<toAlphanumericHyphenated(projectName)>.xj` | line 622 |
| Library file | `libraries/<toAlphanumericHyphenated(library.name)>/<same>.json` | 651-652, 1440 |
| Program file | `libraries/<Library>/<toAlphanumericHyphenated(program.name)>/<same>.json`, content = `subsetForProgramId` | 656-658, 1447 |
| Instrument file | `libraries/<Library>/<toAlphanumericHyphenated(instrument.name)>/<same>.json`, content = `subsetForInstrumentId` | 662-664, 1505 |
| Instrument audio | `libraries/<Library>/<Instrument>/<waveformKey>` (sits beside the instrument JSON) | 674-676 |
| Template file | `templates/<toAlphanumericHyphenated(template.name)>/<same>.json`, content = `subsetForTemplateId` | 646-647, 1431 |
| Build output | `build/<toLowerHyphenatedSlug(projectName)>.json` plus one resampled audio file per `InstrumentAudio` named `<audio.id>.<ext>` | 385-452 |
| Writer | Jackson `writerWithDefaultPrettyPrinter()` on a `HubContent` | 1583-1589 |

The Build step (lines 385-452) copies *all* entities (`entityFactory.copyAll(content.get().getAll())`, line 404), runs `cleanupOrphans`, resamples every audio to the chosen container/rate/bits/channels, **rewrites `InstrumentAudio.waveformKey` to `<audioId>.<ext>`** (lines 425-428), **deletes audio records whose source file was missing** (436-441), and writes one JSON with everything. The load-side comment at lines 524-530 says the workstation itself reads only `libraries/**/*.json` and `templates/**/*.json` and ignores `render/` and `build/`.

### 1.2 What the C++ engine actually reads

`engine/src/Engine.cpp`:

| Step | Behavior | Lines |
|---|---|---|
| Constructor | `Engine(optional<string> pathToProjectFile, optional<ControlMode>, craftAhead, dubAhead, deadline, persistenceWindow)`; if a path is given, `loadProjectContent(path)`; then `templateContent->put(projectContent.get())` | 10-35 |
| Path check | Path must end in `.xj` or `std::invalid_argument("Path to project file must end in .xj ...")` | 142-145 |
| Existence | `std::filesystem::exists` or `std::invalid_argument("Path to project file does not exist: ...")` | 148-151 |
| Parse `.xj` | `ContentEntityStore(std::ifstream&)`; must yield a `project` or `std::runtime_error("Project file does not contain a project: ...")` | 154-160 |
| Build folder | `buildPathPrefix = parent_path(.xj) / "build/"`; if missing, `std::runtime_error("Build folder does not exist. Please build the project before playing it.")` | 164-173 |
| Crawl | `recursive_directory_iterator(build/)`; every regular file whose lower-cased extension is `json` is parsed as a `ContentEntityStore` and merged with `projectContent->put(&subFileContent)` | 177-195 |
| Error handling | `filesystem_error` and any `std::exception` are caught and **printed to stdout**; the loop is inside the `try`, so the first bad JSON aborts loading of the rest of `build/` but the engine construction continues | 197-204 |
| Build path accessor | `std::filesystem::path Engine::getBuildPath()` | 125-128 |

**The engine never reads `libraries/` or `templates/`.** It reads exactly: the `.xj` (for the `project` object) and every `*.json` under `build/`. The proposal's statement that the engine consumes "the `.xj` plus `libraries/**` and `templates/**`" (`03-architecture.md`, `02-where-v1-stands.md` line 27) describes the *workstation's* read path, not the engine's. A V2 `xj-content` that hands the engine a project must either produce a `build/` tree or use an in-memory load (see 1.4).

Audio path resolution is the **host's** job: the engine only skips picks whose `waveformKey` is empty (`src/work/DubWork.cpp:125`); the example player joins `engine->getBuildPath()` with `waveformKey` (`engine/example/XJPlayer.cpp:102`), as does `engine/test/EngineTest.cpp:76`.

### 1.3 Template scoping at `start()`

`Engine::start(optional<string> templateIdentifier)` (Engine.cpp 37-54): constructs `WorkManager(store, projectContent, settings)`; resolves the template with `getTemplateByIdentifier` (matches **name, then shipKey, then id**, first hit wins in `std::map<UUID,...>` iteration order; `src/content/ContentEntityStore.cpp:783-799`) or `getFirstTemplate()` (lowest UUID string; 801-804); clears `templateContent`; fills it from `forTemplate(tmpl)`; if no template resolves, the whole project content is used.

`ContentEntityStore::forTemplate` (811-901):
1. Adds the template and its `TemplateBinding`s.
2. For each binding: `Library` → the library; `Program`/`Instrument` → that entity **regardless of state**.
3. For each bound library: programs and instruments **only in `Published` state** (838-854).
4. Adds all children of included programs (memes, voices, tracks, sequences, bindings, binding memes, chords, voicings, patterns, events) and of included instruments (memes, audios).

`isDeleted` is **never consulted** anywhere outside the content headers (`grep -rn isDeleted src include | grep -v content/` → no hits). Craft additionally filters to `Published` (`src/craft/Craft.cpp:328-376`) and prefers directly bound entities via `Fabricator::isDirectlyBound` (`src/fabricator/Fabricator.cpp:917-920`).

### 1.4 In-memory JSON

`ContentEntityStore` has three constructors (`include/xjmusic/content/ContentEntityStore.h:100-114`; `.cpp:1006-1016`): default, `std::ifstream&`, and `std::string&` (`json::parse(input).get<ContentEntityStore>()`). The string form is tested (`test/content/ContentEntityStoreTest.cpp:175-200`, `FromJsonString`). **`Engine` itself exposes no in-memory entry point**: the only loader is `loadProjectContent(const std::string& pathToProjectFile)` (Engine.h:136). The C ABI's "load from a path or from in-memory JSON" (`04-engine.md` line 40) therefore needs a new `Engine` method that bypasses the `.xj`/`build/` checks and sets `buildPathPrefix` (or an audio-root) explicitly.

---

## 2. JSON payload shape

A payload is one JSON object (Java `HubContent`). `from_json(const json&, ContentEntityStore&)` at `src/content/ContentEntityStore.cpp:23-77` reads these top-level keys, **each optional**:

| Top-level key | Shape | Entity | Required fields (throw if absent) |
|---|---|---|---|
| `project` | single object (not an array) | `Project` | `id` |
| `libraries` | array | `Library` | `id`, `projectId` |
| `templates` | array | `Template` | `id`, `projectId`; `type`-style enum: none |
| `templateBindings` | array | `TemplateBinding` | `id`, `templateId`, `targetId`, `type` (via `json.at`) |
| `programs` | array | `Program` | `id`, `libraryId`, `state`, `type` (via `json.at`) |
| `programMemes` | array | `ProgramMeme` | `id`, `programId` |
| `programVoices` | array | `ProgramVoice` | `id`, `programId`, `type` (via `json.at`) |
| `programVoiceTracks` | array | `ProgramVoiceTrack` | `id`, `programId`, `programVoiceId` |
| `programSequences` | array | `ProgramSequence` | `id`, `programId` |
| `programSequenceBindings` | array | `ProgramSequenceBinding` | `id`, `programId`, `programSequenceId` |
| `programSequenceBindingMemes` | array | `ProgramSequenceBindingMeme` | `id`, `programId`, `programSequenceBindingId` |
| `programSequenceChords` | array | `ProgramSequenceChord` | `id`, `programId`, `programSequenceId` |
| `programSequenceChordVoicings` | array | `ProgramSequenceChordVoicing` | `id`, `programId`, `programSequenceChordId`, `programVoiceId` |
| `programSequencePatterns` | array | `ProgramSequencePattern` | `id`, `programId`, `programSequenceId`, `programVoiceId` |
| `programSequencePatternEvents` | array | `ProgramSequencePatternEvent` | `id`, `programId`, `programSequencePatternId`, `programVoiceTrackId` |
| `instruments` | array | `Instrument` | `id`, `libraryId`, `state`, `type`, `mode` (via `json.at`) |
| `instrumentMemes` | array | `InstrumentMeme` | `id`, `instrumentId` |
| `instrumentAudios` | array | `InstrumentAudio` | `id`, `instrumentId` |
| `demo` | boolean (Java `HubContent.demo`) | — | **ignored by the engine** |

Arrays are parsed with `json.at(key).get<std::set<T>>()`, and `ContentEntity::operator<` compares `id` only (`include/xjmusic/content/ContentEntity.h`). Consequences: entity order in the file is irrelevant; **two entities with the same `id` in one file collapse silently to one** (set semantics); across files, `ContentEntityStore::put(const ContentEntityStore*)` (924-979) overwrites by `id`, last file wins, in directory-iteration order.

Entities are stored in `std::map<UUID, T>` per type (ContentEntityStore.h:42-60). `UUID` is `std::string` (`include/xjmusic/util/EntityUtils.h:28`); no UUID validation of any kind. `EntityUtils::computeUniqueId()` (`src/content/EntityUtils.cpp:10-14`) produces 12-hex-digit counter ids and is used for segment entities, not content; issue #400 (EntityUtils.h:24) records the intent to move to integer ids eventually.

### 2.1 Field-reading rules (`src/content/EntityUtils.cpp`)

| Helper | Behavior | Lines |
|---|---|---|
| `setRequired(json, key, UUID&)` | Missing → `std::invalid_argument("Missing required UUID: <key>")`; non-string → `std::invalid_argument("Invalid value for UUID: ...")` | 16-25 |
| `setIfNotNull(..., std::string&)` | Set **only if** `is_string()` | 27-35 |
| `setIfNotNull(..., float&)` | Set **only if** `is_number_float()` — an integer literal such as `"tempo": 120` is **silently ignored** and the field keeps its default `0` | 37-45 |
| `setIfNotNull(..., bool&)` | Set only if `is_boolean()` | 47-55 |
| `setIfNotNull(..., int&)` | Set only if `is_number_integer()` | 57-65 |
| `setIfNotNull(..., long long&)` | Set only if `is_number_unsigned()` — a negative or float `updatedAt` is ignored | 67-75 |
| Enum fields | `json.at("type").get<std::string>()` then `parseX()`; a **missing** enum key throws nlohmann `out_of_range` (uncaught in `from_json`); an **unknown** value maps to a default (see section 4) | e.g. Instrument.h:112-114 |
| `config` | Read only if present **and** `is_string()`; then parsed by the config class constructor; otherwise the default config object | Template.h:40-43, Instrument.h:117-120, Program.h:85-88 |
| Unknown keys | Never inspected; **ignored** | by construction |
| `null` values | Fail the type check → field keeps its default | by construction |

Java emits floats as `120.0` (Jackson `Float`), so V1 files pass the `is_number_float` test. A Rust writer using `serde_json` must make sure `f32` fields serialize with a fractional part or the engine will read zeros. (`serde_json` writes `120.0` for `f32` 120, so default behavior is safe; a hand-rolled or `Value`-based writer may not be.)

---

## 3. Entity catalogue (C++ side)

All classes derive from `ContentEntity { UUID id; }`. Defaults shown are the C++ member initializers. "J" marks fields that exist in the Java pojo but not in C++.

| Entity (header) | Fields: `jsonKey` → C++ type (default) |
|---|---|
| `Project` (Project.h) | `id`; `name` string; `platformVersion` string; `isDeleted` bool(false); `updatedAt` long long(now) |
| `Library` (Library.h) | `id`; `projectId`*; `name`; `isDeleted`(false); `updatedAt`(now) |
| `Template` (Template.h) | `id`; `projectId`*; `name`; `config` → `TemplateConfig`; `shipKey` string; `isDeleted`(false); `updatedAt`(now) |
| `TemplateBinding` (TemplateBinding.h) | `id`; `templateId`*; `type`* enum {Library, Program, Instrument}; `targetId`* |
| `Program` (Program.h) | `id`; `libraryId`*; `state`* enum; `type`* enum; `key` string; `tempo` float(0); `name`; `config` → `ProgramConfig`; `isDeleted`(false); `updatedAt`(now) |
| `ProgramMeme` (ProgramMeme.h) | `id`; `programId`*; `name` |
| `ProgramVoice` (ProgramVoice.h) | `id`; `programId`*; `type`* `Instrument::Type` (default Drum); `name`; `order` float — **but see the key-mapping defect in section 3.1** |
| `ProgramVoiceTrack` (ProgramVoiceTrack.h) | `id`; `programId`*; `programVoiceId`*; `name`; `order` float — **same defect** |
| `ProgramSequence` (ProgramSequence.h) | `id`; `programId`*; `name`; `key`; `intensity` float; `total` int (beats) |
| `ProgramSequenceBinding` | `id`; `programId`*; `programSequenceId`*; `offset` int |
| `ProgramSequenceBindingMeme` | `id`; `programId`*; `programSequenceBindingId`*; `name` |
| `ProgramSequenceChord` | `id`; `programId`*; `programSequenceId`*; `name` (chord symbol); `position` float (beats) |
| `ProgramSequenceChordVoicing` | `id`; `programId`*; `programSequenceChordId`*; `programVoiceId`*; `notes` string (CSV of notes) |
| `ProgramSequencePattern` | `id`; `programId`*; `programSequenceId`*; `programVoiceId`*; `name`; `total` int |
| `ProgramSequencePatternEvent` | `id`; `programId`*; `programSequencePatternId`*; `programVoiceTrackId`*; `velocity` float; `position` float; `duration` float; `tones` string |
| `Instrument` (Instrument.h) | `id`; `libraryId`*; `state`*; `type`*; `mode`*; `name`; `config` → `InstrumentConfig`; `volume` float(0); `isDeleted`(false); `updatedAt`(now) |
| `InstrumentMeme` | `id`; `instrumentId`*; `name` |
| `InstrumentAudio` (InstrumentAudio.h:17-27) | `id`; `instrumentId`*; `name`; `waveformKey`; `transientSeconds` f; `lengthSeconds` f; `loopBeats` f; `tempo` f; `intensity` f; `event` string; `volume` f; `tones` string. (J: `legngthSeconds`, a typo field in `io/xj/model/pojos/InstrumentAudio.java:20`, absent from emitted JSON samples) |

`*` = required (throws if absent). `Instrument.volume` defaults to **0** in C++ when absent, and `ActiveAudio` multiplies by it (`src/audio/ActiveAudio.cpp:24-25`: `fromVolume = fromIntensity * pick->amplitude * instrument->volume * audio->volume`), so an instrument JSON without `volume` is silent. Java always writes `volume: 1.0`.

Deletion/state fields that exist but are unused by the engine: `isDeleted` (all), `updatedAt` (content; only segment `updatedAt` is used), `Project.platformVersion`, `Library.name`. `Template.shipKey` is used to name the chain (`src/fabricator/ChainUtils.cpp:53,60`, `TemplateUtils.cpp:8`).

### 3.1 Defect: `ProgramVoice`/`ProgramVoiceTrack` read the wrong JSON keys

`include/xjmusic/content/ProgramVoice.h:40-41` and `ProgramVoiceTrack.h:33-34`:

```cpp
EntityUtils::setIfNotNull(json, "duration", entity.name);
EntityUtils::setIfNotNull(json, "tones", entity.order);
```

The Java writer emits `"name"` and `"order"` (pojo fields `ProgramVoice.java:19-20`; sample `test/_data/content_store_test.json`: `{"id":..., "programId":..., "type":"Drum", "name":"Drums", "order":null}`). So every voice and track loaded from JSON has an **empty `name`** in the C++ engine. Introduced in `c804d29c6` (2024-07-05). Consumers that depend on the name: `Fabricator::getTrackName` (`src/fabricator/Fabricator.cpp:837-845`) returns `track->name`; `Craft.cpp:896` sets `pick.event` from it; `Craft.cpp:960` scores audio `+300` when `getTrackName(event) == audio->event`; `BeatCraft.cpp:38` uses `ProgramVoice::getNames`, `:80` uses `getTrackNamesOfVoice`; `Fabricator.cpp:222` matches previous voices by name. Runtime effect was not executed here, but by inspection event-name matching between tracks and audio cannot work for JSON-loaded content, and the engine test project (section 8) contains no Beat/Detail programs, so `EngineTest` does not exercise it. The `xj-content` spec must write `name`/`order` (the Java format) and the engine spec should include a one-line fix plus a round-trip fixture with a Beat program.

---

## 4. Enumerations, exactly

| Enum | C++ declaration order | String forms accepted | Unknown string → |
|---|---|---|---|
| `Instrument::Type` (Instrument.h:16-27) | `Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition` | identical, case-sensitive (`src/content/Instrument.cpp:9-20`) | `Drum` (41-46) |
| `Instrument::Mode` (Instrument.h:29-33) | `Chord, Event, Loop` | identical (25-29) | `Event` (49-54) |
| `Instrument::State` (35-38) | `Draft, Published` | identical | `Draft` (57-62) |
| `Program::Type` (Program.h:16-21) | `Macro, Main, Beat, Detail` | identical (`Program.cpp:13-18`) | `Main` (30-35) |
| `Program::State` | `Draft, Published` | identical | `Draft` |
| `TemplateBinding::Type` (TemplateBinding.h:16-20) | `Library, Program, Instrument` | identical (`TemplateBinding.cpp:14-18`) | `Library` (27-32) |

Note the C++ `Instrument::Type` is declared alphabetically while Java `InstrumentType` (`io/xj/model/enums/InstrumentType.java:5-16`) is `Drum, Bass, Pad, Sticky, Stripe, Stab, Hook, Percussion, Transition, Background`. Any code that sorts by enum ordinal (e.g. `TemplateConfig::formatInstrumentTypeList(const std::set<>&)`, TemplateConfig.cpp:223-227, which sorts the set) will order differently on the two sides. Strings are the stable interchange form.

---

## 5. `InstrumentAudio` field semantics and consumers

| Field | Used by | Meaning in practice |
|---|---|---|
| `waveformKey` | `DubWork.cpp:125` (skip if empty); host joins with build path | File name relative to the audio root. In `libraries/` it is the original name (e.g. `Test-Pads-AH-120-Loop-01-Low-Synth-Cm-X.wav`); in `build/` it is `<audioId>.wav` |
| `transientSeconds` | `DubWork.cpp:131-134` | Audio is started `transientSeconds` *before* the pick time so the transient lands on the beat |
| `lengthSeconds` | `DubWork.cpp:151` | Stop time when the pick has no explicit length |
| `loopBeats` | `DubWork.cpp:120,134` | Loop length in beats for Loop-mode instruments |
| `tempo` | craft (loop selection); `11-resolutions.md` notes `85BPM`-style memes as the de-facto convention | Recorded tempo; no stretch exists today |
| `intensity` | `AudioMathUtils.cpp:9-11` with `TemplateConfig.intensityThreshold` | Intensity-matching for layered audio |
| `event` | `Craft.cpp:960` scored against track name; `TransitionCraft` large/medium/small names | Event name this sample answers to (`X` for "any") |
| `volume` | `ActiveAudio.cpp:24-25` | Linear gain multiplier |
| `tones` | `Craft.cpp:961,992` (note picking) | CSV of note names (or `X`) |
| `name` | — | Display only |

---

## 6. Config strings: the `ConfigParser` dialect

`Template.config`, `Program.config`, `Instrument.config` are **strings inside the JSON** holding a HOCON-like text. The Java side parses them with Typesafe Config (full HOCON, `ConfigFactory.parseString(text).withFallback(parseString(DEFAULT))`, e.g. `io/xj/model/TemplateConfig.java:173`) and re-emits them through `toString()` in a canonical sorted `key = value` multiline form (`InstrumentConfig.java:84-92`, `ProgramConfig.java:75-81`). All JSON samples in the repository carry that canonical form.

The C++ side is a **hand-written subset parser**, `engine/src/util/ConfigParser.cpp` (546 lines), class docs in `include/xjmusic/util/ConfigParser.h:184-186` ("A lightweight HOCON parser and formatter").

### 6.1 Accepted syntax

| Construct | Rule | Lines |
|---|---|---|
| Members | Input is split on **newlines**; a trimmed line containing `=` or `:` at bracket depth 0 starts a member; any other line is appended (with `\n`) to the previous member. Depth is tracked by counting `{ } [ ]` characters **anywhere on the line**, including inside quotes | 228-263 |
| Key | Text before the first `=` (or `:` if no `=`), trimmed; **no quote stripping at top level** (object members do strip quotes: line 136) | 253-260 |
| Value kinds | `[...]` → list; `{...}` → object; else single | 216-225 |
| Single value typing | Regex `^[-+]?[0-9]*\.?[0-9]+$`: matches → **float if the text contains `.`, else int** (`std::round`); `true/True/TRUE` and `false/False/FALSE` → bool; anything else → string with one surrounding pair of `"` stripped | 17-18, 43-47, 55-74 |
| List | Split on top-level commas; `{...}` members become objects; other members single values (unquoted strings allowed: `[RED,GREEN,BLUE]`) | 177-208 |
| Object | Members separated by newline or top-level comma; `key = value` or `key: value`; values are single or **simple list only** (no nested objects, no list-of-objects inside an object) | 99-146 |
| Bare string as list | `getListValue` on a single string value CSV-splits it (`deltaArcBeatLayersToPrioritize = Hook` works) | 328-344 |
| Defaults | `ConfigParser(input, defaults)` copies every default key **not present** in input | 266-272 |
| Lookups | `getSingleValue/getListValue/getObjectValue(key)` throw `ConfigException("Key not found")` or `"Value is not a ..."` | 317-355 |
| Typed getters | `getFloat()` on an int-typed value throws `ConfigException("Value is not a float")`; `getInt()` on a float-typed value throws `"Value is not an integer"` | 283-304 |

Not supported (will mis-parse or throw): comments (`#`, `//`), path keys (`a.b = 1`), substitutions (`${x}`), `include`, unquoted strings containing `=`/`:`/brackets, single quotes, scientific notation, triple-quoted strings, duration/size units, `+=`, trailing commas inside objects spanning one line, and nested objects inside objects. Because typing is by literal shape, **`intensityAutoCrescendoMaximum = 1` throws at `getFloat()` while `1.0` works, and `mixerDspBufferSize = 1024.0` throws at `getInt()`**. Java/Typesafe accepts both. `test/util/ConfigParserTest.cpp` (lines 10-345) is the authoritative set of accepted examples, including the JSON-styled `"memes":["A","B"]` form (312-340).

### 6.2 Failure propagation

`ConfigException` thrown inside `TemplateConfig(const std::string&)` escapes `from_json(Template)` → `get<std::set<Template>>()` → `ContentEntityStore(std::ifstream&)`. In `Engine::loadProjectContent` the build-folder crawl catches it as `std::exception`, prints `General error! ...` and **stops loading further build files** (Engine.cpp 177-204); a bad config in the `.xj` itself propagates out of the `Engine` constructor. There is no per-entity recovery and no error object returned to the host.

### 6.3 Unknown keys in config strings

Unknown keys are parsed into the internal `config` map and never read: **silently ignored**. This is what makes `commitHorizonBeats`, `followHoldMaxBars`, `allowPitchShift`, `pitchShiftRange`, `allowStretch` safe to add for 1.9.x readers, as `04-engine.md` section 9 claims. `toString()` on each config class emits **only the known keys** (TemplateConfig.cpp:268-314; ProgramConfig.cpp:29-47; InstrumentConfig.cpp:47-69), so a 1.9.x round-trip through the C++ formatter would **drop** V2 keys. The Java formatter does the same (fixed key maps). The workstation's save path is Java, so today V2 keys survive only if the writer preserves them.

---

## 7. `TemplateConfig`: complete key list with defaults

Defaults from `engine/src/content/TemplateConfig.cpp:12-96` (`TemplateConfig::DEFAULT`), parsed at 230-265, members declared in `include/xjmusic/content/TemplateConfig.h:26-55`. "Consumers" are files outside `src/content/` that read the key (via member or getter).

| Key | C++ type | Default | Consumers |
|---|---|---|---|
| `choiceMuteProbability` | `map<Instrument::Type,float>` | all ten types `0.0` | `Craft.cpp:381` via `getChoiceMuteProbability` (missing type → `0.0`, TemplateConfig.cpp:322-328) |
| `deltaArcBeatLayersIncoming` | int | `1` | `BeatCraft.cpp` |
| `deltaArcBeatLayersToPrioritize` | `set<string>` | `["kick"]` | `BeatCraft.cpp` |
| `deltaArcDetailLayersIncoming` | int | `1` | `DetailCraft.cpp` |
| `deltaArcEnabled` | bool | `false` | `Craft.cpp` |
| `detailLayerOrder` | `vector<Instrument::Type>` (ordered) | `["Bass","Pad","Stab","Sticky","Stripe"]` | `DetailCraft.cpp` |
| `dubMasterVolume` | `map<Type,float>` | all `1.0` | **none in engine** (getter exists, `getDubMasterVolume`, missing type → `0.0`) |
| `eventNamesLarge` | `set<string>` | `["BIG","HIGH","LARGE","PRIMARY"]` | `TransitionCraft.cpp` |
| `eventNamesMedium` | `set<string>` | `["MEDIUM","MIDDLE","REGULAR","SECONDARY"]` | `TransitionCraft.cpp` |
| `eventNamesSmall` | `set<string>` | `["LITTLE","LOW","SMALL"]` | `TransitionCraft.cpp` |
| `instrumentTypesForAudioLengthFinalization` | `set<Type>` | `["Bass","Pad","Stab","Sticky","Stripe"]` | `Craft.cpp` |
| `instrumentTypesForInversionSeeking` | `set<Type>` | `["Pad","Stab","Sticky","Stripe"]` | `Craft.cpp:850` via `instrumentTypesForInversionSeekingContains` |
| `intensityAutoCrescendoEnabled` | bool | `true` | `MacroMainCraft.cpp` |
| `intensityAutoCrescendoMaximum` | float | `0.8` | `MacroMainCraft.cpp` |
| `intensityAutoCrescendoMinimum` | float | `0.2` | `MacroMainCraft.cpp` |
| `intensityLayers` | `map<Type,int>` | Background 3, Bass 1, Drum 1, Hook 3, Pad 3, Percussion 3, Stab 2, Sticky 2, Stripe 2, Transition 3 | `Craft.cpp:314`, `TransitionCraft.cpp:122`, `DubWork.cpp:164,169`, `AudioMathUtils.cpp` (missing type → `0`) |
| `intensityThreshold` | `map<Type,float>` | all `0.5` | `DubWork.cpp:165,170`, `AudioMathUtils.cpp` (missing type → `0.0`) |
| `mainProgramLengthMaxDelta` | int | `280` | `MacroMainCraft.cpp`, `Fabricator.cpp` |
| `memeTaxonomy` | `MemeTaxonomy` | `[{memes=["BLUE","GREEN","RED"] name="COLOR"},{memes=["FALL","SPRING","SUMMER","WINTER"] name="SEASON"}]` | `Fabricator.cpp:396,401,954,985,1058` (via `chain->config`), `WorkManager.cpp:28` |
| `mixerCompressAheadSeconds` | float | `0.05` | **none in engine** |
| `mixerCompressDecaySeconds` | float | `0.125` | none |
| `mixerCompressRatioMax` | float | `1.0` | none |
| `mixerCompressRatioMin` | float | `0.3` | none |
| `mixerCompressToAmplitude` | float | `1.0` | none |
| `mixerDspBufferSize` | int | `1024` | none |
| `mixerHighpassThresholdHz` | int | `30` | none |
| `mixerLowpassThresholdHz` | int | `15000` | none |
| `mixerNormalizationBoostThreshold` | float | `1.0` | none |
| `mixerNormalizationCeiling` | float | `0.999` | none |
| `stickyBunEnabled` | bool | `true` | `Fabricator.cpp` |

Thirty keys. The ten `mixer*` keys and `dubMasterVolume` are **parsed and validated but unused** by the C++ engine (they drove the Java mixer); they are exactly the knobs a V2 `xj-dsp` would own. The `Chain` carries a copy of the template config (`ChainUtils.cpp:52`, `chain.config = tmpl->config`), and the `Fabricator` reads taxonomy from `chain->config`.

Map-valued keys are parsed by `parseInstrumentTypeFloatMap/IntMap` (TemplateConfig.cpp:102-123): each object key goes through `Instrument::parseType`, so a misspelled type key silently becomes `Drum` and **overwrites** the Drum entry.

Equality (`operator==`, 354-386) compares every key; taxonomy compared via `toString()`.

---

## 8. `ProgramConfig` and `InstrumentConfig`

`ProgramConfig` (`src/content/ProgramConfig.cpp:12-26`; header `ProgramConfig.h`):

| Key | Type | Default | Consumers |
|---|---|---|---|
| `barBeats` | int | `4` | `Craft.cpp`, `TransitionCraft.cpp`, `work/CraftWork.cpp` |
| `cutoffMinimumBars` | int | `2` | `work/CraftWork.cpp` (the override cut-point the proposal cites in `04-engine.md` section 5) |
| `doPatternRestartOnChord` | bool | `false` | `Craft.cpp` |

`InstrumentConfig` (`src/content/InstrumentConfig.cpp:15-44`; header `InstrumentConfig.h`):

| Key | Type | Default | Consumers |
|---|---|---|---|
| `isAudioSelectionPersistent` | bool | `true` | `Craft.cpp`, `TransitionCraft.cpp` |
| `isMultiphonic` | bool | `false` | `Craft.cpp` |
| `isOneShot` | bool | `false` | `Craft.cpp`, `Fabricator.cpp:927-929` |
| `isOneShotCutoffEnabled` | bool | `true` | `Craft.cpp`, `Fabricator.cpp` |
| `isTonal` | bool | `false` | `Craft.cpp` |
| `oneShotObserveLengthOfEvents` | `vector<string>`, each normalized by `StringUtils::toMeme` | `[]` | `Fabricator.cpp:922-925` via `oneShotObserveLengthOfEventsContains` |
| `releaseMillis` | int | `5` | `audio/ActiveAudio.cpp` |

Java defaults are identical (`io/xj/model/ProgramConfig.java:21-25`, `InstrumentConfig.java:24-31`).

---

## 9. Meme taxonomy and meme grammar

### 9.1 Two serializations of a taxonomy

`engine/src/meme/MemeTaxonomy.cpp`:

| Form | Where | Syntax | Lines |
|---|---|---|---|
| **Structured** (what `TemplateConfig` uses) | `TemplateConfig.cpp:251-253` → `MemeTaxonomy::fromList` → `MemeCategory(const MapStringToOneOrManyString&)` | HOCON list of objects: `memeTaxonomy = [ { memes = ["A","B"] name = "CAT" }, ... ]`; unquoted `[A,B]` also accepted; `name` missing → `"CATEGORY"` | 77-84, 215-231 |
| **Compact string** (legacy / `toString()` form) | `MemeTaxonomy(const std::string&)`, `fromString` | `CAT1[A,B,C];CAT2[D,E]` — categories separated by **`;`** (`CATEGORY_SEPARATOR = ';'`, line 127), memes by `,`; per-category regex `^([a-zA-Z ]+)\[([a-zA-Z, ]+)\]$` (line 17) — **letters, spaces and commas only**, no digits, no `!`/`$` | 40-74, 130-138 |

Both forms sanitize every name and meme with `sanitize()` = `toUpperCase(toAlphabetical(trim(raw)))` (line 20-22), where `toAlphabetical` strips `[^a-zA-Z]` (`src/util/StringUtils.cpp:15`). So **digits and punctuation are removed from taxonomy members**: `85BPM` → `BPM`, `3NOTE` → `NOTE`, `CALL TO ACTION` → `CALLTOACTION`. Numeric and grammar memes therefore cannot be taxonomy members; the engine's `xj-base` conventions (`85BPM`, `3NOTE`, `!X`, `$X`) live outside the taxonomy, which matches the proposal's split in `06-commons.md` ("A shared taxonomy").

`MemeTaxonomy::toString()` (153-161) emits the compact form with categories and memes sorted: `COLOR[BLUE,GREEN,RED];SEASON[FALL,SPRING,SUMMER,WINTER]`. `TemplateConfig::formatMemeTaxonomy` (TemplateConfig.cpp:187-210) emits the structured form, also sorted. The proposal's `pack.toml` example (`palettes = ["ELEMENT[EARTH, WIND, FIRE, WATER]"]`) is one compact-form category per TOML array element, which the regex accepts (spaces allowed). Joining several with `;` is the engine's own multi-category string syntax; commas between categories are **not** accepted.

Semantics: `MemeCategory::isAllowed(targets)` passes when **at most one** of the category's memes is present (97-105); `MemeTaxonomy::isAllowed` requires every category to pass (190-197). Empty-memes categories are allowed and named `CATEGORY`.

Where the taxonomy is obtained at runtime: `WorkManager` constructor takes it from `(*content->getTemplates().begin())->config.memeTaxonomy` (`src/work/WorkManager.cpp:28`) where `getTemplates()` returns `std::set<const Template*>` — ordered by **pointer value**, so with several templates the `Taxonomy` control mode's seed is from an arbitrary one. `ContentEntityStore::getMemeTaxonomy()` (806-809) uses `getFirstTemplate()` (lowest id). `Engine::getMemeTaxonomy()` (Engine.cpp:92-95) asks `templateContent`, which after `start()` holds exactly one template, so the public accessor is deterministic.

### 9.2 Meme grammar (outside the taxonomy)

`engine/src/meme/Parse*.cpp`, composed in `MemeStack::isAllowed` (`src/meme/MemeStack.cpp:34-61`) followed by `taxonomy.isAllowed`:

| Form | Regex | Rule |
|---|---|---|
| Anti `!X` | `^!(.+)$` (ParseAnti.cpp) | `!X` and `X` may not coexist |
| Numeric `NX` | `^([0-9]+)(.+)$` (ParseNumeric.cpp) | two memes with the same body and different numeric prefix conflict (`3NOTE` vs `4NOTE`, `85BPM` vs `120BPM`) |
| Unique `$X` | `^\$(.+)$` (ParseUnique.cpp) | at most one `$X` with a given body across the stack |
| Strong `X!` | `^(.+)!$` (ParseStrong.cpp) | a target `X!` is allowed only if some source has body `X` |

Meme normalization for content memes is `StringUtils::toMeme`: strip `[^a-zA-Z0-9!$]` then upper-case (`StringUtils.cpp:18,56-60`). Event names use `toEvent`: strip `[^a-zA-Z]` (line 17). Slugs for file names use `[^a-zA-Z0-9]` (line 20).

---

## 10. Unknown-field handling, summarized

| Layer | Unknown key | Missing key | Wrong type |
|---|---|---|---|
| Top-level payload (`from_json` store) | ignored | section absent → empty | `get<std::set<T>>` throws nlohmann `type_error` |
| Entity object | ignored | required id → `invalid_argument`; enum via `json.at` → `out_of_range`; optional → default | optional: silently ignored; required id → `invalid_argument` |
| Config string (`ConfigParser`) | parsed, never read, **dropped on re-format** | filled from `DEFAULT` | `ConfigException` at the typed getter; escapes to the store constructor |
| Config object keys typed by `Instrument::Type` | unknown type name → `Drum` (overwrites) | type absent from map → getter default `0`/`0.0` | `ConfigException` |
| Taxonomy | n/a | `name` → `"CATEGORY"`; `memes` → empty | non-list `memes` → empty set (MemeTaxonomy.cpp:25-37) |

So the eight proposed additive fields (`allowPitchShift`, `pitchShiftRange`, `allowStretch` in `InstrumentConfig`; `commitHorizonBeats`, `followHoldMaxBars` in `TemplateConfig`; `contentHash` on `InstrumentAudio`; `originPack`, `originVersion` on every entity) are **ignored by 1.9.6 on read** and **absent from the codebase today** (`grep -rIl` over `engine/` and `workstation/` returns zero files for every one of them). The only hazard is the re-format path (section 6.3) and the `is_number_float` rule for any new float field.

---

## 11. Store API seams relevant to the C ABI and `xj-content`

`include/xjmusic/content/ContentEntityStore.h`:

- Per-entity methods generated by `CONTENT_STORE_CORE_HEADERS` (lines 33-37) for all 18 types: `std::optional<const T*> getT(const UUID&)`, `std::set<const T*> getTs()`, `ContentEntityStore setTs(const std::set<T>&)` (replaces), `T* put(const T&)` (upsert by id). Implementations at `.cpp:80-121`, with `catch(...)` swallowing and printing errors.
- Whole-store: `put(const ContentEntityStore*)` merge (924-979), `clear()` (903-922), `forTemplate(const Template*)` (811-901), `getProject()` (160-163, first by id), `getTemplateByIdentifier` (783-799), `getFirstTemplate` (801-804), `getMemeTaxonomy` (806-809), `toString()` count summary (981-1002).
- Relationship queries (header lines 120-600): `getAudiosOfInstrument`, `getBindingsOfTemplate`, `getProgramsOfLibrary`, `getInstrumentsOfLibrary`, `getSequencesOfProgram`, `getTrackNamesOfVoice`, `getVoicingsOfChordAndVoice`, `getInstrumentsOfTypesAndModes`, `getAvailableOffsets`, etc. All are linear scans over the maps.
- **No `to_json` anywhere** in `include/xjmusic/content`, `include/xjmusic/util` or `src/content` (grep returns nothing). The engine is read-only with respect to content; serialization, stable ordering and pretty-printing are entirely the writer's (today Java, tomorrow `xj-content`) responsibility.
- JSON library: vendored `nlohmann-json 3.11.3` (`engine/vendor/nlohmann-json-3.11.3.tar.gz`), `using json = nlohmann::json` in `EntityUtils.h:14`.

---

## 12. `engine/test/_data` contents

56 files, 37 MB.

| Path | What |
|---|---|
| `content_store_test.json` | Full 18-key payload: 2 of every entity type, 1 library, 2 templates, 3 template bindings, 1 project (as `project` object). Fixture for `ContentEntityStoreTest::FromJsonFile/FromJsonString` |
| `music_chord_test.yaml` | Chord parsing fixtures |
| `arrangements/arrangement_0_no_chord_sections.yaml` … `arrangement_12_sticky_bun_basic.yaml` (13 files) | Craft arrangement scenario fixtures (YAML, via `test/_helper/YamlTest`) |
| `picking/picking_4_5_notes_one/two/three.yaml`, `picking_6_notes.yaml` | Note-picking fixtures |
| `test_project/TestProject.xj` | `{"demo": false, "project": {"id": "e4f06956-...", "name": "Engine Test Project", "platformVersion": "1.9.6"}}` |
| `test_project/libraries/Test-Instruments/Test-Instruments.json` | `{"demo": false, "libraries": [ {...} ]}` |
| `test_project/libraries/Test-Instruments/{Test-Basslines,Test-Pads,Test-Perc-Loops}/` | One instrument JSON each (`instruments` + `instrumentAudios`; all `mode: Loop`, `volume: 1.0`, canonical config string) and WAVs named `<Instrument>-<audio name>-<tones>.wav` beside them |
| `test_project/libraries/Test-Programs/{Macro,Main}-Program-{Earth,Fire,Water,Wind}/` | Eight program JSONs: `programs`, `programMemes`, `programSequences`, `programSequenceBindings`, `programSequenceBindingMemes`, `programSequenceChords`. **No Beat or Detail programs; no voices, tracks, patterns, events or voicings anywhere** |
| `test_project/templates/Test-Template/Test-Template.json` | One template (`shipKey: "test"`) with the full canonical `config` string (taxonomy `COLOR`/`SEASON`) and two `Library` bindings |
| `test_project/build/testproject.json` | Workstation Build output: every entity in one file, `waveformKey` rewritten to `<audioId>.wav`; 10 WAVs beside it named by audio id |
| `test_project/.gitignore` | `!build` (keep), `render` (ignore) |

Observed drift: the template declares `COLOR` and `SEASON` but content memes are `EARTH/FIRE/WATER/WIND/HOT/COOL`, the same declaration-versus-usage drift `11-resolutions.md` Q4 found in the demos. `EngineTest.cpp:23` loads `_data/test_project/TestProject.xj` and asserts each scheduled audio exists at `getBuildPath() / waveformKey` (line 76).

---

## 13. Implications for V2 specs

**Loading and the pack format**

1. The engine reads **`.xj` + `build/**/*.json` only**, never `libraries/` or `templates/` (Engine.cpp 137-208). Any spec that says "the engine opens a V1 project" must specify who produces the single build payload: `xj-content` should synthesize the merged payload in memory and feed it through a new in-memory entry point rather than require a `build/` folder on disk.
2. `Engine` has **no in-memory loader**; `ContentEntityStore(std::string&)` exists and is tested. The C ABI `xj_engine_load_project` from JSON needs a new `Engine::loadProjectContent(json, audioRoot)` that skips the `.xj` suffix, existence and `build/` checks and sets `buildPathPrefix`.
3. Audio paths are resolved by the **host** as `buildPath / waveformKey`. For packs, `xj-content` must map `waveformKey` (original filename, or `<audioId>.<ext>` after Build) to the content-addressed `audio/<blake3>.<ext>`; the `contentHash` field is the right hook and the engine never needs to see the hash.
4. Build **drops audios whose files are missing** and **rewrites `waveformKey`**; a pack built from a Build output and one built from `libraries/` will have different `waveformKey` values for the same audio id. Lint must compare by audio `id`/`contentHash`, not by `waveformKey`.
5. Load errors in `build/` are printed to stdout and swallowed; the first failure aborts the rest of the crawl. The C ABI should return structured load errors; do not rely on exceptions crossing the boundary.

**JSON shape and additive fields**

6. Unknown entity fields and unknown config keys are ignored on read, confirming the "ignored by 1.9.x if present" claim. But both formatters (`TemplateConfig::toString`, Java `toString`) **drop unknown keys on write**, so additive config keys survive only if the V2 writer preserves unknown keys. Specify that `xj-content` round-trips config strings key-for-key.
7. `setIfNotNull(float&)` requires `is_number_float()`: an integer literal is silently treated as absent. New float fields (`pitchShiftRange`, `commitHorizonBeats`) and all existing ones must be written with a fractional part; a conformance test should feed `"tempo": 120` and assert the engine reads `0` so the rule is documented, then fix it in engine 2.0 by accepting `is_number()`.
8. `Instrument.volume` defaults to `0` in C++; a writer that omits it produces a silent instrument. Make `volume` required-or-defaulted-to-1.0 in the `xj-content` schema.
9. Required ids throw `std::invalid_argument("Missing required UUID: ...")`; missing enum keys throw nlohmann `out_of_range`; unknown enum **values** degrade silently (`Drum`, `Event`, `Draft`, `Main`, `Library`). Lint should treat unknown enum strings as errors because the engine will not.
10. Duplicate ids within one file collapse silently (set by id); across files last-loaded wins. Pack dependency resolution must define precedence explicitly and lint must reject duplicate ids within a pack.

**Defect to carry into the engine 2.0 spec**

11. `ProgramVoice` and `ProgramVoiceTrack` `from_json` read `"duration"`/`"tones"` into `name`/`order` (ProgramVoice.h:40-41, ProgramVoiceTrack.h:33-34) while the writer emits `"name"`/`"order"`. Voice and track names are empty for JSON-loaded content, which by inspection breaks track-to-audio event matching (`Craft.cpp:896,960`) and voice continuity (`Fabricator.cpp:222`). The engine test project has no Beat/Detail programs, so it is untested. Add a fixture with a Beat program and a round-trip assertion to the determinism/follow-mode test content.

**Config dialect**

12. The C++ `ConfigParser` is a **subset of HOCON**, typed by literal shape: `1` is int, `1.0` is float, and the typed getters throw on mismatch. `xj-content` should emit the canonical sorted `key = value` form the Java formatter uses and lint should reject anything the C++ parser cannot read (comments, substitutions, path keys, nested objects in objects, units). Do not describe the format as "HOCON" in the pack spec without this caveat.
13. Map keys typed as `Instrument::Type` silently collapse misspellings onto `Drum`. Lint must validate the ten type names in `choiceMuteProbability`, `dubMasterVolume`, `intensityLayers`, `intensityThreshold`.
14. Eleven `TemplateConfig` keys (`mixer*` x10, `dubMasterVolume`) are parsed but unused by the C++ engine. They are the natural handoff to `xj-dsp`; the V2 spec should say which side owns them rather than leave them dead.
15. `cutoffMinimumBars` (default 2) and `barBeats` (default 4) are the program-level knobs the override cut-point depends on (`work/CraftWork.cpp`); `commitHorizonBeats` is template-level and must be defined relative to them.

**Taxonomy**

16. Taxonomy sanitization strips everything but letters: digits, `!`, `$`, spaces vanish. Pack `[taxonomy]` categories may not contain numeric or grammar memes; `xj-base` conventions (`85BPM`, `3NOTE`, `!X`, `$X`) are enforced by `MemeStack`, not by the taxonomy. The compact syntax is `NAME[A,B,C]` with **`;`** between categories; `TemplateConfig` stores the structured list-of-objects form. A show's "union of pack categories" must be emitted in the structured form into the template config string.
17. Multi-template content: `WorkManager` seeds the `Taxonomy` control mode from a pointer-ordered `std::set<const Template*>` (`WorkManager.cpp:28`), i.e. arbitrary. Show manifests should produce exactly one template per station, or the engine should take the taxonomy from the started template (it already does for `Fabricator` via `chain->config`).

**Enums and ordering**

18. `Instrument::Type` is declared alphabetically in C++ and in a different order in Java; never persist or sort by ordinal. The ten strings are the contract: `Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition`; modes `Chord, Event, Loop`; program types `Macro, Main, Beat, Detail`; states `Draft, Published`; binding types `Library, Program, Instrument`.

**Lifecycle fields**

19. `isDeleted` and `updatedAt` are ignored by the engine; `forTemplate` includes only `Published` programs/instruments from bound libraries but includes **directly bound** entities regardless of state. Pack lint should fail on `isDeleted: true` or `Draft` entities reachable from a shipped template, since the engine will happily play the former.
20. `Template.shipKey` names the chain; `getTemplateByIdentifier` resolves name → shipKey → id. Show manifests referring to `deep-house-kit/Main` should resolve to a template id before reaching the engine to avoid name collisions across packs.
