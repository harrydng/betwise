// Shapes returned by the backend's /api/admin endpoints (AdminDtos.java).

export const TOPICS = [
  "STOCKS",
  "BONDS",
  "ETFS",
  "DIVERSIFICATION",
  "RISK",
  "SHORT_SELLING",
  "FUNDAMENTAL_ANALYSIS",
  "TECHNICAL_ANALYSIS",
  "REAL_ESTATE",
] as const;

export const DIFFICULTIES = ["BEGINNER", "INTERMEDIATE", "ADVANCED"] as const;

export type Topic = (typeof TOPICS)[number];
export type Difficulty = (typeof DIFFICULTIES)[number];

export interface User {
  id: number;
  name: string;
  email: string;
  role: "USER" | "ADMIN";
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  lessonCount: number;
}

export interface CategoryInput {
  name?: string;
  slug?: string;
  description?: string;
}

export interface LessonSummary {
  id: number;
  title: string;
  categoryId: number;
  category: string;
  topic: Topic;
  difficulty: Difficulty;
  cashReward: number;
  position: number;
  published: boolean;
  archived: boolean;
  checkpointCount: number;
  quizCount: number;
  learnerCount: number;
  updatedAt: string;
}

export interface Checkpoint {
  id?: number;
  position?: number;
  title: string;
  body: string;
}

export interface Question {
  id?: number;
  questionOrder?: number;
  question: string;
  choices: string[];
  correctAnswer: string;
}

export interface Quiz {
  id: number;
  title: string;
  checkpointOrder: number;
  hasAttempts: boolean;
  questions: Question[];
}

export interface QuizInput {
  title: string;
  checkpointOrder: number;
  questions: Question[];
}

export interface LessonDetail {
  summary: LessonSummary;
  description: string | null;
  checkpoints: Checkpoint[];
  quizzes: Quiz[];
}

/** Null fields are left unchanged on update. */
export interface LessonInput {
  title?: string;
  description?: string;
  categoryId?: number;
  topic?: Topic;
  difficulty?: Difficulty;
  cashReward?: number;
  position?: number;
  published?: boolean;
  archived?: boolean;
}

export function labelFor(value: string): string {
  return value
    .toLowerCase()
    .split("_")
    .map((word) => (word === "etfs" ? "ETFs" : word[0].toUpperCase() + word.slice(1)))
    .join(" ");
}
