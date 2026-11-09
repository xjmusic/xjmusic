# Gap map: MIDI device identity in midir 0.11.1, and the LED protocols of two reference 4x4 controllers

Scope: what `xj-control` can actually learn about a plugged-in controller through `midir` 0.11.1 on each desktop backend, how stable that identity is, whether manufacturer / product / USB VID:PID are reachable without a USB crate, and the concrete pad-note and LED protocols of the Novation Launchpad Mini [MK3] (Programmer mode) and the Akai MPD218. It answers the questions left open by `external-stack-research.md` section 3 (midir has no hot-plug callback) against `08-performer-input.md` ("Mapping", "Feedback to the controller") and `05-workstation.md:86`.

Sources read for this map (all local copies are in the session scratchpad; line numbers below refer to them):

| Source | Local path / URL |
|---|---|
| midir 0.11.1 crate (crates.io tarball) | `(research workspace) midir-src/midir-0.11.1/src/...` |
| coremidi 0.9.0, alsa 0.9.0 / 0.12.1, alsa-sys 0.6.1 crates | `.../scratchpad/coremidi-src/coremidi-0.9.0/`, `.../scratchpad/alsa-src/` |
| Apple CoreMIDI SDK headers | `/Library/Developer/CommandLineTools/SDKs/MacOSX15.sdk/System/Library/Frameworks/CoreMIDI.framework/Headers/MIDIServices.h`, `MIDISetup.h` |
| Linux kernel (via `gh api`, master) | `.../scratchpad/scripts/seq_clientmgr.c`, `seq_midi.c`, `midi.c` (sound/usb), `card.c` (sound/usb), `usb_proc.c`, `init.c` |
| mingw-w64 `ksmedia.h` (USB-audio GUID macros) | `.../scratchpad/scripts/ksmedia.h` |
| JUCE native MIDI (reference implementations) | `.../scratchpad/scripts/juce_midi_win.cpp`, `juce_midi_mac.mm` |
| Launchpad Mini [MK3] Programmer's Reference (23 pp, mirror) | `https://static.frigolit.net/files/novation_launchpad_mk3_mini_-_programmers_reference_manual.pdf`; text extracted to `.../scratchpad/pdftext/launchpad.txt` |
| lpminimk3 Python library (numeric layouts, palette groups, port-name regexes) | `github.com/obeezzy/lpminimk3` → `.../scratchpad/scripts/lp_components.py`, `lp__colors.py`, `lp_system_midi_port_parser.py`, `lp_midi_messages.py` |
| Akai MPD218 User Guide v1.0 | `https://library.yorku.ca/ds/wp-content/uploads/2024/09/MPD218-UserGuide-v1.0.pdf`; text in `.../scratchpad/pdftext/mpd218.txt` |
| mungewell/mpd-utils (MPD218 preset SysEx format) | `.../scratchpad/scripts/mpd218.py` |
| The repo itself | `grep -rIl -i midi` over `*.cpp *.h *.java *.rs *.toml *.json` in `/Users/nck/xj/xjmusic` returns **nothing**: there is no MIDI code anywhere in V1 to inherit. |

---

## 1. What midir exposes: the whole identity API

`midir` deliberately hides the backend handle. The only identity surface is in `src/common.rs`:

| Item | Where | What it promises |
|---|---|---|
| `MidiInputPort` / `MidiOutputPort` (`#[derive(Clone, PartialEq)]`) | `common.rs:39-42`, `210-213` | "How the port is identified internally is backend-dependent. If the backend allows it, port objects remain valid when other ports in the system change (i.e. it is not just an index)." (`common.rs:32-35`) |
| `MidiInputPort::id() -> String` | `common.rs:45-49` | "Get a unique stable identifier for this port. This identifier must be treated as an opaque string." Added in 0.10.1 (CHANGELOG, PR #157, whose author wrote: stable "across restarts of the application (and potentially the system but that's *really up to the OS*)"). |
| `MidiInput::port_name(&port) -> Result<String, PortInfoError>` | `common.rs:87-93` | "An error will be returned when the port is no longer valid (e.g. the respective device has been disconnected)." Only true on WinMM since 0.11.1 (#204). |
| `MidiInput::find_port_by_id(&str)` | `common.rs:95-98` | Linear scan of `ports()` comparing `id()`; signature changed from `String` to `&str` in 0.11.0 (#180). |
| `MidiInput::ports()` / `port_count()` | `common.rs:78-85` | Fresh enumeration on every call; no cache, no change notification. |
| `MidiInput::connect(self, &port, port_name, callback, data)` | `common.rs:120-142` | Consumes the `MidiInput`; one connection per `MidiInput`; callback gets `(timestamp_us: u64, bytes: &[u8], &mut T)`. |
| `MidiOutputConnection::send(&[u8])` | `common.rs:346-348` | Full SysEx supported on all desktop backends. |

Nothing else. There is **no** manufacturer, product, vendor id, serial, driver or device accessor, and the backend structs are `pub(crate)` (`common.rs:41`, `212`), so a caller cannot reach the `coremidi::Source`, ALSA `Addr` or WinRT `HSTRING` behind a port.

---

## 2. What each backend puts in `id()` and `port_name()`

### 2.1 ALSA (Linux)

| Function | Code | Result |
|---|---|---|
| `id()` | `backend/alsa/mod.rs:148-149` (input), `553-554` (output): `format!("{}:{}", self.addr.client, self.addr.port)` | **`"20:1"`** style sequencer address. |
| `port_name()` | `alsa/mod.rs:51-77` (`helpers::get_port_name`): `"{client_name}:{port_name} {client}:{port}"` | e.g. `"Launchpad Mini MK3:Launchpad Mini MK3 LPMiniMK3 MI 20:1"`. |
| `ports()` filter | `alsa/mod.rs:23-36`: ports of type `MIDI_GENERIC | SYNTH | APPLICATION` with `READ|SUBS_READ` (input) or `WRITE|SUBS_WRITE` (output) | Includes other applications' ports and `Midi Through`. |

Where the strings come from (kernel):

- **Client name** = `card->shortname` (`seq_midi.c:362-364`), which for USB is the USB **product string** `dev->product` (`sound/usb/card.c:632`), or `"USB Device %#04x:%#04x"` if absent (`card.c:637`).
- **Port name** = the rawmidi substream name (`seq_midi.c:397-398`), built in `sound/usb/midi.c:1885-1895` as `"{shortname} {jack_name}"` when the USB jack string does not already start with the product name, or `"{shortname} MIDI {n}"` when the jack has no string (`default_jack_name = "MIDI"`, `midi.c:1859`). The substream name field is **32 bytes** (`include/sound/rawmidi.h:98`), so `"Launchpad Mini MK3 LPMiniMK3 MIDI"` (33 chars) is cut to `"Launchpad Mini MK3 LPMiniMK3 MI"`; an Ardour bug report shows exactly this truncation (`tracker.ardour.org/view.php?id=9623`). The MPD218 has no jack strings, so its port is `"MPD218 MIDI 1"` (mpd-utils README, `amidi -l`).
- **Client number** = `16 + 4 * card_number` for kernel card clients: `#define SNDRV_SEQ_GLOBAL_CLIENTS 16`, `SNDRV_SEQ_CLIENTS_PER_CARD 4`, `SNDRV_SEQ_DYNAMIC_CLIENTS_BEGIN 128` (`seq_clientmgr.c:45-49`); `client_index += SNDRV_SEQ_GLOBAL_CLIENTS + card->number * SNDRV_SEQ_CLIENTS_PER_CARD` (`seq_clientmgr.c:2333-2334`), falling back to dynamic allocation ≥128 for cards ≥28 (`:2335-2336`, header comment `:38-41`). **Card number** is the first free slot unless pinned by the `snd` module's `slots=` option (`sound/core/init.c:283-286`). So the same device plugged alone gets the same card, hence the same client number; with two devices the numbers follow **plug order**.
- **Port number** = `device * (256 / SNDRV_RAWMIDI_DEVICES) + p` (`seq_midi.c:387`): fixed per device layout (DAW port = 0, MIDI port = 1 on the Launchpad).

Reachable beyond midir on Linux, without a USB crate:

| Datum | How | Crate support |
|---|---|---|
| Card number of a seq client | `snd_seq_client_info_get_card()` | `alsa` 0.12.1 `seq.rs:281-282` (`ClientInfo::get_card`); **absent** from `alsa` 0.9.0 that midir's own lockfile pins (`Cargo.lock:15-16`); midir allows `alsa >=0.9,<0.13` (`Cargo.toml:150`). Raw FFI in `alsa-sys` 0.6.1 `generated.rs:13713`. Kernel field `snd_seq_client_info.card` (`asequencer.h:362`); alsa-lib returns -ENOSYS on old kernels (lore.kernel.org thread "Change card/pid get functions to return -ENOSYS"). |
| USB VID:PID | `/proc/asound/card{N}/usbid` prints `"%04x:%04x"` (`sound/usb/proc.c:40-46`, registered `:53-54`); `usbbus` gives `bus/dev` (`:33-38`). Also sysfs `/sys/class/sound/card{N}/device/../idVendor`. | plain file read |
| Manufacturer + USB topology | `snd_card_get_longname` → `"{manufacturer} {product} at {usb path}, {speed}"` (`card.c:641-684`, `usb_make_path` at `:678`) | `alsa::card::Card::get_longname` (`alsa-0.9.0/src/card.rs:40`) |

### 2.2 CoreMIDI (macOS, iOS)

| Function | Code | Result |
|---|---|---|
| `id()` | `backend/coremidi/mod.rs:27-33` (`source.unique_id().unwrap_or(0).to_string()`), `313-319` for destinations | The endpoint's **`kMIDIPropertyUniqueID`** (SInt32) as a decimal string, e.g. `"1359014613"` or negative like `"-680605692"`. `PartialEq` compares unique IDs (`:36-45`). |
| `port_name()` | `coremidi/mod.rs:77-82`: `source.display_name()` | **`kMIDIPropertyDisplayName`**: "the Apple-recommended user-visible name for an endpoint, by combining the device and endpoint names" (`MIDIServices.h:1319-1328`). lpminimk3's Darwin parser splits it with `r'(.+\sMK3)\s(.+)$'` (`lp_system_midi_port_parser.py`), i.e. `"<device name> <endpoint name>"`. |
| `ports()` | `coremidi/mod.rs:58-67`: iterates `coremidi::Sources` = `MIDIGetNumberOfSources`/`MIDIGetSource` (`coremidi-0.9.0/src/endpoints/sources.rs:36-39, 107-110`) | Apple: "iterations through the system's sources and destinations don't include the endpoints of offline devices" (developer.apple.com `MIDIGetDevice(_:)` discussion). So an unplugged device vanishes from `ports()`. |
| Client creation | `coremidi/mod.rs:48-56`: `Client::new(client_name)` = `MIDIClientCreate` with **no notify proc** (`coremidi-0.9.0/src/client.rs:107-113`); the crate does offer `Client::new_with_notifications` (`client.rs:84-92`) delivering `Notification::{SetupChanged, ObjectAdded, ObjectRemoved, PropertyChanged}` (`notifications.rs:37-41`) | midir never registers for `kMIDIMsgObjectAdded/Removed` (`MIDIServices.h:675-678`); this is issue #78 (devices plugged after start not seen "even when I create a new MidiInput"). Note that CoreMIDI notifications need a run loop on the creating thread. |

Identity semantics on macOS:

- Apple: "The system assigns unique IDs to all objects. You may set this property on virtual endpoints; however, doing so may fail if the ID isn't unique." (`MIDIServices.h:829-838`; developer.apple.com `kMIDIPropertyUniqueID`). Unique IDs live in the MIDI setup, which "the MIDIServer maintains ... stored persistently in a preference file" (`MIDISetup.h:53-56`). Hence the same *device object* keeps its ID across replug and reboot, and `MIDIObjectFindByUniqueID` (`MIDIServices.h:2328-2348`) resolves it.
- The failure mode is **device without a USB serial number moved to another port**: the OS creates a new device object with a new ID, the old one stays offline in Audio MIDI Setup (greyed). Microsoft's USB-MIDI team states the same mechanism applies to macOS and Linux (devblogs.microsoft.com "The importance of including a unique iSerialNumber"); a JUCE thread reports an X-Touch Mini getting different identifiers on adjacent ports under Mojave but the same under Catalina, and iOS 15 regenerating IDs on every reconnect until iOS 16 (forum.juce.com/t/48661). JUCE's identifier is the *entity's* uniqueID (`juce_midi_mac.mm:284-290`); midir's is the *endpoint's*. Both are system-assigned per setup object.
- `unique_id()` returning `None` is mapped to `"0"` (`coremidi/mod.rs:31`); Chromium notes "ghost" endpoints that appear and disappear during device attach and fail property queries (midi_manager_mac.cc comment).

Reachable beyond midir on macOS, without a USB crate:

| Datum | How | Status |
|---|---|---|
| Manufacturer, model, name, driver | `kMIDIPropertyManufacturer` / `kMIDIPropertyModel` are "device/endpoint property, string. Drivers should set this property on their devices." (`MIDIServices.h:804-827`). A dump of `MIDIObjectGetProperties` on this Mac (script `.../scratchpad/scripts/dumpmidi.swift`) shows the keys `manufacturer`, `model`, `name`, `driver`, `offline`, `uniqueID`, `image` on **device** objects, `name`, `embedded`, `maxSysExSpeed`, `protocol`, `uniqueID` on entities, and **only `uniqueID`** on the source endpoint (non-deep query). | `coremidi` 0.9.0 exposes `Properties::manufacturer()/model()/driver_owner()` (`properties.rs:206-213, 276-278`) and `Object::get_property_string` (`object.rs:44-46`); `Source` derefs to `Endpoint` to `Object` (`sources.rs:79-80`, `endpoint.rs:41-42`); `Device`/`Entity` deref to `Object` (`device.rs:35-36`, `entity.rs:35-36`). **No** `MIDIEndpointGetEntity` / `MIDIEntityGetDevice` wrapper in coremidi 0.9.0 (grep of `src/` finds none) → walk `Devices → entities → sources` and join on `unique_id()`, or call `coremidi_sys::MIDIEndpointGetEntity` directly. Join key to midir: `midir id() == source.unique_id().to_string()`. |
| USB VID:PID | **No documented CoreMIDI property.** The SDK header has no `kMIDIPropertyUSB*` key (grep of `MIDIServices.h`). Community code checks for an undocumented `USBLocationID` key in deep properties of USB devices (unverified here: no USB MIDI device was attached during the dump, only the Network, IAC and Bluetooth drivers). | IOKit registry (`io-kit-sys` + `core-foundation`) is the supported route; otherwise a USB crate. |

### 2.3 WinMM (Windows default backend)

| Function | Code | Result |
|---|---|---|
| `id()` | `backend/winmm/mod.rs:70-72`: UTF-16 of `interface_id` minus the trailing NUL (the NUL was included before 0.11.0, issue #168: serialized ids that dropped it stopped matching) | The **device interface path** returned by `midiInMessage(.., DRV_QUERYDEVICEINTERFACE, ..)` (`winmm/mod.rs:91-124`, size query `:94-99`). Microsoft documents it only as "the device-interface name of a ... midiIn, midiOut ... device" (learn.microsoft.com `drv-querydeviceinterface`). Chromium's WinRT code gives the shape of the MMDevAPI software-device interface id: `\\?\SWD#MMDEVAPI#MIDII_60F39FCA.P_0002#{504be32c-ccf6-4d2c-b73f-6f8b3747e22b}` (midi_manager_winrt.cc comment). I could not independently confirm that WinMM returns that same string for USB class devices; JUCE uses the same DRV_QUERYDEVICEINTERFACE string as its identifier and falls back to the name, appending `-N` to duplicates (`juce_midi_win.cpp:2520-2544`). |
| `port_name()` | `winmm/mod.rs:126-146`: `MIDIINCAPSW.szPname` (32 WCHAR, NUL-terminated; learn.microsoft.com `ns-mmeapi-midiincapsw`), validated by `current_port_number()` (`:155-169`) which rescans by name *and* interface id; returns `InvalidPort` when gone (`:220-225`, fixed in 0.11.1 #204) | `"LPMiniMK3 MIDI"`, `"MIDIIN2 (LPMiniMK3 MIDI)"`, `"MIDIOUT2 (LPMiniMK3 MIDI)"` (Hauptwerk forum, OpenLaunchDeck wiki). The 31-char budget is why Windows prefixes the *second* interface with `MIDIIN2 (...)`. |
| `wMid`/`wPid` | `MIDIINCAPSW` members | These are **Microsoft** manufacturer/product ids ("Manufacturer and Product Identifiers" list), **not USB VID/PID**. midir reads `MIDIINCAPSW` (`winmm/mod.rs:127-133`) and discards both. |

Reachable beyond midir on Windows (WinMM), without a USB crate:

- **USB VID/PID are encoded in `MIDIINCAPS2W.ManufacturerGuid` / `ProductGuid`** for USB-audio-class devices: `INIT_USBAUDIO_MID(guid,id)` sets `Data1 = 0x4e1cecd2 + id` and `EXTRACT_USBAUDIO_MID(guid) = (USHORT)(Data1 - 0x4e1cecd2)` (`ksmedia.h:39-63`); `INIT_USBAUDIO_PID` uses base `0xabcc5a5e` (`ksmedia.h:73-100`); `INIT_USBAUDIO_PRODUCT_NAME` encodes vid, pid and string index into `NameGuid` (`:107-113`). Chromium's WinMM manager does exactly this for the vendor (`IS_COMPATIBLE_USBAUDIO_MID` → `UsbIds::GetVendorName`, midi_manager_win.cc). The `windows` crate 0.62 has `Win32::Media::Audio::MIDIINCAPS2W { wMid, wPid, vDriverVersion, szPname: [u16; 32], dwSupport, ManufacturerGuid, ProductGuid, NameGuid }` (microsoft.github.io/windows-docs-rs). This requires calling `midiInGetDevCapsW` yourself with the larger struct; midir's `Cargo.toml:161-165` already pulls `windows` with `Win32_Media`.
- Chromium's own WinMM port identity is `manufacturer_id, product_id, driver_version, product_name` and it explicitly does **not** trust the index ("can be changed on each enumeration"); its Web MIDI id is just `"input-N"` (midi_manager_win.cc). Firefox, which embeds midir, assigns a fresh `Uuid::new_v4()` per enumeration and matches ports **by name and direction** on refresh (`dom/midi/midir_impl/src/lib.rs`): two production users of these ids both declined to persist them.

### 2.4 WinRT (`winrt` feature)

| Function | Code | Result |
|---|---|---|
| `id()` | `backend/winrt/mod.rs:18-25`, `214-221`: `DeviceInformation.Id` as `HSTRING` | The MMDevAPI software device interface id, e.g. `\\?\SWD#MMDEVAPI#MIDII_60F39FCA.P_0002#{504be32c-ccf6-4d2c-b73f-6f8b3747e22b}` (Chromium comment). The instance part `SWD\MMDEVAPI\MIDII_60F39FCA.P_0002` is a child of the hardware devnode. |
| `port_name()` | `winrt/mod.rs:74-84`: `DeviceInformation::CreateFromIdAsync(id).Name()` | Friendly name (not limited to 31 chars). |
| `ports()` | `winrt/mod.rs:46-63`: `DeviceInformation::FindAllAsyncAqsFilter(MidiInPort::GetDeviceSelector())` | Blocking `.join()`; no `DeviceWatcher` (Chromium uses one with `Added/Removed/EnumerationCompleted`). |

Reachable beyond midir on WinRT: walk `CM_Locate_DevNodeW(instance id) → CM_Get_Parent → CM_Get_DevNode_PropertyW(DEVPKEY_Device_HardwareIds)` (Chromium does this for `DEVPKEY_Device_DriverProvider/DriverVersion`); the parent's hardware id is `USB\VID_v(4)&PID_d(4)&REV_r(4)` or, for a composite device's interface, `USB\VID_v(4)&PID_d(4)&MI_z(2)` (learn.microsoft.com "Standard USB Identifiers"). `windows` 0.62 exposes `Win32::Devices::DeviceAndDriverInstallation::CM_Get_Parent`.

### 2.5 Android (for completeness)

`id()` = `format!("{}:in:{}", device_id, port_number)` (`backend/android/mod.rs:257-259`, `534-536`). Not a V2 target.

---

## 3. Stability matrix for `id()`

| Backend | Same process, device untouched | Unplug / replug, same port | Replug, different port | Process restart | Reboot |
|---|---|---|---|---|---|
| ALSA `"client:port"` | stable | stable **iff** the same card slot is still the first free one (`init.c:283-286`) | usually stable (slot, not port, decides) **but** changes if another card took the slot meanwhile | stable only if plug order and card set are identical; not by design | same as restart |
| CoreMIDI uniqueID | stable | stable (persisted MIDI setup, `MIDISetup.h:53-56`) | stable for devices with a USB serial number; **new ID** for serial-less devices (MS devblog; JUCE thread) | stable | stable |
| WinMM device-interface path | stable | stable (device instance IDs "persistent across system restarts", MS "Device Instance ID") | stable with serial number; **new instance → new path** without one (MS devblog) | stable | stable |
| WinRT `DeviceInformation.Id` | stable | stable | as WinMM | stable | stable |

Two cross-platform facts the spec must absorb: (a) on every OS a serial-less controller is a *new device* on a new port, and the Launchpad Mini MK3 and MPD218 class as USB MIDI devices whose `iSerialNumber` presence is not documented, so the profile store must never key on `id()` alone; (b) ALSA ids are addresses, not identities, and Firefox and Chromium both refused to persist any midir / WinMM id.

---

## 4. Are manufacturer, product and VID:PID reachable without a USB crate?

| Datum | Linux (ALSA) | macOS (CoreMIDI) | Windows WinMM | Windows WinRT | Through MIDI itself |
|---|---|---|---|---|---|
| Product string | yes: client name = USB product (`card.c:632`) | yes: device `name` / `model` (`MIDIServices.h:817-827`, dump) | yes: `szPname` (31 chars, may be `MIDIIN2 (...)`) | yes: `Name` | — |
| Manufacturer string | yes: `card longname` prefix (`card.c:667-671`) | yes: device `manufacturer` (`:804-814`, dump) | not as a string (`wMid` is an MS id); vendor **id** via `ManufacturerGuid` | parent devnode `DEVPKEY_Device_Manufacturer` | Device Inquiry reply bytes 5-7 (manufacturer SysEx id, e.g. `00 20 29` Novation, `47` Akai) |
| USB VID:PID | yes: `/proc/asound/cardN/usbid` (`usb_proc.c:40-46`) | **no documented property**; IOKit needed | yes: `MIDIINCAPS2W` GUIDs (`ksmedia.h:53-54, 87-88`) | yes: parent hardware id `USB\VID_&PID_` | — |
| Serial number | sysfs `.../serial` | IOKit | parent instance id (if present) | parent instance id | — |
| Family / model bytes | — | — | — | — | Device Inquiry reply (`F0 7E 7F 06 01 F7` → `F0 7E 00 06 02 <mfr> <family> <member> <version×4> F7`); Launchpad Mini MK3 replies `00 20 29 13 01 00 00 <app_version>` (Programmer's Reference p.6) |

Conclusion for the match key: **`manufacturer`/`product` from `08-performer-input.md` are not implementable from midir alone, and are not uniformly implementable from the OS either** (no manufacturer on WinMM; no VID:PID on macOS without IOKit). The one identity channel that is identical on all four backends is the port name plus a MIDI Universal Device Inquiry handshake over the port itself. See section 8.

---

## 5. Pairing input and output ports

LED feedback needs the *output* that belongs to the *input* the performer presses. midir enumerates them separately and the ids differ by backend: on ALSA a duplex USB port has one `client:port` address with both READ and WRITE capability, so input and output `id()` strings are **equal**; on CoreMIDI the source and destination are different endpoints with **different** uniqueIDs; on WinMM/WinRT they are different software devices (`MIDII_` vs the output interface). Chromium and Firefox pair nothing. The spec must define the pairing rule (same `port_name()` stem within one rescan, confirmed by the Device Inquiry reply arriving on the input after being sent on the candidate output).

---

## 6. Hot-plug, disconnect and timestamps (what the input spec inherits)

- No backend registers for change notifications (ALSA announce port `0:1` unsubscribed; `MIDIClientCreate` without notify, `coremidi/mod.rs:49`; no `DeviceWatcher`, `winrt/mod.rs:46-63`). Open issues #78 (macOS), #86 (poll `ports()`), #191 (ALSA disconnect silent; the input thread just stops delivering), #201 (no portable hotplug). `port_name()` is the only liveness probe and only WinMM errors reliably (#204, 0.11.1).
- Timestamp domains differ: WinMM `ms * 1000` from `midiInOpen` time (`winmm/handler.rs:31`); CoreMIDI `AudioConvertHostTimeToNanos(packet.timestamp)/1000` (`coremidi/mod.rs:104-106`, substituting `AudioGetCurrentHostTime()` when 0); ALSA realtime queue timestamps (`alsa/mod.rs:267-270`, tempo set `:237-240`) unless the `avoid_timestamping` feature is on. None is the audio device clock; `xj-control` must re-stamp on receipt.
- Output: WinMM uses `midiOutShortMsg` for ≤3 bytes and `midiOutLongMsg` for SysEx (`winmm/mod.rs:591-598`); CoreMIDI builds a `PacketBuffer` per `send` (`coremidi/mod.rs:413-427`); ALSA encodes through `snd_midi_event` and errors `InvalidData` on malformed bytes (`alsa/mod.rs:706-717`). A full-surface Launchpad refresh (one SysEx, ≤ 81 colourspecs ≈ 400 bytes) is fine on all three.

---

## 7. Reference controller A: Novation Launchpad Mini [MK3] in Programmer mode

USB identity: vendor `0x1235 Focusrite-Novation` (usb.ids); product `0x0113` per the Windows driver listing `USB\VID_1235&PID_0113&MI_00` (treexy.com driver database; usb.ids only lists the MK1/MK2). Bootloader lets the user set a "USB device ID" 1-16 so several Launchpads can coexist (Programmer's Reference p.5).

Ports (p.6): two interfaces. `LPMiniMK3 DAW In/Out` ("first interface on Windows") is for DAW/Session mode; **`LPMiniMK3 MIDI In/Out` ("second interface on Windows") is the one that "is used to provide external MIDI input or Light controls in Lighting Custom Modes and Programmer mode."** Observed names: Windows `LPMiniMK3 MIDI`, `MIDIIN2 (LPMiniMK3 MIDI)`, `MIDIOUT2 (LPMiniMK3 MIDI)`; ALSA `Launchpad Mini MK3:Launchpad Mini MK3 LPMiniMK3 MI 20:1` (truncated, see 2.1); macOS `<device> <endpoint>` (lpminimk3 regex). lpminimk3's Windows parser handles both `^(.+)\s\d+$` and `(.+)\s\((.+)\)\s\d+$` (`lp_system_midi_port_parser.py`).

Protocol (all from the Programmer's Reference, pages in parentheses; verified against `lp_midi_messages.py`):

| Item | Bytes | Notes |
|---|---|---|
| SysEx header | `F0 00 20 29 02 0D <cmd> ... F7` (p.6) | Every command has a readback form `F0 00 20 29 02 0D <cmd> F7` (p.7). |
| Device Inquiry | host `F0 7E 7F 06 01 F7` → `F0 7E 00 06 02 00 20 29 13 01 00 00 <app_version×4> F7`; bootloader replies `13 11` (p.6) | Cross-platform identity probe. |
| Enter Programmer mode | `F0 00 20 29 02 0D 0E 01 F7`; Live mode `... 0E 00 F7` (p.7) | Alternative `Select layout` `... 00 7F F7` (layouts: `00` Session, `04/05/06` Custom 1-3, `0D` DAW faders, `7F` Programmer, p.7). "Remember to switch the device back to Live mode once done" (p.9); Setup entry is disabled while in Programmer mode (p.7). |
| DAW / Standalone | `... 10 01` / `... 10 00` (p.15) | Not needed for Programmer mode; DAW apps should restore Standalone on exit. |
| Note semantics | Device sends Note On `9n` with velocity 0 for releases; accepts `8n` or `9n vel 0` as off (p.6) | Parse both. |
| Grid pads (Programmer layout) | Note numbers `11..18` bottom row to `81..88` top row, i.e. `note = 10*row + col` with row 1 at the bottom, col 1 at the left (p.10 figure; `lp_components.py` `Grid._PROG_MODE_MIDI_LAYOUT`: `0x51..0x58` top … `0x0b..0x12` bottom). Examples: lower-left `0Bh` (11), upper-left `51h` (81), lower-right `12h` (18) (p.13). | A 4×4 block for XJ: bottom-left quadrant = `11-14, 21-24, 31-34, 41-44`. |
| Top row buttons | CC `91..98` (`0x5b..0x62`), logo `99` (`0x63`); right column Scene Launch CC `89, 79, 69, 59, 49, 39, 29, 19` (`Panel._PROG_MODE_MIDI_LAYOUT`, 9×9 incl. `0x59..0x13` right column) | All buttons and pads "accept either Note or Control Change messages" for lighting (p.10). |
| LED by MIDI event | Channel 1 (`90h`/`B0h`) static, Channel 2 (`91h`/`B1h`) flashing, Channel 3 (`92h`/`B2h`) pulsing; note/CC number = pad, velocity/value = palette index (p.12) | Flashing toggles between the static/pulsing colour (A) and the flash colour (B) at 50% duty, **synchronised to incoming MIDI beat clock (else 120 bpm or last clock)**, one beat period; pulsing dims over two beats (p.12). |
| LED by SysEx | `F0 00 20 29 02 0D 03 <colourspec>* F7`, colourspec = `type, led_index, data`: `00 palette` (1 byte), `01 B A` (flash, 2 bytes), `02 palette` (pulse), `03 R G B` (0-127 each); up to 81 entries per message; indices always Programmer-layout (p.14) | Example `... 03 00 0B 0D 01 0C 15 17 02 0D 25 F7` = pad 11 static yellow(13), pad 12 flashing green 21/23, pad 13 pulsing turquoise 37. |
| Palette anchors | 0 off; 1 dark grey, 2 grey, 3 white; 4 light red, **5 red**, 6/7 darker reds; 8 pale yellow/orange, **9 orange**, 10/11 darker oranges; 12 pale yellow, **13 yellow**; 17–23 greens (**19 green**, **21 bright green**, 23 dim green); **37 turquoise**; 36–43 blues, **45 blue**; 44–47 violets (p.11 table rendered as an image; anchors from the p.13-14 worked examples; group ranges from `lp__colors.py`: red `0x04-0x07`, orange `0x09-0x0b`, yellow `0x08,0x0c`, green `0x0d-0x23`, blue `0x24-0x2b`, violet `0x2c-0x2f`) | The full 128-entry table is only a picture in the PDF; the spec should embed RGB hexes from `lpminimk3/colors/_colors.py` `HEX_VALUES` or use SysEx RGB and avoid the palette. |
| Brightness | `... 08 <0-127>` (p.22) | |
| Sleep | `... 09 <0|1>` (p.21) | LEDs off until message or touch. |
| LED feedback settings | `... 0A <internal> <external>` (p.22): internal = light pads when played; external = light pads for notes arriving on the MIDI interface | Relevant in Custom modes, not Programmer mode. |
| Text scroll | `... 07 <loop> <speed> <colourspec> <text>` (p.20) | Overlay; useful for "lands in 2 bars" prompts. |
| Factory Custom modes | Custom 1 "Drum Rack", Custom 2 "Keys", Custom 3 "Lighting mode in Drum Rack layout" (p.7). Drum Rack layout from `Grid._CUSTOM_MODE_MIDI_LAYOUT`: bottom-left 4×4 = notes **36–51** (`0x24-0x27` bottom row … `0x30-0x33`), top-left 52–67, bottom-right 68–83, top-right 84–99 | This is why `note = 36` in the proposal's TOML is the right default for an MPC-style 4×4: Ableton Drum Rack convention. Channel for Custom modes not verified (Novation's mapping article renders the table as images). |

Implication for the four LED states: `off` = ch1 vel 0; `available` = ch1 dim shade of the control's hue (e.g. 7 for red, 11 for orange, 23 for green); `armed` = ch2 flashing (A = the available shade, B = the active shade) **or** ch3 pulsing; `active` = ch1 bright shade (5 / 9 / 13 / 21 / 45). Because flashing is clocked from MIDI beat clock, sending XJ's beat clock to the Launchpad (already proposed as "MIDI clock out", `11-resolutions.md:108`) makes "armed" blink on the beat for free.

---

## 8. Reference controller B: Akai MPD218 (and why "an MPC in controller mode" is not a reference)

Facts from the User Guide v1.0 (`mpd218.txt`): "16 velocity- and pressure-sensitive pads, red-backlit", "3 banks accessible via Pad Bank button" (48 pads), "6 360° assignable potentiometers, 3 banks", "6 buttons", "1 USB port … Power via USB connection" (Appendix, p.18); **no 5-pin MIDI**. Full Level forces velocity 127 (p.4); Note Repeat with Time Division/Swing via NR Config (p.5); "Prog Select: Press and hold this button and then press a pad to select the Program with the same number as the pad" (p.5) = 16 presets; "download the MPD218 Preset Documentation" from akaipro.com (p.5; I could not retrieve it: the product page exposes no direct link). USB vendor `0x09e8 AKAI Professional M.I. Corp.` (usb.ids); the product id is **not** in usb.ids (likely `0x0034`, matching the SysEx model byte below; unverified).

Preset model (mpd-utils `mpd218.py`): dump request `F0 47 00 34 12 00 01 <preset> F7`, reply header `F0 47 00 34 10 04 1D <preset>`, 549 bytes (README); per pad: `type` NOTE/PROG, `channel`, `note`, `trigger` MOMENTARY=0/TOGGLE=1, `aftertouch` OFF/CHANNEL/POLY (`mpd218.py:87-100`); per dial: type CC/AFTERTOUCH/INC-DEC, channel, cc, min, max (`:108-131`). **There is no LED field anywhere in the preset**, so LED behaviour is firmware-fixed. Defaults seen by the tool for Pad 1 Bank A: channel 10, note 36 (README prompt defaults; factory table for the other 15 pads not verified; MPC-forum users report "pad1=36, 2=37 etc." for Akai pads). ALSA name `MPD218 MIDI 1` → midir `MPD218:MPD218 MIDI 1 N:0`.

LEDs: single red per pad, no colour, no blink primitive. Host-driven lighting is evidenced (not documented): a Traktor user mapped the MPD218 as a hot-cue surface and "When loading a new track, the LEDs under each pad are lit to indicate if there is a hot cue associated with that pad", with the LED going dark after a pad press (community.native-instruments.com/discussion/10336) - consistent with Note On on the pad's own note/channel turning the LED on and Note Off / vel 0 (or the device's own release) turning it off; a Cycling '74 thread documents the same convention for the MPK mini ("pads light when they receive their matching note at maximum velocity"). Treat as: lights follow Note On velocity > 0 on the pad's assigned note and channel, off on Note Off; **verify on hardware in learn mode** before shipping a default profile.

Why not "an MPC in controller mode": MPC Touch/Studio/One controller mode speaks a reverse-engineered proprietary protocol to the MPC software (mungewell `mpc_touch_reverse/sysex_data.md`), not a programmer's reference; the MPC in standalone MIDI-controller mode is user-configurable per preset and has no published LED table. The MPD218 is the audience-realistic Akai 4×4.

Degradation table for the four LED states on a one-colour, no-blink surface:

| State | Launchpad Mini MK3 | MPD218 |
|---|---|---|
| off | ch1 vel 0 | Note Off |
| available | ch1 dim shade | **on** (or off, by profile option `available = "off"`) |
| armed | ch2 flashing / ch3 pulsing (clock-synced) | software blink: host toggles Note On/Off on the beat (xj-control already has the beat) |
| active | ch1 bright shade | on |

---

## 9. Implications for V2 specs

1. **The TOML `match = { manufacturer = "*", product = "*Pad*" }` is not implementable from midir.** midir yields `id()` and `port_name()` only (`common.rs:45-49, 87-93`). Replace with a tiered key: `match.port_name = ["*LPMiniMK3 MI*", "MIDIIN2 (LPMiniMK3 MIDI)"]` (globs; must tolerate ALSA's 31-char truncation and Windows' `MIDIIN2 (...)` prefix), `match.inquiry = { manufacturer = [0x00,0x20,0x29], family = [0x13,0x01] }` (Universal Device Inquiry, works on every backend through the port itself), and optional `match.usb = "1235:0113"` resolved by per-OS helpers (section 4). Name the resolution order and say that `usb` is best-effort.
2. **Never persist `id()` as the profile binding.** ALSA ids are plug-order addresses (`seq_clientmgr.c:2333-2334`, `init.c:283-286`); CoreMIDI/Windows ids change when a serial-less device moves ports. Persist `{port_name glob, inquiry reply, usb}` and re-resolve on every rescan. Keep `id()` for in-session bookkeeping and for `find_port_by_id` (which is just a scan, `common.rs:96-98`).
3. **Two Launchpads look identical.** Same names (Windows appends numbers only in some stacks; JUCE appends `-N`), same inquiry reply; the Launchpad's bootloader "USB device ID 1-16" is the user's disambiguator (p.5). The mapping spec needs an instance slot (`instance = 2`) and a UI to assign it.
4. **Pick the right interface.** Programmer-mode lighting only works on the `LPMiniMK3 MIDI` port, never on `DAW` (p.6). The default profile must bind the second interface; the first will silently ignore LED notes.
5. **Pair in/out explicitly.** ids are equal for ALSA duplex ports but differ on CoreMIDI and Windows (section 5). Define pairing by name stem + inquiry round-trip.
6. **Enter Programmer mode on connect, leave it on disconnect**, and send DAW/Standalone `10 00` if DAW mode was ever enabled (p.9, p.15). A crash that leaves the device in Programmer mode disables its Setup menu until power-cycle (p.7): document the recovery.
7. **Note Off parsing:** Launchpad releases are `9n vel 0` (p.6). Any mapping engine must treat `9n vel 0` as release for all devices.
8. **LED-state table (xj-control spec) can now be literal**: Launchpad = palette index + channel (1 static / 2 flash / 3 pulse), batch refresh via SysEx `03h` with up to 81 colourspecs or RGB 0-127 (p.12-14); MPD218 = Note On/Off on the pad's own note, software blink for `armed`. Define a `capabilities` block per profile: `colors = palette|rgb|single`, `blink = device|host`, `max_batch`.
9. **Clock out makes "armed" musical.** Launchpad flash/pulse lock to incoming MIDI beat clock (p.12); the MIDI-clock-out fallback already in `11-resolutions.md:108` should be specified as "also sent to any controller whose profile declares `blink = device`".
10. **Palette is an image in the vendor PDF.** Embed the 128-entry RGB table from `lpminimk3/colors/_colors.py` (`HEX_VALUES`) or specify RGB SysEx only; do not cite palette indices beyond the anchors verified in section 7.
11. **Default 4×4 layout:** Programmer-mode bottom-left block `11-14/21-24/31-34/41-44`; Drum-Rack convention `36-51` (Launchpad Custom 1, MPD218 Bank A ch 10 Pad 1 = 36). Ship two default profiles, not one `*Pad*` glob.
12. **Hot-plug is the spec's job** (external map item 9): rescan `ports()` on a timer (1-2 s), detect loss by `port_name()` error (reliable only on WinMM) or by set difference, and recreate `MidiInput` (macOS, #78). On macOS the better path is a `coremidi::Client::new_with_notifications` sidecar (`client.rs:84-92`) run on a thread with a CFRunLoop; on Windows a `DeviceWatcher`; on Linux subscribe to the announce port. All three are outside midir.
13. **VID:PID per OS without a USB crate** is possible on Linux (`/proc/asound/cardN/usbid`, needs `alsa ≥0.12` `ClientInfo::get_card`) and Windows (`MIDIINCAPS2W` GUIDs via `ksmedia.h` arithmetic; WinRT parent devnode hardware ids) but **not** on macOS (IOKit required). If the spec wants VID:PID everywhere, budget `io-kit-sys` on macOS or accept `nusb`.
14. **midir pins that bite:** `find_port_by_id(&str)` since 0.11.0; WinMM ids carried a trailing NUL before 0.11.0 (#168), so any profile written by a 0.10 build must be migrated; `alsa 0.9.0` is what midir's lockfile tested, while `get_card` needs 0.12 (midir allows `<0.13`).
15. **Timestamps are per-backend clocks** (section 6); `xj-control` must stamp gestures with its own monotonic clock at callback entry and treat midir's `u64` only as intra-device ordering.
16. **Unverified items to close with hardware in Phase 0:** MPD218 factory note table and LED-on-Note-On behaviour; MPD218 USB PID; Launchpad Custom-mode default channel; exact WinMM `DRV_QUERYDEVICEINTERFACE` string for a USB class device; CoreMIDI `USBLocationID` deep-property key.
