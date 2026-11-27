const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { p, ensure } = require('./paths');
const { downloadFile } = require('./download');
const { getMeta, saveMeta } = require('./store');

function findForgeId(root, entry) {
  const dir = path.join(root, 'versions');
  if (!fs.existsSync(dir)) return null;
  const names = fs.readdirSync(dir).filter((n) => /forge/i.test(n) && n.includes(entry.forge));
  for (const n of names) {
    if (fs.existsSync(path.join(dir, n, n + '.json'))) return n;
  }
  return null;
}

function isInstalled(entry) {
  return !!findForgeId(p.root(), entry);
}

function run(java, args, ui) {
  return new Promise((resolve, reject) => {
    const child = spawn(java, args, { windowsHide: true });
    const pipeLines = (stream) => {
      let buf = '';
      stream.on('data', (d) => {
        buf += d.toString();
        const lines = buf.split(/\r?\n/);
        buf = lines.pop();
        lines.forEach((l) => l.trim() && ui.log('[forge] ' + l.trim()));
      });
    };
    pipeLines(child.stdout);
    pipeLines(child.stderr);
    child.on('error', reject);
    child.on('close', (code) => (code === 0 ? resolve() : reject(new Error('El instalador de Forge terminó con código ' + code))));
  });
}

/** Instala Forge (con el instalador oficial) si no está ya. Devuelve el id de versión. */
async function ensureForge(entry, java, ui) {
  const root = ensure(p.root());
  let id = findForgeId(root, entry);
  if (id) return id;

  const profiles = path.join(root, 'launcher_profiles.json');
  if (!fs.existsSync(profiles)) fs.writeFileSync(profiles, JSON.stringify({ profiles: {}, version: 3 }));

  const full = entry.mc + '-' + entry.forge;
  const url = 'https://maven.minecraftforge.net/net/minecraftforge/forge/' + full + '/forge-' + full + '-installer.jar';
  const jar = path.join(ensure(p.cache()), 'forge-' + full + '-installer.jar');
  ui.status('Descargando Forge ' + entry.forge + '…');
  await downloadFile(url, jar, (d, t) => ui.progress('Forge ' + entry.forge, d, t));

  ui.status('Instalando Forge ' + entry.forge + ' (puede tardar unos minutos)…');
  await run(java, ['-jar', jar, '--installClient', root], ui);

  id = findForgeId(root, entry);
  if (!id) throw new Error('Forge se instaló pero no encuentro la versión en ' + path.join(root, 'versions'));
  const meta = getMeta();
  meta['forge-' + entry.mc] = id;
  saveMeta(meta);
  return id;
}

module.exports = { ensureForge, isInstalled, findForgeId };
