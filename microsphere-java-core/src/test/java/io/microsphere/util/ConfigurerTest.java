package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.Configurer.configure;

/**
 * {@link Configurer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class ConfigurerTest {

    @Test
    void test() {

        configure("test", 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure("test", 1)
                .compare(2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure("test", () -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure("test", () -> 1)
                .compare(2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure("test")
                .value(1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure("test")
                .value(() -> -1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        configure(() -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(value -> null)
                .apply(value -> {
                });

        configure(1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(value -> null)
                .apply(value -> {
                });

        configure("test")
                .value(() -> 1)
                .apply(value -> {
                });
    }

}
