#!/usr/bin/env python3
"""Build large, labeled review sheets from the worldgen showcase screenshots."""

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


SITES = (
    ("01-primitive_settlement", "PRIMITIVE SETTLEMENT",
     "31x31 SETTLEMENT  |  6 varied homes  |  communal hall  |  2 farms and village green"),
    ("02-militia_outpost", "MILITIA OUTPOST",
     "31x31 COMPOUND  |  4 defensive towers  |  barracks, command post and armory"),
    ("03-industrial_city", "INDUSTRIAL CITY",
     "79x79 CITY  |  33 seed-varied buildings  |  civic, residential and industrial districts"),
    ("04-abandoned_factory", "ABANDONED FACTORY",
     "31x31 DERELICT COMPLEX  |  3 ruined buildings  |  loading yard, gantry and chimney"),
    ("05-factory_steel", "STEEL FACTORY",
     "31x31 STEELWORKS  |  high bay, roof lights and twin chimneys  |  live utilities"),
    ("06-factory_electronics", "ELECTRONICS FACTORY",
     "31x31 ELECTRONICS WORKS  |  clean hall, annex and antenna  |  live utilities"),
    ("07-factory_fuel", "FUEL FACTORY",
     "31x31 FUEL WORKS  |  brick process hall, tank farm and chimney  |  live utilities"),
    ("08-factory_armaments", "ARMAMENTS FACTORY",
     "31x31 ARMAMENTS WORKS  |  fortified hall, guard towers and perimeter security"),
    ("09-factory_research", "RESEARCH FACTORY",
     "31x31 RESEARCH CAMPUS  |  glazed laboratory, annex and antenna  |  live utilities"),
    ("10-regional_road", "REGIONAL ROAD",
     "INFRASTRUCTURE SEGMENT  |  3 lanes x 16 blocks  |  dirt frontier, stone trunk routes"),
    ("11-apollo_11_memorial", "APOLLO 11 MEMORIAL",
     "MEMORIAL SITE  |  flag  |  heritage plaque  |  stone landing-site pad"),
    ("12-industrial_city_variant_b", "INDUSTRIAL CITY — SEED VARIANT B",
     "SAME 79x79 GENERATOR  |  distinct deterministic heights, materials, facades and rooftops"),
)

ANGLE_LABELS = ("ANGLE 1  SOUTHWEST", "ANGLE 2  SOUTHEAST", "ANGLE 3  OVERHEAD FOOTPRINT")
NATURAL_ANGLE_LABELS = ("ANGLE 1  NORTH", "ANGLE 2  EAST", "ANGLE 3  OVERHEAD FOOTPRINT")
FONT_PATH = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
BACKGROUND = (16, 24, 32)


def centered_x(draw, text, font, width):
    box = draw.textbbox((0, 0), text, font=font)
    return (width - (box[2] - box[0])) // 2


def label_view(image, text, font):
    draw = ImageDraw.Draw(image, "RGBA")
    box = draw.textbbox((0, 0), text, font=font)
    width = box[2] - box[0]
    height = box[3] - box[1]
    draw.rounded_rectangle((20, 18, 52 + width, 48 + height), radius=8, fill=(0, 0, 0, 190))
    draw.text((36, 28), text, font=font, fill="white")


def build_sheet(directory, stem, title, caption, angle_labels=ANGLE_LABELS):
    views = []
    angle_font = ImageFont.truetype(FONT_PATH, 30)
    for angle, label in enumerate(angle_labels, start=1):
        source = directory / f"{stem}-angle-{angle}.png"
        image = Image.open(source).convert("RGB")
        if image.size != (1280, 720):
            raise ValueError(f"{source} is {image.size}, expected 1280x720")
        label_view(image, label, angle_font)
        views.append(image)

    sheet = Image.new("RGB", (2560, 1660), BACKGROUND)
    sheet.paste(views[0], (0, 120))
    sheet.paste(views[1], (1280, 120))
    sheet.paste(views[2], (640, 840))
    draw = ImageDraw.Draw(sheet)
    title_font = ImageFont.truetype(FONT_PATH, 58)
    caption_font = ImageFont.truetype(FONT_PATH, 30)
    draw.text((centered_x(draw, title, title_font, sheet.width), 26), title,
              font=title_font, fill="white")
    draw.text((centered_x(draw, caption, caption_font, sheet.width), 1590), caption,
              font=caption_font, fill=(220, 230, 242))
    sheet.save(directory / f"{stem}-contact-sheet.png", optimize=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--natural", action="store_true",
                        help="build sheets for the ten Overworld natural-generation targets")
    args = parser.parse_args()
    sites = SITES[:10] if args.natural else SITES
    angle_labels = NATURAL_ANGLE_LABELS if args.natural else ANGLE_LABELS
    for site in sites:
        build_sheet(args.directory, *site, angle_labels=angle_labels)
    print(f"SHOWCASE SHEETS: PASS ({len(sites)} labeled sheets)")


if __name__ == "__main__":
    main()
