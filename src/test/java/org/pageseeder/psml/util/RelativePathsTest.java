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

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath0() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/folder";
    String context      = "/ps/project/group/documents/folder";
    String groupContext = "/ps/project/group";
    assertEquals("file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath1() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/folder";
    String context      = "/ps/project";
    String groupContext = "/ps/project/group";
    assertEquals("file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath2() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents";
    String context      = "/ps/project";
    String groupContext = "/ps/project/group";
    assertEquals("folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath3() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/folder2";
    String context      = "/ps/project";
    String groupContext = "/ps/project/group";
    assertEquals("../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath4() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/folder2/subfolder";
    String context      = "/ps/project";
    String groupContext = "/ps/project/group";
    assertEquals("../../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath5() {
    // simple example
    String parent       = "/ps/project/group/otherdocuments/subfolder";
    String target       = "/ps/project/group/documents/folder/file.psml";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    // one more "../" because of _local folder
    assertEquals("../../../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath6() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group2/project/group2/documents";
    String context      = "/ps/project/group";
    String groupContext = "/ps/project/group";
    // one more "../" because of _external folder
    assertEquals("../../../../../../documents/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath7() {
    // simple example
    String target       = "/ps/project/group2/documents/folder/file.psml";
    String parent       = "/ps/project/group2/documents";
    String context      = "/ps/project/group";
    String groupContext = "/ps/project/group";
    assertEquals("folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath8() {
    // simple example
    String target       = "/ps/project/group2/documents/folder/file.psml";
    String parent       = "/ps/project/group2/documents/subfolder";
    String context      = "/ps/project/group";
    String groupContext = "/ps/project/group";
    assertEquals("../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath9() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/subfolder";
    String context      = "/ps/project/group/documents/subfolder";
    String groupContext = "/ps/project/group";
    assertEquals("_local/documents/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath10() {
    // simple example
    String target       = "/ps/project/group2/documents/folder/file.psml";
    String parent       = "/ps/project/group/documents/subfolder";
    String context      = "/ps/project/group/documents/subfolder";
    String groupContext = "/ps/project/group";
    assertEquals("_external/project/group2/documents/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath11() {
    // simple example
    String target       = "/ps/project/group/otherdocuments/folder/file.psml";
    String parent       = "/ps/project/group/documents/subfolder";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../_local/otherdocuments/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation.
   */
  @Test
  void testRelativiseFullPath12() {
    // simple example
    String target       = "/ps/project/group2/otherdocuments/folder/file.psml";
    String parent       = "/ps/project/group/documents/subfolder/grandchild";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../../_external/project/group2/otherdocuments/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation _external to _local.
   */
  @Test
  void testRelativiseFullPath13() {
    // simple example
    String target       = "/ps/project/group/otherdocuments/folder/file.psml";
    String parent       = "/ps/project/group2/documents/subfolder/grandchild";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../../../../../../_local/otherdocuments/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation _local to _external.
   */
  @Test
  void testRelativiseFullPath14() {
    // simple example
    String target       = "/ps/project/group2/otherdocuments/folder/file.psml";
    String parent       = "/ps/project/group/otherdocuments/subfolder/grandchild";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../../../../_external/project/group2/otherdocuments/folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation _external to context.
   */
  @Test
  void testRelativiseFullPath15() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group2/documents/subfolder/grandchild";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../../../../../../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test relative path computation _local to context.
   */
  @Test
  void testRelativiseFullPath16() {
    // simple example
    String target       = "/ps/project/group/documents/folder/file.psml";
    String parent       = "/ps/project/group/otherdocuments/subfolder/grandchild";
    String context      = "/ps/project/group/documents";
    String groupContext = "/ps/project/group";
    assertEquals("../../../../folder/file.psml", RelativePaths.relativiseFullPath(target, parent, context, groupContext, 0, "/ps"));
  }

  /**
   * Test direct relative path computation.
   */
  @Test
  void testRelativise1() {
    // simple example
    String target = "/ps/project/group2/documents/folder/file.psml";
    String parent = "/ps/project/group2/documents/subfolder";
    assertEquals("../folder/file.psml", RelativePaths.relativise(target, parent));
  }

  /**
   * Test direct relative path computation.
   */
  @Test
  void testRelativise2() {
    // simple example
    String target = "/ps/project/group2/documents/folder/file.psml";
    String parent = "/ps/project/group2/documents";
    assertEquals("folder/file.psml", RelativePaths.relativise(target, parent));
  }

  /**
   * Test direct relative path computation.
   */
  @Test
  void testRelativise3() {
    // simple example
    String target = "/ps/project/group2/documents/folder/file.psml";
    String parent = "/ps/project/group2/documents/subfolder/something/else";
    assertEquals("../../../folder/file.psml", RelativePaths.relativise(target, parent));
  }

  /**
   * Test direct relative path computation.
   */
  @Test
  void testRelativise4() {
    // simple example
    String target = "/something/file.psml";
    String parent = "/something/else/altogether";
    assertEquals("../../file.psml", RelativePaths.relativise(target, parent));
  }
  
}