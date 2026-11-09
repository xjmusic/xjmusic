# Grounded map: the Java workstation audio path (mixer, dub, ship, audio cache)

Scope: everything between "the fabricator produced picks" and "bytes reached the sound card" in the Java workstation at v1.9.6 (`main`, commit a94000b9a). This is the behavior `xj-dsp` (voice, bus, master, sinks) and `xj-content` (decode, resample, cache) must reproduce or consciously replace.

Path abbreviations used below:

- `W/` = `/Users/nck/xj/xjmusic/workstation/src/main/java/io/xj/`
- `T/` = `/Users/nck/xj/xjmusic/workstation/src/test/`
- `E/` = `/Users/nck/xj/xjmusic/engine/`

Everything stated here was read from the files cited. Where a thing is absent, it says so.

---

## 1. Topology: three cursors, two threads, one pipe

`W/engine/work/FabricationManagerImpl.java` starts exactly two threads (lines 130–144):

| Thread | Loop body | Notes |
|---|---|---|
| 1 | `runControlCycle(); runCraftCycle(); runDubCycle();` | lines 130–136. No sleep anywhere in `work/*.java` (grep confirms): a busy loop gated only by the "ahead" checks below. |
| 2 | `runShipCycle();` | lines 139–143. Also busy; blocks inside `SourceDataLine.write`. |

Three monotonically increasing cursors in chain microseconds define the pipeline:

| Cursor | Owner | Source of truth |
|---|---|---|
| `craftedToChainMicros` | `CraftWorkImpl.getCraftedToChainMicros()` (line 259) | end of the last `CRAFTED` segment in the store |
| `dubbedToChainMicros` | `DubWorkImpl.atChainMicros` (line 54), advanced by `mixerLengthMicros` per `doDubFrame()` (line 301) | |
| `shippedToChainMicros` | `ShipWorkImpl.getShippedToChainMicros()` → `StreamPlayerImpl.getHeardAtChainMicros()` → `SourceDataLine.getMicrosecondPosition()` (`W/engine/ship/broadcast/StreamPlayerImpl.java:60-62`) | the audio device's own play position, assumed equal to chain time (chain starts at 0 when the line starts) |

Gates:

- Craft runs until `shipped + mixerLengthSeconds + craftAheadSeconds` (`FabricationManagerImpl.java:331-334` adds `mixerLength`; `CraftWorkImpl.runCycle` line 276 adds `craftAheadMicros`).
- Dub runs only when `atChainMicros < shippedToChainMicros + dubAheadMicros` (`DubWorkImpl.java:126`), only when `craftWork.isReady()` (line 115; false while a rewrite is pending, see `CraftWorkImpl.isReady` line 296), and only when `getSegmentsIfReady` returns something (line 223).
- Ship consumes *all* available bytes from the pipe every cycle (`ShipWorkImpl.doShipOutputPlayback` lines 108–110) and writes them to the device line.

Dub → Ship hand-off is a `BytePipeline` (`W/engine/mixer/BytePipeline.java`): a `PipedOutputStream`/`PipedInputStream` pair, pipe size `MIXER_OUTPUT_PIPE_SIZE = 10_000_000` bytes (`MixerFactoryImpl.java:12`), roughly 52 s of 48 kHz stereo 16-bit. The device line buffer is `mixerLengthSeconds * frameRate * frameSize` bytes (`ShipWorkImpl.java:44-47`), i.e. one mixer chunk.

Settings that size the pipeline (`W/engine/work/FabricationSettings.java` class defaults vs `workstation/src/main/resources/application.properties`):

| Setting | Class default | application.properties | Where applied |
|---|---|---|---|
| `craftAheadSeconds` | none | `fabrication.defaultCraftAheadSeconds=60` | craft gate |
| `dubAheadSeconds` | none | `fabrication.defaultDubAheadSeconds=2` | dub gate |
| `mixerLengthSeconds` | 2 (line 15) | `fabrication.defaultMixerLengthSeconds=1` | mixer chunk length, device buffer |
| `outputFrameRate` | none | `build.defaultOutputFrameRate=48000` | mixer, cache target |
| `outputChannels` | none | `build.defaultOutputChannels=2` | mixer, cache target (1..2 enforced) |
| `persistenceWindowSeconds` | 3600 (line 18) | — | segment GC behind `shipped` (`CraftWorkImpl.doSegmentCleanup` line 523) |
| `shipOutputFileNumberDigits`, `shipOutputPcmChunkSizeBytes` | 7, 1024 (lines 16–17) | — | **unused** in current code (grep: no readers outside the class) |

Note the proposal's "craft 20 s, dub 10 s by default" does not match the shipped workstation defaults (60 s and 2 s). The C++ engine may differ again; cite whichever the spec means.

The chain/segment time base is microseconds as `long` throughout (`Mixer.java:16`).

---

## 2. Dub: picks → `ActiveAudio` (`DubWorkImpl.doDubFrame`, lines 217–307)

Per dub frame, with `atChainMicros` = start of this chunk and `toChainMicros = atChainMicros + mixerLengthMicros`:

1. `segments = craftWork.getSegmentsIfReady(at, to)` (`CraftWorkImpl.java:136-152`): all segments spanning the window **plus the previous and next segment**, filtered to `SegmentState.CRAFTED`. So picks whose tails spill in from the previous segment are included.
2. Intensity for the chunk (lines 228–232): `nextIntensity = intensityOverride ?? segments.get(0).getIntensity()`; `prevIntensity` is last chunk's `nextIntensity` (set at line 292; initial 1.0 at lines 61–62).
3. Picks: `craftWork.getPicks(segments)` filtered by `!craftWork.isMuted(pick)` (line 241). Mute is `SegmentChoice.mute` reached via arrangement → choice (`CraftWorkImpl.isMuted` lines 204–219). Picks with empty `waveformKey` are skipped (line 245).
4. Timing, relative to chunk start (lines 248–268):

```
transientMicros   = audio.transientSeconds * 1e6  (0 if null)
startAtMixerMicros = segment.beginAtChainMicros + pick.startAtSegmentMicros - transientMicros - atChainMicros
stopAtMixerMicros  = pick.lengthMicros != null
                     ? startAtMixerMicros + transientMicros + pick.lengthMicros
                     : startAtMixerMicros + audio.lengthSeconds * 1e6
```

   The transient shifts the *start* earlier so the audio's transient lands on the pick time. With a finite `lengthMicros` the stop is pick-start + length (transient excluded). With `lengthMicros == null` (one-shot / "infinite") the audio plays its full `lengthSeconds` measured from the shifted start.
5. Inclusion filter (line 269): `startAtMixerMicros <= mixerLengthMicros && stopAtMixerMicros >= 0`. Values may be negative or past the chunk; the mixer clips.
6. `new ActiveAudio(pick, instrument, audio, start, stop, fromIntensityAmplitude, toIntensityAmplitude)` where both amplitudes come from `AudioMathUtils.computeIntensityAmplitude(audio, templateConfig.getIntensityLayers(type), templateConfig.getIntensityThreshold(type), false, prev|nextIntensity)` (lines 277–288). `fadeOutLowerIntensities` is hard-coded **false**.
7. `mixer.mix(activeAudios, nextIntensity)` (line 295). The `intensity` argument is **ignored** inside `MixerImpl.mix` (no reads of the parameter, lines 112–147).

### 2.1 Intensity → amplitude (`W/engine/audio/AudioMathUtils.java:34-40`)

```
if intensityLayers == 1: 1.0
else (fadeOutLowerIntensities == false):
  amplitude = min(1, 2 * max(0, 1 - (audio.intensity - intensity) / intensityThreshold))
```

With default `intensityThreshold = 0.5`: amplitude is 1.0 whenever `intensity >= audio.intensity - 0.25`, 0 when `intensity <= audio.intensity - 0.5`, linear between. Higher target intensity never attenuates lower-intensity audio (that branch, `abs(...)`, is only taken when the flag is true, which the Java dub never does). The C++ engine implements the same formula (`E/src/audio/AudioMathUtils.cpp:5-12`) and the same `false` flag (`E/src/work/DubWork.cpp:166-171`).

Template defaults (`W/model/TemplateConfig.java:67-90`): `intensityLayers` = Background 3, Bass 1, Drum 1, Hook 3, Pad 3, Percussion 3, Stab 2, Sticky 2, Stripe 2, Transition 3; `intensityThreshold` = 0.5 for all ten types. Getter fallbacks: layers 1 (line 362), threshold 0.0 (line 426; a zero threshold would divide by zero in the formula if a type were missing from the map).

### 2.2 `ActiveAudio` (`W/engine/mixer/ActiveAudio.java`)

Fields: `pick`, `instrument`, `audio`, `startAtMixerMicros`, `stopAtMixerMicros`, `fromAmplitude`, `toAmplitude`, `instrumentConfig` (lines 15–22).

```
fromAmplitude = fromIntensityAmplitude * pick.amplitude * instrument.volume * audio.volume   (line 40)
toAmplitude   = toIntensityAmplitude   * pick.amplitude * instrument.volume * audio.volume   (line 41)
getAmplitude(ap) = from + (to - from) * ap,  ap in [0,1]                                     (lines 79-82)
getReleaseMillis() = InstrumentConfig.releaseMillis                                           (lines 69-71)
```

`InstrumentConfig.DEFAULT` (`W/model/InstrumentConfig.java:24-32`): `releaseMillis = 5`, `isOneShot = false`, `isOneShotCutoffEnabled = true`, `isAudioSelectionPersistent = true`, `isMultiphonic = false`, `isTonal = false`, `oneShotObserveLengthOfEvents = []`.

The C++ engine's `ActiveAudio` (`E/include/xjmusic/audio/ActiveAudio.h:13-59`) carries the same shape with chain-absolute times (`startAtChainMicros`, `stopAtChainMicros`, `fromVolume`, `toVolume`, `getAmplitude(ap)`, `getReleaseMillis()`), wrapped in `AudioScheduleEvent {Create, Update, Delete}` (`E/include/xjmusic/audio/AudioScheduleEvent.h:13-20`). The V2 `StartVoice/UpdateVoice/StopVoice` commands map 1:1 onto these.

---

## 3. The mixer (`W/engine/mixer/MixerImpl.java`)

### 3.1 Construction and buffers (lines 65–104)

- Output format comes from `MixerConfig.getOutputFormat()`; channels enforced `1 <= ch <= 2` (lines 86–87).
- `framesPerMilli = (int)(frameRate / 1000)` (line 89): 48 at 48 kHz, 44 at 44.1 kHz (truncation).
- `microsPerFrame = 1e6 / frameRate` (line 90).
- `totalMixFrames = floor(totalSeconds * frameRate)` (line 91).
- `busBuf = float[totalBuses][totalMixFrames][channels]` (line 92), `outBuf = float[totalMixFrames][channels]` (line 94). Allocated once; **cleared by triple loop every `mix()`** (`clearBuffers`, lines 152–160). For 10 buses × 48000 × 2 that is 960k floats zeroed per second.
- Compressor state carried across chunks: `compRatio` (line 51), `busCompRatio[]` (line 52, unused: bus compression is commented out).

### 3.2 Bus assignment (lines 200–205, and `DubWorkImpl.mixerInit`)

```java
public int getBusNumber(InstrumentType t) {
  if (!instrumentBusNumber.containsKey(t))
    instrumentBusNumber.put(t, computedBusNumber.getAndIncrement());
  return instrumentBusNumber.get(t);
}
```

First-seen order. However `DubWorkImpl.mixerInit` (lines 334–337) immediately iterates `InstrumentType.values()` calling `getBusNumber` then `setBusLevel(bus, templateConfig.getDubMasterVolume(type))`, so in practice bus numbers are fixed to enum order: `Drum=0, Bass=1, Pad=2, Sticky=3, Stripe=4, Stab=5, Hook=6, Percussion=7, Transition=8, Background=9` (`W/model/enums/InstrumentType.java:5-15`). `totalBuses = InstrumentType.values().length = 10` (line 322); `MixerConfig` class default is 8 (`MixerConfig.java:12`) and is always overridden.

`dubMasterVolume` defaults are 1.0 for all ten types (`TemplateConfig.java:47-58`; getter fallback 1.0 at line 354). Bus level is applied once per frame when summing to master: `outBuf[f][c] += busBuf[b][f][c] * level[b]` (`mixOutputBus`, lines 165–178). There is no bus mute, solo, meter, pan, or per-bus insert anywhere; `MathUtil.left/right` pan helpers exist (`MathUtil.java:131-155`) but have **no callers** (grep).

### 3.3 Voice rendering: `addToMix(ActiveAudio)` (lines 210–271)

```
cached  = audioCache.load(active.getAudio())        // float[frame][channel], already at output rate
bus     = getBusNumber(instrument.type)
release = envelope.length(releaseMillis * framesPerMilli)     // Envelope cached per length
sourceBeginsAtMixerFrame = (int)(startAtMixerMicros / microsPerFrame)     // may be < 0
sourceEndsAtMixerFrame   = (int)(stopAtMixerMicros  / microsPerFrame)
tf_min = clamp(sourceBeginsAtMixerFrame, 0, totalMixFrames-1)
tf_max = clamp(sourceEndsAtMixerFrame + release.exponential.length, 0, totalMixFrames-1)
ap_d   = 1 / (tf_max - tf_min)
for tc in 0..channels:
  sc = tc % sourceChannels                    // mono source duplicated; stereo→mono takes left only
  sf = tf_min - sourceBeginsAtMixerFrame      // skip source frames before chunk start
  ap = 0
  for tf in tf_min..=tf_max:
    if sf < source.length:
      if tf < sourceEndsAtMixerFrame: busBuf[bus][tf][tc] += src[sf][sc] * active.getAmplitude(ap)
      else:                            busBuf[bus][tf][tc] += release.out(rf++, src[sf][sc] * active.getAmplitude(ap))
    sf++; ap += ap_d
```

Facts a spec must carry:

- **The amplitude ramp is per chunk, not per audio.** `ap` runs 0→1 across the clipped window `[tf_min, tf_max]` of *this* mixer chunk. An audio spanning three 1 s chunks ramps three times, each from that chunk's `prevIntensity`-derived `fromAmplitude` to its `nextIntensity`-derived `toAmplitude`. Since prev/next are the same unless intensity changed, this is a piecewise-linear interpolation of intensity at mixer-chunk resolution (1 s). It also means the ramp slope depends on how much of the audio falls inside the chunk.
- **Release is a fade of the still-playing source, not a tail of silence**: for `release.exponential.length` frames after `sourceEndsAtMixerFrame` the loop keeps reading `src[sf]` and multiplies by the envelope. If the source runs out (`sf >= length`) nothing is added.
- **No attack/fade-in**: `Envelope.in()` exists (`Envelope.java:28-31`) and has no callers.
- Frame rounding is truncation (`(int)` cast), so a start of 0.9999 frames becomes 0.
- Empty `waveformKey` → warn and skip (lines 212–215).
- Audio cache miss inside `addToMix` triggers a synchronous disk load + possible ffmpeg resample on the dub thread (`AudioCacheImpl.load` → `compute` → `prepare`).

### 3.4 Release envelope (`W/engine/mixer/Envelope.java`, `EnvelopeProviderImpl.java`)

```
exponential[i-1] = sin(HALF_PI * i / frames),  i = 1..frames-1       (line 18; HALF_PI = 1.57079632679)
out(delta, v) = delta > len ? 0 : delta > 0 ? exponential[len - delta] * v : v    (lines 40-43)
in(delta, v)  = delta < 0 ? 0 : delta < len ? exponential[delta] * v : v           (lines 28-31, unused)
```

Despite the name it is a quarter-sine, not an exponential. Default length: `5 ms * 48 frames/ms = 240 frames` at 48 kHz. Envelopes are memoized by length in a plain `HashMap` (`EnvelopeProviderImpl.java:10-21`). Test vectors: `T/java/io/xj/engine/mixer/EnvelopeProviderTest.java:20-37` (e.g. `length(62).out(60, 0.643) ≈ 0.0325`).

### 3.5 Master chain order (`mix`, lines 112–147)

1. `clearBuffers()`
2. `addToMix(a)` for each active → bus buffers
3. *(commented out: per-bus compressor, lines 123–127 and 313–342)*
4. `mixOutputBus()` → `outBuf` with `dubMasterVolume` per bus
5. `applyLogarithmicDynamicRange()` (line 132)
6. `applyFinalOutputCompressor()` (line 135)
7. `// NO NORMALIZATION! See issue #275` (lines 137–138; `applyNormalization` exists at 360–365, `@SuppressWarnings("unused")`)
8. `buffer.produce(byteBufferOf(audioFormat, outBuf).array())` (line 145)

**There is no high-pass or low-pass filter in the Java mixer.** `MixerConfig` stores `highpassThresholdHz`/`lowpassThresholdHz` (`MixerConfig.java:23-24, 258-288`), `DubWorkImpl.mixerInit` sets them from `TemplateConfig` (lines 329–330), and nothing reads them back: grep for `getHighpassThresholdHz|getLowpassThresholdHz|highpass|lowpass` across `W/engine/` finds only `MixerConfig.java` and `DubWorkImpl.java`. The proposal's 02-where-v1-stands.md:43 ("high-pass and low-pass filters") describes config fields, not DSP. V2 implementing them is new behavior, not a port.

### 3.6 Logarithmic dynamic range (`MathUtil.logarithmicCompression`, `MathUtil.java:20-27`)

```
v < -1 : -ln(-v - 0.85) / 14 - 0.75
v >  1 :  ln( v - 0.85) / 14 + 0.75
else   :  v / 1.61803398875                 // golden ratio
```

Applied per sample to `outBuf` after bus summing (`applyLogarithmicDynamicRange`, lines 348–351; note it allocates a new `float[]` per frame via the array overload at `MathUtil.java:35-42`). Attribution in the comment: Paul Vögler, "Mixing two digital audio streams with on the fly Loudness Normalization by Logarithmic Dynamic Range Compression". Consequences: a full-scale in-range signal comes out at ≤ 0.618; at `|v| = 1` the two branches meet at 0.6180 vs 0.6145 (a ~0.0035 step). Output exceeds 1.0 only when the pre-log sum exceeds `e^(0.25·14) + 0.85 ≈ 34`. Test vector: `logarithmicCompression({0.31937, 0.81640}) ≈ {0.19738, 0.50456}` (`T/java/io/xj/engine/mixer/MathUtilTest.java:41-44`).

### 3.7 Look-ahead compressor (`applyFinalOutputCompressor`, lines 294–311; `computeCompressorTarget`, 372–375)

Constants and parameters:

| Name | Java field | From `TemplateConfig` key (default) | `MixerConfig` class default (overridden) | Frames at 48 kHz |
|---|---|---|---|---|
| target amplitude | `compressToAmplitude` | `mixerCompressToAmplitude` (1.0) | 1.0 | — |
| ratio max | `compressRatioMax` | `mixerCompressRatioMax` (**1.0**) | 1.38 | — |
| ratio min | `compressRatioMin` | `mixerCompressRatioMin` (0.3) | 0.62 | — |
| look-ahead | `framesAhead = round(frameRate * compressAheadSeconds)` (`MixerConfig.java:142-144`) | `mixerCompressAheadSeconds` (0.05) | 0.1 | 2400 |
| decay | `framesDecay = round(frameRate * compressDecaySeconds)` (146–153) | `mixerCompressDecaySeconds` (0.125) | 0.01 | 6000 |
| cycle | `dspBufferSize` (must be power of 2, `MixerConfig.java:248-255`) | `mixerDspBufferSize` (1024) | 1024 | 1024 |
| peak scan grain | `COMPRESSION_GRAIN = 20` (`MixerImpl.java:27`) | — | — | every 20th frame |

Algorithm:

```
if compRatio == 0: compRatio = target(outBuf, 0, framesAhead)      // first chunk only; otherwise carried from last chunk
compRatioDelta = 0                                                  // NOTE: reset every chunk (local), unlike compRatio
targetCompRatio = compRatio
for i in 0..totalMixFrames:
  if i % dspBufferSize == 0:
    targetCompRatio = target(outBuf, i, i + framesAhead)            // re-target once per 1024-frame cycle
  compRatioDeltaDelta = (targetCompRatio - compRatio) / framesDecay
  compRatioDelta     += (compRatioDeltaDelta - compRatioDelta) / dspBufferSize
  compRatio          += compRatioDelta                              // per-frame interpolation with inertia
  outBuf[i][*]       *= compRatio

target(buf, from, to) = limit(ratioMin, ratioMax, compressToAmplitude / maxAbs(buf, from, to, COMPRESSION_GRAIN))
```

`maxAbs` (`MathUtil.java:90-99`) clamps `to` at `buf.length`, so near the end of a chunk the look-ahead window shrinks to zero; it cannot see into the next chunk. A silent window gives `1/0 = +Inf`, which `limit` clamps to `ratioMax`; `delta()` returns 0 if either side is infinite (`MathUtil.java:108-111`). The "rate of change of rate of change" shaping is second-order smoothing: `compRatioDelta` is a low-passed version of the per-frame step toward target.

Derived consequence (not stated in code, but follows from §3.6 and the defaults): with template defaults `ratioMax = 1.0` and post-log peaks ≤ 0.62 for any in-range sum, `target` is `≥ 1.6` and clamps to 1.0, so **the final compressor is a unity gain stage unless the pre-log bus sum exceeds ~34**. It would only act with a template that sets `mixerCompressRatioMax > 1` (boost) or `mixerCompressToAmplitude < 0.62`. A V2 spec that ports this "faithfully" should know the default is a no-op and decide whether to keep the defaults or the behavior.

### 3.8 Normalization (not applied)

`applyNormalization` (lines 360–365): `normRatio = min(normalizationBoostThreshold, normalizationCeiling / maxAbs(outBuf, NORMALIZATION_GRAIN=20))`, applied to every sample. Disabled by comment at line 137–138 citing issue #275 ("Multi-bus output with individual normalization", also cited at line 121). `TemplateConfig` still parses `mixerNormalizationCeiling` (0.999) and `mixerNormalizationBoostThreshold` (1.0) (`TemplateConfig.java:110-111`) and `mixerInit` passes them (lines 331–332). Dead config.

### 3.9 Output sample conversion (`byteBufferOf`, lines 384–391; `AudioSampleFormat.java`)

Output encoding is fixed: `PCM_SIGNED`, `FIXED_SAMPLE_BITS = 16` (`W/engine/mixer/FixedSampleBits.java:6`), little-endian, `frameSize = channels * 2` (`DubWorkImpl.mixerInit` lines 316–319). `toBytesS16LSB(v) = (short)(0x8000 * v)` (`AudioSampleFormat.java:191-193`). **No clipping**: a value ≥ 1.0 wraps (e.g. 1.078 → 35323 → negative). Not an issue under default gains (see §3.7), but the proposal's true-peak limiter is the real fix. `typeOfOutput` rejects unsigned 8/16-bit and 24-bit for output (`typeOf`, lines 76–125); 24-bit input is read via a hand-rolled shift (`fromBytesS24LSB`, line 343–350).

### 3.10 Golden tests that exist

`T/java/io/xj/engine/mixer/demo/DemoIT.java` mixes a fixed 16-step drum pattern at 121 BPM over 2.231404 s with `ActiveAudio(..., 1.0f, 1.0f)` and byte-compares the WAV against `T/resources/demo_reference_outputs/{22000Hz_Signed_8bit_1ch,44100Hz_Signed_16bit_2ch,48000Hz_Signed_32bit_2ch}.wav` (lines 156–175, 311–319). Source audio: `T/resources/demo_source_audio/{kick1,kick2,maracas,snare,tom1,cl_hihat,ding}.wav`. A V2 port can reuse these as cross-implementation fixtures; the comparison would have to be tolerance-based rather than byte-exact because the resampler changes.

---

## 4. Audio cache and loading (`W/engine/audio/`)

### 4.1 Two-level cache (`AudioCacheImpl.java`)

| Level | Key | Store | Populated by |
|---|---|---|---|
| In-memory | `audio.getId().toString()` (the `InstrumentAudio` UUID) (`load`, lines 46–51) | `ConcurrentHashMap<String, AudioInMemory>` | `load` (miss on dub thread), `loadTheseAndForgetTheRest` (craft thread, `CraftWorkImpl.doAudioCacheMaintenance` lines 534–551, window `[min(shipped,dubbed), max(shipped,dubbed) + mixerLength]`) |
| On-disk, resampled | `<projectPathPrefix>render/instrument/<instrumentId>/<rate>-<bits>-<ch>/<rate>-<bits>-<ch>-<waveformKey>` (`computeResampledCachePrefix` 179–181, `computeResampledCachePath` 193–195, `getAudioRenderPathPrefix` 241–243) | files | `prepare` (preload at `FabricationManagerImpl.startPreparingAudio` lines 393–434, and lazily on miss) |

Target format comes from `initialize(frameRate, FIXED_SAMPLE_BITS=16, channels)` (`FabricationManagerImpl.java:108-112`). **The in-memory key is the audio UUID, the on-disk key is `(instrumentId, rate, bits, channels, waveformKey)`; neither includes a content hash, size, or mtime.** Replacing a file under the same `waveformKey` is invisible until `render/` is deleted. `AudioInMemory.isDifferent` (`AudioInMemory.java:55-57`) compares id and `waveformKey` only. `invalidateAll` is called on fabrication finish (`FabricationManagerImpl.java:160`), clearing memory, not disk.

`prepare` decision (lines 101–124): `matchesAudioFormat` is `rate == target && bits == target && channels <= target` (lines 223–230; "mono source audio should be resampled and cached as mono", issue #232). Match → `Files.copy` to the cache path; mismatch → `FFmpegUtils.resampleAudio(src, dst, rate, bits, channels)`. Note the asymmetry: a mono file at the right rate/bits is copied as mono, but a mono file at the wrong rate is resampled **to `targetChannels` (stereo)**, despite the `FUTURE: don't increase # of channels` comment at line 101. The cache file keeps the source filename's extension because the key ends in `waveformKey`; `FFmpegFrameRecorder` chooses the container by extension, so a non-`.wav` key would be written into that container with a PCM codec.

Source path: `ProjectManagerImpl.getPathToInstrumentAudio` = `<projectPathPrefix>libraries/<LibraryName>/<Instrument-Name-Alphanumeric-Hyphenated>/<waveformKey>` (`W/gui/project/ProjectManagerImpl.java:662-676`).

### 4.2 Loader (`AudioLoaderImpl.java`)

Reads the prepared WAV with `javax.sound.sampled.AudioSystem` into `float[expectFrames][channels]` (lines 31–83): 1024-byte read buffer rounded to a frame multiple (line 60), per-sample `AudioSampleFormat.fromBytes` into `float`. Supports whatever `typeOfInput` accepts (U8, S8, U16/S16 LE/BE, S24 LE/BE, S32, F32, F64). Quirks: a workaround for `AudioInputStream.getFrameLength()` returning bytes (lines 47–52); refuses streams over `Integer.MAX_VALUE` bytes (line 43); `System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, ...)` (line 72) means any file with more than two channels reads channel 0 into every slot (harmless in practice because ffmpeg has already reduced to ≤ 2). `javax.sound` without SPI plugins only decodes WAV/AIFF/AU; MP3/OGG/FLAC/AAC never reach the loader because ffmpeg has converted them.

`AudioInMemory.lengthSeconds()` = `floor6(frames / sampleRate)` (`AudioInMemory.java:45-47`). This is distinct from the content model's `InstrumentAudio.lengthSeconds` field, which is what dub uses for one-shot stop time (§2 step 4).

### 4.3 What ffmpeg is used for (`W/engine/mixer/FFmpegUtils.java`)

Dependency: `org.bytedeco:ffmpeg-platform-gpl:6.0-1.5.9`, `javacv-platform:1.5.9`, `flandmark-platform` (`workstation/build.gradle:244-246`; the Gradle plugin `org.bytedeco.gradle-javacpp-platform 1.5.9` at line 17). The **GPL** build is bundled.

`resampleAudio(in, out, rate, bits, channels)` (lines 30–61): `FFmpegFrameGrabber` → `FFmpegFrameRecorder(out, channels)` with `setSampleFormat(computeSampleFormat(bits))`, `setAudioCodec(computeAudioCodec(bits))`, `setSampleRate(rate)`, then `record(frame)` for every audio frame. Codec by bits (lines 63–73): 8 → `PCM_U8`, 16 → `PCM_S16LE`, 32 → `PCM_S32LE`, 64 → `PCM_S64LE`; the `case 32 | 64` and `64 | 128` arms are bitwise-or constants (96, 192) and dead. Resampling quality is whatever javacv's recorder does with libswresample defaults; nothing in XJ sets a resampler option.

Callers: (1) `AudioCacheImpl.prepare` (line 113); (2) `ProjectManagerImpl.resampleAudioIfNeeded` (lines 479–512) used by **Build** (`buildProject`, lines 380–466: writes `build/<audioId>.<ext>` and `build/<slug>.json`, rewriting each `waveformKey` to `<audioId>.<ext>`) and by template export (line 326). (3) Historically the HLS encoder spawned the `ffmpeg` CLI (§5.3). There is no other codec, pitch-shift, time-stretch, or MIDI code anywhere under `W/engine/` (grep for `pitch|stretch|MIDI|midi` finds only music-theory `PitchClass`).

Build output format is effectively fixed: the Settings modal disables the container and sample-bits choices (`W/gui/controllers/SettingsModalController.java:134,138`), so Build writes 16-bit WAV at the configured rate/channels. `AudioFileContainer` enum lists `WAV, OGG, FLAC, MP3, M4A, AAC` (`W/gui/types/AudioFileContainer.java`) but only WAV is reachable.

---

## 5. Ship (`W/engine/ship/`, `W/engine/work/ShipWorkImpl.java`)

### 5.1 What is live at v1.9.6: local playback only

`ShipWorkImpl` constructs only a `StreamPlayer` (lines 43–47). The `AudioFileWriter fileWriter` field (line 27) is declared `@Nullable`, **never assigned**, and only checked in `finish()` (lines 80–86); WAV ship is dead code. `ShipMode {Playback, HLS, WAV}` (`ShipMode.java:11-14`) has no readers outside its own file. `BroadcastFactory.writer()` (`BroadcastFactory.java:34-36`) has no production caller; only `DemoIT` uses `AudioFileWriterImpl`.

`StreamPlayerImpl` (`StreamPlayerImpl.java`): `javax.sound.sampled.SourceDataLine` opened with the mixer's `AudioFormat` and a buffer of one mixer chunk (lines 27–35); `write` is a blocking `line.write(samples, 0, len)` (line 49); `getHeardAtChainMicros` is `line.getMicrosecondPosition()` (line 61). If the line is unsupported, `line = null` and the player silently drops audio (lines 28–32) while `getHeardAtChainMicros` would NPE.

### 5.2 WAV writer (`AudioFileWriterImpl.java`, exists but unused by ship)

`open(path)` creates a temp `.pcm` file and appends raw bytes (lines 48–77); `finish()` wraps the temp file in `AudioInputStream(format, byteCount)` and `AudioSystem.write(..., WAVE, outputFile)` (lines 92–101). Zero-byte output is skipped with a warning (lines 87–90). The pre-simplification nexus `ShipWorkImpl` (git `61c0cac75^:nexus/src/main/java/io/xj/nexus/work/ShipWorkImpl.java`) drove this with `OutputMode {PLAYBACK, FILE}` and `OutputFileMode {CONTINUOUS, SEGMENT, MAIN, MACRO}`: one WAV per segment, per main-program run, per macro-program run, or continuous, named `<prefix><shipKey>-<zeroPadded fileNum>[-<program-slug>].wav`, bytes shipped in multiples of `pcmChunkSizeBytes`, file boundaries snapped to segment ends (lines 221–295, 366–420 of that file). That is where `shipOutputFileNumberDigits`/`shipOutputPcmChunkSizeBytes` in `FabricationSettings` come from. Removed in commit `da52d1a08` "Simplify Java Codebase (#389)".

### 5.3 HLS segmenter design (recovered from git history; for the one-page design note the proposal keeps)

Current tree keeps only `Chunk` (`W/engine/ship/broadcast/Chunk.java`) and the `ChunkFactory` interface (no implementation). The working design, last complete in the `nexus` module at v1.6.7 (`git show 61c0cac75^:nexus/src/main/java/io/xj/nexus/ship/broadcast/*`) and with the encoder live in the earlier `ship` module (`git show 4c9b18f1d^:ship/src/main/java/io/xj/ship/broadcast/StreamEncoderImpl.java`):

**Chunk** (`Chunk.java:24-91`): `shipKey`, `sequenceNumber`, `fromChainMicros`, `durationMicros`, `fileExtension` (defaults to the encoder name). Key `<shipKey>-<seq>`; with bitrate `<shipKey>-<N>k-<seq>`; filename `<shipKey>-<seq>.<ext>`; `toChainMicros = from + duration`.

**Sequence numbering** (`MediaSeqNumProvider`): `seq = floor(chainMicros / (1e6 * chunkDurationSeconds))`, inverse `chainMicros = seq * chunkDurationSeconds * 1e6`. Media sequence is a pure function of chain time, so a restarted shipper continues numbering and chunk boundaries are at fixed chain-time multiples. `ChunkFactoryImpl.build` uses the parsed `#EXTINF` duration when present, else the target duration.

**Encoder** (`StreamEncoderImpl`, ship-module version lines 85–113): spawns

```
ffmpeg -v <verbosity> -i pipe:0 -ac 2 -c:a <encoder=aac> -b:a <N>k -maxrate <N>k -minrate <N>k
       -f hls -start_number <initialSeq> -initial_offset <initialSeq>
       -hls_flags delete_segments -hls_playlist_type event
       -hls_segment_filename <tmp><shipKey>-%d.<encoder> -hls_time <chunkTargetDuration=6>
       <tmp><shipKey>.m3u8
```

writes one WAV header for the mixer format to stdin, then streams raw PCM from a `ConcurrentLinkedQueue<ByteBuffer>` (`append`). The nexus version has this block commented out (lines 55–104), i.e. HLS was already disabled at 1.6.7.

**Publisher** (`PlaylistPublisherImpl`): `publish()` parses ffmpeg's local `.m3u8` (`parseItems`, regexes `^([A-Za-z0-9_]*)-([0-9]*)\.([A-Za-z0-9]*)` and `#EXTINF:([0-9.]*)`), accepts chunks only in strict `+1` sequence (`putNext`, line 146), uploads each media segment to S3, then rewrites the playlist itself (`getPlaylistContent`, lines 178–201):

```
#EXTM3U
#EXT-X-VERSION:4
#EXT-X-TARGETDURATION:<chunkDurationSeconds>
#EXT-X-MEDIA-SEQUENCE:<min seq in window>
#EXT-X-SERVER-CONTROL:HOLD-BACK=<3*chunkDuration + extra>.0
#EXTINF:<duration, 6 decimals, floor>,
<shipKey>-<seq>.<ext>
...
```

`collectGarbage(mediaSequence)` drops chunks with `seq < now-seq` (sliding window). `rehydrate()` fetches the previously published playlist over HTTP on start and resumes from its max sequence. Health: `maxSequenceNumber >= computeMediaSeqNum(toChainMicros)`.

**Defaults** (`ship/src/main/resources/application.properties` at `4c9b18f1d^`): `ship.chunk.target.duration=6`, `ship.chunk.audio.encoder=aac`, `ship.m3u8.content.type=application/vnd.apple.mpegurl`, `ship.m3u8.max.age.seconds=0`, `ship.m3u8.server.control.hold.back.extra.seconds=10`, `ship.media.sequence.number.offset=3`, `ship.playlist.ahead.seconds=30`, `ship.playlist.back.seconds=300`, `ship.mix.cycle.seconds=1`.

Design in one sentence for the note: PCM at the mixer format is piped to an external segmenter/encoder that cuts fixed-duration segments whose numbers are derived from chain time; a publisher tails the segmenter's playlist, uploads segments in order, and republishes a sliding-window EVENT playlist with HOLD-BACK = 3 segments. Open point already flagged in `11-resolutions.md` Q9: AAC licensing; the Java design hard-coded `aac`.

---

## 6. Output formats summary

| Path | Container | Encoding | Rate / channels | Where |
|---|---|---|---|---|
| Live playback | none (device line) | PCM S16LE | settings (48000 / 2 default) | `StreamPlayerImpl` |
| Mixer pipe | raw PCM | S16LE, `frameSize = 2*ch` | same | `MixerImpl.byteBufferOf`, `BytePipeline` |
| WAV capture (dead in 1.9.6) | WAVE via `AudioSystem.write` | S16LE | same | `AudioFileWriterImpl` |
| Build for engine | `build/<audioId>.wav` + `build/<slug>.json` | PCM by bits (16 default) via ffmpeg | settings | `ProjectManagerImpl.buildProject` |
| Resample cache | `render/instrument/<id>/<r>-<b>-<c>/<r>-<b>-<c>-<waveformKey>` | PCM by bits (16) | fabrication settings | `AudioCacheImpl.prepare` |
| HLS (historical) | `.aac` segments + `.m3u8` | AAC CBR | 2 ch | `StreamEncoderImpl` (removed) |

`mixerOutputMicrosecondsPerByte = 1e6 / (frameSize * frameRate)` (`DubWorkImpl.java:90`) = 5.208 µs/byte at 48 kHz stereo; `ShipWorkImpl` advances its own `shippedToChainMicros` by `bytes * that` (line 110) but reports the device position instead (line 98).

---

## 7. Where the Java design and the C++ engine agree

The C++ `DubWork` (`E/src/work/DubWork.cpp:106-174`) reproduces §2 verbatim in chain-absolute time: transient shift (130–143), stop computation (144–151), inclusion `start <= at + dubAhead && stop >= at` (152), `computeIntensityAmplitude(..., false, prev|next)` (162–171), `prevIntensity = nextIntensity` (174). There is no mixer, compressor, envelope, or cache in `E/` (grep `mixer|compressor` in `E/include`, `E/src` hits only `DubWork`/`WorkManager`/`TemplateConfig` config fields). So in V2 the engine supplies `ActiveAudio` events and `TemplateConfig` mixer fields; everything in §3–§5 is `xj-dsp`/`xj-content` work.

---

## 8. Implications for V2 specs

Facts, pitfalls, and seams a spec author must know, each traceable above.

1. **Filters are not a port.** No high-pass/low-pass DSP exists in Java (§3.5). `xj-dsp`'s master filters are new; the spec should pick a topology (e.g. 2nd-order Butterworth) and state that `mixerHighpassThresholdHz=30` / `mixerLowpassThresholdHz=15000` have never been audible in V1, so enabling them changes the sound of every existing template.
2. **The default compressor is a unity stage.** With `mixerCompressRatioMax = 1.0` and the log stage ahead of it, the look-ahead compressor never acts under template defaults (§3.7). Decide: port the algorithm and defaults (faithful, inert), or re-tune (audible change). Either way the spec must say which.
3. **Compressor state continuity is partial**: `compRatio` persists across chunks, `compRatioDelta` resets to 0 each chunk, and the look-ahead window is truncated at the chunk end (§3.7). A streaming V2 implementation with a true ring-buffer look-ahead will not be bit-identical; say so and define the V2 semantics (constant 50 ms look-ahead latency on the master, or zero-latency with truncated look-ahead).
4. **Log stage then compressor, no limiter, no clipping guard.** 16-bit conversion wraps above 1.0 (§3.9). The proposal's true-peak limiter is required, not optional, once filters or compressor boost are enabled.
5. **Amplitude ramps are chunk-relative.** `ActiveAudio.getAmplitude(ap)` runs 0→1 over the voice's portion of the current 1 s chunk (§3.3). V2 voices are continuous, so `gain_from → gain_to` must be defined over an explicit frame span (the proposal's `UpdateVoice {stop frame, gain to}` on each engine Update event is the right seam). The spec should state that V1's effective behavior was linear interpolation of intensity at ~1 s resolution, and V2 may interpolate at the engine cycle instead.
6. **Release is a quarter-sine fade of the still-playing source**, default 5 ms (240 frames at 48 kHz), applied after `stopAtMixerMicros`; there is no attack fade and no click protection at voice start (§3.3–3.4). The V2 voice spec should keep the release shape (`sin(π/2 · i/N)`) for parity and may add a short attack.
7. **Channel handling**: mono sources are duplicated to both outputs (`sc = tc % srcCh`), stereo sources into a mono output take the left channel only, there is no panning (§3.3). V2 should define stereo→mono as a sum (−3 or −6 dB) and keep mono→stereo duplication.
8. **Buses are enum-ordered in practice** because `mixerInit` pre-touches all ten types (§3.2); the proposal's "allocate all ten up front" matches real behavior. Bus gain = `dubMasterVolume` (default 1.0 everywhere). No bus mute/solo/meter/FX existed.
9. **The cache key has no content identity.** In-memory by audio UUID; on disk by `(instrumentId, rate, bits, channels, waveformKey)` (§4.1). The Commons' BLAKE3 `contentHash` fixes a real V1 bug (stale cache after replacing a sample). The V2 cache key should be `(contentHash, targetRate, targetChannels, [stretch ratio, semitones])` and drop `instrumentId`.
10. **Mono-at-wrong-rate is up-mixed to stereo** by the Java cache (§4.1), contradicting the mono-stays-mono intent. V2 should resample without changing channel count and let the voice handle layout.
11. **Everything is pre-resampled to the output rate**; the mixer never resamples (§3.3, §4). `rubato` in `xj-content` replaces ffmpeg's libswresample; byte-exact parity with V1 renders is impossible and should not be a test goal. `DemoIT`'s fixtures (§3.10) are reusable with a tolerance.
12. **`lengthSeconds` ambiguity**: dub uses the content-model `InstrumentAudio.lengthSeconds` for one-shot stop time, not the decoded buffer length (§2 step 4, §4.2). If a file is shorter than its declared length, the loop just stops adding (§3.3). V2 should decide which length wins and whether to clamp.
13. **Timing cursors and the busy loop.** V1 has three cursors with the device line position as "now" (§1), no sleeps, and a 10 MB pipe. The `xj-transport` crate is the formalization; the spec should carry over the invariants `shipped ≤ dubbed ≤ crafted`, `dubbed − shipped ≤ dubAhead + mixerLength`, and that the device position, not wall clock, is chain time.
14. **Intensity override latency in V1 is one dub chunk plus dub-ahead** (`setIntensityOverride` → next `doDubFrame`, §2 step 2): up to `mixerLength + dubAhead` ≈ 3 s with workstation defaults. The proposal's "intensity with a ramp time" at DSP level is a change in kind; the spec should route intensity to the DSP as a gain-target command, while still feeding the engine so future picks are chosen consistently.
15. **Default values differ in three places**: `MixerConfig` class defaults (never used in production), `TemplateConfig.DEFAULT` (authoritative for sound), and `application.properties` (authoritative for timing). The V2 `SetMasterParams` command should be populated from `TemplateConfig` only, and the spec table should quote `TemplateConfig.DEFAULT` lines 102–111.
16. **Ship in 1.9.6 is playback only**: WAV and HLS are dead or deleted (§5.1). The proposal's "replaces the Java workstation's WAV export" refers to `AudioFileWriterImpl` plus the removed nexus `OutputFileMode` logic; the useful V1 behavior worth keeping is file-boundary snapping to segment / main / macro boundaries, which V2 Capture can reinstate as an option.
17. **HLS note contents** are in §5.3: chain-time-derived sequence numbers, 6 s target, EVENT playlist with HOLD-BACK = 3 segments + 10 s, strict in-order publication, playlist rehydration on restart. The encoder was AAC via the `ffmpeg` CLI; the GPL `ffmpeg-platform-gpl` artifact is also what the workstation bundles today, which is its own licensing reason to drop ffmpeg.
18. **`mix(…, intensity)` ignores `intensity`**, `mixerNormalization*` is dead config, `Envelope.in`, `MathUtil.left/right`, `busCompRatio`, `ShipMode`, `shipOutputPcmChunkSizeBytes` are unused (§3.5, §3.8, §5.1). Do not carry them into V2 config schemas as if they did something; if `TemplateConfig` JSON keeps the keys for file compatibility, the spec should mark them "accepted, ignored".
19. **Allocation on the audio path.** V1 zeroes ~1M floats and allocates a `float[]` per frame in the log stage per chunk, on a non-real-time thread with a 1 s buffer (§3.1, §3.6). None of this is a model for `xj-dsp`; the proposal's no-allocation callback is a redesign, and the fixed voice pool (256) should be sized against V1's reality: `getSegmentsIfReady` includes the previous and next segments, so the live voice count is all picks in up to three segments plus release tails.
