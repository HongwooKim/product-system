package com.coupang.product.domain.rule;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    // ═══════════════════════════════════════
    //  Simple Rule implementation for testing
    // ═══════════════════════════════════════

    /**
     * A test rule that matches integers above a threshold and returns a label.
     */
    static class IntRule implements Rule<Integer, String> {
        private final String id;
        private final String name;
        private final int priority;
        private final int threshold;
        private final String result;

        IntRule(String id, String name, int priority, int threshold, String result) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.threshold = threshold;
            this.result = result;
        }

        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public int getPriority() { return priority; }
        @Override public boolean matches(Integer context) { return context > threshold; }
        @Override public String evaluate(Integer context) { return result; }
    }

    private static RuleEngine<Integer, String> engineWith(IntRule... rules) {
        return new RuleEngine<>(List.of(rules));
    }

    // ═══════════════════════════════════════
    //  evaluateFirst()
    // ═══════════════════════════════════════

    @Test
    void evaluateFirst_returnsFirstMatchingRuleByPriority() {
        RuleEngine<Integer, String> engine = engineWith(
                new IntRule("r3", "Low priority", 30, 5, "LOW"),
                new IntRule("r1", "High priority", 10, 5, "HIGH"),
                new IntRule("r2", "Mid priority", 20, 5, "MID")
        );

        Optional<String> result = engine.evaluateFirst(10);

        assertTrue(result.isPresent());
        assertEquals("HIGH", result.get());
    }

    @Test
    void evaluateFirst_returnsEmptyWhenNoRulesMatch() {
        RuleEngine<Integer, String> engine = engineWith(
                new IntRule("r1", "Rule 1", 10, 100, "MATCH")
        );

        Optional<String> result = engine.evaluateFirst(5);

        assertTrue(result.isEmpty());
    }

    // ═══════════════════════════════════════
    //  evaluateAll()
    // ═══════════════════════════════════════

    @Test
    void evaluateAll_returnsAllMatchingResults() {
        RuleEngine<Integer, String> engine = engineWith(
                new IntRule("r1", "Rule 1", 10, 5, "A"),
                new IntRule("r2", "Rule 2", 20, 50, "B"),
                new IntRule("r3", "Rule 3", 30, 5, "C")
        );

        List<String> results = engine.evaluateAll(10);

        assertEquals(2, results.size());
        assertTrue(results.contains("A"));
        assertTrue(results.contains("C"));
        assertFalse(results.contains("B")); // threshold 50, context 10
    }

    // ═══════════════════════════════════════
    //  anyMatches()
    // ═══════════════════════════════════════

    @Test
    void anyMatches_returnsTrueWhenRuleMatches() {
        RuleEngine<Integer, String> engine = engineWith(
                new IntRule("r1", "Rule 1", 10, 5, "MATCH")
        );

        assertTrue(engine.anyMatches(10));
    }

    @Test
    void anyMatches_returnsFalseWhenNoRuleMatches() {
        RuleEngine<Integer, String> engine = engineWith(
                new IntRule("r1", "Rule 1", 10, 100, "MATCH")
        );

        assertFalse(engine.anyMatches(5));
    }
}
