//ExSample3_15.java メール送信に関するサンプル
package es.exsample;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_15 extends AppCompatActivity {

    Button bt;
    EditText[] et = new EditText[3];

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);

        Button bt = new Button(this);
        for(int i=0; i<et.length; i++)
            et[i] = new EditText(this);

        et[0].setText("宛先");
        et[1].setText("タイトル");
        et[2].setText("本文");
        bt.setText("送信");

        for(int i=0; i<et.length; i++)
            ll.addView(et[i]);
        ll.addView(bt);

        bt.setOnClickListener(new SampleClickListener());
    }

    class SampleClickListener implements OnClickListener{
        public void onClick(View v){
            Intent it = new Intent();  //インテントの作成
            it.setAction(Intent.ACTION_SENDTO);  //アクションの設定
            it.setData(Uri.parse("mailto:" + et[0].getText().toString()));  //メールアドレスの設定
            it.putExtra(Intent.EXTRA_SUBJECT, et[1].getText().toString());  //サブジェクトの設定
            it.putExtra(Intent.EXTRA_TEXT, et[2].getText().toString());  //本文の設定
            startActivity(it);  //インテントのスタート
        }
    }
}