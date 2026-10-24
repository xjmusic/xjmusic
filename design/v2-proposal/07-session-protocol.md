# 07 · Sessions: many stations, one foundation

How several XJ stations in one room stay in the same tempo and the same chord changes while each plays its own material. This is the part of the vision that sounds most exotic and turns out to be the most tractable, because beat synchronization on a LAN is a solved problem and the engine's foundation/layers seam does the rest.

## Goals and non-goals

**Goals**
- Up to 16 stations on a venue LAN share tempo, beat phase, downbeat, key, chord changes and memes.
- Each station chooses and plays its own beat, detail, transition and background material against the shared foundation.
- A station joins or leaves without a dropout, at a musical boundary.
- The conducting role can be handed from one station to another at a musical boundary.
- Stations can claim layers so two of them do not both play bass.
- Everything degrades gracefully: a station that loses the network keeps playing.

**Non-goals for 2.0**
- Audio over the network. Each station feeds the venue's mixer like any other instrument.
- Sessions over the public internet.
- More than one conductor at a time.
- Meter changes mid-session.

## Concepts

| Term | Meaning |
|---|---|
| **Session** | A named group of stations sharing one foundation and one show |
| **Station** | One running `xj-station`, with or without a UI |
| **Conductor** | The one station whose engine runs macro/main craft and publishes the foundation |
| **Follower** | A station whose engine runs in follow mode and consumes the foundation |
| **Foundation record** | The resolved harmonic and rhythmic facts of one segment: begin, length, type, key, tempo, intensity, chords, voicings, memes |
| **Harmonic timeline** | The ordered sequence of foundation records, published a few segments ahead |
| **Layer claim** | A station's declaration that it owns an instrument type for now |
| **Cue request** | A follower asking the conductor to apply a foundation-level cue |
| **Handoff** | Transfer of the conductor role at a segment boundary |

## Clock

### Ableton Link

Tempo, beat phase and start/stop are synchronized with **Ableton Link**. Reasons:

- It is peer-to-peer on a LAN, with no master, and converges in well under a second.
- It is what the target audience's gear already speaks. Ableton's list of Link-enabled products includes the Akai Force, MPC One, MPC Live, MPC Live II and MPC X, alongside Ableton Live, monome norns, Denon's Prime 4 and hundreds of apps. An MPC on stage can lock to the same beat grid as the XJ stations, which closes the loop on where this project began.
- It is open source with a mature Rust binding (`rusty_link`).

Link is dual licensed, GPLv2+ or a proprietary license from Ableton, and the XJ license is not GPL-compatible, so the proprietary license is requested in Phase 0 ([11-resolutions.md](11-resolutions.md), Q3). The protocol does not depend on Link. `xj-transport` exposes a `BeatClock` trait with two implementations behind feature flags. `link` wraps Ableton Link through `rusty_link`. `session` is a conductor-hosted clock: followers estimate their offset to the conductor with NTP-style round trips over the session connection, and the conductor's beat timeline is the truth. The `session` clock also emits MIDI clock so non-Link hardware can follow a station. Sessions in CI run on the `session` clock; shows with Link-enabled gear in the room run on `link`.

### Mapping chain time to beats

Every station has its own chain time (microseconds since its engine started). They are not equal and need not be. The session establishes an **epoch**: the conductor declares that its chain time *T₀* corresponds to Link beat *B₀*, and that a bar is `barBeats` long starting at *B₀*. Every foundation record carries both the conductor's chain time and the Link beat of its start. Followers treat the beat as truth and compute their own chain time from it through `xj-transport`.

Tempo is owned by Link. The conductor's engine runs with the tempo policy set to `External` ([04-engine.md](04-engine.md) section 2) so that its foundation records carry the Link tempo. Followers do the same. A tempo nudge from any Link peer, including an MPC, reaches every station the same way.

## Transport

- **Discovery:** mDNS service `_xjsession._tcp` advertised by the conductor with the session name and a short join code.
- **Connection:** followers connect to the conductor over QUIC (`quinn`) or TCP with length-prefixed JSON messages. One connection per follower. QUIC is preferred for its multiplexing and its behavior on lossy Wi-Fi; TCP is the fallback.
- **Join code:** a four-character code shown on the conductor's screen and typed on the follower, so a station cannot wander into the wrong session at a festival with three stages on one network.
- **Wired recommended.** The protocol works on Wi-Fi, and the timing budget below shows how much slack there is, but a venue should run cable.

Multicast was considered and rejected: venue Wi-Fi and managed switches handle it badly, and the message volume is tiny.

## Messages

JSON objects with a `type` field. Only the ones that carry structure are shown in full.

| Type | Direction | Purpose |
|---|---|---|
| `hello` | follower → conductor | Station id, name, engine and ABI versions, join code, `show.lock` hash |
| `welcome` | conductor → follower | Session id, epoch (*T₀*, *B₀*, barBeats), member list, current claims, the next few foundation records |
| `reject` | conductor → follower | Reason: bad code, version mismatch, show lock mismatch |
| `foundation` | conductor → all | One foundation record |
| `retract` | conductor → all | "Discard every record from segment N onward"; sent when a cue rewrote the conductor's future |
| `claim`, `release` | any → conductor → all | Layer claims |
| `cue_request` | follower → conductor | A foundation-level cue the follower wants applied |
| `cue_result` | conductor → follower | Accepted (with landing time) or declined (with reason) |
| `handoff_offer`, `handoff_accept`, `handoff_commit` | see below | Conductor role transfer |
| `presence` | all, periodic | Heartbeat with state, round-trip estimate, meters summary |
| `bye` | any | Clean departure |

### The foundation record

This is the same JSON the engine produces from `xj_engine_get_foundation_json` and consumes through `xj_engine_push_foundation_json`, with the session envelope added:

```json
{
  "type": "foundation",
  "session": "b3c1…",
  "seq": 418,
  "segment": {
    "id": 418,
    "segmentType": "NextMain",
    "beginBeat": 1672.0,
    "beginChainMicros": 812345678,
    "totalBeats": 16,
    "barBeats": 4,
    "key": "A minor",
    "tempo": 124.0,
    "intensity": 0.72,
    "delta": 24
  },
  "chords": [
    { "position": 0.0,  "name": "Am" },
    { "position": 4.0,  "name": "F" },
    { "position": 8.0,  "name": "C" },
    { "position": 12.0, "name": "G" }
  ],
  "voicings": [
    { "chordPosition": 0.0, "instrumentType": "Bass", "notes": "A2" },
    { "chordPosition": 0.0, "instrumentType": "Pad",  "notes": "A3, C4, E4" },
    { "chordPosition": 0.0, "instrumentType": "Stab", "notes": "C4, E4, A4" }
  ],
  "memes": ["COOL", "CHICAGO", "MID", "NIGHT"],
  "foundationChoice": { "macro": "Fire", "main": "Fire Main 2", "mainOffset": 1 }
}
```

Voicings are included because the engine already computes them per instrument type and every detail craft reads them. Publishing them, rather than only chord names, means followers do not need the conductor's main programs and do not need to agree on voicing rules. The `foundationChoice` is informational, for display.

## Roles and handoff

One conductor at a time. Handoff is a three-step exchange that completes at a segment boundary:

1. The current conductor sends `handoff_offer` naming the candidate and the earliest segment at which the transfer can occur (the first segment beyond the conductor's own commit horizon that is also beyond the candidate's craft lead time).
2. The candidate's station confirms its engine can start conducting at that segment and replies `handoff_accept`.
3. The conductor sends `handoff_commit` with the segment number. From that segment the candidate's engine switches its foundation source from `External` to `Internal` and begins publishing; the old conductor switches from `Internal` to `External` and begins following. Everyone else just sees a new sender on the same timeline.

The new conductor's first segment is of type NextMacro, so a handoff is musically what every station already hears at each macro change: a section change, not a break. The patent describes transposing a succeeding macro sequence so that its opening key aligns with the preceding one; today's `MacroMainCraft` takes the key from the chosen main sequence as written. Whether 2.0 adds key alignment at NextMacro boundaries is a craft question outside this proposal's non-goals, and handoff does not depend on it.

## Layer claims

A claim says "this station owns instrument type X for now." When a claim is accepted and broadcast, every *other* station's engine receives a Layer Mute cue for that type at the next segment, and its DSP bus for that type mutes at the next bar. Releasing a claim reverses both.

Claims are first-come and visible. A station may override a claim locally ("doubling allowed") and the UI shows the clash. There is no arbitration beyond that in 2.0; a performer who wants to fight for the bassline can, and the audience will hear it.

Claims are the mechanism behind "one machine sustaining a base layer while the other disrupts it." The default claim set for a two-station session is: station A claims Drum and Bass, station B claims nothing. B's engine then crafts everything but drums and bass against A's foundation.

## Cue requests

A follower's foundation-level cue (Macro, Memes, Chord, Advance, Hold) is rejected by its own engine and forwarded as a `cue_request`. The conductor applies a policy: accept all, accept from a named list, or ask the conductor's performer. The result returns to the requester with the landing time, which the requester's UI shows exactly as it would show a local cue. In a duet with "accept all" this makes the two stations symmetric in practice: either performer can steer the harmony, and the conductor role is only a question of whose engine does the arithmetic.

## Failure handling

| Failure | Follower behavior | Conductor behavior |
|---|---|---|
| Conductor disappears | Hold the last foundation for up to `followHoldMaxBars`, then switch to `Internal` at a segment boundary. Rejoin when the conductor returns, at a NextMacro boundary. | — |
| Follower disappears | — | Release its claims after a timeout; everyone else's engines un-mute those layers at the next segment |
| Link peers vanish | Keep the last tempo; the session continues on its own beat timeline | Same |
| Foundation record arrives too late to craft against | Hold for that one segment, log a warning, show a late-record counter in the UI | — |
| `retract` arrives for segments already handed to the DSP | Impossible by construction: the conductor's commit horizon is at least as long as any follower's, because the follower's horizon is derived from the conductor's in `welcome` | — |
| Version or show-lock mismatch | Refused at `hello` with a reason the UI shows | — |

## Timing budget

The question is whether followers have enough time to craft their layers after learning the foundation.

| Quantity | Typical | Notes |
|---|---|---|
| Conductor craft-ahead | 20 s (default), 8 s in Play mode | Foundation for a segment exists this far ahead of its start |
| Network delivery | under 10 ms wired, under 100 ms on reasonable Wi-Fi | |
| Follower segment craft | tens of ms | To be measured in Phase 0 and reported by the engine |
| Follower dub-ahead | 2 s in Play mode | Audio must be scheduled this far ahead |
| **Slack** | **5 s or more** in Play mode, 15 s or more otherwise | Enough that no plausible LAN condition matters |

The conductor's cues are what consume the slack: a cue landing at the next bar forces the conductor to rewrite its future from that bar, publish a `retract`, then publish new records. Followers then have (landing time − now − their dub-ahead) to craft. With a one-beat commit horizon at 124 bpm that is about half a second minus two seconds of dub-ahead, which is negative. So in a session the **effective commit horizon is the conductor's commit horizon plus every follower's dub-ahead**, and the conductor computes and shows that. In practice: solo stations react at the next beat; sessions react at the next bar. That is the honest cost of sharing a foundation, and it is a cost performers already understand from playing with other humans.

## Security

LAN only. A join code prevents accidents, not attackers. Packs are signed. No message in the protocol executes anything; every message is data applied through the engine's existing cue and foundation paths. A future internet mode would need authentication and is out of scope.

## Testing

- **Two engines, one process.** The follow-mode test from [04-engine.md](04-engine.md) with the session crate in between and an in-memory transport. Assert identical foundations per segment span.
- **Simulated network.** A test transport that injects latency, jitter, loss and reordering. Assert the hold and rejoin paths.
- **Handoff.** Transfer the role back and forth every N segments for an hour; assert no gaps and no duplicate publishers.
- **Soak.** Three headless stations, one of them an `xj-station` CLI in a container, eight hours, with a scripted performer sending cues. Assert zero late records on a wired network and bounded late records on a lossy simulation.
- **Real gear.** An MPC on Link beside two stations, a human listening for drift. This one is not automated and it is the one that matters.

## Alternative considered: deterministic lockstep

Give every station the same seed, the same main-program content and the same cue log, and let each compute the foundation itself; no harmonic timeline needs to be sent. This is how lockstep multiplayer games work. It was rejected for 2.0 because it requires identical foundation content on every station (which conflicts with "each artist brings their own material"), it requires bit-identical floating-point behavior across machines (which [04-engine.md](04-engine.md) deliberately does not promise), and it turns every divergence into a silent musical disagreement rather than a visible network event. The conductor model costs a few kilobytes per segment and gives a single source of truth. If a future show runs identical hardware and wants zero network dependence, lockstep is available as a mode; the seeded randomness work in 2.0 is the prerequisite either way.
