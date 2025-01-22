//ExSample2_10.java ファイルIOに関するサンプル
package es.exsample;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class ExSample2_10 extends AppCompatActivity {
	
	Button[] bt = new Button[2];
	EditText et;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		for(int i=0; i<bt.length; i++)
			bt[i] = new Button(this);
		bt[0].setText("読込");
		bt[1].setText("保存");
		
		et = new EditText(this);
		et.setLines(3);  //エディットテキストの行数の設定
		ll.addView(et);
		
		for(int i=0; i<bt.length; i++){
			ll.addView(bt[i]);
			bt[i].setOnClickListener(new SampleClickListener());
		}
	}
	
	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			if(v == bt[0]){  //「読込」ボタンクリック時の処理
				try{
					FileInputStream fis = openFileInput("Sample.txt");  //ファイル名「Sample.txt」を入力ストリームとして開く
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));  //入力ストリームをバッファードリーダにつなげる
					StringBuffer sb = new StringBuffer();  //読み込んだ文字をためるためのバッファーの生成
					String str;
					while((str = br.readLine()) != null)  //ファイルを1行づつ読み込み
						sb.append(str);  //読み込んだ分をバッファ上でつなぎ合わせる
					et.setText(sb);  //エディットテキストに出力
				}
				catch(Exception e){}
			}
			else if(v == bt[1]){  //「保存」ボタンクリック時の処理
				try{
					FileOutputStream fos = openFileOutput("Sample.txt", Context.MODE_PRIVATE);  //ファイル名「Sample.txt」を出力ストリーム
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));  //出力ストリームをバッファードライターに繋げる
					bw.write(et.getText().toString());  //エディットテキストの内容を文字列に変換後バッファードライターに書き込み
					bw.flush();  //ファイルに書き込み
					fos.close();  //ファイルのクローズ
				}
				catch(Exception e){}
			}
		}
	}
}