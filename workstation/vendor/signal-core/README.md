# Signal core for Java (vendored)

This is **Signal's Java core**, copied unchanged from `sdk/java/signal-core/` of Signal's own repository, which is private. It is here so the workstation builds without it, and so anyone can read every line of what usage reporting does. Gradle compiles it with the workstation (`sourceSets.main.java.srcDir` in [`../../build.gradle`](../../build.gradle)).

What it is, and nothing more:

- `SignalCore`: adds a session's numbers up in memory, and folds them into one summary of a few dozen bytes.
- `Wire`: the summary's bytes (Signal's wire protocol v1: a fixed layout of numbers, no text).
- `SignalClient`: the desktop client around the core: the consent states (nothing recorded to disk or sent before a yes), the two small files it keeps, one POST per session end to `https://in.signal.outright.io/v1/s`, at most eight requests a day, a backoff when the server is busy, and a `finish()` that waits at most a second at quit.
- `SignalStore`, `SignalSpool`, `SignalTransport`, `SignalHost`, `Rules`, `SignalLedger`, `SignalCatalog`, `Json`: what the client is made of. `SignalHost` reads the kill switches and the device class, from cores and memory only.

It has no dependencies: the JDK alone.

`fixtures/golden.json` is Signal's golden fixtures, pinned by `fixtures/golden.sha256`. [`SignalCoreGoldenTest`](../../src/test/java/io/xj/gui/usage/SignalCoreGoldenTest.java) plays every fixture through this copy and checks it writes the reference bytes, byte for byte, as Signal's Dart, Godot and Node implementations do.

To update it, copy `sdk/java/signal-core/src/main/java/` and `fixtures/golden.json` and `fixtures/golden.sha256` from Signal again, and run `./gradlew test`.

What the workstation sends with it: [PRIVACY.md](../../../PRIVACY.md).
