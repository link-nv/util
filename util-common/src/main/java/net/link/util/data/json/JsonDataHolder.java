package net.link.util.data.json;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import net.link.util.data.AbstractDataHolder;
import net.link.util.data.Data;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jetbrains.annotations.Nullable;


public class JsonDataHolder<C> extends AbstractDataHolder<C> {

    static final Logger logger = Logger.get( JsonDataHolder.class );

    private static ObjectMapper mapper;

    /**
     * Override me to add custom deserialization/serialization modules here
     *
     * @return custom jackson serialization modules or {@code null} if none.
     */
    protected List<Module> getCustomModules() {

        return new LinkedList<Module>();
    }

    private void initMapper() {

        if (null == mapper) {
            mapper = new ObjectMapper();
            mapper.setDateFormat( new SimpleDateFormat( "dd-MM-yyyy H:mm:ss" ) );

            // add image module for Image class, it will load it in the image data from the location
            SimpleModule imageModule = new SimpleModule( "ImageModule", new Version( 1, 0, 0, null ) );
            imageModule.addDeserializer( Data.class, new JsonDataDeserializer() {

                @Nullable
                @Override
                protected byte[] getData(String location) {

                    return JsonDataHolder.this.getData( location );
                }
            } );
            mapper.registerModule( imageModule );

            List<Module> customModules = getCustomModules();
            for (Module module : customModules) {
                mapper.registerModule( module );
            }
        }
    }

    @Override
    protected C loadData(FileReader reader) {

        initMapper();

        try {
            return mapper.readValue( reader, dataType );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "Failed to load data", e );
        }
    }

    @Override
    protected Logger getLogger() {

        return logger;
    }

    @Override
    public void exportData(final Writer writer, final Object value)
            throws IOException {

        ObjectMapper exportMapper = new ObjectMapper();

        SimpleModule clientModule = new SimpleModule( "ClientModule", new Version( 1, 0, 0, null ) );
        // only want to output the data content, not the location
        clientModule.addSerializer( Data.class, new JsonDataSerializer() );
        exportMapper.registerModule( clientModule );

        exportMapper.writeValue( writer, value );
    }
}
