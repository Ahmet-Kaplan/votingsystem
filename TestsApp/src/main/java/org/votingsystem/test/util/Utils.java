package org.votingsystem.test.util;

import org.votingsystem.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static void propertyValuesToLowerCase(String sourceFilePath, String destFilePath) throws IOException {
        Properties props = new Properties();
        byte[] propertBytes = FileUtils.getBytesFromStream(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceFilePath));
        //Use a Reader when working with strings. InputStreams are really meant for binary data.
        props.load( new StringReader(new String(propertBytes, "UTF-8")));
        Enumeration properties = props.propertyNames();
        while(properties.hasMoreElements()) {
            String propertyName = (String) properties.nextElement();
            props.put(propertyName, ((String)props.get(propertyName)).toLowerCase());
        }
        File result = new File(destFilePath);
        log.info("AbsolutePath: " + result.getAbsolutePath());
        result.createNewFile();
        props.store(new FileOutputStream(result), null);
    }

}
