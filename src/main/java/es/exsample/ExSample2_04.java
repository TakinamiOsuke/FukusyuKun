//ExSample2_04.java 画面とキーボード操作に関するサンプル
package es.exsample;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_04 extends AppCompatActivity {

	TextView tv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		tv = new TextView(this);
		tv.setText("金沢へようこそ！");

		ll.addView(tv);
	}

	public boolean onTouchEvent(MotionEvent e){  //画面がタッチされているときの処理
		if(e.getAction() == MotionEvent.ACTION_DOWN){
			tv.setText("検索を開始しますか？");
		}
		else if(e.getAction() == MotionEvent.ACTION_UP){
			tv.setText("近江町市場がおすすめです！");
		}
		return true;
	}

	public boolean onKeyDown(int keycode, KeyEvent e){  //キーボードが押されたときの処理
		String str;
		switch(keycode){
			case KeyEvent.KEYCODE_1:
				str = "兼六園を探します"; break;
			case KeyEvent.KEYCODE_2:
				str = "ひがし茶屋街を探します"; break;
			case KeyEvent.KEYCODE_3:
				str = "武家屋敷を探します"; break;
			default:
				str = "1-3を押してください";
		}
		tv.setText(str);
		return true;
	}
}