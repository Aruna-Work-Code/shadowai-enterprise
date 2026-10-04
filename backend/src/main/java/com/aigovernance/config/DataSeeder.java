package com.aigovernance.config;

import com.aigovernance.model.AiTool;
import com.aigovernance.model.IntegrationConnection;
import com.aigovernance.model.Policy;
import com.aigovernance.model.RequestEntity;
import com.aigovernance.model.Role;
import com.aigovernance.model.User;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.repository.IntegrationConnectionRepository;
import com.aigovernance.repository.PolicyRepository;
import com.aigovernance.repository.RequestRepository;
import com.aigovernance.repository.UserRepository;
import com.aigovernance.repository.TenantMembershipRepository;
import com.aigovernance.repository.TenantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds local development/demo data.
 *
 * IMPORTANT:
 * This class is intended for the local/demo environment.
 *
 * In a real production enterprise deployment, users, AI tools,
 * policies and integrations should normally come from controlled
 * administration workflows, enterprise identity systems and
 * governance configuration rather than hard-coded seed data.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(
            UserRepository users,
            AiToolRepository tools,
            PasswordEncoder encoder,
            IntegrationConnectionRepository integrations,
            TenantRepository tenants,
            TenantMembershipRepository memberships,
            PolicyRepository policies,
            RequestRepository requests
    ) {

        return args -> {
            var defaultTenant = tenants.findByKey("default")
                    .orElseGet(() -> {
                        var t = new com.aigovernance.model.Tenant();
                        t.name = "Default Organization";
                        t.key = "default";
                        return tenants.save(t);
                    });


            /*
             * ============================================================
             * 1. DEMO USERS
             * ============================================================
             *
             * These accounts allow us to test the complete governance
             * workflow locally:
             *
             * EMPLOYEE
             *      -> creates governance request
             *
             * IT_REVIEWER
             *      -> reviews/approves/rejects requests
             *
             * SECURITY_ANALYST
             *      -> security/governance investigation role
             *
             * GOVERNANCE_MANAGER
             *      -> governance administration
             *
             * ADMIN
             *      -> full local demo access
             *
             * Demo password for all accounts:
             *
             * Demo@123
             */
            if (users.count() == 0) {

                createUser(
                        users,
                        encoder,
                        "priya.employee",
                        Role.EMPLOYEE,
                        "ENGINEERING"
                );

                createUser(
                        users,
                        encoder,
                        "aruna.it",
                        Role.IT_REVIEWER,
                        "IT"
                );

                createUser(
                        users,
                        encoder,
                        "rahul.security",
                        Role.SECURITY_ANALYST,
                        "SECURITY"
                );

                createUser(
                        users,
                        encoder,
                        "neha.manager",
                        Role.GOVERNANCE_MANAGER,
                        "IT"
                );

                createUser(
                        users,
                        encoder,
                        "admin",
                        Role.ADMIN,
                        "IT"
                );
            }

            for (var user : users.findAll()) {
                if (memberships.findByTenantIdAndUserId(defaultTenant.id, user.id).isEmpty()) {
                    var m = new com.aigovernance.model.TenantMembership();
                    m.tenantId = defaultTenant.id;
                    m.userId = user.id;
                    m.role = user.role;
                    memberships.save(m);
                }
            }

            /*
             * ============================================================
             * 2. SYNTHETIC AI TOOL CATALOGUE
             * ============================================================
             *
             * These tools represent the approved/managed capabilities
             * that the recommendation engine can evaluate.
             *
             * The recommendation engine does NOT simply choose the
             * most generic approved tool.
             *
             * Instead:
             *
             *      User intent
             *          ↓
             *      Capability detection
             *          ↓
             *      Tool capability match
             *          ↓
             *      Approval check
             *          ↓
             *      Policy hard-check
             *          ↓
             *      Risk-aware ranking
             *          ↓
             *      Recommendation
             */

            if (tools.count() == 0) {

                /*
                 * --------------------------------------------------------
                 * GENERAL ENTERPRISE AI
                 * --------------------------------------------------------
                 *
                 * Useful for:
                 *
                 * - Writing
                 * - Research
                 * - Summarization
                 *
                 * Policy:
                 * INTERNAL only
                 */
                tool(
                        tools,
                        "Enterprise AI Assistant",
                        "General AI",
                        "writing,research,summarization",
                        "APPROVED",
                        "LOW",
                        "INTERNAL"
                );

                /*
                 * --------------------------------------------------------
                 * CODE ASSISTANT
                 * --------------------------------------------------------
                 *
                 * Used for:
                 *
                 * - Java
                 * - REST APIs
                 * - Debugging
                 * - Unit testing
                 * - API development
                 * - Software development
                 */
                tool(
                        tools,
                        "Code Assistant",
                        "Developer AI",
                        "code,debugging,testing,java,api,rest,unit tests",
                        "APPROVED",
                        "LOW",
                        "INTERNAL"
                );

                /*
                 * --------------------------------------------------------
                 * DATA ANALYSIS ASSISTANT
                 * --------------------------------------------------------
                 *
                 * This capability was added specifically so the platform
                 * has a dedicated approved analytical capability.
                 *
                 * Example request:
                 *
                 * "I need help analyzing an internal Excel dataset
                 *  and identifying trends."
                 *
                 * Expected recommendation:
                 *
                 * Data Analysis Assistant
                 */
                tool(
                        tools,
                        "Data Analysis Assistant",
                        "Analytics AI",
                        "data,analysis,analytics,excel,spreadsheet,trends",
                        "APPROVED",
                        "LOW",
                        "INTERNAL"
                );

                /*
                 * --------------------------------------------------------
                 * RESEARCH ASSISTANT
                 * --------------------------------------------------------
                 *
                 * Used for research and source-oriented tasks.
                 *
                 * IMPORTANT:
                 *
                 * This tool explicitly prohibits CUSTOMER_DATA.
                 *
                 * Therefore:
                 *
                 * CUSTOMER_DATA
                 *       +
                 * Research Assistant
                 *       =
                 * HARD POLICY FAILURE
                 */
                tool(
                        tools,
                        "Research Assistant",
                        "Research AI",
                        "research,sources,analysis",
                        "APPROVED",
                        "MEDIUM",
                        "PROHIBITED:CUSTOMER_DATA"
                );

                /*
                 * --------------------------------------------------------
                 * PUBLIC AI TOOL
                 * --------------------------------------------------------
                 *
                 * This is deliberately UNAPPROVED.
                 *
                 * It exists so that we can test:
                 *
                 * TC-E04 — Unapproved tool
                 *
                 * The recommendation engine must NEVER recommend this
                 * tool as an approved alternative.
                 */
                tool(
                        tools,
                        "Public AI Tool",
                        "General AI",
                        "writing,research",
                        "UNAPPROVED",
                        "HIGH",
                        "PROHIBITED:CUSTOMER_DATA"
                );
            }

            /*
             * ============================================================
             * 3. GOVERNANCE POLICY CATALOGUE
             * ============================================================
             *
             * These policies make the demo self-explanatory. They are
             * deterministic rules consumed by the decision engine and
             * visible in the Governance Policies page.
             */
            if (policies.count() == 0) {
                policy(
                        policies,
                        "Internal Data — Approved AI Only",
                        "INTERNAL",
                        "Enterprise AI Assistant,Code Assistant,Data Analysis Assistant,Research Assistant",
                        "Public AI Tool",
                        true,
                        "LOW",
                        true,
                        30,
                        true,
                        "Internal information may use approved enterprise AI capabilities. Unapproved public tools require governance review."
                );

                policy(
                        policies,
                        "Customer Data — Human Review",
                        "CUSTOMER_DATA",
                        "",
                        "Public AI Tool,Research Assistant",
                        true,
                        "HIGH",
                        true,
                        7,
                        true,
                        "Customer data must not be sent to unapproved or prohibited AI capabilities. Exceptions require documented human approval."
                );

                policy(
                        policies,
                        "Public Data — Low Risk AI",
                        "PUBLIC",
                        "Enterprise AI Assistant,Code Assistant,Data Analysis Assistant,Research Assistant",
                        "",
                        false,
                        "LOW",
                        true,
                        30,
                        true,
                        "Public information may use approved AI capabilities without a temporary exception when no policy restriction applies."
                );
            }

            /*
             * ============================================================
             * 4. DEMO GOVERNANCE REQUESTS
             * ============================================================
             *
             * A small amount of synthetic data makes a fresh deployment
             * immediately demoable. It is created only for an empty DB.
             */
            if (requests.count() == 0) {
                var employee = users.findByUsername("priya.employee").orElse(null);
                if (employee != null) {
                    RequestEntity recommended = new RequestEntity();
                    recommended.tenantId = defaultTenant.id;
                    recommended.employeeId = employee.id;
                    recommended.intent = "I need help debugging a Java REST API";
                    recommended.dataType = "INTERNAL";
                    recommended.department = "ENGINEERING";
                    recommended.frequency = "DAILY";
                    recommended.requestedTool = "";
                    recommended.recommendedTool = "Code Assistant";
                    recommended.recommendationScore = 100;
                    recommended.confidence = "HIGH";
                    recommended.status = "RECOMMENDED";
                    requests.save(recommended);

                    RequestEntity exceptionRequired = new RequestEntity();
                    exceptionRequired.tenantId = defaultTenant.id;
                    exceptionRequired.employeeId = employee.id;
                    exceptionRequired.intent = "I need to analyze customer complaints using an AI tool";
                    exceptionRequired.dataType = "CUSTOMER_DATA";
                    exceptionRequired.department = "SUPPORT";
                    exceptionRequired.frequency = "DAILY";
                    exceptionRequired.requestedTool = "";
                    exceptionRequired.status = "EXCEPTION_REQUIRED";
                    requests.save(exceptionRequired);
                }
            }

            /*
             * ============================================================
             * 5. LOCAL SYNTHETIC WEBHOOK INTEGRATION
             * ============================================================
             *
             * Used for Phase 7+ investigation testing.
             *
             * This allows us to simulate external governance signals
             * without requiring a real enterprise SIEM/security platform.
             */
            if (integrations.count() == 0) {

                IntegrationConnection connection =
                        new IntegrationConnection();

                connection.name =
                        "Local Synthetic Governance Feed";

                connection.provider =
                        "GENERIC_WEBHOOK";

                /*
                 * Local demo secret.
                 *
                 * It is hashed before storage.
                 */
                connection.webhookSecretHash =
                        encoder.encode("phase7-demo-secret");

                connection.enabled = true;

                integrations.save(connection);
            }
        };
    }

    /**
     * Creates a local demo user.
     */
    private void createUser(
            UserRepository repository,
            PasswordEncoder encoder,
            String username,
            Role role,
            String department
    ) {

        User user = new User();

        user.username = username;

        /*
         * Demo password:
         *
         * Demo@123
         *
         * Store only the encoded value.
         */
        user.passwordHash =
                encoder.encode("Demo@123");

        user.role = role;

        user.department = department;

        repository.save(user);
    }

    private void policy(
            PolicyRepository repository,
            String name,
            String dataType,
            String allowedTools,
            String prohibitedTools,
            boolean approvalRequired,
            String riskLevel,
            boolean exceptionAllowed,
            Integer maxDurationDays,
            boolean enabled,
            String description
    ) {
        Policy p = new Policy();
        p.name = name;
        p.tenantId = 1L;
        p.dataType = dataType;
        p.allowedTools = allowedTools;
        p.prohibitedTools = prohibitedTools;
        p.approvalRequired = approvalRequired;
        p.riskLevel = riskLevel;
        p.exceptionAllowed = exceptionAllowed;
        p.maxDurationDays = maxDurationDays;
        p.enabled = enabled;
        p.description = description;
        repository.save(p);
    }

    /**
     * Creates an AI tool in the synthetic governance catalogue.
     *
     * @param repository      AI tool repository
     * @param name            Display name
     * @param category        Tool category
     * @param capabilities    Comma-separated capability keywords
     * @param approvalStatus  APPROVED / UNAPPROVED
     * @param riskLevel       LOW / MEDIUM / HIGH
     * @param policy          Data governance policy
     */
    private void tool(
            AiToolRepository repository,
            String name,
            String category,
            String capabilities,
            String approvalStatus,
            String riskLevel,
            String policy
    ) {

        AiTool tool = new AiTool();

        tool.name = name;

        tool.category = category;

        tool.capabilities = capabilities;

        tool.approvalStatus = approvalStatus;

        tool.riskLevel = riskLevel;

        tool.dataPolicy = policy;

        repository.save(tool);
    }
}