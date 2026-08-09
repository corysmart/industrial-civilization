#!/usr/bin/env python3
"""Run the simulator's browser audit and optionally capture review screenshots."""

from __future__ import annotations

import argparse
import html
import json
import os
import re
import signal
import shutil
import socket
import subprocess
import sys
import tempfile
import time
import urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
CHROME_CANDIDATES = [
    Path("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"),
    Path("/Applications/Chromium.app/Contents/MacOS/Chromium"),
]


def free_port() -> int:
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def chrome_path() -> Path:
    for candidate in CHROME_CANDIDATES:
        if candidate.exists():
            return candidate
    for command in ("google-chrome", "chromium", "chrome"):
        found = shutil.which(command)
        if found:
            return Path(found)
    raise SystemExit("Google Chrome or Chromium is required for pixel-accurate UI audits.")


def wait_for_server(url: str) -> None:
    for _ in range(80):
        try:
            with urllib.request.urlopen(url, timeout=.25):
                return
        except Exception:
            time.sleep(.1)
    raise RuntimeError("UI simulator server did not start")


def chrome_command(chrome: Path, profile: Path, *extra: str) -> list[str]:
    return [str(chrome), "--headless=new", "--disable-gpu", "--hide-scrollbars",
            f"--user-data-dir={profile}", "--no-first-run", "--no-default-browser-check", *extra]


def invoke_chrome(command: list[str], timeout: int = 20, capture: bool = True) -> str:
    process = subprocess.Popen(command, stdout=subprocess.PIPE if capture else subprocess.DEVNULL,
                               stderr=subprocess.PIPE, text=True, start_new_session=True)
    try:
        stdout, _ = process.communicate(timeout=timeout)
    except subprocess.TimeoutExpired as error:
        # Current macOS Chrome may leave its updater alive after headless output
        # is complete. Preserve the emitted DOM/screenshot, then end the isolated
        # process group instead of making the UI test hang forever.
        os.killpg(process.pid, signal.SIGTERM)
        tail_out, _ = process.communicate(timeout=5)
        partial = error.stdout.decode() if isinstance(error.stdout, bytes) else (error.stdout or "")
        stdout = partial + (tail_out or "")
    return stdout or ""


def run_audit(base_url: str, chrome: Path, profile: Path) -> dict:
    dom = invoke_chrome(chrome_command(chrome, profile, "--disable-background-networking",
                        "--disable-component-update", "--virtual-time-budget=5000", "--dump-dom",
                        base_url + "/?audit=1"))
    match = re.search(r'<pre id="audit-output">(.*?)</pre>', dom, re.S)
    if not match:
        raise RuntimeError("Browser audit did not produce a report")
    return json.loads(html.unescape(match.group(1)))


def capture(base_url: str, chrome: Path, profile: Path, output: Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    screens = {
        "main-menu-mac-window.png": "screen=mainmenu&display=1184x666&scale=2",
        "main-menu-small-window.png": "screen=mainmenu&display=854x480&scale=2",
        "pause-menu-small-window.png": "screen=pause&display=854x480&scale=2",
        "machine-programmable-assembler.png": "screen=machine&machine=programmable_assembler&display=1184x666&scale=2",
        "machine-fusion-core.png": "screen=machine&machine=fusion_research_core&display=854x480&scale=2",
        "factions-small-window.png": "screen=factions&faction=ashline_raiders&display=854x480&scale=2",
        "terrain-warmup-small-window.png": "screen=warmup&display=854x480&scale=2",
        "ai-credits-small-window.png": "screen=credits&display=854x480&scale=2",
        "quest-home-small-window.png": "screen=questhome&display=854x480&scale=2",
        "quest-home-mac-window.png": "screen=questhome&display=1184x666&scale=2",
        "quest-canvas-small-window.png": "screen=quests&display=854x480&scale=2&quest-line=0",
        "advancements-small-window.png": "screen=advancements&display=854x480&scale=2",
        "space-map-small-window.png": "screen=spacemap&display=854x480&scale=2",
    }
    for filename, query in screens.items():
        invoke_chrome(chrome_command(chrome, profile, "--disable-background-networking",
                      "--disable-component-update", "--virtual-time-budget=3000", "--window-size=1440,900",
                      f"--screenshot={output / filename}", base_url + "/?" + query), timeout=8, capture=False)
        if not (output / filename).exists():
            raise RuntimeError(f"Chrome did not create {filename}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--screenshots", action="store_true")
    parser.add_argument("--output", type=Path, default=ROOT / "docs/ui-simulator")
    args = parser.parse_args()
    port = free_port()
    base_url = f"http://127.0.0.1:{port}"
    server = subprocess.Popen([sys.executable, str(HERE / "server.py"), "--port", str(port)],
                              stdout=subprocess.DEVNULL, stderr=subprocess.PIPE, text=True)
    try:
        wait_for_server(base_url + "/api/stamp")
        with tempfile.TemporaryDirectory(prefix="ic-ui-chrome-") as temp:
            chrome = chrome_path()
            report = run_audit(base_url, chrome, Path(temp))
            if args.screenshots:
                capture(base_url, chrome, Path(temp), args.output)
        print(json.dumps(report, indent=2))
        raise SystemExit(0 if report["passed"] else 1)
    finally:
        server.terminate()
        try:
            server.wait(timeout=3)
        except subprocess.TimeoutExpired:
            server.kill()


if __name__ == "__main__":
    main()
