//ExSample3_18.java Mapの様々な設定に関するサンプル

package es.exsample;

import android.app.*;
import android.content.pm.PackageManager;
import android.os.*;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;

public class ExSample3_18 extends AppCompatActivity implements OnMapReadyCallback{

    SupportMapFragment mf;
    private final int REQUEST_PERMISSION_1 = 1;
    private final int REQUEST_PERMISSION_2 = 2;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mf = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, mf)
                .commit();
        mf.getMapAsync(this);  //マップフラグメントにコールバックを設定
    }

    @Override
    public void onMapReady(GoogleMap map) {
        UiSettings us;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ExSample3_18.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_1);
            ActivityCompat.requestPermissions(ExSample3_18.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_2);
            return;
        } //Dangerous Permission の許可

        us = map.getUiSettings(); //マップのユーザインタフェースに関するオブジェクトの取得
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);  //マップを地図と航空写真のハイブリット表示に設定
        //map.setTrafficEnabled(true);  //渋滞情報を提示
        //map.setMyLocationEnabled(true);  //現在位置ボタンを提示
        //us.setCompassEnabled(true);  //コンパスの利用を有効化
        //us.setScrollGesturesEnabled(true);  //スクロールジェスチャーの有効化
        //us.setZoomGesturesEnabled(true);  //ズームジェスチャーの有効化
    }
}