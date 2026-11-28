const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { p, ensure } = require('./paths');
const { downloadFile } = require('./download');

function findJava(dir) {
  if (!fs.existsSync(dir)) return null;
  const direct = path.join(dir, 'bin', 'java.exe');
  if (fs.existsSync(direct)) return direct;
  for (const name of fs.readdirSync(dir)) {
    const candidate = path.join(dir, name, 'bin', 'java.exe');
    if (fs.existsSync(candidate)) return candidate;
  }
  return null;
}

/** Windows 10+ ships bsdtar, which unzips natively (fast). Falls back to extract-zip. */
function unzip(zip, dir, ui) {
  return new Promise((resolve, reject) => {
    const child = spawn('tar', ['-xf', zip, '-C', dir], { windowsHide: true });
    let err = '';
    child.stderr.on('data', (d) => { err += d; });
    child.on('error', () => resolve(false));
    child.on('close', (code) => resolve(code === 0));
    child.on('exit', () => {});
  }).then(async (ok) => {
    if (ok) return;
    ui.log('tar could not extract the archive, trying the built-in extractor...');
    fs.rmSync(dir, { recursive: true, force: true });
    ensure(dir);
    await require('extract-zip')(zip, { dir });
  });
}

/** Returns the path to java.exe for the given major version, downloading a Temurin JRE if needed. */
async function ensureJava(major, override, ui) {
  if (override && fs.existsSync(override)) {
    ui.log('Using custom Java: ' + override);
    return override;
  }
  const dir = path.join(p.java(), String(major));
  const found = findJava(dir);
  if (found) return found;

  ui.status('Downloading Java ' + major + '...');
  // A JRE is ~3x smaller than a JDK and is all Minecraft and the Forge installer need.
  const url = 'https://api.adoptium.net/v3/binary/latest/' + major + '/ga/windows/x64/jre/hotspot/normal/eclipse';
  const zip = path.join(ensure(p.cache()), 'temurin-jre-' + major + '.zip');
  await downloadFile(url, zip, (d, t) => ui.progress('Java ' + major, d, t));
  ui.status('Extracting Java ' + major + '...');
  fs.rmSync(dir, { recursive: true, force: true });
  ensure(dir);
  await unzip(zip, dir, ui);
  fs.rmSync(zip, { force: true });
  ui.done('Java ' + major);
  const java = findJava(dir);
  if (!java) throw new Error('java.exe not found after extracting Java ' + major);
  ui.log('Java ' + major + ' installed in ' + dir);
  return java;
}

module.exports = { ensureJava };
