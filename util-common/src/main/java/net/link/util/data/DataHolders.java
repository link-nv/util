package net.link.util.data;

import com.google.common.collect.Maps;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


/**
 * Container for the {@link AbstractDataHolder}'s.
 */
public class DataHolders {

    private static final Map<Class<?>, AbstractDataHolder<?>> dataHolders = Maps.newHashMap();

    public static <C> C data(Class<C> dataType, Class<? extends AbstractDataHolder<C>> dataHolderType) {

        return dataType.cast( get( dataType, dataHolderType ).getData() );
    }

    private static <C> AbstractDataHolder<C> get(Class<C> dataType, Class<? extends AbstractDataHolder<C>> dataHolderType) {

        AbstractDataHolder<C> dataHolder = get( dataType );
        if (null == dataHolder) {
            try {
                dataHolder = dataHolderType.getConstructor().newInstance();
            }
            catch (InstantiationException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (IllegalAccessException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (NoSuchMethodException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (InvocationTargetException e) {
                throw new InternalInconsistencyException( e );
            }
            dataHolder.setDataType( dataType );
            dataHolders.put( dataType, dataHolder );
        }
        return dataHolder;
    }

    @SuppressWarnings("unchecked")
    public static <C> AbstractDataHolder<C> get(Class<C> clazz) {

        return (AbstractDataHolder<C>) dataHolders.get( clazz );
    }
}
