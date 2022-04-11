package io.xj.lib.meme;

import com.google.api.client.util.Strings;
import io.xj.lib.util.Text;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 TemplateConfig has Meme categories
 https://www.pivotaltracker.com/story/show/181801646
 <p>
 <p>
 A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
 <p>
 For example, this might look like
 <p>
 ```
 memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
 ```
 <p>
 That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
 */
public class MemeTaxonomy {
  private static final String CATEGORY_SEPARATOR = ";";
  private final List<Category> categories;

  private MemeTaxonomy() {
    categories = List.of();
  }

  private MemeTaxonomy(@Nullable String raw) {
    if (Strings.isNullOrEmpty(raw))
      categories = List.of();
    else
      categories = Arrays.stream(raw.split(CATEGORY_SEPARATOR))
        .map(Category::new)
        .filter(Category::hasMemes)
        .toList();
  }

  public static MemeTaxonomy fromString(@Nullable String raw) {
    return new MemeTaxonomy(raw);
  }

  public static MemeTaxonomy empty() {
    return new MemeTaxonomy();
  }

  public String toString() {
    return categories.stream()
      .map(Category::toString)
      .collect(Collectors.joining(CATEGORY_SEPARATOR));
  }

  public List<Category> getCategories() {
    return categories;
  }

  public boolean isAllowed(List<String> memes) {
    return categories.stream().allMatch(category -> category.isAllowed(memes));
  }

  static class Category {
    private static final Pattern rgx = Pattern.compile("^([a-zA-Z\s]+)\\[([a-zA-Z,\s]+)]$");
    private static final String MEME_SEPARATOR = ",";
    private final String name;
    private final List<String> memes;

    private Category(@Nullable String raw) {
      if (Strings.isNullOrEmpty(raw)) {
        name = null;
        memes = List.of();
        return;
      }

      Matcher matcher = rgx.matcher(raw.trim().toUpperCase(Locale.ROOT));

      if (!matcher.find()) {
        name = null;
        memes = List.of();
        return;
      }

      String pfx = matcher.group(1);
      if (java.util.Objects.isNull(pfx) || pfx.length() == 0) {
        name = null;
        memes = List.of();
        return;
      }
      name = Text.toAlphabetical(pfx).toUpperCase(Locale.ROOT);

      var body = matcher.group(2);
      if (java.util.Objects.isNull(body) || body.length() == 0) {
        memes = List.of();
        return;
      }
      memes = Arrays.stream(body.split(MEME_SEPARATOR))
        .map(String::trim)
        .map(Text::toAlphabetical)
        .map(String::toUpperCase)
        .toList();
    }

    public static Category fromString(@Nullable String raw) {
      return new Category(raw);
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
      return 1 >= targets.stream()
        .filter(memes::contains)
        .count();
    }

    public boolean hasMemes() {
      return !memes.isEmpty();
    }
  }
}
