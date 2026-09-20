import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';

function jsonResponse(status: number, body: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response;
}

describe('App', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    (global as any).fetch = fetchMock;
  });

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

    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    await user.type(screen.getByPlaceholderText('Premise 1'), 'P');
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'P');
    await user.click(screen.getByText('Start Proof'));

    await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), 'Rep');
    await user.type(screen.getByLabelText(/Line number:/i), '12');
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/logic/classical/action', expect.anything()));
    await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    const actionListCalls = fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/actions'));
    expect(actionListCalls).toHaveLength(1);
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
