package net.link.util.data.json;

import java.io.IOException;
import net.link.util.data.Data;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.*;
import org.jetbrains.annotations.Nullable;


/**
 * Custom deserializer for the {@link Data} class.
 */
public abstract class JsonDataDeserializer extends JsonDeserializer<Data> {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public Data deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        Data data = mapper.readValue( jp, Data.class );
        data.data = getData( data.location );
        return data;
    }

    @Nullable
    protected abstract byte[] getData(String location);
}
