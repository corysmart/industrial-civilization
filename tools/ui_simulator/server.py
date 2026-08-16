#!/usr/bin/env python3
"""Zero-dependency hot-reload server for the Industrial Civilization UI simulator."""

from __future__ import annotations

import argparse
import json
import mimetypes
import re
import time
import zipfile
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from pathlib import Path
from urllib.parse import unquote, urlparse

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
JAVA = ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core"
RESOURCES = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore"
PACK_RESOURCES = ROOT / "resources/industrialcivilizationcore"
QUESTS = ROOT / "config/betterquesting/DefaultQuests.json"
MINECRAFT_JAR = Path.home() / "Library/Application Support/technic/cache/minecraft_1.12.2.jar"


def parse_lang() -> dict[str, str]:
    result: dict[str, str] = {}
    for raw in (RESOURCES / "lang/en_us.lang").read_text(encoding="utf-8").splitlines():
        if raw and not raw.startswith("#") and "=" in raw:
            key, value = raw.split("=", 1)
            result[key] = value
    result["gui.done"] = "Done"
    return result


def parse_machines() -> list[dict]:
    text = (JAVA / "IndustrialMachineKind.java").read_text(encoding="utf-8")
    pattern = re.compile(
        r'(\w+)\("([^"]+)",\s*(\d+),\s*(\d+),\s*(\d+),\s*'
        r'(\d+),\s*WorkClass\.(\w+)\)')
    return [
        {"enum": enum, "id": machine_id, "capacity": int(capacity),
         "voltage": int(voltage), "duration": int(duration),
         "minimumTicks": int(minimum_ticks), "workClass": work_class}
        for enum, machine_id, capacity, voltage, duration, minimum_ticks, work_class
        in pattern.findall(text)
    ]


def parse_factions() -> list[dict]:
    text = (JAVA / "FactionSystem.java").read_text(encoding="utf-8")
    pattern = re.compile(
        r'new Definition\("([^"]+)",\s*"([^"]+)",\s*(-?\d+),\s*'
        r'"([^"]+)",\s*"([^"]+)",\s*"([^"]+)"\)', re.S)
    return [
        {"id": faction_id, "name": name, "reputation": int(reputation),
         "settlements": settlements, "products": products, "membership": membership}
        for faction_id, name, reputation, settlements, products, membership
        in pattern.findall(text)
    ]


def parse_quests() -> list[dict]:
    data = json.loads(QUESTS.read_text(encoding="utf-8"))
    quest_db = data["questDatabase:9"]
    result = []
    for raw_line in data["questLines:9"].values():
        props = raw_line["properties:10"]["betterquesting:10"]
        nodes = []
        for raw_node in raw_line.get("quests:9", {}).values():
            quest = quest_db.get(f'{raw_node["id:3"]}:10', {})
            quest_props = quest.get("properties:10", {}).get("betterquesting:10", {})
            nodes.append({
                "id": raw_node["id:3"], "x": raw_node["x:3"], "y": raw_node["y:3"],
                "w": raw_node["sizeX:3"], "h": raw_node["sizeY:3"],
                "name": quest_props.get("name:8", f'Quest {raw_node["id:3"]}'),
                "prerequisites": quest.get("preRequisites:11", []),
            })
        result.append({
            "id": raw_line["lineID:3"], "order": raw_line.get("order:3", 0),
            "name": props.get("name:8", "Quest Line"),
            "description": props.get("desc:8", ""),
            "background": props.get("bg_image:8", "").split("/")[-1],
            "backgroundSize": props.get("bg_size:3", 512), "nodes": nodes,
        })
    return sorted(result, key=lambda line: line["order"])


def parse_quest_home() -> dict:
    data = json.loads(QUESTS.read_text(encoding="utf-8"))
    settings = data["questSettings:10"]["betterquesting:10"]
    return {
        "anchorX": settings.get("home_anchor_x:5", 0.5),
        "anchorY": settings.get("home_anchor_y:5", 0.0),
        "offsetX": settings.get("home_offset_x:3", -128),
        "offsetY": settings.get("home_offset_y:3", 0),
    }


def parse_main_menu() -> dict:
    return json.loads((ROOT / "config/CustomMainMenu/mainmenu.json").read_text(encoding="utf-8"))


def source_stamp() -> int:
    watched = [HERE / "index.html", HERE / "styles.css", HERE / "app.js", QUESTS,
               ROOT / "config/CustomMainMenu/mainmenu.json",
               JAVA / "GuiIndustrialMachine.java", JAVA / "GuiFactionDirectory.java",
               JAVA / "GuiTerrainWarmup.java", JAVA / "GuiIndustrialCredits.java",
               JAVA / "IndustrialMachineKind.java", JAVA / "FactionSystem.java",
               RESOURCES / "lang/en_us.lang", RESOURCES / "textures/gui/industrial_machine.png",
               PACK_RESOURCES / "textures/mainmenu/industrial_civilization_background.png",
               PACK_RESOURCES / "textures/gui/quest_home_v2.png"]
    return max(int(path.stat().st_mtime_ns) for path in watched if path.exists())


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt: str, *args) -> None:
        if self.server.verbose:  # type: ignore[attr-defined]
            super().log_message(fmt, *args)

    def send_bytes(self, data: bytes, content_type: str, status: int = 200) -> None:
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_json(self, value) -> None:
        self.send_bytes(json.dumps(value, separators=(",", ":")).encode(), "application/json")

    def do_GET(self) -> None:
        path = unquote(urlparse(self.path).path)
        if path == "/api/data":
            self.send_json({"lang": parse_lang(), "machines": parse_machines(),
                            "factions": parse_factions(), "questLines": parse_quests(),
                            "questHome": parse_quest_home(),
                            "mainMenu": parse_main_menu()})
            return
        if path == "/api/stamp":
            self.send_json({"stamp": source_stamp()})
            return
        if path == "/assets/minecraft/ascii.png":
            if not MINECRAFT_JAR.exists():
                self.send_bytes(b"Minecraft 1.12.2 jar not found", "text/plain", 404)
                return
            with zipfile.ZipFile(MINECRAFT_JAR) as jar:
                self.send_bytes(jar.read("assets/minecraft/textures/font/ascii.png"), "image/png")
            return
        asset_map = {
            "/assets/industrial_machine.png": RESOURCES / "textures/gui/industrial_machine.png",
            "/assets/mainmenu.png": PACK_RESOURCES / "textures/mainmenu/industrial_civilization_background.png",
            "/assets/mainmenu-button.png": PACK_RESOURCES / "textures/mainmenu/btn.png",
            "/assets/quest-home.png": PACK_RESOURCES / "textures/gui/quest_home_v2.png",
        }
        if path.startswith("/assets/quest/"):
            name = Path(path).name
            candidate = PACK_RESOURCES / "textures/gui" / name
            if candidate.parent == PACK_RESOURCES / "textures/gui" and candidate.exists():
                asset_map[path] = candidate
        if path in asset_map and asset_map[path].exists():
            file_path = asset_map[path]
            self.send_bytes(file_path.read_bytes(), mimetypes.guess_type(file_path.name)[0] or "application/octet-stream")
            return
        static_name = "index.html" if path in ("/", "/index.html") else path.lstrip("/")
        candidate = HERE / static_name
        if candidate.parent == HERE and candidate.exists() and candidate.is_file():
            self.send_bytes(candidate.read_bytes(), mimetypes.guess_type(candidate.name)[0] or "text/plain")
            return
        self.send_bytes(b"Not found", "text/plain", 404)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", default=43127, type=int)
    parser.add_argument("--verbose", action="store_true")
    args = parser.parse_args()
    server = ThreadingHTTPServer((args.host, args.port), Handler)
    server.verbose = args.verbose  # type: ignore[attr-defined]
    print(f"Industrial UI Simulator: http://{args.host}:{args.port}", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
