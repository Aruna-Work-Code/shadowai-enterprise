import { useEffect } from "react";
export const reduced = () => matchMedia("(prefers-reduced-motion: reduce)").matches;
export function useGlow() { useEffect(() => { if (matchMedia("(pointer:coarse)").matches) return; const f = e => { document.documentElement.style.setProperty("--mx", e.clientX + "px"); document.documentElement.style.setProperty("--my", e.clientY + "px"); }; addEventListener("pointermove", f); return () => removeEventListener("pointermove", f); }, []); }
