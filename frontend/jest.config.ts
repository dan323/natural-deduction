import type {Config} from '@jest/types';

// Sync object
const config: Config.InitialOptions = {
  verbose: true,
  collectCoverage: true,
  collectCoverageFrom: ['**/src/**/*.tsx','!**/src/**/*.test.tsx']
};
export default config;