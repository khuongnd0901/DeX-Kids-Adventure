#!/usr/bin/env python3
"""T-051 catalog contracts; raster bounds do not establish visual acceptance.

Coast/mountain are catalog sources, intentionally absent from runtime props.atlas.
"""
from pathlib import Path
import hashlib
import io
import xml.etree.ElementTree as ET
import cairosvg
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'art/assets-source/m10/props'
OVERRIDES = ROOT / 'art/assets-source/storybook-v2/m10/props'
ALLOWED = {'svg', 'g', 'path', 'ellipse', 'rect', 'circle', 'polygon', 'line'}

def main():
    signatures = set()
    checked = 0
    for family in ('coast', 'mountain'):
        expected = {p.name for p in (BASE / family).glob('*.svg')}
        actual = {p.name for p in (OVERRIDES / family).glob('*.svg')}
        assert len(expected) == 8 and actual == expected, (family, expected ^ actual)
        for filename in sorted(expected):
            source = OVERRIDES / family / filename
            raw = source.read_bytes()
            assert raw != (BASE / family / filename).read_bytes(), source
            assert b'<!DOCTYPE' not in raw and b'<!ENTITY' not in raw, source
            root = ET.fromstring(raw)
            assert root.attrib['viewBox'] == '0 0 160 160', source
            assert root.attrib['width'] == root.attrib['height'] == '160', source
            assert root.attrib.get('role') == 'img', source
            for node in root.iter():
                assert node.tag.split('}')[-1] in ALLOWED, (source, node.tag)
                for key, value in node.attrib.items():
                    assert 'href' not in key and not key.lower().startswith('on'), source
                    assert 'url(' not in value.lower(), source
            # Render at native resolution: no alpha touches the outer 1px margin.
            raster = Image.open(io.BytesIO(cairosvg.svg2png(bytestring=raw))).convert('RGBA')
            assert raster.size == (160, 160), source
            alpha = raster.getchannel('A')
            bounds = alpha.getbbox()
            assert bounds and min(bounds[:2]) >= 1 and max(bounds[2:]) <= 159, (source, bounds)
            assert alpha.crop((1, 1, 159, 159)).getextrema()[1] == 255, source
            assert 2000 < sum(alpha.histogram()[1:]) < 20000, source
            # Distinct shape alpha masks, not recolors of one shared silhouette.
            signature = hashlib.sha256(alpha.tobytes()).hexdigest()
            assert signature not in signatures, source
            signatures.add(signature)
            checked += 1
    print(f'T-051 coast/mountain: {checked} editable sources; safe SVG/native-alpha/distinct-silhouette contracts PASS; runtime/aesthetic acceptance separate')

if __name__ == '__main__':
    main()
