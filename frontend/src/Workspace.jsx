import React, { useEffect, useRef, useState, useMemo, useCallback } from "react";
import "./workspace.css";
import "./polish-ws.css";
import { useGlow } from "./util.js";

/* =========================================================
   CONFIG
   ========================================================= */
const API = import.meta.env.VITE_API_URL || "";

const REVIEW_ROLES = ["IT_REVIEWER", "SECURITY_ANALYST", "GOVERNANCE_MANAGER", "ADMIN"];
const UID = { "priya.employee": 1, "aruna.it": 2, "rahul.security": 3, "neha.manager": 4, admin: 5 };
const uid = u => UID[u] || 1;
const REVIEW_ONLY = ["exceptions", "discovery", "integrations", "audit", "policies", "tools"];
/* After a role sign-in (Employee / IT Reviewer / Manager) the workspace runs on the same dataset and logic as the demo workspace.
   Authentication still goes through the real backend. Set to false to read live data from the REST API instead. */
const SIMULATED = true;

/* =========================================================
   RICH DEMO DATA (works without backend)
   ========================================================= */
const DEMO = {
  token: "demo-token-shadowai",
  user: "Sarah Mitchell",
  role: "GOVERNANCE_MANAGER",
  dash: { totalTools: 14, pendingRequests: 3, openInvestigations: 2, totalPolicies: 8, openExceptions: 1, policyCompliance: 94 },
  requests: [
    { id: 1042, intent: "Use external AI assistant for customer support email drafts", status: "PENDING", dataType: "CONFIDENTIAL", department: "SALES", frequency: "DAILY" },
    { id: 1041, intent: "Integrate GitHub Copilot for backend development team", status: "APPROVED", dataType: "INTERNAL", department: "ENGINEERING", frequency: "DAILY" },
    { id: 1040, intent: "Analyse quarterly financial projections using AI summarisation", status: "PENDING_REVIEW", dataType: "RESTRICTED", department: "FINANCE", frequency: "MONTHLY" },
    { id: 1039, intent: "Use AI transcription for customer call recordings", status: "REJECTED", dataType: "CONFIDENTIAL", department: "SALES", frequency: "DAILY" },
    { id: 1038, intent: "Deploy AI-powered HR screening assistant for recruitment", status: "APPROVED", dataType: "INTERNAL", department: "HR", frequency: "WEEKLY" },
  ],
  exceptions: [
    { id: 201, requestId: 1040, businessJustification: "Board presentation deadline requires accelerated processing", requestedDurationDays: 14, status: "PENDING" },
    { id: 200, requestId: 1039, businessJustification: "Critical client project, CISO approval pending", requestedDurationDays: 7, status: "APPROVED" },
  ],
  investigations: [
    { id: 14, toolName: "Unauthorised ChatGPT Usage — Finance Dept", status: "UNDER_REVIEW", riskLevel: "HIGH", department: "FINANCE", dataClassification: "RESTRICTED", description: "Multiple employees found using ChatGPT with restricted financial data. Governance review initiated." },
    { id: 13, toolName: "Shadow Analytics Tool — Marketing", status: "OPEN", riskLevel: "MEDIUM", department: "MARKETING", dataClassification: "INTERNAL", description: "Unapproved AI analytics platform discovered processing customer behavioural data." },
    { id: 12, toolName: "AI Email Writer — Sales", status: "RESOLVED", riskLevel: "LOW", department: "SALES", dataClassification: "INTERNAL", description: "Approved after policy alignment. Tool now in governed inventory." },
  ],
  tools: [
    { id: 1, name: "GitHub Copilot", category: "Code Assistant", governanceStatus: "APPROVED", riskLevel: "LOW", vendor: "Microsoft", department: "ENGINEERING" },
    { id: 2, name: "ChatGPT Enterprise", category: "AI Assistant", governanceStatus: "APPROVED", riskLevel: "MEDIUM", vendor: "OpenAI", department: "ALL" },
    { id: 3, name: "Midjourney", category: "Image Generation", governanceStatus: "PENDING", riskLevel: "MEDIUM", vendor: "Midjourney Inc", department: "MARKETING" },
    { id: 4, name: "Otter.ai", category: "Transcription", governanceStatus: "BLOCKED", riskLevel: "HIGH", vendor: "Otter.ai Inc", department: "SALES" },
    { id: 5, name: "Notion AI", category: "Productivity", governanceStatus: "APPROVED", riskLevel: "LOW", vendor: "Notion Labs", department: "ALL" },
    { id: 6, name: "Grammarly Business", category: "Writing Assistant", governanceStatus: "APPROVED", riskLevel: "LOW", vendor: "Grammarly", department: "ALL" },
  ],
  discoveredTools: [
    { id: 1, name: "External Research GPT", type: "EXTERNAL_AI_ASSISTANT", riskLevel: "HIGH", governanceStatus: "PENDING", department: "FINANCE" },
    { id: 2, name: "AI Marketing Copy Tool", type: "CONTENT_GENERATION", riskLevel: "MEDIUM", governanceStatus: "PENDING", department: "MARKETING" },
  ],
  policies: [
    { id: 1, name: "Customer Data Governance Policy", version: "3.2", dataClassification: "RESTRICTED", status: "ACTIVE", decision: "BLOCK", priority: 1, description: "Prohibits processing customer PII through unapproved external AI services. Applies across all departments.", createdAt: "2024-01-15" },
    { id: 2, name: "Developer AI Tools Policy", version: "2.0", dataClassification: "INTERNAL", status: "ACTIVE", decision: "APPROVE", priority: 2, description: "Permits approved AI coding assistants for internal code with no customer or financial data.", createdAt: "2024-03-01" },
    { id: 3, name: "Financial Data AI Policy", version: "1.5", dataClassification: "RESTRICTED", status: "ACTIVE", decision: "REVIEW", priority: 1, description: "Requires governance review for any AI tool processing financial projections, budgets or reports.", createdAt: "2024-02-20" },
    { id: 4, name: "Marketing Content Policy", version: "1.0", dataClassification: "INTERNAL", status: "ACTIVE", decision: "APPROVE", priority: 3, description: "Allows AI-assisted content creation for marketing materials using approved tools only.", createdAt: "2024-04-10" },
  ],
  integrations: [
    { id: 1, name: "Slack Security Alerts", provider: "SLACK_WEBHOOK", status: "ACTIVE", lastEvent: "2026-10-01T14:22:00Z" },
    { id: 2, name: "Jira Governance Tickets", provider: "JIRA_WEBHOOK", status: "ACTIVE", lastEvent: "2026-10-01T09:15:00Z" },
    { id: 3, name: "SIEM Integration", provider: "GENERIC_WEBHOOK", status: "INACTIVE", lastEvent: "2026-09-28T16:40:00Z" },
  ],
  auditEvents: Array.from({ length: 24 }, (_, i) => ({
    id: 1000 - i,
    action: ["REQUEST_APPROVED", "REQUEST_REJECTED", "POLICY_EVALUATED", "INVESTIGATION_CREATED", "EXCEPTION_GRANTED", "TOOL_DISCOVERED", "USER_LOGIN"][i % 7],
    actor: ["sarah.mitchell", "aruna.it", "james.reviewer", "system"][i % 4],
    details: ["Governance review completed", "Policy violation detected", "Risk assessment updated", "Audit trail generated"][i % 4],
    createdAt: new Date(Date.now() - i * 3600000).toISOString(),
  })),
  notifications: [
    { id: 1, title: "High-risk tool discovered", message: "Finance team found using unapproved external AI with restricted data", severity: "HIGH", read: false, createdAt: new Date(Date.now() - 1800000).toISOString() },
    { id: 2, title: "3 requests pending review", message: "Governance decisions required within 24 hours", severity: "MEDIUM", read: false, createdAt: new Date(Date.now() - 7200000).toISOString() },
    { id: 3, title: "Policy updated", message: "Customer Data Governance Policy v3.2 is now active", severity: "LOW", read: true, createdAt: new Date(Date.now() - 86400000).toISOString() },
  ],
  analytics: { approvalRate: 68, avgDecisionTime: "4.2h", toolCoverage: 78, policyCompliance: 94 },
};

const SCENARIOS = [
  {
    id: 1, title: "Governance Decision #01",
    description: "An employee wants to paste customer PII into an external AI assistant to generate a support email.",
    risk: "HIGH", answer: "BLOCK", policy: "Customer Data Governance Policy v3.2",
    reason: "External AI processing is prohibited for restricted customer data. Route through approved internal channels.",
    confidence: "High",
  },
  {
    id: 2, title: "Governance Decision #02",
    description: "A developer wants to use GitHub Copilot to debug internal API code — no customer data involved.",
    risk: "LOW", answer: "APPROVE", policy: "Developer AI Tools Policy v2.0",
    reason: "Internal technical code with no PII qualifies for approved AI tool usage under Developer Productivity policy.",
    confidence: "High",
  },
  {
    id: 3, title: "Governance Decision #03",
    description: "A finance analyst wants to upload quarterly projections into an external AI tool for executive summary generation.",
    risk: "MEDIUM", answer: "REVIEW", policy: "Financial Data AI Policy v1.5",
    reason: "Financial projections are classified restricted. Human governance review required before external AI processing.",
    confidence: "Medium",
  },
];

/* =========================================================
   PARTICLE BACKGROUND — PURE DOTS (no overlapping nodes)
   ========================================================= */
function ParticleCanvas({ count = 60, color = "53,102,246", speed = 0.3 }) {
  const ref = useRef(null);
  const raf = useRef(null);
  useEffect(() => {
    const canvas = ref.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    let W = 0, H = 0;
    let particles = [];

    function resize() {
      W = canvas.width = canvas.offsetWidth;
      H = canvas.height = canvas.offsetHeight;
      particles = Array.from({ length: count }, () => ({
        x: Math.random() * W, y: Math.random() * H,
        vx: (Math.random() - 0.5) * speed, vy: (Math.random() - 0.5) * speed,
        r: Math.random() * 1.2 + 0.4, alpha: Math.random() * 0.35 + 0.08,
      }));
    }

    function draw() {
      ctx.clearRect(0, 0, W, H);
      particles.forEach(p => {
        p.x += p.vx; p.y += p.vy;
        if (p.x < 0) p.x = W; if (p.x > W) p.x = 0;
        if (p.y < 0) p.y = H; if (p.y > H) p.y = 0;
        ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${color},${p.alpha})`; ctx.fill();
      });
      // Lines between close particles
      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const dx = particles[i].x - particles[j].x, dy = particles[i].y - particles[j].y;
          const d = Math.sqrt(dx * dx + dy * dy);
          if (d < 100) {
            ctx.beginPath();
            ctx.moveTo(particles[i].x, particles[i].y);
            ctx.lineTo(particles[j].x, particles[j].y);
            ctx.strokeStyle = `rgba(${color},${(1 - d / 100) * 0.12})`;
            ctx.lineWidth = 0.5; ctx.stroke();
          }
        }
      }
      raf.current = requestAnimationFrame(draw);
    }
    resize();
    window.addEventListener("resize", resize);
    draw();
    return () => { cancelAnimationFrame(raf.current); window.removeEventListener("resize", resize); };
  }, [count, color, speed]);
  return <canvas ref={ref} className="particle-canvas" />;
}

/* =========================================================
   GOVERNANCE NETWORK — dedicated visualization component
   ========================================================= */
function GovernanceNetwork({ size = 300, onNode }) {
  const ref = useRef(null);
  const raf = useRef(null);
  const mouse = useRef({ x: -999, y: -999 });
  const cb = useRef(onNode); cb.current = onNode;

  useEffect(() => {
    const canvas = ref.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    const W = canvas.width = size, H = canvas.height = size;
    const cx = W / 2, cy = H / 2;

    const labels = ["USERS", "TOOLS", "POLICY", "AUDIT", "ACTIONS"];
    const R = size * 0.33;
    const nodes = labels.map((label, i) => {
      const angle = (i / labels.length) * Math.PI * 2 - Math.PI / 2;
      return { x: cx + Math.cos(angle) * R, y: cy + Math.sin(angle) * R, bx: cx + Math.cos(angle) * R, by: cy + Math.sin(angle) * R, vx: 0, vy: 0, label, r: 18, pulse: Math.random() * Math.PI * 2 };
    });
    const center = { x: cx, y: cy, bx: cx, by: cy, vx: 0, vy: 0, label: "GOVERN", r: 26, pulse: 0, isCenter: true };
    const all = [...nodes, center];

    function draw(t) {
      ctx.clearRect(0, 0, W, H);
      const m = mouse.current;
      all.forEach(n => {
        const dx = n.x - m.x, dy = n.y - m.y, dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 80 && dist > 0) { n.vx -= (dx / dist) * 0.5; n.vy -= (dy / dist) * 0.5; }
        n.vx += (n.bx - n.x) * 0.05; n.vy += (n.by - n.y) * 0.05;
        n.vx *= 0.85; n.vy *= 0.85;
        n.x += n.vx; n.y += n.vy;
        n.pulse += 0.025;
      });
      // Edges
      nodes.forEach(n => {
        ctx.beginPath(); ctx.moveTo(center.x, center.y); ctx.lineTo(n.x, n.y);
        ctx.strokeStyle = "rgba(99,102,241,0.25)"; ctx.lineWidth = 1; ctx.stroke();
        // Animated packet
        const prog = ((t * 0.0006 + nodes.indexOf(n) * 0.2) % 1);
        const px = center.x + (n.x - center.x) * prog, py = center.y + (n.y - center.y) * prog;
        ctx.beginPath(); ctx.arc(px, py, 2, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(99,102,241,0.8)"; ctx.fill();
      });
      // Nodes
      all.forEach(n => {
        const pf = Math.sin(n.pulse) * 0.12 + 1;
        const r = n.r * pf;
        // Glow
        const g = ctx.createRadialGradient(n.x, n.y, 0, n.x, n.y, r * 3);
        g.addColorStop(0, n.isCenter ? "rgba(99,102,241,0.3)" : "rgba(99,102,241,0.15)");
        g.addColorStop(1, "rgba(99,102,241,0)");
        ctx.beginPath(); ctx.arc(n.x, n.y, r * 3, 0, Math.PI * 2); ctx.fillStyle = g; ctx.fill();
        // Circle
        const ng = ctx.createRadialGradient(n.x - r * 0.3, n.y - r * 0.3, 0, n.x, n.y, r);
        ng.addColorStop(0, n.isCenter ? "#818cf8" : "#6366f1");
        ng.addColorStop(1, n.isCenter ? "#4338ca" : "#3730a3");
        ctx.beginPath(); ctx.arc(n.x, n.y, r, 0, Math.PI * 2);
        ctx.fillStyle = ng; ctx.fill();
        ctx.strokeStyle = "rgba(165,180,252,0.5)"; ctx.lineWidth = n.isCenter ? 1.5 : 1; ctx.stroke();
        // Label
        ctx.fillStyle = "rgba(255,255,255,0.95)";
        ctx.font = `${n.isCenter ? "bold " : ""}${n.isCenter ? 8 : 7}px Inter,sans-serif`;
        ctx.textAlign = "center"; ctx.textBaseline = "middle";
        ctx.fillText(n.label, n.x, n.y);
      });
      raf.current = requestAnimationFrame(draw);
    }
    const hit = e => { const r = canvas.getBoundingClientRect(), k = size / r.width, x = (e.clientX - r.left) * k, y = (e.clientY - r.top) * k; return all.find(n => Math.hypot(n.x - x, n.y - y) < n.r * 1.5); };
    const onMove = e => { const r = canvas.getBoundingClientRect(); mouse.current = { x: (e.clientX - r.left) * size / r.width, y: (e.clientY - r.top) * size / r.height }; if (cb.current) canvas.style.cursor = hit(e) ? "pointer" : "default"; };
    const onClick = e => { const n = hit(e); if (n && cb.current) cb.current(n.label); };
    canvas.addEventListener("click", onClick);
    canvas.addEventListener("mousemove", onMove);
    raf.current = requestAnimationFrame(draw);
    return () => { cancelAnimationFrame(raf.current); canvas.removeEventListener("mousemove", onMove); canvas.removeEventListener("click", onClick); };
  }, [size]);

  return <canvas ref={ref} width={size} height={size} className="gov-network-canvas" />;
}

/* =========================================================
   ANIMATED NUMBER
   ========================================================= */
function Num({ val, dur = 1000 }) {
  const [n, setN] = useState(0);
  useEffect(() => {
    if (val == null) return;
    const start = Date.now(), from = 0, to = Number(val);
    const step = () => {
      const p = Math.min((Date.now() - start) / dur, 1);
      setN(Math.round(from + (to - from) * (1 - Math.pow(1 - p, 3))));
      if (p < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }, [val]);
  return <>{n}</>;
}

/* =========================================================
   BADGE
   ========================================================= */
function Badge({ s, children }) {
  const v = ((s || "").toUpperCase()).replace(/[_\s]/g, "_");
  const cls = {
    APPROVED: "bg", ACTIVE: "bg", READY: "bg", LOW: "bg",
    PENDING: "by", PENDING_REVIEW: "by", OPEN: "by", CONNECTING: "by",
    REJECTED: "br", BLOCKED: "br", BLOCK: "br", HIGH: "br", ERROR: "br",
    MEDIUM: "bw", REVIEW: "bw",
    RESOLVED: "bb", CLOSED: "bb",
    INACTIVE: "bm",
  }[v] || "bd";
  return <span className={`badge ${cls}`}>{children || s}</span>;
}

/* =========================================================
   TOAST
   ========================================================= */
function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 5000); return () => clearTimeout(t); }, []);
  return (
    <div className={`toast toast-${type}`} onClick={onClose}>
      <span className="toast-icon">{type === "error" ? "⚠" : "✓"}</span>
      <span>{msg}</span>
      <button>×</button>
    </div>
  );
}

/* =========================================================
   POSTURE RING
   ========================================================= */
function PostureRing({ score }) {
  const [v, setV] = useState(0);
  useEffect(() => {
    const s = Date.now(), dur = 1400, to = score;
    const step = () => {
      const p = Math.min((Date.now() - s) / dur, 1), e = 1 - Math.pow(1 - p, 3);
      setV(Math.round(e * to));
      if (p < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }, [score]);
  const r = 52, circ = 2 * Math.PI * r;
  const offset = circ - (circ * v) / 100;
  const color = v >= 80 ? "#22c55e" : v >= 60 ? "#f59e0b" : "#ef4444";
  return (
    <div className="pr-wrap">
      <svg width="128" height="128" viewBox="0 0 128 128">
        <circle cx="64" cy="64" r={r} fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="10" />
        <circle cx="64" cy="64" r={r} fill="none" stroke={color} strokeWidth="10"
          strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
          transform="rotate(-90 64 64)" style={{ transition: "stroke-dashoffset 0.04s" }} />
      </svg>
      <div className="pr-inner">
        <span className="pr-val" style={{ color }}>{v}</span>
        <span className="pr-lbl">/100</span>
      </div>
    </div>
  );
}

/* =========================================================
   DECISION RECEIPT
   ========================================================= */
function Receipt({ data, onClose }) {
  if (!data) return null;
  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal receipt" onClick={e => e.stopPropagation()}>
        <div className="receipt-top">
          <div className="receipt-logo">
            <svg width="16" height="16" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="#6366f1" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.5" fill="#6366f1" /></svg>
          </div>
          <h3>ShadowAI Decision Receipt</h3>
          <button className="x-btn" onClick={onClose}>×</button>
        </div>
        <div className="receipt-rows">
          {[["REQUEST", data.request], ["DECISION", data.decision], ["POLICY APPLIED", data.policy], ["DECISION MAKER", data.maker], ["TIMESTAMP", data.ts], ["AUDIT ID", data.auditId]].map(([k, v]) => (
            <div key={k} className="receipt-row">
              <span className="receipt-key">{k}</span>
              <span className={`receipt-val ${k === "DECISION" ? "dec-" + (v || "").toLowerCase() : ""}`}>{v || "—"}</span>
            </div>
          ))}
        </div>
        <div className="receipt-foot"><button className="btn-p" onClick={onClose}>View Audit Trail →</button></div>
      </div>
    </div>
  );
}

/* =========================================================
   DECISION SCENARIO (interactive)
   ========================================================= */
function Scenario({ sc, onSkip }) {
  const [chosen, setChosen] = useState(null);
  return (
    <div className="scenario fade-in">
      <div className="sc-header">
        <span className="eyebrow">GOVERNANCE SCENARIO</span>
        <Badge s={sc.risk}>{sc.risk} RISK</Badge>
      </div>
      <h3 className="sc-title">{sc.title}</h3>
      <p className="sc-desc">{sc.description}</p>
      {!chosen ? (
        <>
          <p className="sc-q">What would you do?</p>
          <div className="sc-btns">
            {["APPROVE", "REVIEW", "BLOCK"].map(o => (
              <button key={o} className={`sc-btn sc-${o.toLowerCase()}`} onClick={() => setChosen(o)}>{o}</button>
            ))}
          </div>
        </>
      ) : (
        <div className="sc-result fade-in">
          <div className="sc-compare">
            <div className="sc-col">
              <div className="sc-col-label">YOUR DECISION</div>
              <div className={`sc-col-val dec-${chosen.toLowerCase()}`}>{chosen}</div>
            </div>
            <div className="sc-vs">VS</div>
            <div className="sc-col">
              <div className="sc-col-label">SHADOWAI ENGINE</div>
              <div className={`sc-col-val dec-${sc.answer.toLowerCase()}`}>{sc.answer}</div>
            </div>
          </div>
          <div className="sc-details">
            <div className="sc-drow"><span>Policy</span><span>{sc.policy}</span></div>
            <div className="sc-drow"><span>Reason</span><span>{sc.reason}</span></div>
            <div className="sc-drow"><span>Confidence</span><span>{sc.confidence}</span></div>
          </div>
          <div className={`sc-match ${chosen === sc.answer ? "match-y" : "match-n"}`}>
            {chosen === sc.answer ? "✓ Your decision matches the governance engine" : "↗ Engine reached a different conclusion — see why above"}
          </div>
        </div>
      )}
      <button className="sc-skip" onClick={onSkip}>Continue to ShadowAI →</button>
    </div>
  );
}

/* =========================================================
   LOGIN PAGE — PREMIUM REDESIGN
   ========================================================= */
function Login({ login, setLogin, onSignIn, error, onDemo }) {
  const [apiState, setApiState] = useState("connecting"); // connecting | ready | error
  const [step, setStep] = useState("role"); // role | form | scenario
  const [scIdx, setScIdx] = useState(0);
  const [role, setRole] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);

  // Ping backend
  useEffect(() => {
    const ping = async () => {
      try {
        const r = await fetch(API + "/actuator/health", { signal: AbortSignal.timeout(6000) });
        setApiState(r.ok ? "ready" : "error");
      } catch {
        try {
          const r2 = await fetch(API + "/api/v1/health", { signal: AbortSignal.timeout(4000) });
          setApiState(r2.ok ? "ready" : "error");
        } catch { setApiState("error"); }
      }
    };
    if (API) ping(); else setApiState("error");
  }, []);

  const ROLES = [
    { id: "employee", icon: "👤", label: "Employee", desc: "Request AI tools · Track approvals", u: "aruna.it", p: "Demo@123" },
    { id: "reviewer", icon: "🔍", label: "IT Reviewer", desc: "Review requests · Manage exceptions", u: "aruna.it", p: "Demo@123" },
    { id: "manager", icon: "🛡️", label: "Governance Manager", desc: "Full governance control plane", u: "aruna.it", p: "Demo@123" },
  ];

  const handleRolePick = r => {
    setRole(r);
    setLogin({ username: r.u, password: r.p });
    setStep("scenario");
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    await onSignIn(e);
    setLoading(false);
  };

  return (
    <div className="login-page">
      {/* Ambient background */}
      <div className="login-bg">
        <ParticleCanvas count={70} color="99,102,241" speed={0.25} />
        <div className="orb orb1" />
        <div className="orb orb2" />
        <div className="orb orb3" />
        <div className="grid-lines" />
      </div>

      <div className="login-inner">
        {/* LEFT */}
        <div className="login-left">
          <div className="ll-brand">
            <div className="ll-logo">
              <svg width="18" height="18" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="white" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.5" fill="white" /></svg>
            </div>
            <span>ShadowAI</span>
          </div>

          <div className="ll-hero">
            <div className="ll-tag">AI GOVERNANCE DECISION ASSISTANT</div>
            <h1>Make the safe AI decision<br /><span className="gradient-text">the easy decision.</span></h1>
            <p>Connect policy, people and AI usage into one governed decision workflow. Prevent, resolve and audit every AI interaction across your organization.</p>
          </div>

          <div className="ll-features">
            {[
              { icon: "🛡️", label: "Preventive Governance", desc: "Block risky AI usage before it happens" },
              { icon: "🔍", label: "Shadow AI Discovery", desc: "Surface unauthorized AI tools" },
              { icon: "📋", label: "Human Decision Workflows", desc: "Structured approval with full audit trail" },
              { icon: "📊", label: "Real-time Auditability", desc: "Every decision recorded and explainable" },
            ].map((f, i) => (
              <div key={i} className="ll-feat" style={{ animationDelay: `${i * 0.08}s` }}>
                <span className="ll-feat-icon">{f.icon}</span>
                <div>
                  <strong>{f.label}</strong>
                  <small>{f.desc}</small>
                </div>
              </div>
            ))}
          </div>

          <div className="ll-network">
            <GovernanceNetwork size={180} />
            <span className="ll-network-label">Interactive Governance Fabric</span>
          </div>

          <div className="ll-status">
            <div className="status-row">
              <span>Interface</span>
              <span className="st-ready">✓ READY</span>
            </div>
            <div className="status-row">
              <span>Governance API</span>
              <span className={`st-${apiState}`}>
                {apiState === "ready" ? "✓ READY" : apiState === "error" ? "✕ UNAVAILABLE" : "◌ CONNECTING"}
              </span>
            </div>
            <div className="status-row">
              <span>Workspace</span>
              <span className={`st-${apiState === "ready" ? "ready" : "wait"}`}>
                {apiState === "ready" ? "✓ READY" : "◌ WAITING"}
              </span>
            </div>
          </div>
        </div>

        {/* RIGHT */}
        <div className="login-right">
          <div className="login-card">

            {/* Step: Role Picker */}
            {step === "role" && (
              <div className="fade-in">
                <div className="lc-brand">
                  <div className="lc-logo"><svg width="14" height="14" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="#6366f1" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.5" fill="#6366f1" /></svg></div>
                  <span>ShadowAI</span>
                </div>
                <h2>Welcome back</h2>
                <p className="lc-sub">Choose your role to begin your governance session</p>
                <div className="role-list">
                  {ROLES.map(r => (
                    <button key={r.id} className="role-btn" onClick={() => handleRolePick(r)}>
                      <span className="role-icon">{r.icon}</span>
                      <div className="role-text">
                        <strong>{r.label}</strong>
                        <small>{r.desc}</small>
                      </div>
                      <span className="role-arr">→</span>
                    </button>
                  ))}
                </div>
                <div className="lc-divider"><span>or</span></div>
                <button className="lc-manual" onClick={() => { setStep("form"); setShowForm(true); }}>Sign in with credentials</button>
                <button className="demo-btn" onClick={onDemo}>
                  <span>⚡</span> Explore Demo Mode
                  <span className="demo-tag">No backend required</span>
                </button>
              </div>
            )}

            {/* Step: Scenario (before form) */}
            {step === "scenario" && (
              <div className="fade-in">
                <button className="back-link" onClick={() => setStep("role")}>← Back</button>
                <Scenario sc={SCENARIOS[scIdx]} onSkip={() => setStep("form")} />
              </div>
            )}

            {/* Step: Form */}
            {(step === "form" || showForm) && step !== "role" && step !== "scenario" && (
              <div className="fade-in">
                <button className="back-link" onClick={() => { setStep("role"); setShowForm(false); }}>← Back</button>
                <div className="lc-brand">
                  <div className="lc-logo"><svg width="14" height="14" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="#6366f1" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.5" fill="#6366f1" /></svg></div>
                  <span>ShadowAI</span>
                </div>
                <h2>Sign in</h2>
                {role && <p className="lc-sub">Signing in as <strong>{role.label}</strong></p>}
                <form onSubmit={handleSubmit} className="lc-form">
                  <div className="fg">
                    <label>Username</label>
                    <input value={login.username} onChange={e => setLogin({ ...login, username: e.target.value })} placeholder="your.username" autoComplete="username" />
                  </div>
                  <div className="fg">
                    <label>Password</label>
                    <input type="password" value={login.password} onChange={e => setLogin({ ...login, password: e.target.value })} placeholder="••••••••" autoComplete="current-password" />
                  </div>
                  {error && <div className="form-err fade-in"><span>⚠</span> {error}</div>}
                  <button type="submit" className="btn-p btn-full" disabled={loading}>
                    {loading ? <span className="btn-spinner" /> : null}
                    {loading ? "Signing in…" : "Enter ShadowAI"}
                  </button>
                </form>
                <div className="demo-hint">
                  <span className="dot-green" />
                  Demo credentials pre-filled
                </div>
                {apiState === "error" && (
                  <div className="api-warn">
                    <span>ℹ</span> Backend unavailable — try demo mode below
                    <button className="demo-btn-sm" onClick={onDemo}>⚡ Demo Mode</button>
                  </div>
                )}
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   SIDEBAR
   ========================================================= */

function PlugIcon({ s = 16 }) { return <svg width={s} height={s} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M9 2v5M15 2v5M7 7h10v4a5 5 0 0 1-10 0V7zM12 16v6" /></svg>; }
const EXAMPLES = [["Survey comments", "Summarize customer survey comments to find the top three complaints", "INTERNAL"], ["Public blog post", "Draft a blog post from our public product announcement", "PUBLIC"], ["Customer churn", "Analyze customer churn using real customer account records", "RESTRICTED"]];
function ExampleChips({ onPick }) { return <div className="chips"><span className="chips-l">Try an example:</span>{EXAMPLES.map(([l, t, d]) => <button type="button" key={l} className="chip" onClick={() => onPick(t, d)}>{l}</button>)}</div>; }
const PAGE_DESC = {
  dashboard: "Your AI governance posture at a glance. Ask for a safe alternative before using a new AI tool.",
  requests: "Ask for an AI capability. Each request is checked against policy, then a person decides.",
  exceptions: "Time-limited approvals for work policy would otherwise block, backed by a written business case.",
  investigations: "Cases opened when unapproved or risky AI use is found, tracked until a person resolves them.",
  tools: "The inventory of AI tools your organization knows about, with approval status and risk.",
  discovery: "Report AI tools people use outside the approved list so they can be assessed and governed.",
  policies: "The rules behind every decision: which data may be used with which AI, and what happens when it can't.",
  integrations: "Connect ShadowAI to chat and ticketing systems so decisions trigger real actions.",
  audit: "A trail of who decided what, when and why.",
  notifications: "Alerts that need your attention.",
  guide: "A five-minute tour of the product.",
};
const DECISION_INFO = (d = "") => { const x = String(d).toUpperCase(); if (/BLOCK|DENY|REJECT/.test(x)) return ["block", "Blocked", "Requests involving this data are stopped before they reach an AI tool."]; if (/REVIEW/.test(x)) return ["review", "Human review", "A reviewer decides each request, so nothing is approved silently."]; return ["allow", "Allowed", "Approved AI tools can be used without extra approval."]; };
const CLASS_LEVEL = { PUBLIC: 1, INTERNAL: 2, CONFIDENTIAL: 3, RESTRICTED: 4 };
function Policies({ policies }) {
  const [q, setQ] = useState(""), [f, setF] = useState("ALL"), [exp, setExp] = useState(null);
  const list = policies.map(p => ({ ...p, _d: DECISION_INFO(p.decision) })), counts = { block: 0, review: 0, allow: 0 };
  list.forEach(p => counts[p._d[0]]++);
  const filtered = list.filter(p => (f === "ALL" || p._d[0] === f) && (!q || `${p.name} ${p.dataClassification}`.toLowerCase().includes(q.toLowerCase())));
  return (
    <div className="page fade-in pol2">
      <div className="pol-sum">{[["ALL", "All policies", list.length], ["block", "Block", counts.block], ["review", "Review", counts.review], ["allow", "Allow", counts.allow]].map(([k, l, n]) => <button key={k} className={`pol-chip pc-${k} ${f === k ? "on" : ""}`} onClick={() => setF(k)} aria-pressed={f === k}><b>{n}</b><span>{l}</span></button>)}</div>
      <div className="toolbar"><input className="search" type="search" placeholder="Search policies or data type…" value={q} onChange={e => setQ(e.target.value)} aria-label="Search policies" /><span className="res-count">{filtered.length} shown</span></div>
      {filtered.length === 0 ? <div className="empty"><span>⊠</span><p>No policies match.</p></div> : (
        <div className="pol-grid">{filtered.map((p, i) => {
          const lvl = CLASS_LEVEL[String(p.dataClassification || "").toUpperCase()] || 1, open = exp === p.id;
          return (
            <article key={p.id} className={`pol-card pk-${p._d[0]} ${open ? "open" : ""}`} style={{ animationDelay: `${i * 60}ms` }}>
              <button className="pol-top" onClick={() => setExp(open ? null : p.id)} aria-expanded={open}>
                <span className="pol-shield"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6l8-3z" /></svg></span>
                <span className="pol-main"><b>{p.name}</b><small>v{p.version} · {p.status}</small></span>
                <span className="pol-verdict">{p._d[1]}</span>
              </button>
              <div className="pol-lvl"><span>Data sensitivity: {p.dataClassification}</span><i>{[1, 2, 3, 4].map(n => <em key={n} className={n <= lvl ? "on" : ""} />)}</i></div>
              <p className="pol-what">{p._d[2]}</p>
              <div className="pol-more"><div>
                {p.description && <p>{p.description}</p>}
                <dl>{p.priority != null && <><dt>Priority</dt><dd>{p.priority}</dd></>}{p.createdAt && <><dt>Created</dt><dd>{new Date(p.createdAt).toLocaleDateString()}</dd></>}</dl>
              </div></div>
              <button className="pol-tog" onClick={() => setExp(open ? null : p.id)}>{open ? "Show less" : "Show details"}</button>
            </article>);
        })}</div>)}
    </div>);
}

const NAV = [
  { g: "OVERVIEW", items: [["dashboard", "Command Center", "◉"]] },
  { g: "GOVERN", items: [["requests", "Requests", "⊞"], ["exceptions", "Exceptions", "⊡"], ["investigations", "Investigations", "⊟"]] },
  { g: "INVENTORY", items: [["tools", "AI Tools", "⊕"], ["discovery", "Shadow AI Discovery", "◎"], ["policies", "Policies", "⊠"]] },
  { g: "CONTROL", items: [["integrations", "Integrations", <PlugIcon />], ["audit", "Audit Log", "☰"], ["notifications", "Notifications", "◆"]] },
  { g: "HELP", items: [["guide", "Product Guide", "?"]] },
];

function Sidebar({ page, setPage, user, role, logout, notifs, isDemoMode, open, onClose }) {
  const unread = (notifs || []).filter(n => !n.read).length;
  const can = id => REVIEW_ONLY.includes(id) ? REVIEW_ROLES.includes(role) : true;
  return (
    <aside className={`sidebar ${open ? "open" : ""}`} id="app-sidebar" aria-label="Primary navigation">
      <div className="sb-brand">
        <div className="sb-logo"><svg width="16" height="16" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="white" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.2" fill="white" /></svg></div>
        <div><strong>ShadowAI</strong><small>Governance Platform</small></div>
        {isDemoMode && <span className="demo-mode-badge">DEMO</span>}
        <button type="button" className="sb-close" onClick={onClose} aria-label="Close navigation">×</button>
      </div>
      <nav className="sb-nav">
        {NAV.map(g => {
          const vis = g.items.filter(([id]) => can(id));
          if (!vis.length) return null;
          return (
            <div key={g.g} className="nav-g">
              <div className="nav-g-label">{g.g}</div>
              {vis.map(([id, label, ico]) => (
                <button key={id} className={`nav-item ${page === id ? "nav-on" : ""}`} aria-current={page === id ? "page" : undefined} onClick={() => { setPage(id); onClose?.(); }}>
                  <span className="nav-ico">{ico}</span>
                  <span>{label}</span>
                  {id === "notifications" && unread > 0 && <span className="nav-notif">{unread}</span>}
                  {page === id && <span className="nav-ind" />}
                </button>
              ))}
            </div>
          );
        })}
      </nav>
      <div className="sb-foot">
        <div className="sb-user">
          <div className="sb-avatar">{(user[0] || "U").toUpperCase()}</div>
          <div className="sb-uinfo"><strong>{user}</strong><small>{role.replace(/_/g, " ")}</small></div>
          </div>
        <button className="sb-signout" onClick={logout}><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" /></svg><span>Sign out</span></button>
      </div>
    </aside>
  );
}

/* =========================================================
   TOPBAR
   ========================================================= */
const PAGE_META = {
  dashboard: ["OVERVIEW", "Governance Command Center"],
  requests: ["GOVERN", "Requests"],
  exceptions: ["GOVERN", "Exceptions"],
  investigations: ["GOVERN", "Investigations"],
  tools: ["INVENTORY", "AI Tools"],
  discovery: ["INVENTORY", "Shadow AI Discovery"],
  policies: ["INVENTORY", "Policies"],
  integrations: ["CONTROL", "Integrations"],
  audit: ["CONTROL", "Audit Log"],
  notifications: ["CONTROL", "Notifications"],
  guide: ["HELP", "Product Guide"],
};

function Topbar({ page, loading, refresh, apiOk, isDemoMode, theme, setTheme, onMenu, navOpen }) {
  const [e, s] = PAGE_META[page] || ["", page];
  const [ok, setOk] = useState(false);
  return (
    <header className="topbar">
      <button type="button" className="menu-btn" onClick={onMenu} aria-label={navOpen ? "Close navigation" : "Open navigation"} aria-expanded={!!navOpen} aria-controls="app-sidebar"><span /><span /><span /></button>
      <div className="topbar-text">
        <div className="topbar-eye">SHADOWAI · {e}</div>
        <h1 className="topbar-title">{s}</h1>
        {PAGE_DESC[page] && <p className="topbar-sub">{PAGE_DESC[page]}</p>}
      </div>
      <div className="topbar-right">
        <button className="theme-btn" onClick={() => setTheme(theme === "dark" ? "light" : "dark")} aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"} title={theme === "dark" ? "Light mode" : "Dark mode"}>{theme === "dark" ? <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></svg> : <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" /></svg>}</button>
        {isDemoMode && <span className="topbar-demo">⚡ Demo Mode</span>}
        <div className={`api-pill ${apiOk ? "api-ok" : "api-err"}`}>
          <span className="api-dot" />{apiOk ? "Systems operational" : "Demo mode"}
        </div>
        <button className={`refresh-btn ${loading ? "refreshing" : ""}`} onClick={async () => { await refresh(); setOk(true); setTimeout(() => setOk(false), 1400); }} disabled={loading} aria-busy={loading}>
          ↻ {loading ? "Loading…" : ok ? "Refreshed ✓" : "Refresh"}
        </button>
      </div>
    </header>
  );
}

/* =========================================================
   POSTURE DRAWER
   ========================================================= */
function PostureDrawer({ score, onClose }) {
  const factors = [
    { l: "Policy Coverage", w: 30, s: 91 },
    { l: "Tool Governance", w: 25, s: 78 },
    { l: "Open Risk Items", w: 20, s: 75 },
    { l: "Decision Backlog", w: 15, s: 82 },
    { l: "Exception Exposure", w: 10, s: 88 },
  ];
  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 460 }} onClick={e => e.stopPropagation()}>
        <div className="modal-head"><h3>Why {score}?</h3><button className="x-btn" onClick={onClose}>×</button></div>
        <p className="modal-sub">GOVERNANCE POSTURE METHODOLOGY</p>
        {factors.map(f => (
          <div key={f.l} className="pf">
            <div className="pf-top"><span>{f.l}</span><span className="pf-w">{f.w}% weight</span></div>
            <div className="pf-bar"><div className="pf-fill" style={{ width: `${f.s}%`, background: f.s >= 80 ? "#22c55e" : f.s >= 60 ? "#f59e0b" : "#ef4444" }} /></div>
            <span className="pf-score">{f.s}/100</span>
          </div>
        ))}
        <p className="modal-note">Calculated from live workspace governance data.</p>
      </div>
    </div>
  );
}

/* =========================================================
   DASHBOARD
   ========================================================= */
function Dashboard({ dash, analytics, intent, setIntent, dataType, setDataType, recommend, recommendation, busy, setPage, tools, policies, investigations, notifications, role, isDemoMode }) {
  const [showPosture, setShowPosture] = useState(false);
  const [receipt, setReceipt] = useState(null);
  const [mapMsg, setMapMsg] = useState("");
  const goMap = l => { const t = { USERS: "requests", TOOLS: "tools", POLICY: "policies", AUDIT: "audit", ACTIONS: "investigations", GOVERN: "dashboard" }[l]; if (!t) return; if (REVIEW_ONLY.includes(t) && !REVIEW_ROLES.includes(role)) { setMapMsg(`${l[0] + l.slice(1).toLowerCase()} is available to reviewer and manager roles.`); return; } setMapMsg(""); setPage(t); };

  const total = dash?.totalTools ?? 0;
  const pending = dash?.pendingRequests ?? 0;
  const openInv = dash?.openInvestigations ?? 0;
  const totalP = dash?.totalPolicies ?? 0;

  const score = useMemo(() => {
    if (!dash) return null;
    const p = Math.min(100, totalP * 12);
    const t = Math.min(100, total * 7);
    const r = Math.max(0, 100 - pending * 8);
    const i = Math.max(0, 100 - openInv * 12);
    return Math.round(p * 0.3 + t * 0.25 + r * 0.25 + i * 0.2);
  }, [dash, total, pending, openInv, totalP]);

  return (
    <div className="page fade-in">
      {showPosture && score != null && <PostureDrawer score={score} onClose={() => setShowPosture(false)} />}
      {receipt && <Receipt data={receipt} onClose={() => setReceipt(null)} />}

      {/* Row 1: posture + stats */}
      <div className="dash-top">
        <div className="posture-card">
          <div className="posture-inner">
            {score != null ? <PostureRing score={score} /> : <div className="pr-skeleton" />}
            <div className="posture-detail">
              <div className="posture-title">Governance Posture</div>
              {score != null && <button className="posture-why" onClick={() => setShowPosture(true)}>Why {score}? →</button>}
              <div className="posture-metrics">
                {[["Policy Coverage", totalP > 0 ? `${Math.min(100, totalP * 12)}%` : "—"], ["Tool Coverage", total > 0 ? `${Math.min(100, total * 7)}%` : "—"], ["Open Exceptions", dash?.openExceptions ?? "—"], ["Pending Decisions", pending]].map(([l, v]) => (
                  <div key={l} className="pm-row"><span>{l}</span><span>{v}</span></div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="stats-g">
          {[
            { l: "AI Tools", v: total, sub: "Under governance", c: "#6366f1", p: "tools" },
            { l: "Pending Requests", v: pending, sub: "Awaiting review", c: pending > 0 ? "#f59e0b" : "#22c55e", p: "requests" },
            { l: "Investigations", v: openInv, sub: "Open cases", c: openInv > 0 ? "#ef4444" : "#22c55e", p: "investigations" },
            { l: "Active Policies", v: totalP, sub: "Enforced", c: "#22c55e", p: "policies" },
          ].map(s => (
            <div key={s.l} className="stat-card" onClick={() => setPage(s.p)}>
              <div className="stat-val" style={{ color: s.c }}><Num val={s.v} /></div>
              <div className="stat-label">{s.l}</div>
              <div className="stat-sub">{s.sub}</div>
              <div className="stat-arrow">→</div>
            </div>
          ))}
        </div>
      </div>

      {/* Row 2: Network + analytics */}
      <div className="dash-mid">
        <div className="net-card">
          <div className="card-head"><span className="card-title">Governance Fabric</span><span className="card-hint">Click a node to open it</span></div>
          <div className="net-wrap"><GovernanceNetwork size={260} onNode={goMap} /></div>
          {mapMsg && <p className="map-msg" role="status">{mapMsg}</p>}
          <div className="net-legend">
            {["Users", "Tools", "Policy", "Audit", "Actions"].map(l => (
              <button key={l} className="net-leg-item" onClick={() => goMap(l.toUpperCase())}><span className="net-leg-dot" />{l}</button>
            ))}
          </div>
        </div>

        <div className="analytics-card">
          <div className="card-head"><span className="card-title">Governance Analytics</span></div>
          {analytics ? (
            <div className="analytics-grid">
              {[
                { l: "Approval Rate", v: analytics.approvalRate, unit: "%", c: "#22c55e" },
                { l: "Avg Decision Time", v: analytics.avgDecisionTime, unit: "", c: "#6366f1" },
                { l: "Tool Coverage", v: analytics.toolCoverage, unit: "%", c: "#f59e0b" },
                { l: "Policy Compliance", v: analytics.policyCompliance, unit: "%", c: "#22c55e" },
              ].map(a => (
                <div key={a.l} className="an-item">
                  <div className="an-val" style={{ color: a.c }}>{typeof a.v === "number" ? <><Num val={a.v} />{a.unit}</> : a.v}</div>
                  <div className="an-label">{a.l}</div>
                  {typeof a.v === "number" && a.unit === "%" && (
                    <div className="an-bar"><div className="an-fill" style={{ width: `${a.v}%`, background: a.c }} /></div>
                  )}
                </div>
              ))}
            </div>
          ) : <div className="an-empty">Analytics data loading…</div>}
        </div>

        {/* Recent activity */}
        <div className="activity-card">
          <div className="card-head"><span className="card-title">Recent Activity</span><button className="card-link" onClick={() => setPage("notifications")}>All →</button></div>
          {(notifications || []).slice(0, 4).map((n, i) => (
            <div key={n.id || i} className={`act-row ${!n.read ? "act-unread" : ""}`}>
              <div className={`act-dot sev-${(n.severity || "info").toLowerCase()}`} />
              <div className="act-body">
                <div className="act-title">{n.title}</div>
                <div className="act-time">{n.createdAt ? new Date(n.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : ""}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Row 3: Recommendation */}
      <div className="rec-card">
        <div className="card-head"><span className="card-title">Safe Alternative Recommendation Engine</span><Badge s="ACTIVE">LIVE</Badge></div>
        <div className="rec-row">
          <div className="fg flex1">
            <label>What do you need to accomplish?</label>
            <small className="hint">Describe the job, not the tool. Say what data is involved.</small>
            <textarea value={intent} onChange={e => setIntent(e.target.value)} placeholder="e.g. Analyse customer churn using AI to identify at-risk accounts…" rows={2} />
            <ExampleChips onPick={(t, d) => { setIntent(t); setDataType(d); }} />
          </div>
          <div className="fg" style={{ width: 160 }}>
            <label>Data Classification</label>
            <select value={dataType} onChange={e => setDataType(e.target.value)}>
              <option value="INTERNAL">Internal</option>
              <option value="CONFIDENTIAL">Confidential</option>
              <option value="PUBLIC">Public</option>
              <option value="RESTRICTED">Restricted</option>
            </select>
          </div>
          <button className="btn-p" onClick={recommend} disabled={busy || !intent.trim()} style={{ alignSelf: "flex-end" }}>
            {busy ? <span className="btn-spinner" /> : null}{busy ? "Analysing…" : "Get Recommendation"}
          </button>
        </div>
        {recommendation && (
          <div className="rec-result fade-in">
            <div className="rec-top"><Badge s={recommendation.decision}>{recommendation.decision}</Badge><strong>{recommendation.recommendedTool || recommendation.tool}</strong></div>
            {recommendation.reason && <p>{recommendation.reason}</p>}
            {recommendation.policyName && <div className="rec-policy">Policy: <strong>{recommendation.policyName}</strong></div>}
            <button className="btn-o btn-sm" onClick={() => setReceipt({ request: intent, decision: recommendation.decision, policy: recommendation.policyName || "Governance Policy", maker: "ShadowAI Engine", ts: new Date().toLocaleString("en-GB", { dateStyle: "medium", timeStyle: "short" }), auditId: `GOV-${new Date().getFullYear()}-${String(Math.floor(Math.random() * 99999)).padStart(6, "0")}` })}>
              View Decision Receipt →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

/* =========================================================
   REQUESTS
   ========================================================= */
function Requests({ requests, approve, reject, form, setForm, create }) {
  const [tab, setTab] = useState("list");
  const [receipt, setReceipt] = useState(null);
  const [acts, setActs] = useState({});

  const set = (id, st) => setActs(p => ({ ...p, [id]: st }));

  const doApprove = async id => {
    set(id, "approving");
    try { await approve(id); set(id, "approved"); setReceipt({ request: requests.find(r => r.id === id)?.intent || "AI Request", decision: "APPROVED", policy: "Governance Review", maker: "Governance Reviewer", ts: new Date().toLocaleString("en-GB", { dateStyle: "medium", timeStyle: "short" }), auditId: `GOV-${new Date().getFullYear()}-${String(id).padStart(6, "0")}` }); }
    catch { set(id, null); }
  };
  const doReject = async id => {
    set(id, "rejecting");
    try { await reject(id); set(id, "rejected"); }
    catch { set(id, null); }
  };

  const sorted = [...requests].sort((a, b) => { const o = { PENDING: 0, PENDING_REVIEW: 0 }; return (o[a.status] ?? 1) - (o[b.status] ?? 1); });

  return (
    <div className="page fade-in">
      {receipt && <Receipt data={receipt} onClose={() => setReceipt(null)} />}
      <div className="tabs">
        <button className={`tab ${tab === "list" ? "tab-on" : ""}`} onClick={() => setTab("list")}>All Requests ({requests.length})</button>
        <button className={`tab ${tab === "new" ? "tab-on" : ""}`} onClick={() => setTab("new")}>+ New Request</button>
      </div>
      {tab === "list" && (
        <div className="tcard">
          {requests.length === 0 ? <div className="empty"><span>⊞</span><p>No governance requests yet.</p><button className="btn-p" onClick={() => setTab("new")}>Submit Request</button></div> : (
            <table className="dt">
              <thead><tr><th>ID</th><th>Intent</th><th>Data</th><th>Dept</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>{sorted.map(r => {
                const st = acts[r.id];
                const pend = r.status === "PENDING" || r.status === "PENDING_REVIEW";
                return (
                  <tr key={r.id} className="tr">
                    <td><span className="id-chip">REQ-{r.id}</span></td>
                    <td className="td-main">{r.intent}</td>
                    <td><Badge s={r.dataType}>{r.dataType}</Badge></td>
                    <td>{r.department}</td>
                    <td><Badge s={r.status}>{(r.status || "").replace("_", " ")}</Badge></td>
                    <td>{pend ? (
                      <div className="act-btns">
                        <button className={`btn-approve ${st ? "btn-disabled" : ""}`} onClick={() => doApprove(r.id)} disabled={!!st}>
                          {st === "approving" ? "…" : st === "approved" ? "✓" : "Approve"}
                        </button>
                        <button className={`btn-reject ${st ? "btn-disabled" : ""}`} onClick={() => doReject(r.id)} disabled={!!st}>
                          {st === "rejecting" ? "…" : st === "rejected" ? "✓" : "Reject"}
                        </button>
                      </div>
                    ) : <span className="act-done">Decided</span>}</td>
                  </tr>
                );
              })}</tbody>
            </table>
          )}
        </div>
      )}
      {tab === "new" && (
        <div className="form-card fade-in">
          <h3>Request AI Capability</h3>
          <p className="form-sub">Describe your use case and our governance engine will evaluate the request against active policies.</p>
          <form onSubmit={create} className="gform">
            <div className="fg"><label>What are you trying to accomplish? *</label>
              <small className="hint">Describe the job, not the tool, and the kind of data involved. Example: "Summarize customer survey comments to find the top three complaints."</small>
              <ExampleChips onPick={t => setForm({ ...form, intent: t })} /><textarea value={form.intent} onChange={e => setForm({ ...form, intent: e.target.value })} placeholder="Describe your intended AI use case…" rows={4} required /></div>
            <div className="frow3">
              <div className="fg"><label>Data Classification</label><select value={form.dataType} onChange={e => setForm({ ...form, dataType: e.target.value })}>{["INTERNAL", "CONFIDENTIAL", "PUBLIC", "RESTRICTED"].map(v => <option key={v}>{v}</option>)}</select></div>
              <div className="fg"><label>Department</label><select value={form.department} onChange={e => setForm({ ...form, department: e.target.value })}>{["ENGINEERING", "SALES", "MARKETING", "FINANCE", "HR", "OPERATIONS"].map(v => <option key={v}>{v}</option>)}</select></div>
              <div className="fg"><label>Frequency</label><select value={form.frequency} onChange={e => setForm({ ...form, frequency: e.target.value })}>{["DAILY", "WEEKLY", "MONTHLY", "ONE_TIME"].map(v => <option key={v}>{v.replace("_", " ")}</option>)}</select></div>
            </div>
            <div className="fact">
              <button type="button" className="btn-o" onClick={() => setTab("list")}>Cancel</button>
              <button type="submit" className="btn-p" disabled={!form.intent.trim()}>Submit Request →</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

/* =========================================================
   EXCEPTIONS
   ========================================================= */
function Exceptions({ exceptions, requests, form, setForm, create, approve, reject, role }) {
  const [tab, setTab] = useState("list");
  const canR = REVIEW_ROLES.includes(role);
  const sorted = [...exceptions].sort((a, b) => (a.status === "PENDING" ? -1 : 1));
  return (
    <div className="page fade-in">
      <div className="tabs">
        <button className={`tab ${tab === "list" ? "tab-on" : ""}`} onClick={() => setTab("list")}>Exceptions ({exceptions.length})</button>
        <button className={`tab ${tab === "new" ? "tab-on" : ""}`} onClick={() => setTab("new")}>+ Request Exception</button>
      </div>
      {tab === "list" && (
        <div className="tcard">{exceptions.length === 0 ? <div className="empty"><span>⊡</span><p>No exceptions requested.</p></div> : (
          <table className="dt">
            <thead><tr><th>ID</th><th>Request</th><th>Justification</th><th>Duration</th><th>Status</th>{canR && <th>Actions</th>}</tr></thead>
            <tbody>{sorted.map(ex => (
              <tr key={ex.id} className="tr">
                <td><span className="id-chip">EX-{ex.id}</span></td>
                <td>{ex.requestId ? `REQ-${ex.requestId}` : "—"}</td>
                <td className="td-main">{ex.businessJustification}</td>
                <td>{ex.requestedDurationDays}d</td>
                <td><Badge s={ex.status}>{ex.status}</Badge></td>
                {canR && <td>{(ex.status === "PENDING" || ex.status === "OPEN") ? (
                  <div className="act-btns"><button className="btn-approve" onClick={() => approve(ex.id)}>Approve</button><button className="btn-reject" onClick={() => reject(ex.id)}>Reject</button></div>
                ) : <span className="act-done">Decided</span>}</td>}
              </tr>
            ))}</tbody>
          </table>
        )}</div>
      )}
      {tab === "new" && (
        <div className="form-card fade-in">
          <h3>Request Exception</h3>
          <p className="form-sub">Submit a time-limited exception request with full business justification.</p>
          <form onSubmit={create} className="gform">
            <div className="fg"><label>Governance Request *</label>
              <select value={form.requestId} onChange={e => setForm({ ...form, requestId: e.target.value })} required>
                <option value="">Select a request…</option>
                {requests.filter(r => r.status === "PENDING" || r.status === "PENDING_REVIEW").map(r => <option key={r.id} value={r.id}>REQ-{r.id} — {r.intent?.substring(0, 55)}</option>)}
              </select>
            </div>
            <div className="fg"><label>Business Justification *</label>
              <small className="hint">Cover three things: what you need to do, why the approved option does not work, and why it is needed now.</small><textarea value={form.businessJustification} onChange={e => setForm({ ...form, businessJustification: e.target.value })} placeholder="e.g. Finance must summarize 40 vendor contracts before the 30 June audit. The approved tool cannot read PDFs. Only internal data is used and nothing leaves the company." rows={4} required /></div>
            <div className="fg" style={{ width: 200 }}><label>Duration (days)</label>
              <small className="hint">1 to 90 days. Shorter requests are easier to approve.</small><input type="number" min={1} max={90} value={form.requestedDurationDays} onChange={e => setForm({ ...form, requestedDurationDays: e.target.value })} /></div>
            <div className="fact"><button type="button" className="btn-o" onClick={() => setTab("list")}>Cancel</button><button type="submit" className="btn-p">Submit Exception</button></div>
          </form>
        </div>
      )}
    </div>
  );
}

/* =========================================================
   INVESTIGATIONS
   ========================================================= */
function Investigations({ investigations, resolve }) {
  const [sel, setSel] = useState(null);
  const STAGES = ["DISCOVERY", "RISK ASSESSMENT", "POLICY ANALYSIS", "HUMAN REVIEW", "RESOLUTION", "AUDIT"];
  const stageMap = { OPEN: 1, UNDER_REVIEW: 3, PENDING_REVIEW: 3, RESOLVED: 5, CLOSED: 5 };
  const inv = sel ? investigations.find(i => i.id === sel) : null;
  const curStage = inv ? (stageMap[inv?.status] ?? 0) : 0;
  return (
    <div className="page fade-in">
      <div className="inv-layout">
        <div className="inv-list">
          <div className="inv-list-head">Investigations</div>
          {investigations.length === 0 ? <div className="empty-sm">No active investigations</div> : investigations.map(i => (
            <button key={i.id} className={`inv-item ${sel === i.id ? "inv-sel" : ""}`} onClick={() => { setSel(i.id); if (innerWidth <= 1024) setTimeout(() => document.querySelector(".inv-detail")?.scrollIntoView({ behavior: "smooth", block: "start" }), 60); }}>
              <div className="inv-item-top"><span className="id-chip">INV-{i.id}</span><Badge s={i.riskLevel}>{i.riskLevel}</Badge></div>
              <div className="inv-item-name">{i.toolName}</div>
              <Badge s={i.status}>{(i.status || "").replace("_", " ")}</Badge>
            </button>
          ))}
        </div>
        <div className="inv-detail">
          {!inv ? (
            <div className="inv-empty"><span>⊟</span><p>Select an investigation to view the governance workflow</p></div>
          ) : (
            <>
              <div className="inv-dhead">
                <div><span className="id-chip">INV-{inv.id}</span><h3>{inv.toolName}</h3></div>
                <Badge s={inv.riskLevel}>{inv.riskLevel} RISK</Badge>
              </div>
              {/* Stage pipeline */}
              <div className="pipeline">
                {STAGES.map((s, i) => (
                  <div key={s} className={`pip-step ${i < curStage ? "pip-done" : i === curStage ? "pip-cur" : ""}`}>
                    <div className="pip-dot">{i < curStage ? "✓" : i === curStage ? "◉" : "○"}</div>
                    <div className="pip-name">{s}</div>
                    {i < STAGES.length - 1 && <div className="pip-line" />}
                  </div>
                ))}
              </div>
              <div className="inv-meta">
                {[["Status", <Badge s={inv.status}>{(inv.status || "").replace("_", " ")}</Badge>], ["Department", inv.department || "—"], ["Data Classification", inv.dataClassification || "—"]].map(([k, v]) => (
                  <div key={k} className="inv-mrow"><span className="inv-mk">{k}</span><span>{v}</span></div>
                ))}
              </div>
              {inv.description && <p className="inv-desc">{inv.description}</p>}
              {(inv.status === "OPEN" || inv.status === "UNDER_REVIEW" || inv.status === "PENDING_REVIEW") && (
                <button className="btn-p" onClick={() => resolve(inv.id)}>Mark as Resolved →</button>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   AI TOOLS
   ========================================================= */
function Tools({ tools }) {
  const [q, setQ] = useState("");
  const [f, setF] = useState("ALL");
  const statuses = ["ALL", ...new Set(tools.map(t => t.governanceStatus || t.status).filter(Boolean))];
  const filtered = tools.filter(t => (!q || (t.name || "").toLowerCase().includes(q.toLowerCase())) && (f === "ALL" || t.governanceStatus === f || t.status === f));
  return (
    <div className="page fade-in">
      <div className="toolbar">
        <input className="search" type="search" placeholder="Search AI tools…" value={q} onChange={e => setQ(e.target.value)} />
        <div className="filters">{statuses.map(s => <button key={s} className={`filter-btn ${f === s ? "filter-on" : ""}`} onClick={() => setF(s)}>{s}</button>)}</div>
      </div>
      {filtered.length === 0 ? <div className="empty"><span>⊕</span><p>{tools.length === 0 ? "No AI tools in inventory." : "No tools match."}</p></div> : (
        <div className="tools-grid">
          {filtered.map(t => (
            <div key={t.id} className="tool-card">
              <div className="tc-header"><div className="tc-icon">{(t.name || "T")[0]}</div><div><div className="tc-name">{t.name}</div><div className="tc-cat">{t.category || t.type}</div></div></div>
              <div className="tc-badges"><Badge s={t.governanceStatus || t.status}>{t.governanceStatus || t.status}</Badge>{t.riskLevel && <Badge s={t.riskLevel}>{t.riskLevel}</Badge>}</div>
              {t.vendor && <div className="tc-meta">Vendor: {t.vendor}</div>}
              {t.department && <div className="tc-meta">{t.department}</div>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* =========================================================
   SHADOW AI DISCOVERY
   ========================================================= */
function Discovery({ tools, discover }) {
  const [step, setStep] = useState(1);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const [form, setForm] = useState({ name: "", type: "EXTERNAL_AI_ASSISTANT", dataClassification: "INTERNAL", owner: "", businessUse: "", department: "ENGINEERING", vendor: "" });
  const STEPS = ["Identify Tool", "Classification", "Data Handling", "Ownership", "Business Use"];

  const submit = async () => {
    setBusy(true);
    try { const r = await discover({ ...form, reportedBy: uid(localStorage.getItem("username") || "") }); setResult(r || { riskLevel: "HIGH", governanceStatus: "PENDING", recommendedAction: "Initiate governance review" }); }
    catch { setResult({ riskLevel: "HIGH", governanceStatus: "PENDING", recommendedAction: "Initiate governance review" }); }
    finally { setBusy(false); }
  };

  if (result) return (
    <div className="page fade-in">
      <div className="disc-result">
        <div className="dr-icon">◎</div>
        <div className="eyebrow">SHADOW AI SIGNAL — DISCOVERY COMPLETE</div>
        <h3>{form.name}</h3>
        <div className="dr-rows">
          {[["Risk Level", <Badge s={result.riskLevel}>{result.riskLevel}</Badge>], ["Data Policy", <Badge s={form.dataClassification}>{form.dataClassification}</Badge>], ["Governance State", <Badge s={result.governanceStatus}>{result.governanceStatus || "PENDING REVIEW"}</Badge>], ["Recommended Action", <strong>{result.recommendedAction}</strong>]].map(([k, v]) => (
            <div key={k} className="dr-row"><span>{k}</span><span>{v}</span></div>
          ))}
        </div>
        <button className="btn-p" onClick={() => { setResult(null); setStep(1); setForm({ name: "", type: "EXTERNAL_AI_ASSISTANT", dataClassification: "INTERNAL", owner: "", businessUse: "", department: "ENGINEERING", vendor: "" }); }}>Discover Another Tool</button>
      </div>
      {tools.length > 0 && (
        <div className="tcard" style={{ marginTop: 28 }}>
          <div className="card-head"><span className="card-title">Shadow AI Inventory ({tools.length})</span></div>
          <table className="dt"><thead><tr><th>Tool</th><th>Risk</th><th>Status</th><th>Dept</th></tr></thead>
          <tbody>{tools.map(t => <tr key={t.id} className="tr"><td className="td-main">{t.name}</td><td><Badge s={t.riskLevel}>{t.riskLevel}</Badge></td><td><Badge s={t.governanceStatus}>{t.governanceStatus}</Badge></td><td>{t.department}</td></tr>)}</tbody></table>
        </div>
      )}
    </div>
  );

  return (
    <div className="page fade-in">
      <div className="disc-flow">{[["1", "Report", "Name an AI tool people use outside the approved list."], ["2", "Assess", "ShadowAI scores its risk from the data it touches."], ["3", "Govern", "High risk opens an investigation for a human decision."]].map(([n, t, d]) => <div key={n} className="df-step"><b>{n}</b><div><strong>{t}</strong><span>{d}</span></div></div>)}</div>
      <div className="wizard">
        <div className="wiz-steps">
          {STEPS.map((s, i) => (
            <div key={s} className={`wstep ${i < step - 1 ? "ws-done" : i === step - 1 ? "ws-cur" : ""}`}>
              <div className="ws-dot">{i < step - 1 ? "✓" : i + 1}</div>
              <span>{s}</span>
              {i < STEPS.length - 1 && <div className="ws-line" />}
            </div>
          ))}
        </div>
        <div className="wiz-body">
          {step === 1 && <div className="fade-in"><h3>What AI tool did you discover?</h3><p className="hint">Use the name people know it by, e.g. "ChatGPT (personal account)" or "Grammarly browser extension".</p><div className="fg"><label>Tool Name *</label><input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. External Research Assistant" /></div><div className="fg"><label>Vendor</label><input value={form.vendor} onChange={e => setForm({ ...form, vendor: e.target.value })} placeholder="e.g. OpenAI, Anthropic…" /></div></div>}
          {step === 2 && <div className="fade-in"><h3>What type of AI tool?</h3><div className="fg"><label>Tool Type</label><select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>{["EXTERNAL_AI_ASSISTANT", "CODE_ASSISTANT", "DATA_ANALYSIS", "IMAGE_GENERATION", "DOCUMENT_PROCESSING", "OTHER"].map(v => <option key={v}>{v.replace(/_/g, " ")}</option>)}</select></div><div className="fg"><label>Department</label><select value={form.department} onChange={e => setForm({ ...form, department: e.target.value })}>{["ENGINEERING", "SALES", "MARKETING", "FINANCE", "HR", "OPERATIONS"].map(v => <option key={v}>{v}</option>)}</select></div></div>}
          {step === 3 && <div className="fade-in"><h3>What data does it handle?</h3><div className="radio-group">{["PUBLIC", "INTERNAL", "CONFIDENTIAL", "RESTRICTED"].map(dc => <label key={dc} className={`radio-opt ${form.dataClassification === dc ? "radio-on" : ""}`}><input type="radio" name="dc" value={dc} checked={form.dataClassification === dc} onChange={() => setForm({ ...form, dataClassification: dc })} />{dc}</label>)}</div></div>}
          {step === 4 && <div className="fade-in"><h3>Who owns this tool?</h3><p className="hint">The person or team who asked for it or uses it most. Leave blank if unknown.</p><div className="fg"><label>Business Owner</label><input value={form.owner} onChange={e => setForm({ ...form, owner: e.target.value })} placeholder="Name or email of owner" /></div></div>}
          {step === 5 && <div className="fade-in"><h3>What is the business use?</h3><p className="hint">One or two sentences: what people do with it and what data goes in.</p><div className="fg"><label>Description *</label><textarea value={form.businessUse} onChange={e => setForm({ ...form, businessUse: e.target.value })} placeholder="Describe how this tool is being used…" rows={4} /></div></div>}
        </div>
        <div className="wiz-foot">
          {step > 1 && <button className="btn-o" onClick={() => setStep(s => s - 1)}>Back</button>}
          {step < 5 ? <button className="btn-p" onClick={() => setStep(s => s + 1)} disabled={step === 1 && !form.name.trim()}>Next →</button>
            : <button className="btn-p" onClick={submit} disabled={busy || !form.businessUse.trim()}>{busy ? "Submitting…" : "Discover & Assess →"}</button>}
        </div>
      </div>
      {tools.length > 0 && (
        <div className="tcard" style={{ marginTop: 28 }}>
          <div className="card-head"><span className="card-title">Known Shadow AI ({tools.length})</span></div>
          <table className="dt"><thead><tr><th>Tool</th><th>Risk</th><th>Status</th><th>Dept</th></tr></thead>
          <tbody>{tools.map(t => <tr key={t.id} className="tr"><td className="td-main">{t.name}</td><td><Badge s={t.riskLevel}>{t.riskLevel}</Badge></td><td><Badge s={t.governanceStatus}>{t.governanceStatus}</Badge></td><td>{t.department}</td></tr>)}</tbody></table>
        </div>
      )}
    </div>
  );
}

/* =========================================================
   POLICIES
   ========================================================= */

/* =========================================================
   INTEGRATIONS
   ========================================================= */
function Integrations({ integrations, create, role }) {
  const canM = REVIEW_ROLES.includes(role);
  return (
    <div className="page fade-in">
      {canM && <div className="toolbar-right"><button className="btn-p" onClick={create}>+ Add Integration</button></div>}
      {integrations.length === 0 ? <div className="empty"><span><PlugIcon s={34} /></span><p>No integrations configured.</p>{canM && <button className="btn-p" onClick={create}>Add Integration</button>}</div> : (
        <div className="intg-grid">
          {integrations.map(i => (
            <div key={i.id} className="intg-card">
              <div className="intg-head"><span className="intg-icon"><PlugIcon s={20} /></span><div><div className="intg-name">{i.name}</div><div className="intg-prov">{i.provider}</div></div></div>
              <div className="intg-foot"><Badge s={i.status}>{i.status}</Badge>{i.lastEvent && <span className="intg-last">{new Date(i.lastEvent).toLocaleDateString()}</span>}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* =========================================================
   AUDIT LOG
   ========================================================= */
function Audit({ events }) {
  const [q, setQ] = useState("");
  const [pg, setPg] = useState(1);
  const PS = 20;
  const filtered = events.filter(e => !q || JSON.stringify(e).toLowerCase().includes(q.toLowerCase()));
  const total = Math.ceil(filtered.length / PS);
  const vis = filtered.slice((pg - 1) * PS, pg * PS);
  return (
    <div className="page fade-in">
      <div className="toolbar"><input className="search" type="search" placeholder="Search audit events…" value={q} onChange={e => { setQ(e.target.value); setPg(1); }} /><span className="res-count">{filtered.length} events</span></div>
      {events.length === 0 ? <div className="empty"><span>☰</span><p>No audit events.</p></div> : (
        <>
          <div className="audit-tl">
            {vis.map((ev, i) => (
              <div key={ev.id || i} className="audit-ev">
                <div className="audit-dot" />
                <div className="audit-body">
                  <div className="audit-top"><span className="audit-action">{ev.action || ev.eventType}</span><span className="audit-time">{ev.createdAt ? new Date(ev.createdAt).toLocaleString("en-GB", { dateStyle: "short", timeStyle: "short" }) : ""}</span></div>
                  <div className="audit-actor">{ev.actor || ev.actorUsername || "System"}</div>
                  {ev.details && <div className="audit-det">{typeof ev.details === "string" ? ev.details : JSON.stringify(ev.details)}</div>}
                </div>
              </div>
            ))}
          </div>
          {total > 1 && <div className="pagination"><button disabled={pg === 1} onClick={() => setPg(p => p - 1)} className="btn-o btn-sm">← Prev</button><span>Page {pg} of {total}</span><button disabled={pg === total} onClick={() => setPg(p => p + 1)} className="btn-o btn-sm">Next →</button></div>}
        </>
      )}
    </div>
  );
}

/* =========================================================
   NOTIFICATIONS
   ========================================================= */
function Notifications({ notifications }) {
  const sorted = [...notifications].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
  return (
    <div className="page fade-in">
      {sorted.length === 0 ? <div className="empty"><span>◆</span><p>No notifications.</p></div> : (
        <div className="notif-list">
          {sorted.map((n, i) => (
            <div key={n.id || i} className={`notif ${n.read ? "notif-read" : "notif-new"}`}>
              <div className={`notif-bar sev-${(n.severity || "info").toLowerCase()}`} />
              <div className="notif-body">
                <div className="notif-title">{n.title || n.message}</div>
                {n.message && n.title && <div className="notif-msg">{n.message}</div>}
                <div className="notif-foot"><Badge s={n.severity}>{n.severity}</Badge><span>{n.createdAt ? new Date(n.createdAt).toLocaleString("en-GB", { dateStyle: "short", timeStyle: "short" }) : ""}</span></div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* =========================================================
   GUIDE
   ========================================================= */
function Guide({ setPage }) {
  return (
    <div className="page fade-in">
      <div className="guide-hero">
        <div className="guide-net"><GovernanceNetwork size={160} /></div>
        <div className="guide-hero-text">
          <span className="eyebrow">SHADOWAI PRODUCT GUIDE</span>
          <h2>AI Governance Decision Assistant</h2>
          <p>Enterprise-grade AI governance combining policy enforcement, human decision workflows, Shadow AI discovery and auditable governance records.</p>
        </div>
      </div>
      <div className="guide-grid">
        {[
          { icon: "◉", title: "Command Center", items: ["Real governance posture score from live data", "Interactive AI Governance Network visualization", "Safe Alternative Recommendation engine", "Recent governance activity feed"] },
          { icon: "⊞", title: "Requests & Exceptions", items: ["Employees submit AI capability requests", "Governance reviewers approve, review or block", "Exception workflow with business justification", "Every decision generates an auditable receipt"] },
          { icon: "◎", title: "Shadow AI Discovery", items: ["5-step wizard to report unauthorized AI tools", "Automatic risk assessment and classification", "Governance state recommendation from engine", "Tools added to the governed AI inventory"] },
          { icon: "☰", title: "Audit & Compliance", items: ["Full audit trail of every governance decision", "Policy evaluation records with explanations", "Timestamp, actor, and policy for every event", "Export-ready for compliance reporting"] },
        ].map(s => (
          <div key={s.title} className="guide-section">
            <div className="gs-head"><span className="gs-icon">{s.icon}</span><h3>{s.title}</h3></div>
            <ul className="gs-list">{s.items.map((item, i) => <li key={i}>{item}</li>)}</ul>
          </div>
        ))}
      </div>
      <div className="guide-nav">
        <span>Quick Navigation</span>
        <div className="guide-nav-btns">
          {[["dashboard", "◉ Command Center"], ["requests", "⊞ Requests"], ["investigations", "⊟ Investigations"], ["discovery", "◎ Discovery"], ["audit", "☰ Audit Log"]].map(([id, l]) => (
            <button key={id} className="gnav-btn" onClick={() => setPage(id)}>{l}</button>
          ))}
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   WORKSPACE INIT SCREEN
   ========================================================= */
function WorkspaceInit({ states, onDone }) {
  const items = [
    ["auth", "Authentication"], ["governance", "Governance context"], ["policies", "Policies"],
    ["tools", "AI Tool inventory"], ["requests", "Requests"], ["notifications", "Notifications"],
  ];
  const allDone = items.every(([k]) => states[k] === "done");
  useEffect(() => { const t = setTimeout(onDone, allDone ? 600 : 15000); return () => clearTimeout(t); }, [allDone]);
  return (
    <div className="winit">
      <div className="winit-bg"><ParticleCanvas count={60} color="99,102,241" speed={0.2} /></div>
      <div className="orb orb1" /><div className="orb orb2" />
      <div className="winit-content fade-in">
        <div className="winit-brand">
          <svg width="22" height="22" viewBox="0 0 20 20"><polygon points="10,2 18,7 18,13 10,18 2,13 2,7" fill="none" stroke="#6366f1" strokeWidth="1.5" /><circle cx="10" cy="10" r="2.5" fill="#6366f1" /></svg>
          <span>ShadowAI Workspace</span>
        </div>
        <h2>Initializing governance workspace…</h2>
        <div className="winit-rows">
          {items.map(([k, l], i) => (
            <div key={k} className={`winit-row ${states[k] === "done" ? "wr-done" : states[k] === "loading" ? "wr-loading" : ""}`} style={{ animationDelay: `${i * 0.07}s` }}>
              <span>{l}</span>
              <span className="wr-icon">{states[k] === "done" ? "✓" : states[k] === "loading" ? <span className="btn-spinner" style={{ width: 12, height: 12, borderWidth: 2 }} /> : "◌"}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   APP
   ========================================================= */
export default function Workspace({ onExit, theme, setTheme }) {
  useGlow();
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [user, setUser] = useState(localStorage.getItem("username") || "");
  const [role, setRole] = useState(localStorage.getItem("role") || "");
  const [isDemoMode, setIsDemoMode] = useState(false);
  const [page, setPage] = useState("dashboard");
  const [navOpen, setNavOpen] = useState(false);
  useEffect(() => {
    if (!navOpen) return;
    const k = e => e.key === "Escape" && setNavOpen(false), r = () => innerWidth > 768 && setNavOpen(false);
    addEventListener("keydown", k); addEventListener("resize", r); document.body.style.overflow = "hidden";
    return () => { removeEventListener("keydown", k); removeEventListener("resize", r); document.body.style.overflow = ""; };
  }, [navOpen]);
  useEffect(() => { scrollTo({ top: 0 }); }, [page]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [apiOk, setApiOk] = useState(true);
  const [initDone, setInitDone] = useState(false);
  const [initStates, setInitStates] = useState({ auth: "done", governance: "waiting", policies: "waiting", tools: "waiting", requests: "waiting", notifications: "waiting" });

  const [dash, setDash] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [requests, setRequests] = useState([]);
  const [exceptions, setExceptions] = useState([]);
  const [investigations, setInvestigations] = useState([]);
  const [integrations, setIntegrations] = useState([]);
  const [tools, setTools] = useState([]);
  const [discoveredTools, setDiscoveredTools] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [auditEvents, setAuditEvents] = useState([]);
  const [notifications, setNotifications] = useState([]);

  const [login, setLogin] = useState({ username: "aruna.it", password: "Demo@123" });
  const [loginError, setLoginError] = useState("");
  const [intent, setIntent] = useState("I need help debugging internal API code with no customer data");
  const [dataType, setDataType] = useState("INTERNAL");
  const [recommendation, setRecommendation] = useState(null);
  const [requestForm, setRequestForm] = useState({ intent: "", dataType: "INTERNAL", department: "ENGINEERING", frequency: "DAILY", requestedTool: "" });
  const [exceptionForm, setExceptionForm] = useState({ requestId: "", businessJustification: "", requestedDurationDays: 7 });
  const [exceptionDecision, setExceptionDecision] = useState({ reviewerId: "", reason: "" });

  /* --- demo mode --- */
  const activateDemo = useCallback(() => {
    setIsDemoMode(true);
    setToken("demo");
    setUser(DEMO.user);
    setRole(DEMO.role);
    setDash(DEMO.dash);
    setRequests(DEMO.requests);
    setExceptions(DEMO.exceptions);
    setInvestigations(DEMO.investigations);
    setTools(DEMO.tools);
    setDiscoveredTools(DEMO.discoveredTools);
    setPolicies(DEMO.policies);
    setIntegrations(DEMO.integrations);
    setAuditEvents(DEMO.auditEvents);
    setNotifications(DEMO.notifications);
    setAnalytics(DEMO.analytics);
    setInitStates({ auth: "done", governance: "done", policies: "done", tools: "done", requests: "done", notifications: "done" });
    setApiOk(false);
  }, []);

  /* --- role sign-in: start from the same governance dataset the demo workspace uses, then overlay live API data when available --- */
  const [seeded, setSeeded] = useState(false);
  const fb = isDemoMode || SIMULATED || seeded;
  const seedData = useCallback(() => {
    setDash(DEMO.dash); setRequests(DEMO.requests); setExceptions(DEMO.exceptions); setInvestigations(DEMO.investigations);
    setTools(DEMO.tools); setDiscoveredTools(DEMO.discoveredTools); setPolicies(DEMO.policies); setIntegrations(DEMO.integrations);
    setAuditEvents(DEMO.auditEvents); setNotifications(DEMO.notifications); setAnalytics(DEMO.analytics);
  }, []);
  useEffect(() => { if (!token || token === "demo" || isDemoMode || seeded) return; seedData(); setSeeded(true);
    if (SIMULATED) { setApiOk(true); setInitStates({ auth: "done", governance: "done", policies: "done", tools: "done", requests: "done", notifications: "done" }); }
  }, [token, isDemoMode, seeded, seedData]);

  /* --- api helpers --- */
  const setIS = (k, v) => setInitStates(p => ({ ...p, [k]: v }));

  async function apiFetch(method, path, body, retry = true) {
    if (isDemoMode || SIMULATED) throw new Error("Demo mode — API calls disabled");
    const t = localStorage.getItem("token") || token;
    const h = { "Content-Type": "application/json" };
    if (t) h.Authorization = `Bearer ${t}`;
    let res;
    try { res = await fetch(API + path, { method, headers: h, body: body ? JSON.stringify(body) : undefined }); }
    catch { const e = new Error("Cannot connect to backend"); e.status = 0; throw e; }
    if (res.status === 401 && retry) {
      const rt = localStorage.getItem("refreshToken");
      if (rt) {
        try {
          const r2 = await fetch(API + "/api/auth/refresh", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken: rt }) });
          if (r2.ok) { const d = await r2.json(); localStorage.setItem("token", d.token); localStorage.setItem("refreshToken", d.refreshToken); setToken(d.token); return apiFetch(method, path, body, false); }
        } catch { }
      }
      const e = new Error("Session expired"); e.status = 401; throw e;
    }
    if (res.status === 403) { const e = new Error("FORBIDDEN"); e.status = 403; throw e; }
    if (res.status === 409) throw new Error("Already decided");
    if (!res.ok) { let m = `Error ${res.status}`; try { const d = await res.json(); m = d.error || d.message || m; } catch { } throw new Error(m); }
    const ct = res.headers.get("content-type") || "";
    return ct.includes("json") ? res.json() : null;
  }

  const get = p => apiFetch("GET", p);
  const post = (p, b) => apiFetch("POST", p, b);

  function showErr(m) { setError(m || "Error"); setTimeout(() => setError(""), 6000); }

  async function refresh() {
    if (!token || isDemoMode) return;
    if (SIMULATED) { setLoading(true); await new Promise(r => setTimeout(r, 450)); setLoading(false); return; }
    setLoading(true);
    try {
      setIS("governance", "loading");
      const [dr, rr, ir] = await Promise.allSettled([get("/api/v1/dashboard/summary"), get("/api/v1/requests"), get("/api/v1/investigations")]);
      const has = v => Array.isArray(v) ? v.length > 0 : !!v;
      if (dr.status === "fulfilled" && has(dr.value)) setDash(d => ({ ...(d || {}), ...dr.value }));
      if (rr.status === "fulfilled" && has(rr.value)) setRequests(rr.value);
      if (ir.status === "fulfilled" && has(ir.value)) setInvestigations(ir.value);
      setIS("governance", "done"); setIS("requests", "done");
      setApiOk(dr.status === "fulfilled" || rr.status === "fulfilled" || ir.status === "fulfilled");
      if (REVIEW_ROLES.includes(role)) {
        setIS("policies", "loading"); setIS("tools", "loading");
        const [er, ingr, tr, discr, polr, ar] = await Promise.allSettled([get("/api/v1/exceptions"), get("/api/v1/integrations"), get("/api/v1/tools"), get("/api/v1/discovery/tools"), get("/api/v1/policies"), get("/api/v1/audit?limit=100")]);
        if (er.status === "fulfilled" && has(er.value)) setExceptions(er.value);
        if (ingr.status === "fulfilled" && has(ingr.value)) setIntegrations(ingr.value);
        if (tr.status === "fulfilled") { if (has(tr.value)) setTools(tr.value); setIS("tools", "done"); } else setIS("tools", "done");
        if (discr.status === "fulfilled" && has(discr.value)) setDiscoveredTools(discr.value);
        if (polr.status === "fulfilled") { if (has(polr.value)) setPolicies(polr.value); setIS("policies", "done"); } else setIS("policies", "done");
        if (ar.status === "fulfilled" && has(ar.value)) setAuditEvents(ar.value);
      } else { setIS("policies", "done"); setIS("tools", "done"); }
      try { const an = await get("/api/v1/analytics/overview"); if (an) setAnalytics(a => ({ ...(a || {}), ...an })); } catch { }
      setIS("notifications", "loading");
      try { const nt = await get("/api/v1/notifications"); if (has(nt)) setNotifications(nt); } catch { } finally { setIS("notifications", "done"); }
    } catch (e) {
      if (e.status === 0) setApiOk(false);
      if (e.status !== 401 && e.status !== 403) showErr(e.message);
    } finally { setLoading(false); }
  }

  useEffect(() => { if (token && !isDemoMode) refresh(); }, [token, role]);

  useEffect(() => {
    if (!token || isDemoMode || SIMULATED) return;
    let ev;
    try { ev = new EventSource(API + "/api/v1/events/stream"); ev.addEventListener("investigation.created", refresh); ev.addEventListener("notification.created", refresh); ev.onerror = () => { }; }
    catch { }
    return () => ev?.close();
  }, [token, isDemoMode]);

  async function signIn(e) {
    e.preventDefault(); setLoginError("");
    if (isDemoMode) return;
    try {
      const res = await fetch(API + "/api/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(login) });
      if (!res.ok) { let m = "Invalid credentials"; try { const d = await res.json(); m = d.error || m; } catch { } throw new Error(m); }
      const data = await res.json();
      if (!data.token) throw new Error("Login failed");
      localStorage.setItem("token", data.token);
      localStorage.setItem("refreshToken", data.refreshToken || "");
      localStorage.setItem("username", data.username || login.username);
      localStorage.setItem("role", data.role || "");
      setToken(data.token); setUser(data.username || login.username); setRole(data.role || ""); setPage("dashboard");
    } catch (e) { setLoginError(e.message); }
  }

  async function logout() {
    const rt = localStorage.getItem("refreshToken");
    if (rt && !isDemoMode) { try { await fetch(API + "/api/auth/logout", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken: rt }) }); } catch { } }
    ["token", "refreshToken", "username", "role", "display_name"].forEach(k => localStorage.removeItem(k));
    setToken(null); setUser(""); setRole(""); setIsDemoMode(false); setPage("dashboard"); setInitDone(false); sessionStorage.removeItem("sa_demo"); onExit?.();
  }

  async function recommend() {
    setLoading(true); setRecommendation(null);
    try {
      if (isDemoMode) {
        await new Promise(r => setTimeout(r, 800));
        const isRestricted = dataType === "RESTRICTED" || dataType === "CONFIDENTIAL";
        setRecommendation({ decision: isRestricted ? "REVIEW" : "APPROVE", recommendedTool: isRestricted ? "Internal Analytics Platform" : "GitHub Copilot", reason: isRestricted ? "Data classification requires governance review before external AI processing." : "This use case aligns with the Developer AI Tools Policy. Proceed with approved tools.", policyName: isRestricted ? "Customer Data Governance Policy v3.2" : "Developer AI Tools Policy v2.0" });
        return;
      }
      const q = new URLSearchParams({ intent, dataType, department: "ENGINEERING", frequency: "DAILY" });
      setRecommendation(await apiFetch("POST", `/api/v1/recommendations?${q}`));
    } catch (e) {
      if (fb) { await new Promise(r => setTimeout(r, 500)); const isRestricted = dataType === "RESTRICTED" || dataType === "CONFIDENTIAL"; setRecommendation({ decision: isRestricted ? "REVIEW" : "APPROVE", recommendedTool: isRestricted ? "Internal Analytics Platform" : "GitHub Copilot", reason: isRestricted ? "Data classification requires governance review before external AI processing." : "This use case aligns with the Developer AI Tools Policy. Proceed with approved tools.", policyName: isRestricted ? "Customer Data Governance Policy v3.2" : "Developer AI Tools Policy v2.0" }); }
      else showErr(e.message);
    } finally { setLoading(false); }
  }

  const approveRequest = async id => { try { await post(`/api/v1/requests/${id}/approve?reason=${encodeURIComponent("Approved after governance review")}`); await refresh(); } catch (e) { if (!fb) showErr(e.message); else setRequests(p => p.map(r => r.id === id ? { ...r, status: "APPROVED" } : r)); } };
  const rejectRequest = async id => { try { await post(`/api/v1/requests/${id}/reject?reason=${encodeURIComponent("Rejected")}`); await refresh(); } catch { if (fb) setRequests(p => p.map(r => r.id === id ? { ...r, status: "REJECTED" } : r)); } };
  const resolveInv = async id => { try { await post(`/api/v1/investigations/${id}/resolve?resolution=${encodeURIComponent("Resolved")}`); await refresh(); } catch { if (fb) setInvestigations(p => p.map(i => i.id === id ? { ...i, status: "RESOLVED" } : i)); } };
  const createRequest = async e => { e.preventDefault(); if (!requestForm.intent.trim()) { showErr("Describe your use case"); return; } try { await post("/api/v1/requests", { ...requestForm, employeeId: uid(user) }); setRequestForm({ ...requestForm, intent: "" }); await refresh(); } catch { if (fb) { setRequests(p => [{ id: Math.max(...p.map(r => r.id)) + 1, ...requestForm, status: "PENDING" }, ...p]); setRequestForm({ ...requestForm, intent: "" }); } } };
  const createException = async e => { e.preventDefault(); if (!exceptionForm.requestId || !exceptionForm.businessJustification.trim()) { showErr("Fill required fields"); return; } try { await post("/api/v1/exceptions", { requestId: Number(exceptionForm.requestId), requestedBy: uid(user), businessJustification: exceptionForm.businessJustification, requestedDurationDays: Number(exceptionForm.requestedDurationDays) }); setExceptionForm({ requestId: "", businessJustification: "", requestedDurationDays: 7 }); await refresh(); } catch { if (fb) { setExceptions(p => [{ id: Date.now(), ...exceptionForm, status: "PENDING" }, ...p]); setExceptionForm({ requestId: "", businessJustification: "", requestedDurationDays: 7 }); } } };
  const approveException = async id => { if (!exceptionDecision.reason.trim()) { showErr("Enter approval reason"); return; } try { await post(`/api/v1/exceptions/${id}/approve`, { reviewerId: uid(user), reason: exceptionDecision.reason }); setExceptionDecision({ reviewerId: "", reason: "" }); await refresh(); } catch { if (fb) setExceptions(p => p.map(ex => ex.id === id ? { ...ex, status: "APPROVED" } : ex)); } };
  const rejectException = async id => { if (!exceptionDecision.reason.trim()) { showErr("Enter rejection reason"); return; } try { await post(`/api/v1/exceptions/${id}/reject`, { reviewerId: uid(user), reason: exceptionDecision.reason }); setExceptionDecision({ reviewerId: "", reason: "" }); await refresh(); } catch { if (fb) setExceptions(p => p.map(ex => ex.id === id ? { ...ex, status: "REJECTED" } : ex)); } };
  const createIntegration = async () => { const name = window.prompt("Integration name", "Security Event Feed"); if (!name) return; const secret = (crypto.randomUUID?.() || String(Date.now())).replace(/-/g, ""); try { await post(`/api/v1/integrations?name=${encodeURIComponent(name)}&provider=GENERIC_WEBHOOK&secret=${secret}&actor=${uid(user)}`); window.alert("Webhook secret (shown once, copy it now):\n" + secret); await refresh(); } catch { if (fb) setIntegrations(p => [{ id: Date.now(), name, provider: "GENERIC_WEBHOOK", status: "ACTIVE" }, ...p]); } };
  const discoverTool = async payload => { try { const r = await post("/api/v1/discovery/tools", payload); await refresh(); return r; } catch { if (fb) { const r = { riskLevel: payload.dataClassification === "RESTRICTED" ? "HIGH" : "MEDIUM", governanceStatus: "PENDING", recommendedAction: "Initiate governance review" }; setDiscoveredTools(p => [{ id: Date.now(), name: payload.name, type: payload.type, riskLevel: r.riskLevel, governanceStatus: r.governanceStatus, department: payload.department }, ...p]); return r; } throw payload; } };

  useEffect(() => { if (sessionStorage.getItem("sa_demo")) activateDemo(); }, [activateDemo]);

  /* --- render --- */
  if (!token) return null;
  if (!initDone) return <WorkspaceInit states={initStates} onDone={() => setInitDone(true)} />;

  return (
    <div className="app">
      {error && <Toast msg={error} type="error" onClose={() => setError("")} />}
      <Sidebar page={page} setPage={setPage} user={localStorage.getItem("display_name") || user} role={role} logout={logout} notifs={notifications} isDemoMode={isDemoMode} open={navOpen} onClose={() => setNavOpen(false)} />
      <div className={`nav-scrim ${navOpen ? "show" : ""}`} onClick={() => setNavOpen(false)} aria-hidden="true" />
      <div className="app-main">
        <Topbar page={page} loading={loading} refresh={refresh} apiOk={apiOk} isDemoMode={isDemoMode} theme={theme} setTheme={setTheme} onMenu={() => setNavOpen(o => !o)} navOpen={navOpen} />
        <div className="app-content" id="main-content" key={page}>
          {page === "dashboard" && <Dashboard dash={dash} analytics={analytics} intent={intent} setIntent={setIntent} dataType={dataType} setDataType={setDataType} recommend={recommend} recommendation={recommendation} busy={loading} setPage={setPage} tools={tools} policies={policies} investigations={investigations} notifications={notifications} role={role} isDemoMode={isDemoMode} />}
          {page === "requests" && <Requests requests={requests} approve={approveRequest} reject={rejectRequest} form={requestForm} setForm={setRequestForm} create={createRequest} />}
          {page === "exceptions" && <Exceptions exceptions={exceptions} requests={requests} form={exceptionForm} setForm={setExceptionForm} create={createException} approve={approveException} reject={rejectException} role={role} />}
          {page === "investigations" && <Investigations investigations={investigations} resolve={resolveInv} />}
          {page === "tools" && <Tools tools={tools} />}
          {page === "discovery" && <Discovery tools={discoveredTools} discover={discoverTool} />}
          {page === "policies" && <Policies policies={policies} />}
          {page === "integrations" && <Integrations integrations={integrations} create={createIntegration} role={role} />}
          {page === "audit" && <Audit events={auditEvents} />}
          {page === "notifications" && <Notifications notifications={notifications} />}
          {page === "guide" && <Guide setPage={setPage} />}
        </div>
      </div>
    </div>
  );
}

