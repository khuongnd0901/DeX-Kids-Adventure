#!/usr/bin/env python3
"""Derived runtime contact sheet; never label this as a device screenshot."""
import argparse
import json
from PIL import Image,ImageDraw
from build_t048_atlas import OUT
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('output')
a=p.parse_args()
manifest=json.loads((OUT/'manifest.json').read_text())
with Image.open(OUT/'t048.png') as page:
    gallery=Image.new('RGB',(1024,940),(238,244,247))
    draw=ImageDraw.Draw(gallery)
    for item in manifest['regions']:
        x,y=item['xy'];w,h=item['size']
        sprite=page.crop((x,y,x+w,y+h))
        gallery.paste(sprite,(x,y),sprite)
        draw.text((x,y+h+3),item['name'],fill='black')
    gallery.save(a.output)
