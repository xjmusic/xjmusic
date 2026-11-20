package io.outright.signal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * How a summary leaves the device (README §7.1): one small POST, and silence
 * if it fails. The client decides what a status means; a transport only
 * carries bytes. Tests put another one in its place. The Flutter package's
 * {@code transport.dart}.
 */
public interface SignalTransport {
  /** What the platform answered: its status (0 for no answer), and how long it asked to be left alone, in seconds. */
  record Response(int status, int retryAfter) {
  }

  /** POST {@code body} to {@code url} with the stream's key. Never fails: no answer is status 0. */
  CompletableFuture<Response> post(String url, String key, String contentType, byte[] body);

  /** The transport over the JDK's own {@link HttpClient}: no library. */
  final class Http implements SignalTransport {
    private final HttpClient client;
    private final Duration timeout;

    public Http() {
      this(Duration.ofSeconds(10));
    }

    public Http(Duration timeout) {
      this.timeout = timeout;
      this.client = HttpClient.newBuilder()
        .connectTimeout(timeout)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    }

    @Override
    public CompletableFuture<Response> post(String url, String key, String contentType, byte[] body) {
      try {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
          .timeout(timeout)
          .header("content-type", contentType)
          .header("x-signal-key", key)
          .POST(HttpRequest.BodyPublishers.ofByteArray(body))
          .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
          .thenApply(response -> new Response(response.statusCode(), retryAfter(response)))
          .exceptionally(error -> new Response(0, 0));
      } catch (RuntimeException e) {
        return CompletableFuture.completedFuture(new Response(0, 0));
      }
    }

    private static int retryAfter(HttpResponse<?> response) {
      try {
        long seconds = Long.parseLong(response.headers().firstValue("retry-after").orElse("0").trim());
        return (int) Math.max(0, Math.min(86400, seconds));
      } catch (NumberFormatException e) {
        return 0;
      }
    }
  }
}
