const fs = require('fs');
const path = require('path');
const { Readable } = require('stream');
const { Transform } = require('stream');
const { pipeline } = require('stream/promises');

const UA = 'AltairLauncher/1.0';

async function getJson(url) {
  const res = await fetch(url, { headers: { 'User-Agent': UA, Accept: 'application/vnd.github+json' }, redirect: 'follow' });
  if (!res.ok) throw new Error('HTTP ' + res.status + ' en ' + url);
  return res.json();
}

/** Downloads to a .part file, verifies the size and renames when finished. Retries once on failure. */
async function downloadFile(url, dest, onProgress) {
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  let lastErr;
  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      return await downloadOnce(url, dest, onProgress);
    } catch (e) {
      lastErr = e;
      fs.rmSync(dest + '.part', { force: true });
    }
  }
  throw new Error('Download failed (' + url + '): ' + lastErr.message);
}

async function downloadOnce(url, dest, onProgress) {
  // identity: ask for the raw bytes so nothing between us and the file can re-encode them
  const res = await fetch(url, { headers: { 'User-Agent': UA, 'Accept-Encoding': 'identity' }, redirect: 'follow' });
  if (!res.ok || !res.body) throw new Error('HTTP ' + res.status);
  const total = parseInt(res.headers.get('content-length') || '0', 10);
  let done = 0;
  const part = dest + '.part';
  const counter = new Transform({
    transform(chunk, _enc, cb) {
      done += chunk.length;
      if (onProgress) onProgress(done, total);
      cb(null, chunk);
    }
  });
  await pipeline(Readable.fromWeb(res.body), counter, fs.createWriteStream(part));
  if (total && done !== total) throw new Error('incomplete download (' + done + ' of ' + total + ' bytes)');
  fs.rmSync(dest, { force: true });
  fs.renameSync(part, dest);
  return dest;
}

module.exports = { getJson, downloadFile };
