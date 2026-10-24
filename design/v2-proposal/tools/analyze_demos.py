import json, os, sys, collections
root = sys.argv[1]
memes = collections.Counter()
meme_by_kind = collections.defaultdict(collections.Counter)
taxonomies = []
programs = []          # (type, name, tempo, key)
main_sequences = []    # (program name, total beats, tempo)
instruments = collections.Counter()  # (type, mode)
audios = 0
audio_bytes = 0
projects = set()
for dp, dn, fn in os.walk(root):
    if '.git' in dp: continue
    for f in fn:
        p = os.path.join(dp, f)
        if f.lower().endswith(('.wav', '.mp3', '.flac', '.ogg', '.aif', '.aiff')):
            audio_bytes += os.path.getsize(p); continue
        if f.endswith('.xj'):
            projects.add(os.path.relpath(p, root)); continue
        if not f.endswith('.json'): continue
        try:
            with open(p, encoding='utf-8') as fh: d = json.load(fh)
        except Exception as e:
            print('BAD', p, e); continue
        if not isinstance(d, dict): continue
        for kind in ('programMemes', 'instrumentMemes', 'programSequenceBindingMemes'):
            for m in d.get(kind, []) or []:
                memes[m['name'].upper()] += 1; meme_by_kind[kind][m['name'].upper()] += 1
        for t in d.get('templates', []) or []:
            cfg = t.get('config', '') or ''
            for line in cfg.splitlines():
                if 'memeTaxonomy' in line: taxonomies.append((t.get('name'), line.strip()))
        prog_by_id = {}
        for pr in d.get('programs', []) or []:
            programs.append((pr.get('type'), pr.get('name'), pr.get('tempo'), pr.get('key')))
            prog_by_id[pr['id']] = pr
        for s in d.get('programSequences', []) or []:
            pr = prog_by_id.get(s.get('programId'))
            if pr and pr.get('type') == 'Main':
                main_sequences.append((pr.get('name'), s.get('total'), pr.get('tempo')))
        for ins in d.get('instruments', []) or []:
            instruments[(ins.get('type'), ins.get('mode'))] += 1
        audios += len(d.get('instrumentAudios', []) or [])
print('PROJECTS:', len(projects)); [print('  ', p) for p in sorted(projects)]
print('PROGRAMS by type:', collections.Counter(p[0] for p in programs))
print('INSTRUMENTS by (type,mode):'); [print('  ', k, v) for k, v in sorted(instruments.items(), key=lambda kv: -kv[1])]
print('AUDIOS:', audios, ' audio bytes: %.1f MB' % (audio_bytes / 1e6))
print('TAXONOMIES:'); [print('  ', t) for t in taxonomies]
print('DISTINCT MEMES:', len(memes))
print('TOP MEMES:'); [print('  %-14s %4d  (prog %d, inst %d, bind %d)' % (m, c, meme_by_kind['programMemes'][m], meme_by_kind['instrumentMemes'][m], meme_by_kind['programSequenceBindingMemes'][m])) for m, c in memes.most_common(60)]
print('MAIN SEQUENCES (name, totalBeats, tempo):', len(main_sequences))
import statistics
secs = [t * 60.0 / tp for _, t, tp in main_sequences if t and tp]
if secs:
    print('  segment seconds: min %.1f  median %.1f  p90 %.1f  max %.1f' % (min(secs), statistics.median(secs), sorted(secs)[int(0.9 * (len(secs) - 1))], max(secs)))
    print('  total beats distribution:', collections.Counter(t for _, t, _ in main_sequences).most_common(10))
    print('  tempos:', collections.Counter(tp for _, _, tp in main_sequences).most_common(10))
