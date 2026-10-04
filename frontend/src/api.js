const API = import.meta.env.VITE_API_URL || "";
export async function login(username, password) {
  const r = await fetch(API + "/api/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username, password }) });
  if (!r.ok) throw new Error("Invalid credentials");
  const d = await r.json();
  if (!d.token) throw new Error("Login failed");
  localStorage.setItem("token", d.token); localStorage.setItem("refreshToken", d.refreshToken || ""); localStorage.setItem("username", d.username || username); localStorage.setItem("role", d.role || "");
  sessionStorage.removeItem("sa_demo");
  return d;
}
export async function health() {
  try {
    const c = new AbortController(); const t = setTimeout(() => c.abort(), 8000);
    const r = await fetch(API + "/actuator/health", { cache: "no-store", signal: c.signal }); clearTimeout(t);
    return r.ok && (await r.json()).status === "UP";
  } catch { return false; }
}
