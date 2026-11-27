const fs = require('fs');
const path = require('path');
const { ensure } = require('./paths');
const { getJson, downloadFile } = require('./download');
const { getMeta, saveMeta } = require('./store');

const RECHECK_MS = 3 * 60 * 60 * 1000; // do not hit the GitHub API more than every 3 hours per version

function modsDir(instanceDir) {
  return path.join(instanceDir, 'mods');
}

function installedJar(instanceDir) {
  try {
    const dir = modsDir(instanceDir);
    const f = fs.readdirSync(dir).find((n) => /^altairclient.*\.jar$/i.test(n));
    return f ? path.join(dir, f) : null;
  } catch (e) {
    return null;
  }
}

/** Downloads the Altair Client jar for this version (and updates it when the release changes). */
async function ensureMod(entry, repo, instanceDir, ui) {
  const dir = ensure(modsDir(instanceDir));
  const meta = getMeta();
  const key = 'mod-' + entry.mc;
  const have = installedJar(instanceDir);
  const last = meta[key + '-checked'] || 0;
  if (have && meta[key] && Date.now() - last < RECHECK_MS) {
    ui.log('Altair Client ' + entry.mc + ' is up to date (checked recently).');
    return;
  }
  let release;
  try {
    release = await getJson('https://api.github.com/repos/' + repo + '/releases/tags/' + entry.tag);
  } catch (e) {
    if (have) {
      ui.log('Could not check for mod updates (' + e.message + '); using the installed one.');
      return;
    }
    throw new Error('Could not download the mod for ' + entry.mc + ': ' + e.message);
  }
  const asset = (release.assets || []).find((a) => a.name.endsWith('.jar'));
  if (!asset) throw new Error('Release ' + entry.tag + ' has no .jar');
  const stamp = asset.id + ':' + asset.updated_at;
  meta[key + '-checked'] = Date.now();
  if (meta[key] === stamp && have) {
    saveMeta(meta);
    ui.log('Altair Client ' + entry.mc + ' is up to date.');
    return;
  }
  ui.status('Downloading Altair Client ' + entry.mc + '...');
  for (const n of fs.readdirSync(dir)) {
    if (/^altairclient.*\.jar$/i.test(n)) fs.rmSync(path.join(dir, n), { force: true });
  }
  await downloadFile(asset.browser_download_url, path.join(dir, 'altairclient-' + entry.mc + '.jar'),
    (d, t) => ui.progress('Altair Client', d, t));
  ui.done('Altair Client');
  meta[key] = stamp;
  saveMeta(meta);
  ui.log('Altair Client ' + entry.mc + ' installed.');
}

module.exports = { ensureMod, installedJar };
