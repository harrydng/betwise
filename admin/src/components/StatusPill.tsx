export function lessonStatus(lesson: { published: boolean; archived: boolean }) {
  if (lesson.archived) return "archived" as const;
  if (lesson.published) return "published" as const;
  return "draft" as const;
}

const LABELS = {
  published: "Published",
  draft: "Draft",
  archived: "Archived",
};

export function StatusPill({ published, archived }: { published: boolean; archived: boolean }) {
  const status = lessonStatus({ published, archived });

  return <span className={`pill ${status}`}>{LABELS[status]}</span>;
}
