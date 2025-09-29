package _epml;

//ノート種別
public enum NoteType {
	NUM,	// 問題（大問）番号
	ENL,	// 番号選択問題
	EWL,	// 文選択問題
	ETF,	// 単語記述問題
	ERB,	// ボタン選択問題
	ETA,	// 文章、コード記述問題
	TEXT,	// 一般テキスト
	
	EPT,	// 配点 
	ADMY,	// ダミー選択肢
	ASEL,	// 選択語表示
	TIME,	// 制限時間
}