/*
 *
 */
package Zdraft;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** 階層ディレクトリに保存された複数のファイルを
                     zip形式のアーカイブにする Zip */

public class MakeZip {

  /** ファイルの読み込みの終わりを表す定数 */

     protected static final int EOF = -1;

  /** 対象のディレクトリ */

      protected File dir;

  /** 親のディレクトリ名 */

      protected String parentName;

  /** コンストラクタ */

  	MakeZip( String parentName, File dir ) {

          this.parentName = parentName;
          this.dir = dir;
      }  

  /** 最初に呼び出される処理 */

     public static void main( String argv[] ) {

          if( argv.length != 1 ) {
               System.err.println( "Usage 1:java Zip filename" );
               System.err.println( "Usage 2:java Zip dirname" );
               System.exit(0);
          }
          else {
               try {
                    File file = new File( argv[0] );
                    File zipFile = new File( argv[0] + ".zip" );
                    ZipOutputStream zos
                        = new ZipOutputStream(
                               new FileOutputStream( zipFile ) );

                    if( file.isFile() ) {

                          addTargetFile( zos, file );
                    }
                    else if( file.isDirectory() ) {

						MakeZip origin = new MakeZip( argv[0], file );
                         Vector vec = origin.pathNames();
                         Enumeration enu = vec.elements();
                         while( enu.hasMoreElements() ) {
                              File f = new File(
                                  (String)enu.nextElement() );
                              addTargetFile( zos, f );
                         }
                    }
                    zos.close();
               }
               catch( FileNotFoundException e ){
                      System.err.println( "file not found" );
               }
               catch( ZipException e ){
                      System.err.println( "Zip error..." );
               }
               catch( IOException e ){
                      System.err.println( "IO error..." );
               }
          }
     }

  /** ディレクトリとファイル名の一覧を返すメソッド */

      protected Vector pathNames()
                throws FileNotFoundException, IOException {

          try {
            // ディレクトリ内を調べる

               String[] list = dir.list();
               File[] files = new File[list.length];
               Vector vec = new Vector();
               for( int i=0; i<list.length; i++ ) {
 
                    String pathName = parentName
                           + File.separator + list[i];
                    files[i] = new File( pathName );
                    if( files[i].isFile() )
                        vec.addElement( files[i].getPath() );
               } 
 
             // サブディレクトリを再起的に調べる
 
               for( int i=0; i<list.length; i++ ) {
 
                  if( files[i].isDirectory() ) {
 
                        String name = parentName
                           + File.separator + list[i];
						MakeZip sub = new MakeZip( name, files[i] );
                        Vector sv = sub.pathNames();
                        Enumeration enu = sv.elements();
                        while( enu.hasMoreElements() )
                           vec.addElement( enu.nextElement() );
                    }
               }
               return vec;
          }
          catch( FileNotFoundException e ){
                 throw e;
          }
          catch( IOException e ){
                 throw e;
          }
     }

  /** 与えられたファイルを圧縮し、Zipファイルに追加する */

     public static void addTargetFile(
                         ZipOutputStream zos, File file )
                         throws FileNotFoundException,
                                ZipException,IOException {
          try {
               BufferedInputStream bis
                     = new BufferedInputStream(
                           new FileInputStream( file ) );

               ZipEntry target = new ZipEntry( file.getPath() );
               System.out.println("・path = " + file.getPath());
               zos.putNextEntry( target );  // target の書き込み開始

        /* バッファリングは自動的に行われるので、ここで指定している
           バッファサイズは、特別に意味は持たない  */

               byte buf[] = new byte[1024];
               int count;
               while( ( count = bis.read( buf, 0, 1024 ) ) != EOF ) {
                    zos.write( buf, 0, count );
               }

               bis.close();
               zos.closeEntry();  // target の書き込み終了
          }
          catch( FileNotFoundException e ){
                 throw e;
          }
          catch( ZipException e ){
                 throw e;
          }
          catch( IOException e ){
                 throw e;
          }
     }
}
