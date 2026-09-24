import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache } from '../service/actions';
import { ActionDescriptor, ProofDto, StepDto } from '../types';
import { ASSUME, jsonResponse, mockStatesBackend, requestsTo as requestsToPath } from './statesBackend';

// A modal proof is started as a user would, against a mocked modal backend.

const BOX_E: ActionDescriptor = { name: '[]E', params: ['INT', 'INT'] };

describe('App with a modal proof', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    (global as any).fetch = fetchMock;
    window.sessionStorage.clear();
    // `[]E 1, 2` puts `p` in `s1`.
    mockStatesBackend(fetchMock, [BOX_E, ASSUME], { '[]E': { expression: 'p', rule: '[]E [1, 2]', assmsLevel: 0, extraParameters: { state: 's1' } } });
  });

  const requestsTo = (path: string) => requestsToPath(fetchMock, path);

  // Picks modal logic on the empty page and starts the proof of `p` from `[]p` and `s0 <= s1` in the New Proof dialog.
  const startModalProof = async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.selectOptions(screen.getByLabelText('Logic:'), 'modal');
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByPlaceholderText('Premise 1'), '[[]p');
    await user.click(within(dialog).getByRole('button', { name: /Add Premise/i }));
    await user.type(within(dialog).getByPlaceholderText('Premise 2'), 's0 <= s1');
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), 'p');
    await user.click(within(dialog).getByText('Start Proof'));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    return user;
  };

  const stateCells = () => screen.getAllByRole('row').slice(1).map((row) => row.querySelector('td.state')?.textContent);

  test('the premises are in s0, except a relation between states, which is in none', async () => {
    await startModalProof();

    const [replay] = requestsTo('/logic/modal/action');
    expect(replay.proofDto.logic).toBe('modal');
    expect(replay.proofDto.steps.map((step: StepDto) => step.extraParameters)).toEqual([{ state: 's0' }, {}]);

    expect(screen.getByRole('columnheader', { name: 'State' })).toBeInTheDocument();
    expect(stateCells()).toEqual(['s0', '–none (a relation between states)']);
  });

  test('the proof survives its first action, and the new step says which state it is in', async () => {
    const user = await startModalProof();

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), '[]E');
    const [first, second] = screen.getAllByLabelText(/Line number:/i);
    await user.type(first, '1');
    await user.type(second, '2');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(4));
    expect(stateCells()).toEqual(['s0', '–none (a relation between states)', 's1']);
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  test('a rule with a State input sends the state and shows the step in it', async () => {
    const user = await startModalProof();

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Ass');
    await user.type(screen.getByLabelText(/Expression:/i), 'q');
    await user.type(screen.getByLabelText(/State:/i), 's2');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(4));
    const rule = requestsTo('/logic/modal/action').find((body) => body.actionDto.name === 'Ass');
    expect(rule.actionDto.extraParameters).toEqual({ expression: 'q', state: 's2' });
    expect(rule.proofDto.steps.map((step: StepDto) => step.extraParameters)).toEqual([{ state: 's0' }, {}]);
    expect(stateCells()[2]).toBe('s2');
  });

  test('a classical proof has no State column and sends its premises without a state', async () => {
    const user = userEvent.setup();
    fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
      if (url.endsWith('/actions')) return jsonResponse(200, []);
      const { proofDto } = JSON.parse(String(init!.body));
      return jsonResponse(202, { proof: proofDto, success: false, done: false, message: 'out of range' });
    });
    render(<App />);
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByPlaceholderText('Premise 1'), 'p');
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), 'p');
    await user.click(within(dialog).getByText('Start Proof'));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());

    expect(requestsTo('/logic/classical/action')[0].proofDto.steps[0].extraParameters).toEqual({});
    expect(screen.queryByRole('columnheader', { name: 'State' })).not.toBeInTheDocument();
    expect(stateCells()).toEqual([undefined]);
  });
});
