# Grounded map: layer craft stages and choice machinery (engine 1.9.6)

All paths are relative to `/Users/nck/xj/xjmusic/engine/`. Line numbers are from `grep -n` / `nl` against the working tree at commit `a94000b9a`. Where a behavior is inferred rather than read, it is marked **(inferred)**. Where the code looks wrong, it is marked **(defect)** and the evidence is cited; nothing here was fixed or tested, only read.

## 0. Pipeline position and shared plumbing

The five craft stages run in fixed order per segment in `src/work/CraftWork.cpp:386-390`:

```
MacroMainCraft(&fabricator, overrideMacroProgram, overrideMemes).doWork();   // :386
BeatCraft(&fabricator).doWork();                                             // :387
DetailCraft(&fabricator).doWork();                                           // :388
TransitionCraft(&fabricator).doWork();                                       // :389
BackgroundCraft(&fabricator).doWork();                                       // :390
```

The `SegmentRetrospective` is built at `CraftWork.cpp:378` with `segmentId` normally, but with `0` (an empty retrospective) when `overrideMemes` is non-empty. `SegmentRetrospective::SegmentRetrospective` (`src/fabricator/SegmentRetrospective.cpp:11-52`) loads the previous segment whenever `segmentId > 0`, regardless of segment type (the comment at :15-16 claiming it is empty for Initial/NextMain/NextMacro is not enforced by code). `previousSegmentIds` (:43-51) is the set of all stored segments whose Main choice has the same `programId` as the previous segment's Main choice, so "retrospective" means "the current main program's run so far".

All four layer stages derive from `Craft` (`include/xjmusic/craft/Craft.h:25`), itself a `FabricationWrapper` holding `Fabricator *fabricator`. Every choice is stored through `Fabricator::put(const SegmentChoice&, bool force)` (`src/fabricator/Fabricator.cpp:952-961`), which is the second meme gate (section 4). All four layer stages call `put(choice, false)`.

Instrument enums (`include/xjmusic/content/Instrument.h`): `Type` :17-26 = `Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition`; `Mode` :30-32 = `Chord, Event, Loop`; `State` :36-37 = `Draft, Published`.

## 1. Stage-by-stage

| Stage | File | Instrument types | Program type | Continuity source | Fresh choice path | Pick producer |
|---|---|---|---|---|---|---|
| BeatCraft | `src/craft/BeatCraft.cpp` (94 lines) | `Drum` only (:17, :79) | `Program::Type::Beat` | `getChoicesIfContinued(Beat)` :13 and `getChoiceIfContinued(voice)` :66 (Continue segments only) | `chooseFreshProgram(Beat, Drum)` :17, then per voice `chooseFreshInstrument(Drum, trackNamesOfVoice)` :79-80 | `craftNoteEventArrangements(tempo, choice, defaultAtonal=true)` :76, :90 |
| DetailCraft | `src/craft/DetailCraft.cpp` (137 lines) | `DETAIL_INSTRUMENT_TYPES` :7-14 = `Bass, Pad, Sticky, Stripe, Stab, Hook, Percussion` | `Program::Type::Detail` (Event mode only) | `getChoiceIfContinued(instrumentType)` :46 | **instrument first** `chooseFreshInstrument(type, {})` :51, then by `instrument->mode` :60: Event → `chooseFreshProgram(Detail, type)` :67; Chord → none; Loop → none | Event: `craftEventParts` :73; Chord: `craftChordParts` :78; Loop: `craftLoopParts` :82 |
| TransitionCraft | `src/craft/TransitionCraft.cpp` (124 lines) | `Transition` :19, :23 | none | `retrospective->getPreviousChoiceOfType(Instrument::Type::Transition)` :18-19, **any segment type** | `chooseFreshInstrument(Transition, {})` :23 | `craftTransition` :54-95 |
| BackgroundCraft | `src/craft/BackgroundCraft.cpp` (53 lines) | `Background` :12, :16 | none | `retrospective->getPreviousChoiceOfType(Instrument::Type::Background)` :12, **any segment type** | `chooseFreshInstrument(Background, {})` :16 | `craftBackground` :25-52 |

### 1.1 BeatCraft details

- One Beat program per segment. On a Continue segment the program is taken from the first prior Beat choice (:16-18). Otherwise a fresh program is chosen requiring a `Drum` voice.
- Sequence: `fabricator->getRandomlySelectedSequence(program)` :50 → uniform `MarbleBag::quickPick` over the program's sequences (`Fabricator.cpp:617-630`). Comment :49 acknowledges multi-sequence beat programs are FUTURE.
- Per `ProgramVoice` (:54-92): `choice.mute = computeMute(voice->type)` :58 is rolled **before** and independently of whether the voice continues. If `getChoiceIfContinued(voice)` matches by voice **name and type** (`Fabricator.cpp:214-232`), `deltaIn/deltaOut/instrumentId/instrumentMode` are copied (:70-73). Otherwise `chooseFreshInstrument(Drum, getTrackNamesOfVoice(voice))` requires the instrument to have an audio whose `event` string equals **every** track name of the voice (`Craft.cpp:1012-1024`, exact string compare, no `toMeme` normalization).
- Delta arcs: `precomputeDeltas(filter=Beat, index=voice name, layers=voice names, deltaArcBeatLayersToPrioritize, deltaArcBeatLayersIncoming)` :41-46.

### 1.2 DetailCraft details

- Iteration is over a `std::set<Instrument::Type>` :7-14, so the order is enum order: Bass, Hook, Pad, Percussion, Stab, Sticky, Stripe (not the declared textual order). This matters because memes added by earlier choices gate later ones (section 4).
- Exactly one instrument per detail type per segment (`getChoiceIfContinued(instrumentType)` finds the first retrospective choice of that type, `Fabricator.cpp:234-251`).
- Mode dispatch :60-91. `default:` emits `addWarningMessage("Cannot craft unsupported mode ...")` :87-89; today all three enum values are handled, so this is unreachable.
- Loop mode (`craftLoopParts` :95-137): choice has no program; one arrangement; for each audio from `selectGeneralAudioIntensityLayers(instrument)` :110, picks are laid from beat 0 stepping `audio->loopBeats` (:113-135) with `lengthMicros = min(remaining segment micros, loopBeats * microsPerBeat(segment tempo))` :118-120, `amplitude 1.0`, `event "LOOP"`. **The audio's own `tempo` field is never read**: a loop recorded at 90 bpm played in a 120 bpm segment is scheduled every `loopBeats` beats of the *segment* tempo, so it overlaps or truncates itself. No stretch is requested anywhere. This is the concrete G7 gap.
- Delta arcs: `precomputeDeltas(filter=Detail, index=Instrument::toString(type), layers=detailLayerOrder, {}, deltaArcDetailLayersIncoming)` :35-40. Default `detailLayerOrder = ["Bass","Pad","Stab","Sticky","Stripe"]` (`src/content/TemplateConfig.cpp:29`) omits Hook and Percussion, which therefore always get `DELTA_UNLIMITED` via the map-miss in `computeDeltaIn` (`Craft.cpp:584-587`).

### 1.3 TransitionCraft details

- Instrument persistence is not segment-type-aware: `getPreviousChoiceOfType(Instrument::Type)` (`SegmentRetrospective.cpp:99-108`) returns the first previous-segment choice of that type. Once a Transition instrument is chosen it is inherited across NextMain and NextMacro forever, until a meme override empties the retrospective (`CraftWork.cpp:378`). Same for Background.
- Event-name classes come from `TemplateConfig` (`eventNamesLarge/Medium/Small`, defaults `TemplateConfig.cpp:42-44`: `["BIG","HIGH","LARGE","PRIMARY"]`, `["MEDIUM","MIDDLE","REGULAR","SECONDARY"]`, `["LITTLE","LOW","SMALL"]`), cached in the ctor :11-13.
- `isBigTransitionSegment` :32-41 = type ∈ {Initial, NextMain, NextMacro}. `isMediumTransitionSegment` :43-52 = Continue **and** `getCurrentMainSequence()->id != getPreviousMainSequence()->id`; throws `FabricationException` if either sequence is missing (:46-47, :49-50). This is one of the three direct main-program reads the proposal's follow-mode caveat table lists.
- Picks :73-83: all big audios at 0 if big segment and any exist; else all medium; else all small. Then :85-94 all small audios again at every subsection boundary, `deltaUnits = Bar::of(mainProgramConfig.barBeats).computeSubsectionBeats(segment->total)` (second direct main-program read).
- **(defect)** Every transition pick is given `lengthMicros = fabricator->getTotalSegmentMicros()` (:75, :79, :83, :92) even when it starts mid-segment, so the stored pick nominally overruns the segment end.
- `selectAudiosForInstrument` :97-124: if `isAudioSelectionPersistent` and previous picks of this instrument exist with an event in the name set, reuse exactly those audios :104-112; else filter the instrument's audios by `toMeme(audio->event) ∈ names` and bin by intensity layers with **`getIntensityLayers(Instrument::Type::Background)`** :120-123, not `Transition`. **(defect, benign by default)** Both default to 3.

### 1.4 BackgroundCraft details

- One choice, one arrangement, one pick per intensity layer :40-51: `startAtSegmentMicros = 0`, `lengthMicros = getTotalSegmentMicros()`, `amplitude 1.0`, `event "BACKGROUND"`.
- Audios via `selectGeneralAudioIntensityLayers` (`Craft.cpp:299-315`): if `isAudioSelectionPersistent` (default true, `src/content/InstrumentConfig.cpp:16`) and the retrospective has previous picks for this instrument, the same audios are reused; otherwise `selectAudioIntensityLayers(allAudios, getIntensityLayers(type))`.

## 2. The choice algorithm: candidates, meme isometry, MarbleBag phases

### 2.1 `Craft::chooseFreshProgram` (`src/craft/Craft.cpp:149-188`)

1. `programMap` = all programs of `programType` (:154-157).
2. Candidates = programs that have a `ProgramVoice` of `voicingType` (:159-165). **If `voicingType` is `nullopt` the candidate set is empty** (the condition requires `has_value()`); every current caller passes a type.
3. `iso = fabricator->getMemeIsometryOfSegment()` (:168) = `MemeIsometry::of(chain->config.memeTaxonomy, SegmentMeme names)` (`Fabricator.cpp:399-402`).
4. Phase 1 (:172-176): for each **directly bound** candidate (`fabricator->isDirectlyBound(program)`, `Fabricator.cpp:917-920` → `boundProgramIds`), if `iso.isAllowed(programMemes)` then `bag.add(1, id, 1 + iso.score(memes))`.
5. Phase 2 (:179-183): same for every **Published** candidate (`program->state == Published`, :327-335), phase 2. A bound and published program appears in both phases.
6. Return `nullopt` if the bag is empty, else `getProgram(bag.pick())` (:186-187).

Exclusion by `iso.isAllowed` is **silent**: no message, no record. The comment at :174/:181 ("FUTURE consider meme isometry, but for now, just use the meme stack") is stale; the score is used as weight.

### 2.2 `Craft::chooseFreshInstrument` (:190-221)

Same shape. Candidates = `getInstrumentsOfType(type)` filtered by `instrumentContainsAudioEventsLike(instrument, requireEventNames)` (:196-200; body :1012-1024 returns true if `requireEvents` is empty, else requires each required name to equal some `audio->event`). Phase 1 bound (:207-210), phase 2 published (:213-216), weight `1 + iso.score(instrumentMemes)`.

### 2.3 `Craft::chooseFreshInstrumentAudio` (:223-278)

Four phases: bound+preferred-event = 1, published+preferred = 2, bound+other = 3, published+other = 4 (:247, :255). Builds `typeNames`/`modeNames` strings (:259-273) that are never used. **No call site exists anywhere in `src/`** (grep for `chooseFreshInstrumentAudio` outside `Craft.cpp` returns nothing); it is dead code.

### 2.4 Weighting semantics

`MemeIsometry::score` (`src/meme/MemeIsometry.cpp:26-40`): returns 0 if `!isAllowed(targets)`, else the count of target memes whose `toMeme` form equals a source meme. Since targets are a `std::set`, duplicates do not double-count. Weight is therefore `1 + (number of memes shared with the segment)`: a candidate sharing nothing still has one marble; a candidate sharing k memes has k+1.

### 2.5 MarbleBag (`include/xjmusic/fabricator/MarbleBag.h`, `src/fabricator/MarbleBag.cpp`)

| Member | Lines | Behavior |
|---|---|---|
| `marbles` | `.h:32` | `std::map<int phase, std::map<UUID, int qty>>` |
| `rd`, `gen` | `.h:33-34` | per-instance `std::random_device` → `std::mt19937`; a new bag is constructed in every choose call, so every choice consumes a fresh `random_device` draw |
| copy ctor / `operator=` | `.cpp:182-184`, `.h:52-57` | copy `marbles` only, not the generator |
| `add(phase,id,qty)` | `.cpp:60-67` | increments quantity |
| `pick()` | `.cpp:16-30` | sorts phases ascending; first phase whose `pickPhase` returns a value wins; throws `FabricationException("<n> available; no marble picked!")` if none |
| `pickPhase(phase)` | `.cpp:128-154` | builds contiguous `Group{id, from, to}` blocks in `UUID` (map) order over marbles with `qty > 0`; `uniform_int_distribution(0, total-1)` on `gen` (:145-146); linear scan to the block |
| `quickPick(total)` | `.cpp:162-171` | returns 0 if `total == 1`; throws if `0`; otherwise constructs **a new `random_device` and `mt19937` per call** (:167-168) |
| `quickBooleanChanceOf(p)` | `.cpp:173-180` | **throws `FabricationException("Probability must be 0 <= n < 1")` if `p < 0 || p >= 1`** (:174-175); new `random_device`/`mt19937` per call (:176-177); `uniform_real_distribution<>(0,1) < p` |
| `toString()` | `.cpp:87-106` | `"Phase1[id:qty, ...], Phase2[...]"`: an existing, unused, diagnostics-grade dump |

Tests: `test/fabricator/MarbleBagTest.cpp` covers `pick_phaseLowerPreferred` (:62), `pick_skipEmptyPhases` (:75), `quickPick_exceptionIfZero` (:124).

Where phases are used across the engine: programs/instruments use phases 1 (bound) and 2 (published); `selectNewChordPartInstrumentAudio` uses phase 0 (exact chord) and 1 (acceptable chord) (`Craft.cpp:287, :289`); `selectAudioIntensityLayers` uses phase 1 only (:423); `Fabricator::getRandomlySelectedPatternOfSequenceByVoiceAndType` uses phase 1 only (`Fabricator.cpp:655`).

## 3. Intensity layers, thresholds, and mute

### 3.1 TemplateConfig fields that gate layers (`include/xjmusic/content/TemplateConfig.h`, `src/content/TemplateConfig.cpp`)

| Field | Type (.h) | Default (.cpp lines) | Getter fallback | Consumers |
|---|---|---|---|---|
| `intensityLayers` | `map<Instrument::Type,int>` :40 | Background 3, Bass 1, Drum 1, Hook 3, Pad 3, Percussion 3, Stab 2, Sticky 2, Stripe 2, Transition 3 (:50-61) | `getIntensityLayers` :346-352 → **0** if type absent | `Craft.cpp:314`, `TransitionCraft.cpp:122`, `DubWork.cpp:164,169` |
| `intensityThreshold` | `map<Instrument::Type,float>` :39 | all 0.5 (:62-73) | `getIntensityThreshold` :338-344 → 0.0 | **dub only**: `DubWork.cpp:165,170` |
| `choiceMuteProbability` | `map<Instrument::Type,float>` :37 | all 0.0 (:13-24) | `getChoiceMuteProbability` :322-328 → 0.0 | `Craft::computeMute` :380-382 |
| `deltaArcEnabled` | bool :35 | **false** (:28) | n/a | `Craft.cpp:65, :794` |
| `deltaArcBeatLayersIncoming` / `deltaArcDetailLayersIncoming` | int :50-51 | 1 / 1 (:25, :27) | n/a | `BeatCraft.cpp:46`, `DetailCraft.cpp:40` |
| `deltaArcBeatLayersToPrioritize` | `set<string>` :33 | `["kick"]` (:26) | n/a | `BeatCraft.cpp:45` |
| `detailLayerOrder` | `vector<Instrument::Type>` :26 | `["Bass","Pad","Stab","Sticky","Stripe"]` (:29) | n/a | `DetailCraft.cpp:38` |
| `instrumentTypesForInversionSeeking` | set :28 | `["Pad","Stab","Sticky","Stripe"]` (:46) | `instrumentTypesForInversionSeekingContains` :317-320 | `Craft.cpp:850` |
| `instrumentTypesForAudioLengthFinalization` | set :27 | `["Bass","Pad","Stab","Sticky","Stripe"]` (:45) | n/a | `Craft.cpp:11, :747` |
| `eventNamesLarge/Medium/Small` | set :29-31 | see 1.3 (:42-44) | n/a | `TransitionCraft.cpp:11-13` |
| `stickyBunEnabled` | bool | true (:95) | n/a | `Fabricator.cpp:697` |
| `memeTaxonomy` | `MemeTaxonomy` | `COLOR[BLUE,GREEN,RED]; SEASON[FALL,SPRING,SUMMER,WINTER]` (:75-84) | n/a | `Fabricator.cpp:401, :954, :985` |

### 3.2 How "intensity layers" actually work in craft

`Craft::selectAudioIntensityLayers(audios, layers)` (`Craft.cpp:402-437`): sort audios by `audio->intensity` ascending (:405-409); `marblesPerLayer = ceil(n / layers)` (:419-420); audio `i` goes into bag `i / marblesPerLayer` (:422-424); pick one audio from each non-empty bag (:426-435). So "layers" is a count of contiguous intensity bands, and craft picks **one audio per band**, all of which become picks for the whole segment. Nothing in craft compares the segment's `intensity` to anything; the segment intensity is written by `MacroMainCraft::computeSegmentIntensity` (`MacroMainCraft.cpp:86, :168-183`) and read only at dub.

**(defect)** With `layers == 0` (a type missing from `intensityLayers`), `bags` is empty and `marblesPerLayer` is `static_cast<int>(inf)`; the `== 0` guard at :421 does not fire, and `bags[i / marblesPerLayer]` indexes an empty vector. Only reachable with a custom template that omits a type.

### 3.3 Thresholds gate at dub, by amplitude

`DubWork.cpp:118-172`: for every segment in the dub window, for every choice with `!choice->mute` (:120), for every pick, an `ActiveAudio` is created with `fromAmplitude`/`toAmplitude` from `AudioMathUtils::computeIntensityAmplitude(audio, layers, threshold, fadeOutLowerIntensities=false, intensity)` (:162-171). That function (`src/audio/AudioMathUtils.cpp:5-12`): returns 1.0 if `layers == 1`; otherwise `min(1, 2 * max(0, 1 - (audio.intensity - intensity) / threshold))`. Because `fadeOutLowerIntensities` is always `false`, audios whose intensity is **below** the current intensity are always full volume; only audios above it fade, reaching silence when `audio.intensity - intensity >= threshold`. So "intensity gating" is a continuous per-pick gain computed each dub frame from `segment->intensity` or `intensityOverride` (:106-113); craft is not involved after choosing one audio per band.

### 3.4 Mute probability

`Craft::computeMute(type)` (:380-382) = `MarbleBag::quickBooleanChanceOf(getChoiceMuteProbability(type))`. Called at `BeatCraft.cpp:58`, `DetailCraft.cpp:99`, `TransitionCraft.cpp:58`, `BackgroundCraft.cpp:29`, `Craft.cpp:449` (event voices), `Craft.cpp:494` (chord parts). Facts a spec must know:

- The roll happens **per choice per segment**, including Continue segments, and is never copied from the prior choice; a muted bass in segment 7 is independently re-rolled in segment 8.
- Stored as `SegmentChoice::mute` (`include/xjmusic/segment/SegmentChoice.h:30`). Arrangements and picks are still crafted for muted choices; the only consumer is `DubWork.cpp:120`, which reads the stored bit live each dub frame.
- `quickBooleanChanceOf` **throws for probability `>= 1`** (`MarbleBag.cpp:174-175`). The proposal's layer-mute cue ("sets that type's choice mute probability to 1") would throw `FabricationException` from inside craft as written.

## 4. Meme grammar and the meme stack, as implemented

### 4.1 Normalization

`StringUtils::toMeme` (`src/util/StringUtils.cpp:56-60`) removes every character matching `nonMeme = "[^a-zA-Z0-9!$]"` (:18) and uppercases. So `!`, `$` and digits survive; spaces, `-`, `_`, `#` are deleted ("Hip-Hop" → "HIPHOP"). `MemeStack`'s ctor applies `toMeme` to sources (`src/meme/MemeStack.cpp:19-24`); `MemeIsometry` applies it to sources (`MemeIsometry.cpp:11`) and to targets when scoring (:31) but **not** inside `MemeStack::isAllowed`'s target loop, which relies on callers already having normalized (both `Craft.cpp` paths pass raw `ProgramMeme::getNames`/`InstrumentMeme::getNames`; `Fabricator.cpp:1207, :1211, :1215` normalize before `put`). **(inferred)** A program meme authored as `"!apples"` is scored against the normalized stack correctly only because `ParseAnti` on the raw string still sees `!`; a meme authored `"Apples!"` vs `"APPLES"` would mismatch on case in `MemeIsometry::isAllowed`.

### 4.2 The four parsers

| Grammar | Class / regex | File:line | Violation rule | Direction in `MemeStack::isAllowed` |
|---|---|---|---|---|
| `!X` anti-meme | `ParseAnti`, `^!(.+)$` | `src/meme/ParseAnti.cpp:38`; `isViolatedBy` :27-30 | `!X` vs `X` (either side) with equal body | source → targets (`MemeStack.cpp:36-39`) |
| `X!` strong meme | `ParseStrong`, `^(.+)!$` | `ParseStrong.cpp:37`; `isAllowed` :30-35 | a strong target is allowed only if **some source has body X** (plain `X`, or `X!` whose body is X) | **target → sources** (`MemeStack.cpp:51-55`) |
| `$X` unique meme | `ParseUnique`, `^\$(.+)$` | `ParseUnique.cpp:40`; `isViolatedBy` :30-32 | two valid `$X` with equal body | source → targets (:45-47) |
| `NNNX` numeric meme | `ParseNumeric`, `^([0-9]+)(.+)$` | `ParseNumeric.cpp:50`; `isViolatedBy` :40-42 | both valid, equal body, **different** prefix (`2STEP` excludes `4STEP`, not `4NOTE`) | source → targets (:41-43) |

Test fixtures with the exact accepted/rejected sets: `test/meme/MemeStackTest.cpp:25-84`.

### 4.3 Taxonomy categories

`MemeCategory` regex `^([a-zA-Z ]+)\[([a-zA-Z, ]+)\]$` (`src/meme/MemeTaxonomy.cpp:17`), categories separated by `;` (:127), each name and meme `sanitize`d = trim → alphabetical only → upper (:20-22). Digits, `!`, `$` are therefore **stripped** from taxonomy memes; a taxonomy cannot contain numeric/anti/unique/strong forms. `MemeCategory::isAllowed(targets)` (:97-105) returns `count(category memes present in targets) <= 1`; `MemeTaxonomy::isAllowed` (:190-197) ANDs over categories. The default taxonomy is in `TemplateConfig.cpp:75-84`.

### 4.4 MemeStack axioms (`src/meme/MemeStack.cpp`)

`isAllowed(sources, targets)` (:34-62): for each source, parse all targets as Anti/Numeric/Unique and check the source against them (:36-48); for each target, parse all sources as Strong and check (:51-55); finally `taxonomy.isAllowed(sources ∪ targets)` (:58-61). `isValid()` (:64-81) checks each member against all the others, then the taxonomy. Documented behaviors from tests: `{"LEMONS!"}` alone is invalid, `{"LEMONS","LEMONS!"}` valid (`MemeStackTest.cpp:79-83`). `getConstellation()` (:83-85) → `MemeConstellation::fromNames` (`MemeConstellation.cpp:7-13`), names joined by `"_"` in `std::set` order.

### 4.5 The two gates per choice

1. **Bag time** (`Craft.cpp:175, :182, :209, :215`): `iso.isAllowed(candidateMemes)` against the segment memes present *at that moment*. Silent exclusion.
2. **Put time** (`Fabricator.cpp:952-961` → `isValidChoiceAndMemesHaveBeenAdded` :1201-1235): gathers `toMeme` names from the choice's program memes, sequence-binding memes and instrument memes (:1205-1215); if `!force && !memeStack.isAllowed(names)` an **Error** `SegmentMessage` is written: `"Refused to add Choice[<describe>] because adding Memes[...] to MemeStack[<constellation>] would result in an invalid meme stack theorem!"` (:1219-1220) and `nullopt` is returned; every layer stage then silently `continue`s or returns (e.g. `BeatCraft.cpp:74-76`, `DetailCraft.cpp:103`, `TransitionCraft.cpp:62`, `BackgroundCraft.cpp:33`). On success each meme name is added as a `SegmentMeme` via `put(segmentMeme, false)` (:1225-1232), which dedupes (`isValidMemeAddition` :1237-1246).

Because gate 2 runs with memes accumulated by earlier stages and earlier voices in the same stage, a program chosen in gate 1 can still be refused in gate 2 (e.g. Beat program memes `2STEP` accepted at bag time, then a Drum instrument with `4STEP` refused). The Error message already exists; it is the one existing "why excluded" record.

## 5. Picks: production and length (beats × tempo, no stretch)

### 5.1 Time base

- `fabricator->getTempo()` = `segment->tempo` (`Fabricator.cpp:1063-1066`), set from `mainProgram->tempo` at `MacroMainCraft.cpp:74`. Segment `total` (beats) :76, `durationMicros` :77, `delta` :81-83 (cumulative beats since the main program began; 0 on a new main).
- `getMicrosPerBeat(tempo)` (:1036-1041) = `MICROS_PER_MINUTE / tempo`, **cached in the member `microsPerBeat` on first call** for the Fabricator's lifetime (one segment), so the first tempo passed wins.
- `getSegmentMicrosAtPosition(tempo, beats)` (:752-755) = `long(microsPerBeat * beats)`. `getTotalSegmentMicros()` (:757-765) = `segment->durationMicros` or throws `FabricationFatalException`.

### 5.2 Event-mode picks (`Craft.cpp:35-52, 594-732`)

`craftNoteEventArrangements` (:35-52) creates one `NoteRange range` for voice leading across the whole choice, then either one section `[0, total)` or one per chord if `programConfig.doPatternRestartOnChord` (`ProgramConfig` default `false`, `src/content/ProgramConfig.cpp:15`) via `computeSections` (:603-620). `craftNoteEventSection` (:622-642) loops: `getRandomlySelectedPatternOfSequenceByVoiceAndType(choice)` (`Fabricator.cpp:645-665`, phase-1 bag over patterns matching `programSequenceId` and `programVoiceId`), then `craftPatternEvents` (:644-678) writes one `SegmentChoiceArrangement` per pattern instance (with `programSequencePatternId`) and advances by `min(loopBeats, pattern->total)` (:677).

`pickNotesAndInstrumentAudioForEvent` (:680-732):

| Step | Line | Rule |
|---|---|---|
| position | :691 | `segmentPosition = fromPosition + event->position` (beats) |
| bounds | :694 | skip if `< 0` or `>= segment->total` |
| duration | :696 | `min(event->duration, toPosition - segmentPosition)` |
| chord/voicing | :697-701 | `getChordAt(pos)` (`Fabricator.cpp:137-154`); `chooseVoicing(chord, type)` (`Fabricator.cpp:854-879`) picks **uniformly at random** among voicings of that type and chord with valid notes, using its own fresh `random_device` |
| volume ratio | :703-704 | `computeVolumeRatioForPickedNote` (:793-799): 1.0 unless `deltaArcEnabled`, then `inBounds(deltaIn, deltaOut, segment->delta + pos) ? 1 : 0`; `<= 0` skips the event |
| notes | :707-711 | voicing present → `pickNotesForEvent`; else `{"ATONAL"}` if `defaultAtonal` (Beat passes `true`, Detail `false`) else nothing |
| start | :714 | `getSegmentMicrosAtPosition(tempo, pos)` |
| length | :715-719 | `nullopt` if `isOneShot(instrument, trackName)` (`Fabricator.cpp:922-925`: `isOneShot && !oneShotObserveLengthOfEventsContains(track)`), else `micros(pos + duration) - start` |
| per note | :722-731 | `pickInstrumentAudio(note, ...)` (:873-903): `amplitude = event->velocity * volRatio` :899; `tones = isTonal ? note : "X"` :900 (`Note::ATONAL = "X"`, `src/music/Note.cpp:44`); `programSequencePatternEventId`, `segmentChordVoicingId` recorded |

One-shot cutoff pass (`finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks` :734-791): only for instruments with `isOneShot` and `isOneShotCutoffEnabled` (`InstrumentConfig` defaults `false`/`true`, `InstrumentConfig.cpp:18-19`) whose type is in `instrumentTypesForAudioLengthFinalization`; each zero-length pick is cut at the next pick start of the same choice (:766-780), else at segment end (:782-787), else deleted (:789).

### 5.3 Chord-mode picks (`Craft.cpp:488-565`)

Per `computeSections()` section: `selectChordPartInstrumentAudio(instrument, Chord::of(chord->name))` (:937-951; persistent cache keyed by `instrument->id` + chord name) → `selectNewChordPartInstrumentAudio` (:280-297): phase 0 if `Chord::of(audio->tones) == chord`, phase 1 if `isAcceptable` (= same root and description, `src/music/Chord.cpp:259-261`). Pick :550-560: `startAtSegmentMicros` at `fromPos`, `lengthMicros` to `toPos` unless one-shot, `tones = chord->name`, `event = toEvent(type name)`, `amplitude = volRatio`.

### 5.4 Loop, Background, Transition

See 1.2, 1.4, 1.3. All three compute length directly from `getTotalSegmentMicros()` or `loopBeats * microsPerBeat`. No path reads `InstrumentAudio::tempo` for scheduling, and no pick carries a stretch or pitch field (`SegmentChoiceArrangementPick` fields: `include/xjmusic/segment/SegmentChoiceArrangementPick.h:18-26` = `segmentChoiceArrangementId, segmentChordVoicingId, instrumentAudioId, programSequencePatternEventId, startAtSegmentMicros, lengthMicros, amplitude, tones, event`).

## 6. NotePicker, RankedNote, and the "no sample has these tones" path

### 6.1 Preparing the pick (`Craft::pickNotesForEvent`, `Craft.cpp:801-871`)

| Quantity | Line | Source |
|---|---|---|
| `segChord` | :809 | `Chord::of(segmentChord->name)` |
| `dpKey` | :810 | `getKeyForChoice` (`Fabricator.cpp:288-303`): sequence key if present else program key |
| `dpRange` | :811 | `getProgramRange(programId, type)` (`Fabricator.cpp:519-529`, cached) |
| `voicingListRange` | :812 | `getProgramVoicingNoteRange(type)` (:597-615): range over **all** segment voicings of that type |
| `dpTransposeSemitones` | :815 | `getProgramTargetShift(type, dpKey, segChord)` (:558-577): Bass → `Step::delta(fromRoot, toChord.slashRoot or root)`; others → root to root; 0 if the key chord has no root |
| octave shift | :817-818 | `12 * getProgramRangeShiftOctaves(type, sourceRange, voicingListRange)` (:531-556): Bass → `computeLowestOptimalRangeShiftOctaves`; Pad/Stab/Sticky/Stripe → `NoteRange::computeMedianOptimalRangeShiftOctaves`; **Drum and every other type (Hook, Percussion, Background, Transition) → 0** via `default:` |
| `eventNotes` | :821-827 | CSV `event->tones`, each shifted, then **sorted** |
| optimal range | :829-835 | expanded once from the first event's range; carried across events (:866-867) |
| `bun` | :838 | `getStickyBun(event->id)` |
| `voicingNotes` | :841-846 | `Fabricator::getNotes(voicing)` = `CsvUtils::split(voicing->notes)` (:424-428), authored order, only `Note::isValid` entries |
| `notePicker` | :847-850 | `NotePicker(optimalRange shifted, voicingNotes, seekInversions = instrumentTypesForInversionSeekingContains(type))` |
| per event note | :854-858 | `isAtonal && bun` → `bun.compute(voicingNotes, i)`; else `notePicker.pick(eventNote)` |

### 6.2 `NotePicker::pick` (`src/fabricator/NotePicker.cpp:23-57`)

1. `noteInAvailableOctave = voicingRange.toAvailableOctave(eventNote)` (:24; `src/music/NoteRange.cpp:176-193` shifts by octaves, up to `MAX_SEEK_OCTAVES`, until inside the voicing's low..high).
2. If the pitch class is `Atonal` → `pickRandom(voicingNotes)` (:28-29; :107-111 → `MarbleBag::quickPick`).
3. Else build `RankedNote(vN, |vN.delta(noteInAvailableOctave)|)` for every remaining voicing note (:31-35; `RankedNote` is a plain `{Note, int delta}` pair, `src/fabricator/RankedNote.cpp`), take `std::min_element` (:38-40; ties resolve to the first in `std::set<Note>` order, i.e. the lowest note), then `seekInversion` (:45).
4. `seekInversion(source, targetRange, options)` (:74-104): no-op unless `seekInversions`; if `source > targetRange.high` return the option `<= high` nearest to `high`; else if `source < targetRange.low` return the option `>= low` nearest to `low`; else `source`.
5. `targetRange.expand(picked)` (:52) and `removePicked` (:53, :65-71): **each voicing note may be used at most once per event**. If nothing could be picked, returns a default-constructed `Note{}` (:56).

Tests: `test/fabricator/NotePickerTest.cpp:136-151` (`picking_4_5_notes_*`, `picking_6_notes`).

### 6.3 From note to audio, and what happens when nothing matches

`Craft::pickInstrumentAudio(note, ...)` (:873-903) → `isMultiphonic` (`InstrumentConfig` default `false`, `InstrumentConfig.cpp:17`) ? `selectMultiphonicInstrumentAudio` : `selectMonophonicInstrumentAudio`. `nullopt` → the pick is **silently skipped** (:887).

- **Multiphonic** (:905-921 → :974-1010): candidates are audios with non-empty `tones` where `a.isAtonal() || b.isAtonal() || a == b` (:979-986); `Note::operator==` compares **pitch class and octave** (`src/music/Note.cpp:19-21`), so a C3 sample never serves a C4 request. If none, a Warning `SegmentMessage` is written through `reportMissing` (`src/fabricator/FabricationWrapper.cpp:24-31`: `"EntityUtils not found! instrumentId=…, searchForNote=…, availableNotes=…"`) and `nullopt` is returned (:988-1006). Among matches, uniform `quickPick` (:1008). **There is no nearest-note, no octave-fold, and no pitch-shift fallback.** Persistence: `getPreferredAudio(event->programVoiceTrackId, note)` (:910-917).
- **Monophonic** (:923-935 → :953-972): scores every audio of the instrument: Drum → 300 if `getTrackName(event) == audio->event` else 0; others → 100 if `Note::of(audio->tones) == Note::of(event->tones)` else 0 (**the raw event tones, not the picked voicing note**); `ValueUtils::getKeyOfHighestValue` (`src/util/ValueUtils.cpp:144-153`) returns the first maximum in `UUID` order, so when nothing matches the lowest-UUID audio plays. It never returns `nullopt` if the instrument has any audio; the persistent path throws `FabricationException("Unable to select note event instrument audio!")` only when the instrument has no audio at all (:928).
- **Preferred-audio cache**: `Fabricator::preferredAudios` is seeded at construction from the retrospective's picks (`Fabricator.cpp:42`, `computePreferredInstrumentAudio` :1184-1199, key `computeCacheKeyForVoiceTrack(pick)`), so with `isAudioSelectionPersistent = true` (default) an audio chosen for a (track, note) pair persists for the life of the main program.

### 6.4 StickyBun (`include/xjmusic/music/StickyBun.h`, `src/music/StickyBun.cpp`)

- Static process-wide generator: `std::random_device StickyBun::rd; std::mt19937 StickyBun::gen(rd()); MAX_VALUE = 100; uniform_int_distribution<> distrib(0, 99)` (:14-17). `StickyBun(eventId, size)` draws `size` values (:21-26).
- `compute(voicingNotes, index)` (:54-63): `ratio = values[min(index, size-1)] / 100`; returns `voicingNotes[floor((n-1) * ratio)]` clamped, in the **authored voicing order** (not pitch-sorted). `replaceAtonal` (:40-51) is unused by craft; craft calls `compute` directly (`Craft.cpp:856`).
- Persistence (`Fabricator::getStickyBun` :695-750): disabled → `nullopt`; else read `SegmentMeta` key `"StickyBun_<eventId>"` (:33-37) from the current segment, then from the retrospective (`getPreviousMeta`, `SegmentRetrospective.cpp:164-172`, searching all segments of the current main program's run), else create a new bun sized by the event's tone count and `putStickyBun` (:684-693) as `SegmentMeta{key, value = {"eventId","values"} JSON}` (:71-85). Buns therefore survive for a main program's run and are re-rolled at NextMain/NextMacro (because the retrospective window resets). Deserialize failures are Error messages, not fatal (:710, :724).

## 7. Delta arcs (layer in/out automation), as implemented

`Craft::precomputeDeltas` (`Craft.cpp:54-132`):

- `deltaArcEnabled == false` (the default) → every layer gets `DELTA_UNLIMITED` (`SegmentChoice.h:19`, value `-1`) (:65-71), and `computeVolumeRatioForPickedNote` returns 1.0 (:794). Everything below is dormant by default.
- Initial/NextMain/NextMacro (:79-119): `deltaUnits = Bar::of(mainBarBeats).computeSubsectionBeats(total)` (:85-86; `src/music/Bar.cpp:24-29` with `FACTORS_TO_TEST = {3,4,5,7}` :11 and `ValueUtils::subDiv` :116-124). Layers whose lowercased name contains a prioritization search term go to `priLayers` (:92-99). **(defect)** `:98 secLayers.emplace_back();` pushes an **empty string** instead of `layer`. Three `std::shuffle` calls on the Craft instance's `gen` (:101, :104, :110). Initial `delta = roundToNearest(deltaUnits, quickPick(deltaUnits*4) - deltaUnits*2*numLayersIncoming)` (:112-113); per layer `deltaIn = delta > 0 ? delta : UNLIMITED`, `deltaOut = UNLIMITED`, `delta += roundToNearest(deltaUnits, quickPick(deltaUnits*5))` (:114-118).
- Continue (:121-130): copies `deltaIn/Out` from retrospective choices where `choiceIndexProvider.get(choice) == index`.
- **(defect, slicing)** `Craft.h:93` declares `ChoiceIndexProvider choiceIndexProvider = {};` **by value**, and `Craft.cpp:60` copy-assigns the lambda provider into it, slicing it to the base class whose `get` returns `""` (:1030-1032). Consequently `computeDeltaIn/Out` (:584-592) always look up key `""`, which only the empty-named secondary layers populate; every fresh choice shares one delta, and the Continue branch never matches a named layer. The only test, `CraftTest.cpp:100-115 PrecomputeDeltas`, has **no assertions**. The stages themselves copy deltas from prior choices directly (`BeatCraft.cpp:70-71`, `Craft.cpp:464-465, :503-504`), which masks the Continue-branch problem.
- Consumers: `isIntroSegment/isOutroSegment/isSilentEntireSegment/isActiveEntireSegment` (:14-33) are public but have **no callers in `src/` or `include/`** outside their own definition (verified by grep); only `test/craft/CraftTest.cpp:117-126` exercises them.

## 8. Every RNG construction site in `engine/src` and `engine/include`

Verified with `grep -rnE "random_device|mt19937|\brand\(|srand\(|std::shuffle|uniform_(int|real)_distribution|<random>" src include`. There is **no** `rand()`/`srand()` anywhere.

| # | Site | Kind | Scope | Draws |
|---|---|---|---|---|
| 1 | `include/xjmusic/craft/Craft.h:417-418` | `std::random_device rd; std::mt19937 gen{rd()}` | per `Craft` instance (5 per segment) | `std::shuffle` at `src/craft/Craft.cpp:101, :104, :110` (delta arcs only) |
| 2 | `include/xjmusic/fabricator/MarbleBag.h:33-34` | per-instance `rd`/`gen` | per bag (one per choose call) | `MarbleBag.cpp:145-146` `pickPhase` |
| 3 | `src/fabricator/MarbleBag.cpp:167-168` | fresh `rd`+`gen` **per call** | `quickPick` | :169-170 |
| 4 | `src/fabricator/MarbleBag.cpp:176-177` | fresh `rd`+`gen` **per call** | `quickBooleanChanceOf` | :178-179 |
| 5 | `src/fabricator/Fabricator.cpp:874-875` | fresh `rd`+`gen` **per call** | `chooseVoicing` | :876-878 |
| 6 | `src/music/StickyBun.cpp:14-15` (decl `StickyBun.h:22-23`) | **static** process-wide | `StickyBun(eventId,size)` | :17 distrib, :24 |
| 7 | `src/util/ValueUtils.cpp:33-34` (decl `ValueUtils.h:23-24`) | **static** process-wide | `randomFrom` :82-94, `withIdsRemoved` :161-171 | **no callers in `src/`** (grep for `ValueUtils::randomFrom|withIdsRemoved` outside `ValueUtils.cpp` is empty): dead in the craft path |

The proposal (`02-where-v1-stands.md:33, :71`, `04-engine.md` §5) counts four generators in four files; the truth is **seven construction sites in five files**, and `Craft.h` is the file the proposal misses. `quickPick` callers: `Craft.cpp:112, :117, :1008`, `Fabricator.cpp:629, :642`, `NotePicker.cpp:110`. `quickBooleanChanceOf` caller: `Craft.cpp:381`.

Secondary nondeterminism that a seed alone will not fix: iteration over `std::set<const Program*>` / `std::set<const Instrument*>` (pointer order) when building bags (`Craft.cpp:172, :179, :207, :213`) does not affect the *distribution* (bag contents are keyed by UUID in a `std::map`), but `std::set<const InstrumentAudio*>` order feeds `std::sort` by intensity (:405-409), whose tie order among equal-intensity audios is pointer order, which changes bag membership per band. `std::min_element` ties in `NotePicker` and `getKeyOfHighestValue` are deterministic (set/UUID order).

## 9. Where a "why this / why excluded" diagnostic hook attaches

| Decision | Candidate list exists at | Exclusion points (today: silent unless noted) | Recorded today |
|---|---|---|---|
| Beat/Detail program | `Craft.cpp:159-165` (`candidates`) | `:161` no voice of type; `:175`/`:182` `!iso.isAllowed`; not bound and not published (falls through both phases) | none |
| Instrument | `Craft.cpp:195-200` | `:197` `instrumentContainsAudioEventsLike`; `:209`/`:215` meme; unbound+unpublished | none |
| Choice stored | `Fabricator.cpp:1217-1221` | meme stack theorem at put time | **Error SegmentMessage** (text at :1220) |
| MarbleBag draw | `MarbleBag.cpp:128-154` (`blocks`, `total`, `pickIdx`) | empty phase skipped `:139-140` | none; `toString()` :87-106 exists |
| Sequence / pattern | `Fabricator.cpp:619-626`, `:651-657` | none | none |
| Voicing | `Fabricator.cpp:859-866` (`validVoicings`) | `:868-870` none valid → event becomes ATONAL or nothing (`Craft.cpp:707-711`) | none |
| Voicing note | `NotePicker.cpp:31-40` (`rankedNotes`) | none | none |
| Audio for note | `Craft.cpp:979-986` (`filteredAudios`) | `:988-1006` no tone match | **Warning SegmentMessage** via `reportMissing` |
| Audio for event (monophonic) | `Craft.cpp:955-964` (`score` map) | never excludes; worst case arbitrary audio | none |
| Chord audio | `Craft.cpp:284-291` | neither exact nor acceptable | none |
| Intensity band | `Craft.cpp:405-424` (`sorted`, `bags`) | empty band `:428` | none |
| Event dropped | `Craft.cpp:694` (out of range), `:704`/`:547` (delta arc volume 0), `:887` (no audio) | — | none |
| Stage skipped | `BeatCraft.cpp:21-23, :81-83`; `DetailCraft.cpp:54-56, :70-72`; `TransitionCraft.cpp:25-27`; `BackgroundCraft.cpp:18-20` | no program/instrument found | none |
| Mute roll | `Craft.cpp:380-382` | — | result stored in `SegmentChoice::mute` |
| Unsupported mode | `DetailCraft.cpp:86-90` | — | Warning (unreachable today) |

`SegmentMeta` (`include/xjmusic/segment/SegmentMeta.h:19-20`: `key`, `value`) is already the engine's free-form per-segment record and is how StickyBun persists; `Fabricator::put(const SegmentMeta&)` (`Fabricator.cpp:999-1002`) is available to every stage. The `barBeats` candidate filter from Q14 (`design/v2-proposal/11-resolutions.md:367-385`) belongs in the `chooseFreshProgram` candidate loop at `Craft.cpp:159-165`, comparing `program->config.barBeats` with `fabricator->getCurrentMainProgramConfig().barBeats` (`Fabricator.cpp:332-347`; `ProgramConfig` default `barBeats = 4`, `cutoffMinimumBars = 2`, `src/content/ProgramConfig.cpp:12-16`).

## 10. Implications for V2 specs

**Seeded randomness (04-engine §5)**

1. There are seven RNG construction sites in five files, not four (section 8). `Craft.h:417-418` must be on the list, and the three per-call `random_device` constructions (`MarbleBag.cpp:167, :176`, `Fabricator.cpp:874`) are the ones that make a seed impossible today; they also pay the OS-entropy cost on every choice. The static generators in `StickyBun` and `ValueUtils` are process-global and would leak state across two engine handles in one process (the follow-mode test in 04-engine §11 runs two engines in one process).
2. `MarbleBag` is value-copied (`MarbleBag.h:52-57`, `.cpp:182-184`) and the copy does not carry the generator; a threaded-generator design should pass a `std::mt19937_64&` (or an index into the chain generator) into `pick`, not store it in the bag. `std::vector<MarbleBag> bags(layers)` at `Craft.cpp:413` constructs `layers` separate generators today.
3. The per-segment sub-seed the proposal wants ("same future had the cue been there from the start") must also cover `chooseVoicing`, `StickyBun` creation and `computeMute`, each of which draws outside `MarbleBag`.
4. Byte-identical determinism also needs stable iteration: `std::sort` by intensity over pointer-ordered sets (`Craft.cpp:405-409`) should tie-break on `id`. `std::set<const T*>` iteration elsewhere does not change outcomes because bags are keyed by UUID.

**Choice diagnostics (04-engine §8)**

5. Two exclusion gates exist, not one: bag-time `iso.isAllowed` (silent) and put-time `MemeStack` (Error message with constellation). A diagnostics spec should either record both or move the put-time check earlier, since a candidate can pass gate 1 and fail gate 2 after earlier voices add memes.
6. `MarbleBag::pickPhase` is the single choke point where candidates, phases, weights and the winning index are all in scope (`MarbleBag.cpp:128-150`); `toString()` already renders the bag. Recording there plus one "excluded because" record per silent `continue` in section 9 covers every decision the UI could ask about.
7. Weight semantics to document in the UI: weight = 1 + shared-meme count; phase 1 beats phase 2 absolutely, so a directly bound program with zero shared memes always beats a published program with five.
8. Two existing silent-failure modes deserve first-class diagnostics: Transition and Background instruments are chosen once and inherited forever across NextMain/NextMacro (section 1.3); and monophonic non-Drum instruments play the lowest-UUID audio when nothing matches the event tones (section 6.3).

**barBeats candidate filtering (Q14)**

9. The filter attaches at `Craft.cpp:159-165` for programs. Instruments have no `barBeats`; Loop-mode audios have `loopBeats`, which is the analogous field for Detail Loop / Background. Today `Bar::computeSubsectionBeats` (`Bar.cpp:24-29`) is the only consumer of `barBeats` in the layer stages (`Craft.cpp:85`, `TransitionCraft.cpp:85`), so a mismatched beat program is not currently rejected anywhere, confirming the proposal's claim.

**Pitch-shift NotePicker (04-engine §9)**

10. The "nearest sample" fallback must live in `selectNewMultiphonicInstrumentAudio` (`Craft.cpp:974-1010`), which is the only place the engine knows it has no exact-tone audio; it currently warns via `reportMissing` and drops the pick. Monophonic instruments never reach a "no audio" state (section 6.3), so `allowPitchShift` on a monophonic instrument would need to change the scoring in `selectNewNoteEventInstrumentAudio` (:953-972), which compares the **raw event tones**, not the picked voicing note.
11. `Note::operator==` is pitch class **and** octave (`Note.cpp:19-21`); a shift spec must decide whether octave-equivalent samples count as "exact" (today they do not) before counting semitone distance.
12. `pick.tones` is already `"X"` for non-tonal instruments (`Craft.cpp:900`); the schedule event's `pitch_shift_semitones` needs the picked note *and* the audio's tones, both available at `Craft.cpp:873-903`.
13. The octave-shift policy table (`Fabricator.cpp:538-552`) sends Hook, Percussion, Background and Transition to the Drum branch (shift 0). A spec that adds tonal Hook instruments should state the intended branch.

**Layer-mute cue (04-engine §6, 08-performer-input)**

14. `quickBooleanChanceOf` throws for probability `>= 1` (`MarbleBag.cpp:174-175`). "Set mute probability to 1" must either clamp (`>= 1 → true`) or bypass the roll. The getter `getChoiceMuteProbability` (`TemplateConfig.cpp:322-328`) is the natural override point since every stage goes through it.
15. Mute is re-rolled per segment and never inherited (`BeatCraft.cpp:58` before the prior-choice branch), so a probabilistic mute is a flicker, not a state. A cue wants a deterministic per-type flag, separate from the probability.
16. Because `DubWork.cpp:120` reads `choice->mute` live from the store each frame, flipping the stored `SegmentChoice::mute` on already-crafted, not-yet-dubbed segments takes effect without a re-craft. That is a cheaper landing path than the whole-future rewrite, and it is also how an un-mute can be immediate for crafted segments (the picks still exist).

**Intensity and layers (05-workstation, 08-performer-input)**

17. "Intensity layers" in craft means one audio per intensity band for the whole segment; thresholds and the current intensity act only at dub as a per-frame gain (`DubWork.cpp:162-171`, `AudioMathUtils.cpp:5-12`, `fadeOutLowerIntensities` hard-coded `false`). A spec describing "intensity gates layers" should say "dub fades audios whose intensity exceeds the current intensity by up to `intensityThreshold`"; lower-intensity audios are never faded.
18. `getIntensityLayers` returns 0 for a type absent from the map and `selectAudioIntensityLayers` then indexes an empty vector (`Craft.cpp:413-424`). Any V2 template schema should require all ten types or fix the fallback to 1.
19. `isAudioSelectionPersistent` (default true) makes Background, Loop and Transition audio sets sticky for a main program's run (`Craft.cpp:299-310`, `TransitionCraft.cpp:104-112`), so intensity changes mid-program never change *which* audios were picked, only their gain.

**Delta arcs**

20. Delta arcs are off by default and, when on, degenerate because of two defects (`Craft.cpp:98` empty-string layer; `Craft.h:93`/`Craft.cpp:60` provider slicing), with an assertion-free test. Any V2 feature that leans on layer in/out automation (a "build" move, a staged drop return) should first fix and test this rather than assume the Java behavior survived the port.

**Follow mode**

21. Confirmed direct main-program reads in the layer stages: `Craft.cpp:85` and `TransitionCraft.cpp:85` (`barBeats`), `TransitionCraft.cpp:45-51` (`getCurrentMainSequence`/`getPreviousMainSequence`). Everything else the four stages need (tempo, chords, voicings, memes, `total`, `delta`, `durationMicros`) is read from the segment store through `Fabricator`. `getPreviousMainSequence` on a follower implies the foundation record must carry a stable main-sequence identity across consecutive segments or `isMediumTransitionSegment` throws.

**Stretch (G7)**

22. `DetailCraft::craftLoopParts` (`DetailCraft.cpp:113-135`) is the one place loop audio is scheduled; `audio->loopBeats` is read, `audio->tempo` is not. A `stretch_ratio = segmentTempo / audio.tempo` can be computed there with no other change, and Background picks (`BackgroundCraft.cpp:40-51`) have no beat length at all, so stretch does not apply to them.
