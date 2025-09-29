
/*
	範囲の決まったランダムナンバーを保持し
	ランダムに選んだ順列内の番号を返す
*/
package epml;
//import	epml.tools.*;
import java.util.Random;
public class RandomPermutation extends Object{
	
	int	[]	permMap;
	int		size;
	Random	rand;
	//
	public	RandomPermutation(int _size){
		size		= _size;
		permMap 	= new int[size];
		//
		for(int j=0; j<size; j++){
			permMap[j] = 0;	// 選択地図 0=空き,1=使用済み
		}
		rand	= new Random();
	}
	//
	public void setRandom(Random r)	{ rand = r; }
	//
	// ランダムに選んだ次の空き番号を返す(１オリジン、１～size までの整数）
	public int	nextNumber(){
		int			start	= rand.nextInt(size);	// 探査開始点を得る( 0 ～ mapSize-1 ）
		boolean 	end 	= false;				// 完了を示すフラグ
		int			num		= -1;					// 次の空き番号を入れる
		int			pos;
		//
		// 大きい方へ向かって探索
		for(pos=start; pos<size; pos++){
			if(permMap[pos]==0){		// 未使用があればこれを消費して１をセットしておく
				permMap[pos]	= 1;
				end 			= true;		// 完了
				num				= pos;
				break;
			}
		}
		if(!end){
			// 小さい方へ向かって探索
			for(pos=start-1; pos>=0; pos--){
				if(permMap[pos] == 0){		// 未使用があればこれを消費して１をセットしておく
					permMap[pos]	= 1;
					end 			= true;	// 完了
					num				= pos;
					break;
				}
			}
		}
		return num + 1;
	}
}