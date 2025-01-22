//ExSample3_06.java 音声再生に関するサンプル
package es.exsample;

import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.media.*;
import android.media.MediaPlayer.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_06 extends AppCompatActivity {

	Button bt;
	MediaPlayer mp;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		bt = new Button(this);
		bt.setText("再生");
		bt.setEnabled(true);
		ll.addView(bt);
		bt.setOnClickListener(new SampleClickListener());
	}

	public void onResume(){
		super.onResume();
		mp = MediaPlayer.create(this, R.raw.ring);  //音声再生用メディアプレイヤーの生成、音声データは「raw」フォルダに格納
		mp.setOnCompletionListener(new SampleCompletionListener());  //再生終了時のリスナー登録
	}

	public void onPause(){
		super.onPause();
		mp.release();  //ポーズにメディアプレイヤーを開放
	}

	class SampleCompletionListener implements OnCompletionListener{  //再生終了時にボタンを有効化
		public void onCompletion(MediaPlayer mp){
			bt.setEnabled(true);
		}
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){  //ボタン押下時の処理
			if(v == bt){
				bt.setEnabled(false);  //ボタンの無効化
				try{
					mp.prepare();  //メディアプレイヤーの準備
				}
				catch(Exception e){}
				mp.start();  //メディアの再生
			}
		}
	}
}