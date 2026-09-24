import { InputHTMLAttributes, useState } from 'react';
import { act, render, fireEvent, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import NewProofModal from '../NewProofModal';

describe('NewProofModal Component', () => {
  const onCloseMock = jest.fn();
  // The caller accepts every proof.
  const onSubmitMock = jest.fn().mockResolvedValue(null);

  const setup = (isOpen: boolean) => {
    return render(
      <NewProofModal isOpen={isOpen} onClose={onCloseMock} onSubmit={onSubmitMock} />
    );
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('renders correctly when open', () => {
    const { getByText, getByPlaceholderText } = setup(true);

    expect(getByText('New Proof')).toBeInTheDocument();
    expect(getByPlaceholderText('Premise 1')).toBeInTheDocument();
    expect(getByPlaceholderText('Enter the goal expression')).toBeInTheDocument();
    expect(getByText('+ Add Premise')).toBeInTheDocument();
    expect(getByText('Start Proof')).toBeInTheDocument();
    expect(getByText('Cancel')).toBeInTheDocument();
  });

  test('does not render when not open', () => {
    const { queryByText } = setup(false);

    expect(queryByText('New Proof')).not.toBeInTheDocument();
  });

  test('can add a premise and update goal', () => {
    const { getByText, getByPlaceholderText } = setup(true);

    // Add a new premise
    fireEvent.click(getByText('+ Add Premise'));
    const premise1 = getByPlaceholderText('Premise 1');
    const premise2 = getByPlaceholderText('Premise 2');
    expect(premise1).toBeInTheDocument();
    expect(premise2).toBeInTheDocument();

    // Update premises and goal
    fireEvent.change(premise1, { target: { value: 'Premise 1 value' } });
    fireEvent.change(premise2, { target: { value: 'Premise 2 value' } });
    fireEvent.change(getByPlaceholderText('Enter the goal expression'), { target: { value: 'Goal' } });

    expect((premise1 as HTMLInputElement).value).toBe('Premise 1 value');
    expect((premise2 as HTMLInputElement).value).toBe('Premise 2 value');
    expect((getByPlaceholderText('Enter the goal expression') as HTMLInputElement).value).toBe('Goal');
  });

  test('submits correct data and closes modal', async () => {
    const { getByText, getByPlaceholderText } = setup(true);

    fireEvent.change(getByPlaceholderText('Premise 1'), { target: { value: 'P -> Q' } });
    fireEvent.change(getByPlaceholderText('Enter the goal expression'), { target: { value: 'Q' } });

    fireEvent.click(getByText('Start Proof'));

    expect(onSubmitMock).toHaveBeenCalledWith(['P -> Q'], 'Q');
    await waitFor(() => expect(onCloseMock).toHaveBeenCalled());
  });

  test('closes modal without submitting when close button is clicked', () => {
    const { getByText } = setup(true);

    fireEvent.click(getByText('Cancel'));

    expect(onSubmitMock).not.toHaveBeenCalled();
    expect(onCloseMock).toHaveBeenCalled();
  });
});

describe('NewProofModal typing', () => {
  test('keeps focus and the full value while typing a multi-character premise', async () => {
    const user = userEvent.setup();
    render(<NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />);

    // The modal moves focus to the first premise shortly after opening; let that settle first.
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    const premise = screen.getByPlaceholderText('Premise 1');
    await user.click(premise);
    await user.type(premise, 'P -> Q');

    expect(premise).toHaveValue('P -> Q');
    expect(premise).toHaveFocus();
  });

  test('keeps focus while typing in an added premise', async () => {
    const user = userEvent.setup();
    render(<NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />);

    // The modal moves focus to the first premise shortly after opening; let that settle first.
    await waitFor(() => expect(screen.getByPlaceholderText('Premise 1')).toHaveFocus());
    await user.click(screen.getByText('+ Add Premise'));
    const second = screen.getByPlaceholderText('Premise 2');
    await user.click(second);
    await user.type(second, 'Q & R');

    expect(second).toHaveValue('Q & R');
    expect(second).toHaveFocus();
  });
});

describe('NewProofModal accessibility', () => {
  test('is a labelled modal dialog', () => {
    render(<NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />);

    const dialog = screen.getByRole('dialog', { name: 'New Proof' });
    expect(dialog).toHaveAttribute('aria-modal', 'true');
  });

  test('every input has a label tied to it', () => {
    render(<NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />);

    expect(screen.getByLabelText('Premise 1')).toBe(screen.getByPlaceholderText('Premise 1'));
    expect(screen.getByLabelText('Goal:')).toBe(screen.getByPlaceholderText('Enter the goal expression'));
    expect(screen.getByRole('group', { name: 'Premises:' })).toContainElement(screen.getByLabelText('Premise 1'));
  });

  test('closes on Escape', async () => {
    const onClose = jest.fn();
    const user = userEvent.setup();
    render(<NewProofModal isOpen={true} onClose={onClose} onSubmit={jest.fn()} />);

    await user.keyboard('{Escape}');

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  test('focuses the first premise on open and gives the focus back on close', async () => {
    const Harness = () => {
      const [open, setOpen] = useState(false);
      return (
        <>
          <button onClick={() => setOpen(true)}>Open</button>
          <NewProofModal isOpen={open} onClose={() => setOpen(false)} onSubmit={jest.fn()} />
        </>
      );
    };
    const user = userEvent.setup();
    render(<Harness />);

    await user.click(screen.getByRole('button', { name: 'Open' }));
    await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());

    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Open' })).toHaveFocus();
  });
});

describe('NewProofModal focus trap', () => {
  const setup = async () => {
    const user = userEvent.setup();
    render(
      <>
        <button>Outside</button>
        <NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />
      </>
    );
    await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());
    return user;
  };

  test('Tab from the last control wraps to the first one', async () => {
    const user = await setup();
    // Start Proof is disabled without a goal, so Cancel is the last control.
    screen.getByRole('button', { name: 'Cancel' }).focus();

    await user.tab();

    expect(screen.getByLabelText('Premise 1')).toHaveFocus();
  });

  test('Shift+Tab from the first control wraps to the last one', async () => {
    const user = await setup();

    await user.tab({ shift: true });

    expect(screen.getByRole('button', { name: 'Cancel' })).toHaveFocus();
  });

  test('Tab still moves on inside the dialog, and takes a focus that is outside back in', async () => {
    const user = await setup();

    await user.tab();
    expect(screen.getByRole('button', { name: 'Insert implies (->) in Premise 1' })).toHaveFocus();

    screen.getByRole('button', { name: 'Outside' }).focus();
    await user.tab();
    expect(screen.getByLabelText('Premise 1')).toHaveFocus();
  });
});

describe('NewProofModal validation', () => {
  const startButton = () => screen.getByRole('button', { name: 'Start Proof' });

  const setup = async () => {
    const onClose = jest.fn();
    const onSubmit = jest.fn().mockResolvedValue(null);
    const user = userEvent.setup();
    render(<NewProofModal isOpen={true} onClose={onClose} onSubmit={onSubmit} />);
    // The modal moves focus to the first premise shortly after opening; let that settle before typing elsewhere.
    await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());
    return { onClose, onSubmit, user };
  };

  test('marks the goal as required and explains why Start Proof is disabled', async () => {
    const { user } = await setup();

    expect(screen.getByLabelText('Goal:')).toBeRequired();
    expect(startButton()).toBeDisabled();
    expect(startButton()).toHaveAccessibleDescription('Enter the goal to start the proof.');

    await user.type(screen.getByLabelText('Goal:'), 'P');

    expect(startButton()).toBeEnabled();
    expect(screen.queryByText('Enter the goal to start the proof.')).not.toBeInTheDocument();
  });

  test.each(['p ->', 'q &&', '(p', 'p q', 'p $ q'])('keeps the modal open and flags the goal %j', async (goal) => {
    const { onClose, onSubmit, user } = await setup();

    await user.type(screen.getByLabelText('Premise 1'), 'P');
    await user.type(screen.getByLabelText('Goal:'), goal);
    await user.click(startButton());

    const goalInput = screen.getByLabelText('Goal:');
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(goalInput).toHaveAttribute('aria-invalid', 'true');
    expect(goalInput).toHaveAccessibleDescription(expect.stringContaining(screen.getByRole('alert').textContent as string));
    expect(goalInput).toHaveFocus();
    expect(onSubmit).not.toHaveBeenCalled();
    expect(onClose).not.toHaveBeenCalled();
  });

  test.each(['p ->', 'q &&'])('keeps the modal open and flags the premise %j', async (premise) => {
    const { onClose, onSubmit, user } = await setup();

    await user.type(screen.getByLabelText('Premise 1'), premise);
    await user.type(screen.getByLabelText('Goal:'), 'Q');
    await user.click(startButton());

    const premiseInput = screen.getByLabelText('Premise 1');
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(premiseInput).toHaveAttribute('aria-invalid', 'true');
    expect(premiseInput).toHaveAccessibleDescription(expect.stringContaining(screen.getByRole('alert').textContent as string));
    expect(premiseInput).toHaveFocus();
    expect(onSubmit).not.toHaveBeenCalled();
    expect(onClose).not.toHaveBeenCalled();
  });

  test('reports each faulty field on its own, and clears a message once its field is edited', async () => {
    const { user } = await setup();
    await user.click(screen.getByText('+ Add Premise'));
    await user.type(screen.getByLabelText('Premise 1'), 'P');
    await user.type(screen.getByLabelText('Premise 2'), 'Q &');
    await user.type(screen.getByLabelText('Goal:'), '(R');

    await user.click(startButton());

    expect(screen.getAllByRole('alert')).toHaveLength(2);
    expect(screen.getByLabelText('Premise 1')).not.toHaveAttribute('aria-invalid');
    expect(screen.getByLabelText('Premise 2')).toHaveAttribute('aria-invalid', 'true');
    expect(screen.getByLabelText('Goal:')).toHaveAttribute('aria-invalid', 'true');
    expect(screen.getByLabelText('Premise 2')).toHaveFocus();

    await user.type(screen.getByLabelText('Premise 2'), ' R');

    expect(screen.getAllByRole('alert')).toHaveLength(1);
    expect(screen.getByLabelText('Premise 2')).not.toHaveAttribute('aria-invalid');
    expect(screen.getByLabelText('Goal:')).toHaveAttribute('aria-invalid', 'true');
  });

  test('a blank premise is ignored, the others are submitted', async () => {
    const { onClose, onSubmit, user } = await setup();
    await user.click(screen.getByText('+ Add Premise'));
    await user.type(screen.getByLabelText('Premise 2'), 'P & Q');
    await user.type(screen.getByLabelText('Goal:'), 'Q');

    await user.click(startButton());

    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(onSubmit).toHaveBeenCalledWith(['P & Q'], 'Q');
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });

  test('a goal of only spaces is not accepted', async () => {
    const { onSubmit, user } = await setup();

    await user.type(screen.getByLabelText('Goal:'), '   ');

    expect(startButton()).toBeDisabled();
    await user.click(startButton());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  test('a fixed formula can be submitted after an error', async () => {
    const { onSubmit, user } = await setup();
    await user.type(screen.getByLabelText('Goal:'), 'p ->');
    await user.click(startButton());
    expect(onSubmit).not.toHaveBeenCalled();

    await user.clear(screen.getByLabelText('Goal:'));
    await user.type(screen.getByLabelText('Goal:'), 'p -> q');
    await user.click(startButton());

    expect(onSubmit).toHaveBeenCalledWith([], 'p -> q');
  });

  test('a proof the caller refuses keeps the modal open with the reason, until a field is edited', async () => {
    const { onClose, onSubmit, user } = await setup();
    onSubmit.mockResolvedValue('The proof could not be read, check its expressions and rules');
    await user.type(screen.getByLabelText('Premise 1'), 'P');
    await user.type(screen.getByLabelText('Goal:'), 'Q');

    await user.click(startButton());

    expect(await screen.findByRole('alert')).toHaveTextContent('The proof could not be read, check its expressions and rules');
    expect(onClose).not.toHaveBeenCalled();
    await waitFor(() => expect(startButton()).toHaveFocus());
    expect(startButton()).toHaveAccessibleDescription('The proof could not be read, check its expressions and rules');

    await user.type(screen.getByLabelText('Goal:'), ' & P');

    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  test('Start Proof is disabled while the caller checks the proof', async () => {
    const { onClose, onSubmit, user } = await setup();
    let answer: (error: string | null) => void = () => {};
    onSubmit.mockReturnValue(new Promise<string | null>((resolve) => { answer = resolve; }));
    await user.type(screen.getByLabelText('Goal:'), 'Q');

    await user.click(startButton());

    const starting = screen.getByRole('button', { name: 'Starting…' });
    expect(starting).toBeDisabled();
    await user.click(starting);
    expect(onSubmit).toHaveBeenCalledTimes(1);

    await act(async () => answer(null));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  test('the premises and the goal cannot be edited while the caller checks them', async () => {
    const { onSubmit, user } = await setup();
    let answer: (error: string | null) => void = () => {};
    onSubmit.mockReturnValue(new Promise<string | null>((resolve) => { answer = resolve; }));
    await user.type(screen.getByLabelText('Premise 1'), 'P');
    await user.click(screen.getByRole('button', { name: '+ Add Premise' }));
    await user.type(screen.getByLabelText('Goal:'), 'P');

    await user.click(startButton());

    const premise = screen.getByLabelText('Premise 1');
    const goal = screen.getByLabelText('Goal:');
    expect(premise).toHaveAttribute('readonly');
    expect(goal).toHaveAttribute('readonly');
    await user.type(premise, ' & Q');
    await user.type(goal, ' | Q');
    expect(premise).toHaveValue('P');
    expect(goal).toHaveValue('P');
    expect(screen.getByRole('button', { name: '+ Add Premise' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Remove premise 1' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Insert and (&) in Goal' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Insert and (&) in Premise 1' })).toBeDisabled();

    // A refusal describes exactly the text still shown, and the fields can be edited again.
    await act(async () => answer('The proof could not be read, check its expressions and rules'));
    expect(screen.getByRole('alert')).toHaveTextContent('The proof could not be read');
    expect(goal).not.toHaveAttribute('readonly');
    await user.type(goal, ' | Q');
    expect(goal).toHaveValue('P | Q');
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  test('an answer arriving after the modal was closed neither closes it again nor shows an error', async () => {
    let answer: (error: string | null) => void = () => {};
    const onSubmit = jest.fn().mockReturnValue(new Promise<string | null>((resolve) => { answer = resolve; }));
    const onClose = jest.fn();
    const user = userEvent.setup();
    const { rerender } = render(<NewProofModal isOpen={true} onClose={onClose} onSubmit={onSubmit} />);
    await user.type(screen.getByLabelText('Goal:'), 'Q');
    await user.click(startButton());

    rerender(<NewProofModal isOpen={false} onClose={onClose} onSubmit={onSubmit} />);
    rerender(<NewProofModal isOpen={true} onClose={onClose} onSubmit={onSubmit} />);
    await act(async () => answer('too late'));

    expect(onClose).not.toHaveBeenCalled();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(startButton()).toBeDisabled(); // the reopened dialog is empty again, not "Starting…"
  });
});

describe('NewProofModal reopening', () => {
  const Harness = () => {
    const [open, setOpen] = useState(false);
    return (
      <>
        <button onClick={() => setOpen(true)}>Open</button>
        <NewProofModal isOpen={open} onClose={() => setOpen(false)} onSubmit={jest.fn()} />
      </>
    );
  };

  test('opens with empty fields and no errors, whatever was typed before', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    await user.click(screen.getByRole('button', { name: 'Open' }));
    await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());
    await user.click(screen.getByText('+ Add Premise'));
    await user.type(screen.getByLabelText('Premise 1'), 'P ->');
    await user.type(screen.getByLabelText('Premise 2'), 'Q');
    await user.type(screen.getByLabelText('Goal:'), 'R &');
    await user.click(screen.getByRole('button', { name: 'Start Proof' }));
    expect(screen.getAllByRole('alert').length).toBeGreaterThan(0);

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'Open' }));

    expect(screen.getByLabelText('Premise 1')).toHaveValue('');
    expect(screen.queryByLabelText('Premise 2')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Goal:')).toHaveValue('');
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Start Proof' })).toBeDisabled();
  });
});

describe('NewProofModal removing a premise', () => {
  const setup = async (count: number) => {
    const user = userEvent.setup();
    render(<NewProofModal isOpen={true} onClose={jest.fn()} onSubmit={jest.fn()} />);
    await waitFor(() => expect(screen.getByLabelText('Premise 1')).toHaveFocus());
    for (let i = 1; i < count; i++) {
      await user.click(screen.getByText('+ Add Premise'));
    }
    return user;
  };

  test('moves the focus to the premise that takes its place', async () => {
    const user = await setup(3);
    await user.type(screen.getByLabelText('Premise 1'), 'A');
    await user.type(screen.getByLabelText('Premise 2'), 'B');
    await user.type(screen.getByLabelText('Premise 3'), 'C');

    await user.click(screen.getByRole('button', { name: 'Remove premise 2' }));

    expect(screen.getByLabelText('Premise 2')).toHaveValue('C');
    expect(screen.getByLabelText('Premise 2')).toHaveFocus();
  });

  test('moves the focus to the previous premise when the last one is removed', async () => {
    const user = await setup(3);

    await user.click(screen.getByRole('button', { name: 'Remove premise 3' }));

    expect(screen.queryByLabelText('Premise 3')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Premise 2')).toHaveFocus();
  });

  test('moves the focus to the first premise when the first one is removed', async () => {
    const user = await setup(2);
    await user.type(screen.getByLabelText('Premise 2'), 'B');

    await user.click(screen.getByRole('button', { name: 'Remove premise 1' }));

    expect(screen.getByLabelText('Premise 1')).toHaveValue('B');
    expect(screen.getByLabelText('Premise 1')).toHaveFocus();
  });

  test('the error of a premise goes away with it and does not move to its neighbour', async () => {
    const user = await setup(2);
    await user.type(screen.getByLabelText('Premise 1'), 'P ->');
    await user.type(screen.getByLabelText('Premise 2'), 'Q');
    await user.type(screen.getByLabelText('Goal:'), 'Q');
    await user.click(screen.getByRole('button', { name: 'Start Proof' }));
    expect(screen.getAllByRole('alert')).toHaveLength(1);

    await user.click(screen.getByRole('button', { name: 'Remove premise 1' }));

    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Premise 1')).not.toHaveAttribute('aria-invalid');
  });
});

describe('NewProofModal loading a proof from text', () => {
  const onCloseMock = jest.fn();
  const onSubmitMock = jest.fn();

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('is only offered when the caller can load a proof', () => {
    render(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} />);
    expect(screen.queryByLabelText(/Proof text/)).not.toBeInTheDocument();
  });

  test('sends the text alone, then closes; there is no goal to type, and Load is disabled while the text is blank', async () => {
    const onLoadText = jest.fn().mockResolvedValue(null);
    render(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    const load = screen.getByRole('button', { name: 'Load proof' });
    expect(load).toBeDisabled();
    expect(screen.queryByLabelText(/Goal of the loaded proof/)).not.toBeInTheDocument();
    expect(screen.getAllByRole('textbox', { name: /Goal/ })).toHaveLength(1);

    fireEvent.change(screen.getByLabelText(/Proof text/), { target: { value: '  ' } });
    expect(load).toBeDisabled();
    fireEvent.change(screen.getByLabelText(/Proof text/), { target: { value: 'P           Ass' } });
    expect(load).toBeEnabled();
    fireEvent.click(load);

    await waitFor(() => expect(onCloseMock).toHaveBeenCalled());
    expect(onLoadText).toHaveBeenCalledWith('P           Ass');
    expect(onSubmitMock).not.toHaveBeenCalled();
  });

  test('says why an unfinished proof could not be loaded, until the text is edited', async () => {
    const onLoadText = jest.fn().mockResolvedValue('The proof is invalid: it does not end at the top level');
    render(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    const text = screen.getByLabelText(/Proof text/);
    fireEvent.change(text, { target: { value: 'P           Ass\n   Q           Ass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Load proof' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('The proof is invalid: it does not end at the top level');
    expect(text).toHaveAttribute('aria-invalid', 'true');
    expect(text).toHaveAttribute('aria-describedby', alert.id);
    expect(text).toHaveFocus();
    expect(onCloseMock).not.toHaveBeenCalled();

    fireEvent.change(text, { target: { value: 'P           Ass' } });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(text).not.toHaveAttribute('aria-invalid');
  });

  test('the text cannot be edited while the caller loads it, so the proof loaded is the text on screen', async () => {
    let finishLoad: (error: string | null) => void = () => undefined;
    const onLoadText = jest.fn(() => new Promise<string | null>((resolve) => { finishLoad = resolve; }));
    render(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    const text = screen.getByLabelText(/Proof text/);
    fireEvent.change(text, { target: { value: 'P           Nope' } });
    fireEvent.click(screen.getByRole('button', { name: 'Load proof' }));

    expect(text).toHaveAttribute('readonly');
    expect(screen.getByRole('button', { name: 'Loading…' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Start Proof' })).toBeDisabled();

    await act(async () => finishLoad('Line 1 is not valid: unknown rule'));

    expect(screen.getByRole('alert')).toHaveTextContent('Line 1 is not valid: unknown rule');
    expect(text).toHaveValue('P           Nope');
    expect(text).not.toHaveAttribute('readonly');
    expect(screen.getByRole('button', { name: 'Load proof' })).toBeEnabled();
  });

  test('the answer to a load of a dialog that was closed since does not close the dialog opened again', async () => {
    let finishLoad: (error: string | null) => void = () => undefined;
    const onLoadText = jest.fn(() => new Promise<string | null>((resolve) => { finishLoad = resolve; }));
    const { rerender } = render(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    fireEvent.change(screen.getByLabelText(/Proof text/), { target: { value: 'P           Ass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Load proof' }));
    expect(screen.getByRole('button', { name: 'Loading…' })).toBeDisabled();

    // Cancelled while loading, then opened again, and something typed in it.
    rerender(<NewProofModal isOpen={false} onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    rerender(<NewProofModal isOpen onClose={onCloseMock} onSubmit={onSubmitMock} onLoadText={onLoadText} />);
    fireEvent.change(screen.getByLabelText('Premise 1'), { target: { value: 'Q' } });

    await act(async () => finishLoad(null));

    expect(onCloseMock).not.toHaveBeenCalled();
    expect(screen.getByLabelText('Premise 1')).toHaveValue('Q');
    expect(screen.getByRole('button', { name: 'Load proof' })).toBeInTheDocument();
  });
});

describe('NewProofModal syntax help', () => {
  const HINT = 'Syntax: -> implies, & and, | or, - not';

  test('every formula field is described by the syntax hint', () => {
    render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={jest.fn()} onLoadText={jest.fn()} />);

    expect(screen.getByText(HINT)).toBeInTheDocument();
    expect(screen.getByLabelText('Premise 1')).toHaveAccessibleDescription(HINT);
    expect(screen.getByLabelText('Goal:')).toHaveAccessibleDescription(HINT);
  });

  test('the connective buttons never submit the proof', async () => {
    const user = userEvent.setup();
    const onSubmit = jest.fn();
    render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={onSubmit} />);

    for (const button of screen.getAllByRole('button', { name: /^Insert / })) {
      expect(button).toHaveAttribute('type', 'button');
    }
    await user.click(screen.getByRole('button', { name: 'Insert implies (->) in Goal' }));
    expect(onSubmit).not.toHaveBeenCalled();
  });

  test('p → q entered with the buttons is submitted as p -> q', async () => {
    const user = userEvent.setup();
    const onSubmit = jest.fn();
    render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={onSubmit} />);
    const premise = screen.getByLabelText('Premise 1') as HTMLInputElement;
    const goal = screen.getByLabelText('Goal:') as HTMLInputElement;

    await user.type(premise, 'p ');
    await user.click(screen.getByRole('button', { name: 'Insert implies (->) in Premise 1' }));
    expect(premise).toHaveFocus();
    await user.type(premise, ' q');

    await user.type(goal, 'q');
    goal.setSelectionRange(0, 0);
    await user.click(screen.getByRole('button', { name: 'Insert not (-) in Goal' }));
    await user.click(screen.getByRole('button', { name: 'Insert not (-) in Goal' }));
    expect(goal).toHaveValue('--q');
    expect(goal.selectionStart).toBe(2);

    await user.click(screen.getByRole('button', { name: 'Start Proof' }));
    expect(onSubmit).toHaveBeenCalledWith(['p -> q'], '--q');
  });

  test('each premise has its own buttons', async () => {
    const user = userEvent.setup();
    render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={jest.fn()} />);
    await user.click(screen.getByRole('button', { name: '+ Add Premise' }));

    await user.click(screen.getByRole('button', { name: 'Insert and (&) in Premise 2' }));

    expect(screen.getByLabelText('Premise 1')).toHaveValue('');
    expect(screen.getByLabelText('Premise 2')).toHaveValue('&');
    expect(screen.getByLabelText('Premise 2')).toHaveFocus();
  });

  test('the delayed initial focus does not take the focus from a field the user already reached', () => {
    jest.useFakeTimers();
    try {
      render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={jest.fn()} />);
      fireEvent.click(screen.getByRole('button', { name: '+ Add Premise' }));
      // Before the initial focus timer runs, a connective is typed into Premise 2, which gets the focus.
      fireEvent.click(screen.getByRole('button', { name: 'Insert and (&) in Premise 2' }));
      expect(screen.getByLabelText('Premise 2')).toHaveFocus();

      act(() => { jest.advanceTimersByTime(100); });

      expect(screen.getByLabelText('Premise 2')).toHaveFocus();
    } finally {
      jest.useRealTimers();
    }
  });

  test('the first premise gets the focus when the dialog opens', () => {
    jest.useFakeTimers();
    try {
      render(<NewProofModal isOpen onClose={jest.fn()} onSubmit={jest.fn()} />);
      act(() => { jest.advanceTimersByTime(100); });
      expect(screen.getByLabelText('Premise 1')).toHaveFocus();
    } finally {
      jest.useRealTimers();
    }
  });
});
