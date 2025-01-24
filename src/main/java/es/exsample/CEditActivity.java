package es.exsample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Spinner と EditText のみ編集可能 (微分積分版)。
 * レイアウト: c_edit.xml
 */
public class CEditActivity extends AppCompatActivity {

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "編集中";

    private Button btnClose;
    private Button btnSave;
    private TextView tvTitle;
    private Spinner spinnerEdit;
    private EditText editTextEdit;

    private List<CalculusActivity.CalItem> itemList = new ArrayList<>();
    private CalculusActivity.CalItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_edit);

        // View 紐付け
        btnClose = findViewById(R.id.btn_close_edit);
        btnSave = findViewById(R.id.btn_save_edit);
        tvTitle = findViewById(R.id.tv_edit_title);
        spinnerEdit = findViewById(R.id.spinner_edit);
        editTextEdit = findViewById(R.id.et_edit_text);

        // Intentから受け取り
        itemIndex = getIntent().getIntExtra("INDEX", -1);
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");
        if (activityTitle == null) {
            activityTitle = "編集中";
        }
        tvTitle.setText(activityTitle + " - 編集中");

        // itemListをロードして currentItem を取得
        itemList = loadItemListFromPrefs();
        if (itemIndex >= 0 && itemIndex < itemList.size()) {
            currentItem = itemList.get(itemIndex);
        }

        // 現在のアイテム情報を反映 (Spinner と EditText)
        if (currentItem != null) {
            // Spinner の初期選択を currentItem.spinnerText に合わせる
            setSpinnerSelection(spinnerEdit, currentItem.spinnerText);
            editTextEdit.setText(currentItem.editText);
        }

        // 「✖」ボタン → 何も保存せずに戻る
        btnClose.setOnClickListener(v -> finishToCalculus());

        // 「保存」ボタン → Spinner & EditText の内容を更新して戻る
        btnSave.setOnClickListener(v -> {
            if (currentItem == null) {
                finishToCalculus();
                return;
            }
            String newSpinnerText = spinnerEdit.getSelectedItem().toString();
            String newEditText = editTextEdit.getText().toString().trim();

            // 更新
            currentItem.spinnerText = newSpinnerText;
            currentItem.editText = newEditText;

            // itemList の該当アイテムを書き換えて保存
            itemList.set(itemIndex, currentItem);
            saveItemListToPrefs(itemList);

            // CalculusActivity に戻る
            finishToCalculus();
        });
    }

    /**
     * Spinner で指定した文字列に該当する index を探し、setSelection する
     */
    private void setSpinnerSelection(Spinner spinner, String target) {
        SpinnerAdapter adapter = spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            String itemText = adapter.getItem(i).toString();
            if (itemText.equals(target)) {
                spinner.setSelection(i);
                return;
            }
        }
        // 該当がなければ先頭
        spinner.setSelection(0);
    }

    private void finishToCalculus() {
        // 直接 CalculusActivity に戻る
        Intent intent = new Intent(this, CalculusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ================================
    // SharedPreferences
    // ================================
    private List<CalculusActivity.CalItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<CalculusActivity.CalItem> result = new ArrayList<>();
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
            String base64 = fields[0];
            String spin = fields[1];
            String txt = fields[2];
            result.add(new CalculusActivity.CalItem(base64, spin, txt));
        }
        return result;
    }

    private void saveItemListToPrefs(List<CalculusActivity.CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalculusActivity.CalItem item : list) {
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
}
