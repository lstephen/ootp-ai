package com.github.lstephen.ootp.ai.data;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/** @author lstephen */
public final class Id<T> {

  private final String id;

  private Id(String id) {
    Preconditions.checkNotNull(id);

    this.id = id;
  }

  public String get() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    Id rhs = Id.class.cast(obj);

    return Objects.equals(id, rhs.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).toString();
  }

  public static <T> Id<T> valueOf(String id) {
    return new Id<T>(id);
  }

  public static <T> Id<T> valueOf(Integer id) {
    return valueOf(id.toString());
  }
}
