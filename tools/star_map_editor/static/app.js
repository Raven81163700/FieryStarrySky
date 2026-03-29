const { createApp, ref, computed, onMounted } = Vue;

const vuetify = Vuetify.createVuetify();

createApp({
  setup() {
    const ZOOM_STEP = 0.1;
    const ZOOM_MIN = 0.45;
    const ZOOM_MAX = 2.4;
    const GRID_SIZE = 24;
    // 跨星座星门配色策略: "gradient"(两端渐变) | "neutral"(固定中性色)
    const CROSS_CONSTELLATION_GATE_STYLE = "gradient";
    const CROSS_CONSTELLATION_NEUTRAL_COLOR = "#b9c6d8";

    const mapWidth = ref(2400);
    const mapHeight = ref(1600);
    const zoom = ref(1);
    const panX = ref(0);
    const panY = ref(0);
    const selectionMode = ref("systems");

    const systems = ref([]);
    const links = ref([]);
    const constellations = ref([]);
    const domains = ref([]);

    const selectedSystemIds = ref([]);
    const selectedConstellationIds = ref([]);
    const selectedDomainId = ref(null);
    const activeSystemId = ref(null);
    const activeConstellationId = ref(null);

    const nextSystemId = ref(1);
    const nextConstellationId = ref(1);
    const nextDomainId = ref(1);

    const svgRef = ref(null);
    const selectionRect = ref(null);

    const draggingSystemId = ref(null);
    const pointerState = ref(null);

    const dialog = ref({
      open: false,
      type: "",
      title: "",
      form: {
        name: "",
        security: 0.8,
        controller: "",
        color: "#ffb74d",
        description: "",
      },
      pending: {
        x: 0,
        y: 0,
      },
    });

    const snackbar = ref({ open: false, text: "", color: "warning" });
    const socketConnected = ref(false);
    // 延迟连接，先绑定事件再连接，避免错过 connect 事件
    const socket = io({ autoConnect: false });

    const gridSize = GRID_SIZE;
    const gridColumnCount = computed(() => Math.ceil(mapWidth.value / GRID_SIZE));
    const gridRowCount = computed(() => Math.ceil(mapHeight.value / GRID_SIZE));
    const viewTransform = computed(() => `translate(${panX.value} ${panY.value}) scale(${zoom.value})`);
    const zoomPercent = computed(() => Math.round(zoom.value * 100));
    const modeHint = computed(() => {
      if (selectionMode.value === "systems") {
        return "星系选择模式: 可创建/删除星门，或由已选星系创建星座";
      }
      return "星座选择模式: 可由已选星座创建星域，删除星座或删除星域";
    });

    const selectedSystem = computed(() => {
      const id = activeSystemId.value || selectedSystemIds.value[selectedSystemIds.value.length - 1];
      return systems.value.find((s) => s.id === id) || null;
    });

    const selectedConstellation = computed(() => {
      const id = activeConstellationId.value || selectedConstellationIds.value[selectedConstellationIds.value.length - 1];
      return constellations.value.find((c) => c.id === id) || null;
    });

    const selectedDomain = computed(() => domains.value.find((d) => d.id === selectedDomainId.value) || null);

    const selectedGatePair = computed(() => {
      if (selectedSystemIds.value.length !== 2) return null;
      const [a, b] = selectedSystemIds.value;
      if (!a || !b || a === b) return null;
      return [a, b];
    });

    const hasGateBetweenSelected = computed(() => {
      if (!selectedGatePair.value) return false;
      const [a, b] = selectedGatePair.value;
      return links.value.some(
        (l) => Number(l.link_type) === 2 && ((l.from_id === a && l.to_id === b) || (l.from_id === b && l.to_id === a))
      );
    });

    const canToggleGate = computed(() => !!selectedGatePair.value);

    const visibleLabels = computed(() => {
      const systemOpacity = clamp((zoom.value - 0.8) / 0.55, 0, 1);
      const constellationOpacity =
        clamp((zoom.value - 0.6) / 0.35, 0, 1) * clamp((1.3 - zoom.value) / 0.45, 0, 1);
      const domainOpacity = clamp((1.45 - zoom.value) / 0.9, 0.12, 1);

      const domainLabels = domains.value
        .map((d) => {
          const center = domainLabel(d);
          if (!center) return null;
          const force = selectedDomainId.value === d.id;
          return {
            id: d.id,
            name: d.name,
            x: center.x,
            y: center.y,
            opacity: force ? 1 : domainOpacity,
          };
        })
        .filter(Boolean)
        .filter((x) => x.opacity > 0.1);

      const constellationLabels = constellations.value
        .map((c) => {
          const center = constellationLabel(c);
          if (!center) return null;
          const force = selectedConstellationIds.value.includes(c.id);
          return {
            id: c.id,
            name: c.name,
            x: center.x,
            y: center.y,
            opacity: force ? 1 : constellationOpacity,
          };
        })
        .filter(Boolean)
        .filter((x) => x.opacity > 0.1);

      const systemLabels = systems.value
        .map((s) => {
          const force = selectedSystemIds.value.includes(s.id);
          return {
            id: s.id,
            name: s.name,
            x: s.x,
            y: s.y - 13,
            opacity: force ? 1 : systemOpacity,
          };
        })
        .filter((x) => x.opacity > 0.1);

      return {
        systems: systemLabels,
        constellations: constellationLabels,
        domains: domainLabels,
      };
    });

    function showTip(text, color) {
      snackbar.value.text = text;
      snackbar.value.color = color || "warning";
      snackbar.value.open = true;
    }

    function clamp(value, min, max) {
      return Math.max(min, Math.min(max, value));
    }

    function clampSecurity(value) {
      const v = Number(value);
      if (Number.isNaN(v)) return 0;
      return clamp(v, -1, 1);
    }

    function normalizeSystem(src) {
      return {
        id: Number(src.id),
        name: src.name || `星系-${src.id}`,
        x: Number(src.x) || 0,
        y: Number(src.y) || 0,
        security: clampSecurity(src.security ?? 0.8),
        controller: src.controller || "",
        description: src.description || "",
      };
    }

    function normalizeConstellation(src) {
      return {
        id: Number(src.id),
        name: src.name || `星座-${src.id}`,
        controller: src.controller || "",
        description: src.description || "",
        color: src.color || "#ffb74d",
        systemIds: Array.isArray(src.systemIds)
          ? src.systemIds.map((x) => Number(x)).filter(Boolean)
          : [],
      };
    }

    function normalizeDomain(src) {
      return {
        id: Number(src.id),
        name: src.name || `星域-${src.id}`,
        controller: src.controller || "",
        description: src.description || "",
        color: src.color || "rgba(72, 196, 255, 0.3)",
        constellationIds: Array.isArray(src.constellationIds)
          ? src.constellationIds.map((x) => Number(x)).filter(Boolean)
          : [],
      };
    }

    function systemById(id) {
      return systems.value.find((s) => s.id === id) || { x: 0, y: 0 };
    }

    function buildPayload() {
      return {
        systems: systems.value.map((s) => ({
          id: s.id,
          name: s.name,
          x: s.x,
          y: s.y,
          security: clampSecurity(s.security),
          controller: s.controller || "",
          description: s.description || "",
        })),
        links: links.value.map((l) => ({
          from_id: Number(l.from_id),
          to_id: Number(l.to_id),
          link_type: Number(l.link_type || 1),
          cost: Number(l.cost || 1),
        })),
        bodies: [],
        constellations: constellations.value.map((c) => ({
          id: c.id,
          name: c.name,
          controller: c.controller || "",
          description: c.description || "",
          color: c.color || "#ffb74d",
          systemIds: (c.systemIds || []).map((x) => Number(x)).filter(Boolean),
        })),
        domains: domains.value.map((d) => ({
          id: d.id,
          name: d.name,
          controller: d.controller || "",
          description: d.description || "",
          color: d.color || "rgba(72, 196, 255, 0.3)",
          constellationIds: (d.constellationIds || []).map((x) => Number(x)).filter(Boolean),
        })),
      };
    }

    // 保存到服务器（socket 优先，HTTP POST 入底）
    async function _persistToServer(payload) {
      if (socketConnected.value) {
        socket.emit("update_state", payload);
      } else {
        // socket 未连接时用 HTTP POST 保证写入磁盘
        try {
          const resp = await fetch("/save", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });
          if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
          showTip("已保存（HTTP）", "success");
        } catch (e) {
          showTip(`保存失败: ${e.message}`, "error");
        }
      }
    }

    function emitState() {
      _persistToServer(buildPayload());
    }

    function save() {
      socket.emit("save", buildPayload());
    }

    // 防抖包装，用于输入框实时同步
    function _debounce(fn, delay) {
      let _t = null;
      return function () { clearTimeout(_t); _t = setTimeout(() => fn(), delay); };
    }
    const emitStateDebounced = _debounce(emitState, 500);

    function exportJson() {
      fetch("/export", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(buildPayload()),
      })
        .then((r) => r.blob())
        .then((blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = "star_map_export.json";
          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
        });
    }

    function loadState(data) {
      const src = data || {};
      systems.value = Array.isArray(src.systems) ? src.systems.map(normalizeSystem) : [];
      links.value = Array.isArray(src.links)
        ? src.links
            .map((l) => ({
              from_id: Number(l.from_id),
              to_id: Number(l.to_id),
              link_type: Number(l.link_type || 1),
              cost: Number(l.cost || 1),
            }))
            .filter((l) => l.from_id && l.to_id && l.from_id !== l.to_id)
        : [];
      constellations.value = Array.isArray(src.constellations)
        ? src.constellations.map(normalizeConstellation)
        : [];
      domains.value = Array.isArray(src.domains) ? src.domains.map(normalizeDomain) : [];

      nextSystemId.value = systems.value.reduce((m, s) => Math.max(m, s.id), 0) + 1;
      nextConstellationId.value = constellations.value.reduce((m, c) => Math.max(m, c.id), 0) + 1;
      nextDomainId.value = domains.value.reduce((m, d) => Math.max(m, d.id), 0) + 1;

      selectedSystemIds.value = selectedSystemIds.value.filter((id) => systems.value.some((s) => s.id === id));
      selectedConstellationIds.value = selectedConstellationIds.value.filter((id) =>
        constellations.value.some((c) => c.id === id)
      );
      if (!systems.value.some((s) => s.id === activeSystemId.value)) activeSystemId.value = null;
      if (!constellations.value.some((c) => c.id === activeConstellationId.value)) activeConstellationId.value = null;
      if (!domains.value.some((d) => d.id === selectedDomainId.value)) selectedDomainId.value = null;
    }

    function setSelectionMode(mode) {
      selectionMode.value = mode;
      selectionRect.value = null;
    }

    function applyZoom(v) {
      zoom.value = clamp(Number(v.toFixed(3)), ZOOM_MIN, ZOOM_MAX);
    }

    function stepZoom(direction) {
      applyZoom(zoom.value + ZOOM_STEP * direction);
    }

    function setZoomByPercent(percent) {
      applyZoom(percent / 100);
    }

    function expandMap() {
      mapWidth.value += 1200;
      mapHeight.value += 900;
    }

    function screenToWorld(clientX, clientY) {
      const rect = svgRef.value.getBoundingClientRect();
      const sx = clientX - rect.left;
      const sy = clientY - rect.top;
      return {
        x: (sx - panX.value) / zoom.value,
        y: (sy - panY.value) / zoom.value,
      };
    }

    function findSystemAt(wx, wy) {
      const radius = 12 / Math.max(zoom.value, 0.3);
      return systems.value.find((s) => Math.hypot(s.x - wx, s.y - wy) <= radius) || null;
    }

    function snapToGrid(x, y) {
      return {
        x: Math.round(x / GRID_SIZE) * GRID_SIZE,
        y: Math.round(y / GRID_SIZE) * GRID_SIZE,
      };
    }

    function ensureMapBounds(x, y) {
      while (x > mapWidth.value - GRID_SIZE * 2) mapWidth.value += 1200;
      while (y > mapHeight.value - GRID_SIZE * 2) mapHeight.value += 900;
    }

    function toggleSystemSelection(id) {
      const idx = selectedSystemIds.value.indexOf(id);
      if (idx >= 0) {
        selectedSystemIds.value.splice(idx, 1);
        if (activeSystemId.value === id) activeSystemId.value = selectedSystemIds.value[selectedSystemIds.value.length - 1] || null;
      } else {
        selectedSystemIds.value.push(id);
        activeSystemId.value = id;
      }
    }

    function toggleConstellationSelection(id) {
      const idx = selectedConstellationIds.value.indexOf(id);
      if (idx >= 0) {
        selectedConstellationIds.value.splice(idx, 1);
      } else {
        selectedConstellationIds.value.push(id);
      }
      activeConstellationId.value = selectedConstellationIds.value[selectedConstellationIds.value.length - 1] || null;
    }

    function systemClass(id) {
      if (activeSystemId.value === id) return "system-point active";
      if (selectedSystemIds.value.includes(id)) return "system-point selected";
      return "system-point";
    }

    function findConstellationBySystem(systemId) {
      return constellations.value.find((c) => (c.systemIds || []).includes(systemId)) || null;
    }

    function linkConstellations(link) {
      if (!link) return { from: null, to: null };
      return {
        from: findConstellationBySystem(link.from_id),
        to: findConstellationBySystem(link.to_id),
      };
    }

    function isCrossConstellationLink(link) {
      if (!link || Number(link.link_type) !== 2) return false;
      const pair = linkConstellations(link);
      if (!pair.from || !pair.to) return false;
      return pair.from.id !== pair.to.id;
    }

    // 返回给定 link 的星座颜色（优先取 from_id 的星座，其次 to_id），仅用于星门(link_type==2)
    function linkColor(link) {
      if (!link) return null;
      if (Number(link.link_type) !== 2) return null;
      const pair = linkConstellations(link);
      if (pair.from && pair.from.color) return pair.from.color;
      if (pair.to && pair.to.color) return pair.to.color;
      return '#ffc473';
    }

    function gateEndpointColor(link, side) {
      const pair = linkConstellations(link);
      const c = side === 'to' ? pair.to : pair.from;
      if (c && c.color) return c.color;
      return CROSS_CONSTELLATION_NEUTRAL_COLOR;
    }

    // 返回 gate 线条的 stroke 值: 同星座=原色；跨星座=中性色或渐变
    function gateStrokePaint(link, index) {
      if (!link || Number(link.link_type) !== 2) return null;
      if (isCrossConstellationLink(link)) {
        if (CROSS_CONSTELLATION_GATE_STYLE === 'neutral') return CROSS_CONSTELLATION_NEUTRAL_COLOR;
        return `url(#gate-gradient-${index})`;
      }
      return linkColor(link);
    }

    // 返回适合的发光滤镜 id（针对星门）：被选中星座使用强发光
    function linkFilter(link) {
      if (!link) return null;
      if (Number(link.link_type) !== 2) return null;
      const c1 = findConstellationBySystem(link.from_id);
      const c2 = findConstellationBySystem(link.to_id);
      const selected = (c) => c && selectedConstellationIds.value.includes(c.id);
      return selected(c1) || selected(c2) ? 'url(#glow-strong)' : 'url(#glow)';
    }

    // 返回用于描边的可见颜色：对深色进行一定亮化处理
    function linkStrokeColor(link) {
      const base = linkColor(link);
      if (!base) return null;
      // 计算亮化：对深色向白色 lerp 30%
      return lerpColor(base, '#ffffff', 0.32);
    }

    function constellationLinks(constellation) {
      if (!constellation || !Array.isArray(constellation.systemIds)) return [];
      const set = new Set(constellation.systemIds);
      return links.value
        .filter((l) => set.has(l.from_id) && set.has(l.to_id))
        .map((l) => ({
          x1: systemById(l.from_id).x,
          y1: systemById(l.from_id).y,
          x2: systemById(l.to_id).x,
          y2: systemById(l.to_id).y,
          k: `${l.from_id}-${l.to_id}`,
        }));
    }

    function _addCell(map, gx, gy) {
      const k = `${gx},${gy}`;
      if (!map.has(k)) {
        map.set(k, { gx, gy, x: gx * GRID_SIZE, y: gy * GRID_SIZE, k });
      }
    }

    // 将一条线段离散到网格格子，避免用包围盒导致区域异常放大
    function _rasterizeLineToCells(ax, ay, bx, by) {
      const out = new Map();
      const dist = Math.hypot(bx - ax, by - ay);
      const step = Math.max(1, Math.ceil(dist / (GRID_SIZE * 0.45)));
      for (let i = 0; i <= step; i += 1) {
        const t = i / step;
        const x = ax + (bx - ax) * t;
        const y = ay + (by - ay) * t;
        const gx = Math.floor(x / GRID_SIZE);
        const gy = Math.floor(y / GRID_SIZE);
        _addCell(out, gx, gy);
      }
      return Array.from(out.values());
    }

    // 轻度扩张占格，保证断续线段不会出现可见裂缝
    function _expandCellSet(cells, radius) {
      const out = new Map();
      cells.forEach((c) => _addCell(out, c.gx, c.gy));
      if (radius <= 0) return Array.from(out.values());
      const base = Array.from(out.values());
      for (const c of base) {
        for (let dx = -radius; dx <= radius; dx += 1) {
          for (let dy = -radius; dy <= radius; dy += 1) {
            _addCell(out, c.gx + dx, c.gy + dy);
          }
        }
      }
      return Array.from(out.values());
    }

    function constellationCells(constellation) {
      if (!constellation || !Array.isArray(constellation.systemIds) || constellation.systemIds.length === 0) return [];
      const idSet = new Set(constellation.systemIds);
      const occupied = new Map();

      // 1) 星系所在格
      constellation.systemIds.forEach((sid) => {
        const s = systemById(sid);
        if (!Number.isFinite(s.x) || !Number.isFinite(s.y)) return;
        _addCell(occupied, Math.floor(s.x / GRID_SIZE), Math.floor(s.y / GRID_SIZE));
      });

      // 2) 星座内部连线经过的格子
      links.value
        .filter((l) => idSet.has(l.from_id) && idSet.has(l.to_id))
        .forEach((l) => {
          const a = systemById(l.from_id);
          const b = systemById(l.to_id);
          if (!Number.isFinite(a.x) || !Number.isFinite(b.x)) return;
          _rasterizeLineToCells(a.x, a.y, b.x, b.y).forEach((c) => _addCell(occupied, c.gx, c.gy));
        });

      // 3) 轻度扩展一圈，保持区域连续性但避免包围盒式膨胀
      return _expandCellSet(Array.from(occupied.values()), 1);
    }

    // 返回星域核心格子（所有下属星座的包围盒合并）
    function domainCoreCells(domain) {
      if (!domain || !Array.isArray(domain.constellationIds)) return [];
      const map = new Map();
      domain.constellationIds.forEach((cid) => {
        const c = constellations.value.find((x) => x.id === cid);
        if (!c) return;
        constellationCells(c).forEach((cell) => map.set(cell.k, cell));
      });
      return Array.from(map.values());
    }

    // 返回核心格子 + 向外扩展 ring 圈缓冲格子（不重叠）的并集
    function domainCellsWithBuffer(domain, ring) {
      const core = domainCoreCells(domain);
      if (!core.length) return [];

      // 邻接域保护：缓冲层不侵入其他星域核心区，避免半环形大面积覆盖冲突
      const otherCoreKeys = new Set();
      domains.value
        .filter((d) => d.id !== domain.id)
        .forEach((d) => {
          domainCoreCells(d).forEach((c) => otherCoreKeys.add(c.k));
        });

      const all = new Map();
      core.forEach((c) => all.set(c.k, { ...c, layer: 0 }));
      const dirs = [[1,0],[-1,0],[0,1],[0,-1]];
      let frontier = core.map((c) => ({ gx: c.gx, gy: c.gy }));
      for (let r = 1; r <= ring; r++) {
        const next = [];
        frontier.forEach((p) => {
          dirs.forEach(([dx, dy]) => {
            const gx = p.gx + dx, gy = p.gy + dy;
            const k = `${gx},${gy}`;
            if (!all.has(k)) {
              if (otherCoreKeys.has(k)) return;
              const cell = { gx, gy, x: gx * GRID_SIZE, y: gy * GRID_SIZE, k, layer: r };
              all.set(k, cell);
              next.push({ gx, gy });
            }
          });
        });
        frontier = next;
      }
      return Array.from(all.values());
    }

    // 公开接口：外部 template 需要用 domainCells
    function domainCells(domain) {
      return domainCellsWithBuffer(domain, 2);
    }

    function domainEdges(domain) {
      // 边描绘范围取整个（含缓冲）格子集的外边界
      const cells = domainCells(domain);
      const map = new Map();
      cells.forEach((c) => map.set(c.k, c));
      const edges = [];
      cells.forEach((c) => {
        const left = `${c.gx - 1},${c.gy}`;
        const right = `${c.gx + 1},${c.gy}`;
        const up = `${c.gx},${c.gy - 1}`;
        const down = `${c.gx},${c.gy + 1}`;
        if (!map.has(left))  edges.push({ x1: c.x,            y1: c.y,           x2: c.x,            y2: c.y + GRID_SIZE, k: `l-${c.k}` });
        if (!map.has(right)) edges.push({ x1: c.x + GRID_SIZE, y1: c.y,           x2: c.x + GRID_SIZE, y2: c.y + GRID_SIZE, k: `r-${c.k}` });
        if (!map.has(up))    edges.push({ x1: c.x,            y1: c.y,           x2: c.x + GRID_SIZE, y2: c.y,            k: `u-${c.k}` });
        if (!map.has(down))  edges.push({ x1: c.x,            y1: c.y + GRID_SIZE, x2: c.x + GRID_SIZE, y2: c.y + GRID_SIZE, k: `d-${c.k}` });
      });
      return edges;
    }

    // 每个格子的不透明度：核心格更深，缓冲格按层级衰减，但保证最低值
    function domainCellOpacity(cell, isSelected) {
      const MIN_OPACITY = 0.12;
      const base = isSelected ? 0.38 : 0.22;
      if (!cell || cell.layer === undefined) return base;
      if (cell.layer === 0) return base;
      // 每圈衰减 0.055，最低 MIN_OPACITY
      return Math.max(MIN_OPACITY, base - cell.layer * 0.055);
    }

    function constellationLabel(constellation) {
      if (!constellation || !Array.isArray(constellation.systemIds) || constellation.systemIds.length === 0) return null;
      const pts = constellation.systemIds
        .map((id) => systemById(id))
        .filter((s) => Number.isFinite(s.x) && Number.isFinite(s.y));
      if (!pts.length) return null;
      const x = pts.reduce((acc, p) => acc + p.x, 0) / pts.length;
      const y = pts.reduce((acc, p) => acc + p.y, 0) / pts.length;
      return { x, y };
    }

    // 颜色插值辅助（hex -> rgb -> lerp -> hex）
    function _hexToRgb(hex) {
      const h = hex.replace('#', '');
      const bigint = parseInt(h.length === 3 ? h.split('').map(c=>c+c).join('') : h, 16);
      return [(bigint >> 16) & 255, (bigint >> 8) & 255, bigint & 255];
    }
    function _rgbToHex(r, g, b) {
      return (
        '#' + [r, g, b].map((v) => Math.max(0, Math.min(255, Math.round(v))).toString(16).padStart(2, '0')).join('')
      );
    }
    function _lerp(a, b, t) {
      return a + (b - a) * t;
    }
    function lerpColor(hexA, hexB, t) {
      const ra = _hexToRgb(hexA), rb = _hexToRgb(hexB);
      return _rgbToHex(_lerp(ra[0], rb[0], t), _lerp(ra[1], rb[1], t), _lerp(ra[2], rb[2], t));
    }

    // 根据安全等级返回颜色（按要求映射）
    function systemColor(security) {
      const s = Number(security);
      if (!Number.isFinite(s)) return '#ecf4ff';
      // clamp to [-1,1]
      const v = Math.max(-1, Math.min(1, s));
      const blue = '#0077FF';
      const yellow = '#FFD54F';
      const red = '#FF3B30';
      // below 0.1 => red
      if (v <= 0.1) return red;
      // 0.1 .. 0.5 : red -> yellow (passes through orange visually)
      if (v <= 0.5) {
        const t = (v - 0.1) / 0.4; // 0..1
        return lerpColor(red, yellow, t);
      }
      // 0.5 .. 1.0 : yellow -> blue
      const t = (v - 0.5) / 0.5; // 0..1
      return lerpColor(yellow, blue, t);
    }

    function domainLabel(domain) {
      // 标签位置基于核心格重心，不受缓冲格干扰
      const cells = domainCoreCells(domain);
      if (!cells.length) return null;
      const x = cells.reduce((acc, c) => acc + c.x, 0) / cells.length + GRID_SIZE / 2;
      const y = cells.reduce((acc, c) => acc + c.y, 0) / cells.length + GRID_SIZE / 2;
      return { x, y };
    }

    function findDomainAt(wx, wy) {
      const gx = Math.floor(wx / GRID_SIZE);
      const gy = Math.floor(wy / GRID_SIZE);
      const key = `${gx},${gy}`;
      // 只在核心格内命中，不扩散到缓冲格
      for (let i = domains.value.length - 1; i >= 0; i -= 1) {
        const d = domains.value[i];
        const hit = domainCoreCells(d).some((c) => c.k === key);
        if (hit) return d;
      }
      return null;
    }

    // 点到线段的最短距离（世界坐标）
    function _ptSegDist(px, py, ax, ay, bx, by) {
      const dx = bx - ax, dy = by - ay;
      const len2 = dx * dx + dy * dy;
      if (len2 === 0) return Math.hypot(px - ax, py - ay);
      const t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
      return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    // 点击星门连线 → 返回所属星座（取连线任意端点的归属星座）
    function findConstellationByLinkAt(wx, wy) {
      // 阈值随缩放自适应，保持屏幕空间约 10px
      const threshold = 10 / zoom.value;
      for (const l of links.value) {
        const s1 = systemById(l.from_id);
        const s2 = systemById(l.to_id);
        if (!s1 || !s2) continue;
        if (!Number.isFinite(s1.x) || !Number.isFinite(s2.x)) continue;
        const d = _ptSegDist(wx, wy, s1.x, s1.y, s2.x, s2.y);
        if (d <= threshold) {
          const c = findConstellationBySystem(l.from_id) || findConstellationBySystem(l.to_id);
          if (c) return c;
        }
      }
      return null;
    }

    // 点击星座名称标签 → 返回所属星座（屏幕空间约 30px 半径）
    function findConstellationByLabelAt(wx, wy) {
      const threshold = 30 / zoom.value;
      for (const c of constellations.value) {
        const center = constellationLabel(c);
        if (!center) continue;
        if (Math.hypot(wx - center.x, wy - center.y) <= threshold) return c;
      }
      return null;
    }

    function onSecurityChange() {
      if (!selectedSystem.value) return;
      selectedSystem.value.security = clampSecurity(selectedSystem.value.security);
      emitState();
    }

    function openSystemDialog(x, y) {
      dialog.value.type = "system";
      dialog.value.title = "创建星系";
      dialog.value.form.name = `星系-${nextSystemId.value}`;
      dialog.value.form.security = 0.8;
      dialog.value.form.controller = "";
      dialog.value.form.description = "";
      dialog.value.pending.x = x;
      dialog.value.pending.y = y;
      dialog.value.open = true;
    }

    function openCreateConstellationDialog() {
      if (selectedSystemIds.value.length < 2) {
        showTip("请至少选中两个星系后再创建星座。");
        return;
      }
      const occupied = selectedSystemIds.value.filter((sid) =>
        constellations.value.some((c) => (c.systemIds || []).includes(sid))
      );
      if (occupied.length > 0) {
        showTip("有星系已属于其他星座，不能重复归属。");
        return;
      }
      dialog.value.type = "constellation";
      dialog.value.title = "创建星座";
      dialog.value.form.name = `星座-${nextConstellationId.value}`;
      dialog.value.form.controller = "";
      dialog.value.form.color = "#ffb74d";
      dialog.value.form.description = "";
      dialog.value.open = true;
    }

    function openCreateDomainDialog() {
      if (selectedConstellationIds.value.length === 0) {
        showTip("请先选中一个或多个星座。");
        return;
      }
      const occupied = selectedConstellationIds.value.filter((cid) =>
        domains.value.some((d) => (d.constellationIds || []).includes(cid))
      );
      if (occupied.length > 0) {
        showTip("有星座已属于其他星域，不能重复归属。");
        return;
      }
      dialog.value.type = "domain";
      dialog.value.title = "创建星域";
      dialog.value.form.name = `星域-${nextDomainId.value}`;
      dialog.value.form.controller = "";
      dialog.value.form.color = "rgba(72, 196, 255, 0.3)";
      dialog.value.form.description = "";
      dialog.value.open = true;
    }

    function closeDialog() {
      dialog.value.open = false;
    }

    function submitDialog() {
      const formName = (dialog.value.form.name || "").trim();
      if (!formName) {
        showTip("名称不能为空。");
        return;
      }

      if (dialog.value.type === "system") {
        const item = {
          id: nextSystemId.value,
          name: formName,
          x: dialog.value.pending.x,
          y: dialog.value.pending.y,
          security: clampSecurity(dialog.value.form.security),
          controller: dialog.value.form.controller || "",
          description: dialog.value.form.description || "",
        };
        nextSystemId.value += 1;
        systems.value.push(item);
        selectedSystemIds.value = [item.id];
        activeSystemId.value = item.id;
        ensureMapBounds(item.x, item.y);
      }

      if (dialog.value.type === "constellation") {
        const item = {
          id: nextConstellationId.value,
          name: formName,
          controller: dialog.value.form.controller || "",
          description: dialog.value.form.description || "",
          color: dialog.value.form.color || "#ffb74d",
          systemIds: [...selectedSystemIds.value],
        };
        nextConstellationId.value += 1;
        constellations.value.push(item);
        selectedConstellationIds.value = [item.id];
        activeConstellationId.value = item.id;
      }

      if (dialog.value.type === "domain") {
        const item = {
          id: nextDomainId.value,
          name: formName,
          controller: dialog.value.form.controller || "",
          description: dialog.value.form.description || "",
          color: dialog.value.form.color || "rgba(72, 196, 255, 0.3)",
          constellationIds: [...selectedConstellationIds.value],
        };
        nextDomainId.value += 1;
        domains.value.push(item);
        selectedDomainId.value = item.id;
      }

      emitState();
      closeDialog();
    }

    function toggleGateBetweenSelected() {
      if (!selectedGatePair.value) {
        showTip("请选中两个星系。");
        return;
      }
      const [a, b] = selectedGatePair.value;
      const idx = links.value.findIndex(
        (l) => Number(l.link_type) === 2 && ((l.from_id === a && l.to_id === b) || (l.from_id === b && l.to_id === a))
      );
      if (idx >= 0) {
        links.value.splice(idx, 1);
      } else {
        links.value.push({ from_id: a, to_id: b, link_type: 2, cost: 1 });
      }
      emitState();
    }

    function removeSelectedConstellations() {
      if (selectedConstellationIds.value.length === 0) return;
      const removeSet = new Set(selectedConstellationIds.value);
      constellations.value = constellations.value.filter((c) => !removeSet.has(c.id));
      domains.value.forEach((d) => {
        d.constellationIds = (d.constellationIds || []).filter((cid) => !removeSet.has(cid));
      });
      domains.value = domains.value.filter((d) => (d.constellationIds || []).length > 0);
      selectedConstellationIds.value = [];
      activeConstellationId.value = null;
      emitState();
    }

    // 解除已选星座与星域的绑定（从所属星域中移除已选星座）
    function unassignSelectedConstellationsFromDomains() {
      if (selectedConstellationIds.value.length === 0) return;
      const removeSet = new Set(selectedConstellationIds.value);
      domains.value.forEach((d) => {
        d.constellationIds = (d.constellationIds || []).filter((cid) => !removeSet.has(cid));
      });
      // 删除空的星域
      domains.value = domains.value.filter((d) => (d.constellationIds || []).length > 0);
      // 如果当前选中的域被清空，取消选中
      if (selectedDomainId.value && !domains.value.some((dd) => dd.id === selectedDomainId.value)) selectedDomainId.value = null;
      emitState();
    }

    function removeSelectedDomain() {
      if (!selectedDomainId.value) return;
      domains.value = domains.value.filter((d) => d.id !== selectedDomainId.value);
      selectedDomainId.value = null;
      emitState();
    }

    function clearSelections() {
      selectedSystemIds.value = [];
      selectedConstellationIds.value = [];
      selectedDomainId.value = null;
      activeSystemId.value = null;
      activeConstellationId.value = null;
    }

    // 解除已选星系与其所属星座的绑定（把选中星系从其星座中移除）
    function unassignSelectedSystemsFromConstellations() {
      if (selectedSystemIds.value.length === 0) return;
      const removeSet = new Set(selectedSystemIds.value);
      constellations.value.forEach((c) => {
        c.systemIds = (c.systemIds || []).filter((sid) => !removeSet.has(sid));
      });
      // 删除空的星座
      constellations.value = constellations.value.filter((c) => (c.systemIds || []).length > 0);
      // 清理 domains 中可能引用的已被删除星座
      domains.value.forEach((d) => {
        d.constellationIds = (d.constellationIds || []).filter((cid) => constellations.value.some((cc) => cc.id === cid));
      });
      domains.value = domains.value.filter((d) => (d.constellationIds || []).length > 0);
      selectedSystemIds.value = [];
      emitState();
    }

    // 删除已选星系：移除连线、从星座中移除、删除星系自身
    function removeSelectedSystems() {
      if (selectedSystemIds.value.length === 0) return;
      const removeSet = new Set(selectedSystemIds.value);
      // 移除连线
      links.value = links.value.filter((l) => !removeSet.has(Number(l.from_id)) && !removeSet.has(Number(l.to_id)));
      // 从星座中移除
      constellations.value.forEach((c) => {
        c.systemIds = (c.systemIds || []).filter((sid) => !removeSet.has(sid));
      });
      // 删除空的星座
      constellations.value = constellations.value.filter((c) => (c.systemIds || []).length > 0);
      // 从 star 列表中删除
      systems.value = systems.value.filter((s) => !removeSet.has(s.id));
      // 清理 domains 的 constellation 引用（如相关星座被删除）
      domains.value.forEach((d) => {
        d.constellationIds = (d.constellationIds || []).filter((cid) => constellations.value.some((cc) => cc.id === cid));
      });
      domains.value = domains.value.filter((d) => (d.constellationIds || []).length > 0);
      // 清空选中
      selectedSystemIds.value = [];
      activeSystemId.value = null;
      emitState();
    }

    // 长按阈值 (ms) —— 超过此时间才激活拖拽，否则视为单击
    const LONG_PRESS_MS = 220;
    let _longPressTimer = null;

    function onPointerDown(clientX, clientY, button) {
      const world = screenToWorld(clientX, clientY);
      const hit = findSystemAt(world.x, world.y);
      pointerState.value = {
        button,
        startClientX: clientX,
        startClientY: clientY,
        startWorldX: world.x,
        startWorldY: world.y,
        moved: false,
        hitId: hit ? hit.id : null,
        // 长按后才拖动：初始设 false，定时器到期后改 true
        dragReady: false,
      };

      if (button === 1 || button === 2) return;

      // 中/右键立即平移不需要等待
      if (hit && selectionMode.value === "systems") {
        // 长按才激活拖拽
        _longPressTimer = setTimeout(() => {
          if (pointerState.value && !pointerState.value.moved) {
            pointerState.value.dragReady = true;
            draggingSystemId.value = hit.id;
          }
        }, LONG_PRESS_MS);
      }

      if (selectionMode.value === "systems" && !hit) {
        selectionRect.value = { x: world.x, y: world.y, w: 0, h: 0 };
      }
    }

    function onPointerMove(clientX, clientY) {
      if (!pointerState.value) return;
      const dx = clientX - pointerState.value.startClientX;
      const dy = clientY - pointerState.value.startClientY;
      const dist = Math.hypot(dx, dy);
      if (dist > 3) pointerState.value.moved = true;

      if (pointerState.value.button === 1 || pointerState.value.button === 2) {
        panX.value += dx;
        panY.value += dy;
        pointerState.value.startClientX = clientX;
        pointerState.value.startClientY = clientY;
        return;
      }

      const world = screenToWorld(clientX, clientY);

      // 框选矩形更新（未命中星系时的拖拽）
      if (selectionRect.value && selectionMode.value === "systems" && !pointerState.value.hitId) {
        const ox = pointerState.value.startWorldX;
        const oy = pointerState.value.startWorldY;
        selectionRect.value = {
          x: Math.min(ox, world.x),
          y: Math.min(oy, world.y),
          w: Math.abs(ox - world.x),
          h: Math.abs(oy - world.y),
        };
        return;
      }

      // 拖动星系（长按后才激活）
      if (draggingSystemId.value) {
        const s = systems.value.find((item) => item.id === draggingSystemId.value);
        if (!s) return;
        const snap = snapToGrid(world.x, world.y);
        s.x = Math.max(0, snap.x);
        s.y = Math.max(0, snap.y);
        ensureMapBounds(s.x, s.y);
      }
    }

    function onPointerUp(clientX, clientY) {
      // 清掉长按计时器
      if (_longPressTimer !== null) {
        clearTimeout(_longPressTimer);
        _longPressTimer = null;
      }
      if (!pointerState.value) return;

      const moved = pointerState.value.moved;
      const wasDragging = !!draggingSystemId.value;
      const world = screenToWorld(clientX, clientY);
      const hit = findSystemAt(world.x, world.y);

      if (!wasDragging) {
        // 框选处理（仅当起点无星系时）
        if (selectionRect.value && selectionMode.value === "systems" && !pointerState.value.hitId) {
          const rectIsDrag = selectionRect.value.w > 6 || selectionRect.value.h > 6;
          if (rectIsDrag) {
            selectedSystemIds.value = systems.value
              .filter(
                (s) =>
                  s.x >= selectionRect.value.x &&
                  s.x <= selectionRect.value.x + selectionRect.value.w &&
                  s.y >= selectionRect.value.y &&
                  s.y <= selectionRect.value.y + selectionRect.value.h
              )
              .map((s) => s.id);
            activeSystemId.value = selectedSystemIds.value[selectedSystemIds.value.length - 1] || null;
          } else if (!moved) {
            // 单击空白 → 创建星系
            const snapPt = snapToGrid(world.x, world.y);
            openSystemDialog(Math.max(0, snapPt.x), Math.max(0, snapPt.y));
          }
        } else if (!moved) {
          // 单击行为
          if (selectionMode.value === "systems") {
            if (hit) {
              toggleSystemSelection(hit.id);
            } else {
              const snapPt = snapToGrid(world.x, world.y);
              openSystemDialog(Math.max(0, snapPt.x), Math.max(0, snapPt.y));
            }
          } else {
            // 星座选择模式
            if (hit) {
              const c = findConstellationBySystem(hit.id);
              if (c) {
                toggleConstellationSelection(c.id);
              } else {
                showTip("该星系尚未归属任何星座。");
              }
            } else {
              // 依次尝试：星门连线 → 星座标签 → 星域
              const cByLink = findConstellationByLinkAt(world.x, world.y);
              if (cByLink) {
                toggleConstellationSelection(cByLink.id);
              } else {
                const cByLabel = findConstellationByLabelAt(world.x, world.y);
                if (cByLabel) {
                  toggleConstellationSelection(cByLabel.id);
                } else {
                  const d = findDomainAt(world.x, world.y);
                  selectedDomainId.value = d ? d.id : null;
                }
              }
            }
          }
        }
      }

      if (wasDragging) emitState();
      draggingSystemId.value = null;
      selectionRect.value = null;
      pointerState.value = null;
    }

    function onMouseDown(e) {
      onPointerDown(e.clientX, e.clientY, e.button);
    }

    function onMouseMove(e) {
      onPointerMove(e.clientX, e.clientY);
    }

    function onMouseUp(e) {
      onPointerUp(e.clientX, e.clientY);
    }

    function getSingleTouch(event) {
      if (!event || !event.touches || event.touches.length === 0) return null;
      return event.touches[0];
    }

    function onTouchStart(event) {
      const t = getSingleTouch(event);
      if (!t) return;
      onPointerDown(t.clientX, t.clientY, 0);
    }

    function onTouchMove(event) {
      const t = getSingleTouch(event);
      if (!t) return;
      onPointerMove(t.clientX, t.clientY);
    }

    function onTouchEnd(event) {
      const t = event && event.changedTouches && event.changedTouches[0];
      if (!t) return;
      onPointerUp(t.clientX, t.clientY);
    }

    socket.on("connect", () => {
      socketConnected.value = true;
      // 建连后请求最新状态
      socket.emit("request_state");
    });

    socket.on("disconnect", () => {
      socketConnected.value = false;
    });

    socket.on("state", (data) => {
      loadState(data);
    });

    socket.on("saved", () => {
      showTip("已保存", "success");
    });

    socket.on("save_error", (data) => {
      showTip(`服务器保存失败: ${data.message}`, "error");
    });

    onMounted(() => {
      socket.connect();
    });

    return {
      gridSize,
      mapWidth,
      mapHeight,
      zoom,
      ZOOM_MIN,
      ZOOM_MAX,
      zoomPercent,
      selectionMode,
      modeHint,
      systems,
      links,
      constellations,
      domains,
      selectedSystemIds,
      selectedConstellationIds,
      selectedDomainId,
      activeSystemId,
      selectedSystem,
      selectedConstellation,
      selectedDomain,
      gridColumnCount,
      gridRowCount,
      viewTransform,
      svgRef,
      selectionRect,
      visibleLabels,
      canToggleGate,
      hasGateBetweenSelected,
      systemById,
      findConstellationBySystem,
      linkColor,
      linkFilter,
      linkStrokeColor,
      gateEndpointColor,
      gateStrokePaint,
      systemClass,
      systemColor,
      constellationLinks,
      domainCells,
      domainEdges,
      domainCellOpacity,
      setSelectionMode,
      emitState,
      emitStateDebounced,
      save,
      exportJson,
      stepZoom,
      setZoomByPercent,
      expandMap,
      toggleGateBetweenSelected,
      openCreateConstellationDialog,
      openCreateDomainDialog,
      removeSelectedConstellations,
      removeSelectedDomain,
      clearSelections,
      unassignSelectedConstellationsFromDomains,
      unassignSelectedSystemsFromConstellations,
      removeSelectedSystems,
      onSecurityChange,
      onMouseDown,
      onMouseMove,
      onMouseUp,
      onTouchStart,
      onTouchMove,
      onTouchEnd,
      socketConnected,
      dialog,
      snackbar,
      closeDialog,
      submitDialog,
    };
  },
})
  .use(vuetify)
  .mount("#app");