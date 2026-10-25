#!/usr/bin/env python3
"""GitHub ratchet for the XJ music V2 roadmap.

Publishes and keeps in sync, idempotently and concurrency-safely:
  labels, milestones, the umbrella roadmap issue, one issue per epic and task,
  sub-issue links (umbrella > epics > tasks), blocked-by dependencies,
  org issue fields (Start date, Target date, Effort, Priority),
  and issue bodies rendered from specs/<key>.md (or a WIP placeholder).

Usage:
  python3 gh_sync.py                 # full pass: create missing, sync changed bodies, links, deps, fields
  python3 gh_sync.py --epic e1-cues  # only that epic and its tasks (create + body sync + links)
  python3 gh_sync.py --bodies        # only re-render and push changed bodies (fast ratchet step)

State lives in state.json, guarded by an exclusive file lock so several agents may run this at once.
Spec status per key lives in spec_status.json: "placeholder" | "draft" | "reviewed" | "consistent".
"""
import fcntl, hashlib, json, os, re, subprocess, sys, tempfile, time, contextlib

HERE = os.path.dirname(os.path.abspath(__file__))
SCR = os.path.dirname(HERE)  # design/v2-proposal/roadmap
REPO = os.environ.get("XJ_REPO", "xjmusic/xjmusic")
OWNER = REPO.split("/")[0]
MASTER = os.environ.get("XJ_MASTER", os.path.join(SCR, "master.json"))
SPECS = os.environ.get("XJ_SPECS", os.path.join(SCR, "specs"))  # spec bodies; the published issues are their home
STATE = os.environ.get("XJ_STATE", os.path.join(SCR, "issue-state.json"))
LOCK = STATE + ".lock"
SCHED = os.path.join(SCR, "schedule.json")
STATUS = os.path.join(SCR, "spec-status.json")
DELAY = float(os.environ.get("XJ_WRITE_DELAY", "1.2"))
API_V = "X-GitHub-Api-Version: 2026-03-10"
PROPOSAL = "https://github.com/xjmusic/xjmusic/tree/main/design/v2-proposal"
PHASE_TITLES = {"0": "v2 · Phase 0 · Spikes and decisions", "1": "v2 · Phase 1 · Engine 2.0", "2": "v2 · Phase 2 · Station core",
                "3": "v2 · Phase 3 · Workstation 2.0, Play mode", "4": "v2 · Phase 4 · Commons", "5": "v2 · Phase 5 · Sessions",
                "6": "v2 · Phase 6 · Make mode parity", "7": "v2 · Phase 7 · The show"}
PHASE_DESC = {
 "0": "Retire the biggest unknowns with small experiments and paperwork. Exit: every spike row has a recorded decision; the C ABI spike seeds xj-engine-sys.",
 "1": "Engine 2.0 per 04-engine.md: C ABI, seeded randomness, cues and commit horizon, tempo policy, follow and conduct modes, preview, diagnostics, additive content fields. Exit: two-engine follow test green on three platforms; engine 2.0.0 tagged; Unreal plugin builds.",
 "2": "Headless Rust station: xj-engine, xj-dsp, xj-transport, xj-content, xj-control, xj-station CLI, capture and replay. Exit: plays a V1 demo with a MIDI controller at least as well as the Java workstation, 8 h without dropout, byte-exact replay.",
 "3": "The instrument: shell, performance surface, timeline and why panel, mapping UI, starter pack and tour, Ship mode, packaging. Exit: sound in under 10 s on a 2019 laptop; a new musician performs 20 minutes; five usability sessions filed.",
 "4": "The Commons: pack format, xj-content CLI, lint, registry, shows and lockfiles, xj-base@1, seed packs, licensing. Exit: two machines load byte-identical show content; an external contributor publishes a pack that passes lint.",
 "5": "Sessions: discovery, roles, harmonic timeline, claims, cue requests, handoff, failure handling, session clock, UI, soaks. Exit: two workstations and an MPC on Link play a 30-minute duet with two handoffs and no dropout.",
 "6": "Make mode parity: instrument and audio editor, template editor, program editor. Exit: every Java editing operation has a Rust equivalent; the Java workstation is archived.",
 "7": "The show: a real night with three or more stations, outside artists and an audience. Exit: a recording that stands beside Live in Boulder."}
AREA_COLORS = {"engine": "1d76db", "c-abi": "0e8a16", "rust-engine": "5319e7", "content": "fbca04", "dsp": "d93f0b", "transport": "006b75", "control": "b60205", "station": "0052cc", "session": "c2e0c6", "workstation-ui": "f9d0c4", "commons": "bfd4f2", "commons-content": "fef2c0", "ci-release": "d4c5f9", "docs": "0075ca", "legal-paperwork": "e99695", "decision": "ffd700", "unreal": "222222", "show": "ff69b4"}
STATUS_TEXT = {
 "placeholder": "🚧 **Specification in progress.** This issue was published early so the roadmap is visible and persistent; the full specification replaces this text when it is written.",
 "draft": "📝 **Draft specification.** Written against the proposal and the code; adversarial review pending.",
 "reviewed": "🔍 **Reviewed specification.** Checked for fidelity to the code and proposal and for engineering soundness; cross-epic consistency pass pending.",
 "consistent": "✅ **Specification complete.** Reviewed and checked for consistency with every other V2 issue that shares its contracts."}

# ---------------------------------------------------------------- state with lock
@contextlib.contextmanager
def locked_state():
    with open(LOCK, "w") as lf:
        fcntl.flock(lf, fcntl.LOCK_EX)
        st = json.load(open(STATE)) if os.path.exists(STATE) else {}
        for k, v in (("labels", {}), ("milestones", {}), ("issues", {}), ("subissues", []), ("deps", []), ("fields_set", []), ("body_hash", {}), ("title", {})):
            st.setdefault(k, v)
        yield st
        tmp = STATE + ".tmp"
        json.dump(st, open(tmp, "w"), indent=1, sort_keys=True)
        os.replace(tmp, STATE)
        fcntl.flock(lf, fcntl.LOCK_UN)

def read_state():
    with locked_state() as st:
        return json.loads(json.dumps(st))

# ---------------------------------------------------------------- API
def api(method, path, payload=None, headers=None, tolerate=()):
    """gh api with retries. Returns parsed JSON, {} on empty, {"_tolerated": code} for tolerated errors, None on failure."""
    for attempt in range(40):
        cmd = ["gh", "api", "-X", method, path, "-H", "Accept: application/vnd.github+json"]
        for h in headers or []:
            cmd += ["-H", h]
        tmp = None
        if payload is not None:
            fd, tmp = tempfile.mkstemp(suffix=".json")
            with os.fdopen(fd, "w") as f:
                json.dump(payload, f)
            cmd += ["--input", tmp]
        r = subprocess.run(cmd, capture_output=True, text=True)
        if tmp:
            os.unlink(tmp)
        if r.returncode == 0:
            try:
                return json.loads(r.stdout) if r.stdout.strip() else {}
            except json.JSONDecodeError:
                return {}
        err = r.stderr + r.stdout
        for code in tolerate:
            if f"HTTP {code}" in err:
                return {"_tolerated": code, "_err": err[:300]}
        if any(s in err.lower() for s in ("rate limit", "abuse", "secondary")) or "HTTP 429" in err or "HTTP 403" in err and "rate" in err.lower():
            wait = min(60 * (2 ** min(attempt, 4)), 900)
            m = re.search(r"retry-after[:\s]+(\d+)", err, re.I)
            if m:
                wait = int(m.group(1)) + 5
            print(f"  rate limited ({method} {path}); sleeping {wait}s", flush=True)
            time.sleep(wait)
            continue
        if any(s in err for s in ("HTTP 502", "HTTP 503", "HTTP 504", "timeout", "connection", "ENOTFOUND", "could not resolve")):
            time.sleep(min(30 * (attempt + 1), 300))
            continue
        print(f"  ERROR {method} {path}: {err[:400]}", flush=True)
        return None
    return None

def write(method, path, payload=None, headers=None, tolerate=()):
    res = api(method, path, payload, headers, tolerate)
    time.sleep(DELAY)
    return res

# ---------------------------------------------------------------- model
def load_master():
    m = json.load(open(MASTER))
    for t in m["tasks"]:
        t["milestone"] = PHASE_TITLES[t["phase"]]
    for e in m["epics"]:
        e["milestone"] = PHASE_TITLES[e["phase"]]
    return m

def spec_status():
    return json.load(open(STATUS)) if os.path.exists(STATUS) else {}

def labels_for(item, kind):
    labs = ["v2", f"area/{item['area']}"]
    if kind == "epic":
        labs.append("epic")
    else:
        if any(s in item["key"] for s in ("spike", "bakeoff", "bake-off")):
            labs.append("spike")
        if item["area"] == "decision" or "-dec-" in item["key"] or item["key"].endswith("-decision"):
            labs.append("decision")
    return labs

def render_body(item, kind, m, st, status):
    path = os.path.join(SPECS, item["key"] + ".md")
    has_spec = os.path.exists(path)
    s = status.get(item["key"], "draft" if has_spec else "placeholder")
    if not has_spec or s == "hold":
        has_spec = False
        s = "placeholder"
    def ref(key):
        i = st["issues"].get(key)
        return f"#{i['number']}" if i else f"`{key}`"
    parts = [f"> {STATUS_TEXT[s]}", ""]
    if has_spec:
        body = open(path).read().strip()
        body = re.sub(r"\{\{ref:([A-Za-z0-9._-]+)\}\}", lambda mm: ref(mm.group(1)), body)
        parts.append(body)
    else:
        parts.append(item.get("summary", "").strip())
        if item.get("exit"):
            parts += ["", f"**Exit:** {item['exit']}"]
        if item.get("proposal_refs"):
            parts += ["", "**Proposal:** " + ", ".join(item["proposal_refs"])]
    tasks = [t for t in m["tasks"] if t["epic"] == item["key"]] if kind == "epic" else []
    parts += ["", "---", ""]
    if kind == "task":
        parts.append(f"**Epic:** {ref(item['epic'])} · **Phase:** {item['phase']} · **Area:** `{item['area']}` · **Size:** {item['size']} (~{item['estimate_days']:g} engineer-days)")
        if item.get("blocked_by"):
            parts.append("**Blocked by:** " + ", ".join(ref(b) for b in item["blocked_by"]))
        dependents = [t["key"] for t in m["tasks"] if item["key"] in t.get("blocked_by", [])]
        if dependents:
            parts.append("**Blocks:** " + ", ".join(ref(d) for d in dependents))
    else:
        parts.append(f"**Phase:** {item['phase']} · **Area:** `{item['area']}` · **Tasks:** {len(tasks)} · **Estimate:** {sum(float(t['estimate_days']) for t in tasks):g} engineer-days")
        if item.get("blocked_by"):
            parts.append("**Blocked by epics:** " + ", ".join(ref(b) for b in item["blocked_by"]))
        if tasks:
            parts += ["", "**Tasks, in dependency order:**"]
            for t in tasks:
                parts.append(f"- [ ] {ref(t['key'])} {t['title']}")
    umb = st["issues"].get("umbrella")
    roadmap = f"#{umb['number']}" if umb else "the roadmap issue"
    parts += ["", f"<sub>Part of the v2.0 Collaborative Musical Instrument roadmap ({roadmap}, [project](https://github.com/orgs/xjmusic/projects/7)), generated from the [V2 proposal]({PROPOSAL}). Key: `{item['key']}`. Spec status: {s}. Edit freely; the tracker is the source of truth.</sub>", ""]
    return "\n".join(parts)

# ---------------------------------------------------------------- passes
def ensure_labels(m):
    want = [("v2", "5319e7", "v2.0 Collaborative Musical Instrument roadmap"), ("epic", "3e4b9e", "Parent issue grouping a set of tasks"),
            ("spike", "fbca04", "Time-boxed experiment with a recorded decision"), ("decision", "ffd700", "A decision record to be made and written down")]
    for a in sorted({x["area"] for x in m["epics"] + m["tasks"]}):
        want.append((f"area/{a}", AREA_COLORS.get(a, "cccccc"), f"V2 area: {a}"))
    st = read_state()
    existing = {l["name"] for l in (api("GET", f"repos/{REPO}/labels?per_page=100") or [])}
    for name, color, desc in want:
        if name in existing or st["labels"].get(name):
            continue
        print("label", name)
        write("POST", f"repos/{REPO}/labels", {"name": name, "color": color, "description": desc}, tolerate=(422,))
        with locked_state() as s:
            s["labels"][name] = True

def ensure_milestones():
    sched = json.load(open(SCHED)) if os.path.exists(SCHED) else {"milestones": {}}
    existing = {x["title"]: x["number"] for x in (api("GET", f"repos/{REPO}/milestones?state=all&per_page=100") or [])}
    for p, title in PHASE_TITLES.items():
        st = read_state()
        if title in st["milestones"]:
            continue
        if title in existing:
            with locked_state() as s:
                s["milestones"][title] = existing[title]
            continue
        payload = {"title": title, "description": PHASE_DESC[p]}
        due = sched.get("milestones", {}).get(title, {}).get("due")
        if due:
            payload["due_on"] = due + "T08:00:00Z"
        print("milestone", title)
        res = write("POST", f"repos/{REPO}/milestones", payload)
        if res and "number" in res:
            with locked_state() as s:
                s["milestones"][title] = res["number"]

def ensure_umbrella():
    st = read_state()
    if "umbrella" in st["issues"]:
        return
    print("create umbrella roadmap issue")
    res = write("POST", f"repos/{REPO}/issues", {"title": "v2.0 Collaborative Musical Instrument: roadmap", "labels": ["v2", "epic"], "type": "Feature",
        "body": f"Roadmap index for the [V2 proposal]({PROPOSAL}) and [project #7](https://github.com/orgs/xjmusic/projects/7). Epics are sub-issues of this issue; tasks are sub-issues of their epics; blocked-by links form the waterfall. This body is regenerated as the plan ratchets forward."})
    if res and "number" in res:
        with locked_state() as s:
            s["issues"]["umbrella"] = {"number": res["number"], "id": res["id"], "node_id": res.get("node_id"), "url": res["html_url"], "kind": "umbrella"}

def topo(tasks):
    keys = {t["key"]: t for t in tasks}
    indeg = {k: 0 for k in keys}; out = {k: [] for k in keys}
    for t in tasks:
        for b in t.get("blocked_by", []):
            if b in keys:
                indeg[t["key"]] += 1; out[b].append(t["key"])
    order, ready = [], sorted(k for k, d in indeg.items() if d == 0)
    while ready:
        k = ready.pop(0); order.append(k)
        for n in out[k]:
            indeg[n] -= 1
            if indeg[n] == 0:
                ready.append(n); ready.sort()
    return [keys[k] for k in order] + [t for t in tasks if t["key"] not in set(order)]

def create_or_sync(item, kind, m, status, create_only=False):
    st = read_state()
    body = render_body(item, kind, m, st, status)
    h = hashlib.sha256((item["title"] + "\n" + body).encode()).hexdigest()
    issue = st["issues"].get(item["key"])
    if not issue:
        print(f"create {kind} {item['key']}: {item['title']}", flush=True)
        payload = {"title": item["title"], "body": body, "labels": labels_for(item, kind),
                   "milestone": st["milestones"].get(item["milestone"]), "type": "Feature" if kind == "epic" else "Task"}
        res = write("POST", f"repos/{REPO}/issues", payload)
        if not res or "number" not in res:
            print("  failed to create", item["key"]); return
        with locked_state() as s:
            s["issues"][item["key"]] = {"number": res["number"], "id": res["id"], "node_id": res.get("node_id"), "url": res["html_url"], "kind": kind}
            s["body_hash"][item["key"]] = h if "`e" not in body and "`p" not in body else "stale"
        return
    if create_only:
        return
    if st["body_hash"].get(item["key"]) == h:
        return
    print(f"sync {kind} {item['key']}", flush=True)
    res = write("PATCH", f"repos/{REPO}/issues/{issue['number']}", {"title": item["title"], "body": body})
    if res is not None:
        with locked_state() as s:
            s["body_hash"][item["key"]] = h

def link_subissues(m, only=None):
    for e in m["epics"]:
        if only and e["key"] != only:
            continue
        pairs = [("umbrella", e["key"])] + [(e["key"], t["key"]) for t in m["tasks"] if t["epic"] == e["key"]]
        for parent, child in pairs:
            st = read_state()
            if [parent, child] in st["subissues"] or parent not in st["issues"] or child not in st["issues"]:
                continue
            res = write("POST", f"repos/{REPO}/issues/{st['issues'][parent]['number']}/sub_issues", {"sub_issue_id": st["issues"][child]["id"]}, tolerate=(422,))
            if res is not None:
                with locked_state() as s:
                    s["subissues"].append([parent, child])

def link_deps(m, only=None):
    for coll in (m["tasks"], m["epics"]):
        for t in coll:
            if only and t["key"] != only and t.get("epic") != only:
                continue
            for b in t.get("blocked_by", []):
                st = read_state()
                if [t["key"], b] in st["deps"] or t["key"] not in st["issues"] or b not in st["issues"]:
                    continue
                res = write("POST", f"repos/{REPO}/issues/{st['issues'][t['key']]['number']}/dependencies/blocked_by", {"issue_id": st["issues"][b]["id"]}, tolerate=(422,))
                if res is not None:
                    with locked_state() as s:
                        s["deps"].append([t["key"], b])

def set_fields(m, only=None):
    if not os.path.exists(SCHED):
        return
    sched = json.load(open(SCHED))
    fields = {f["name"]: f for f in (api("GET", f"orgs/{OWNER}/issue-fields", headers=[API_V]) or [])}
    if not all(n in fields for n in ("Start date", "Target date", "Effort", "Priority")):
        print("org issue fields missing; skipping"); return
    tk = {t["key"]: t for t in m["tasks"]}; memo = {}
    def lp(k):
        if k in memo: return memo[k]
        best = (tk[k]["estimate_days"], [k])
        for b in tk[k].get("blocked_by", []):
            d, p = lp(b)
            if d + tk[k]["estimate_days"] > best[0]: best = (d + tk[k]["estimate_days"], p + [k])
        memo[k] = best; return best
    crit = set(lp(max(tk, key=lambda k: lp(k)[0]))[1])
    order = {"Urgent": 0, "High": 1, "Medium": 2, "Low": 3}
    def prio(t):
        if t["epic"] == "e0-paperwork": return "Urgent"
        if t["key"] in crit: return "High"
        return "Low" if t["phase"] in ("6", "7") else "Medium"
    vals = {}
    for t in m["tasks"]:
        d = sched["tasks"].get(t["key"], {})
        vals[t["key"]] = {"Start date": d.get("start"), "Target date": d.get("target"), "Effort": {"XS": "Low", "S": "Low", "M": "Medium", "L": "High"}[t["size"]], "Priority": prio(t)}
    for e in m["epics"]:
        kids = [t for t in m["tasks"] if t["epic"] == e["key"]]; d = sched["epics"].get(e["key"], {})
        days = sum(float(t["estimate_days"]) for t in kids)
        vals[e["key"]] = {"Start date": d.get("start"), "Target date": d.get("target"), "Effort": "Low" if days < 10 else "Medium" if days <= 25 else "High",
                          "Priority": min((vals[t["key"]]["Priority"] for t in kids), key=lambda p: order[p], default="Medium")}
    if sched.get("milestones"):
        vals["umbrella"] = {"Start date": min(v["start"] for v in sched["milestones"].values()), "Target date": max(v["due"] for v in sched["milestones"].values()), "Effort": "High", "Priority": "High"}
    for key, v in vals.items():
        if only and key != only and tk.get(key, {}).get("epic") != only:
            continue
        st = read_state()
        sig = key + ":" + json.dumps(v, sort_keys=True)
        if sig in st["fields_set"] or key not in st["issues"]:
            continue
        body = {"issue_field_values": [{"field_id": fields[n]["id"], "value": x} for n, x in v.items() if x]}
        res = write("POST", f"repos/{REPO}/issues/{st['issues'][key]['number']}/issue-field-values", body, headers=[API_V])
        if res is not None:
            with locked_state() as s:
                s["fields_set"] = [x for x in s["fields_set"] if not x.startswith(key + ":")] + [sig]

def main():
    args = sys.argv[1:]
    only = args[args.index("--epic") + 1] if "--epic" in args else None
    bodies_only = "--bodies" in args
    m = load_master(); status = spec_status()
    if not bodies_only:
        ensure_labels(m); ensure_milestones(); ensure_umbrella()
    epics = [e for e in m["epics"] if not only or e["key"] == only]
    tasks = [t for t in topo(m["tasks"]) if not only or t["epic"] == only]
    # pass 1: create everything that is missing (fast persistence), pass 2: sync bodies (now that numbers exist)
    if not bodies_only:
        for e in epics:
            create_or_sync(e, "epic", m, status, create_only=True)
        for t in tasks:
            create_or_sync(t, "task", m, status, create_only=True)
    st0 = read_state()
    for e in epics:
        if bodies_only and e["key"] not in st0["issues"]:
            continue
        create_or_sync(e, "epic", m, status)
    for t in tasks:
        if bodies_only and t["key"] not in st0["issues"]:
            continue
        create_or_sync(t, "task", m, status)
    if bodies_only:
        print("bodies synced"); return
    link_subissues(m, only)
    link_deps(m, only)
    set_fields(m, only)
    st = read_state()
    print(f"done: {len(st['issues'])} issues, {len(st['subissues'])} sub-issue links, {len(st['deps'])} dependencies, {len(st['fields_set'])} field sets")

if __name__ == "__main__":
    main()
