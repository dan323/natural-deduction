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

| Rule                        | Rule text       | Symbol | What it does                                                                                       |
|-----------------------------|-----------------|--------|----------------------------------------------------------------------------------------------------|
| Assumption                  | `Ass`           | Ass    | Assume `A`, opening a new subproof (a premise is an assumption at the top level)                   |
| And introduction            | `&I [i, j]`     | ∧I     | From `A` and `B`, derive `A ∧ B`                                                                   |
| And elimination             | `&E [i]`        | ∧E     | From `A ∧ B`, derive `A` (left) or `B` (right)                                                     |
| Or introduction             | `\|I [i]`       | ∨I     | From `A`, derive `A ∨ B` (left) or `B ∨ A` (right) for any formula given                           |
| Or elimination              | `\|E [i, j, k]` | ∨E     | From `A ∨ B`, `A → C` and `B → C`, derive `C`                                                      |
| Deduction theorem           | `->I [i-j]`     | →I     | Close the last assumption `A` (line `i`), with `B` the last line (`j`), and derive `A → B`         |
| Modus ponens                | `->E [i, j]`    | →E     | From `A → B` (line `i`) and `A` (line `j`), derive `B`                                             |
| Negation introduction       | `-I [i-j]`      | ¬I     | Close the last assumption `A`, which led to `⊥`, and derive `¬A`                                   |
| Double negation elimination | `-E [i]`        | ¬E     | From `¬¬A`, derive `A` (classical only, see [Intuitionistic](#intuitionistic-propositional-logic)) |
| Falsum introduction         | `FI [i, j]`     | ⊥I     | From `A` and `¬A`, derive `⊥` (`FALSE`)                                                            |
| Falsum elimination          | `FE [i]`        | ⊥E     | From `⊥`, derive any `A`                                                                           |
| Repetition                  | `Rep [i]`       | Rep    | Repeat a line that is still available                                                              |

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

A proof that uses `-E` (in a request or in an uploaded file) is refused, and so is the action. The automatic solver
is its own (`IntuitionisticAutomate`: the classical goal-directed solver without `-E` and without proof by
contradiction, which instead tries case splits, each side of a disjunction goal, `->E` backwards and ex falso); it may
leave an intuitionistic theorem unproved. See
[API.md](./API.md#intuitionistic-logic).

## Modal Propositional Logic

### Overview

Modal propositional logic extends classical logic with **modal operators** that express necessity and possibility. The system uses **possible world semantics** with labeled states.

### Extended Operators

In addition to classical operators, modal logic includes (written `[]` and `<>` in the parser):

| Operator            | Symbol | Name               | Meaning                               |
|---------------------|--------|--------------------|---------------------------------------|
| Box/Necessity       | □      | Always/Necessarily | True in all accessible worlds         |
| Diamond/Possibility | ◇      | Possibly           | True in at least one accessible world |

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

| Rule                 | Rule text      | Symbol | What it does                                                                                                                               |
|----------------------|----------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------|
| Box introduction     | `[]I [i-j]`    | □I     | Close the last assumption `s <= t`, with `t` a fresh state and `A` in state `t` the last line, and derive `□A` in state `s`                |
| Box elimination      | `[]E [i, j]`   | □E     | From `□A` in state `s` and `s <= t`, derive `A` in state `t`                                                                               |
| Diamond introduction | `<>I [i, j]`   | ◇I     | From `A` in state `t` and `s <= t`, derive `◇A` in state `s`                                                                               |
| Diamond elimination  | `<>E [i, j-k]` | ◇E     | From `◇A` in state `s`, close the last two assumptions `s <= t` and `A` in state `t`, with `t` a fresh state, and derive the last line `C` |
| Reflexivity          | `Refl [i]`     | Refl   | From any line in state `s`, derive `s <= s`                                                                                                |
| Transitivity         | `Trans [i, j]` | Trans  | From `s <= t` and `t <= u`, derive `s <= u`                                                                                                |

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

The automatic solver (`ModalNextUntilAutomate`) is the modal one plus `XI`/`XE`, `UI`, `UE`, `U<>` and `Succ`: it
turns a goal `X A` into `A` in the next state, and tries `B` now, then `A` now and `A U B` in the next state, for a
goal `A U B`. It does not use `Ind`, so a goal that needs induction is left unproved. See
[API.md](./API.md#modal-logic-with-next-and-until).

## First-Order Logic (`first-order`)

### Overview

`first-order` is classical first-order logic with equality. Formulas talk about individuals through **terms**, and
the quantifiers `∀` and `∃` range over them. There are no states and no automatic solver.

### Syntax

```
Term    ::= name | name(Term, ..., Term)          (variables, constants and function applications)
Formula ::= P(Term, ..., Term) | P | Term = Term | TRUE | FALSE
          | - Formula | Formula & Formula | Formula | Formula | Formula -> Formula
          | forall x. Formula | exists x. Formula
```

- Terms start with a lowercase letter. There is no infix notation on terms: a binary operation is a function symbol,
  so a product is `m(x, y)`, not `x * y`.
- A predicate is any name; a bare name used as a formula is a 0-ary predicate, so `p -> q` parses.
- `forall`, `exists`, `TRUE` and `FALSE` are reserved words. The body of a quantifier reaches as far right as it can:
  `forall x. P(x) & Q(x)` quantifies over the conjunction, and parentheses limit it (`(forall x. P(x)) & Q(a)`).
- A name that is free in a premise, such as `e` or `a`, acts as a constant: it cannot be generalized with `∀I`.
- Formulas are equal up to renaming of bound variables, so `exists y. P(y)` proves the goal `exists x. P(x)`.
- Substitution is capture-avoiding: a bound variable is renamed rather than capturing a free one.

The UI shows `forall x.` and `exists x.` as `∀x.` and `∃x.`; its ∀, ∃ and = buttons type `forall `, `exists ` and
` = `.

### Natural Deduction Rules

The 14 propositional rules of classical logic apply as they are (listed under their modal names, e.g. `|I1`; the
classical names are accepted too). `GET /logic/first-order/actions` adds:

| Rule                  | Rule text     | Symbol | What it does                                                                                                    |
|-----------------------|---------------|--------|-----------------------------------------------------------------------------------------------------------------|
| For all introduction  | `∀I [i]`      | ∀I     | From `A[x:=a]`, derive the target `∀x. A`; `a` is free in no premise, no open assumption and not in the target  |
| For all elimination   | `∀E [i]`      | ∀E     | From `∀x. A` and a term `t`, derive `A[x:=t]`                                                                   |
| Exists introduction   | `∃I [i]`      | ∃I     | From `A[x:=t]` for some term `t`, derive the target `∃x. A`                                                     |
| Exists elimination    | `∃E [i, j-k]` | ∃E     | From `∃x. A`, close the last assumption `A[x:=a]` (`a` fresh), whose subproof ends on `C`, and derive `C`       |
| Equality introduction | `=I`          | =I     | Derive `t = t` for a term `t`                                                                                   |
| Equality elimination  | `=E [i, j]`   | =E     | From `s = t` (line `i`) and `A` (line `j`), derive the target: `A` with some occurrences of `s` replaced by `t` |

`∀E` and `=I` take a term; `∀I`, `∃I` and `=E` take the formula to derive. The term of a `∀E` step is not written in
the proof: it is recovered from the formula.

### Example First-Order Proofs

These are the reference solutions of two of the first-order exercises, in the plain proof-text layout.

**Goal**: `a = b ⊢ b = a` (symmetry of equality)

```
a = b           Ass
a = a           =I
b = a           =E [1, 2]
```

**Goal**: `∃x. ∀y. R(x, y) ⊢ ∀y. ∃x. R(x, y)`

```
exists x. forall y. R(x, y)           Ass
   forall y. R(a, y)           Ass
   R(a, b)           ∀E [2]
   exists x. R(x, b)           ∃I [3]
exists x. R(x, b)           ∃E [1, 2-4]
forall y. exists x. R(x, y)           ∀I [5]
```

### Group Theory

`GET /logic/first-order/theories` lists named premise sets. The `group` theory has the group axioms, with `m` the
operation, `e` the identity and `i` the inverse:

```
forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))
forall x. m(e, x) = x & m(x, e) = x
forall x. m(i(x), x) = e & m(x, i(x)) = e
```

Six exercises (ids `group-...`: the identity and inverses are unique, left cancellation, the inverse of the inverse
and of a product, and a group in which every element is its own inverse is commutative) start from exactly these
premises. In the UI, the New Proof dialog's "Premises from theory" picker fills them in, and the exercise list shows
the group exercises under "Group theory".

## Comparison

| Aspect         | Classical   | Intuitionistic   | Modal                                       | Modal with Next and Until                                               | First-order                                           |
|----------------|-------------|------------------|---------------------------------------------|-------------------------------------------------------------------------|-------------------------------------------------------|
| **Operators**  | ∧, ∨, ¬, →  | as classical     | classical + □, ◇                            | modal + X, U                                                            | classical + ∀, ∃, =, predicates and functions         |
| **Actions**    | 14          | 13 (no ¬E)       | 14 classical + 6 modal                      | the 20 modal + 8 (`XI`, `XE`, `Succ`, `UI1`, `UI2`, `UE`, `U<>`, `Ind`) | 14 classical + 6 (`∀I`, `∀E`, `∃I`, `∃E`, `=I`, `=E`) |
| **States**     | none        | none             | named states, `<=` reflexive and transitive | `s0`, `s0+1`, ...: discrete time                                        | none                                                  |
| **Solver**     | yes         | yes              | yes                                         | yes, without `Ind`                                                      | no                                                    |
| **Logic name** | `classical` | `intuitionistic` | `modal`                                     | `modal-next-until`                                                      | `first-order`                                         |

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

### First-Order Logic
- Formulas and terms: `domain/logic-language/implementation.firstorder/` (`com.dan323.expressions.firstorder`:
  `FirstOrderParser`, `Forall`, `Exists`, `Predicate`, `Equals`, `FunctionApplication`, `Alpha`)
- Rules: `domain/proof-structures/implementation.deduction.firstorder/` (`FirstOrderNaturalDeduction`, one
  `FirstOrder*` class per rule, `Instances` for the side conditions of the quantifier and equality rules)
- Use cases: `domain/use-cases/first-order-use-case/` (`com.dan323.uses.firstorder`, with `FirstOrderExercises` and
  `FirstOrderTheories`)

### Parsing

Formulas are parsed from string input by an expression parser built on javaluator, specific to each logic
(`ClassicalParser`, `ModalLogicParser`, `ModalNextUntilLogicParser`). First-order logic has a hand-written
recursive-descent `FirstOrderParser` instead, since javaluator cannot read binders or undeclared function symbols. Variables are single tokens without spaces. The
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

**First-Order Examples**:
```
P(a)                          (predicate applied to a constant)
forall x. P(x) -> Q(x)        (the body reaches to the right)
exists x. m(x, e) = x         (equation between terms)
(forall x. P(x)) & Q(a)       (parentheses limit a quantifier)
```

## Further Reading

- **Classical Logic**: https://en.wikipedia.org/wiki/Natural_deduction
- **Modal Logic**: https://en.wikipedia.org/wiki/Modal_logic
- **Kripke Semantics**: https://en.wikipedia.org/wiki/Kripke_semantics
- **First-Order Logic**: https://en.wikipedia.org/wiki/First-order_logic
- **Proof Verification**: See [Development Guide](./DEVELOPMENT.md)

## References

- [Architecture Overview](./ARCHITECTURE.md) - System design
- [Project Modules](./MODULES.md) - Implementation locations
- [Development Guide](./DEVELOPMENT.md) - Extending with new logics

