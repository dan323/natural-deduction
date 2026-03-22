# Logical Languages

This document describes the logical systems supported by the Natural Deduction project.

## Classical Propositional Logic

### Overview

Classical propositional logic is the standard logic system dealing with propositions and truth values. It forms the foundation for the Natural Deduction system and all classical proofs.

### Logical Operators

| Operator | Symbol | Name | Meaning |
|----------|--------|------|---------|
| AND | ∧ | Conjunction | Both propositions are true |
| OR | ∨ | Disjunction | At least one proposition is true |
| NOT | ¬ | Negation | The proposition is false |
| IMPLIES | → | Implication | If first is true, then second is true |
| BICONDITIONAL | ↔ | Biconditional | Both propositions have the same truth value |

### Syntax

**Propositions**: Variables representing atomic propositions
```
p, q, r, A, B, C, ...
```

**Formulas**: Combinations of propositions and operators
```
Grammar:
  Formula ::= Atom | (Formula ∧ Formula) | (Formula ∨ Formula) 
            | (Formula → Formula) | (Formula ↔ Formula) | ¬Formula
  Atom ::= p | q | r | A | B | C | ... (any letter or identifier)
```

### Examples

```
p
¬p
p ∧ q
p ∨ q
p → q
¬p ∨ q  (equivalent to p → q)
(p ∧ q) → r
¬(p ∧ ¬q)
```

### Natural Deduction Rules

The Natural Deduction system includes the following rules for classical logic:

#### Conjunction (∧)

**∧-Introduction**: If we have proven both A and B, we can conclude A ∧ B
```
  A    B
  ------
  A ∧ B
```

**∧-Elimination Left**: From A ∧ B, we can conclude A
```
  A ∧ B
  -----
  A
```

**∧-Elimination Right**: From A ∧ B, we can conclude B
```
  A ∧ B
  -----
  B
```

#### Disjunction (∨)

**∨-Introduction Left**: From A, we can conclude A ∨ B
```
  A
  -----
  A ∨ B
```

**∨-Introduction Right**: From B, we can conclude A ∨ B
```
  B
  -----
  A ∨ B
```

**∨-Elimination**: From A ∨ B, if we can derive C from A and also from B, then we can conclude C
```
  A ∨ B    [A] ... C    [B] ... C
  --------------------------------
  C
```

#### Implication (→)

**→-Introduction**: To prove A → B, assume A and derive B
```
  [A] ... B
  ---------
  A → B
```

**→-Elimination (Modus Ponens)**: From A → B and A, we can conclude B
```
  A → B    A
  ----------
  B
```

#### Negation (¬)

**¬-Introduction**: To prove ¬A, assume A and derive a contradiction
```
  [A] ... ⊥
  ---------
  ¬A
```

**¬-Elimination**: From ¬A and A, we have a contradiction
```
  ¬A    A
  ------
  ⊥
```

**Double Negation Elimination**: From ¬¬A, we can conclude A
```
  ¬¬A
  ----
  A
```

#### Biconditional (↔)

**↔-Introduction**: To prove A ↔ B, prove both A → B and B → A
```
  A → B    B → A
  ---------------
  A ↔ B
```

**↔-Elimination Left**: From A ↔ B and A, we can conclude B
```
  A ↔ B    A
  ----------
  B
```

**↔-Elimination Right**: From A ↔ B and B, we can conclude A
```
  A ↔ B    B
  ----------
  A
```

### Example Proof

**Goal**: Prove p ∨ q from p

```
1. p           (Assumption: Goal)
2. p ∨ q       (∨-Introduction Left on 1)
```

**Goal**: Prove (p ∧ q) → (p ∨ q)

```
1. p ∧ q       (Assumption: Hypothesis)
2. p           (∧-Elimination Left on 1)
3. p ∨ q       (∨-Introduction Left on 2)
4. (p ∧ q) → (p ∨ q)  (→-Introduction, discharged assumption 1)
```

## Modal Propositional Logic

### Overview

Modal propositional logic extends classical logic with **modal operators** that express necessity and possibility. The system uses **possible world semantics** with labeled states.

### Extended Operators

In addition to classical operators, modal logic includes:

| Operator | Symbol | Name | Meaning |
|----------|--------|------|---------|
| Box/Necessity | □ | Always/Necessarily | True in all accessible worlds |
| Diamond/Possibility | ◇ | Possibly | True in at least one accessible world |

### Syntax

**Base Formulas** (from classical logic)
```
Atom ::= p | q | r | A | B | C | ...
Classical ::= Atom | (Classical ∧ Classical) | ... (as in classical logic)
```

**Modal Formulas**
```
Formula ::= Classical | □Formula | ◇Formula | (Formula ∧ Formula) | ...
```

**Labeled Formulas** (for world-specific propositions)
```
LabeledFormula ::= w:Formula
where w is a world label (s0, s1, w1, etc.)
```

### Examples

```
□p              (p is necessarily true)
◇q              (q is possibly true)
□(p → q)        (if p, then q, necessarily)
p ∧ ◇q          (p and possibly q)
□p → ◇q         (if necessarily p, then possibly q)
s0:p            (p is true in world s0)
s0:□p           (p is necessarily true, from world s0)
```

### World Semantics

Modal logic uses **possible world semantics** where:

- Formulas are evaluated in specific worlds
- Accessibility relations define which worlds can be reached from others
- Modal operators quantify over accessible worlds

**Accessibility Relations**:
- K (Knowledge): Reflexive and transitive
- S4: Reflexive and transitive
- S5: Equivalence relation (reflexive, symmetric, transitive)
- T: Reflexive only
- B: Reflexive and symmetric

This implementation typically uses S5 semantics (complete information).

### Natural Deduction Rules for Modal Logic

All classical rules apply in each world. Additionally:

#### Necessity (□)

**□-Introduction**: If we can derive A in all worlds, we can conclude □A
```
  [a new world w] ... A_w
  -----------------------
  □A
```

**□-Elimination**: From □A, we can conclude A in any world
```
  □A
  ----
  A
```

#### Possibility (◇)

**◇-Introduction**: From A in some world, we can conclude ◇A
```
  A_w
  ----
  ◇A
```

**◇-Elimination**: From ◇A, if we can derive C assuming A in a new world, then C
```
  ◇A    [A in new world w] ... C_w
  --------------------------------
  C
```

**Note**: C must not mention the new world w

### Example Modal Proof

**Goal**: Prove □p → ◇p (in S5 semantics)

```
1. □p           (Assumption: Hypothesis)
2. p            (□-Elimination on 1)
3. ◇p           (◇-Introduction on 2, in same world)
4. □p → ◇p      (→-Introduction, discharged assumption 1)
```

**Goal**: Prove □(p → q) ∧ □p → □q (K axiom)

```
1. □(p → q) ∧ □p    (Assumption: Hypothesis)
2. □(p → q)         (∧-Elimination Left on 1)
3. □p               (∧-Elimination Right on 1)
4. p → q            (□-Elimination on 2)
5. p                (□-Elimination on 3)
6. q                (→-Elimination on 4, 5)
7. □q               (□-Introduction on 6)
8. □(p → q) ∧ □p → □q  (→-Introduction, discharged assumption 1)
```

## Comparison: Classical vs Modal

| Aspect | Classical | Modal |
|--------|-----------|-------|
| **Operators** | ∧, ∨, ¬, →, ↔ | Classical + □, ◇ |
| **Worlds** | Single world assumed | Multiple possible worlds |
| **Semantics** | Truth values | Truth in specific worlds |
| **Accessibility** | N/A | Relations between worlds |
| **Quantification** | Over truth values | Over accessible worlds |
| **Proof Complexity** | Moderate | Higher (world management) |

## Implementation Details

### Classical Logic Implementation
- Located in: `domain/logic-language/implementation/`
- Rule implementations: `domain/proof-structures/implementation.deduction.classic/`

### Modal Logic Implementation
- Located in: `domain/logic-language/implementation.modal/`
- Rule implementations: `domain/proof-structures/implementation.deduction.modal/`
- Extends classical logic with modal operators and world handling

### Parsing

Formulas are parsed from string input using recursive descent parsers specific to each logic.

**Classical Examples**:
```
p
p & q        (AND)
p | q        (OR)
~p           (NOT)
p -> q       (IMPLIES)
p <-> q      (BICONDITIONAL)
```

**Modal Examples**:
```
[]p          (Box: necessity)
<>p          (Diamond: possibility)
s0:p         (Label: p in world s0)
[](p -> q)   (Box: necessity of implication)
```

## Further Reading

- **Classical Logic**: https://en.wikipedia.org/wiki/Natural_deduction
- **Modal Logic**: https://en.wikipedia.org/wiki/Modal_logic
- **Kripke Semantics**: https://en.wikipedia.org/wiki/Kripke_semantics
- **Proof Verification**: See [Development Guide](./DEVELOPMENT.md)

## References

- [Architecture Overview](./ARCHITECTURE.md) - System design
- [Project Modules](./MODULES.md) - Implementation locations
- [Development Guide](./DEVELOPMENT.md) - Extending with new logics

