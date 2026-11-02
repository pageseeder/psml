package org.pageseeder.psml.md;

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.md.MarkdownOutputOptions.DiffFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.TableFormat;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownOutputOptionsTest {

  @Test
  void testDiff_DefaultIsIgnore() {
    assertEquals(DiffFormat.IGNORE, MarkdownOutputOptions.defaultOptions().diff());
    assertEquals(DiffFormat.IGNORE, new MarkdownOutputOptions.Builder().build().diff());
    assertEquals(MarkdownOutputOptions.defaultOptions(), new MarkdownOutputOptions.Builder().build());
  }

  @Test
  void testDiff_Fluent() {
    MarkdownOutputOptions defaults = MarkdownOutputOptions.defaultOptions();
    MarkdownOutputOptions options = defaults.diff(DiffFormat.INS_DEL);
    assertEquals(DiffFormat.INS_DEL, options.diff());
    assertSame(options, options.diff(DiffFormat.INS_DEL));
    assertNotEquals(defaults, options);
    assertNotEquals(defaults.hashCode(), options.hashCode());
    assertTrue(options.toString().contains("diff=INS_DEL"));
    // Other fluent setters keep the diff format
    assertEquals(DiffFormat.INS_DEL, options.table(TableFormat.COMPACT).diff());
    assertEquals(DiffFormat.INS_DEL, options.metadata(false).diff());
    assertEquals(DiffFormat.INS_DEL, options.captions(true).diff());
  }

  @Test
  void testDiff_Builder() {
    MarkdownOutputOptions options = new MarkdownOutputOptions.Builder().diff(DiffFormat.INS_DEL).build();
    assertEquals(DiffFormat.INS_DEL, options.diff());
    assertEquals(options, options.builder().build());
    assertEquals(options, MarkdownOutputOptions.Builder.from(options).build());
    assertThrows(NullPointerException.class, () -> new MarkdownOutputOptions.Builder().diff(null));
  }

}
