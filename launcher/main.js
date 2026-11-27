const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const path = require('path');
const fs = require('fs');
const store = require('./lib/store');
const { p, ensure } = require('./lib/paths');
const auth = require('./lib/auth');
const forge = require('./lib/forge');
const mod = require('./lib/mod');
const { play } = require('./lib/game');
const manifest = require('./versions.json');

let win = null;
let busy = false;

const send = (ch, data) => win && !win.isDestroyed() && win.webContents.send(ch, data);
const ui = {
  log: (m) => send('log', String(m)),
  status: (m) => send('status', String(m)),
  progress: (label, done, total) => send('progress', { label, done, total }),
  launched: () => send('launched')
};

function createWindow() {
  win = new BrowserWindow({
    width: 1040,
    height: 640,
    minWidth: 900,
    minHeight: 560,
    frame: false,
    backgroundColor: '#070b22',
    title: 'Altair Launcher',
    icon: path.join(__dirname, 'build', 'icon.png'),
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true, nodeIntegration: false }
  });
  win.removeMenu();
  win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

function stateFor() {
  const settings = store.getSettings();
  return {
    settings,
    account: auth.current(),
    repo: manifest.repo,
    versions: manifest.versions.map((v) => {
      const inst = settings.gameDir ? path.join(settings.gameDir, v.mc) : p.instance(v.mc);
      return Object.assign({}, v, { installed: forge.isInstalled(v) && !!mod.installedJar(inst) });
    }),
    base: p.base()
  };
}

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
  if (busy) return { ok: false, error: 'Ya hay una operación en curso.' };
  const entry = manifest.versions.find((v) => v.mc === mc);
  if (!entry) return { ok: false, error: 'Versión desconocida: ' + mc };
  busy = true;
  store.saveSettings({ version: mc });
  try {
    send('busy', true);
    const code = await play(entry, manifest.repo, store.getSettings(), ui);
    ui.status('Minecraft se cerró (código ' + code + ').');
    return { ok: true, code, state: stateFor() };
  } catch (e) {
    const msg = String(e && e.message || e);
    ui.log('ERROR: ' + msg);
    ui.status('Error: ' + msg);
    return { ok: false, error: msg };
  } finally {
    busy = false;
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
