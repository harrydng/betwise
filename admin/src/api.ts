import type {
  Category,
  CategoryInput,
  Checkpoint,
  LessonDetail,
  LessonInput,
  LessonSummary,
  LoginResponse,
  Quiz,
  QuizInput,
} from "./types";

const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? "http://localhost:8080";
const TOKEN_KEY = "betwise_admin_token";

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

// Session storage clears when the tab closes, which suits an admin tool.
let token: string | null = sessionStorage.getItem(TOKEN_KEY);
let onSessionExpired: (() => void) | null = null;

export function setToken(value: string | null) {
  token = value;

  if (value) {
    sessionStorage.setItem(TOKEN_KEY, value);
  } else {
    sessionStorage.removeItem(TOKEN_KEY);
  }
}

export function hasToken() {
  return token !== null;
}

export function setSessionExpiredHandler(handler: () => void) {
  onSessionExpired = handler;
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let response: Response;

  try {
    response = await fetch(BASE_URL + path, {
      method,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, `Can't reach the backend at ${BASE_URL}. Is it running?`);
  }

  if (!response.ok) {
    // The backend's error JSON carries a human-readable message.
    let message = `Request failed (${response.status})`;

    try {
      const errorBody = await response.json();
      if (typeof errorBody?.message === "string") {
        message = errorBody.message;
      }
    } catch {
      // Empty body; keep the generic message.
    }

    if (response.status === 401 && token) {
      setToken(null);
      onSessionExpired?.();
      message = "Your session expired. Sign in again.";
    } else if (response.status === 403) {
      message = "This account isn't an admin.";
    }

    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  login: (email: string, password: string) =>
    request<LoginResponse>("POST", "/api/auth/login", { email, password }),

  categories: {
    list: () => request<Category[]>("GET", "/api/admin/categories"),
    create: (input: CategoryInput) => request<Category>("POST", "/api/admin/categories", input),
    update: (id: number, input: CategoryInput) =>
      request<Category>("PATCH", `/api/admin/categories/${id}`, input),
    remove: (id: number) => request<void>("DELETE", `/api/admin/categories/${id}`),
  },

  lessons: {
    list: () => request<LessonSummary[]>("GET", "/api/admin/lessons"),
    get: (id: number) => request<LessonDetail>("GET", `/api/admin/lessons/${id}`),
    create: (input: LessonInput) => request<LessonDetail>("POST", "/api/admin/lessons", input),
    update: (id: number, input: LessonInput) =>
      request<LessonDetail>("PATCH", `/api/admin/lessons/${id}`, input),
    remove: (id: number) => request<void>("DELETE", `/api/admin/lessons/${id}`),
    saveCheckpoints: (id: number, checkpoints: Checkpoint[]) =>
      request<Checkpoint[]>(
        "PUT",
        `/api/admin/lessons/${id}/checkpoints`,
        checkpoints.map(({ title, body }) => ({ title, body })),
      ),
  },

  quizzes: {
    create: (lessonId: number, input: QuizInput) =>
      request<Quiz>("POST", `/api/admin/lessons/${lessonId}/quizzes`, input),
    update: (id: number, input: QuizInput) => request<Quiz>("PUT", `/api/admin/quizzes/${id}`, input),
    remove: (id: number) => request<void>("DELETE", `/api/admin/quizzes/${id}`),
  },
};
