#!/usr/bin/env python3
"""Generate Astra's IC2 Classic texture overrides.

The IC2 sheets retain their original atlas layout and machine markings.  Only the
surface palette and casing treatment change, which keeps custom renderers and
inventory block renders aligned with the installed IC2 Classic version.
"""

from __future__ import annotations

import colorsys
import io
import json
import sys
import zipfile
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[2]
IC2_JAR = ROOT / "mods" / "IC2Classic-1.12.2-1.5.11.jar"
RESOURCE_ROOT = ROOT / "resources"
CORE_ASSET_ROOT = (
    ROOT / "development" / "IndustrialCivilizationCore" / "src" / "main"
    / "resources" / "assets" / "industrialcivilizationcore"
)

IC2_MACHINE_SHEETS = (
    "batbox.png",
    "batterystation.png",
    "block_electric.png",
    "block_generator.png",
    "block_generator_compact.png",
    "block_machine_hv.png",
    "block_machine_lv.png",
    "block_machine_lv_2.png",
    "block_machine_mv.png",
    "block_pads.png",
    "block_personal.png",
    "block_personal_energy.png",
    "mfe.png",
    "mfsu.png",
    "pesu.png",
)

# Registry name, metadata count, source sheet, and the sheet row that most
# clearly preserves the machine's familiar front/top symbol. Personal energy
# storage uses a small companion sheet for metadata 8-10.
IC2_ICON_SPECS = (
    ("blockmachinelv", 16, "block_machine_lv.png", 3, 0),
    ("blockmachinelv2", 8, "block_machine_lv_2.png", 3, 0),
    ("blockmachinemv", 14, "block_machine_mv.png", 3, 0),
    ("blockmachinehv", 7, "block_machine_hv.png", 3, 0),
    ("blockgenerator", 15, "block_generator.png", 3, 0),
    ("blockcompactedgenerator", 9, "block_generator_compact.png", 3, 0),
    ("blockelectric", 11, "block_electric.png", 3, 0),
    ("blockpersonal", 8, "block_personal.png", 3, 0),
    ("blockpersonal", 3, "block_personal_energy.png", 3, 8),
    ("blockchargepad", 4, "block_pads.png", 0, 0),
)


def clamp(value: float) -> int:
    return max(0, min(255, round(value)))


def astra_pixel(pixel: tuple[int, int, int, int]) -> tuple[int, int, int, int]:
    """Move legacy IC2 colors into Astra's steel/orange/cyan visual language."""
    red, green, blue, alpha = pixel
    if alpha == 0:
        return pixel

    hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)

    if saturation < 0.22:
        # Neutral pixels become cool steel. Preserve luminance differences so
        # vents, rollers, sockets, and face markings stay recognizable.
        if value < 0.14:
            level = value / 0.14
            return (clamp(8 + 15 * level), clamp(14 + 19 * level), clamp(16 + 21 * level), alpha)
        if value > 0.76:
            level = (value - 0.76) / 0.24
            return (clamp(72 + 108 * level), clamp(91 + 108 * level), clamp(96 + 110 * level), alpha)
        level = (value - 0.14) / 0.62
        return (clamp(22 + 62 * level), clamp(34 + 69 * level), clamp(38 + 72 * level), alpha)

    # Warm activity pixels use safety orange; power/status pixels use cyan.
    if hue < 0.18 or hue >= 0.92:
        brightness = 0.55 + value * 0.45
        return (clamp(239 * brightness), clamp(92 * brightness), clamp(22 * brightness), alpha)
    if 0.18 <= hue < 0.72:
        brightness = 0.52 + value * 0.48
        return (clamp(47 * brightness), clamp(218 * brightness), clamp(217 * brightness), alpha)

    # Retain uncommon violet accents, but cool them to match the housing.
    return (clamp(red * 0.72), clamp(green * 0.82), clamp(blue * 0.98), alpha)


def add_casing_definition(image: Image.Image) -> None:
    """Give full 16x16 machine faces subtle rails and corner fasteners."""
    pixels = image.load()
    for tile_y in range(0, image.height, 16):
        for tile_x in range(0, image.width, 16):
            width = min(16, image.width - tile_x)
            height = min(16, image.height - tile_y)
            if width != 16 or height != 16:
                continue

            opaque = 0
            neutral = 0
            for y in range(16):
                for x in range(16):
                    red, green, blue, alpha = pixels[tile_x + x, tile_y + y]
                    if alpha > 220:
                        opaque += 1
                        if max(red, green, blue) - min(red, green, blue) < 34:
                            neutral += 1
            if opaque < 220 or neutral < 100:
                continue

            # Only replace already-neutral edge pixels. Colored ports and
            # machine-specific markings at the edge are intentionally retained.
            for y in range(16):
                for x in range(16):
                    if x not in (0, 1, 14, 15) and y not in (0, 1, 14, 15):
                        continue
                    current = pixels[tile_x + x, tile_y + y]
                    if current[3] < 220 or max(current[:3]) - min(current[:3]) >= 42:
                        continue
                    if x in (0, 15) or y in (0, 15):
                        color = (18, 29, 32, current[3])
                    elif x == 1 or y == 1:
                        color = (151, 174, 178, current[3])
                    else:
                        color = (58, 77, 81, current[3])
                    pixels[tile_x + x, tile_y + y] = color

            for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
                current = pixels[tile_x + x, tile_y + y]
                if current[3] > 220 and max(current[:3]) - min(current[:3]) < 42:
                    pixels[tile_x + x, tile_y + y] = (13, 23, 26, current[3])


def generate_ic2_item_icons(sheets: dict[str, Image.Image]) -> list[Path]:
    """Generate readable flat HEI/inventory art from each familiar IC2 face."""
    texture_dir = CORE_ASSET_ROOT / "textures" / "items" / "ic2_machines"
    model_dir = CORE_ASSET_ROOT / "models" / "item" / "ic2_machines"
    texture_dir.mkdir(parents=True, exist_ok=True)
    model_dir.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []

    for registry_name, count, sheet_name, row, metadata_offset in IC2_ICON_SPECS:
        sheet = sheets[sheet_name]
        for local_meta in range(count):
            metadata = local_meta + metadata_offset
            face = sheet.crop((local_meta * 16, row * 16,
                               local_meta * 16 + 16, row * 16 + 16))
            icon = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            icon.alpha_composite(face.resize((48, 48), Image.Resampling.NEAREST), (8, 8))
            stem = f"{registry_name}_{metadata}"
            texture_path = texture_dir / f"{stem}.png"
            icon.save(texture_path, format="PNG", optimize=True)
            written.append(texture_path)

            model_path = model_dir / f"{stem}.json"
            model_path.write_text(json.dumps({
                "parent": "item/generated",
                "textures": {
                    "layer0": f"industrialcivilizationcore:items/ic2_machines/{stem}"
                },
            }, indent=2) + "\n")
            written.append(model_path)

    return written


def generate_ic2_overrides() -> list[Path]:
    output_dir = RESOURCE_ROOT / "ic2" / "textures" / "sprites"
    output_dir.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []
    sheets: dict[str, Image.Image] = {}

    if not IC2_JAR.exists():
        raise FileNotFoundError(f"Missing source mod: {IC2_JAR}")

    with zipfile.ZipFile(IC2_JAR) as archive:
        for filename in IC2_MACHINE_SHEETS:
            member = f"assets/ic2/textures/sprites/{filename}"
            with archive.open(member) as source:
                image = Image.open(io.BytesIO(source.read())).convert("RGBA")
            source_pixels = (image.get_flattened_data()
                             if hasattr(image, "get_flattened_data") else image.getdata())
            image.putdata([astra_pixel(pixel) for pixel in source_pixels])
            add_casing_definition(image)
            sheets[filename] = image
            destination = output_dir / filename
            image.save(destination, format="PNG", optimize=True)
            written.append(destination)

    return written + generate_ic2_item_icons(sheets)


def main() -> int:
    written = generate_ic2_overrides()
    for path in written:
        print(path.relative_to(ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
