//ExSample3_13.java データベースに関するサンプル
package es.exsample;

import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_13 extends AppCompatActivity {

	ListView lv;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		setContentView(ll);

		lv = new ListView(this);
		String str = "data/data/" + getPackageName() + "/Sample.db";  //データベースの保存先の指定
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(str, null);  //データベースオブジェクトの生成

		String qry0 = "DROP TABLE IF EXISTS site"; //テーブルリセットのクエリ
		String qry1 = "CREATE TABLE site" + "(id INTEGER PRIMARY KEY, name STRING, time INTEGER)";  //テーブル作成のクエリ
		String[] qry2 ={"INSERT INTO site(name, time) VALUES ('兼六園', 10)",  //データ挿入のクエリ
				"INSERT INTO site(name, time) VALUES ('21世紀美術館', 5)",
				"INSERT INTO site(name, time) VALUES ('近江町市場', 20)",
				"INSERT INTO site(name, time) VALUES ('東茶屋街', 30)"};

		String qry3 = "SELECT * FROM site";  //データ選択のクエリ
		//String qry3 = "SELECT * FROM site WHERE time <= 10";
		//String qry3 = "SELECT * FROM site WHERE name = '兼六園'";
		//String qry3 = "SELECT * FROM site ORDER BY time DESC";

		db.execSQL(qry0);  //クエリの実行 2回目以降の実行する際にはコメントアウトする
		db.execSQL(qry1);  //2回目以降の実行する際にはコメントアウトする
		for(int i=0; i<qry2.length; i++)  //2回目以降の実行する際にはコメントアウトする
			db.execSQL(qry2[i]);

		Cursor cr = db.rawQuery(qry3, null);  //クエリ結果をカーソルで受け取り
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);  //結果出力のリスト

		while(cr.moveToNext()){  //カーソルを一つづつ動かしデータを取得
			int i = cr.getColumnIndex("id");  //データをテーブルの要素ごとに取得
			int n = cr.getColumnIndex("name");
			int p = cr.getColumnIndex("time");
			int id = cr.getInt(i);
			String name = cr.getString(n);
			int time = cr.getInt(p);
			String row = id + "------" + name + "～" + time + "分";
			ad.add(row);
		}

		lv.setAdapter(ad);
		ll.addView(lv);
		db.close();  //データベースのクローズ
	}
}