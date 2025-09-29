/*
 *
 */
package Zdraft;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** zip形式のファイルをファイルシステムに展開する UnZip */

public class MakeUnzip {

  /** データの読み込み終了を表す定数 */

     public static final int EOF = -1;

  /** 最初に呼び出される処理 */

     public static void main( String argv[] ) {

          Enumeration enm;

          if( argv.length == 1 ) {
              try {
                   ZipFile zipFile = new ZipFile( argv[0] );
                   enm = zipFile.entries();

                   while( enm.hasMoreElements() ) {

                         ZipEntry target
                               = (ZipEntry)enm.nextElement();
                         System.out.print(
                                    target.getName() + " ." );
                         getEntry( zipFile, target );
                         System.out.println( ". unpacked" );
                   }
              }
              catch( FileNotFoundException e ){
                     System.err.println( "zipfile not found" );
              }
              catch( ZipException e ){
                     System.err.println( "zip error..." );
              }
              catch( IOException e ){
                     System.err.println( "IO error..." );
              }
         }
         else {
               System.out.println( "Usage:java UnZip zipfile" );
         }
   }

  /** 与えられた ZipEntry の内容を取り出して再生する */

     public static void getEntry( ZipFile zipFile,
                                  ZipEntry target )
                           throws ZipException,IOException {
          try {
               File file = new File( target.getName() );

               if( target.isDirectory() ) {
                     file.mkdirs(); // ディレクトリをリカーシブに作成
               }
               else {
                     BufferedInputStream bis
                         = new BufferedInputStream( 
                              zipFile.getInputStream( target ) ); 

                     String parentName;
                     if( ( parentName = file.getParent() )
                                                     != null ) {
                         File dir = new File( parentName );
                         dir.mkdirs();  // ディレクトリをリカーシブに作成
                    }
                    BufferedOutputStream bos
                          = new BufferedOutputStream( 
                                 new FileOutputStream( file ) );

                    int c;
                    while( ( c = bis.read() ) != EOF ) {
                         bos.write( (byte)c );
                    }
                    bos.close();
               }
          }
          catch( ZipException e ){
                 throw e;
          }
          catch( IOException e ){
                 throw e;
          }
     }
}

