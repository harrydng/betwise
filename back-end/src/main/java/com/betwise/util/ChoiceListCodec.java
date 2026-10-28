package com.betwise.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quiz choices live in one text column as a JSON array of strings, such as
 * ["Stocks","Bonds, mostly"]. This reads and writes that format without
 * pulling in a JSON library, since it only ever holds a flat string list.
 */
public final class ChoiceListCodec {

    private ChoiceListCodec() {
    }

    public static String encode(List<String> choices) {
        return choices.stream()
                .map(choice -> "\"" + choice.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Decodes a JSON string array. Anything else is treated as a legacy
     * comma-separated list, matching what the iOS app accepts.
     */
    public static List<String> decode(String stored) {
        if (stored == null || stored.isBlank()) {
            return List.of();
        }

        String text = stored.trim();

        if (!text.startsWith("[")) {
            return splitLegacy(text);
        }

        List<String> choices = new ArrayList<>();
        StringBuilder current = null;
        boolean escaping = false;

        for (int i = 1; i < text.length(); i++) {
            char character = text.charAt(i);

            if (current == null) {
                if (character == '"') {
                    current = new StringBuilder();
                }
                continue;
            }

            if (escaping) {
                current.append(character);
                escaping = false;
            } else if (character == '\\') {
                escaping = true;
            } else if (character == '"') {
                choices.add(current.toString());
                current = null;
            } else {
                current.append(character);
            }
        }

        return choices;
    }

    private static List<String> splitLegacy(String text) {
        List<String> choices = new ArrayList<>();

        for (String part : text.split(",")) {
            String trimmed = part.trim();

            if (!trimmed.isEmpty()) {
                choices.add(trimmed);
            }
        }

        return choices;
    }
}
