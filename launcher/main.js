const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const path = require('path');
const fs = require('fs');
const store = require('./lib/store');
const { p, ensure } = require('./lib/paths');
const auth = require('./lib/auth');
const forge = require('./lib/forge');
const mod = require('./lib/mod');
const manifest = require('./versions.json');

let win = null;
let busy = false;

const send = (ch, data) => win && !win.isDestroyed() && win.webContents.send(ch, data);

// Log lines are batched (one IPC message every 100 ms) so a chatty game cannot flood the UI.
let logBuf = [];
let logTimer = null;
function flushLog() {
  logTimer = null;
  if (logBuf.length) send('log', logBuf.join('\n'));
  logBuf = [];
}
// Parallel downloads are aggregated into a single progress bar (max ~10 updates per second).
const tasks = new Map();
let lastProg = 0;
function emitProgress(force) {
  const now = Date.now();
  if (!force && now - lastProg < 100) return;
  lastProg = now;
  let done = 0;
  let total = 0;
  const labels = [];
  for (const [label, t] of tasks) {
    labels.push(label);
    done += t.done;
    total += t.total;
  }
  send('progress', { label: labels.join(' + '), done, total, active: tasks.size });
}
const ui = {
  log: (m) => {
    logBuf.push(String(m));
    if (!logTimer) logTimer = setTimeout(flushLog, 100);
  },
  status: (m) => send('status', String(m)),
  progress: (label, done, total) => {
    tasks.set(label, { done: done || 0, total: total || 0 });
    emitProgress(false);
  },
  done: (label) => {
    tasks.delete(label);
    emitProgress(true);
  },
  launched: () => {
    tasks.clear();
    emitProgress(true);
    if (win && !win.isDestroyed()) win.minimize(); // free the screen while playing
    send('launched');
  }
};

function createWindow() {
  win = new BrowserWindow({
    width: 1040,
    height: 640,
    minWidth: 900,
    minHeight: 560,
    frame: false,
    show: false,
    backgroundColor: '#070b22',
    title: 'Altair Launcher',
    icon: path.join(__dirname, 'build', 'icon.png'),
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true, nodeIntegration: false }
  });
  win.removeMenu();
  win.once('ready-to-show', () => win.show());
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

function stateFor() {
  const settings = store.getSettings();
  const names = forge.listVersions(p.root()); // one directory read for all versions
  return {
    settings,
    account: auth.current(),
    repo: manifest.repo,
    versions: manifest.versions.map((v) => {
      const inst = settings.gameDir ? path.join(settings.gameDir, v.mc) : p.instance(v.mc);
      return Object.assign({}, v, { installed: forge.isInstalled(v, names) && !!mod.installedJar(inst) });
    }),
    base: p.base()
  };
}

// A launcher does not need the GPU process: saves RAM and startup time.
app.disableHardwareAcceleration();

app.whenReady().then(() => {
  if (!app.requestSingleInstanceLock()) return app.quit();
  createWindow();
});
app.on('window-all-closed', () => app.quit());

ipcMain.handle('state', () => stateFor());
ipcMain.handle('save-settings', (_e, patch) => {
  store.saveSettings(patch);
  return stateFor();
});
ipcMain.handle('login-microsoft', async () => {
  try {
    await auth.loginMicrosoft();
    return { ok: true, state: stateFor() };
  } catch (e) {
    return { ok: false, error: String(e && e.message || e) };
  }
});
ipcMain.handle('login-offline', (_e, name) => {
  auth.loginOffline(name);
  return stateFor();
});
ipcMain.handle('logout', () => {
  auth.logout();
  return stateFor();
});
ipcMain.handle('pick-folder', async () => {
  const r = await dialog.showOpenDialog(win, { properties: ['openDirectory', 'createDirectory'] });
  return r.canceled ? null : r.filePaths[0];
});
ipcMain.handle('open-folder', (_e, which) => {
  const s = store.getSettings();
  const dir = which === 'base' ? p.base() : (s.gameDir || p.instances());
  ensure(dir);
  shell.openPath(dir);
});
ipcMain.handle('play', async (_e, mc) => {
  const { play } = require('./lib/game'); // lazy: keeps startup fast
  if (busy) return { ok: false, error: 'Another operation is already running.' };
  const entry = manifest.versions.find((v) => v.mc === mc);
  if (!entry) return { ok: false, error: 'Unknown version: ' + mc };
  busy = true;
  store.saveSettings({ version: mc });
  try {
    send('busy', true);
    const code = await play(entry, manifest.repo, store.getSettings(), ui);
    flushLog();
    ui.status('Minecraft closed (exit code ' + code + ').');
    return { ok: true, code, state: stateFor() };
  } catch (e) {
    const msg = String(e && e.message || e);
    ui.log('ERROR: ' + msg);
    ui.status('Error: ' + msg);
    return { ok: false, error: msg };
  } finally {
    busy = false;
    tasks.clear();
    flushLog();
    send('busy', false);
    if (win && !win.isDestroyed()) {
      win.show();
      win.restore();
    }
  }
});
ipcMain.on('win', (_e, action) => {
  if (!win) return;
  if (action === 'min') win.minimize();
  if (action === 'close') win.close();
});
