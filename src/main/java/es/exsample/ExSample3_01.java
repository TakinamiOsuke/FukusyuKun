//ExSample3_01.java アニメーションに関するサンプル
package es.exsample;

import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_01 extends AppCompatActivity {

	ImageView iv;  //画像を提示するためのイメージビュー
	Bitmap bmp;  //画像を扱うためのビットマップクラスオブジェクト
	Matrix m;  //アフィン変換を行うための行列クラス
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this); 
		ll.setOrientation(LinearLayout.HORIZONTAL);
		//ll.setGravity(Gravity.CENTER);  //リニアレイアウトのグラビティ（重力）を中心に
		setContentView(ll);
		
		iv = new ImageView(this); //イメージビューの生成
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.dri);  //画像の読み込み、データは「drawable」フォルダに入れておく
		
		iv.setImageBitmap(bmp); //読み込んだ画像をイメージビューに設定
		m = new Matrix(); //アフィン変換を行う行列の生成
		ll.addView(iv);  //イメージビューをリニアレイアウトに追加
		iv.setOnClickListener(new SampleClickListener());  //イメージビュークリック時のリスナー登録
	}
	
	class SampleClickListener implements OnClickListener{
		public void onClick(View v){
			m.postRotate(90);  //行列を回転に設定
			//m.postScale(0.5f, 1.2f);  //行列をスケール変換に設定
			//m.postTranslate(2.0f, 5.0f);  //行列を平行移動に設定
			//m.postSkew(1.2f, 1.5f);  //行列をせん断に設定
			
			Bitmap tmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);  //ビットマップオブジェクトにアフィン変換を適用
			iv.setImageBitmap(tmp);  //イメージビューにビットマップデータを再読み込み
		}
	}
}