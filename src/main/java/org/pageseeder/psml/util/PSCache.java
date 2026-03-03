package org.pageseeder.psml.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An LRU Cache implementation
 *
 * @version 1.7.3
 * @since 0.6.5
 */
public class PSCache<K, V> extends LinkedHashMap<K, V> {

  private final int maxSize;

  public PSCache(int size) {
    super(size, 0.75f, true);
    this.maxSize = size;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    // remove the oldest element if size limit is reached
    return size() > maxSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PSCache<?, ?>)) return false;
    if (!super.equals(o)) return false;
    PSCache<?, ?> psCache = (PSCache<?, ?>) o;
    return this.maxSize == psCache.maxSize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), maxSize);
  }

}
