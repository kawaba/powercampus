/*
 * 作成日: 2005/02/11
 *
 * TODO
 */
package kamoku;

/**
 *
 */
public interface KamokuVar {
	
	/*
	 *  エクスポート関連の変数 
	 */
	
	/** 科目定義ファイル名 */
	public static final String	KAMOKU_DEF_FILE 			= "kamoku.def";

	/** emlファイルディレクトリ(eml)を圧縮したファイル名　*/
	public static final String	KAMOKU_EML_FILE 			= "eml.zip";

	/** htmlファイルディレクトリ(html)を圧縮したファイル名　*/
	public static final String	KAMOKU_HTML_FILE 			= "html.zip";

	/** 課題のグラフィックスデータディレクトリ(files)を圧縮したファイル名　*/
	public static final String	KAMOKU_GRPH_FILE 			= "kadaigrph.zip";
	
	/**
	 * 科目定義作成のために生成されるHtmlのパラメータ
	 */
	public	static	final	String	DEFAULT_COLS_F	=	"50";
	public	static	final	String	DEFAULT_ROWS	=	"4";
	public	static	final	String	DEFAULT_COLS_A	=	"90";
	
	/**
	 * 科目定義XMLが新しいserialに移ったかどうかを記憶しておく値 
	 * ref. KamokuEdit.java
	 */
	public	static	final	String	CHANGED			=	"CHANGED";
	public	static	final	String	NOT_CHANGED		=	"";
	/**
	 * 科目定義レコードが古いレコードから新しいXML形式のレコードに更新されたかどうか
	 * のマーク．
	 * 更新されたレコードはレコードの bikou 欄にこの値が書き込まれる
	 */
	public	static	final	String	UPDATED_MARK	=	"** UPDATED **";
}
