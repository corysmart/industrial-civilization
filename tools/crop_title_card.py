#!/usr/bin/env python3
"""Remove the opaque exterior matte from the shared title-card artwork."""

from pathlib import Path

from PIL import Image, ImageChops


ROOT = Path(__file__).resolve().parents[1]
PACK_LOGO = ROOT / "resources/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo.png"
MOD_LOGO = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo.png"
QUEST_HOME = ROOT / "resources/industrialcivilizationcore/textures/gui/quest_home_v2.png"
PLAQUE_MASK = ROOT / "resources/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo_mask.png"


def plaque_cutout(source: Image.Image) -> Image.Image:
    image = source.convert("RGBA")
    if image.size != (512, 256):
        raise ValueError(f"expected 512x256 title card, got {image.size}")
    mask = Image.open(PLAQUE_MASK).convert("L")
    if mask.size != image.size:
        raise ValueError(f"expected 512x256 title-card mask, got {mask.size}")
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
