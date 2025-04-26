package io.microsphere.net.console;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static java.net.Proxy.NO_PROXY;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ConsoleURLConnection} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConsoleURLConnection
 * @since 1.0.0
 */
public class ConsoleURLConnectionTest {

    private ConsoleURLConnection connection;

    @BeforeEach
    public void init() throws IOException {
        Handler handler = new Handler();
        this.connection = (ConsoleURLConnection) handler.openConnection(new URL("console://localhost:12345/abc"), NO_PROXY);
    }

    @Test
    public void testConnect() throws IOException {
        connection.connect();
    }

    @Test
    public void testGetInputStream() throws IOException {
        assertSame(System.in, connection.getInputStream());
    }

    @Test
    public void testGetOutputStream() throws IOException {
        assertSame(System.out, connection.getOutputStream());
    }
}