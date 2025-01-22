//ExSample4_03.java ブロードキャストレシーバに関するサンプル


package es.exsample;

import android.content.*;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample4_03 extends AppCompatActivity {
	TextView tv;
	SampleBroadcastReceiver sb;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		tv = new TextView(this);		
		ll.addView(tv);
		sb = new SampleBroadcastReceiver();  //リスナー登録するブロードキャストレシーバクラスオブジェクトの生成
	}
	
	protected void onResume(){
		super.onResume();
		IntentFilter itf = new IntentFilter();  //インテントフィルタの生成
		itf.addAction(Intent.ACTION_BATTERY_CHANGED);  //インテントフィルタにバッテリー量変化のアクションを設定
		registerReceiver(sb, itf);  //アクションを検知した時のリスナー登録
	}
	
	protected void onPause(){
		super.onPause();
		unregisterReceiver(sb);  //ポーズ時にリスナー登録を外す
	}
	
	class SampleBroadcastReceiver extends BroadcastReceiver{
		public void onReceive(Context cn, Intent it){  //インテントフィルタでアクションを検知した時の処理
			String str = it.getAction();
			if(str.equals(Intent.ACTION_BATTERY_CHANGED)){ //バッテリー量が変化した際の処理
				float level = it.getIntExtra("level", 0);  //バッテリーの残量
				float scale = it.getIntExtra("scale", 1);  //バッテリーのフルスケール
				float b = level/scale;
				tv.setText("バッテリ" + b*100 + "％");
			}
		}
	}
}