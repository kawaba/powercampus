/*
 * 作成日: 2005/09/17
 *
 */
package epml;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import epml.tools.DBG;
import epml.tools.Property;
import epml.tools.TemplateBox;
/**
 *
 */
public class ParserInitializer {
   
    /** 変換用HTMLテンプレートファイルのパス */
    String	templatePath;	// hmac.txt
    
    /**
     *  ユーザーグラフィックスディレクトリのURL
     *  ＝ファイルアップロードディレクトリのURL
     */
    String	userImgURL;
    
    /**
     *  ユーザーグラフィックスディレクトリへの絶対パス
     *  システムグラフィックスをコピーするのに必要
     */
    String	userImgDir;
    
    
    /** システムグラフィックスディレクトリのURL */
    String	sysImgURL;
    
    /** システムグラフィックスディレクトリへの絶対パス */
    String	sysImgDir;

    /** 
     * 資料用HTMLを生成するのかどうか
     * true なら資料用HTML
     */
    boolean	referenceHtmlFlag;
    
    
    /** 
     * aliasの初期値やタイピング試験の評価レベルなどの初期値を持つ
     * 設定ファイル( %textdir%\parserInitialData.txt )
     */
    String	initialDataFile;
    
    
    /** aliasの初期値 */
    Hashtable	aliases;
    
    /** タイピング試験の評価レベルの初期値 */
    String	typingLevel;
    
    
    /** 入力テスト評価レベルの初期値 */
    String	timetrialLevel;
    
    /**
     * コンストラクタ
     *

    public	ParserInitializer(Param para) {
    	var prop = new MyProperty(para.getEmlConfPath());
    	
        this.templatePath		=	prop.get("wikiTemplatePath");
        this.userImgURL			=	prop.get(userImgURL);
        this.userImgDir			=	prop.get(userImgDir);
        this.sysImgURL			=	prop.get(sysImgURL);
        this.sysImgDir			=	prop.get(sysImgDir);
        this.initialDataFile	=	prop.get(initialDataFile);
    	
        this.referenceHtmlFlag	=	false;
    }
     */
    
    public	ParserInitializer(
	            
	            /** 変換用HTMLテンプレートファイルのパス */
	            String	templatePath,
	            
	            /**
	             *  ユーザーグラフィックスディレクトリのURL
	             *  ＝ファイルアップロードディレクトリのURL
	             */
	            String	userImgURL,
	            
	            /**
	             *  ユーザーグラフィックスディレクトリへの絶対パス
	             *  システムグラフィックスをコピーするのに必要
	             */
	            String	userImgDir,
	            
	            
	            /** システムグラフィックスディレクトリのURL */
	            String	sysImgURL,
	            
	            /** システムグラフィックスディレクトリへの絶対パス */
	            String	sysImgDir,
	
	            /** 
	             * 資料用HTMLを生成するのかどうか
	             * true なら資料用HTML
	             */
	            boolean	referenceHtmlFlag,
	            
	            
	            /** 
	             * aliasの初期値やタイピング試験の評価レベルなどの初期値を持つ
	             * 設定ファイル( %textdir%\parserInitialData.txt )
	             */
	            String	initialDataFile
	            
	            //Hashtable	aliases
	                    
    		){
        
        this.templatePath		=	templatePath;
        this.userImgURL			=	userImgURL;
        this.userImgDir			=	userImgDir;
        this.sysImgURL			=	sysImgURL;
        this.sysImgDir			=	sysImgDir;
        this.referenceHtmlFlag	=	referenceHtmlFlag;
        this.initialDataFile	=	initialDataFile;
        
        if(DBG.fa){
            DBG.println("□ ParserInitializer -- コンストラクタ");
            DBG.println("initialDataFile = " + initialDataFile);
        }
        
        /**
         * エイリアスをセットする。
         */
        setInitialData();
        
        //if(aliases!=null){
        //    this.aliases	=	aliases;
        //}
    }
    
    //////////////////////////////////////////////////////////////
    /**
     * 初期値を格納したファイルパス(initialDataFile)を得た後で、
     * ファイルから初期値を読み込んでセットする
     */
    public	void	setInitialData(){
        
        TemplateBox	tb				=	new	TemplateBox(this.initialDataFile);
        String		aliasesData		=	tb.get("format");
        try {
            Property	prop		=	new	Property(new StringReader(aliasesData), ":");
            aliases					=	prop.getHash();
        
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        typingLevel		=	tb.get("typingLevel");
        timetrialLevel	=	tb.get("trialLevel");

        if(DBG.fa){
            if(aliases!=null){
                DBG.println("alias size = " + aliases.size());
            }else{
                DBG.println("alias size = 0 (null)" );
            }
        }
    }
    
    /**
     * オブジェクトのクローンを作成して返す
     */
    @Override
	public	Object	clone(){
        
        ParserInitializer	clonePI	=	new	ParserInitializer(templatePath,userImgURL,userImgDir,sysImgURL,sysImgDir,referenceHtmlFlag,initialDataFile);
        return		clonePI;
        
    }
    //////////////////////////////////////////////////////////////
    
    
    /**
     * @return initialDataFile を戻します。
     */
    public String getInitialDataFile() {
        return initialDataFile;
    }
    /**
     * @param initialDataFile initialDataFile を設定。
     */
    public void setInitialDataFile(String initialDataFile) {
        this.initialDataFile = initialDataFile;
        
    }
    /**
     * @return referenceHtmlFlag を戻します。
     */
    public boolean isReferenceHtmlFlag() {
        return referenceHtmlFlag;
    }
    /**
     * @param referenceHtmlFlag referenceHtmlFlag を設定。
     */
    public void setReferenceHtmlFlag(boolean referenceHtmlFlag) {
        this.referenceHtmlFlag = referenceHtmlFlag;
    }
    /**
     * @return sysImgDir を戻します。
     */
    public String getSysImgDir() {
        return sysImgDir;
    }
    /**
     * @param sysImgDir sysImgDir を設定。
     */
    public void setSysImgDir(String sysImgDir) {
        this.sysImgDir = sysImgDir;
    }
    /**
     * @return sysImgURL を戻します。
     */
    public String getSysImgURL() {
        return sysImgURL;
    }
    /**
     * @param sysImgURL sysImgURL を設定。
     */
    public void setSysImgURL(String sysImgURL) {
        this.sysImgURL = sysImgURL;
    }
    /**
     * @return templatePath を戻します。
     */
    public String getTemplatePath() {
        return templatePath;
    }
    /**
     * @param templatePath templatePath を設定。
     */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
    /**
     * @return userImgDir を戻します。
     */
    public String getUserImgDir() {
        return userImgDir;
    }
    /**
     * @param userImgDir userImgDir を設定。
     */
    public void setUserImgDir(String userImgDir) {
        this.userImgDir = userImgDir;
    }
    /**
     * @return userImgURL を戻します。
     */
    public String getUserImgURL() {
        return userImgURL;
    }
    /**
     * @param userImgURL userImgURL を設定。
     */
    public void setUserImgURL(String userImgURL) {
        this.userImgURL = userImgURL;
    }
    /**
     * @return aliases を戻します。
     */
    public Hashtable getAliases() {
        return aliases;
    }
    /**
     * @param aliases aliases を設定。
     */
    public void setAliases(Hashtable aliases) {
        this.aliases = aliases;
    }
    /**
     * @return timetrialLevel を戻します。
     */
    public String getTimetrialLevel() {
        return timetrialLevel;
    }
    /**
     * @param timetrialLevel timetrialLevel を設定。
     */
    public void setTimetrialLevel(String timetrialLevel) {
        this.timetrialLevel = timetrialLevel;
    }
    /**
     * @return typingLevel を戻します。
     */
    public String getTypingLevel() {
        return typingLevel;
    }
    /**
     * @param typingLevel typingLevel を設定。
     */
    public void setTypingLevel(String typingLevel) {
        this.typingLevel = typingLevel;
    }

	@Override
	public String toString() {
		return "ParserInitializer "
				+ "\n\t templatePath     =" + templatePath
				+ "\n\t userImgURL       =" + userImgURL 
				+ "\n\t  userImgDir      =" + userImgDir 
				+ "\n\t sysImgURL        =" + sysImgURL 
				+ "\n\t sysImgDir        =" + sysImgDir
				+ "\n\t referenceHtmlFlag=" + referenceHtmlFlag 
				+ "\n\t initialDataFile  =" + initialDataFile 
				+ "\n\t aliases          =" + aliases 
				+ "\n\t typingLevel      =" + typingLevel 
				+ "\n\t timetrialLevel   =" + timetrialLevel;
				
	}
    
}
