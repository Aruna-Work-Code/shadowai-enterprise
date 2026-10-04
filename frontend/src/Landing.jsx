import { useEffect, useState } from "react";
import Fabric from "./Fabric.jsx";
import { reduced, useGlow } from "./util.js";
const PHASES = ["Policy", "Decision", "Action", "Audit"];

/* ---------- Public website ---------- */
const PILLARS = [["Prevent", "Recommend an approved AI capability before an employee reaches for an unmanaged one."], ["Resolve", "Turn unmanaged AI usage into an investigation with a risk level, an owner and a decision."], ["Discover", "Register AI tools observed outside the approved inventory and classify their risk."], ["Govern", "Policies define data types, allowed and prohibited tools and when approval is required."], ["Audit", "Every governance decision leaves an accountable record you can trace."]];
const FLOW = ["Employee need", "AI request", "Policy evaluation", "Safe recommendation", "Human decision", "Action", "Audit"];
export default function Landing() {
  useGlow();
  const [menu, setMenu] = useState(false);
  useEffect(() => { if (!menu) return; const k = e => e.key === "Escape" && setMenu(false), r = () => innerWidth > 900 && setMenu(false); addEventListener("keydown", k); addEventListener("resize", r); return () => { removeEventListener("keydown", k); removeEventListener("resize", r); }; }, [menu]);
  useEffect(() => { const o = new IntersectionObserver(e => e.forEach(i => i.isIntersecting && i.target.classList.add("in")), { threshold: 0.15 }); document.querySelectorAll(".rv").forEach(n => o.observe(n)); return () => o.disconnect(); }, []);
  const go = id => { setMenu(false); document.getElementById(id)?.scrollIntoView({ behavior: reduced() ? "auto" : "smooth" }); };
  return (<div className="site">
    <header className="nav"><b className="brand"><i />ShadowAI</b><nav id="site-nav" className={menu ? "open" : ""} aria-label="Sections"><button onClick={() => go("product")}>Product</button><button onClick={() => go("flow")}>Workflow</button><button onClick={() => go("trust")}>Trust</button><a className="btn sm nav-cta" href="#/app">Open workspace →</a></nav><a className="btn sm nav-open" href="#/app">Open workspace →</a><button type="button" className="nav-burger" aria-label={menu ? "Close menu" : "Open menu"} aria-expanded={menu} aria-controls="site-nav" onClick={() => setMenu(m => !m)}><span /><span /><span /></button></header>
    <section className="hero"><Fabric /><div className="wrap hero-in"><p className="eyebrow">AI governance decision assistant</p><h1>Make the safe AI decision the <span>easy decision.</span></h1><p className="lead">ShadowAI connects policy, people and AI usage in one decision workflow, so organizations can adopt AI with visibility, control and accountability.</p><div className="row"><a className="btn" href="#/app">Explore the workspace →</a><button className="btn ghost" onClick={() => go("product")}>See how it works</button></div><div className="chain">{PHASES.map((p, i) => <span key={p} style={{ animationDelay: i * 0.25 + "s" }}>{p}</span>)}</div></div></section>
    <section id="product" className="wrap sec"><h2 className="rv">Prevention and correction in one control plane</h2><div className="grid5">{PILLARS.map(([t, d], i) => <article key={t} className="card rv" style={{ transitionDelay: i * 70 + "ms" }}><span className="num">0{i + 1}</span><h3>{t}</h3><p>{d}</p></article>)}</div></section>
    <section id="flow" className="wrap sec"><h2 className="rv">One decision path, end to end</h2><ol className="flow">{FLOW.map((f, i) => <li key={f} className="rv" style={{ transitionDelay: i * 80 + "ms" }}><b>{i + 1}</b>{f}</li>)}</ol></section>
    <section id="trust" className="wrap sec"><h2 className="rv">Built to be inspected</h2><div className="grid3"><article className="card rv"><h3>Real states only</h3><p>Status shown in the workspace comes from live API calls. Nothing is simulated.</p></article><article className="card rv"><h3>Explainable posture</h3><p>The governance score is calculated from open investigations and pending requests. The formula is shown in the product.</p></article><article className="card rv"><h3>Role-based access</h3><p>JWT authentication with Employee, IT Reviewer and Manager roles enforced on the server.</p></article></div></section>
    <section className="wrap sec cta rv"><h2>See it with real data</h2><a className="btn" href="#/app">Open the workspace →</a></section>
    <footer className="foot">ShadowAI is an enterprise-style prototype designed and engineered as a portfolio product. It is not a production service with an SLA.<small className="credit">Aruna</small></footer>
  </div>);
}

