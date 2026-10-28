import { useState, type FormEvent } from "react";

import { api } from "../api";
import type { User } from "../types";

export function LoginPage({ onSignIn }: { onSignIn: (user: User, token: string) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isWorking, setIsWorking] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsWorking(true);

    try {
      const response = await api.login(email.trim(), password);

      // Any account can log in to the API; only admins get into this app.
      if (response.user.role !== "ADMIN") {
        setError("This account isn't an admin. Ask an existing admin to promote it.");
        return;
      }

      onSignIn(response.user, response.accessToken);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't sign in");
    } finally {
      setIsWorking(false);
    }
  }

  return (
    <div className="login">
      <form className="card login-card" onSubmit={submit}>
        <div className="brand-mark large">B</div>
        <h1>Betwise Admin</h1>
        <p className="muted">Sign in with an admin account to manage lessons and quizzes.</p>

        <label className="field">
          <span>Email</span>
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </label>

        {error && <p className="error">{error}</p>}

        <button className="button primary" type="submit" disabled={isWorking}>
          {isWorking ? "Signing in…" : "Sign in"}
        </button>
      </form>
    </div>
  );
}
