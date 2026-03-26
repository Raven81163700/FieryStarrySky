# Character Avatar Asset Spec (JavaME)

## 1) Base Canvas
- Logical canvas size: 32x64 px
- Render scale in UI: x2 or x3 (nearest-neighbor)
- Pixel format: PNG, indexed color preferred (<= 256 colors)
- Anchor origin: top-left of sprite is (0,0)

## 2) Layer Order
1. Base body preset (pose + clothes)
2. Skin replacement layer (by metadata mask)
3. Face presets (eyes/nose/mouth)
4. Face free-paint layer (face rect only)
5. Accessory layer (highest priority)
6. Full-body free-paint overlay (optional, highest in editor mode)

## 3) Required Sprite Sheets
- body_presets.png
  - Frame size: 32x64
  - Suggested count: >= 3
  - Naming: body_00, body_01, body_02...
- face_eyes.png
  - Unit size: 16x16 (fits face rect)
  - Suggested count: >= 8
- face_nose.png
  - Unit size: 16x16
  - Suggested count: >= 6
- face_mouth.png
  - Unit size: 16x16
  - Suggested count: >= 8
- accessories.png
  - Unit size: 32x64 (or 16x16 + anchor metadata)
  - Suggested count: >= 10

## 4) Core Metadata
- face_rect
  - Fixed for all body presets
  - Required fields: x, y, w, h
  - Current client assumption: x=8, y=8, w=16, h=16
- skin_masks
  - Per body preset, skin pixel blocks list
  - Required fields per block: x, y, w, h
- accessory_anchors (if using 16x16 accessory units)
  - Required fields: bodyPresetId, accessoryId, x, y

## 5) Color Metadata
- skin_palettes
  - Recommended predefined skin entries: 6~12
  - Required fields: id, r, g, b, label
- user_custom_skin
  - RGB sliders range: 0..255
  - Suggested step: 5
- paint_palette
  - Recommended color count: 8~16
  - Required fields: id, rgb

## 6) Preset Metadata
- body_presets
  - id, name, genderStyle(optional), frameIndex
- face_presets
  - eyesId, noseId, mouthId combinable
- accessory_presets
  - id, name, layerOrder

## 7) Runtime Editable Data (per character)
- roleName
- gender (male/female/agender)
- bodyPresetId
- skinPresetId + customSkinRGB
- eyesPresetId, nosePresetId, mouthPresetId
- accessoryPresetId
- facePaintPixels (faceRect local pixel overrides)
- bodyPaintPixels (32x64 pixel overrides)

## 8) Recommended File Naming
- body_presets.png
- face_eyes.png
- face_nose.png
- face_mouth.png
- accessories.png
- metadata_avatar.json

## 9) metadata_avatar.json Example
```json
{
  "canvas": { "width": 32, "height": 64 },
  "faceRect": { "x": 8, "y": 8, "w": 16, "h": 16 },
  "skinPalettes": [
    { "id": 0, "rgb": [255, 224, 189], "label": "fair" },
    { "id": 1, "rgb": [224, 177, 132], "label": "warm" }
  ],
  "bodyPresets": [
    {
      "id": 0,
      "name": "classic",
      "frameIndex": 0,
      "skinBlocks": [
        { "x": 11, "y": 8, "w": 10, "h": 10 },
        { "x": 9, "y": 18, "w": 14, "h": 6 }
      ]
    }
  ],
  "accessoryAnchors": [
    { "bodyPresetId": 0, "accessoryId": 3, "x": 8, "y": 8 }
  ]
}
```

## 10) Sprite Sheet Mechanism
- Resource location: place all avatar assets under `client/res/avatar/` so they are packaged into the JAR.
- Slice rules:
  - `body_presets.png`: slice horizontally by 32x64 cells, `frameIndex` maps to the cell index.
  - `face_eyes.png` / `face_nose.png` / `face_mouth.png`: slice horizontally by 16x16 cells.
  - `accessories.png`: either 32x64 cells, or 16x16 cells plus anchor metadata.
- Composition order:
  1. `body frame`
  2. `skin masks recolor`
  3. `eyes + nose + mouth`
  4. `face paint`
  5. `accessory`
  6. `body paint`
- Runtime index mapping:
  - `bodyPresetId` -> body frame
  - `eyesPresetId` / `nosePresetId` / `mouthPresetId` -> face frames
  - `accessoryPresetId` -> accessory frame

## 11) Metadata Mechanism (Including Skin Block Storage)
- Recommended formats:
  - Option A: `metadata_avatar.json` (readable)
  - Option B: line-based `metadata_avatar.txt` (lighter parsing for JavaME, recommended for v1)
- Skin block storage recommendation: group by body preset and store rectangle blocks as `bodyPresetId|x|y|w|h`.
- Example (line-based format):
```txt
CANVAS|32|64
FACE_RECT|8|8|16|16

SKIN_PRESET|0|255|224|189|fair
SKIN_PRESET|1|241|194|125|warm

BODY|0|0
BODY_SKIN_BLOCK|0|11|8|10|10
BODY_SKIN_BLOCK|0|9|18|14|6
BODY_SKIN_BLOCK|0|6|28|4|14
BODY_SKIN_BLOCK|0|22|28|4|14

BODY|1|1
BODY_SKIN_BLOCK|1|10|8|12|10
BODY_SKIN_BLOCK|1|9|18|14|6

EYE_PRESET_COUNT|8
NOSE_PRESET_COUNT|6
MOUTH_PRESET_COUNT|8
ACCESSORY_PRESET_COUNT|10

ACCESSORY_ANCHOR|0|3|8|8
```
- Runtime logic:
  1. Find `BODY_SKIN_BLOCK` list by `bodyPresetId`.
  2. Fill all listed blocks with current skin RGB.
  3. Layer face presets, face paint, accessories, and body paint on top.
- User paint data recommendation:
  - store `facePaintPixels` and `bodyPaintPixels` sparsely (only non-empty pixels) to reduce payload and storage size.
