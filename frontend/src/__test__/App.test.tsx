import { act, cleanup, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import { clearActionsCache, clearExercisesCache } from '../service/actions';
import { ActionDescriptor, Exercise } from '../types';

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
    const solvedText = '   P           Ass\nP -> P           ->I [1-1]';
    const UNFINISHED = 'The proof is invalid: it does not end at the top level';
    // The text of the `file` part of a `POST .../proof` request.
    const sentFile = (init?: RequestInit) => new Promise<string>((resolve) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result));
      reader.readAsText((init!.body as FormData).get('file') as Blob);
    });
    const proofRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/proof'));
    // A backend that solves to `solved`, replays a proof (the out-of-range COPY) unchanged (the proof is done when a
    // top-level step is its goal, as the domain's `Proof.isDone()` decides), and reads back the text of `solved` as
    // a proof file; any other text is an unfinished proof, which `POST .../proof` rejects.
    const mockRoundTripBackend = () => {
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, []);
        if (url.endsWith('/action')) return replayAnswer(init, true);
        if (url.endsWith('/proof')) {
          return (await sentFile(init)) === solvedText ? jsonResponse(201, { ...solved }) : jsonResponse(400, { message: UNFINISHED });
        }
        return jsonResponse(200, solved);
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

    test('a finished proof copied as text loads back as the same proof, with its goal and nothing else to type', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'Q', 'P -> P');
      await user.click(await screen.findByRole('button', { name: 'Solve' }));
      expect(await screen.findByRole('status')).toHaveTextContent('Proof complete.');

      // Copy (to the clipboard stub that user-event installs)
      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
      const notice = await screen.findByText(/Proof copied to the clipboard as text/);
      expect(notice).toHaveTextContent('To load it again, paste it in the New Proof dialog.');
      expect(notice).not.toHaveTextContent(/goal/);
      const copied = await navigator.clipboard.readText();
      expect(copied).toBe(solvedText);

      // Paste into a new proof and load it: there is no goal to type.
      await openLoadFromText(user);
      await user.paste();
      expect(proofTextInput()).toHaveValue(copied);
      expect(screen.queryByLabelText(/Goal of the loaded proof/i)).not.toBeInTheDocument();
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      // The text went to the backend's proof-file endpoint, and the dialog closed on the proof it answered with.
      await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
      expect(proofRequests()).toHaveLength(1);
      expect(await sentFile(proofRequests()[0][1])).toBe(copied);
      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');

      // Copying the loaded proof gives the same text again.
      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));
      expect(await navigator.clipboard.readText()).toBe(copied);
    });

    test('copying an unfinished proof that ends at the top level says it loads back as a proof of its last line', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'Q', 'P -> P');

      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));

      const notice = await screen.findByText(/Proof copied to the clipboard as text/);
      expect(notice).toHaveTextContent('It is not finished, so it loads back in the New Proof dialog as a proof of its last line Q instead of the goal P -> P.');
      expect(notice).not.toHaveTextContent('To load it again');
    });

    test('copying an unfinished proof that ends inside a subproof says that its text only loads back once the proof is finished', async () => {
      // Kept from before a page reload: the premise Q, then an open assumption P.
      const openAssumption = {
        steps: [
          { expression: 'Q', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
          { expression: 'P', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        ],
        logic: 'classical',
        goal: 'P -> P',
      };
      window.sessionStorage.setItem('natural-deduction.proof', JSON.stringify(openAssumption));
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));

      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));

      const notice = await screen.findByText(/Proof copied to the clipboard as text/);
      expect(notice).toHaveTextContent('It only loads back in the New Proof dialog once the proof is finished.');
      expect(notice).not.toHaveTextContent(/goal/);
    });

    test('copying a done proof whose last line is not the goal says the goal changes when it is loaded back', async () => {
      // Done (a top-level step, the premise, is the goal), but the last line, which `POST .../proof` takes as the goal,
      // is another formula.
      const pastGoal = {
        steps: [
          { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
          { expression: 'P | Q', rule: '|I [1]', assmsLevel: 0, extraParameters: {} },
        ],
        logic: 'classical',
        goal: 'P',
      };
      // Such a proof, kept from before a page reload (a proof done from its first line cannot be extended in the UI).
      window.sessionStorage.setItem('natural-deduction.proof', JSON.stringify(pastGoal));
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');

      await user.click(screen.getByRole('button', { name: 'Copy proof as text' }));

      const notice = await screen.findByText(/Proof copied to the clipboard as text/);
      expect(notice).toHaveTextContent('Its last line is not the goal, so it loads back in the New Proof dialog with the goal P | Q instead of P.');
      expect(notice).not.toHaveTextContent('To load it again');
    });

    test('an unfinished proof is rejected with the reason, and the proof on screen stays', async () => {
      mockRoundTripBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'Q', 'P -> P');

      await openLoadFromText(user);
      await user.paste('Q           Ass\n   P           Ass');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));

      expect(await screen.findByRole('alert')).toHaveTextContent(UNFINISHED);
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(proofTextInput()).toHaveAttribute('aria-invalid', 'true');
      expect(proofTextInput()).toHaveFocus();

      // Cancelling goes back to the proof that was on screen.
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      await dialogClosed();
      expect(screen.getAllByRole('row')).toHaveLength(2); // header row + the premise, Q
    });

    test('a load answer arriving after the dialog was cancelled is dropped', async () => {
      let answerLoad: ((response: Response) => void) | null = null;
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, []);
        if (url.endsWith('/proof')) return new Promise<Response>((resolve) => { answerLoad = resolve; });
        return replayAnswer(init, true);
      });
      const user = userEvent.setup();
      render(<App />);

      await openLoadFromText(user);
      await user.paste(solvedText);
      await user.click(screen.getByRole('button', { name: 'Load proof' }));
      await waitFor(() => expect(answerLoad).not.toBeNull());
      expect(proofTextInput()).toHaveAttribute('readonly');
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      await dialogClosed();

      // The answer, a proof the backend accepted, arrives late; let it go through the whole chain.
      await act(async () => {
        answerLoad!(jsonResponse(201, { ...solved }));
        await new Promise((resolve) => setTimeout(resolve, 0));
      });

      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(screen.queryAllByRole('row')).toHaveLength(0);
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
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

    test.each([
      ['the backend cannot be reached', () => Promise.reject(new TypeError('Failed to fetch')), 'Network error'],
      ['a server error', async () => jsonResponse(500, { message: 'Internal error' }), 'Internal error'],
      ['a busy server', async () => jsonResponse(429, { message: 'Too many requests' }), 'Too many requests'],
    ])('a saved proof that cannot be replayed because of %s is kept for the next reload', async (_, replay, reason) => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      jest.spyOn(console, 'error').mockImplementation(() => {});
      try {
        fetchMock.mockImplementation((url: string) => url.endsWith('/actions')
          ? Promise.resolve(jsonResponse(200, [REP]))
          : replay());
        render(<App />);

        const alert = await screen.findByRole('alert');
        expect(alert).toHaveTextContent(reason);
        expect(alert).toHaveTextContent('It is still saved: reload the page to try again.');
        expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
        expect(savedProof()).toEqual(repProof);

        // The next reload, with the backend back, restores it.
        cleanup();
        fetchMock.mockReset();
        mockBackend([REP], 200, {}, true);
        render(<App />);
        await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      } finally {
        (console.error as jest.Mock).mockRestore();
      }
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

    // A backend whose replay of the saved proof (goal P) waits for `answerRestore`, and whose other replays (the
    // example's, or a new proof's) wait for `answerOthers` when `holdOthers` is set.
    const slowRestoreBackend = (holdOthers: boolean) => {
      const answers = { answerRestore: () => {}, answerOthers: () => {} };
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, [REP]);
        if (JSON.parse(String(init!.body)).proofDto.goal === repProof.goal) {
          return new Promise<Response>((resolve) => { answers.answerRestore = () => resolve(replayAnswer(init, true)); });
        }
        if (!holdOthers) return replayAnswer(init);
        return new Promise<Response>((resolve) => { answers.answerOthers = () => resolve(replayAnswer(init)); });
      });
      return answers;
    };
    const flush = () => act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
      await new Promise((resolve) => setTimeout(resolve, 0));
    });

    test('"Try an example" still wins when the restore answers before the example does', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      const answers = slowRestoreBackend(true);
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(replayRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: 'Try an example' }));
      await waitFor(() => expect(replayRequests()).toHaveLength(2));
      await act(async () => answers.answerRestore());
      await flush();
      expect(screen.queryByRole('row')).not.toBeInTheDocument();

      await act(async () => answers.answerOthers());
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      expect(savedProof().goal).toBe('q');
      expect(screen.queryByText('Proof complete.')).not.toBeInTheDocument();
    });

    test('a restore answering while the New Proof dialog is open does not replace the proof started from it', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      const answers = slowRestoreBackend(false);
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(replayRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      await act(async () => answers.answerRestore());
      await flush();
      expect(screen.queryByRole('row')).not.toBeInTheDocument();

      await user.type(screen.getByPlaceholderText('Premise 1'), 'Q');
      await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'Q');
      await user.click(screen.getByText('Start Proof'));
      await dialogClosed();
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2));
      expect(savedProof().goal).toBe('Q');
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    test('a restore answering while the New Proof dialog is open is not shown after Cancel, and stays saved', async () => {
      window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(repProof));
      const answers = slowRestoreBackend(false);
      const user = userEvent.setup();
      render(<App />);
      await waitFor(() => expect(replayRequests()).toHaveLength(1));

      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      await act(async () => answers.answerRestore());
      await flush();
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      await dialogClosed();

      expect(screen.queryByRole('row')).not.toBeInTheDocument();
      expect(screen.getByText(/No proof loaded/)).toBeInTheDocument();
      expect(screen.getByRole('alert')).toHaveTextContent('It is still saved: reload the page to restore it.');
      expect(savedProof()).toEqual(repProof);
    });
  });

  describe('exercises', () => {
    const EXERCISES: Exercise[] = [
      { id: 'first', title: 'First', premises: ['P'], goal: 'P', difficulty: 'EASY' },
      { id: 'second', title: 'Second', premises: ['Q'], goal: 'Q', difficulty: 'MEDIUM' },
      { id: 'third', title: 'Third', premises: [], goal: 'R | (- R)', difficulty: 'HARD' },
    ];
    const exerciseRequests = () => fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/exercises'));

    // The backend: the Rep rule, the exercises (or `exercisesAnswer`), a replay that accepts the proof as it is and never
    // says it is done (or `replay`), and a rule that finishes the proof P |- P.
    const mockExerciseBackend = (
      exercisesAnswer: () => Response = () => jsonResponse(200, EXERCISES),
      replay: (init?: RequestInit) => Response = (init) => replayAnswer(init),
    ) => {
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, [REP]);
        if (url.endsWith('/exercises')) return exercisesAnswer();
        if (url.endsWith('/action') && isReplay(init)) return replay(init);
        return jsonResponse(200, { proof: repProof, success: true, done: true, message: '' });
      });
    };

    // The line above the proof that names its exercise.
    const currentExercise = () => document.querySelector('.current-exercise');
    const exerciseItem = (title: string) => screen.getByText(title, { selector: '.exercise-title' }).closest('li')!;

    const startExercise = async (user: ReturnType<typeof userEvent.setup>, title: string) => {
      await user.click(await screen.findByRole('button', { name: `Start exercise ${title}` }));
      await waitFor(() => expect(currentExercise()).toHaveTextContent(`Exercise: ${title}`));
    };

    // Applies Rep to line 1, which the backend answers with the finished proof P |- P.
    const finishWithRep = async (user: ReturnType<typeof userEvent.setup>) => {
      await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Proof complete.'));
    };

    beforeEach(() => {
      clearExercisesCache();
      window.localStorage.clear();
    });

    test('the list is reachable from the empty state and from the toolbar, and groups the exercises by difficulty', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      // Nothing is fetched before the list is asked for.
      expect(exerciseRequests()).toHaveLength(0);

      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));

      const easy = await screen.findByRole('region', { name: 'Easy' });
      expect(within(easy).getByText('First')).toBeInTheDocument();
      expect(within(easy).getByText('P ⊢ P')).toBeInTheDocument();
      expect(within(screen.getByRole('region', { name: 'Medium' })).getByText('Second')).toBeInTheDocument();
      expect(within(screen.getByRole('region', { name: 'Hard' })).getByText('⊢ R | (- R)')).toBeInTheDocument();
      expect(screen.getByText('Solved 0 of 3.')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveAttribute('aria-expanded', 'true');

      await user.click(screen.getByRole('button', { name: 'Close exercises' }));
      expect(screen.queryByRole('region', { name: 'Exercises' })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveFocus();

      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      expect(await screen.findByRole('region', { name: 'Exercises' })).toBeInTheDocument();
      // The list is fetched once.
      expect(exerciseRequests()).toHaveLength(1);
      expect(exerciseRequests()[0][0]).toBe('/logic/classical/exercises');
    });

    test('"Browse exercises" hands the focus to the list it opens, since the button goes away', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);

      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));

      expect(screen.queryByRole('button', { name: 'Browse exercises' })).not.toBeInTheDocument();
      expect(screen.getByRole('heading', { name: 'Exercises' })).toHaveFocus();
      // Opening it from the toolbar keeps the focus on the toolbar button, which stays.
      await user.click(screen.getByRole('button', { name: 'Close exercises' }));
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveFocus();
    });

    test('a logic without exercises says so', async () => {
      mockExerciseBackend(() => jsonResponse(200, []));
      const user = userEvent.setup();
      render(<App />);

      await user.click(screen.getByRole('button', { name: 'Exercises' }));

      expect(await screen.findByText('There are no exercises for this logic yet.')).toBeInTheDocument();
    });

    test('exercises that could not be fetched are fetched again when the list is opened again', async () => {
      let fail = true;
      mockExerciseBackend(() => fail ? jsonResponse(500, { message: 'Something went wrong' }) : jsonResponse(200, EXERCISES));
      const user = userEvent.setup();
      render(<App />);

      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      expect(await screen.findByRole('alert')).toHaveTextContent('The exercises could not be loaded: Something went wrong');

      fail = false;
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      await user.click(screen.getByRole('button', { name: 'Exercises' }));

      expect(await screen.findByRole('region', { name: 'Easy' })).toBeInTheDocument();
      expect(exerciseRequests()).toHaveLength(2);
    });

    test('starting an exercise has the backend check it, then shows its premises and goal', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));

      await startExercise(user, 'Second');

      expect(replayRequests()).toHaveLength(1);
      expect(JSON.parse(String(replayRequests()[0][1]!.body)).proofDto).toEqual({
        steps: [{ expression: 'Q', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
        logic: 'classical',
        goal: 'Q',
      });
      const rows = screen.getAllByRole('row');
      expect(rows).toHaveLength(2);
      expect(rows[1]).toHaveTextContent('Q');
      expect(screen.getByText('GOAL:').parentElement).toHaveTextContent('Q');
      // The list closes, and hands the focus to its toolbar button.
      expect(screen.queryByRole('region', { name: 'Exercises' })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveFocus();
    });

    test('an exercise the backend refuses is not shown, and the list says why', async () => {
      mockExerciseBackend(undefined, () => jsonResponse(400, { message: 'Cannot parse R | (- R)' }));
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));

      await user.click(await screen.findByRole('button', { name: 'Start exercise Third' }));

      expect(await screen.findByRole('alert')).toHaveTextContent('The exercise "Third" could not be started: Cannot parse R | (- R)');
      expect(screen.queryAllByRole('row')).toHaveLength(0);
      expect(screen.getByRole('region', { name: 'Exercises' })).toBeInTheDocument();
    });

    // Starts the proof P |- P, then the exercise "Second", whose check by the backend is held until the returned
    // function is called; meanwhile Rep is applied in the Menu, which is still usable, then `beforeAnswer` runs.
    const applyRuleWhileExerciseStarts = async (beforeAnswer?: (user: ReturnType<typeof userEvent.setup>) => Promise<void>) => {
      let answerExercise: (() => void) | null = null;
      mockExerciseBackend(undefined, (init) => {
        if (JSON.parse(String(init!.body)).proofDto.goal !== 'Q') return replayAnswer(init);
        return new Promise<Response>((resolve) => { answerExercise = () => resolve(replayAnswer(init)); }) as unknown as Response;
      });
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'P', 'P');
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      // Only the premises are on screen, so starting the exercise does not ask.
      await user.click(await screen.findByRole('button', { name: 'Start exercise Second' }));
      await waitFor(() => expect(answerExercise).not.toBeNull());
      await finishWithRep(user);
      expect(screen.getAllByRole('row')).toHaveLength(3);
      await beforeAnswer?.(user);
      await act(async () => answerExercise!());
      return user;
    };

    test("New Proof asked for while an exercise is being started wins over the exercise's late answer", async () => {
      const user = await applyRuleWhileExerciseStarts(async (u) => {
        await u.click(screen.getByRole('button', { name: /Start a new proof/i }));
        expect(screen.getByRole('button', { name: 'Discard and start new' })).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));

      // The New Proof dialog opens, as asked; the exercise is not started behind it.
      expect(await screen.findByRole('dialog')).toBeInTheDocument();
      expect(currentExercise()).not.toBeInTheDocument();
      expect(screen.getAllByRole('row')).toHaveLength(3);
    });

    test('an exercise dropped for a New Proof whose discard was then cancelled says it was not started', async () => {
      const user = await applyRuleWhileExerciseStarts(async (u) => {
        await u.click(screen.getByRole('button', { name: /Start a new proof/i }));
        await u.click(screen.getByRole('button', { name: 'Cancel' }));
      });

      expect(await screen.findByRole('alert')).toHaveTextContent('The exercise "Second" was not started, since a new proof was asked for meanwhile.');
      expect(currentExercise()).not.toBeInTheDocument();
      expect(screen.getAllByRole('row')).toHaveLength(3);

      // Starting a proof replaces the notice.
      await startProof(user, 'R', 'R');
      expect(screen.queryByText(/was not started/)).not.toBeInTheDocument();
    });

    test('an exercise answer arriving while the New Proof dialog is open is not swapped in behind it', async () => {
      let answerExercise: (() => void) | null = null;
      mockExerciseBackend(undefined, (init) => {
        if (JSON.parse(String(init!.body)).proofDto.goal !== 'Q') return replayAnswer(init);
        return new Promise<Response>((resolve) => { answerExercise = () => resolve(replayAnswer(init)); }) as unknown as Response;
      });
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'P', 'P');
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      await user.click(await screen.findByRole('button', { name: 'Start exercise Second' }));
      await waitFor(() => expect(answerExercise).not.toBeNull());
      // Only the premises are on screen, so the dialog opens straight away.
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(await screen.findByRole('dialog')).toBeInTheDocument();

      await act(async () => answerExercise!());

      expect(currentExercise()).not.toBeInTheDocument();
      expect(screen.getByText('GOAL:').parentElement).toHaveTextContent('P');
      expect(JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!).goal).toBe('P');
    });

    // A backend whose replays of the example (goal q) and of the exercise "Second" (goal Q) each wait for their own
    // answer.
    const slowExampleAndExerciseBackend = () => {
      const answers: { example: (() => void) | null, exercise: (() => void) | null } = { example: null, exercise: null };
      mockExerciseBackend(undefined, (init) => {
        const which = JSON.parse(String(init!.body)).proofDto.goal === 'q' ? 'example' : 'exercise';
        return new Promise<Response>((resolve) => { answers[which] = () => resolve(replayAnswer(init)); }) as unknown as Response;
      });
      return answers;
    };

    test('an exercise started while "Try an example" is being checked wins, even when the example answers first', async () => {
      const answers = slowExampleAndExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Try an example' }));
      await waitFor(() => expect(answers.example).not.toBeNull());
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await user.click(await screen.findByRole('button', { name: 'Start exercise Second' }));
      await waitFor(() => expect(answers.exercise).not.toBeNull());

      await act(async () => answers.example!());
      expect(screen.queryByRole('row')).not.toBeInTheDocument();
      await act(async () => answers.exercise!());

      await waitFor(() => expect(currentExercise()).toHaveTextContent('Exercise: Second'));
      expect(screen.getByText('GOAL:').parentElement).toHaveTextContent('Q');
      expect(JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!).goal).toBe('Q');
    });

    test('an example dropped for a New Proof dialog that was then cancelled says it was not started', async () => {
      const answers = slowExampleAndExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Try an example' }));
      await waitFor(() => expect(answers.example).not.toBeNull());
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(await screen.findByRole('dialog')).toBeInTheDocument();

      await act(async () => answers.example!());
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      await dialogClosed();

      expect(screen.queryByRole('row')).not.toBeInTheDocument();
      expect(screen.getByRole('alert')).toHaveTextContent('The example was not started, since a new proof was asked for meanwhile.');
      expect(screen.getByRole('button', { name: 'Try an example' })).toBeEnabled();
    });

    test('a solver answer for one exercise that arrives after another was started neither replaces it nor marks it solved', async () => {
      let answerSolve: (() => void) | null = null;
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        if (url.endsWith('/actions')) return jsonResponse(200, [REP]);
        if (url.endsWith('/exercises')) return jsonResponse(200, EXERCISES);
        if (url.endsWith('/action') && isReplay(init)) return replayAnswer(init);
        if (url.endsWith('/solve')) {
          return new Promise<Response>((resolve) => {
            answerSolve = () => resolve(jsonResponse(200, { proof: { ...repProof, done: true }, success: true, done: true, message: '' }));
          }) as unknown as Response;
        }
        return jsonResponse(500, { message: 'unexpected' });
      });
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await startExercise(user, 'First');
      await user.click(await screen.findByRole('button', { name: 'Solve' }));
      await waitFor(() => expect(answerSolve).not.toBeNull());
      // Only the premises are on screen, so starting another exercise does not ask.
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      await startExercise(user, 'Second');

      await act(async () => answerSolve!());

      expect(currentExercise()).toHaveTextContent('Exercise: Second');
      expect(currentExercise()).not.toHaveTextContent('(Solved)');
      expect(screen.getAllByRole('row')).toHaveLength(2);
      expect(screen.getByText('GOAL:').parentElement).toHaveTextContent('Q');
      expect(window.localStorage.getItem('natural-deduction.solved-exercises')).toBeNull();
      const saved = JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!);
      expect(saved.exerciseId).toBe('second');
      expect(saved.goal).toBe('Q');
    });

    test('a step applied while an exercise is being started is not discarded without asking', async () => {
      const user = await applyRuleWhileExerciseStarts();

      expect(await screen.findByRole('button', { name: 'Discard and start new' })).toBeInTheDocument();
      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(currentExercise()).not.toBeInTheDocument();

      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));

      await waitFor(() => expect(currentExercise()).toHaveTextContent('Exercise: Second'));
      expect(screen.getAllByRole('row')).toHaveLength(2);
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveFocus();
    });

    test('cancelling the discard of a step applied while an exercise was being started keeps that step', async () => {
      const user = await applyRuleWhileExerciseStarts();

      await user.click(await screen.findByRole('button', { name: 'Cancel' }));

      expect(screen.getAllByRole('row')).toHaveLength(3);
      expect(currentExercise()).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Exercises' })).toHaveFocus();
    });

    test('a confirmation left by an exercise start does not replace a proof started after it', async () => {
      const user = await applyRuleWhileExerciseStarts();
      expect(await screen.findByRole('button', { name: 'Discard and start new' })).toBeInTheDocument();
      // Undo brings the proof back to its premises, so New Proof opens the dialog without asking.
      await user.click(screen.getByRole('button', { name: 'Undo last step' }));
      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(2));

      // By hand rather than with `startProof`, which would click the stale confirmation.
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(await screen.findByRole('dialog')).toBeInTheDocument();
      await user.type(screen.getByPlaceholderText('Premise 1'), 'R');
      await user.type(screen.getByPlaceholderText('Enter the goal expression'), 'R');
      await user.click(screen.getByText('Start Proof'));
      await dialogClosed();

      // The exercise's confirmation went away with the proof it was about; the new proof stays.
      expect(screen.queryByRole('button', { name: 'Discard and start new' })).not.toBeInTheDocument();
      expect(currentExercise()).not.toBeInTheDocument();
      expect(screen.getByText('GOAL:').parentElement).toHaveTextContent('R');
    });

    test('finishing an exercise marks it solved, in text, and that survives a remount', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      expect(exerciseItem('First')).not.toHaveTextContent('(Solved)');
      await startExercise(user, 'First');
      expect(currentExercise()).not.toHaveTextContent('(Solved)');

      await finishWithRep(user);

      expect(currentExercise()).toHaveTextContent('Exercise: First (Solved)');
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      expect(exerciseItem('First')).toHaveTextContent('(Solved)');
      expect(exerciseItem('Second')).not.toHaveTextContent('(Solved)');
      expect(screen.getByText('Solved 1 of 3.')).toBeInTheDocument();

      // A new visit: nothing on screen, but the solved exercises are remembered.
      cleanup();
      window.sessionStorage.clear();
      clearExercisesCache();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await screen.findByRole('region', { name: 'Easy' });
      expect(exerciseItem('First')).toHaveTextContent('(Solved)');
      expect(exerciseItem('Second')).not.toHaveTextContent('(Solved)');
    });

    test('solving a proof that did not come from an exercise marks nothing solved', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProof(user, 'P', 'P');

      await finishWithRep(user);

      expect(window.localStorage.getItem('natural-deduction.solved-exercises')).toBeNull();
      expect(screen.queryByRole('button', { name: 'Next exercise' })).not.toBeInTheDocument();
    });

    test('"Next exercise" starts the next exercise of the list', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await startExercise(user, 'First');
      // Only offered once the proof is done.
      expect(screen.queryByRole('button', { name: 'Next exercise' })).not.toBeInTheDocument();
      await finishWithRep(user);

      await user.click(screen.getByRole('button', { name: 'Next exercise' }));
      // Starting it discards the finished proof, which asks first, like New Proof.
      await user.click(screen.getByRole('button', { name: 'Discard and start new' }));

      await waitFor(() => expect(currentExercise()).toHaveTextContent('Exercise: Second'));
      expect(JSON.parse(String(replayRequests().at(-1)![1]!.body)).proofDto.goal).toBe('Q');
      expect(screen.getAllByRole('row')).toHaveLength(2);
    });

    test('the last exercise offers no "Next exercise"', async () => {
      mockExerciseBackend(() => jsonResponse(200, [EXERCISES[0]]));
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await startExercise(user, 'First');

      await finishWithRep(user);

      expect(screen.getByRole('button', { name: 'New Proof' })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Next exercise' })).not.toBeInTheDocument();
    });

    test('a proof of an exercise stays one across a reload: finishing it then marks it solved and offers the next one', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await startExercise(user, 'First');
      expect(JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!).exerciseId).toBe('first');

      // A reload: the page starts from nothing but the saved proof, and fetches the exercises to know which it is.
      cleanup();
      clearExercisesCache();
      render(<App />);
      await waitFor(() => expect(currentExercise()).toHaveTextContent('Exercise: First'));

      await finishWithRep(user);

      expect(currentExercise()).toHaveTextContent('Exercise: First (Solved)');
      expect(JSON.parse(window.localStorage.getItem('natural-deduction.solved-exercises')!)).toEqual({ classical: ['first'] });
      expect(screen.getByRole('button', { name: 'Next exercise' })).toBeInTheDocument();
    });

    test('a proof started another way is not an exercise, even after one', async () => {
      mockExerciseBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));
      await startExercise(user, 'First');

      await startProof(user, 'Q', 'Q');

      expect(document.querySelector('.current-exercise')).not.toBeInTheDocument();
      expect(JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!).exerciseId).toBeUndefined();
    });
  });

  describe('picking the logic', () => {
    const NOTE: ActionDescriptor = { name: 'NOTE', params: ['INT'], label: 'Double negation elimination', symbol: '¬E' };
    const EXERCISES: Record<string, Exercise[]> = {
      classical: [
        { id: 'first', title: 'First', premises: ['P'], goal: 'P', difficulty: 'EASY' },
        { id: 'double-negation-elimination', title: 'Double negation elimination', premises: ['- (- P)'], goal: 'P', difficulty: 'MEDIUM' },
      ],
      intuitionistic: [
        { id: 'first', title: 'First', premises: ['P'], goal: 'P', difficulty: 'EASY' },
      ],
    };
    // The logic of a request, from its URL `/logic/{logic}/...`.
    const logicOf = (url: unknown) => String(url).split('/')[2];
    const requestsTo = (logic: string, resource: string) =>
      fetchMock.mock.calls.filter(([url]) => String(url) === `/logic/${logic}/${resource}`);

    // Classical logic has NOTE (¬E), intuitionistic logic does not; every other request is answered as for any logic.
    const mockLogicsBackend = () => {
      fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
        const logic = logicOf(url);
        if (url.endsWith('/actions')) return jsonResponse(200, logic === 'classical' ? [REP, NOTE] : [REP]);
        if (url.endsWith('/exercises')) return jsonResponse(200, EXERCISES[logic] ?? []);
        if (url.endsWith('/action') && isReplay(init)) return replayAnswer(init);
        if (url.endsWith('/proof')) return jsonResponse(201, { ...repProof, logic, done: true });
        return jsonResponse(200, { proof: { ...repProof, logic }, success: true, done: true, message: '' });
      });
    };

    // Opens the New Proof dialog, picks the logic and starts the proof `premise` |- `goal`.
    const startProofIn = async (user: ReturnType<typeof userEvent.setup>, logic: string, premise: string, goal: string) => {
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      const confirm = screen.queryByRole('button', { name: 'Discard and start new' });
      if (confirm) await user.click(confirm);
      await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
      await user.selectOptions(dialogLogic(), logic);
      await user.type(screen.getByPlaceholderText('Premise 1'), premise);
      await user.type(screen.getByPlaceholderText('Enter the goal expression'), goal);
      await user.click(screen.getByText('Start Proof'));
      await dialogClosed();
    };

    // The logic selector of the New Proof dialog (the empty page has one too, behind the dialog).
    const dialogLogic = () => within(screen.getByRole('dialog')).getByLabelText('Logic:');
    const ruleOptions = () => within(screen.getByLabelText(/Select Inference Rule:/i)).getAllByRole('option').map((option) => option.textContent);

    beforeEach(() => {
      clearExercisesCache();
      window.localStorage.clear();
    });

    test('an intuitionistic proof fetches its own rules, has no ¬E and no Solve, and names its logic next to the goal', async () => {
      mockLogicsBackend();
      const user = userEvent.setup();
      render(<App />);

      await startProofIn(user, 'intuitionistic', '- (- P)', 'P');

      await waitFor(() => expect(ruleOptions()).toEqual(['-- Choose a rule --', 'Rep']));
      expect(requestsTo('intuitionistic', 'actions')).toHaveLength(1);
      expect(JSON.parse(String(replayRequests()[0][1]!.body)).proofDto.logic).toBe('intuitionistic');
      expect(replayRequests()[0][0]).toBe('/logic/intuitionistic/action');
      expect(screen.getByText('in Intuitionistic logic')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Solve' })).not.toBeInTheDocument();
      expect(screen.getByText('Intuitionistic logic has no automatic solver.')).toBeInTheDocument();

      await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), 'Rep');
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(ruleRequests()).toHaveLength(1));
      expect(ruleRequests()[0][0]).toBe('/logic/intuitionistic/action');
    });

    test('switching the logic resets the Menu to the rules of the new logic', async () => {
      mockLogicsBackend();
      const user = userEvent.setup();
      render(<App />);
      await startProofIn(user, 'classical', '- (- P)', 'P');
      await waitFor(() => expect(ruleOptions()).toContain('Double negation elimination (¬E)'));
      await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), 'NOTE');
      expect(screen.getByRole('button', { name: 'Solve' })).toBeInTheDocument();
      expect(screen.getByText('in Classical logic')).toBeInTheDocument();

      // The dialog starts at the logic of the proof on screen.
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(dialogLogic()).toHaveValue('classical');
      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      await startProofIn(user, 'intuitionistic', '- (- P)', 'P');

      await waitFor(() => expect(ruleOptions()).toEqual(['-- Choose a rule --', 'Rep']));
      expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('');
      expect(screen.queryByLabelText(/Line number:/i)).not.toBeInTheDocument();
      expect(screen.getByText('in Intuitionistic logic')).toBeInTheDocument();
    });

    test('a restored proof keeps its logic', async () => {
      const saved = { ...repProof, logic: 'intuitionistic' };
      window.sessionStorage.setItem('natural-deduction.proof', JSON.stringify(saved));
      mockLogicsBackend();
      render(<App />);

      await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
      expect(replayRequests()[0][0]).toBe('/logic/intuitionistic/action');
      expect(JSON.parse(String(replayRequests()[0][1]!.body)).proofDto).toEqual(saved);
      expect(screen.getByText('in Intuitionistic logic')).toBeInTheDocument();
      await waitFor(() => expect(requestsTo('intuitionistic', 'actions')).toHaveLength(1));
      expect(JSON.parse(window.sessionStorage.getItem('natural-deduction.proof')!).logic).toBe('intuitionistic');
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    test('the empty page picks the logic of the example, and of the New Proof dialog', async () => {
      mockLogicsBackend();
      const user = userEvent.setup();
      render(<App />);
      const picker = screen.getByLabelText('Logic:');
      expect(picker).toHaveValue('classical');

      await user.selectOptions(picker, 'intuitionistic');
      expect(picker).toHaveAccessibleDescription(expect.stringContaining('without double negation elimination'));
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      expect(dialogLogic()).toHaveValue('intuitionistic');
      await user.click(screen.getByRole('button', { name: 'Cancel' }));

      await user.click(screen.getByRole('button', { name: 'Try an example' }));

      await waitFor(() => expect(screen.getByText('in Intuitionistic logic')).toBeInTheDocument());
      expect(replayRequests()[0][0]).toBe('/logic/intuitionistic/action');
      expect(JSON.parse(String(replayRequests()[0][1]!.body)).proofDto.logic).toBe('intuitionistic');
    });

    test('"Load from text" loads the text as a proof of the logic picked in the dialog', async () => {
      mockLogicsBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.click(screen.getByRole('button', { name: /Start a new proof/i }));
      await user.selectOptions(dialogLogic(), 'intuitionistic');
      await user.type(screen.getByLabelText(/Proof text/), 'P           Ass');
      await user.click(screen.getByRole('button', { name: 'Load proof' }));
      await dialogClosed();

      expect(fetchMock).toHaveBeenCalledWith('/logic/intuitionistic/proof', expect.anything());
      expect(screen.getByText('in Intuitionistic logic')).toBeInTheDocument();
    });

    test('the exercise list follows the logic, and so do the solved exercises', async () => {
      mockLogicsBackend();
      const user = userEvent.setup();
      render(<App />);
      await user.selectOptions(screen.getByLabelText('Logic:'), 'intuitionistic');
      await user.click(screen.getByRole('button', { name: 'Browse exercises' }));

      expect(await screen.findByText('Solved 0 of 1.')).toBeInTheDocument();
      expect(screen.getByText(/In Intuitionistic logic\./)).toBeInTheDocument();
      expect(requestsTo('intuitionistic', 'exercises')).toHaveLength(1);
      expect(requestsTo('classical', 'exercises')).toHaveLength(0);

      await user.click(screen.getByRole('button', { name: 'Start exercise First' }));
      await waitFor(() => expect(document.querySelector('.current-exercise')).toHaveTextContent('Exercise: First'));
      expect(replayRequests()[0][0]).toBe('/logic/intuitionistic/action');
      await user.selectOptions(await screen.findByLabelText(/Select Inference Rule:/i), 'Rep');
      await user.type(screen.getByLabelText(/Line number:/i), '1');
      await user.click(applyButton());
      await waitFor(() => expect(screen.getByText('Proof complete.')).toBeInTheDocument());
      expect(JSON.parse(window.localStorage.getItem('natural-deduction.solved-exercises')!)).toEqual({ intuitionistic: ['first'] });

      // The same exercise in classical logic is not solved yet, and the list is the classical one.
      await startProofIn(user, 'classical', 'P', 'P');
      await user.click(screen.getByRole('button', { name: 'Exercises' }));
      expect(await screen.findByText('Solved 0 of 2.')).toBeInTheDocument();
      expect(screen.getByText(/In Classical logic\./)).toBeInTheDocument();
      expect(screen.getByText('Double negation elimination', { selector: '.exercise-title' })).toBeInTheDocument();
    });
  });
});
