package es.exsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 拡大表示画面。
 * - 大きい maxSize (例: 2000) で画像を復元 → 高解像度
 * - 編集→LAEditActivity、削除→AlertDialog
 */
public class LAExpansionActivity extends AppCompatActivity {

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "拡大表示";

    private ImageView imgExpanded;
    private TextView tvSpinnerTitle;
    private TextView tvComment;
    private TextView tvExpansionTitle;
    private Button btnClose;
    private Button btnEdit;
    private Button btnDelete;

    private List<LinearAlgebraActivity.LAItem> itemList = new ArrayList<>();
    private LinearAlgebraActivity.LAItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.la_expansion);

        tvExpansionTitle = findViewById(R.id.tv_expansion_title);
        imgExpanded = findViewById(R.id.img_expanded);
        tvSpinnerTitle = findViewById(R.id.tv_spinner_title);
        tvComment = findViewById(R.id.tv_comment);

        btnClose = findViewById(R.id.btn_close);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        // インテントから
        itemIndex = getIntent().getIntExtra("INDEX", -1);
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");
        if (activityTitle == null) {
            activityTitle = "拡大表示";
        }
        tvExpansionTitle.setText(activityTitle);

        // itemList読み込み
        itemList = loadItemListFromPrefs();
        if (itemIndex >= 0 && itemIndex < itemList.size()) {
            currentItem = itemList.get(itemIndex);
        }

        // 画像を大きいサイズで復元
        if (currentItem != null) {
            Bitmap bigBitmap = decodeBase64ToBitmap(currentItem.base64Image, 2000);
            if (bigBitmap != null) {
                imgExpanded.setImageBitmap(bigBitmap);
            }
            tvSpinnerTitle.setText(currentItem.spinnerText);
            tvComment.setText(currentItem.editText);
        }

        // ボタンリスナー
        btnClose.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (currentItem == null) return;
            // 編集画面へ
            Intent intent = new Intent(this, LAEditActivity.class);
            intent.putExtra("INDEX", itemIndex);
            intent.putExtra("ACTIVITY_TITLE", activityTitle);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (currentItem == null) return;
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("削除の確認")
                .setMessage("本当に削除しますか？")
                .setPositiveButton("はい", (dialog, which) -> {
                    deleteCurrentItem();
                })
                .setNegativeButton("いいえ", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteCurrentItem() {
        if (itemIndex < 0 || itemIndex >= itemList.size()) {
            return;
        }
        itemList.remove(itemIndex);
        saveItemListToPrefs(itemList);
        finish();
    }

    // ========================
    // SharedPreferences
    // ========================
    private List<LinearAlgebraActivity.LAItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<LinearAlgebraActivity.LAItem> result = new ArrayList<>();
        if (serialized.isEmpty()) {
            return result;
        }
        String[] itemChunks = serialized.split(ITEM_DELIMITER);
        for (String chunk : itemChunks) {
            if (chunk.trim().isEmpty()) {
                continue;
            }
            String[] fields = chunk.split(FIELD_DELIMITER);
            if (fields.length < 3) {
                continue;
            }
            result.add(new LinearAlgebraActivity.LAItem(fields[0], fields[1], fields[2]));
        }
        return result;
    }

    private void saveItemListToPrefs(List<LinearAlgebraActivity.LAItem> list) {
        StringBuilder sb = new StringBuilder();
        for (LinearAlgebraActivity.LAItem item : list) {
            sb.append(item.base64Image)
                    .append(FIELD_DELIMITER)
                    .append(item.spinnerText)
                    .append(FIELD_DELIMITER)
                    .append(item.editText)
                    .append(ITEM_DELIMITER);
        }
        String serialized = sb.toString();

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEM_LIST, serialized).apply();
    }

    // ========================
    // 画像デコード
    // ========================
    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (rawBitmap == null) return null;

            int width = rawBitmap.getWidth();
            int height = rawBitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float ratio = (float) width / (float) height;
                if (ratio > 1f) {
                    width = maxSize;
                    height = (int)(maxSize / ratio);
                } else {
                    height = maxSize;
                    width = (int)(maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(rawBitmap, width, height, true);
            } else {
                return rawBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
