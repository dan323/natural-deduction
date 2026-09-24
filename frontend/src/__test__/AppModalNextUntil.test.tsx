import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache } from '../service/actions';
import { ActionDescriptor, StepDto } from '../types';
import { ASSUME, mockStatesBackend, requestsTo as requestsToPath } from './statesBackend';

// A modal-next-until proof is started as a user would, against a mocked modal-next-until backend.

const NEXT_E: ActionDescriptor = { name: 'XE', params: ['INT'], label: 'Next elimination', symbol: 'XE' };

describe('App with a modal-next-until proof', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    (global as any).fetch = fetchMock;
    window.sessionStorage.clear();
    // `XE 1` puts `p` in `s0+1` and, since the goal `p` has to be in `s0` (`ModalNextUntilNaturalDeduction.isDone`), is
    // not done.
    mockStatesBackend(fetchMock, [NEXT_E, ASSUME], { XE: { expression: 'p', rule: 'XE [1]', assmsLevel: 0, extraParameters: { state: 's0+1' } } });
  });

  const requestsTo = (path: string) => requestsToPath(fetchMock, path);

  // Picks modal-next-until on the empty page and starts the proof of `p` from `X p` and `s0 <= s0+1`, typing X with its
  // button.
  const startProof = async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.selectOptions(screen.getByLabelText('Logic:'), 'modal-next-until');
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByLabelText('Logic:')).toHaveValue('modal-next-until');
    await user.click(within(dialog).getByRole('button', { name: 'Insert next (X) in Premise 1' }));
    await user.type(within(dialog).getByPlaceholderText('Premise 1'), 'p');
    await user.click(within(dialog).getByRole('button', { name: /Add Premise/i }));
    await user.type(within(dialog).getByPlaceholderText('Premise 2'), 's0 <= s0+1');
    await user.type(within(dialog).getByPlaceholderText('Enter the goal expression'), 'p');
    await user.click(within(dialog).getByText('Start Proof'));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    return user;
  };

  const stateCells = () => screen.getAllByRole('row').slice(1).map((row) => row.querySelector('td.state')?.textContent);

  test('the empty page offers the logic', () => {
    render(<App />);
    const option = within(screen.getByLabelText('Logic:')).getByRole('option', { name: 'Modal with Next and Until' });
    expect(option).toHaveValue('modal-next-until');
  });

  test('the premises are in s0, except a relation between successor states, and there is no Solve', async () => {
    await startProof();

    const [replay] = requestsTo('/logic/modal-next-until/action');
    expect(replay.proofDto.logic).toBe('modal-next-until');
    expect(replay.proofDto.steps.map((step: StepDto) => [step.expression, step.extraParameters])).toEqual([
      ['X p', { state: 's0' }], ['s0 <= s0+1', {}],
    ]);

    expect(stateCells()).toEqual(['s0', '–none (a relation between states)']);
    expect(screen.queryByRole('button', { name: /Solve/i })).not.toBeInTheDocument();
    expect(screen.getByText('Modal with Next and Until logic has no automatic solver.')).toBeInTheDocument();
  });

  test('a step in a successor state shows it, and the goal outside s0 does not finish the proof', async () => {
    const user = await startProof();

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'XE');
    await user.type(screen.getByLabelText(/Line number:/i), '1');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(4));
    expect(stateCells()).toEqual(['s0', '–none (a relation between states)', 's0+1']);
    expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
  });

  test('a State input takes a successor state, and the expression input has the X and U buttons', async () => {
    const user = await startProof();

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Ass');
    expect(screen.getByRole('button', { name: 'Insert next (X)' })).toBeInTheDocument();
    await user.type(screen.getByLabelText(/Expression:/i), 'q');
    await user.click(screen.getByRole('button', { name: 'Insert until (U)' }));
    await user.type(screen.getByLabelText(/Expression:/i), 'r');
    await user.type(screen.getByLabelText(/State:/i), 's0+2');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(4));
    const rule = requestsTo('/logic/modal-next-until/action').find((body) => body.actionDto.name === 'Ass');
    expect(rule.actionDto.extraParameters).toEqual({ expression: 'q U r', state: 's0+2' });
    expect(stateCells()[2]).toBe('s0+2');
  });
});
