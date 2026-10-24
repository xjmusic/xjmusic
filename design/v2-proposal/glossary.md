# Glossary

Terms used across the V2 proposal. Terms marked **(new)** are introduced by V2; the rest exist in V1 and are defined here as the engine uses them.

| Term | Meaning |
|---|---|
| **Beat time** (new) | Time in beats on the shared Link timeline. One of the four time bases in [03-architecture.md](03-architecture.md). |
| **Boundary** (new) | A musical moment at which a cue may land: now, beat, bar, segment, main (section), macro. |
| **Capture** (new) | A recording of a performance as data: seed, show lock, cue log, gesture log, and bounced audio. Replayable. |
| **Chain** | The unbounded sequence of segments an engine fabricates from a template. |
| **Chain time** | Microseconds since the engine's chain began. The engine's own clock. |
| **Choice** | A segment's selection of one program (macro, main, beat, detail) or one instrument, from which arrangements and picks follow. |
| **Commit horizon** (new) | The span of upcoming audio that has already been handed to the DSP and cannot be changed. Cues land beyond it. |
| **Commons** (new) | The public registry of packs, and the content in it. |
| **Conductor** (new) | The station whose engine runs macro/main craft and publishes the foundation to a session. |
| **Craft** | The engine's process of choosing and arranging content for a segment. Five stages: macro/main, beat, detail, transition, background. |
| **Craft-ahead** | How far ahead of now the engine keeps segments crafted. Default 20 s. |
| **Cue** (new) | A performer's request to the engine, with a type, a payload and a boundary. Cues change what the engine will play. |
| **Delta arc** | The template feature that staggers layers in and out across a main program's length. |
| **Device time** (new) | Sample frames since the audio device stream began. The DSP's clock. |
| **Dub** | The engine's process of turning crafted segments into scheduled audio within the dub-ahead window. In V2 the DSP renders what dub schedules. |
| **Dub-ahead** | How far ahead of now audio is scheduled. Default 10 s. |
| **Follower** (new) | A station whose engine runs in follow mode and consumes the foundation from a conductor. |
| **Foundation** (new) | The resolved harmonic and rhythmic facts of a segment: begin, length, type, key, tempo, intensity, chords, voicings, memes. What macro/main craft produces. |
| **Foundation source** (new) | Whether a station's engine computes its own foundation (internal) or ingests one (external). |
| **Gesture** (new) | A performer's immediate action on the DSP: a hit, a sweep, a mute. Gestures change how audio sounds, never what the engine chooses. |
| **Handoff** (new) | Transfer of the conductor role between stations at a segment boundary. |
| **Harmonic timeline** (new) | The ordered sequence of foundation records a conductor publishes. |
| **Influence** (new) | Named, smoothed float signals that bias intensity without setting it. |
| **Instrument** | A group of audio samples with musical metadata. Has a type (Background, Bass, Drum, Hook, Pad, Percussion, Stab, Sticky, Stripe, Transition) and a mode (Chord, Event, Loop). |
| **Intensity** | A continuous 0–1 dimension of a segment that layers respond to through thresholds and layer counts in the template. |
| **Layer** | An instrument type, as a mixable and claimable unit. |
| **Layer claim** (new) | A station's declaration in a session that it owns an instrument type for now. |
| **Link** | Ableton Link, the LAN protocol for shared tempo, beat phase and start/stop. |
| **Macro program** | The program type that sets the long-range shape of the chain: a sequence of memes and keys across its bindings. |
| **Main program** | The program type that sets a section's chords, key and voicings. |
| **Marble bag** | The engine's phased, weighted random chooser. |
| **Meme** | A tag carrying meaning (COOL, CHICAGO, NIGHT) on programs, sequence bindings and instruments; the engine's unit of semantic matching. |
| **Meme stack** | The engine's validator that refuses contradictory meme combinations (anti, numeric, unique, taxonomy axioms). |
| **Move** (new) | A user-composed bundle of gestures and cues with relative timing, bound to one control. |
| **Pack** (new) | A distributable unit of content: a manifest, V1-shaped content, content-addressed audio, an optional preview. |
| **Performance surface** (new) | The Play-mode screen: scenes, palettes, intensity, harmony strip, layers, timeline. |
| **Pick** | One scheduled use of one audio sample for one event, with start, length, amplitude and tones. |
| **Preview** (new) | The engine's answer to "what would this cue do," computed without committing. |
| **Program** | A partial composition: sequences bound at offsets, with chords, voicings, patterns and events. Types: Macro, Main, Beat, Detail. |
| **Retract** (new) | A session message telling followers to discard foundation records from a segment onward, after the conductor rewrote its future. |
| **Scene** (new) | The performance surface's name for a macro program. |
| **Segment** | One crafted span of the chain with a type (Initial, Continue, NextMain, NextMacro), key, tempo, chords, memes, intensity and choices. |
| **Session** (new) | A named group of stations sharing one foundation and one show. |
| **Show** (new) | A manifest naming the packs and templates a session plays from, plus a lockfile pinning them. |
| **Starter pack** (new) | The pack bundled with the workstation so that first launch has something to play. |
| **Station** (new) | One running `xj-station`, with or without a UI. |
| **Sticky bun** | Per-event persisted random choices so that an event keeps the same realization across segments. |
| **Template** | A show's rules: which content is bound, the meme taxonomy, intensity layers, mixer settings, and the new V2 horizon settings. |
| **Taxonomy** | The template's categories of mutually exclusive memes, e.g. `CITY[CHICAGO,DENVER,PHILADELPHIA]`. |
| **Tempo policy** (new) | Whether tempo comes from content (as in V1) or from the host (Link). |
| **Tones** | The pitches recorded in an audio sample or required by an event; the engine matches these to pick samples. |
| **Voicing** | The specific notes an instrument type plays for a chord in a segment. |
| **Why panel** (new) | The UI view of the engine's choice diagnostics: candidates, weights, exclusions. |
