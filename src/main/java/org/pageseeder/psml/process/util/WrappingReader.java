/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process.util;

import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Wrapping reader.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 1.6.0
 * @since 1.0
 */
public class WrappingReader extends Reader {

  /**
   * The original wrapped reader.
   */
  private final Reader originalReader;

  /**
   * Flag indicating whether the original reader has been reader entirely
   */
  private boolean readerFinished = false;

  /**
   * The text to put before.
   */
  private final StringBuilder before = new StringBuilder();

  /**
   * The text to put after
   */
  private final StringBuilder after = new StringBuilder();

  /**
   * Index for the text before.
   */
  private int firstIndex = 0;

  /**
   * Index for the text after.
   */
  private int secondIndex = 0;

  /**
   * Creates the reader with the fields needed.
   *
   * @param reader       The reader to wrap.
   * @param before       The text to put before.
   * @param after        The text to put after.
   *
   * @throws IOException If thrown by the reader
   */
  public WrappingReader(@Nullable Reader reader, @Nullable String before, @Nullable String after) throws IOException {
    this.originalReader = reader == null ? new StringReader("") : reader;
    if (before != null) {
      this.before.append(before);
    }
    if (after != null) {
      this.after.append(after);
    }
    // check for XML declaration
    char[] beginning = new char[6];
    int read = this.originalReader.read(beginning);
    if (read == 6 && new String(beginning).equals("<?xml ")) {
      while (true) {
        char c = (char) this.originalReader.read();
        if (c == '>') {
          break;
        }
      }
    } else if (read > 0) {
      this.before.append(beginning, 0, read);
    }
  }

  @Override
  public int read(char[] cbuf, int off, int length) throws IOException {
    // finished?
    if (this.readerFinished && this.secondIndex >= this.after.length())
      return -1;
    int i;
    for (i = 0; i < length; i++) {
      char c;
      // finished?
      if (this.readerFinished && this.secondIndex >= this.after.length()) {
        break;
      } else if (this.readerFinished) {
        c = this.after.charAt(this.secondIndex++);
      } else if (this.firstIndex < this.before.length()) {
        c = this.before.charAt(this.firstIndex++);
      } else {
        int r = this.originalReader.read();
        this.readerFinished = r == -1;
        if (this.readerFinished && this.after.length() != 0) {
          c = this.after.charAt(this.secondIndex++);
        } else if (this.readerFinished) {
          break;
        } else {
          c = (char) r;
        }
      }
      cbuf[off + i] = c;
    }
    return i;
  }

  @Override
  public void close() throws IOException {
    this.originalReader.close();
  }
}
