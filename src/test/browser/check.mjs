// Browser regression check for the templates and static JS (no npm dependencies).
//
//   CHROME_BIN=/path/to/chrome node src/test/browser/check.mjs
//
// A local fixture server serves the real templates and static files, and fakes
// /counter/{name} and /success/{rate}, so the check controls every response and
// never calls sample-node. The CDN assets are loaded from the network.
import { spawn } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { createServer } from 'node:http';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const RES = new URL('../../main/resources/', import.meta.url).pathname;
const CHROME = process.env.CHROME_BIN || 'google-chrome';
const SIZES = [[1280, 800], [375, 812]];
const PAGES = ['/', '/drop/100', '/drop/0'];

let log = [];
let counts = { up: 5, down: 3 };
let result;

// Runs in the page after the app scripts; reports what it observed to /__result.
const PROBE = `(async () => {
  const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
  const until = async (fn) => { for (let i = 0; i < 100 && !fn(); i++) await sleep(50); return fn(); };
  const getLog = async () => (await fetch('/__log')).json();
  const css = (sel, prop, pseudo) => {
    const el = document.querySelector(sel);
    return el ? getComputedStyle(el, pseudo)[prop] : null;
  };
  await new Promise((r) => (document.readyState === 'complete' ? r() : addEventListener('load', r)));
  await document.fonts.ready;
  const o = { jquery: jQuery.fn.jquery, overflow: document.documentElement.scrollWidth > innerWidth,
    heroPadding: css('.hero', 'paddingTop') };
  if (location.pathname === '/') {
    o.btnColors = [css('.btn-thumbs-up', 'backgroundColor'), css('.btn-thumbs-down', 'backgroundColor')];
    o.iconWidths = [...document.querySelectorAll('.btn i')].map((i) => i.getBoundingClientRect().width);
    o.iconFont = css('.fa-thumbs-up', 'fontFamily', '::before');
    o.iconFontLoaded = document.fonts.check('400 16px "Font Awesome 7 Free"');
    const text = () => [$('#thumbs-up-count').text(), $('#thumbs-down-count').text()].join(',');
    o.initial = (await until(() => text() === '5,3')) && text();
    const start = (await getLog()).length;
    document.querySelector('.btn-thumbs-up').click();
    document.querySelector('.btn-thumbs-down').click();
    o.afterClick = (await until(() => text() === '6,4')) && text();
    await sleep(2000);
    const after = (await getLog()).slice(start);
    o.writes = after.filter((r) => r.method !== 'GET').map((r) => r.method + ' ' + r.path);
    o.pollsPerName = after.filter((r) => r.path === '/counter/up').length;
  } else {
    await sleep(2500);
    const canvas = document.getElementById('drop-canvas');
    o.canvas = !!canvas && canvas.width === innerWidth && canvas.height === innerHeight;
    o.segments = [...document.querySelectorAll('#drop-rate .progress-stacked > .progress')].map((p) => ({
      label: p.getAttribute('aria-label'), width: parseFloat(p.style.width),
      color: getComputedStyle(p.querySelector('.progress-bar')).backgroundColor }));
    o.successCalls = (await getLog()).filter((r) => r.path.startsWith('/success/')).length;
  }
  await fetch('/__result', { method: 'POST', body: JSON.stringify(o) });
})();`;

function template(name, rate) {
  const html = readFileSync(join(RES, 'templates', name), 'utf8')
    .replace('/*[[${rate}]]*/ "0"', JSON.stringify(rate))
    .replace('</body>', '<script src="/__probe.js"></script>\n</body>');
  return ['text/html', html];
}

const server = createServer((req, res) => {
  const path = req.url.split('?')[0];
  const send = (status, type, body) => res.writeHead(status, { 'Content-Type': type }).end(body);
  if (path.startsWith('/counter/') || path.startsWith('/success/')) log.push({ method: req.method, path });
  let m;
  if (path === '/') return send(200, ...template('index.html'));
  if ((m = path.match(/^\/drop\/(\d+)$/))) return send(200, ...template('drop.html', m[1]));
  if (path === '/__probe.js') return send(200, 'text/javascript', PROBE);
  if (path === '/__log') return send(200, 'application/json', JSON.stringify(log));
  if ((m = path.match(/^\/counter\/(up|down)$/))) {
    if (req.method !== 'GET') counts[m[1]]++;
    return send(200, 'text/plain', String(counts[m[1]]));
  }
  if ((m = path.match(/^\/success\/(\d+)$/))) {
    return m[1] === '100'
      ? send(200, 'application/json', JSON.stringify({ result: 'OK', version: 'v-fixture' }))
      : send(500, 'application/json', '{"error":"fixture failure"}');
  }
  if (path === '/__result') {
    let body = '';
    req.on('data', (d) => (body += d)).on('end', () => { result = JSON.parse(body); send(204, 'text/plain', ''); });
    return;
  }
  if (path.startsWith('/static/') && !path.includes('..')) {
    try {
      const type = path.endsWith('.js') ? 'text/javascript' : path.endsWith('.css') ? 'text/css' : 'image/png';
      return send(200, type, readFileSync(join(RES, path)));
    } catch { /* fall through */ }
  }
  send(404, 'text/plain', 'not found');
});

async function visit(url, [width, height]) {
  log = [];
  counts = { up: 5, down: 3 };
  result = undefined;
  const profile = mkdtempSync(join(tmpdir(), 'check-chrome-'));
  // detached: Chrome gets its own process group, so killing the group also stops its
  // helper processes before the profile directory is removed.
  const chrome = spawn(CHROME, ['--headless', '--no-sandbox', '--disable-gpu', '--remote-debugging-port=0',
    `--user-data-dir=${profile}`, `--window-size=${width},${height}`, url], { stdio: 'ignore', detached: true });
  let running = true;
  const closed = new Promise((r) => chrome.once('close', () => { running = false; r(); }));
  chrome.once('error', (e) => console.error(`cannot start ${CHROME}: ${e.message}`));
  for (let i = 0; i < 300 && !result && running; i++) await new Promise((r) => setTimeout(r, 100));
  if (chrome.pid) try { process.kill(-chrome.pid, 'SIGKILL'); } catch (e) { if (e.code !== 'ESRCH') throw e; }
  await closed;
  rmSync(profile, { recursive: true, force: true, maxRetries: 10, retryDelay: 100 });
  return result;
}

function expectations(page, width, o) {
  const crimson = 'rgba(220, 20, 60, 0.9)';
  const total = (o.segments || []).reduce((s, x) => s + x.width, 0);
  const common = {
    'jQuery 4.0.0': o.jquery === '4.0.0',
    'no horizontal overflow': !o.overflow,
    'hero padding': o.heroPadding === (width < 576 ? '32px' : '64px'),
  };
  if (page === '/') return { ...common,
    'Bootstrap 5 button colors': o.btnColors.join() === 'rgb(13, 110, 253),rgb(220, 53, 69)',
    'Font Awesome 7 icons render': o.iconFontLoaded && /Font Awesome 7/.test(o.iconFont) && o.iconWidths.every((w) => w > 0),
    'initial GET fills counts': o.initial === '5,3',
    'click response updates counts': o.afterClick === '6,4',
    'one POST up and one DELETE down per click': o.writes.join() === 'POST /counter/up,DELETE /counter/down',
    'single 1s GET poll': o.pollsPerName >= 1 && o.pollsPerName <= 3,
  };
  return { ...common,
    'canvas fills viewport': o.canvas,
    'calls /success/{rate}': o.successCalls > 0,
    'stacked progress sums to 100%': o.segments.length > 0 && Math.abs(total - 100) < 0.01,
    [page === '/drop/0' ? 'failure is crimson' : 'success uses version color']: page === '/drop/0'
      ? o.segments.every((s) => s.label === 'error' && s.color === crimson)
      : o.segments.every((s) => s.label === 'version v-fixture' && s.color !== crimson),
  };
}

await new Promise((r) => server.listen(0, '127.0.0.1', r));
const base = `http://127.0.0.1:${server.address().port}`;
let failed = 0;
for (const size of SIZES) {
  for (const page of PAGES) {
    const o = await visit(base + page, size);
    const checks = o ? expectations(page, size[0], o) : { 'page reported a result': false };
    for (const [name, ok] of Object.entries(checks)) {
      if (!ok) failed++;
      console.log(`${ok ? 'ok  ' : 'FAIL'} ${size.join('x')} ${page} ${name}`);
    }
    if (o && Object.values(checks).includes(false)) console.log('     observed:', JSON.stringify(o));
  }
}
server.close();
process.exit(failed ? 1 : 0);
