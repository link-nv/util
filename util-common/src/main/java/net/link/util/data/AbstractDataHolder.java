package net.link.util.data;

import static com.google.common.base.Preconditions.*;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import com.lyndir.lhunath.opal.system.util.TypeUtils;
import java.io.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;


public abstract class AbstractDataHolder<C> {

    protected Class<C> dataType;
    private   C        data;
    private   DateTime lastUpdated;

    /**
     * Override me for different ways to provide the root data file.
     * <p/>
     * By default it looks for a {@link DataLocation} annotation containing the
     * absolute path of the data file
     *
     * @return the data file
     */
    protected File getDataFile() {

        DataLocation dataLocationAnnotation = TypeUtils.findAnnotation( dataType, DataLocation.class );
        checkNotNull( dataLocationAnnotation, "No @Data annotation found on %s ?!", dataType );

        return new File( dataLocationAnnotation.location() );
    }

    /**
     * Override me for a different way of fetching image data.
     *
     * @param location location of the image
     *
     * @return the image file or {@code null} if not found.
     */
    @Nullable
    protected byte[] getData(String location) {

        // read in image file
        File imageFile = new File( location );
        if (imageFile.exists()) {
            try {
                return FileUtils.readFileToByteArray( imageFile );
            }
            catch (IOException e) {
                throw new InternalInconsistencyException( e );
            }
        } else {
            getLogger().wrn( "image data for \"%s\" not found!", location );
            return null;
        }
    }

    private void loadData() {

        try {
            FileReader reader = new FileReader( getDataFile() );
            try {
                data = loadData( reader );
                lastUpdated = new DateTime();
            }
            finally {
                reader.close();
            }
        }
        catch (FileNotFoundException e) {
            getLogger().err( e.getMessage(), e );
        }
        catch (IOException e) {
            getLogger().err( e.getMessage(), e );
        }

        checkNotNull( data, "Data not found or unable to parse" );
    }

    private boolean hasToBeUpdated() {

        // first time?
        return null == data || null == lastUpdated || isOutDated( getDataFile() );
    }

    private boolean isOutDated(File dataFile) {

        return new DateTime( dataFile.lastModified() ).isAfter( lastUpdated );
    }

    // Accessors

    public C getData() {

        if (hasToBeUpdated()) {
            getLogger().dbg( "Data update needed..." );
            loadData();
        }
        return data;
    }

    public void setDataType(Class<C> dataType) {

        this.dataType = dataType;
    }

    protected abstract C loadData(FileReader reader);

    public abstract void exportData(Writer writer, Object value)
            throws IOException;

    protected abstract Logger getLogger();
}
