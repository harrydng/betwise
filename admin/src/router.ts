import { useEffect, useState } from "react";

// Hash-based routes (#/lessons/12) keep the app a static bundle with no
// server-side routing setup.

export type Route =
  | { page: "lessons" }
  | { page: "lesson"; id: number }
  | { page: "categories" };

function parse(hash: string): Route {
  const parts = hash.replace(/^#\/?/, "").split("/");

  if (parts[0] === "lessons" && parts[1]) {
    const id = Number(parts[1]);
    if (Number.isInteger(id)) {
      return { page: "lesson", id };
    }
  }

  if (parts[0] === "categories") {
    return { page: "categories" };
  }

  return { page: "lessons" };
}

export function navigate(path: string) {
  window.location.hash = path;
}

export function useRoute(): Route {
  const [route, setRoute] = useState(() => parse(window.location.hash));

  useEffect(() => {
    const update = () => setRoute(parse(window.location.hash));
    window.addEventListener("hashchange", update);
    return () => window.removeEventListener("hashchange", update);
  }, []);

  return route;
}
