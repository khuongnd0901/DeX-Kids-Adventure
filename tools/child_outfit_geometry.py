"""T-053 build-time pose-aware garments; original pixels are never rewritten on disk.
Coordinates refer to each 256x384 quadrant, not the alpha-trimmed passenger.
Blue fabric is recolored with original shading; exposed skin and faces are excluded.
"""
from io import BytesIO
import math
import xml.etree.ElementTree as ET
import cairosvg
from PIL import Image, ImageDraw

# Face/hair/hat exclusion rectangles established from the original PNG quadrants.
FACES = {
 ('sau','story'): [(85,0,219,145),(28,20,170,169),(84,0,223,139),(43,23,191,170)],
 ('sau','passenger'): [(70,50,241,243),(20,61,182,258),(70,12,232,218),(5,25,196,219)],
 ('ong','story'): [(50,7,248,179),(24,34,221,208),(56,0,244,175),(12,45,239,209)],
 ('ong','passenger'): [(47,7,247,211),(16,35,222,229),(49,1,244,191),(9,25,239,214)],
}
CHEST = {
 ('sau','story'): [(133,203),(118,209),(138,195),(135,214)],
 ('sau','passenger'): [(143,296),(116,301),(151,264),(137,265)],
 ('ong','story'): [(139,224),(124,246),(137,219),(146,247)],
 ('ong','passenger'): [(143,279),(126,302),(144,259),(150,278)],
}
CROWN = {
 'story': [(139,34),(129,50),(145,25),(142,69)],
 'passenger': [(140,84),(120,104),(143,43),(142,77)],
}
SKIRTS = [
 'M90 248 Q133 265 179 248 Q204 291 211 337 Q139 362 72 337 Q76 295 90 248Z',
 'M90 273 Q126 285 166 265 Q186 309 193 344 Q129 363 69 342 Q72 305 90 273Z',
 'M93 244 Q136 257 179 242 Q209 294 213 329 Q143 354 76 330 Q78 288 93 244Z',
 'M76 224 Q124 247 164 226 Q179 251 202 276 Q147 310 77 283 Q64 259 76 224Z',
]

FACE_CORE={
 ('sau','story'): [(106,78,183,133),(70,98,150,145),(107,76,192,129),(84,108,173,151)],
 ('sau','passenger'): [(99,158,192,224),(64,171,162,241),(107,128,202,192),(69,140,174,204)],
 ('ong','story'): [(109,104,189,162),(105,138,185,185),(113,101,196,152),(100,140,182,181)],
 ('ong','passenger'): [(108,158,194,211),(102,193,179,237),(107,148,191,195),(97,164,179,212)],
}

def rgb(value):
    return tuple(int(value[i:i+2],16) for i in (1,3,5))

def palette(raw):
    root=ET.fromstring(raw)
    stops=[e.attrib['stop-color'] for e in root.iter() if e.tag.endswith('stop')]
    return stops[-1],stops[0]

def garment(source, raw, child, kind, pose, style):
    base=source.resize((256,384),Image.Resampling.LANCZOS)
    overlay=Image.new('RGBA',base.size)
    primary,light=palette(raw); color=rgb(primary)
    face=FACES[child,kind][pose]
    # Preserve linework, highlights, fabric folds and arm/leg pose of original art.
    for y in range(384):
        for x in range(256):
            r,g,b,a=base.getpixel((x,y))
            if face[0]<=x<face[2] and face[1]<=y<face[3] and (r>g*.8 or b<70):continue
            if a and b>45 and b>r*1.45 and b>g*1.12:
                brightness=max(.18,min(1.45,(.30*r+.59*g+.11*b)/105))
                overlay.putpixel((x,y),tuple(min(255,int(c*brightness)) for c in color)+(a,))
    cx,cy=CHEST[child,kind][pose]
    # Six distinct authored motifs follow each chest, never a generic headband.
    motifs=[
        '<circle r="10" fill="#ffe59c"/><g stroke="#ffe59c" stroke-width="2">'+''.join(f'<path d="M{math.cos(i*math.pi/4)*12:.2f} {math.sin(i*math.pi/4)*12:.2f}l{math.cos(i*math.pi/4)*4:.2f} {math.sin(i*math.pi/4)*4:.2f}"/>' for i in range(8))+'</g>',
        '<rect x="-10" y="-9" width="20" height="18" rx="3" fill="#a5edf0"/><path d="M-6-4H4V5H-3M0-9V-14M10 0H15" fill="none" stroke="#315b78" stroke-width="2"/>',
        '<circle r="13" fill="#fbe3b8"/><path d="M-11-6L11 6M-11 6L11-6M0-12V12M-12 0H12M-7-7Q0-1 7-7M-7 7Q0 1 7 7" fill="none" stroke="#814957" stroke-width="1.4"/>',
        '<path d="M2-15L-10 2H-1L-3 15L11-4H3L7-15Z" fill="#fff3a9" stroke="#906839" stroke-width="1.5"/>',
        '<path d="M-12-12H12V1Q9 11 0 15Q-9 11-12 1Z" fill="#fff2b4" stroke="#5c668a" stroke-width="1.5"/><path d="M0-8L3-2H8L4 2L5 8L0 5L-5 8L-4 2L-8-2H-3Z" fill="#dd9b57"/>',
        '<circle r="13" fill="#ffeab0"/><circle cx="6" cy="-5" r="11" fill="'+primary+'"/>',
    ]
    if child=='ong':
        # A skirt per gesture; exclude skin so waving/counting/petting hands stay intact.
        if kind=='story':
            ornaments = [
                '<path d="M0-9Q10-15 8-3Q20 0 8 6Q8 18 0 9Q-12 15-9 3Q-20-2-8-6Q-6-18 0-9Z" fill="#fff0da"/><circle r="4" fill="#eaa46c"/>',
                '<path d="M-12-5Q-6-12 0-5T12-5M-12 3Q-6-4 0 3T12 3M-10 10Q-5 4 0 10T10 10" stroke="#f5fffa" fill="none" stroke-width="3"/>',
                '<path d="M-13 5Q-19-11-4-5Q0-20 4-5Q19-11 13 5Z" fill="#f2f8ff"/><circle cy="6" r="4" fill="#e9cf8d"/>',
                '<path d="M-11 11Q-18-12 12-13Q18 8-11 11Z" fill="#d9f5b5"/><path d="M-9 9L9-10" stroke="#538769" stroke-width="2"/>',
                '<path d="M0-15L4-4L15-4L6 3L9 14L0 7L-9 14L-6 3L-15-4L-4-4Z" fill="#fff4ae"/>',
                '<path d="M0 14V-13M-5 10V-7M5 10V-7" stroke="#71996a" stroke-width="2"/><g fill="#ead7ff"><ellipse cy="-9" rx="4" ry="7"/><ellipse cx="-5" cy="-2" rx="3" ry="6"/><ellipse cx="5" cy="-2" rx="3" ry="6"/></g>',
            ]
            skirt_y = [298,316,289,275][pose]
            svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="256" height="384"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="{light}"/><stop offset=".5" stop-color="{primary}"/><stop offset="1" stop-color="{light}"/></linearGradient><clipPath id="sk"><path d="{SKIRTS[pose]}"/></clipPath></defs><path d="{SKIRTS[pose]}" fill="url(#g)" stroke="{primary}" stroke-width="2"/><g clip-path="url(#sk)"><path d="M100 240Q95 290 86 352M165 240Q183 297 191 352" fill="none" stroke="{light}" stroke-width="4" opacity=".8"/><g transform="translate(140 {skirt_y})">{ornaments[style]}</g></g></svg>'
            skirt=Image.open(BytesIO(cairosvg.svg2png(bytestring=svg.encode()))).convert('RGBA')
            for y in range(384):
                for x in range(256):
                    r,g,b,a=base.getpixel((x,y))
                    if a>80 and r>g*1.12 and r>b*1.35 and g>100 and b>70 and ((pose==3 and y<268 and (80<x<125 or x>171)) or (pose!=3 and y<250)):
                        skirt.putpixel((x,y),(0,0,0,0))
            overlay.alpha_composite(skirt)
        cx,cy=CROWN[kind][pose]
        svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="256" height="384"><g transform="translate({cx} {cy})"><path d="M-24 4L-25-11L-12-2L0-20L12-2L25-11L24 4Z" fill="#ffe6a2" stroke="#936b39" stroke-width="1.5"/><circle cy="-8" r="3" fill="{primary}"/></g></svg>'
        overlay.alpha_composite(Image.open(BytesIO(cairosvg.svg2png(bytestring=svg.encode()))).convert('RGBA'))
    else:
        svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="256" height="384"><g transform="translate({cx} {cy})">{motifs[style]}</g></svg>'
        overlay.alpha_composite(Image.open(BytesIO(cairosvg.svg2png(bytestring=svg.encode()))).convert('RGBA'))
    # Absolute eye/mouth exclusion; garment antialiasing must never touch identity.
    ImageDraw.Draw(overlay).rectangle(FACE_CORE[child,kind][pose], fill=(0,0,0,0))
    return overlay
