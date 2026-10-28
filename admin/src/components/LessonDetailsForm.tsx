import { useState, type FormEvent } from "react";

import { api } from "../api";
import { DIFFICULTIES, labelFor, TOPICS, type Category, type Difficulty, type LessonDetail, type Topic } from "../types";

export function LessonDetailsForm({
  lesson,
  categories,
  onSaved,
}: {
  lesson: LessonDetail;
  categories: Category[];
  onSaved: (lesson: LessonDetail) => void;
}) {
  const { summary } = lesson;

  const [title, setTitle] = useState(summary.title);
  const [description, setDescription] = useState(lesson.description ?? "");
  const [categoryId, setCategoryId] = useState(summary.categoryId);
  const [topic, setTopic] = useState<Topic>(summary.topic);
  const [difficulty, setDifficulty] = useState<Difficulty>(summary.difficulty);
  const [reward, setReward] = useState(String(summary.cashReward));
  const [position, setPosition] = useState(String(summary.position));
  const [error, setError] = useState<string | null>(null);
  const [savedAt, setSavedAt] = useState<Date | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsSaving(true);

    try {
      const saved = await api.lessons.update(summary.id, {
        title,
        description,
        categoryId,
        topic,
        difficulty,
        cashReward: Number(reward),
        position: Number(position),
      });
      onSaved(saved);
      setSavedAt(new Date());
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't save");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="card section" onSubmit={submit}>
      <div className="section-header">
        <h2>Details</h2>
        {savedAt && <span className="muted small">Saved {savedAt.toLocaleTimeString()}</span>}
      </div>

      <label className="field">
        <span>Title</span>
        <input value={title} onChange={(event) => setTitle(event.target.value)} required />
      </label>

      <label className="field">
        <span>Summary</span>
        <textarea
          rows={3}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          placeholder="Shown on the lesson's page in the app"
        />
      </label>

      <div className="row">
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

        <label className="field">
          <span>Order</span>
          <input type="number" step="1" value={position} onChange={(event) => setPosition(event.target.value)} />
        </label>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="section-actions">
        <button className="button primary" type="submit" disabled={isSaving}>
          {isSaving ? "Saving…" : "Save details"}
        </button>
      </div>
    </form>
  );
}
