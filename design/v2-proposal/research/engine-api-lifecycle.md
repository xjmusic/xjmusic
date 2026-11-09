# Engine public API and work cycle: grounded map (engine 1.9.6)

Source of truth: `/Users/nck/xj/xjmusic/engine/` at commit `a94000b9a` (engine 1.9.6). Every line number below comes from `grep -n` / `cat -n` on that tree. Where a statement is inferred rather than read, it says so. Items marked **BUG** are behaviors that contradict the header comments or the Java original (`workstation/src/main/java/io/xj/engine/work/CraftWorkImpl.java`); they were found by reading and have not been confirmed by running a test.

## 0. Files in scope

| Path | Lines | Role |
|---|---|---|
| `engine/include/xjmusic/Engine.h` / `engine/src/Engine.cpp` | 141 / 208 | Public facade, project loading |
| `engine/include/xjmusic/work/WorkManager.h` / `engine/src/work/WorkManager.cpp` | 151 / 205 | Owns CraftWork + DubWork, WorkState, event diff |
| `engine/include/xjmusic/work/WorkSettings.h` / `engine/src/work/WorkSettings.cpp` | 26 / 11 | Timing settings |
| `engine/include/xjmusic/work/WorkState.h` | 19 | State enum |
| `engine/include/xjmusic/work/CraftWork.h` / `engine/src/work/CraftWork.cpp` | 308 / 473 | Craft cycle, rewrite, cutoff, cleanup |
| `engine/include/xjmusic/work/DubWork.h` / `engine/src/work/DubWork.cpp` | 124 / 190 | Dub cycle: crafted picks to `ActiveAudio` set |
| `engine/include/xjmusic/audio/ActiveAudio.h` / `engine/src/audio/ActiveAudio.cpp` | 112 / 78 | Scheduled-audio record |
| `engine/include/xjmusic/audio/AudioScheduleEvent.h` / `.cpp` | 66 / 34 | Create/Update/Delete envelope |
| `engine/include/xjmusic/audio/AudioMathUtils.h` / `.cpp` | 49 / 11 | Intensity to amplitude |
| `engine/example/{main,XJPlayer,EngineUiBase,EngineScheduledAudio}.cpp` | | Reference host (SDL2 + FTXUI) |
| `engine/test/EngineTest.cpp` (386 lines), `engine/test/work/WorkManagerTest.cpp` (88) | | The only tests of the work layer |

There is no MIDI, OSC, network, thread, mutex or atomic anywhere under `engine/src` or `engine/include` (`grep -rn "mutex\|std::thread\|atomic\|lock_guard\|condition_variable"` returns nothing). `std::thread` appears only in the example host (`example/XJPlayer.cpp:60`).

## 1. `XJ::Engine` public surface

Members (Engine.h:13-18): `WorkSettings settings; unique_ptr<SegmentEntityStore> store; unique_ptr<ContentEntityStore> projectContent, templateContent; unique_ptr<WorkManager> work; std::filesystem::path buildPathPrefix;`

| Method (Engine.h line) | Signature | What it does (Engine.cpp lines) |
|---|---|---|
| ctor (30-36) | `Engine(const std::optional<std::string>& pathToProjectFile, std::optional<Fabricator::ControlMode> controlMode, std::optional<int> craftAheadSeconds, std::optional<int> dubAheadSeconds, std::optional<int> deadlineSeconds, std::optional<int> persistenceWindowSeconds)` | 10-35: fills `settings` (seconds times `ValueUtils::MICROS_PER_SECOND`), creates the three stores, loads the project if a path is given, copies `projectContent` into `templateContent`. Throws `std::invalid_argument` (path not `.xj`, line 144; path missing, 150) and `std::runtime_error` (no project entity, 159; no `build/` folder, 172). |
| `start` (46) | `void start(const std::optional<std::string>& templateIdentifier)` | 37-54: constructs a **new** `WorkManager(store, projectContent, settings)`; resolves the template by name, then shipKey, then id (`ContentEntityStore::getTemplateByIdentifier`, ContentEntityStore.cpp:784-792), else the first template; rebuilds `templateContent` with `forTemplate` (ContentEntityStore.cpp:811-870: the template, its bindings, bound libraries' Published programs and instruments, direct program/instrument bindings); calls `work->start()`. **The header comment says "1. Clear the segment entity store"; the code never clears `store`.** A second `start()` on the same Engine continues from whatever segments are already in the store. |
| `finish` (51) | `void finish(bool cancelled) const` | 56-61: `work->finish(cancelled)` = `updateState(cancelled ? Cancelled : Done)` (WorkManager.cpp:69-72). Does not call `CraftWork::finish` or `DubWork::finish`. |
| `RunCycle` (58) | `[[nodiscard]] std::vector<AudioScheduleEvent> RunCycle(unsigned long long atChainMicros) const` | 63-68: `{}` before `start()`, else `work->runCycle(atChainMicros)`. Section 4. |
| `getSegmentStore` (63) | `SegmentEntityStore*` | Raw pointer to the live store (the UI reads it directly). |
| `getProjectContent` (70) | `ContentEntityStore*` | Everything loaded from `build/**.json`. **This is what craft uses** (Engine.cpp:39). |
| `getTemplateContent` (76) | `ContentEntityStore*` | Filtered copy for display; craft never reads it. |
| `getWorkState` (81) | `WorkState` | `Standby` before start, else `work->getState()`. |
| `getMemeTaxonomy` (86) | `std::optional<MemeTaxonomy>` | From `templateContent` (its first template). |
| `getAllMacroPrograms` (91) | `std::vector<const Program*>` | WorkManager.cpp:121-128. **BUG:** the `std::sort` comparator is `return a->name.compare(b->name);`, an `int` converted to `bool`, true for any two different names; not a strict weak ordering, so the documented "alphabetical order" is not delivered and the sort is technically UB. |
| `doOverrideMacro` (99) | `void doOverrideMacro(const Program*) const` | Forwarded to CraftWork (section 6). |
| `doOverrideMemes` (107) | `void doOverrideMemes(const std::set<std::string>&) const` | Forwarded to CraftWork. |
| `setIntensityOverride` (114) | `void setIntensityOverride(std::optional<float>) const` | Forwarded to DubWork. |
| `getBuildPath` (119) | `std::filesystem::path` | `<folder of the .xj>/build/` (Engine.cpp:167). Audio file for a pick is `getBuildPath() / audio->waveformKey` (EngineTest.cpp:76). |
| `getSettings` (125) | `WorkSettings` (by value) | Resolved settings. No setter exists anywhere. |
| dtor (130) | `~Engine()` | default |

Present on `WorkManager` but not exposed on `Engine`: `getAndResetDidOverride()` (WorkManager.h:106, the only acknowledgement that a rewrite happened), `getEntityStore()`, `getSourceMaterial()`.

**Loading** (Engine.cpp:137-208): the `.xj` file is parsed only to check it contains a project and to print its name (154-161); its entities are **not** put into `projectContent`. What the engine crafts from is every `.json` found by `recursive_directory_iterator(build/)` (177-195). Filesystem errors during that crawl are caught and printed, not thrown (197-204). `ContentEntityStore(std::string&)` (ContentEntityStore.cpp:1014-1016) parses in-memory JSON and is the natural seam for a "load from memory" ABI entry point. `EngineTest::CorrectlyDubsAudio` (EngineTest.cpp:87-178) builds all content programmatically through `getProjectContent()->put(...)` with `pathToProjectFile = nullopt`.

## 2. Settings: the four timing values

`WorkSettings` (WorkSettings.h:13-22):

| Field | Type | Default | Set from | Read at |
|---|---|---|---|---|
| `controlMode` | `Fabricator::ControlMode` | `Auto` | Engine.cpp:18 | WorkManager.cpp:37, once, in `start()` |
| `craftAheadMicros` | `int` | `20 * MICROS_PER_SECOND` = 20,000,000 | Engine.cpp:19-20 | CraftWork ctor (WorkManager.cpp:13, CraftWork.cpp:26); `doFabrication` CraftWork.cpp:221 |
| `dubAheadMicros` | `int` | 10,000,000 | Engine.cpp:21-22 | DubWork ctor (WorkManager.cpp:17, DubWork.cpp:15, stored as `unsigned long`); `computeActiveAudios` DubWork.cpp:98 and 152 |
| `deadlineMicros` | `int` | 1,000,000 | Engine.cpp:23-24 | CraftWork.cpp:27; its **only** use is the rewrite target, `doFabrication` CraftWork.cpp:216 |
| `persistenceWindowSeconds` | `long` | 3600 | Engine.cpp:25-26 (no multiplication) | WorkManager.cpp:12 passes it into `CraftWork(..., persistenceWindowSeconds, ...)`; CraftWork.cpp:28 stores it as `persistenceWindowMicros`; read in `doSegmentCleanup` CraftWork.cpp:398 |

Facts a spec must carry:

- The brief calls it `persistenceWindowMicros`; the public setting is `persistenceWindowSeconds`. **BUG (units):** `CraftWork.h:45` declares the constructor parameter `int persistenceWindowSeconds`; `CraftWork.cpp:20` names the same parameter `persistenceWindowMicros` and stores it unconverted (`this->persistenceWindowMicros = persistenceWindowMicros;`, line 28). The Java original converts: `persistenceWindowMicros = persistenceWindowSeconds * MICROS_PER_SECOND` (CraftWorkImpl.java:101). The effective default window is therefore **3,600 microseconds**, not one hour. Section 9 gives the consequences.
- `int` micros cap craft/dub/deadline at 2,147 s. The API takes whole seconds (`std::optional<int>`), so sub-second horizons (the proposal's "commit horizon of one beat") are unrepresentable today.
- `WorkSettings::toString()` (WorkSettings.cpp:5-11) is the only formatter; it prints whole seconds.
- Settings are copied into `WorkManager::config` at `start()`. There is no runtime setter; changing horizons means constructing a new `Engine`.
- `ValueUtils` constants (ValueUtils.cpp:20-31): `MICROS_PER_SECOND` (long) = 1,000,000; `MICROS_PER_SECOND_FLOAT`; `MICROS_PER_MINUTE` (long) = 60,000,000; `MICROS_PER_MINUTE_FLOAT`.

## 3. Lifecycle and `WorkState`

`enum WorkState { Standby, Active, Done, Cancelled, Failed }` (WorkState.h:10-16; unscoped, values 0-4). `WorkManager::toString(WorkState)` (WorkManager.cpp:186-200) exists but is private.

Transitions actually present in code:

- `Standby` to `Active`: `WorkManager::start()` calls `updateState(Active)` (WorkManager.cpp:64) **before** `craftWork.start()` (65). `CraftWork::start()` throws `std::runtime_error("Cannot initialize CraftWork without Templates")` (CraftWork.cpp:35-38) when the content has no template; that exception escapes `Engine::start` with the state already `Active`.
- `Active` to `Done` / `Cancelled`: `finish(cancelled)` (WorkManager.cpp:71).
- `Active` to `Failed`: only `WorkManager::didFailWhile` (180-184), reached only if `craftWork.runCycle` or `dubWork.runCycle` **throws**. Both catch `std::exception` internally (CraftWork.cpp:168-176; DubWork.cpp:54-62 and 178-182). **`Failed` is unreachable for any `std::exception`.** The real failure sequence: `CraftWork::didFailWhile` sets `running = false` (CraftWork.cpp:431-436); on the next cycle `DubWork::runCycle` sees `craftWork->isFinished()` and finishes itself (DubWork.cpp:46-51); `WorkManager::state` stays `Active`; `RunCycle` emits a `Delete` for every active audio and then returns empty vectors forever. A host cannot tell "failed" from "healthy but silent" through `getWorkState()`.

`CraftWork::start()` (CraftWork.cpp:32-42) builds the `Chain` from `*content->getTemplates().begin()`. `getTemplates()` returns `std::set<const Template*>` (ContentEntityStore.cpp:91-101), ordered by pointer value. In a multi-template project the chain's `TemplateConfig` (and `WorkManager::memeTaxonomy`, WorkManager.cpp:28) therefore come from an arbitrary template, not the one named in `Engine::start`. The `Fabricator` reads the bindings of **all** templates in `projectContent` (Fabricator.cpp:37-38) and its meme taxonomy from `chain->config` (Fabricator.cpp:1058-1061).

`DubWork::start()` (DubWork.cpp:18-22) snapshots `templateConfig = craftWork->getTemplateConfig()` (= `store->readChain()->config`, CraftWork.cpp:52-55) once. Intensity layers and thresholds used by dub are frozen at start.

After `finish()`, `runCraftCycle` and `runDubCycle` return early (`state != Active`, WorkManager.cpp:151 and 165), so the **next** `RunCycle` returns a `Delete` for each entry of `activeAudioMap` (96-104). A host that stops calling `RunCycle` after `finish()` never receives those deletes.

## 4. `RunCycle` contract

`WorkManager::runCycle(atChainMicros)` (WorkManager.cpp:74-107) is synchronous and does, in order:

1. `runCraftCycle(at)` → `craftWork.runCycle(at)` → `doFabrication(at)` then `doSegmentCleanup(at)` (CraftWork.cpp:163-177). At most one segment is crafted per call.
2. `runDubCycle(at)` → `dubWork.runCycle(at)` → `std::set<ActiveAudio>` (section 10). Returns `{}` when the state is not `Active`, when craft is not ready, when craft is finished, or on exception.
3. Diff against `activeAudioMap` (`std::map<std::string, ActiveAudio>`, keyed by pick id):
   - id absent: `Create`, insert (lines 90-92).
   - id present and `operator!=` (compares id, start, stop, fromVolume, toVolume; ActiveAudio.h:89-96): `Update` (85-88). **BUG:** the intended replacement is `activeAudioMap.emplace(id, audio)` (line 86), and `std::map::emplace` does not overwrite an existing key. The stored record is never refreshed, so an `Update` is re-emitted every cycle for as long as the live record differs from the **first-seen** record, and the eventual `Delete` carries the first-seen start/stop/volumes.
   - id in the map but absent from this cycle's set: `Delete` with the stored (stale) record, then erase (97-104).
4. Returns the vector: creates and updates in `std::set<ActiveAudio>` iteration order (ordered by pick-id string, ActiveAudio.h:67-70), then deletes in map order. The vector is **not** sorted by start time, although `AudioScheduleEvent::operator<` (AudioScheduleEvent.h:45-47) orders by `getStartAtChainMicros()`.

Window semantics (DubWork.cpp:152): an audio is in the set iff `startAtChainMicros <= at + dubAheadMicros && stopAtChainMicros >= at`. `Create` therefore arrives up to `dubAheadMicros` before the start; `Delete` arrives on the first cycle after `stop < at` (natural end), **or** when the pick vanished (rewrite, cutoff, cleanup), **or** when dub returned an empty set (not ready, finished, failed). A `Delete` does not say which.

Cycle rate: Engine.h:55 says "1-3 times per second". The example host uses `CYCLE_MILLISECONDS = 382` (EngineUiBase.h:23); the tests step 1,000,000 µs per call (EngineTest.cpp:28, WorkManagerTest.cpp:25); the 11-resolutions harness stepped 100 ms. Nothing in the engine depends on the rate except coverage: if `at` advances by more than `dubAheadMicros` between calls, picks are skipped silently.

Clock types: `atChainMicros` is `unsigned long long` at the API, but becomes `long` inside (`DubWork::getSegmentAtChainMicros(long)`, DubWork.h:80; `Segment::beginAtChainMicros` and `durationMicros` are `long`, Segment.h:37-38; `SegmentChoiceArrangementPick::startAtSegmentMicros/lengthMicros` are `long`, SegmentChoiceArrangementPick.h:22-23; `ChainUtils::computeFabricatedToChainMicros` returns `long`). On MSVC `long` is 32-bit: chain time wraps at 2,147.48 s (35.8 min) in Windows builds. The example host's own clock is `Uint32 AtChainMicros` (EngineUiBase.h:56) and wraps at 71.6 min on every platform.

## 5. `ActiveAudio`, `AudioScheduleEvent`, `AudioMathUtils`

`ActiveAudio` (ActiveAudio.h:13-108), fields in declaration order:

| Field | Type | Source (ActiveAudio.cpp:7-26) |
|---|---|---|
| `id` (private) | `UUID` = `std::string` (EntityUtils.h:28) | `pick->id` |
| `pick` (protected) | `const SegmentChoiceArrangementPick*` | raw pointer into `SegmentEntityStore::segmentChoiceArrangementPicks` |
| `instrument` | `const Instrument*` | into `ContentEntityStore` |
| `audio` | `const InstrumentAudio*` | into `ContentEntityStore` |
| `startAtChainMicros` | `unsigned long long` | section 10 |
| `stopAtChainMicros` | `unsigned long long` | section 10 |
| `fromVolume` | `float` | `fromIntensity * pick->amplitude * instrument->volume * audio->volume` (line 24) |
| `toVolume` | `float` | same with `toIntensity` (line 25) |

Accessors: `getFromVolume, getToVolume, getId, getPick, getInstrument, getStartAtChainMicros, getStopAtChainMicros, getAudio`; `getReleaseMillis()` = `instrument->config.releaseMillis` (ActiveAudio.cpp:58-61; default `5`, InstrumentConfig.cpp DEFAULT); `getAmplitude(float ap)` = linear `from + (to - from) * ap` (63-68). `<` and `>` order by id; `==` compares id, start, stop, from, to and ignores the pointers. The `pick` pointer inside a `Delete` event may already dangle: the pick was erased from the store (`SegmentEntityStore::deleteSegment`, SegmentEntityStore.cpp:310-320, or `deleteSegmentChoiceArrangementPick`) before the diff ran. `instrument` and `audio` stay valid for the Engine's life because content is never deleted after load.

`AudioScheduleEvent` (AudioScheduleEvent.h:10-62): `enum class EType { Create, Update, Delete }` (0, 1, 2); public `EType type; ActiveAudio schedule;`; static `toString` and `parseType` (unknown strings parse to `Update`, AudioScheduleEvent.cpp:27-32).

`AudioMathUtils::computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, fadeOutLowerIntensities, intensity)` (AudioMathUtils.cpp:5-12): returns `1.0` when `intensityLayers == 1`; otherwise `min(1, 2 * max(0, 1 - (audio->intensity - intensity) / threshold))`. The symmetric `abs` form exists only for `fadeOutLowerIntensities == true`, and DubWork always passes `false` (DubWork.cpp:166 and 171). Template defaults (TemplateConfig.cpp DEFAULT): `intensityLayers` Bass 1, Drum 1, Stab/Sticky/Stripe 2, Background/Hook/Pad/Percussion/Transition 3; `intensityThreshold` 0.5 for every type. Bass and Drum therefore ignore intensity entirely by default.

Enum values an ABI would freeze: `Instrument::Type { Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition }` (0-9) and `Instrument::Mode { Chord, Event, Loop }` (0-2) (Instrument.h:16-32); `Segment::Type { Pending, Initial, Continue, NextMain, NextMacro }` and `Segment::State { Planned, Crafting, Crafted, Failed }` (Segment.h:16-29); `SegmentMessage::Type { Debug, Info, Warning, Error }` (SegmentMessage.h:17-22).

## 6. Control modes and the three live inputs

`enum class Fabricator::ControlMode { Auto, Macro, Taxonomy }` (Fabricator.h:70-74); `parseControlMode` (Fabricator.cpp:1340-1347) accepts "Taxonomy" and "Macro", anything else is `Auto`. The mode is consulted exactly once, in `WorkManager::start()` (WorkManager.cpp:37-57):

- `Macro`: `doOverrideMacro(*getAllMacroPrograms().begin())`, i.e. whichever program the broken sort puts first.
- `Taxonomy`: for each taxonomy category take `*category.getMemes().begin()`, then `doOverrideMemes(memes)`.
- `Auto`: nothing. After `start()` the mode has no further effect; all three inputs work in every mode.

`doOverrideMacro(program)` (CraftWork.cpp:184-189): sets `nextCycleOverrideMacroProgram = program` and calls `doNextCycleRewriteUnlessInitialSegment()` (206-210), which sets `nextCycleRewrite = true` iff `store->getSegmentCount() > 0`. Consumed by `MacroMainCraft::chooseMacroProgram` (MacroMainCraft.cpp:274-276: returns the override unconditionally, skipping the marble bag) and `computeMacroSequenceBindingOffset` (195-198: for Initial/NextMacro segments start at `getSecondMacroSequenceBindingOffset(override)`, Fabricator.h:809, instead of offset 0).

`doOverrideMemes(memes)` (CraftWork.cpp:191-197): sets `nextCycleOverrideMemes = memes`, same rewrite trigger. Consumed by `MacroMainCraft::doWork` (16-40): each meme is written as a forced `SegmentMeme` (`fabricator->put(segmentMeme, true)`), then **main is chosen before macro** (34-35) and `chooseMacroProgram` builds its isometry from the override memes (298-300). It also changes the retrospective: `doFabricationWork` constructs `SegmentRetrospective(store, overrideMemes.empty() ? segment->id : 0)` (CraftWork.cpp:378), and id 0 yields an empty retrospective (SegmentRetrospective.cpp:20-25; commit e9ebef6fa).

Both overrides are **sticky and independent**. Neither is ever cleared: there is no reset method on `Engine`, `WorkManager` or `CraftWork` (the comment "force until reset", Engine.h:102, has no counterpart), they are passed into every later `doFabricationDefault` (CraftWork.cpp:221), and setting one does not clear the other. After a macro override every later segment's `chooseMacroProgram` returns that program; after a meme override every later segment carries those memes. The only way back to `Auto` behavior is a new `Engine`.

`setIntensityOverride(optional<float>)` (DubWork.cpp:90-93) stores `intensityOverride`. Each dub cycle (DubWork.cpp:106-113): `nextIntensity = override.has_value() ? override : segments.at(0)->intensity`. It affects only `fromVolume`/`toVolume` of emitted `ActiveAudio` through `computeIntensityAmplitude`. It never reaches craft: craft reads `Segment::intensity`, which `MacroMainCraft` computes from the macro and main sequences and the delta (MacroMainCraft.cpp:86 and 160-192; auto-crescendo between 0.2 and 0.8 by default). `nullopt` returns to segment intensity. It takes effect on the next `RunCycle` for every active audio, as `Update` events, with no rewrite.

`isReady()` is `!nextCycleRewrite` (CraftWork.cpp:179-182); `DubWork::runCycle` returns `{}` while not ready (DubWork.cpp:40-44). Because craft runs before dub inside one `runCycle`, this gate is never observed in single-threaded use; it only matters if an override arrives from another thread between the two phases (section 11), in which case every active audio is deleted for one cycle and re-created on the next.

## 7. The craft cycle

`doFabrication(at)` (CraftWork.cpp:212-223): if `nextCycleRewrite`, run `doFabricationRewrite(at + deadlineMicros, ...)` and clear the flag; else `doFabricationDefault(at + craftAheadMicros, ...)`.

`doFabricationDefault(to, ...)` (225-264): `fabricatedTo = ChainUtils::computeFabricatedToChainMicros(readAllSegments())`, the end of the **last Crafted** segment (ChainUtils.cpp:37-45). If `fabricatedTo > to` return (lines 234-238). Otherwise build the next `Segment`: `buildSegmentInitial()` (403-413: id 0, begin 0, delta 0, type Pending, state Planned) or `buildSegmentFollowing(last)` (415-429: id + 1, begin = last.begin + last.duration, delta carried, throws `std::runtime_error` if the last segment has no duration), put it, and call `doFabricationWork`.

`doFabricationWork(segment, overrideType, overrideMacro, overrideMemes)` (371-394): build the retrospective; `Fabricator(content, store, &retro, segment->id, overrideType)`; Planned to Crafting via `updateSegmentState` (438-450; throws `std::runtime_error` on a wrong from-state); `MacroMainCraft(&fab, overrideMacro, overrideMemes).doWork(); BeatCraft; DetailCraft; TransitionCraft; BackgroundCraft`; Crafting to Crafted. The store enforces transitions (SegmentEntityStore.cpp:323-352): Crafted may only go to Crafted or Crafting. Nothing in `src/` ever assigns `Segment::State::Failed`.

Segment type comes from `Fabricator::computeType()` (Fabricator.cpp:1158-1176): Initial if first; Continue if the previous main choice has one more binding offset and `mainProgramLengthMaxDelta` (default 280) exceeds the previous delta; NextMain if the previous macro choice has two more offsets; else NextMacro. An `overrideSegmentType` given to the constructor pre-empts this (Fabricator.cpp:49-52).

`MacroMainCraft::doWork` writes the foundation (MacroMainCraft.cpp:71-89): `type`, `tempo = mainProgram->tempo`, `key`, `total = mainSequence->total`, `durationMicros = getSegmentMicrosAtPosition(tempo, total)` (`static_cast<long>(double(MICROS_PER_MINUTE) / tempo * position)`, Fabricator.cpp:752-755 and 1036-1040, with `microsPerBeat` cached per Fabricator on first call), `delta`, `intensity`, plus the `SegmentChord`, `SegmentChordVoicing` and `SegmentMeme` rows (46-69).

## 8. The rewrite path

`doFabricationRewrite(toChainMicros, overrideMacro, overrideMemes)` (CraftWork.cpp:266-330), called with `toChainMicros = at + deadlineMicros` (line 216):

| Lines | Step |
|---|---|
| 274-280 | `lastSegment = getSegmentAtChainMicros(to)`: the **Crafted** segment spanning `at + deadline` (`SegmentUtils::isSpanning`: `begin + duration > t && begin <= t`, SegmentUtils.cpp:73-79). If none, log "Will not delete any segments ..." and return. The caller still clears the flag, so the override is applied only by the normal path from then on. |
| 283-297 | `currentMainProgram = getMainProgram(lastSegment)` (none: return). `ProgramConfig(program->config)`; defaults `barBeats = 4`, `cutoffMinimumBars = 2` (ProgramConfig.cpp:12-14). |
| 298 | `subBeats = float(barBeats * cutoffMinimumBars)` (default 8). |
| 299 | `dubbedToSegmentMicros = to - lastSegment->beginAtChainMicros` (unsigned). |
| 300 | `microsPerBeat = long(MICROS_PER_MINUTE_FLOAT / mainProgram->tempo)`. |
| 301 | `dubbedToSegmentBeats = dubbedToSegmentMicros / microsPerBeat`: **integer division** (unsigned long long by long). |
| 302 | `cutoffAfterBeats = subBeats * ceil(float(dubbedToSegmentBeats) / subBeats)`. |
| 303-306 | If `cutoffAfterBeats < lastSegment->total`, `doCutoffLastSegment(lastSegment, cutoffAfterBeats)`; otherwise the current segment plays to its natural end and the rewrite begins at the following segment. |
| 319 | `store->deleteSegmentsAfter(lastSegment->id)` (ids greater than id, SegmentEntityStore.cpp:398-408; cascades to every child map). |
| 320-322 | `buildSegmentFollowing(lastSegment)`, `type = NextMacro`, `doFabricationWork(..., Segment::Type::NextMacro, overrideMacro, overrideMemes)`. The forced type bypasses `computeType`, so even a meme-only override produces a NextMacro segment. |
| 323 | `didOverride = true`. |

Worked example with defaults: 120 bpm (`microsPerBeat = 500,000`), `subBeats = 8`, a 32-beat segment beginning at 100.0 s, `at = 110.3 s`, deadline 1 s so `to = 111.3 s`. `dubbedToSegmentMicros = 11,300,000`, divided by 500,000 gives 22 (22.6 floored); `ceil(22 / 8) = 3`; cutoff at **24 beats = 12.0 s** into the segment, i.e. chain time 112.0 s, which is 0.7 s after the deadline point and 1.7 s after `at`. The NextMacro segment begins at 112.0 s. Because of the floor, when the floored beat count is already a multiple of `subBeats` the cutoff lands **before** `to` by up to one beat (`dubbedTo = 24.9` beats floors to 24, cutoff at 24 beats, 0.9 beat early). At tempos of 60 bpm or slower the cutoff can land at or before `at` itself. The Java original passed the mixer's actual `dubbedToChainMicros` into this function (CraftWorkImpl.java:338-340 and 406); the C++ port substitutes `at + deadlineMicros`. **Today's de-facto commit horizon is `deadlineMicros` (1 s), not `dubAheadMicros` (10 s)**: everything the host was already told about in the 1-to-10-second window is revoked through `Delete`/`Update` events in the same `RunCycle` that performed the rewrite.

`doCutoffLastSegment(segment, cutoffAfterBeats)` (332-369): `durationMicros = long(cutoffAfterBeats * MICROS_PER_MINUTE_FLOAT / segment->tempo)` (float arithmetic, a third beats-to-micros formula alongside line 300 and Fabricator.cpp:754; rounding can disagree by a microsecond); `segment.total = int(cutoffAfterBeats)`; `segment.durationMicros`; `store->updateSegment` (Crafted to Crafted is allowed, 339-342). Then for each pick of the segment (343-362): `startAtSegmentMicros >= durationMicros` deletes the pick (347-348); else if `0 < lengthMicros && start + length > durationMicros` the pick is shortened and `store->put(updatePick)` is called (349-354). **BUG:** the generic child-entity `put` is `STORE[segmentId].emplace(id, entity)` (SegmentEntityStore.cpp:16), which does not overwrite, so the shortened pick is never stored. Picks straddling the cutoff keep their full length and sound across the new boundary. Picks with `lengthMicros == 0` (one-shots) are never trimmed by design (line 349). Per-pick failures are logged and swallowed (356-361); any other failure is rethrown as `FabricationException` (364-368) and ends craft via `didFailWhile`.

## 9. Persistence window and segment cleanup

`doSegmentCleanup(shippedTo)` (CraftWork.cpp:396-401): `segment = getSegmentAtChainMicros(shippedTo - persistenceWindowMicros)`; if found, `store->deleteSegmentsBefore(segment->id)` (ids less than id, SegmentEntityStore.cpp:385-395). `getSegmentAtChainMicros` requires state Crafted (78-87). Runs every cycle after `doFabrication`.

With the units bug (section 2) the window is 3,600 µs: the segment containing `at - 3.6 ms` survives and every earlier segment is erased with all its choices, picks, chords, voicings, memes, messages and metas. Consequences (by reading; `EngineTest` loops end inside their segments, so no test exercises a tail crossing a boundary):

1. Any pick of the previous segment whose audio is still sounding disappears from the dub set on the first cycle at least 3.6 ms after the boundary, producing a `Delete`. A host that honors `Delete` literally cuts tails at every segment boundary.
2. `getSegmentsIfReady` (CraftWork.cpp:57-76) deliberately prepends the previous segment and appends the next. With cleanup this aggressive the previous one is almost never present, so `segments.at(0)` in `DubWork::computeActiveAudios` (DubWork.cpp:112) is the current segment. **If the window is corrected to 3600 s, `segments.at(0)` becomes the previous segment** and dub intensity lags one segment behind. The two must be fixed together.
3. `SegmentRetrospective` (reads `readAllSegments`, SegmentRetrospective.cpp:43) and anything else that looks back sees only what survived; one hour was the Java intent (CraftWorkImpl.java:71, 101, 523-526).
4. While `at < 3600` µs the unsigned subtraction wraps, no segment matches, nothing is deleted.

## 10. The dub cycle in detail

`DubWork::computeActiveAudios(at)` (DubWork.cpp:96-183): `segments = getSegmentsIfReady(at, at + dubAhead)` (previous, spanning, next; Crafted only). If empty, return `{}`, which the WorkManager diff turns into a `Delete` for everything previously active. Otherwise, for each segment, each `SegmentChoice` with `!mute` (muting is decided at craft from `TemplateConfig::choiceMuteProbability`, all 0.0 by default; `Craft.cpp:449,494`, `BeatCraft.cpp:58`, `DetailCraft.cpp:99`, `TransitionCraft.cpp:58`, `BackgroundCraft.cpp:29`), each arrangement, each pick:

- skip picks whose `audio->waveformKey` is empty (125-128);
- `transientMicros = audio->transientSeconds > 0 ? seconds * 1e6 : 0` (130-132);
- `startAtChainMicros = segment->beginAtChainMicros + pick->startAtSegmentMicros - transientMicros` (137-143), unsigned, so it wraps if the transient exceeds the chain position (a transient-bearing pick at chain time 0 is never scheduled);
- `stopAtChainMicros = lengthMicros > 0 ? start + transient + length : start + audio->lengthSeconds * 1e6` (144-151): for finite picks the transient cancels and stop = segment.begin + pick.start + length; for one-shots the whole file plays from `start`;
- window test (152), then `ActiveAudio(pick, instrument, audio, start, stop, amp(prevIntensity or nextIntensity), amp(nextIntensity))` (155-171) using `templateConfig.getIntensityLayers / getIntensityThreshold(instrument->type)`;
- after the loop `prevIntensity = nextIntensity` (174).

So `fromVolume` is the amplitude at the previous cycle's intensity and `toVolume` at this cycle's; with steady intensity they are equal and no volume `Update` is produced. The host is expected to glide from `from` to `to` over the cycle (that is what `getAmplitude(ap)` is for). Volumes include `pick->amplitude`, `instrument->volume` and `audio->volume` but **not** `TemplateConfig::dubMasterVolume`, which nothing in the C++ engine reads.

## 11. Threading

Nothing is thread-safe. `WorkManager`, `CraftWork`, `DubWork` and both stores are plain objects mutated by `RunCycle`, `doOverride*`, `setIntensityOverride`, `start` and `finish`, with no synchronization. Process-global mutable state shared by **every** `Engine` in a process: `StickyBun::rd/gen` (StickyBun.h:22-23; StickyBun.cpp:14-15, 24), `ValueUtils::rd/gen` (ValueUtils.h:23-24; ValueUtils.cpp:33-34, 85, 92, 166) and `EntityUtils::UNIQUE_ID_COUNTER` (`static unsigned long long` at namespace scope in a header, EntityUtils.h:29; incremented non-atomically in `computeUniqueId`, EntityUtils.cpp:10-14). Per-instance generators: `MarbleBag` (MarbleBag.h:33-34; MarbleBag.cpp:167-168, 176-177), `Craft` (Craft.h:417-418), and a local one in `Fabricator::chooseVoicing` (Fabricator.cpp:874-875). The reference host already violates the single-thread assumption: `RunCycle` runs on a detached `std::thread` (XJPlayer.cpp:60-92) while `engageMemeOverride` calls `engine->doOverrideMemes` from the FTXUI thread (EngineUiBase.cpp:597) and the UI thread reads `engine->getSegmentStore()` (EngineUiBase.cpp:160, 449, 475, 611, 674) while craft mutates it. The comments "the dub cycle depends on the craft cycle operating on a separate thread" (CraftWork.h:20-21, DubWork.h:17-18) describe the Java design, not this code.

## 12. Errors and diagnostics

Two exception types, both deriving `std::exception`: `FabricationException` ("just a warning, fabrication continues", FabricationException.h:12-15; 41 throw sites in `src/`) and `FabricationFatalException` ("the chain must be restarted", FabricationFatalException.h:12-17; thrown at Fabricator.cpp:34, 762, 771 and SegmentRetrospective.cpp:31, 40). Despite the names the work layer treats them identically: any `std::exception` escaping a craft stage reaches the catch in `doFabricationDefault` or `doFabricationRewrite` and ends in `CraftWork::didFailWhile` (`running = false`, CraftWork.cpp:260-263, 325-329, 431-436). There is no retry and no per-segment isolation. All logging is `std::cout` / `std::cerr` (commit 2edad3c49 removed spdlog); there is no log callback. `SegmentMessage`s written through `Fabricator::addMessage / addErrorMessage / addWarningMessage / addInfoMessage` (Fabricator.h:82-103) land in the store and are readable only via `getSegmentStore()->readAllSegmentMessages(segmentId)`; `Engine` exposes nothing for them. `Engine` itself throws only from the constructor (section 1) and, indirectly, from `start()` when no template exists.

## 13. How a host is expected to play audio

The reference host documents the protocol but **does not play audio**. `XJPlayer::RunEngineCycle` (XJPlayer.cpp:97-118) maps `Create` to constructing an `EngineScheduledAudio` (which `SDL_LoadWAV`s `getBuildPath() + waveformKey`, EngineScheduledAudio.cpp:24-28, throwing `std::runtime_error` if the file is missing), `Update` to copying the seven fields (37-46), `Delete` to erasing the map entry. `EngineScheduledAudio::Play()` (31-35, `SDL_QueueAudio`) is defined but never called (a grep of `example/` finds only the definition; commit 8486c7180 is titled "Engine incremental audio schedule & example app without playback"). Proposal chapter 02 says the example "plays it at the scheduled time with the scheduled volume"; at 1.9.6 that is not true.

What the contract implies a real host must do: keep a map by pick id; on `Create`, load the file and schedule the **whole file** to start at `startAtChainMicros` on the host's chain clock (the engine has already subtracted the transient so the file's first sample starts early and the onset lands on the beat); stop at `stopAtChainMicros`, fading over `getReleaseMillis()`; glide gain from `fromVolume` to `toVolume` across the cycle; on `Update`, apply the new stop and volumes (the id is the pick, so file and onset never change); on `Delete`, stop, possibly immediately, because the pick may have been revoked by a rewrite. `EngineTest::CorrectlyDubsAudio` (EngineTest.cpp:87-380) is the only executable specification of the stream: 60 bpm, 8-beat segments, 1 s cycles, exact keys such as `000003000000_Delete_PercLoop1Audio_at[000000000000,000002000000]_vol[100,100]` (line 214), i.e. a 2 s loop pick created at cycle 0 and deleted on the first cycle after its stop. The engine never mixes, never opens audio, never shifts or stretches: `InstrumentAudio` carries `tempo`, `loopBeats` and `tones` (InstrumentAudio.h:17-27) but nothing at dub time consumes them.

## 14. Identifiers and numeric types

- `UUID` is `std::string` (EntityUtils.h:28). Content ids come from JSON as UUID strings. **Segment-side ids (chain, choices, arrangements, picks, chords, voicings, memes, messages) are `EntityUtils::computeUniqueId()`, a 12-hex-digit zero-padded process counter** such as `000000000042` (EntityUtils.cpp:10-14), not RFC 4122. They restart at 0 in every process and are shared across engines in one process. The proposal's `uint8_t active_audio_id[16]` cannot be a byte-cast of today's pick id.
- Micros are a mix of `unsigned long long` (API, `ActiveAudio`), `long` (`Segment`, picks, `CraftWork` members, `ChainUtils`), `unsigned long` (`DubWork`) and `int` (`WorkSettings`). `long` is 32-bit on MSVC.
- Segment ids are `int`, monotonic from 0, never reused within a chain except that `deleteSegmentsAfter` plus `buildSegmentFollowing` re-issues the next id after a rewrite.

## 15. Implications for V2 specs

1. **C ABI, lifecycle.** `xj_engine_start` must either clear the segment store (as `Engine.h:40` already claims) or document that a second start continues the chain; settings are immutable after construction, so horizon changes need `destroy`+`create` or a new setter. `xj_engine_finish` must either tell hosts to call `run_cycle` once more for the trailing `Delete`s or emit them itself.
2. **C ABI, status.** `WorkState::Failed` is unreachable; expose `CraftWork::isFinished()` / `DubWork::isFinished()` or make `didFailWhile` propagate, add a log callback replacing `cout/cerr`, and give `SegmentMessage`s (the proposal's section 10) an actual path out of the store.
3. **C ABI, events.** Specify event ordering (today: set order then map order, not time order), that `Delete` can mean revocation of audio already playing, that `Update` may repeat every cycle until the `emplace` at WorkManager.cpp:86 is fixed, and that `getPick()` is invalid on a `Delete`. Use `uint64_t` for every micros field and widen the `long` fields before any Windows host runs past 35 minutes.
4. **C ABI, identifiers.** Make `computeUniqueId` produce 128-bit ids, or define `active_audio_id` as the 12-hex string / a `uint64_t`. For Sessions, segment-side ids must be unique across stations; today they collide by construction.
5. **Commit horizon.** The implicit horizon today is `deadlineMicros` (1 s) with floor-then-ceil arithmetic that can land inside, or before, the deadline at slow tempos; `dubAheadMicros` is not a commitment. The cue spec must define the horizon in chain micros, compute `earliest_landing` with exact beat arithmetic, decide whether revoking already-emitted events inside the horizon is allowed (today it is), and allow sub-second settings (today the API takes whole seconds).
6. **Cutoff correctness.** Unify the three beats-to-micros formulas (CraftWork.cpp:300, 336; Fabricator.cpp:754) and make the child-entity `put` overwrite (SegmentEntityStore.cpp:16), or loop and chord tails will keep crossing cue boundaries.
7. **Tempo policy.** The rewrite reads `mainProgram->tempo` (CraftWork.cpp:300) while the cutoff reads `segment->tempo` (336) and `Fabricator::getMicrosPerBeat` caches the first tempo it sees (Fabricator.cpp:1038). In `External` tempo mode all three must read `segment.tempo`.
8. **Foundation source.** `doFabricationRewrite` needs `getMainProgram(segment)` and its `ProgramConfig` to compute the cutoff (283-298) and returns without rewriting if the main program is absent (284-288). A follower must get `barBeats` and `cutoffMinimumBars` from the foundation record or its template, exactly as 04-engine section 3 lists. The rewritten segment is forced `NextMacro` (321-322); follower rejoin and conductor cue both reuse this path.
9. **Persistence window.** Fix the units (CraftWork.cpp:28, multiply by `MICROS_PER_SECOND`) **together with** `segments.at(0)` in DubWork.cpp:112 (use the segment spanning `at`), and add a test for an audio tail crossing a segment boundary; fixing one alone exposes the other.
10. **Overrides to cues.** No clear/cancel exists; cue semantics need `xj_engine_cancel_cue` and a defined interaction between macro and meme overrides (today both persist independently and forever). `getAndResetDidOverride` is the only acknowledgement and is not on `Engine`; a cue API needs a landing acknowledgement with the landing chain micros.
11. **Determinism.** Seeding must cover `StickyBun::gen` and `ValueUtils::gen` (process-global), `MarbleBag`, `Craft`, and the local generator in `Fabricator::chooseVoicing`. The two globals make "two engines in one process" (the follow-mode test plan) share random state and id counters until they are moved per chain.
12. **Threading.** "One handle, one thread" must also forbid the direct `getSegmentStore()` reads the UI does today from another thread; the ABI's JSON snapshot functions exist to end that pattern. Because of the globals, even two handles on two threads race until the statics are per instance.
13. **Template selection.** `CraftWork::start` and `WorkManager` use an arbitrary template from `projectContent` (pointer-ordered set), and `Fabricator` honors every template's bindings. `xj_engine_start(template)` should hand the chosen template to `CraftWork` and craft from `templateContent`.
14. **Host guide.** The only working reference for playback is `EngineTest::CorrectlyDubsAudio`; the example app loads files but never plays. The workstation/DSP spec should state the transient-subtraction convention, the release fade, the `from/to` glide, and the fact that `dubMasterVolume` is not applied by the engine, as sections 5 and 10 define them.
