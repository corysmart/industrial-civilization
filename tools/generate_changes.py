#!/usr/bin/env python3
"""Generate an exact baseline-to-current file inventory without touching game state."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / ".backups" / "pre-industrial-civilization"
OUTPUT = ROOT / "manifest" / "changed-files.json"
EXCLUDED_PREFIXES = (
    ".git/",
    ".backups/",
    "cache/",
    "logs/",
    "vintagefix/",
    "development/IndustrialCivilizationCore/.gradle-user-home/",
)


def included(relative):
    value = relative.as_posix()
    return value != ".DS_Store" and not any(value.startswith(prefix) for prefix in EXCLUDED_PREFIXES)


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def inventory(root):
    result = {}
    for path in sorted(root.rglob("*")):
        if path.is_file():
            relative = path.relative_to(root)
            if included(relative):
                result[relative.as_posix()] = sha256(path)
    return result


before = inventory(BASE)
after = inventory(ROOT)
output_relative = OUTPUT.relative_to(ROOT).as_posix()

added = sorted(set(after) - set(before))
if output_relative not in added:
    added.append(output_relative)
    added.sort()
modified = sorted(path for path in set(after) & set(before) if after[path] != before[path])
removed = sorted(set(before) - set(after))

payload = {
    "baseline": ".backups/pre-industrial-civilization",
    "scope_note": "Git metadata, disposable cache, logs, VintageFix caches, the rollback snapshot itself, and Gradle download caches are excluded.",
    "added": added,
    "modified": modified,
    "removed": removed,
    "disposable_cache_removed": [
        "cache/ic2cuumatter-1.12.2-1.1.3.zip",
        "cache/project-e-1.12.2-1.4.8-14-technic.zip",
        "cache/view-emc-1.12.2-8.zip",
        "cache/z-tekkit2-configs-1.2.6.zip",
    ],
    "counts": {"added": len(added), "modified": len(modified), "removed": len(removed)},
}
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
OUTPUT.write_text(json.dumps(payload, indent=2) + "\n")
print(f"Wrote {OUTPUT.relative_to(ROOT)}: {len(added)} added, {len(modified)} modified, {len(removed)} removed")
