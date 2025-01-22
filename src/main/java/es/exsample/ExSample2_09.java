//ExSample2_09.java メニューとダイアログに関するサンプル
package es.exsample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_09 extends AppCompatActivity {
	
	TextView tv;
	String str = null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		TableLayout tl =new TableLayout(this);
		setContentView(tl);

		tv = new TextView(this);
		tv.setText("金沢へようこそ！");
		tl.addView(tv);        
	}
	
	public boolean onCreateOptionsMenu(Menu menu){  //メニューの項目の設定
		menu.add(Menu.NONE, 0, 0, "兼六園");
		menu.add(Menu.NONE, 1, 1, "21世紀美術館"); 
		menu.add(Menu.NONE, 2, 2, "近江町市場");
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem mi){  //メニュー選択時の処理
		AlertDialog.Builder db = new AlertDialog.Builder(ExSample2_09.this); //ダイアログの生成
		switch(mi.getItemId()){  //メニュー選択時の具体的な処理
			case 0:
				str = "兼六園";
				break;
			case 1:
				str = "21世紀美術館";
				break;
			case 2:
				str = "近江町市場";
				break;
		}
		
		db.setTitle("観光地の確認");  //ダイアログのタイトル設定
		db.setMessage("本当に" + str + "にしますか？");  //ダイアログのメッセージ設定
		db.setPositiveButton("はい", new SampleDialogClickListener());  //ダイアログの「Yes」ボタンとクリック時のリスナーの設定
		db.setNegativeButton("いいえ", null).show();  //ダイアログの「No」ボタンの設定と、ダイアログの表示
		return true;
	}
		
	class SampleDialogClickListener implements DialogInterface.OnClickListener{  //ダイアログのボタンクリック時のイベント処理
		public void onClick(DialogInterface d, int w){
			new AlertDialog.Builder(ExSample2_09.this).setTitle("観光地の決定")  //ダイアログのタイトル、メッセージ、「Yes」ボタンの設定とダイアログの表示
                .setMessage(str+"にしました。").setPositiveButton("OK", null).show();

			tv.setText(str + "を選択しました。");
		} 
	}
}