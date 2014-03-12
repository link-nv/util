package net.link.util.util;

import javax.annotation.Nullable;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 11:34
 */
@ObjectMeta
public abstract class MetaObject {

    @Override
    public int hashCode() {

        return ObjectUtils.hashCode( this );
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(@Nullable final Object obj) {

        return ObjectUtils.equals( this, obj );
    }

    @Override
    public String toString() {

        return ObjectUtils.toString( this );
    }
}
