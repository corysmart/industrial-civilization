#!/usr/bin/env python3
"""Generate original IC2-adjacent pixel assets and offline review sheets."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore"
BLOCKS = ASSETS / "textures/blocks"
ITEMS = ASSETS / "textures/items"
NEI_BLOCKS = ITEMS / "nei_blocks"
GUI = ASSETS / "textures/gui"
DOCS = ROOT / "docs/art"
NEI_BLOCK_ATLAS = DOCS / "industrial_nei_blocks_alpha_v3.png"
NEI_ITEM_ATLAS = DOCS / "industrial_nei_items_alpha_v3.png"

PALETTE = {
    "dark": "#17242a", "panel": "#253941", "shadow": "#52636a",
    "metal": "#aebdc1", "light": "#e5f0ef", "edge": "#71858b",
    "cyan": "#36dbe8", "cyan2": "#9af8f4", "orange": "#e78232",
    "copper": "#a95228", "copper_dark": "#71351f", "red": "#bb3c2f",
    "paper": "#d9d2b4", "paper_dark": "#9a9276", "rust": "#805033",
}

RUNTIME_CONTENT = json.loads((ROOT / "progression/runtime-content.json").read_text())
BLOCK_IDS = [entry["id"] for entry in RUNTIME_CONTENT["blocks"]]
ITEM_IDS = [entry["id"] for entry in RUNTIME_CONTENT["items"]]


def pixel_canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def machine_shell(draw, weathered=False):
    """Layered IC2-adjacent casing with readable bevels at native scale."""
    draw.rectangle((0, 0, 15, 15), fill=PALETTE["dark"])
    draw.rectangle((1, 1, 14, 14), fill=PALETTE["edge"])
    draw.rectangle((2, 2, 13, 13), fill=PALETTE["metal"])
    draw.line((2, 2, 13, 2), fill=PALETTE["light"])
    draw.line((2, 3, 2, 12), fill="#cbd8da")
    draw.line((2, 13, 13, 13), fill=PALETTE["shadow"])
    draw.line((13, 3, 13, 12), fill="#45575e")
    for p in ((2, 2), (13, 2), (2, 13), (13, 13)):
        draw.point(p, fill=PALETTE["dark"])
    if weathered:
        for p in ((3, 2), (11, 3), (2, 8), (12, 11), (6, 13)):
            draw.point(p, fill=PALETTE["rust"])


def inset(draw, box=(3, 3, 12, 11)):
    x0, y0, x1, y1 = box
    draw.rectangle(box, fill="#0b1216")
    draw.line((x0, y0, x1, y0), fill=PALETTE["shadow"])
    draw.line((x0, y0, x0, y1), fill="#45575e")
    draw.rectangle((x0 + 1, y0 + 1, x1 - 1, y1 - 1), fill=PALETTE["panel"])


def footer(draw, accent):
    draw.rectangle((4, 12, 11, 12), fill="#35474e")
    for x in (5, 7, 9):
        draw.point((x, 12), fill=PALETTE["dark"])
    draw.rectangle((12, 5, 12, 9), fill=accent)
    draw.point((12, 5), fill=PALETTE["cyan2"])


def block_pattern(block_id):
    def analyzer(d, a):
        d.line((5, 9, 7, 6, 10, 8), fill=a); d.point((5, 9), fill=PALETTE["cyan2"]); d.point((10, 8), fill=PALETTE["cyan2"])
    def station(d, a):
        d.line((5, 9, 5, 6, 10, 6, 10, 9), fill=a); d.line((6, 8, 9, 8), fill=PALETTE["cyan2"])
    def experiment(d, a):
        d.ellipse((4, 4, 11, 11), fill="#091319", outline=PALETTE["edge"]); d.arc((5, 5, 10, 10), 120, 300, fill=PALETTE["light"]); d.point((8, 8), fill=a); d.point((10, 9), fill=PALETTE["cyan2"])
    def fabricator(d, a):
        d.line((5, 9, 7, 7, 7, 5), fill=a); d.line((8, 5, 8, 9, 10, 9), fill=PALETTE["orange"])
    def assembler(d, a):
        for x in (5, 8):
            for y in (6, 9): d.rectangle((x, y, x+1, y+1), fill=a)
    def robot(d, a):
        d.line((5, 10, 6, 8, 8, 8, 9, 6, 10, 5), fill=PALETTE["copper"], width=2); d.point((6, 8), fill=PALETTE["orange"]); d.point((9, 6), fill=a)
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
        for y in (5, 7, 9): d.line((5, y, 10, y), fill=a)
        d.line((5, 10, 10, 10), fill=PALETTE["cyan2"])
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


def make_block_face(block_id, accent):
    image = pixel_canvas(); d = ImageDraw.Draw(image)
    weathered = block_id == "factory_control_terminal"
    machine_shell(d, weathered)
    inset(d)
    block_pattern(block_id)(d, accent)
    footer(d, accent)
    if block_id == "factory_control_terminal":
        d.line((3, 4, 5, 4), fill=PALETTE["rust"]); d.point((11, 10), fill=PALETTE["rust"])
    elif block_id == "matter_replicator":
        d.ellipse((5, 4, 10, 10), outline=PALETTE["light"])
    elif block_id == "fusion_research_core":
        d.rectangle((4, 4, 11, 11), outline=PALETTE["copper_dark"])
    return image


def make_block_side(accent, port=False):
    image = pixel_canvas(); d = ImageDraw.Draw(image); machine_shell(d)
    d.rectangle((4, 3, 10, 12), fill=PALETTE["shadow"])
    d.rectangle((5, 4, 9, 11), fill=PALETTE["panel"])
    for y in (5, 7, 9): d.line((6, y, 8, y), fill=PALETTE["dark"])
    d.rectangle((11, 5, 12, 10), fill=PALETTE["dark"])
    d.line((12, 6, 12, 9), fill=accent)
    if port:
        d.rectangle((3, 6, 4, 9), fill=PALETTE["copper_dark"]); d.line((4, 7, 4, 8), fill=PALETTE["orange"])
    return image


def make_block_top(accent):
    image = pixel_canvas(); d = ImageDraw.Draw(image); machine_shell(d)
    d.rectangle((4, 4, 11, 11), fill=PALETTE["shadow"])
    d.rectangle((5, 5, 10, 10), fill=PALETTE["panel"])
    d.rectangle((6, 6, 9, 9), outline=accent)
    d.line((7, 5, 8, 5), fill=PALETTE["light"])
    d.point((8, 8), fill=PALETTE["cyan2"])
    return image


def make_blocks():
    accents = ["cyan", "cyan", "cyan2", "orange", "cyan", "orange", "copper",
               "cyan2", "orange", "cyan", "orange", "cyan2", "cyan", "orange"]
    for index, (block_id, accent_name) in enumerate(zip(BLOCK_IDS, accents)):
        accent = PALETTE[accent_name]
        make_block_face(block_id, accent).save(BLOCKS / f"{block_id}.png")
        make_block_side(accent, port=index % 3 == 0).save(BLOCKS / f"{block_id}_side.png")
        make_block_top(accent).save(BLOCKS / f"{block_id}_top.png")


def cartridge(d, color, blank=False):
    d.polygon([(4, 2), (10, 2), (12, 4), (12, 12), (10, 14), (4, 14), (2, 12), (2, 4)], fill=PALETTE["dark"])
    d.polygon([(4, 3), (10, 3), (11, 4), (11, 11), (9, 12), (4, 12), (3, 11), (3, 4)], fill=color)
    d.line((4, 3, 9, 3), fill=PALETTE["light"])
    d.rectangle((5, 6, 9, 9), fill=PALETTE["panel"])
    if not blank: d.line((6, 7, 8, 7), fill=PALETTE["cyan"]); d.point((7, 9), fill=PALETTE["orange"])
    for x in (5, 7, 9): d.point((x, 13), fill=PALETTE["orange"])


def archive(d, color, mark="lines"):
    d.polygon([(3, 2), (11, 2), (13, 4), (13, 13), (3, 13)], fill=PALETTE["dark"])
    d.polygon([(4, 2), (10, 2), (12, 4), (12, 12), (4, 12)], fill=color)
    d.line((10, 2, 10, 4, 12, 4), fill=PALETTE["light"])
    if mark == "gear":
        d.ellipse((6, 6, 9, 9), outline=PALETTE["panel"]); d.point((7, 7), fill=PALETTE["panel"])
    elif mark == "orbit":
        d.ellipse((5, 5, 10, 10), outline=PALETTE["copper"]); d.line((5, 9, 10, 6), fill=PALETTE["copper"])
    else:
        d.line((5, 7, 10, 7), fill=PALETTE["panel"]); d.line((5, 9, 9, 9), fill=PALETTE["panel"])


def core(d, large=False):
    box = (3, 3, 12, 12) if large else (4, 4, 11, 11)
    d.ellipse(box, fill=PALETTE["dark"], outline=PALETTE["light"])
    d.ellipse((6, 6, 9, 9), fill=PALETTE["cyan"], outline=PALETTE["cyan2"])
    for p in ((2, 7), (12, 7), (7, 2), (7, 12)):
        d.rectangle((p[0], p[1], p[0] + 1, p[1] + 1), fill=PALETTE["copper"])


def capsule(d, antimatter=False):
    fluid = PALETTE["cyan2"] if antimatter else PALETTE["cyan"]
    d.rectangle((5, 2, 10, 13), fill=PALETTE["dark"])
    d.rectangle((6, 3, 9, 12), fill=PALETTE["edge"])
    d.rectangle((6, 5, 9, 10), fill=fluid)
    d.line((7, 5, 7, 9), fill=PALETTE["light"])
    d.rectangle((4, 2, 11, 3), fill=PALETTE["copper"])
    d.rectangle((4, 12, 11, 13), fill=PALETTE["metal"])


def make_item(item_id, index):
    image = pixel_canvas(); d = ImageDraw.Draw(image)
    if item_id == "industrial_credit":
        d.ellipse((2, 2, 13, 13), fill=PALETTE["copper_dark"], outline=PALETTE["dark"])
        d.ellipse((3, 3, 12, 12), fill=PALETTE["copper"], outline=PALETTE["orange"])
        d.ellipse((5, 5, 10, 10), fill=PALETTE["metal"], outline=PALETTE["light"])
        d.line((7, 5, 7, 10), fill=PALETTE["panel"])
        d.line((8, 5, 8, 10), fill=PALETTE["cyan"])
        d.point((4, 7), fill=PALETTE["orange"]); d.point((11, 8), fill=PALETTE["orange"])
    elif item_id == "blank_data_cartridge": cartridge(d, PALETTE["paper"], True)
    elif item_id in {"material_pattern_record", "research_data"}: cartridge(d, PALETTE["cyan"] if index == 0 else PALETTE["cyan2"])
    elif "core" in item_id: core(d, item_id == "civilization_scale_ai_core")
    elif item_id == "precision_frame":
        d.rectangle((2, 2, 13, 13), fill=PALETTE["dark"]); d.rectangle((3, 3, 12, 12), fill=PALETTE["metal"])
        d.rectangle((5, 5, 10, 10), fill=PALETTE["dark"])
        for p in ((3, 3), (11, 3), (3, 11), (11, 11)): d.rectangle((p[0], p[1], p[0]+1, p[1]+1), fill=PALETTE["copper"])
    elif item_id in {"control_processor", "recovered_factory_control_system"}:
        d.rectangle((3, 3, 12, 12), fill=PALETTE["dark"]); d.rectangle((5, 5, 10, 10), fill=PALETTE["edge"])
        d.rectangle((6, 6, 9, 9), fill=PALETTE["panel"]); d.point((7, 7), fill=PALETTE["cyan"])
        for n in (4, 7, 10): d.point((n, 2), fill=PALETTE["copper"]); d.point((n, 13), fill=PALETTE["copper"])
        if item_id == "recovered_factory_control_system":
            d.point((5, 5), fill=PALETTE["rust"]); d.point((10, 9), fill=PALETTE["rust"])
    elif item_id == "uu_matter_capsule": capsule(d, False)
    elif item_id == "contained_antimatter_capsule": capsule(d, True)
    elif item_id == "interplanetary_cargo_network_key":
        d.ellipse((2, 2, 10, 10), fill=PALETTE["metal"], outline=PALETTE["dark"]); d.ellipse((5, 5, 8, 8), fill=PALETTE["dark"])
        d.line((8, 9, 13, 14), fill=PALETTE["edge"], width=2); d.point((12, 12), fill=PALETTE["copper"])
    elif item_id == "lunar_quantum_component":
        core(d); d.rectangle((6, 6, 9, 9), fill=PALETTE["cyan2"])
    else:
        colors = {
            "mars_mission_authorization": PALETTE["red"], "martian_autonomy_archive": PALETTE["rust"],
            "underworld_dossier": PALETTE["red"], "criminal_network_ledger": PALETTE["cyan"],
            "factory_restoration_certificate": PALETTE["paper"],
            "controlled_replication_record": PALETTE["red"],
            "megastructure_control_record": PALETTE["cyan2"],
            "autonomous_colony_charter": PALETTE["paper"],
        }
        color = colors.get(item_id, PALETTE["cyan2"] if index % 2 == 0 else PALETTE["paper"])
        mark = "gear" if any(x in item_id for x in ("engineering", "control", "colony")) else "orbit" if any(x in item_id for x in ("orbital", "mega", "replication")) else "lines"
        archive(d, color, mark)
    if item_id == "megastructure_control_record":
        d.rectangle((3, 11, 5, 12), fill=PALETTE["copper"])
    if item_id in {
        "orbital_research_archive", "lunar_engineering_archive", "mars_mission_authorization",
        "martian_autonomy_archive", "underworld_dossier", "criminal_network_ledger",
        "factory_restoration_certificate", "controlled_replication_record",
        "megastructure_control_record", "autonomous_colony_charter"
    }:
        # A keyed copper contact makes each physical archive cartridge identifiable.
        d.point((3 + index % 9, 13), fill=PALETTE["orange"])
    return image


def make_items():
    for index, item_id in enumerate(ITEM_IDS):
        make_item(item_id, index).save(ITEMS / f"{item_id}.png")


def atlas_cell(atlas, index, columns, rows):
    column, row = index % columns, index // columns
    x0 = round(column * atlas.width / columns); x1 = round((column + 1) * atlas.width / columns)
    y0 = round(row * atlas.height / rows); y1 = round((row + 1) * atlas.height / rows)
    return atlas.crop((x0, y0, x1, y1))


def inventory_sprite(cell, size=64, padding=2):
    # Remove rare saturated key pixels left where the generated atlas varied
    # slightly from its sampled border color. The concept palette contains no
    # magenta, so this is lossless for every intended sprite.
    cleaned = cell.copy()
    pixels = cleaned.load()
    for y in range(cleaned.height):
        for x in range(cleaned.width):
            red, green, blue, alpha = pixels[x, y]
            if alpha and red > 30 and blue > 30 and green < min(red, blue) * 0.8:
                pixels[x, y] = (red, green, blue, 0)
    cell = cleaned
    alpha = cell.getchannel("A")
    bbox = alpha.getbbox()
    if bbox is None:
        raise ValueError("NEI atlas cell contains no visible sprite")
    sprite = cell.crop(bbox)
    available = size - padding * 2
    scale = min(available / sprite.width, available / sprite.height)
    width = max(1, round(sprite.width * scale)); height = max(1, round(sprite.height * scale))
    sprite = sprite.resize((width, height), Image.Resampling.NEAREST)
    output = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    output.alpha_composite(sprite, ((size - width) // 2, (size - height) // 2))
    # Cell-boundary remnants can survive chroma despill as tiny disconnected
    # dark lines. Inventory concepts are single physical silhouettes, so keep
    # only the dominant connected alpha component.
    alpha = output.getchannel("A")
    remaining = {(x, y) for y in range(size) for x in range(size) if alpha.getpixel((x, y)) > 0}
    components = []
    while remaining:
        pending = [remaining.pop()]; component = []
        while pending:
            point = pending.pop(); component.append(point)
            x, y = point
            for neighbor in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if neighbor in remaining:
                    remaining.remove(neighbor); pending.append(neighbor)
        components.append(component)
    if components:
        keep = set(max(components, key=len))
        pixels = output.load()
        for y in range(size):
            for x in range(size):
                if (x, y) not in keep:
                    pixels[x, y] = (0, 0, 0, 0)
    return output


def make_nei_sprites():
    """Create detailed inventory-only sprites without changing placed block faces."""
    block_atlas = Image.open(NEI_BLOCK_ATLAS).convert("RGBA")
    item_atlas = Image.open(NEI_ITEM_ATLAS).convert("RGBA")
    NEI_BLOCKS.mkdir(parents=True, exist_ok=True)
    for index, block_id in enumerate(BLOCK_IDS):
        inventory_sprite(atlas_cell(block_atlas, index, 7, 2)).save(NEI_BLOCKS / f"{block_id}.png")
    # The generated source faithfully retained the final two machine concepts
    # in cells 0-1 before the twenty-two item concepts in cells 2-23.
    for index, item_id in enumerate(ITEM_IDS):
        if item_id == "industrial_credit":
            source = make_item(item_id, index).resize((48, 48), Image.Resampling.NEAREST)
            output = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            output.alpha_composite(source, (8, 8))
            output.save(ITEMS / f"{item_id}.png")
        else:
            inventory_sprite(atlas_cell(item_atlas, index + 2, 6, 4)).save(ITEMS / f"{item_id}.png")


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
            "parent": "block/cube",
            "textures": {
                "particle": f"industrialcivilizationcore:blocks/{block_id}",
                "north": f"industrialcivilizationcore:blocks/{block_id}",
                "south": f"industrialcivilizationcore:blocks/{block_id}_side",
                "east": f"industrialcivilizationcore:blocks/{block_id}_side",
                "west": f"industrialcivilizationcore:blocks/{block_id}_side",
                "up": f"industrialcivilizationcore:blocks/{block_id}_top",
                "down": f"industrialcivilizationcore:blocks/{block_id}_side"
            }
        }, indent=2) + "\n")
        (ASSETS / "models/item" / f"{block_id}.json").write_text(json.dumps({
            "parent": "item/generated",
            "textures": {"layer0": f"industrialcivilizationcore:items/nei_blocks/{block_id}"}
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
    entries = [("block", x, Image.open(NEI_BLOCKS / f"{x}.png")) for x in BLOCK_IDS]
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


def block_face_sheet():
    cell_w, cell_h, columns = 192, 132, 7
    sheet = Image.new("RGB", (cell_w * columns, cell_h * 2), "#182329")
    d = ImageDraw.Draw(sheet); font = ImageFont.load_default()
    for i, block_id in enumerate(BLOCK_IDS):
        x, y = (i % columns) * cell_w, (i // columns) * cell_h
        for face_index, suffix in enumerate(("", "_side", "_top")):
            icon = Image.open(BLOCKS / f"{block_id}{suffix}.png")
            enlarged = icon.resize((48, 48), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (x + 12 + face_index * 56, y + 8), enlarged)
        d.text((x + 8, y + 62), block_id.replace("_", " ")[:30], fill="#d9eeee", font=font)
        d.text((x + 8, y + 76), "front       side        top", fill="#36dbe8", font=font)
    sheet.save(DOCS / "industrial_content_block_faces_review.png")


def main():
    for path in (BLOCKS, ITEMS, NEI_BLOCKS, GUI, DOCS): path.mkdir(parents=True, exist_ok=True)
    make_blocks(); make_items(); make_nei_sprites(); make_gui(); write_models(); contact_sheet(); block_face_sheet()
    print(f"Generated {len(BLOCK_IDS) * 3} block-face textures, {len(BLOCK_IDS) + len(ITEM_IDS)} concept-faithful NEI sprites, GUI, models, and review sheets")


if __name__ == "__main__":
    main()
