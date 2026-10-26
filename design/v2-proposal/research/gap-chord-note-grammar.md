# Chord and Note grammar: the engine's music library as the canonical text form

Scope: `engine/src/music/{Accidental,Chord,Note,NoteRange,Octave,PitchClass,Root,SlashRoot,Step,Tuning}.cpp` and their headers under `engine/include/xjmusic/music/`, the three Fabricator helpers that transpose, and the Java twins under `workstation/src/main/java/io/xj/model/music/`. (`BPM`, `Bar`, `StickyBun` are in the same directory but are not grammar; `StickyBun` is covered in `engine-craft-stages-choice.md`.)

Method: every "oracle" result below was produced by compiling the real sources into a standalone binary (`scratchpad/understand/oracle/oracle.cpp`, `oracle2.cpp`, built with `clang++ -std=c++17` against `engine/include` plus `engine/src/util/StringUtils.cpp`) and running it, not by reading alone. Java behaviour is by reading; where it differs from C++ it is marked **(by reading)**. The demo survey (`scratchpad/understand/survey_chords.py`) walked all 676 JSON files of the four demo projects in `scratchpad/understand/demos-src/`.

## 1. File inventory

| File (src / include) | Lines | What it defines |
|---|---|---|
| `PitchClass.cpp` / `.h` | 99 / 55 | `enum PitchClass {Atonal, C, Cs, D, Ds, E, F, Fs, G, Gs, A, As, B}` (`PitchClass.h:19-33`); `pitchClassOf(string)` (`:35-42`) via regex `^([A-G][#b]*)` (`:8`) and a 22-entry spelling map (`:9-32`); `stringOf(PitchClass, Accidental)` (`:45-99`) |
| `Accidental.cpp` / `.h` | 35 / 34 | `enum Accidental {Natural, Sharp, Flat}` (`.h:12-16`); `accidentalOf(name)` (`.cpp:16-24`) decides sharps-vs-flats for a whole chord name; `accidentalOfBeginning` (`:27-35`) |
| `Octave.cpp` / `.h` | 23 / 20 | `octaveOf(text)` (`:15-23`): regex `(-*[0-9]+)$` (`:12`), `--` collapsed to `-`, default 0 |
| `Root.cpp` / `.h` | 44 / 51 | `Root::of(name)` splits a chord name into `pitchClass` + `remainingText` (`:12-21`, `:29-44`); regexes `^([ABCDEFG]).*` and `^([ABCDEFG][#b]).*` (`:8-9`) |
| `SlashRoot.cpp` / `.h` | 85 / 92 | `SlashRoot::of(description)` → `pitchClass` (optional), `pre`, `post`; four regexes at `:11-14`; `display(Accidental)` (`:66-80`); `operator==` (`:83-85`) |
| `Chord.cpp` / `.h` | 266 / 139 | `ChordSynonym`, `ChordForm`, the 41-entry `Chord::forms` table (`:62-158`); constructor/parser (`:174-207`); `normalize` (`:236-246`); `getName` (`:215-223`); `delta` (`:210-212`); `operator==`/`<` (`:161-171`); `isNoChord`/`has_value`/`isAcceptable` (`:254-266`) |
| `Step.cpp` / `.h` | 263 / 90 | `stepUpMap`/`stepDownMap` (`:8-39`), the 12x12 `deltaMap` (`:42-211`), `Step::delta(from,to)` (`:225-228`), `Step::step/stepUp/stepDown` (`:231-263`) |
| `Note.cpp` / `.h` | 187 / 235 | `rgxValidNote` `^([ABCDEFGX][#b]*[0-9]*)$` (`:12`); `ATONAL = "X"` (`:44`); ctor from string (`:50`); `isValid`/`ifValid`/`ifTonal`/`containsAnyValidNotes` (`:73-96`); `delta` (`:107-126`); `toString(Accidental)` (`:129-131`); `shift`/`shiftOctave` (`:134-142`); `setOctaveNearest` (`:150-158`); `nextUp/nextDown/next` (`:166-187`); comparison operators (`:19-41`) |
| `NoteRange.cpp` / `.h` | 201 / 122 | `low`/`high` optionals; `ofStrings` (`:55-62`), `computeMedianOptimalRangeShiftOctaves` (`:70-98`), `toString` (`:101-116`), `getMedianNote` (`:144-149`), `shifted` (`:152-156`), `toAvailableOctave` (`:176-193`, `MAX_SEEK_OCTAVES = 3` at `.h:18`), `includes` (`:196-202`) |
| `Tuning.cpp` / `.h` | 81 / 107 | default `A4 = 440 Hz` (`:10`); `pitch(Note)` (`:30-41`); `getTones(Hz)` (`:44-49`); `deltaFromRootPitch` (`:52-58`); `pitchAtDelta` (`:61-63`); `validate` (`:66-81`) |

Oracle fixture: `engine/test/_data/music_chord_test.yaml` (174 input→canonical pairs), consumed by `engine/test/music/ChordTest.cpp:17-43`; byte-identical to the Java fixture `workstation/src/test/resources/music/expect_chord.yaml` (verified with `diff`).

## 2. Note grammar

### 2.1 Parse versus validity

A `Note` is parsed by two independent regexes and validated by a third. These disagree at the edges, and craft uses them in different places, so a spec has to name which one it means.

| Function | Regex / rule | Source |
|---|---|---|
| `Note::isValid(s)` | full match of `^([ABCDEFGX][#b]*[0-9]*)$` | `Note.cpp:12, 88-90` |
| pitch class | `^([A-G][#b]*)` captured, then looked up in a 22-entry map; anything not in the map (including `C##`, `Cbb`, `X`, lowercase) → `Atonal` | `PitchClass.cpp:8-42` |
| octave | last run of `-*[0-9]+` at end of string; `--` → `-`; absent → `0` | `Octave.cpp:12-23` |
| `Note::of("")` | `atonal()` = `{Atonal, 0}` | `Note.cpp:56-70` |

Oracle results (`valid` is `Note::isValid`; `pc`/`oct` are what `Note::of` produces regardless of validity):

| Input | valid | pc | oct | `toString(Sharp)` | `toString(Flat)` |
|---|---|---|---|---|---|
| `C4` | 1 | C | 4 | `C4` | `C4` |
| `C#4` / `Db4` | 1 | Cs | 4 | `C#4` | `Db4` |
| `Cb4` | 1 | B | 4 | `B4` | `B4` |
| `B#3` | 1 | C | 3 | `C3` (not `C4`: enharmonic re-spelling does not carry the octave) | |
| `C##4`, `Cbb4` | 1 | **Atonal** | 4 | `X4` | |
| `X`, `X0`, `X4` | 1 | Atonal | 0/0/4 | `X0`/`X0`/`X4` | |
| `C`, `Eb`, `G#` (no octave) | 1 | C/Ds/Gs | **0** | `C0`, `D#0`, `G#0` | `Eb0`, `Ab0` |
| `C-1`, `C--1`, `F#-1` | **0** | C/C/Fs | -1 | `C-1`, `F#-1` | |
| `c4`, `H4`, `4`, `#4`, `Cs4` | 0 | Atonal (Cs4→C) | 4 | `X4` | |
| ` C4` (leading space) | 0 | **Atonal** | 4 | | |
| `C4 ` (trailing space) | 0 | C | 4 | | |
| `C 4` | 0 | C | 4 | | |
| `C♯4`, `C♭4` | 0 | **C** (unicode accidental ignored) | 4 | `C4` | |
| `(None)` | 0 | Atonal | 0 | | |
| `C10`, `C12` | 1 | C | 10/12 | | |

Facts a schema must carry:
- Valid note text is `[A-G][#b]*[0-9]*` or `X[#b]*[0-9]*`. Negative octaves parse but are **invalid**; `Note::ifTonal`, `NoteRange::ofStrings` and the voicing filter at `Craft.cpp:843` all drop them.
- At most one accidental is meaningful. Double accidentals pass validity and then silently become atonal (`X`).
- An omitted octave means octave 0, not "any octave". `Note::operator==` compares octave **and** pitch class (`Note.cpp:19-21`), so `C` (= `C0`) never equals `C4`. This is why Event-mode `InstrumentAudio.tones` without an octave (79 voicing tokens and the Chord-mode `F`/`D`/`B` hook tones in the demos) can only match requests that are also octave-0.
- `X` is the atonal note (`Note::ATONAL`, `Note.cpp:44`). Lowercase `x`, which the demos use 176+126+26+3 times in `tones`, is **not** valid and parses as atonal only by accident of the fallthrough.
- Leading whitespace is fatal to the pitch class but trailing is not; the engine never trims `tones` before `Note::of` (`Craft.cpp:961, 977, 981, 992`, `Fabricator.cpp:1277`). Voicing notes are safe because `CsvUtils::split` trims each item (`engine/src/util/CsvUtils.cpp:12-27`), and `Fabricator::getNotes` is just that split (`Fabricator.cpp:424-427`).

### 2.2 Note lists (voicings, multi-note event tones)

`ProgramSequenceChordVoicing.notes`, `SegmentChordVoicing.notes` and `ProgramSequencePatternEvent.tones` are comma-separated note lists, split on `,` with per-item trim (`CsvUtils.cpp:12-27`). `Note::containsAnyValidNotes(csv)` (`Note.cpp:93-96`) is true if any token is valid; it gates which voicings exist at all (`ContentEntityStore.cpp:683-692`, `Fabricator.cpp:605`). A voicing whose text is `(None)` or empty therefore vanishes, which is how "no chord" sections are realised (see 3.6). Invalid tokens inside an otherwise valid list are dropped one by one at `Craft.cpp:841-846` and `NoteRange.cpp:55-62`; they are not errors.

Demo reality: 646 distinct voicing strings; the engine tolerates `D1, G1, ... D3,` (trailing comma → empty token, dropped), `F1, Ab1, Bb1, Db2, F2, Ab2 Bb2` (missing comma → token `Ab2 Bb2`, dropped), `A3, C#4, E4, F#, A4` (`F#` → `F#0`, kept, octave 0), and one voicing pasted twice onto itself (`... Ab5F3, Ab3, ...` → `Ab5F3` dropped). Voicing octaves span 1–7; sharps and flats are used almost exactly equally (5321 vs 5323 tokens).

## 3. Chord grammar

### 3.1 Parse pipeline (`Chord.cpp:174-207`)

1. Empty input → `root = Atonal`, `description = ""`, `accidental = Natural`, no slash (`:177-183`). `getName()` → `"X"`.
2. `StringUtils::stripExtraSpaces` (collapse runs of spaces, trim; `StringUtils.cpp:128-133`) (`:186`).
3. `accidental = accidentalOf(name)` on the **whole** name (`:189`; rule in 3.5).
4. `Root::of(name)` (`:192`): regex `^([ABCDEFG]).*` then `^([ABCDEFG][#b]).*` (`Root.cpp:8-9, 18-19`); the longer match wins; `remainingText` is the rest with leading whitespace removed (`:39-43`). Root letters are **uppercase only**; `c`, `cmaj7`, `a-` → `Atonal` root with the whole string as description (oracle: `[cmaj7] -> name=[X cmaj7]`).
5. `raw = stripExtraSpaces(remainingText)`; `normalized = Chord::normalize(raw)` (`:198-199`): first `ChordForm` whose synonym list matches wins, else the input unchanged (`:236-246`).
6. `slashRoot = SlashRoot::of(normalized)` (`:202`).
7. `description = (raw == normalized) ? normalize(slashRoot.pre) : slashRoot.pre` (`:206`): if step 5 changed nothing, the pre-slash text is normalised again (so `m7/G` → pre `m7` → `-7`); if step 5 already substituted, the pre text is kept.

Synonym matching (`Chord.cpp:24-27`): case-insensitive unless the synonym is flagged `caseSensitive` (the `M`-family: `M`, `M7`, `m`, `m7`, `m7b5`, `Madd2`, `Madd9`, `m7/9`, `m7add9`, `M7/9`, `M7add9`, `M6`, `M6/9`, `M6add9`, `m6`, `Madd4`, `Madd11`, `m7/11`, `m11`, `m7b5/11`, `M7#11`, `M7/13`, `dimM7`, `mM7`, `+M7`, `augM7`, `mb6`, `m13`). **Canonical descriptions themselves are not in the synonym lists**, so they pass through `normalize` unchanged only when spelled exactly: `Cmaj7` → `C maj7` but `CMaj7` → `C Maj7` and `CMAJ7` → `C MAJ7` (oracle), which are unknown descriptions and are not acceptable substitutes for `C maj7`. The demos contain `Bb Maj7` and `BbMaj7`.

### 3.2 The 41 canonical forms (`Chord.cpp:62-158`)

Canonical description → accepted synonyms (cs = case-sensitive). Empty description is major.

| Canonical | Synonyms |
|---|---|
| `` (major) | maj, major, M(cs) |
| `-` | min, minor, m(cs), mi |
| `maj7` | major7, M7(cs), ∆ |
| `-7` | min7, minor7, m7(cs), mi7 |
| `dim` | diminished, ° |
| `dim7` | diminished7, °7 |
| `-7b5` | min7b5, minor7b5, m7b5(cs), Ø, half dim, half diminished |
| `+` | aug, augmented, #5 |
| `+7` | aug7, augmented7, 7#5 |
| `7` | dom7, dominant 7, dominant |
| `7/9` | dom7/9, dominant 7/9, 9, 7add9 |
| `7/9/13` | dom 7/9/13, dominant 7/9/13, 7/6/9, dom7 6/9, dominant 7 6/9 |
| `7/13` | dom 7/13, dominant 7/13, 7/6, dom7/6, dominant 7/6, 13 |
| `7b9` | dom7b9, dominant 7b9 |
| `7b9b13` | dom7b9b13, dominant 7b9b13 |
| `7b9/13` | dom7b9/13, dominant 7 b9/13 |
| `7#9` | dom7#9, dominant 7#9 |
| `7#9b13` | dom7#9b13, dominant 7#9b13 |
| `7/9b13` | dom7/9b13, dominant 7/9b13 |
| `7#11` | dom7#11, dominant 7#11, 7b5, dom7b5, dominant 7b5 |
| `7add4` | dom7add4, dominant 7add4, 7/11, 11, dom7/11, dominant7/11 |
| `7sus4` | dom7sus4, dominant 7sus4, 7sus |
| `sus4` | sus |
| `sus2` | 5add9, 5add2 |
| `5` | (none) |
| `add9` | maj add9, major add9, Madd2(cs), Madd9(cs), add2, maj add2, major add2 |
| `-7/9` | min7/9, minor7/9, m7/9(cs), m7add9(cs), min7add9, minor7add9, mi7/9, mi7add9, -7add9 |
| `maj7/9` | major7/9, M7/9(cs), M7add9(cs), ∆9, maj7add9, major7add9, maj7add2, major7add2 |
| `6` | maj6, major6, M6(cs) |
| `6/9` | maj6/9, major6/9, M6/9(cs), M6add9(cs), 6add9, maj6add9, major6add9 |
| `-6` | min6, minor6, m6(cs), mi6 |
| `add4` | maj add4, major add4, Madd4(cs), Madd11(cs), add11, maj add11, major add11 |
| `-7/11` | min7/11, minor7/11, m7/11(cs), m11(cs), -11, min11, minor11, mi11, mi7/11 |
| `-7b5/11` | -7b5/11, minor7b5/11, m7b5/11(cs), Ø11, half dim 11, half diminished 11 |
| `maj7#11` | major7#11, M7#11(cs), ∆#11 |
| `maj7/13` | major7/13, M7/13(cs), ∆13 |
| `dim maj7` | diminished major 7, dimM7(cs), dim♮7, °M7 |
| `min/maj7` | -maj7, minor/maj7, mM7(cs), min∆, -∆, mi/maj7, minor/major7, minor major 7 |
| `aug maj7` | +Maj7, augmented major 7, +M7(cs), augM7(cs), aug∆, +∆, maj7#5, major7#5, +♮7, ∆#5 |
| `-b6` | minb6, minor b6, mb6(cs) |
| `-13` | min13, minor 13, m13(cs), -7/13, min7/13, minor7/13 |

Not in the table and therefore **not** recognised (kept verbatim as an unknown description): `maj9`, `m9`, `maj13`, `7b13`, `7alt`, `major 7` (with a space), `-7add11`, `-7add9add11`, `-7no5`, `maj7add13`, `7add#9,b13`, `ø` (lowercase o-slash; only `Ø` is a synonym), `∆7`, `power`. The proposal's `minor` is recognised and canonicalises to `-`: `A minor`, `Am`, `A-`, `A min`, `Amin` all → `A -` (oracle).

### 3.3 Unrecognised descriptions and non-chords

`Chord::of` never throws and never reports failure. Three outcomes exist:

| Input class | Result | Example (oracle) | Detectable by |
|---|---|---|---|
| Known root, known description | canonical | `Fmaj7add9` → `F maj7/9`; `C5add9` → `C sus2`; `C13` → `C 7/13`; `C11` → `C 7add4` | — |
| Known root, unknown description | root kept, description verbatim (after space-collapse) | `Cmaj9` → `C maj9`; `D#-7no5` → `D# -7no5`; `C7alt` → `C 7alt` | `description` not in the 41-form set (no API for this; a spec must add one) |
| No root letter at position 0 | `root = Atonal`, whole input is the description | `NC` → `X NC`; `N.C.` → `X N.C.`; `X` → `X X`; `P-funk` → `X P-funk`; `c` → `X c`; a UUID → `X <uuid>` | `isNoChord()` / `!has_value()` (`Chord.cpp:254-266`) |

Round trip: every known-root result is stable (`Chord::of(c.getName()).getName() == c.getName()` for all 228 distinct demo names and keys with a root). **Every atonal-root result is unstable**: `X NC` re-parses to `X X NC` because `X` is not a root letter (oracle). `Chord::of("")` → `X` → re-parse → `X X`. There is no canonical spelling of "no chord" in the C++ library; the Java twin declares `Chord.NO_CHORD_NAME = "NC"` (`Chord.java:12`) and the Java test asserts `Chord.of("NC").isNoChord()`, but neither side can serialise it back to `NC`.

### 3.4 Slash chords (`SlashRoot.cpp:11-14`)

```
rgxSlashPost          "[^/]*/([A-G##bb]+)$"   -> post: text after the LAST slash, only if it is all note letters/accidentals to end of string
rgxSlashNote          "/([A-G])$"
rgxSlashNoteModified  "/([A-G][##bb])$"       -> pitch class of the slash root (one accidental)
rgxSlashPre           "^([^/]*)/"             -> pre: text before the FIRST slash
```

Constructor (`:17-30`): `post` from `rgxSlashPost` (empty if the thing after the slash is not a bare note); `pre = post.empty() ? name : match(rgxSlashPre)`; pitch class from the modified regex, else the plain regex, else `nullopt`. Consequences, all oracle-verified:

- Tensions written with a slash are **not** slash roots: `C 7/9` → `post=""`, `pre="C 7/9"`, no slash pitch class (`SlashRootTest.cpp:44-48, 68-83`). `Gmaj7/9` → description `maj7/9`; `Gmaj7/E` → description `maj7`, slash E (`ChordTest.cpp:89-93`).
- Slash root must be uppercase: `Dmaj7/f#` → description `maj7/f#`, no slash.
- Two slashes: `C/E/G` → `C/G` (pre is `""` because `^([^/]*)/` on `/E/G` captures empty and `StringUtils::match` treats an empty group as no match, `StringUtils.cpp:136-148`).
- `/G` alone → `X/G` (slash with atonal root).
- Trailing slash: `Cm/` → `C m/`, `Cmaj7/` → `C maj7/` (unknown description; the slash is swallowed into it).
- Serialisation (`display`, `:66-80`): if a pitch class was parsed, `/` + `stringOf(pc, accidental)` using the **chord's** accidental; else `/` + raw `post`. So `Ab/D#` → `G#/D#` (sharp wins: 1 `#` vs 1 `b`, see 3.5) while `Db/Eb` and `Ab/Eb` stay flat.
- `SlashRoot::operator==` (`:83-85`): equal if both `post`s are non-empty and identical, **or** the optional pitch classes are equal. This makes `Chord::of("C") != Chord::of("C/G")` (nullopt vs G) while `Chord::of("C/G").isAcceptable(Chord::of("C"))` is true (oracle; `ChordTest.cpp:118-125, 139-149`).
- `slashRootPitchClass()` = slash pitch class or the root (`Chord.cpp:231-233`); the Bass transposition path uses exactly `toChord->slashRoot.pitchClass.value_or(toChord->root)` (`Fabricator.cpp:568`).

### 3.5 Serialisation and spelling: `getName()` (`Chord.cpp:215-223`)

`getName() = stringOf(root, accidental) + (description.empty() ? "" : " " + description) + slashRoot.display(accidental)`. One space between root and description, no space before `/`. Hence `A -`, `C -7/G`, `Bb maj7/C`, `F# 5`, `C` (major).

The accidental (`Accidental.cpp:12-23`) is chosen once per chord from the raw input: count literal `#` characters, literal `b` characters (anywhere, so the `b` in `7b9`, `-7b5`, `b13` counts), "sharpish" words `(M|maj|Major|major|aug)` and "flattish" words `([^a-z]|^)(m|min|Minor|minor|dim)`. **Flat iff `#flats > #sharps`, or they are equal and `flattish > sharpish`; otherwise Sharp.** `stringOf` with `Natural` prints sharps (`PitchClass.cpp:64-65`); `Natural` only ever arises from the empty chord. Oracle-verified spellings:

| Input | Name | Why |
|---|---|---|
| `Am`, `A minor`, `Cdim`, `C half dim` | `A -`, `A -`, `C dim`, `C -7b5` | Flat accidental (flattish word) but root is natural, so invisible |
| `C#m7b5`, `F#m7b5` | `Db -7b5`, `Gb -7b5` | 1 `#` = 1 `b`; `m` is flattish → Flat → root re-spelled flat |
| `Bbm7b5` | `Bb -7b5` | 2 `b` > 0 `#` |
| `Db7#9`, `Bb7#11`, `Ab/D#` | `C# 7#9`, `A# 7#11`, `G#/D#` | tie with no words → Sharp → root re-spelled sharp |
| `Cb`, `Cbm`, `B#` | `B`, `B -`, `C` | enharmonic roots normalise; `Cb` keeps Flat but B has no flat spelling |
| `F#7add#9,b13` (demo) | `F# 7add#9,b13` | 2 `#` > 1 `b` |
| `C°` vs `Cdim` | both `C dim` | accidental differs (Sharp vs Flat) but is invisible |

So the canonical name is stable under re-parsing, but **not** under enharmonic re-spelling by the author: `Db7#9` and `C#7#9` are the same chord (`operator==` true) with the same canonical name `C# 7#9`, whereas `Db7` and `C#7` are the same chord with two different canonical names (`Db 7`, `C# 7`). A spec that compares names as strings must compare `Chord::of(a) == Chord::of(b)` instead, or define a stricter spelling rule than `accidentalOf`.

### 3.6 Equality, acceptability, ordering, no-chord

| Operation | Rule | Source |
|---|---|---|
| `operator==` | same root **and** description **and** `SlashRoot::operator==` (3.4) | `Chord.cpp:169-171` |
| `isAcceptable(other)` | same root and description; slash ignored | `:259-261`; used for Chord-mode audio fallback at `Craft.cpp:288` |
| `operator<` | by `root` enum, then `description` string | `:161-166` |
| `isNoChord()` / `has_value()` | `root == Atonal` / `!=` | `:254-266` |
| `delta(target)` | `Step::delta(root, target.root)` (3-method primitive for "how far to transpose"; **unused anywhere in the engine**, grep) | `:210-212` |

Enharmonic equality holds (`G#` == `Ab`, `ChordTest.cpp:111-115`). `Cm7 == CM7` is false (descriptions `-7` vs `maj7`).

No-chord in practice: nothing in `engine/src/craft` or `engine/src/fabricator` calls `isNoChord` (grep: only `Chord.cpp` and its header). A main-program chord named `NC` becomes a `SegmentChord` named `X NC` (3.7); its voicings are authored as `(None)` or empty, fail `containsAnyValidNotes`, and so are never stored (`ContentEntityStore.cpp:687`, `MacroMainCraft.cpp:59`). Detail craft then finds no voicing for that chord span and writes nothing. The chord **name** plays no part; the absence of voicings does. The fixture `engine/test/_data/arrangements/arrangement_0_no_chord_sections.yaml` exercises this.

### 3.7 Where chord and note text lives, and where it is canonicalised

| Entity.field | JSON key | Parser on load | Canonicalised? |
|---|---|---|---|
| `Program.key` | `"key"` (`content/Program.h:79`) | none (raw string) | no; read by `Fabricator::getKeyForChoice` (`Fabricator.cpp:288-304`) via `Chord::of` |
| `ProgramSequence.key` | `"key"` (`ProgramSequence.h:34`) | none | no; preferred over `Program.key` when non-empty (`:294-295`, `MacroMainCraft.cpp:152-158`) |
| `ProgramSequenceChord.name` | `"name"` (`ProgramSequenceChord.h:33`) | none | **yes, on write to the segment**: `SegmentChord.name = Chord(sequenceChord->name).getName()` (`MacroMainCraft.cpp:51, 57`) |
| `ProgramSequenceChordVoicing.notes` | `"notes"` (`ProgramSequenceChordVoicing.h:34`) | none; filtered by `containsAnyValidNotes` (`ContentEntityStore.cpp:687`) | **no**: copied verbatim to `SegmentChordVoicing.notes` (`MacroMainCraft.cpp:65`) |
| `InstrumentAudio.tones` | `"tones"` (`InstrumentAudio.h:47`) | none | no; Event mode: `Note::of` (`Craft.cpp:961, 981, 992`); Chord mode: `Chord::of` (`Craft.cpp:285`) |
| `ProgramSequencePatternEvent.tones` | `"tones"` (`ProgramSequencePatternEvent.h:40`) | none | no; CSV of notes or `X` (`Craft.cpp:821-826`) |
| `Segment.key` | — (no JSON serialiser exists for any segment entity; grep `to_json` in `engine/include/xjmusic/segment`, `engine/src/segment` → nothing) | — | **yes**: `Chord::of(mainKey).getName()` (`MacroMainCraft.cpp:160`, `:75`) |
| `SegmentChord.name` | — | — | yes (above) |
| `SegmentChordVoicing.{type,notes}` | — | — | `type` is `Instrument::Type` enum (`segment/SegmentChordVoicing.h:22`), notes raw |
| `SegmentChoiceArrangementPick.tones` | — | — | the picked note (`Note::toString`) or chord name or `X` (`Craft.cpp:900`, `:552`) |

So the proposal's example `"key": "A minor"` with `"name": "Am"` (07, lines 96-105) describes what an author might type; what the store holds after `MacroMainCraft` is `"key": "A -"` and `"name": "A -"`. The Java twin does the same (`MacroMainCraftImpl.java:88, 94, 199`; `FabricatorImpl.java:380-383`). No layer validates these fields on load or in the editor: `SequenceChordPropertiesController.java:151, 163` saves any non-empty string; `SequenceChordVoicingController.java:63-64` saves any notes text; `ProgramEditorController.java:199, 213` saves any key text.

### 3.8 What the demos actually contain

From the 676 demo JSON files: 209 distinct `ProgramSequenceChord.name` values plus program/sequence keys, 228 distinct strings in all.

- Keys: dominant spellings are `C` (324), `A-` (131), `D-`, `F`, `E-`, `F-`, `F#-`, `Eb-`, `Bb-`, `C-`, `C#-` ...; also `Am` (5), `Ebm` (5), `Bm` (3), `C minor` (2), `Dm`, `Gm`, `Cm`. Problem keys: `X` (13 programs + 9 sequences → `X X`), `NC` (10 → `X NC`), `c` (5 programs → `X c`), and **five programs whose key is a UUID** (`a8210f7c-61e2-...` ×6, four others ×1), all parsing as atonal. Keys with atonal roots make `getProgramTargetShift` return 0 (`Fabricator.cpp:560-561`), so those detail programs are never transposed to the chord.
- Chord names: 194 of 209 have a canonical description. The 15 non-canonical descriptions are: `-7add11` (Bb, Eb), `-7add9add11`, `-7no5`, `7add#9,b13`, `Maj7` (two spellings), `maj7add13`, `N.C.`, `NC`, `X`, `c`, and the five UUIDs. Every rooted name round-trips stably.
- Slash chords are common (`Bsus2/D#` ×7, `Db/Eb` ×6, `E-7/A` ×5, `Bmaj7/C#` ×4, `F#/C#`, `Bbmaj7/C`, `C/G`, `F5/Db` ...); `Gsus4/A` and `G/A` from the test suite also appear.
- `InstrumentAudio.tones`: Chord-mode Pad instruments carry 325 distinct chord names; Event-mode Bass/Stripe/Stab/Sticky/Pad carry notes with octaves (`A#2`, `C5`, ...), spelled with sharps; Drum/Percussion/Transition carry `X` or lowercase `x`; Hook/Loop instruments carry `F`, `D`, `B` without octave.
- `ProgramSequencePatternEvent.tones`: 60 distinct; 652 `X`, lowercase `x` present, 68 multi-note CSVs (`F4, A4, C5, D5`), flats outnumber sharps 53 to 4.

## 4. Transposition primitives (question b)

### 4.1 `Step::delta(from, to)` (`Step.cpp:225-228`, table `:42-211`)

Returns the **nearest signed interval** between two pitch classes, from a hard-coded 12×12 table. Rule the table encodes: let `d = (to − from) mod 12 ∈ [0,11]`; result is `d` if `d ≤ 5`, else `d − 12`. Range is therefore `[-6, +5]`; the tritone is **always −6**, never +6; a perfect fifth up is reported as −5. `Atonal` on either side → 0 (`:226`). Oracle: `C→G = −5`, `C→F# = −6`, `F#→C = −6`, `A→C = +3`, `C→A = −3`, `E→Bb = −6`, `Bb→E = −6`, `X→C = 0`. Java computes the same by walking both directions and preferring down on ties (`PitchClass.java:234-243`, test `PitchClassTest.java:56-64`).

### 4.2 `Step::step`, `Note::shift`, `Note::shiftOctave`, `Note::delta`

`Step::step(pc, inc)` walks `stepUpMap`/`stepDownMap` `|inc|` times, accumulating `deltaOctave` at the B→C / C→B boundary (`Step.cpp:231-263`). `Note::shift(inc)` = `{step.pitchClass, octave + step.deltaOctave}` (`Note.cpp:134-137`); `shiftOctave(n) = shift(12n)` (`:140-142`). Oracle: `C4.shift(1)=C#4`, `.shift(11)=B4`, `.shift(12)=C5`, `.shift(13)=C#5`, `.shift(-1)=B3`, `.shift(-13)=B2`; `B3.shift(1)=C4`. This is the only semitone transposition of a pitched thing in the engine and it is exact integer arithmetic, so a DSP ratio of `2^(n/12)` for the same `n` lands on the same note. `Note::delta(target)` (`:107-126`) walks up and down simultaneously until it hits the target, so it returns a true signed semitone distance with no ±6 folding (`C4.delta(A4) = +9`); atonal involvement → 0 after a 1000-step walk (`MAX_DELTA_SEMITONES`, `:16`).

### 4.3 `Fabricator::getProgramTargetShift` (`Fabricator.cpp:558-577`)

```
if (!fromChord->has_value()) return 0;                       // 560-561: atonal program key -> never transpose
cacheKey = type + "__" + fromChord->toString() + "__" + toChord->toString();   // 562-563
Bass:   Step::delta(fromChord->root, toChord->slashRoot.pitchClass.value_or(toChord->root));   // 566-569
others: Step::delta(fromChord->root, toChord->root);                                             // 570-573
```

`fromChord` is the detail program's key (`getKeyForChoice`, `:288-304`: the bound sequence's `key` if non-empty, else the program's `key`), `toChord` is the segment chord (`Craft.cpp:809-815`). It is a **root-to-root** pitch-class distance in `[-6,+5]`: the chord quality, the key's quality, and the destination octave play no part; Bass alone follows the slash root. The result is cached for the Fabricator's lifetime (one segment) by the two `toString()`s. Note the semantic asymmetry with the Java port: identical (`FabricatorImpl.java` uses `PitchClass.delta`).

### 4.4 Octave fitting

- `Fabricator::getProgramRangeShiftOctaves(type, source, target)` (`:531-556`): Bass → `computeLowestOptimalRangeShiftOctaves` (`:1112-1131`): scan `o` from +10 down to −10, keep the `o` with the smallest non-negative `target.low.delta(source.low.shiftOctave(o))`, i.e. put the program's lowest note at or just above the voicing range's lowest note; Pad/Stab/Sticky/Stripe → `NoteRange::computeMedianOptimalRangeShiftOctaves` (`NoteRange.cpp:70-98`): scan `o` from +10 down to −10, keep the smallest `|o|` such that the shifted source fits **entirely inside** the target (`0 <= dLow && 0 >= dHigh`); if nothing fits (source wider than target) → 0 (oracle: `C3-C5` into `E1-G2` → 0; `C5-G5` into `A2-E4` → −2); **Drum and every other type → 0** (`default:` shares the Drum label, `:543-545`). Cached by `type__sourceRange__targetRange` with `toString(Natural)` (sharps).
- Applied at `Craft.cpp:815-826`: `dpTransposeSemitones` (4.3) first, then `sourceRange = dpRange.shifted(semitones)`, then `12 * octaves`, then every event note is `Note::of(n).shift(semitones + 12*octaves)`. The comment at `:814` cites issue #245 for the order.
- `NoteRange::toAvailableOctave(note)` (`NoteRange.cpp:176-193`): if either bound is missing return the note unchanged; otherwise shift by whole octaves toward the range, at most `MAX_SEEK_OCTAVES = 3` times, and **return whatever you have**, in range or not. Oracle with range `E2-G3`: `E1→E2`, `C2→C3`, `G4→G3`, `A3→A2`, `C7→C4` (still outside after 3 shifts). With a range narrower than an octave, `E2-G2`, a pitch class outside the range oscillates: `A4→A1`, `A3→A2`, `A1→A2`, `C5→C2`; the result depends on the parity of the shift count and may lie below the range. `NotePicker` calls it first (`NotePicker.cpp:24`) and then ranks voicing notes by `|delta|` from that note (`:34`), so the picked note is always a voicing note, but the "nearest" is measured from a possibly out-of-range anchor.

### 4.5 What does not exist

- No `Chord::transpose(n)`; no `Chord` constructor from `(root, description, slash)`. A spec that needs "this chord, n semitones up" has to build it: `t = c; t.root = Step::step(c.root, n).pitchClass; if (c.slashRoot.pitchClass) t.slashRoot.pitchClass = Step::step(*c.slashRoot.pitchClass, n).pitchClass;` then `t.getName()`. Oracle: `Bb-7/Db +3 → Db -7/E`, `F#-7 +1 → G -7`, `E −1 → D#` — the accidental is **inherited from the source spelling** (`E` was Sharp, so the result is `D#` not `Eb`). Note `SlashRoot::post` would still hold the old text (`Db`) but `display()` prefers the pitch class (`SlashRoot.cpp:67-71`), so the name is right.
- No `Key` type; `segment.key` is a chord name (3.7). The Java package has an `Interval` enum (`Interval.java`, values 1–16 with `I16` mislabelled 15) that nothing uses; C++ has no equivalent.
- No chord-tone model anywhere: neither side can enumerate the pitch classes of `C 7#9`. Voicings are the only source of actual notes.
- `Tuning` is referenced by no file outside `music/` on either side (grep).

## 5. Octave and tuning convention (question c)

- Default `Tuning()` is `rootNote = A4`, `rootPitch = 440` (`Tuning.cpp:10`); `Tuning::atA4(hz)` (`:20-22`). `pitch(note) = rootPitch * 2^(rootNote.delta(note)/12)` (`:30-41, 61-63`), computed in `float` with a `double` twelfth root (`.h:25-27`), cached per `(octave, pitchClass)`. Oracle at 440: `A4 = 440.0000`, `C4 = 261.6256`, `A3 = 220`, `C0 = 16.3516`, `C-1 = 8.1758`, `C8 = 4186.0093`. **So `C4` is middle C and octave numbers are scientific pitch notation: MIDI = 12 × (octave + 1) + (PitchClass − 1)**, with `PitchClass::C = 1 ... B = 12` (`PitchClass.h:19-33`); `A4` = MIDI 69. A DSP that computes `ratio = 2^(pitch_shift_semitones/12)` and an engine that computes `Note::shift(pitch_shift_semitones)` agree exactly, because both are integer-semitone, 12-TET, A4-referenced. The test suite pins this at A4 = 432 (`TuningTest.cpp:17-128`, `C4 = 256.87`).
- `validate()` (`:66-81`) requires `1 ≤ rootPitch ≤ 100000` Hz and `0 ≤ rootNote.octave ≤ 15`, so a `Tuning` cannot be rooted below octave 0 even though `Note` parses negative octaves.
- **`Tuning::getTones(hz)` and `deltaFromRootPitch(hz)` truncate instead of rounding** (`static_cast<int>(log(pitch/root)/log(2^(1/12)))`, `:52-58`). Oracle at 440: `getTones(261.6256) = C#4` (−8.99999 → −8), `getTones(466.16) = A4` (0.9999 → 0), `getTones(415.3) = G#4` (−1.0 → −1 correct), `getTones(880) = A5` correct. The Java version has the same cast (`Tuning.java:103`). Anything that converts Hz to a note name (an audio analyser, a lint that checks a sample's detected pitch against its `tones`) must use `lround`, not this.
- Negative octaves: `octaveOf("C-1") = -1`, `Note::isValid("C-1") = false` (2.1). If V2 wants MIDI 0–11 (`C-1 … B-1`) representable in text, it has to change `rgxValidNote`; today such notes are parsed by `Note::of` but filtered out of every range and voicing.

## 6. Spelling on output (question d)

`Note::toString(Accidental)` (`Note.cpp:129-131`) = `stringOf(pitchClass, accidental) + to_string(octave)`; `stringOf` prints natural letters as themselves, black keys as sharps for `Sharp` **and** `Natural`, flats only for `Flat` (`PitchClass.cpp:45-99`). Nothing in the engine stores a `Note`'s original spelling: `Note` holds only `{pitchClass, octave}` (`Note.h:30-35`), so `Db4` → `C#4` under any sharp-or-natural rendering. The craft paths that write note text use `toString(Sharp)` or `toString(Natural)` (`NoteRange::toString(Natural)` in the cache keys at `Fabricator.cpp:534`; pick tones), so **engine-emitted note text is always sharp-spelled**: a picked `Bb2` voicing note becomes `A#2` in `SegmentChoiceArrangementPick.tones`. The only flat-spelled text that survives in the store is text the engine copied verbatim: `SegmentChordVoicing.notes` and `SegmentChord.name`'s root/slash when `accidentalOf` chose Flat (3.5). Round trips of note text are stable only modulo enharmonic spelling and only for `isValid` notes with a non-negative octave.

## 7. Java parity (question e): `io.xj.model.music`

The Java package is the one every demo was authored and auditioned through (`02-where-v1-stands.md:43`: the workstation plays its own Java engine). Identical by construction: the 41 forms and all synonyms (`ChordForm.java:15-57` vs `Chord.cpp:62-158`, checked line by line), the shared `expect_chord.yaml`/`music_chord_test.yaml` fixture, `getName` layout (`Chord.java:90-95`), parse pipeline (`Chord.java:21-54`), `SlashRoot` logic (`SlashRoot.java:28-37`), `Octave` regex (`Octave.java:17`), `Note.isValid` regex shape (`Note.java:21`), `NoteRange.toAvailableOctave` with `MAX_SEEK_OCTAVES = 3` (`NoteRange.java:16, 168-185`), `Tuning` (`Tuning.java:18-116`), segment-key and segment-chord canonicalisation (`MacroMainCraftImpl.java:88, 199`), voicing CSV parsing (`FabricatorImpl.java:481-483`). Divergences, all by reading:

| Topic | C++ | Java | Effect |
|---|---|---|---|
| Unicode accidentals `♯ ♭` | not recognised anywhere (`Root.cpp:9`, `PitchClass.cpp:8`, `Note.cpp:12`, `SlashRoot.cpp:11-13`, `Accidental.cpp:17-18`); `C♯` → `C ♯` (root C, unknown description) | accepted everywhere: `Root.java:13`, `SlashRoot.java:13-15`, `Note.java:21`, `Accidental.java:19-26`, `PitchClass.baseDeltaOf` via `Accidental.firstOf` (`:136-145`); `Accidental.replaceWithExplicit` rewrites them for file names (`:88-91`, used by `LocalFileUtils.java:82-83`) | Content saved with `♯`/`♭` plays correctly in Java and wrong in C++. Issue #415 (open, "Workstation should preprocess special accidentals ♯ → # and ♭ → b", body: "Originally supported by the engine, removed for platform compatibility in #377") records this. No demo file uses them (grep). |
| Double accidentals | `C##4` → atonal (`PitchClass.cpp:38-41`) for notes; `C##` chord → `C# #` | `PitchClass.of` applies only the **first** accidental (`baseDeltaOf`, `:136-145`), so `C##4` → `C#4` | Edge case; no demo content |
| Sharp/flat choice on ties | Flat iff `b > #` or (`b == #` and flattish > sharpish) (`Accidental.cpp:23`) | `rgxSharpIn = [♯#]|major` counts the word `major` as a sharp sign; then Sharp if `# > b`, else Flat if any `b`, else sharpish/flattish, else Sharp (`Accidental.java:36-53`) | On `# == b` with no quality words, C++ → Sharp, Java → Flat: `Db7#9` → `C# 7#9` (C++) vs `Db 7#9` (Java); `Bb7#11`, `Ab/D#` likewise. `Db maj7#11` → C++ Sharp (`maj` outweighs), Java Flat. Chord identity is unaffected (enharmonic `==`), only the stored `SegmentChord.name` spelling. The shared yaml fixture has no tie case, so the tests cannot see it. |
| Whitespace in notes | `Note::of` does not trim; ` C4` → atonal | `Note.of` trims (`Note.java:59-63`) | Only matters for `tones` fields (voicings are CSV-trimmed on both sides) |
| Empty chord accidental | `Natural` | `Accidental.None` | cosmetic |
| `isPresent()` | `has_value()` = root not Atonal | `Objects.nonNull(root)`, always true after construction (`Chord.java:154-156`) | Java callers must use `isNoChord()` |
| `Note.isValid` | `regex_match` (full) | `matcher.find()` on an anchored pattern | equivalent |

Nothing else in the Java workstation parses chord or note text: the editors store raw strings (3.7), and `io.xj.gui` has no call to `Chord.of`, `Note.of`, `Note.isValid` or `ChordForm` (grep). The content the Commons will inherit was therefore validated only by whether it happened to sound right through the Java craft.

## 8. Side finding: the C++ JSON reader drops voice and track names

Not grammar, but found while confirming JSON keys. `engine/include/xjmusic/content/ProgramVoice.h:40-41` and `ProgramVoiceTrack.h:33-34` read `"duration"` into `entity.name` and `"tones"` into `entity.order` (copy-pasted from `ProgramSequencePatternEvent.h:39-40`). The Java POJOs serialise these fields as `name` and `order` (`ProgramVoice.java:19-20`, `ProgramVoiceTrack.java:17-18`). Consequence to verify with a JSON-loaded project: `ProgramVoiceTrack.name` is empty, so Drum event-to-audio matching `getTrackName(event) == audio->event` (`Craft.cpp:960`) never scores 300 and every drum event falls to the lowest-UUID audio. The engine's own `ContentEntityStoreTest` builds entities through fixtures, not JSON, so it would not catch this. Whatever `xj-content` reader V2 writes must not copy these two lines.

## 9. Implications for V2 specs

1. **Canonical chord text is `Chord::of(s).getName()`, and only for inputs with a root.** Schema regex for a chord name field: `^[A-G][#b]?( [^/]+)?(/[A-G][#b]?)?$` describes the canonical output; the *input* grammar is wider and should be normalised at the boundary (pack import, cue ingress, foundation-record egress) by the same code or a verified port. Store `A -`, display `Am` if you like, but never store `Am`.
2. **"No chord" has no canonical spelling.** `NC`, `N.C.`, `X`, `` all become atonal and serialise unstably (`X NC` → `X X NC`). The foundation-record and cue schemas need an explicit representation (e.g. `null` chord, or reserve the literal `NC` and special-case it before `Chord::of`), and the engine's actual no-chord mechanism is "no valid voicings for that span", not the name (3.6). A follower must not reject an `X NC` chord name coming from a 1.9.x conductor.
3. **Unknown descriptions are silently accepted.** `Cmaj9`, `D#-7no5`, `Bb Maj7` are stored as-is and never match any `isAcceptable` partner. The Commons lint needs a "description ∈ 41 forms" check that the library does not provide today; the demos would fail it on 7 names plus 8 corrupt keys (UUIDs, `c`, `X`, `NC`), so the lint must be a warning at first.
4. **Spelling is derived per chord from its raw text and differs between C++ and Java on `#`/`b` ties** (7). Equality must use `Chord::operator==` (enharmonic-aware), never string compare, and the foundation record should state that `key`/`chords[].name` are canonical **names**, not canonical **spellings**. If V2 wants deterministic spelling, define it (e.g. prefer the key's accidental) and apply it on both sides.
5. **Note text: `[A-G][#b]?[0-9]+` or `X`, octave required in schemas.** The library tolerates missing octaves (→ 0) and negative octaves (parse but invalid), lowercase `x`, trailing commas and missing commas, and double accidentals (→ atonal). Those tolerances exist in the demos; a pack lint should normalise them (`x` → `X`, add octave where the instrument is Event-mode) rather than reject on day one.
6. **`Note::operator==` is pitch class + octave.** Any "covers its declared note range with sampled tones" lint must define coverage per exact note (today's matching rule, `Craft.cpp:979-986`) and say whether `allowPitchShift` makes octave-equivalent or nearby samples count. There is **no declared instrument note range in V1 content** (`InstrumentConfig.h:22-28` has no range field); the lint's "declared range" is a new field or must be derived from the audios' `tones`, and the *demand* side is `NoteRange::ofStrings(voicing notes of that type)` per main program.
7. **Octave convention is scientific pitch notation: `C4` = MIDI 60, `A4` = 440 Hz by default; MIDI = 12 × (octave + 1) + (PitchClass − 1).** `pitch_shift_semitones` applied as `2^(n/12)` in the DSP matches `Note::shift(n)` in craft exactly. Do not use `Tuning::getTones`/`deltaFromRootPitch` for Hz→note (they truncate); use `lround`.
8. **Engine-emitted note text is sharp-spelled** (`toString(Sharp|Natural)`); voicing text is copied verbatim and may be flat-spelled. A round-trip test must compare parsed `Note`s, not strings.
9. **The only existing semitone transposition is root-to-root `Step::delta` in `[-6,+5]`** (`Fabricator::getProgramTargetShift`), plus octave fitting that returns 0 for Drum/Hook/Percussion/Background/Transition and when nothing fits. A Chord cue's "else transposes" needs a chord-level shift that does not exist: the recipe in 4.5 (shift root and slash pitch classes with `Step::step`, keep description, inherit accidental) is four lines, but the spec must say which accidental the result takes and must leave `description` untouched because the library has no chord-tone model to re-derive it.
10. **`NoteRange::toAvailableOctave` can return an out-of-range note** (3 shifts max, oscillation on sub-octave ranges). Any voicing or pitch-shift logic that assumes its output lies inside the range is wrong; check `includes()` afterwards.
11. **Atonal program keys disable transposition** (`getProgramTargetShift` → 0 when `fromChord` has no root). Five demo programs have UUIDs as keys and five have `c`; the Commons lint should flag a key that parses atonal unless it is explicitly `X`.
12. **No JSON serialiser exists for `Segment`, `SegmentChord`, `SegmentChordVoicing`** in the C++ engine; the foundation-record field names in 07 are a new contract, and `SegmentChordVoicing.type` is an `Instrument::Type` enum that must be spelled as its string form (`Bass`, `Pad`, `Stab`, ...).
13. **Issue #415 is a grammar-compatibility bug, not cosmetics.** Java accepts `♯`/`♭` and C++ does not; a pack authored in the Java editor with Unicode accidentals plays wrong in the C++ engine. Resolve by normalising `♯→#`, `♭→b` at the `xj-content` read boundary (the issue's own proposal) and by adding both characters to the yaml fixture so both parsers are tested on them.
14. **Case matters in two asymmetric ways**: root letters must be uppercase (lowercase → atonal, so `c` is a corrupt key, not C), while synonyms are mostly case-insensitive except the `M`/`m` family. `CMaj7` is **not** `Cmaj7`. The harmony-strip chord picker should emit canonical names from a menu rather than accept free text.
15. **Separate `tones`/`order` JSON slip** (8): the V2 content reader must read `name` and `order` for voices and tracks; a conformance test that loads a JSON project and asserts drum track names is cheap and currently missing.
