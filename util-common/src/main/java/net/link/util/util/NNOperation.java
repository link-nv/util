package net.link.util.util;

import javax.annotation.Nonnull;


/**
 * An operation that can be applied to a value.
 *
 * @param <T> The type of the value this operation can be applied to.
 */
public interface NNOperation<T> {

    void apply(@Nonnull T input);
}
