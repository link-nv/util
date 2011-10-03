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

    /**
     * Override me to add custom deserialization/serialization modules here
     *
     * @return custom jackson serialization modules or {@code null} if none.
     */
    protected List<Module> getCustomModules() {

        return new LinkedList<Module>();
    }

    private ObjectMapper initMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat( new SimpleDateFormat( "dd-MM-yyyy H:mm:ss" ) );

        // add data module for Data class, it will load it in the data from the location
        SimpleModule dataModule = new SimpleModule( "DataModule", new Version( 1, 0, 0, null ) );
        dataModule.addDeserializer( Data.class, new JsonDataDeserializer() {

            @Nullable
            @Override
            protected byte[] getData(String location) {

                return JsonDataHolder.this.getData( location );
            }
        } );
        mapper.registerModule( dataModule );

        List<Module> customModules = getCustomModules();
        for (Module module : customModules) {
            mapper.registerModule( module );
        }

        return mapper;
    }

    @Override
    protected C loadData(FileReader reader) {

        ObjectMapper mapper = initMapper();

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
        for (Module module : getCustomModules()) {
            exportMapper.registerModule( module );
        }

        exportMapper.writeValue( writer, value );
    }
}
