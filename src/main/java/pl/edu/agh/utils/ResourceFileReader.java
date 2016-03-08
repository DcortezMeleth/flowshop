package pl.edu.agh.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class ResourceFileReader {

    /** Prefix of tmp file */
    private static final String PREFIX = "input";

    /** Suffix of tmp file*/
    private static final String SUFFIX = ".tmp";

    /** tmp files counter */
    private static int counter = 0;

    /** Gets content of file from resources directory. */
    public File getResourcesFile(final String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(PREFIX + counter++, SUFFIX);
            tmpFile.deleteOnExit();
            try(FileOutputStream result = new FileOutputStream(tmpFile)) {
                IOUtils.copy(classLoader.getResourceAsStream(fileName), result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmpFile;
    }

}
