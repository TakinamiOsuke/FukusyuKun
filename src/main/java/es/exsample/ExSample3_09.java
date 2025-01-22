//ExSample3_09.java インテントを用いたカメラ利用のサンプル
package es.exsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_09 extends AppCompatActivity {

	ImageView iv;
	public static int SAMPLE_APP = 1;  //呼び出し元のインテントのID

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		iv = new ImageView(this);
		Button bt = new Button(this);
		bt.setText("カメラ起動");
		ll.addView(iv);
		ll.addView(bt);

		bt.setOnClickListener(new ExSampleClickListener());
	}

	ActivityResultLauncher<Intent> imagepickup = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				public void onActivityResult(ActivityResult result) {
					if (result.getResultCode() == RESULT_OK) {
						Intent data = result.getData();
						if (data != null) {
							Bitmap bmp = (Bitmap) data.getExtras().get("data");  //インテント先からのデータの受け取り
							iv.setImageBitmap(bmp);
						}
					}
				}
			});

	class ExSampleClickListener implements OnClickListener {
		public void onClick(View v) {
			Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //画像取得のためのインテントの設定
			Intent chooserit = Intent.createChooser(it, "画像取得");
			imagepickup.launch(chooserit);
		}
	}

	/*
    class ExSampleClickListener implements OnClickListener {
		public void onClick(View v) {
			Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //画像取得のためのインテントの設定
			startActivityForResult(it, SAMPLE_APP);  //インテントの開始
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SAMPLE_APP) {
			Bitmap bmp = (Bitmap) data.getExtras().get("data");  //インテント先からのデータの受け取り
			iv.setImageBitmap(bmp);
		}
	}*/
}