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
 * 拡大表示画面 (微分積分)。
 * - 画質向上のため、Base64→Bitmapで maxSize を大きく (例: 4000)
 * - 他の機能 (編集/削除) は従来どおり
 */
public class CExpansionActivity extends AppCompatActivity {

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "拡大表示";

    private ImageView imgExpanded;
    private TextView tvSpinnerTitle;
    private TextView tvComment;
    private TextView tvExpansionTitle;
    private Button btnClose, btnEdit, btnDelete;

    private List<CalculusActivity.CalItem> itemList = new ArrayList<>();
    private CalculusActivity.CalItem currentItem;

    // 画質向上
    private static final int MAX_EXPANSION_SIZE = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_expansion);

        tvExpansionTitle = findViewById(R.id.tv_expansion_title);
        imgExpanded = findViewById(R.id.img_expanded);
        tvSpinnerTitle = findViewById(R.id.tv_spinner_title);
        tvComment = findViewById(R.id.tv_comment);

        btnClose = findViewById(R.id.btn_close);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

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

        // 高解像度でデコード
        if (currentItem != null) {
            Bitmap bigBmp = decodeBase64ToBitmap(currentItem.base64Image, MAX_EXPANSION_SIZE);
            if (bigBmp != null) {
                imgExpanded.setImageBitmap(bigBmp);
            }
            tvSpinnerTitle.setText(currentItem.spinnerText);
            tvComment.setText(currentItem.editText);
        }

        btnClose.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> {
            if (currentItem == null) return;
            Intent intent = new Intent(this, CEditActivity.class);
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
                .setPositiveButton("はい", (dialog, which) -> deleteCurrentItem())
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
    private List<CalculusActivity.CalItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<CalculusActivity.CalItem> result = new ArrayList<>();
        if (serialized.isEmpty()) {
            return result;
        }
        String[] chunks = serialized.split(ITEM_DELIMITER);
        for (String c : chunks) {
            if (c.trim().isEmpty()) {
                continue;
            }
            String[] fields = c.split(FIELD_DELIMITER);
            if (fields.length < 3) {
                continue;
            }
            result.add(new CalculusActivity.CalItem(fields[0], fields[1], fields[2]));
        }
        return result;
    }

    private void saveItemListToPrefs(List<CalculusActivity.CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalculusActivity.CalItem it : list) {
            sb.append(it.base64Image)
                    .append(FIELD_DELIMITER)
                    .append(it.spinnerText)
                    .append(FIELD_DELIMITER)
                    .append(it.editText)
                    .append(ITEM_DELIMITER);
        }
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEM_LIST, sb.toString())
                .apply();
    }

    // ========================
    // 画像デコード (高解像度)
    // ========================
    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap rawBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (rawBmp == null) return null;

            int w = rawBmp.getWidth();
            int h = rawBmp.getHeight();
            if (w > maxSize || h > maxSize) {
                float ratio = (float) w / (float) h;
                if (ratio > 1f) {
                    w = maxSize;
                    h = (int)(maxSize / ratio);
                } else {
                    h = maxSize;
                    w = (int)(maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(rawBmp, w, h, true);
            } else {
                return rawBmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
