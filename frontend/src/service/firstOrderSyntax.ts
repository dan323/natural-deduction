// The instant syntax check of a first-order formula (`checkFormula` for the `first-order` logic). It follows the grammar
// of the backend's `FirstOrderParser`, token by token, so that it accepts what the backend accepts:
//
//   formula := or ('&' or)*
//   or      := imp ('|' imp)*
//   imp     := unary ('->' unary)*
//   unary   := '-' unary | ('forall' | 'exists') var '.' formula | primary
//   primary := term '=' term | '(' formula ')' | 'TRUE' | 'FALSE' | name ['(' term (',' term)* ')']
//   term    := '(' term ')' | name ['(' term (',' term)* ')']
//
// A quantifier body reaches as far right as it can. Terms (variables, constants and function symbols) start with a
// lowercase letter; a predicate is any name, and one without arguments is a propositional variable (`p -> q`). There
// is no infix operation on terms: a product is a function symbol, `m(x, y)`. `forall`, `exists`, `TRUE` and `FALSE`
// are reserved. The backend also limits the nesting depth; that is left to it.

const QUANTIFIERS: ReadonlySet<string> = new Set(['forall', 'exists']);
const RESERVED: ReadonlySet<string> = new Set([...QUANTIFIERS, 'TRUE', 'FALSE']);
// The longer symbol first, so that `->` is not read as `-` and `>`.
const SYMBOLS = ['->', '&', '|', '-', '(', ')', ',', '.', '='];
const BINARY_OPERATORS: ReadonlySet<string> = new Set(['->', '&', '|', '=']);
// An identifier, as the backend's tokenizer reads one: a letter, then letters, digits and `_`.
const IDENTIFIER = /^\p{L}[\p{L}\p{N}_]*/u;
// The symbols the proof table shows for the quantifiers, which cannot be typed as such.
const QUANTIFIER_SYMBOLS: Record<string, string> = { '∀': 'forall', '∃': 'exists' };

const ENDS_EARLY = 'The formula ends with an operator that has nothing after it.';
const NEVER_CLOSED = 'Unbalanced parentheses: "(" is never closed.';

class SyntaxProblem {
  constructor(readonly message: string) {}
}

function tokenize(text: string): string[] {
  const tokens: string[] = [];
  let rest = text.trimStart();
  while (rest !== '') {
    const token = IDENTIFIER.exec(rest)?.[0] ?? SYMBOLS.find((symbol) => rest.startsWith(symbol));
    if (token === undefined) {
      const char = String.fromCodePoint(rest.codePointAt(0)!);
      const keyword = QUANTIFIER_SYMBOLS[char];
      throw new SyntaxProblem(keyword
        ? `Type ${char} as "${keyword}", as in ${keyword} x. P(x).`
        : `Unexpected symbol "${char}".`);
    }
    tokens.push(token);
    rest = rest.slice(token.length).trimStart();
  }
  return tokens;
}

function isIdentifier(token: string | undefined): token is string {
  return token !== undefined && IDENTIFIER.test(token) && !RESERVED.has(token);
}

function isTermName(token: string | undefined): token is string {
  return isIdentifier(token) && /^\p{Ll}/u.test(token);
}

class Cursor {
  private position = 0;

  constructor(private readonly tokens: string[]) {}

  private peek(): string | undefined {
    return this.tokens[this.position];
  }

  private accept(token: string): boolean {
    if (this.peek() !== token) return false;
    this.position++;
    return true;
  }

  // The problem with a token that is left over where the formula (or a parenthesized part of it) should have ended.
  private leftover(token: string): SyntaxProblem {
    switch (token) {
      case ')': return new SyntaxProblem('Unbalanced parentheses: ")" has no matching "(".');
      case '=': return new SyntaxProblem('Both sides of "=" must be terms, such as x, e or m(x, y), not formulas.');
      case '.': return new SyntaxProblem('"." only follows the variable of forall or exists, as in forall x. P(x).');
      case ',': return new SyntaxProblem('"," only separates the arguments of a function or a predicate, as in P(x, y).');
      default: return new SyntaxProblem(`Missing operator before "${token}".`);
    }
  }

  expectEnd(): void {
    const token = this.peek();
    if (token !== undefined) throw this.leftover(token);
  }

  private expectClose(): void {
    if (this.accept(')')) return;
    const token = this.peek();
    throw token === undefined ? new SyntaxProblem(NEVER_CLOSED) : this.leftover(token);
  }

  // What is wrong with the token found where a formula (`what` = 'formula') or a term should start.
  private missing(what: 'formula' | 'term'): SyntaxProblem {
    const token = this.peek();
    const previous = this.tokens[this.position - 1];
    if (token === undefined) return new SyntaxProblem(ENDS_EARLY);
    if (token === ')') return new SyntaxProblem('A parenthesis is closed right after an operator or "(".');
    if (BINARY_OPERATORS.has(token)) {
      return new SyntaxProblem(previous === undefined || previous === '('
        ? `The operator "${token}" is missing its left operand.`
        : `Missing an operand between "${previous}" and "${token}".`);
    }
    if (what === 'term' && isIdentifier(token)) {
      return new SyntaxProblem(`"${token}" is not a term: variables, constants and function symbols start with a lowercase letter.`);
    }
    if (what === 'term') return new SyntaxProblem(`Expected a term, such as x or f(x), but found "${token}".`);
    return this.leftover(token);
  }

  formula(): void {
    this.disjunction();
    while (this.accept('&')) this.disjunction();
  }

  private disjunction(): void {
    this.implication();
    while (this.accept('|')) this.implication();
  }

  private implication(): void {
    this.unary();
    while (this.accept('->')) this.unary();
  }

  private unary(): void {
    if (this.accept('-')) {
      this.unary();
      return;
    }
    const keyword = this.peek();
    if (keyword !== undefined && QUANTIFIERS.has(keyword)) {
      this.position++;
      const variable = this.peek();
      if (!isTermName(variable)) {
        throw new SyntaxProblem(isIdentifier(variable)
          ? `The variable of "${keyword}" must start with a lowercase letter, as in ${keyword} x. P(x).`
          : `"${keyword}" must be followed by a variable and a ".", as in ${keyword} x. P(x).`);
      }
      this.position++;
      if (!this.accept('.')) {
        throw new SyntaxProblem(`"${keyword} ${variable}" must be followed by a ".", as in ${keyword} ${variable}. P(${variable}).`);
      }
      this.formula();
      return;
    }
    this.primary();
  }

  private primary(): void {
    const start = this.position;
    if (this.tryTerm() && this.accept('=')) {
      this.term();
      return;
    }
    this.position = start;
    if (this.accept('(')) {
      this.formula();
      this.expectClose();
      return;
    }
    if (this.accept('TRUE') || this.accept('FALSE')) return;
    if (!isIdentifier(this.peek())) throw this.missing('formula');
    this.position++;
    if (this.accept('(')) this.arguments();
  }

  // Whether a term starts here; when none does, the position is where it was.
  private tryTerm(): boolean {
    const start = this.position;
    try {
      this.term();
      return true;
    } catch (problem) {
      if (!(problem instanceof SyntaxProblem)) throw problem;
      this.position = start;
      return false;
    }
  }

  private term(): void {
    if (this.accept('(')) {
      this.term();
      this.expectClose();
      return;
    }
    if (!isTermName(this.peek())) throw this.missing('term');
    this.position++;
    if (this.accept('(')) this.arguments();
  }

  // The arguments after an opening parenthesis, and the closing one.
  private arguments(): void {
    this.term();
    while (this.accept(',')) this.term();
    const token = this.peek();
    if (token !== undefined && token !== ')') {
      throw new SyntaxProblem(`Expected "," or ")" after an argument but found "${token}".`);
    }
    this.expectClose();
  }
}

// The first problem with a first-order formula (already trimmed and not blank), or null when there is none.
export function checkFirstOrderFormula(text: string): string | null {
  try {
    const cursor = new Cursor(tokenize(text));
    cursor.formula();
    cursor.expectEnd();
    return null;
  } catch (problem) {
    if (problem instanceof SyntaxProblem) return problem.message;
    throw problem;
  }
}
