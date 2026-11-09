#!/usr/bin/env python3
"""Validate a decomposition JSON (candidate or master). Usage: python3 -I validate.py file.json
Exit 0 and print a summary when valid; print every problem and exit 1 otherwise."""
import json, sys, re, collections
PHASES = {"0","1","2","3","4","5","6","7"}
AREAS = {"engine","c-abi","rust-engine","content","dsp","transport","control","station","session","workstation-ui","commons","commons-content","ci-release","docs","legal-paperwork","decision","unreal","show"}
SIZES = {"XS": (0.5, 1), "S": (2, 3), "M": (4, 7), "L": (8, 12)}
KEY = re.compile(r"^[a-z0-9][a-z0-9-]{2,60}$")
def main(path):
    d = json.load(open(path))
    probs = []
    epics = d.get("epics", []); tasks = d.get("tasks", [])
    ek = [e.get("key") for e in epics]; tk = [t.get("key") for t in tasks]
    for name, keys in (("epic", ek), ("task", tk)):
        dup = [k for k, c in collections.Counter(keys).items() if c > 1]
        if dup: probs.append(f"duplicate {name} keys: {dup}")
        for k in keys:
            if not k or not KEY.match(k): probs.append(f"bad {name} key {k!r}")
    if set(ek) & set(tk): probs.append(f"keys shared between epics and tasks: {set(ek) & set(tk)}")
    eset, tset = set(ek), set(tk)
    for e in epics:
        for f in ("key","title","phase","area","summary"):
            if not e.get(f): probs.append(f"epic {e.get('key')} missing {f}")
        if e.get("phase") not in PHASES: probs.append(f"epic {e.get('key')} bad phase {e.get('phase')}")
        if e.get("area") not in AREAS: probs.append(f"epic {e.get('key')} bad area {e.get('area')}")
        for b in e.get("blocked_by", []):
            if b not in eset: probs.append(f"epic {e['key']} blocked by unknown epic {b}")
            if b == e.get("key"): probs.append(f"epic {e['key']} blocked by itself")
        if len(e.get("title","")) > 90: probs.append(f"epic {e['key']} title > 90 chars")
    per_epic = collections.Counter(t.get("epic") for t in tasks)
    for e in epics:
        if per_epic[e["key"]] < 2: probs.append(f"epic {e['key']} has {per_epic[e['key']]} tasks (<2)")
    for t in tasks:
        for f in ("key","title","epic","phase","area","size","estimate_days","summary"):
            if t.get(f) in (None, ""): probs.append(f"task {t.get('key')} missing {f}")
        if t.get("epic") not in eset: probs.append(f"task {t.get('key')} unknown epic {t.get('epic')}")
        if t.get("phase") not in PHASES: probs.append(f"task {t.get('key')} bad phase {t.get('phase')}")
        if t.get("area") not in AREAS: probs.append(f"task {t.get('key')} bad area {t.get('area')}")
        if t.get("size") not in SIZES: probs.append(f"task {t.get('key')} bad size {t.get('size')}")
        else:
            lo, hi = SIZES[t["size"]]
            try:
                ed = float(t.get("estimate_days"))
                if not (lo <= ed <= hi): probs.append(f"task {t['key']} estimate {ed} outside {t['size']} band {lo}-{hi}")
            except (TypeError, ValueError): probs.append(f"task {t.get('key')} bad estimate_days")
        if len(t.get("title","")) > 90: probs.append(f"task {t.get('key')} title > 90 chars")
        for b in t.get("blocked_by", []):
            if b not in tset: probs.append(f"task {t['key']} blocked by unknown task {b}")
            if b == t.get("key"): probs.append(f"task {t['key']} blocked by itself")
        ep = next((e for e in epics if e["key"] == t.get("epic")), None)
        if ep and ep.get("phase") != t.get("phase"): probs.append(f"task {t['key']} phase {t.get('phase')} differs from epic phase {ep.get('phase')}")
    # cycles (tasks) and epic cycles
    def cyc(items):
        g = {i["key"]: [b for b in i.get("blocked_by", []) if b in {x["key"] for x in items}] for i in items}
        WHITE, GRAY, BLACK = 0, 1, 2; color = {k: WHITE for k in g}; found = []
        def dfs(u, stack):
            color[u] = GRAY; stack.append(u)
            for v in g[u]:
                if color[v] == GRAY: found.append(stack[stack.index(v):] + [v])
                elif color[v] == WHITE: dfs(v, stack)
            stack.pop(); color[u] = BLACK
        for k in g:
            if color[k] == WHITE: dfs(k, [])
        return found
    for c in cyc(tasks)[:5]: probs.append("task cycle: " + " -> ".join(c))
    for c in cyc(epics)[:5]: probs.append("epic cycle: " + " -> ".join(c))
    # cross-phase sanity: a task may only be blocked by tasks in the same or an earlier phase
    tp = {t["key"]: t.get("phase") for t in tasks}
    for t in tasks:
        for b in t.get("blocked_by", []):
            if b in tp and tp[b] and t.get("phase") and tp[b] > t["phase"]:
                probs.append(f"task {t['key']} (phase {t['phase']}) blocked by later-phase task {b} (phase {tp[b]})")
    # orphans: tasks with no blockers and no dependents, outside phase 0
    dependents = collections.Counter(b for t in tasks for b in t.get("blocked_by", []))
    for t in tasks:
        if t.get("phase") != "0" and not t.get("blocked_by") and dependents[t["key"]] == 0:
            probs.append(f"task {t['key']} is isolated (no blockers, no dependents)")
    if probs:
        print("\n".join(probs)); print(f"\n{len(probs)} problem(s)"); sys.exit(1)
    days = sum(float(t["estimate_days"]) for t in tasks)
    print(f"OK: {len(epics)} epics, {len(tasks)} tasks, {days:.0f} engineer-days; per phase: " + ", ".join(f"P{p}={sum(1 for t in tasks if t['phase']==p)}" for p in sorted(PHASES)))
if __name__ == "__main__":
    main(sys.argv[1])
