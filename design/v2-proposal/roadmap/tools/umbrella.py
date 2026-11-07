#!/usr/bin/env python3
"""Render the umbrella roadmap issue body from the tracker and, with --push, publish it.

  python3 -I tools/umbrella.py           # print the body
  python3 -I tools/umbrella.py --push    # PATCH the umbrella issue named in issue-map.json

Reads master.json, schedule.json and issue-map.json beside this folder. Milestone
numbers are looked up from GitHub by title (they are not stored in the tracker).
"""
import collections, json, os, re, subprocess, sys, tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
ISSUE_URL_RE = re.compile(r"^https://github\.com/([^/]+/[^/]+)/issues/(\d+)$")


def repo_of(imap):
    """The owner/repo every issue in issue-map.json lives in; refuse a mixed map or a mismatched XJ_REPO."""
    repos = set()
    for key, v in imap.items():
        m = ISSUE_URL_RE.match(v.get("url", ""))
        if not m or int(m.group(2)) != v["number"]:
            raise SystemExit(f"issue-map.json: unexpected url for {key}: {v.get('url')!r}")
        repos.add(m.group(1))
    if len(repos) != 1:
        raise SystemExit(f"issue-map.json mixes repositories: {sorted(repos)}")
    repo = repos.pop()
    env = os.environ.get("XJ_REPO")
    if env and env != repo:
        raise SystemExit(f"XJ_REPO={env} but issue-map.json maps issues in {repo}; refusing to mix them")
    return repo


def cell(text):
    """Make a title safe inside a Markdown table cell: GitHub drops unknown HTML tags and | ends a cell."""
    return text.replace("<", "&lt;").replace(">", "&gt;").replace("|", "\\|")
PHASES = [("0", "Spikes and decisions"), ("1", "Engine 2.0"), ("2", "Station core"), ("3", "Workstation 2.0, Play mode"), ("4", "Commons"), ("5", "Sessions"), ("6", "Make mode parity"), ("7", "The show")]
EXIT = {
    "0": "every spike row has a recorded decision; the C ABI spike seeds `xj-engine-sys`. Gate: toolkit, stretch library, Link path, go/no-go on the plan.",
    "1": "the two-engine follow-mode test is green on three platforms; `engine-v2.0.0` is tagged with ABI 1.0.0; the Unreal plugin builds against it unchanged.",
    "2": "`xj-station play` on a V1 demo with a MIDI controller sounds at least as good as the Java workstation, runs 8 hours without a dropout, and `replay` reproduces a capture byte for byte. Gate: does the station core sound good enough to build a product on?",
    "3": "first launch to sound in under 10 s on a 2019 laptop; a musician new to XJ performs 20 minutes with a pad controller after the tour; five usability sessions filed as issues. Gate: do musicians want to play it? Release: workstation 2.0.0.",
    "4": "a fresh install fetches a show from the registry, writes a lockfile, and two machines with the same lockfile load byte-identical content; an external contributor publishes a pack that passes lint. Release: workstation 2.1.",
    "5": "two workstations and one MPC on Link play a 30-minute duet with two handoffs and no dropout, captured on both stations and published. Gate: did the duet work in a real room? Release: workstation 2.2.",
    "6": "every editing operation in the Java workstation has an equivalent in the Rust workstation; the Java folder is archived with a pointer. Releases: workstation 2.3 and 2.4.",
    "7": "a real night with three or more stations, artists who did not build the software, and an audience; a recording that stands beside *Live in Boulder*.",
}
GATES = {"0": "p0-dec-go-no-go-gate", "2": "p2-gate-ab-listening-record", "3": "p3-ux-gate-decision", "5": "p5-dec-gate-duet-q13"}
RELEASES = {"1": ["p1-rel-engine-2-0-0-tag"], "2": ["p2-rel-cli-release-checksums"], "3": ["p3-pkg-release-workstation-2-0-0"], "4": ["p4-rel-workstation-2-1"], "5": ["p5-rel-workstation-2-2"], "6": ["p6-rel-workstation-2-3-instrument-editor", "p6-par-java-archive-and-roundtrip-retire"]}
AMEND = {"1": "p1-docs-amend-proposal-phase-1", "2": "p2-docs-amend-proposal-phase-2", "3": "p3-docs-amend-proposal-phase-3", "4": "p4-docs-amend-proposal-phase-4", "5": "p5-docs-amend-proposal-phase-5"}


def milestone_numbers(repo):
    r = subprocess.run(["gh", "api", "--paginate", f"repos/{repo}/milestones?state=all&per_page=100", "--jq", ".[] | [.number, .title] | @tsv"], capture_output=True, text=True)
    if r.returncode != 0:
        raise SystemExit(f"could not list milestones of {repo}: {r.stderr.strip()}")
    out = {}
    for line in r.stdout.splitlines():
        num, title = line.split("\t", 1)
        out[title] = int(num)
    return out


def phase_edges(m):
    """Phase-level edges from the task graph (blocker phase -> dependent phase), without those a longer path implies."""
    phase = {t["key"]: t["phase"] for t in m["tasks"]}
    E = collections.defaultdict(set)
    for t in m["tasks"]:
        for b in t.get("blocked_by", []):
            if phase[b] != t["phase"]:
                E[t["phase"]].add(phase[b])

    def implied(a, b):
        seen, stack = set(), [x for x in E.get(a, ()) if x != b]
        while stack:
            x = stack.pop()
            if x == b:
                return True
            if x not in seen:
                seen.add(x)
                stack.extend(E.get(x, ()))
        return False

    E = dict(E)
    return sorted((b, a) for a in E for b in E[a] if not implied(a, b))


def render(repo):
    m = json.load(open(os.path.join(ROOT, "master.json")))
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    sched = json.load(open(os.path.join(ROOT, "schedule.json")))
    ms = milestone_numbers(repo)
    missing = [f"v2 · Phase {p} · {name}" for p, name in PHASES if f"v2 · Phase {p} · {name}" not in ms]
    if missing:
        raise SystemExit(f"milestones not found in {repo}: {missing}")
    tasks_by_epic = collections.defaultdict(list)
    for t in m["tasks"]:
        tasks_by_epic[t["epic"]].append(t)

    def ref(k):
        return f"#{imap[k]['number']}" if k in imap else f"`{k}`"

    total_days = sum(t["estimate_days"] for t in m["tasks"])
    out = []
    out.append("Roadmap index for the [V2 proposal](https://github.com/xjmusic/xjmusic/tree/main/design/v2-proposal) and [project #7](https://github.com/orgs/xjmusic/projects/7): the plan that takes XJ music from version 1.9.6 to a Rust performance workstation on the C++ engine, a shared content Commons, and multi-station sessions. Epics are sub-issues of this issue; tasks are sub-issues of their epics; formal *blocked by* links form the waterfall. One milestone per phase carries the phase's exit demonstration and due date.")
    out.append("")
    out.append(f"**Shape:** {len(m['epics'])} epics · {len(m['tasks'])} tasks · {total_days:g} engineer-days of estimates · schedule for two engineers from {min(v['start'] for v in sched['milestones'].values())} to {max(v['due'] for v in sched['milestones'].values())} (planning numbers, ±50%, re-estimated at each gate, not commitments).")
    out.append("")
    out.append("## How to read the tracker")
    out.append("")
    out.append("- **Milestones** are the phases of [09-roadmap.md](https://github.com/xjmusic/xjmusic/blob/main/design/v2-proposal/09-roadmap.md); a phase is done when its exit demonstration below has happened, not when its issues are closed.")
    out.append("- **Epics** (type Feature, label `epic`) group one capability per phase. Each epic body lists its tasks in dependency order. **Tasks** (type Task) are half a day to 12 engineer-days each and carry a full specification: context, scope, design, acceptance criteria, test plan, dependencies, risks, references.")
    out.append("- **Labels:** `v2` on everything; `area/<crate or discipline>`; `spike` for time-boxed experiments; `decision` for decision records. **Fields:** Start date, Target date, Effort and Priority come from the schedule.")
    out.append("- **Spec status** is the first line of every body: 🚧 placeholder, 📝 draft, 🔍 reviewed, ✅ consistent. Edit issues freely; when the shape of the plan changes, regenerate the tracker from `design/v2-proposal/roadmap/`.")
    out.append("- **Decision gates** close a phase and may re-plan the next one: " + ", ".join(f"after Phase {p} ({ref(k)})" for p, k in GATES.items()) + ". After each gate, one task writes the phase's recorded departures back into the proposal chapters, so the proposal stays the single description of the plan: " + ", ".join(ref(k) for k in AMEND.values()) + ".")
    out.append("")
    out.append("## Phases")
    for p, name in PHASES:
        epics = [e for e in m["epics"] if e["phase"] == p]
        ntasks = sum(len(tasks_by_epic[e["key"]]) for e in epics)
        days = sum(t["estimate_days"] for e in epics for t in tasks_by_epic[e["key"]])
        title = f"v2 · Phase {p} · {name}"
        msd = sched["milestones"].get(title, {})
        mslink = f"[Milestone](https://github.com/{repo}/milestone/{ms[title]})"
        out.append("")
        out.append(f"### Phase {p} · {name}")
        out.append("")
        out.append(f"{mslink} · {len(epics)} epics · {ntasks} tasks · {days:g} engineer-days · scheduled {msd.get('start', '?')} → {msd.get('due', '?')}")
        out.append("")
        out.append(f"**Exit:** {EXIT[p]}")
        extra = []
        if p in GATES:
            extra.append(f"gate {ref(GATES[p])}")
        if p in RELEASES:
            extra.append("release " + ", ".join(ref(k) for k in RELEASES[p]))
        if extra:
            out.append("")
            out.append("**Closing tasks:** " + "; ".join(extra) + ".")
        out.append("")
        out.append("| Epic | Area | Tasks | Days | Blocked by |")
        out.append("|---|---|---|---|---|")
        for e in epics:
            bb = ", ".join(ref(b) for b in e.get("blocked_by", [])) or "—"
            out.append(f"| {ref(e['key'])} {cell(e['title'])} | `{e['area']}` | {len(tasks_by_epic[e['key']])} | {sum(t['estimate_days'] for t in tasks_by_epic[e['key']]):g} | {bb} |")
    out.append("")
    out.append("## Phase dependencies")
    out.append("")
    short = {"0": "Spikes", "1": "Engine 2.0", "2": "Station core", "3": "Play mode", "4": "Commons", "5": "Sessions", "6": "Make parity", "7": "The show"}
    out.append("```mermaid")
    out.append("flowchart LR")
    for p, _ in PHASES:
        out.append(f"  P{p}[\"Phase {p}<br/>{short[p]}\"]")
    for b, a in phase_edges(m):
        out.append(f"  P{b} --> P{a}")
    out.append("```")
    out.append("")
    out.append("Computed from the task dependencies: an arrow from one phase to another means some task in the second is blocked by a task in the first. Arrows that a longer path already implies are not drawn.")
    out.append("")
    out.append("## Versions along the way")
    out.append("")
    out.append("| Tag | When | Owner |")
    out.append("|---|---|---|")
    out.append(f"| `engine-v2.0.0` with C ABI 1.0.0 | end of Phase 1 | {ref('p1-rel-engine-2-0-0-tag')} |")
    out.append(f"| `workstation-v2.0.0-alpha.N` (headless `xj-station` CLI) | Phase 2 | {ref('p2-rel-cli-release-checksums')} |")
    out.append(f"| `workstation-v2.0.0` (Play and Ship modes) | end of Phase 3 | {ref('p3-pkg-release-workstation-2-0-0')} |")
    out.append(f"| `workstation-v2.1.0` (Commons) | end of Phase 4 | {ref('p4-rel-workstation-2-1')} |")
    out.append(f"| `workstation-v2.2.0` (Sessions) | end of Phase 5 | {ref('p5-rel-workstation-2-2')} |")
    out.append(f"| `workstation-v2.3.0` (instrument and audio editor), `workstation-v2.4.0` (Make mode complete, Java archived) | Phase 6 | {ref('p6-rel-workstation-2-3-instrument-editor')}, {ref('p6-par-java-archive-and-roundtrip-retire')} |")
    out.append(f"| Java workstation 1.9.x | maintenance line from the end of Phase 1 | {ref('p1-java-maintenance-mode')} |")
    out.append("")
    out.append("<sub>Generated from `design/v2-proposal/roadmap/` (master.json, schedule.json, issue-map.json) by `tools/umbrella.py`. Regenerate when the plan changes.</sub>")
    return "\n".join(out) + "\n"


def main():
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    repo = repo_of(imap)
    body = render(repo)
    if "--push" in sys.argv:
        num = imap["umbrella"]["number"]
        fd, tmp = tempfile.mkstemp(suffix=".json")
        json.dump({"body": body}, os.fdopen(fd, "w"))
        r = subprocess.run(["gh", "api", "-X", "PATCH", f"repos/{repo}/issues/{num}", "--input", tmp, "--jq", ".number"], capture_output=True, text=True)
        os.unlink(tmp)
        if r.returncode != 0:
            raise SystemExit(f"failed to update #{num}: {r.stderr.strip()}")
        print("pushed", f"#{num}")
    else:
        sys.stdout.write(body)


if __name__ == "__main__":
    main()
