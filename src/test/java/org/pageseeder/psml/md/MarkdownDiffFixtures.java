package org.pageseeder.psml.md;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pageseeder.psml.diff.PSMLDiffer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generates combined Diff-X PSML fixtures from the old/new pairs in {@code src/test/data/mddiff/pairs/}.
 *
 * <p>Each pair is {@code <name>.old.psml} and {@code <name>.new.psml}. The differ is called the same
 * way as {@code DiffHandler}: current (new) content first, then the compared (old) content.
 *
 * <p>Run {@link #main(String[])} to regenerate {@code src/test/data/mddiff/generated/}. The test in
 * this class fails when the committed outputs no longer match what the current differ produces.
 */
public final class MarkdownDiffFixtures {

  static final Path PAIRS = Paths.get("src/test/data/mddiff/pairs");
  static final Path GENERATED = Paths.get("src/test/data/mddiff/generated");

  public static void main(String[] args) throws Exception {
    Files.createDirectories(GENERATED);
    for (String name : names()) {
      Files.write(GENERATED.resolve(name + ".psml"), generate(name).getBytes(StandardCharsets.UTF_8));
      System.out.println("Generated " + name);
    }
  }

  /**
   * @return the names of all fixture pairs, sorted.
   */
  static List<String> names() throws IOException {
    List<String> names = new ArrayList<>();
    try (Stream<Path> files = Files.list(PAIRS)) {
      files.map(p -> p.getFileName().toString())
          .filter(f -> f.endsWith(".old.psml"))
          .map(f -> f.substring(0, f.length() - ".old.psml".length()))
          .sorted()
          .forEach(names::add);
    }
    return names;
  }

  /**
   * Diffs the named pair and returns the combined Diff-X output wrapped in {@code <diff>}.
   */
  static String generate(String name) throws Exception {
    String oldXml = Files.readString(PAIRS.resolve(name + ".old.psml")).trim();
    String newXml = Files.readString(PAIRS.resolve(name + ".new.psml")).trim();
    PSMLDiffer differ = new PSMLDiffer(4_000_000);
    StringWriter out = new StringWriter();
    // Same argument order as DiffHandler: current (new) first, then compared (old)
    differ.diff(new StringReader(newXml), new StringReader(oldXml), out);
    String diffx = out.toString();
    if (diffx.startsWith("<?")) diffx = diffx.substring(diffx.indexOf('>') + 1);
    return "<diff>" + diffx + "</diff>\n";
  }

  @Test
  void generatedFixturesAreUpToDate() throws Exception {
    for (String name : names()) {
      Path file = GENERATED.resolve(name + ".psml");
      Assertions.assertTrue(Files.exists(file), "Missing generated fixture " + file + ": run MarkdownDiffFixtures.main");
      Assertions.assertEquals(Files.readString(file), generate(name), "Outdated generated fixture " + file);
    }
  }

}
