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

現時点では自動テストを用意していません。必要に応じて Maven プロジェクトにテストを追加してください。

### JaCoCo（コードカバレッジ）

1. `./mvnw clean verify` を実行すると、テストの実行と同時に JaCoCo レポートが生成されます。
2. 成果物は `target/site/jacoco/index.html` に出力されるため、ブラウザで開いてカバレッジを確認してください。

### SpotBugs（静的解析）

1. `./mvnw clean verify` を実行すると、SpotBugs による静的解析が行われます。
2. 解析結果は `target/spotbugsXml.xml` に出力されます。HTML レポートが必要な場合は `./mvnw site` を実行し、`target/site/spotbugs.html` を参照してください。

## 開発メモ
- アプリ起動時に管理者アカウント（ユーザー名: `admin`, パスワード: `admin`）を自動生成します。
- H2 コンソールは <http://localhost:8080/h2-console> で利用できます（JDBC URL: `jdbc:h2:mem:blogdb`）。
- テンプレートは Thymeleaf を使用しており、`src/main/resources/templates` 以下に配置されています。
