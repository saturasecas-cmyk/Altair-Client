const fs = require('fs');
const path = require('path');
const { p, ensure } = require('./paths');
const { ensureJava } = require('./java');
const { ensureForge } = require('./forge');
const { ensureMod } = require('./mod');
const auth = require('./auth');

const DEFAULT_JVM = '-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication';

/** Prepares everything (Java, Forge, mod) in parallel and launches the game. Resolves when the game closes. */
async function play(entry, repo, settings, ui) {
  const instance = ensure(settings.gameDir ? path.join(settings.gameDir, entry.mc) : p.instance(entry.mc));

  // Everything independent runs at the same time: login refresh, Java, Forge installer download, mod download.
  // Each step is labelled so an error says exactly where it happened.
  const step = (name, promise) => promise.catch((e) => {
    ui.log('[' + name + '] ' + (e && e.stack ? e.stack : e));
    throw new Error(name + ': ' + (e && e.message ? e.message : e));
  });
  const javaPromise = step('Java', ensureJava(entry.java, settings.javaPath, ui));
  javaPromise.catch(() => {});
  const [authorization, java, forgeId] = await Promise.all([
    step('Login', auth.getAuthorization()),
    javaPromise,
    step('Forge', ensureForge(entry, javaPromise, ui)),
    step('Mod', ensureMod(entry, repo, instance, ui))
  ]).then((r) => [r[0], r[1], r[2]]);

  ui.status('Starting Minecraft ' + entry.mc + '...');
  const { Client } = require('minecraft-launcher-core'); // lazy: heavy and only needed here
  const launcher = new Client();
  const javaw = java.replace(/java\.exe$/i, 'javaw.exe'); // no console window
  const ram = settings.ram;
  const extra = (settings.jvmArgs === undefined || settings.jvmArgs === null ? DEFAULT_JVM : settings.jvmArgs)
    .split(/\s+/).filter(Boolean);
  const opts = {
    authorization,
    root: ensure(p.root()),
    javaPath: fs.existsSync(javaw) ? javaw : java,
    version: { number: entry.mc, type: 'release', custom: forgeId },
    memory: { max: ram + 'M', min: Math.round(ram / 2) + 'M' },
    customArgs: extra,
    overrides: { gameDirectory: instance, detached: false, maxSockets: 8 },
    window: settings.fullscreen ? { fullscreen: true } : { width: settings.width, height: settings.height }
  };
  return new Promise((resolve, reject) => {
    launcher.on('debug', (m) => ui.log('[launcher] ' + m));
    launcher.on('data', (m) => {
      ui.log(String(m).trimEnd());
      if (/Setting user|Backend library|LWJGL|Sound engine started|Created: /.test(String(m))) ui.status('Playing Minecraft ' + entry.mc);
    });
    launcher.on('progress', (e) => ui.progress(e.type || 'Files', e.task, e.total));
    launcher.on('close', (code) => resolve(code));
    launcher.launch(opts).then((proc) => {
      if (!proc) reject(new Error('The launcher could not start the game (see the Log tab).'));
      else ui.launched();
    }).catch(reject);
  });
}

module.exports = { play, DEFAULT_JVM };
