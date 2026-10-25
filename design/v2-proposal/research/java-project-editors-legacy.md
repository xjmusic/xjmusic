# Grounded map: Java workstation project management, serialization, editors, fabrication tab, legacy code

All paths relative to `/Users/nck/xj/xjmusic/workstation/` unless absolute. Line numbers from `grep -n` against the tree at commit `a94000b9a` (version 1.9.6). Totals: 294 Java files, 47,463 lines under `src/main/java/io/xj` (engine 12,256 · gui 16,622 · model 18,585).

## 1. Package inventory (non-recursive LOC)

| Package | Files | LOC | Role |
|---|---|---|---|
| `gui/project` | 6 | 2,284 | `ProjectManager` (interface, 476) + `ProjectManagerImpl` (1,644): open/create/demo/save/build/export + entity CRUD |
| `gui/services` + `impl` | 8 + 5 | 1,336 + 2,708 | JavaFX property-based services (below) |
| `gui/controllers` | 11 | 2,361 | main window, menu, modals (project creation, settings, EULA, about, Cmd) |
| `gui/controllers/content/**` | 21 | 4,823 | library/instrument/audio/program editors (bind, chord, event modes) |
| `gui/controllers/fabrication` | 2 | 1,232 | timeline (933) + controls (299) |
| `gui/controllers/template` | 4 | 711 | template browser/editor/add-binding/export |
| `gui/types`, `gui/utils`, `gui` root | 8+3+9 | 196+380+591 | enums, UI helpers, Spring/JavaFX bootstrap |
| `model/HubContent.java` etc. | 8 | 3,285 | `HubContent` (1,725) in-memory store, 3 HOCON configs, legacy `Hub*` payload classes |
| `model/pojos` | 33 | 5,459 | content + segment + user entities |
| `model/entity` (+common) | 11+3 | 1,866+289 | `EntityFactory`, reflection `EntityUtils`, `ContentEntityStore` |
| `model/json` | 4 | 118 | Jackson `JsonProvider` |
| `model/jsonapi` | 14 | 2,587 | JSON:API payload layer (legacy) |
| `engine/hub_client`, `http`, `telemetry` | 3+2+3 | 205+38+104 | cloud-era |
| `engine/{craft,fabricator,work,mixer,ship,audio,util}` | — | ~11,900 | the second engine (out of scope here) |

Resources: `src/main/resources/views/**/*.fxml` (42 FXML files), `application.properties` (defaults below), `EULA.txt`, `styles/default-theme.css`, Roboto fonts.

## 2. Project management (`io.xj.gui.project`)

### 2.1 Wiring and state

`ProjectManagerImpl` is constructed in `gui/WorkstationConfiguration.java:85-95` with `JsonProvider, EntityFactory, HttpClientProvider, HubClientFactory, AudioLoader`. It holds `AtomicReference<HubContent> content`, `AtomicReference<Project> project`, `projectPathPrefix`, `projectName`, `demoBaseUrl` (default `https://audio.xj.io/`, `ProjectManagerImpl.java:103`), and four callbacks (`onProgress`, `onProgressLabel`, `onStateChange`, `onBuildStateChange`). `ProjectState` enum (`ProjectState.java`): Standby, CreatingFolder, CreatedFolder, LoadingContent, LoadedContent, LoadingAudio, LoadedAudio, Ready, ExportingTemplate, BuildingProject, Saving, Cancelled, Failed. `ProjectBuildState`: Standby, Dirty, Clean, Building, Failed.

`ProjectServiceImpl` (gui/services/impl, 1,226 lines) wraps the manager for the UI: every operation runs on a fresh `Thread` started via `Platform.runLater` (`executeInBackground`, :1148-1152); callbacks are marshalled back with `Platform.runLater` (:177-180). Any `didUpdate(type, modified)` sets `isModified` and `buildState = Dirty` (:547-553) and fires per-type listeners registered with `addProjectUpdateListener` — this is how editors refresh.

### 2.2 Create / Open / Demo

| Operation | Code | Behavior |
|---|---|---|
| New project | `createProject` `ProjectManagerImpl.java:~565` | `createProjectFolder(parent, name)` → new `Project{id, name, platformVersion}` → `HubContent` with only that project → `saveProjectContent` |
| Open | `openProjectFromLocalFile` `:~514` | Regex `ProjectPathUtils.matchPrefixNameExtension` splits prefix/name; reads the `.xj` plus **every** `*.json` found recursively under `<prefix>/libraries` and `<prefix>/templates` (`LocalFileUtils.findJsonFiles` `model/util/LocalFileUtils.java:96-107`, `Files.walk`, suffix filter only); each file is deserialized to `HubContent`; `HubContent.combine` (`model/HubContent.java:115-123`) merges by `putAll` and ORs the `demo` flag. `build/` and `render/` folders are ignored by construction. |
| Version gate | `ProjectServiceImpl.java:224-235` | After open: if `platformVersion` null or `< versionService.getVersion()` (`ValueUtils.compareMonotonicVersion`) → confirm "Legacy Project Version… migrate and save" → `saveProject`; if newer → warn and close. |
| Demo | `createProjectFromDemoTemplate` `:154-274` | **Not git/GitHub.** HTTP GET `String.format("%s%s.json", baseUrl, templateKey)` (`engine/hub_client/HubClientFactoryImpl.java:52-53`) deserialized straight to `HubContent` (`:65`). Then per instrument audio (sorted by name): `HEAD` for `Content-Length` (`getRemoteFileSize` `:117`), compare with local size, `downloadRemoteFileWithRetry` (`:75`, retries from `demo.audioDownloadRetries=3`) into the **ideal** path (below); audio whose remote file is 404 is deleted from content (`:240-245`). Finally `saveProjectContent`. Template keys are the FXML `id`s of the four demo toggles: `vgm`, `bump_deep`, `slaps_lofi`, `space_flow` (`views/project-creation-modal.fxml:90,107,124,141`); project name is prefilled e.g. "Video-Game-Demo". |
| Recent projects | `ProjectServiceImpl.java:1189-1205` | `List<ProjectDescriptor(project, filename, path)>` serialized as JSON into `java.util.prefs` key `recentProjects`, max `view.recentProjectsMax=10`. |

### 2.3 Save (`saveProjectContent`, `ProjectManagerImpl.java:~1370-1520`) — the V1 on-disk format of record

1. `cleanupOrphans(content)` (`:~1270-1300`): 26 `deleteAllIf` rules dropping any child whose parent is missing (instrument→library, audio/meme→instrument, program→library, sequence/voice/track/pattern/event/chord/voicing/binding/bindingMeme→program and intermediate parents, templateBinding→template).
2. `project.setPlatformVersion(platformVersion)`; `content.setProject(project)`.
3. Walk the existing project folder (`Files.walk`) and remember every regular file and directory whose path starts with `/instrument`, `/libraries`, `/templates`, or ends with `.xj` (`:~1398-1410`). Anything in those areas not re-written below is **deleted** at the end (`:~1495-1510`). Other folders (`build/`, `render/`, user files) are untouched.
4. Writes, all via `writeContentToJsonFile` = `jsonProvider.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(HubContent)` (`:~1555`):

| File | Content (a `HubContent` subset) |
|---|---|
| `<Project>.xj` (name = `toAlphanumericHyphenated(projectName) + ".xj"`, `getProjectFilename` `:~600`) | only `project` + `demo` flag (`:~1414-1418`) |
| `templates/<Template>/<Template>.json` | `subsetForTemplateId`: template + its `TemplateBinding`s (`HubContent.java:169-174`) |
| `libraries/<Library>/<Library>.json` | the library only (`new HubContent(Set.of(library))`) |
| `libraries/<Library>/<Program>/<Program>.json` | `subsetForProgramId` (`HubContent.java:146-160`): program, memes, sequence bindings + binding memes, chords + voicings, patterns + events, sequences, tracks, voices |
| `libraries/<Library>/<Instrument>/<Instrument>.json` | `subsetForInstrumentId` (`:131-136`): instrument, audios, memes — written **after** audio files are relocated so new `waveformKey`s are included |
| `libraries/<Library>/<Instrument>/<waveformKey>` | the audio file itself |

Folder/file names use `StringUtils.toAlphanumericHyphenated` = replace `[^a-zA-Z0-9.\-]` with `-` (`model/util/StringUtils.java:26,120-124`); note it keeps `.` and does not collapse runs, so "My  Lib" → "My--Lib". Two entities whose names differ only by punctuation collide on disk (last writer wins, silently).

5. Audio relocation per audio (`:~1440-1480`): ideal `waveformKey = computeWaveformKey(instrument, audio, ext)` = `"<Instrument>-<AudioName>-<Tones>.<ext>"` with accidentals rewritten (`LocalFileUtils.java:78-87`, `Accidental.replaceWithExplicit`). If the ideal path does not exist, copy/move from, in order: prior project path (Save As), prior legacy path, legacy `instrument/<instrumentId>/<waveformKey>` (`getLegacyPathToInstrumentAudio` `:~1525`), or the current non-ideal path; then `update(InstrumentAudio, "waveformKey", ideal)`. Audio with empty `waveformKey` is deleted. If `lengthSeconds` is null/0 it is computed by `AudioLoader.load(...).lengthSeconds()` (issue #450).
6. Progress is reported as savedItems/(templates+libraries+programs+instruments+audios).

Tests that pin this format: `src/test/java/io/xj/gui/project/ProjectManagerImplStructureMigrationTest.java` (3 tests, fixture `src/test/resources/LegacyExampleProject/` = one `.xj` holding everything + `instrument/<uuid>/*.wav`; asserts the migrated tree `templates/Legacy-Template/Legacy-Template.json` has 3 entities, `libraries/Legacy-Programs/Legacy-Beat/Legacy-Beat.json` 10, etc., and waveform keys like `Test-Instrument-Test-Ambience-Loop-X.wav`), and `ProjectManagerImplTest.java` (31 tests on create/move/duplicate defaults and name enumeration).

### 2.4 Build (`buildProject`, `ProjectManagerImpl.java:~385-470`)

- Output dir: `<project>/build/`. Content = `new HubContent(entityFactory.copyAll(content.getAll()))` (full copy of **all** entities, every template), then `cleanupOrphans`.
- Every audio: `build/<audioId>.<container-lowercase>` via `resampleAudioIfNeeded(src, dst, frameRate, sampleBits, channels)` (`:~476-505`): if the destination exists and `javax.sound.sampled.AudioSystem.getAudioFileFormat` reports matching frame rate/bits/channels it is skipped, else deleted and re-rendered by `FFmpegUtils.resampleAudio` (`engine/mixer/FFmpegUtils.java:31-82`, javacv `FFmpegFrameGrabber`/`FFmpegFrameRecorder`, PCM codec chosen by bit depth: 8→U8, 16→S16LE, 32→S32LE, 64→S64LE). Missing source files → audio deleted from built content.
- `builtContent.update(InstrumentAudio, id, "waveformKey", "<audioId>.<ext>")`; JSON written to `build/<toLowerHyphenatedSlug(projectName)>.json` (pretty-printed). `toLowerHyphenatedSlug`: spaces→`-`, strip `[^a-zA-Z0-9\-]`, lowercase (`StringUtils.java:29,172`).
- Settings come from `ProjectService` properties persisted in prefs: `outputContainer` (`AudioFileContainer`: WAV, OGG, FLAC, MP3, M4A, AAC), `outputFrameRate` (default `48000`, stored as string "48000.0" via `Double.toString`), `outputSampleBits` (`16`), `outputChannels` (`2`) (`application.properties:2-5`, `ProjectServiceImpl.java:1174-1185`). Note `buildProject` parses frame rate with `(int) Float.parseFloat`.
- UI: Build button + animated icon in `MainMenuController` (`:324-330`, `handleBuild`, `handleBuildSettings`), enabled only when `ProjectState.Ready`; build state Dirty after any edit.

### 2.5 Export template (`exportTemplate`, `ProjectManagerImpl.java:~292-384`)

- `TemplateExportModalController` (158 lines): export name defaults to `toLowerScored(shipKey or name)`; audio format combo "Original" vs converted (`:135`), conversion uses the build settings; path from `exportPathPrefix` pref.
- Content = `entityFactory.forTemplate(content, template)` (`model/entity/EntityFactoryImpl.java:227-257`): the template, its bindings, bound libraries/programs/instruments, and for bound libraries only programs/instruments in state `Published`, plus all program sub-entities and instrument audios/memes. Then `cleanupOrphans`.
- Audio copied (or converted) to `<exportPath>/<exportName>/<shipKey>-<audioId>.<ext>`; `waveformKey` rewritten accordingly; JSON `<exportName>.json`. This is the flat "one JSON + files" layout the C++ engine example and Unreal consume, whereas Build keeps `<audioId>.<ext>` names.

### 2.6 Entity creation defaults (parity-relevant constants, `ProjectManagerImpl.java:78-98`)

| Entity | Defaults |
|---|---|
| Template | `config = new TemplateConfig().toString()` (full default HOCON), `isDeleted=false`, name uniquified by `FormatUtils.iterateNumericalSuffixFromExisting` ("Name 2", "Name 3") |
| Library | `isDeleted=false`, projectId |
| Program | type/state/tempo/key inherited from first program in same library, else any program, else `Main`/`Published`/`120`/`"C"`; `config = new ProgramConfig().toString()` |
| ProgramSequence | name "New Sequence", `total` from sibling else 4, key from sibling else "C", intensity from sibling in library else 1.0 |
| ProgramSequencePattern | name "New Pattern", `total` from sibling pattern else sequence total |
| ProgramSequencePatternEvent | velocity 1.0, tones "X", position/duration from the click |
| ProgramVoice | name "New Voice", type = lowest `InstrumentType` not already used in program (else Drum), order = max+1 |
| ProgramVoiceTrack | name "New Track", order max+1 |
| Memes (program, instrument, binding) | name "XXX" |
| Instrument | type/mode/state/volume inherited from sibling else `Drum`/`Event`/`Published`/`1.0`; `config = new InstrumentConfig().toString()`; instrument folder created immediately |
| InstrumentAudio (`createInstrumentAudio(instrument, filePath)`) | name = file stem; tones/event "X"; intensity 1.0; tempo from: sibling audio in library → program in library → any audio → any program → 120; loopBeats 4.0; transientSeconds 0; volume 1.0; file copied into the instrument folder under the computed waveform key; `lengthSeconds` from `AudioLoader` (javax.sound; WAV/AIFF only in practice, loader throws on >2^31 frames) |

Duplicate operations (`duplicateTemplate/Library/Program/ProgramSequence/ProgramSequencePattern/Instrument`, `:~975-1265`) deep-copy via `EntityFactory.duplicateAll(entities, newRelationships)` with fresh UUIDs and re-pointed parent ids; `duplicateInstrument` also copies audio files to the new folder; `duplicateLibrary` recursively duplicates programs and instruments with "Copy of …" names.

## 3. JSON serialization (`io.xj.model.json`, `HubContent`, pojos)

`JsonProviderImpl` (`model/json/JsonProviderImpl.java:24-31`): one Jackson `ObjectMapper` with `WRITE_DATES_AS_TIMESTAMPS=false`, `FAIL_ON_UNKNOWN_PROPERTIES=false` (unknown keys silently dropped on read → forward-compatible but lossy), `ORDER_MAP_ENTRIES_BY_KEYS=true`, inclusion `NON_NULL`; `Instant` serialized truncated to microseconds (`model/json/InstantSerializer.java`; a duplicate pair also lives in `model/entity/`). Every file is written with `writerWithDefaultPrettyPrinter()` (2-space indent, `"key" : value` style).

`HubContent` is the document model and the in-memory store: `Map<Class<?>, Map<UUID, Object>> store` (ConcurrentHashMap, `HubContent.java:52`), `List<Error> errors` (`java.lang.Error`, no import — fixture `test/resources/project/test-project.xj` really contains a serialized stack trace), `boolean demo`. JSON keys are the getter names, in this getter order: `errors` (NON_EMPTY), `users`, `projectUsers`, `instruments`, `instrumentAudios`, `instrumentMemes`, `programs`, `programMemes`, `programSequences`, `programSequenceBindings`, `programSequenceBindingMemes`, `programSequencePatterns`, `programSequencePatternEvents`, `programSequenceChords`, `programSequenceChordVoicings`, `programVoices`, `programVoiceTracks`, `libraries`, `templates`, `project` (single object, `:557-560`), `templateBindings`, `demo` (always written, `"demo": false`). All list getters are `@JsonInclude(NON_EMPTY)`. Read-side `setProjects(List)` keeps the first project for backward compatibility (`:1575-1578`). Jackson property ordering is the default (not alphabetical), so the physical order above is what a Rust writer must reproduce for byte-identical output.

**Stable ordering** inside arrays comes from `getAll(type)` → `values().stream().sorted()` (`:1640`), i.e. each pojo's `compareTo`: name then id for Project, Library, Template, Instrument, InstrumentAudio, InstrumentMeme, Program, ProgramMeme, ProgramSequence, ProgramSequenceBindingMeme, ProgramSequenceChord, ProgramSequencePattern; `TemplateBinding` by templateId then id; `ProgramSequenceBinding` by programSequenceId then id; `ProgramSequenceChordVoicing` by programSequenceChordId then …; `ProgramVoice`/`ProgramVoiceTrack` by programId then …; `ProgramSequencePatternEvent` by patternId, then trackId, then … (see each `model/pojos/*.java` `compareTo`, e.g. `Program.java:228-232`, `ProgramSequencePatternEvent.java:191-197`). Entities with equal sort keys fall through to UUID order, so output is deterministic for a given UUID set.

Pojo fields (private, with getters/setters, no Jackson annotations, so JSON key = field name):

| Pojo | Fields |
|---|---|
| Project | id, name, updatedAt (Long epoch ms), platformVersion, isDeleted |
| Library | id, name, projectId, isDeleted, updatedAt |
| Template | id, projectId, name, config (HOCON string), shipKey, isDeleted, updatedAt |
| TemplateBinding | id, type (`ContentBindingType`: Library/Program/Instrument), templateId, targetId |
| Instrument | id, libraryId, type (`InstrumentType` ×10), state (`InstrumentState`), name, config, isDeleted, volume (Float), mode (`InstrumentMode`), updatedAt |
| InstrumentAudio | id, instrumentId, name, waveformKey, transientSeconds, lengthSeconds, loopBeats, tempo, intensity, event, volume, tones (all Float except strings); dead private field `legngthSeconds` with no accessor (`InstrumentAudio.java:20`) |
| InstrumentMeme / ProgramMeme | id, instrumentId|programId, name |
| Program | id, libraryId, state, key, tempo (Float), type (`ProgramType` Macro/Main/Beat/Detail), name, config, isDeleted, updatedAt |
| ProgramSequence | id, programId, name, key, intensity (Float), total (Short) |
| ProgramSequenceBinding | id, programId, programSequenceId, offset (Integer) |
| ProgramSequenceBindingMeme | id, programId, programSequenceBindingId, name |
| ProgramSequenceChord | id, programId, programSequenceId, name, position (**Double**) |
| ProgramSequenceChordVoicing | id, programId, programSequenceChordId, notes, programVoiceId |
| ProgramVoice | id, programId, type (InstrumentType), name, order (Float) |
| ProgramVoiceTrack | id, programId, programVoiceId, name, order (Float) |
| ProgramSequencePattern | id, programId, programSequenceId, programVoiceId, name, total (Short) |
| ProgramSequencePatternEvent | id, programId, programSequencePatternId, programVoiceTrackId, velocity, position, duration (**Float**), tones |

`updatedAt` is never maintained by the workstation; whatever came from the hub is preserved and new entities omit it (NON_NULL). `isDeleted` is written `false` on new entities; nothing filters on it in the editors.

**HOCON configs** (`model/TemplateConfig.java:30-170`, `InstrumentConfig.java:24-31`, `ProgramConfig.java:21-25`) are stored as strings. Parsing uses `ConfigFactory.parseString(text).withFallback(DEFAULT)`. `toString()` re-emits **every** key sorted alphabetically, one `key = value` per line (`TemplateConfig.java:262`), using `StringUtils.formatMultiline` which joins with `System.lineSeparator()` (`StringUtils.java:59-62`) — CRLF on Windows, LF elsewhere. The editors normalize on blur (`TemplateEditorController.java:113`, `InstrumentConfigController.java:79`, `ProgramConfigController.java:79`), so saved configs are canonical-but-platform-dependent. Defaults worth citing: ProgramConfig `barBeats=4, cutoffMinimumBars=2, doPatternRestartOnChord=false`; InstrumentConfig `isAudioSelectionPersistent=true, isMultiphonic=false, isOneShot=false, isOneShotCutoffEnabled=true, isTonal=false, oneShotObserveLengthOfEvents=[], releaseMillis=5`; TemplateConfig 30 keys incl. per-InstrumentType maps `choiceMuteProbability`, `dubMasterVolume`, `intensityLayers` (Background 3, Bass 1, Drum 1, Hook 3, Pad 3, Percussion 3, Stab 2, Sticky 2, Stripe 2, Transition 3), `intensityThreshold` (0.5), `detailLayerOrder=["Bass","Stripe","Pad","Sticky","Stab"]`, `memeTaxonomy` (COLOR/SEASON sample), `mixer*` (compressAhead 0.05, decay 0.125, ratioMax 1.0, ratioMin 0.3, toAmplitude 1.0, dspBufferSize 1024, highpass 30, lowpass 15000, normalizationBoostThreshold 1.0, normalizationCeiling 0.999), `mainProgramLengthMaxDelta=280`, `deltaArcEnabled=false`, `stickyBunEnabled=true`, `intensityAutoCrescendo{Enabled=true,Minimum=0.2,Maximum=0.8}`.

Attribute updates from editors go through `HubContent.update(type, id, attribute, value)` (`:1507-1513`) → `EntityUtils.set` (`model/entity/EntityUtils.java:171-183`), reflection on `setX` by name with type coercion from the text field string; a wrong attribute name throws `EntityException`.

## 4. Editors: parity checklist (every user operation)

Navigation model: `UIStateService.navigateTo(Route)` with a 99-entry history (`view.navHistory`), `Route` enum (`gui/types/Route.java`): ContentLibraryBrowser, ContentLibraryEditor, ContentProgramBrowser, ContentProgramEditor, ContentInstrumentBrowser, ContentInstrumentEditor, ContentInstrumentAudioEditor, TemplateBrowser, TemplateEditor, FabricationTimeline, FabricationSegment (declared, never navigated to). Three top-level view modes (Content / Templates / Fabrication) via menu radio items and toolbar toggles (`MainMenuController.java:341-358`). All editors commit a field on blur or Enter (`UiUtils.onBlur`, `blurOnEnterKeyPress`, `onChange`, `gui/utils/UiUtils.java:151-219`); there is **no undo** anywhere.

### 4.1 Library browser / editor (`ContentBrowserController`, `LibraryEditorController`, `CmdModalController`)
- Three `TableView`s (libraries, programs, instruments); double-click opens editor; right-click/quick-action menu → Edit, Duplicate, Move (program/instrument), Delete.
- Toolbar: "up a level", Programs/Instruments toggle within a library, Create-entity button (text depends on route).
- `CmdModalController` (489) implements Create/Duplicate/Move/Delete for Template, Library, Program, Instrument with a name field and library chooser; Delete Library warns with counts of programs/instruments/template bindings and confirms "cannot be undone"; Delete Program warns about template bindings. Deletes are **non-cascading** `HubContent.delete(type,id)` (`ProjectServiceImpl.java:464-472`); children become orphans until the next save's `cleanupOrphans`.
- Library editor: rename only (`fieldName`, OK/Cancel).

### 4.2 Instrument editor (`InstrumentEditorController`, 334)
Fields: name (text), type (ComboBox `InstrumentType`), mode (`InstrumentMode`), state (`InstrumentState`), volume (text→Float). Memes container (`EntityMemesController`: add tag "XXX", edit inline, Enter commits, empty text deletes, × deletes). Actions: Duplicate (via Cmd modal), Edit Config (popup `TextArea` of HOCON, Save validates by `new InstrumentConfig(text)`), Open instrument folder (desktop), **Import audio** (`ProjectUtils.chooseAudioFile`, filter `*.wav, *.aiff, *.mp3, *.aac, *.flac`, `gui/utils/ProjectUtils.java:66`) → `createInstrumentAudio` → navigates to audio editor. Audios `TableView` columns Name, Event, Volume, Intensity, Tones, Tempo, Transient, Loop Beats; double-click edits; context menu Edit/Delete (confirm).

### 4.3 Instrument audio editor (`InstrumentAudioEditorController`, 502)
Fields (all commit on blur): name, event, volume, tones, tempo, intensity, transientSeconds, lengthSeconds (editable text, though normally computed), loopBeats; label shows waveform file name. Waveform view: `ImageView` rendered pixel-by-pixel into a `WritableImage` (`renderWaveform` `:378-447`): height `instrumentAudioEditor.heightPixels=1000`, max width 10,000 px, min 2 samples/pixel, display normalization to 0.95 (`computeDisplayVolumeRatio`), one lane per channel, grid lines every beat before/after the transient computed from `tempo`, dashed transient marker (4-px dashes, color `#ff17ff`), sample color `#17ef17`, grid `#606060`. Actions: Zoom in/out (integer zoom ratio ≥1), **Set transient** toggle then click on the waveform to place `transientSeconds` (`handleClickedWaveformContainer` `:303-325`), Open audio folder, Open audio file (OS default app). No playback, no loop-point markers, no trimming, no re-import, no drag-drop.

### 4.4 Template browser / editor (`gui/controllers/template/*`, 711)
Browser table: double-click edit; menu Edit, Duplicate, Delete, **Export** (modal above). Editor: name, shipKey (forced `toLowerScored` on blur), config `TextArea` (normalized through `TemplateConfig` on blur; parse errors only logged, `:110-118`), bindings table (Type, Name; double-click jumps to the bound entity; context Delete), **Add binding** modal: choose Library, then optionally Programs/Instruments toggle and a specific program or instrument → `createTemplateBinding(templateId, type, targetId)` (`ProjectServiceImpl.java:~886-897`). No duplicate-binding check.

### 4.5 Program editor shell (`ProgramEditorController`, 473)
Program fields: name, type (ComboBox; `updateProgramType` runs a destructive cascade with confirmation — to Main deletes tracks/patterns/events; to Macro deletes chords/voicings/voices/tracks/patterns/events; to Beat/Detail deletes bindings/binding memes/chords/voicings, `ProjectServiceImpl.java:1021-1099`), state, key, tempo; program memes container. Mode toggles **Bind** (enabled for Macro/Main) and **Edit** (disabled for Macro) → `ProgramEditorMode {Bind, Edit}`. Sequence selector popup (choose current sequence) and sequence action popup: Create sequence, Delete (blocked if bindings or patterns reference it), Duplicate. Current-sequence fields: name, total (validated ≥1; cannot shrink below any pattern total), key, intensity. Timeline options (Edit mode only): grid (1/2…1/32; `programEditor.gridChoices=0.5,0.25,0.125,0.0625,0.03125`, default 1/4), zoom (5%…400%, default 50%), snap toggle; base size 300 px/beat. Duplicate program (Cmd modal), Edit Config popup.

### 4.6 Bind mode (`bind_mode/*`, 403)
Columns per offset 0…maxOffset+1 (`BindModeController.java:102-116`); each column: offset label, "+" → popup selector of the program's sequences → creates `ProgramSequenceBinding{offset}`; each bound item shows sequence name, its own memes container (`createProgramSequenceBindingMeme`), and delete (refused if the binding has memes, `SequenceBindingColumnController.java:116-118`). No drag between offsets, no reorder.

### 4.7 Chord edit mode (`chord_edit_mode/*`, 943; active when Edit mode and program type Main)
Chord timeline for current sequence: Add chord (modal properties: name, position validated 0 ≤ p < sequence total and unique), click chord → properties modal (edit name/position) or right-click/ctrl-click quick menu → Delete. Chord width = distance to next chord or sequence end. Voice lanes: Add voice; per-voice type ComboBox and action popup (New Voice / Delete voice with cascade confirmation); one voicing cell per chord per voice, auto-created with empty `notes` on display (`getOrCreateVoicing`, `ChordVoiceTimelineController.java:181-191` — viewing creates entities and dirties the project), notes text field commits on every keystroke (`SequenceChordVoicingController.java:62-65`).

### 4.8 Event edit mode (`event_edit_mode/*`, 1,182; active when Edit mode and type ≠ Main)
Per voice (`EventVoiceController`): name, type ComboBox, voice action popup (New Voice/Delete), pattern selector popup (patterns of current sequence × voice), pattern action popup (New Pattern / Delete with confirm / Duplicate), pattern name and total (validated ≥1, ≤ sequence total; shrinking deletes events past the end after confirm). Per track (`VoiceTrackTimelineController`): name, track action popup (Delete, confirm if events), Add track button on the last track; click on the empty active region creates an event at the (optionally snapped) position with duration = grid (`:183-220`). Per event (`SequencePatternEventController`): drag body to move, drag left edge to change start+duration, drag right edge to change duration (all live `projectService.update(event)`), click → properties modal (tones, velocity as %, position shown 1-based, duration), right-click → Delete. Velocity drawn as shaded height. Keyboard editing, multi-select, copy/paste: **none**.

### 4.9 Project-level actions (`MainMenuController`, modals)
New, Open (`*.xj` chooser), Open Recent (10), Demos, Save (enabled when modified), Save As (re-homes audio), Close, Quit (all with "Save changes?" prompt: Save / Don't Save / Cancel, `ProjectServiceImpl.java:914-945`), Build, Build Settings, Settings (General: projects path, export path; Fabrication: control mode, craft/dub/mixer seconds, timeline limit; Build: frame rate, channels, container, sample bits, reset-to-defaults), View mode radios, Show/Tail logs, log level, Help links (guide, tutorial, Discord), About, EULA on first run (`eula.accepted` pref, accept enabled only after scrolling to the end).

## 5. Fabrication tab

### 5.1 Controls (`FabricationControlsController`, 299; built in code, mounted by `fabrication-timeline-controls.fxml`; its declared `main-pane-right.fxml` resource does not exist, the FXML `fx:controller` wires it)
Visible only when control mode ≠ AUTO (`UIStateServiceImpl.java:155`), populated only while fabrication is Active (`:158-160`). Elements: **Override Intensity** `ToggleSwitch` + `Slider` 0–100 (initial 38 = `fabrication.baseIntensity=0.38`; writes `intensityOverrideProperty` which calls `FabricationManager.setIntensityOverride(double|null)`); in MACRO mode a `ToggleGroup` with one `ToggleButton` per macro program plus **Engage** (→ `doOverrideMacro(program)`); in TAXONOMY mode one labeled `ToggleGroup` per `memeTaxonomy` category, first meme preselected, plus Engage (→ `doOverrideMemes(selected)`). Engaged state shown by `:engaged` pseudo-class. On start, MACRO preselects the alphabetically first macro and TAXONOMY the first meme of each category (`FabricationServiceImpl.java:213-228`, issue #201).

### 5.2 Settings and lifecycle (`FabricationServiceImpl`, 627)
Prefs keys `craftAheadSeconds` (default `fabrication.defaultCraftAheadSeconds=60`), `dubAheadSeconds` (2), `mixerLengthSeconds` (1), `macroMode` (default `TAXONOMY`), `timelineSegmentViewLimit` (`view.timelineMaxSegments=20`). `start()` (`:183-245`) builds `FabricationSettings{craftAhead, dubAhead, mixerLength, controlMode, inputTemplate, outputChannels, outputFrameRate}` (plus fixed `persistenceWindowSeconds=3600`, `shipOutputPcmChunkSizeBytes=1024`), takes `projectService.getContent(template)` (the `forTemplate` subset, so unpublished content never plays), and calls `FabricationManager.start(content, config)` on the FX thread. `FabricationState`: Standby, Starting, PreparingAudio, PreparedAudio, Initializing, Active, Done, Cancelled, Failed. Main action button text Start/Stop; `cancel()` → `finish(true)`. Follow-playback toggle.

### 5.3 Timeline (`FabricationTimelineController`, 933)
Refresh every `view.timelineRefreshMillis=618` ms; segment width `320` px, spacing 4, `view.timelineDisplaySegmentsBeforeNow=5`; segments are re-rendered when their choice hash changes (`SegmentEntityStore.readChoiceHash`, rechecked at most `view.timelineSegmentHashRecheckLimit=3` times) or after an override (`getAndResetDidOverride`). Four shaded regions scroll smoothly: past, shipped-to, dubbed-to, crafted-to (`getShippedToChainMicros`, `getDubbedToChainMicros`, `getCraftedToChainMicros`). Per segment (`buildSegment` `:469-660`): header row with id/type/delta, properties **Intensity** (`%.2f`), **Tempo**, **Key**; **Memes** list and **Chords** list; **choices** grouped as Macro, Main, Beat, then one group per non-Drum `InstrumentType`, each item showing program (+ sequence binding), voice (order and name), instrument, and `[deltaIn … deltaOut]` when `TemplateConfig.deltaArcEnabled`; items muted (`mute`, or outside delta window) get `choice-group-item-muted`; **Metas** (key/value) and **Messages** (INFO/WARNING/ERROR). Data read directly from `SegmentEntityStore` via `FabricationService.getSegment{Memes,Chords,Choices,Messages,Metas}`, `getArrangements`, `getPicks`. Segment pojo fields shown/available: id, chainId, type, state, beginAtChainMicros, durationMicros, key, total, intensity, tempo, storageKey, waveformPreroll, waveformPostroll, delta (`model/pojos/Segment.java:24-40`); `SegmentChoice`: segmentId, programId, programSequenceId, programSequenceBindingId, programVoiceId, instrumentId, deltaIn, deltaOut, mute, instrumentType, instrumentMode, programType (`SegmentChoice.java:16-28`). Nothing shows *why* a choice was made (no candidate list), nor picks on the timeline.

## 6. Services (`io.xj.gui.services`)

| Interface (LOC) → Impl (LOC) | Responsibility, notable API |
|---|---|
| `ProjectService` (634) → `ProjectServiceImpl` (1,226) | façade over `ProjectManager`: open/create/demo/save/saveAs/build/export/close, prefs for paths and build settings, `isModified`, `isDemoProject`, `buildState`, recent projects, `addProjectUpdateListener(Class, Runnable)`, `update(entity)` / `update(type,id,attr,value)`, cascading deletes (`deleteProgramVoice/Track/SequencePattern`), `updateProgramType`, `updateProgramSequenceTotal`, `updateProgramSequencePatternTotal`, dialogs (`showConfirmationDialog`, `showWarningAlert`, `showErrorDialog`) |
| `FabricationService` (213) → `FabricationServiceImpl` (627) | wraps `FabricationManager`; properties above; segment-store readers; `doOverrideMacro/Memes`, intensity override |
| `UIStateService` (362) → `UIStateServiceImpl` (717) | routes/history, current library/program/sequence/instrument/audio/template, program editor mode/grid/zoom/snap, popup launchers (`launchPopupSelectorMenu`, `launchPopupActionMenu`, `launchQuickActionMenu`, `launchModalMenu`), timeline background drawing, log visibility/level, window title |
| `ThemeService` (35) → `ThemeServiceImpl` (87) | CSS + fonts for scenes/dialogs |
| `SupportService` (23) → `SupportServiceImpl` (51) | open guide/tutorial/Discord/website URLs |
| `VersionService` (41, `@Component`) | reads `version.properties` generated by Gradle `generateVersionFile` |
| `ReadyAfterBoot` (15), `ProjectDescriptor` (13) | lifecycle hook; recent-project record |

Spring Boot (`spring.main.web-application-type=none`) is used purely as a DI container; `WorkstationConfiguration.java` builds the engine graph (`FabricationManagerImpl` with `TelemetryImpl`, `BroadcastFactoryImpl`, `CraftFactoryImpl`, `AudioCacheImpl`, `SegmentEntityStoreImpl`, `JsonapiPayloadFactoryImpl`, `FabricatorFactoryImpl`, `MixerFactoryImpl`).

## 7. Legacy / cloud-era code to drop

| Package or class | LOC | Who references it in `main` | Disposition |
|---|---|---|---|
| `engine/hub_client` (`HubClientFactory`, `Impl`, `HubClientException`) | 205 | `ProjectManagerImpl` (demo download), `WorkstationConfiguration` | Replace with a plain HTTP fetch in the demo path, or drop with the demo feature once the Commons exists |
| `engine/http` (`HttpClientProvider[Impl]`, Apache HttpClient5 pool 200/20) | 38 | hub_client, ProjectManagerImpl | Drop with above |
| `engine/telemetry` (`Telemetry[Impl]`, timers logged) | 104 | `CraftWorkImpl`, `DubWorkImpl`, `ShipWorkImpl`, `FabricationManagerImpl` | Goes with the Java engine |
| `model/jsonapi` (14 files) | 2,587 | `HubClientFactoryImpl` ctor, `FabricatorImpl`/`FabricatorFactoryImpl` (field injected, **never called**), `WorkstationConfiguration` | Drop; no file on disk is JSON:API any more |
| `model/HubContentPayload` | 352 | `HubContent.from(payload)` only | Drop |
| `model/HubProjectList`, `model/HubUploadAuthorization` | 86 + 88 | `ProjectAudioUpload` only | Drop |
| `gui/project/ProjectAudioUpload` | 100 | nothing in `main` (test only) | Drop |
| `model/pojos/User`, `UserAuth`, `UserAuthToken`, `ProjectUser`, `TemplatePublication` + enums `UserAuthType`, `ProjectUserRole` | 137+159+118+116+119+~40 | `HubContent.getUsers/getProjectUsers` (JSON keys `users`, `projectUsers`), `HubTopology.java:212-257` | Drop; a V1 reader must still tolerate these keys (they exist in old hub exports) |
| `model/entity/ContentEntityStore[Impl]` | 195 | only a comment in `HubContent` | Drop |
| `model/pojos/Chain` + `ChainState/ChainType` | 219+ | Java fabricator/work | Goes with the Java engine |
| build deps `spring-boot-starter-webflux`, `httpclient5`, `jib` plugin, `blockhound`, `component-inspector-fx`, `jackson-dataformat-xml`, `snowball-stemmer`, `flandmark-platform` | — | `build.gradle:30-49, 186-190` | webflux/jib/flandmark have no code references in `src/main` |

Roughly **4,500 LOC** is cloud-era and removable without touching the editors; the Java engine (`engine/{craft,fabricator,work,mixer,ship,audio}` ≈ 11,000) is the separate retirement in the proposal.

## 8. Build and distribution

- `gradle.properties`: `version=1.9.6`; `javaFxVersion=20.0.2`, `springBootVersion=3.1.2`, `jacksonAnnotationsVersion=2.15.2`, `logbackVersion=1.4.11`, JUnit 5.9.2. `build.gradle`: Java 17 source/target; plugins `org.openjfx.javafxplugin 0.0.14`, `org.panteleyev.jpackageplugin 1.6.0`, `org.bytedeco.gradle-javacpp-platform 1.5.9`, Spring Boot; `bootJar` (fat jar, `launchScript()`, base name `xjmusic-workstation`); `generateVersionFile` writes `build/resources/main/version.properties`.
- Native audio toolchain: `org.bytedeco:ffmpeg-platform-gpl:6.0-1.5.9` (GPL build) + `javacv-platform:1.5.9` + `flandmark-platform` (`build.gradle:187-189`) — all platforms' binaries bundled into the one jar.
- `jpackage` block (`build.gradle:155-245`): `appName "XJ music workstation"`, vendor "XJ Music Inc", input `build/jpackage/`; **mac**: `ImageType.DMG`, `macSign=true`, `macSigningKeyUserName="XJ Music Inc"`, `macPackageIdentifier="io.xj.music.workstation"`, category `public.app-category.music`; **linux**: `DEB`, category Audio, shortcut; **windows**: `EXE`, start menu, dir chooser, shortcut; output renamed `xjmusic-workstation-v<version>-installer.<ext>`.
- `.github/workflows/workstation_distro.yml` on tag push: `nothing_left_todo.sh workstation` (fails if the word "todo" appears anywhere in `workstation/src` or `test`) → version in `gradle.properties` must equal tag `v<version>` → `./gradlew build test` on `ubuntu-latest, macos-13, macos-14, windows-latest` → per-OS `./gradlew :jpackage`; on macOS imports a P12 from `MACOS_BUILD_CERTIFICATE_BASE64` into a temp keychain and notarizes with `xcrun notarytool submit --wait` (`MACOS_NOTARIZATION_APPLE_ID/TEAM_ID/PWD`); uploads `…-installer-macOS-Intel.dmg.zip` / `…-macOS-Silicon.dmg.zip` / `…-Windows.exe.zip` / `…-Linux.deb.tar.gz` to the GitHub release (`svenstaro/upload-release-action@v2`) and to S3 (`AWS_S3_BUCKET`). `engine_distro.yml` also reads `workstation/gradle.properties` and requires the two versions to agree with the tag. `workstation_ci.yml`/`workstation_pr.yml`: same matrix, `build test`, plus the TODO check.

## 9. Absence checks (grep over `workstation/src`)

- `grep -rni midi` → no matches in Java, FXML, properties or CSS. `grep -rnwi osc` → none. `javax.sound.midi` → none.
- Network: the only client code is Apache HttpClient5 in `engine/hub_client` + `engine/http`, used solely for demo download from `https://audio.xj.io/`. No `java.net.Socket`, `WebSocket`, `RestTemplate`, `WebClient`, OSC or mDNS anywhere in `main`. `spring-boot-starter-webflux` is on the classpath but unused.
- Audio decode for the editor uses `javax.sound.sampled` (`engine/audio/AudioLoaderImpl.java:26-78`) — WAV/AIFF only; MP3/FLAC/AAC chosen in the import dialog will import (file copy) but `AudioLoader.load` throws `UnsupportedAudioFileException` → the import fails. Only Build/Export convert via ffmpeg.

## 10. Implications for V2 specs

**xj-content: V1 project read/write round-trip**
1. The unit of truth is a *set of files*, each a `HubContent` JSON subset; a reader must glob `libraries/**/*.json` and `templates/**/*.json` recursively plus the single `*.xj`, and merge by UUID (`HubContent.combine`). Writer must emit the five file kinds in §2.3 with the exact subset membership (`subsetForProgramId` includes bindings, memes, chords, voicings, patterns, events, sequences, tracks, voices; the library file holds only the library).
2. Byte-identical output requires: 2-space Jackson pretty printer (`"key" : value`), the getter order of §3, `NON_NULL` per field, `NON_EMPTY` per collection, `"demo": false` always present on every file, arrays sorted by each pojo's `compareTo` (name→id, with the per-type exceptions listed), floats printed Java-style (`120.0`, `0.38`), `total` as integer, chord `position` as double, event `position/duration/velocity` as float. Rust `serde_json` pretty printing differs (`"key": value`), so "same files" in the proposal should mean *semantically equal after canonicalization*, and the CI round-trip test (R14) should compare parsed trees, not bytes, unless a custom formatter is written.
3. `FAIL_ON_UNKNOWN_PROPERTIES=false`: the Java reader silently drops unknown keys and **rewrites the file without them on next save**. Any V2 additive fields (stretch allowances, content hashes, pack metadata) written into these JSON files will be erased by a Java save during the transition. Put V2 extensions in sibling files (`*.v2.json`, manifest) or accept loss until Java is archived.
4. Legacy keys `users`, `projectUsers`, `errors` may appear in old files and must be tolerated (and may be dropped).
5. File/folder names derive from entity names via `toAlphanumericHyphenated`; renaming an entity moves its folder on save and deletes the old one; two names that differ only by non-alphanumerics collide. A Rust writer must replicate the slug rule and the delete-unknown-files behavior, or at least never leave stale `.json` in `libraries/`/`templates/` (the Java opener would merge them back).
6. `waveformKey` is both the file name and content identity: `<Instrument>-<Audio>-<Tones>.<ext>` with explicit accidentals; Save enforces it; Build renames to `<audioId>.<ext>`; Export to `<shipKey>-<audioId>.<ext>`. Content hashing in the Commons should treat `waveformKey` as derived, not canonical.
7. HOCON config strings are re-canonicalized with sorted keys and the host line separator — expect CRLF inside `"config"` strings from Windows users; a Rust writer should parse HOCON (or at least preserve the string verbatim) and compare configs semantically.
8. `lengthSeconds` is computed lazily on save from the audio file; `transientSeconds` defaults to 0; `updatedAt` is unmaintained. Do not rely on `updatedAt` for sync; use file mtimes or hashes.
9. The legacy single-file layout (`<Project>.xj` with everything + `instrument/<uuid>/<file>`) still exists in the wild (demo projects created before #335); the migration rules in `saveProjectContent` are the spec for reading it.

**Build for engine (Ship mode)**
10. Build = all entities of all templates, orphans removed, each audio rendered once to `build/<audioId>.<container>` at (frameRate, bits, channels), JSON `build/<slug>.json`; skip-if-matching is decided by reading the destination's header. Default 48 kHz / 16-bit / stereo / WAV. Export differs (template subset, Published-only filter, `<shipKey>-<audioId>` names, optional conversion). V2 "Build for engine" must keep the Build layout for Unreal, and should decide whether Export's Published-only filter is a feature to keep.
11. `ProjectBuildState.Dirty` is set on *any* edit, including viewing a chord lane (auto-created voicings). V2 should compute dirtiness from content hashes.

**Make-mode parity (Phase 6)**
12. The checklist in §4 is the complete operation surface; notable *absences* to decide on rather than port: no undo/redo, no multi-select, no copy/paste of events or patterns, no keyboard note entry, no audio playback or audition in the audio editor, no loop markers (only one transient), no drag of bindings between offsets, no duplicate-binding check, no `isDeleted` filtering, no import of non-WAV/AIFF without failing.
13. Destructive cascades with confirmations are business rules worth preserving verbatim: changing program type (§4.5), shrinking pattern total deletes out-of-range events, sequence total cannot shrink below any pattern total, deleting a sequence is refused while bindings or patterns reference it, deleting a binding is refused while it has memes.
14. Defaults when creating entities inherit from siblings (§2.6) — users rely on this (e.g., new audio tempo follows the library). Port the inheritance chain, not just the constants.
15. Event positions are stored 0-based floats but shown 1-based in the properties dialog; velocity stored 0–1 shown as %; chord positions are doubles validated unique within a sequence.

**Fabrication tab → performance surface**
16. The live control vocabulary today is exactly: control mode (AUTO/MACRO/TAXONOMY, chosen before start), macro override (one program, Engage), meme override (one meme per taxonomy category, Engage), intensity override (switch + 0–1), start/stop. Settings craft-ahead 60 s / dub-ahead 2 s / mixer length 1 s. Content played is the template's `forTemplate` subset (Published only). V2 cues map 1:1 onto these plus the new ones; preselection-at-start behavior (#201) should be preserved as "the first segment obeys the engaged selection".
17. The timeline already exposes segment type/key/tempo/intensity/memes/chords/choices/delta windows/mute/metas/messages from the segment store; the "why" panel is the only genuinely new data requirement.

**Java maintenance mode**
18. The release pipeline is brittle in two spec-relevant ways: the `nothing_left_todo.sh` gate fails any commit containing the word "todo" in source or tests, and `engine_distro.yml` requires workstation and engine versions to match the tag — decoupling engine 2.0 tags from Java 1.9.x maintenance releases requires changing that workflow first.
19. Removing cloud code is mechanical (≈4,500 LOC, §7) but `HubClientFactory` is the demo loader; keep a minimal HTTP fetch (or the Commons fetcher) for `createProjectFromDemoTemplate`, and keep `HubContent` tolerant of `users/projectUsers` keys.
20. No MIDI, OSC, sockets, or clock code exists in the Java workstation; nothing there informs `xj-control`, `xj-transport`, or `xj-session` beyond the override API shape.
