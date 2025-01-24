package es.exsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

/**
 * 追加項目をタップした際に拡大表示するアクティビティ
 * レイアウト: la_expansion.xml
 * ScrollViewにより画像・テキストをスクロールで参照可能
 */
public class LAExpansionActivity extends AppCompatActivity {

    private ImageView imgExpanded;
    private TextView tvSpinnerTitle;
    private TextView tvComment;
    private TextView tvExpansionTitle;

    private Button btnClose;
    private Button btnEdit;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.la_expansion);

        // View の紐付け
        tvExpansionTitle = findViewById(R.id.tv_expansion_title);
        imgExpanded = findViewById(R.id.img_expanded);
        tvSpinnerTitle = findViewById(R.id.tv_spinner_title);
        tvComment = findViewById(R.id.tv_comment);

        btnClose = findViewById(R.id.btn_close);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        // Intent からデータを受け取る
        Intent intent = getIntent();
        String imageUriStr = intent.getStringExtra("IMAGE_URI");
        String spinnerText = intent.getStringExtra("SPINNER_TEXT");
        String editTextContent = intent.getStringExtra("EDIT_TEXT");
        // タイトル（例: "線形代数"）
        String activityTitle = intent.getStringExtra("ACTIVITY_TITLE");

        if (activityTitle != null) {
            tvExpansionTitle.setText(activityTitle);
        }

        // 画像を表示 (リサイズ)
        if (imageUriStr != null) {
            Uri uri = Uri.parse(imageUriStr);
            Bitmap scaledBitmap = getScaledBitmapFromUri(uri, 1200);
            if (scaledBitmap != null) {
                imgExpanded.setImageBitmap(scaledBitmap);
            }
        }

        // Spinnerテキスト・コメントテキストを表示
        tvSpinnerTitle.setText(spinnerText);
        tvComment.setText(editTextContent);

        // ボタンのリスナー設定
        btnClose.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            // TODO: 編集機能の実装
            Toast.makeText(this, "編集ボタン（未実装）", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            // TODO: 削除機能の実装
            Toast.makeText(this, "削除ボタン（未実装）", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Uri からアスペクト比を保ちつつ最大 maxSize に収まる Bitmap を生成するユーティリティ
     * LinearAlgebraActivity と同様のコード
     */
    private Bitmap getScaledBitmapFromUri(Uri uri, int maxSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            InputStream inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            int inSampleSize = 1;
            while ((originalWidth / inSampleSize) > maxSize || (originalHeight / inSampleSize) > maxSize) {
                inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            inputStream = getContentResolver().openInputStream(uri);
            Bitmap sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (sampledBitmap == null) {
                return null;
            }

            int scaledWidth = sampledBitmap.getWidth();
            int scaledHeight = sampledBitmap.getHeight();
            float ratio = (float) scaledWidth / (float) scaledHeight;

            if (scaledWidth > maxSize || scaledHeight > maxSize) {
                if (ratio > 1f) {
                    // 横長
                    scaledWidth = maxSize;
                    scaledHeight = (int) (maxSize / ratio);
                } else {
                    // 縦長
                    scaledHeight = maxSize;
                    scaledWidth = (int) (maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(sampledBitmap, scaledWidth, scaledHeight, true);
            } else {
                return sampledBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
