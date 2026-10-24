import json, os, sys, re, collections
root = sys.argv[1]
for proj in sorted(os.listdir(root)):
    pd = os.path.join(root, proj)
    if not os.path.isdir(os.path.join(pd, 'libraries')): continue
    memes = collections.defaultdict(collections.Counter)
    tax = []
    for dp, dn, fn in os.walk(pd):
        if 'build' in dp.split(os.sep): continue
        for f in fn:
            if not f.endswith('.json'): continue
            with open(os.path.join(dp, f), encoding='utf-8') as fh: d = json.load(fh)
            for kind in ('programMemes', 'instrumentMemes', 'programSequenceBindingMemes'):
                for m in d.get(kind, []) or []: memes[kind][m['name'].upper()] += 1
            for t in d.get('templates', []) or []:
                cfg = t.get('config', '') or ''
                m = re.search(r'memeTaxonomy\s*=\s*\[(.*?)\n\s*\]', cfg, re.S)
                block = re.sub(r'\s+', ' ', m.group(1)).strip() if m else '(none)'
                tax.append((t['name'], block))
    print('=== %s' % proj)
    for name, block in tax: print('  template %-22s memeTaxonomy: %s' % (name, block))
    for kind in ('programMemes', 'instrumentMemes', 'programSequenceBindingMemes'):
        print('  %-28s %s' % (kind, ', '.join('%s(%d)' % kv for kv in memes[kind].most_common())))
