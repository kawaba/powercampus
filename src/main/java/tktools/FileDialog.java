/*
 * 
 */
package tktools;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
/**
 * ファイルダイアログを表示して、選択された File オブジェクトを返す
 * ＜用法＞
 * 　　FileDialog	fd	= new FileDialog("csv","CSVファイル","d:\\");
 * 　　if(fd!=null)	String filepath	= fd.getPath();
 * 
 */
public class FileDialog {
	
	public	FileDialog(){
			
	}
	/**
	 * ファイルダイアログを表示して、選択された File オブジェクトを返す
	 * @param filter	java, csv など拡張子のパターン
	 * @param disp		「javaファイル」など、filter の説明語句
	 * @param dir		初期表示のディレクトリ
	 * @return			選択されたファイルの File オブジェクト.失敗するとnullを返す
	 */
	public	static	File	show(String filter, String disp, String dir){
		ExtensionFileFilter fl = new ExtensionFileFilter(filter,disp);
		JFileChooser 		ch = new JFileChooser();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(ch);
		}
		catch(Exception e) {
		}		
		ch.setFileFilter(fl);
		ch.setCurrentDirectory(new File(dir));
		int returnVal = ch.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {// CANCEL_OPTION, ERROR_OPTION
			return	ch.getSelectedFile();		
		}else{
			return	null;		
		}
	}
	
	    public static void main(String[] args) {
	        // ファイルダイアログを表示してCSVファイルを選択する
	        FileDialog fileDialog = new FileDialog();
	        File selectedFile = fileDialog.show("csv", "CSVファイル", "C:\\"); // ここに初期表示ディレクトリを設定

	        // 選択されたファイルがnullでない場合は、ファイルのパスを表示
	        if (selectedFile != null) {
	            System.out.println("選択されたファイルのパス: " + selectedFile.getAbsolutePath());
	        } else {
	            System.out.println("ファイルが選択されませんでした。");
	        }
	    }

}
