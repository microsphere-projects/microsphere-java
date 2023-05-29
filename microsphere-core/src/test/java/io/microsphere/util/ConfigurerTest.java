package io.github.microsphere.util;

import org.junit.Test;

/**
 * {@link Configurer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConfigurerTest {

    @Test
    public void test() {

        Configurer.configure("test")
                .value(() -> 1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        Configurer.configure("test")
                .value(() -> -1)
                .compare(() -> 1)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        Configurer.configure("test")
                .value(() -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(value -> null)
                .apply(value -> {
                });

        Configurer.configure("test", () -> 1)
                .compare(() -> 2)
                .on(value -> value > 0)
                .as(String::valueOf)
                .apply(value -> {
                });

        Configurer.configure("test")
                .value(() -> 1)
                .apply(value -> {
                });
    }

}
