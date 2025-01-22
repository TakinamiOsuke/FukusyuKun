//ExSample4_07.java サービスとノーティフィケーションに関するサンプル

package es.exsample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ExSample4_07 extends AppCompatActivity {

    Button bt1, bt2;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);

        bt1 = new Button(this);
        bt2 = new Button(this);
        bt1.setText("開始");
        bt2.setText("停止");

        ll.addView(bt1);
        ll.addView(bt2);
        bt1.setOnClickListener(new SampleClickListener());
        bt2.setOnClickListener(new SampleClickListener());
    }

    class SampleClickListener implements OnClickListener{
        public void onClick(View v){
            Context ct = getApplicationContext();  //現在のコンテキストを取得
            Intent it = new Intent(ct, ExSampleService.class);  //明示的インテントで起動するサービスの設定
            PendingIntent pi = PendingIntent.getService(ct, 0, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);  //ペンディングインテントを取得
            AlarmManager am = (AlarmManager)ct.getSystemService(Context.ALARM_SERVICE);  //アラームマネージャをシステムから取得

            if(v == bt1){
                long time = Calendar.getInstance().getTimeInMillis();  //現在時刻を取得
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, 5*1000, pi);  //5秒おきにペンディングインテントを起動
            }
            else if(v == bt2){
                am.cancel(pi);  //アラームの解除
                stopService(it);  //サービスの停止
            }
        }
    }
}