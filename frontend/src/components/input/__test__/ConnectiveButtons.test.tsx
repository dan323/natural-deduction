import { FC, useRef, useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ConnectiveButtons from '../ConnectiveButtons';

// Two controlled inputs in one parent, like the goal and premises of the new proof modal: typing in the other input
// re-renders the buttons of the first one.
const Harness: FC<{ onGoal: (value: string) => void }> = ({ onGoal }) => {
    const [goal, setGoal] = useState('p & q');
    const [other, setOther] = useState('');
    const goalRef = useRef<HTMLInputElement>(null);
    const handleGoal = (value: string) => {
        onGoal(value);
        setGoal(value);
    };
    return (
        <>
            <label htmlFor="goal">Goal</label>
            <input id="goal" ref={goalRef} value={goal} onChange={(e) => handleGoal(e.target.value)} />
            <ConnectiveButtons getInput={() => goalRef.current} onInsert={handleGoal} target="Goal" />
            <label htmlFor="other">Other</label>
            <input id="other" value={other} onChange={(e) => setOther(e.target.value)} />
        </>
    );
};

describe('ConnectiveButtons', () => {
    test('replacing a selection by the same connective keeps the caret after it and does not steal the focus later', async () => {
        const user = userEvent.setup();
        const onGoal = jest.fn();
        render(<Harness onGoal={onGoal} />);
        const goal = screen.getByLabelText('Goal') as HTMLInputElement;
        const other = screen.getByLabelText('Other') as HTMLInputElement;
        goal.focus();
        goal.setSelectionRange(2, 3);

        await user.click(screen.getByRole('button', { name: 'Insert and (&) in Goal' }));

        expect(goal).toHaveValue('p & q');
        expect(onGoal).not.toHaveBeenCalled();
        expect(goal).toHaveFocus();
        expect(goal.selectionStart).toBe(3);
        expect(goal.selectionEnd).toBe(3);

        other.focus();
        fireEvent.change(other, { target: { value: 'r' } });

        expect(other).toHaveValue('r');
        expect(other).toHaveFocus();
    });
});
