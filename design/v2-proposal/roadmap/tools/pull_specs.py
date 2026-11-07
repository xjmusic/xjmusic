#!/usr/bin/env python3
"""Recover the specification bodies from the live GitHub issues into specs/<key>.md.

The issues are the home of the specifications (README.md). This is the inverse of
gh_sync.py's body rendering: it strips the status header line and the generated
trailer from every issue body named in issue-map.json and writes the rest to
specs/<key>.md, so that `gh_sync.py --bodies` (or any local edit) can start from
what is actually published. Run it before editing specs locally:

  python3 -I tools/pull_specs.py            # all issues in issue-map.json
  python3 -I tools/pull_specs.py e1-cues    # one epic and its tasks

Numbers in the recovered text stay as `#<number>`; new cross-references may use
`{{ref:<key>}}`, which the renderer resolves.
"""
import json, os, re, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
REPO = os.environ.get("XJ_REPO", "xjmusic/xjmusic")
SPECS = os.environ.get("XJ_SPECS", os.path.join(ROOT, "specs"))
STATUS_RE = re.compile(r"^> (🚧|📝|🔍|✅) \*\*[^\n]*\n\n", re.S)


def fetch_body(number):
    r = subprocess.run(["gh", "api", f"repos/{REPO}/issues/{number}", "--jq", ".body"], capture_output=True, text=True)
    if r.returncode != 0:
        raise SystemExit(f"gh api failed for #{number}: {r.stderr.strip()}")
    return r.stdout


def split(body):
    """Return the spec text inside a rendered body, or None if the body is a placeholder or unrecognized."""
    m = STATUS_RE.match(body)
    if not m:
        return None
    rest = body[m.end():]
    idx = max(rest.rfind("\n---\n\n**Epic:**"), rest.rfind("\n---\n\n**Phase:**"))
    if idx < 0:
        return None
    return rest[:idx].strip() + "\n"


def main():
    only = sys.argv[1] if len(sys.argv) > 1 else None
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    master = json.load(open(os.path.join(ROOT, "master.json")))
    epic_of = {t["key"]: t["epic"] for t in master["tasks"]}
    os.makedirs(SPECS, exist_ok=True)
    n = 0
    for key, v in sorted(imap.items(), key=lambda kv: kv[1]["number"]):
        if key == "umbrella":
            continue
        if only and key != only and epic_of.get(key) != only:
            continue
        spec = split(fetch_body(v["number"]))
        if spec is None:
            print(f"placeholder or unrecognized body, skipped: {key} (#{v['number']})")
            continue
        with open(os.path.join(SPECS, key + ".md"), "w") as f:
            f.write(spec)
        n += 1
    print(f"recovered {n} specifications into {SPECS}")


if __name__ == "__main__":
    main()
