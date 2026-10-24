# 08 · Performer input: from gesture to music to mix

The vision's central claim is that automation at the lowest level of the performance elevates the performer. This document makes that mechanical: how a touch on a pad becomes a musical decision, which part of the system makes it, and when the room hears it.

## The problem in one paragraph

The engine plans ahead. It has to: crafting a segment means choosing programs, chords, voicings, instruments and samples for the next several bars, and the audio for those bars is scheduled before they arrive. A performer wants the room to respond now. Both are right, and the only way to satisfy both is to admit that performer input exists at more than one time scale and to route each scale to the part of the system that can honor it. V1 collapsed everything into three overrides that all rewrote the future. V2 separates three scales.

## Three scales, three destinations

| Scale | Horizon | Question it answers | Examples | Destination | Lands at |
|---|---|---|---|---|---|
| **Direction** | Next minutes | Where is this going? | Change scene (macro), change meaning (memes), set the intensity trajectory, leave this section, stay in this section | Engine, as a **cue** | Chosen boundary: segment or section |
| **Articulation** | Next bars or beats | When does it change, and how? | Next chord, bring a layer in or out, land a change on *this* bar | Engine as a cue **and** DSP as a gesture at the same boundary | Chosen boundary: beat or bar |
| **Gesture** | Now | What happens this instant? | Hit, sweep, drop, stutter, mute, solo, fill | DSP only | Next buffer, or the next beat if quantized |

The engine never sees a gesture, and a gesture never changes what the engine will choose. The engine sees every cue, and a cue never produces sound before the commit horizon. The UI shows, for every control, which of the two it is and when it will land.

### Why this resolves the tension

- A **drop** is the canonical test. The performer wants silence except drums, now, and then the music to come back changed. In V2 "drop" is a *move* (see below) composed of: a gesture that mutes every bus except Drum at the next beat; a cue that sets intensity to 0.2 with a four-bar ramp back, landing at the next bar; and an Advance cue at the next segment so the material that returns is new. The room hears the drop on the beat. The engine learns about it in time to make the return musical.
- A **chord change** the performer wants on the next downbeat is a Chord cue at the bar boundary. If the next downbeat is inside the commit horizon, the UI says "lands in 2 bars" before the performer commits, and the performer decides. That honesty is the feature.
- A **filter sweep** on the pads is a gesture. It is audible within one buffer, it never involves the engine, and it never breaks the harmony because it does not touch what is played, only how it sounds.

## Cues

Defined in [04-engine.md](04-engine.md) section 6. Each cue has a type, a payload and a boundary:

| Cue | Payload | Default boundary | Notes |
|---|---|---|---|
| Macro | program id | Segment | "Scene change" on the surface |
| Memes | set of taxonomy memes | Segment | Palette buttons |
| Intensity | target, ramp beats | Beat | The only cue applied in dub rather than craft |
| Chord | chord name, optional key | Bar | Harmony strip |
| Advance | — | Segment | Leave the current section |
| Hold | — | Segment | Stay in the current section |
| Layer mute | instrument type, on/off | Segment | Also sent as a gesture so it is audible at once |

In a session, foundation cues (Macro, Memes, Chord, Advance, Hold) from a follower become cue requests to the conductor ([07-session-protocol.md](07-session-protocol.md)). Intensity and layer cues stay local.

## Gestures

Defined by the DSP command set in [05-workstation.md](05-workstation.md):

| Gesture | Acts on | Quantize options |
|---|---|---|
| One-shot | A sample from a chosen instrument, on its bus | None, beat, half-beat |
| Bus mute / solo | A bus | None, beat, bar |
| Bus gain | A bus | None; ramped |
| Effect parameter | A bus insert (filter cutoff and resonance, delay time and feedback, reverb mix, saturation) | None; ramped |
| Stutter | Retrigger the current voices on a bus at a beat division for as long as held | Beat division |
| Fill | Play a designated fill sample on the Drum bus at the next bar, muting the Drum voices under it | Bar |

One-shot samples are ordinary `InstrumentAudio` entries in the project, chosen by the performer from the running template's instruments, so a pack author can ship performance hits beside the material the engine plays.

## Moves

A move is a user-composed bundle of gestures and cues with relative timing, saved in the mapping file and bound to one control. Moves are how a performer builds their own vocabulary ("my drop", "my build", "my hand-off to the other station") without the application having to anticipate it. A move is replayable, previewable (the engine previews its cues; the DSP part is described in words), and shareable as part of a pack.

## Input sources

| Source | Library | Notes |
|---|---|---|
| MIDI notes, CCs, program changes, NRPN | `midir` | Hot-plug; multiple devices; per-device mapping |
| OSC | `rosc` | For custom controllers, tablets, and integrations such as lighting desks that want to *send* as well as read |
| Keyboard | toolkit | The whole surface is drivable from a laptop keyboard; this is the fallback and the demo path |
| Gamepad | `gilrs` | Cheap, ubiquitous, has sticks for continuous control |
| On-screen | toolkit | Every control works by mouse or touch |

## Mapping

A TOML file per controller profile, with a learn mode in the UI that writes it. Example for a 4×4 pad controller:

```toml
[profile]
name = "Generic 4x4 pads + 8 knobs"
match = { manufacturer = "*", product = "*Pad*" }

[[map]]
input = { midi = "note", channel = 10, note = 36 }
cue   = { type = "macro", program = "Fire", boundary = "bar" }
led   = { on = "amber", armed = "amber-blink", off = "dim" }

[[map]]
input = { midi = "note", channel = 10, note = 37 }
move  = "my-drop"

[[map]]
input   = { midi = "cc", channel = 1, cc = 7 }
cue     = { type = "intensity", ramp_beats = 2 }

[[map]]
input   = { midi = "cc", channel = 1, cc = 74 }
gesture = { type = "fx", bus = "Pad", slot = 0, param = "cutoff", curve = "log" }

[[map]]
input   = { midi = "note", channel = 10, note = 48 }
gesture = { type = "oneshot", instrument = "Perf Hits", audio = "crash-big", bus = "Drum", quantize = "beat" }

[[map]]
input   = { midi = "note", channel = 10, note = 49 }
session = { type = "claim", layer = "Bass", toggle = true }

[moves.my-drop]
steps = [
  { at = "beat",    gesture = { type = "mute", bus = "*", except = ["Drum"] } },
  { at = "bar",     cue     = { type = "intensity", target = 0.2, ramp_beats = 16 } },
  { at = "segment", cue     = { type = "advance" } },
  { at = "bar+4",   gesture = { type = "unmute", bus = "*" } },
]
```

The mapping references programs, instruments and audios by name. Names resolve against the loaded show, so a profile works across packs that follow the same naming, and the UI flags anything that does not resolve.

## Feedback to the controller

Pads and buttons get LED states from the same mapping: *off*, *available*, *armed* (cue pending, landing soon) and *active* (the current scene or meme). Knobs with rings get the current value. This is what makes a controller usable without looking at the screen, and it uses MIDI out through the same `midir` connection.

## Decision information beyond buttons

The vision speaks of "sourcing events and decision information from the live performer." Buttons and faders are events. Decision information is subtler: how actively the performer is playing, how long they have left a section alone, what the room is doing. V2 adds a small **influence** layer: named float signals, smoothed, that bias (never set) the intensity cue and the engine's existing auto-crescendo.

| Signal | Source | Effect |
|---|---|---|
| Control activity | Rate of mapped inputs over the last 8 bars | High activity nudges intensity up; silence lets the auto-crescendo take over, as it does today |
| Room level | An OSC float from a microphone or the FOH desk | Optional; a venue can wire crowd energy to a gentle intensity bias |
| Time in section | Bars since the last Advance | Suggests, on the surface, that a change is overdue; never acts on its own |

Influence is optional, off by default, visible on the surface when on, and bounded in magnitude. The performer always wins. This is deliberately modest: the honest version of "machine intelligence sourcing decisions from the performer" in 2.0 is a few well-chosen signals with clear effects, not a model guessing what the performer meant.

## Legibility

Every mapped control answers three questions before it is touched:

1. **What will it do?** The engine's preview for cues ("Fire, A minor, 124"); a plain description for gestures and moves.
2. **When?** The landing time, from `earliest_landing`, in bars and beats, drawn on the harmony strip.
3. **Why did the last one do that?** The "why" panel for the resulting choice.

And one question after: **what did I do?** The cue log, with beat timestamps, is part of every capture.

## Capture and replay

A capture is the chain's seed, the show lock, the cue log with beat timestamps, the gesture log with frame timestamps, and the bounced audio. Given the same engine build and content, replaying the cue log reproduces the same chain ([04-engine.md](04-engine.md) section 5), and replaying the gesture log through the DSP reproduces the same mix. This is how a performance becomes an album, how a bug in a show becomes a reproducible test, and how a mapping profile is tested in CI without a human.

## Latency, end to end

| Path | Budget |
|---|---|
| Gesture, unquantized | Under 10 ms at a 256-frame buffer |
| Gesture, beat-quantized | Within one frame of the beat |
| Intensity cue | Next beat (applied in dub) |
| Layer cue | Audible at once via the paired gesture; craft follows at the next segment |
| Foundation cue, solo station | Chosen boundary, not inside the commit horizon (default one beat in Play mode) |
| Foundation cue, in a session | Chosen boundary, not inside the conductor's effective horizon (typically the next bar) |

## Dependencies on other documents

- The engine's cue API, commit horizon, preview and diagnostics: [04-engine.md](04-engine.md).
- The DSP command set, buses and effects: [05-workstation.md](05-workstation.md).
- Cue requests and layer claims in a session: [07-session-protocol.md](07-session-protocol.md).
