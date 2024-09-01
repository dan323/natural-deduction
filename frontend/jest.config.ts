import type {Config} from '@jest/types';

// Sync object
const config: Config.InitialOptions = {
  verbose: true,
  collectCoverage: true,
  collectCoverageFrom: ['**/*.{ts,tsx}','!**/*.test.tsx']
};

export default config;