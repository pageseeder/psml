package org.pageseeder.psml.diff;

/**
 * Small value object describing what happened during a diff.
 *
 * <p>This is intentionally minimal but extensible (timings, algorithm used, sizes, etc.)
 * without forcing changes to existing method signatures.
 *
 * @author Christophe Lauret
 *
 * @since 1.7.3
 * @version 1.7.3
 */
public final class DiffReport {
  private final boolean changed;
  private final boolean fallbackUsed;
  private final long durationNanos;

  DiffReport(boolean changed, boolean fallbackUsed, long durationNanos) {
    this.changed = changed;
    this.fallbackUsed = fallbackUsed;
    this.durationNanos = durationNanos;
  }

  DiffReport(boolean changed, boolean fallbackUsed) {
    this.changed = changed;
    this.fallbackUsed = fallbackUsed;
    this.durationNanos = 0L;
  }

  DiffReport withDuration(long durationNanos) {
    return new DiffReport(this.changed, this.fallbackUsed, durationNanos);
  }

  /**
   * Determines whether there are any differences or changes in the diff operation.
   *
   * @return {@code true} if changes have been detected, {@code false} otherwise.
   */
  public boolean hasChanges() {
    return this.changed;
  }

  /**
   * Indicates whether the primary algorithm failed and had to use a fallback.
   *
   * @return {@code true} if a fallback was used, {@code false} otherwise.
   */
  public boolean fallbackUsed() {
    return this.fallbackUsed;
  }

  /**
   * Returns the duration of the diff operation in nanoseconds.
   *
   * @return The duration of the diff operation in nanoseconds.
   */
  public long durationNanos() {
    return this.durationNanos;
  }

}