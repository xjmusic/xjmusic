# 04 · Engine 2.0

Changes to the C++ engine in [`engine/`](../../engine/). The rule from [01-vision.md](01-vision.md) applies throughout: **the craft algorithms are not redesigned.** Everything here is an input the engine gains, an output it exposes, or a contract it honors. The existing C++ API and the gtest matrix remain and must keep passing.

## Summary of changes

| Change | Addresses | Kind |
|---|---|---|
| Stable C ABI | G1 (one engine for every host) | New surface |
| Tempo as input | G3 | New input, additive |
| Follow mode: external harmonic foundation | G4 | New craft stage substituting for macro/main |
| Conduct mode: publish the resolved foundation | G4 | New output |
| Seeded randomness | G6 | Internal change, behavior-preserving by default |
| Cues with explicit landing boundaries | G5 | Generalization of override and rewrite |
| Commit horizon | G5 | New timing parameter |
| Preview ("what if") | G12 | New read-only operation |
| Choice diagnostics | G12 | New optional output |
| Additive content fields | G7 | Optional fields on existing entities |

## 1. Stable C ABI

### Why a C ABI and not `cxx` or JNI-style bindings

The Rust workstation is the first consumer, but not the only one. Unreal already consumes the C++ API; Unity (C#), Godot (GDExtension), Python and a WebAssembly build all speak C and nothing else easily. A C ABI also decouples versions: the Rust crate can pin an ABI version and keep building across engine patch releases. The `cxx` crate is excellent for Rust-and-C++ in one repository, but it would make Rust the privileged host and leave every other host on the raw C++ API.

### Shape

A single header, `engine/include/xjmusic/xj_c.h`, compiled into the same static library. Conventions:

- `extern "C"`, opaque handles (`xj_engine*`), explicit `xj_abi_version()` returning a semver triple.
- Every function returns an `xj_status` enum; results come out through pointers.
- Strings are UTF-8 with explicit lengths. The engine owns any buffer it returns until the next call on the same handle or an explicit `xj_free`.
- One engine handle is used from one thread at a time. The Rust wrapper encodes this with a `!Sync` type.
- Identifiers are 16-byte UUIDs as byte arrays, not strings, on the hot path.

### Surface

| Group | Functions | Notes |
|---|---|---|
| Lifecycle | `xj_engine_create`, `xj_engine_destroy`, `xj_engine_load_project`, `xj_engine_start`, `xj_engine_finish` | Load from a path or from in-memory JSON, so hosts without a filesystem (browser, console) can work |
| Cycle | `xj_engine_run_cycle(at_chain_micros, out_events, out_count)` | Returns a flat array of `xj_schedule_event` structs |
| Control (existing) | `set_control_mode`, `override_macro`, `override_memes`, `set_intensity_override` | Mirrors `Engine.h` |
| Cues (new) | `xj_engine_cue(const xj_cue*)`, `xj_engine_cancel_cue`, `xj_engine_earliest_landing(boundary, out_chain_micros)` | Section 5 |
| Tempo (new) | `xj_engine_set_tempo_policy`, `xj_engine_set_external_tempo` | Section 2 |
| Foundation (new) | `xj_engine_set_foundation_source`, `xj_engine_push_foundation_json`, `xj_engine_get_foundation_json` | Sections 3 and 4 |
| Determinism (new) | `xj_engine_set_seed`, `xj_engine_get_seed` | Section 5 |
| Snapshot | `xj_engine_get_segments_json(from_micros, to_micros)`, `xj_engine_get_messages_json(since)` | JSON via the already-vendored nlohmann library; the UI reads these at most 10 times a second |
| Preview | `xj_engine_preview_cue_json(const xj_cue*)` | Section 7 |
| Introspection | `xj_engine_get_template_config_json`, `xj_engine_get_meme_taxonomy_json`, `xj_engine_list_macro_programs_json` | Replaces the typed getters for hosts that do not want the C++ types |

The flat event struct is the one thing on the hot path and deserves care:

```c
typedef struct xj_schedule_event {
  uint8_t  type;                 /* 0 create, 1 update, 2 delete */
  uint8_t  instrument_type;      /* enum Instrument::Type */
  uint8_t  instrument_mode;      /* enum Instrument::Mode */
  uint8_t  _pad;
  uint8_t  active_audio_id[16];
  uint8_t  instrument_audio_id[16];
  uint64_t start_at_chain_micros;
  uint64_t stop_at_chain_micros;
  float    from_volume;
  float    to_volume;
  int32_t  release_millis;
  /* new in 2.0, zero when not applicable */
  float    pitch_shift_semitones;  /* requested by craft when content allows */
  float    stretch_ratio;          /* 1.0 = none */
  uint32_t waveform_key_offset;    /* into the string table returned alongside */
} xj_schedule_event;
```

Everything else (segments, choices, picks, chords, memes, messages) is read as JSON on demand. If profiling later shows the UI needs a binary snapshot, that is an additive function.

### Build integration

A new CMake target `xjmusic_c` wraps the library with the C ABI and an `install()` rule so that a Rust `build.rs` can invoke CMake and link the result. Nothing changes for existing consumers. The engine's CI gains a job that compiles a C test program against the header with a C compiler, not a C++ one, to prove the header is actually C.

## 2. Tempo as input

Today tempo is a property of content: `MacroMainCraft` sets the segment tempo from the chosen main program, and every beats-to-microseconds conversion in the `Fabricator` uses it. That stays the default.

V2 adds a tempo policy:

- `Content` (default): behavior unchanged.
- `External`: the host supplies a tempo (from Ableton Link via `xj-transport`). `MacroMainCraft` records the external tempo on each new segment instead of the program's. Main programs are still chosen by their memes and bindings; their nominal tempo becomes advisory.

Already-crafted segments are never re-timed. A tempo change from Link therefore takes effect at the next segment boundary, and the host shows the performer when that is. A tempo change inside a segment is a non-goal for 2.0 because the DSP would then need to re-time scheduled audio, which conflicts with the commit horizon in section 6.

Loop-mode audio recorded at a different tempo than the segment is the content problem behind G7. Section 9 adds the optional content fields that let craft request a stretch ratio from the DSP; absent those fields the engine behaves exactly as today.

## 3. Follow mode: an external harmonic foundation

This is the engine change that makes the venue possible, and it is smaller than it sounds because of the seam described in [02-where-v1-stands.md](02-where-v1-stands.md): every craft stage after macro/main reads the foundation through the `Fabricator`.

### What the foundation is

Precisely the entities `MacroMainCraft` writes to the segment store for a segment:

- the segment itself: begin time, total beats, type (Initial, Continue, NextMain, NextMacro), key, tempo, intensity, delta
- its `SegmentChord`s with positions
- its `SegmentChordVoicing`s, one per chord per instrument type, with notes
- its `SegmentMeme`s
- and, for continuity, the identity of the macro and main choices (as opaque names, since a follower may not have the same programs)

### How it works

A new foundation source setting:

- `Internal` (default): `MacroMainCraft` runs as today.
- `External`: a new `ExternalFoundationCraft` runs in its place. It consumes foundation records pushed by the host through `xj_engine_push_foundation_json`, writes the same entities to the segment store, and returns. `BeatCraft`, `DetailCraft`, `TransitionCraft` and `BackgroundCraft` then run **unchanged**, because they read key, chords, voicings, memes, tempo and intensity from the `Fabricator`, which reads them from the store.

The follower station still has its own libraries of beat, detail, transition and background programs and instruments, and chooses among them by meme isometry against the published memes, exactly as it does today against its own main program's memes. This is why two performers with different material stay in the same tempo and harmony: harmony is given, material is chosen to fit.

"Unchanged" deserves a precise caveat. A search of the craft sources finds three places that read the main program directly rather than through segment entities, and the foundation record carries what each needs:

| Where | What it reads | Why | Foundation field |
|---|---|---|---|
| `TransitionCraft::isMediumTransitionSegment` | Current and previous main sequence identity | To detect a section change within a Continue segment | `foundationChoice.main` and `mainOffset`, compared as opaque values |
| `Craft.cpp` and `TransitionCraft.cpp` | `getCurrentMainProgramConfig().barBeats` | Sub-section arithmetic | `segment.barBeats` |
| `CraftWork::doFabricationRewrite` | Main program tempo and `cutoffMinimumBars` | Where to cut the current segment | `segment.tempo`; the cutoff bars come from the follower's template |

In follow mode the `Fabricator` answers these three calls from the foundation record. That is the complete list, which is what makes the change small rather than hopeful.

### Timing

The conductor publishes a segment's foundation as soon as it has crafted it, which is up to `craftAheadMicros` before it plays. Followers therefore have the same lead time minus network latency (milliseconds on a LAN) to craft their layers. The follower's craft horizon is tied to the conductor's, not to its own setting; [07-session-protocol.md](07-session-protocol.md) covers the handshake.

### When the timeline runs dry

If no foundation record exists for the next segment when the follower must craft it (conductor crashed, network partition), the follower **holds**: it fabricates a Continue-type segment that repeats the last known chords, key, tempo and memes, marks it with a `SegmentMessage` of type warning, and keeps doing so for a configurable number of bars. After that it switches its foundation source to `Internal` at a segment boundary and continues alone. Rejoining later is the same as joining: a NextMacro-type segment aligned to the conductor's next boundary.

### Overrides in follow mode

A macro or meme override on a follower would be nonsense: the follower does not own the foundation. In follow mode the engine rejects foundation-level cues with a status code, and the station forwards them to the conductor as cue *requests* ([07-session-protocol.md](07-session-protocol.md)). Intensity and layer cues remain local, because intensity and layering are per-station decisions.

## 4. Conduct mode: publish the resolved foundation

Symmetrically, in `Internal` mode with conduct enabled, after each segment is crafted the engine makes its foundation available through `xj_engine_get_foundation_json(segment_id)`. The station publishes it. This is read-only; the conductor's own craft is unchanged. The record format is the same JSON the follower consumes, so a station can be tested as its own follower in one process.

## 5. Seeded randomness

Replace the four `std::random_device`-seeded generators with one `std::mt19937_64` owned by the chain and threaded through the `Fabricator` to `MarbleBag`, `StickyBun`, `ValueUtils` and the voicing selection in `Fabricator.cpp`. The seed is set by `xj_engine_set_seed` before `start`; if unset, the engine draws one from `random_device` and reports it, so default behavior is as random as today but now reproducible after the fact.

Each segment derives a sub-seed from the chain seed and the segment id, so rewriting the future from a cue produces the same future it would have produced had the cue been there from the start. That property makes cue tests deterministic.

Determinism is promised for the same binary on the same platform. Cross-platform floating-point determinism is explicitly not promised, and the session design in [07-session-protocol.md](07-session-protocol.md) does not depend on it.

## 6. Cues with explicit landing boundaries, and the commit horizon

### Today

An override causes a whole-future rewrite with the current segment cut short at the next multiple of bar beats times `cutoffMinimumBars` after the point already dubbed. There is one implicit boundary, derived from content, and the performer cannot choose it.

### V2

A **cue** is a request plus a **boundary**:

```c
typedef enum { XJ_BOUNDARY_NOW, XJ_BOUNDARY_BEAT, XJ_BOUNDARY_BAR,
               XJ_BOUNDARY_SEGMENT, XJ_BOUNDARY_MAIN, XJ_BOUNDARY_MACRO } xj_boundary;

typedef enum { XJ_CUE_MACRO, XJ_CUE_MEMES, XJ_CUE_INTENSITY, XJ_CUE_CHORD,
               XJ_CUE_ADVANCE, XJ_CUE_HOLD, XJ_CUE_LAYER_MUTE } xj_cue_type;
```

| Cue | Meaning | Engine behavior at the landing boundary |
|---|---|---|
| Macro | Go to this macro program | As today's override, but cut at the chosen boundary |
| Memes | Force these taxonomy memes | As today's override, but cut at the chosen boundary |
| Intensity | Move to this intensity, optionally over a ramp | Applied in dub as today; ramp is new |
| Chord | Make the next chord this one (name, optional key) | Cut the segment at the boundary; craft the next segment with its first chord forced; main program selection prefers a sequence whose first chord matches, else transposes |
| Advance | Leave the current section now | Cut at the boundary; next segment is NextMain |
| Hold | Stay in the current section | Next segment continues at the same sequence binding offset instead of advancing |
| Layer mute | Silence an instrument type from now on, or un-silence it | Sets that type's choice mute probability to 1 (or back) for future segments; the station also mutes the DSP bus immediately so the performer hears it now |

A cue is accepted only if its boundary falls beyond the **commit horizon**: audio inside the horizon has already been handed to the DSP and is immutable. `xj_engine_earliest_landing(boundary)` tells the host the first time such a cue could land, and the UI shows that to the performer before they touch the control. The commit horizon replaces the implicit coupling between dub-ahead and responsiveness: a station tuned for performance might run craft-ahead at 8 s, dub-ahead at 2 s and a commit horizon of one beat, while a game integration keeps today's defaults.

Internally this generalizes `doFabricationRewrite` and `doCutoffLastSegment`. The existing override functions become thin wrappers that create cues with the boundary the old behavior implied, so no existing consumer changes.

## 7. Preview: what will this do

Craft is a pure function of content, the segment store, the configuration and the random generator. That makes a scratch run cheap to reason about: copy the store (or the relevant tail of it), apply the cue, craft the next segment into the copy, serialize the result, discard the copy. `xj_engine_preview_cue_json` does exactly that and returns what the next segment *would* contain: macro and main names, key, chords, memes, intensity, and which instruments would play.

This is how the UI shows, on hover over a pad, "this will take you to *Fire*, key of A minor, 124 bpm, in 2 bars." It runs on request, not every cycle, and it uses the segment's derived sub-seed so the preview matches what will actually happen if the cue is sent.

## 8. Choice diagnostics

Behind a flag, `MarbleBag` records the candidates and weights at each draw, and each craft stage records why candidates were excluded (meme stack violation, out of range, muted, no audio for tones). These land as `SegmentMeta` records keyed by choice. The UI uses them for a "why this?" panel. The flag exists because recording is not free, and game integrations should pay nothing for it.

## 9. Additive content fields

All optional, all defaulting to today's behavior, all ignored by 1.9.x if present.

| Entity | Field | Purpose |
|---|---|---|
| `InstrumentConfig` | `allowPitchShift` (bool), `pitchShiftRange` (semitones) | Lets craft pick a nearby sample and request a shift when no sample has the exact tones, instead of failing or choosing badly. Fills gaps in sparse instruments. |
| `InstrumentConfig` | `allowStretch` (bool) | Lets craft use loop audio at a tempo other than its recorded tempo by requesting a ratio from the DSP. |
| `InstrumentAudio` | (none new) | `tempo`, `loopBeats` and `tones` already carry what stretch and shift need |
| `TemplateConfig` | `commitHorizonBeats` (float) | Default behavior if absent matches 1.9.x |
| `TemplateConfig` | `followHoldMaxBars` (int) | Section 3, "when the timeline runs dry" |

When `allowPitchShift` is set, `NotePicker` ranks exact-tone candidates first and shifted candidates by distance, and the resulting pick carries `pitch_shift_semitones` in the schedule event. The engine does not touch audio; it only asks.

## 10. Diagnostics the engine already has, made reachable

`SegmentMessage`s (info, warning, error) and the `WorkState` exist today. They become reachable through the C ABI and are surfaced in the UI. Craft timing per segment is added as a message so the station can tune its horizons from measured data rather than guesses.

## 11. Testing

- The existing gtest matrix is unchanged and remains the acceptance test for the craft algorithms.
- **C ABI tests** in C, compiled with a C compiler in CI.
- **Follow-mode tests**: two engines in one process, one conducting and one following, over the test project. Assert that for every segment span the follower's key, tempo, chords, voicings and memes equal the conductor's, and that the follower's beat and detail choices are valid against them.
- **Determinism tests**: same seed, same project, same cues at the same times produce byte-identical segment stores.
- **Cue tests**: each cue type lands exactly at the requested boundary and never inside the commit horizon.
- **Hold and rejoin tests**: starve a follower of foundation records and assert the hold, the fallback, and the clean rejoin.
- **Preview tests**: preview of a cue equals the result of sending it.

## 12. Versioning

Engine 2.0.0. The C ABI is versioned separately starting at 1.0.0 and follows semver strictly: additive functions bump minor, any change to an existing struct or signature bumps major. Engine 2.0 reads every 1.9.x project unchanged.

## Not in 2.0

- Tempo changes within a segment.
- Meter changes (everything is `barBeats` per program, as today).
- Multiple simultaneous chains in one engine handle (open two handles instead; the Rust wrapper makes that easy).
- Any change to how macro, main, beat, detail, transition or background craft choose material.
