# Betwise Admin

A web app for writing lessons and quizzes. It talks to the backend's
`/api/admin` endpoints, which only accept users with the `ADMIN` role.

## Setup

1. Start the backend with an admin account configured. On startup it creates
   the account, or promotes it if it already exists:

   ```sh
   BETWISE_ADMIN_EMAIL=you@example.com BETWISE_ADMIN_PASSWORD=change-me ./mvnw spring-boot:run
   ```

2. Install and run the admin app:

   ```sh
   npm install
   npm run dev
   ```

3. Open http://localhost:5173 and sign in with that account.

The app expects the backend at `http://localhost:8080`. To change that, copy
`.env.example` to `.env.local` and edit `VITE_API_URL`. The backend only
accepts requests from origins listed in `betwise.cors.allowed-origins`
(default `http://localhost:5173`).

## Writing lessons

- New lessons start as **drafts** and are only visible here.
- A lesson needs at least one section before it can be published.
- Sections use inline Markdown: `**bold**`, `*italic*`, `` `code` `` and
  `[links](https://…)`. Line breaks are kept, so start a line with `- ` for a
  bullet. The preview matches what the iOS app shows.
- A quiz unlocks after the learner finishes the section you choose.
- Lessons and quizzes that learners have started can't be deleted. Archive a
  lesson instead to hide it from the app.

## Scripts

| Command | Does |
|---|---|
| `npm run dev` | Dev server with hot reload |
| `npm run build` | Type-check and build to `dist/` |
| `npm run typecheck` | Type-check only |
