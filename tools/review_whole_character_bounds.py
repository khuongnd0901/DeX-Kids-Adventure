#!/usr/bin/env python3
"""Propose whole-figure crop bounds for visual review; never modifies generated art.

Connected alpha silhouettes compensate for generated sheets with uneven row gutters.
Reject touching figures instead of silently slicing hats, feet or limbs at grid edges.
The checked-in rectangles are then pinned by source SHA256 for reproducible packing.
"""
from collections import deque
from pathlib import Path
import hashlib
import json
from PIL import Image
from build_whole_characters import SOURCE

def propose(path):
    image = Image.open(path)
    assert image.mode == 'RGBA'
    w, h = image.size
    mask = bytearray(image.getchannel('A').point(lambda a: 255 if a >= 192 else 0).tobytes())
    figures = []
    for start in range(len(mask)):
        if not mask[start]:
            continue
        mask[start] = 0
        queue = deque([start])
        left = right = start % w
        top = bottom = start // w
        count = 0
        while queue:
            at = queue.popleft()
            y, x = divmod(at, w)
            left, right = min(left,x), max(right,x)
            top, bottom = min(top,y), max(bottom,y)
            count += 1
            for n in (at-w if y else -1, at+w if y<h-1 else -1,
                      at-1 if x else -1, at+1 if x<w-1 else -1):
                if n >= 0 and mask[n]:
                    mask[n] = 0
                    queue.append(n)
        if count > 1500:
            figures.append((count, (left,top,right+1,bottom+1)))
    figures.sort(reverse=True)
    if len(figures) != 16:
        raise ValueError(f'{path.name}: expected16 separate whole figures, found{len(figures)}: {figures}')
    median = sorted(f[0] for f in figures)[8]
    if figures[0][0] > median*1.6:
        raise ValueError(f'{path.name}: likely touching whole figures; request corrected source')
    boxes = [None]*16
    for mass, (left,top,right,bottom) in figures:
        col = min(3,int((left+right)/2/w*4))
        row = min(3,int((top+bottom)/2/h*4))
        index = row*4+col
        if boxes[index] is not None:
            raise ValueError(f'{path.name}: duplicate grid slot{index}')
        boxes[index] = [max(0,left-2),max(0,top-2),min(w,right+2),min(h,bottom+2)]
    assert all(boxes)
    return dict(sha256=hashlib.sha256(path.read_bytes()).hexdigest(),size=[w,h],
                method='reviewed whole connected figure rectangles; no body-part extraction',boxes=boxes)

def main():
    file = SOURCE/'extraction.json'
    current = json.loads(file.read_text()) if file.exists() else {}
    for path in sorted(SOURCE.glob('*.png')):
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if current.get(path.name,{}).get('sha256') == digest:
            continue
        current[path.name] = propose(path)
        print('Whole-figure bounds proposed:',path.name)
        file.write_text(json.dumps(current,indent=2)+'\n')

if __name__ == '__main__':
    main()
