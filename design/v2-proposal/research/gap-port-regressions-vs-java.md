# Gap map: C++ 1.9.6 defects, sorted into port regressions vs inherited V1 behavior

Source of truth: `/Users/nck/xj/xjmusic` at commit `a94000b9a`. C++ paths are under `engine/` (version `1.9.6`, `engine/CMakeLists.txt:6`); Java paths are under `workstation/src/main/java/io/xj/` (the original `io.xj.engine` the C++ was ported from in `be1cbae2e`, 2024-06-25, "Engine Craft in C++ (#405)"). Every line number is from `grep -n`/`sed -n` on that tree. Nothing was run; the one runtime path recommended in section 6 is described, not executed, because no `engine/build` exists on this machine.

Why this matters for the proposal: `04-engine.md:13` promises seeded randomness is "behavior-preserving by default", and `04-engine.md:224` promises "Engine 2.0 reads every 1.9.x project unchanged". Neither statement can be written into a spec without knowing *which* 1.9.6 behavior is the reference. A defect that the Java original also has is V1 behavior and content authors have tuned against it; a defect introduced by the port is something the Java workstation never did, and the demo packs were authored on Java. The disposition column below is the recommendation; the Java column is the evidence.

The earlier maps already found these by reading (`engine-api-lifecycle.md`, `engine-craft-stages-choice.md`, `engine-fabricator-foundation.md`, `engine-content-config.md`). This map does not repeat their descriptions; it adds the Java comparison, the git provenance, and three divergences the earlier maps did not notice (intensity computed from the stale delta, Java's two-decimal floor on intensity, and the third shuffle that defeats layer prioritization).

---

## 1. Summary table

Legend for the class column: **REGRESSION** = Java does it right, C++ does not; **INHERITED** = Java has the identical behavior; **C++-ONLY** = the code path has no Java counterpart (the Java architecture differs), so there is nothing to inherit from.

| # | Defect (C++ 1.9.6) | C++ evidence | Java behavior | Class | V2 disposition |
|---|---|---|---|---|---|
| 1 | `persistenceWindowSeconds` stored as micros, window = 3,600 µs | `CraftWork.h:45` param `persistenceWindowSeconds`; `CraftWork.cpp:20,28` names it `persistenceWindowMicros` and stores unconverted; fed from `WorkManager.cpp:12` `config.persistenceWindowSeconds` (default 3600, `WorkSettings.h:20`) | `CraftWorkImpl.java:101` `persistenceWindowMicros = persistenceWindowSeconds * MICROS_PER_SECOND` | REGRESSION | Fix in 2.0 (multiply). Must land together with #5. |
| 2 | `delta` never advances; stays 0 for the chain | `MacroMainCraft.cpp:76` sets `updatedSegment.total`, `:81` reads `segment->total` (the shell, `int total{}` `Segment.h:40`, never set by `CraftWork::buildSegmentFollowing` `CraftWork.cpp:415-425`) | `MacroMainCraftImpl.java:112` `segment.setTotal(...)` then `:117` `segment.setDelta(segment.getDelta() + segment.getTotal())` on the same object | REGRESSION | Fix in 2.0. See §2 for the test-level proof. |
| 2b | Intensity computed from the *previous* delta even where delta did change | `MacroMainCraft.cpp:86` `computeSegmentIntensity(segment->delta, ...)` (old value) | `MacroMainCraftImpl.java:122` `computeSegmentIntensity(segment.getDelta(), ...)` after `setDelta` (new value) | REGRESSION | Fix with #2. |
| 2c | Intensity not floored to two decimals | `MacroMainCraft::computeSegmentIntensity` returns raw `float` | `MacroMainCraftImpl.java:216` `ValueUtils.limitDecimalPrecision2(...)` = `Math.floor(v*100)/100` (`ValueUtils.java:189-191`) | REGRESSION (cosmetic) | Decide explicitly; it changes every stored `intensity` and golden fixtures. |
| 3 | `ProgramVoice`/`ProgramVoiceTrack` read JSON keys `duration`/`tones` instead of `name`/`order` | `ProgramVoice.h:40-41`, `ProgramVoiceTrack.h:33-34`; introduced `c804d29c6` (2024-07-05, #417) | Java writes and reads `name`/`order` (`ProgramVoice.java:19-20`) | C++-ONLY (JSON loader is new code) | Fix in 2.0; add a loader test against a workstation-exported project. |
| 4 | `SegmentEntityStore::put` of an existing child entity is a no-op; pick trimming in `doCutoffLastSegment` is discarded | `SegmentEntityStore.cpp:16` `STORE[segmentId].emplace(id, entity)`; `CraftWork.cpp:351-353` copies the pick, shortens, `store->put(updatePick)` | `SegmentEntityStoreImpl.java:107` `ConcurrentHashMap.put(id, entity)` **overwrites**; also `pick.lengthMicros(...)` is a fluent in-place setter (`SegmentChoiceArrangementPick.java:146-148`) on the stored instance, so Java trims even without the put (`CraftWorkImpl.java:475`) | REGRESSION | Fix in 2.0: `insert_or_assign`. Audit every C++ re-`put`. |
| 5 | Dub intensity taken from `segments.at(0)` | `DubWork.cpp:106-113` | `DubWorkImpl.java:228-232` `segments.get(0).getIntensity()`; both lists come from `getSegmentsIfReady`, which prepends the previous Crafted segment (`CraftWorkImpl.java` `getSegmentsIfReady`; `CraftWork.cpp:57-76`) | INHERITED | Preserve the rule or deliberately change it; but note the interaction with #1 (§3.5). |
| 6 | `Fabricator::getMicrosPerBeat(tempo)` caches on first call and ignores later `tempo` | `Fabricator.cpp:1036-1041` `if (0 == microsPerBeat)`; member `Fabricator.h:907` | `FabricatorImpl.java:927-931` identical (`if (Objects.isNull(microsPerBeat))`) | INHERITED | Preserve; a Fabricator lives for one segment. Tempo-as-input (04 §2) must set tempo before the Fabricator is built. |
| 7 | `MarbleBag::quickBooleanChanceOf` throws for `p >= 1` | `MarbleBag.cpp:173-180` `if (probability < 0 \|\| probability >= 1) throw`; compares `distrib(gen) < probability` | `TremendouslyRandom.java:30-32` `nextDouble(0,1) <= probability`; never throws; `p=1` always true | REGRESSION | Fix in 2.0: accept `[0,1]`. The Layer Mute cue (`04-engine.md:178`, "sets that type's choice mute probability to 1") throws in 1.9.6. |
| 8 | TransitionCraft bins audios with `getIntensityLayers(Background)` | `TransitionCraft.cpp:122` | `TransitionCraftImpl.java:160` same `InstrumentType.Background` | INHERITED | Preserve (both default to 3). Document as V1 quirk. |
| 8b | Transition picks get `lengthMicros = getTotalSegmentMicros()` even mid-segment | `TransitionCraft.cpp:75-92` | `TransitionCraftImpl.java:117-132` identical | INHERITED | Preserve. |
| 8c | Pick `event` label is the alphabetically-first name, not the first configured | `TransitionCraft.cpp:11-13,75-93` `*largeNames.begin()` on `std::set` (`TemplateConfig.h:31`) → `"BIG"` | `TransitionCraftImpl.java:30-41,117` `largeNames.get(0)` on `List` (`TemplateConfig.java:119`) → `"LARGE"` | REGRESSION (cosmetic) | Fix or document; it is visible in the segment store and in `isAudioSelectionPersistent` matching by `event`. |
| 9a | `precomputeDeltas` pushes an empty string instead of the secondary layer | `Craft.cpp:98` `secLayers.emplace_back();` | `CraftImpl.java:391` `secLayers.add(layer)` | REGRESSION | Fix in 2.0. |
| 9b | `ChoiceIndexProvider` sliced to the base, `get` returns `""` | `Craft.h:93` by-value member; `Craft.cpp:60` copy-assign; base `get` `Craft.cpp:1030-1032` | `CraftImpl.java:66` interface field, `:357` assigned by reference | REGRESSION | Fix in 2.0 (`std::function` or pointer). |
| 9c | Prioritized layers re-shuffled with the rest | `Craft.cpp:110` `std::shuffle(orderedLayers...)` after concatenation | `CraftImpl.java:396` `Stream.concat(priLayers, secLayers)`, **no** third shuffle | REGRESSION | Fix in 2.0; `deltaArcBeatLayersToPrioritize` is a no-op in 1.9.6. |
| 9d | Delta-arc test has no assertions | `CraftTest.cpp:100-115` | `CraftImplTest.java:67-71` **also** has no assertions | INHERITED (test gap) | 2.0 needs a real test; neither suite would have caught 9a-9c. |
| 10 | `getAllMacroPrograms` comparator returns `int` as `bool` | `WorkManager.cpp:121-128` `return a->name.compare(b->name);` | `FabricationManagerImpl.java:203-207` `Comparator.comparing(Program::getName)` | REGRESSION (UB) | Fix in 2.0: `a->name < b->name`. No test in `engine/test`. |
| 11 | `chooseVoicing` picks at random with a fresh unseeded generator, no caching | `Fabricator.cpp:854-879` `random_device`+`mt19937` per call | `FabricatorImpl.chooseVoicing` filters identically and `MarbleBag.quickPick` (ThreadLocalRandom); no caching | INHERITED (random in both) | Route through the chain generator (04 §5 names it). |
| 12 | `getChordAt` accepts the first chord unconditionally | `Fabricator.cpp:137-155` `foundPosition < 0` | `FabricatorImpl.java:251-267` `Objects.isNull(foundPosition)` identical; Java additionally memoizes per position (`chordAtPosition`, `:94,143,264`); C++ declares the cache (`Fabricator.h:884`) and never uses it | INHERITED | Preserve. Caching is perf-only. |
| 13 | `readChain()` returns an engaged optional holding `nullptr` | `SegmentEntityStore.cpp:93-95` `chain.has_value() ? &chain.value() : nullptr`; guards at `Fabricator.cpp:32-34` and `:1146-1150` never fire | `SegmentEntityStoreImpl.java:117-119` `Optional.ofNullable(chain)` | C++-ONLY | Fix in 2.0 (`std::nullopt`). Only reachable through API misuse; matters for C ABI error codes. |
| 14 | `activeAudioMap.emplace` on update never refreshes the record | `WorkManager.cpp:83-93` | No counterpart: Java has no `AudioScheduleEvent` diff (`grep -rln AudioScheduleEvent workstation/src/main/java` → none); `DubWorkImpl` pushes to `MixerImpl` directly | C++-ONLY | Fix in 2.0 (`insert_or_assign`). |
| 15 | `selectAudioIntensityLayers` with `layers == 0` | `Craft.cpp:418-424` `static_cast<int>(ceil(n/0.f))` is UB; `bags[i / marblesPerLayer]` on empty vector | `CraftImpl.java:823-826` `(int) Math.ceil(n / 0.0)` = `Integer.MAX_VALUE`, then `bags.get(0)` on an empty list throws `IndexOutOfBoundsException` | INHERITED (both fail) | Validate `intensityLayers` covers all 10 types at template load. |

Items 1, 2, 3 were established before this map; 2b, 2c, 8c, 9c are new. The Java-side counterparts of 5, 6, 8, 8b, 11, 12, 15 were not previously checked and all turn out to be inherited.

---

## 2. The delta regression, proved by the two test suites disagreeing

Both repositories carry a `CraftFoundationContinueTest` built on the same fixture, "Chain Test Print #1 has 5 total segments", with a planned `segment4` of key `"C"`, `total 4`, `intensity 1.0`, `tempo 120`, and `delta 0`:

- Java: `workstation/src/test/java/io/xj/engine/craft/macro_main/CraftFoundationContinueTest.java:116-125`, through `SegmentFixtures.java:343-346`, which passes `delta = 0` to the 11-argument overload (`:350-360`).
- C++: `engine/test/craft/macro_main/CraftFoundationContinueTest.cpp:86-95`, through `test/_helper/SegmentFixtures.cpp:69-82` (`segment.delta = delta` at `:100`).

Both run `macroMain(...).doWork()` on that segment and then assert `total == 32` (the main sequence length). Then:

- Java asserts `assertEquals(0.23, result.getIntensity(), 0.001)` (`CraftFoundationContinueTest.java:138`).
- C++ asserts `ASSERT_NEAR(0.2, result->intensity, 0.001)` (`CraftFoundationContinueTest.cpp:108`).

Same fixture, same algorithm, different expected number. The arithmetic: Java sets `total = 32` first (`MacroMainCraftImpl.java:112`), then `delta = 0 + 32` (`:117`), then intensity from the new delta (`:122`): `0.2 + (0.8 - 0.2) * (32/280) * programmaticIntensity`, floored to two decimals = `0.23`. C++ computes `delta = 0 + segment->total` where `segment->total` is the fixture's `4` (`MacroMainCraft.cpp:81`), but then intensity from `segment->delta = 0` (`:86`), so position is 0 and intensity is exactly the floor `0.2`. The C++ test was written to match the port's output rather than the original's.

In production the shell segment's `total` is `0`, not `4` (`CraftWork::buildSegmentFollowing` sets `chainId`, `id`, `state`, `beginAtChainMicros`, `type`, `delta = last->delta`, `CraftWork.cpp:415-425`; `total` keeps the `int total{}` default of `Segment.h:40`), so `delta` is `0 + 0` on every Continue segment and the chain never leaves `0`. Two consequences, both regressions from Java:

1. `intensity == intensityAutoCrescendoMinimum (0.2)` for the whole chain whenever `intensityAutoCrescendoEnabled` (default `true`, `TemplateConfig.cpp:47-49`). The Java workstation produces a crescendo from 0.2 toward `0.2 + 0.6 * programmatic` over `mainProgramLengthMaxDelta = 280` beats.
2. The main-program length guard `getTemplateConfig().mainProgramLengthMaxDelta > getPreviousSegmentDelta()` (`Fabricator.cpp:1166`, Java `FabricatorImpl.java:1037`) is always true, so a main program only ends when its binding offsets run out. Java ends it after 280 beats of delta.

The formulas themselves agree. Java `interpolate(floor, ceiling, position, multiplier) = floor + (ceiling - floor) * position * multiplier` (`ValueUtils.java:430-432`); C++ `interpolate(min, min + (max - min) * programmatic, position)` with `interpolate(floor, ceiling, position) = floor + (ceiling - floor) * position` (`MacroMainCraft.cpp` `computeSegmentIntensity`; `ValueUtils.cpp:133-135`) expands to the same expression. The only formula-level difference is Java's `limitDecimalPrecision2` floor (#2c).

---

## 3. Notes on the remaining rows

### 3.1 Store `put` (#4)
`SegmentEntityStoreImpl.java:64-110`: Chain and Segment are stored by assignment; every child entity goes through `entities.get(segmentId).get(entity.getClass()).put(id, entity)` (`:107`), which replaces. The C++ macro `SEGMENT_STORE_CORE_METHODS` (`SegmentEntityStore.cpp:11-18`) uses `emplace`, which leaves an existing key untouched and returns a pointer to the *old* value (`:17`). Segments are fine (`this->segments[segment.id] = segment`, `:80`); only the eight child types are affected. `doCutoffLastSegment` is the confirmed victim (`CraftWork.cpp:342-356`), and its only test coverage in either repo is none (`grep -rli cutoff engine/test workstation/src/test` finds only config tests and a legacy `.xj`). Java's version also works for a second reason: `store.readAll` returns the stored instances and the fluent setter `pick.lengthMicros(...)` mutates in place (`SegmentChoiceArrangementPick.java:146-148`), so the trim would stick even if `put` were a no-op. In C++ the pick is copied first (`SegmentChoiceArrangementPick updatePick = *pick;` `CraftWork.cpp:351`), so nothing in the store changes. A 2.0 spec that relies on "re-put to update" anywhere (follow mode writing foundation records, cue rewrites) needs `insert_or_assign` semantics stated in the store contract.

### 3.2 Mute probability and the Layer Mute cue (#7)
`Craft::computeMute` (`Craft.cpp:380-382`) is called on every fresh choice: `Craft.cpp:449,494`, `BackgroundCraft.cpp:29`, `BeatCraft.cpp:58`, `DetailCraft.cpp:99`, `TransitionCraft.cpp:58`. A `choiceMuteProbability` of `1.0` for any type therefore throws `FabricationException("Probability must be 0 <= n < 1")` from inside the stage, which fails the segment. The Java original accepts `1.0` and returns `true` every time (`nextDouble(0,1) <= 1.0`). The proposal's Layer Mute cue is defined exactly as setting that probability to 1 (`04-engine.md:178`; `07-session-protocol.md:131` relies on it for claims), so 2.0 must change the bound to `0 <= p <= 1`. The comparison operator also differs (`<` in C++, `<=` in Java); keep `<` so `p = 0` can never mute (Java's `<=` with `p = 0` mutes only on an exact `0.0` draw, which is effectively never). No test exercises a non-zero probability (`engine/test` only asserts the `0.0` defaults in `TemplateConfigTest.cpp:29,77`).

### 3.3 Delta arcs (#9a-9d)
All three code defects date from the port commit `be1cbae2e` (`git log -S 'secLayers.emplace_back();'` and `-S 'probability >= 1'` both resolve to it). Delta arcs are off by default in both engines (`deltaArcEnabled = false`, `TemplateConfig.cpp:28`, `TemplateConfig.java:45`), which is why the demo content never exposed them. The third shuffle (`Craft.cpp:110`) is not a bug in the Java logic the port copied; it is an addition, and it makes `deltaArcBeatLayersToPrioritize` meaningless. Java's `CraftImplTest.precomputeDeltas` (`CraftImplTest.java:67-71`) calls the method and asserts nothing, same as the C++ test, so the "existing gtest matrix is the acceptance test" claim in `04-engine.md:214` does not cover this feature at all.

### 3.4 Sort comparator (#10)
`std::sort` with a non-strict-weak-ordering comparator is undefined behavior, not just a wrong order; libc++ debug builds can abort on it. The workstation's macro override menu and `craft_timing.cpp:35,40` (`engine.getAllMacroPrograms()` cycled by index) both consume this list, so override sequences in the 24 ms measurement were in an unspecified order.

### 3.5 Dub intensity source and the window bug together (#5 with #1)
Both engines take intensity from the first element of `getSegmentsIfReady`, and both prepend the previous segment when it is still Crafted. In Java, with a one-hour window, that element is the *previous* segment for most of each segment's playback, so dub intensity lags one segment behind craft intensity; that is the V1 reference behavior. In C++ 1.9.6 the previous segment has usually been deleted 3.6 ms after the boundary (`doSegmentCleanup`, `CraftWork.cpp:396-401`), so `at(0)` is the current segment. Fixing #1 alone restores the Java lag. The 2.0 spec should say which it wants (the proposal's intensity cue, `04-engine.md` §6, suggests "the segment at `atChainMicros`"), and say it as a deliberate V1 deviation if so.

### 3.6 Engaged-null optional (#13)
`std::optional<Chain*>` holding `nullptr` is `has_value() == true`. Both guards (`Fabricator.cpp:33` "No chain found", `:1148` "No chain") are dead code; a Fabricator built before `start` dereferences null. With a C ABI in front of the engine this becomes an error-code question: the spec should require `xj_engine_*` calls before `start` to return a defined error rather than rely on these guards.

### 3.7 Things checked and found equivalent
`DubWork` intensity override path (`DubWorkImpl.java:206,228-229` vs `DubWork.cpp:106-109`); `getSegmentsIfReady` filtering to Crafted (`CraftWorkImpl.java` vs `CraftWork.cpp:57-76`); `readAllSegmentsSpanning` (unsorted stream filter in Java, `SegmentEntityStoreImpl.java:184-189`; `std::map` iteration by id in C++, `SegmentEntityStore.cpp:172-180`, so C++ is sorted and Java relies on insertion order of a `ConcurrentHashMap`, which is not guaranteed, meaning the Java "first segment" was itself not strictly defined); `NotePicker` random note (Java shuffles with a `SecureRandom` then `quickPick`s, `NotePicker.java:26,134-139`; C++ `quickPick`s directly, `NotePicker.cpp:110`; same distribution, different draw count).

Not checked here: the enum `parseType` fallback to the first enumerator noted in `engine-store-build-tests.md` (Java `valueOf` would throw); it is a loader-tolerance difference, not a craft difference.

---

## 4. Random generator inventory (correcting "four")

`02-where-v1-stands.md:71` (G6) and `04-engine.md:146` say four `random_device`-seeded generators. `grep -rn "random_device" engine/src engine/include` finds **seven construction sites in five files**:

| Site | Scope | Consumers |
|---|---|---|
| `StickyBun.cpp:14-17` (`StickyBun.h:22-25`) | static, process-wide `mt19937` + `uniform_int_distribution<>(0, MAX_VALUE-1)` | sticky-bun note offsets |
| `ValueUtils.cpp:33-34` (`ValueUtils.h:23-24`) | static `mt19937`; `uniform_int_distribution` at `:84`, `:165` | `randomFrom`, id shuffles |
| `MarbleBag.h:33-34` | per-`MarbleBag` instance `mt19937`; `uniform_int_distribution` at `MarbleBag.cpp:145` | every weighted `pick()` |
| `MarbleBag.cpp:167-170` | function-local, new `random_device` **per call** in `quickPick` | `precomputeDeltas` (`Craft.cpp:112,117`), `NotePicker.cpp:110`, others |
| `MarbleBag.cpp:176-179` | function-local per call in `quickBooleanChanceOf`, `uniform_real_distribution<>(0,1)` | `computeMute` |
| `Craft.h:417-418` | per-`Craft` instance `mt19937`; three `std::shuffle` at `Craft.cpp:101,104,110` | layer ordering |
| `Fabricator.cpp:874-876` | function-local per call in `chooseVoicing` | voicing choice |

All are `std::mt19937` (32-bit), not `mt19937_64`. The Java original is also unseedable (`ThreadLocalRandom` in `TremendouslyRandom.java:21,31,42`, `Collections.shuffle` in `CraftImpl.java:393,395`, `SecureRandom` in `NotePicker.java:26`), so "behavior-preserving by default" for seeding means "as random as before", which is achievable; it does not mean the same sequences as any existing build, which is not.

---

## 5. Portability: distributions and `shuffle` are implementation-defined

The engine (`std::mt19937`, `mt19937_64`) is fully specified: the standard fixes the algorithm and the 10,000th output. The distributions are not. [rand.dist.general]/3: "The algorithms for producing each of the specified distributions are implementation-defined." (https://eel.is/c++draft/rand.dist.general). cppreference on `std::shuffle`: "the implementation is not dictated by the standard, so even if you use exactly the same RandomFunc or URBG you may get different results with different standard library implementations" (https://en.cppreference.com/w/cpp/algorithm/random_shuffle, Notes). The three mainstream libraries in fact differ:

| Library | `uniform_int_distribution` | `std::shuffle` | `uniform_real_distribution` |
|---|---|---|---|
| libc++ (Apple clang 16, `MacOSX.sdk/usr/include/c++/v1`) | `__independent_bits_engine` (`__random/uniform_int_distribution.h:33-218`): assembles `log2(range)` bits from engine words and rejects out-of-range | `__algorithm/shuffle.h:98-107`: Fisher–Yates from the **front**, `uid(g, P(0, d))` with `d` counting down from `n-1`, one draw per element | `(b - a) * generate_canonical<RealType, digits>(g) + a` (`__random/uniform_real_distribution.h:103`) |
| libstdc++ (GCC `bits/uniform_int_dist.h`, `bits/stl_algo.h`) | Lemire's "nearly divisionless" multiply-high with rejection (`_S_nd`, comment cites ACM TOMACS 2019), plus separate upscaling and two-division fallback branches | iterates `for (__i = __first + 1; __i != __last; ++__i)`; when `__urngrange / __urange >= __urange` it draws **two indices per engine call** via `__gen_two_uniform_ints(__swap_range, __swap_range + 1, __g)` | `generate_canonical` with a different `1.0`-clamp history (LWG 2524) |
| MSVC STL (`stl/inc/algorithm`, `stl/inc/random`) | `_Rng_from_urng_v2`: masks to `_Bits` (`_Bmask = ~0 >> (_Udiff_bits - _Bits)`), rejects `_Val > _Bmask` in a `for (;;)` loop, then `_Get_random_product(_Index, ...)` multiplication | `_Random_shuffle1`: front to back, `_RngFunc(_Target_index + 1)`, swap with `*(_UFirst + _Off)` | own `_Nrand_impl` |

Sources: libc++ headers read locally at the paths above; libstdc++ and MSVC from `raw.githubusercontent.com/gcc-mirror/gcc/master/libstdc++-v3/include/bits/{uniform_int_dist.h,stl_algo.h}` and `raw.githubusercontent.com/microsoft/STL/main/stl/inc/{random,algorithm}` (fetched 2026-10-25; MSVC `_Rng_from_urng_v2`'s class header fell outside the fetched window, so its description rests on the visible `operator()` tail and call sites).

Consequence for `04-engine.md:217` ("same seed, same project, same cues ... byte-identical segment stores"): that holds per platform only if the engine owns its distributions. A fixed `mt19937_64` seed gives identical 64-bit words everywhere; the moment those words pass through `std::uniform_int_distribution`, `std::uniform_real_distribution` or `std::shuffle`, the number of words consumed and the mapping to an index differ by library, and every subsequent draw is offset. Golden segment stores built on macOS would fail on Windows CI (`engine_ci.yaml` runs all three). An engine-owned `bounded(uint64_t n)` (Lemire multiply-high with the rejection threshold written out, or plain `x % n` with its small bias accepted and documented) and an engine-owned Fisher–Yates over it make the stores portable; floating-point `intensity` and `*Micros` arithmetic remain a separate, already-acknowledged non-promise (`04-engine.md:150`).

---

## 6. Confirming #2 at runtime with `craft_timing.cpp`

`design/v2-proposal/tools/craft_timing.cpp` prints only timing (`grep -c "delta\|intensity"` → 0): header line, load/start times, and a cycle-time table (`:55-61`). It does not print per-segment fields. But it already holds a handle that exposes them, `engine.getSegmentStore()->readAllSegments()` (`:47`, returns `std::vector<const Segment*>`, `SegmentEntityStore.h:119`), so a six-line addition after `engine.finish(false)` (`:54`) prints the proof:

```cpp
for (const auto s : engine.getSegmentStore()->readAllSegments())
  std::printf("seg %d type=%s delta=%d total=%d intensity=%.3f\n",
              s->id, Segment::toString(s->type).c_str(), s->delta, s->total, s->intensity);
```

Expected with the 1.9.6 regression: `delta=0` on every line and `intensity=0.200` on every line for any demo project (all demo templates leave `intensityAutoCrescendoEnabled = true`). Expected after fixing `MacroMainCraft.cpp:81,86`: `delta` climbing by `total` on each `Continue` and resetting to `0` on `NextMain`/`NextMacro`, with `intensity` rising above 0.2. Caveats:

- Only the last hour's segments survive `doSegmentCleanup`; with the 3.6 ms window of #1, `readAllSegments()` after `finish` contains only the final one or two segments. Print inside the loop whenever `segCount != lastSegCount` instead (the harness already detects that at `:50`), or fix #1 first.
- Do not use the engine's own test project for this: `EngineTest.cpp:100` builds its template with `intensityAutoCrescendoEnabled=false`, and that is the only template under `engine/test/_data` configured that way for a reason worth asking about.
- The 11-resolutions measurement was done on Windows with MSVC (`11-resolutions.md:395`). On this Mac there is no `engine/build` (`ls engine/build` → missing), so a library build is needed: `cmake .. -DBUILD_TESTING=OFF && cmake --build . --target xjmusic` per `tools/README.md`. `example/` is added unconditionally (`engine/CMakeLists.txt:81`) but SDL comes from a vendored tarball through `add_vendor_external_project` (`example/CMakeLists.txt:16-32`) and is only built with the `xj_player` target, so `--target xjmusic` compiles the library without SDL; FTXUI is `FetchContent` from a local tarball (`:36-37`) and costs only configure time. Minutes, not a fresh SDL build.

---

## 7. Implications for V2 specs

1. **"Behavior-preserving by default" needs a named reference.** Eleven of the rows above are regressions or C++-only defects; four are inherited. The engine 2.0 spec should state: "the reference behavior is the Java 1.x craft where the port diverged (rows 1, 2, 2b, 4, 7, 8c, 9a-c, 10) and the common behavior otherwise (rows 5, 6, 8, 8b, 11, 12, 15)". Without that sentence, "preserve" freezes the regressions.

2. **"Reads every 1.9.x project unchanged" is false today for voice names** (row 3). Every `ProgramVoice.name` and `ProgramVoiceTrack.name` loads empty in 1.9.6. The compatibility spec must include a loader fixture exported by the workstation, with voice and track names asserted.

3. **The intensity crescendo and the 280-beat main-program limit have never worked in the C++ engine.** Any V2 feature that reads `segment.intensity` (the Energy meter in `05-workstation.md:40`, the intensity cue, `computeIntensityAmplitude` in dub) or expects main programs to rotate on `mainProgramLengthMaxDelta` must assume the Java behavior returns when #2 is fixed, and the demo packs were authored against Java. Fix #2 and #2b together and decide #2c (two-decimal floor) explicitly because it changes every golden value.

4. **Fix #1 and #5 together.** Correcting the persistence window makes dub intensity lag one segment, which is V1 Java behavior but not what the intensity-cue spec implies. Write down which segment supplies dub intensity.

5. **Layer Mute cannot be built on `choiceMuteProbability = 1` until #7 is fixed.** Spec the bound as `[0, 1]`, `<` comparison, and add a test for `p = 1` (always muted) and `p = 0` (never muted).

6. **Store semantics belong in the spec.** `put` of an existing child id must replace (`insert_or_assign`). Follow mode's `ExternalFoundationCraft` writes chords, voicings, memes and key through the same store, and a rewrite from a cue re-puts entities for the same segment id; with 1.9.6 semantics the second write is silently dropped.

7. **Delta arcs need a real test before any V2 "build"/"drop return" move leans on them.** Three regressions (9a-9c) and two assertion-free tests (9d) mean the Java behavior has never been verified in C++. The existing gtest matrix is not the acceptance test for this feature, contrary to `04-engine.md:214`.

8. **Seeding touches seven sites, not four, and must replace the distributions too.** Thread one engine-owned generator through `StickyBun`, `ValueUtils`, `MarbleBag` (instance and the two static helpers), `Craft`, and `Fabricator::chooseVoicing`; replace `std::uniform_int_distribution`, `std::uniform_real_distribution` and `std::shuffle` with engine-owned `bounded()`, `unit()` and `fisher_yates()`. Otherwise the "byte-identical segment stores" test in `04-engine.md:217` passes on one CI runner and fails on the other two.

9. **`std::sort` UB (#10) and the engaged-null optional (#13) are crash classes, not quirks.** The C ABI spec should define error returns for calls before `start` and for empty macro lists rather than inherit these paths.

10. **Cosmetic divergences still break golden stores.** The `event` label (`"BIG"` vs `"LARGE"`, row 8c) and unrounded `intensity` (row 2c) differ between Java and C++ outputs. A golden-store format should either canonicalize these fields or the spec should pick one behavior and say so.

11. **Test fixtures encode the regression.** `CraftFoundationContinueTest.cpp:108` asserts `0.2` where Java asserts `0.23`. Fixing #2 will fail that test; the spec's test plan should list the fixtures whose expected values change (this one, and any `delta`-dependent `CraftFoundation*Test`), so the change is reviewed as a correction, not a regression.

12. **Provenance for the issue tracker.** Rows 1, 2, 4, 7, 9a-c, 10 all trace to `be1cbae2e` (#405); row 3 to `c804d29c6` (#417). Rows 5, 6, 8, 8b, 11, 12, 15 trace to the Java original and should be filed, if at all, as V1 design decisions to confirm, not bugs.
