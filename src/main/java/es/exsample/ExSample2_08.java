//ExSample2_08.java グリッドビューとシークバーに関するサンプル
package es.exsample;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ExSample2_08 extends Activity
{
	TextView tv;
	GridView gv;
	SeekBar sb;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll =new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		tv = new TextView(this);
		tv.setText("金沢へようこそ!");

		gv = new GridView(this);  //グリッドビューの生成
		String[] str ={"兼六園","21世紀美術館","近江町市場",
				"東茶屋街","武家屋敷","忍者寺"}; //グリッドビューに格納するテキストデータ

		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str); //テキストをグリッドビューに渡すためのアレイアダプター

		gv.setAdapter(ad);  //グリッドビューにアレイアダプターを登録
		gv.setNumColumns(3);  //グリッドビューのカラムを３に設定

		sb = new SeekBar(this);  //シークバーの生成

		ll.addView(tv);
		ll.addView(gv);  //リニアレイアウトにグリッドビューを設定
		ll.addView(sb);  //リニアレイアウトにシークバーを設定

		gv.setOnItemClickListener(new SampleItemClickListener());  //グリッドビューのアイテムクリック時のリスナー登録
		sb.setOnSeekBarChangeListener(new SampleSeekBarChangeListener());  //シークバー操作時のリスナー登録
	}

	class SampleItemClickListener implements OnItemClickListener{  //グリッドビューのアイテムクリック時のイベント処理
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id){
			TextView tmp = (TextView) iv;
			tv.setText(tmp.getText() + "ですね。");
		}
	}

	class SampleSeekBarChangeListener implements OnSeekBarChangeListener{  //シークバー操作時のイベント処理
		public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser){
			tv.setText("観光に利用できる時間:" + progress + "分"); // ツマミ操作時の処理
		}
		public void onStartTrackingTouch(SeekBar seekBar) {
			// ツマミに触れたときの処理
		}
		public void onStopTrackingTouch(SeekBar seekBar) {
			// ツマミから手を離した時の処理
		}
	}
}