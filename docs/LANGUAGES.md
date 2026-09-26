# Logical Languages

This document describes the logical systems supported by the Natural Deduction project.

## Classical Propositional Logic

### Overview

Classical propositional logic is the standard logic system dealing with propositions and truth values. It forms the foundation for the Natural Deduction system and all classical proofs.

### Logical Operators

| Operator      | Symbol | Name          | Meaning                                     |
|---------------|--------|---------------|---------------------------------------------|
| AND           | ∧      | Conjunction   | Both propositions are true                  |
| OR            | ∨      | Disjunction   | At least one proposition is true            |
| NOT           | ¬      | Negation      | The proposition is false                    |
| IMPLIES       | →      | Implication   | If first is true, then second is true       |

There is no biconditional operator; the constants `TRUE` and `FALSE` exist. In the parsers the operators are
written `&`, `|`, `->` and `-` (negation), see [Parsing](#parsing).

### Syntax

**Propositions**: Variables representing atomic propositions
```
p, q, r, A, B, C, ...
```

**Formulas**: Combinations of propositions and operators
```
Grammar:
  Formula ::= Atom | (Formula ∧ Formula) | (Formula ∨ Formula) 
            | (Formula → Formula) | ¬Formula
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

These are the rules the tool offers for classical logic (`GET /logic/classical/actions`). Each one is written in a
proof as its rule text, e.g. `&I [1, 2]`; the UI shows the symbol, e.g. `∧I`. Line numbers are 1-based.

| Rule | Rule text | Symbol | What it does |
|------|-----------|--------|--------------|
| Assumption | `Ass` | Ass | Assume `A`, opening a new subproof (a premise is an assumption at the top level) |
| And introduction | `&I [i, j]` | ∧I | From `A` and `B`, derive `A ∧ B` |
| And elimination | `&E [i]` | ∧E | From `A ∧ B`, derive `A` (left) or `B` (right) |
| Or introduction | `\|I [i]` | ∨I | From `A`, derive `A ∨ B` (left) or `B ∨ A` (right) for any formula given |
| Or elimination | `\|E [i, j, k]` | ∨E | From `A ∨ B`, `A → C` and `B → C`, derive `C` |
| Deduction theorem | `->I [i-j]` | →I | Close the last assumption `A` (line `i`), with `B` the last line (`j`), and derive `A → B` |
| Modus ponens | `->E [i, j]` | →E | From `A → B` (line `i`) and `A` (line `j`), derive `B` |
| Negation introduction | `-I [i-j]` | ¬I | Close the last assumption `A`, which led to `⊥`, and derive `¬A` |
| Double negation elimination | `-E [i]` | ¬E | From `¬¬A`, derive `A` (classical only, see [Intuitionistic](#intuitionistic-propositional-logic)) |
| Falsum introduction | `FI [i, j]` | ⊥I | From `A` and `¬A`, derive `⊥` (`FALSE`) |
| Falsum elimination | `FE [i]` | ⊥E | From `⊥`, derive any `A` |
| Repetition | `Rep [i]` | Rep | Repeat a line that is still available |

`∨E` takes the two cases as implications, not as subproofs: prove `A → C` and `B → C` first (with `→I`). A closed
subproof is not removed from the proof, but its lines can no longer be used.

### Example Proofs

In the proof-text layout ("Copy proof as text" in the UI, `POST /logic/classical/proof`): 3 spaces of indent per open
assumption, an 11-space gap, then the rule. The last line is the goal and the leading top-level `Ass` lines are the
premises.

**Goal**: `p ⊢ p ∨ q`

```
p           Ass
p | q           |I [1]
```

**Goal**: `⊢ (p ∧ q) → (p ∨ q)`

```
   p & q           Ass
   p           &E [1]
   p | q           |I [2]
(p & q) -> (p | q)           ->I [1-3]
```

## Intuitionistic Propositional Logic

`intuitionistic` is classical logic without double negation elimination (`-E`, shown as ¬E). It has the same
formulas, parser, proof format and the other 13 actions, ex falso (`⊥E`) included. So `¬¬A ⊢ A` and `⊢ A ∨ ¬A` cannot be
proved, while `A ⊢ ¬¬A` can:

```
p           Ass
   - p           Ass
   FALSE           FI [1, 2]
- (- p)           -I [2-3]
```

A proof that uses `-E` (in a request or in an uploaded file) is refused, and so is the action. There is no automatic
solver: the classical one may use `-E`. See [API.md](./API.md#intuitionistic-logic).

## Modal Propositional Logic

### Overview

Modal propositional logic extends classical logic with **modal operators** that express necessity and possibility. The system uses **possible world semantics** with labeled states.

### Extended Operators

In addition to classical operators, modal logic includes (written `[]` and `<>` in the parser):

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

**States** (worlds): every step of a modal proof carries a state, e.g. `s0`, sent as `extraParameters.state` in the
API. Formulas relate states with `<=` and `=` (`LessEqual`, `Equals`); a step that is such a relation is in no state and
has empty `extraParameters`. A proof starts in `s0`: a premise (a leading top-level `Ass`) that is not a relation must
say `{"state": "s0"}`, or the proof is a `400` ("the assumptions are not in a valid state"); a relation premise such as
`s0 <= s1` needs no state. In the proof-file layout a step in a state starts with it, before the indent
(`s1:    p           []E [1, 2]`); a relation line has no prefix. The UI sends its premises this way and shows each
step's state in a State column. `ModalLogicParser` does not parse the model's `Until`
operator; the `modal-next-until` logic below does.

In the frontend, every formula field of a modal proof adds □ and ◇ buttons (they type `[]` and `<>`) to the connective
buttons, and its syntax hint also explains the relations `s0 <= s1` and `s0 = s1` (`connectives.ts`); `Until` is left
out, as it cannot be typed.

A `modal-next-until` proof gets those and also X and U buttons (they type `X ` and ` U `, with a space before `X` too when a
name is right before the caret, since the backend reads `X` and `U` only as words of their own), and its hint says so and that `s0+1` is the state after `s0`. There, the instant
check of the New Proof dialog (`checkFormula`) reads `X` as a unary and `U` as a binary operator only as whole words
(`Xp`, `pUq` and `TRUE` stay names) and a successor state such as `s0+1` or `s0 + 1` as one operand; the State column,
the State inputs and "Copy proof as text" (`s0+1: p           XE [1]`) take successor states as the backend writes them.

### Examples

```
□p              (p is necessarily true)
◇q              (q is possibly true)
□(p → q)        (if p, then q, necessarily)
p ∧ ◇q          (p and possibly q)
□p → ◇q         (if necessarily p, then possibly q)
```

### World Semantics

Modal logic uses **possible world semantics** where:

- Formulas are evaluated in specific worlds (states)
- An accessibility relation `<=` says which states can be reached from which
- □ quantifies over every reachable state, ◇ over some reachable state

**Accessibility**: `<=` is reflexive and transitive (the modal logic S4): the rules `Refl` (from any line in state `s`,
derive `s <= s`) and `Trans` (from `s <= t` and `t <= u`, derive `s <= u`) derive relation formulas between states.

### Natural Deduction Rules for Modal Logic

The classical rules apply within a state: their premises are in the same state and so is the result (`Ass` and
`⊥E` take the state of the new line). The modal rules (`GET /logic/modal/actions`) are:

| Rule | Rule text | Symbol | What it does |
|------|-----------|--------|--------------|
| Box introduction | `[]I [i-j]` | □I | Close the last assumption `s <= t`, with `t` a fresh state and `A` in state `t` the last line, and derive `□A` in state `s` |
| Box elimination | `[]E [i, j]` | □E | From `□A` in state `s` and `s <= t`, derive `A` in state `t` |
| Diamond introduction | `<>I [i, j]` | ◇I | From `A` in state `t` and `s <= t`, derive `◇A` in state `s` |
| Diamond elimination | `<>E [i, j-k]` | ◇E | From `◇A` in state `s`, close the last two assumptions `s <= t` and `A` in state `t`, with `t` a fresh state, and derive the last line `C` |
| Reflexivity | `Refl [i]` | Refl | From any line in state `s`, derive `s <= s` |
| Transitivity | `Trans [i, j]` | Trans | From `s <= t` and `t <= u`, derive `s <= u` |

A fresh state is not `s0` and no earlier line that is still available uses it.

### Example Modal Proofs

In the proof-text layout, a line in a state starts with it (`s0: `), before the indent; a relation line has no
prefix. These are the reference solutions of two of the modal exercises.

**Goal**: `□p ⊢ ◇p`

```
s0: [] p           Ass
s0 <= s0           Refl [1]
s0: p           []E [1, 2]
s0: <> p           <>I [3, 2]
```

**Goal**: `□(p → q), □p ⊢ □q` (the K axiom)

```
s0: [] (p -> q)           Ass
s0: [] p           Ass
   s0 <= s1           Ass
   s1:    p -> q           []E [1, 3]
   s1:    p           []E [2, 3]
   s1:    q           ->E [4, 5]
s0: [] q           []I [3-6]
```

## Modal Logic with Next and Until (`modal-next-until`)

`modal-next-until` reads modal logic over **discrete time**: every state `s` has a successor `s+1`, and `<=` is the
reflexive-transitive closure of the successor (`s <= t` when `t` is `s+k` for some `k >= 0`). So `[] A` is "from now
on, always" and `<> A` is "now or later", as in linear temporal logic, and two connectives are added:

- **Next**, `X A`: `A` holds in `s+1`.
- **Until**, `A U B` (strong): `B` holds in some `s+k`, and `A` in `s`, ..., `s+(k-1)`.

States are terms: `s0`, `s0+1`, `s0+2`, ... (`StateTerm`, in `com.dan323.expressions.relation`). `ModalNextUntilLogicParser`
reads `X` with the unary connectives and `U` between them and `&` (see `docs/API.md`), and only as words of their own.

Rules (on top of the modal ones): `XI`/`XE` move a formula between `X A` in `s` and `A` in `s+1`; `Succ` gives
`s <= s+1`; `UI` introduces `A U B` from `B` now, or from `A` now and `X (A U B)`; `UE` unfolds `A U B` into
`B | (A & X (A U B))`; `U<>` gives `<> B`; `Ind` is induction, from `A` and `[] (A -> X A)` derive `[] A`. The fresh
state of `[]I`/`<>E` must be a new name (`s0+1` is not fresh), and a proof is done only when the goal is in `s0`.

Each rule is sound for that reading. With `Ind` and `U<>`, every axiom of the usual complete axiomatization of
future-time linear temporal logic (the `X` and `[]` distribution laws, `X - A <-> - X A`, `[] A -> A & X [] A`,
induction, the Until expansion law and `A U B -> <> B`) is derivable, which is the argument for completeness.

## Comparison

| Aspect | Classical | Intuitionistic | Modal | Modal with Next and Until |
|--------|-----------|----------------|-------|---------------------------|
| **Operators** | ∧, ∨, ¬, → | as classical | classical + □, ◇ | modal + X, U |
| **Actions** | 14 | 13 (no ¬E) | 14 classical + 6 modal | the 20 modal + 8 (`XI`, `XE`, `Succ`, `UI1`, `UI2`, `UE`, `U<>`, `Ind`) |
| **States** | none | none | named states, `<=` reflexive and transitive | `s0`, `s0+1`, ...: discrete time |
| **Solver** | yes | no | yes | no |
| **Logic name** | `classical` | `intuitionistic` | `modal` | `modal-next-until` |

## Implementation Details

### Classical and Intuitionistic Logic
- Formulas: `domain/logic-language/implementation/`
- Rules: `domain/proof-structures/implementation.deduction.classic/`
- Use cases: `domain/use-cases/classical-use-case/` (`com.dan323.uses.classical` and `com.dan323.uses.intuitionistic`,
  which filters out `-E`)

### Modal Logic (and Next and Until)
- Formulas: `domain/logic-language/implementation.modal/` (`ModalLogicParser`, `ModalNextUntilLogicParser`)
- Rules: `domain/proof-structures/implementation.deduction.modal/` (Next and Until in `com.dan323.proof.modal.nextuntil`)
- Use cases: `domain/use-cases/modal-use-case/` (`com.dan323.uses.modal` and `com.dan323.uses.modal.nextuntil`)

### Parsing

Formulas are parsed from string input by an expression parser built on javaluator, specific to each logic
(`ClassicalParser`, `ModalLogicParser`, `ModalNextUntilLogicParser`). Variables are single tokens without spaces. The
server prints formulas fully parenthesized with spaces (`(A | B) -> ((- C) & D)`), and a printed formula parses back
to the same one. Two negations in a row must be separated by parentheses: `- (- p)` parses, `--p` and `- -p` do not.

**Classical Examples**:
```text
p
p & q        (AND)
p | q        (OR)
-p           (NOT, printed - p)
- (- p)      (double negation)
p -> q       (IMPLIES)
TRUE, FALSE  (constants)
```

**Modal Examples**:
```
[]p          (Box: necessity)
<>p          (Diamond: possibility)
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

