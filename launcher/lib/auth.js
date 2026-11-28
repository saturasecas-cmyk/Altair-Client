const fs = require('fs');
const { safeStorage } = require('electron');
const Auth = (...a) => new (require('msmc').Auth)(...a);
const offlineAuth = (name) => require('minecraft-launcher-core').Authenticator.getAuth(name);
const { p } = require('./paths');
const { readJson, writeJson } = require('./store');

let cached = null;
let lastXbox = null; // { auth, name, type, at }

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
  lastXbox = xbox;
  const mc = await xbox.getMinecraft();
  const auth = mc.mclc();
  const name = (mc.profile && mc.profile.name) || auth.name;
  writeJson(p.account(), { type: 'microsoft', name, token: seal(xbox.save()) });
  cached = { auth, name, type: 'microsoft', at: Date.now() };
  return { name, type: 'microsoft' };
}

async function loginMicrosoft() {
  const manager = Auth('select_account');
  const xbox = await manager.launch('electron');
  return finish(xbox);
}

function loginOffline(name) {
  const clean = String(name || 'Altair').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16) || 'Altair';
  writeJson(p.account(), { type: 'offline', name: clean });
  cached = { auth: offlineAuth(clean), name: clean, type: 'offline', at: Date.now() };
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

/** Returns the MCLC authorization object, refreshing the Microsoft token when needed. */
async function getAuthorization() {
  const a = readJson(p.account(), null);
  if (!a) throw new Error('Sign in first (Microsoft or offline).');
  if (a.type === 'offline') return offlineAuth(a.name);
  if (cached && cached.type === 'microsoft' && Date.now() - cached.at < 45 * 60 * 1000) return cached.auth;
  const token = unseal(a.token);
  if (!token) throw new Error('Your Microsoft session expired. Please sign in again.');
  const manager = Auth('select_account');
  const xbox = await manager.refresh(token);
  await finish(xbox);
  return cached.auth;
}

/** The msmc Xbox session (used for the friends list); refreshes it from the saved token if needed. */
async function getXbox() {
  if (lastXbox) return lastXbox;
  const a = readJson(p.account(), null);
  if (!a || a.type !== 'microsoft') throw new Error('Sign in with a Microsoft account to see your friends.');
  await getAuthorization();
  if (!lastXbox) throw new Error('Could not open your Xbox session.');
  return lastXbox;
}

module.exports = { getXbox, loginMicrosoft, loginOffline, logout, current, getAuthorization };
