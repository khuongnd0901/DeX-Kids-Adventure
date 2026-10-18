#!/usr/bin/env python3
"""Density resources from a generated original mascot; fixed adaptive safe zone."""
from pathlib import Path
import argparse
import hashlib
import json
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'art/assets-source/app-icon-v1/capybara-icon.png'
BACKGROUND='#167E86'

def build(out):
    source=Image.open(SOURCE)
    assert source.mode=='RGBA' and source.getchannel('A').getextrema()==(0,255)
    box=source.getchannel('A').point(lambda a:255 if a>=32 else 0).getbbox()
    assert box
    art=source.crop(box)
    # This mascot's62dp envelope fits inside the central66dp safe circle.
    foreground=Image.new('RGBA',(432,432))
    fitted=ImageOps.contain(art,(248,248),Image.Resampling.LANCZOS)
    foreground.alpha_composite(fitted,((432-fitted.width)//2,(432-fitted.height)//2))
    alpha=foreground.getchannel('A')
    radius_squared=max((x-215.5)**2+(y-215.5)**2 for y in range(432) for x in range(432) if alpha.getpixel((x,y))>=64)
    assert radius_squared<=132**2, 'Mascot silhouette exceeds adaptive safe circle'
    directory=out/'mipmap-xxxhdpi';directory.mkdir(parents=True,exist_ok=True)
    foreground.save(directory/'ic_launcher_foreground.png',optimize=True)
    (out/'values').mkdir(parents=True,exist_ok=True)
    (out/'values/launcher_colors.xml').write_text('<resources><color name="launcher_background">'+BACKGROUND+'</color></resources>\n')
    adaptive='''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
'''
    (out/'mipmap-anydpi-v26').mkdir(parents=True,exist_ok=True)
    for name in ('ic_launcher','ic_launcher_round'):
        (out/('mipmap-anydpi-v26/'+name+'.xml')).write_text(adaptive)
    for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
        directory=out/('mipmap-'+density);directory.mkdir(parents=True,exist_ok=True)
        icon=Image.new('RGBA',(size,size),BACKGROUND)
        fitted=ImageOps.contain(art,(round(size*.82),round(size*.82)),Image.Resampling.LANCZOS)
        icon.alpha_composite(fitted,((size-fitted.width)//2,(size-fitted.height)//2))
        for name in ('ic_launcher','ic_launcher_round'):
            icon.save(directory/(name+'.png'),optimize=True)
    record=dict(sourceSha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(),background=BACKGROUND,
                adaptiveLayerPx=432,foregroundEnvelopePx=248,guaranteedSafeDiameterPx=264,
                actualForegroundMaxRadiusPx=radius_squared**.5,
                legacyDensities=5,adaptiveResources=2)
    return record

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--out',type=Path,required=True)
    args=parser.parse_args();print(json.dumps(build(args.out),indent=2))
