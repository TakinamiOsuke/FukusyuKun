//ExSample2_11.java ＸＭＬを用いたリニアレイアウトに関するサンプル
package es.exsample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_11 extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llexsample);  //リニアレイアウトのXMLファイルをアクティビティに設定
        //setContentView(R.layout.activity_tlexsample);  //テーブルレイアウトのXMLファイルをアクティビティに設定
        //setContentView(R.layout.activity_clexsample);  //コンストレイントレイアウトのXMLファイルをアクティビティに設定
        //setContentView(R.layout.activity_rlexsample);  //レラティブレイアウトのXMLファイルをアクティビティに設定

    }
}