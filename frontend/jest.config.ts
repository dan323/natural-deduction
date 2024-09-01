import type { Config } from 'jest';

// Sync object
const config: Config = {
  verbose: true,
  collectCoverage: true,
  collectCoverageFrom: ['**/*.{ts,tsx}', '!**/*.test.tsx', '!**/*.config.{ts,js}', '!**/*.d.ts'],
  transform: {
    '^.+\\.tsx?$': 'ts-jest',
  },
  moduleNameMapper: {
    '\\.(css|less)$': 'identity-obj-proxy',
  },
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
};

export default config;