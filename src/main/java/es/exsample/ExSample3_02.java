//ExSample3_02.java 複数のアニメーションに関するサンプル
package es.exsample;

import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_02 extends AppCompatActivity {

	ImageView[] iv = new ImageView[3];
	Bitmap bmp;
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setGravity(Gravity.CENTER);
		setContentView(ll);
		
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.dri);
		
		for(int i=0; i<iv.length; i++){
			iv[i] = new ImageView(this);
			iv[i].setImageBitmap(bmp);
			ll.addView(iv[i]);
			iv[i].setOnClickListener(new SampleClickListener());
		}
	}
	
	class SampleClickListener implements OnClickListener{
		public void onClick(View v){	
			ImageView tmp = (ImageView) v;
			AnimationSet anim = new AnimationSet(true);  //複数のアニメーションを設定するためのオブジェクト生成
			anim.addAnimation(new RotateAnimation(0, 180, tmp.getWidth()/2, tmp.getHeight()/2));  //回転アニメーションを追加
			//anim.addAnimation(new ScaleAnimation(1,5,1,5,tmp.getWidth()/2, tmp.getHeight()/2));  //スケール変換アニメーションを追加
			//anim.addAnimation(new TranslateAnimation(0.0f, 100.0f, 0.0f, 0.0f));  //平行移動アニメーションを追加
			anim.setDuration(1000);  //アニメーションのスピード（動作時間）の設定
			tmp.startAnimation(anim);  //アニメーションのスタート
		}
	}
}