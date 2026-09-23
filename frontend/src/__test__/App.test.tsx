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

  // Opens the New Proof dialog and starts a proof with one premise. Confirms discarding the current proof first,
  // when there is one with more than its premises (the dedicated tests for that confirmation exercise it directly).
  const startProof = async (user: ReturnType<typeof userEvent.setup>, premise: string, goal: string) => {
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const confirm = screen.queryByRole('button', { name: 'Discard and start new' });
    if (confirm) await user.click(confirm);
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

  test('closing the dialog gives the focus back to the opener even when the browser drops it as the page goes inert', async () => {
    const user = userEvent.setup();
    mockBackend([REP], 200, {});
    render(<App />);
    const opener = screen.getByRole('button', { name: /Start a new proof/i });
    // jsdom has no inert focus fixup. A browser blurs the focused element as soon as an ancestor of it becomes inert, and
    // an element inside something inert cannot be focused (React's own attempt to restore the focus after a commit
    // included).
    const setAttribute = Element.prototype.setAttribute;
    const focus = HTMLElement.prototype.focus;
    const spies = [
      jest.spyOn(Element.prototype, 'setAttribute').mockImplementation(function (this: Element, name: string, value: string) {
        setAttribute.call(this, name, value);
        if (name === 'inert' && this.contains(document.activeElement)) (document.activeElement as HTMLElement).blur();
      }),
      jest.spyOn(HTMLElement.prototype, 'focus').mockImplementation(function (this: HTMLElement, options?: FocusOptions) {
        if (!this.closest('[inert]')) focus.call(this, options);
      }),
    ];
    try {
      await user.click(opener);
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
      await user.keyboard('{Escape}');
    } finally {
      spies.forEach((spy) => spy.mockRestore());
    }

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
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

    expect(await screen.findByRole('status')).toHaveTextContent('Proof complete.');
    expect(fetchMock).toHaveBeenCalledWith('/logic/classical/solve', expect.objectContaining({ method: 'POST' }));
    expect(screen.getAllByText(/→/).length).toBeGreaterThan(0);
  });
  describe('picking the lines of a rule from the proof', () => {
    const MP: ActionDescriptor = { name: 'MP', params: ['INT', 'INT'] };
    const mpProof = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'P -> Q', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'Q', rule: '->E [1, 2]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'Q',
      done: true,
    };

    // Starts the proof P, P -> Q |- Q and selects the MP rule.
    const startWithMp = async () => {
      mockBackend([MP], 200, { proof: mpProof, success: true, message: '' });
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
      await user.type(screen.getByPlaceholderText('Premise 1'), 'P');
      await user.click(screen.getByText('+ Add Premise'));
      await user.type(screen.getByPlaceholderText('Premise 2'), 'P -> Q');
      await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'Q');
      await user.click(screen.getByText('Start Proof'));
      await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'MP');
      return user;
    };

    const stepRow = (line: number) => screen.getAllByRole('row')[line];

    test('clicking two rows fills the two inputs, glows them and lets the rule be applied', async () => {
      const user = await startWithMp();
      const lineInputs = () => screen.getAllByLabelText(/Line number:/i);

      await user.click(stepRow(1));
      expect(lineInputs()[0]).toHaveValue('1');
      expect(lineInputs()[1]).toHaveValue('');
      expect(stepRow(1)).toHaveClass('glow');
      expect(applyButton()).toBeDisabled();

      await user.click(stepRow(2));
      expect(lineInputs()[1]).toHaveValue('2');
      expect(stepRow(2)).toHaveClass('glow');
      expect(stepRow(1)).toHaveClass('glow');

      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));
      expect(JSON.parse(applyRequests()[0][1].body).actionDto.sources).toEqual([1, 2]);
    });

    test('Enter and Space on a focused row fill the inputs too', async () => {
      const user = await startWithMp();

      stepRow(2).focus();
      await user.keyboard('{Enter}');
      stepRow(1).focus();
      await user.keyboard(' ');

      const [first, second] = screen.getAllByLabelText(/Line number:/i);
      expect(first).toHaveValue('2');
      expect(second).toHaveValue('1');
      expect(applyButton()).toBeEnabled();
    });

    test('a line that was typed is kept, and the next pick goes to the next empty input', async () => {
      const user = await startWithMp();
      const lineInputs = () => screen.getAllByLabelText(/Line number:/i);
      await user.type(lineInputs()[1], '2');

      await user.click(stepRow(1));
      expect(lineInputs()[0]).toHaveValue('1');
      expect(lineInputs()[1]).toHaveValue('2');

      // Both are filled now: another click changes nothing.
      await user.click(stepRow(2));
      expect(lineInputs()[0]).toHaveValue('1');
      expect(lineInputs()[1]).toHaveValue('2');
    });

    test('without a rule selected a click on a row changes nothing', async () => {
      const user = await startWithMp();
      await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), '');

      await user.click(stepRow(1));

      expect(screen.queryByLabelText(/Line number:/i)).not.toBeInTheDocument();
      expect(stepRow(1)).not.toHaveClass('glow');
    });
  });

  describe('when a rule completes the proof', () => {
    const doneRep = { ...repProof, done: true };

    const completeWithRep = async () => {
      mockBackend([REP], 200, { proof: doneRep, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await screen.findByText('Proof complete.');
      return user;
    };

    test('announces it, marks the goal in text and takes no more rules', async () => {
      await completeWithRep();

      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
      expect(screen.getByText('✓ Proved')).toBeInTheDocument();
      expect(screen.getByLabelText(/Select Inference Rule:/i)).toBeDisabled();
      expect(applyButton()).toBeDisabled();
      expect(screen.getByRole('button', { name: 'Solve' })).toBeDisabled();
    });

    test('offers a new proof, which asks to confirm and then starts with the rule controls enabled again', async () => {
      const user = await completeWithRep();

      await user.click(screen.getByRole('button', { name: 'New Proof' }));
      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
      await user.type(screen.getByPlaceholderText('Premise 1'), 'Q');
      await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'Q');
      await user.click(screen.getByText('Start Proof'));

      expect(await screen.findByLabelText(/Select Inference Rule:/i)).toBeEnabled();
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
      expect(screen.getByText('Not proved yet')).toBeInTheDocument();
    });

    test('closing the dialog hands the focus back to the New Proof button of the toolbar', async () => {
      const user = await completeWithRep();

      await user.click(screen.getByRole('button', { name: 'New Proof' }));
      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
      await user.keyboard('{Escape}');

      await waitFor(() => expect(screen.getByRole('button', { name: /Start a new proof/i })).toHaveFocus());
    });
  });

  describe('confirming before New Proof discards work', () => {
    test('a proof with only its premises starts a new one right away, without asking', async () => {
      mockBackend([REP], 200, {});
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'P', 'P');

      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));

      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    });

    test('a proof with a step beyond its premises asks first, and Cancel keeps it and hands the focus back', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));

      const opener = screen.getByRole('button', { name: /Start a new proof/i });
      await user.click(opener);

      expect(screen.getByRole('alertdialog')).toHaveTextContent(/discards the current one/i);
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      await waitFor(() => expect(screen.getByRole('button', { name: 'Cancel' })).toHaveFocus());

      await user.click(screen.getByRole('button', { name: 'Cancel' }));

      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(opener).toHaveFocus();
      // Nothing was lost: the proof (2 steps: the premise and the applied rule, plus the header row) is still there.
      expect(screen.getAllByRole('row')).toHaveLength(3);
    });

    test('Escape cancels the confirmation the same way as the Cancel button', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(screen.getByRole('alertdialog')).toBeInTheDocument();

      await user.keyboard('{Escape}');

      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    test('confirming discards the current proof and opens the New Proof dialog', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));

      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    });
  });

  describe('undoing the last step', () => {
    test('is disabled with only the premises, or no proof at all', async () => {
      mockBackend([REP], 200, {});
      const user = userEvent.setup();
      render(<App />);

      expect(screen.getByRole('button', { name: 'Undo last step' })).toBeDisabled();

      await startProof(user, 'P', 'P');

      expect(screen.getByRole('button', { name: 'Undo last step' })).toBeDisabled();
    });

    test('resends the proof without its last step, and the backend revalidates it', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));
      expect(screen.getAllByRole('row')).toHaveLength(3);

      // The backend, once it replays only the premise, reports it not done: a fresh mock for the undo request.
      mockBackend([REP], 202, {
        proof: { steps: [repProof.steps[0]], logic: 'classical', goal: 'P' },
        success: false,
        done: false,
        message: 'Line 2 does not exist, the proof has 1 lines',
      });

      await user.click(screen.getByRole('button', { name: 'Undo last step' }));

      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2));
      const [, init] = applyRequests()[applyRequests().length - 1];
      const body = JSON.parse(init.body);
      expect(body.proofDto.steps).toEqual([repProof.steps[0]]);
      expect(body.actionDto.name).toEqual('COPY');
      expect(body.actionDto.sources).toEqual([2]); // one past the end of the trimmed (1-step) proof
      // Undoing down to only the premise disables the button again and hands the focus to New Proof.
      expect(screen.getByRole('button', { name: 'Undo last step' })).toBeDisabled();
      expect(screen.getByRole('button', { name: /Start a new proof/i })).toHaveFocus();
    });

    test('a network error is shown and the proof is kept', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));

      fetchMock.mockImplementation(async (url: string) =>
        url.endsWith('/actions') ? jsonResponse(200, [REP]) : Promise.reject(new Error('offline')));

      await user.click(screen.getByRole('button', { name: 'Undo last step' }));

      expect(await screen.findByRole('alert')).toHaveTextContent(/network error/i);
      expect(screen.getAllByRole('row')).toHaveLength(3);
    });

    test('a stale response cannot overwrite a newer proof started while it was in flight', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(applyRequests()).toHaveLength(1));

      // The undo request never resolves on its own; it is resolved by hand below, after New Proof has already
      // replaced the proof on screen.
      let resolveUndo: (response: Response) => void = () => {};
      fetchMock.mockImplementation(async (url: string) =>
        url.endsWith('/actions')
          ? jsonResponse(200, [REP])
          : new Promise<Response>((resolve) => { resolveUndo = resolve; }));

      await user.click(screen.getByRole('button', { name: 'Undo last step' }));
      await waitFor(() => expect(applyRequests()).toHaveLength(2));

      await startProof(user, 'Q', 'Q');
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2)); // header row + the new premise, Q

      // The stale undo response now arrives, for the discarded P |- P proof.
      resolveUndo(jsonResponse(202, {
        proof: { steps: [repProof.steps[0]], logic: 'classical', goal: 'P' },
        success: false,
        done: false,
        message: 'Line 2 does not exist, the proof has 1 lines',
      }));

      // It must neither replace the new proof nor show an error about the old one.
      await waitFor(() => expect(screen.getByRole('button', { name: 'Undo last step' })).toBeDisabled());
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      expect(screen.getAllByRole('row')).toHaveLength(2);
    });
  });

  describe('copying a proof as text and loading it back', () => {
    // P -> P, proved with a subproof, so that the text has an indented line.
    const solved = {
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P -> P', rule: '->I [1-1]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'P -> P',
      done: true,
    };
    // A backend that solves to `solved` and replays a proof (the out-of-range COPY) unchanged; the proof is done when
    // a top-level step is its goal, as the domain's `Proof.isDone()` decides.
    const mockRoundTripBackend = () => {
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, []);
        if (url.endsWith('/solve')) return jsonResponse(200, solved);
        const { proofDto } = JSON.parse(String(init!.body));
        const done = proofDto.steps.some((step: { assmsLevel: number, expression: string }) =>
          step.assmsLevel === 0 && step.expression === proofDto.goal);
        return jsonResponse(202, { proof: proofDto, success: false, done, message: 'out of range' });
      });
    };

    const proofTextInput = () => screen.getByLabelText(/Proof text/i);

    // Opens the New Proof dialog (discarding the current proof, if asked) and puts the focus on the proof text, once
    // the dialog has put it on its first premise.
    const openLoadFromText = async (user: ReturnType<typeof userEvent.setup>) => {
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      const confirm = screen.queryByRole('button', { name: 'Discard and start new' });
      if (confirm) await user.click(confirm);
      await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());
      await user.click(proofTextInput());
    };

    test('there is nothing to copy before a proof has steps', () => {
      render(<App />);
      expect(screen.getByRole('button', { name: 'Copy proof as text' })).toBeDisabled();
    });

    test('a proof copied as text loads back as the same proof', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'Q', 'P -> P');
      await user.click(await screen.findByRole('button', { name: 'Solve' }));
      expect(await screen.findByRole('status')).toHaveTextContent('Proof complete.');

      // Copy (to the clipboard stub that user-event installs)
      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
      expect(await screen.findByText(/Proof copied to the clipboard as text/)).toHaveTextContent('with the goal P -> P');
      const copied = await navigator.clipboard.readText();
      expect(copied).toBe('   P           Ass\nP -> P           ->I [1-1]');

      // Paste into a new proof, with its goal, and load it
      await openLoadFromText(user);
      await user.paste();
      expect(proofTextInput()).toHaveValue(copied);
      await user.type(screen.getByLabelText(/Goal of the loaded proof/i), 'P -> P');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      // The steps were replayed by the backend, and the dialog closed on the loaded proof.
      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
      const [, replay] = applyRequests()[applyRequests().length - 1];
      expect(JSON.parse(replay.body).proofDto).toEqual({ steps: solved.steps, logic: 'classical', goal: 'P -> P' });
      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');

      // Copying the loaded proof gives the same text again.
      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
      expect(await navigator.clipboard.readText()).toBe(copied);
    });

    test('an unfinished proof loads with its goal, not as a finished proof of its last line', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);

      await openLoadFromText(user);
      await user.paste('Q           Ass');
      expect(screen.getByRole('button', { name: 'Load proof' })).toBeDisabled();
      await user.type(screen.getByLabelText(/Goal of the loaded proof/i), 'P -> Q');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
      const [, replay] = applyRequests()[0];
      expect(JSON.parse(replay.body).proofDto.goal).toBe('P -> Q');
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
    });

    test('a proof that ends inside an open subproof loads back', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);

      await openLoadFromText(user);
      await user.paste('Q           Ass\n   P           Ass');
      await user.type(screen.getByLabelText(/Goal of the loaded proof/i), 'P -> P');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
    });

    test('premises typed with spaces around them are copied without the spaces', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, '   Q ', 'P -> P');

      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));

      await screen.findByText(/Proof copied to the clipboard as text/);
      expect(await navigator.clipboard.readText()).toBe('Q           Ass');
    });

    test('a text the backend rejects keeps the dialog open with the reason', async () => {
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [])
        : jsonResponse(400, { message: 'Line 1 is not valid: unknown rule' }));
      const user = userEvent.setup();
      render(<App />);

      await openLoadFromText(user);
      await user.paste('P           Nope');
      await user.type(screen.getByLabelText(/Goal of the loaded proof/i), 'P');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      expect(await screen.findByRole('alert')).toHaveTextContent('Line 1 is not valid: unknown rule');
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(proofTextInput()).toHaveAttribute('aria-invalid', 'true');
      expect(proofTextInput()).toHaveFocus();
    });

    test('without a clipboard the text is shown to be copied by hand', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      // After setup, which installs a clipboard stub of its own.
      Object.defineProperty(navigator, 'clipboard', { value: undefined, configurable: true });
      render(<App />);
      await startProof(user, 'Q', 'P -> P');
      await user.click(await screen.findByRole('button', { name: 'Solve' }));
      await screen.findByText('Proof complete.');

      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));

      expect(await screen.findByText(/clipboard is not available/)).toBeInTheDocument();
      expect(screen.getByLabelText('Proof as text')).toHaveValue('   P           Ass\nP -> P           ->I [1-1]');
    });
  });
});
