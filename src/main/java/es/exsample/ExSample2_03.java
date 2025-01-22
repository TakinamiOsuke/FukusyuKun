//ExSample2_03.java 基本的なイベント処理のサンプル
package es.exsample;

import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample2_03 extends AppCompatActivity {
	
	TextView tv;
    Button bt;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        LinearLayout ll =new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);
       
        tv = new TextView(this);
        tv.setText("金沢へようこそ！");
        
        bt = new Button(this);
        bt.setText("検索");
        
        ll.addView(tv); 
        ll.addView(bt);
            	
        bt.setOnClickListener(new ExSampleClickListener());  //ボタンがクリックされるときのリスナー登録
        //bt.setOnTouchListener(new ExSampleTouchListener());  //ボタンがタッチされるときのリスナー登録
    }    

    class ExSampleClickListener implements OnClickListener{  //ボタンがクリックされたときのイベント処理
    	public void onClick(View v){
               tv.setText("兼六園がおすすめです！");
    	}  //ボタンがクリックされたときの具体的な処理
    }
    
/*    class ExSampleTouchListener implements OnTouchListener{  //ボタンがタッチされたときのイベント処理
    	public boolean onTouch(View v, MotionEvent e){
    		if(e.getAction() == MotionEvent.ACTION_DOWN){  //ボタンが押されているときの具体的な処理
    			tv.setText("検索中です...");
    		}
            else if(e.getAction() == MotionEvent.ACTION_UP){  //ボタンが上がった時の具体的な処理
            	tv.setText("21世紀美術館がおすすめです!");
            }
    		return true;
    	}
    }*/
}