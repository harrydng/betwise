# Betwise Backend

Betwise is an app for learning to invest with practice money. Users get a
paper-trading portfolio, buy and sell stocks, ETFs, bonds and REITs at market
prices, and earn extra buying power by finishing short lessons and quizzes.

This is the Spring Boot API behind it. It serves:

- **The iOS app** (`../front-end`): accounts, portfolios, trading, watchlists,
  lessons, quizzes, news and notifications.
- **The admin app** (`../admin`): authoring lessons, sections and quizzes.

## Tech stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security) |
| Database | MySQL 8.4, schema managed by Hibernate (`ddl-auto=update`) |
| Cache | Redis 7 |
| Auth | Stateless JWT access and refresh tokens (jjwt) |
| Build | Maven (`./mvnw`) |

## Running locally

1. **Start MySQL and Redis**

   ```sh
   export DB_PASSWORD=choose-a-password
   docker compose up -d
   ```

2. **Set the remaining secrets.** Each JWT secret must be at least 32 characters.

   ```sh
   export JWT_SECRET=$(openssl rand -base64 48)
   export JWT_REFRESH_SECRET=$(openssl rand -base64 48)
   ```

3. **(Optional) Create an admin account** for the admin app:

   ```sh
   export BETWISE_ADMIN_EMAIL=you@example.com
   export BETWISE_ADMIN_PASSWORD=change-me
   ```

4. **Run it**

   ```sh
   ./mvnw spring-boot:run
   ```

   The API listens on `http://localhost:8080`.

5. **Run the tests**

   ```sh
   ./mvnw test
   ```

   Spring can't load test classes from a path containing a space, so
   `AuthControllerTest` fails if the project lives under a folder such as
   `Personal Projects`. The other tests are unaffected.

### Configuration

| Setting | Environment variable | Default | Purpose |
|---|---|---|---|
| `spring.datasource.password` | `DB_PASSWORD` | none | MySQL password |
| `jwt.secret` | `JWT_SECRET` | none | Signs access tokens (valid 24 hours) |
| `jwt.refresh-secret` | `JWT_REFRESH_SECRET` | none | Signs refresh tokens (valid 30 days) |
| `betwise.admin.email` | `BETWISE_ADMIN_EMAIL` | empty | Account to create or promote to admin on startup |
| `betwise.admin.password` | `BETWISE_ADMIN_PASSWORD` | empty | Password used only if that account doesn't exist yet |
| `betwise.cors.allowed-origins` | `BETWISE_CORS_ALLOWEDORIGINS` | `http://localhost:5173` | Comma-separated browser origins allowed to call the API |
| `betwise.google.client-ids` | `BETWISE_GOOGLE_CLIENTIDS` | empty | Comma-separated Google OAuth client IDs allowed to sign in. Empty turns Google sign-in off |

### Seed data

On startup, `DataSeeder` fills empty tables with sample content. It never
overwrites or duplicates existing rows.

- **12 assets:** AAPL, NVDA, MSFT, VOO, TSLA, AMZN, GOOGL, QQQ, BND, TLT, O, VNQ
- **4 lesson categories and 9 published lessons**, each with 3 sections and a 2-question quiz
- **6 news articles**

## How it works

### Authentication

`POST /api/auth/login` returns an access token and a refresh token. Send the
access token on every protected request:

```http
Authorization: Bearer <access_token>
```

When it expires, exchange the refresh token at `POST /api/auth/refresh`.

| Status | Meaning |
|---|---|
| `401` | Missing, invalid or expired token. Refresh it or sign in again. |
| `403` | Signed in, but not allowed. For example, a non-admin calling `/api/admin/**`. |

### Signing in with Google

The app runs Google's sign-in itself, using OAuth with PKCE, and sends the
resulting ID token to `POST /api/auth/google`. The backend only trusts the
token after checking that it:

- is signed by one of Google's current public keys (fetched from Google and cached)
- was issued by `accounts.google.com`
- is addressed to one of the client IDs in `betwise.google.client-ids`
- hasn't expired
- belongs to a verified email address
- carries the one-time `nonce` the app generated for that sign-in, so a token
  can't be replayed from another sign-in

Accounts are matched on Google's stable account ID (`sub`), not on email.
The first Google sign-in either creates an account, or links to an existing
email-and-password account with the same verified email. Accounts created
with Google have no password until the user sets one.

### Password security

- Passwords are hashed with BCrypt. They must be 8 to 72 bytes, the most
  BCrypt uses.
- After 5 wrong passwords for an email, that email is blocked from signing in
  for 15 minutes, and requests get `429`. A successful sign-in resets the
  count. The count is kept in memory, so move it to Redis if you run more than
  one backend instance.
- Wrong password and unknown email return the same message and take the same
  time, so responses don't reveal which emails have accounts.

### Roles

Every user is `USER` or `ADMIN`. The role is read from the database on every
request, so changing it takes effect immediately without a new token.
Everything under `/api/admin/**` requires `ADMIN`.

### Prices

All prices come from `MarketPriceService`. The current implementation,
`MockMarketPriceService`, uses fixed prices and generates repeatable price
histories. To use real market data, write another implementation of that
interface; nothing else needs to change.

Trades always execute at the server's current price. The client can't choose
the price.

### Lessons and quizzes

- A lesson is an ordered list of **sections** (checkpoints) written in
  Markdown. Finishing a section advances the user's `completedCheckpoints`.
- Finishing the last section and calling `/complete` pays the lesson's cash
  reward into the user's chosen portfolio.
- A quiz unlocks once `completedCheckpoints` reaches its `checkpointOrder`.
- Lessons have a draft, published or archived status. The app only sees
  published lessons that aren't archived.
- Quiz `choices` are stored as a JSON array string, such as `["A","B","C"]`.

### Notifications

The server creates a notification when:

| Type | When |
|---|---|
| `TRADE_FILLED` | A buy or sell order fills |
| `CASH_TRANSFER` | Cash moves between portfolios |
| `LESSON_REWARD` | A lesson is completed and its reward is paid |
| `QUIZ_COMPLETED` | A quiz is scored |

Each notification is saved in the same database transaction as the action, so
it only appears if the action actually succeeded.

### Errors

Every error returns JSON with a readable message:

```json
{
  "timestamp": "2026-10-28T14:03:12",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient cash balance",
  "path": "/api/portfolios/1/buy"
}
```

| Status | Used for |
|---|---|
| `400` | Validation and business rules |
| `401` | Bad credentials or token |
| `403` | Not allowed |
| `404` | Not found, or belongs to another user |
| `409` | Conflicts such as duplicate names, or deleting something people have used |
| `429` | Too many failed sign-in attempts. Try again later |
| `500` | Unexpected errors. Details are logged on the server, not sent to the client |

---

# API Endpoints

Unless marked **Public**, every endpoint needs `Authorization: Bearer <access_token>`.
Dates are ISO-8601 without a time zone, such as `2026-10-28T14:03:12`. Money and
quantities are JSON numbers.

## Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | **Public.** Log in. Body: `{ email, password }`. Returns `{ accessToken, refreshToken, user }` |
| POST | `/api/auth/google` | **Public.** Sign in or sign up with Google. Body: `{ idToken, nonce }`. Returns `{ accessToken, refreshToken, user }` |
| POST | `/api/auth/refresh` | **Public.** Body: `{ refreshToken }`. Returns `{ accessToken }` |

## Users

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | **Public.** Create an account. Body: `{ name, email, password, dateOfBirth?, experienceLevel?, investmentGoal?, riskTolerance? }`. Returns `201` |
| GET | `/api/users/me` | Get the signed-in user's profile, including `role`, `hasPassword` and `googleLinked` |
| PATCH | `/api/users/me` | Update the profile. Fields left out stay unchanged |
| PATCH | `/api/users/me/password` | Body: `{ currentPassword, newPassword }`. Accounts created with Google that have no password can leave out `currentPassword`. Returns `204` |
| DELETE | `/api/users/me` | Delete the account and everything it owns. Returns `204` |

## Assets

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/assets` | All assets with `currentPrice`, `dayChangePercent` and a small `sparkline` of today's prices |
| GET | `/api/assets/{id}` | One asset |
| GET | `/api/assets/search?query={query}` | Search by ticker or name |
| GET | `/api/assets/{id}/history?range={range}` | Price chart. `range` is `1D`, `1W`, `1M`, `3M`, `1Y` or `ALL` (default `1D`). Returns `{ range, changePercent, points: [{ timestamp, value }] }` |

## Portfolios

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/portfolios` | Create a portfolio. Body: `{ name, startingBalance }`. Returns `201` |
| GET | `/api/portfolios` | The user's portfolios, with holdings and current value |
| GET | `/api/portfolios/{id}` | One portfolio, with holdings and current value |
| PATCH | `/api/portfolios/{id}` | Rename. Body: `{ name }` |
| DELETE | `/api/portfolios/{id}` | Delete, with its holdings and history. Returns `204` |
| GET | `/api/portfolios/{id}/holdings` | Holdings with market value and gain |
| GET | `/api/portfolios/{id}/transactions` | Buy and sell history, newest first |
| GET | `/api/portfolios/{id}/history?range={range}` | Total value over time, rebuilt from the portfolio's trades and cash movements. Same ranges as assets (default `1M`) |
| POST | `/api/portfolios/{id}/buy` | Buy at the current market price. Body: `{ assetId, quantity }`. Returns `201` |
| POST | `/api/portfolios/{id}/sell` | Sell at the current market price. Body: `{ assetId, quantity }`. Returns `201` |
| POST | `/api/portfolios/{id}/transfer` | Move cash. Body: `{ destinationPortfolioId, amount }`. Returns `204` |

## Watchlists

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/watchlists` | Create a watchlist. Body: `{ name }`. Returns `201` |
| GET | `/api/watchlists` | The user's watchlists, with items |
| GET | `/api/watchlists/{id}` | One watchlist |
| PATCH | `/api/watchlists/{id}` | Rename. Body: `{ name }` |
| DELETE | `/api/watchlists/{id}` | Delete. Returns `204` |
| POST | `/api/watchlists/{id}/items` | Add an asset. Body: `{ assetId }`. Returns the whole watchlist, `201` |
| DELETE | `/api/watchlists/{watchlistId}/items/{itemId}` | Remove an asset. Returns `204` |

## Lesson Categories

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lesson-categories` | All lesson categories |

## Lessons

These only return published lessons that aren't archived.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lessons` | All lessons in display order, each with `checkpointCount` |
| GET | `/api/lessons?category={slug}` | Lessons in one category |
| GET | `/api/lessons/search?query={query}` | Search by title |
| GET | `/api/lessons/{id}` | One lesson |
| GET | `/api/lessons/{id}/checkpoints` | The lesson's sections in reading order: `[{ id, position, title, body }]` |
| POST | `/api/lessons/{id}/start` | Start the lesson, creating progress |
| GET | `/api/lessons/{id}/progress` | The user's progress |
| PATCH | `/api/lessons/{id}/progress` | Body: `{ currentCheckpoint?, completedCheckpoints? }` |
| POST | `/api/lessons/{id}/complete` | Finish the lesson and pay its reward. Body: `{ portfolioId }` |
| GET | `/api/lessons/{id}/quizzes` | The lesson's quizzes, with `locked` and questions |

## Quizzes

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/quizzes/search?query={query}` | Search by title |
| GET | `/api/quizzes/{id}` | One quiz with its questions. Correct answers are never included |
| POST | `/api/quizzes/{id}/start` | Start an unlocked quiz |
| POST | `/api/quizzes/{id}/answers` | Answer a question. Body: `{ questionId, selectedAnswer }`. Returns `{ questionId, correct }` |
| GET | `/api/quizzes/{id}/progress` | The user's progress: `{ quizId, currentQuestion, score, completed }` |
| POST | `/api/quizzes/{id}/complete` | Score the quiz once every question is answered. Body: `{}` |

## Notifications

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | The user's notifications, newest first |
| GET | `/api/notifications/unread-count` | Returns `{ count }`, for a badge |
| PATCH | `/api/notifications/{id}/read` | Mark one notification as read. Returns it |
| POST | `/api/notifications/read-all` | Mark every notification as read. Returns `204` |

A notification looks like this:

```json
{
  "id": 7,
  "type": "TRADE_FILLED",
  "title": "Order filled",
  "body": "Bought 2 AAPL at $200.00 for $400.00.",
  "relatedId": 42,
  "read": false,
  "readAt": null,
  "createdAt": "2026-10-28T15:00:00"
}
```

`relatedId` points to what the notification is about, depending on `type`:

| Type | `relatedId` |
|---|---|
| `TRADE_FILLED` | The transaction |
| `CASH_TRANSFER` | The destination portfolio |
| `LESSON_REWARD` | The lesson |
| `QUIZ_COMPLETED` | The quiz |

## News

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/news` | Articles, newest first: `[{ id, category, headline, summary, source, tickers, publishedAt }]` |
| GET | `/api/news?category={category}` | Filter by `markets`, `economy`, `tech` or `earnings` |

## Admin

Every admin endpoint requires the `ADMIN` role. These see every lesson,
including drafts and archived lessons.

### Categories

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/categories` | All categories, each with `lessonCount` |
| POST | `/api/admin/categories` | Body: `{ name, slug?, description? }`. A missing slug is generated from the name. Returns `201` |
| PATCH | `/api/admin/categories/{id}` | Update any field |
| DELETE | `/api/admin/categories/{id}` | Only allowed for categories with no lessons. Returns `204` |

### Lessons

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/lessons` | Every lesson, with status and counts of sections, quizzes and learners |
| GET | `/api/admin/lessons/{id}` | A lesson with its sections and quizzes, including correct answers |
| POST | `/api/admin/lessons` | Create a draft. Body: `{ title, description?, categoryId, topic, difficulty, cashReward?, position? }`. Returns `201` |
| PATCH | `/api/admin/lessons/{id}` | Update any field, including `published` and `archived`. Publishing needs at least one section |
| DELETE | `/api/admin/lessons/{id}` | Only allowed if nobody has started the lesson; archive it otherwise. Returns `204` |
| PUT | `/api/admin/lessons/{id}/checkpoints` | Replace the sections, in order. Body: `[{ title, body }]` |

### Quizzes

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/lessons/{id}/quizzes` | Body: `{ title, checkpointOrder, questions: [{ question, choices, correctAnswer }] }`. Returns `201` |
| PUT | `/api/admin/quizzes/{id}` | Replace the title, unlock point and questions. Include a question's `id` to keep it |
| DELETE | `/api/admin/quizzes/{id}` | Only allowed if nobody has taken the quiz. Returns `204` |

Quiz validation rules:

- A quiz needs at least one question.
- Each question needs at least two distinct choices.
- The correct answer must be one of the choices.
- `checkpointOrder` must point to a section that exists.
- Questions that have already been answered can be edited but not removed.
