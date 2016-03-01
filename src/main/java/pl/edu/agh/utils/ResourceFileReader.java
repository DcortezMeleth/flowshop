package pl.edu.agh.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class ResourceFileReader {

    /** Gets content of file from resources directory. */
    public String getFileContent(final String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        String result = "";
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /** Gets content of file from resources directory. */
    public File getFile(final String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File result = null;
        if(classLoader.getResource(fileName) != null) {
            result = new File(classLoader.getResource(fileName).getFile());
        }

        return result;
    }

}
