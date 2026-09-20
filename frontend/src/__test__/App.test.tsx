import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache } from '../service/actions';

function jsonResponse(status: number, body: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response;
}

describe('App', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    clearActionsCache();
    (global as any).fetch = fetchMock;
  });

  const actionRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/actions'));

  // Opens the New Proof dialog and starts a proof with one premise.
  const startProof = async (user: ReturnType<typeof userEvent.setup>, premise: string, goal: string) => {
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    await user.type(screen.getByPlaceholderText('Premise 1'), premise);
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), goal);
    await user.click(screen.getByText('Start Proof'));
  };

  test('fetches the actions once, no matter how many proof or colour updates follow', async () => {
    const user = userEvent.setup();
    const proof = {
      steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
      logic: 'classical',
      goal: 'P',
    };
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/actions')) return jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]);
      return jsonResponse(200, { proof, success: true, message: '' });
    });
    render(<App />);

    await startProof(user, 'P', 'P');

    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
    await user.type(screen.getByLabelText(/Line number:/i), '1');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/logic/classical/action', expect.anything()));
    await startProof(user, 'Q', 'Q');

    // The Menu is remounted for the new proof, which does not ask for the actions again.
    expect(await screen.findByLabelText(/Select Inference Rule:/i)).toBeInTheDocument();
    expect(actionRequests()).toHaveLength(1);
  });

  test('a new proof starts with no rule, no typed input and no error left from the previous one', async () => {
    const user = userEvent.setup();
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/actions')) return jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]);
      return jsonResponse(202, { proof: {}, success: false, message: 'Rule not applicable' });
    });
    render(<App />);
    await startProof(user, 'P', 'P');
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
    await user.type(screen.getByLabelText(/Line number:/i), '1');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Rule not applicable');

    await startProof(user, 'Q', 'Q');

    expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('');
    expect(screen.getByText('-- Choose a rule --')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Line number:/i)).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  test('an invalid line number can never reach the backend, so "Line -1 does not exist" cannot happen', async () => {
    const user = userEvent.setup();
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/actions')) return jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]);
      return jsonResponse(202, { proof: {}, success: false, message: 'Line -1 does not exist' });
    });
    render(<App />);
    await startProof(user, 'P', 'P');
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
    const applyButton = screen.getByRole('button', { name: /Apply Rule/i });

    for (const text of ['a', '1.5', '0', '2']) {
      const input = screen.getByLabelText(/Line number:/i);
      await user.clear(input);
      await user.type(input, text);
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(applyButton).toBeDisabled();
      await user.click(applyButton);
    }
    await user.clear(screen.getByLabelText(/Line number:/i));
    expect(applyButton).toBeDisabled();
    await user.click(applyButton);

    const actionCalls = fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/action'));
    expect(actionCalls).toHaveLength(0);
    expect(screen.queryByText(/does not exist/)).not.toBeInTheDocument();
  });

  test('a valid line number is sent as a number, and the inputs start over afterwards', async () => {
    const user = userEvent.setup();
    const proof = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'P', rule: 'Rep [1]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'P',
    };
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/actions')) return jsonResponse(200, [{ name: 'Rep', params: ['INT'] }]);
      return jsonResponse(200, { proof, success: true, message: '' });
    });
    render(<App />);
    await startProof(user, 'P', 'P');
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
    await user.type(screen.getByLabelText(/Line number:/i), '1');

    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    await waitFor(() => expect(screen.getByLabelText(/Line number:/i)).toHaveValue(''));
    const [, init] = fetchMock.mock.calls.find(([url]) => String(url).endsWith('/action'))!;
    expect(JSON.parse(init.body).actionDto).toEqual({ name: 'Rep', sources: [1], extraParameters: { expression: '' } });
    expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('Rep');
    expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeDisabled();
  });

  test('an unparsable premise or goal never becomes a proof line', async () => {
    const user = userEvent.setup();
    fetchMock.mockImplementation(async () => jsonResponse(200, []));
    render(<App />);

    await startProof(user, 'p ->', 'q &&');

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getAllByRole('alert')).toHaveLength(2);
    expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
    expect(screen.queryByText('p ->')).not.toBeInTheDocument();
  });

  test('the hint to start a proof is shown once', () => {
    fetchMock.mockImplementation(async () => jsonResponse(200, []));
    render(<App />);

    expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
    expect(screen.queryByText(/to get started/)).not.toBeInTheDocument();
  });

  test('the Solve button sends the proof to the solver and shows the solved proof', async () => {
    const user = userEvent.setup();
    const solved = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P -> P', rule: '->I [1-1]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'P -> P',
      done: true,
    };
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/actions')) return jsonResponse(200, []);
      return jsonResponse(200, solved);
    });
    render(<App />);

    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    await user.type(screen.getByPlaceholderText('Premise 1'), 'P');
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'P -> P');
    await user.click(screen.getByText('Start Proof'));
    await user.click(screen.getByRole('button', { name: 'Solve' }));

    expect(await screen.findByRole('status')).toHaveTextContent('The proof is complete.');
    expect(fetchMock).toHaveBeenCalledWith('/logic/classical/solve', expect.objectContaining({ method: 'POST' }));
    expect(screen.getAllByText(/→/).length).toBeGreaterThan(0);
  });
});
