import type { Config } from 'jest';

// Sync object
const config: Config = {
  collectCoverage: true,
  coverageDirectory: "coverage",
  coverageReporters: ["lcov", "text"],
  collectCoverageFrom: [
    "src/**/*.{js,ts,tsx}", // Adjust as necessary
    "!src/**/*.d.ts",       // Exclude TypeScript declaration files
    "!src/**/*.test.{js,ts,tsx}", // Exclude test files
    "!src/**/*.spec.{js,ts,tsx}", // Exclude spec files if used
  ],
  verbose: true,
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