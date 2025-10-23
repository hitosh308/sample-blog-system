import { test, expect } from '@playwright/test';

const adminUsername = 'admin';
const adminPassword = 'admin';

function buildArticleData() {
  const unique = Date.now();
  return {
    title: `Playwright E2E Article ${unique}`,
    summary: `Summary generated at ${unique}.`,
    content: `This article was created by an automated Playwright test.\nTimestamp: ${unique}.`,
  };
}

test.describe('Admin article workflow', () => {
  test('administrator can create and publish an article that becomes visible to readers', async ({ page }) => {
    const article = buildArticleData();

    await page.goto('/login');

    await page.fill('#username', adminUsername);
    await page.fill('#password', adminPassword);

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle' }),
      page.getByRole('button', { name: 'ログイン' }).click(),
    ]);

    await expect(page).toHaveURL(/\/admin\/articles$/);
    await expect(page.getByRole('heading', { level: 2 })).toHaveText('記事一覧');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle' }),
      page.getByRole('link', { name: '新規記事を作成' }).click(),
    ]);

    await expect(page.getByRole('heading', { level: 2 })).toHaveText('新規記事');

    await page.fill('#title', article.title);
    await page.fill('#summary', article.summary);
    await page.fill('#content', article.content);
    await page.getByLabel('公開する').check();

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle' }),
      page.getByRole('button', { name: '保存' }).click(),
    ]);

    const successMessage = page.locator('.alert.success');
    await expect(successMessage).toContainText('記事を作成しました');

    const articleRow = page
      .locator('table tbody tr')
      .filter({ has: page.locator('td', { hasText: article.title }) })
      .first();

    await expect(articleRow).toBeVisible();
    await expect(articleRow.locator('td').nth(2)).toContainText('公開中');

    const slug = ((await articleRow.locator('td:nth-child(2) a').textContent()) || '').trim();
    expect(slug).not.toEqual('');

    await page.goto('/');

    const articleCard = page
      .locator('main article')
      .filter({ has: page.locator('h3 a', { hasText: article.title }) })
      .first();

    await expect(articleCard).toBeVisible();
    await expect(articleCard.locator('p').first()).toContainText(article.summary);

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle' }),
      articleCard.getByRole('link', { name: article.title }).click(),
    ]);

    await expect(page).toHaveURL(`/posts/${slug}`);
    await expect(page.getByRole('heading', { level: 2 })).toHaveText(article.title);
    await expect(page.locator('main article div')).toContainText('This article was created by an automated Playwright test.');
  });
});
