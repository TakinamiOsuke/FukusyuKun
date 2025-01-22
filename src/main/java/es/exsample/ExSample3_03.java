//ExSample3_03.java ビューフリッパーに関するサンプル
package es.exsample;

import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_03 extends AppCompatActivity {
	ViewFlipper vf;
	TextView[] tv = new TextView[3];
	LinearLayout[] ll = new LinearLayout[3];
	float x;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout llp = new LinearLayout(this);  //親リニアレイアウト
		setContentView(llp);

		vf = new ViewFlipper(this);  //ビューフリッパーオブジェクトの生成

		for(int i=0; i<tv.length;i++){
			ll[i] = new LinearLayout(this); //ビューフリッパー各画面用の子リニアレイアウト
			tv[i] = new TextView(this);
			tv[i].setText("これは" + (i+1) + "ページ目です");
			tv[i].setTextSize(20);
			ll[i].addView(tv[i]);  //子リニアレイアウトにテキストビューを追加
			vf.addView(ll[i]);  //ビューフリッパーに子リニアレイアウトを追加
		}

		llp.addView(vf);  //ビューフリッパーを親リニアレイアウトに追加
		llp.setOnTouchListener(new SampleTouchListener());  //親リニアレイアウトタッチ時のリスナー登録
	}

	class SampleTouchListener implements OnTouchListener{  //フリップ操作時のアニメーション設定
		public boolean onTouch(View v, MotionEvent e){
			if(e.getAction() == MotionEvent.ACTION_DOWN){  //タッチ時にx座標値を取得
				x = e.getX();
			}
			else if(e.getAction() == MotionEvent.ACTION_UP){  //タッチ終了時の処理
				if(x-20 > e.getX()){  //右にフリップ時
					TranslateAnimation inanim = new TranslateAnimation(tv[0].getWidth(), 0, 0, 0); //入ってくるビューのアニメーション
					inanim.setDuration(200);  //アニメーションの動作スピード（時間）の設定
					TranslateAnimation outanim = new TranslateAnimation(0,-tv[0].getWidth(), 0, 0);  //出ていくビューのアニメーション
					outanim.setDuration(200);  //アニメーションの動作スピード（時間）の設定
					vf.setInAnimation(inanim);  //入ってくるアニメーションの設定
					vf.setOutAnimation(outanim);  //出ていくアニメーションの設定
					vf.showNext();  //アニメーションのスタート
				}
				else if(x+20 < e.getX()){  //左にフリップ時
					TranslateAnimation inanim = new TranslateAnimation(-tv[0].getWidth(), 0, 0, 0);
					inanim.setDuration(200);
					TranslateAnimation outanim = new TranslateAnimation(0,tv[0].getWidth(), 0, 0);
					outanim.setDuration(200);
					vf.setInAnimation(inanim);
					vf.setOutAnimation(outanim);
					vf.showPrevious();
				}
			}
			return true;
		}
	}
}