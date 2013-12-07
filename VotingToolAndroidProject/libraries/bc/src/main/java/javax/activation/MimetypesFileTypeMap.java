/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)MimetypesFileTypeMap.java	1.18 07/05/14
 */

package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MimeTypeFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

/**
 * This class extends FileTypeMap and provides data typing of files
 * via their file extension. It uses the <code>.mime.types</code> format. <p>
 *
 * <b>MIME types file search order:</b><p>
 * The MimetypesFileTypeMap looks in various places in the user's
 * system for MIME types file entries. When requests are made
 * to search for MIME types in the MimetypesFileTypeMap, it searches  
 * MIME types files in the following order:
 * <p>
 * <ol>
 * <li> Programmatically added entries to the MimetypesFileTypeMap instance.
 * <li> The file <code>.mime.types</code> in the user's home directory.
 * <li> The file &lt;<i>java.home</i>&gt;<code>/lib/mime.types</code>.
 * <li> The file or resources named <code>META-INF/mime.types</code>.
 * <li> The file or resource named <code>META-INF/mimetypes.default</code>
 * (usually found only in the <code>activation.jar</code> file).
 * </ol>
 * <p>
 * <b>MIME types file format:</b><p>
 *
 * <code>
 * # comments begin with a '#'<br>
 * # the format is &lt;mime type> &lt;space separated file extensions><br>
 * # for example:<br>
 * text/plain    txt text TXT<br>
 * # this would map file.txt, file.text, and file.TXT to<br>
 * # the mime type "text/plain"<br>
 * </code>
 *
 * @author Bart Calder
 * @author Bill Shannon
 */
public class MimetypesFileTypeMap extends FileTypeMap {
    /*
     * We manage a collection of databases, searched in order.
     * The default database is shared between all instances
     * of this class.
     * XXX - Can we safely share more databases between instances?
     */
    private static MimeTypeFile defDB = null;
    private MimeTypeFile[] DB;
    private static final int PROG = 0;	// programmatically added entries

    private static String defaultType = "application/octet-stream";

    /**
     * The default constructor.
     */
    public MimetypesFileTypeMap() {
	Vector dbv = new Vector(5);	// usually 5 or less databases
	MimeTypeFile mf = null;
	dbv.addElement(null);		// place holder for PROG entry

	LogSupport.log("MimetypesFileTypeMap: load HOME");
	try {
	    String user_home = System.getProperty("user.home");

	    if (user_home != null) {
		String path = user_home + File.separator + ".mime.types";
		mf = loadFile(path);
		if (mf != null)
		    dbv.addElement(mf);
	    }
	} catch (SecurityException ex) {}

	LogSupport.log("MimetypesFileTypeMap: load SYS");
	try {
	    // check system's home
	    String system_mimetypes = System.getProperty("java.home") +
		File.separator + "lib" + File.separator + "mime.types";
	    mf = loadFile(system_mimetypes);
	    if (mf != null)
		dbv.addElement(mf);
	} catch (SecurityException ex) {}

	LogSupport.log("MimetypesFileTypeMap: load JAR");
	// load from the app's jar file
	loadAllResources(dbv, "META-INF/mime.types");

	LogSupport.log("MimetypesFileTypeMap: load DEF");
	synchronized (MimetypesFileTypeMap.class) {
	    // see if another instance has created this yet.
	    if (defDB == null)
		defDB = loadResource("/META-INF/mimetypes.default");
	}

	if (defDB != null)
	    dbv.addElement(defDB);

	DB = new MimeTypeFile[dbv.size()];
	dbv.copyInto(DB);
    }

    /**
     * Load from the named resource.
     */
    private MimeTypeFile loadResource(String name) {
	InputStream clis = null;
	try {
	    clis = SecuritySupport.getResourceAsStream(this.getClass(), name);
	    if (clis != null) {
		MimeTypeFile mf = new MimeTypeFile(clis);
		if (LogSupport.isLoggable())
		    LogSupport.log("MimetypesFileTypeMap: successfully " +
			"loaded mime types file: " + name);
		return mf;
	    } else {
		if (LogSupport.isLoggable())
		    LogSupport.log("MimetypesFileTypeMap: not loading " +
			"mime types file: " + name);
	    }
	} catch (IOException e) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MimetypesFileTypeMap: can't load " + name, e);
	} catch (SecurityException sex) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MimetypesFileTypeMap: can't load " + name, sex);
	} finally {
	    try {
		if (clis != null)
		    clis.close();
	    } catch (IOException ex) { }	// ignore it
	}
	return null;
    }

    /**
     * Load all of the named resource.
     */
    private void loadAllResources(Vector v, String name) {
	boolean anyLoaded = false;
	try {
	    URL[] urls;
	    ClassLoader cld = null;
	    // First try the "application's" class loader.
	    cld = SecuritySupport.getContextClassLoader();
	    if (cld == null)
		cld = this.getClass().getClassLoader();
	    if (cld != null)
		urls = SecuritySupport.getResources(cld, name);
	    else
		urls = SecuritySupport.getSystemResources(name);
	    if (urls != null) {
		if (LogSupport.isLoggable())
		    LogSupport.log("MimetypesFileTypeMap: getResources");
		for (int i = 0; i < urls.length; i++) {
		    URL url = urls[i];
		    InputStream clis = null;
		    if (LogSupport.isLoggable())
			LogSupport.log("MimetypesFileTypeMap: URL " + url);
		    try {
			clis = SecuritySupport.openStream(url);
			if (clis != null) {
			    v.addElement(new MimeTypeFile(clis));
			    anyLoaded = true;
			    if (LogSupport.isLoggable())
				LogSupport.log("MimetypesFileTypeMap: " +
				    "successfully loaded " +
				    "mime types from URL: " + url);
			} else {
			    if (LogSupport.isLoggable())
				LogSupport.log("MimetypesFileTypeMap: " +
				    "not loading " +
				    "mime types from URL: " + url);
			}
		    } catch (IOException ioex) {
			if (LogSupport.isLoggable())
			    LogSupport.log("MimetypesFileTypeMap: can't load " +
						url, ioex);
		    } catch (SecurityException sex) {
			if (LogSupport.isLoggable())
			    LogSupport.log("MimetypesFileTypeMap: can't load " +
						url, sex);
		    } finally {
			try {
			    if (clis != null)
				clis.close();
			} catch (IOException cex) { }
		    }
		}
	    }
	} catch (Exception ex) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MimetypesFileTypeMap: can't load " + name, ex);
	}

	// if failed to load anything, fall back to old technique, just in case
	if (!anyLoaded) {
	    LogSupport.log("MimetypesFileTypeMap: !anyLoaded");
	    MimeTypeFile mf = loadResource("/" + name);
	    if (mf != null)
		v.addElement(mf);
	}
    }

    /**
     * Load the named file.
     */
    private MimeTypeFile loadFile(String name) {
	MimeTypeFile mtf = null;

	try {
	    mtf = new MimeTypeFile(name);
	} catch (IOException e) {
	    //	e.printStackTrace();
	}
	return mtf;
    }

    /**
     * Construct a MimetypesFileTypeMap with programmatic entries
     * added from the named file.
     *
     * @param mimeTypeFileName	the file name
     */
    public MimetypesFileTypeMap(String mimeTypeFileName) throws IOException {
	this();
	DB[PROG] = new MimeTypeFile(mimeTypeFileName);
    }

    /**
     * Construct a MimetypesFileTypeMap with programmatic entries
     * added from the InputStream.
     *
     * @param is	the input stream to read from
     */
    public MimetypesFileTypeMap(InputStream is) {
	this();
	try {
	    DB[PROG] = new MimeTypeFile(is);
	} catch (IOException ex) {
	    // XXX - really should throw it
	}
    }

    /**
     * Prepend the MIME type values to the registry.
     *
     * @param mime_types A .mime.types formatted string of entries.
     */
    public synchronized void addMimeTypes(String mime_types) {
	// check to see if we have created the registry
	if (DB[PROG] == null)
	    DB[PROG] = new MimeTypeFile(); // make one

	DB[PROG].appendToRegistry(mime_types);
    }

    /**
     * Return the MIME type of the file object.
     * The implementation in this class calls
     * <code>getContentType(f.getName())</code>.
     *
     * @param f	the file
     * @return	the file's MIME type
     */
    public String getContentType(File f) {
	return this.getContentType(f.getName());
    }

    /**
     * Return the MIME type based on the specified file name.
     * The MIME type entries are searched as described above under
     * <i>MIME types file search order</i>.
     * If no entry is found, the type "application/octet-stream" is returned.
     *
     * @param filename	the file name
     * @return		the file's MIME type
     */
    public synchronized String getContentType(String filename) {
	int dot_pos = filename.lastIndexOf("."); // period index

	if (dot_pos < 0)
	    return defaultType;

	String file_ext = filename.substring(dot_pos + 1);
	if (file_ext.length() == 0)
	    return defaultType;

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    String result = DB[i].getMIMETypeString(file_ext);
	    if (result != null)
		return result;
	}
	return defaultType;
    }

    /**
     * for debugging...
     *
    public static void main(String[] argv) throws Exception {
	MimetypesFileTypeMap map = new MimetypesFileTypeMap();
	System.out.println("File " + argv[0] + " has MIME type " +
						map.getContentType(argv[0]));
	System.exit(0);
    }
    */
}
