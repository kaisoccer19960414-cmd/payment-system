# 簡易決済システム (Payment System)

Spring Boot学習の一環として構築した、決済・在庫管理システムです。
単純なCRUDアプリではなく、同時アクセス制御・認証・REST API・CI/CDまで、
実務に近い構成を意識して段階的に発展させました。

## できること

- ユーザーごとのログイン(Spring Security)
- 商品購入(在庫チェック・残高チェック・複数商品対応の注文)
- 注文履歴の記録(Order / OrderItem)
- 管理者専用の商品管理画面(ロールベース認可)
- REST API(Swaggerでドキュメント自動生成)
- Docker Composeによる一発起動

## 技術スタック

| 分類 | 技術 |
|---|---|
| 言語 | Java 17 |
| フレームワーク | Spring Boot 4.1 |
| DB | PostgreSQL |
| ORM | Spring Data JPA (Hibernate) |
| マイグレーション | Flyway |
| 認証 | Spring Security |
| API仕様書 | springdoc-openapi (Swagger) |
| テスト | JUnit 5 |
| CI | GitHub Actions |
| コンテナ | Docker / Docker Compose |

## アーキテクチャ上のポイント

- **層ごとのパッケージ構成**:controller(web/api分離) / service / repository / entity / dto(request/response) / mapper / exception / security
- **同時アクセス対策**:ユーザー残高には悲観ロック、商品在庫には楽観ロック(`@Version`)を使い分け
- **例外処理の一元化**:`@ControllerAdvice`で、画面向けにはHTML、API向けにはJSONを出し分け
- **DBスキーマのバージョン管理**:Flywayでマイグレーション履歴を管理し、`ddl-auto=validate`で本番相当の運用を再現

## 起動方法

### Docker Composeで起動(推奨)

\`\`\`bash
docker compose up --build
\`\`\`

起動後、`http://localhost:8080/users` にアクセスしてください。

### ローカル環境で起動する場合

1. PostgreSQLを起動し、`paymentdb`という名前のデータベースを作成
2. `src/main/resources/application.properties` の接続情報を環境に合わせて設定
3. 以下を実行

\`\`\`bash
./gradlew bootRun
\`\`\`

## テスト

\`\`\`bash
./gradlew test
\`\`\`

pushすると、GitHub Actionsで自動的にテストが実行されます。

## API仕様書(Swagger)

起動後、以下にアクセス:

\`\`\`
http://localhost:8080/swagger-ui/index.html
\`\`\`

## 学習の記録

このプロジェクトは以下の順序で段階的に構築しました。

1. MVC構成の基礎(Controller / Service / Repository、DI、Bean)
2. 決済ロジックとトランザクション管理
3. 同時アクセス問題の再現と対策(悲観ロック → 楽観ロック)
4. PostgreSQL移行、JUnitテスト
5. 設計整理(例外処理統一、DTO/Mapper分離)
6. Spring Securityによる認証・認可
7. Order/OrderItemによる複数商品注文への対応
8. REST API化、Swaggerドキュメント化
9. Docker化、GitHub ActionsによるCI構築
10. Flywayによるスキーマ管理、入力バリデーション、ロールベース認可