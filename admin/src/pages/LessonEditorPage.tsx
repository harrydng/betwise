import { useCallback, useEffect, useState } from "react";

import { api } from "../api";
import { CheckpointsEditor } from "../components/CheckpointsEditor";
import { LessonDetailsForm } from "../components/LessonDetailsForm";
import { QuizzesEditor } from "../components/QuizzesEditor";
import { StatusPill } from "../components/StatusPill";
import { navigate } from "../router";
import type { Category, LessonDetail } from "../types";

export function LessonEditorPage({ lessonId }: { lessonId: number }) {
  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isWorking, setIsWorking] = useState(false);

  const reload = useCallback(async () => {
    try {
      setLesson(await api.lessons.get(lessonId));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't load the lesson");
    }
  }, [lessonId]);

  useEffect(() => {
    reload();
    api.categories.list().then(setCategories).catch(() => setCategories([]));
  }, [reload]);

  if (error) {
    return (
      <section>
        <a href="#/lessons" className="back">
          ← Lessons
        </a>
        <p className="error">{error}</p>
      </section>
    );
  }

  if (!lesson) {
    return <p className="muted">Loading…</p>;
  }

  const { summary } = lesson;

  async function run(action: () => Promise<unknown>) {
    setActionError(null);
    setIsWorking(true);

    try {
      await action();
      await reload();
    } catch (caught) {
      setActionError(caught instanceof Error ? caught.message : "Something went wrong");
    } finally {
      setIsWorking(false);
    }
  }

  async function remove() {
    if (!window.confirm(`Delete "${summary.title}"? This can't be undone.`)) return;

    setActionError(null);

    try {
      await api.lessons.remove(summary.id);
      navigate("/lessons");
    } catch (caught) {
      setActionError(caught instanceof Error ? caught.message : "Couldn't delete");
    }
  }

  return (
    <section>
      <a href="#/lessons" className="back">
        ← Lessons
      </a>

      <div className="page-header">
        <div>
          <div className="title-row">
            <h1>{summary.title}</h1>
            <StatusPill published={summary.published} archived={summary.archived} />
          </div>
          <p className="muted">
            {summary.checkpointCount} sections · {summary.quizCount} quizzes · {summary.learnerCount} learners started
          </p>
        </div>

        <div className="actions">
          {!summary.archived &&
            (summary.published ? (
              <button
                className="button ghost"
                disabled={isWorking}
                onClick={() => run(() => api.lessons.update(summary.id, { published: false }))}
              >
                Unpublish
              </button>
            ) : (
              <button
                className="button primary"
                disabled={isWorking || summary.checkpointCount === 0}
                title={summary.checkpointCount === 0 ? "Add and save at least one section first" : undefined}
                onClick={() => run(() => api.lessons.update(summary.id, { published: true }))}
              >
                Publish
              </button>
            ))}

          <button
            className="button ghost"
            disabled={isWorking}
            onClick={() => run(() => api.lessons.update(summary.id, { archived: !summary.archived }))}
          >
            {summary.archived ? "Restore" : "Archive"}
          </button>

          <button
            className="button danger"
            disabled={summary.learnerCount > 0}
            title={summary.learnerCount > 0 ? "Learners have started this lesson. Archive it instead." : undefined}
            onClick={remove}
          >
            Delete
          </button>
        </div>
      </div>

      {actionError && <p className="error">{actionError}</p>}

      {summary.archived && (
        <p className="notice">Archived lessons are hidden in the app. Learners who started it can still finish.</p>
      )}

      <LessonDetailsForm lesson={lesson} categories={categories} onSaved={setLesson} />

      <CheckpointsEditor
        lessonId={summary.id}
        initial={lesson.checkpoints}
        hasLearners={summary.learnerCount > 0}
        onSaved={reload}
      />

      <QuizzesEditor
        lessonId={summary.id}
        quizzes={lesson.quizzes}
        checkpointCount={summary.checkpointCount}
        onChanged={reload}
      />
    </section>
  );
}
