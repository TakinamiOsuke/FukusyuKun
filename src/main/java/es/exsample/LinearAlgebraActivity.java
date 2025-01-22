package es.exsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LinearAlgebraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liner_algebra); // liner_algebra.xml を表示

        // 戻るボタンを取得
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // home.xml に戻る
                Intent intent = new Intent(LinearAlgebraActivity.this, FukusyuKun.class);
                startActivity(intent);
                finish(); // 現在のアクティビティを終了
            }
        });
    }
}
