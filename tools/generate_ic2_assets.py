#!/usr/bin/env python3
"""Generate original IC2-adjacent pixel assets and offline review sheets."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore"
BLOCKS = ASSETS / "textures/blocks"
ITEMS = ASSETS / "textures/items"
GUI = ASSETS / "textures/gui"
DOCS = ROOT / "docs/art"

PALETTE = {
    "dark": "#17242a", "panel": "#253941", "shadow": "#52636a",
    "metal": "#aebdc1", "light": "#e5f0ef", "edge": "#71858b",
    "cyan": "#36dbe8", "cyan2": "#9af8f4", "orange": "#e78232",
    "copper": "#a95228", "red": "#bb3c2f", "paper": "#d9d2b4",
}

RUNTIME_CONTENT = json.loads((ROOT / "progression/runtime-content.json").read_text())
BLOCK_IDS = [entry["id"] for entry in RUNTIME_CONTENT["blocks"]]
ITEM_IDS = [entry["id"] for entry in RUNTIME_CONTENT["items"]]


def pixel_canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def casing(draw, accent, pattern):
    draw.rectangle((0, 0, 15, 15), fill=PALETTE["edge"])
    draw.rectangle((1, 1, 14, 14), fill=PALETTE["metal"])
    draw.line((2, 2, 13, 2), fill=PALETTE["light"])
    draw.line((2, 13, 13, 13), fill=PALETTE["shadow"])
    for p in ((1, 1), (14, 1), (1, 14), (14, 14)):
        draw.point(p, fill=PALETTE["dark"])
    draw.rectangle((3, 4, 12, 11), fill=PALETTE["dark"])
    draw.rectangle((4, 5, 11, 10), fill=PALETTE["panel"])
    pattern(draw, accent)
    draw.rectangle((13, 5, 13, 9), fill=accent)
    draw.point((13, 4), fill=PALETTE["cyan2"])


def block_pattern(block_id):
    def analyzer(d, a):
        d.line((5, 9, 7, 6, 10, 8), fill=a, width=1); d.point((8, 7), fill=PALETTE["cyan2"])
    def station(d, a):
        d.line((5, 9, 5, 6, 10, 6, 10, 9), fill=a); d.line((6, 8, 9, 8), fill=PALETTE["cyan2"])
    def experiment(d, a):
        d.ellipse((5, 5, 10, 10), outline=a); d.point((7, 7), fill=PALETTE["cyan2"]); d.point((9, 8), fill=PALETTE["cyan2"])
    def fabricator(d, a):
        d.line((5, 9, 7, 7, 7, 5), fill=a); d.line((8, 5, 8, 9, 10, 9), fill=PALETTE["orange"])
    def assembler(d, a):
        for x in (5, 8):
            for y in (6, 9): d.rectangle((x, y, x+1, y+1), fill=a)
    def robot(d, a):
        d.line((5, 9, 7, 7, 9, 9, 10, 6), fill=PALETTE["orange"]); d.point((7, 7), fill=a)
    def terminal(d, a):
        d.line((5, 9, 6, 7, 8, 8, 10, 6), fill=a); d.point((10, 9), fill=PALETTE["orange"])
    def replicator(d, a):
        d.ellipse((5, 5, 10, 10), outline=a); d.ellipse((7, 7, 8, 8), fill=PALETTE["cyan2"])
    def fusion(d, a):
        d.line((5, 8, 10, 8), fill=a); d.line((7, 5, 8, 10), fill=PALETTE["orange"]); d.point((8, 8), fill=PALETTE["light"])
    def cargo(d, a):
        d.rectangle((5, 6, 10, 9), outline=a); d.point((6, 10), fill=PALETTE["orange"]); d.point((9, 10), fill=PALETTE["orange"])
    def megastructure(d, a):
        d.ellipse((5, 5, 10, 10), outline=a); d.line((4, 8, 11, 8), fill=PALETTE["orange"])
    def colony(d, a):
        d.line((8, 5, 8, 10), fill=a); d.line((5, 8, 11, 8), fill=a); d.point((8, 5), fill=PALETTE["orange"])
    def solar(d, a):
        d.rectangle((5, 5, 10, 9), outline=a); d.line((6, 6, 9, 9), fill=PALETTE["cyan2"]); d.line((9, 6, 6, 9), fill=PALETTE["cyan2"])
    return {
        "molecular_analyzer": analyzer, "research_station": station,
        "orbital_experiment_module": experiment, "electric_fabricator": fabricator,
        "programmable_assembler": assembler, "robotic_manufacturing_cell": robot,
        "factory_control_terminal": terminal,
        "matter_replicator": replicator, "fusion_research_core": fusion,
        "interplanetary_cargo_controller": cargo,
        "orbital_megastructure_controller": megastructure,
        "autonomous_colony_beacon": colony,
        "environmental_solar_array": solar, "tracking_solar_array": solar,
    }[block_id]


def make_blocks():
    accents = ["cyan", "cyan", "cyan2", "orange", "cyan", "orange", "copper",
               "cyan2", "orange", "cyan", "orange", "cyan2"]
    accents.append("cyan")
    accents.append("orange")
    for block_id, accent in zip(BLOCK_IDS, accents):
        image = pixel_canvas(); draw = ImageDraw.Draw(image)
        casing(draw, PALETTE[accent], block_pattern(block_id))
        image.save(BLOCKS / f"{block_id}.png")


def item_base(draw, color, page=False):
    if page:
        draw.polygon([(3, 2), (11, 2), (13, 4), (13, 13), (3, 13)], fill=PALETTE["dark"])
        draw.polygon([(4, 2), (10, 2), (12, 4), (12, 12), (4, 12)], fill=color)
        draw.line((10, 2, 10, 4, 12, 4), fill=PALETTE["light"])
    else:
        draw.rectangle((3, 3, 12, 12), fill=PALETTE["dark"])
        draw.rectangle((4, 4, 11, 11), fill=color)
        draw.line((5, 4, 10, 4), fill=PALETTE["light"])


def make_item(item_id, index):
    image = pixel_canvas(); d = ImageDraw.Draw(image)
    archive = "archive" in item_id or "authorization" in item_id or item_id in {
        "material_pattern_record", "underworld_dossier", "criminal_network_ledger",
        "factory_restoration_certificate"}
    colors = [PALETTE["cyan"], PALETTE["paper"], PALETTE["cyan2"], PALETTE["orange"],
              PALETTE["red"], PALETTE["copper"]]
    item_base(d, colors[index % len(colors)], archive)
    if "core" in item_id:
        d.ellipse((5, 5, 10, 10), fill=PALETTE["metal"], outline=PALETTE["light"])
        d.rectangle((7, 7, 8, 8), fill=PALETTE["cyan"])
        for p in ((2, 7), (13, 7), (7, 2), (7, 13)): d.rectangle((p[0], p[1], p[0]+1, p[1]+1), fill=PALETTE["copper"])
    elif "quantum" in item_id or "antimatter" in item_id or "uu_matter" in item_id:
        d.ellipse((5, 5, 10, 10), outline=PALETTE["light"]); d.rectangle((7, 6, 8, 9), fill=PALETTE["cyan2"])
    elif "processor" in item_id or "system" in item_id:
        d.rectangle((6, 6, 9, 9), fill=PALETTE["panel"])
        for x in range(5, 11, 2): d.point((x, 5), fill=PALETTE["cyan"]); d.point((x, 10), fill=PALETTE["copper"])
    elif "frame" in item_id:
        d.rectangle((5, 5, 10, 10), outline=PALETTE["light"]); d.rectangle((7, 7, 8, 8), fill=PALETTE["dark"])
    elif "data" in item_id or "cartridge" in item_id:
        d.rectangle((5, 6, 10, 10), fill=PALETTE["panel"]); d.line((6, 7, 9, 7), fill=PALETTE["cyan"])
    else:
        d.line((5, 7, 10, 7), fill=PALETTE["dark"]); d.line((5, 9, 9, 9), fill=PALETTE["copper"])
    # Tiny per-artifact registry mark keeps silhouettes distinct at native scale.
    d.point((1 + index % 15, 14), fill=PALETTE["cyan"] if (index // 15) % 2 == 0 else PALETTE["orange"])
    return image


def make_items():
    for index, item_id in enumerate(ITEM_IDS):
        make_item(item_id, index).save(ITEMS / f"{item_id}.png")


def slot(draw, x, y):
    draw.rectangle((x, y, x + 17, y + 17), fill="#657278")
    draw.rectangle((x + 1, y + 1, x + 16, y + 16), fill="#2d373b")
    draw.rectangle((x + 2, y + 2, x + 15, y + 15), fill="#a9b5b8")


def make_gui():
    image = Image.new("RGBA", (256, 256), (0, 0, 0, 0)); d = ImageDraw.Draw(image)
    d.rectangle((0, 0, 175, 165), fill="#aeb8ba", outline="#1c2529")
    d.rectangle((3, 3, 172, 162), outline="#e8eeee")
    d.rectangle((8, 17, 167, 62), fill="#7b898e", outline="#3a484d")
    for x in (44, 66, 88, 130): slot(d, x - 1, 34)
    d.line((105, 42, 126, 42), fill=PALETTE["dark"], width=2)
    d.polygon([(126, 39), (132, 42), (126, 45)], fill=PALETTE["orange"])
    d.rectangle((15, 10, 26, 60), fill=PALETTE["dark"])
    d.rectangle((17, 12, 24, 58), fill="#52636a")
    for row in range(3):
        for col in range(9): slot(d, 7 + col * 18, 83 + row * 18)
    for col in range(9): slot(d, 7 + col * 18, 141)
    d.rectangle((176, 0, 183, 47), fill=PALETTE["cyan"])
    for y in range(0, 48, 4): d.line((176, y, 183, y), fill=PALETTE["cyan2"])
    d.rectangle((176, 49, 199, 64), fill=PALETTE["orange"])
    d.line((176, 49, 199, 49), fill="#ffc06b")
    image.save(GUI / "industrial_machine.png")


def write_models():
    (ASSETS / "models/block").mkdir(parents=True, exist_ok=True)
    (ASSETS / "models/item").mkdir(parents=True, exist_ok=True)
    (ASSETS / "blockstates").mkdir(parents=True, exist_ok=True)
    for block_id in BLOCK_IDS:
        (ASSETS / "models/block" / f"{block_id}.json").write_text(json.dumps({
            "parent": "block/cube_all",
            "textures": {"all": f"industrialcivilizationcore:blocks/{block_id}"}
        }, indent=2) + "\n")
        (ASSETS / "models/item" / f"{block_id}.json").write_text(json.dumps({
            "parent": f"industrialcivilizationcore:block/{block_id}"
        }, indent=2) + "\n")
        (ASSETS / "blockstates" / f"{block_id}.json").write_text(json.dumps({
            "variants": {"normal": {"model": f"industrialcivilizationcore:{block_id}"}}
        }, indent=2) + "\n")
    for item_id in ITEM_IDS:
        (ASSETS / "models/item" / f"{item_id}.json").write_text(json.dumps({
            "parent": "item/generated",
            "textures": {"layer0": f"industrialcivilizationcore:items/{item_id}"}
        }, indent=2) + "\n")


def contact_sheet():
    entries = [("block", x, Image.open(BLOCKS / f"{x}.png")) for x in BLOCK_IDS]
    entries += [("item", x, Image.open(ITEMS / f"{x}.png")) for x in ITEM_IDS]
    width, cell_h = 960, 164
    sheet = Image.new("RGB", (width, ((len(entries) + 5) // 6) * cell_h), "#182329")
    d = ImageDraw.Draw(sheet); font = ImageFont.load_default()
    for i, (kind, name, icon) in enumerate(entries):
        x, y = (i % 6) * 160, (i // 6) * cell_h
        sheet.paste(icon.resize((128, 128), Image.Resampling.NEAREST), (x + 16, y + 4), icon.resize((128, 128), Image.Resampling.NEAREST))
        label = name.replace("_", " ")
        d.text((x + 6, y + 136), label[:25], fill="#d9eeee", font=font)
        d.text((x + 6, y + 149), kind, fill="#36dbe8", font=font)
    sheet.save(DOCS / "industrial_content_sprite_review.png")


def main():
    for path in (BLOCKS, ITEMS, GUI, DOCS): path.mkdir(parents=True, exist_ok=True)
    make_blocks(); make_items(); make_gui(); write_models(); contact_sheet()
    print(f"Generated {len(BLOCK_IDS)} block textures, {len(ITEM_IDS)} item sprites, GUI, models, and review sheet")


if __name__ == "__main__":
    main()
