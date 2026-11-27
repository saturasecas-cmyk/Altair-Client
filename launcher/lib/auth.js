const fs = require('fs');
const { safeStorage } = require('electron');
const { Auth } = require('msmc');
const { Authenticator } = require('minecraft-launcher-core');
const { p } = require('./paths');
const { readJson, writeJson } = require('./store');

let cached = null; // { auth, name, type, at }

function seal(text) {
  if (safeStorage.isEncryptionAvailable()) return { enc: true, v: safeStorage.encryptString(text).toString('base64') };
  return { enc: false, v: text };
}
function unseal(o) {
  if (!o || !o.v) return null;
  try {
    return o.enc ? safeStorage.decryptString(Buffer.from(o.v, 'base64')) : o.v;
  } catch (e) {
    return null;
  }
}

async function finish(xbox) {
  const mc = await xbox.getMinecraft();
  const auth = mc.mclc();
  const name = (mc.profile && mc.profile.name) || auth.name;
  writeJson(p.account(), { type: 'microsoft', name, token: seal(xbox.save()) });
  cached = { auth, name, type: 'microsoft', at: Date.now() };
  return { name, type: 'microsoft' };
}

async function loginMicrosoft() {
  const manager = new Auth('select_account');
  const xbox = await manager.launch('electron');
  return finish(xbox);
}

function loginOffline(name) {
  const clean = String(name || 'Altair').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16) || 'Altair';
  writeJson(p.account(), { type: 'offline', name: clean });
  cached = { auth: Authenticator.getAuth(clean), name: clean, type: 'offline', at: Date.now() };
  return { name: clean, type: 'offline' };
}

function logout() {
  cached = null;
  fs.rmSync(p.account(), { force: true });
}

function current() {
  const a = readJson(p.account(), null);
  return a ? { name: a.name, type: a.type } : null;
}

/** Devuelve el objeto de autorización de MCLC, renovando el token de Microsoft si hace falta. */
async function getAuthorization() {
  const a = readJson(p.account(), null);
  if (!a) throw new Error('Inicia sesión primero (Microsoft u offline).');
  if (a.type === 'offline') return Authenticator.getAuth(a.name);
  if (cached && cached.type === 'microsoft' && Date.now() - cached.at < 45 * 60 * 1000) return cached.auth;
  const token = unseal(a.token);
  if (!token) throw new Error('La sesión de Microsoft caducó. Vuelve a iniciar sesión.');
  const manager = new Auth('select_account');
  const xbox = await manager.refresh(token);
  await finish(xbox);
  return cached.auth;
}

module.exports = { loginMicrosoft, loginOffline, logout, current, getAuthorization };
