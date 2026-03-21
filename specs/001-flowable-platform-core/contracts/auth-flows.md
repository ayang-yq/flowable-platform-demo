# Authentication Flow Contracts

**Version": 1.0.0
**Purpose**: Complete authentication and authorization flow definitions for OAuth2/OIDC and local authentication

---

## Authentication Architecture

### Supported Authentication Methods

1. **OAuth2 / OpenID Connect**: Azure AD, Okta, Google Workspace (tenant-configurable)
2. **Local Username/Password**: Fallback authentication for simple deployments

### Security Features

- JWT-based session management
- Multi-tenant authentication isolation
- Role-based access control (RBAC)
- CSRF protection for state-changing operations
- Secure token storage (httpOnly cookies)

---

## OAuth2 / OpenID Connect Flow

### Flow Diagram

```
User
  │
  ├─ 1. Click "Login with Azure AD"
  │
  ↓
Frontend (Next.js)
  │
  ├─ 2. Redirect to /oauth2/authorization/azure
  │
  ↓
Backend (Spring Boot)
  │
  ├─ 3. Generate state parameter and nonce
  ├─ 4. Store in session (Redis)
  ├─ 5. Redirect to Azure AD authorization endpoint
  │      https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/authorize
  │      ?client_id={clientId}
  │      &response_type=code
  │      &redirect_uri={callbackUrl}
  │      &scope=openid profile email
  │      &state={state}
  │      &nonce={nonce}
  │
  ↓
Azure AD
  │
  ├─ 6. User authenticates and consents
  ├─ 7. Redirect back with authorization code
  │      {callbackUrl}?code={code}&state={state}
  │
  ↓
Backend (Spring Boot)
  │
  ├─ 8. Validate state parameter
  ├─ 9. Exchange authorization code for tokens
  ├─ 10. POST https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token
  │         ?grant_type=authorization_code
  │         &code={code}
  │         &redirect_uri={callbackUrl}
  │         &client_id={clientId}
  │         &client_secret={clientSecret}
  │
  ├─ 11. Receive token response
  │         {
  │           "access_token": "...",
  │           "id_token": "...",
  │           "refresh_token": "...",
  │           "expires_in": 3600
  │         }
  │
  ├─ 12. Validate ID token signature
  ├─ 13. Extract user info from ID token
  │         {
  │           "sub": "user-oid",
  │           "name": "John Doe",
  │           "email": "john@acme.com",
  │           "oid": "...",
  │           "tid": "tenant-identifier"
  │         }
  │
  ├─ 14. Query or create User entity in tenant context
  ├─ 15. Assign default role (USER)
  ├─ 16. Create JWT token for internal session
  ├─ 17. Generate httpOnly cookie with JWT
  ├─ 18. Log authentication event (AuditLog)
  │
  └─ 19. Redirect to frontend with JWT in cookie
           {frontendUrl}/login/callback?token={jwt}
```

### Token Response (from IdP)

```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "0.ARoA6W...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "openid profile email"
}
```

### JWT Structure (Internal)

```json
{
  "sub": "user-uuid",
  "username": "johndoe",
  "email": "john@acme.com",
  "roles": ["USER", "MANAGER"],
  "tenantId": "tenant-uuid",
  "tenantCode": "acme",
  "exp": 1711068800,
  "iat": 1711065200
}
```

---

## Local Authentication Flow

### Login Request

```
User
  │
  ├─ 1. Enter username and password
  │
  ↓
Frontend (Next.js)
  │
  ├─ 2. POST /auth/login
  │      {
  │        "username": "johndoe",
  │        "password": "securePassword123"
  │      }
  │
  ↓
Backend (Spring Boot)
  │
  ├─ 3. Validate request (username, password provided)
  ├─ 4. Query User by username and tenantId
  ├─ 5. Verify password hash (bcrypt)
  ├─ 6. Check user is_active = true
  ├─ 7. Load user roles and permissions
  ├─ 8. Create JWT token
  ├─ 9. Log authentication event (AuditLog)
  │
  └─ 10. Return JWT token
           {
             "success": true,
             "data": {
               "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
               "user": { /* user object */ }
             }
           }
```

### Password Hashing

**Algorithm**: bcrypt
**Work Factor**: 10 rounds (default)
**Migration**: Auto-upgrade work factor on successful login

```java
String hashedPassword = passwordEncoder.encode(plainPassword);
boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
```

---

## Session Management

### JWT Configuration

**Algorithm**: HS256 (HMAC-SHA256)
**Secret Key**: 256-bit random key (per environment)
**Expiration**: 1 hour (configurable)
**Refresh**: Token refresh endpoint available

### Token Storage

**Frontend**: httpOnly cookie (JavaScript inaccessible)
```typescript
// Cookie set by backend, automatically sent with requests
Set-Cookie: token=eyJhbGc...; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=3600
```

**Backend**: No server-side session store (stateless JWT)

---

## Token Refresh Flow

```
Frontend
  │
  ├─ 1. Detect token about to expire (< 5 minutes remaining)
  │
  └─ 2. POST /auth/refresh
           Header: Authorization: Bearer {current_token}
Backend
  │
  ├─ 3. Validate current token (not expired)
  ├─ 4. Check user still exists and is_active = true
  ├─ 5. Generate new JWT token
  ├─ 6. Return new token in httpOnly cookie
  │
  └─ 7. Response: { "success": true, "token": "new-jwt-token" }
```

---

## Logout Flow

```
User
  │
  ├─ 1. Click "Logout"
  │
  ↓
Frontend
  │
  ├─ 2. POST /auth/logout
  │      Header: Authorization: Bearer {jwt}
  │
  ↓
Backend
  │
  ├─ 3. Validate token
  ├─ 4. Extract user info from token
  ├─ 5. Log logout event (AuditLog)
  ├─ 6. Clear httpOnly cookie
  │      Set-Cookie: token=; Path=/; HttpOnly; Secure; Max-Age=0
  │
  └─ 7. Return success response
Frontend
  │
  └─ 8. Redirect to login page
```

---

## Multi-Tenant Authentication

### Tenant Identification

**Method 1**: Subdomain
```
acme.platform.com  → Tenant "acme"
xyz.platform.com   → Tenant "xyz"
```

**Method 2**: Header (Current Approach)
```
GET /api/tasks
Headers:
  X-Tenant-Id: tenant-uuid
  Authorization: Bearer {jwt}
```

**Method 3**: User Context
```
JWT contains tenantId claim
Backend validates user belongs to tenant
```

### Tenant-Specific OAuth2 Configuration

**Per-Tenant Provider Storage**:
```java
@Entity
public class Tenant {
    // ... other fields

    @Column(name = "oauth_provider")
    private String oauthProvider;  // "azure", "google", "okta"

    @Column(name = "oauth_client_id")
    private String oauthClientId;

    @Column(name = "oauth_client_secret")
    private String oauthClientSecret;  // Encrypted at rest

    @Column(name = "oauth_tenant_id")
    private String oauthTenantId;  // Azure AD tenant ID
}
```

**Dynamic Client Registration**:
```java
@Service
public class OAuth2ClientRegistrationService {

    public void registerClientForTenant(Tenant tenant) {
        ClientRegistration.Builder registration =
            ClientRegistration.withRegistrationId(tenant.getCode())
                .clientId(tenant.getOauthClientId())
                .clientSecret(tenant.getOauthClientSecret())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)
                .authorizationUri(providerConfig.getAuthorizationUri())
                .tokenUri(providerConfig.getTokenUri())
                .userInfoUri(providerConfig.getUserInfoUri())
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Flowable Platform")
                .build();

        clientRegistrationRepository.save(registration);
    }
}
```

---

## Role-Based Access Control (RBAC)

### Role Resolution

```java
@Entity
public class User {
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

@Service
public class AuthorizationService {

    public boolean hasPermission(String permission, User user) {
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(perm -> perm.getCode().equals(permission));
    }
}
```

### Permission Check Flow

```
Request arrives with JWT
  ↓
Extract user info from JWT (userId, tenantId, roles)
  ↓
Load User entity with roles and permissions
  ↓
Check if required permission present in user's permissions
  ↓
If YES: Allow request
If NO: Return 403 Forbidden
```

---

## Security Considerations

### JWT Token Validation

1. **Signature Verification**: Verify JWT signature with secret key
2. **Expiration Check**: Reject expired tokens (exp claim)
3. **Issuer Verification**: Validate iss claim matches expected issuer
4. **Tenant Validation**: Ensure tenantId claim matches X-Tenant-Id header
5. **User Validation**: Verify user exists and is_active = true

### CSRF Protection

**State Parameter** (OAuth2):
- Random string generated before authorization redirect
- Stored in session
- Validated on callback to prevent CSRF attacks

**SameSite Cookies**:
- httpOnly cookies set with `SameSite=Strict`
- Prevents CSRF attacks from cross-site requests

### Password Security

**Requirements**:
- Minimum 8 characters
- Contains uppercase, lowercase, number, special character
- Hashed using bcrypt (never plain text)
- Password change requires current password verification

**Password Reset Flow**:
```
User clicks "Forgot Password"
  ↓
Enter email address
  ↓
Backend generates reset token
  ↓
Backend sends email with reset link
  ↓
User clicks link (reset token in URL)
  ↓
Frontend shows password reset form
  ↓
User submits new password
  ↓
Backend validates reset token
  ↓
Backend hashes new password and updates User
  ↓
Backend invalidates reset token
```

---

## Authentication Endpoints Summary

### OAuth2 Endpoints

- `GET /oauth2/authorization/{provider}`: Initiate OAuth2 flow
- `GET /login/oauth2/code/{provider}`: OAuth2 callback handler
- `POST /auth/refresh`: Refresh JWT token

### Local Auth Endpoints

- `POST /auth/login`: Username/password login
- `POST /auth/logout`: Terminate session
- `POST /auth/register`: User self-registration (optional)
- `POST /auth/forgot-password`: Initiate password reset
- `POST /auth/reset-password`: Complete password reset

---

**Authentication flows complete and ready for implementation**.
