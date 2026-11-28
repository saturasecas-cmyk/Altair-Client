const { getXbox } = require('./auth');

const UA = 'AltairLauncher/1.4';
let cache = { at: 0, data: null };

/** Finds the Microsoft access token inside the msmc Xbox session object (the field name differs between versions). */
function findAccessToken(xbox) {
  const seen = new Set();
  const walk = (o, depth) => {
    if (!o || typeof o !== 'object' || depth > 3 || seen.has(o)) return null;
    seen.add(o);
    if (typeof o.access_token === 'string' && o.access_token.length > 40) return o.access_token;
    for (const k of Object.keys(o)) {
      const v = o[k];
      if (v && typeof v === 'object') {
        const r = walk(v, depth + 1);
        if (r) return r;
      }
    }
    return null;
  };
  return walk(xbox, 0);
}

async function postJson(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', 'User-Agent': UA, 'x-xbl-contract-version': '1' },
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error('Xbox sign-in failed (HTTP ' + res.status + ' at ' + new URL(url).host + ')');
  return res.json();
}

/** Returns the Xbox friends list (cached for 60 s unless force). */
async function getFriends(force) {
  if (!force && cache.data && Date.now() - cache.at < 60000) return cache.data;
  const xbox = await getXbox();
  const ms = findAccessToken(xbox);
  if (!ms) throw new Error('Could not read your Microsoft token (try signing out and in again).');

  const xbl = await postJson('https://user.auth.xboxlive.com/user/authenticate', {
    Properties: { AuthMethod: 'RPS', SiteName: 'user.auth.xboxlive.com', RpsTicket: 'd=' + ms },
    RelyingParty: 'http://auth.xboxlive.com',
    TokenType: 'JWT'
  });
  const xsts = await postJson('https://xsts.auth.xboxlive.com/xsts/authorize', {
    Properties: { SandboxId: 'RETAIL', UserTokens: [xbl.Token] },
    RelyingParty: 'http://xboxlive.com',
    TokenType: 'JWT'
  });
  const uhs = xsts.DisplayClaims && xsts.DisplayClaims.xui && xsts.DisplayClaims.xui[0] && xsts.DisplayClaims.xui[0].uhs;
  if (!uhs) throw new Error('Xbox did not return a user hash.');

  const res = await fetch('https://peoplehub.xboxlive.com/users/me/people/social', {
    headers: {
      Authorization: 'XBL3.0 x=' + uhs + ';' + xsts.Token,
      'x-xbl-contract-version': '5',
      'Accept-Language': 'en-US',
      Accept: 'application/json',
      'User-Agent': UA
    }
  });
  if (!res.ok) throw new Error('Could not load friends (HTTP ' + res.status + ').');
  const json = await res.json();
  const people = (json.people || []).map((p) => ({
    xuid: p.xuid,
    name: p.gamertag || p.displayName || '?',
    realName: p.displayName && p.displayName !== p.gamertag ? p.displayName : '',
    pic: p.displayPicRaw || '',
    online: p.presenceState === 'Online',
    status: p.presenceText || (p.presenceState === 'Online' ? 'Online' : 'Offline'),
    favorite: !!p.isFavorite,
    mutual: p.isFollowingCaller !== false
  }));
  people.sort((a, b) => (b.online - a.online) || (b.favorite - a.favorite) || a.name.localeCompare(b.name));
  cache = { at: Date.now(), data: people };
  return people;
}

module.exports = { getFriends };
