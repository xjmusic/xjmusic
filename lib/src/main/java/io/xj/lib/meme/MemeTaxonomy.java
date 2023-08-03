package io.xj.lib.meme;

import java.util.Map;
import io.xj.lib.util.StringUtils;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TemplateConfig has Meme categories
 * https://www.pivotaltracker.com/story/show/181801646
 * <p>
 * <p>
 * A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
 * <p>
 * For example, this might look like
 * <p>
 * ```
 * memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
 * ```
 * <p>
 * That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
 */
public class MemeTaxonomy {
  static final String CATEGORY_SEPARATOR = ";";
  final List<Category> categories;

  MemeTaxonomy() {
    categories = List.of();
  }

  MemeTaxonomy(@Nullable String raw) {
    if (StringUtils.isNullOrEmpty(raw))
      categories = List.of();
    else
      categories = Arrays.stream(Objects.requireNonNull(raw).split(CATEGORY_SEPARATOR))
        .map(Category::new)
        .filter(Category::hasMemes)
        .toList();
  }

  public MemeTaxonomy(List<Object> data) {
    categories = data.stream()
      .flatMap(d -> {
        if (d instanceof Map<?, ?>) try {
          //noinspection unchecked
          return Stream.of(Category.fromMap((Map<String, Object>) d));
        } catch (Exception ignored) {
          //noop
        }
        return Stream.empty();
      })
      .collect(Collectors.toList());
  }

  public static MemeTaxonomy fromString(@Nullable String raw) {
    return new MemeTaxonomy(raw);
  }

  public static MemeTaxonomy fromList(List<Object> data) {
    return new MemeTaxonomy(data);
  }

  public static MemeTaxonomy empty() {
    return new MemeTaxonomy();
  }

  public String toString() {
    return categories.stream()
      .map(Category::toString)
      .collect(Collectors.joining(CATEGORY_SEPARATOR));
  }

  public List<Map<String, Object>> toList() {
    return categories.stream()
      .map(Category::toMap)
      .toList();
  }

  public List<Category> getCategories() {
    return categories;
  }

  public boolean isAllowed(List<String> memes) {
    return categories.stream().allMatch(category -> category.isAllowed(memes));
  }

  public static class Category {
    static final Pattern rgx = Pattern.compile("^([a-zA-Z ]+)\\[([a-zA-Z, ]+)]$");
    static final String MEME_SEPARATOR = ",";
    static final String KEY_NAME = "name";
    static final String KEY_MEMES = "memes";
    static final String DEFAULT_CATEGORY_NAME = "CATEGORY";
    final String name;
    final List<String> memes;

    Category(@Nullable String raw) {
      if (StringUtils.isNullOrEmpty(raw)) {
        name = DEFAULT_CATEGORY_NAME;
        memes = List.of();
        return;
      }

      Matcher matcher = rgx.matcher(Objects.requireNonNull(raw).trim());

      if (!matcher.find()) {
        name = DEFAULT_CATEGORY_NAME;
        memes = List.of();
        return;
      }

      String pfx = matcher.group(1);
      if (java.util.Objects.isNull(pfx) || pfx.length() == 0) {
        name = DEFAULT_CATEGORY_NAME;
        memes = List.of();
        return;
      }
      name = sanitize(StringUtils.toAlphabetical(pfx));

      String body = matcher.group(2);
      if (java.util.Objects.isNull(body) || body.length() == 0)
        memes = List.of();
      else
        memes = Arrays.stream(body.split(MEME_SEPARATOR))
          .map(this::sanitize)
          .toList();
    }

    public Category(Map<String, Object> data) {
      name = data.containsKey(KEY_NAME) ? sanitize(String.valueOf(data.get(KEY_NAME))) : DEFAULT_CATEGORY_NAME;
      memes = parseMemeList(data);
    }

    List<String> parseMemeList(Map<String, Object> data) {
      if (data.containsKey(KEY_MEMES) && data.get(KEY_MEMES) instanceof List<?>) try {
        //noinspection unchecked
        return ((List<String>) data.get(KEY_MEMES)).stream().map(this::sanitize).toList();
      } catch (Exception ignored) {
        //noop
      }
      return List.of();
    }

    public static Category fromString(@Nullable String raw) {
      return new Category(raw);
    }

    public static Category fromMap(Map<String, Object> data) {
      return new Category(data);
    }

    public String toString() {
      return String.format("%s[%s]", name, String.join(MEME_SEPARATOR, memes));
    }

    public String getName() {
      return name;
    }

    public List<String> getMemes() {
      return memes;
    }

    public boolean isAllowed(List<String> targets) {
      return 1 >= new HashSet<>(targets)
        .stream()
        .filter(memes::contains)
        .count();
    }

    public boolean hasMemes() {
      return !memes.isEmpty();
    }

    public Map<String, Object> toMap() {
      return Map.of(
        KEY_NAME, name,
        KEY_MEMES, memes
      );
    }

    String sanitize(String raw) {
      return StringUtils.toAlphabetical(raw.trim()).toUpperCase(Locale.ROOT);
    }
  }
}
