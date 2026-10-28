import { useEffect, useState } from "react";

import { hasToken, setSessionExpiredHandler, setToken } from "./api";
import { navigate, useRoute } from "./router";
import type { User } from "./types";
import { CategoriesPage } from "./pages/CategoriesPage";
import { LessonEditorPage } from "./pages/LessonEditorPage";
import { LessonsPage } from "./pages/LessonsPage";
import { LoginPage } from "./pages/LoginPage";

const USER_KEY = "betwise_admin_user";

function storedUser(): User | null {
  const raw = sessionStorage.getItem(USER_KEY);

  if (!raw || !hasToken()) {
    return null;
  }

  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

export function App() {
  const [user, setUser] = useState<User | null>(storedUser);
  const route = useRoute();

  useEffect(() => {
    setSessionExpiredHandler(() => setUser(null));
  }, []);

  function signIn(signedIn: User, accessToken: string) {
    setToken(accessToken);
    sessionStorage.setItem(USER_KEY, JSON.stringify(signedIn));
    setUser(signedIn);
  }

  function signOut() {
    setToken(null);
    sessionStorage.removeItem(USER_KEY);
    setUser(null);
  }

  if (!user) {
    return <LoginPage onSignIn={signIn} />;
  }

  return (
    <div className="shell">
      <header className="topbar">
        <button className="brand" onClick={() => navigate("/lessons")}>
          <span className="brand-mark">B</span>
          Betwise Admin
        </button>

        <nav className="tabs">
          <a
            href="#/lessons"
            className={route.page === "categories" ? "" : "active"}
          >
            Lessons
          </a>
          <a href="#/categories" className={route.page === "categories" ? "active" : ""}>
            Categories
          </a>
        </nav>

        <div className="account">
          <span className="muted">{user.email}</span>
          <button className="button ghost small" onClick={signOut}>
            Sign out
          </button>
        </div>
      </header>

      <main className="content">
        {route.page === "lessons" && <LessonsPage />}
        {route.page === "lesson" && <LessonEditorPage key={route.id} lessonId={route.id} />}
        {route.page === "categories" && <CategoriesPage />}
      </main>
    </div>
  );
}
