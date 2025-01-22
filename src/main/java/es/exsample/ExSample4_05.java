//ExSample4_05.java 2つのアクティビティを明示的なインテントで実現するサンプル

package es.exsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample4_05 extends AppCompatActivity {

	public static int SAMPLE_APP = 1;
	TextView tv1, tv2;
	Button bt;
	String text1,text2;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		tv1 = new TextView(this);
		tv2 = new TextView(this);
		bt = new Button(this);
		bt.setText("入力");

		text1 = "abc";
		text2 = "def";

		tv1.setText(text1);
		tv2.setText(text2);

		ll.addView(tv1);
		ll.addView(tv2);
		ll.addView(bt);

		bt.setOnClickListener(new SampleClickListener());
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			Intent it = new Intent(getApplicationContext(), ExSampleSub1.class);  //明示的なインテントで起動する他のアクティビティを設定
			it.putExtra("Text1", text1);  //インテント先にデータを渡す
			it.putExtra("Text2", text2);
			startActivityForResult(it, SAMPLE_APP); //インテントの起動
		}
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		super.onActivityResult(reqcode, result, it);
		if (reqcode == SAMPLE_APP && result == RESULT_OK) {
			text1 = it.getStringExtra("Text1");  //インテント先からデータを受け取る
			text2 = it.getStringExtra("Text2");
			tv1.setText(text1);
			tv2.setText(text2);
		}
	}
}