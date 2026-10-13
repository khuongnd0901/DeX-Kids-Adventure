#!/usr/bin/env python3
"""M10 source guard for single-display renderer and scoped atlas consumption.

Not a substitute for Gradle/LibGDX/Android render execution.
"""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"core/src/main/java/com/khuongnd/dexkids/game"
sprites=(JAVA/"CartoonSprites.java").read_text(encoding="utf-8")
selector=(JAVA/"M10SceneVariety.java").read_text(encoding="utf-8")
atlas=(JAVA/"M10PropAtlas.java").read_text(encoding="utf-8")
screen=(JAVA/"AdventureScreen.java").read_text(encoding="utf-8")
builder=(ROOT/"tools/build_m10_atlas.py").read_text(encoding="utf-8")
prop_builder=(ROOT/"tools/build_m10_prop_atlas.py").read_text(encoding="utf-8")

assert "variety.beginVisibleWindow(first)" in sprites
assert "variety.pick(idx, biome, pool)" in sprites
assert "M10PropAtlas m10Art" in sprites
assert "m10Art.choose(biome, which)" in sprites
assert "else batch.draw(extras[which],x,y,w,h);" in sprites
assert "m10Art.dispose();" in sprites
legacy_motion = sprites[sprites.index("if (upgrade == null)"):sprites.index("if (upgrade != null) batch.draw")]
assert "y=637f" in legacy_motion and "Math.sin" in legacy_motion
hotpath=sprites[sprites.index("void drawExtras(SpriteBatch batch,double distanceMeters,PoiSceneDirector.Scene scene,") :sprites.index("private void drawLamp")]
assert "new Texture(" not in hotpath and "new TextureAtlas(" not in hotpath
assert "Gdx.files.internal" not in hotpath
assert "new AnimationTimeline()" in screen
assert "DynamicSceneComposer composer" in screen
assert "new ProceduralWorldGenerator(20261008L)" in screen
assert "this.scenery = java.util.Objects.requireNonNull(scenery)" in sprites
assert "Gdx.files.internal(\"generated/m10/props.atlas\")" in atlas
assert "Gdx.files.internal(\"generated/m10/props-map.tsv\")" in atlas
assert "case BRIDGE, RIVER, GENERAL -> -1" in atlas
assert 'Gdx.app.error("M10Art"' in atlas
assert "if(atlas!=null)atlas.dispose();" in atlas
assert "private final int[] lastEight = new int[8]" in selector
assert "beginVisibleWindow(long firstChunk)" in selector
assert "build_props(result)" in builder
assert "if len(pages)>3:" in prop_builder
print("M10 static source guard PASS: bounded 3-page atlas, old fallback, no per-frame texture IO, no new composer")
