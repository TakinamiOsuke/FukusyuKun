//ExSample4_01.java 暗黙的なインテントを使ったアプリケーションの連携に関するサンプル

package es.exsample;

import android.app.SearchManager;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample4_01 extends AppCompatActivity {
	
	Button bt;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		bt = new Button(this);
		bt.setText("電話");
		//bt.setText("検索");
		
		ll.addView(bt);
		bt.setOnClickListener(new SampleClickListener());
	}
	
	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			Intent it = new Intent();  //インテントの生成
			it.setAction(Intent.ACTION_VIEW);  //アクションの設定
			//it.setAction(Intent.ACTION_SEARCH);  //アクションの設定
			it.setData(Uri.parse("tel:0762747515"));  //データ（電話番号）の設定
			it.putExtra(SearchManager.QUERY, "Android");  //データ（検索ワード）の設定
			startActivity(it);  //インテントのスタート
		}
	}
}