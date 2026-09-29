package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.util.Configurer.configure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Configurer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class ConfigurerTest {

    @Test
    void test() {
        List<Object> appliedValues = new ArrayList<>();

        configure("test", 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test", 1)
                .compare(2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test", () -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test", () -> 1)
                .compare(2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test")
                .value(1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test")
                .value(() -> -1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure(() -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(value -> null)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure(1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(value -> null)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure("test")
                .value(() -> 1)
                .apply(value -> {
                    appliedValues.add(value);
                });

        configure((Object) null)
                .compare(1)
                .apply(value -> {
                    appliedValues.add(value);
                });

        // Chains 1-4 keep a mapped "1" and chain 9 keeps 1; the rest are discarded by compare/on/as
        assertEquals(5, appliedValues.size());
        assertEquals(4, appliedValues.stream().filter("1"::equals).count());
        assertTrue(appliedValues.contains(1));

    }

}
