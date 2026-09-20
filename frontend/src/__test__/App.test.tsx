import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache } from '../service/actions';
import { ActionDescriptor } from '../types';

function jsonResponse(status: number, body: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response;
}

const REP: ActionDescriptor = { name: 'Rep', params: ['INT'] };

describe('App', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    (global as any).fetch = fetchMock;
  });

  const actionRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/actions'));
  const applyRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/action'));

  // The backend: the list of actions, and one answer to every other request (apply or solve).
  const mockBackend = (actions: ActionDescriptor[], status: number, body: unknown) => {
    fetchMock.mockImplementation(async (url: string) =>
      url.endsWith('/actions') ? jsonResponse(200, actions) : jsonResponse(status, body));
  };

  // Opens the New Proof dialog and starts a proof with one premise.
  const startProof = async (user: ReturnType<typeof userEvent.setup>, premise: string, goal: string) => {
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    await user.type(screen.getByPlaceholderText('Premise 1'), premise);
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), goal);
    await user.click(screen.getByText('Start Proof'));
  };

  // Renders the app, starts the proof P |- P and selects the Rep rule.
  const startWithRep = async () => {
    const user = userEvent.setup();
    render(<App />);
    await startProof(user, 'P', 'P');
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
    return user;
  };

  const applyButton = () => screen.getByRole('button', { name: /Apply Rule/i });

  const repProof = {
    steps: [
      { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
      { expression: 'P', rule: 'Rep [1]', assmsLevel: 0, extraParameters: {} },
    ],
    logic: 'classical',
    goal: 'P',
  };

  test('fetches the actions once, no matter how many proof or colour updates follow', async () => {
    mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
    const user = await startWithRep();

    await user.type(screen.getByLabelText(/Line number:/i), '1');
    await user.click(applyButton());
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/logic/classical/action', expect.anything()));
    await startProof(user, 'Q', 'Q');

    // The Menu is remounted for the new proof, which does not ask for the actions again.
    expect(await screen.findByLabelText(/Select Inference Rule:/i)).toBeInTheDocument();
    expect(actionRequests()).toHaveLength(1);
  });

  test('a new proof starts with no rule, no typed input and no error left from the previous one', async () => {
    mockBackend([REP], 202, { proof: {}, success: false, message: 'Rule not applicable' });
    const user = await startWithRep();
    await user.type(screen.getByLabelText(/Line number:/i), '1');
    await user.click(applyButton());
    expect(await screen.findByRole('alert')).toHaveTextContent('Rule not applicable');

    await startProof(user, 'Q', 'Q');

    expect(await screen.findByLabelText(/Select Inference Rule:/i)).toHaveValue('');
    expect(screen.getByText('-- Choose a rule --')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Line number:/i)).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  test('an invalid line number can never reach the backend, so "Line -1 does not exist" cannot happen', async () => {
    mockBackend([REP], 202, { proof: {}, success: false, message: 'Line -1 does not exist' });
    const user = await startWithRep();

    for (const text of ['a', '1.5', '0', '2']) {
      const input = screen.getByLabelText(/Line number:/i);
      await user.clear(input);
      await user.type(input, text);
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(applyButton()).toBeDisabled();
      await user.click(applyButton());
    }
    await user.clear(screen.getByLabelText(/Line number:/i));
    expect(applyButton()).toBeDisabled();
    await user.click(applyButton());

    expect(applyRequests()).toHaveLength(0);
    expect(screen.queryByText(/does not exist/)).not.toBeInTheDocument();
  });

  test('a valid line number is sent as a number, and the inputs start over afterwards', async () => {
    mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
    const user = await startWithRep();
    await user.type(screen.getByLabelText(/Line number:/i), '1');

    await user.click(applyButton());

    await waitFor(() => expect(screen.getByLabelText(/Line number:/i)).toHaveValue(''));
    const [, init] = applyRequests()[0];
    expect(JSON.parse(init.body).actionDto).toEqual({ name: 'Rep', sources: [1], extraParameters: { expression: '' } });
    expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('Rep');
    expect(applyButton()).toBeDisabled();
  });

  test('an unparsable premise or goal never becomes a proof line', async () => {
    mockBackend([], 200, {});
    const user = userEvent.setup();
    render(<App />);

    await startProof(user, 'p ->', 'q &&');

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getAllByRole('alert')).toHaveLength(2);
    expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
    expect(screen.queryByText('p ->')).not.toBeInTheDocument();
  });

  test('while the New Proof dialog is open the page behind it is inert, and closing it gives the focus back', async () => {
    const user = userEvent.setup();
    mockBackend([REP], 200, {});
    const { container } = render(<App />);
    const opener = screen.getByRole('button', { name: /Start a new proof/i });
    const behindTheDialog = () => container.querySelector('.App > div');
    expect(behindTheDialog()).not.toHaveAttribute('inert');

    await user.click(opener);
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());

    expect(behindTheDialog()).toHaveAttribute('inert');

    await user.keyboard('{Escape}');

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(behindTheDialog()).not.toHaveAttribute('inert');
    expect(opener).toHaveFocus();
  });

  test('the hint to start a proof is shown once', () => {
    mockBackend([], 200, {});
    render(<App />);

    expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
    expect(screen.queryByText(/to get started/)).not.toBeInTheDocument();
  });

  test('the Solve button sends the proof to the solver and shows the solved proof', async () => {
    const solved = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P -> P', rule: '->I [1-1]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'P -> P',
      done: true,
    };
    mockBackend([], 200, solved);
    const user = userEvent.setup();
    render(<App />);

    await startProof(user, 'P', 'P -> P');
    await user.click(await screen.findByRole('button', { name: 'Solve' }));

    expect(await screen.findByRole('status')).toHaveTextContent('The proof is complete.');
    expect(fetchMock).toHaveBeenCalledWith('/logic/classical/solve', expect.objectContaining({ method: 'POST' }));
    expect(screen.getAllByText(/→/).length).toBeGreaterThan(0);
  });
});
