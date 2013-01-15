package org.sistemavotacion.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import android.util.Log;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/blob/master/licencia.txt
*/
public class FileUtils {
	
    public static byte[] getBytesFromFile(File file) throws IOException {
        byte[] b = new byte[(int) file.length()];
        FileInputStream fs = new FileInputStream(file);
        fs.read(b);
        fs.close();
        return b;
    }
    
    public static byte[] getBytesFromInputStream(InputStream entrada) throws IOException {
    	ByteArrayOutputStream salida = new ByteArrayOutputStream();
        byte[] buf =new byte[1024];
        int len;
        while((len = entrada.read(buf)) > 0){
            salida.write(buf,0,len);
        }
        salida.close();
        entrada.close();
        return salida.toByteArray();
    }

   public static File copyFileToFile(File inputFile, File outputFile)
         throws Exception {
       FileInputStream fs = new FileInputStream(inputFile);
       return copyStreamToFile(fs, outputFile);
    }

    public static File copyStreamToFile(InputStream entrada, File outputFile)
         throws Exception {
        OutputStream salida = new FileOutputStream(outputFile);
        byte[] buf =new byte[1024];
        int len;
        while((len = entrada.read(buf)) > 0){
            salida.write(buf,0,len);
        }
        salida.close();
        entrada.close();
        return outputFile;
    }
	
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[(int) src.length()];
        in.read(buf);
        out.write(buf);
        in.close();
        out.close();
    }
     
    public static String getStringFromFile (File file) 
            throws FileNotFoundException, IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        }
        finally {
            stream.close();
        }
     }

    public static void save(String content, String filePath,
            String fileExtension) {
        String rutaCompletaArchivo = filePath;
        if (!(fileExtension == null || fileExtension.equals("")))
            rutaCompletaArchivo = filePath + "." + fileExtension;
        try {
            File archivoSalida = new File(rutaCompletaArchivo);
            FileWriter out = new FileWriter(archivoSalida);
            out.write(content);
            out.close();
        } catch (IOException ex) {
        	Log.e("FileUtils", ex.getMessage(), ex);
        }
    }

}
