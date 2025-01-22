//ExSample3_05.java OpenGLESを用いたCGに関するサンプル
package es.exsample;

import android.os.*;
import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_05 extends AppCompatActivity {

	private GLSurfaceView glView;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		glView=new GLSurfaceView(this);  //OpenGLES用のビューの生成
		glView.setRenderer(new SampleGLRenderer());  //レンダリング用ソースの設定
		setContentView(glView);  //OpenGLES用のビューをアクティビティに設定
	}

	public void onResume() {
		super.onResume();
		glView.onResume();
	}

	public void onPause() {
		super.onPause();
		glView.onPause();
	}
}