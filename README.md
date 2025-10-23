# Sample Blog System

シンプルなブログ管理・公開システムです。管理者は記事とアカウントを管理でき、読者は公開済みの記事を閲覧できます。

## 主な機能

### 管理者向け
- 記事の一覧表示、作成、編集、削除
- 記事の公開・下書き切り替え（公開日時の自動管理）
- アカウント管理（一覧、作成、編集、削除）
- 管理画面へのログイン機能（初期アカウント: `admin` / `admin`）

### 読者向け
- 公開済み記事の一覧表示
- 記事詳細ページ

## 技術スタック
- Java 21
- Spring Boot 3
- Spring Data JPA / H2（インメモリ）
- Spring Security（フォームログイン）
- Thymeleaf

## 実行方法

```bash
./mvnw spring-boot:run
```

同梱の `mvnw` はローカルにインストールされた Maven (`mvn`) コマンドをラップする簡易スクリプトです。Maven が利用できない環境では別途インストールしてください。IDE やローカルに Maven がインストールされている場合は `mvn spring-boot:run` でも起動できます。

アプリケーションを起動すると、ブラウザで <http://localhost:8080> にアクセスできます。管理画面には <http://localhost:8080/login> からログインしてください。

## テスト

### Playwright E2E テストの実行

Playwright を利用した E2E テストは以下の手順で実行できます。

1. Node.js (推奨: 18 以降) をインストールします。
2. 依存関係をインストールします。

   ```bash
   npm install
   ```

3. 初回のみ、Playwright のブラウザインストールを実行します。

   ```bash
   npx playwright install --with-deps
   ```

4. テストを実行します。Maven のアプリケーション起動は Playwright の設定で自動的に行われます。

   ```bash
   npm test
   ```

GUI でテストの進行を確認したい場合は `npm run test:ui` も利用できます。

## 開発メモ
- アプリ起動時に管理者アカウント（ユーザー名: `admin`, パスワード: `admin`）を自動生成します。
- H2 コンソールは <http://localhost:8080/h2-console> で利用できます（JDBC URL: `jdbc:h2:mem:blogdb`）。
- テンプレートは Thymeleaf を使用しており、`src/main/resources/templates` 以下に配置されています。
