//ExSample2_01.java 基本的なリニアレイアウトとビューのサンプル
package es.exsample;

import android.os.*;
import android.app.*;
import android.widget.*;

public class ExSample2_01 extends Activity { //Activityクラスを拡張したクラスを作成

        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);  //スーパークラスのonCreate()の呼び出し

                LinearLayout ll =new LinearLayout(this);   // リニアレイアウトの生成
                ll.setOrientation(LinearLayout.VERTICAL);  // リニアレイアウトの方向の設定
                //ll.setOrientation(LinearLayout.HORIZONTAL);

                setContentView(ll);  //リニアレイアウトをアクティビティに設定

                TextView tv = new TextView(this);  //テキストビューの生成
                tv.setTextSize(20);  //テキストビューのフォントサイズの設定
                tv.setText("金沢へようこそ！");  //テキストビューのテキスト内容の設定

                Button bt = new Button(this);  //ボタンの生成
                bt.setText("検索"); //ボタンのテキストの設定

                ll.addView(tv);  //テキストビューをリニアレイアウトに設定
                ll.addView(bt);  //ボタンをリニアレイアウトに設定
        
/*        Button bt1[] = new Button[5];  //ボタンを5つ準備
        
        for(int i=0; i<bt1.length; i++){
        	bt1[i] = new Button(this);  //ボタンを生成
        	bt1[i].setText("観光地 " + Integer.toString(i+1)); //ボタンのテキストを設定
        	ll.addView(bt1[i]); //ボタンをリニアレイアウトに設定
        }
*/
        }

}
