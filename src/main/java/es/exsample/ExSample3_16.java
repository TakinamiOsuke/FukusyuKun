//ExSample3_16.java Webサイト閲覧に関するサンプル

package es.exsample;

import android.os.*;
import android.view.*;
import android.view.View.*;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_16 extends AppCompatActivity {

    Button[] bt = new Button[5];
    WebView wv;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        TableLayout tl = new TableLayout(this);
        setContentView(tl);

        wv = new WebView(this);  //Webサイト出力のためのビューの生成
        wv.setWebViewClient(new WebViewClient());  //Webビュークライアントの設定
        wv.loadUrl("http://www.kanazawa-it.ac.jp");  //URLの設定

        TableRow tr = new TableRow(this);

        for(int i=0; i<bt.length; i++){
            bt[i] = new Button(this);
            tr.addView(bt[i]);
        }

        bt[0].setText("←");  //戻るボタンの設定
        bt[1].setText("→");  //進むボタンの設定
        bt[2].setText("+");  //ズームインボタンの設定
        bt[3].setText("-");  //ズームアウトボタンの設定
        bt[4].setText("R");  //再度読み込みボタンの設定

        for(int i=0; i<bt.length; i++)
            bt[i].setOnClickListener(new SampleClickListener());

        tl.addView(tr);
        tl.addView(wv);
    }

    class SampleClickListener implements OnClickListener{
        public void onClick(View v){
            if(v == bt[0]){  //戻るボタンの処理
                if(wv.canGoBack())
                    wv.goBack();
            }
            else if(v == bt[1]){  //進むボタンの処理
                if(wv.canGoForward())
                    wv.goForward();
            }
            else if(v == bt[2])
                wv.zoomIn();  //ズームイン
            else if(v == bt[3])
                wv.zoomOut();  //ズームアウト
            else if(v == bt[4])
                wv.reload();  //再度読み込み
        }
    }
}