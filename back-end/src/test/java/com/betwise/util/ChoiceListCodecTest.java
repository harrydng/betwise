package com.betwise.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChoiceListCodecTest {

    @Test
    void roundTripsChoicesWithCommasQuotesAndBackslashes() {

        List<String> choices = List.of(
                "Stocks",
                "Bonds, mostly",
                "The \"safe\" choice",
                "C:\\path");

        String encoded = ChoiceListCodec.encode(choices);

        assertEquals("[\"Stocks\",\"Bonds, mostly\",\"The \\\"safe\\\" choice\",\"C:\\\\path\"]", encoded);
        assertEquals(choices, ChoiceListCodec.decode(encoded));
    }

    @Test
    void readsLegacyCommaSeparatedChoices() {

        assertEquals(List.of("A", "B", "C"), ChoiceListCodec.decode(" A, B ,C "));
    }

    @Test
    void emptyInputHasNoChoices() {

        assertEquals(List.of(), ChoiceListCodec.decode(null));
        assertEquals(List.of(), ChoiceListCodec.decode("  "));
        assertEquals(List.of(), ChoiceListCodec.decode("[]"));
    }
}
