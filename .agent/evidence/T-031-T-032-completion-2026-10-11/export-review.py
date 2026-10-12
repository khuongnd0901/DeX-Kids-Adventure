import json
from pathlib import Path
p=Path(__file__).parent
rows=json.loads((p/'reviewed-lessons.json').read_text())
s=['# Nội dung Gemini sau sửa, cần phụ huynh duyệt','', 'Bộ kiểm tra Java và lượt duyệt AI đã chấp nhận. Không chứa dữ liệu trẻ.','']
for pack in rows:
    s.extend([f"## Sâu {pack['sauAge']} / Ong {pack['ongAge']}",''])
    for i,x in enumerate(pack['lessons'],1):
        s.extend([f"{i}. **{x['focus']} · {x['age']} tuổi · {x['genre']}**: {x['prompt']}",f"   - {x['answer']}",f"   - {x['english']}: {x['meaning']}",''])
(p/'content-review.md').write_text('\n'.join(s))
print('Exported',sum(len(x['lessons']) for x in rows),'lessons in',len(rows),'packs')
