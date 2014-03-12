package net.link.util.util;

import com.google.common.base.Supplier;
import javax.annotation.Nonnull;


/**
 * A {@link Supplier} that cannot supply {@code null}.
 *
 * @param <T> The type of the supplied value.
 */
public interface NNSupplier<T> extends Supplier<T> {

    @Nonnull
    @Override
    T get();
}
