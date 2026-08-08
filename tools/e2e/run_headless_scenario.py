#!/usr/bin/env python3
"""Run a deterministic scenario through the actual Forge client and integrated server."""
from __future__ import annotations
import argparse
import os
import signal
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GAME = ROOT / ".headlessmc/game"
HMC_JAR = ROOT / ".headlessmc/headlessmc-launcher-wrapper.jar"
HMC_JAVA = ROOT / ".headlessmc/HeadlessMC/java/jdk-21.0.12+8-jre/Contents/Home/bin/java"
GAME_JAVA = Path("/private/tmp/ic-jdk8/jdk8u502-b07/Contents/Home/bin/java")

def stop_process(process: subprocess.Popen[str]) -> None:
    if process.poll() is not None: return
    try:
        os.killpg(process.pid, signal.SIGTERM)
        process.wait(timeout=15)
    except (ProcessLookupError, subprocess.TimeoutExpired):
        try: os.killpg(process.pid, signal.SIGKILL)
        except ProcessLookupError: pass

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("scenario", nargs="?", default="workshop_adjacency")
    parser.add_argument("--timeout", type=int, default=720, help="maximum real-client runtime in seconds")
    args = parser.parse_args()
    subprocess.run([sys.executable, str(ROOT / "tools/e2e/preflight.py")], cwd=ROOT, check=True)
    subprocess.run([sys.executable, str(ROOT / "tools/e2e/prepare_headless_pack.py"),
                    "--scenario", args.scenario], cwd=ROOT, check=True)
    launcher_log = ROOT / ".headlessmc/launcher-e2e.log"
    game_log = GAME / "logs/latest.log"
    snapshot = ROOT / ".headlessmc/artifacts" / f"{args.scenario}-snapshot.json"
    snapshot.parent.mkdir(parents=True, exist_ok=True)
    command = [str(HMC_JAVA), "-Djdk.lang.Process.launchMechanism=FORK", "-Dhmc.jline.enabled=false",
               f"-Dhmc.gamedir={GAME}",
               "-Dhmc.jvmargs=-Xmx8G",
               f"-Dhmc.java.versions={GAME_JAVA}", "-jar", str(HMC_JAR)]
    print(f"E2E CLIENT: launching {args.scenario} (timeout {args.timeout}s)", flush=True)
    with launcher_log.open("w") as output:
        process = subprocess.Popen(command, cwd=ROOT, stdin=subprocess.PIPE, stdout=output,
                                   stderr=subprocess.STDOUT, text=True, start_new_session=True)
        assert process.stdin is not None
        time.sleep(2)
        process.stdin.write("launch 1 -offline -lwjgl -paulscode\n")
        process.stdin.flush()
        deadline = time.monotonic() + args.timeout
        result = "timeout"
        try:
            while time.monotonic() < deadline:
                if game_log.is_file():
                    text = game_log.read_text(errors="replace")
                    if f"IC_TEST|PASS|{args.scenario}" in text and "IC_TEST|SNAPSHOT|" in text:
                        result = "pass"
                        break
                    if f"IC_TEST|FAIL|{args.scenario}" in text or "---- Minecraft Crash Report ----" in text:
                        result = "fail"
                        break
                if process.poll() is not None:
                    result = "launcher_exit"
                    break
                time.sleep(2)
        finally:
            stop_process(process)
    if result != "pass":
        print(f"E2E CLIENT: FAIL ({result})")
        source = game_log if game_log.is_file() else launcher_log
        print("\n".join(source.read_text(errors="replace").splitlines()[-80:]))
        return 1
    check = subprocess.run([
        sys.executable, str(ROOT / "tools/e2e/runtime_log_assert.py"), str(game_log),
        "--require", f"IC_TEST|PASS|{args.scenario}",
        "--require", "IC_TEST|SNAPSHOT|", "--snapshot", str(snapshot)
    ], cwd=ROOT)
    if check.returncode == 0:
        print(f"E2E CLIENT: PASS; snapshot={snapshot}")
    return check.returncode

if __name__ == "__main__":
    sys.exit(main())
