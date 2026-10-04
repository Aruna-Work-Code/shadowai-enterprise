import { useEffect, useRef } from "react";
// Governance Fabric: people, AI tools, policies, decisions and audit as one living network. Canvas only (no 3D library), so it loads instantly.
export default function Fabric() {
  const ref = useRef();
  useEffect(() => {
    const c = ref.current, x = c.getContext("2d"), still = matchMedia("(prefers-reduced-motion: reduce)").matches;
    let w, h, raf, t = 0, m = { x: -999, y: -999 }; const P = [];
    const size = () => {
      const d = Math.min(devicePixelRatio || 1, 2); w = c.clientWidth; h = c.clientHeight; c.width = w * d; c.height = h * d; x.setTransform(d, 0, 0, d, 0, 0);
      P.length = 0; const n = w < 640 ? 38 : 84;
      for (let i = 0; i < n; i++) P.push({ a: Math.random() * 6.283, r: 0.1 + Math.random() * 0.42, z: Math.random(), s: (0.05 + Math.random() * 0.15) * (i % 2 ? 1 : -1), c: i % 23 === 0 ? "#f87171" : i % 9 === 0 ? "#fbbf24" : i % 3 === 0 ? "#34d399" : "#60a5fa" });
      if (still) draw();
    };
    const draw = () => {
      x.clearRect(0, 0, w, h); const cx = w * (w > 900 ? 0.62 : 0.5), cy = h * 0.5, R = Math.min(w, h) * 1.05;
      const pts = P.map(p => {
        const a = p.a + t * p.s, k = 0.55 + p.z * 0.7; let px = cx + Math.cos(a) * p.r * R * 1.15, py = cy + Math.sin(a) * p.r * R * 0.7;
        const dx = m.x - px, dy = m.y - py, dist = Math.hypot(dx, dy); if (dist < 140) { px += dx * (1 - dist / 140) * 0.25; py += dy * (1 - dist / 140) * 0.25; }
        return { px, py, k, c: p.c };
      });
      x.lineWidth = 1;
      for (let i = 0; i < pts.length; i++) for (let j = i + 1; j < pts.length; j++) {
        const d = Math.hypot(pts[i].px - pts[j].px, pts[i].py - pts[j].py);
        if (d < 120) { x.strokeStyle = `rgba(120,160,255,${0.22 * (1 - d / 120)})`; x.beginPath(); x.moveTo(pts[i].px, pts[i].py); x.lineTo(pts[j].px, pts[j].py); x.stroke(); }
      }
      pts.forEach(p => { x.fillStyle = p.c; x.globalAlpha = 0.45 + p.k * 0.4; x.beginPath(); x.arc(p.px, p.py, 1.6 * p.k + 0.6, 0, 6.283); x.fill(); });
      x.globalAlpha = 1;
      pts.forEach((p, i) => { if (i % 6 === 0) { x.strokeStyle = "rgba(79,140,255,.16)"; x.beginPath(); x.moveTo(cx, cy); x.lineTo(p.px, p.py); x.stroke(); } });
      for (let i = 0; i < 3; i++) { const q = ((t * 0.4 + i / 3) % 1); x.strokeStyle = `rgba(96,165,250,${0.5 * (1 - q)})`; x.beginPath(); x.arc(cx, cy, 14 + q * 70, 0, 6.283); x.stroke(); }
      x.fillStyle = "#e6ecf8"; x.save(); x.translate(cx, cy); x.rotate(Math.PI / 4 + t * 0.3); x.shadowColor = "#4f8cff"; x.shadowBlur = 24; x.fillRect(-8, -8, 16, 16); x.restore(); x.shadowBlur = 0;
    };
    const loop = () => { if (!document.hidden) { t += 0.004; draw(); } raf = requestAnimationFrame(loop); };
    const mv = e => { const b = c.getBoundingClientRect(); m = { x: e.clientX - b.left, y: e.clientY - b.top }; };
    size(); addEventListener("resize", size); addEventListener("pointermove", mv); if (!still) loop();
    return () => { cancelAnimationFrame(raf); removeEventListener("resize", size); removeEventListener("pointermove", mv); };
  }, []);
  return <canvas ref={ref} className="fabric" aria-hidden="true" />;
}
