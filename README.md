# Betwise

Betwise is an app for learning to invest with practice money. You get a
paper-trading portfolio, buy and sell stocks, ETFs, bonds and REITs at market
prices, and earn extra buying power by finishing short lessons and quizzes.

## What's in this repo

| Folder | What it is | Tech |
|---|---|---|
| [`back-end/`](back-end/README.md) | The API: accounts, portfolios, trading, watchlists, lessons, quizzes, news and notifications | Java 21, Spring Boot 4, MySQL, Redis, JWT |
| [`front-end/`](front-end/README.md) | The iOS app | SwiftUI, Swift Charts, iOS 27 |
| [`admin/`](admin/README.md) | A web app for writing lessons, sections and quizzes | React, TypeScript, Vite |

```
 iOS app ───────┐
                ├──►  Spring Boot API  ──►  MySQL
 Admin app ─────┘     localhost:8080        Redis
```

The iOS app and the admin app both talk to the same API, which holds all the
data and business rules. Neither one touches the database directly.

## Getting started

1. **Backend.** Start MySQL and Redis, set the secrets, and run the API. Steps
   are in [`back-end/README.md`](back-end/README.md#running-locally). On first
   start it seeds sample assets, lessons, quizzes and news.
2. **iOS app.** Open `front-end/front-end.xcodeproj` in Xcode and run it on an
   iPhone simulator. Create an account, or explore with sample data without a
   backend. See [`front-end/README.md`](front-end/README.md).
3. **Admin app (optional).** Start the backend with `BETWISE_ADMIN_EMAIL` and
   `BETWISE_ADMIN_PASSWORD` set, then run `npm install && npm run dev` in
   `admin/`. See [`admin/README.md`](admin/README.md).

## Features

- **Portfolios and trading:** create a portfolio with practice cash and trade
  at the server's current price. Track holdings, returns, allocation and value
  over time.
- **Market:** browse and search assets, with price charts from 1 day to 5 years,
  and keep a watchlist.
- **Learn and earn:** read lessons section by section, take quizzes that unlock
  as you go, and earn cash rewards into your portfolio.
- **News:** market stories tagged with the tickers they mention.
- **Notifications:** filled orders, cash transfers, lesson rewards and quiz
  scores.
- **Accounts:** sign up, sign in, edit your profile, change your password,
  delete your account.
- **Admin:** draft, publish and archive lessons; edit sections with a live
  preview; build quizzes.

## API reference

The full endpoint list, with request bodies, status codes and error format, is
in [`back-end/README.md`](back-end/README.md#api-endpoints).
