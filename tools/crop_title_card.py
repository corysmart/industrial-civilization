#!/usr/bin/env python3
"""Remove the opaque exterior matte from the shared title-card artwork."""

from pathlib import Path

from PIL import Image, ImageChops, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
PACK_LOGO = ROOT / "resources/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo.png"
MOD_LOGO = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo.png"
QUEST_HOME = ROOT / "resources/industrialcivilizationcore/textures/gui/quest_home_v2.png"

# Native-pixel outline of the plaque's outer metal silhouette. The mask keeps
# every original plaque pixel while discarding only the rectangular matte that
# surrounded it in the 512x256 source canvas.
PLAQUE_OUTLINE = [
    (29, 15), (483, 15), (502, 38), (502, 218),
    (483, 240), (29, 240), (10, 218), (10, 38),
]


def plaque_cutout(source: Image.Image) -> Image.Image:
    image = source.convert("RGBA")
    if image.size != (512, 256):
        raise ValueError(f"expected 512x256 title card, got {image.size}")
    mask = Image.new("L", image.size, 0)
    ImageDraw.Draw(mask).polygon(PLAQUE_OUTLINE, fill=255)
    alpha = ImageChops.multiply(image.getchannel("A"), mask)
    image.putalpha(alpha)
    return image


def main() -> None:
    cutout = plaque_cutout(Image.open(PACK_LOGO))
    cutout.save(PACK_LOGO)
    cutout.save(MOD_LOGO)

    home = Image.open(QUEST_HOME).convert("RGBA")
    if home.size != (512, 512):
        raise ValueError(f"expected 512x512 Better Questing atlas, got {home.size}")
    corrected = Image.new("RGBA", home.size, (0, 0, 0, 0))
    corrected.alpha_composite(home.crop((0, 0, 512, 256)), (0, 0))
    corrected.alpha_composite(cutout, (0, 256))
    corrected.save(QUEST_HOME)
    print("Cropped exterior matte from main-menu and Better Questing title cards")


if __name__ == "__main__":
    main()
