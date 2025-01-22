//ExSample2_07.java リストビュー、スピナー、スクロールビューに関するサンプル
package es.exsample;

import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_07 extends AppCompatActivity
{
	TextView tv;
	ListView lv1, lv2;
	Spinner sp;

	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		tv = new TextView(this);
		tv.setText("金沢へようこそ!");
		lv1 = new ListView(this);  //リストビュー1の生成
		lv2 = new ListView(this);  //リストビュー2の生成
		sp = new Spinner(this);  //スピナーの生成

		String[] str ={"兼六園","21世紀美術館","近江町市場",
				"東茶屋街","武家屋敷","忍者寺"};  //リストビュー、スピナーに格納するテキストデータ

		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);  //テキストをリストビュー、スピナーに渡すためのアレイアダプター
		lv1.setAdapter(ad);  //リストビュー1にアレイアダプターを登録
		lv2.setAdapter(ad);  //リストビュー2にアレイアダプターを登録

		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  //アレイアダプターにドロップダウンリストを設定
		sp.setAdapter(ad);  //スピナーにアレイアダプターを登録

		ScrollView sv = new ScrollView(this);  //スクロールビューの生成
		sv.addView(lv2);  //スクロールビューにリストビューを設定

		ll.addView(tv);
		ll.addView(lv1);  //リニアレイアウトにリストビューを設定
		ll.addView(sp);  //リニアレイアウトにスピナーを設定
		ll.addView(sv);  //リニアレイアウトにスピナーを設定

		lv1.setOnItemClickListener(new ExSampleItemClickListener());  //リストビュー1のアイテムクリック時のリスナー登録
		lv2.setOnItemClickListener(new ExSampleItemClickListener());  //リストビュー2のアイテムクリック時のリスナー登録
		sp.setOnItemSelectedListener(new ExSampleItemSelectedListener());  //スピナーのアイテム選択時のリスナー登録
	}

	class ExSampleItemClickListener implements OnItemClickListener{  //リストビューのアイテムクリック時のイベント処理
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id){
			TextView tmp = (TextView) iv;
			tv.setText(tmp.getText() + "を表示します。");
		}
	}

	class ExSampleItemSelectedListener implements OnItemSelectedListener{  //スピナーのアイテムクリック時のイベント処理
		public void onItemSelected(AdapterView<?>v, View iv, int pos, long id){
			TextView tmp = (TextView) iv;
			tv.setText(tmp.getText() + "を表示します。");
		}
		public void onNothingSelected(AdapterView<?> arg0){}
	}
}


