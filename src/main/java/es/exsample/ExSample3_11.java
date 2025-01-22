//ExSample3_11.java 加速度センサに関するサンプル
package es.exsample;

import android.content.*;
import android.os.*;
import android.widget.*;
import android.hardware.*;
import androidx.appcompat.app.AppCompatActivity;

public class ExSample3_11 extends AppCompatActivity {
	TextView tv[] = new TextView[3];
	SensorManager sm;
	Sensor s;
	SampleSensorEventListener sse;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		for(int i=0; i<tv.length; i++){
			tv[i] = new TextView(this);
			ll.addView(tv[i]);
		}

		sse = new SampleSensorEventListener();  //センサデータ取得時のイベント処理用オブジェクトの生成
	}

	public void onResume(){
		super.onResume();
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);  //センサマネジャーをシステム側から取得
		s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  //センサを加速度センサに設定
		sm.registerListener(sse, s, SensorManager.SENSOR_DELAY_NORMAL);  //センサのリスナー登録とセンサの感度を設定
	}

	public void onPause(){
		super.onPause();
		sm.unregisterListener(sse);  //ポーズ時にセンサを開放
	}

	class SampleSensorEventListener implements SensorEventListener{
		public void onSensorChanged(SensorEvent e){  //センサの値が変化したときの処理
			if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER){  //変化のあったセンサが加速度センサの場合
				tv[0].setText("X軸："+Float.toString(e.values[0]));
				tv[1].setText("Y軸："+Float.toString(e.values[1]));
				tv[2].setText("Z軸："+Float.toString(e.values[2]));
			}
		}
		public void onAccuracyChanged(Sensor s, int accuracy){}
	}
}