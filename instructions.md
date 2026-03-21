# Instructions - Flowable Platform Development Protocol

## 🔒 MANDATORY: Audit Logging Protocol

**CRITICAL REQUIREMENT**: Before executing ANY user request, I MUST:

1. **Log the original user request** to `dev-log.md` with:
   - Exact timestamp (YYYY-MM-DD HH:MM:SS UTC)
   - Original user prompt (in Chinese/English as provided)
   - Request ID/Session context if applicable

2. **Log BEFORE execution** - The audit entry must be created BEFORE I start working on the request

3. **Chronological ordering** - New entries go at the TOP of dev-log.md (reverse chronological order)

4. **Complete the entry** with:
   - Actions performed
   - Files modified/created
   - Outcome summary

### Template for Audit Entries:

```markdown
## [YYYY-MM-DD] - [Brief Title]

**Timestamp**: [YYYY-MM-DD HH:MM:SS] UTC
**User Request**:
> [Original user prompt - preserve exact language]

**Actions Performed**:
- [Action 1]
- [Action 2]
- ...

**Files Modified**:
- [File 1]
- [File 2]
- ...

**Outcome**: [Result summary]
```

## 🤝 Collaboration Principles

### 1. Transparency First
- All user requests are logged before execution
- All decisions and reasoning are documented
- All file changes are tracked and summarized

### 2. Constitution Compliance
- Every operation must comply with `.specify/memory/constitution.md`
- Constitutional violations require explicit user approval and documentation
- When in doubt, reference the constitution before proceeding

### 3. Progressive Development
- Follow Spec Kit workflow: spec → plan → tasks → implementation
- Test-driven development (TDD) is mandatory for feature implementation
- Independent user story validation at each phase

### 4. Multi-Tenant Discipline
- Every database query must include tenant_id filtering
- Every cache key must include tenant identifier
- Every Flowable operation must respect tenant boundaries
- Cross-tenant data access is strictly prohibited

## 📋 Workflow Protocol

### Before Starting Any Task:

1. **LOG THE REQUEST** to `dev-log.md` (MANDATORY - FIRST STEP)
2. Check if task aligns with constitution principles
3. Identify relevant Spec Kit commands/skills
4. Clarify ambiguities with user before proceeding
5. Break down complex tasks into phases

### During Execution:

1. Update dev-log.md with actions performed
2. Reference applicable constitution principles
3. Use appropriate tools (Read, Edit, Write, etc.)
4. Maintain clear communication with user
5. Document any deviations from original plan

### After Completion:

1. Finalize dev-log.md entry with outcome
2. Summarize files modified/created
3. Update project statistics if applicable
4. Provide clear next steps or validation checkpoints
5. **Execute auto-commit** (MANDATORY - see Git Automation Rules):
   - Run pre-commit validation checks
   - Stage relevant files
   - Commit with standardized message format
   - Update dev-log.md with commit reference

## 🚨 Decision Recording

### When User Approves Non-Standard Approaches:

If user explicitly approves something that deviates from constitution or best practices:

1. Log the original concern
2. Document user's explicit approval
3. Record justification in dev-log.md
4. Note any architectural debt incurred

Example:
```markdown
**Constitutional Concern**: [Explain the concern]
**User Approval**: User explicitly approved [approach]
**Justification**: [User's reasoning]
**Technical Debt**: [Any ongoing maintenance concerns]
```

### When Requirements Are Ambiguous:

1. Stop and ask clarifying questions
2. Document assumptions made
3. Propose options with trade-offs
4. Await user confirmation before proceeding

## 📊 Quality Assurance

### Code Quality Gates:
- [ ] Constitution compliance verified
- [ ] Spec Kit process followed (if applicable)
- [ ] Tests written/updated (TDD compliance)
- [ ] Multi-tenant isolation maintained
- [ ] Audit log complete

### Review Checklist Before Claiming Completion:
- [ ] User request fully addressed
- [ ] dev-log.md entry complete
- [ ] All files listed in dev-log.md
- [ ] No placeholders or TODOs remaining
- [ ] Clear outcome summary provided

## 🔄 Git Automation Rules

### Auto-Commit Protocol
**MANDATORY**: The AI agent is authorized and required to commit changes upon the successful completion of a task.

### Commit Message Standard
Follow the pattern: `type(scope): description`

**Types**:
- `feat`: New feature or functionality
- `fix`: Bug fix or error correction
- `docs`: Documentation changes only
- `refactor`: Code refactoring without behavior change
- `test`: Test additions or modifications
- `chore`: Build process, tooling, or dependency updates
- `perf`: Performance improvements
- `style`: Code style changes (formatting, etc.)

**Scope Examples**:
- `workflow`: Process engine and workflow management
- `tasks`: Task center functionality
- `forms`: Form engine and schema management
- `rbac`: Identity and access control
- `audit`: Logging and compliance features
- `analytics`: Dashboards and reporting
- `admin`: Administrative console
- `infra`: Infrastructure and configuration

**Message Format**:
```
type(scope): description

Example: feat(workflow): implement task rejection logic (Phase 2.3)
```

**Detailed Commit Message Template** (for complex changes):
```
type(scope): brief description (Phase X.Y)

- Detailed change 1
- Detailed change 2
- Detailed change 3

Refs: #issue-number or related-spec
```

### Pre-Commit Requirements
**CRITICAL**: Before committing, always run validation checks:

1. **Dry-run build or lint check**:
   - For backend: Run `mvn clean compile` or equivalent dry-run
   - For frontend: Run `npm run lint` or type checking
   - For docs: Validate markdown syntax

2. **Verify git status**:
   - Review staged files with `git status`
   - Ensure only intended files are included
   - Check for unintended files (secrets, temp files)

3. **Stability check**:
   - Ensure the main branch remains stable
   - No breaking changes without explicit user approval
   - Tests pass (if applicable)

### Commit Workflow
After successful task completion:

1. **Finalize dev-log.md entry** (mandatory first step)
2. **Run pre-commit checks** (lint, validation)
3. **Stage relevant files**:
   ```bash
   git add <files-changed>
   ```
4. **Create commit with standardized message**:
   ```bash
   git commit -m "type(scope): description (Phase X.Y)"
   ```
5. **Update dev-log.md** with commit hash reference

### Commit Examples
```bash
# Feature implementation
git commit -m "feat(workflow): implement task delegation with audit trail (Phase 2.1)"

# Bug fix
git commit -m "fix(tasks): resolve race condition in concurrent task assignment"

# Documentation
git commit -m "docs(api): update authentication endpoints documentation"

# Refactoring
git commit -m "refactor(forms): extract form validation logic to shared service"

# Test addition
git commit -m "test(rbac): add multi-tenant isolation tests for user management"
```

### Branch Strategy
- **Feature branches**: `###-feature-name` (created by Spec Kit)
- **Main branch**: `main` or `master` (primary integration branch)
- **Commit frequency**: After each completed task or logical group of related changes

### Emergency Protocol
If pre-commit checks fail:
1. **DO NOT COMMIT** - address the issues first
2. **Log the failure** in dev-log.md
3. **Inform user** of the validation failure
4. **Fix issues** and re-run validation
5. **Proceed with commit** only after validation passes

## 🎯 Communication Style

### DO:
- ✅ Use professional, precise language
- ✅ Reference constitution principles by name
- ✅ Provide file paths with line numbers
- ✅ Explain architectural decisions clearly
- ✅ Use Chinese for user-facing responses (unless user prefers English)
- ✅ Log everything before execution

### DON'T:
- ❌ Proceed without logging the request first
- ❌ Skip constitutional compliance checks
- ❌ Make assumptions without clarification
- ❌ Use vague language in technical discussions
- ❌ Leave audit entries incomplete

## 🔧 Tool Usage Guidelines

### Preferred Tool Order:
1. **Specialized tools first**: Read (files), Glob (search), Grep (content search)
2. **Edit existing files**: Edit tool (preserves structure)
3. **Create new files**: Write tool (only when necessary)
4. **System operations**: Bash tool (for commands only)

### Tool Selection Rules:
- Use Read instead of `cat`
- Use Edit instead of `sed`/`awk`
- Use Write instead of `echo` redirection
- Use Glob instead of `find`
- Use Grep instead of `grep`/`rg`

## 📝 Documentation Standards

### Code Documentation:
- Complex business logic requires comments
- All Flowable process definitions need XML comments
- API endpoints must document multi-tenant behavior
- Form schemas must include version metadata

### Project Documentation:
- dev-log.md: Append-only audit trail
- instructions.md: This file - collaboration protocol
- constitution.md: `.specify/memory/constitution.md` - technical governance
- architecture.md: `architecture.md` - system design reference

## 🚨 Emergency Protocols

### If User Requests Potentially Destructive Actions:
1. Pause and explain risks clearly
2. Request explicit confirmation
3. Suggest safer alternatives if available
4. Log the decision in dev-log.md

### If Constitution Must Be Violated:
1. Identify specific principle(s) affected
2. Explain why violation is necessary
3. Request explicit user approval
4. Document technical debt incurred
5. Plan remediation timeline

### If Mistake Occurs:
1. Acknowledge immediately
2. Log the error in dev-log.md
3. Propose remediation steps
4. Execute fix with user approval
5. Add prevention measures to instructions.md

---

## 📌 Quick Reference

### First Step for ANY Request:
```bash
# Add entry to dev-log.md (TOP OF FILE - reverse chronological)
## [YYYY-MM-DD] - [Brief Title]
**Timestamp**: [YYYY-MM-DD HH:MM:SS] UTC
**User Request**:
> [Copy exact user prompt]
```

### Constitution Principles Summary:
1. Multi-Tenant Architecture First
2. Flowable-Native Integration
3. Frontend-Backend Separation
4. Form-Process Binding
5. Test-Driven Development
6. Observability & Audit Trail

### Critical Constraints:
- PostgreSQL 15+, Spring Boot 3.5.x, Flowable 7.x, Java 21, Next.js
- No mocking Flowable core services
- Server Components for data fetching only
- tenant_id in all queries
- <500ms p95 response time

### Git Commit Quick Reference:
```bash
# After task completion, always commit:
git add <changed-files>
git commit -m "type(scope): description (Phase X.Y)"

# Examples:
git commit -m "feat(workflow): implement task delegation (Phase 2.1)"
git commit -m "fix(tasks): resolve concurrent assignment race condition"
git commit -m "docs(spec): clarify multi-tenant requirements"
```

### Commit Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build/tooling
- `perf`: Performance
- `style`: Code style

---

**Version**: 1.1.0
**Last Updated**: 2026-03-21
**Compliance**: MANDATORY for all development activities