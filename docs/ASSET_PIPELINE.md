# T-003: editable SVG to LibGDX TextureAtlas

Source art lives in `art/assets-source/{characters,vehicles,environment}`.
Generated PNG files (`assets/generated/sprites/*.png`) and the packed `kids.png` + `kids.atlas` are **build outputs**, ignored by Git.
Android assets source points to `../assets`, and Desktop uses repository root as its working directory.
The screenshot reference from the discussion is an art direction example only. Packaged graphics are independent, original SVG redraws.

Build (on Ubuntu JDK17 with Python virtual environment):
```
python3 -m pip install cairosvg==2.8.2 Pillow==12.3.0
./gradlew generateKidsArt
./gradlew :core:test :android:assembleDebug
./gradlew :desktop:run
```
Gradle automatically builds sprites before Android asset merge and desktop `run`.
LibGDX loads `generated/kids.atlas` with `TextureAtlas` and uses `SpriteBatch` for the bus/capybara/wheels. Both Android and desktop reference the same assets.

Animations: wheel rotation tracks journey distance; capybara idle/wave/blink cycles with render time; the talking pose is in the atlas but **not played without actual narration**.
Still missing: approved final art, talking audio sync, parallax final environment tiles, device screenshots and visual seam checks.
