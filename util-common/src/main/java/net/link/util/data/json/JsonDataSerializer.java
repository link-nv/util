package net.link.util.data.json;

import java.io.IOException;
import net.link.util.data.Data;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;


/**
 * Custom JSon Serializer for {@link Data} classes which outputs the
 * image's data base64 encoded and leaves away the location property.
 */
public class JsonDataSerializer extends JsonSerializer<Data> {

    @Override
    public void serialize(Data value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        if (null != value.data) {
            jgen.writeString( new String( Base64.encode( value.data ) ) );
        } else {
            jgen.writeString( JsonToken.VALUE_NULL.asString() );
        }
    }
}
