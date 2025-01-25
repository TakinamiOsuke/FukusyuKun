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
 * 微分積分: 編集画面 (画像変更対応)
 * レイアウト: c_edit.xml
 */
public class CEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_EDIT = 2;

    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";
    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";

    private int itemIndex = -1;
    private String activityTitle = "編集中";

    private Button btnClose, btnSave;
    private TextView tvTitle;
    private Spinner spinnerEdit;
    private EditText editTextEdit;

    private ImageButton imageButton;
    private TextView tvImageHint;
    private Uri selectedImageUri = null;
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
        tvTitle = findViewById(R.id.tv_edit_title);
        spinnerEdit = findViewById(R.id.spinner_edit);
        editTextEdit = findViewById(R.id.et_edit_text);
        imageButton = findViewById(R.id.edit_image_button);
        tvImageHint = findViewById(R.id.tv_edit_image_hint);

        itemIndex = getIntent().getIntExtra("INDEX", -1);
        activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");
        if (activityTitle == null) {
            activityTitle = "編集中";
        }
        tvTitle.setText(activityTitle + " - 編集中");

        // itemList load
        itemList = loadItemListFromPrefs();
        if (itemIndex >= 0 && itemIndex < itemList.size()) {
            currentItem = itemList.get(itemIndex);
        }

        // Spinner: index=0 => 「タイトル選択」
        String[] fromRes = getResources().getStringArray(R.array.calculus_menu);
        calcTitles = new ArrayList<>();
        calcTitles.add("タイトル選択");
        for (String s : fromRes) {
            calcTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, calcTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdit.setAdapter(spAdapter);

        // 既存情報
        if (currentItem != null) {
            setSpinnerSelection(spinnerEdit, currentItem.spinnerText);
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

        imageButton.setOnClickListener(v -> openGalleryForEdit());

        btnClose.setOnClickListener(v -> finishToCalculus());
        btnSave.setOnClickListener(v -> {
            if (currentItem == null) {
                finishToCalculus();
                return;
            }
            String titleSelected = getSelectedSpinnerTitle();
            String newText = editTextEdit.getText().toString().trim();

            if (titleSelected.equals("タイトル選択")) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            currentItem.spinnerText = titleSelected;
            currentItem.editText = newText;

            if (hasNewImage && selectedImageUri != null) {
                String newBase64 = encodeImageToBase64(selectedImageUri);
                if (newBase64 != null) {
                    currentItem.base64Image = newBase64;
                }
            }

            itemList.set(itemIndex, currentItem);
            saveItemListToPrefs(itemList);

            finishToCalculus();
        });
    }

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
            Bitmap bmp = getScaledBitmapFromUri(selectedImageUri, 300);
            if (bmp != null) {
                imageButton.setImageBitmap(bmp);
                tvImageHint.setVisibility(View.GONE);
            }
        }
    }

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

    private void finishToCalculus() {
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
        if (serialized.isEmpty()) return result;

        String[] items = serialized.split(ITEM_DELIMITER);
        for (String chunk : items) {
            if (chunk.trim().isEmpty()) continue;
            String[] f = chunk.split(FIELD_DELIMITER);
            if (f.length < 3) continue;
            result.add(new CalculusActivity.CalItem(f[0], f[1], f[2]));
        }
        return result;
    }

    private void saveItemListToPrefs(List<CalculusActivity.CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalculusActivity.CalItem it : list) {
            sb.append(it.base64Image).append(FIELD_DELIMITER)
                    .append(it.spinnerText).append(FIELD_DELIMITER)
                    .append(it.editText).append(ITEM_DELIMITER);
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEM_LIST, sb.toString()).apply();
    }

    // ================================
    // 画像関連
    // ================================
    private String encodeImageToBase64(Uri uri) {
        Bitmap bmp = getScaledBitmapFromUri(uri, 2000);
        if (bmp == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
            Bitmap sample = BitmapFactory.decodeStream(in, null, opts);
            in.close();
            if (sample == null) return null;

            w = sample.getWidth();
            h = sample.getHeight();
            float r = (float) w / (float) h;
            if (w > maxSize || h > maxSize) {
                if (r > 1f) {
                    w = maxSize;
                    h = (int)(maxSize / r);
                } else {
                    h = maxSize;
                    w = (int)(maxSize * r);
                }
                return Bitmap.createScaledBitmap(sample, w, h, true);
            }
            return sample;
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
            int w = raw.getWidth(), h = raw.getHeight();
            if (w > maxSize || h > maxSize) {
                float r = (float) w / (float) h;
                if (r > 1f) {
                    w = maxSize;
                    h = (int)(maxSize / r);
                } else {
                    h = maxSize;
                    w = (int)(maxSize * r);
                }
                return Bitmap.createScaledBitmap(raw, w, h, true);
            }
            return raw;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
