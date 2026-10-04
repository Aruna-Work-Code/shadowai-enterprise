import { useEffect, useState } from "react";
import Fabric from "./Fabric.jsx";
import { login, health } from "./api.js";
import { reduced, useGlow } from "./util.js";

/* ---------- Entry (login) ---------- */
const SC = [
  { q: "An employee wants to paste customer records into a public AI assistant.", a: "BLOCK", w: "Customer data is restricted. Unapproved external AI must not process it.", p: "Customer data → restricted" },
  { q: "A developer asks to use an unapproved AI coding assistant on internal code.", a: "REVIEW", w: "The tool is not in the approved inventory, so a human reviewer decides and an approved alternative is recommended.", p: "Unapproved tool → exception workflow" },
  { q: "Marketing wants an approved AI writer to draft a post from public information.", a: "APPROVE", w: "Public data on an approved tool is low risk and needs no escalation.", p: "Public data + approved tool → allow" }];
const ROLES = [["Employee", "priya.employee"], ["IT Reviewer", "aruna.it"], ["Manager", "neha.manager"]];
/* Own-credentials flow: the chosen role maps to the matching demo account, so the backend behaviour per role is unchanged. */
const DEMO_PW = "Demo@123";
const ROLE_OPTS = [["Employee", "priya.employee", "employee"], ["IT Reviewer", "aruna.it", "it"], ["Manager", "neha.manager", "manager"]];
const isDemoPair = (a, b) => ROLES.some(([, n]) => n === a) && b === DEMO_PW;
const baseName = s => { let x = s.trim(); if (x.includes("@")) x = x.split("@")[0]; x = x.toLowerCase().replace(/\s+/g, "").replace(/\.(employee|it|manager)$/, ""); return x || "user"; };
function Scenario() {
  const [i, setI] = useState(0), [pick, setPick] = useState(null), s = SC[i];
  return (<div className="scn" aria-live="polite"><p className="eyebrow">Governance decision {i + 1} of {SC.length}</p><p className="q">{s.q}</p>
    <div className="row">{["APPROVE", "REVIEW", "BLOCK"].map(o => <button key={o} className={"opt " + (pick === o ? "sel" : "")} onClick={() => setPick(o)} disabled={!!pick}>{o}</button>)}</div>
    {pick && <div className="why"><b className={pick === s.a ? "okc" : "warn"}>{pick === s.a ? "Matches the reference outcome" : "Reference outcome: " + s.a}</b><p>{s.w}</p><small>Policy: {s.p}</small><button className="link" onClick={() => { setI((i + 1) % SC.length); setPick(null); }}>Next scenario →</button></div>}
    <small className="note">Illustrative scenarios. Live decisions run on the Decision Engine inside the workspace.</small></div>);
}

const GSTEPS = [["Start with Requests", "Describe a business task. The decision assistant checks it against approved AI capabilities and policy."], ["Watch the decision", "A safe request gets an approved capability. A risky one enters an exception workflow instead of being silently approved."], ["Simulate Shadow AI", "Open Shadow AI Discovery and register an unapproved tool. Unknown tools can open an investigation."], ["Follow the governance signal", "Investigations, AI Tools, Notifications and the Audit Log show the full chain from detection to human review."], ["Review policies", "Policies are the guardrails behind the decision engine: data sensitivity, allowed tools, risk and exception duration."]];
const GROLES = [["Employee", "priya.employee", "Submit requests and use the decision assistant."], ["IT Reviewer", "aruna.it", "Review requests, investigations, tools, policies and audit evidence."], ["Governance Manager", "neha.manager", "Demonstrate governance administration and exception decisions."]];
function DemoGuide({ onClose, onPick }) {
  return (<div className="gscrim" onClick={onClose}><div className="gmodal" role="dialog" aria-modal="true" aria-label="Demo guide" onClick={e => e.stopPropagation()}>
    <div className="gm-head"><div><p className="eyebrow">Evaluation guide</p><h2>See the product in five minutes</h2></div><button className="gm-x" onClick={onClose} aria-label="Close guide">×</button></div>
    <p className="muted">ShadowAI connects prevention, discovery, investigation and human decisions in one workflow.</p>
    <ol className="gsteps">{GSTEPS.map(([t, d], i) => <li key={t} style={{ animationDelay: i * 70 + "ms" }}><b>{i + 1}</b><div><strong>{t}</strong><span>{d}</span></div></li>)}</ol>
    <h3>Demo roles</h3><p className="muted">For this public demo only. Pick a role to fill in the sign-in form.</p>
    <div className="groles">{GROLES.map(([n, u, d]) => <button key={u} onClick={() => onPick(u)}><strong>{n}</strong><code>{u}</code><span>{d}</span></button>)}</div>
    <p className="muted gpw">Demo password: <code>Demo@123</code></p></div></div>);
}

export default function Entry({ onAuth, onDemo }) {
  useGlow();
  const [guide, setGuide] = useState(false), [leaving, setLeaving] = useState(false), [up, setUp] = useState(false), [sec, setSec] = useState(0), [u, setU] = useState(""), [p, setP] = useState(""), [busy, setBusy] = useState(false), [err, setErr] = useState(""), [askRole, setAskRole] = useState(false), [role, setRole] = useState("");
  useEffect(() => { let stop = false; const t0 = Date.now(), tick = setInterval(() => setSec(Math.floor((Date.now() - t0) / 1000)), 1000);
    (async () => { while (!stop) { if (await health()) { setUp(true); clearInterval(tick); break; } await new Promise(r => setTimeout(r, 2500)); } })();
    return () => { stop = true; clearInterval(tick); }; }, []);
  useEffect(() => { if (!guide) return; const k = e => e.key === "Escape" && setGuide(false); addEventListener("keydown", k); return () => removeEventListener("keydown", k); }, [guide]);
  const state = up ? "READY" : sec > 90 ? "DELAYED" : "CONNECTING";
  const demoPair = isDemoPair(u.trim(), p), needRole = askRole && !demoPair;
  const pickDemo = un => { setU(un); setP(DEMO_PW); setAskRole(false); setRole(""); setErr(""); };
  const submit = async e => { e.preventDefault(); const uu = u.trim(); if (!uu || !p) return; setErr("");
    if (!demoPair && !askRole) { setAskRole(true); return; }
    let loginU = uu, loginP = p, disp = null;
    if (!demoPair) { const r = ROLE_OPTS.find(x => x[0] === role); if (!r) { setErr("Please choose a role to continue."); return; } loginU = r[1]; loginP = DEMO_PW; disp = baseName(uu) + "." + r[2]; }
    setBusy(true);
    try { const d = await login(loginU, loginP); if (disp) localStorage.setItem("display_name", disp); else localStorage.removeItem("display_name"); setLeaving(true); await new Promise(r => setTimeout(r, reduced() ? 0 : 650)); onAuth(d); } catch { setErr(up ? "Sign-in failed. Check the username and password." : "The API is still starting. Wait for READY, then try again."); setBusy(false); } };
  return (<div className={"entry" + (leaving ? " leaving" : "")}><Fabric /><div className="entry-in wrap">
    <section><div className="entry-top"><a className="back-home" href="#/"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M19 12H5M12 19l-7-7 7-7" /></svg>Back to home</a><button type="button" className="guide-btn" onClick={() => setGuide(true)}>Demo guide</button></div><a className="brand" href="#/"><i />ShadowAI</a><h1>Policy → Decision → Action → Audit</h1>
      <ul className="status" aria-live="polite"><li><span>Interface</span><em className="ok">READY</em></li><li><span>Governance API</span><em className={state === "READY" ? "ok" : state === "DELAYED" ? "bad" : "wait"}>{state}{state === "CONNECTING" ? ` · ${sec}s` : ""}</em></li><li><span>Authentication</span><em className={up ? "ok" : "wait"}>{up ? "AVAILABLE" : "WAITING"}</em></li></ul>
      {state === "DELAYED" && <p className="note">The backend is taking longer than expected. It keeps retrying automatically.</p>}
      <Scenario /></section>
    <form className={"login" + (needRole ? " step2" : "")} onSubmit={submit}><h2>Sign in</h2><p className="muted">Choose a demo role or enter credentials.</p>
      <div className={"roles" + (u.trim() || p ? "" : " hint")} role="group" aria-label="Demo roles">{ROLES.map(([n, un]) => <button type="button" key={n} className={u === un ? "sel" : ""} aria-pressed={u === un} title={"Fill demo credentials for " + n} onClick={() => pickDemo(un)}>{n}</button>)}</div>
      <label>Username<input value={u} onChange={e => { setU(e.target.value); setErr(""); }} autoComplete="username" required /></label>
      <label>Password<input type="password" value={p} onChange={e => { setP(e.target.value); setErr(""); }} autoComplete="current-password" required /></label>
      {needRole && <label className="role-pick">Choose your role<select value={role} onChange={e => { setRole(e.target.value); setErr(""); }} required autoFocus><option value="" disabled>Select a role…</option>{ROLE_OPTS.map(([n]) => <option key={n} value={n}>{n}</option>)}</select><small>You will enter the workspace for this role as <b>{baseName(u)}{role ? "." + ROLE_OPTS.find(x => x[0] === role)[2] : ""}</b>.</small></label>}
      {err && <p className="err" role="alert">{err}</p>}
      <button className={"btn " + (busy ? "loading" : "")} disabled={busy} aria-busy={busy}>{busy ? "Signing in…" : "Sign in"}</button><button type="button" className="demo-btn" onClick={onDemo}><span>⚡</span> Explore the demo workspace<span className="demo-tag">No sign-in needed</span></button></form>
    {guide && <DemoGuide onClose={() => setGuide(false)} onPick={un => { pickDemo(un); setGuide(false); }} />}
  </div></div>);
}

