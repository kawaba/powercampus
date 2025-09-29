/*
 * 作成日: 2005/02/11
 *
 * TODO
 */
package kadai;

/**
 *
 */
public interface KadaiVar {
	
	/**
	 * KdaiInfo DB のフィールドアクセス名
	 */
	public	static	final String	KI_SHUBETSU		=	"_shubetsu";
	public	static	final String	KI_SAITEN		=	"_saitenFlag";
	public	static	final String	KI_DATECSV		=	"_dateCsv";
	public	static	final String	KI_SUBJECT		=	"_subject";
	public	static	final String	KI_POINTS		=	"_points";

	/**
	 *  完成したので提出する mkAnswer.html の _radio_disposal の値
	 * (checked ではない.タイプミス．)
	 */
	//public	static	final String 	REPO_CHECKED	= "checed";	
	public	static	final String 	MAIL_DONE		= "done";
	public	static final String 	CHECKED			= "checked";	//完成したので提出する mkAnswer.html の _radio_disposal の値
	/**
	 * 解答レコードのソート状態 
	 * 　　番号順か提出日順か
	 */
	public	static	final	String	NUMBER_MODE	=	"number_mode";
	public	static	final	String	DATE_MODE	=	"date_mode";	
	
	public	static	final	String	PREVIEW_ON		=	"ON";
	public	static	final	String	PREVIEW_OFF		=	"OFF";	
}
