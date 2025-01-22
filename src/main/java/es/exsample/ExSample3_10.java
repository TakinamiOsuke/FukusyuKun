//ExSample3_10.java テキスト読み上げに関するサンプル
package es.exsample;

import java.util.*;
import android.os.*;
import android.speech.tts.*;
import android.speech.tts.TextToSpeech.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_10 extends AppCompatActivity {

	EditText et;
	Button bt;
	TextToSpeech tts;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		et = new EditText(this);
		bt = new Button(this);
		bt.setText("開始");

		ll.addView(et);
		ll.addView(bt);

		tts = new TextToSpeech(getApplicationContext(), new SampleInitListener());  //テキストトゥスピーチオブジェクトの生成と、イベントリスナーの登録
		tts.setLanguage(Locale.ENGLISH);  //読み上げる言語の設定

		bt.setOnClickListener(new SampleClickListener());
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			String str  = et.getText().toString();
			String utteranceId = this.hashCode() + "";  //utteranceIdの取得
			if(str != null)
				tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, utteranceId);  //テキストの読み上げ
		}
	}

	class SampleInitListener implements OnInitListener{
		public void onInit(int status){}
	}
}