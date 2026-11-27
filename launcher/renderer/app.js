(() => {
  const $ = (id) => document.getElementById(id);
  const A = window.altair;
  let state = null;
  let busy = false;

  const MAX_LOG = 4000;
  const logEl = $('log');
  let logLines = 0;
  function addLog(chunk) {
    logEl.textContent += chunk + '\n';
    logLines += chunk.split('\n').length - 1;
    if (++logLines > MAX_LOG) {
      logEl.textContent = logEl.textContent.split('\n').slice(-MAX_LOG / 2).join('\n');
      logLines = MAX_LOG / 2;
    }
    logEl.scrollTop = logEl.scrollHeight;
  }

  function setStatus(msg, err) {
    $('status').textContent = msg;
    $('status').classList.toggle('err', !!err);
  }

  function render() {
    // cuenta
    const acc = state.account;
    $('who').innerHTML = acc ? '' : 'Not signed in';
    if (acc) {
      const n = document.createElement('span');
      n.textContent = acc.name;
      const s = document.createElement('small');
      s.textContent = acc.type === 'microsoft' ? 'Microsoft account' : 'Offline mode';
      $('who').append(n, s);
    }
    $('btn-login').classList.toggle('hidden', !!acc && acc.type === 'microsoft');
    $('btn-offline').classList.toggle('hidden', !!acc);
    $('btn-logout').classList.toggle('hidden', !acc);

    // versiones
    const sel = $('version');
    const cur = sel.value || state.settings.version;
    sel.innerHTML = '';
    const groups = [['26.x', (v) => v.mc.startsWith('26.')], ['1.21.x', (v) => v.mc.startsWith('1.21')], ['1.20.x', (v) => v.mc.startsWith('1.20')]];
    for (const [label, test] of groups) {
      const og = document.createElement('optgroup');
      og.label = label;
      state.versions.filter(test).reverse().forEach((v) => {
        const o = document.createElement('option');
        o.value = v.mc;
        o.textContent = v.mc + (v.installed ? '  \u2713' : '');
        og.appendChild(o);
      });
      sel.appendChild(og);
    }
    sel.value = state.versions.some((v) => v.mc === cur) ? cur : state.versions[state.versions.length - 1].mc;
    updateBadge();

    // ajustes
    const s = state.settings;
    $('set-ram').value = s.ram;
    $('set-w').value = s.width;
    $('set-h').value = s.height;
    $('set-full').checked = !!s.fullscreen;
    $('set-dir').value = s.gameDir || '';
    $('set-java').value = s.javaPath || '';
    $('set-offline').value = s.offlineName || 'Altair';
    $('set-jvm').value = s.jvmArgs === undefined ? '' : s.jvmArgs;
    $('notice').textContent = 'Launcher folder: ' + state.base;
  }

  function updateBadge() {
    const v = state.versions.find((x) => x.mc === $('version').value);
    const b = $('installed');
    if (!v) return;
    b.textContent = v.installed ? 'Installed' : 'Downloads on first play (Java ' + v.java + ')';
    b.classList.toggle('ok', v.installed);
  }

  function setBusy(v) {
    busy = v;
    $('btn-play').disabled = v;
    $('btn-play').textContent = v ? 'WORKING...' : 'PLAY';
    $('version').disabled = v;
    if (!v) $('progress').classList.add('hidden');
  }

  document.querySelectorAll('.nav').forEach((b) => b.addEventListener('click', () => {
    document.querySelectorAll('.nav').forEach((x) => x.classList.toggle('active', x === b));
    ['play', 'settings', 'log'].forEach((p) => $('page-' + p).classList.toggle('hidden', p !== b.dataset.page));
  }));

  $('version').addEventListener('change', updateBadge);
  $('btn-min').addEventListener('click', () => A.win('min'));
  $('btn-close').addEventListener('click', () => A.win('close'));

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

  $('btn-play').addEventListener('click', async () => {
    if (busy) return;
    if (!state.account) { setStatus('Sign in first (Microsoft or offline).', true); return; }
    setBusy(true);
    setStatus('Preparing...');
    const r = await A.play($('version').value);
    setBusy(false);
    if (r.ok) { state = r.state; render(); }
    else setStatus('Error: ' + r.error, true);
  });

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

  A.onLog(addLog);
  A.onStatus((m) => setStatus(m, /^Error/.test(m)));
  A.onBusy(setBusy);
  A.onLaunched(() => { $('progress').classList.add('hidden'); });
  A.onProgress(({ label, done, total, active }) => {
    if (!active) { $('progress').classList.add('hidden'); return; }
    $('progress').classList.remove('hidden');
    const pct = total ? Math.min(100, Math.round((done / total) * 100)) : 0;
    $('bar').style.width = pct + '%';
    if (total && typeof done === 'number' && total > 1000) setStatus(label + ': ' + (done / 1048576).toFixed(1) + ' / ' + (total / 1048576).toFixed(1) + ' MB');
    else if (total) setStatus(label + ': ' + done + ' / ' + total);
  });

  A.state().then((s) => { state = s; render(); });
})();
