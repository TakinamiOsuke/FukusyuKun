package es.exsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class FukusyuKun extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // アプリ起動時は home.xml を表示

        // 線形代数ボタンを取得
        Button btnLinearAlgebra = findViewById(R.id.btn_linear_algebra);
        btnLinearAlgebra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // liner_algebra.xml に遷移
                Intent intent = new Intent(FukusyuKun.this, LinearAlgebraActivity.class);
                startActivity(intent);
            }
        });

        // 微分積分ボタンを取得
        Button btnCalculus = findViewById(R.id.btn_calculus);
        btnCalculus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calculus.xml に遷移
                Intent intent = new Intent(FukusyuKun.this, CalculusActivity.class);
                startActivity(intent);
            }
        });
    }
}
