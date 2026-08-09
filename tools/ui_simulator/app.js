"use strict";

const $ = id => document.getElementById(id);
const canvas = $("ui");
const ctx = canvas.getContext("2d", {alpha: false});
ctx.imageSmoothingEnabled = false;
let data, fontImage, machineTexture, menuImage, menuLogo, menuButton, questHomeImage, stamp;
const questImages = new Map();
const issues = [];
const glyphCache = new Map();

function image(url) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error(`Unable to load simulator asset: ${url}`));
    img.src = url + "?t=" + Date.now();
  });
}

function minecraftScale(displayWidth, displayHeight, requested) {
  let factor = 1;
  const target = requested === 0 ? 1000 : requested;
  while (factor < target && displayWidth / (factor + 1) >= 320 && displayHeight / (factor + 1) >= 240) factor++;
  return factor;
}

function dimensions() {
  const [displayWidth, displayHeight] = $("display").value.split("x").map(Number);
  const factor = minecraftScale(displayWidth, displayHeight, Number($("scale").value));
  return {displayWidth, displayHeight, factor,
    width: Math.ceil(displayWidth / factor), height: Math.ceil(displayHeight / factor)};
}

function glyphMetrics(ch) {
  if (glyphCache.has(ch)) return glyphCache.get(ch);
  const code = ch.charCodeAt(0);
  if (ch === " ") { const value = {sx: 0, sy: 0, width: 4}; glyphCache.set(ch, value); return value; }
  if (code < 0 || code > 255) { const value = {sx: 0, sy: 0, width: 6, fallback: true}; glyphCache.set(ch, value); return value; }
  const sx = (code % 16) * 8, sy = Math.floor(code / 16) * 8;
  const scratch = glyphMetrics.scratch || (glyphMetrics.scratch = document.createElement("canvas"));
  scratch.width = scratch.height = 8;
  const sc = scratch.getContext("2d", {willReadFrequently: true});
  sc.clearRect(0, 0, 8, 8); sc.drawImage(fontImage, sx, sy, 8, 8, 0, 0, 8, 8);
  const pixels = sc.getImageData(0, 0, 8, 8).data;
  let right = 0;
  for (let x = 0; x < 8; x++) for (let y = 0; y < 8; y++) if (pixels[(y * 8 + x) * 4 + 3] > 16) right = Math.max(right, x);
  const value = {sx, sy, width: Math.min(8, Math.max(2, right + 2))};
  glyphCache.set(ch, value);
  return value;
}

function textWidth(text) {
  return [...String(text).replace(/§./g, "")].reduce((sum, ch) => sum + glyphMetrics(ch).width, 0);
}

function drawText(text, x, y, color = "#ffffff", scale = 1) {
  const clean = String(text).replace(/§./g, "");
  let dx = Math.round(x);
  ctx.save(); ctx.imageSmoothingEnabled = false;
  const glyphCanvas = drawText.scratch || (drawText.scratch = document.createElement("canvas"));
  glyphCanvas.width = glyphCanvas.height = 8;
  const glyphContext = glyphCanvas.getContext("2d");
  for (const ch of clean) {
    const g = glyphMetrics(ch);
    if (g.fallback) {
      ctx.fillStyle = color; ctx.font = `${8 * scale}px monospace`; ctx.fillText(ch, dx, y + 7 * scale);
    } else if (ch !== " ") {
      glyphContext.clearRect(0, 0, 8, 8);
      glyphContext.globalCompositeOperation = "source-over";
      glyphContext.drawImage(fontImage, g.sx, g.sy, 8, 8, 0, 0, 8, 8);
      glyphContext.globalCompositeOperation = "source-in";
      glyphContext.fillStyle = color;
      glyphContext.fillRect(0, 0, 8, 8);
      glyphContext.globalCompositeOperation = "source-over";
      // FontRenderer renders only charWidth - 1 source pixels, stretched across
      // charWidth display pixels. Drawing the whole 8-pixel cell causes the
      // next glyph to overwrite it and is why the first simulator build looked
      // like every word had been crushed together.
      const sourceWidth = Math.max(1, g.width - 1);
      ctx.drawImage(glyphCanvas, 0, 0, sourceWidth, 8,
        dx, Math.round(y), g.width * scale, 8 * scale);
    }
    dx += g.width * scale;
  }
  ctx.restore();
  return dx;
}

function centered(text, centerX, y, color = "#fff", scale = 1) {
  drawText(text, centerX - textWidth(text) * scale / 2, y, color, scale);
}

function trim(text, maxWidth) {
  let out = "";
  for (const ch of text) { if (textWidth(out + ch) > maxWidth) break; out += ch; }
  return out;
}

function wrap(text, maxWidth) {
  const lines = [];
  for (const paragraph of String(text).split("\n")) {
    let line = "";
    for (const word of paragraph.split(/\s+/)) {
      const candidate = line ? line + " " + word : word;
      if (line && textWidth(candidate) > maxWidth) { lines.push(line); line = word; }
      else line = candidate;
    }
    lines.push(line);
  }
  return lines;
}

function rect(x, y, w, h, color, stroke) {
  ctx.fillStyle = color; ctx.fillRect(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
  if (stroke) { ctx.strokeStyle = stroke; ctx.strokeRect(Math.round(x) + .5, Math.round(y) + .5, Math.round(w) - 1, Math.round(h) - 1); }
}

function button(x, y, w, h, label, enabled = true) {
  rect(x, y, w, h, enabled ? "#777" : "#444", "#111");
  rect(x + 1, y + 1, w - 2, 1, enabled ? "#ddd" : "#666");
  centered(trim(label, w - 8), x + w / 2, y + Math.max(2, (h - 8) / 2), enabled ? "#fff" : "#999");
}

function worldBackground(width, height, dim = true) {
  const gradient = ctx.createLinearGradient(0, 0, 0, height);
  gradient.addColorStop(0, "#608bc0"); gradient.addColorStop(.55, "#8aabca"); gradient.addColorStop(.56, "#466d35"); gradient.addColorStop(1, "#243f25");
  ctx.fillStyle = gradient; ctx.fillRect(0, 0, width, height);
  for (let x = 0; x < width; x += 24) rect(x, height * .62 + (x % 72) / 6, 22, height * .38, x % 48 ? "#31562c" : "#3c6232");
  if (dim) rect(0, 0, width, height, "rgba(0,0,0,.48)");
}

function issue(screen, message, box) { issues.push({screen, message, box}); }
function overlap(a, b) { return a.x < b.x + b.w && a.x + a.w > b.x && a.y < b.y + b.h && a.y + a.h > b.y; }
function contained(inner, outer) { return inner.x >= outer.x && inner.y >= outer.y && inner.x + inner.w <= outer.x + outer.w && inner.y + inner.h <= outer.y + outer.h; }

function compactNumber(value) {
  const suffixes = ["", "k", "M", "G"];
  let scaled = value, suffix = 0;
  while (Math.abs(scaled) >= 999.5 && suffix < suffixes.length - 1) { scaled /= 1000; suffix++; }
  const nearest = Math.round(scaled);
  return Math.abs(scaled) < 10 && Math.abs(scaled - nearest) >= .05
    ? scaled.toFixed(1) + suffixes[suffix] : String(nearest) + suffixes[suffix];
}

function renderHeiPanel(x, y, w, h, label, populated) {
  rect(x, y, w, h, "rgba(35,41,45,.92)", "#8b989b");
  centered(label, x + w / 2, y + 7, "#ddd");
  const searchHeight = 14;
  rect(x + 4, y + h - searchHeight - 4, w - 8, searchHeight, "#111719", "#9aa5a8");
  drawText(populated ? "Search..." : "No bookmarks", x + 8, y + h - searchHeight - 1, "#9aa5a8");
  if (!populated) return;
  const slot = 18, columns = Math.max(1, Math.floor((w - 8) / slot));
  const rows = Math.max(1, Math.floor((h - 42) / slot));
  const palette = ["#7f9ca4", "#c47f39", "#6e8f55", "#b8b7af", "#8d6ba8", "#a9554d", "#c4ae45"];
  for (let row = 0; row < rows; row++) for (let column = 0; column < columns; column++) {
    const sx = x + 4 + column * slot, sy = y + 22 + row * slot;
    rect(sx, sy, 16, 16, "#5a6062", "#aeb5b6");
    const color = palette[(row * columns + column) % palette.length];
    rect(sx + 4, sy + 4, 8, 8, color, "#263034");
  }
}

function anchoredBox(d, entry) {
  let x = entry.posX || 0, y = entry.posY || 0;
  const alignment = entry.alignment || "top_left";
  if (alignment.includes("center")) x += d.width / 2;
  else if (alignment.includes("right")) x += d.width;
  if (alignment.includes("bottom")) y += d.height;
  return {x, y, w: entry.width || 0, h: entry.height || 0};
}

function mainMenuLayout(d, auditScreen = "mainmenu") {
  const title = anchoredBox(d, data.mainMenu.images.title);
  const versionLabel = data.mainMenu.labels.industrialcivilization;
  const version = {x: versionLabel.posX || 0, y: versionLabel.posY || 0,
    w: textWidth(versionLabel.text), h: 8};
  const buttons = Object.entries(data.mainMenu.buttons)
    .filter(([id]) => id !== "replay" && id !== "modslabel" && id !== "speaker")
    .map(([id, entry]) => ({id, entry, box: anchoredBox(d, entry)}));
  const viewport = {x: 0, y: 0, w: d.width, h: d.height};
  if (!contained(title, viewport)) issue(auditScreen, "Main-menu logo clips the viewport", title);
  if (overlap(title, version)) issue(auditScreen, "Main-menu version label overlaps the logo", version);
  for (const buttonEntry of buttons) {
    if (!contained(buttonEntry.box, viewport)) issue(auditScreen, `Main-menu ${buttonEntry.id} button clips the viewport`, buttonEntry.box);
    if (overlap(title, buttonEntry.box)) issue(auditScreen, `Main-menu logo overlaps the ${buttonEntry.id} button`, buttonEntry.box);
  }
  for (let i = 0; i < buttons.length; i++) for (let j = i + 1; j < buttons.length; j++) {
    if (overlap(buttons[i].box, buttons[j].box)) issue(auditScreen, `Main-menu ${buttons[i].id} and ${buttons[j].id} buttons overlap`, buttons[i].box);
  }
  return {title, buttons, version};
}

function renderMainMenu(d, auditScreen = "mainmenu") {
  ctx.drawImage(menuImage, 0, 0, d.width, d.height);
  const layout = mainMenuLayout(d, auditScreen);
  ctx.drawImage(menuLogo, layout.title.x, layout.title.y, layout.title.w, layout.title.h);
  drawText(data.mainMenu.labels.industrialcivilization.text,
    layout.version.x, layout.version.y, "#fff");
  const labels = {singleplayer: "Singleplayer", multiplayer: "Multiplayer", options: "Options...", quit: "Quit Game", mods: "Loaded Mods", languagebutton: "Language..."};
  for (const {id, box} of layout.buttons) {
    ctx.drawImage(menuButton, 0, 0, 200, 20, box.x, box.y, box.w, box.h);
    centered(labels[id] || id, box.x + box.w / 2, box.y + Math.max(2, (box.h - 8) / 2), "#fff");
  }
  drawText("192 mods loaded and active", 2, d.height - 9, "#fff");
  const copyright = "Copyright Mojang AB. Do not distribute!";
  drawText(copyright, d.width - textWidth(copyright) - 2, d.height - 9, "#fff");
}

function pauseLayout(d, auditScreen = "pause") {
  const width = Math.min(200, d.width - 24), x = (d.width - width) / 2;
  const start = Math.max(30, d.height / 2 - 92), step = 24;
  const rows = [
    {label: "Back to Game", x, y: start, w: width, h: 20},
    {label: "Advancements", x, y: start + step, w: width, h: 20},
    {label: "Factions & Settlements", x, y: start + step * 2, w: width, h: 20},
    {label: "Open to LAN", x, y: start + step * 3, w: width, h: 20},
    {label: "Options...", x, y: start + step * 4, w: (width - 4) / 2, h: 20},
    {label: "Mod Options...", x: x + (width + 4) / 2, y: start + step * 4, w: (width - 4) / 2, h: 20},
    {label: "Save and Quit to Title", x, y: start + step * 5, w: width, h: 20},
  ];
  const viewport = {x: 0, y: 0, w: d.width, h: d.height};
  for (const row of rows) {
    if (!contained(row, viewport)) issue(auditScreen, `Pause-menu ${row.label} button clips the viewport`, row);
    if (textWidth(row.label) > row.w - 8) issue(auditScreen, `Pause-menu ${row.label} text does not fit`, row);
  }
  return rows;
}

function renderPause(d, auditScreen = "pause") {
  worldBackground(d.width, d.height, true);
  centered("Game Menu", d.width / 2, Math.max(8, d.height / 2 - 116), "#fff");
  pauseLayout(d, auditScreen).forEach(row => button(row.x, row.y, row.w, row.h, row.label));
}

function renderAdvancements(d, auditScreen = "advancements") {
  worldBackground(d.width, d.height, true);
  const panel = {x: Math.max(8, (d.width - Math.min(400, d.width - 16)) / 2), y: Math.max(20, (d.height - Math.min(240, d.height - 40)) / 2), w: Math.min(400, d.width - 16), h: Math.min(240, d.height - 40)};
  rect(panel.x, panel.y, panel.w, panel.h, "#c6c6c6", "#202020");
  rect(panel.x + 7, panel.y + 24, panel.w - 14, panel.h - 32, "#332f2a", "#fff");
  button(panel.x + 8, panel.y - 18, Math.min(132, panel.w - 16), 20, "Industrial Civilization");
  centered("Industrial Civilization Advancements", panel.x + panel.w / 2, panel.y + 8, "#303030");
  const nodes = [[.18,.50],[.38,.50],[.58,.38],[.58,.62],[.78,.50]];
  ctx.strokeStyle = "#a9b5b4"; ctx.lineWidth = 2;
  for (let index = 1; index < nodes.length; index++) { ctx.beginPath(); ctx.moveTo(panel.x + 12 + nodes[index - 1][0] * (panel.w - 24), panel.y + 30 + nodes[index - 1][1] * (panel.h - 44)); ctx.lineTo(panel.x + 12 + nodes[index][0] * (panel.w - 24), panel.y + 30 + nodes[index][1] * (panel.h - 44)); ctx.stroke(); }
  nodes.forEach((node, index) => rect(panel.x + node[0] * (panel.w - 24), panel.y + 26 + node[1] * (panel.h - 44), 18, 18, index ? "#737a7b" : "#c7a44b", "#eee"));
  if (!contained(panel, {x:0,y:0,w:d.width,h:d.height})) issue(auditScreen, "Advancements panel clips viewport", panel);
}

function renderQuestHome(d, auditScreen = "questhome") {
  worldBackground(d.width, d.height, true);
  const panel = {x: 8, y: 8, w: d.width - 16, h: d.height - 16};
  rect(panel.x, panel.y, panel.w, panel.h, "#c6c6c6", "#202020");
  const buttonHeight = 24, imageBox = {x: panel.x + 6, y: panel.y + 6, w: panel.w - 12, h: panel.h - buttonHeight - 14};
  const scale = Math.max(imageBox.w / questHomeImage.width, imageBox.h / questHomeImage.height);
  const iw = questHomeImage.width * scale, ih = questHomeImage.height * scale;
  ctx.save(); ctx.beginPath(); ctx.rect(imageBox.x, imageBox.y, imageBox.w, imageBox.h); ctx.clip();
  ctx.drawImage(questHomeImage, imageBox.x + (imageBox.w - iw) / 2, imageBox.y + (imageBox.h - ih) / 2, iw, ih); ctx.restore();
  const labels = ["Exit", "Quests", "Party", "Theme"], bw = (panel.w - 12) / labels.length;
  labels.forEach((label, index) => button(panel.x + 6 + index * bw, panel.y + panel.h - buttonHeight - 5, bw, buttonHeight, label));
  if (imageBox.w < 100 || imageBox.h < 80) issue(auditScreen, "Quest-home artwork viewport is too small", imageBox);
}

function renderSpaceMap(d, auditScreen = "spacemap") {
  rect(0, 0, d.width, d.height, "#03050b");
  for (let i = 0; i < 100; i++) rect((i * 83) % d.width, (i * 47) % d.height, 1, 1, i % 4 ? "#777" : "#fff");
  centered("Industrial Civilization Space Program", d.width / 2, 8, "#fff");
  const bodies = [{name:"Earth",x:.22,color:"#4b7cb5",allowed:true},{name:"Moon",x:.50,color:"#b8b8ac",allowed:true},{name:"Mars",x:.78,color:"#b45d3d",allowed:false}];
  bodies.forEach(body => { const x=d.width*body.x,y=d.height*.48; ctx.beginPath();ctx.arc(x,y,Math.max(12,d.width*.035),0,Math.PI*2);ctx.fillStyle=body.color;ctx.fill();centered(body.name,x,y+Math.max(17,d.width*.045),body.allowed?"#fff":"#777"); if(!body.allowed) centered("LOCKED",x,y-4,"#ff776d"); });
  const status = {x:d.width/2-100,y:d.height-30,w:200,h:20}; button(status.x,status.y,status.w,status.h,"Travel only to authorized bodies");
  if (!contained(status,{x:0,y:0,w:d.width,h:d.height})) issue(auditScreen,"Space-map status clips viewport",status);
}

function machineLayout(d, machine, energyPercent, operations, auditScreen = "machine") {
  const gui = {x: Math.floor((d.width - 176) / 2), y: Math.floor((d.height - 166) / 2), w: 176, h: 166};
  if (!contained(gui, {x: 0, y: 0, w: d.width, h: d.height})) issue(auditScreen, "176×166 machine panel does not fit the logical viewport", gui);
  const title = data.lang[`tile.industrialcivilizationcore.${machine.id}.name`] || machine.id;
  const shownTitle = trim(title, 136);
  const energy = Math.round(machine.capacity * energyPercent / 100);
  const energyText = `EU ${compactNumber(energy)}/${compactNumber(machine.capacity)}`;
  const operationsText = `Ops ${compactNumber(operations)}`;
  const energyBox = {x: 8, y: 63, w: textWidth(energyText), h: 9};
  const opsBox = {x: 168 - textWidth(operationsText), y: 63, w: textWidth(operationsText), h: 9};
  const statusRow = {x: 8, y: 63, w: 160, h: 9};
  const inventoryRow = {x: 8, y: 73, w: textWidth("Inventory"), h: 9};
  if (overlap(energyBox, opsBox)) issue(auditScreen, `${machine.id}: energy and operation labels overlap`, {x: gui.x + energyBox.x, y: gui.y + 63, w: 160, h: 9});
  if (overlap(statusRow, inventoryRow)) issue(auditScreen, `${machine.id}: status row vertically overlaps the inventory label`, {x: gui.x + 8, y: gui.y + 63, w: 160, h: 19});
  return {gui, title: shownTitle, titleX: 32 + (136 - textWidth(shownTitle)) / 2,
    energyText, operationsText, energyBox, opsBox, energy, progress: .62};
}

function renderMachine(d, machineOverride, auditScreen) {
  worldBackground(d.width, d.height, true);
  const machine = machineOverride || data.machines.find(m => m.id === $("machine").value) || data.machines[0];
  const state = machineLayout(d, machine, Number($("energy").value), Number($("operations").value), auditScreen);
  const {gui} = state;
  if ($("hei").checked) {
    const gap = 4;
    const leftW = Math.max(0, gui.x - gap), rightX = gui.x + gui.w + gap;
    if (leftW >= 64) renderHeiPanel(2, 18, leftW - 4, d.height - 36, "Bookmarks", false);
    if (d.width - rightX >= 64) renderHeiPanel(rightX, 18, d.width - rightX - 2, d.height - 36, "HEI Items", true);
  }
  ctx.drawImage(machineTexture, 0, 0, 176, 166, gui.x, gui.y, 176, 166);
  const energyHeight = Math.floor(48 * state.energy / machine.capacity);
  if (energyHeight > 0) ctx.drawImage(machineTexture, 176, 48 - energyHeight, 8, energyHeight, gui.x + 17, gui.y + 59 - energyHeight, 8, energyHeight);
  ctx.drawImage(machineTexture, 176, 49, Math.floor(24 * state.progress), 16, gui.x + 104, gui.y + 35, Math.floor(24 * state.progress), 16);
  drawText(state.title, gui.x + state.titleX, gui.y + 6, "#25333a");
  drawText(state.energyText, gui.x + state.energyBox.x, gui.y + 63, "#25333a");
  drawText(state.operationsText, gui.x + state.opsBox.x, gui.y + 63, "#25333a");
  drawText("Inventory", gui.x + 8, gui.y + 73, "#404b50");
}

function factionDetail(faction, width) {
  const lines = [];
  const add = (text, color) => wrap(text, Math.max(60, width)).forEach(value => lines.push({text: value, color}));
  add("Membership: Independent", "#9af8f4"); lines.push({text: "", color: "#fff"});
  add(faction.name, "#fff"); add(`Reputation: ${faction.reputation}   ${faction.reputation < 0 ? "GUARDED" : "NEUTRAL"}`, "#d9d2b4");
  add("Encountered: yes", "#aebcc1"); lines.push({text: "", color: "#fff"});
  add("Settlements: " + faction.settlements, "#d9d2b4"); lines.push({text: "", color: "#fff"});
  add("Trade specialties: " + faction.products, "#d9d2b4"); lines.push({text: "", color: "#fff"});
  add("Membership: " + faction.membership, "#d9d2b4"); lines.push({text: "", color: "#fff"});
  add("How to interact: normal right-click trades with IC Credits. Sneak-right-click requests membership. Members at 60 reputation can sneak-right-click while holding 8 IC Credits to recruit or dismiss a companion.", "#9af8f4");
  return lines;
}

function renderFactions(d, factionOverride, auditScreen = "factions") {
  worldBackground(d.width, d.height, true);
  const panelWidth = Math.max(1, Math.min(520, d.width - 16)), panelHeight = Math.max(1, Math.min(330, d.height - 16));
  const left = (d.width - panelWidth) / 2, top = (d.height - panelHeight) / 2;
  const panel = {x: left, y: top, w: panelWidth, h: panelHeight};
  const listWidth = Math.min(155, Math.max(82, panelWidth / 3)), divider = left + listWidth;
  const detailLeft = divider + 10, detailWidth = left + panelWidth - detailLeft - 10;
  if (detailWidth < 60) issue(auditScreen, `Faction detail column is only ${Math.round(detailWidth)} px`, panel);
  rect(left, top, panelWidth, panelHeight, "rgba(24,36,42,.93)"); rect(divider, top + 24, 2, panelHeight - 31, "#71858b");
  centered(data.lang["gui.industrialcivilization.factions"], d.width / 2, top + 8, "#e5f0ef");
  const available = Math.max(90, panelHeight - 62), buttonHeight = Math.min(20, Math.max(10, (available - (data.factions.length - 1) * 3) / data.factions.length));
  data.factions.forEach((faction, index) => button(left + 6, top + 28 + index * (buttonHeight + 3), listWidth - 12, buttonHeight, faction.name));
  button(left + panelWidth - 45, top + 25, 18, 16, "^"); button(left + panelWidth - 25, top + 25, 18, 16, "v");
  const doneWidth = Math.max(60, Math.min(100, panelWidth - 12)); button(left + panelWidth - doneWidth - 6, top + panelHeight - 27, doneWidth, 20, "Done");
  const faction = factionOverride || data.factions.find(f => f.id === $("faction").value) || data.factions[0];
  const lines = factionDetail(faction, detailWidth), maxLines = Math.max(1, Math.floor((top + panelHeight - 34 - (top + 43)) / 11));
  lines.slice(0, maxLines).forEach((line, index) => drawText(line.text, detailLeft, top + 43 + index * 11, line.color));
}

function auditFactionLayout(d, faction, auditScreen) {
  const panelWidth = Math.max(1, Math.min(520, d.width - 16));
  const panelHeight = Math.max(1, Math.min(330, d.height - 16));
  const panel = {x: (d.width - panelWidth) / 2, y: (d.height - panelHeight) / 2, w: panelWidth, h: panelHeight};
  const listWidth = Math.min(155, Math.max(82, panelWidth / 3));
  const detailWidth = panelWidth - listWidth - 20;
  if (!contained(panel, {x: 0, y: 0, w: d.width, h: d.height})) issue(auditScreen, "Faction panel clips viewport", panel);
  if (detailWidth < 60) issue(auditScreen, `Faction detail column is only ${Math.round(detailWidth)} px`, panel);
  const available = Math.max(90, panelHeight - 62);
  const buttonHeight = Math.min(20, Math.max(10, (available - (data.factions.length - 1) * 3) / data.factions.length));
  if (28 + data.factions.length * buttonHeight + (data.factions.length - 1) * 3 > panelHeight - 27)
    issue(auditScreen, `${faction.name}: faction buttons collide with Done`, panel);
  factionDetail(faction, detailWidth); // Exercise exact wrapping with the real Minecraft font.
}

function auditWarmupLayout(d, auditScreen) {
  const buttonWidth = Math.max(80, Math.min(150, d.width - 32));
  const buttonBox = {x: d.width / 2 - buttonWidth / 2, y: Math.min(d.height - 24, d.height / 2 + 42), w: buttonWidth, h: 20};
  if (!contained(buttonBox, {x: 0, y: 0, w: d.width, h: d.height})) issue(auditScreen, "Warmup button clips viewport", buttonBox);
}

function auditCreditsLayout(d, auditScreen) {
  const buttonWidth = Math.max(80, Math.min(200, d.width - 24));
  const box = {x: d.width / 2 - buttonWidth / 2, y: Math.max(4, d.height - 28), w: buttonWidth, h: 20};
  if (!contained(box, {x: 0, y: 0, w: d.width, h: d.height})) issue(auditScreen, "Credits Done button clips viewport", box);
}

function auditQuestLayout(d, line, auditScreen) {
  const sidebar = Math.min(150, Math.max(92, d.width * .28));
  const board = {x: sidebar + 4, y: 8, w: d.width - sidebar - 12, h: d.height - 16};
  if (board.w < 100 || board.h < 100) issue(auditScreen, "Quest canvas is too small to navigate", board);
  const coverScale = Math.max(board.w / line.backgroundSize, board.h / line.backgroundSize);
  const size = line.backgroundSize * coverScale;
  if (size + .01 < board.w || size + .01 < board.h) issue(auditScreen, "Quest background can expose an empty edge", board);
  for (const node of line.nodes) {
    if (node.x < 0 || node.y < 0 || node.x + node.w > line.backgroundSize || node.y + node.h > line.backgroundSize)
      issue(auditScreen, `${line.name}: quest ${node.id} lies outside its background`, node);
  }
}

function renderWarmup(d, auditScreen = "warmup") {
  worldBackground(d.width, d.height, true);
  const contentWidth = Math.max(80, Math.min(200, d.width - 32)), cx = d.width / 2;
  wrap(data.lang["gui.industrialcivilization.terrain.preparing"], contentWidth).forEach((line, i) => centered(line, cx, d.height / 2 - 48 + i * 11, "#e5f0ef"));
  wrap("Loaded nearby chunks: 67 / 81 — finishing world initialization", contentWidth).forEach((line, i) => centered(line, cx, d.height / 2 - 25 + i * 11, "#a9c9c6"));
  const bar = {x: cx - contentWidth / 2, y: d.height / 2, w: contentWidth, h: 12}; rect(bar.x, bar.y, bar.w, bar.h, "#10191d"); rect(bar.x + 2, bar.y + 2, (bar.w - 4) * 67 / 81, 8, "#5bc4b8");
  const buttonWidth = Math.max(80, Math.min(150, d.width - 32)), buttonY = Math.min(d.height - 24, d.height / 2 + 42);
  const buttonBox = {x: cx - buttonWidth / 2, y: buttonY, w: buttonWidth, h: 20}; button(buttonBox.x, buttonBox.y, buttonBox.w, buttonBox.h, data.lang["gui.industrialcivilization.terrain.enter_now"]);
  if (!contained(buttonBox, {x: 0, y: 0, w: d.width, h: d.height})) issue(auditScreen, "Warmup button clips viewport", buttonBox);
}

function renderCredits(d, auditScreen = "credits") {
  ctx.drawImage(menuImage, 0, 0, d.width, d.height); rect(0, 0, d.width, d.height, "rgba(0,0,0,.72)");
  let y = Math.max(12, d.height / 2 - 95);
  const line = (text, color, scale = 1) => { const lines = wrap(text, Math.max(40, (d.width - 24) / scale)); lines.forEach((part, i) => centered(part, d.width / 2, y + i * 11 * scale, color, scale)); y += Math.max(18 * scale, lines.length * 11 * scale); };
  line(data.lang["credits.industrialcivilization.title"], "#f0b35a", 2); y += 8;
  line(data.lang["credits.industrialcivilization.unlocked"], "#b8eaf2"); y += 8;
  line("Created by", "#c8c8c8"); line("corysmart", "#fff", 2); y += 8;
  line(data.lang["credits.industrialcivilization.foundation_detail"], "#fff");
  const buttonWidth = Math.max(80, Math.min(200, d.width - 24)); button(d.width / 2 - buttonWidth / 2, Math.max(4, d.height - 28), buttonWidth, 20, "Done");
}

async function questImage(name) {
  if (!questImages.has(name)) questImages.set(name, await image("/assets/quest/" + name));
  return questImages.get(name);
}

async function renderQuests(d, lineOverride, auditScreen = "quests") {
  worldBackground(d.width, d.height, true);
  const sidebar = Math.min(150, Math.max(92, d.width * .28)), board = {x: sidebar + 4, y: 8, w: d.width - sidebar - 12, h: d.height - 16};
  rect(4, 8, sidebar - 4, d.height - 16, "#c7c7c7", "#222"); rect(board.x, board.y, board.w, board.h, "#000", "#ddd");
  const line = lineOverride || data.questLines.find(q => String(q.id) === $("quest-line").value) || data.questLines[0];
  data.questLines.slice(0, Math.max(1, Math.floor((d.height - 30) / 18))).forEach((questLine, index) => button(8, 16 + index * 18, sidebar - 12, 16, questLine.name));
  const bg = await questImage(line.background), zoom = Number($("quest-zoom").value) / 100;
  // The backdrop covers the viewport at minimum zoom, so panning can never
  // reveal an empty edge. Higher zoom levels remain available for inspection.
  const fit = Math.max(board.w / line.backgroundSize, board.h / line.backgroundSize);
  const effective = Math.max(fit, fit * zoom);
  const size = line.backgroundSize * effective;
  const bx = board.x + (board.w - size) / 2, by = board.y + (board.h - size) / 2;
  // The image is always clamped to cover the viewport. No pan state can expose space beyond its edges.
  ctx.save(); ctx.beginPath(); ctx.rect(board.x, board.y, board.w, board.h); ctx.clip(); ctx.drawImage(bg, bx, by, size, size);
  const nodeScale = effective;
  const nodesById = new Map(line.nodes.map(node => [node.id, node]));
  ctx.strokeStyle = "#ddc87a"; ctx.lineWidth = Math.max(1, nodeScale * 2);
  line.nodes.forEach(node => node.prerequisites.forEach(parentId => { const parent = nodesById.get(parentId); if (!parent) return; ctx.beginPath(); ctx.moveTo(bx + (parent.x + parent.w / 2) * nodeScale, by + (parent.y + parent.h / 2) * nodeScale); ctx.lineTo(bx + (node.x + node.w / 2) * nodeScale, by + (node.y + node.h / 2) * nodeScale); ctx.stroke(); }));
  line.nodes.forEach(node => { const x = bx + node.x * nodeScale, y = by + node.y * nodeScale, w = node.w * nodeScale, h = node.h * nodeScale; rect(x, y, w, h, "#8a6c2f", "#fff2a0"); if (w >= 18) centered(String(node.id + 1), x + w / 2, y + Math.max(1, (h - 8) / 2), "#fff"); });
  ctx.restore();
  if (size + .01 < board.w || size + .01 < board.h) issue(auditScreen, "Quest background can expose an empty edge", board);
}

async function render() {
  if (!data) return;
  issues.length = 0;
  const d = dimensions(); canvas.width = d.width; canvas.height = d.height; ctx.imageSmoothingEnabled = false;
  const screen = $("screen").value;
  $("machine-controls").hidden = screen !== "machine"; $("faction-controls").hidden = screen !== "factions"; $("quest-controls").hidden = screen !== "quests";
  if (screen === "mainmenu") renderMainMenu(d);
  else if (screen === "pause") renderPause(d);
  else if (screen === "machine") renderMachine(d);
  else if (screen === "factions") renderFactions(d);
  else if (screen === "warmup") renderWarmup(d);
  else if (screen === "credits") renderCredits(d);
  else if (screen === "questhome") renderQuestHome(d);
  else if (screen === "quests") await renderQuests(d);
  else if (screen === "advancements") renderAdvancements(d);
  else renderSpaceMap(d);
  const availableW = Math.max(320, window.innerWidth - 350), availableH = Math.max(240, window.innerHeight - 100);
  const previewScale = Math.min(d.factor, availableW / d.width, availableH / d.height);
  canvas.style.width = Math.floor(d.width * previewScale) + "px"; canvas.style.height = Math.floor(d.height * previewScale) + "px";
  $("metrics").textContent = `${d.displayWidth}×${d.displayHeight} · GUI ${d.factor}× · logical ${d.width}×${d.height}`;
  $("status").className = issues.length ? "fail" : "pass";
  $("status").textContent = issues.length ? issues.map(i => i.message).join(" · ") : "Current screen passes layout checks.";
}

async function audit() {
  issues.length = 0;
  const presets = [[854,480],[1184,666],[1280,720],[1920,1080],[2560,1600]];
  const originalHei = $("hei").checked; $("hei").checked = true;
  for (const [displayWidth, displayHeight] of presets) for (const requested of [0,1,2,3,4]) {
    const factor = minecraftScale(displayWidth, displayHeight, requested);
    const d = {displayWidth, displayHeight,factor,width:Math.ceil(displayWidth/factor),height:Math.ceil(displayHeight/factor)};
    mainMenuLayout(d, `mainmenu@${displayWidth}x${displayHeight}/g${requested}`);
    pauseLayout(d, `pause@${displayWidth}x${displayHeight}/g${requested}`);
    for (const machine of data.machines) machineLayout(d, machine, 100, 999999999, `machine/${machine.id}@${displayWidth}x${displayHeight}/g${requested}`);
    for (const faction of data.factions) auditFactionLayout(d, faction, `factions/${faction.id}@${displayWidth}x${displayHeight}/g${requested}`);
    auditWarmupLayout(d, `warmup@${displayWidth}x${displayHeight}/g${requested}`);
    auditCreditsLayout(d, `credits@${displayWidth}x${displayHeight}/g${requested}`);
    for (const line of data.questLines) auditQuestLayout(d, line, `quests/${line.id}@${displayWidth}x${displayHeight}/g${requested}`);
  }
  $("hei").checked = originalHei;
  const report = {passed: issues.length === 0, checks: presets.length * 5 * (data.machines.length + data.factions.length + data.questLines.length + 7), issues};
  $("audit-output").textContent = JSON.stringify(report, null, 2);
  $("status").className = report.passed ? "pass" : "fail";
  $("status").textContent = report.passed ? `PASS · ${report.checks} layout states` : `FAIL · ${issues.length} layout issue(s)`;
  document.body.dataset.auditComplete = "true";
  return report;
}

async function init() {
  [data, fontImage, machineTexture, menuImage, menuLogo, menuButton, questHomeImage] = await Promise.all([
    fetch("/api/data").then(r => r.json()), image("/assets/minecraft/ascii.png"),
    image("/assets/industrial_machine.png"), image("/assets/mainmenu.png"),
    image("/assets/mainmenu-logo.png"), image("/assets/mainmenu-button.png"),
    image("/assets/quest-home.png")]);
  for (const machine of data.machines) { const option = document.createElement("option"); option.value = machine.id; option.textContent = data.lang[`tile.industrialcivilizationcore.${machine.id}.name`] || machine.id; $("machine").append(option); }
  $("machine").value = "programmable_assembler";
  for (const faction of data.factions) { const option = document.createElement("option"); option.value = faction.id; option.textContent = faction.name; $("faction").append(option); }
  for (const line of data.questLines) { const option = document.createElement("option"); option.value = line.id; option.textContent = line.name; $("quest-line").append(option); }
  const params = new URLSearchParams(location.search);
  for (const id of ["screen", "display", "scale", "machine", "faction", "quest-line"]) {
    if (params.has(id)) $(id).value = params.get(id);
  }
  document.querySelectorAll("select,input").forEach(control => control.addEventListener("input", render));
  $("audit").addEventListener("click", audit);
  stamp = (await fetch("/api/stamp").then(r => r.json())).stamp;
  if (!params.has("audit")) setInterval(async () => { try { const next = (await fetch("/api/stamp").then(r => r.json())).stamp; if (next !== stamp) location.reload(); } catch (_) {} }, 1000);
  await render();
  if (params.has("audit")) await audit();
}

init().catch(error => { $("status").className = "fail"; $("status").textContent = error.stack || error; });
