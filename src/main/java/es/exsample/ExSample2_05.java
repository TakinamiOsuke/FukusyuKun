//ExSample2_05.java テキストビューとイベント処理に関するサンプル
package es.exsample;

import android.graphics.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_05 extends AppCompatActivity
{
	TextView tv;
	Button bt;
	EditText et;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		tv = new TextView(this);
		bt = new Button(this);
		et = new EditText(this);  //エディットテキストの生成
		
		tv.setText("金沢へようこそ！");
		tv.setTextColor(Color.WHITE);  //テキストの色の設定
		tv.setBackgroundColor(Color.RED);  //テキスト ビューの背景色の設定
        tv.setTextSize(18);  //テキストサイズの設定
        
        bt.setText("行きたい場所を入力してください!");
        
        ll.addView(tv);
        ll.addView(bt);
        ll.addView(et);

        //匿名クラスによる記述法
        bt.setOnClickListener(
        		new OnClickListener() {
					public void onClick(View v) {
						tv.setText(et.getText() + "を検索しました。");
						bt.setEnabled(false);  //ボタンの無効化
					}
				}
		);

        /*ラムダ式を使った表現（メソッドが一つの時だけ使える）
        bt.setOnClickListener(v-> {
						tv.setText(et.getText() + "を検索しました。");
						bt.setEnabled(false);  //ボタンの無効化
					}
		);
        */
		//bt.setOnClickListener(new ExSampleClickListener());
	}
	
/*	class ExSampleClickListener implements OnClickListener{
		public void onClick(View v){			
			tv.setText(et.getText()+"を検索しました。");
			bt.setEnabled(false);  //ボタンの無効化
		}
	}
*/
}
