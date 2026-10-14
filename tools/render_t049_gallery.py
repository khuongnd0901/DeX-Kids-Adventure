#!/usr/bin/env python3
"""Offline two-column original vs Storybook V2 art contact sheet."""
from io import BytesIO
from pathlib import Path
import argparse
import cairosvg
from PIL import Image, ImageDraw, ImageFont
from storybook_art import SOURCE_ROOT, select_source, stylize

COMPANIONS = ("rabbit","fox","panda","penguin","cat")
VEHICLES = ("ambulance_cartoon","cable_car","camper_van",
 "double_decker_tour_bus","electric_city_bus","family_van","farm_tractor",
 "fire_engine","ice_cream_van","mini_train","recycling_truck","sailboat_family")
TRAFFIC = ("traffic_car","traffic_taxi","traffic_truck",
 "traffic_minibus","traffic_scooter","traffic_bicycle")

def render(path: Path, width: int, height: int, styled: bool) -> Image.Image:
    data=cairosvg.svg2png(url=str(path),output_width=width,output_height=height)
    image=Image.open(BytesIO(data)).convert("RGBA")
    return stylize(image) if styled else image

def main() -> None:
    parser=argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    args=parser.parse_args()
    pairs=[(f"companion/{name}", SOURCE_ROOT/"characters/companions"/(name+".svg"),220,250)
          for name in COMPANIONS]
    pairs.extend((f"vehicle/{name}",SOURCE_ROOT/"m10/vehicles"/(name+".svg"),280,160)
                 for name in VEHICLES)
    pairs.extend((f"road/{name}", SOURCE_ROOT/"vehicles"/(name+".svg"),360,185)
                 for name in TRAFFIC)
    line_height=310
    canvas=Image.new("RGB",(850,55+len(pairs)*line_height),(250,248,243))
    draw=ImageDraw.Draw(canvas)
    draw.text((40,15),"T-049  ORIGINAL",fill=(68,54,52))
    draw.text((468,15),"STORYBOOK V2  (review)",fill=(68,54,52))
    for index,(name,source,width,height) in enumerate(pairs):
        top=55+index*line_height
        draw.rounded_rectangle((10,top,840,top+299),radius=12,fill=(255,255,255))
        draw.text((25,top+10),name,fill=(87,68,65))
        for left,path,mode in ((30,source,False),(455,select_source(source),True)):
            if not path.is_file():
                raise FileNotFoundError(path)
            image=render(path,width,height,mode)
            canvas.paste(image,(left+(335-width)//2,top+34),image)
        draw.line((420,top+22,420,top+285),fill=(233,227,217),width=2)
    args.output.parent.mkdir(parents=True,exist_ok=True)
    canvas.save(args.output,optimize=True)
    print(f"Created {args.output} with {len(pairs)} before/after rows")

if __name__=="__main__":
    main()
