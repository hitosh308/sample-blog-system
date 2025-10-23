import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './playwright/tests',
  timeout: 120 * 1000,
  expect: {
    timeout: 5000,
  },
  fullyParallel: false,
  retries: process.env.CI ? 2 : 0,
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: './mvnw spring-boot:run',
    port: 8080,
    reuseExistingServer: !process.env.CI,
    timeout: 180 * 1000,
    stdout: 'pipe',
    stderr: 'pipe',
  },
});
