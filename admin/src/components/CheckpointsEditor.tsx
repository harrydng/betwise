import { useEffect, useState } from "react";

import { api } from "../api";
import type { Checkpoint } from "../types";
import { Markdown } from "./Markdown";

// Drafts get a client-only key so React keeps each editor's focus and
// state while sections are reordered.
type DraftCheckpoint = Checkpoint & { key: string };

let nextKey = 0;
const withKey = (checkpoint: Checkpoint): DraftCheckpoint => ({ ...checkpoint, key: `cp-${nextKey++}` });

export function CheckpointsEditor({
  lessonId,
  initial,
  hasLearners,
  onSaved,
}: {
  lessonId: number;
  initial: Checkpoint[];
  hasLearners: boolean;
  onSaved: () => Promise<void> | void;
}) {
  const [drafts, setDrafts] = useState<DraftCheckpoint[]>(() => initial.map(withKey));
  const [isDirty, setIsDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Warn before leaving the page with unsaved sections.
  useEffect(() => {
    if (!isDirty) return;

    const warn = (event: BeforeUnloadEvent) => event.preventDefault();
    window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [isDirty]);

  function change(next: DraftCheckpoint[]) {
    setDrafts(next);
    setIsDirty(true);
  }

  function update(key: string, field: "title" | "body", value: string) {
    change(drafts.map((draft) => (draft.key === key ? { ...draft, [field]: value } : draft)));
  }

  function move(index: number, offset: number) {
    const target = index + offset;
    if (target < 0 || target >= drafts.length) return;

    const next = [...drafts];
    [next[index], next[target]] = [next[target], next[index]];
    change(next);
  }

  function remove(key: string) {
    change(drafts.filter((draft) => draft.key !== key));
  }

  function add() {
    change([...drafts, withKey({ title: "", body: "" })]);
  }

  async function save() {
    setError(null);
    setIsSaving(true);

    try {
      const saved = await api.lessons.saveCheckpoints(lessonId, drafts);
      setDrafts(saved.map(withKey));
      setIsDirty(false);
      await onSaved();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't save sections");
    } finally {
      setIsSaving(false);
    }
  }

  function discard() {
    setDrafts(initial.map(withKey));
    setIsDirty(false);
    setError(null);
  }

  return (
    <div className="card section">
      <div className="section-header">
        <div>
          <h2>Sections</h2>
          <p className="muted small">
            Learners read these one at a time. Use **bold**, *italic*, `code` and [links](https://…). Start a line
            with "- " for a bullet.
          </p>
        </div>
        {isDirty && <span className="pill draft">Unsaved changes</span>}
      </div>

      {hasLearners && (
        <p className="notice">
          Learners have started this lesson. Reordering or removing sections changes what their saved progress points
          to.
        </p>
      )}

      {drafts.length === 0 && <p className="muted">No sections yet. A lesson needs at least one to be published.</p>}

      <div className="stack">
        {drafts.map((draft, index) => (
          <div key={draft.key} className="checkpoint">
            <div className="checkpoint-editor">
              <div className="checkpoint-header">
                <span className="step">{index + 1}</span>
                <input
                  className="grow"
                  placeholder="Section title"
                  value={draft.title}
                  onChange={(event) => update(draft.key, "title", event.target.value)}
                />
                <button className="icon-button" onClick={() => move(index, -1)} disabled={index === 0} title="Move up">
                  ↑
                </button>
                <button
                  className="icon-button"
                  onClick={() => move(index, 1)}
                  disabled={index === drafts.length - 1}
                  title="Move down"
                >
                  ↓
                </button>
                <button className="icon-button danger" onClick={() => remove(draft.key)} title="Remove section">
                  ✕
                </button>
              </div>
              <textarea
                rows={8}
                placeholder="Write the section in Markdown…"
                value={draft.body}
                onChange={(event) => update(draft.key, "body", event.target.value)}
              />
            </div>

            <div className="checkpoint-preview">
              <div className="preview-label">Preview</div>
              <div className="phone">
                <div className="phone-progress">
                  Section {index + 1} of {drafts.length}
                  <div className="bar">
                    <div style={{ width: `${((index + 1) / drafts.length) * 100}%` }} />
                  </div>
                </div>
                <h3>{draft.title || "Untitled section"}</h3>
                <Markdown text={draft.body || "Nothing written yet."} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {error && <p className="error">{error}</p>}

      <div className="section-actions">
        <button className="button ghost" onClick={add}>
          Add section
        </button>
        <div className="spacer" />
        {isDirty && (
          <button className="button ghost" onClick={discard}>
            Discard changes
          </button>
        )}
        <button className="button primary" onClick={save} disabled={!isDirty || isSaving}>
          {isSaving ? "Saving…" : "Save sections"}
        </button>
      </div>
    </div>
  );
}
