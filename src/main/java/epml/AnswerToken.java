/*
    特定用途のEPML、HMACパーサー
    
    EPML原文から特定の項目を探し出し、置き換えを行うためにパースする
    いくつかの専用パーサーからなる．
    
*/
package epml;
import epml.tools.Csv;
import epml.tools.DBG;
//import java.io.*;
//import java.text.*;
//import java.util.*;
//
public class AnswerToken extends EpmlToken {
    // 
    public AnswerToken(String   str){
        //
        super(str);
    }
    // 
    // 学生の解答語をEPML原文に埋め込むためのパース
    // 解答語以外については一般文字列として扱う
    // 
    @Override
	public  int getToken()  throws EpmlTokenException {
        //
        try{
            // トークンを切り出して tkn に置き、そのデリミッタ文字の種別をsubID にセット
            // デリミッタ文字からトークン種別を確定する
            char subID  = nextWord();
            //
            if(subID==LBL){
                /*
                 * ![  /]
                 * 
                 */
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();
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
                /*
                 * !{  /}
                 * 
                 */
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
                /*
                 * #[  /]
                 * 
                 */
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
                /*
                 * !{  /}
                 * 
                 */
                getWordBeforeEndmark();
                tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
                ++nETA;
                seq     = _ETA + String.valueOf(nETA) + RBLACKET;
                seqNum  = nETA;
                id      = ETA;
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

