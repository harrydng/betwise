# Betwise iOS App

Betwise is an app for learning to invest with practice money. You build a
paper-trading portfolio, buy and sell stocks, ETFs, bonds and REITs at market
prices, and earn extra buying power by finishing short lessons and quizzes.

This is the SwiftUI app. It talks to the Spring Boot API in `../back-end`.

## Requirements

- Xcode with the iOS 27 SDK. The deployment target is iOS 27, because the app
  uses Liquid Glass and the latest SwiftUI APIs.
- The backend running on `http://localhost:8080`, for anything beyond guest
  mode. See `../back-end/README.md`.

## Running

1. Open `front-end.xcodeproj`.
2. Pick an iPhone simulator and run the `betwise` scheme.
3. On the welcome screen, either:
   - **Create account** or **Sign in** to use real data from the backend, or
   - tap **Explore with sample data** to look around without a backend.

The API address is set in `Services/Network/APIClient.swift` (`baseURL`).
`localhost` only works in the Simulator. On a physical iPhone, use your Mac's
local network address instead, and the backend has to be reachable on that
network.

## Setting up Google sign-in

"Continue with Google" needs an OAuth client ID. Until one is set, tapping it
shows a "not set up yet" message, and email sign-in keeps working.

1. In [Google Cloud Console](https://console.cloud.google.com/), open
   **APIs & Services → OAuth consent screen** and configure it.
2. Under **Credentials → Create credentials → OAuth client ID**, choose **iOS**
   and enter this app's bundle identifier.
3. Paste the client ID (`1234-abcd.apps.googleusercontent.com`) into
   `GoogleSignInConfiguration.clientID` in `Services/Auth/GoogleSignIn.swift`.
4. Give the backend the same ID: `BETWISE_GOOGLE_CLIENTIDS=1234-abcd.apps.googleusercontent.com`.

How sign-in works:

1. The app opens Google's sign-in page in Apple's secure web authentication
   sheet. The app never sees the Google password.
2. It uses the authorization code flow with PKCE, a random `state` to reject
   forged redirects, and a `nonce` that the backend checks.
3. The app exchanges the code for an ID token and sends it to the backend. The
   backend verifies it with Google before issuing Betwise tokens.

No Info.plist URL scheme is needed; the web authentication sheet catches
Google's redirect itself.

## Features

| Tab | What it does |
|---|---|
| **Portfolio** | Total balance with an interactive chart (1D to ALL); buying power, invested amount and total return; an allocation bar; holdings; recent activity. Profile and notifications are in the toolbar |
| **Market** | Index cards, your watchlist, and every asset filtered by type. Each asset has a detail page with a price chart, your position, key stats, and Buy and Sell buttons |
| **Learn** | Lesson progress and rewards earned. A lesson reader steps through sections and pays the reward at the end. Quizzes unlock as you read |
| **News** | Market stories by category, tagged with tickers |
| **Search** | Live search across all assets |

Also included:

- **Accounts:** sign up and sign in with Google or with email and password;
  edit profile, change password (or set one for Google accounts), sign out,
  delete account.
- **Notifications:** an unread badge on the bell. Mark one as read by tapping
  or swiping, or mark all as read.
- **Trading:** an order ticket that checks buying power and shares owned. Orders
  fill at the server's current price.

## Guest mode and real data

The app runs in one of two modes, decided at sign-in:

- **Guest** ("Explore with sample data"): every screen shows the sample content
  in `Core/SampleData`. Nothing is sent to the backend. Trading and starting
  lessons ask you to sign in.
- **Signed in:** every screen loads from the backend. Signed-in users never see
  sample data. Empty states appear instead, for example a "Create your
  portfolio" card for a new account.

`MainTabView` creates a fresh set of view models for each session, so guest
data can't leak into a real account.

Market index cards (S&P 500, Nasdaq and so on) are always sample data, because
the backend has no index endpoint yet.

## Project structure

```
betwise/
├── ContentView.swift        Root view: welcome screen or the tab bar, by session
├── Core/
│   ├── SampleData/          Guest-mode content and generated sample price charts
│   ├── Theme/               AppColors, the black-and-blue palette
│   └── Utilities/           Decimal helpers, stable hashing for repeatable data
├── Models/                  Codable types matching the backend's JSON, by feature
├── Services/
│   ├── Network/             APIClient, APIError, JSON date handling, Keychain token storage
│   └── <Feature>/           One service per API area (Portfolio, Lesson, Quiz, …)
├── ViewModels/              @Observable models: Session, Portfolio, Market, Lessons, News, Notifications
└── Views/
    ├── Components/          Reusable pieces: cards, charts, badges, form fields
    └── Screens/             One folder per feature: Auth, Portfolio, Market, Learn, News, Profile, Notifications
```

## How it's built

- **SwiftUI + Observation:** view models use `@Observable` with main-actor
  isolation, which is the project default. They cache derived lists, such as
  filtered rows, so views only read prepared data. Combine isn't used.
- **Networking:** `APIClient` is the only place that talks HTTP.
  - It refreshes the access token shortly before it expires, and shares one
    refresh between concurrent requests.
  - On a rejected session it signs the user out.
  - It turns the backend's error JSON into readable `APIError` messages.
- **Tokens** are stored in the Keychain, not `UserDefaults`.
- **Money and quantities** use `Decimal`, so values like `$224.47` stay exact.
- **Dates:** the backend sends ISO-8601 dates without a time zone. `JSONCoding.swift`
  decodes them in the device's time zone.
- **Charts** use Swift Charts. Price charts can be scrubbed, and the value under
  your finger shows above the chart.
- **Design:** pure black backgrounds, near-black cards with a hairline border,
  one electric-blue accent, and green or red only for gains and losses. The tab
  bar and buttons use Liquid Glass.
- **Accessibility:** text uses Dynamic Type styles. Rows combine into single
  VoiceOver elements, and decorative images are hidden from VoiceOver.

## Lesson content

Lessons are written in the admin app (`../admin`), and the reader displays each
section's Markdown. It renders inline styles (**bold**, *italic*, `code`,
links) and keeps line breaks, so lines starting with `- ` read as bullets.
Headings and other block-level Markdown aren't rendered.

## Known gaps

- **Before releasing on the App Store:**
  - Apple requires apps that offer Google sign-in to also offer **Sign in with Apple** (App Review Guideline 4.8).
  - Replace the placeholder "G" in `GoogleSignInButton.swift` with Google's official logo asset, as Google's branding guidelines require.

- Prices come from the backend's mock price service, not a live market feed.
- Notifications are only shown inside the app. There are no push notifications yet.
- The app uses your first portfolio. You can't switch between portfolios yet.
