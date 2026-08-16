#!/usr/bin/env python3
"""Generate the deterministic empty multiplayer list expected by Default Options."""

import gzip
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / "config" / "defaultoptions" / "servers.dat"

# NBT: unnamed root compound containing an empty TAG_List<Compound> named servers.
EMPTY_SERVER_LIST_NBT = (
    b"\x0a\x00\x00"
    b"\x09\x00\x07servers"
    b"\x0a\x00\x00\x00\x00"
    b"\x00"
)


def main() -> None:
    TARGET.write_bytes(gzip.compress(EMPTY_SERVER_LIST_NBT, mtime=0))
    print(f"Wrote deterministic empty server list: {TARGET}")


if __name__ == "__main__":
    main()
