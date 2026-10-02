import { createRef } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Button from '../Button';

describe('Button', () => {
  it('is a primary medium button by default', () => {
    render(<Button>Go</Button>);
    expect(screen.getByRole('button', { name: 'Go' })).toHaveClass('btn', 'btn-primary', 'btn-md');
  });

  it.each(['primary', 'secondary', 'danger', 'ghost'] as const)('renders the %s variant', (variant) => {
    render(<Button variant={variant}>Go</Button>);
    const button = screen.getByRole('button');
    expect(button).toHaveClass(`btn-${variant}`);
    for (const other of ['primary', 'secondary', 'danger', 'ghost'].filter((v) => v !== variant)) {
      expect(button).not.toHaveClass(`btn-${other}`);
    }
  });

  it.each(['sm', 'md'] as const)('renders the %s size', (size) => {
    render(<Button size={size}>Go</Button>);
    expect(screen.getByRole('button')).toHaveClass(`btn-${size}`);
  });

  it('keeps an extra class name', () => {
    render(<Button className="extra">Go</Button>);
    expect(screen.getByRole('button')).toHaveClass('btn', 'extra');
  });

  it('does not fire onClick when disabled', async () => {
    const onClick = jest.fn();
    render(<Button disabled onClick={onClick}>Go</Button>);
    const button = screen.getByRole('button');
    expect(button).toBeDisabled();
    await userEvent.setup().click(button);
    expect(onClick).not.toHaveBeenCalled();
  });

  it('fires onClick when enabled', async () => {
    const onClick = jest.fn();
    render(<Button onClick={onClick}>Go</Button>);
    await userEvent.setup().click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('forwards the other props to the button', () => {
    render(<Button type="button" aria-label="Do it" title="Hint" aria-expanded={true} data-x="1">Go</Button>);
    const button = screen.getByRole('button', { name: 'Do it' });
    expect(button).toHaveAttribute('type', 'button');
    expect(button).toHaveAttribute('title', 'Hint');
    expect(button).toHaveAttribute('aria-expanded', 'true');
    expect(button).toHaveAttribute('data-x', '1');
  });

  it('forwards the ref to the button', () => {
    const ref = createRef<HTMLButtonElement>();
    render(<Button ref={ref}>Go</Button>);
    expect(ref.current).toBe(screen.getByRole('button'));
  });
});
