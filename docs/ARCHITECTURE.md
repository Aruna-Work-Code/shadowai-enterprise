# ShadowAI — Architecture

ShadowAI is an enterprise-oriented AI Governance Decision Assistant designed around two connected product capabilities:

1. **Safe Alternative Recommendation** — preventive governance.
2. **Governance Resolution Intelligence** — corrective governance.

The core architectural principle is:

> **Policy and governance rules remain authoritative. Automation assists classification, recommendation, workflow, and evidence collection, while consequential decisions remain human-controlled.**

---

## High-Level Architecture

```text
                         SHADOWAI
                AI Governance Decision Assistant
                              |
              +---------------+---------------+
              |                               |
              v                               v
     PREVENTIVE GOVERNANCE           CORRECTIVE GOVERNANCE
              |                               |
              v                               v
   Safe Alternative                 Governance Resolution
   Recommendation                  Intelligence
              |                               |
              +---------------+---------------+
                              |
                              v
                    GOVERNANCE ENGINE
                              |
             +----------------+----------------+
             |                |                |
             v                v                v
          Policies        Workflow        AI Tool Catalog
             |                |                |
             +----------------+----------------+
                              |
                              v
                    HUMAN GOVERNANCE DECISION
                              |
              +---------------+---------------+
              |               |               |
              v               v               v
            Audit       Notifications      Analytics





+-----------------------------------------------------------+
|                     React + Vite                          |
|                                                           |
| Dashboard | Requests | AI Tools | Policies                |
| Investigations | Exceptions | Integrations                |
| Notifications | Audit | Live Activity                     |
+----------------------------+------------------------------+
                             |
                         REST / SSE
                             |
                             v
+-----------------------------------------------------------+
|                  Spring Boot Backend                      |
|                                                           |
| Controllers / REST APIs                                   |
|                                                           |
| Security / JWT / RBAC / Tenant Context                    |
|                                                           |
| Governance Services                                       |
|                                                           |
| Policy + Recommendation + Workflow                        |
|                                                           |
| Audit + Notifications + Analytics + Integrations          |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                       PostgreSQL                          |
|                                                           |
| Users | Tenants | Policies | Requests | AI Tools          |
| Exceptions | Investigations | Integrations                |
| Integration Events | Notifications | Audit                |
+-----------------------------------------------------------+

