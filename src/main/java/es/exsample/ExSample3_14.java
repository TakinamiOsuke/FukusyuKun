//ExSample3_14.java ヘルパークラスを利用したデータベース利用に関するサンプル

package es.exsample;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_14 extends AppCompatActivity {

	private Button bt1,bt2;
	private EditText et;
	private TextView tv;
	private SampleDBHelper dbhelper;
	private String str;
	private int num = 0;
	private static SQLiteDatabase db;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		bt1 = new Button(this);
		bt1.setText("追加");
		bt1.setOnClickListener(new ExSampleClickListener());
		bt2 = new Button(this);
		bt2.setText("削除");
		bt2.setOnClickListener(new ExSampleClickListener());

		ll.addView(bt1);
		ll.addView(bt2);

		et = new EditText(this);
		tv = new TextView(this);
		tv.setText("データベース一覧\n");

		ll.addView(et);
		ll.addView(tv);

		dbhelper = new SampleDBHelper(this);  //データベースヘルパークラスの生成
		db = dbhelper.getWritableDatabase();  //データベースの作成 or オープン
		db.delete("sampletable", null, null);  //データベースのリセット
	}

	class ExSampleClickListener implements OnClickListener {
		public void onClick(View v) {
			ContentValues values = new ContentValues();  //データベースに入力するデータを保存するためのオブジェクトの生成
			if(v == bt1) {
				values.put("sampletext", et.getText().toString());  //エディットテキストからデータベースに入力する値を取得
				db.insert("sampletable", null, values);  //データベースに値を挿入
				num++;  //リストのナンバーをインクリメント
			}
			else if(v == bt2) {
				String[] args = {String.valueOf(num)};  //リストの最後の番号を取得
				db.delete("sampletable", "_id = ?", args);  //リストの最後の値を削除
				num--;  //リストのナンバーをデクリメント
			}

			Cursor cr = db.query("sampletable", new String[]{"_id", "sampletext"}, null, null, null, null, null);  //クエリ結果をカーソルで取得

			str = "データベース一覧\n";
			while (cr.moveToNext()) {  //カーソルを一つづつ動かしてデータ取得
				str += cr.getString(cr.getColumnIndexOrThrow("_id")) + ":" +
						cr.getString(cr.getColumnIndexOrThrow("sampletext")) + "\n";
			}
			tv.setText(str);
		}
	}
}