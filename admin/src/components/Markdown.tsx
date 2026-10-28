import type { ReactNode } from "react";

// Renders checkpoint text the same way the iOS app does: inline styles
// (**bold**, *italic*, `code`, [links](url)) with line breaks kept, so
// "- " lines read as bullets. Headings and other block syntax aren't
// interpreted in the app, so they aren't here either. Builds React nodes
// instead of HTML, so authored text can never inject markup.

const INLINE_PATTERN = /(\*\*[^*]+\*\*|\*[^*\s][^*]*\*|_[^_\s][^_]*_|`[^`]+`|\[[^\]]+\]\([^)\s]+\))/g;

function renderInline(text: string, keyPrefix: string): ReactNode[] {
  const nodes: ReactNode[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  let index = 0;

  INLINE_PATTERN.lastIndex = 0;

  while ((match = INLINE_PATTERN.exec(text)) !== null) {
    if (match.index > lastIndex) {
      nodes.push(text.slice(lastIndex, match.index));
    }

    const token = match[0];
    const key = `${keyPrefix}-${index++}`;

    if (token.startsWith("**")) {
      nodes.push(<strong key={key}>{token.slice(2, -2)}</strong>);
    } else if (token.startsWith("`")) {
      nodes.push(<code key={key}>{token.slice(1, -1)}</code>);
    } else if (token.startsWith("[")) {
      const [, label, url] = /\[([^\]]+)\]\(([^)\s]+)\)/.exec(token) ?? [];
      const safeUrl = /^https?:\/\//i.test(url ?? "") ? url : undefined;
      nodes.push(
        <a key={key} href={safeUrl} target="_blank" rel="noreferrer">
          {label}
        </a>,
      );
    } else {
      nodes.push(<em key={key}>{token.slice(1, -1)}</em>);
    }

    lastIndex = match.index + token.length;
  }

  if (lastIndex < text.length) {
    nodes.push(text.slice(lastIndex));
  }

  return nodes;
}

export function Markdown({ text }: { text: string }) {
  const lines = text.split("\n");

  return (
    <div className="markdown">
      {lines.map((line, i) => (
        <span key={i}>
          {renderInline(line, `line-${i}`)}
          {i < lines.length - 1 && <br />}
        </span>
      ))}
    </div>
  );
}
