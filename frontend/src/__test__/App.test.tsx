import { act, cleanup, render, screen, waitFor } from '@testing-library/react';
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
    // The proof on screen is saved in sessionStorage, which jsdom keeps from one test to the next.
    window.sessionStorage.clear();
  });

  const actionRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/actions'));
  const applyRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/action'));
  // A replay is the out-of-range COPY (see `replayProof`) that New Proof, "Try an example" and Undo send.
  const isReplay = (init?: RequestInit) => JSON.parse(String(init?.body)).actionDto?.name === 'COPY';
  const replayRequests = () => applyRequests().filter(([, init]) => isReplay(init));
  // The requests that apply a rule, leaving out the replays.
  const ruleRequests = () => applyRequests().filter(([, init]) => !isReplay(init));

  // The backend's answer to a replay: the proof unchanged. With `judgeDone` it is done when a top-level step is its goal,
  // as the domain's `Proof.isDone()` decides; without it, it is never done, so that a proof like P |- P, which the
  // domain counts as done from the start, still takes a rule in the tests about applying rules.
  const replayAnswer = (init?: RequestInit, judgeDone = false) => {
    const { proofDto } = JSON.parse(String(init!.body));
    const done = judgeDone && proofDto.steps.some((step: { assmsLevel: number, expression: string }) =>
      step.assmsLevel === 0 && step.expression === proofDto.goal);
    return jsonResponse(202, { proof: proofDto, success: false, done, message: 'out of range' });
  };

  // The backend: the list of actions, a replay that accepts the proof as it is (see `replayAnswer`), and one answer to
  // every other request (apply or solve).
  const mockBackend = (actions: ActionDescriptor[], status: number, body: unknown, judgeDone = false) => {
    fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
      if (url.endsWith('/actions')) return jsonResponse(200, actions);
      if (url.endsWith('/action') && isReplay(init)) return replayAnswer(init, judgeDone);
      return jsonResponse(status, body);
    });
  };

  // Opens the New Proof dialog, fills in one premise and the goal, and clicks Start Proof. Confirms discarding the
  // current proof first, when there is one with more than its premises (the dedicated tests for that confirmation
  // exercise it directly).
  const submitNewProof = async (user: ReturnType<typeof userEvent.setup>, premise: string, goal: string) => {
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    const confirm = screen.queryByRole('button', { name: 'Discard and start new' });
    if (confirm) await user.click(confirm);
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    await user.type(screen.getByPlaceholderText('Premise 1'), premise);
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), goal);
    await user.click(screen.getByText('Start Proof'));
  };

  // The dialog closes once the backend has accepted the new proof.
  const dialogClosed = () => waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());

  // Starts a proof with one premise, and waits for the backend to accept it.
  const startProof = async (user: ReturnType<typeof userEvent.setup>, premise: string, goal: string) => {
    await submitNewProof(user, premise, goal);
    await dialogClosed();
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

    expect(ruleRequests()).toHaveLength(0);
    expect(screen.queryByText(/does not exist/)).not.toBeInTheDocument();
  });

  test('a valid line number is sent as a number, and the inputs start over afterwards', async () => {
    mockBackend([REP], 200, { proof: repProof, success: true, message: '' });
    const user = await startWithRep();
    await user.type(screen.getByLabelText(/Line number:/i), '1');

    await user.click(applyButton());

    await waitFor(() => expect(screen.getByLabelText(/Line number:/i)).toHaveValue(''));
    const [, init] = ruleRequests()[0];
    expect(JSON.parse(init.body).actionDto).toEqual({ name: 'Rep', sources: [1], extraParameters: { expression: '' } });
    expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('Rep');
    expect(applyButton()).toBeDisabled();
  });

  test('an unparsable premise or goal never becomes a proof line', async () => {
    mockBackend([], 200, {});
    const user = userEvent.setup();
    render(<App />);

    await submitNewProof(user, 'p ->', 'q &&');

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getAllByRole('alert')).toHaveLength(2);
    expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
    expect(screen.queryByText('p ->')).not.toBeInTheDocument();
    expect(applyRequests()).toHaveLength(0);
  });

  describe('the backend checks a new proof before it is shown', () => {
    test('a formula checkFormula accepts but the backend rejects keeps the dialog open with its message', async () => {
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [])
        : jsonResponse(400, { message: 'The proof could not be read, check its expressions and rules' }));
      const user = userEvent.setup();
      render(<App />);

      await submitNewProof(user, 'P', 'Q');

      expect(await screen.findByRole('alert')).toHaveTextContent('The proof could not be read, check its expressions and rules');
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      await waitFor(() => expect(screen.getByRole('button', { name: 'Start Proof' })).toHaveFocus());
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      // The premises-only proof went through the replay, the path Undo uses.
      const [[, init]] = applyRequests();
      expect(JSON.parse(init.body)).toEqual({
        actionDto: { name: 'COPY', sources: [2], extraParameters: {} },
        proofDto: {
          steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
          logic: 'classical',
          goal: 'Q',
        },
      });
    });

    test('an accepted proof is the one the backend returned, with its done verdict', async () => {
      // The backend writes the formulas in its own way, and a premise that is the goal already proves it.
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [])
        : jsonResponse(202, {
          proof: {
            steps: [{ expression: 'p -> q', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
            logic: 'classical',
            goal: 'p -> q',
          },
          success: false,
          done: true,
          message: 'Line 2 does not exist, the proof has 1 lines',
        }));
      const user = userEvent.setup();
      render(<App />);

      await startProof(user, 'p->q', 'p->q');

      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
      await screen.findByText(/Proof copied to the clipboard as text/);
      expect(await navigator.clipboard.readText()).toBe('p -> q           Ass');
    });

    test('a late answer after Cancel is ignored', async () => {
      let answer: (response: Response) => void = () => {};
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [])
        : new Promise<Response>((resolve) => { answer = resolve; }));
      const user = userEvent.setup();
      render(<App />);

      await submitNewProof(user, 'P', 'P');
      expect(await screen.findByRole('button', { name: 'Starting…' })).toBeDisabled();
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

      answer(jsonResponse(202, {
        proof: { steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }], logic: 'classical', goal: 'P' },
        success: false,
        done: true,
        message: 'Line 2 does not exist, the proof has 1 lines',
      }));

      // Give the answer every chance to land: it must not replace the empty state.
      await new Promise((resolve) => setTimeout(resolve, 0));
      await new Promise((resolve) => setTimeout(resolve, 0));
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(screen.queryByRole('row')).not.toBeInTheDocument();
    });

    test('"Try an example" the backend cannot check stays on the empty state with the reason', async () => {
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [])
        : Promise.reject(new Error('offline')));
      const user = userEvent.setup();
      render(<App />);

      await user.click(screen.getByRole('button', { name: 'Try an example' }));

      expect(await screen.findByRole('alert')).toHaveTextContent(/network error/i);
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Try an example' })).toBeEnabled();
    });
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

  test('the empty state explains the three steps of a proof', () => {
    mockBackend([], 200, {});
    render(<App />);

    expect(screen.getByRole('heading', { name: 'How it works' })).toBeInTheDocument();
    const steps = screen.getAllByRole('listitem').map((item) => item.textContent);
    expect(steps).toEqual([
      'Enter the premises and the goal of the proof.',
      'Pick an inference rule.',
      'Enter the line numbers the rule uses, and apply it.',
    ]);
  });

  test('"Try an example" hands the focus to the New Proof button, since the empty state it lives in goes away', async () => {
    mockBackend([], 200, {});
    const user = userEvent.setup();
    render(<App />);

    screen.getByRole('button', { name: 'Try an example' }).focus();
    await user.keyboard('{Enter}');

    await waitFor(() => expect(screen.queryByRole('button', { name: /Try an example/ })).not.toBeInTheDocument());
    expect(screen.getByRole('button', { name: /Start a new proof/i })).toHaveFocus();
  });

  test('"Try an example" starts p -> q, p |- q in one click, the same proof the New Proof dialog would start', async () => {
    const MP: ActionDescriptor = { name: 'MP', params: ['INT', 'INT'] };
    mockBackend([MP], 202, { proof: {}, success: false, message: 'Rule not applicable' });
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Try an example' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText(/No proof loaded/)).not.toBeInTheDocument());
    expect(screen.queryByRole('button', { name: /Try an example/ })).not.toBeInTheDocument();
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'MP');
    const [first, second] = screen.getAllByLabelText(/Line number:/i);
    await user.type(first, '1');
    await user.type(second, '2');
    await user.click(applyButton());

    await waitFor(() => expect(ruleRequests()).toHaveLength(1));
    const exampleProof = JSON.parse(ruleRequests()[0][1].body).proofDto;
    const examplePremises = {
      steps: [
        { expression: 'p -> q', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'p', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'q',
    };
    // The backend checked the example before it was shown, through the same replay as a typed proof, and the proof
    // shown is the one it answered with, its verdict included.
    expect(JSON.parse(replayRequests()[0][1].body).proofDto).toEqual(examplePremises);
    expect(exampleProof).toEqual({ ...examplePremises, done: false });

    // The same premises and goal typed in the New Proof dialog give the very same proof.
    await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    await user.type(screen.getByPlaceholderText('Premise 1'), 'p -> q');
    await user.click(screen.getByText('+ Add Premise'));
    await user.type(screen.getByPlaceholderText('Premise 2'), 'p');
    await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'q');
    await user.click(screen.getByText('Start Proof'));
    await dialogClosed();
    await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'MP');
    const [again1, again2] = screen.getAllByLabelText(/Line number:/i);
    await user.type(again1, '1');
    await user.type(again2, '2');
    await user.click(applyButton());

    await waitFor(() => expect(ruleRequests()).toHaveLength(2));
    expect(JSON.parse(ruleRequests()[1][1].body).proofDto).toEqual(exampleProof);
    expect(JSON.parse(replayRequests()[1][1].body).proofDto).toEqual(examplePremises);
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
      await dialogClosed();
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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));
      expect(JSON.parse(ruleRequests()[0][1].body).actionDto.sources).toEqual([1, 2]);
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
      await dialogClosed();

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));
      expect(screen.getAllByRole('row')).toHaveLength(3);

      // The backend, once it replays only the premise, reports it not done: a fresh mock for the undo request.
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions') ? jsonResponse(200, [REP]) : jsonResponse(202, {
        proof: { steps: [repProof.steps[0]], logic: 'classical', goal: 'P' },
        success: false,
        done: false,
        message: 'Line 2 does not exist, the proof has 1 lines',
      }));

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));

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
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));

      // The undo request never resolves on its own; it is resolved by hand below, after New Proof has already
      // replaced the proof on screen. The replay that checks that new proof answers right away.
      let resolveUndo: ((response: Response) => void) | null = null;
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, [REP]);
        if (resolveUndo === null) return new Promise<Response>((resolve) => { resolveUndo = resolve; });
        return replayAnswer(init);
      });

      await user.click(screen.getByRole('button', { name: 'Undo last step' }));
      await waitFor(() => expect(resolveUndo).not.toBeNull());

      await startProof(user, 'Q', 'Q');
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2)); // header row + the new premise, Q

      // The stale undo response now arrives, for the discarded P |- P proof.
      resolveUndo!(jsonResponse(202, {
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
    const mockRoundTripBackend = () => mockBackend([], 200, solved, true);

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

  describe('keeping the proof across a page reload', () => {
    const SAVED_PROOF_KEY = 'natural-deduction.proof';
    const savedProof = () => JSON.parse(window.sessionStorage.getItem(SAVED_PROOF_KEY) ?? 'null');
    // Starts a proof with one premise on a page that is already rendered.
    const startProofOnPage = (premise: string, goal: string) => startProof(userEvent.setup(), premise, goal);

    test('reloading mid-proof shows the same proof with the same done state, checked again by the backend', async () => {
      mockBackend([REP], 200, { proof: repProof, success: true, done: true, message: '' });
      const user = await startWithRep();
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
      // Only the steps and the goal are saved; `done` is asked for again.
      expect(savedProof()).toEqual(repProof);

      // A reload: the page starts from nothing but the saved proof.
      cleanup();
      fetchMock.mockClear();
      mockBackend([REP], 200, {}, true);
      render(<App />);

      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
      expect(replayRequests()).toHaveLength(1);
      expect(JSON.parse(replayRequests()[0][1].body)).toEqual({
        actionDto: { name: 'COPY', sources: [3], extraParameters: {} },
        proofDto: repProof,
      });
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    test('an unfinished saved proof comes back unfinished, and the page says it is being restored meanwhile', async () => {
      const unfinished = { steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }], logic: 'classical', goal: 'Q' };
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(unfinished));
      let answer: () => void = () => {};
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => url.endsWith('/actions')
        ? jsonResponse(200, [REP])
        : new Promise<Response>((resolve) => { answer = () => resolve(replayAnswer(init, true)); }));
      render(<App />);

      expect(screen.getByRole('status')).toHaveTextContent('Restoring the proof from before the page was reloaded');
      await waitFor(() => expect(replayRequests()).toHaveLength(1));
      await act(async () => answer());

      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2));
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
      expect(savedProof()).toEqual(unfinished);
    });

    test.each([
      ['text that is not JSON', '{"steps": ['],
      ['JSON that is not a proof', JSON.stringify({ steps: 'P', logic: 'classical', goal: 'P' })],
      ['a step without a rule', JSON.stringify({ steps: [{ expression: 'P', assmsLevel: 0, extraParameters: {} }], logic: 'classical', goal: 'P' })],
      ['a proof of another logic', JSON.stringify({ ...repProof, logic: 'modal' })],
    ])('a corrupt saved proof (%s) is discarded and the empty state says so', async (_, text) => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, text);
      mockBackend([REP], 200, {});
      render(<App />);

      expect(await screen.findByRole('alert')).toHaveTextContent('could not be read, so it was discarded');
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(window.sessionStorage.getItem(SAVED_PROOF_KEY)).toBeNull();
      expect(applyRequests()).toHaveLength(0);
    });

    test('a saved proof the backend rejects is discarded and the empty state gives the reason', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      fetchMock.mockImplementation(async (url: string) => url.endsWith('/actions')
        ? jsonResponse(200, [REP])
        : jsonResponse(400, { message: 'The proof could not be read, check its expressions and rules' }));
      render(<App />);

      expect(await screen.findByRole('alert')).toHaveTextContent(
        'could not be restored: The proof could not be read, check its expressions and rules');
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(screen.queryByRole('row')).not.toBeInTheDocument();
      expect(window.sessionStorage.getItem(SAVED_PROOF_KEY)).toBeNull();

      // The notice goes away with the next proof.
      mockBackend([REP], 200, {});
      await startProofOnPage('P', 'P');
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    test('a storage that throws is ignored: nothing is restored, and proofs still work', async () => {
      const blocked = () => { throw new DOMException('The operation is insecure.', 'SecurityError'); };
      const spies = [
        jest.spyOn(Storage.prototype, 'getItem').mockImplementation(blocked),
        jest.spyOn(Storage.prototype, 'setItem').mockImplementation(blocked),
        jest.spyOn(Storage.prototype, 'removeItem').mockImplementation(blocked),
      ];
      try {
        mockBackend([REP], 200, {});
        render(<App />);

        expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
        await startProofOnPage('P', 'Q');
        expect(screen.getAllByRole('row')).toHaveLength(2);
        expect(spies[1]).toHaveBeenCalled();
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      } finally {
        spies.forEach((spy) => spy.mockRestore());
      }
    });

    test('a new proof replaces the saved one', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      mockBackend([REP], 200, {});
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));

      await startProof(user, 'Q', 'Q');

      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2));
      expect(savedProof()).toEqual({
        steps: [{ expression: 'Q', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
        logic: 'classical',
        goal: 'Q',
      });
    });

    test('"Try an example" while the saved proof is still being restored replaces it, and the late answer is dropped', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      let answerRestore: () => void = () => {};
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, [REP]);
        if (JSON.parse(String(init!.body)).proofDto.goal === repProof.goal) {
          return new Promise<Response>((resolve) => { answerRestore = () => resolve(replayAnswer(init, true)); });
        }
        return replayAnswer(init);
      });
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(replayRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: 'Try an example' }));
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      const example = savedProof();
      expect(example.goal).toBe('q');

      await act(async () => {
        answerRestore();
        await new Promise((resolve) => setTimeout(resolve, 0));
        await new Promise((resolve) => setTimeout(resolve, 0));
      });
      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(savedProof()).toEqual(example);
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
    });
  });
});
