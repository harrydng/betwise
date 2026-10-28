import { useEffect, useState, type FormEvent } from "react";

import { api } from "../api";
import type { Category } from "../types";

export function CategoriesPage() {
  const [categories, setCategories] = useState<Category[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.categories
      .list()
      .then(setCategories)
      .catch((caught: Error) => setError(caught.message));
  }

  useEffect(load, []);

  return (
    <section>
      <div className="page-header">
        <div>
          <h1>Categories</h1>
          <p className="muted">
            Categories group lessons in the app's Learn tab. The slug is what the app filters by.
          </p>
        </div>
      </div>

      {error && <p className="error">{error}</p>}

      <CategoryForm onSaved={load} />

      {categories === null && !error ? (
        <p className="muted">Loading…</p>
      ) : (
        <div className="stack">
          {(categories ?? []).map((category) => (
            <CategoryRow key={category.id} category={category} onChanged={load} />
          ))}
        </div>
      )}
    </section>
  );
}

function CategoryForm({ onSaved }: { onSaved: () => void }) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    try {
      await api.categories.create({ name, description });
      setName("");
      setDescription("");
      onSaved();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't create the category");
    }
  }

  return (
    <form className="card inline-form" onSubmit={submit}>
      <label className="field grow">
        <span>New category</span>
        <input placeholder="Name" value={name} onChange={(event) => setName(event.target.value)} required />
      </label>
      <label className="field grow">
        <span>Description</span>
        <input
          placeholder="Optional"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
      </label>
      <button className="button primary" type="submit">
        Add
      </button>
      {error && <p className="error full">{error}</p>}
    </form>
  );
}

function CategoryRow({ category, onChanged }: { category: Category; onChanged: () => void }) {
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(category.name);
  const [slug, setSlug] = useState(category.slug);
  const [description, setDescription] = useState(category.description ?? "");
  const [error, setError] = useState<string | null>(null);

  async function save() {
    setError(null);

    try {
      await api.categories.update(category.id, { name, slug, description });
      setIsEditing(false);
      onChanged();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't save");
    }
  }

  async function remove() {
    if (!window.confirm(`Delete "${category.name}"?`)) return;

    setError(null);

    try {
      await api.categories.remove(category.id);
      onChanged();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't delete");
    }
  }

  return (
    <div className="card category-row">
      {isEditing ? (
        <div className="inline-form">
          <label className="field grow">
            <span>Name</span>
            <input value={name} onChange={(event) => setName(event.target.value)} />
          </label>
          <label className="field">
            <span>Slug</span>
            <input value={slug} onChange={(event) => setSlug(event.target.value)} />
          </label>
          <label className="field grow">
            <span>Description</span>
            <input value={description} onChange={(event) => setDescription(event.target.value)} />
          </label>
          <button className="button ghost" onClick={() => setIsEditing(false)}>
            Cancel
          </button>
          <button className="button primary" onClick={save}>
            Save
          </button>
        </div>
      ) : (
        <div className="category-summary">
          <div>
            <div className="cell-title">{category.name}</div>
            <div className="muted small">
              {category.slug} · {category.lessonCount} {category.lessonCount === 1 ? "lesson" : "lessons"}
              {category.description ? ` · ${category.description}` : ""}
            </div>
          </div>
          <div className="actions">
            <button className="button ghost small" onClick={() => setIsEditing(true)}>
              Edit
            </button>
            <button
              className="button danger small"
              onClick={remove}
              disabled={category.lessonCount > 0}
              title={category.lessonCount > 0 ? "Move or delete its lessons first" : undefined}
            >
              Delete
            </button>
          </div>
        </div>
      )}
      {error && <p className="error">{error}</p>}
    </div>
  );
}
