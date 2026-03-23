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

  test('submits correct data and closes modal', () => {
    const { getByText, getByPlaceholderText } = setup(true);

    fireEvent.change(getByPlaceholderText('Premise 1'), { target: { value: 'Premise 1' } });
    fireEvent.change(getByPlaceholderText('Enter the goal expression'), { target: { value: 'Goal' } });

    fireEvent.click(getByText('Start Proof'));

    expect(onSubmitMock).toHaveBeenCalledWith(['Premise 1'], 'Goal');
    expect(onCloseMock).toHaveBeenCalled();
  });

  test('closes modal without submitting when close button is clicked', () => {
    const { getByText } = setup(true);

    fireEvent.click(getByText('Cancel'));

    expect(onSubmitMock).not.toHaveBeenCalled();
    expect(onCloseMock).toHaveBeenCalled();
  });
});
