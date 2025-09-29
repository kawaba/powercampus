package Zdraft;
/*
** a simple unZIP tool
**
** ex.  java UnZip file.zip file1   to unzip file 1 from file.zip
**      java UnZip file.zip         to unzip file.zip 
**
*/
import java.io.*;
import java.util.zip.*;

class unZip {
	public static void main(String args[]) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry e;

		while((e=zin.getNextEntry())!= null) {
			if (args.length > 1) {
				if (e.getName().equals(args[1])) {
			   		unzip(zin, args[1]);
			   		break;
			  	}
			}
		   	unzip(zin, e.getName());
		}
		zin.close();
	}
    
	public static void unzip(ZipInputStream zin, String s) throws IOException {
		System.out.println("unzipping " + s);
		FileOutputStream out = new FileOutputStream(s);
		byte [] b = new byte[512];
		int len = 0;
		while ( (len=zin.read(b))!= -1 ) {
			out.write(b,0,len);
		}
		out.close();
	}
}
  /*
The following program demonstrates how to use the ZipFile class to create a simple unzip utility. 
It simply cycles through all of the entries in the file and copies each entry to a new file by reading its input stream. 
If an entry is a directory, it is created. The example is not robust and is only intended to demonstrate the ZipFile class. 
Obvious additions would include checking if a file already exists before creating it, 
prompting the user if it should be overwritten, as well as other error checking. View the example>: 

import java.io.*;
import java.util.*;
import java.util.zip.*;


public class Unzip {

  public static final void copyInputStream(InputStream in, OutputStream out)
  throws IOException
  {
	byte[] buffer = new byte[1024];
	int len;

	while((len = in.read(buffer)) >= 0)
	  out.write(buffer, 0, len);

	in.close();
	out.close();
  }

  public static final void main(String[] args) {
	Enumeration entries;
	ZipFile zipFile;

	if(args.length != 1) {
	  System.err.println("Usage: Unzip zipfile");
	  return;
	}

	try {
	  zipFile = new ZipFile(args[0]);

	  entries = zipFile.entries();

	  while(entries.hasMoreElements()) {
		ZipEntry entry = (ZipEntry)entries.nextElement();

		if(entry.isDirectory()) {
		  // Assume directories are stored parents first then children.
		  System.err.println("Extracting directory: " + entry.getName());
		  // This is not robust, just for demonstration purposes.
		  (new File(entry.getName())).mkdir();
		  continue;
		}

		System.err.println("Extracting file: " + entry.getName());
		copyInputStream(zipFile.getInputStream(entry),
		   new BufferedOutputStream(new FileOutputStream(entry.getName())));
	  }

	  zipFile.close();
	} catch (IOException ioe) {
	  System.err.println("Unhandled exception:");
	  ioe.printStackTrace();
	  return;
	}
  }

}
 
*/