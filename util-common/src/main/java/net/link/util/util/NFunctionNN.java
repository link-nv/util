package net.link.util.util;

import com.google.common.base.Function;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;


/**
 * A {@link Function} that can be applied only to not-{@code null} values but yield {@code null} as result.
 *
 * @param <T> The type of the value this operation can be applied to.
 */
@SuppressWarnings({ "NullableProblems" })
public interface NFunctionNN<F, T> extends Function<F, T> {

    @Nullable
    @Override
    T apply(@Nonnull F input);

    boolean equals(@Nonnull Object object);

    int hashCode();
}
