/*
    特定用途のEPMLパーサー
    
    EPML原文から特定の項目を探し出し、置き換えを行うためにパースする
    いくつかの専用パーサーからなる．
    
*/
package epml;
import  epml.tools.*;
//import java.io.*;
//import java.text.*;
//import java.util.*;
//
public class SearchToken extends EpmlToken {
    // 
    public SearchToken(String   str){
        //
        super(str);
    }
    //
    //  選択肢語群の指示語のみ探索し、あとは全てテキストとみなして処理する
    //
    public  int searchALST()  throws EpmlTokenException {
        //
        try{
            //
            char subID  = nextALST();   
            //
            
            if(subID==ALST){
				// 解答選択肢書き込み位置( [+  ) -- ひとつの問題グループが終了したとみなせる
				
				/*
				 * 終了文字までの文字列をtknに取得する
				 */				
				getWordBeforeEndmark(); 
                
                Csv cs = tblOption(tkn);    // 語群テーブルサイズと語順の指定を取り出す（なければ "5" と"" ）
                String  sz  = cs.get(0);    // 語群テーブルサイズ
                String  pm  = "";
                if(cs.size() > 1)   pm = cs.get(1);     // 語順
                //
                if(pm.length() >0) sz = sz + "/" + pm;
                //
                ++nLST;
                seq     = _ALST + String.valueOf(nLST) + "/" + sz + RBLACKET;   // 例：ALST(1/6) , ALST(1/6/2-4-3-1)
                seqNum  = nLST;
                id      = ALST;
            //  
            // 問題文のパート
            }else{  // STR
                //tkn = tkn;
                ++nTEXT;
                seq      = _TEXT + String.valueOf(nTEXT) + "/" + tkn + RBLACKET;
                seqNum   = nTEXT;
                id       = TEXT;
                //
            }
            //
        }catch(EpmlTokenException e){
            int ret = 0;
            if(e.getMessageValue() == EpmlTokenException.EndOfBuffer){
                return -1;
            }else{
                throw e;
            }
        }
        //
        return  id;
    }
    //
    private char nextALST() throws EpmlTokenException {
        //
        // バッファが空だと例外を発生
        char    c1 = 0;
        char    c2 = 0;
        if( (c1=nextChar()) == 0 ){
            throw new EpmlTokenException(EpmlTokenException.EndOfBuffer);
        }
        /*
         * 制御文字ならその１文字を返す
         */
        char subID  = firstChar(c1);
        //
        if(subID==LBL){ // [ であればさらにチェック
        /*
         * [@10 /]          配点
         * [& abc,def /]    ダミー選択肢
         * [+ 語群(3) /]    選択肢語群
         */
            if( (c2=nextChar()) == 0 ){
                tkn =   String.valueOf(c1);
                return STR;             
            }
            subID   =   secondChar(c2);
            if(subID==PLS){
                subID = ALST;       // subID を [+　に変更
                return  subID;

            }else{
                StringBuffer bf = new StringBuffer(1024);
                bf.append(c1);
                bf.append(c2);
                normalStrings(bf);
                return STR;             
            }
        }else{
            // その他の場合一般の文字なので文字列にして返す
            StringBuffer bf = new StringBuffer(1024);
            bf.append(c1);          // c を戻す
            normalStrings(bf);
            return STR;
        }
    }
    // 
    // 学生の解答語をEPML原文に埋め込むためのパース
    // 解答語以外については一般文字列として扱う
    // 
    public  int searchAnswer()  throws EpmlTokenException {
        //
        try{
            // トークンを切り出して tkn に置き、そのデリミッタ文字の種別をsubID にセット
            // デリミッタ文字からトークン種別を確定する
            char subID  = nextWord();
            //
            if(subID==LBL){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                //
                // Csv は StringTokenizer で文字列を切るので複数デリミッタに対応している
                // また、要素に格納するとき trim() するので、空白のみのデータは長さ＝０である
                Csv cs = new Csv(tkn,",，、|｜");   // デリミッタ
                //
                // 文中に埋め込んだドロップダウンリストから正解を選ぶ形式
                if(cs.notZeroLengthCount() > 1){
                    ++nEWL;
                    seq     = _EWL + String.valueOf(nEWL) + RBLACKET;
                    seqNum  = nEWL;
                    id      = EWL;
                //
                // 文末の選択肢一覧から該当の番号をテキストボックスに書き込む形式
                }else{
                    ++nENL;
                    if(newENLFLAG){
                        newENLFLAG = false;     // ひとつ要素を加えるので flase にする
                        ++groupNumENL;          // 新しいグループになるのでグループ番号を１増やす（１から始まる）
                        nENL = 1;               // 要素番号もクリアしておく（１オリジン）
                    }
                    if(tkn.length()==0){
                        throw new EpmlTokenException( p, "選択語が指定されていません．" ); 
                    }
                    seq     = _ENL + String.valueOf(groupNumENL) + "," + String.valueOf(nENL) + RBLACKET;
                    seqNum  = nENL;
                    id      = ENL;
                }
                sequency = false;
            //
            // 文中に埋め込んだテキストボックスに解答語句を書き込む形式
            }else if(subID==LBM){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nETF;
                seq     = _ETF + String.valueOf(nETF) + RBLACKET;
                seqNum  = nETF;
                id      = ETF;
                sequency = false;
            //
            // 複数行のリストから、ラジオボタンで正解をチェックする形式．複数回答ではチェックボックスとなる．
            }else if(subID==SEL){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nERB;
                if(!sequency){
                    ++groupNum;     // 新しいグループの始まりなら番号を１増やす
                    nERB    = 1;    // シーケンス番号も初期化する
                }
                seq     = _ERB + String.valueOf(groupNum) + "," + String.valueOf(nERB) + RBLACKET;
                seqNum  = nERB;
                id      = ERB;
                sequency  = true;   // 連続
            //
            // テキストエリアに解答文を書き込む形式
            }else if(subID==WRT){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nETA;
                seq     = _ETA + String.valueOf(nETA) + RBLACKET;
                seqNum  = nETA;
                id      = ETA;
                sequency = false;
            //
            // 配点（ [@nn] ）
            }else if(subID==PNT){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nEPT;
                seq     = _EPT + String.valueOf(nEPT) + RBLACKET;
                seqNum  = nEPT;
                id      = EPT;
                sequency = false;
            //
            // 制限時間（ {@nn} ）
            }else if(subID==TML){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                seq     = _TIME + String.valueOf(1) + RBLACKET; // 未使用のダミー
                seqNum  = 1;                                    // 未使用のダミー
                id      = TIME;
                sequency = false;
            //
            // 追加的なダミーの埋め込み語句（ [&aaa,bbb,ccc] ）
            }else if(subID==BAM){
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nAENL;
                seq     = _AENL + String.valueOf(nAENL) + RBLACKET;
                seqNum  = nAENL;
                id      = AENL;
                sequency = false;
            //
            // 解答選択肢書き込み位置( ++ ) -- ひとつの問題グループが終了したとみなせる
            }else if(subID==LST){
                getWordBeforeEndmark();
                Csv cs = tblOption(tkn);    // 語群テーブルサイズと語順の指定を取り出す（なければ "5" と"" ）
                if(cs==null) throw new EpmlTokenException(p,"選択語を並べる語順の指定に誤りがあります");
                //
                String  sz  = cs.get(0);    // 語群テーブルサイズ
                String  pm  ="";
                if(cs.size() > 1){
                    pm  = cs.get(1);    // 語順
                }
                if(pm.length() >0) sz = sz + "/" + pm;
                //
                ++nLST;
                seq     = _ALST + String.valueOf(nLST) + "/" + sz + RBLACKET;   // 例：ALST(1/6) , ALST(1/6/2-4-3-1)
                seqNum  = nLST;
                id      = ALST;
                //
                newENLFLAG  = true;     // グループが終了したことをENLへ伝える
                //
                sequency = false;
            //  
            // 問題文のパート
            }else{  // STR
                //tkn = tkn;
                ++nTEXT;
                seq      = _TEXT + String.valueOf(nTEXT) + "/" + tkn + RBLACKET;
                seqNum   = nTEXT;
                id       = TEXT;
                sequency = (sequency && !count()); // 直前が true でかつ空白と改行のみの文字列なら true となる
                //
            }
            //
        }catch(EpmlTokenException e){
            int ret = 0;
            if(e.getMessageValue() == EpmlTokenException.EndOfBuffer){
                return -1;
            }else{
                DBG.println("＊＊＊　例外発生　＊＊＊");
                e.printStackTrace();
                DBG.println("＊＊＊＊＊＊＊＊＊＊＊＊");
                throw e;
            }
        }
        //
        return  id;
    }
}

