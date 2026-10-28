import { useEffect, useMemo, useState, type FormEvent } from "react";

import { api } from "../api";
import { lessonStatus, StatusPill } from "../components/StatusPill";
import { navigate } from "../router";
import { DIFFICULTIES, labelFor, TOPICS, type Category, type Difficulty, type LessonSummary, type Topic } from "../types";

type Filter = "all" | "published" | "draft" | "archived";

const FILTERS: { value: Filter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "published", label: "Published" },
  { value: "draft", label: "Drafts" },
  { value: "archived", label: "Archived" },
];

export function LessonsPage() {
  const [lessons, setLessons] = useState<LessonSummary[] | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [filter, setFilter] = useState<Filter>("all");
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    Promise.all([api.lessons.list(), api.categories.list()])
      .then(([loadedLessons, loadedCategories]) => {
        setLessons(loadedLessons);
        setCategories(loadedCategories);
      })
      .catch((caught: Error) => setError(caught.message));
  }, []);

  const visible = useMemo(() => {
    const query = search.trim().toLowerCase();

    return (lessons ?? []).filter(
      (lesson) =>
        (filter === "all" || lessonStatus(lesson) === filter) &&
        (!query || lesson.title.toLowerCase().includes(query) || lesson.category.toLowerCase().includes(query)),
    );
  }, [lessons, filter, search]);

  return (
    <section>
      <div className="page-header">
        <div>
          <h1>Lessons</h1>
          <p className="muted">Drafts are only visible here. Publish a lesson to show it in the app.</p>
        </div>
        <button className="button primary" onClick={() => setIsCreating(true)} disabled={categories.length === 0}>
          New lesson
        </button>
      </div>

      {categories.length === 0 && lessons !== null && (
        <p className="notice">
          Create a <a href="#/categories">category</a> first. Every lesson belongs to one.
        </p>
      )}

      <div className="toolbar">
        <div className="segmented">
          {FILTERS.map((option) => (
            <button
              key={option.value}
              className={filter === option.value ? "active" : ""}
              onClick={() => setFilter(option.value)}
            >
              {option.label}
            </button>
          ))}
        </div>
        <input
          className="search"
          placeholder="Search lessons"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      {error && <p className="error">{error}</p>}

      {lessons === null && !error ? (
        <p className="muted">Loading…</p>
      ) : visible.length === 0 ? (
        <div className="card empty">No lessons match.</div>
      ) : (
        <div className="card table-card">
          <table>
            <thead>
              <tr>
                <th className="narrow">#</th>
                <th>Lesson</th>
                <th>Status</th>
                <th className="number">Sections</th>
                <th className="number">Quizzes</th>
                <th className="number">Learners</th>
                <th className="number">Reward</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((lesson) => (
                <tr key={lesson.id} className="clickable" onClick={() => navigate(`/lessons/${lesson.id}`)}>
                  <td className="narrow muted">{lesson.position}</td>
                  <td>
                    <div className="cell-title">{lesson.title}</div>
                    <div className="muted small">
                      {lesson.category} · {labelFor(lesson.difficulty)}
                    </div>
                  </td>
                  <td>
                    <StatusPill published={lesson.published} archived={lesson.archived} />
                  </td>
                  <td className="number">{lesson.checkpointCount}</td>
                  <td className="number">{lesson.quizCount}</td>
                  <td className="number">{lesson.learnerCount}</td>
                  <td className="number">${lesson.cashReward}</td>
                  <td className="muted small">{new Date(lesson.updatedAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {isCreating && <NewLessonDialog categories={categories} onClose={() => setIsCreating(false)} />}
    </section>
  );
}

function NewLessonDialog({ categories, onClose }: { categories: Category[]; onClose: () => void }) {
  const [title, setTitle] = useState("");
  const [categoryId, setCategoryId] = useState(categories[0]?.id ?? 0);
  const [topic, setTopic] = useState<Topic>("STOCKS");
  const [difficulty, setDifficulty] = useState<Difficulty>("BEGINNER");
  const [reward, setReward] = useState("50");
  const [error, setError] = useState<string | null>(null);
  const [isWorking, setIsWorking] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsWorking(true);

    try {
      const created = await api.lessons.create({
        title,
        categoryId,
        topic,
        difficulty,
        cashReward: Number(reward) || 0,
      });
      navigate(`/lessons/${created.summary.id}`);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't create the lesson");
      setIsWorking(false);
    }
  }

  return (
    <div className="overlay" onClick={onClose}>
      <form className="card dialog" onClick={(event) => event.stopPropagation()} onSubmit={submit}>
        <h2>New lesson</h2>
        <p className="muted">It starts as a draft. Add sections, then publish it.</p>

        <label className="field">
          <span>Title</span>
          <input value={title} onChange={(event) => setTitle(event.target.value)} autoFocus required />
        </label>

        <label className="field">
          <span>Category</span>
          <select value={categoryId} onChange={(event) => setCategoryId(Number(event.target.value))}>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>

        <div className="row">
          <label className="field">
            <span>Topic</span>
            <select value={topic} onChange={(event) => setTopic(event.target.value as Topic)}>
              {TOPICS.map((value) => (
                <option key={value} value={value}>
                  {labelFor(value)}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Difficulty</span>
            <select value={difficulty} onChange={(event) => setDifficulty(event.target.value as Difficulty)}>
              {DIFFICULTIES.map((value) => (
                <option key={value} value={value}>
                  {labelFor(value)}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Reward ($)</span>
            <input type="number" min="0" step="1" value={reward} onChange={(event) => setReward(event.target.value)} />
          </label>
        </div>

        {error && <p className="error">{error}</p>}

        <div className="dialog-actions">
          <button type="button" className="button ghost" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="button primary" disabled={isWorking}>
            {isWorking ? "Creating…" : "Create draft"}
          </button>
        </div>
      </form>
    </div>
  );
}
