/* ═══════════════════════════════════════════
   PARKABLE — main.js
   Efectos visuales + cursor glow + idioma ES/EN
═══════════════════════════════════════════ */

// ── CURSOR GLOW ──────────────────────────────
const glow = document.createElement('div');
glow.style.cssText = `
  position:fixed; pointer-events:none; z-index:9999;
  width:420px; height:420px; border-radius:50%;
  background:radial-gradient(circle,
    rgba(124,179,66,.18) 0%,
    rgba(26,110,158,.10) 35%,
    transparent 70%);
  transform:translate(-50%,-50%);
  opacity:0; filter:blur(2px);
  mix-blend-mode:screen;
  transition:opacity .4s ease;
`;
document.body.appendChild(glow);

let mx = 0, my = 0, gx = 0, gy = 0, glowOn = false;

document.addEventListener('mousemove', e => {
  mx = e.clientX; my = e.clientY;
  if (!glowOn) { glow.style.opacity = '1'; glowOn = true; }
});
document.addEventListener('mouseleave', () => { glow.style.opacity = '0'; glowOn = false; });

(function lerpGlow() {
  gx += (mx - gx) * 0.08;
  gy += (my - gy) * 0.08;
  glow.style.left = gx + 'px';
  glow.style.top  = gy + 'px';
  requestAnimationFrame(lerpGlow);
})();

// ── TRAIL DE PARTÍCULAS ──────────────────────
const trailCanvas = document.createElement('canvas');
trailCanvas.style.cssText = 'position:fixed;inset:0;pointer-events:none;z-index:9998;opacity:.55;';
document.body.appendChild(trailCanvas);
const tctx = trailCanvas.getContext('2d');

const resizeTrail = () => { trailCanvas.width = innerWidth; trailCanvas.height = innerHeight; };
resizeTrail(); addEventListener('resize', resizeTrail);

const particles = [];
class Particle {
  constructor(x, y) {
    this.x = x; this.y = y;
    this.size  = Math.random() * 3.2 + 1;
    this.alpha = Math.random() * 0.7 + 0.3;
    this.vx    = (Math.random() - .5) * 1.2;
    this.vy    = (Math.random() - .5) * 1.2 - .4;
    this.decay = Math.random() * .025 + .015;
    this.color = Math.random() > .5 ? '124,179,66' : '26,110,158';
  }
  update() { this.x += this.vx; this.y += this.vy; this.alpha -= this.decay; this.size *= .97; }
  draw() {
    tctx.save();
    tctx.globalAlpha = Math.max(0, this.alpha);
    tctx.fillStyle = `rgba(${this.color},1)`;
    tctx.shadowBlur = 8; tctx.shadowColor = `rgba(${this.color},.9)`;
    tctx.beginPath(); tctx.arc(this.x, this.y, this.size, 0, Math.PI*2); tctx.fill();
    tctx.restore();
  }
}

let lastTrail = 0;
document.addEventListener('mousemove', e => {
  const now = Date.now();
  if (now - lastTrail > 28) {
    particles.push(new Particle(e.clientX, e.clientY));
    if (particles.length > 30) particles.shift();
    lastTrail = now;
  }
});

(function animTrail() {
  tctx.clearRect(0, 0, trailCanvas.width, trailCanvas.height);
  for (let i = particles.length - 1; i >= 0; i--) {
    particles[i].update(); particles[i].draw();
    if (particles[i].alpha <= 0) particles.splice(i, 1);
  }
  requestAnimationFrame(animTrail);
})();

// ── PARTÍCULAS FLOTANTES DE FONDO ────────────
const bgCanvas = document.createElement('canvas');
bgCanvas.style.cssText = 'position:fixed;inset:0;pointer-events:none;z-index:0;opacity:.35;';
document.body.appendChild(bgCanvas);
const bctx = bgCanvas.getContext('2d');

const resizeBg = () => { bgCanvas.width = innerWidth; bgCanvas.height = innerHeight; };
resizeBg(); addEventListener('resize', resizeBg);

const dots = Array.from({length: 55}, () => ({
  x: Math.random() * innerWidth,
  y: Math.random() * innerHeight,
  r: Math.random() * 1.8 + .4,
  vy: -(Math.random() * .35 + .1),
  vx: (Math.random() - .5) * .18,
  a: Math.random() * .5 + .1,
  c: ['124,179,66','26,110,158','38,166,154'][Math.floor(Math.random()*3)]
}));

(function animBg() {
  bctx.clearRect(0, 0, bgCanvas.width, bgCanvas.height);
  dots.forEach(d => {
    d.y += d.vy; d.x += d.vx;
    if (d.y < -5) { d.y = bgCanvas.height + 5; d.x = Math.random() * bgCanvas.width; }
    bctx.save();
    bctx.globalAlpha = d.a;
    bctx.fillStyle = `rgba(${d.c},1)`;
    bctx.shadowBlur = 6; bctx.shadowColor = `rgba(${d.c},.8)`;
    bctx.beginPath(); bctx.arc(d.x, d.y, d.r, 0, Math.PI*2); bctx.fill();
    bctx.restore();
  });
  requestAnimationFrame(animBg);
})();

// ── RIPPLE AL HACER CLICK ────────────────────
const rippleCSS = document.createElement('style');
rippleCSS.textContent = `@keyframes ripple-out{to{transform:translate(-50%,-50%) scale(18);opacity:0}}`;
document.head.appendChild(rippleCSS);

document.addEventListener('click', e => {
  const r = document.createElement('div');
  r.style.cssText = `position:fixed;left:${e.clientX}px;top:${e.clientY}px;
    width:8px;height:8px;border-radius:50%;pointer-events:none;z-index:9997;
    border:1.5px solid rgba(124,179,66,.8);
    transform:translate(-50%,-50%) scale(1);
    animation:ripple-out .6s ease forwards;`;
  document.body.appendChild(r);
  setTimeout(() => r.remove(), 650);
});

// ── NAV SCROLL ───────────────────────────────
const nav = document.getElementById('nav');
addEventListener('scroll', () => nav.classList.toggle('scrolled', scrollY > 24), {passive:true});

// ── BURGER MENU ──────────────────────────────
const burger     = document.getElementById('burger');
const navLinks   = document.getElementById('navLinks');
const navActions = document.getElementById('navActions');
let menuOpen = false;

const burgerCSS = document.createElement('style');
burgerCSS.textContent = `
  @keyframes slideDown{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:none}}
  .nav__burger.open span:nth-child(1){transform:translateY(7px) rotate(45deg)}
  .nav__burger.open span:nth-child(2){opacity:0}
  .nav__burger.open span:nth-child(3){transform:translateY(-7px) rotate(-45deg)}
`;
document.head.appendChild(burgerCSS);

burger?.addEventListener('click', () => {
  menuOpen = !menuOpen;
  burger.classList.toggle('open', menuOpen);
  if (menuOpen) {
    navLinks.style.cssText = `display:flex;flex-direction:column;gap:24px;
      position:fixed;top:68px;left:0;right:0;
      background:rgba(23,23,33,.97);backdrop-filter:blur(20px);
      padding:28px 32px;border-bottom:1px solid rgba(112,112,125,.2);
      z-index:99;animation:slideDown .2s ease;`;
    navActions.style.cssText = `display:flex;flex-direction:column;gap:12px;
      position:fixed;top:calc(68px + 200px);left:0;right:0;
      background:rgba(23,23,33,.97);backdrop-filter:blur(20px);
      padding:0 32px 28px;z-index:99;`;
  } else {
    navLinks.style.cssText = '';
    navActions.style.cssText = '';
  }
});

const closeMenu = () => {
  if (!menuOpen) return;
  menuOpen = false;
  burger?.classList.remove('open');
  navLinks.style.cssText = '';
  navActions.style.cssText = '';
};

// ── SMOOTH SCROLL ────────────────────────────
document.querySelectorAll('a[href^="#"]').forEach(a => {
  a.addEventListener('click', e => {
    const t = document.querySelector(a.getAttribute('href'));
    if (!t) return;
    e.preventDefault();
    window.scrollTo({top: t.getBoundingClientRect().top + scrollY - 72, behavior:'smooth'});
    closeMenu();
  });
});

// ── HOW TABS ─────────────────────────────────
document.querySelectorAll('.how__tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.how__tab').forEach(t => t.classList.remove('how__tab--active'));
    tab.classList.add('how__tab--active');
    document.querySelectorAll('.how__panel').forEach(p => p.classList.remove('how__panel--active'));
    const panel = document.querySelector(`.how__panel[data-panel="${tab.dataset.tab}"]`);
    if (!panel) return;
    panel.classList.add('how__panel--active');
    panel.querySelectorAll('.how__step').forEach((s, i) => {
      s.style.opacity = '0'; s.style.transform = 'translateY(16px)';
      setTimeout(() => {
        s.style.transition = 'opacity .4s ease,transform .4s ease';
        s.style.opacity = '1'; s.style.transform = 'none';
      }, i * 100);
    });
  });
});

// ── SCROLL REVEAL ────────────────────────────
const revealObs = new IntersectionObserver(entries => {
  entries.forEach(e => {
    if (!e.isIntersecting) return;
    setTimeout(() => e.target.classList.add('visible'), +e.target.dataset.delay || 0);
    revealObs.unobserve(e.target);
  });
}, {threshold:.1, rootMargin:'0px 0px -30px 0px'});

document.querySelectorAll('.reveal,.feat,.testi,.why__card,.impact__card,.earn-item,.catalog-item').forEach((el,i) => {
  el.classList.add('reveal');
  el.dataset.delay = (i % 4) * 90;
  revealObs.observe(el);
});

// ── ECO BAR ──────────────────────────────────
const ecoObs = new IntersectionObserver(entries => {
  entries.forEach(e => { if (e.isIntersecting) { e.target.style.width='65%'; ecoObs.unobserve(e.target); } });
}, {threshold:.3});
document.querySelectorAll('.feat__eco-fill').forEach(el => {
  el.style.width='0'; el.style.transition='width 1.4s ease'; ecoObs.observe(el);
});

// ══════════════════════════════════════════════
// SISTEMA DE IDIOMA ES / EN
// ══════════════════════════════════════════════
const T = {
  es: {
    nav_problem:'El problema', nav_how:'Cómo funciona', nav_features:'Funcionalidades',
    nav_impact:'Impacto eco', nav_rewards:'Recompensas',
    nav_login:'Iniciar sesión', nav_download:'Descargar gratis',
    hero_badge:'Movilidad urbana sostenible',
    hero_h1_1:'Aparca mejor.', hero_h1_2:'Contamina menos.',
    hero_sub:'Encuentra una plaza en menos de 2 minutos o comparte la tuya cuando salgas. Menos tiempo circulando, menos CO₂, más ciudad para todos.',
    hero_cta1:'Descargar en Android', hero_cta2:'Ver cómo funciona →',
    stat1_num:'−90%', stat1_lbl:'tiempo buscando aparcamiento',
    stat2_num:'−2 kg', stat2_lbl:'CO₂ evitado por viaje',
    stat3_num:'100%', stat3_lbl:'gratuito para conductores',
    trust1:'Ciudades más habitables', trust2:'Menos emisiones de CO₂',
    trust3:'Ahorra tiempo cada día', trust4:'Genera ingresos con tu garaje',
    trust5:'Comunidad de conductores', trust6:'Compromiso con el planeta',
    why_label:'El problema',
    why_title:'30 minutos buscando aparcamiento.<br/><span class="green-text">Cada día.</span>',
    why_sub1:'El conductor urbano medio pierde más de 100 horas al año dando vueltas sin encontrar dónde aparcar. Eso equivale a quemar combustible de forma innecesaria, contaminar más y llegar tarde a todas partes.',
    why_sub2:'Parkable resuelve los dos lados: conecta en tiempo real a quien necesita plaza con quien tiene una libre, reduciendo drásticamente el tiempo y las emisiones.',
    why_cta:'Ver la solución →',
    card_bad1_num:'30 min', card_bad1_lbl:'tiempo medio buscando aparcamiento en ciudad',
    card_bad2_num:'18 kg',  card_bad2_lbl:'CO₂ emitido innecesariamente por conductor al año',
    card_good1_num:'2 min', card_good1_lbl:'tiempo medio con Parkable',
    card_good2_num:'−90%',  card_good2_lbl:'emisiones evitadas por viaje',
    how_label:'Cómo funciona', how_title:'Tan fácil como parece.',
    how_sub:'Elige cómo quieres usar Parkable hoy.',
    tab_find:'🔍 Buscar plaza', tab_share:'🅿️ Alquilar mi garaje', tab_alert:'📍 Aviso en la calle',
    find_s1_h:'Abre el mapa', find_s1_p:'Ve en el mapa todas las plazas disponibles cerca de tu destino, con precio, fotos y disponibilidad en tiempo real.',
    find_s2_h:'Elige y reserva', find_s2_p:'Filtra por horas, días o meses. Consulta las fotos y reserva con un toque. El pago es automático.',
    find_s3_h:'Aparca sin estrés', find_s3_p:'Tu plaza te espera. Llegas directo, sin dar vueltas, sin contaminación innecesaria, sin estrés.',
    share_s1_h:'Publica tu plaza', share_s1_p:'Sube fotos de tu garaje, marca la ubicación en el mapa y fija tu precio. En menos de 5 minutos está online.',
    share_s2_h:'Recibe reservas automáticas', share_s2_p:'Los conductores reservan y pagan solos. Tú recibes una notificación y el dinero, sin gestionar nada.',
    share_s3_h:'Gana entre 50 € y 200 € al mes', share_s3_p:'Una plaza vacía es dinero perdido. Parkable la convierte en ingresos pasivos sin esfuerzo para ti.',
    alert_s1_h:'Vas a salir', alert_s1_p:'Antes de arrancar, pulsa "Publicar aviso". Indicas en cuántos minutos sales y la ubicación exacta.',
    alert_s2_h:'Otro conductor la reclama', alert_s2_p:'Un conductor cercano que necesita aparcar la reclama al instante. Ambos confirmáis el intercambio.',
    alert_s3_h:'Los dos ganáis puntos', alert_s3_p:'Tú ganas 100 puntos por ofrecer. El otro conductor gana 25 puntos. Canjéalos por recompensas reales.',
    feat_label:'Funcionalidades', feat_title:'Todo lo que necesitas<br/>en una sola app.',
    f1_h:'Mapa en tiempo real', f1_p:'Visualiza plazas disponibles en el mapa o en lista. Se actualiza al instante cuando aparece una nueva.',
    f2_h:'Fotos reales del garaje', f2_p:'Cada plaza incluye mínimo 5 fotos para que sepas exactamente lo que reservas. Sin sorpresas.',
    f3_h:'4 modalidades de alquiler', f3_p:'Reserva por horas, días, semanas o meses. El precio se calcula automáticamente.',
    f4_h:'Tu huella de carbono', f4_p:'Seguimos el CO₂ que ahorras con cada reserva. Lo puedes ver en tu perfil y compartir.', f4_eco:'CO₂ ahorrado hoy',
    f5_h:'Avisos instantáneos', f5_p:'Recibe una notificación cuando alguien deje libre una plaza cerca de donde necesitas aparcar.',
    f6_h:'Recompensas por colaborar', f6_p:'Cada aviso que publiques te genera puntos canjeables por premios reales. Colaborar tiene su recompensa.',
    f7_h:'Pago integrado', f7_p:'Paga desde la app en segundos. Sin efectivo, sin transferencias. Seguro y automático.',
    f8_h:'Disponible en dos idiomas', f8_p:'Cambia entre español e inglés en cualquier momento desde los ajustes.',
    impact_label:'Impacto ambiental', impact_title:'Pequeños gestos.<br/>Gran cambio.',
    impact_sub:'Buscar aparcamiento genera millones de toneladas de CO₂ al año en todo el mundo. Parkable reduce esa cifra.',
    ic1_num:'2 kg', ic1_lbl:'CO₂ evitado por conductor y semana', ic1_note:'Equivalente a no encender el coche un día entero',
    ic2_num:'1 árbol', ic2_lbl:'es lo que Parkable ahorra en emisiones en un solo mes de uso', ic2_note:'Un árbol adulto absorbe ~21 kg CO₂/año',
    ic3_num:'€180', ic3_lbl:'en combustible ahorrado por conductor al año', ic3_note:'Basado en 20 min de búsqueda diaria',
    mission:'<strong>Nuestra misión:</strong> hacer que las ciudades respiren mejor, una plaza a la vez. Cada garaje compartido es un coche que circula menos.',
    pts_label:'Sistema de recompensas', pts_title:'Colaborar tiene<br/>su premio.',
    pts_sub:'Cada aviso de plaza libre genera puntos automáticamente para ambos conductores. Canjéalos por premios reales.',
    pts_earn_title:'¿Cómo gano puntos?', pts_cat_title:'¿Qué puedo canjear?',
    e1_strong:'Ofrecí mi plaza en la calle', e1_span:'Publicaste un aviso y otro conductor la aprovechó',
    e2_strong:'Aparqué gracias a un aviso', e2_span:'Reclamaste una plaza libre avisada por otro',
    r1_name:'Lavado de coche', r1_sub:'En gasolineras adheridas',
    r2_name:'5 € en gasolina', r2_sub:'En repostajes desde 30 €',
    r3_name:'Semana Premium', r3_sub:'Reservas prioritarias sin comisión',
    r4_name:'Café gratis', r4_sub:'En áreas de servicio asociadas',
    testi_label:'Usuarios reales', testi_title:'Ya lo están usando.',
    t1_text:'"Antes tardaba 25 minutos buscando aparcamiento cerca del trabajo. Ahora reservo desde el desayuno y llego directo. Sin estrés."',
    t2_text:'"Tenía el garaje vacío cinco días a la semana. Ahora genera 120 € al mes sin que yo tenga que hacer nada. No me lo creía."',
    t3_text:'"Lo de los puntos es adictivo. Cada vez que aviso que salgo de mi plaza, gano puntos. Ya he canjeado dos lavados de coche."',
    t2_sub:'Propietario · Málaga',
    dl_deco:'', dl_title:'Únete a la movilidad<br/>del futuro.',
    dl_sub:'Gratuito para conductores. Sin tarjeta de crédito. Disponible para Android.',
    dl_store_label:'Disponible en', dl_store_name:'Google Play', dl_demo:'Ver demo',
    dl_b1:'Gratis', dl_b2:'Android 8+', dl_b3:'Sostenible', dl_b4:'Seguro',
    ft_tagline:'Smart Parking. Shared Spaces.', ft_eco:'Comprometidos con la movilidad sostenible',
    ft_col1:'Producto', ft_col2:'Empresa', ft_col3:'Legal',
    ft_l1:'El problema', ft_l2:'Cómo funciona', ft_l3:'Funcionalidades', ft_l4:'Descargar',
    ft_l5:'Impacto ambiental', ft_l6:'Sobre nosotros', ft_l7:'Prensa', ft_l8:'Contacto',
    ft_l9:'Privacidad', ft_l10:'Términos de uso', ft_l11:'Cookies',
    ft_copy:'© 2026 Parkable S.L. — Todos los derechos reservados ℗ - made with 🩷 by rañó',
    ft_green:'🌱 Cada reserva ayuda al planeta',
  },
  en: {
    nav_problem:'The problem', nav_how:'How it works', nav_features:'Features',
    nav_impact:'Eco impact', nav_rewards:'Rewards',
    nav_login:'Sign in', nav_download:'Download free',
    hero_badge:'Sustainable urban mobility',
    hero_h1_1:'Park smarter.', hero_h1_2:'Pollute less.',
    hero_sub:'Find a parking spot in under 2 minutes or share yours when you leave. Less time circling, less CO₂, better city for everyone.',
    hero_cta1:'Download for Android', hero_cta2:'See how it works →',
    stat1_num:'−90%', stat1_lbl:'time spent finding parking',
    stat2_num:'−2 kg', stat2_lbl:'CO₂ avoided per trip',
    stat3_num:'100%', stat3_lbl:'free for drivers',
    trust1:'More liveable cities', trust2:'Lower CO₂ emissions',
    trust3:'Save time every day', trust4:'Earn money from your garage',
    trust5:'Community of drivers', trust6:'Committed to the planet',
    why_label:'The problem',
    why_title:'30 minutes looking for parking. <span class="green-text">Every day.</span>',
    why_sub1:'The average urban driver wastes over 100 hours a year circling to find parking. That means burning fuel unnecessarily, polluting more and being late everywhere.',
    why_sub2:'Parkable solves both sides: it connects in real time those who need a spot with those who have one free, drastically reducing time and emissions.',
    why_cta:'See the solution →',
    card_bad1_num:'30 min', card_bad1_lbl:'average time looking for parking in the city',
    card_bad2_num:'18 kg',  card_bad2_lbl:'CO₂ unnecessarily emitted per driver per year',
    card_good1_num:'2 min', card_good1_lbl:'average time with Parkable',
    card_good2_num:'−90%',  card_good2_lbl:'emissions avoided per trip',
    how_label:'How it works', how_title:'As simple as it looks.',
    how_sub:'Choose how you want to use Parkable today.',
    tab_find:'🔍 Find a spot', tab_share:'🅿️ Rent my garage', tab_alert:'📍 Street alert',
    find_s1_h:'Open the map', find_s1_p:'See all available spots near your destination on the map, with price, photos and real-time availability.',
    find_s2_h:'Choose and book', find_s2_p:'Filter by hours, days or months. Check the photos and book with one tap. Payment is automatic.',
    find_s3_h:'Park stress-free', find_s3_p:'Your spot is waiting. You arrive straight there, no circling, no unnecessary pollution, no stress.',
    share_s1_h:'List your spot', share_s1_p:'Upload photos of your garage, pin the location on the map and set your price. Online in under 5 minutes.',
    share_s2_h:'Receive automatic bookings', share_s2_p:'Drivers book and pay on their own. You get a notification and the money, no management needed.',
    share_s3_h:'Earn €50–€200 per month', share_s3_p:'An empty spot is money left on the table. Parkable turns it into passive income effortlessly.',
    alert_s1_h:"You're about to leave", alert_s1_p:'Before you start the engine, tap "Post alert". Say how many minutes until you leave and your exact location.',
    alert_s2_h:'Another driver claims it', alert_s2_p:'A nearby driver who needs to park claims it instantly. You both confirm the handover in the app.',
    alert_s3_h:'You both earn points', alert_s3_p:'You earn 100 points for sharing. The other driver earns 25 points. Redeem them for real rewards.',
    feat_label:'Features', feat_title:'Everything you need<br/>in one app.',
    f1_h:'Real-time map', f1_p:'View available spots on the map or list view. Updates instantly when a new one appears.',
    f2_h:'Real garage photos', f2_p:"Every spot includes at least 5 photos so you know exactly what you're booking. No surprises.",
    f3_h:'4 rental modes', f3_p:'Book by hour, day, week or month. The price is calculated automatically.',
    f4_h:'Your carbon footprint', f4_p:'We track the CO₂ you save with each booking. See it in your profile and share it.', f4_eco:'CO₂ saved today',
    f5_h:'Instant alerts', f5_p:'Get a notification when someone frees up a spot near where you need to park.',
    f6_h:'Rewards for helping', f6_p:'Every alert you post earns you points redeemable for real prizes. Helping has its reward.',
    f7_h:'Integrated payments', f7_p:'Pay from the app in seconds. No cash, no transfers. Secure and automatic.',
    f8_h:'Available in two languages', f8_p:'Switch between Spanish and English at any time from the settings.',
    impact_label:'Environmental impact', impact_title:'Small actions.<br/>Big change.',
    impact_sub:'Searching for parking generates millions of tonnes of CO₂ every year worldwide. Parkable reduces that figure.',
    ic1_num:'2 kg', ic1_lbl:'CO₂ avoided per driver per week', ic1_note:'Equivalent to not starting your car for a full day',
    ic2_num:'1 tree', ic2_lbl:'is what Parkable saves in emissions in just one month of use', ic2_note:'A mature tree absorbs ~21 kg CO₂/year',
    ic3_num:'€180', ic3_lbl:'in fuel saved per driver per year', ic3_note:'Based on 20 min daily search average',
    mission:'<strong>Our mission:</strong> make cities breathe easier, one parking spot at a time. Every shared garage is one less car circling.',
    pts_label:'Rewards system', pts_title:'Helping has<br/>its reward.',
    pts_sub:'Every street alert generates points automatically for both drivers. Redeem them for real prizes.',
    pts_earn_title:'How do I earn points?', pts_cat_title:'What can I redeem?',
    e1_strong:'I shared my street spot', e1_span:'You posted an alert and another driver used it',
    e2_strong:'I parked thanks to an alert', e2_span:'You claimed a free spot alerted by another driver',
    r1_name:'Car wash', r1_sub:'At partner petrol stations',
    r2_name:'€5 fuel discount', r2_sub:'On refuels over €30',
    r3_name:'Premium week', r3_sub:'Priority bookings, no fees',
    r4_name:'Free coffee', r4_sub:'At associated service areas',
    testi_label:'Real users', testi_title:'Already using it.',
    t1_text:'"I used to spend 25 minutes looking for parking near work. Now I book from breakfast and arrive straight there. No stress."',
    t2_text:'"My garage was empty five days a week. Now it generates €120 a month without me doing anything. I couldn\'t believe it."',
    t3_text:'"The points thing is addictive. Every time I post an alert when I leave my spot, I earn points. I\'ve already redeemed two car washes."',
    t2_sub:'Owner · Málaga',
    dl_deco:'', dl_title:'Join the mobility<br/>of the future.',
    dl_sub:'Free for drivers. No credit card. Available for Android.',
    dl_store_label:'Get it on', dl_store_name:'Google Play', dl_demo:'See demo',
    dl_b1:'Free', dl_b2:'Android 8+', dl_b3:'Eco-friendly', dl_b4:'Secure',
    ft_tagline:'Smart Parking. Shared Spaces.', ft_eco:'Committed to sustainable mobility',
    ft_col1:'Product', ft_col2:'Company', ft_col3:'Legal',
    ft_l1:'The problem', ft_l2:'How it works', ft_l3:'Features', ft_l4:'Download',
    ft_l5:'Environmental impact', ft_l6:'About us', ft_l7:'Press', ft_l8:'Contact',
    ft_l9:'Privacy', ft_l10:'Terms of use', ft_l11:'Cookies',
    ft_copy:'© 2026 Parkable S.L. — All rights reserved ℗ - made with 🩷 by rañó',
    ft_green:'🌱 Every booking helps the planet',
  }
};

let lang = localStorage.getItem('pk_lang') || 'es';

function applyLang(l) {
  const t = T[l]; if (!t) return;
  lang = l; localStorage.setItem('pk_lang', l);

  const s  = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
  const si = (id, html) => { const el = document.getElementById(id); if (el) el.innerHTML = html; };

  s('nav-problem',t.nav_problem); s('nav-how',t.nav_how); s('nav-features',t.nav_features);
  s('nav-impact',t.nav_impact);   s('nav-rewards',t.nav_rewards);
  s('nav-login',t.nav_login);
  document.querySelectorAll('.nav-download-btn').forEach(el => el.textContent = t.nav_download);

  s('hero-badge',t.hero_badge); s('hero-h1-1',t.hero_h1_1); s('hero-h1-2',t.hero_h1_2);
  s('hero-sub',t.hero_sub);     s('hero-cta1',t.hero_cta1); s('hero-cta2',t.hero_cta2);
  s('stat1-num',t.stat1_num); s('stat1-lbl',t.stat1_lbl);
  s('stat2-num',t.stat2_num); s('stat2-lbl',t.stat2_lbl);
  s('stat3-num',t.stat3_num); s('stat3-lbl',t.stat3_lbl);

  s('trust1',t.trust1); s('trust2',t.trust2); s('trust3',t.trust3);
  s('trust4',t.trust4); s('trust5',t.trust5); s('trust6',t.trust6);

  s('why-label',t.why_label); si('why-title',t.why_title);
  s('why-sub1',t.why_sub1);   s('why-sub2',t.why_sub2); s('why-cta',t.why_cta);
  s('card-bad1-num',t.card_bad1_num);   s('card-bad1-lbl',t.card_bad1_lbl);
  s('card-bad2-num',t.card_bad2_num);   s('card-bad2-lbl',t.card_bad2_lbl);
  s('card-good1-num',t.card_good1_num); s('card-good1-lbl',t.card_good1_lbl);
  s('card-good2-num',t.card_good2_num); s('card-good2-lbl',t.card_good2_lbl);

  s('how-label',t.how_label); s('how-title',t.how_title); s('how-sub',t.how_sub);
  s('tab-find',t.tab_find); s('tab-share',t.tab_share); s('tab-alert',t.tab_alert);
  s('find-s1-h',t.find_s1_h);   s('find-s1-p',t.find_s1_p);
  s('find-s2-h',t.find_s2_h);   s('find-s2-p',t.find_s2_p);
  s('find-s3-h',t.find_s3_h);   s('find-s3-p',t.find_s3_p);
  s('share-s1-h',t.share_s1_h); s('share-s1-p',t.share_s1_p);
  s('share-s2-h',t.share_s2_h); s('share-s2-p',t.share_s2_p);
  s('share-s3-h',t.share_s3_h); s('share-s3-p',t.share_s3_p);
  s('alert-s1-h',t.alert_s1_h); s('alert-s1-p',t.alert_s1_p);
  s('alert-s2-h',t.alert_s2_h); s('alert-s2-p',t.alert_s2_p);
  s('alert-s3-h',t.alert_s3_h); s('alert-s3-p',t.alert_s3_p);

  s('feat-label',t.feat_label); si('feat-title',t.feat_title);
  s('f1-h',t.f1_h); s('f1-p',t.f1_p); s('f2-h',t.f2_h); s('f2-p',t.f2_p);
  s('f3-h',t.f3_h); s('f3-p',t.f3_p); s('f4-h',t.f4_h); s('f4-p',t.f4_p);
  s('f4-eco',t.f4_eco);
  s('f5-h',t.f5_h); s('f5-p',t.f5_p); s('f6-h',t.f6_h); s('f6-p',t.f6_p);
  s('f7-h',t.f7_h); s('f7-p',t.f7_p); s('f8-h',t.f8_h); s('f8-p',t.f8_p);

  s('impact-label',t.impact_label); si('impact-title',t.impact_title); s('impact-sub',t.impact_sub);
  s('ic1-num',t.ic1_num); s('ic1-lbl',t.ic1_lbl); s('ic1-note',t.ic1_note);
  s('ic2-num',t.ic2_num); s('ic2-lbl',t.ic2_lbl); s('ic2-note',t.ic2_note);
  s('ic3-num',t.ic3_num); s('ic3-lbl',t.ic3_lbl); s('ic3-note',t.ic3_note);
  si('mission-text',t.mission);

  s('pts-label',t.pts_label); si('pts-title',t.pts_title); s('pts-sub',t.pts_sub);
  s('pts-earn-title',t.pts_earn_title); s('pts-cat-title',t.pts_cat_title);
  s('e1-strong',t.e1_strong); s('e1-span',t.e1_span);
  s('e2-strong',t.e2_strong); s('e2-span',t.e2_span);
  s('r1-name',t.r1_name); s('r1-sub',t.r1_sub); s('r2-name',t.r2_name); s('r2-sub',t.r2_sub);
  s('r3-name',t.r3_name); s('r3-sub',t.r3_sub); s('r4-name',t.r4_name); s('r4-sub',t.r4_sub);

  s('testi-label',t.testi_label); s('testi-title',t.testi_title);
  s('t1-text',t.t1_text); s('t2-text',t.t2_text); s('t3-text',t.t3_text); s('t2-sub',t.t2_sub);

  s('dl-deco',t.dl_deco); si('dl-title',t.dl_title); s('dl-sub',t.dl_sub);
  s('dl-store-label',t.dl_store_label); s('dl-store-name',t.dl_store_name); s('dl-demo',t.dl_demo);
  s('dl-b1',t.dl_b1); s('dl-b2',t.dl_b2); s('dl-b3',t.dl_b3); s('dl-b4',t.dl_b4);

  document.querySelectorAll('.ft-tagline').forEach(el => el.textContent = t.ft_tagline);
  document.querySelectorAll('.ft-eco').forEach(el => el.textContent = t.ft_eco);
  s('ft-col1',t.ft_col1); s('ft-col2',t.ft_col2); s('ft-col3',t.ft_col3);
  s('ft-l1',t.ft_l1); s('ft-l2',t.ft_l2); s('ft-l3',t.ft_l3); s('ft-l4',t.ft_l4);
  s('ft-l5',t.ft_l5); s('ft-l6',t.ft_l6); s('ft-l7',t.ft_l7); s('ft-l8',t.ft_l8);
  s('ft-l9',t.ft_l9); s('ft-l10',t.ft_l10); s('ft-l11',t.ft_l11);
  s('ft-copy',t.ft_copy); s('ft-green',t.ft_green);

  // Actualizar botón
  const btn = document.getElementById('lang-toggle');
  if (btn) btn.innerHTML = l === 'es' ? '<span>🇬🇧</span> EN' : '<span>🇪🇸</span> ES';
}

// ── BOTÓN DE IDIOMA ───────────────────────────
const langBtn = document.createElement('button');
langBtn.id = 'lang-toggle';
langBtn.style.cssText = `
  display:inline-flex;align-items:center;gap:6px;
  padding:7px 14px;border-radius:40px;
  background:rgba(237,237,243,.07);
  border:1px solid rgba(237,237,243,.14);
  color:#ededf3;font-family:inherit;
  font-size:13px;font-weight:600;
  cursor:pointer;transition:all .2s;white-space:nowrap;
`;
langBtn.innerHTML = lang === 'es' ? '<span>🇬🇧</span> EN' : '<span>🇪🇸</span> ES';
langBtn.addEventListener('mouseenter', () => {
  langBtn.style.background = 'rgba(124,179,66,.15)';
  langBtn.style.borderColor = 'rgba(124,179,66,.35)';
});
langBtn.addEventListener('mouseleave', () => {
  langBtn.style.background = 'rgba(237,237,243,.07)';
  langBtn.style.borderColor = 'rgba(237,237,243,.14)';
});
langBtn.addEventListener('click', () => {
  const next = lang === 'es' ? 'en' : 'es';
  document.body.style.transition = 'opacity .15s ease';
  document.body.style.opacity = '0';
  setTimeout(() => { applyLang(next); document.body.style.opacity = '1'; }, 150);
});

const navActionsEl = document.getElementById('navActions');
if (navActionsEl) navActionsEl.insertBefore(langBtn, navActionsEl.firstChild);

// Aplicar idioma al cargar
applyLang(lang);

console.log('%cParkable 🅿','color:#1A6E9E;font-size:22px;font-weight:bold');
console.log('%cSmart Parking. Shared Spaces. 🌿','color:#7CB342;font-size:13px');
