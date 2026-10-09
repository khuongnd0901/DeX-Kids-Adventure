#!/usr/bin/env python3
"""Validate stable facial landmarks across Capybara expression assets."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1] / "art/assets-source/characters"
STATES=("idle","blink","talk","wave","sleep","surprised")
NAMESPACE="{http://www.w3.org/2000/svg}"
def child(root, element_id):
    node = next((e for e in root.iter() if e.get("id")==element_id), None)
    if node is None: raise AssertionError(f"Missing landmark {element_id}")
    return node

def main():
    for state in STATES:
        path=ROOT / f"capybara_{state}.svg"
        root=ET.parse(path).getroot()
        assert root.attrib["viewBox"]=="0 0 240 250", str(path)
        muzzle=child(root,"muzzle")
        nose=child(root,"nose")
        mouth=child(root,"mouth")
        cheeks=[child(root,x) for x in ("left-blush","right-blush")]
        assert (float(muzzle.get("cx")), float(muzzle.get("cy")))==(161,153)
        assert all(float(c.get("cy"))<153 for c in cheeks), state
        assert (float(cheeks[0].get("cx")), float(cheeks[1].get("cx")))==(103,195)
        assert nose.get("d")==child(ET.parse(ROOT/"capybara_idle.svg").getroot(),"nose").get("d")
        if mouth.tag==NAMESPACE+"ellipse":
            assert abs(float(mouth.get("cx"))-159)<=1 and float(mouth.get("cy"))>164
        else:
            assert mouth.get("d"), state
    print("Verified: 6 Capybara expressions share anatomically aligned cheeks, nose and mouth")
if __name__=="__main__":
    main()
