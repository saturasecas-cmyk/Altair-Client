const { app } = require('electron');
const path = require('path');
const fs = require('fs');

function base() {
  return path.join(app.getPath('appData'), '.altair-client');
}
const p = {
  base,
  root: () => path.join(base(), 'minecraft'),
  instances: () => path.join(base(), 'instances'),
  instance: (mc) => path.join(base(), 'instances', mc),
  java: () => path.join(base(), 'java'),
  cache: () => path.join(base(), 'cache'),
  settings: () => path.join(app.getPath('userData'), 'settings.json'),
  account: () => path.join(app.getPath('userData'), 'account.json'),
  meta: () => path.join(base(), 'meta.json')
};
function ensure(dir) {
  fs.mkdirSync(dir, { recursive: true });
  return dir;
}
module.exports = { p, ensure };
