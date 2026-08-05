# 簡易決済システム (Payment System)

Spring Boot学習の一環として構築した、決済・在庫・注文管理システムです。
単純なCRUDアプリではなく、同時アクセス制御・認証・REST API・CI/CD・
自動テスト・Botによる運用シミュレーションまで、実務に近い構成を意識して
段階的に発展させました。

## できること

- ユーザーごとのログイン(Spring Security)、ロールベースの認可
- 商品の購入(在庫チェック・残高チェック・複数商品対応の注文)
- 注文履歴の記録(Order / OrderItem)
- 商品管理(登録・編集・在庫調整・論理削除)を管理者専用画面から操作
- REST API(Swaggerでドキュメント自動生成)
- Docker Composeによる一発起動
- Flywayによるスキーマのバージョン管理
- テストコード一式(単体・境界値・異常系・整合性・バリデーション)
- Playwrightによる「実際に運営されているECサイト」の自動シミュレーション

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
| テスト | JUnit 5, Testcontainers |
| CI | GitHub Actions |
| コンテナ | Docker / Docker Compose |
| ログ | Logback |
| 監視 | Spring Boot Actuator |
| 動作シミュレーション | Playwright (Java) |

## アーキテクチャ上のポイント

- **層ごとのパッケージ構成**:controller(web/api分離) / service / repository / entity / dto(request/response) / mapper / exception / security
- **同時アクセス対策**:ユーザー残高には悲観ロック、商品在庫には楽観ロック(`@Version`)を使い分け
- **例外処理の一元化**:`@ControllerAdvice`で、画面向けにはHTML、API向けにはJSONを出し分け
- **DBスキーマのバージョン管理**:Flywayでマイグレーション履歴を管理し、`ddl-auto=validate`で本番相当の運用を再現
- **論理削除**:商品削除は物理削除ではなく`active`フラグによる論理削除を採用し、過去の注文履歴の整合性を保持
- **秘密情報の分離**:DBパスワード等は環境変数化し、リポジトリに含めない設計

## 起動方法

### Docker Composeで起動(推奨)

```bash
docker compose up --build
```

起動後、`http://localhost:8080/users` にアクセスしてください。

### ローカル環境で起動する場合

1. PostgreSQLを起動し、`paymentdb`という名前のデータベースを作成
2. 環境変数 `DB_PASSWORD` を設定
3. 以下を実行

```bash
./gradlew bootRun
```

## テスト

```bash
./gradlew test
```

以下のテストスイートを含みます(Testcontainersにより本番相当のPostgreSQL上で実行)。

- `PaymentServiceTest`:正常系・境界値・異常系・注文整合性の検証
- `PurchaseRequestValidationTest`:入力バリデーションの検証
- `RandomChaosTest`:多人数同時アクセス時の金額・在庫整合性を検証するカオステスト

pushすると、GitHub Actionsで自動的にこれらのテストが実行されます。

## API仕様書(Swagger)

起動後、以下にアクセス: 
http://localhost:8080/swagger-ui/index.html

## ヘルスチェック
http://localhost:8080/actuator/health

## 運用シミュレーション(Bot)

`payment-system-bot`(別プロジェクト、Playwright使用)を実行すると、実際のブラウザ操作で
ログイン・商品選択・購入を自動的に繰り返し、「運営されているECサイト」の状態
(在庫減少・売上増加・注文履歴の蓄積)を再現できます。

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
11. 秘密情報の環境変数化、Testcontainers導入、N+1問題の解決、ロギング整備、ヘルスチェック、ページネーション
12. 体系的なテストスイート(境界値・異常系・整合性)の整備
13. 商品管理機能(登録・編集・在庫調整・論理削除)の実装
14. Playwrightによる自動運用シミュレーションBotの構築