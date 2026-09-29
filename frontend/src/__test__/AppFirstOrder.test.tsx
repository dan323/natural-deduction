import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache, clearExercisesCache, clearTheoriesCache } from '../service/actions';
import { ActionDescriptor, Exercise, ProofDto, StepDto, Theory } from '../types';
import { jsonResponse, requestsTo as requestsToPath } from './statesBackend';

// A first-order proof, `forall x. P(x) ⊢ exists x. P(x)`, is started and finished as a user would, against a mocked
// first-order backend that answers as `FirstOrderProofTransformer` does.

const FORALL_E: ActionDescriptor = {
  name: '∀E', params: ['INT', 'TERM'], label: 'For-all elimination', symbol: '∀E', category: 'ELIMINATION',
  description: 'From ∀x.A and a term t, derive A[x:=t]', paramLabels: ['Universal (∀x.A)', 'Term (t)'],
};
const EXISTS_I: ActionDescriptor = {
  name: '∃I', params: ['INT', 'EXPRESSION'], label: 'Exists introduction', symbol: '∃I', category: 'INTRODUCTION',
  description: 'From A[x:=t] for some term t, derive ∃x.A', paramLabels: ['Line with A[x:=t]', 'Target (∃x.A)'],
};
const EQUALS_I: ActionDescriptor = {
  name: '=I', params: ['TERM'], label: 'Equality introduction', symbol: '=I', category: 'INTRODUCTION',
  description: 'For a term t, derive t = t', paramLabels: ['Term (t)'],
};

const PREMISE = 'forall x. P(x)';

// The `group` theory as `FirstOrderTheories` serves it, and two exercises: one of the group theory, one not.
const GROUP_AXIOMS = [
  'forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))',
  'forall x. m(e, x) = x & m(x, e) = x',
  'forall x. m(i(x), x) = e & m(x, i(x)) = e',
];
const THEORIES: Theory[] = [{ id: 'group', name: 'Group', premises: GROUP_AXIOMS }];
const EXERCISES: Exercise[] = [
  { id: 'forall-to-exists', title: 'All to some', premises: [PREMISE], goal: 'exists x. P(x)', difficulty: 'EASY' },
  { id: 'group-double-inverse', title: 'The inverse of the inverse', premises: GROUP_AXIOMS, goal: 'forall x. i(i(x)) = x', difficulty: 'MEDIUM' },
];
const GOAL = 'exists x. P(x)';

type ActionBody = { proofDto: ProofDto, actionDto: { name: string, sources: number[], extraParameters: Record<string, string> } };

// `∀E` on a `forall x. P(x)` line gives `P(t)` for the term sent, `∃I` gives its target, `=I` gives `t = t`; the goal is
// done once a top-level step is it. The replay of a new proof (an out-of-range COPY) answers the proof as it came.
function mockFirstOrderBackend(fetchMock: jest.Mock) {
  fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
    if (url.endsWith('/actions')) return jsonResponse(200, [FORALL_E, EXISTS_I, EQUALS_I]);
    if (url.endsWith('/exercises')) return jsonResponse(200, EXERCISES);
    if (url.endsWith('/theories')) return jsonResponse(200, THEORIES);
    const { proofDto, actionDto } = JSON.parse(init!.body as string) as ActionBody;
    const withStep = (expression: string, rule: string) => {
      const steps: StepDto[] = [...proofDto.steps, { expression, rule, assmsLevel: 0, extraParameters: {} }];
      const done = steps.some((step) => step.assmsLevel === 0 && step.expression === proofDto.goal);
      return jsonResponse(200, { proof: { ...proofDto, steps, done }, success: true, done, message: '' });
    };
    const { name, sources, extraParameters } = actionDto;
    if (name === 'COPY') return jsonResponse(202, { proof: { ...proofDto, done: false }, success: false, done: false, message: 'out of range' });
    if (name === '∀E' && proofDto.steps[sources[0] - 1]?.expression === PREMISE && extraParameters.term) {
      return withStep(`P(${extraParameters.term})`, `∀E [${sources[0]}]`);
    }
    if (name === '∃I' && extraParameters.expression === GOAL) return withStep(GOAL, `∃I [${sources[0]}]`);
    if (name === '=I' && extraParameters.term) return withStep(`${extraParameters.term} = ${extraParameters.term}`, '=I');
    return jsonResponse(400, { message: 'unexpected action' });
  });
}

describe('App with a first-order proof', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    clearExercisesCache();
    clearTheoriesCache();
    (global as any).fetch = fetchMock;
    window.sessionStorage.clear();
    mockFirstOrderBackend(fetchMock);
  });

  const requestsTo = (path: string) => requestsToPath(fetchMock, path);
  const ruleRequests = () => requestsTo('/logic/first-order/action').filter((body) => body.actionDto.name !== 'COPY');

  // Picks first-order on the empty page and starts `forall x. P(x) ⊢ exists x. P(x)`, typing the quantifiers with their
  // buttons.
  const startProof = async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.selectOptions(screen.getByLabelText('Logic:'), 'first-order');
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByLabelText('Logic:')).toHaveValue('first-order');
    await user.click(within(dialog).getByRole('button', { name: 'Insert for all (forall) in Premise 1' }));
    await user.type(within(dialog).getByPlaceholderText('Premise 1'), 'x. P(x)');
    await user.click(within(dialog).getByRole('button', { name: 'Insert there exists (exists) in Goal' }));
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), 'x. P(x)');
    await user.click(within(dialog).getByText('Start Proof'));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    return user;
  };

  const expressionCells = () => screen.getAllByRole('row').slice(1).map((row) => row.querySelector('pre')?.textContent);

  test('the empty page offers the logic, and a malformed quantifier is refused before the backend sees it', async () => {
    const user = userEvent.setup();
    render(<App />);
    const option = within(screen.getByLabelText('Logic:')).getByRole('option', { name: 'First-order' });
    expect(option).toHaveValue('first-order');

    await user.selectOptions(screen.getByLabelText('Logic:'), 'first-order');
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByPlaceholderText('Premise 1'), 'forall x P(x)');
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), GOAL);
    await user.click(within(dialog).getByText('Start Proof'));

    expect(await within(dialog).findByText(/"forall x" must be followed by a "\."/)).toBeInTheDocument();
    expect(requestsTo('/logic/first-order/action')).toHaveLength(0);
  });

  test('∀E with a term, then ∃I, finishes the proof, which shows ∀ and ∃ and has no Solve', async () => {
    const user = await startProof();

    const [replay] = requestsTo('/logic/first-order/action');
    expect(replay.proofDto).toEqual({
      logic: 'first-order', goal: GOAL, steps: [{ expression: PREMISE, rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
    });
    expect(expressionCells()).toEqual(['∀x. P(x)']);
    expect(screen.queryByRole('button', { name: /Solve/i })).not.toBeInTheDocument();
    expect(screen.getByText('First-order logic has no automatic solver.')).toBeInTheDocument();

    // ∀E: a line and a term. The term input is plain text, without the formula hint or connective buttons.
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), '∀E');
    await user.type(screen.getByLabelText('Universal (∀x.A)'), '1');
    const term = screen.getByLabelText('Term (t)');
    expect(term).not.toHaveAttribute('aria-describedby');
    expect(screen.queryByRole('button', { name: /Insert/ })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeDisabled();
    await user.type(term, 'a');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
    expect(ruleRequests()[0].actionDto).toEqual({ name: '∀E', sources: [1], extraParameters: { expression: '', term: 'a' } });
    expect(expressionCells()).toEqual(['∀x. P(x)', 'P(a)']);

    // ∃I: a line and the target, typed with the ∃ button.
    await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), '∃I');
    await user.type(screen.getByLabelText('Line with A[x:=t]'), '2');
    await user.click(screen.getByRole('button', { name: 'Insert there exists (exists)' }));
    await user.type(screen.getByLabelText('Target (∃x.A)'), 'x. P(x)');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    expect(await screen.findByRole('status')).toHaveTextContent('Proof complete.');
    expect(ruleRequests()[1].actionDto).toEqual({ name: '∃I', sources: [2], extraParameters: { expression: GOAL } });
    expect(expressionCells()).toEqual(['∀x. P(x)', 'P(a)', '∃x. P(x)']);

    // The text of the proof is the layout the backend's FirstOrderProofParser reads.
    await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
    await screen.findByText(/Proof copied to the clipboard as text/);
    expect(await navigator.clipboard.readText()).toBe([
      'forall x. P(x)           Ass',
      'P(a)           ∀E [1]',
      'exists x. P(x)           ∃I [2]',
    ].join('\n'));
  });

  test('=I takes only a term, sent in extraParameters.term', async () => {
    const user = await startProof();

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), '=I');
    await user.type(screen.getByLabelText('Term (t)'), 'm(a, e)');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
    expect(ruleRequests()[0].actionDto).toEqual({ name: '=I', sources: [], extraParameters: { expression: '', term: 'm(a, e)' } });
    expect(expressionCells()).toEqual(['∀x. P(x)', 'm(a, e) = m(a, e)']);
  });

  test('New Proof fills in the group axioms from the theory picker, and they can be edited before Start Proof', async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.selectOptions(screen.getByLabelText('Logic:'), 'first-order');
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');

    await user.selectOptions(await within(dialog).findByLabelText('Premises from theory:'), 'Group');
    expect(within(dialog).getAllByPlaceholderText(/^Premise \d$/).map((input) => (input as HTMLInputElement).value)).toEqual(GROUP_AXIOMS);
    await user.click(within(dialog).getByRole('button', { name: 'Remove premise 1' }));
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), 'forall x. m(e, x) = x');
    await user.click(within(dialog).getByText('Start Proof'));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    const [replay] = requestsTo('/logic/first-order/action');
    expect(replay.proofDto.steps.map((step: StepDto) => step.expression)).toEqual(GROUP_AXIOMS.slice(1));
  });

  test('the group exercises have their own heading, and one starts from the group axioms in two clicks', async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.selectOptions(screen.getByLabelText('Logic:'), 'first-order');

    await user.click(screen.getByRole('button', { name: 'Exercises' }));
    const group = await screen.findByRole('region', { name: 'Group theory' });
    expect(within(group).getByText('Group axioms ⊢ forall x. i(i(x)) = x')).toBeInTheDocument();
    expect(within(screen.getByRole('region', { name: 'Easy' })).getByText('All to some')).toBeInTheDocument();
    await user.click(within(group).getByRole('button', { name: 'Start exercise The inverse of the inverse' }));

    await waitFor(() => expect(document.querySelector('.current-exercise')).toHaveTextContent('Exercise: The inverse of the inverse'));
    expect(expressionCells()).toHaveLength(3);
    const [replay] = requestsTo('/logic/first-order/action');
    expect(replay.proofDto.goal).toBe('forall x. i(i(x)) = x');
    expect(replay.proofDto.steps.map((step: StepDto) => step.expression)).toEqual(GROUP_AXIOMS);
  });
});
