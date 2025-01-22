//ExSample4_04.java コンテントプロバイダに関するサンプル

package es.exsample;

import java.io.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample4_04 extends AppCompatActivity {
	public static int SAMPLE_APP = 1;
	Button bt;
	ImageView iv;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		bt = new Button(this);
		iv = new ImageView(this);
		ll.addView(iv);
		ll.addView(bt);
		
		bt.setOnClickListener(new SampleClickListener());
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			Intent it = new Intent();
			it.setType("image/*");  //インテントで扱うデータタイプ（MIME）を設定
			it.setAction(Intent.ACTION_GET_CONTENT);  //アクションを設定
			startActivityForResult(it, SAMPLE_APP);
		}
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		super.onActivityResult(reqcode, result, it);
		if (reqcode == SAMPLE_APP && result == RESULT_OK) {
			Uri u = it.getData();  //データのURIを取得
			try {
				InputStream is = getContentResolver().openInputStream(u); //URIに基づいて入力ストリームを取得
				Bitmap bmp = BitmapFactory.decodeStream(is);  //入力ストリームからデータをビットマップオブジェクトに渡す
				iv.setImageBitmap(bmp);  //ビットマップオブジェクトをイメージビューに設定
			} catch (Exception e) {
			}
		}
	}
}