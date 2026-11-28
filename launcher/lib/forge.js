const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { p, ensure } = require('./paths');
const { downloadFile } = require('./download');
const { getMeta, saveMeta } = require('./store');

function listVersions(root) {
  try {
    return fs.readdirSync(path.join(root, 'versions'));
  } catch (e) {
    return [];
  }
}

function findForgeId(root, entry, names) {
  const list = names || listVersions(root);
  for (const n of list) {
    if (/forge/i.test(n) && n.includes(entry.forge) && fs.existsSync(path.join(root, 'versions', n, n + '.json'))) return n;
  }
  return null;
}

function isInstalled(entry, names) {
  return !!findForgeId(p.root(), entry, names);
}

function run(java, args, ui) {
  return new Promise((resolve, reject) => {
    const child = spawn(java, args, { windowsHide: true });
    const tail = [];
    const pipeLines = (stream) => {
      let buf = '';
      stream.on('data', (d) => {
        buf += d.toString();
        const lines = buf.split(/\r?\n/);
        buf = lines.pop();
        lines.forEach((l) => {
          if (!l.trim()) return;
          ui.log('[forge] ' + l.trim());
          tail.push(l.trim());
          if (tail.length > 6) tail.shift();
        });
      });
    };
    pipeLines(child.stdout);
    pipeLines(child.stderr);
    child.on('error', reject);
    child.on('close', (code) => (code === 0 ? resolve() : reject(new Error('The Forge installer exited with code ' + code + (tail.length ? ' - ' + tail.slice(-3).join(' | ') : '')))));
  });
}

/**
 * Installs Forge with the official installer if it is not there yet. Returns the version id.
 * The installer jar is downloaded while Java may still be downloading (javaPromise).
 */
async function ensureForge(entry, javaPromise, ui) {
  const root = ensure(p.root());
  let id = findForgeId(root, entry);
  if (id) return id;

  const profiles = path.join(root, 'launcher_profiles.json');
  if (!fs.existsSync(profiles)) fs.writeFileSync(profiles, JSON.stringify({ profiles: {}, version: 3 }));

  const full = entry.mc + '-' + entry.forge;
  const url = 'https://maven.minecraftforge.net/net/minecraftforge/forge/' + full + '/forge-' + full + '-installer.jar';
  const jar = path.join(ensure(p.cache()), 'forge-' + full + '-installer.jar');
  const jarReady = fs.existsSync(jar)
    ? Promise.resolve()
    : downloadFile(url, jar, (d, t) => ui.progress('Forge ' + entry.forge, d, t)).then(() => ui.done('Forge ' + entry.forge));
  const [java] = await Promise.all([javaPromise, jarReady]);

  ui.status('Installing Forge ' + entry.forge + ' (this can take a few minutes the first time)...');
  await run(java, ['-jar', jar, '--installClient', root], ui);

  id = findForgeId(root, entry);
  if (!id) throw new Error('Forge installed but its version was not found in ' + path.join(root, 'versions'));
  fs.rmSync(jar, { force: true });
  const meta = getMeta();
  meta['forge-' + entry.mc] = id;
  saveMeta(meta);
  return id;
}

module.exports = { ensureForge, isInstalled, listVersions };
