//ExSample2_06.java チェックボックス、ラジオボタン、トグルボタンに関するサンプル

package es.exsample;

import android.os.*;
import android.util.Log;
import android.widget.*;
import android.widget.CompoundButton.*;
import android.view.*;
import android.view.View.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_06 extends AppCompatActivity
{
	TextView tv;
	CheckBox cb1, cb2;
	RadioButton rb[] = new RadioButton[4];
	RadioGroup rg;
	ToggleButton tb;

	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		tv = new TextView(this);
		tv.setText("金沢へようこそ!");

		cb1 = new CheckBox(this);  //チェックボックスの生成
		cb2 = new CheckBox(this);
		cb1.setText("兼六園");  //チェックボックスのテキストの設定
		cb2.setText("21世紀美術館");

		for(int i=0; i<rb.length; i++)  //ラジオボタンの各要素の生成
			rb[i] = new RadioButton(this);

		rb[0].setText("近江町市場");  //ラジオボタンのテキストの設定
		rb[1].setText("東茶屋街");
		rb[2].setText("武家屋敷");
		rb[3].setText("忍者寺");

		rg = new RadioGroup(this);  //ラジオグループの生成
		for(int i=0; i<rb.length; i++)  //ラジオグループにラジオボタンを登録
			rg.addView(rb[i]);

		rb[0].setChecked(true);

		tb = new ToggleButton(this);  //トグルボタンの生成
		tb.setTextOn("観光地を非表示にします");  //トグルボタンオン時のボタン表示
		tb.setTextOff("観光地を表示します");  //トグルボタンオフ時のボタン表示

		ll.addView(tv);
		ll.addView(cb1); //リニアレイアウトへチェックボックスの登録
		ll.addView(cb2);
		ll.addView(rg);  //リニアレイアウトへラジオグループの登録
		ll.addView(tb);  //リニアレイアウトへのトグルボタンの登録

		cb1.setOnCheckedChangeListener(new ExSampleCheckedChangeListener());  //チェックボックス選択時のリスナー登録
		cb2.setOnCheckedChangeListener(new ExSampleCheckedChangeListener());

		for(int i=0; i<rb.length; i++)
			rb[i].setOnClickListener(new ExSampleClickListener());  //ラジオボタンクリック時のリスナー登録

		tb.setOnCheckedChangeListener(new ExSampleCheckedChangeListener());  //トグルボタン押下時のリスナー登録
	}

	class ExSampleCheckedChangeListener implements OnCheckedChangeListener{  //チェックボックス選択時、トグルボタン押下時のイベント処理
		public void onCheckedChanged(CompoundButton cb, boolean isChecked){
			if(cb == cb1 || cb == cb2) {  //チェックボックス選択時
				if (isChecked == true) {  //選択が行われたとき
					tv.setText(cb.getText() + "を選びました。");
					Log.d("選択された項目", cb.getText().toString());  //選択された内容のログへの出力
				}
				else if (isChecked == false) {  //選択が外されたとき
					tv.setText(cb.getText() + "をはずしました。");
					Log.d("選択がはずされた項目", cb.getText().toString());  //選択がはずされた内容のログへの出力
				}
			} else if (cb == tb) {  //トグルボタン押下時
				if (isChecked == true)  //ボタンがOnのとき
					tv.setText("観光地が表示されました。");
				else if (isChecked == false)  //ボタンがOffのとき
					tv.setText("観光地が非表示になりました。");
			}
		}
	}

	class ExSampleClickListener implements OnClickListener{
		public void onClick(View v){
			RadioButton tmp = (RadioButton) v;
			tv.setText(tmp.getText() + "を選びました。");
		}
	}
}

