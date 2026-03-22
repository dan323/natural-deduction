# REST API Reference

This document describes the REST API endpoints for the Natural Deduction system.

## Base URL

```
http://localhost:8080/api
```

## General Conventions

- **Request Format**: JSON
- **Response Format**: JSON
- **Error Status Codes**: 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)
- **Success Status Codes**: 200 (OK), 201 (Created)

## Classical Logic Endpoints

Base path: `/api/classical`

### Parse Formula

Parses a formula string into internal representation.

**Endpoint**: `POST /parse`

**Request**:
```json
{
  "formula": "p & q"
}
```

**Response** (200 OK):
```json
{
  "formula": "p & q",
  "parsed": true,
  "terms": [
    {
      "type": "AND",
      "left": { "type": "ATOM", "value": "p" },
      "right": { "type": "ATOM", "value": "q" }
    }
  ]
}
```

**Response** (400 Bad Request):
```json
{
  "error": "Invalid formula syntax",
  "message": "Unexpected token at position 5"
}
```

### Get Available Rules

Returns rules applicable to the current proof state.

**Endpoint**: `POST /rules`

**Request**:
```json
{
  "proofState": "...",
  "currentGoal": "p | q"
}
```

**Response** (200 OK):
```json
{
  "applicableRules": [
    {
      "id": "or-intro-left",
      "name": "∨-Introduction (Left)",
      "description": "From A, derive A ∨ B",
      "parameters": [
        {
          "name": "premise",
          "type": "formula"
        }
      ]
    },
    {
      "id": "or-intro-right",
      "name": "∨-Introduction (Right)",
      "description": "From B, derive A ∨ B",
      "parameters": [
        {
          "name": "premise",
          "type": "formula"
        }
      ]
    }
  ]
}
```

### Apply Rule

Applies a deduction rule to advance the proof.

**Endpoint**: `POST /apply-rule`

**Request**:
```json
{
  "ruleId": "or-intro-left",
  "proof": {
    "premises": ["p"],
    "goal": "p | q",
    "steps": []
  },
  "parameters": {
    "premise": "p"
  }
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "proof": {
    "premises": ["p"],
    "goal": "p | q",
    "steps": [
      {
        "ruleApplied": "∨-Introduction (Left)",
        "result": "p | q",
        "justification": "From p, derive p | q"
      }
    ],
    "complete": true,
    "valid": true
  }
}
```

**Response** (400 Bad Request):
```json
{
  "success": false,
  "error": "Rule cannot be applied",
  "message": "Required premise not found in proof"
}
```

### Verify Complete Proof

Verifies that a complete proof is valid.

**Endpoint**: `POST /verify`

**Request**:
```json
{
  "premises": ["A | B", "~A"],
  "goal": "B",
  "proof": [
    {
      "step": 1,
      "formula": "A | B",
      "justification": "Premise"
    },
    {
      "step": 2,
      "formula": "~A",
      "justification": "Premise"
    },
    {
      "step": 3,
      "formula": "B",
      "justification": "∨-Elimination on 1, assume A→(A,B), assume B→(B,B)"
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "valid": true,
  "message": "Proof is valid",
  "proofSteps": 3,
  "rulesUsed": ["∨-Elimination"]
}
```

**Response** (200 OK - Invalid):
```json
{
  "valid": false,
  "message": "Proof is invalid",
  "errors": [
    {
      "step": 3,
      "error": "Formula does not follow from justification"
    }
  ]
}
```

### Get Proof State

Retrieves the current state of an ongoing proof.

**Endpoint**: `POST /proof-state`

**Request**:
```json
{
  "proofId": "proof-12345"
}
```

**Response** (200 OK):
```json
{
  "proofId": "proof-12345",
  "premises": ["p -> q", "p"],
  "goal": "q",
  "currentSteps": [
    {
      "stepNumber": 1,
      "formula": "p -> q",
      "justification": "Premise"
    },
    {
      "stepNumber": 2,
      "formula": "p",
      "justification": "Premise"
    }
  ],
  "remainingGoals": ["q"],
  "isComplete": false,
  "availableRules": ["→-Elimination"]
}
```

## Modal Logic Endpoints

Base path: `/api/modal`

All endpoints have the same structure as classical logic, but support modal operators:

- `□` (Box/Necessity)
- `◇` (Diamond/Possibility)
- World labels: `w0:`, `s1:`, etc.

### Parse Modal Formula

**Endpoint**: `POST /parse`

**Request**:
```json
{
  "formula": "[]p -> <>(p | q)"
}
```

**Response** (200 OK):
```json
{
  "formula": "[]p -> <>(p | q)",
  "parsed": true,
  "terms": [
    {
      "type": "IMPLIES",
      "left": {
        "type": "BOX",
        "inner": { "type": "ATOM", "value": "p" }
      },
      "right": {
        "type": "DIAMOND",
        "inner": {
          "type": "OR",
          "left": { "type": "ATOM", "value": "p" },
          "right": { "type": "ATOM", "value": "q" }
        }
      }
    }
  ]
}
```

### Apply Modal Rule

**Endpoint**: `POST /apply-rule`

**Request**:
```json
{
  "ruleId": "box-elimination",
  "proof": {
    "premises": ["[]p"],
    "goal": "p",
    "steps": []
  },
  "parameters": {
    "premise": "[]p",
    "world": "w0"
  }
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "proof": {
    "premises": ["[]p"],
    "goal": "p",
    "steps": [
      {
        "ruleApplied": "□-Elimination",
        "result": "p (at w0)",
        "justification": "From □p in world w0, derive p"
      }
    ],
    "complete": true,
    "valid": true
  }
}
```

### Get Modal Rules

**Endpoint**: `POST /rules`

Modal endpoints return additional rules beyond classical logic:

**Response** (200 OK):
```json
{
  "applicableRules": [
    {
      "id": "box-intro",
      "name": "□-Introduction",
      "description": "To prove □A, prove A in all accessible worlds"
    },
    {
      "id": "box-elim",
      "name": "□-Elimination",
      "description": "From □A, derive A"
    },
    {
      "id": "diamond-intro",
      "name": "◇-Introduction",
      "description": "From A in some world, derive ◇A"
    },
    {
      "id": "diamond-elim",
      "name": "◇-Elimination",
      "description": "From ◇A, if C from A in new world, then C"
    }
  ]
}
```

## System Endpoints

### Health Check

Check application status.

**Endpoint**: `GET /actuator/health`

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP"
    },
    "db": {
      "status": "UP"
    }
  }
}
```

### Application Info

Get application version and metadata.

**Endpoint**: `GET /actuator/info`

**Response** (200 OK):
```json
{
  "app": {
    "name": "Natural Deduction",
    "version": "0.1-SNAPSHOT",
    "description": "A system for natural deduction"
  }
}
```

### Metrics

Get application metrics (if enabled).

**Endpoint**: `GET /actuator/metrics`

**Response** (200 OK):
```json
{
  "names": [
    "jvm.memory.used",
    "http.server.requests",
    "logback.events"
  ]
}
```

## Error Responses

All error responses follow this format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "path": "/api/classical/parse"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_FORMULA` | 400 | Formula syntax is invalid |
| `RULE_NOT_APPLICABLE` | 400 | Requested rule cannot be applied |
| `INVALID_PROOF` | 400 | Proof structure is invalid |
| `NOT_FOUND` | 404 | Requested resource not found |
| `INTERNAL_ERROR` | 500 | Internal server error |

## Request/Response Models

### ProofRequest

```json
{
  "premises": ["string"],
  "goal": "string"
}
```

### ProofResponse

```json
{
  "valid": "boolean",
  "message": "string",
  "proof": {
    "steps": [
      {
        "stepNumber": "integer",
        "formula": "string",
        "justification": "string"
      }
    ],
    "complete": "boolean"
  }
}
```

### RuleInfo

```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "parameters": [
    {
      "name": "string",
      "type": "string",
      "required": "boolean"
    }
  ]
}
```

### FormulaParse

```json
{
  "formula": "string",
  "parsed": "boolean",
  "terms": [
    {
      "type": "string",
      "left": "object",
      "right": "object"
    }
  ]
}
```

## Examples

### Example 1: Simple Classical Proof

**Goal**: Prove `p ∨ q` from `p`

```powershell
# Step 1: Parse the formula
$body = @{
    formula = "p | q"
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/classical/parse

# Step 2: Apply rule to prove it
$proofBody = @{
    ruleId = "or-intro-left"
    proof = @{
        premises = @("p")
        goal = "p | q"
        steps = @()
    }
    parameters = @{
        premise = "p"
    }
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $proofBody `
  http://localhost:8080/api/classical/apply-rule
```

### Example 2: Modal Proof

**Goal**: Prove `◇p` from `□p`

```powershell
# Parse modal formula
$body = @{
    formula = "<>p"
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/modal/parse

# Apply rules
$proofBody = @{
    ruleId = "box-elim"
    proof = @{
        premises = @("[]p")
        goal = "<>p"
        steps = @()
    }
    parameters = @{
        premise = "[]p"
    }
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $proofBody `
  http://localhost:8080/api/modal/apply-rule
```

## Rate Limiting

Currently, no rate limiting is applied. This may change in future versions.

## Authentication

Currently, no authentication is required. Future versions may require API tokens.

## CORS

The API supports CORS for development. Configuration can be updated in `application.properties`.

## Versioning

API version: `v1` (implicit in current endpoints)

Future versions will use explicit versioning: `/api/v2/classical/...`

## References

- [Setup & Installation](./SETUP.md) - Run the application
- [Logical Languages](./LANGUAGES.md) - Formula specifications
- [Development Guide](./DEVELOPMENT.md) - Adding new endpoints

