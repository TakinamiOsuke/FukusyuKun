//ExSample3_17.java Mapに関するサンプル
package es.exsample;

import android.os.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;

public class ExSample3_17 extends AppCompatActivity{

    SupportMapFragment mf;  //サポートマップフラグメントの宣言

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mf = SupportMapFragment.newInstance();  //サポートマップフラグメントの生成
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, mf)
                .commit(); //フラグメントマネージャーの取得、フラグメントトランザクションの開始、マップフラグメントをコンテナに追加、フラグメントの実行
    }
}