#!/usr/bin/env python3
"""Patch KleeSlabs' bundled Quark table for the pack's Quark block set."""

import json
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path


RESOURCE = "assets/kleeslabs/compat/quark.json"
ABSENT_PATTERN_SLABS = {
    "biotite_slab",
    "stone_basalt_slab",
    "stone_basalt_bricks_slab",
    "stone_marble_slab",
}


def patch_jar(jar: Path) -> None:
    with zipfile.ZipFile(jar, "r") as source:
        compat = json.loads(source.read(RESOURCE).decode("utf-8"))
        pattern_slabs = compat.get("pattern_slabs", [])
        filtered_pattern_slabs = [
            slab for slab in pattern_slabs if slab not in ABSENT_PATTERN_SLABS
        ]
        if (
            compat.get("silent") is True
            and filtered_pattern_slabs == pattern_slabs
        ):
            print(f"Already patched: {jar}")
            return
        compat["silent"] = True
        compat["pattern_slabs"] = filtered_pattern_slabs
        replacement = (json.dumps(compat, indent=2) + "\n").encode("utf-8")
        with tempfile.NamedTemporaryFile(
            prefix="kleeslabs-", suffix=".jar", dir=str(jar.parent), delete=False
        ) as handle:
            temporary = Path(handle.name)
        try:
            with zipfile.ZipFile(temporary, "w") as target:
                for entry in source.infolist():
                    payload = replacement if entry.filename == RESOURCE else source.read(entry)
                    target.writestr(entry, payload)
            shutil.copymode(jar, temporary)
            temporary.replace(jar)
        finally:
            if temporary.exists():
                temporary.unlink()
    print(f"Patched Quark slab compatibility table: {jar}")


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("usage: patch_kleeslabs_quark_compat.py PATH_TO_JAR")
    patch_jar(Path(sys.argv[1]).resolve())


if __name__ == "__main__":
    main()
