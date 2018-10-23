/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Wrapping reader.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 5.9100
 * @since 4.9019
 */
public class WrappingReader extends Reader {

  /**
   * The original wrapped reader.
   */
  private final Reader _originalReader;

  /**
   * Flag indicating whether the original reader has been reader entirely
   */
  private boolean readerFinished = false;

  /**
   * The text to put before.
   */
  private final StringBuilder _before = new StringBuilder();

  /**
   * The text to put after
   */
  private final StringBuilder _after = new StringBuilder();

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
   */
  public WrappingReader(Reader reader, String before, String after) throws IOException {
    this._originalReader = reader == null ? new StringReader("") : reader;
    if (before != null) {
      this._before.append(before);
    }
    if (after != null) {
      this._after.append(after);
    }
    // check for XML declaration
    char[] beginning = new char[6];
    int read = this._originalReader.read(beginning);
    if (read == 6 && new String(beginning).equals("<?xml ")) {
      while (true) {
        char c = (char) this._originalReader.read();
        if (c == '>') {
          break;
        }
      }
    } else if (read > 0) {
      this._before.append(beginning, 0, read);
    }
  }

  @Override
  public int read(char[] cbuf, int off, int length) throws IOException {
    // finished?
    if (this.readerFinished && this.secondIndex >= this._after.length())
      return -1;
    int i;
    for (i = 0; i < length; i++) {
      char c;
      // finished?
      if (this.readerFinished && this.secondIndex >= this._after.length()) {
        break;
      } else if (this.readerFinished) {
        c = this._after.charAt(this.secondIndex++);
      } else if (this.firstIndex < this._before.length()) {
        c = this._before.charAt(this.firstIndex++);
      } else {
        int r = this._originalReader.read();
        this.readerFinished = r == -1;
        if (this.readerFinished && this._after.length() != 0) {
          c = this._after.charAt(this.secondIndex++);
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
    this._originalReader.close();
  }
}
