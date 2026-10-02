import { ComponentProps, FC } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import './Button.css';

export const buttonVariants = cva('btn', {
  variants: {
    variant: {
      primary: 'btn-primary',
      secondary: 'btn-secondary',
      danger: 'btn-danger',
      ghost: 'btn-ghost',
    },
    size: {
      sm: 'btn-sm',
      md: 'btn-md',
    },
  },
  defaultVariants: {
    variant: 'primary',
    size: 'md',
  },
});

export type ButtonProps = ComponentProps<'button'> & VariantProps<typeof buttonVariants>;

/** The one button of the app. Everything but the look (props, ref, `type`) is a plain button's. */
const Button: FC<ButtonProps> = ({ variant, size, className, ...props }) => (
  <button className={buttonVariants({ variant, size, className })} {...props} />
);

export default Button;
