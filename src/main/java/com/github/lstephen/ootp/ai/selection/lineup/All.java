package com.github.lstephen.ootp.ai.selection.lineup;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

/** @author lstephen */
public final class All<T> implements Iterable<T> {

  private final T vsRhp;
  private final T vsRhpPlusDh;
  private final T vsLhp;
  private final T vsLhpPlusDh;

  private All(Builder<T> builder) {
    Preconditions.checkNotNull(builder.vsRhp);
    Preconditions.checkNotNull(builder.vsRhpPlusDh);
    Preconditions.checkNotNull(builder.vsLhp);
    Preconditions.checkNotNull(builder.vsLhpPlusDh);

    this.vsRhp = builder.vsRhp;
    this.vsRhpPlusDh = builder.vsRhpPlusDh;
    this.vsLhp = builder.vsLhp;
    this.vsLhpPlusDh = builder.vsLhpPlusDh;
  }

  public T getVsRhp() {
    return vsRhp;
  }

  public T getVsRhpPlusDh() {
    return vsRhpPlusDh;
  }

  public T getVsLhp() {
    return vsLhp;
  }

  public T getVsLhpPlusDh() {
    return vsLhpPlusDh;
  }

  @Override
  public UnmodifiableIterator<T> iterator() {
    return ImmutableList.of(vsRhp, vsRhpPlusDh, vsLhp, vsLhpPlusDh).iterator();
  }

  public static <T> Builder<T> builder() {
    return Builder.create();
  }

  private static <T> All<T> build(Builder<T> builder) {
    return new All<T>(builder);
  }

  public static final class Builder<T> {

    private T vsRhp;
    private T vsRhpPlusDh;
    private T vsLhp;
    private T vsLhpPlusDh;

    private Builder() {}

    public Builder<T> vsRhp(T vsRhp) {
      this.vsRhp = vsRhp;
      return this;
    }

    public Builder<T> vsRhpPlusDh(T vsRhpPlusDh) {
      this.vsRhpPlusDh = vsRhpPlusDh;
      return this;
    }

    public Builder<T> vsLhp(T vsLhp) {
      this.vsLhp = vsLhp;
      return this;
    }

    public Builder<T> vsLhpPlusDh(T vsLhpPlusDh) {
      this.vsLhpPlusDh = vsLhpPlusDh;
      return this;
    }

    public All<T> build() {
      return All.build(this);
    }

    private static <T> Builder<T> create() {
      return new Builder<T>();
    }
  }
}
