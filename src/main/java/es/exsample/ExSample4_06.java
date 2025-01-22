//ExSample4_06.java 明示的なインテントを利用したTODOリストに関するサンプル

package es.exsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;

public class ExSample4_06 extends AppCompatActivity {

	public static int SAMPLE_APP = 1;

	ListView lv;
	Button bt1, bt2;
	ArrayList<HashMap<String, String>> data;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		lv = new ListView(this);
		bt1 = new Button(this);
		bt2 = new Button(this);
		bt1.setText("新規");
		bt2.setText("削除");

		data = new ArrayList<HashMap<String, String>>();  //TODOデータの保存先

		String[] str = {"Title", "Todo"};  //タイトルとTODOデータを渡す際のテキストID
		int[] ids = {android.R.id.text1, android.R.id.text2};  //タイトルとTODOデータのID

		SimpleAdapter ad = new SimpleAdapter(this, data, android.R.layout.simple_list_item_single_choice, str,ids);  //リストビューに渡すアダプタ
		lv.setAdapter(ad); //アダプタをリストビューに設定
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);  //リストビューに選択ボタンを設定

		ll.addView(bt1);
		ll.addView(bt2);
		ll.addView(lv);

		lv.setOnItemClickListener(new SampleItemClickListener());
		bt1.setOnClickListener(new SampleClickListener());
		bt2.setOnClickListener(new SampleClickListener());
	}

	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			if(v == bt1){
				Intent it = new Intent(getApplicationContext(), ExSampleInput.class);  //明示的なインテントで起動するアクティビティを設定
				int pos = data.size();  //リストの最後の番号
				String title = "タイトル";
				String todo = "TODO";
				it.putExtra("Title", title);  //インテント先にデータを渡す
				it.putExtra("Todo", todo);
				it.putExtra("Pos", pos);
				startActivityForResult(it, SAMPLE_APP);
			}
			else if(v == bt2){
				int pos = lv.getCheckedItemPosition();  //選択が行われたチェックボックスの場所
				if(pos >= 0){
					data.remove(pos);  //選択されたデータの削除
					SimpleAdapter ad = (SimpleAdapter) lv.getAdapter();  //削除されたデータの更新
					ad.notifyDataSetChanged();
					lv.setItemChecked(-1, true);
				}
			}
		}
	}

	class SampleItemClickListener implements OnItemClickListener{
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id){
			Intent it = new Intent(getApplicationContext(), ExSampleInput.class); //TODOリストが選択された際に明示的インテントでアクティビティを起動
			HashMap<String, String> hm = data.get(pos);  //選択された場所のハッシュマップデータを取得
			String title = hm.get("Title");  //登録されているタイトルデータの取得
			String todo = hm.get("Todo");  //登録されているTODOデータを取得
			it.putExtra("Title", title);  //インテント先に送るデータの設定
			it.putExtra("Todo", todo);
			it.putExtra("Pos", pos);
			startActivityForResult(it, SAMPLE_APP);
		}
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		super.onActivityResult(reqcode, result, it);
		if (reqcode == SAMPLE_APP && result == RESULT_OK) {
			String title = it.getStringExtra("Title");  //インテント先からのデータの受け取り
			String todo = it.getStringExtra("Todo");
			int pos = it.getIntExtra("Pos", 0);
			HashMap<String, String> hm = new HashMap<String, String>(); //ハッシュマップを生成してデータを登録
			hm.put("Title", title);
			hm.put("Todo", todo);

			if (pos == data.size())  //リストにTODOデータを追加
				data.add(pos, hm);
			else
				data.set(pos, hm);

			SimpleAdapter ad = (SimpleAdapter) lv.getAdapter();
			ad.notifyDataSetChanged();
		}
	}
}