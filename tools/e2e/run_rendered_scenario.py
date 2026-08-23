#!/usr/bin/env python3
"""Run the E2E scenario with official LWJGL so GUI screenshots contain real pixels."""
from __future__ import annotations

import os
import argparse
import re
import signal
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GAME = ROOT / ".headlessmc/game"
RENDER_XMX = os.environ.get("IC_RENDER_XMX", "4G")
LIBRARIES = Path("/Users/cory/Library/Application Support/minecraft/libraries")
NATIVES = Path("/Users/cory/Library/Application Support/technic/modpacks/industrial-civilization-astra/bin/natives")
JAVA = Path(os.environ.get(
    "IC_JAVA8", "/private/tmp/astra-jdk8/jdk8u502-b07/Contents/Home/bin/java"))

REPLACEMENTS = {
    "librarylwjglopenal-20100824.jar":
        LIBRARIES / "com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar",
    "soundsystem-20120107.jar":
        LIBRARIES / "com/paulscode/soundsystem/20120107/soundsystem-20120107.jar",
    "lwjgl-2.9.2-nightly-20140822.jar":
        LIBRARIES / "org/lwjgl/lwjgl/lwjgl/2.9.2-nightly-20140822/lwjgl-2.9.2-nightly-20140822.jar",
    "lwjgl_util-2.9.2-nightly-20140822.jar":
        LIBRARIES / "org/lwjgl/lwjgl/lwjgl_util/2.9.2-nightly-20140822/lwjgl_util-2.9.2-nightly-20140822.jar",
    "log4j-api-2.15.0.jar":
        LIBRARIES / "org/apache/logging/log4j/log4j-api/2.15.0/log4j-api-2.15.0.jar",
    "log4j-core-2.15.0.jar":
        LIBRARIES / "org/apache/logging/log4j/log4j-core/2.15.0/log4j-core-2.15.0.jar",
}

def classpath_from_verified_launch() -> list[str]:
    log = ROOT / ".headlessmc/launcher-e2e.log"
    entries: list[str] = []
    collecting = False
    for line in log.read_text(errors="replace").splitlines():
        if "Java classpath at launch is:" in line:
            entries = []
            collecting = True
            continue
        if collecting and "Java library path at launch is:" in line:
            break
        if not collecting: continue
        match = re.search(r"\[FML\]:\s{5}(.+)$", line)
        if not match: continue
        path = Path(match.group(1))
        if path.name == "headlessmc-lwjgl.jar": continue
        entries.append(str(REPLACEMENTS.get(path.name, path)))
    if not entries: raise RuntimeError("could not recover verified Forge classpath")
    missing = [entry for entry in entries if not Path(entry).is_file()]
    if missing: raise RuntimeError("rendered classpath entries missing: " + ", ".join(missing))
    return entries

def stop(process: subprocess.Popen[str]) -> None:
    if process.poll() is not None: return
    os.killpg(process.pid, signal.SIGTERM)
    try: process.wait(timeout=20)
    except subprocess.TimeoutExpired: os.killpg(process.pid, signal.SIGKILL)

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("scenario", nargs="?", default="advancement_ui")
    parser.add_argument("--seed-world", type=Path)
    parser.add_argument("--record", type=Path,
                        help="record the visible scenario interval to an H.264 MP4")
    args = parser.parse_args()
    scenario = args.scenario
    subprocess.run([sys.executable, str(ROOT / "tools/e2e/preflight.py")], cwd=ROOT, check=True)
    subprocess.run([sys.executable, str(ROOT / "tools/e2e/prepare_headless_pack.py"),
                    "--scenario", scenario], cwd=ROOT, check=True)
    if args.seed_world:
        source = args.seed_world.expanduser().resolve()
        if not (source / "level.dat").is_file():
            raise SystemExit(f"seed world is not a Minecraft save: {source}")
        target = GAME / "saves" / ("ic-e2e-" + re.sub(r"[^a-z0-9_-]", "-", scenario.lower()))
        shutil.copytree(source, target)
        print(f"RENDERED CLIENT: seeded disposable world from {source.name}", flush=True)
    command = [
        str(JAVA), f"-Xmx{RENDER_XMX}", "-XstartOnFirstThread",
        f"-Djava.library.path={NATIVES}", "-cp", os.pathsep.join(classpath_from_verified_launch()),
        "net.minecraft.launchwrapper.Launch", "--username", "Offline", "--version",
        "1.12.2-forge-14.23.5.2860", "--gameDir", str(GAME), "--assetsDir",
        "/Users/cory/Library/Application Support/minecraft/assets", "--assetIndex", "1.12",
        "--uuid", "22689332a7fd41919600b0fe1135ee34", "--accessToken", "",
        "--userType", "msa", "--tweakClass", "net.minecraftforge.fml.common.launcher.FMLTweaker",
        "--versionType", "Forge", "--width", "1280", "--height", "720",
    ]
    frames_path = GAME / "screenshots/quarry-video"
    if args.record:
        command.insert(3, "-Dic.e2e.captureFrames=true")
        shutil.rmtree(frames_path, ignore_errors=True)
    output_path = ROOT / ".headlessmc/direct-rendered.log"
    game_log = GAME / "logs/latest.log"
    print(f"RENDERED CLIENT: launching {scenario}", flush=True)
    with output_path.open("w") as output:
        process = subprocess.Popen(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT,
                                   text=True, start_new_session=True)
        recording_announced = False
        recording_path = args.record.expanduser().resolve() if args.record else None
        if recording_path: recording_path.parent.mkdir(parents=True, exist_ok=True)
        deadline = time.monotonic() + 900
        result = "timeout"
        try:
            while time.monotonic() < deadline:
                text = "\n".join(path.read_text(errors="replace")
                                 for path in (game_log, output_path) if path.is_file())
                if recording_path and not recording_announced and (
                        f"IC_TEST|STATE|{scenario}|phase=natural_lifecycle_started" in text):
                    recording_announced = True
                    print(f"RENDERED CLIENT: recording {recording_path}", flush=True)
                pass_ready = f"IC_TEST|PASS|{scenario}" in text
                if scenario == "advancement_ui":
                    pass_ready = pass_ready and "IC_E2E|SCREENSHOT|" in text
                if pass_ready:
                    result = "pass"
                    time.sleep(3)
                    break
                if f"IC_TEST|FAIL|{scenario}" in text or "---- Minecraft Crash Report ----" in text:
                    result = "fail"
                    break
                if process.poll() is not None:
                    result = "client_exit"
                    break
                time.sleep(2)
        finally:
            stop(process)
    if result == "pass":
        if recording_path:
            frames = sorted(frames_path.glob("frame-*.png"))
            if not frames:
                print("RENDERED CLIENT: FAIL (framebuffer recording missing)")
                return 1
            encode = subprocess.run([
                "ffmpeg", "-y", "-framerate", "5", "-i",
                str(frames_path / "frame-%05d.png"), "-c:v", "libx264",
                "-preset", "veryfast", "-crf", "20", "-pix_fmt", "yuv420p",
                str(recording_path)
            ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            if (encode.returncode != 0 or not recording_path.is_file()
                    or recording_path.stat().st_size == 0):
                print("RENDERED CLIENT: FAIL (video encode failed)")
                return 1
            print(f"RENDERED CLIENT: encoded {len(frames)} framebuffer frames", flush=True)
        print("RENDERED CLIENT: PASS", flush=True)
        return 0
    print(f"RENDERED CLIENT: FAIL ({result})")
    source = game_log if game_log.is_file() else output_path
    print("\n".join(source.read_text(errors="replace").splitlines()[-80:]))
    return 1

if __name__ == "__main__":
    sys.exit(main())
