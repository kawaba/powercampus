/*
 *
 */
package Zdraft;

/**
 * @
 */
/*
** a simple viewZIP tool
**
** ex.  java ViewZip file.zip
**
*/
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;

class ZipView {
  public static void main(String args[]) throws IOException {
	InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
	ZipInputStream zin = new ZipInputStream(in);
	ZipEntry e;
	System.err.println("Size\t  Date       Time    Method    Ratio   Name");
	System.err.println("----\t  ----       ----    ------    -----   ----");
	while((e=zin.getNextEntry())!= null) {
	  zin.closeEntry();
	  print(e);
	  }
	zin.close();
	}
    
  public static void print(ZipEntry e) {
	PrintStream err = System.err;
	err.print(e.getSize() + "\t");
    
	DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss");
	Date d = new Date(e.getTime());
    
	err.print(df.format(d) + " ");
	if (e.getMethod() == ZipEntry.DEFLATED) {
	  err.print("deflated  ");
	  long size = e.getSize();
	  if (size > 0) {
		long csize = e.getCompressedSize();
		long ratio = ((size-csize)*100) / size;
		if (ratio < 10) {
		  err.write(' ');
		  }
		err.print(ratio + "%   ");
		}
	  else {
		err.print(" 0%    ");
		}
	  }
	else {
	  err.println(" (stored   0 %");
	  }

	  err.println(e.getName());
	}
  }

