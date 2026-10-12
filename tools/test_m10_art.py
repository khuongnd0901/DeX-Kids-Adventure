#!/usr/bin/env python3
"""M10 asset planning, atlas reproducibility and unsafe XML regression."""
import hashlib
from pathlib import Path
from tempfile import TemporaryDirectory
from build_m10_atlas import ROOT, OUTPUT, inventory, parse_svg, build, DIMENSIONS


def checksums():
    return {x.name:hashlib.sha256(x.read_bytes()).hexdigest()
            for x in sorted(OUTPUT.glob("*")) if x.is_file()}


def main():
    rows=inventory()
    assert len(rows)==172
    assert len({x["asset_id"] for x in rows})==172
    assert len({x["source_path"] for x in rows})==172
    for entry in rows:
        path=ROOT/entry["source_path"]
        if path.is_file():
            parse_svg(path,*DIMENSIONS[entry["family"]])
    result=build()
    before=checksums()
    assert result["declared"]==172
    assert result["produced"]==len(result["regions"])
    assert result["prop_pages"] <= 3
    assert len((OUTPUT/"props-map.tsv").read_text(encoding="utf-8").splitlines()) == 48
    assert (OUTPUT/"props.atlas").is_file()
    assert len({x["region"] for x in result["regions"]})==result["produced"]
    for region in result["regions"]:
        x,y=region["xy"]; w,h=region["size"]
        assert x>=0 and y>=0 and x+w<=1024 and y+h<=1024
        assert region["review"]=="SOURCE_CREATED_UNREVIEWED"
    assert build()==result and checksums()==before, "Non-deterministic atlas output"
    with TemporaryDirectory() as directory:
        bad=Path(directory)/"injection.svg"
        bad.write_text('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 160"><script>oops</script></svg>')
        try:
            parse_svg(bad,160,160)
        except ValueError:
            pass
        else:
            raise AssertionError("Unsafe SVG accepted")
    print(f"M10 local contracts PASS: {len(rows)} planned, {result['produced']} sourced, {result['pages']} pages (runtime not tested)")


if __name__=="__main__":
    main()
