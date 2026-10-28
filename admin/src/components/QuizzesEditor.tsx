import { useState } from "react";

import { api } from "../api";
import type { Question, Quiz, QuizInput } from "../types";

export function QuizzesEditor({
  lessonId,
  quizzes,
  checkpointCount,
  onChanged,
}: {
  lessonId: number;
  quizzes: Quiz[];
  checkpointCount: number;
  onChanged: () => Promise<void> | void;
}) {
  const [isAdding, setIsAdding] = useState(false);

  return (
    <div className="card section">
      <div className="section-header">
        <div>
          <h2>Quizzes</h2>
          <p className="muted small">A quiz unlocks once the learner finishes the section you pick.</p>
        </div>
        {!isAdding && (
          <button
            className="button ghost"
            onClick={() => setIsAdding(true)}
            disabled={checkpointCount === 0}
            title={checkpointCount === 0 ? "Save at least one section first" : undefined}
          >
            Add quiz
          </button>
        )}
      </div>

      {quizzes.length === 0 && !isAdding && <p className="muted">No quizzes for this lesson.</p>}

      <div className="stack">
        {quizzes.map((quiz) => (
          <QuizForm
            key={quiz.id}
            quiz={quiz}
            checkpointCount={checkpointCount}
            onSave={(input) => api.quizzes.update(quiz.id, input)}
            onDelete={async () => {
              await api.quizzes.remove(quiz.id);
              await onChanged();
            }}
            onSaved={onChanged}
          />
        ))}

        {isAdding && (
          <QuizForm
            checkpointCount={checkpointCount}
            onSave={(input) => api.quizzes.create(lessonId, input)}
            onSaved={async () => {
              setIsAdding(false);
              await onChanged();
            }}
            onCancel={() => setIsAdding(false)}
          />
        )}
      </div>
    </div>
  );
}

type DraftQuestion = Question & { key: string };

let nextKey = 0;
const draftQuestion = (question?: Question): DraftQuestion => ({
  id: question?.id,
  question: question?.question ?? "",
  choices: question?.choices.length ? [...question.choices] : ["", ""],
  correctAnswer: question?.correctAnswer ?? "",
  key: `q-${nextKey++}`,
});

function QuizForm({
  quiz,
  checkpointCount,
  onSave,
  onSaved,
  onDelete,
  onCancel,
}: {
  quiz?: Quiz;
  checkpointCount: number;
  onSave: (input: QuizInput) => Promise<Quiz>;
  onSaved: () => Promise<void> | void;
  onDelete?: () => Promise<void>;
  onCancel?: () => void;
}) {
  const [isOpen, setIsOpen] = useState(!quiz);
  const [title, setTitle] = useState(quiz?.title ?? "");
  const [unlockAfter, setUnlockAfter] = useState(Math.min(quiz?.checkpointOrder ?? 1, Math.max(checkpointCount, 1)));
  const [questions, setQuestions] = useState<DraftQuestion[]>(() =>
    quiz?.questions.length ? quiz.questions.map(draftQuestion) : [draftQuestion()],
  );
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  function updateQuestion(key: string, change: Partial<Question>) {
    setQuestions((current) => current.map((q) => (q.key === key ? { ...q, ...change } : q)));
  }

  function updateChoice(question: DraftQuestion, index: number, value: string) {
    const choices = question.choices.map((choice, i) => (i === index ? value : choice));

    // Keep the correct answer pointing at the same choice while it's edited.
    const wasCorrect = question.choices[index] === question.correctAnswer && question.correctAnswer !== "";
    updateQuestion(question.key, { choices, correctAnswer: wasCorrect ? value : question.correctAnswer });
  }

  function removeChoice(question: DraftQuestion, index: number) {
    const removed = question.choices[index];
    updateQuestion(question.key, {
      choices: question.choices.filter((_, i) => i !== index),
      correctAnswer: removed === question.correctAnswer ? "" : question.correctAnswer,
    });
  }

  function moveQuestion(index: number, offset: number) {
    const target = index + offset;
    if (target < 0 || target >= questions.length) return;

    const next = [...questions];
    [next[index], next[target]] = [next[target], next[index]];
    setQuestions(next);
  }

  async function save() {
    setError(null);
    setIsSaving(true);

    try {
      await onSave({
        title,
        checkpointOrder: unlockAfter,
        questions: questions.map(({ id, question, choices, correctAnswer }) => ({
          id,
          question,
          choices: choices.map((choice) => choice.trim()).filter(Boolean),
          correctAnswer,
        })),
      });
      await onSaved();
      if (quiz) setIsOpen(false);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't save the quiz");
    } finally {
      setIsSaving(false);
    }
  }

  async function remove() {
    if (!onDelete || !window.confirm(`Delete "${quiz?.title}"?`)) return;

    setError(null);

    try {
      await onDelete();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Couldn't delete the quiz");
    }
  }

  if (quiz && !isOpen) {
    return (
      <div className="quiz-summary">
        <div>
          <div className="cell-title">{quiz.title}</div>
          <div className="muted small">
            {quiz.questions.length} questions · unlocks after section {quiz.checkpointOrder}
            {quiz.hasAttempts ? " · has attempts" : ""}
          </div>
        </div>
        <button className="button ghost small" onClick={() => setIsOpen(true)}>
          Edit
        </button>
      </div>
    );
  }

  return (
    <div className="quiz-form">
      <div className="row">
        <label className="field grow">
          <span>Quiz title</span>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="e.g. Stock Basics Check" />
        </label>
        <label className="field">
          <span>Unlocks after section</span>
          <select value={unlockAfter} onChange={(event) => setUnlockAfter(Number(event.target.value))}>
            {Array.from({ length: Math.max(checkpointCount, 1) }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </label>
      </div>

      {quiz?.hasAttempts && (
        <p className="notice">
          Learners have taken this quiz. You can edit questions, but ones that were already answered can't be removed.
        </p>
      )}

      {questions.map((question, index) => (
        <div key={question.key} className="question">
          <div className="checkpoint-header">
            <span className="step">{index + 1}</span>
            <input
              className="grow"
              placeholder="Question"
              value={question.question}
              onChange={(event) => updateQuestion(question.key, { question: event.target.value })}
            />
            <button className="icon-button" onClick={() => moveQuestion(index, -1)} disabled={index === 0} title="Move up">
              ↑
            </button>
            <button
              className="icon-button"
              onClick={() => moveQuestion(index, 1)}
              disabled={index === questions.length - 1}
              title="Move down"
            >
              ↓
            </button>
            <button
              className="icon-button danger"
              onClick={() => setQuestions((current) => current.filter((q) => q.key !== question.key))}
              disabled={questions.length === 1}
              title="Remove question"
            >
              ✕
            </button>
          </div>

          <div className="choices">
            {question.choices.map((choice, choiceIndex) => (
              <div key={choiceIndex} className="choice">
                <input
                  type="radio"
                  name={`correct-${question.key}`}
                  checked={choice !== "" && choice === question.correctAnswer}
                  onChange={() => updateQuestion(question.key, { correctAnswer: choice })}
                  disabled={choice.trim() === ""}
                  title="Mark as the correct answer"
                />
                <input
                  className="grow"
                  placeholder={`Choice ${choiceIndex + 1}`}
                  value={choice}
                  onChange={(event) => updateChoice(question, choiceIndex, event.target.value)}
                />
                <button
                  className="icon-button"
                  onClick={() => removeChoice(question, choiceIndex)}
                  disabled={question.choices.length <= 2}
                  title="Remove choice"
                >
                  ✕
                </button>
              </div>
            ))}
            <button
              className="link-button"
              onClick={() => updateQuestion(question.key, { choices: [...question.choices, ""] })}
            >
              + Add choice
            </button>
            {!question.correctAnswer && <p className="muted small">Pick the correct answer with the radio button.</p>}
          </div>
        </div>
      ))}

      {error && <p className="error">{error}</p>}

      <div className="section-actions">
        <button className="button ghost" onClick={() => setQuestions((current) => [...current, draftQuestion()])}>
          Add question
        </button>
        <div className="spacer" />
        {quiz && onDelete && (
          <button
            className="button danger"
            onClick={remove}
            disabled={quiz.hasAttempts}
            title={quiz.hasAttempts ? "Learners have taken this quiz" : undefined}
          >
            Delete quiz
          </button>
        )}
        <button
          className="button ghost"
          onClick={() => {
            if (quiz) {
              setIsOpen(false);
            } else {
              onCancel?.();
            }
          }}
        >
          Cancel
        </button>
        <button className="button primary" onClick={save} disabled={isSaving}>
          {isSaving ? "Saving…" : "Save quiz"}
        </button>
      </div>
    </div>
  );
}
