package net.link.util.util;

import com.google.common.base.Supplier;
import javax.annotation.Nullable;


/**
 * A {@link Supplier} that can supply {@code null}.
 *
 * @param <T> The type of the supplied value.
 */
public interface NSupplier<T> extends Supplier<T> {

    @Nullable
    @Override
    T get();
}
