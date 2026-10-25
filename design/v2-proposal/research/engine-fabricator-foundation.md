# Grounded map: Fabricator + MacroMainCraft (the "foundation" seam)

Engine version 1.9.6 (`engine/CMakeLists.txt:6`). All paths below are relative to `/Users/nck/xj/xjmusic/engine/` unless prefixed. Line numbers were taken with `grep -n`/`awk` against the working tree at commit `a94000b9a` on `main`.

Files read in full: `include/xjmusic/fabricator/Fabricator.h` (1,022 lines), `src/fabricator/Fabricator.cpp` (1,348), `include/xjmusic/craft/MacroMainCraft.h` (132), `src/craft/MacroMainCraft.cpp` (420), `src/craft/Craft.cpp` (1,040), `src/craft/TransitionCraft.cpp` (124), `src/craft/BeatCraft.cpp` (94), `src/craft/DetailCraft.cpp` (137), `src/craft/BackgroundCraft.cpp` (53), the segment entity headers, and the relevant parts of `src/work/CraftWork.cpp`, `src/work/DubWork.cpp`, `src/fabricator/MarbleBag.cpp`, `src/fabricator/SegmentRetrospective.cpp`, `src/content/ContentEntityStore.cpp`, `src/content/ProgramConfig.cpp`, `src/content/TemplateConfig.cpp`.

---

## 1. Where the foundation is made: the per-segment pipeline

`CraftWork::doFabricationWork` (`src/work/CraftWork.cpp:371-394`) is the single place a segment is crafted:

| Line | Statement | Note |
|---|---|---|
| 378 | `SegmentRetrospective(store, overrideMemes.empty() ? inputSegment->id : 0)` | **With a meme override the retrospective is built for segment 0, i.e. empty** (ctor returns early when `segmentId <= 0`, `src/fabricator/SegmentRetrospective.cpp:20-25`). Macro override keeps a real retrospective. |
| 381 | `Fabricator(content, store, &retrospective, inputSegment->id, overrideSegmentType)` | `overrideSegmentType` is `std::nullopt` on the normal path and `Segment::Type::NextMacro` on the rewrite path (`CraftWork.cpp:322`). |
| 384-385 | `updateSegmentState(... Planned -> Crafting)` | State machine enforced by `SegmentEntityStore::protectSegmentStateTransition` (`src/segment/SegmentEntityStore.cpp:325-347`). |
| 386 | `MacroMainCraft(&fabricator, overrideMacroProgram, overrideMemes).doWork()` | **The foundation.** |
| 387-390 | `BeatCraft`, `DetailCraft`, `TransitionCraft`, `BackgroundCraft` `.doWork()` | The layers. |
| 393 | `updateSegmentState(... Crafting -> Crafted)` | |

The segment that arrives at `MacroMainCraft` is a shell. `CraftWork::buildSegmentInitial` (`403-413`) sets `id=0, beginAtChainMicros=0, delta=0, type=Pending, state=Planned`. `CraftWork::buildSegmentFollowing` (`415-429`) sets `id=last.id+1, beginAtChainMicros=last.begin+last.durationMicros, delta=last.delta, type=Pending, state=Planned`. **Neither sets `total`, `tempo`, `key`, `intensity` or `durationMicros`**; those are owned by `MacroMainCraft`. `Segment::Type::Pending` (`include/xjmusic/segment/Segment.h:11`) is the pre-craft value and is also the one case `Craft::precomputeDeltas` treats as a no-op (`src/craft/Craft.cpp:75-77`).

The `Fabricator` constructor (`src/fabricator/Fabricator.cpp:19-56`) reads the chain (`32-36`), template bindings and directly-bound program/instrument id sets (`37-39`), digests the retrospective's picks into `preferredAudios` (`42`, `1184-1199`), applies the type override (`49-52`) and ensures a ship key (`55`, `1141-1156`; format `<chain.shipKey or "chain-"+chain.id>-<beginAtChainMicros>`, `1133-1139`).

---

## 2. Exactly what MacroMainCraft writes

`MacroMainCraft::doWork` (`src/craft/MacroMainCraft.cpp:16-90`), in order:

### 2.1 Override memes (only when `overrideMemes` non-empty)
`MacroMainCraft.cpp:25-32`: one `SegmentMeme{id, segmentId, name}` per override meme, written through `fabricator->put(segmentMeme, true)` (force, bypasses meme-stack validation). Then main is chosen **before** macro (`34-35`); otherwise macro before main (`38-39`).

### 2.2 The macro choice (`doMacroChoiceWork`, `92-119`)
Writes one `SegmentChoice` (`107-116`) with `programType=Macro`, `programId`, `programSequenceId`, `programSequenceBindingId`, `deltaIn=deltaOut=DELTA_UNLIMITED (-1)`. Put with `force=true` (`116`). Because `Fabricator::put(SegmentChoice, force)` (`Fabricator.cpp:952-961`) calls `isValidChoiceAndMemesHaveBeenAdded` (`1201-1235`), this put **also writes `SegmentMeme`s** for every meme of the macro program and of the chosen sequence binding (`1205-1211`, `1225-1232`).

### 2.3 The main choice (`doMainChoiceWork`, `121-149`)
Writes one `SegmentChoice` (`138-146`) with `programType=Main`, `programId`, `programSequenceBindingId`, `deltaIn=deltaOut=-1`. **No `programSequenceId` is set on the main choice** (contrast macro at `110`); `Fabricator::getProgramSequence` (`306-316`) resolves it through the binding. Put with `force=true` (`146`), again adding program and binding memes.

### 2.4 Chords and voicings (`47-69`)

```cpp
// MacroMainCraft.cpp:47-69
for (const auto sequenceChord: fabricator->getProgramSequenceChords(mainSequence)) {
  if (sequenceChord->position < static_cast<float>(mainSequence->total)) {      // L49: drop chords past end
    const std::string name = Chord(sequenceChord->name).getName();                // L51: normalize name
    auto chord = SegmentChord(); chord.id=...; chord.segmentId=segment->id;
    chord.position = sequenceChord->position; chord.name = name;
    fabricator->put(chord);                                                       // L58
    for (const auto voicing: fabricator->getSourceMaterial()->getVoicingsOfChord(sequenceChord)) {
      auto v = SegmentChordVoicing(); v.id=...; v.segmentId=segment->id;
      v.segmentChordId = chord.id;
      v.type  = fabricator->getProgramVoiceType(voicing);                         // L64: ProgramVoice.type
      v.notes = voicing->notes;                                                   // L65: CSV copied verbatim
      fabricator->put(v);                                                         // L66
    }
  }
}
```

`Fabricator::getProgramSequenceChords` (`483-517`) "de-ghosts": for chords sharing one `position`, it keeps the one whose voicings have the most notes (`492-502`), sorts ascending by position (`509-512`) and caches per sequence id. There can be **several `SegmentChordVoicing`s per chord per `Instrument::Type`** (one per `ProgramVoice` of that type); nothing collapses them.

### 2.5 The segment update (`72-89`)

| Segment field | Source | Line |
|---|---|---|
| `type` | `fabricator->getType()` (cached `computeType()` or ctor override, see §3) | 73 |
| `tempo` | `mainProgram->tempo` (`Program.h:34`, float). `ProgramSequence` has **no tempo field** (`ProgramSequence.h:18-22`: programId, name, key, intensity, total) | 74 |
| `key` | `computeSegmentKey(mainSequence)` (`151-161`): `mainSequence->key`, else `mainProgram->key`; normalized via `Chord::of(k).getName()` (e.g. `"G -"` in the Continue test) | 75 |
| `total` | `mainSequence->total` (beats, int) | 76 |
| `durationMicros` | `segmentLengthMicros` (`418-420`) = `getSegmentMicrosAtPosition(mainProgram->tempo, total)` = `(MICROS_PER_MINUTE / tempo) * total` | 77 |
| `delta` | `Continue ? segment->delta + segment->total : 0` | 80-83 |
| `intensity` | `computeSegmentIntensity(segment->delta, macroSequence, mainSequence)` using the **pre-update** `segment->delta` | 86 |
| write | `fabricator->updateSegment(updatedSegment)` (`Fabricator.cpp:1013-1024` -> `SegmentEntityStore::updateSegment` `274-302`) | 89 |

`beginAtChainMicros` is **not** touched by craft; it is fixed by `buildSegmentFollowing` (`CraftWork.cpp:424`). `storageKey` is set by the Fabricator ctor. `waveformPreroll/Postroll` are never written in `src/`.

### 2.6 Summary table: foundation entities and their writers

| Entity | Fields (header) | Written by |
|---|---|---|
| `Segment` (`Segment.h:24-39`) | `id:int, chainId, type, state, beginAtChainMicros:long, durationMicros:optional<long>, key:string, total:int, intensity:float, tempo:float, storageKey, waveformPreroll, waveformPostroll, delta:int, createdAt, updatedAt` | shell: `CraftWork.cpp:403-429`; type/tempo/key/total/duration/delta/intensity: `MacroMainCraft.cpp:72-89`; storageKey: `Fabricator.cpp:1141-1156`; state: `CraftWork::updateSegmentState` |
| `SegmentChord` (`SegmentChord.h:11-12`, + `SegmentEntity.h:13-14` `id:UUID(string), segmentId:int`) | `position:float, name:string` | `MacroMainCraft.cpp:53-58` |
| `SegmentChordVoicing` (`SegmentChordVoicing.h:13-15`) | `segmentChordId:UUID, type:Instrument::Type, notes:string(CSV)` | `MacroMainCraft.cpp:60-66` |
| `SegmentMeme` (`SegmentMeme.h:11`) | `name:string` | override: `MacroMainCraft.cpp:27-31`; from choices: `Fabricator.cpp:1225-1232` (every `put(SegmentChoice)` of every stage, so memes keep growing after MacroMainCraft) |
| `SegmentChoice` (`SegmentChoice.h:13-26`) | `DELTA_UNLIMITED=-1; position, name, programId, programSequenceId, programSequenceBindingId, programVoiceId, instrumentId, deltaIn, deltaOut, mute, instrumentType, instrumentMode, programType` | macro: `MacroMainCraft.cpp:107-116`; main: `138-146` |

`Instrument::Type` (`include/xjmusic/content/Instrument.h:16-27`): `Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition`.

Type names serialized as `"Pending","Initial","Continue","NextMain","NextMacro"` (`src/segment/Segment.cpp:9-15`).

---

## 3. Segment type: how Initial / Continue / NextMain / NextMacro is decided

`Fabricator::getType()` (`Fabricator.cpp:847-852`) returns the cached `type`, which is either the ctor override (`49-52`) or `computeType()` (`1158-1176`):

```cpp
// Fabricator.cpp:1158-1176
if (isInitialSegment()) return Initial;                       // 1160-1161; isInitialSegment == (segment id == 0), 947-950
previousMainChoice = getPreviousMainChoice();                  // 1164 (retrospective, store)
if (previousMainChoice && hasOneMoreSequenceBindingOffset(previousMainChoice)
    && getTemplateConfig().mainProgramLengthMaxDelta > getPreviousSegmentDelta())
  return Continue;                                             // 1166-1167
previousMacroChoice = getMacroChoiceOfPreviousSegment();       // 1170
if (previousMacroChoice && hasTwoMoreSequenceBindingOffsets(previousMacroChoice))
  return NextMain;                                             // 1172-1173
return NextMacro;                                              // 1175
```

- `hasMoreSequenceBindingOffsets(choice, N)` (`881-900`) reads **content**: the previous choice's `ProgramSequenceBinding` and `ContentEntityStore::getAvailableOffsets` (`src/content/ContentEntityStore.cpp:200-213`, sorted unique offsets of the program).
- `getPreviousSegmentDelta()` (`1178-1182`) is the previous segment's `delta`; `mainProgramLengthMaxDelta` defaults to **280** (`src/content/TemplateConfig.cpp:74`).
- `getPreviousMainChoice`/`getMacroChoiceOfPreviousSegment` (`318-330`) go through `SegmentRetrospective::getPreviousChoiceOfType(Program::Type)` (`SegmentRetrospective.cpp:69-72`), which is `nullopt` when the retrospective is empty. Consequence: with a meme override (empty retrospective, §1) the computed type would be `NextMacro` regardless; the rewrite path forces `NextMacro` anyway.
- `isContinuationOfMacroProgram()` (`912-915`) is `Continue || NextMain`.

**Important for follow mode:** later stages read the type through `fabricator->getType()` (`Craft.cpp:74`, `TransitionCraft.cpp:33`, `TransitionCraft.cpp:44`), i.e. the cached/computed value, **not** `getSegment()->type`. `getChoiceIfContinued*` (`214-286`) instead reads `getSegment()->type`. An `ExternalFoundationCraft` must therefore pass the record's type as `overrideSegmentType` to the ctor (or `computeType` will consult content for programs the follower may not have), and also write it to the segment.

Binding offsets derived from the type (`MacroMainCraft.cpp`):
- `computeMacroSequenceBindingOffset` (`194-212`): Initial/NextMacro -> `0`, **or `getSecondMacroSequenceBindingOffset(override)` when a macro override is present** (`196-198`, `Fabricator.cpp:1043-1056`: an overridden macro starts at its second offset); Continue -> previous macro's offset (`205`); NextMain -> next offset of previous macro (`208`).
- `computeMainProgramSequenceBindingOffset` (`214-233`): Initial/NextMain/NextMacro -> `0`; Continue -> `getNextSequenceBindingOffset(previousMainChoice)` (`Fabricator.cpp:404-422`, wraps to 0 when exhausted).
- Bindings at an offset are fetched with `includeNearest=true` (`MacroMainCraft.cpp:97, 126`; `ContentEntityStore.cpp:283-300` snaps to the nearest existing offset), then one is chosen uniformly by `MarbleBag::quickPick` (`Fabricator.cpp:632-643`).

Program selection: `chooseMacroProgram` (`274-346`): override wins (`275-276`); Initial -> `chooseRandomProgram` (`235-272`, 5 phases); Continue/NextMain -> previous macro program (`285-295`); else a 7-phase `MarbleBag` scored by `MemeIsometry` from override memes or `getMemeIsometryOfNextSequenceInPreviousMacro` (`Fabricator.cpp:365-397`), avoiding the previous macro program in published phases 4-5 (`321`). `chooseMainProgram` (`348-416`): Continue -> previous main program (`353-366`); else isometry of the **segment memes as they stand** (`369`, `getMemeIsometryOfSegment` `399-402`), `isAllowed` filter (`379, 388`), 4 phases, avoiding previous main in phase 2 (`391-394`). "Memes at beginning" = program memes + offset-0 binding memes (`ContentEntityStore.cpp:544-561`).

Tests pinning this: `test/craft/macro_main/CraftFoundationInitialTest.cpp:56`, `CraftFoundationContinueTest.cpp:98` (asserts type Continue, `total 32`, `duration 32*MICROS_PER_MINUTE/140`, `intensity 0.2`, key `"G -"`, tempo 140, memes `{OUTLOOK,TROPICAL,COZY,WILD,PESSIMISM}`, chords `{"Bb -","C"}`, macro offset 1, main binding offset 1), `CraftFoundationNextMainTest.cpp:83`, `CraftFoundationNextMacroTest.cpp:30`; `test/fabricator/FabricatorTest.cpp:146-174` (`GetType` -> NextMacro when no previous choices).

---

## 4. Intensity and delta

### 4.1 Intensity
`computeIntensity` (`MacroMainCraft.cpp:177-192`): average of `macroSequence->intensity` and `mainSequence->intensity` when both exist, else whichever exists, else throws. `computeSegmentIntensity` (`163-175`):

```cpp
if (!cfg.intensityAutoCrescendoEnabled) return programmaticIntensity;
return ValueUtils::interpolate(cfg.intensityAutoCrescendoMinimum,
       cfg.intensityAutoCrescendoMinimum + (cfg.intensityAutoCrescendoMaximum - cfg.intensityAutoCrescendoMinimum) * programmaticIntensity,
       float(delta) / float(cfg.mainProgramLengthMaxDelta));
```
`interpolate(floor, ceiling, position) = floor + (ceiling - floor) * position` (`src/util/ValueUtils.cpp:133-135`). Defaults (`TemplateConfig.cpp:47-49, 74`): `intensityAutoCrescendoEnabled = true`, `Maximum = 0.8`, `Minimum = 0.2`, `mainProgramLengthMaxDelta = 280`.

Consumption: `DubWork.cpp:106-113` uses `intensityOverride` if set, else `segments.at(0)->intensity`, and converts to per-layer amplitude via `AudioMathUtils::computeIntensityAmplitude` with `intensityLayers`/`intensityThreshold` per instrument type (`DubWork.cpp:162-171`). Craft itself never reads `segment.intensity`.

### 4.2 Delta, and an apparent defect
`delta` is "beats elapsed within the current main program" and feeds both `computeType` (§3) and the crescendo position. Writers: `CraftWork.cpp:409` (`0` for initial), `CraftWork.cpp:425` (`last->delta`, copied), `MacroMainCraft.cpp:81` (`segment->delta + segment->total` on Continue), `:83` (`0` otherwise). Readers: `Craft.cpp:15-32` (intro/outro/silent/active), `Craft.cpp:796` (volume ratio under delta arcs), `Fabricator.cpp:1181`.

By static reading, **`segment->total` at `MacroMainCraft.cpp:81` is always `0` in the live chain**: `buildSegmentFollowing` never sets `total` (it is first assigned at `MacroMainCraft.cpp:76`, into `updatedSegment`, not `segment`). So a Continue segment's `delta` = previous delta + 0, and since Initial/NextMain/NextMacro set 0, `delta` stays `0` for the whole chain. Downstream: the crescendo position is always 0 so `intensity == intensityAutoCrescendoMinimum (0.2)` whenever auto-crescendo is enabled (the default), and the `mainProgramLengthMaxDelta` guard in `computeType` never fires, so a main program only ends when its binding offsets run out. The gtest fixtures build segments with `delta=0` explicitly (`test/_helper/SegmentFixtures.cpp:78-80, 125, 135`), which is why `CraftFoundationContinueTest.cpp:108` expects exactly `0.2` and does not catch this. A spec author should confirm with a runtime dump before relying on `delta` or `intensity` semantics (nothing in `workstation/` or the Java original is in this repo to compare against: `find . -name "MacroMainCraft*"` outside `engine/` returns nothing).

---

## 5. Fabricator getters the later stages use (and where each one really reads from)

Tallied with `grep -o "fabricator->[A-Za-z]*"` over `src/craft/{Beat,Detail,Transition,Background}Craft.cpp` and `Craft.cpp`.

| Getter | Impl (`Fabricator.cpp`) | Reads from | Used at |
|---|---|---|---|
| `getSegment()` | 767-773 | **store** (`readSegment`) | everywhere; fields read in craft: `id`, `delta`, `total` only (`grep "getSegment()->"`). `key` and `intensity` are **never read by any craft stage**; `tempo` only via `getTempo()` |
| `getTempo()` | 1063-1066 | **store** `getSegment()->tempo` | `BeatCraft.cpp:76,90`, `DetailCraft.cpp:73,78,82`, `TransitionCraft.cpp:29` |
| `getType()` | 847-852 (cache) / 1158-1176 | **content + retrospective** unless overridden (§3) | `Craft.cpp:74`, `TransitionCraft.cpp:33,44` |
| `getSegmentChords()` | 775-783 | **store**, sorted by position | `Craft.cpp:44,520,605,607` |
| `getChordAt(pos)` | 137-155 | store via `getSegmentChords` | `Craft.cpp:697`. Quirk: the first chord is accepted unconditionally (`147`, `foundPosition < 0`), so positions before the first chord get the first chord. The `chordAtPosition` cache member (`Fabricator.h:884`) is declared but never used |
| `getChordVoicings()` | 785-788 | **store** | via `getProgramVoicingNoteRange` |
| `chooseVoicing(chord, type)` | 854-879 | **store** + fresh RNG (§7) | `Craft.cpp:699` (once per event) |
| `getProgramVoicingNoteRange(type)` | 597-615 | **store** voicings with any valid notes; cached per type | `Craft.cpp:812` |
| `getSegmentMemes()` / `getMemeIsometryOfSegment()` | 790-793 / 399-402 | **store** + `chain->config.memeTaxonomy` | `Craft.cpp:168,203,240` (every fresh program/instrument/audio choice) |
| `getTotalSegmentMicros()` | 757-765 | **store** `durationMicros` (throws if unset) | `Craft.cpp:782,784`, `DetailCraft.cpp:119`, `TransitionCraft.cpp:75,79,83,92`, `BackgroundCraft.cpp:46` |
| `getSegmentMicrosAtPosition(tempo,pos)` / `getMicrosPerBeat(tempo)` | 752-755 / 1036-1041 | arithmetic, **cached on first call** (`if (0 == microsPerBeat)`): later `tempo` arguments are ignored for the life of the Fabricator | `Craft.cpp:537,542,714,718`, `DetailCraft.cpp:117,120`, `TransitionCraft.cpp:91`, `MacroMainCraft.cpp:419` |
| `getTemplateConfig()` | 127-130 | `chain->config` | all stages |
| `getRetrospective()` / `getChoiceIfContinued*` / `getChoicesIfContinued` | 1026-1029 / 214-286 | **store** (previous segments); gated on `getSegment()->type == Continue` (`216,236,256,275`) | `BeatCraft.cpp:13,66`, `DetailCraft.cpp:46`, `Craft.cpp:123,300,461,500`, `TransitionCraft.cpp:19,99`, `BackgroundCraft.cpp:12` |
| `getKeyForChoice(choice)` | 288-304 | content key of the **choice's own** (beat/detail) program or bound sequence, not the main program | `Craft.cpp:810` |
| `getProgram(choice)` | 478-481 | content of the choice's own program (`config.doPatternRestartOnChord`) | `Craft.cpp:40,43` |
| `getCurrentMainProgramConfig()` | 332-347 | **content**: main choice's `Program.config` | `Craft.cpp:85`, `TransitionCraft.cpp:85` |
| `getCurrentMainSequence()` / `getPreviousMainSequence()` | 349-355 / 357-363 | **content** via choice -> binding -> sequence | `TransitionCraft.cpp:45,48` |
| `getDistinctChordVoicingTypes()` | 183-205 | **content**: `getSequenceChordVoicingsOfProgram(mainChoice.programId)` | **no production caller**; only `test/fabricator/FabricatorTest.cpp:136` |
| `getMacroChoiceOfPreviousSegment()` / `getPreviousMainChoice()` | 318-330 | retrospective (store) | `MacroMainCraft.cpp` only, plus `computeType` |
| `put(...)` overloads | 952-1002 | store; `put(SegmentChoice, force)` and `put(SegmentMeme, force)` validate against `MemeStack::from(taxonomy, segment memes)` (`954, 985`) | all stages |

Bar/sub-section arithmetic used with `barBeats`: `Bar::of(barBeats).computeSubsectionBeats(total)` (`src/music/Bar.cpp:24-29`; `FACTORS_TO_TEST = {3,4,5,7}` at `Bar.cpp:11`; `ValueUtils::subDiv` `116-124`, `factors` `107-113`).

---

## 6. Every direct read of the main (or macro) program that bypasses segment entities

Verified with `grep -rn "getCurrentMainProgramConfig\|getMainProgram\|getCurrentMainSequence\|getPreviousMainSequence\|getMacroChoiceOfPreviousSegment\|getCurrentMainChoice\|getPreviousMainChoice\|getDistinctChordVoicingTypes\|cutoffMinimumBars\|barBeats" src include`.

### 6.1 In craft stages after MacroMainCraft (the proposal's "exactly three")

| # | File:line | Code | What it needs | Executes when |
|---|---|---|---|---|
| 1 | `src/craft/Craft.cpp:85` | `const auto barBeats = fabricator->getCurrentMainProgramConfig().barBeats;` (then `:86` `Bar::of(barBeats).computeSubsectionBeats(getSegment()->total)`) | main program `barBeats` | only inside `precomputeDeltas` for Initial/NextMain/NextMacro **and only if `deltaArcEnabled`** (`Craft.cpp:65`; default `false`, `TemplateConfig.cpp:28`). Dead in default configs. |
| 2 | `src/craft/TransitionCraft.cpp:45` | `const auto mainSequence = fabricator->getCurrentMainSequence();` | identity of current main `ProgramSequence` | `isMediumTransitionSegment`, Continue segments with a Transition instrument |
| 3 | `src/craft/TransitionCraft.cpp:48` | `const auto previousMainSequence = fabricator->getPreviousMainSequence();` (compared by `->id` at `:51`) | identity of previous main `ProgramSequence` | same |
| 4 | `src/craft/TransitionCraft.cpp:85` | `Bar::of(fabricator->getCurrentMainProgramConfig().barBeats).computeSubsectionBeats(fabricator->getSegment()->total)` | main program `barBeats` | every segment with a Transition instrument |

That is four call sites in three functions; the proposal's row "Craft.cpp and TransitionCraft.cpp barBeats" covers #1 and #4, and "TransitionCraft::isMediumTransitionSegment" covers #2-3. **No other craft code reads the main or macro program.** `BeatCraft.cpp`, `DetailCraft.cpp` and `BackgroundCraft.cpp` contain zero such calls.

### 6.2 In the work layer

| File:line | Code | Note |
|---|---|---|
| `src/work/CraftWork.cpp:130-142` | `CraftWork::getMainProgram(segment)`: scans `store->readAllSegmentChoices` for `programType == Main`, returns `content->getProgram(programId)` | the lookup used below; also exposed to hosts |
| `src/work/CraftWork.cpp:144-156` | `CraftWork::getMacroProgram(segment)` | same for Macro; **no in-engine caller** |
| `src/work/CraftWork.cpp:283` | `const auto currentMainProgram = getMainProgram(lastSegment.value());` | in `doFabricationRewrite` (`266-330`) |
| `src/work/CraftWork.cpp:292-298` | `mainProgramConfig = ProgramConfig(currentMainProgram->config)`; `subBeats = barBeats * cutoffMinimumBars` | `ProgramConfig` defaults `barBeats=4, cutoffMinimumBars=2, doPatternRestartOnChord=false` (`src/content/ProgramConfig.cpp:12-16`) |
| `src/work/CraftWork.cpp:300` | `microsPerBeat = MICROS_PER_MINUTE_FLOAT / currentMainProgram.value()->tempo` | **reads the program's tempo, not `lastSegment->tempo`**; `doCutoffLastSegment` (`332-369`) at `:336` uses `inputSegment->tempo` (segment). Under an External tempo policy these two disagree. |
| `src/work/DubWork.cpp:80-88` | `DubWork::getMainProgram/getMacroProgram` forward to `CraftWork` | no internal caller; public read API for hosts |

### 6.3 Latent, inside Fabricator
`getDistinctChordVoicingTypes` (`Fabricator.cpp:183-205`) reads the main program's chord voicings from content; it has no production caller. `getCurrentMainProgramConfig`, `getCurrentMainSequence`, `getPreviousMainSequence` are the content-reading getters behind §6.1.

**Verdict on the proposal's claim:** correct in substance. The complete list of content-side main-program dependencies that a follower must satisfy is: `barBeats` (two sites), current/previous main *sequence identity* (one function), and the rewrite's `barBeats * cutoffMinimumBars` plus tempo (one function in `CraftWork`). Plus the hidden one in §3: `computeType()` reads content unless the type is overridden.

---

## 7. Randomness: every generator in the engine

`grep -rn "random_device\|mt19937" --include=*.cpp --include=*.h .` (excluding `_deps`/`build`):

| Site | Kind | Used by |
|---|---|---|
| `src/fabricator/Fabricator.cpp:874-876` | **fresh `std::random_device rd; std::mt19937 gen(rd()); uniform_int_distribution` per call** inside `chooseVoicing` | `Craft.cpp:699`, once per pattern event |
| `include/xjmusic/fabricator/MarbleBag.h:33-34` | member `rd` + `gen{rd()}` per `MarbleBag` instance | `MarbleBag::pickPhase` (`MarbleBag.cpp:145-146`), i.e. every `bag.pick()` |
| `src/fabricator/MarbleBag.cpp:167-169` | fresh `random_device`+`mt19937` per call in static `quickPick(total)` | `Fabricator.cpp:629,642` (sequence / binding selection), `Craft.cpp:112,117,1008` |
| `src/fabricator/MarbleBag.cpp:176-178` | fresh per call in static `quickBooleanChanceOf(p)` | `Craft::computeMute` (`Craft.cpp:380-382`) |
| `include/xjmusic/craft/Craft.h:417-418` | member `rd` + `gen{rd()}` per `Craft` instance | `std::shuffle` at `Craft.cpp:101,104,110` (delta-arc layer order) |
| `include/xjmusic/music/StickyBun.h:22-25`, `src/music/StickyBun.cpp:14-17` | **static** `rd`, `gen`, `distrib(0, MAX_VALUE-1)` | `StickyBun.cpp:24` (values for atonal notes) |
| `include/xjmusic/util/ValueUtils.h:23-24`, `src/util/ValueUtils.cpp:33-34` | **static** `rd`, `gen` | `randomFrom` (`84-85`), `withIdsRemoved` (`165-166`) |
| `test/_helper/ContentFixtures.cpp:39-57` | test only | |

So the proposal's "four `random_device`-seeded generators (Fabricator, MarbleBag, StickyBun, ValueUtils)" undercounts: there are **seven distinct seeding sites in five source files**; `Craft.h:417` is a fifth file and `MarbleBag` has three (one member, two per-call statics). Entity ids are **not** random: `EntityUtils::computeUniqueId()` (`src/content/EntityUtils.cpp:10-14`) formats a process-global `static unsigned long long UNIQUE_ID_COUNTER` (`include/xjmusic/util/EntityUtils.h:29`) as 12 hex digits, so ids depend only on operation order but are shared by every engine instance in the process.

### Voicing selection in detail
`Fabricator::chooseVoicing(chord, instrumentType)` (`854-879`): reads all voicings of the segment from the store (`857`), keeps those with `SegmentUtils::containsAnyValidNotes(voicing)` (`src/fabricator/SegmentUtils.cpp:68-70` -> `Note::containsAnyValidNotes`, `src/music/Note.cpp:93-96`: any CSV token is a valid note) **and** matching `type` **and** `segmentChordId == chord->id` (`862`); returns `nullopt` if none (`868-870`); otherwise picks uniformly with a brand-new generator (`873-878`). Result is **not cached**: two events on the same chord and type may be voiced from different `SegmentChordVoicing`s when the main program defines more than one voice of that type. `getProgramVoicingNoteRange` (`597-615`) by contrast unions all voicings of the type.

---

## 8. Rewrite, override, and control-mode entry points (context for cues)

- `Engine::doOverrideMacro/doOverrideMemes/setIntensityOverride` (`src/Engine.cpp:104-122`) -> `WorkManager` (`src/work/WorkManager.cpp:113-139`) -> `CraftWork::doOverrideMacro` (`CraftWork.cpp:184-189`) / `doOverrideMemes` (`191-197`), which set `nextCycleOverride*` and `nextCycleRewrite` via `doNextCycleRewriteUnlessInitialSegment` (`206-210`; no rewrite when the store has zero segments).
- `CraftWork::doFabrication` (`212-223`): if `nextCycleRewrite`, `doFabricationRewrite(atChainMicros + deadlineMicros, ...)`; else `doFabricationDefault(atChainMicros + craftAheadMicros, ...)`. The overrides stay in force for every later segment (passed on both paths at `216` and `221`).
- `doFabricationRewrite` (`266-330`): finds the segment spanning `toChainMicros` (`274`), computes `cutoffAfterBeats = subBeats * ceil(dubbedToSegmentBeats / subBeats)` (`298-302`), cuts if shorter than `total` (`303-306`, `doCutoffLastSegment` `332-369`: sets `total`, `durationMicros`, deletes/trims picks), `deleteSegmentsAfter` (`319`), builds the following segment, forces `type = NextMacro` (`321`) and crafts it with `overrideSegmentType = NextMacro` (`322`).
- `WorkManager::start` (`34-57`): in `ControlMode::Macro` overrides with `*getAllMacroPrograms().begin()`; in `Taxonomy` with the first meme of each taxonomy category.

---

## 9. Implications for V2 specs

**Foundation record content**
1. The record must carry `segment.type, key, tempo, intensity, delta, total, beginAtChainMicros, durationMicros`, the chords (`position, name`), voicings (`segmentChordId, type, notes` CSV; **multiple per chord per type must be allowed**, `MacroMainCraft.cpp:59-67`), and the memes present at the end of `MacroMainCraft::doWork`. Note memes keep being appended by every later `put(SegmentChoice)` (`Fabricator.cpp:1225-1232`), so "foundation memes" are a snapshot, not the segment's final meme set.
2. `barBeats` is **not a segment field** (`Segment.h:24-39`); the proposal's `segment.barBeats` is new. In internal mode it would be filled from `getCurrentMainProgramConfig().barBeats`. `cutoffMinimumBars` likewise lives only in `ProgramConfig` (`ProgramConfig.h:20-22`).
3. `isMediumTransitionSegment` compares **`ProgramSequence` ids** (`TransitionCraft.cpp:51`), not binding offsets. Two offsets can bind the same sequence (then it is *not* medium). The record should carry an opaque "main sequence identity" per segment (and the follower keeps the previous record's), rather than relying on `mainOffset`.
4. `durationMicros` is required by five later-stage call sites via `getTotalSegmentMicros()` (throws if unset, `Fabricator.cpp:760-763`); the record must include it or the follower must recompute `total * MICROS_PER_MINUTE / tempo` exactly as `segmentLengthMicros` does (`MacroMainCraft.cpp:418-420`, integer `long` truncation).

**ExternalFoundationCraft design**
5. It must write the segment via `Fabricator::updateSegment` after `Planned -> Crafting` (state transitions are enforced, `SegmentEntityStore.cpp:325-347`), and must pass the record's type as `overrideSegmentType` to the `Fabricator` ctor (`CraftWork.cpp:381`), because `getType()` (`Craft.cpp:74`, `TransitionCraft.cpp:33,44`) returns the cached computed type, and `computeType()` consults **content** bindings of the previous main choice (`Fabricator.cpp:1166,1172`).
6. `getChoiceIfContinued*` is gated on `getSegment()->type == Continue` (`214-286`) and on `SegmentRetrospective`, whose ctor **throws** `"Retrospective sees no main choice!"` when the previous segment has no Main choice (`SegmentRetrospective.cpp:35-41`). A follower that writes no macro/main `SegmentChoice` will crash on the second segment unless it writes placeholder choices or the retrospective is relaxed. Decide which (the proposal's "opaque names" suggest placeholder choices whose `programId` is not resolvable in content; check every `sourceMaterial->getProgram(choice->programId)` on those, e.g. `getKeyForChoice` throws at `Fabricator.cpp:300`, though it is only called for beat/detail choices).
7. The retrospective also groups "previous segments of the same main program" by comparing main-choice `programId` across all stored segments (`SegmentRetrospective.cpp:43-51`); an opaque main identity must be stable across the conductor's Continue segments for `getChoicesIfContinued` to behave as today.
8. `getMicrosPerBeat` caches the first tempo it sees (`Fabricator.cpp:1038`). Any tempo-as-input or follow-mode change must write `segment.tempo` before the first beats-to-micros conversion, or drop the cache.
9. `CraftWork::doFabricationRewrite` reads `currentMainProgram->tempo` (`CraftWork.cpp:300`) while `doCutoffLastSegment` reads `inputSegment->tempo` (`:336`). For External tempo and follow mode, `:300` must become `lastSegment->tempo`, and `barBeats * cutoffMinimumBars` must come from the record/template as the proposal says.
10. `Craft.cpp:85` only runs with `deltaArcEnabled=true` (default false). It still needs the record's `barBeats` for correctness, but a spec can note it is inert for most templates today.

**Seeded randomness**
11. Replace seven sites (§7), not four. Per-call fresh `random_device` in `chooseVoicing`, `MarbleBag::quickPick` and `quickBooleanChanceOf` means a single engine-owned generator must be threaded through static helpers; `Craft` subclasses also need it (`Craft.h:417`). `computeUniqueId` is a process-global counter, so "byte-identical segment stores" across two engines in one process will differ in ids unless the counter is per-engine or ids are excluded from the comparison.
12. `chooseVoicing` is uncached and re-rolled per event (`Craft.cpp:699`). Determinism makes this reproducible, but a spec for voicing selection (or the Commons' voicing rules) should decide whether per-event re-rolling across multiple voicings of one type is intended.

**Delta / intensity**
13. By static reading, `delta` never leaves 0 in a live chain (`MacroMainCraft.cpp:81` adds `segment->total`, which `buildSegmentFollowing` never sets), so `intensity` is pinned to `intensityAutoCrescendoMinimum` (0.2) under default config and `mainProgramLengthMaxDelta` never ends a main program. Any spec that publishes `intensity`/`delta` in the foundation record, or builds performer intensity cues on top of the crescendo, should first verify this at runtime and decide whether V2 fixes it (behavior-preserving default is then impossible to keep literally).
14. Craft never reads `segment.key` or `segment.intensity` (`grep "getSegment()->"` finds only `id`, `delta`, `total`); the key is consumed indirectly through chords/voicings and `getKeyForChoice` reads the *detail program's* key. A follower therefore needs chords+voicings far more than `key`; `key` is for display and for the main-program selection a follower does not do.

**Override / cue semantics**
15. A macro override starts the overridden macro at its **second** binding offset (`MacroMainCraft.cpp:196-198`, `Fabricator.cpp:1043-1056`), and a meme override discards the retrospective entirely (`CraftWork.cpp:378`). Cue specs that "wrap the old behavior" must reproduce both quirks or declare them changed.
16. `doFabricationRewrite` always forces `NextMacro` (`CraftWork.cpp:321-322`) even for a meme-only override; a "Chord" or "Advance" cue that wants `NextMain` needs a new path, not a parameterization of this one.

**Tests to extend**
17. The foundation-type tests live at `test/craft/macro_main/CraftFoundation{Initial,Continue,NextMain,NextMacro}Test.cpp`; follow-mode conformance tests should assert the same fields those tests assert (`CraftFoundationContinueTest.cpp:105-119`) for conductor vs follower per segment span.
