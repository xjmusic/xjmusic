#!/usr/bin/env python3
"""Recover the specification bodies from the live GitHub issues into specs/<key>.md.

The issues are the home of the specifications (README.md). This is the inverse of
gh_sync.py's body rendering: it strips the status header line and the generated
trailer from every issue body named in issue-map.json and writes the rest to
specs/<key>.md, so that `gh_sync.py --bodies` (or any local edit) can start from
what is actually published. Run it before editing specs locally:

  python3 -I tools/pull_specs.py            # all issues in issue-map.json
  python3 -I tools/pull_specs.py e1-cues    # one epic and its tasks
  python3 -I tools/pull_specs.py --force    # overwrite local specs that differ

A local spec that differs from the published one is never overwritten without
--force: the published text is written beside it as <key>.remote.md instead, so
an unpushed local edit and an edit made on GitHub can be compared and merged.

Numbers in the recovered text stay as `#<number>`; new cross-references may use
`{{ref:<key>}}`, which the renderer resolves.
"""
import json, os, re, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SPECS = os.environ.get("XJ_SPECS", os.path.join(ROOT, "specs"))
# Placeholder bodies (the 🚧 header) carry no specification and are skipped.
STATUS_RE = re.compile(r"^> (📝|🔍|✅) \*\*[^\n]*\n\n", re.S)
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


def fetch_body(repo, number):
    r = subprocess.run(["gh", "api", f"repos/{repo}/issues/{number}", "--jq", ".body"], capture_output=True, text=True)
    if r.returncode != 0:
        raise SystemExit(f"gh api failed for #{number}: {r.stderr.strip()}")
    # Bodies edited in the web UI come back with CRLF line endings.
    return r.stdout.replace("\r\n", "\n")


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
    args = sys.argv[1:]
    force = "--force" in args
    rest = [a for a in args if a != "--force"]
    only = rest[0] if rest else None
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    master = json.load(open(os.path.join(ROOT, "master.json")))
    repo = repo_of(imap)
    epic_of = {t["key"]: t["epic"] for t in master["tasks"]}
    os.makedirs(SPECS, exist_ok=True)
    written = kept = 0
    for key, v in sorted(imap.items(), key=lambda kv: kv[1]["number"]):
        if key == "umbrella":
            continue
        if only and key != only and epic_of.get(key) != only:
            continue
        spec = split(fetch_body(repo, v["number"]))
        if spec is None:
            print(f"placeholder or unrecognized body, skipped: {key} (#{v['number']})")
            continue
        path = os.path.join(SPECS, key + ".md")
        if not force and os.path.exists(path):
            with open(path) as f:
                local = f.read()
            if local != spec:
                with open(os.path.join(SPECS, key + ".remote.md"), "w") as f:
                    f.write(spec)
                print(f"local spec differs, kept it and wrote {key}.remote.md (#{v['number']})")
                kept += 1
                continue
        with open(path, "w") as f:
            f.write(spec)
        written += 1
    print(f"recovered {written} specifications into {SPECS}; {kept} differing local specs kept")


if __name__ == "__main__":
    main()
