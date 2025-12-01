import { test, expect } from '@playwright/test'

const BACKEND = 'http://localhost:5079'

test('Parcours client, coach et admin', async ({ page, request }) => {
  const ts = Date.now()
  const clientEmail = `client_${ts}@example.com`
  const coachEmail = `coach_${ts}@example.com`
  const adminEmail = `admin_${ts}@example.com`
  const password = 'Test1234!'

  const regRes = await request.post(`${BACKEND}/api/Auth/register`, {
    data: {
      email: clientEmail,
      motDePasse: password,
      nom: 'Client',
      prenom: 'Test',
      telephone: '12345678',
      adresse: '10 rue de Paris',
      codeParrainage: ''
    }
  })
  const regJson = await regRes.json()
  const token = regJson?.token as string
  const clientId = regJson?.utilisateurId as number
  expect(token).toBeTruthy()

  const authHeaders = { Authorization: `Bearer ${token}` }

  const coachRes = await request.post(`${BACKEND}/api/Utilisateurs`, {
    headers: authHeaders,
    data: {
      email: coachEmail,
      motDePasse: password,
      nom: 'Coach',
      prenom: 'Test',
      telephone: '20000000',
      adresse: '20 avenue Coach',
      photo: '',
      typeUtilisateur: 1,
      codeParrainage: '',
      specialites: 'Musculation',
      description: 'Coach certifié'
    }
  })
  expect(coachRes.ok()).toBeTruthy()
  const coachJson = await coachRes.json()
  const coachId = coachJson?.id as number

  const adminRes = await request.post(`${BACKEND}/api/Utilisateurs`, {
    headers: authHeaders,
    data: {
      email: adminEmail,
      motDePasse: password,
      nom: 'Admin',
      prenom: 'Test',
      telephone: '30000000',
      adresse: '30 boulevard Admin',
      photo: '',
      typeUtilisateur: 2,
      codeParrainage: '',
      specialites: '',
      description: ''
    }
  })
  expect(adminRes.ok()).toBeTruthy()

  const typesRes = await request.get(`${BACKEND}/api/Abonnements/types`, { headers: authHeaders })
  const types: Array<{ id: number; type: string }> = await typesRes.json()
  let coursType: { id: number; type: string } | undefined = (types || []).find((t) => t.type === 'COURS_COLLECTIFS')
  if (!coursType) {
    const createTypeRes = await request.post(`${BACKEND}/api/Abonnements/types`, {
      headers: authHeaders,
      data: {
        type: 'COURS_COLLECTIFS',
        nom: 'Abonnement Cours Collectifs',
        description: 'Accès aux cours collectifs',
        dureeEnMois: 1,
        prix: 100
      }
    })
    coursType = await createTypeRes.json()
  }

  await request.post(`${BACKEND}/api/Abonnements`, {
    headers: authHeaders,
    data: {
      clientId: clientId,
      typeAbonnementId: coursType!.id
    }
  })

  const coursRes = await request.post(`${BACKEND}/api/CoursCollectifs`, {
    headers: authHeaders,
    data: {
      nom: 'Test Cours',
      description: 'Cours de test',
      coachId: coachId,
      jourSemaine: 'LUNDI',
      heureDebut: '09:00:00',
      heureFin: '10:00:00',
      capaciteMax: 10
    }
  })
  const coursJson = await coursRes.json()
  const coursId = coursJson?.id as number

  const seanceRes = await request.post(`${BACKEND}/api/CoursCollectifs/seances`, {
    headers: authHeaders,
    data: {
      coursCollectifId: coursId,
      dateSeance: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      placesDisponibles: 5
    }
  })
  const seanceJson = await seanceRes.json()
  expect(seanceJson?.id).toBeTruthy()

  await page.goto('/login')
  await page.fill('#email', clientEmail)
  await page.fill('#password', password)
  await page.getByRole('button', { name: /se connecter/i }).click()
  await expect(page).toHaveURL(/\/client\/dashboard/)

  await page.getByRole('button', { name: 'Cours Collectifs' }).click()
  await expect(page.getByRole('heading', { name: /Séances de cours collectifs disponibles/i })).toBeVisible()

  const coursCard = page.locator('div').filter({ has: page.getByRole('heading', { name: 'Test Cours' }) }).first()
  await request.post(`${BACKEND}/api/ReservationsCours`, {
    headers: authHeaders,
    data: {
      clientId: clientId,
      seanceCoursCollectifId: seanceJson?.id as number,
      delaiAnnulationHeures: 24,
    }
  })
  await page.reload()

  const [cancelOk] = await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/ReservationsCours/') && res.request().method() === 'DELETE' && res.status() >= 200 && res.status() < 300),
    coursCard.locator('button:has-text("Annuler la réservation")').first().click(),
  ])
  await page.waitForLoadState('networkidle')
  await expect(coursCard.locator('button:has-text("Réserver cette séance")').first()).toBeVisible()

  await page.getByRole('button', { name: /Déconnexion/i }).click()
  await expect(page).toHaveURL(/\/login/)

  await page.fill('#email', coachEmail)
  await page.fill('#password', password)
  await page.getByRole('button', { name: /se connecter/i }).click()
  await expect(page).toHaveURL(/\/coach\/dashboard/)
  await page.getByRole('button', { name: 'Disponibilités' }).click()
  await expect(page.getByRole('heading', { name: /Mes Disponibilités/i })).toBeVisible()

  const dispoCreateRes = await request.post(`${BACKEND}/api/Disponibilites/coach/${coachId}`, {
    headers: authHeaders,
    data: {
      jourSemaine: 'MARDI',
      heureDebut: '11:00:00',
      heureFin: '12:00:00',
      actif: true
    }
  })
  const dispoJson = await dispoCreateRes.json()
  const slotText = '11:00 - 12:00'
  await page.reload()
  await expect(page.getByRole('heading', { name: /Mes Disponibilités/i })).toBeVisible()
  await expect(page.getByText(slotText)).toBeVisible()

  const slotRow = page.locator('div').filter({ hasText: slotText }).first()
  const dispoId = dispoJson?.id as number
  await request.put(`${BACKEND}/api/Disponibilites/${dispoId}`, {
    headers: authHeaders,
    data: {
      jourSemaine: 'MARDI',
      heureDebut: '11:30:00',
      heureFin: '12:30:00',
      actif: true,
    }
  })
  await page.reload()
  await expect(page.getByText('11:30 - 12:30')).toBeVisible()

  const updatedRow = page.locator('div').filter({ hasText: '11:30 - 12:30' }).first()
  const deleteRes = await request.delete(`${BACKEND}/api/Disponibilites/${dispoId}`, { headers: authHeaders })
  await page.reload()
  await expect(page.getByText('11:30 - 12:30')).toHaveCount(0)

  await page.getByRole('button', { name: /Déconnexion/i }).click()
  await expect(page).toHaveURL(/\/login/)

  await page.fill('#email', adminEmail)
  await page.fill('#password', password)
  await page.getByRole('button', { name: /se connecter/i }).click()
  await expect(page).toHaveURL(/\/admin\/dashboard/)
  await page.getByRole('button', { name: 'Utilisateurs' }).click()
  await expect(page.getByRole('heading', { name: /Gestion Utilisateurs/i })).toBeVisible()

  await page.getByPlaceholder('Rechercher par nom ou email...').fill(coachEmail)
  const coachRow = page.locator('div').filter({ hasText: coachEmail }).first()
  await expect(coachRow).toBeVisible()
  await request.put(`${BACKEND}/api/Utilisateurs/${coachId}`, {
    headers: authHeaders,
    data: {
      nom: 'Coach',
      prenom: 'Test',
      telephone: '00001111',
      adresse: '20 avenue Coach'
    }
  })
  await page.reload()
  const usersRes = await request.get(`${BACKEND}/api/Utilisateurs`, { headers: authHeaders })
  const usersJson = await usersRes.json()
  const coachUpdated = usersJson.find((u: any) => u.id === coachId)
  expect(coachUpdated?.telephone).toBe('00001111')

  await page.getByRole('button', { name: /Déconnexion/i }).click()
  await expect(page).toHaveURL(/\/login/)
})
