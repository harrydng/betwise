# API Endpoints

## Public Endpoints

Endpoints do not require authentication.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a new user account |
| POST | `/api/auth/login` | Log in and return an access token and refresh token |
| POST | `/api/auth/refresh` | Generate a new access token using a refresh token |

---

## User

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a new account |
| GET | `/api/users/me` | Get the currently authenticated user's profile |
| PATCH | `/api/users/me` | Update the authenticated user's profile |
| PATCH | `/api/users/me/password` | Change the authenticated user's password |
| DELETE | `/api/users/me` | Delete the authenticated user's account |

---

## Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authenticate a user and return access and refresh tokens |
| POST | `/api/auth/refresh` | Exchange a valid refresh token for a new access token |

---

## Watchlists

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/watchlists` | Create a new watchlist |
| GET | `/api/watchlists` | Get all watchlists belonging to the authenticated user |
| GET | `/api/watchlists/{id}` | Get a specific watchlist owned by the authenticated user |
| PATCH | `/api/watchlists/{id}` | Rename or update a watchlist |
| DELETE | `/api/watchlists/{id}` | Delete a watchlist |
| POST | `/api/watchlists/{id}/items` | Add an asset to a watchlist |
| DELETE | `/api/watchlists/{watchlistId}/items/{itemId}` | Remove an asset from a watchlist |

---

## Portfolios

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/portfolios` | Create a new portfolio |
| GET | `/api/portfolios` | Get all portfolios belonging to the authenticated user |
| GET | `/api/portfolios/{id}` | Get a specific portfolio with its holdings and current portfolio value |
| PATCH | `/api/portfolios/{id}` | Rename or update a portfolio |
| DELETE | `/api/portfolios/{id}` | Delete a portfolio |
| GET | `/api/portfolios/{id}/holdings` | Get all holdings in a portfolio with current market values |
| GET | `/api/portfolios/{id}/transactions` | Get the portfolio's buy and sell transaction history |
| POST | `/api/portfolios/{id}/buy` | Buy an asset using the portfolio's available cash |
| POST | `/api/portfolios/{id}/sell` | Sell an asset from the portfolio |
| POST | `/api/portfolios/{id}/transfer` | Transfer available cash from one portfolio to another |

---

## Lesson Categories

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lesson-categories` | Get all lesson categories |

---

## Lessons

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lessons` | Get all available lessons |
| GET | `/api/lessons?category={category}` | Get lessons belonging to a specific category |
| GET | `/api/lessons/search?query={query}` | Search for lessons by title |
| GET | `/api/lessons/{id}` | Get a specific lesson |
| POST | `/api/lessons/{id}/start` | Start a lesson and create lesson progress |
| GET | `/api/lessons/{id}/progress` | Get the authenticated user's progress for a lesson |
| PATCH | `/api/lessons/{id}/progress` | Update the authenticated user's lesson progress |
| POST | `/api/lessons/{id}/complete` | Complete a lesson and allocate its reward to a selected portfolio |
| GET | `/api/lessons/{id}/quizzes` | Get all quizzes belonging to a lesson |

---

## Quizzes

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/quizzes/search?query={query}` | Search for quizzes by title |
| GET | `/api/quizzes/{id}` | Get a specific quiz and its questions |
| POST | `/api/quizzes/{id}/start` | Start an unlocked quiz |
| POST | `/api/quizzes/{id}/answers` | Submit an answer to a quiz question |
| GET | `/api/quizzes/{id}/progress` | Get the authenticated user's progress for a quiz |
| POST | `/api/quizzes/{id}/complete` | Complete a quiz and calculate the final score |

---

## Authentication

Protected endpoints require an access token in the request header:

```http
Authorization: Bearer <access_token>