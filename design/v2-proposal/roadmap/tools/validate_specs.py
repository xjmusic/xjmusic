#!/usr/bin/env python3
"""Validate spec files for one epic (or all). Usage: python3 -I validate_specs.py <master.json> <specs dir> [epic-key]"""
import json, os, re, sys
TASK_HEADS = ["### Context", "### Scope", "### Design", "### Acceptance criteria", "### Test plan", "### Dependencies and interfaces", "### Risks and open points", "### References", "### New names introduced"]
EPIC_HEADS = ["### Goal and exit criteria", "### Why this order", "### Interfaces this epic owns or consumes", "### Risks", "### References"]
def words(s): return len(re.findall(r"\S+", s))
def check(path, heads, lo, hi, keys, probs, key):
    if not os.path.exists(path):
        probs.append(f"{key}: missing file {path}"); return
    s = open(path).read()
    pos = -1
    for h in heads:
        i = s.find("\n" + h)
        if i < 0: probs.append(f"{key}: missing heading '{h}'"); continue
        if i < pos: probs.append(f"{key}: heading '{h}' out of order")
        pos = i
    w = words(s)
    if w < lo: probs.append(f"{key}: too short ({w} words < {lo})")
    if w > hi: probs.append(f"{key}: too long ({w} words > {hi})")
    for m in re.findall(r"\{\{ref:([^}]+)\}\}", s):
        if m not in keys: probs.append(f"{key}: unresolved ref {m}")
    if re.search(r"\bTBD\b|\bTODO\b|lorem ipsum", s): probs.append(f"{key}: contains TBD/TODO placeholder")
    if s.lstrip().startswith("#"): probs.append(f"{key}: body must start with a summary paragraph, not a heading")
    if "- [ ]" not in s and heads is TASK_HEADS: probs.append(f"{key}: acceptance criteria has no checklist items")
def main():
    master = json.load(open(sys.argv[1])); d = sys.argv[2]; only = sys.argv[3] if len(sys.argv) > 3 else None
    keys = {e["key"] for e in master["epics"]} | {t["key"] for t in master["tasks"]}
    probs = []; n = 0
    for e in master["epics"]:
        if only and e["key"] != only: continue
        check(os.path.join(d, e["key"] + ".md"), EPIC_HEADS, 350, 1400, keys, probs, e["key"]); n += 1
        for t in master["tasks"]:
            if t["epic"] != e["key"]: continue
            check(os.path.join(d, t["key"] + ".md"), TASK_HEADS, 600, 3200, keys, probs, t["key"]); n += 1
    if probs:
        print("\n".join(probs)); print(f"\n{len(probs)} problem(s) across {n} files"); sys.exit(1)
    print(f"OK: {n} spec files valid")
if __name__ == "__main__": main()
