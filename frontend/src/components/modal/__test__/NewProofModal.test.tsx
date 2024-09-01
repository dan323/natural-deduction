import React, { InputHTMLAttributes } from 'react';
import { render, fireEvent } from '@testing-library/react';
import NewProofModal from '../NewProofModal';

describe('NewProofModal Component', () => {
  const onCloseMock = jest.fn();
  const onSubmitMock = jest.fn();

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
    expect(getByPlaceholderText('Enter a premise')).toBeInTheDocument();
    expect(getByPlaceholderText('Enter the goal')).toBeInTheDocument();
    expect(getByText('+ Add Premise')).toBeInTheDocument();
    expect(getByText('Submit')).toBeInTheDocument();
    expect(getByText('Close')).toBeInTheDocument();
  });

  test('does not render when not open', () => {
    const { queryByText } = setup(false);

    expect(queryByText('New Proof')).not.toBeInTheDocument();
  });

  test('can add a premise and update goal', () => {
    const { getByText, getAllByPlaceholderText, getByPlaceholderText } = setup(true);

    // Add a new premise
    fireEvent.click(getByText('+ Add Premise'));
    const premiseInputs = getAllByPlaceholderText('Enter a premise');
    expect(premiseInputs).toHaveLength(2);

    // Update premises and goal
    fireEvent.change(premiseInputs[0], { target: { value: 'Premise 1' } });
    fireEvent.change(premiseInputs[1], { target: { value: 'Premise 2' } });
    fireEvent.change(getByPlaceholderText('Enter the goal'), { target: { value: 'Goal' } });

    expect((premiseInputs[0] as HTMLInputElement).value).toBe('Premise 1');
    expect((premiseInputs[1] as HTMLInputElement).value).toBe('Premise 2');
    expect((getByPlaceholderText('Enter the goal') as HTMLInputElement).value).toBe('Goal');
  });

  test('submits correct data and closes modal', () => {
    const { getByText, getByPlaceholderText } = setup(true);

    fireEvent.change(getByPlaceholderText('Enter a premise'), { target: { value: 'Premise 1' } });
    fireEvent.change(getByPlaceholderText('Enter the goal'), { target: { value: 'Goal' } });

    fireEvent.click(getByText('Submit'));

    expect(onSubmitMock).toHaveBeenCalledWith(['Premise 1'], 'Goal');
    expect(onCloseMock).toHaveBeenCalled();
  });

  test('closes modal without submitting when close button is clicked', () => {
    const { getByText } = setup(true);

    fireEvent.click(getByText('Close'));

    expect(onSubmitMock).not.toHaveBeenCalled();
    expect(onCloseMock).toHaveBeenCalled();
  });
});
