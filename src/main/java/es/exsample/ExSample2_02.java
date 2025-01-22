//ExSample2_02.java 基本的なテーブルレイアウトとビューのサンプル
package es.exsample;

import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_02 extends AppCompatActivity
{
   public void onCreate(Bundle savedInstanceState)
   {
          super.onCreate(savedInstanceState);
          TableLayout tl = new TableLayout(this);  //テーブルレイアウトの設定
          setContentView(tl); //アクティビティにテーブルレイアウトを設定
      
          TableRow[] tr = new TableRow[5];  //テーブルレイアウトの行の準備
          TextView[] tv = new TextView[5];
          Button[] bt = new Button[5];
     
          for(int i=0; i<tr.length; i++){
                       tr[i] = new TableRow(this);  //テーブルレイアウトの行の生成
                       tv[i] = new TextView(this);
                       tv[i].setText("観光地 "+ (i+1) + "******");
                       bt[i] = new Button(this);
                       bt[i].setText("見る");
                       tr[i].addView(tv[i]);  //行にテキストビューを追加
                       tr[i].addView(bt[i]);  //行にボタンを追加
                       tl.addView(tr[i]);  //テーブルレイアウトに行を追加
          }
   }

}
