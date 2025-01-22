//ExSample3_04.java ビューフリッパーを用いた複数画面アプリケーションに関するサンプル
package es.exsample;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_04 extends AppCompatActivity {
    static final int num = 100;
    ViewFlipper vf;
    TextView tv1,tv2;
    Button bt1,bt2;
    ListView lv;    
    LinearLayout[] ll = new LinearLayout[3];
    float x;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        LinearLayout llp = new LinearLayout(this);
        setContentView(llp);

        vf = new ViewFlipper(this);

        tv1 = new TextView(this);
        tv1.setText("金沢へようこそ！");

        tv2 = new TextView(this);
        tv2.setText("左にフリップしてください");

        bt1 = new Button(this);
        bt1.setText("観光地リストへ");

        bt2 = new Button(this);
        bt2.setText("トップページへ");

        String[] str ={"兼六園","21世紀美術館","近江町市場",
                "東茶屋街","武家屋敷","忍者寺"};

        lv = new ListView(this);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
        lv.setAdapter(ad);

        ll[0] = new LinearLayout(this);
        ll[0].addView(tv1);
        ll[0].addView(tv2);
        vf.addView(ll[0]);

        ll[1] = new LinearLayout(this);
        ll[1].addView(bt1);
        ll[1].addView(bt2);
        vf.addView(ll[1]);

        ll[2] = new LinearLayout(this);
        ll[2].addView(lv);
        vf.addView(ll[2]);

        llp.addView(vf);
        setContentView(llp);
        llp.setOnTouchListener(new SampleTouchListener());

        bt1.setOnClickListener(new ExSampleClickListener());  //ボタンを押した際のフリップアニメーションの設定
        bt2.setOnClickListener(new ExSampleClickListener());
    }

    class SampleTouchListener implements OnTouchListener{
        public boolean onTouch(View v, MotionEvent e){
            if(e.getAction() == MotionEvent.ACTION_DOWN){
                x = e.getX();
            }
            else if(e.getAction() == MotionEvent.ACTION_UP){
                if(x-20 > e.getX()){
                    TranslateAnimation inanim = new TranslateAnimation(tv1.getWidth(), 0, 0, 0);
                    inanim.setDuration(500);
                    TranslateAnimation outanim = new TranslateAnimation(0,-tv1.getWidth(), 0, 0);
                    outanim.setDuration(500);
                    vf.setInAnimation(inanim);
                    vf.setOutAnimation(outanim);
                    vf.showNext();
                }
                else if(x+20 < e.getX()){
                    TranslateAnimation inanim = new TranslateAnimation(-tv1.getWidth(), 0, 0, 0);
                    inanim.setDuration(500);
                    TranslateAnimation outanim = new TranslateAnimation(0,tv1.getWidth(), 0, 0);
                    outanim.setDuration(500);
                    vf.setInAnimation(inanim);
                    vf.setOutAnimation(outanim);
                    vf.showPrevious();
                }
            }
            return true;
        }
    }

    class ExSampleClickListener implements View.OnClickListener { //ボタンを押した際のフリップアニメーション処理
        public void onClick(View v){
            if(v==bt1) {
                TranslateAnimation inanim = new TranslateAnimation(tv1.getWidth(), 0, 0, 0);
                inanim.setDuration(500);
                TranslateAnimation outanim = new TranslateAnimation(0, -tv1.getWidth(), 0, 0);
                outanim.setDuration(500);
                vf.setInAnimation(inanim);
                vf.setOutAnimation(outanim);
                vf.showNext();
            }else if(v==bt2) {
                TranslateAnimation inanim = new TranslateAnimation(-tv1.getWidth(), 0, 0, 0);
                inanim.setDuration(500);
                TranslateAnimation outanim = new TranslateAnimation(0,tv1.getWidth(), 0, 0);
                outanim.setDuration(500);
                vf.setInAnimation(inanim);
                vf.setOutAnimation(outanim);
                vf.showPrevious();
            }
        }
    }
}