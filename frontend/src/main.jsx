import { Component, useState, useEffect, lazy, Suspense } from "react";
import { createRoot } from "react-dom/client";
import "./base.css";
import "./styles.css";
import "./polish-pub.css";
import Landing from "./Landing.jsx";
import Entry from "./Entry.jsx";
const Workspace = lazy(() => import("./Workspace.jsx"));
/* Prevents a blank screen if a render error ever occurs: shows a calm recovery card instead. */
class Boundary extends Component {
  constructor(p) { super(p); this.state = { e: false }; }
  static getDerivedStateFromError() { return { e: true }; }
  componentDidCatch(err) { console.error(err); }
  render() {
    if (!this.state.e) return this.props.children;
    return <div className="boot" role="alert" style={{ textAlign: "center", padding: 24 }}><div><h2 style={{ margin: "0 0 8px", color: "#e6ecf8" }}>Something went wrong</h2><p style={{ margin: "0 0 16px" }}>The page hit an unexpected problem. Your session is safe.</p><button onClick={() => { location.hash = "#/"; location.reload(); }} style={{ padding: "10px 18px", borderRadius: 10, border: 0, background: "#4f8cff", color: "#fff", font: "inherit", cursor: "pointer" }}>Reload</button></div></div>;
  }
}
const hasSession = () => !!localStorage.getItem("token") || !!sessionStorage.getItem("sa_demo");
function Root() {
  const [theme, setThemeState] = useState(() => localStorage.getItem("sa_theme") || "light"), setTheme = t => { localStorage.setItem("sa_theme", t); setThemeState(t); };
  const [h, setH] = useState(location.hash || "#/"), [authed, setAuthed] = useState(hasSession());
  useEffect(() => { const f = () => setH(location.hash || "#/"); addEventListener("hashchange", f); return () => removeEventListener("hashchange", f); }, []);
  if (!h.startsWith("#/app")) return <div className="pub"><Landing /></div>;
  if (authed) return <div className="ws" data-theme={theme}><Suspense fallback={<div className="boot">Loading workspace&hellip;</div>}><Workspace onExit={() => setAuthed(false)} theme={theme} setTheme={setTheme} /></Suspense></div>;
  return <div className="pub"><Entry onAuth={() => setAuthed(true)} onDemo={() => { sessionStorage.setItem("sa_demo", "1"); setAuthed(true); }} /></div>;
}
createRoot(document.getElementById("root")).render(<Boundary><Root /></Boundary>);
