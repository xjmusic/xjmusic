package io.outright.signal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough JSON for Signal, with no library: what a host keeps (the
 * client's state, the ledger) and what `signal compile` prints.
 *
 * Objects read as {@code LinkedHashMap<String, Object>}, arrays as
 * {@code ArrayList<Object>}, whole numbers as {@link Long}, other numbers as
 * {@link Double}, and {@code true}, {@code false} and {@code null} as
 * themselves. Writing takes the same types back, and any {@link Number} or
 * {@link Map} or {@link List}.
 */
public final class Json {
  private Json() {
  }

  /** Thrown for text that is not JSON. */
  public static final class JsonException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JsonException(String message) {
      super(message);
    }
  }

  // ── Reading ────────────────────────────────────────────────────────────────

  public static Object parse(String text) {
    Reader reader = new Reader(text);
    reader.space();
    Object value = reader.value();
    reader.space();
    if (reader.at < text.length()) throw new JsonException("text after the value at " + reader.at);
    return value;
  }

  /** The text as an object, or an empty one when it is not one. */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> parseObject(String text) {
    try {
      Object value = parse(text);
      return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    } catch (JsonException | NullPointerException e) {
      return new LinkedHashMap<>();
    }
  }

  private static final class Reader {
    final String text;
    int at = 0;

    Reader(String text) {
      this.text = text;
    }

    void space() {
      while (at < text.length()) {
        char c = text.charAt(at);
        if (c != ' ' && c != '\n' && c != '\r' && c != '\t') return;
        at++;
      }
    }

    char peek() {
      if (at >= text.length()) throw new JsonException("unexpected end");
      return text.charAt(at);
    }

    void expect(String word) {
      if (!text.startsWith(word, at)) throw new JsonException("expected " + word + " at " + at);
      at += word.length();
    }

    Object value() {
      char c = peek();
      switch (c) {
        case '{':
          return object();
        case '[':
          return array();
        case '"':
          return string();
        case 't':
          expect("true");
          return Boolean.TRUE;
        case 'f':
          expect("false");
          return Boolean.FALSE;
        case 'n':
          expect("null");
          return null;
        default:
          return number();
      }
    }

    Map<String, Object> object() {
      Map<String, Object> out = new LinkedHashMap<>();
      at++;
      space();
      if (peek() == '}') {
        at++;
        return out;
      }
      while (true) {
        space();
        if (peek() != '"') throw new JsonException("expected a key at " + at);
        String key = string();
        space();
        expect(":");
        space();
        out.put(key, value());
        space();
        char c = peek();
        at++;
        if (c == '}') return out;
        if (c != ',') throw new JsonException("expected , or } at " + (at - 1));
      }
    }

    List<Object> array() {
      List<Object> out = new ArrayList<>();
      at++;
      space();
      if (peek() == ']') {
        at++;
        return out;
      }
      while (true) {
        space();
        out.add(value());
        space();
        char c = peek();
        at++;
        if (c == ']') return out;
        if (c != ',') throw new JsonException("expected , or ] at " + (at - 1));
      }
    }

    String string() {
      StringBuilder out = new StringBuilder();
      at++;
      while (true) {
        char c = peek();
        at++;
        if (c == '"') return out.toString();
        if (c != '\\') {
          out.append(c);
          continue;
        }
        char e = peek();
        at++;
        switch (e) {
          case '"', '\\', '/' -> out.append(e);
          case 'b' -> out.append('\b');
          case 'f' -> out.append('\f');
          case 'n' -> out.append('\n');
          case 'r' -> out.append('\r');
          case 't' -> out.append('\t');
          case 'u' -> {
            if (at + 4 > text.length()) throw new JsonException("short \\u escape");
            out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
            at += 4;
          }
          default -> throw new JsonException("bad escape at " + at);
        }
      }
    }

    Object number() {
      int start = at;
      boolean whole = true;
      while (at < text.length()) {
        char c = text.charAt(at);
        if ((c >= '0' && c <= '9') || c == '-' || c == '+') {
          at++;
        } else if (c == '.' || c == 'e' || c == 'E') {
          whole = false;
          at++;
        } else {
          break;
        }
      }
      String number = text.substring(start, at);
      if (number.isEmpty()) throw new JsonException("unexpected " + text.charAt(start) + " at " + start);
      try {
        if (whole) {
          try {
            return Long.parseLong(number);
          } catch (NumberFormatException tooBig) {
            return Double.parseDouble(number);
          }
        }
        return Double.parseDouble(number);
      } catch (NumberFormatException e) {
        throw new JsonException("not a number at " + start);
      }
    }
  }

  // ── Writing ────────────────────────────────────────────────────────────────

  public static String write(Object value) {
    StringBuilder out = new StringBuilder();
    write(out, value);
    return out.toString();
  }

  private static void write(StringBuilder out, Object value) {
    if (value == null) {
      out.append("null");
    } else if (value instanceof String s) {
      quote(out, s);
    } else if (value instanceof Boolean) {
      out.append(value);
    } else if (value instanceof Double || value instanceof Float) {
      double d = ((Number) value).doubleValue();
      if (Double.isNaN(d) || Double.isInfinite(d)) out.append("null");
      else if (d == Math.rint(d) && Math.abs(d) < 1e15) out.append((long) d);
      else out.append(d);
    } else if (value instanceof Number n) {
      out.append(n.longValue());
    } else if (value instanceof Map<?, ?> map) {
      out.append('{');
      boolean first = true;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!first) out.append(',');
        first = false;
        quote(out, String.valueOf(entry.getKey()));
        out.append(':');
        write(out, entry.getValue());
      }
      out.append('}');
    } else if (value instanceof Iterable<?> list) {
      out.append('[');
      boolean first = true;
      for (Object entry : list) {
        if (!first) out.append(',');
        first = false;
        write(out, entry);
      }
      out.append(']');
    } else {
      quote(out, value.toString());
    }
  }

  private static void quote(StringBuilder out, String s) {
    out.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        case '\b' -> out.append("\\b");
        case '\f' -> out.append("\\f");
        default -> {
          if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
          else out.append(c);
        }
      }
    }
    out.append('"');
  }

  // ── Reading values out of parsed JSON ──────────────────────────────────────

  /** A whole number, or the fallback: what Dart's `value is num ? value.toInt() : fallback` is. */
  public static long asLong(Object value, long fallback) {
    return value instanceof Number n ? n.longValue() : fallback;
  }

  public static int asInt(Object value, int fallback) {
    return value instanceof Number n ? (int) n.longValue() : fallback;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> asMap(Object value) {
    if (!(value instanceof Map<?, ?> map)) return null;
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) out.put(String.valueOf(entry.getKey()), entry.getValue());
    return out;
  }

  @SuppressWarnings("unchecked")
  public static List<Object> asList(Object value) {
    return value instanceof List<?> list ? (List<Object>) list : null;
  }
}
