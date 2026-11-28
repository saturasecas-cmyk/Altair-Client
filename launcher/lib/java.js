const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { p, ensure } = require('./paths');
const { downloadFile } = require('./download');

/** A runtime only counts as installed when it is complete (a half-extracted one has java.exe but no lib/). */
function complete(home) {
  return fs.existsSync(path.join(home, 'bin', 'java.exe'))
    && fs.existsSync(path.join(home, 'lib', 'jvm.cfg'))
    && fs.existsSync(path.join(home, 'lib', 'modules'));
}

function findJava(dir) {
  if (!fs.existsSync(dir)) return null;
  if (complete(dir)) return path.join(dir, 'bin', 'java.exe');
  for (const name of fs.readdirSync(dir)) {
    const home = path.join(dir, name);
    if (complete(home)) return path.join(home, 'bin', 'java.exe');
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
  }).then((ok) => {
    if (!ok) ui.log('tar could not extract the archive.');
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
  if (fs.existsSync(dir)) ui.log('Java ' + major + ' was incomplete; reinstalling.');

  ui.status('Downloading Java ' + major + '...');
  // A JRE is ~3x smaller than a JDK and is all Minecraft and the Forge installer need.
  const url = 'https://api.adoptium.net/v3/binary/latest/' + major + '/ga/windows/x64/jre/hotspot/normal/eclipse';
  const zip = path.join(ensure(p.cache()), 'temurin-jre-' + major + '.zip');
  await downloadFile(url, zip, (d, t) => ui.progress('Java ' + major, d, t));
  ui.status('Extracting Java ' + major + '...');
  // Extract into a temporary folder and rename at the end, so a failed extraction never leaves a half-installed Java.
  const tmp = dir + '.tmp';
  fs.rmSync(tmp, { recursive: true, force: true });
  fs.rmSync(dir, { recursive: true, force: true });
  ensure(tmp);
  await unzip(zip, tmp, ui);
  if (!findJava(tmp)) {
    ui.log('tar produced an incomplete Java; retrying with the built-in extractor...');
    fs.rmSync(tmp, { recursive: true, force: true });
    ensure(tmp);
    await require('extract-zip')(zip, { dir: tmp }).catch((e) => ui.log('extract-zip: ' + e.message));
  }
  if (!findJava(tmp)) {
    fs.rmSync(tmp, { recursive: true, force: true });
    fs.rmSync(zip, { force: true });
    throw new Error('Java ' + major + ' could not be extracted completely (the download may be corrupt). Try again.');
  }
  fs.renameSync(tmp, dir);
  fs.rmSync(zip, { force: true });
  ui.done('Java ' + major);
  const java = findJava(dir);
  ui.log('Java ' + major + ' installed in ' + dir);
  return java;
}

module.exports = { ensureJava };
