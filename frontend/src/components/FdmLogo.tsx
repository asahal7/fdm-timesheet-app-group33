interface Props {
  height?: number;
  variant?: 'light' | 'dark';
}

export default function FdmLogo({ height = 36, variant = 'light' }: Props) {
  return (
    <img
      src="/fdm-logo.jpeg"
      alt="FDM Group"
      height={height}
      style={{
        filter: variant === 'light' ? 'brightness(0) invert(1)' : 'none',
        objectFit: 'contain',
      }}
    />
  );
}