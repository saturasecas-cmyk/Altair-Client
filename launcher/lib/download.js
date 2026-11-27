const fs = require('fs');
const path = require('path');
const { Readable } = require('stream');
const { pipeline } = require('stream/promises');

const UA = 'AltairLauncher/1.0';

async function getJson(url) {
  const res = await fetch(url, { headers: { 'User-Agent': UA, Accept: 'application/vnd.github+json' }, redirect: 'follow' });
  if (!res.ok) throw new Error('HTTP ' + res.status + ' en ' + url);
  return res.json();
}

/** Descarga a un fichero .part y lo renombra al terminar. onProgress(hecho, total). */
async function downloadFile(url, dest, onProgress) {
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  const res = await fetch(url, { headers: { 'User-Agent': UA }, redirect: 'follow' });
  if (!res.ok || !res.body) throw new Error('HTTP ' + res.status + ' al descargar ' + url);
  const total = parseInt(res.headers.get('content-length') || '0', 10);
  let done = 0;
  const part = dest + '.part';
  const body = Readable.fromWeb(res.body);
  body.on('data', (chunk) => {
    done += chunk.length;
    if (onProgress) onProgress(done, total);
  });
  await pipeline(body, fs.createWriteStream(part));
  fs.renameSync(part, dest);
  return dest;
}

module.exports = { getJson, downloadFile };
