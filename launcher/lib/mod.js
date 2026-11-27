const fs = require('fs');
const path = require('path');
const { ensure, p } = require('./paths');
const { getJson, downloadFile } = require('./download');
const { getMeta, saveMeta } = require('./store');

function modsDir(instanceDir) {
  return path.join(instanceDir, 'mods');
}

function installedJar(instanceDir) {
  const dir = modsDir(instanceDir);
  if (!fs.existsSync(dir)) return null;
  const f = fs.readdirSync(dir).find((n) => /^altairclient.*\.jar$/i.test(n));
  return f ? path.join(dir, f) : null;
}

/** Descarga el jar de Altair Client de esa versión (y lo actualiza si la release cambió). */
async function ensureMod(entry, repo, instanceDir, ui) {
  const dir = ensure(modsDir(instanceDir));
  const meta = getMeta();
  const key = 'mod-' + entry.mc;
  let release;
  try {
    release = await getJson('https://api.github.com/repos/' + repo + '/releases/tags/' + entry.tag);
  } catch (e) {
    if (installedJar(instanceDir)) {
      ui.log('No pude comprobar actualizaciones del mod (' + e.message + '); uso el que ya está instalado.');
      return;
    }
    throw new Error('No se pudo descargar el mod de la versión ' + entry.mc + ': ' + e.message);
  }
  const asset = (release.assets || []).find((a) => a.name.endsWith('.jar'));
  if (!asset) throw new Error('La release ' + entry.tag + ' no tiene ningún .jar');
  const stamp = asset.id + ':' + asset.updated_at;
  if (meta[key] === stamp && installedJar(instanceDir)) {
    ui.log('Altair Client ' + entry.mc + ' al día.');
    return;
  }
  ui.status('Descargando Altair Client ' + entry.mc + '…');
  for (const n of fs.readdirSync(dir)) {
    if (/^altairclient.*\.jar$/i.test(n)) fs.rmSync(path.join(dir, n), { force: true });
  }
  await downloadFile(asset.browser_download_url, path.join(dir, 'altairclient-' + entry.mc + '.jar'),
    (d, t) => ui.progress('Altair Client', d, t));
  meta[key] = stamp;
  saveMeta(meta);
  ui.log('Altair Client ' + entry.mc + ' instalado.');
}

module.exports = { ensureMod, installedJar };
