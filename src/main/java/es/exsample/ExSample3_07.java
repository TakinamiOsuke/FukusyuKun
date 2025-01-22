//ExSample3_07.java 音声の録音に関するサンプル
package es.exsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.media.*;
import android.media.MediaPlayer.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.*;

public class ExSample3_07 extends AppCompatActivity {
	Button[] bt = new Button[3];
	MediaPlayer mp;
	MediaRecorder mr;
	File dir, f;
	private final int REQUEST_PERMISSION = 1;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		for(int i=0; i<bt.length; i++)
			bt[i] = new Button(this);

		bt[0].setText("開始");
		bt[1].setText("停止");
		bt[2].setText("再生");

		bt[0].setEnabled(true);  //各ボタンの有効化、無効化
		bt[1].setEnabled(false);
		bt[2].setEnabled(true);

		File dir = new File("data/data/" + getPackageName() + "/ExSample/");  //録音データの保存先の指定
		if(dir.exists() == false)
			dir.mkdir();

		f = new File(dir, "Sample.3gp");  //録音データのファイル名の決定

		for(int i=0; i<bt.length; i++){
			ll.addView(bt[i]);
			bt[i].setOnClickListener(new SampleClickListener());
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(ExSample3_07.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
		} //Dangerous Permission の許可
	}

	public void onResume(){
		super.onResume();
		mp = new MediaPlayer(); //メディアプレイヤーの生成
		mr = new MediaRecorder();  //メディアレコーダーの生成
		mp.setOnCompletionListener(new SampleCompletionListener());
	}

	public void onPause(){
		super.onPause();
		mp.release();  //メディアプレイヤーの開放
		mr.release();  //メディアレコーダーの開放
	}

	class SampleCompletionListener implements OnCompletionListener{
		public void onCompletion(MediaPlayer mp){  //各ボタンの有効化、無効化
			bt[0].setEnabled(true);
			bt[1].setEnabled(false);
			bt[2].setEnabled(true);
		}
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			if(v == bt[0]){
				bt[0].setEnabled(false);  //各ボタンの有効化、無効化
				bt[1].setEnabled(true);
				bt[2].setEnabled(false);

				try{
					mp.reset();  //メディアプレイヤーのリセット
					mr.setAudioSource(MediaRecorder.AudioSource.MIC);  //録音ソースの設定
					mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  //録音データのフォーマットの設定
					mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  //エンコーダの設定
					String path = f.getAbsolutePath();  //データを保存するファイルへのパス
					mr.setOutputFile(path);  //メディアレコーダへ保存パスの設定
					mr.prepare();  //メディアレコーダの準備
					mr.start();  //録音開始
				}
				catch(Exception e){}
			}
			else if(v == bt[1]){
				bt[0].setEnabled(true);  //各ボタンの有効化、無効化
				bt[1].setEnabled(false);
				bt[2].setEnabled(true);
				mr.stop();  //録音停止
				mr.reset();  //メディアレコーダのリセット
			}
			else if(v == bt[2]){
				bt[0].setEnabled(false);  //各ボタンの有効化、無効化
				bt[1].setEnabled(false);
				bt[2].setEnabled(false);
				try{
					String path = f.getAbsolutePath();  //再生ファイルのパスの生成
					mp.setDataSource(path);  //再生ファイルのパスの設定
					mp.prepare();  //メディアプレイヤーの準備
				}
				catch(Exception e){}
				mp.start();  //録音音声の再生
			}
		}
	}
}