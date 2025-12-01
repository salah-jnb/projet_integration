import { test, expect } from '@playwright/test'

test('Inscription puis connexion client', async ({ page }) => {
  const email = `e2e_${Date.now()}@example.com`
  const motDePasse = 'Test1234!'

  await page.goto('/register')

  await page.fill('#prenom', 'Jean')
  await page.fill('#nom', 'Dupont')
  await page.fill('#email', email)
  await page.fill('#motDePasse', motDePasse)
  await page.fill('#telephone', '12345678')
  await page.fill('#adresse', '10 rue de Paris')

  await page.getByRole('button', { name: /s'inscrire/i }).click()

  await expect(page).toHaveURL(/\/client\/dashboard/)

  const token = await page.evaluate(() => localStorage.getItem('jnb_token'))
  const userJson = await page.evaluate(() => localStorage.getItem('jnb_user'))
  expect(token).toBeTruthy()
  expect(userJson).toBeTruthy()

  await expect(page.getByRole('heading', { name: /Bienvenue/i })).toBeVisible()

  await page.getByRole('button', { name: /Déconnexion/i }).click()
  await expect(page).toHaveURL(/\/login/)

  await page.fill('#email', email)
  await page.fill('#password', motDePasse)
  await page.getByRole('button', { name: /se connecter/i }).click()

  await expect(page).toHaveURL(/\/client\/dashboard/)
  await expect(page.getByRole('heading', { name: /Bienvenue/i })).toBeVisible()
})
