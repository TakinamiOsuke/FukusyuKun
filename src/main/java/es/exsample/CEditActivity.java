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
 * 微分積分: 編集画面
 *  - ImageButton + hint
 *  - 高解像度(1200) で取り込み
 */
public class CEditActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_GALLERY = 101;

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "編集中";

    private Button btnClose, btnSave;
    private ImageButton ibEditImage;
    private TextView tvEditImageHint, tvTitle;
    private Spinner spinnerEdit;
    private EditText editTextEdit;

    private Bitmap editBitmap = null;
    private boolean hasNewImage = false;

    private List<CalculusActivity.CalItem> itemList = new ArrayList<>();
    private CalculusActivity.CalItem currentItem;

    private List<String> calcTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_edit);

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
        String[] arr = getResources().getStringArray(R.array.calculus_menu);
        calcTitles = new ArrayList<>();
        calcTitles.add("タイトル選択");
        for (String s : arr) {
            calcTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, calcTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdit.setAdapter(spAdapter);

        // 既存データ
        if (currentItem != null) {
            setSpinnerSelection(spinnerEdit, currentItem.spinnerText);
            editTextEdit.setText(currentItem.editText);

            // 既存画像(高解像度1200)
            Bitmap existing = decodeBase64ToBitmap(currentItem.base64Image, 1200);
            if (existing != null) {
                editBitmap = existing;
                ibEditImage.setImageBitmap(editBitmap);
                tvEditImageHint.setVisibility(View.GONE);
            } else {
                ibEditImage.setImageResource(android.R.drawable.ic_menu_gallery);
                tvEditImageHint.setVisibility(View.VISIBLE);
            }
        }

        ibEditImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_EDIT_GALLERY);
        });

        btnClose.setOnClickListener(v -> finishToCalculus());
        btnSave.setOnClickListener(v -> {
            if (currentItem == null) {
                finishToCalculus();
                return;
            }
            String spVal = getSpinnerVal();
            String newText = editTextEdit.getText().toString().trim();
            if (spVal.equals("タイトル選択")) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            currentItem.spinnerText = spVal;
            currentItem.editText = newText;

            if (hasNewImage && editBitmap != null) {
                String b64 = encodeBitmapToBase64(editBitmap);
                if (b64 != null) {
                    currentItem.base64Image = b64;
                }
            }
            itemList.set(itemIndex, currentItem);
            saveItemListToPrefs(itemList);

            finishToCalculus();
        });
    }

    private void finishToCalculus() {
        Intent intent = new Intent(this, CalculusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQUEST_EDIT_GALLERY && res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            // 高解像度1200
            Bitmap newBmp = decodeUriToBitmap(uri, 1200);
            if (newBmp != null) {
                editBitmap = newBmp;
                hasNewImage = true;
                ibEditImage.setImageBitmap(editBitmap);
                tvEditImageHint.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "画像取得に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ============== SharedPreferences ================
    private List<CalculusActivity.CalItem> loadItemListFromPrefs() {
        String s = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_ITEM_LIST, "");
        List<CalculusActivity.CalItem> result = new ArrayList<>();
        if (s.isEmpty()) return result;

        String[] items = s.split(ITEM_DELIMITER);
        for (String it : items) {
            if (it.trim().isEmpty()) continue;
            String[] f = it.split(FIELD_DELIMITER);
            if (f.length < 3) continue;
            result.add(new CalculusActivity.CalItem(f[0], f[1], f[2]));
        }
        return result;
    }

    private void saveItemListToPrefs(List<CalculusActivity.CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalculusActivity.CalItem c : list) {
            sb.append(c.base64Image).append(FIELD_DELIMITER)
                    .append(c.spinnerText).append(FIELD_DELIMITER)
                    .append(c.editText).append(ITEM_DELIMITER);
        }
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEM_LIST, sb.toString())
                .apply();
    }

    // ============== 画像処理 ================
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
