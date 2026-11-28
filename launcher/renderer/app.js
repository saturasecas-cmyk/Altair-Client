(() => {
  const $ = (id) => document.getElementById(id);
  const A = window.altair;
  let state = null;
  let busy = false;
  let selected = null;

  // ---- log (capped) ----
  const MAX_LOG = 4000;
  const logEl = $('log');
  let logLines = 0;
  function addLog(chunk) {
    logEl.textContent += chunk + '\n';
    logLines += chunk.split('\n').length;
    if (logLines > MAX_LOG) {
      logEl.textContent = logEl.textContent.split('\n').slice(-MAX_LOG / 2).join('\n');
      logLines = MAX_LOG / 2;
    }
    logEl.scrollTop = logEl.scrollHeight;
  }

  function setStatus(msg, err) {
    $('status').textContent = msg;
    $('status').classList.toggle('err', !!err);
  }

  function pill(text, cls) {
    const s = document.createElement('span');
    s.className = 'pill' + (cls ? ' ' + cls : '');
    s.textContent = text;
    return s;
  }

  // ---- version chips ----
  const GROUPS = [['26.x', (v) => v.mc.startsWith('26.')], ['1.21.x', (v) => v.mc.startsWith('1.21')], ['1.20.x', (v) => v.mc.startsWith('1.20')]];
  function renderVersions() {
    const box = $('versions');
    box.textContent = '';
    for (const [label, test] of GROUPS) {
      const row = document.createElement('div');
      row.className = 'vgroup';
      const l = document.createElement('span');
      l.textContent = label;
      row.appendChild(l);
      state.versions.filter(test).reverse().forEach((v) => {
        const b = document.createElement('button');
        b.className = 'chip' + (v.mc === selected ? ' sel' : '') + (v.installed ? ' inst' : '');
        b.textContent = v.mc;
        b.dataset.mc = v.mc;
        row.appendChild(b);
      });
      box.appendChild(row);
    }
    renderInfo();
  }
  $('versions').addEventListener('click', (e) => {
    const b = e.target.closest('.chip');
    if (!b || busy) return;
    selected = b.dataset.mc;
    document.querySelectorAll('.chip').forEach((c) => c.classList.toggle('sel', c.dataset.mc === selected));
    renderInfo();
    closeModal();
  });

  function openModal() {
    if (busy) return;
    $('modal').classList.remove('hidden');
    const sel = document.querySelector('.chip.sel');
    if (sel) sel.focus();
  }
  function closeModal() { $('modal').classList.add('hidden'); }
  $('btn-version').addEventListener('click', openModal);
  $('modal-close').addEventListener('click', closeModal);
  $('modal').addEventListener('mousedown', (e) => { if (e.target === $('modal')) closeModal(); });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeModal(); });

  function renderInfo() {
    const v = state.versions.find((x) => x.mc === selected);
    const box = $('vinfo');
    box.textContent = '';
    if (!v) return;
    $('vname').textContent = v.mc;
    box.append(
      pill('Minecraft ' + v.mc),
      pill('Forge ' + v.forge),
      pill('Java ' + v.java),
      pill(((state.settings.ram / 1024) % 1 ? (state.settings.ram / 1024).toFixed(1) : state.settings.ram / 1024) + ' GB RAM'),
      v.installed ? pill('Installed', 'ok') : pill('Downloads on first play')
    );
  }

  function render() {
    const acc = state.account;
    $('who').textContent = '';
    if (acc) {
      const n = document.createElement('span');
      n.textContent = acc.name;
      const s = document.createElement('small');
      s.textContent = acc.type === 'microsoft' ? 'Microsoft account' : 'Offline mode';
      $('who').append(n, s);
    } else {
      $('who').textContent = 'Not signed in';
    }
    $('avatar').textContent = acc ? acc.name.charAt(0).toUpperCase() : '?';
    $('btn-login').classList.toggle('hidden', !!acc && acc.type === 'microsoft');
    $('btn-offline').classList.toggle('hidden', !!acc);
    $('btn-logout').classList.toggle('hidden', !acc);

    if (!selected || !state.versions.some((v) => v.mc === selected)) {
      selected = state.versions.some((v) => v.mc === state.settings.version) ? state.settings.version : state.versions[state.versions.length - 1].mc;
    }
    renderVersions();

    const s = state.settings;
    const max = Math.max(2048, Math.min(32768, Math.floor((state.sysRam || 16384) * 0.75 / 512) * 512));
    $('set-ram').max = max;
    $('set-ram').value = Math.min(s.ram, max);
    ramLabel();
    $('ram-hint').textContent = 'Your PC has ' + Math.round((state.sysRam || 0) / 1024) + ' GB. 3-6 GB is enough for most versions.';
    $('set-w').value = s.width;
    $('set-h').value = s.height;
    $('set-full').checked = !!s.fullscreen;
    $('set-dir').value = s.gameDir || '';
    $('set-java').value = s.javaPath || '';
    $('set-offline').value = s.offlineName || 'Altair';
    $('set-jvm').value = s.jvmArgs === undefined ? '' : s.jvmArgs;
    $('lver').textContent = 'v' + state.version;
    $('notice').textContent = 'Launcher folder: ' + state.base;
  }

  function ramLabel() {
    const mb = parseInt($('set-ram').value, 10);
    $('ram-out').textContent = (mb / 1024 % 1 ? (mb / 1024).toFixed(1) : mb / 1024) + ' GB';
  }
  $('set-ram').addEventListener('input', ramLabel);

  function setBusy(v) {
    busy = v;
    $('btn-play').disabled = v;
    $('btn-version').disabled = v;
    $('play-label').textContent = v ? 'WORKING...' : 'PLAY';
    if (!v) $('progress').classList.add('hidden');
  }

  // ---- navigation ----
  document.querySelectorAll('.nav').forEach((b) => b.addEventListener('click', () => {
    document.querySelectorAll('.nav').forEach((x) => x.classList.toggle('active', x === b));
    ['play', 'settings', 'log'].forEach((p) => $('page-' + p).classList.toggle('hidden', p !== b.dataset.page));
  }));
  $('btn-min').addEventListener('click', () => A.win('min'));
  $('btn-close').addEventListener('click', () => A.win('close'));

  // ---- account ----
  $('btn-login').addEventListener('click', async () => {
    setStatus('Opening Microsoft sign-in...');
    const r = await A.loginMicrosoft();
    if (r.ok) { state = r.state; render(); setStatus('Signed in as ' + state.account.name + '.'); }
    else setStatus('Sign-in failed: ' + r.error, true);
  });
  $('btn-offline').addEventListener('click', async () => {
    state = await A.loginOffline($('set-offline').value || state.settings.offlineName);
    render();
    setStatus('Offline mode: you cannot join servers that require authentication.');
  });
  $('btn-logout').addEventListener('click', async () => { state = await A.logout(); render(); setStatus('Signed out.'); });

  // ---- play ----
  $('btn-play').addEventListener('click', async () => {
    if (busy) return;
    if (!state.account) { setStatus('Sign in first (Microsoft or offline).', true); return; }
    setBusy(true);
    setStatus('Preparing...');
    const r = await A.play(selected);
    // On success the window is closed while Minecraft runs and recreated afterwards; this code only runs on errors.
    setBusy(false);
    if (r && r.ok) { state = r.state; render(); }
    else if (r) setStatus('Error: ' + r.error, true);
  });

  // ---- settings ----
  $('btn-dir').addEventListener('click', async () => { const d = await A.pickFolder(); if (d) $('set-dir').value = d; });
  $('btn-dir-clear').addEventListener('click', () => { $('set-dir').value = ''; });
  $('btn-open').addEventListener('click', () => A.openFolder('base'));
  $('btn-save').addEventListener('click', async () => {
    state = await A.saveSettings({
      ram: parseInt($('set-ram').value, 10) || 4096,
      width: parseInt($('set-w').value, 10) || 1280,
      height: parseInt($('set-h').value, 10) || 720,
      fullscreen: $('set-full').checked,
      gameDir: $('set-dir').value,
      javaPath: $('set-java').value.trim(),
      offlineName: $('set-offline').value.trim() || 'Altair',
      jvmArgs: $('set-jvm').value.trim()
    });
    render();
    $('saved').textContent = 'Saved';
    $('saved').classList.add('ok');
    setTimeout(() => { $('saved').textContent = ''; }, 2000);
  });

  // ---- events from the main process ----
  A.onLog(addLog);
  A.onStatus((m) => setStatus(m, /^Error/.test(m)));
  A.onBusy(setBusy);
  A.onLaunched(() => {});
  A.onProgress(({ label, done, total, active }) => {
    if (!active) { $('progress').classList.add('hidden'); return; }
    $('progress').classList.remove('hidden');
    const pct = total ? Math.min(100, Math.round((done / total) * 100)) : 0;
    $('bar').style.width = pct + '%';
    if (total && total > 1000) setStatus(label + ': ' + (done / 1048576).toFixed(1) + ' / ' + (total / 1048576).toFixed(1) + ' MB');
    else if (total) setStatus(label + ': ' + done + ' / ' + total);
  });

  A.state().then((s) => { state = s; render(); });
})();
