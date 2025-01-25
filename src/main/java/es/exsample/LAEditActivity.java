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
 * 線形代数: 編集画面
 * - ImageButton +「画像選択」テキストを重ねる
 * - 選択後にテキストを隠し、高解像度(1200)で取り込み
 */
public class LAEditActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_GALLERY = 101;

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "編集中";

    private Button btnClose, btnSave;
    private ImageButton ibEditImage;       // ImageButton
    private TextView tvEditImageHint;      // 「画像選択」テキスト
    private TextView tvTitle;
    private Spinner spinnerEdit;
    private EditText editTextEdit;

    private Bitmap editBitmap = null; // 選択された画像
    private boolean hasNewImage = false;

    private List<LinearAlgebraActivity.LAItem> itemList = new ArrayList<>();
    private LinearAlgebraActivity.LAItem currentItem;

    private List<String> algebraTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.la_edit);

        btnClose = findViewById(R.id.btn_close_edit);
        btnSave = findViewById(R.id.btn_save_edit);
        ibEditImage = findViewById(R.id.ib_edit_image);
        tvEditImageHint = findViewById(R.id.tv_edit_image_hint);

        tvTitle = findViewById(R.id.tv_edit_title);
        spinnerEdit = findViewById(R.id.spinner_edit);
        editTextEdit = findViewById(R.id.et_edit_text);

        itemIndex = getIntent().getIntExtra("INDEX", -1);
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");
        if (activityTitle == null) activityTitle = "編集中";
        tvTitle.setText(activityTitle + " - 編集中");

        itemList = loadItemListFromPrefs();
        if (itemIndex >= 0 && itemIndex < itemList.size()) {
            currentItem = itemList.get(itemIndex);
        }

        // Spinner
        String[] arr = getResources().getStringArray(R.array.linear_algebra_menu);
        algebraTitles = new ArrayList<>();
        algebraTitles.add("タイトル選択");
        for (String s : arr) {
            algebraTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algebraTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdit.setAdapter(spAdapter);

        if (currentItem != null) {
            setSpinnerSelection(spinnerEdit, currentItem.spinnerText);
            editTextEdit.setText(currentItem.editText);

            // 既存画像を解像度高め(1200)で復元
            Bitmap existing = decodeBase64ToBitmap(currentItem.base64Image, 1200);
            if (existing != null) {
                editBitmap = existing;
                ibEditImage.setImageBitmap(editBitmap);
                tvEditImageHint.setVisibility(View.GONE);
            } else {
                // 画像なし状態
                ibEditImage.setImageResource(android.R.drawable.ic_menu_gallery);
                tvEditImageHint.setVisibility(View.VISIBLE);
            }
        }

        // ImageButton -> ギャラリー
        ibEditImage.setOnClickListener(v -> openGalleryForEdit());

        btnClose.setOnClickListener(v -> finishToLinearAlgebra());
        btnSave.setOnClickListener(v -> {
            if (currentItem == null) {
                finishToLinearAlgebra();
                return;
            }
            String spinnerVal = getSpinnerVal();
            String newText = editTextEdit.getText().toString().trim();
            if (spinnerVal.equals("タイトル選択")) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            currentItem.spinnerText = spinnerVal;
            currentItem.editText = newText;

            // 画像を更新
            if (hasNewImage && editBitmap != null) {
                String b64 = encodeBitmapToBase64(editBitmap);
                if (b64 != null) {
                    currentItem.base64Image = b64;
                }
            }
            itemList.set(itemIndex, currentItem);
            saveItemListToPrefs(itemList);
            finishToLinearAlgebra();
        });
    }

    private void openGalleryForEdit() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_EDIT_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            // 画質向上(1200)
            Bitmap newBmp = decodeUriToBitmap(uri, 1200);
            if (newBmp != null) {
                editBitmap = newBmp;
                hasNewImage = true;
                // ImageButton にセット & テキスト非表示
                ibEditImage.setImageBitmap(editBitmap);
                tvEditImageHint.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "画像取得に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getSpinnerVal() {
        int pos = spinnerEdit.getSelectedItemPosition();
        if (pos == 0) return "タイトル選択";
        return spinnerEdit.getItemAtPosition(pos).toString();
    }

    private void setSpinnerSelection(Spinner sp, String val) {
        for (int i = 0; i < sp.getCount(); i++) {
            if (sp.getItemAtPosition(i).equals(val)) {
                sp.setSelection(i);
                return;
            }
        }
        sp.setSelection(0);
    }

    private void finishToLinearAlgebra() {
        Intent intent = new Intent(this, LinearAlgebraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    //=================== SharedPreferences ===================
    private List<LinearAlgebraActivity.LAItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String data = prefs.getString(KEY_ITEM_LIST, "");
        List<LinearAlgebraActivity.LAItem> result = new ArrayList<>();
        if (data.isEmpty()) return result;

        String[] items = data.split(ITEM_DELIMITER);
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

    //=================== 画像処理 ===================
    private Bitmap decodeUriToBitmap(Uri uri, int maxSize) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream in = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in, null, opts);
            in.close();

            int w = opts.outWidth;
            int h = opts.outHeight;
            int inSampleSize = 1;
            while (w / inSampleSize > maxSize || h / inSampleSize > maxSize) {
                inSampleSize *= 2;
            }
            opts.inSampleSize = inSampleSize;
            opts.inJustDecodeBounds = false;

            in = getContentResolver().openInputStream(uri);
            Bitmap sampled = BitmapFactory.decodeStream(in, null, opts);
            in.close();
            if (sampled == null) return null;

            return scaleBitmap(sampled, maxSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float ratio = (float) w / (float) h;
        if (ratio > 1f) {
            w = maxSize;
            h = (int)(maxSize / ratio);
        } else {
            h = maxSize;
            w = (int)(maxSize * ratio);
        }
        return Bitmap.createScaledBitmap(src, w, h, true);
    }

    private String encodeBitmapToBase64(Bitmap bmp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (raw == null) return null;
            return scaleBitmap(raw, maxSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
