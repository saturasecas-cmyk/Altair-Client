const { contextBridge, ipcRenderer } = require('electron');

const on = (ch, cb) => ipcRenderer.on(ch, (_e, data) => cb(data));
contextBridge.exposeInMainWorld('altair', {
  state: () => ipcRenderer.invoke('state'),
  saveSettings: (patch) => ipcRenderer.invoke('save-settings', patch),
  loginMicrosoft: () => ipcRenderer.invoke('login-microsoft'),
  loginOffline: (name) => ipcRenderer.invoke('login-offline', name),
  logout: () => ipcRenderer.invoke('logout'),
  friends: (force) => ipcRenderer.invoke('friends', force),
  pickFolder: () => ipcRenderer.invoke('pick-folder'),
  openFolder: (which) => ipcRenderer.invoke('open-folder', which),
  play: (mc) => ipcRenderer.invoke('play', mc),
  win: (action) => ipcRenderer.send('win', action),
  onLog: (cb) => on('log', cb),
  onStatus: (cb) => on('status', cb),
  onProgress: (cb) => on('progress', cb),
  onBusy: (cb) => on('busy', cb),
  onLaunched: (cb) => on('launched', cb)
});
