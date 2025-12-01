import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: 'tests/e2e',
  fullyParallel: true,
  timeout: 240000,
  expect: { timeout: 5000 },
  use: {
    baseURL: 'http://localhost:4173',
    trace: 'on-first-retry',
    video: 'off',
    screenshot: 'only-on-failure',
    channel: 'chrome',
    headless: false,
  },
  webServer: {
    command: 'npm run preview',
    port: 4173,
    reuseExistingServer: true,
  },
  projects: [
    { name: 'chrome', use: { ...devices['Desktop Chrome'], channel: 'chrome' } },
  ],
})
