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
// are reserved. Like the backend, nesting is limited to MAX_DEPTH levels, counted as the backend counts them (each
// negation, quantifier, parenthesis and function argument, and each further operand of an `&`, `|` or `->` chain), so a
// pasted formula of thousands of `-` or `(` gets a message instead of overflowing the stack, and a chain the backend
// would refuse is refused here too.

const QUANTIFIERS: ReadonlySet<string> = new Set(['forall', 'exists']);
const RESERVED: ReadonlySet<string> = new Set([...QUANTIFIERS, 'TRUE', 'FALSE']);
// The longer symbol first, so that `->` is not read as `-` and `>`.
const SYMBOLS = ['->', '&', '|', '-', '(', ')', ',', '.', '='];
const BINARY_OPERATORS: ReadonlySet<string> = new Set(['->', '&', '|', '=']);
// An identifier, as the backend's (Java `char`-based) tokenizer reads one: a letter, then letters, decimal digits and
// `_`. `Character.isLetter(char)` is false for the halves of a character outside the BMP, so none of those count.
const IDENTIFIER = /^(?![\u{10000}-\u{10FFFF}])\p{L}(?:(?![\u{10000}-\u{10FFFF}])[\p{L}\p{Nd}_])*/u;
// The whitespace Java's `Character.isWhitespace` skips: the Unicode separators except the non-breaking spaces
// (U+00A0, U+2007, U+202F), plus tab, line feed, vertical tab, form feed, carriage return and U+001C to U+001F.
const WHITESPACE = /^(?:(?![   ])[\t\n\v\f\r\u001C-\u001F\p{Z}])+/u;
// The symbols the proof table shows for the quantifiers, which cannot be typed as such.
const QUANTIFIER_SYMBOLS: Record<string, string> = { '∀': 'forall', '∃': 'exists' };

const MAX_DEPTH = 500;
const TOO_DEEP = `The formula is nested more than ${MAX_DEPTH} levels deep.`;

const ENDS_EARLY = 'The formula ends with an operator that has nothing after it.';
const NEVER_CLOSED = 'Unbalanced parentheses: "(" is never closed.';

class SyntaxProblem {
  constructor(readonly message: string) {}
}

function skipWhitespace(text: string): string {
  return text.slice(WHITESPACE.exec(text)?.[0].length ?? 0);
}

function tokenize(text: string): string[] {
  const tokens: string[] = [];
  let rest = skipWhitespace(text);
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
    rest = skipWhitespace(rest.slice(token.length));
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
  private depth = 0;

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

  private enter(): void {
    if (++this.depth > MAX_DEPTH) throw new SyntaxProblem(TOO_DEEP);
  }

  // Runs `parse` one nesting level deeper.
  private nested(parse: () => void): void {
    this.enter();
    try {
      parse();
    } finally {
      this.depth--;
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

  // `operand` (operator operand)*, each further operand one level deeper, as the backend nests the tree it builds.
  private chain(operator: string, operand: () => void): void {
    const start = this.depth;
    try {
      operand();
      while (this.accept(operator)) {
        this.enter();
        operand();
      }
    } finally {
      this.depth = start;
    }
  }

  formula(): void {
    this.chain('&', () => this.disjunction());
  }

  private disjunction(): void {
    this.chain('|', () => this.implication());
  }

  private implication(): void {
    this.chain('->', () => this.unary());
  }

  private unary(): void {
    this.nested(() => this.unaryBody());
  }

  private unaryBody(): void {
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
    this.nested(() => this.termBody());
  }

  private termBody(): void {
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
