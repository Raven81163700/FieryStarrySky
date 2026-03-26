# 角色形象资源规格说明（JavaME）

## 1）基础画布
- 逻辑画布尺寸：32x64 像素
- UI 预览缩放：建议 x2 或 x3（最近邻，不做平滑）
- 图片格式：PNG，推荐索引色（<= 256 色）
- 坐标原点：精灵左上角为 (0,0)

## 2）图层顺序
1. 基础体态层（动作 + 服饰）
2. 肤色替换层（根据元数据中的皮肤区域掩码）
3. 五官预设层（眼/鼻/嘴）
4. 面部自由绘制层（仅 faceRect 区域）
5. 装饰层（最高优先级）
6. 全身自由绘制层（可选，在编辑时作为顶层覆盖）

## 3）所需精灵图
- body_presets.png
  - 单帧尺寸：32x64
  - 建议数量：>= 3
  - 命名建议：body_00、body_01、body_02...
- face_eyes.png
  - 单元尺寸：16x16（与 faceRect 对齐）
  - 建议数量：>= 8
- face_nose.png
  - 单元尺寸：16x16
  - 建议数量：>= 6
- face_mouth.png
  - 单元尺寸：16x16
  - 建议数量：>= 8
- accessories.png
  - 方案 A：单元尺寸 32x64
  - 方案 B：单元尺寸 16x16 + 锚点元数据
  - 建议数量：>= 10

## 4）核心元数据
- face_rect
  - 所有体态必须使用同一面部区域坐标
  - 必填字段：x, y, w, h
  - 当前客户端默认：x=8, y=8, w=16, h=16
- skin_masks
  - 每个体态预设对应一组皮肤区域块
  - 每块必填字段：x, y, w, h
- accessory_anchors（当装饰使用 16x16 单元时）
  - 必填字段：bodyPresetId, accessoryId, x, y

## 5）颜色元数据
- skin_palettes
  - 预设肤色建议：6~12 组
  - 必填字段：id, r, g, b, label
- user_custom_skin
  - 用户自定义肤色 RGB 范围：0..255
  - 建议步进：5
- paint_palette
  - 绘制调色板颜色数：建议 8~16
  - 必填字段：id, rgb

## 6）预设元数据
- body_presets
  - id, name, genderStyle（可选）, frameIndex
- face_presets
  - eyesId, noseId, mouthId（可任意组合）
- accessory_presets
  - id, name, layerOrder

## 7）角色运行时可编辑数据
- roleName（角色昵称）
- gender（male/female/agender）
- bodyPresetId
- skinPresetId + customSkinRGB
- eyesPresetId, nosePresetId, mouthPresetId
- accessoryPresetId
- facePaintPixels（面部局部像素覆盖）
- bodyPaintPixels（32x64 全身像素覆盖）

## 8）推荐文件命名
- body_presets.png
- face_eyes.png
- face_nose.png
- face_mouth.png
- accessories.png
- metadata_avatar.json

## 9）metadata_avatar.json 示例
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

## 10）精灵表机制（Sprite Sheet）
- 资源放置：统一放在 `client/res/avatar/` 目录，打包时会进入 JAR。
- 切片规则：
  - `body_presets.png`：按 32x64 单元横向切片，`frameIndex` 对应第几个单元。
  - `face_eyes.png` / `face_nose.png` / `face_mouth.png`：按 16x16 单元横向切片。
  - `accessories.png`：可选 32x64 单元切片，或 16x16 单元 + 锚点偏移。
- 组合绘制顺序：
  1. `body frame`
  2. `skin masks recolor`
  3. `eyes + nose + mouth`
  4. `face paint`
  5. `accessory`
  6. `body paint`
- 运行时索引：
  - `bodyPresetId` -> 体态帧
  - `eyesPresetId` / `nosePresetId` / `mouthPresetId` -> 五官帧
  - `accessoryPresetId` -> 装饰帧

## 11）元数据机制（含肤色色块存储）
- 推荐两种格式：
  - 方案 A：`metadata_avatar.json`（可读性好）
  - 方案 B：`metadata_avatar.txt` 行协议（JavaME 解析更轻量，推荐首版）
- 肤色色块存储建议：按体态分组，使用矩形块列表，字段为 `bodyPresetId|x|y|w|h`。
- 示例（行协议）：
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
- 运行时逻辑：
  1. 根据 `bodyPresetId` 找到对应 `BODY_SKIN_BLOCK` 列表。
  2. 用当前肤色 RGB 填充所有块。
  3. 再叠加五官、面部涂改、装饰、全身涂改。
- 用户涂改数据建议：
  - `facePaintPixels` 与 `bodyPaintPixels` 使用稀疏存储（仅保存非空像素），减少传输和存储体积。
