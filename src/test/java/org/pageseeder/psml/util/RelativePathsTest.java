package org.pageseeder.psml.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RelativePathsTest {

  /**
   * Test case: file and root are the same.
   * Expect: Return an empty string.
   */
  @Test
  void testComputeRelativePath_SameFileAndRoot() throws IOException {
    File root = new File("test/root");
    File file = new File("test/root");

    assertEquals("", RelativePaths.compute(file.getCanonicalFile(), root.getCanonicalFile()));
    assertEquals("", RelativePaths.compute(file, root));
    assertEquals("", RelativePaths.computeCanonical(file, root));
  }

  /**
   * Test case: file is directly under the root directory.
   * Expect: Return only the file name.
   */
  @Test
  void testComputeRelativePath_FileInRoot() throws IOException {
    File root = new File("test/root");
    File file = new File("test/root/file.txt");

    assertEquals("file.txt", RelativePaths.compute(file.getCanonicalFile(), root.getCanonicalFile()));
    assertEquals("file.txt", RelativePaths.compute(file, root));
    assertEquals("file.txt", RelativePaths.computeCanonical(file, root));
  }

  /**
   * Test case: file is located in a subdirectory under the root.
   * Expect: Return the relative path using forward slashes.
   */
  @Test
  void testComputeRelativePath_FileInSubdirectory() throws IOException {
    File root = new File("test/root");
    File file = new File("test/root/dir/file.txt");

    assertEquals("dir/file.txt", RelativePaths.compute(file.getCanonicalFile(), root.getCanonicalFile()));
    assertEquals("dir/file.txt", RelativePaths.compute(file, root));
    assertEquals("dir/file.txt", RelativePaths.computeCanonical(file, root));
  }

  /**
   * Test case: file is outside the root directory.
   * Expect: Throw IllegalArgumentException with appropriate message.
   */
  @Test
  void testComputeRelativePath_FileOutsideRoot() {
    File root = new File("test/root");
    File file = new File("test/other/file.txt");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        RelativePaths.compute(file.getCanonicalFile(), root.getCanonicalFile())
    );
    assertTrue(exception.getMessage().contains("is outside the src path"));
  }

  /**
   * Test case: file does not exist (but path is valid) and located under root.
   * Expect: Compute the correct relative path.
   */
  @Test
  void testComputeRelativePath_NonExistentFileUnderRoot() throws IOException {
    File root = new File("test/root");
    File file = new File("test/root/nonexistent.txt");

    assertEquals("nonexistent.txt", RelativePaths.compute(file.getCanonicalFile(), root.getCanonicalFile()));
    assertEquals("nonexistent.txt", RelativePaths.compute(file, root));
    assertEquals("nonexistent.txt", RelativePaths.computeCanonical(file, root));

  }

  /**
   * Test case: root or file paths cannot be resolved to their canonical form.
   * Expect: Throw IllegalStateException due to IOException.
   */
  @Test
  void testComputeRelativePath_CanonicalPathIOException() {
    File root = new File("invalid/root") {
      @Override
      public String getCanonicalPath() throws IOException {
        throw new IOException("Cannot resolve root");
      }
    };
    File file = new File("test/file.txt");

    assertThrows(IllegalArgumentException.class, () -> RelativePaths.computeCanonical(file, root));
  }
}