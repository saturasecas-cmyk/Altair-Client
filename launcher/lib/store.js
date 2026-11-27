const fs = require('fs');
const { p } = require('./paths');

const DEFAULTS = {
  version: '1.21.1',
  ram: 4096,
  width: 1280,
  height: 720,
  fullscreen: false,
  gameDir: '',
  javaPath: '',
  offlineName: 'Altair',
  jvmArgs: '-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication'
};

function readJson(file, fallback) {
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (e) {
    return fallback;
  }
}
function writeJson(file, data) {
  fs.mkdirSync(require('path').dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(data, null, 2));
}

function getSettings() {
  return Object.assign({}, DEFAULTS, readJson(p.settings(), {}));
}
function saveSettings(patch) {
  const s = Object.assign(getSettings(), patch);
  s.ram = Math.max(1024, Math.min(32768, parseInt(s.ram, 10) || 4096));
  writeJson(p.settings(), s);
  return s;
}
function getMeta() {
  return readJson(p.meta(), {});
}
function saveMeta(m) {
  writeJson(p.meta(), m);
}
module.exports = { getSettings, saveSettings, getMeta, saveMeta, readJson, writeJson };
