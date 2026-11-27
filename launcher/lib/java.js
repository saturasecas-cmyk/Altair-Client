const fs = require('fs');
const path = require('path');
const extract = require('extract-zip');
const { p, ensure } = require('./paths');
const { downloadFile } = require('./download');

function findJava(dir) {
  if (!fs.existsSync(dir)) return null;
  const direct = path.join(dir, 'bin', 'java.exe');
  if (fs.existsSync(direct)) return direct;
  for (const name of fs.readdirSync(dir)) {
    const candidate = path.join(dir, name, 'bin', 'java.exe');
    if (fs.existsSync(candidate)) return candidate;
  }
  return null;
}

/** Devuelve la ruta a java.exe de la versión mayor pedida, descargando Temurin si hace falta. */
async function ensureJava(major, override, ui) {
  if (override && fs.existsSync(override)) {
    ui.log('Usando Java personalizado: ' + override);
    return override;
  }
  const dir = path.join(p.java(), String(major));
  const found = findJava(dir);
  if (found) return found;

  ui.status('Descargando Java ' + major + '…');
  const url = 'https://api.adoptium.net/v3/binary/latest/' + major + '/ga/windows/x64/jdk/hotspot/normal/eclipse';
  const zip = path.join(ensure(p.cache()), 'temurin-' + major + '.zip');
  await downloadFile(url, zip, (d, t) => ui.progress('Java ' + major, d, t));
  ui.status('Extrayendo Java ' + major + '…');
  fs.rmSync(dir, { recursive: true, force: true });
  ensure(dir);
  await extract(zip, { dir });
  fs.rmSync(zip, { force: true });
  const java = findJava(dir);
  if (!java) throw new Error('No se encontró java.exe tras extraer Java ' + major);
  ui.log('Java ' + major + ' instalado en ' + dir);
  return java;
}

module.exports = { ensureJava };
