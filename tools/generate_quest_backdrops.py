#!/usr/bin/env python3
"""Derive legible Better Questing backdrops from full-bright concept art."""
from pathlib import Path
from PIL import Image, ImageEnhance, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
GUI = ROOT / "resources/industrialcivilizationcore/textures/gui"

for source in sorted(GUI.glob("quest_bg_*.png")):
    if source.stem.endswith("_ui"):
        continue
    image = Image.open(source).convert("RGB")
    # Better Questing fits the entire line at once, shrinking 24 px nodes.
    # Keep the concept readable but quiet enough for locked-node silhouettes.
    image = ImageEnhance.Color(image).enhance(0.52)
    # Preserve the source art and layout. The first 50% lift was still too
    # subdued in-game, so raise it another 50% (0.51 * 1.5 = 0.765).
    image = ImageEnhance.Brightness(image).enhance(0.765)
    image = image.filter(ImageFilter.GaussianBlur(radius=0.45))
    image.save(source.with_name(source.stem + "_ui.png"), optimize=True)

print("Generated subdued UI variants for five quest-board backdrops")
