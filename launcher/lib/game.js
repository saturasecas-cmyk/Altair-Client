const path = require('path');
const { Client } = require('minecraft-launcher-core');
const { p, ensure } = require('./paths');
const { ensureJava } = require('./java');
const { ensureForge } = require('./forge');
const { ensureMod } = require('./mod');
const auth = require('./auth');

/** Prepara todo (Java, Forge, mod) y lanza el juego. Resuelve cuando el juego se cierra. */
async function play(entry, repo, settings, ui) {
  const authorization = await auth.getAuthorization();
  const java = await ensureJava(entry.java, settings.javaPath, ui);
  const forgeId = await ensureForge(entry, java, ui);
  const instance = ensure(settings.gameDir ? path.join(settings.gameDir, entry.mc) : p.instance(entry.mc));
  await ensureMod(entry, repo, instance, ui);

  ui.status('Iniciando Minecraft ' + entry.mc + '…');
  const launcher = new Client();
  const opts = {
    authorization,
    root: ensure(p.root()),
    javaPath: java,
    version: { number: entry.mc, type: 'release', custom: forgeId },
    memory: { max: settings.ram + 'M', min: Math.min(1024, settings.ram) + 'M' },
    overrides: { gameDirectory: instance, detached: false },
    window: settings.fullscreen ? { fullscreen: true } : { width: settings.width, height: settings.height }
  };
  return new Promise((resolve, reject) => {
    launcher.on('debug', (m) => ui.log('[mclc] ' + m));
    launcher.on('data', (m) => {
      ui.log(String(m).trimEnd());
      if (/Setting user|Backend library|LWJGL|Sound engine started|Created: /.test(String(m))) ui.status('Jugando a Minecraft ' + entry.mc);
    });
    launcher.on('progress', (e) => ui.progress(e.type || 'Archivos', e.task, e.total));
    launcher.on('package-extract', () => ui.log('Extrayendo nativos…'));
    launcher.on('close', (code) => resolve(code));
    launcher.launch(opts).then((proc) => {
      if (!proc) reject(new Error('MCLC no pudo iniciar el juego (revisa el registro).'));
      else ui.launched();
    }).catch(reject);
  });
}

module.exports = { play };
