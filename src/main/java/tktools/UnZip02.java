/*
 * 作成日: 2005/02/01
 *
 * TODO
 */
package tktools;

/**
 *
 */
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class UnZip02 {
	static final int BUFFER = 2048;
	public static void main (String argv[]) {
		try {
			BufferedOutputStream 	dest 	= null;
			BufferedInputStream 	is 		= null;
			ZipEntry entry;
			ZipFile 				zipfile = new ZipFile("d:\\test\\test.zip");
			Enumeration 			e 		= zipfile.entries();
			while(e.hasMoreElements()) {
				entry 					= (ZipEntry) e.nextElement();
				is 						= new BufferedInputStream (zipfile.getInputStream(entry));
				byte data[] 			= new byte[BUFFER];
				FileOutputStream fos 	= new FileOutputStream(entry.getName());
				dest 					= new BufferedOutputStream(fos, BUFFER);

				System.out.println("Extracting: " + entry);
				int count;
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}


