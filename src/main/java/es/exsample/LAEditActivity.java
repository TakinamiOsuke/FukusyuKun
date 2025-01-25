package es.exsample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 線形代数: 編集画面 (Spinner + EditText + 画像変更対応)
 * レイアウト: la_edit.xml
 */
public class LAEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_EDIT = 2;

    // SharedPreferences
    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "編集中";

    private Button btnClose, btnSave;
    private TextView tvTitle;
    private Spinner spinnerEdit;
    private EditText editTextEdit;

    // 画像変更用
    private FrameLayout frameImageButton;
    private ImageButton imageButton;
    private TextView tvImageHint;
    private Uri selectedImageUri = null; // 新しく選んだ画像
    private boolean hasNewImage = false; // 新しい画像を選択したかどうか

    private List<LinearAlgebraActivity.LAItem> itemList = new ArrayList<>();
    private LinearAlgebraActivity.LAItem currentItem;

    // データ入力用 Spinner リスト
    private List<String> algebraTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.la_edit);

        btnClose = findViewById(R.id.btn_close_edit);
        btnSave = findViewById(R.id.btn_save_edit);
        tvTitle = findViewById(R.id.tv_edit_title);
        spinnerEdit = findViewById(R.id.spinner_edit);
        editTextEdit = findViewById(R.id.et_edit_text);

        // 画像関連
        frameImageButton = findViewById(R.id.spinner_edit).getParent() instanceof LinearLayout
                ? null : null; // ← 実際はレイアウト構造に合わせて取得（今回別に変数不要）
        imageButton = findViewById(R.id.edit_image_button);
        tvImageHint = findViewById(R.id.tv_edit_image_hint);

        // レイアウトに edit_image_button, tv_edit_image_hint を追加済み想定
        // (後述の la_edit.xml 参照)

        // インテント
        itemIndex = getIntent().getIntExtra("INDEX", -1);
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");
        if (activityTitle == null) {
            activityTitle = "編集中";
        }
        tvTitle.setText(activityTitle + " - 編集中");

        // itemList読み込み & currentItem確定
        itemList = loadItemListFromPrefs();
        if (itemIndex >= 0 && itemIndex < itemList.size()) {
            currentItem = itemList.get(itemIndex);
        }

        // Spinnerセット (index=0: 「タイトル選択」 + 既存メニュー)
        String[] fromRes = getResources().getStringArray(R.array.linear_algebra_menu);
        algebraTitles = new ArrayList<>();
        algebraTitles.add("タイトル選択");
        for (String s : fromRes) {
            algebraTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algebraTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdit.setAdapter(spAdapter);

        // 既存アイテムの表示
        if (currentItem != null) {
            // Spinner
            setSpinnerSelection(spinnerEdit, currentItem.spinnerText);
            // EditText
            editTextEdit.setText(currentItem.editText);

            // 画像
            Bitmap bmp = decodeBase64ToBitmap(currentItem.base64Image, 300);
            if (bmp != null) {
                imageButton.setImageBitmap(bmp);
                tvImageHint.setVisibility(View.GONE);
            } else {
                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
                tvImageHint.setVisibility(View.VISIBLE);
            }
        }

        // 画像タップでギャラリーから変更
        imageButton.setOnClickListener(v -> openGalleryForEdit());

        // 「✖」ボタン
        btnClose.setOnClickListener(v -> finishToLinearAlgebra());

        // 「保存」ボタン
        btnSave.setOnClickListener(v -> {
            if (currentItem == null) {
                finishToLinearAlgebra();
                return;
            }
            String newSpinnerText = getSelectedSpinnerTitle();
            String newEditText = editTextEdit.getText().toString().trim();

            // バリデーション (タイトルが「タイトル選択」ではないか, テキストが空でないか)
            // 画像変更は任意: ユーザーが変更しなければ currentItem.base64Image を使う
            if (newSpinnerText == null || newSpinnerText.equals("タイトル選択")) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newEditText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新
            currentItem.spinnerText = newSpinnerText;
            currentItem.editText = newEditText;

            // 画像変更があれば上書き
            if (hasNewImage && selectedImageUri != null) {
                String newBase64 = encodeImageToBase64(selectedImageUri);
                if (newBase64 != null) {
                    currentItem.base64Image = newBase64;
                }
            }

            itemList.set(itemIndex, currentItem);
            saveItemListToPrefs(itemList);

            finishToLinearAlgebra();
        });
    }

    // ===============================
    // ギャラリー (編集用)
    // ===============================
    private void openGalleryForEdit() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_EDIT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            hasNewImage = true;
            // サムネイル表示
            Bitmap scaled = getScaledBitmapFromUri(selectedImageUri, 300);
            if (scaled != null) {
                imageButton.setImageBitmap(scaled);
                tvImageHint.setVisibility(View.GONE);
            }
        }
    }

    // ===============================
    // Spinner ユーティリティ
    // ===============================
    private void setSpinnerSelection(Spinner spinner, String target) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(target)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private String getSelectedSpinnerTitle() {
        int pos = spinnerEdit.getSelectedItemPosition();
        if (pos == 0) return "タイトル選択";
        return spinnerEdit.getItemAtPosition(pos).toString();
    }

    private void finishToLinearAlgebra() {
        Intent intent = new Intent(this, LinearAlgebraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ===============================
    // SharedPreferences
    // ===============================
    private List<LinearAlgebraActivity.LAItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<LinearAlgebraActivity.LAItem> result = new ArrayList<>();
        if (serialized.isEmpty()) {
            return result;
        }
        String[] items = serialized.split(ITEM_DELIMITER);
        for (String chunk : items) {
            if (chunk.trim().isEmpty()) continue;
            String[] fields = chunk.split(FIELD_DELIMITER);
            if (fields.length < 3) continue;
            result.add(new LinearAlgebraActivity.LAItem(fields[0], fields[1], fields[2]));
        }
        return result;
    }

    private void saveItemListToPrefs(List<LinearAlgebraActivity.LAItem> list) {
        StringBuilder sb = new StringBuilder();
        for (LinearAlgebraActivity.LAItem it : list) {
            sb.append(it.base64Image).append(FIELD_DELIMITER)
                    .append(it.spinnerText).append(FIELD_DELIMITER)
                    .append(it.editText).append(ITEM_DELIMITER);
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEM_LIST, sb.toString()).apply();
    }

    // ===============================
    // 画像処理
    // ===============================
    private String encodeImageToBase64(Uri uri) {
        Bitmap bmp = getScaledBitmapFromUri(uri, 2000);
        if (bmp == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (raw == null) return null;
            int w = raw.getWidth(), h = raw.getHeight();
            if (w > maxSize || h > maxSize) {
                float ratio = (float) w / (float) h;
                if (ratio > 1f) {
                    w = maxSize;
                    h = (int)(maxSize / ratio);
                } else {
                    h = maxSize;
                    w = (int)(maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(raw, w, h, true);
            }
            return raw;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getScaledBitmapFromUri(Uri uri, int maxSize) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream in = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in, null, opts);
            in.close();

            int w = opts.outWidth;
            int h = opts.outHeight;
            int inSampleSize = 1;
            while ((w / inSampleSize) > maxSize || (h / inSampleSize) > maxSize) {
                inSampleSize *= 2;
            }
            opts.inSampleSize = inSampleSize;
            opts.inJustDecodeBounds = false;

            in = getContentResolver().openInputStream(uri);
            Bitmap sampled = BitmapFactory.decodeStream(in, null, opts);
            in.close();
            if (sampled == null) return null;

            w = sampled.getWidth();
            h = sampled.getHeight();
            float ratio = (float) w / (float) h;
            if (w > maxSize || h > maxSize) {
                if (ratio > 1f) {
                    w = maxSize;
                    h = (int)(maxSize / ratio);
                } else {
                    h = maxSize;
                    w = (int)(maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(sampled, w, h, true);
            }
            return sampled;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
