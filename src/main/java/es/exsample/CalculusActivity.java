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
 * 微分積分画面。
 * - 検索Spinnerで絞り込み
 * - ImageButtonに「画像選択」メッセージ
 * - データ入力用 Spinner: index=0 => 「タイトル選択」
 * - 編集/削除は CExpansionActivity, CEditActivity
 */
public class CalculusActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    public static class CalItem {
        public String base64Image;
        public String spinnerText;
        public String editText;
        public CalItem(String base64Image, String spinnerText, String editText) {
            this.base64Image = base64Image;
            this.spinnerText = spinnerText;
            this.editText = editText;
        }
    }

    private Uri selectedImageUri;
    private boolean hasSelectedImage = false;
    private String selectedSpinnerItem;
    private boolean isSpinnerTitleSelected = false;
    private String enteredText;

    private ImageButton imageButton;
    private TextView tvImageButtonHint;
    private Spinner spinner;
    private EditText editTextField;
    private Button btnAdd;
    private LinearLayout dynamicContainer;

    private Spinner searchSpinner;
    private List<String> searchSpinnerItems;

    private List<CalItem> itemList = new ArrayList<>();

    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";
    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";

    private List<String> calcTitles; // "タイトル選択" + "微分" "積分" etc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculus);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        imageButton = findViewById(R.id.image_button);
        tvImageButtonHint = findViewById(R.id.tv_image_button_hint);
        spinner = findViewById(R.id.spinner);
        editTextField = findViewById(R.id.edit_text);
        btnAdd = findViewById(R.id.btn_add);
        dynamicContainer = findViewById(R.id.dynamic_table_container);
        searchSpinner = findViewById(R.id.search_spinner);

        // データ入力用Spinner: index=0 => 「タイトル選択」
        String[] fromRes = getResources().getStringArray(R.array.calculus_menu);
        calcTitles = new ArrayList<>();
        calcTitles.add("タイトル選択");
        for (String s : fromRes) {
            calcTitles.add(s);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, calcTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 「タイトル選択」
                    selectedSpinnerItem = null;
                    isSpinnerTitleSelected = false;
                } else {
                    selectedSpinnerItem = calcTitles.get(position);
                    isSpinnerTitleSelected = true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 画像選択
        imageButton.setOnClickListener(v -> openGallery());

        // 追加
        btnAdd.setOnClickListener(v -> {
            enteredText = editTextField.getText().toString().trim();
            if (!hasSelectedImage) {
                Toast.makeText(this, "画像を選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isSpinnerTitleSelected) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (enteredText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            String base64 = encodeImageToBase64(selectedImageUri);
            if (base64 == null) {
                Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }

            CalItem item = new CalItem(base64, selectedSpinnerItem, enteredText);
            itemList.add(item);

            reloadDynamicViews(itemList);
            saveItemListToPrefs(itemList);
            clearInputFields();
        });

        // SharedPreferences読み込み
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);

        // 検索Spinner設定
        setupSearchSpinner();
    }

    private void setupSearchSpinner() {
        searchSpinnerItems = new ArrayList<>();
        searchSpinnerItems.add("すべて");
        String[] fromRes = getResources().getStringArray(R.array.calculus_menu);
        for (String s : fromRes) {
            searchSpinnerItems.add(s);
        }
        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, searchSpinnerItems);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(sAdapter);

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sel = searchSpinnerItems.get(position);
                if (sel.equals("すべて")) {
                    reloadDynamicViews(itemList);
                } else {
                    List<CalItem> filtered = new ArrayList<>();
                    for (CalItem c : itemList) {
                        if (c.spinnerText.equals(sel)) {
                            filtered.add(c);
                        }
                    }
                    reloadDynamicViews(filtered);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, @Nullable Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == PICK_IMAGE_REQUEST && resCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            hasSelectedImage = true;
            Bitmap scaled = getScaledBitmapFromUri(selectedImageUri, 300);
            if (scaled != null) {
                imageButton.setImageBitmap(scaled);
                tvImageButtonHint.setVisibility(View.GONE);
            } else {
                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
                tvImageButtonHint.setVisibility(View.VISIBLE);
                hasSelectedImage = false;
            }
        }
    }

    private void clearInputFields() {
        selectedImageUri = null;
        hasSelectedImage = false;
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        tvImageButtonHint.setVisibility(View.VISIBLE);

        spinner.setSelection(0);
        isSpinnerTitleSelected = false;
        selectedSpinnerItem = null;

        editTextField.setText("");
    }

    private void reloadDynamicViews(List<CalItem> list) {
        dynamicContainer.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            addDynamicItem(list.get(i), i);
        }
    }

    private void addDynamicItem(CalItem item, int position) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        row.setPadding(8, 8, 8, 8);

        ImageView imageView = new ImageView(this);
        TableRow.LayoutParams imgParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT, 1f);
        imageView.setLayoutParams(imgParams);

        Bitmap small = decodeBase64ToBitmap(item.base64Image, 300);
        if (small != null) {
            imageView.setImageBitmap(small);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams txtParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT, 2f);
        textLayout.setLayoutParams(txtParams);

        TextView spinnerTxt = new TextView(this);
        spinnerTxt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        spinnerTxt.setText(item.spinnerText);

        TextView editTxt = new TextView(this);
        editTxt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f));
        editTxt.setText(item.editText);

        textLayout.addView(spinnerTxt);
        textLayout.addView(editTxt);

        row.addView(imageView);
        row.addView(textLayout);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, CExpansionActivity.class);
            intent.putExtra("INDEX", position);
            intent.putExtra("ACTIVITY_TITLE", "微分積分");
            startActivity(intent);
        });

        dynamicContainer.addView(row);
    }

    // ================================
    // SharedPreferences
    // ================================
    private void saveItemListToPrefs(List<CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalItem c : list) {
            sb.append(c.base64Image).append(FIELD_DELIMITER)
                    .append(c.spinnerText).append(FIELD_DELIMITER)
                    .append(c.editText).append(ITEM_DELIMITER);
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEM_LIST, sb.toString()).apply();
    }

    private List<CalItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<CalItem> result = new ArrayList<>();
        if (serialized.isEmpty()) return result;

        String[] chunks = serialized.split(ITEM_DELIMITER);
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty()) continue;
            String[] fields = chunk.split(FIELD_DELIMITER);
            if (fields.length < 3) continue;
            result.add(new CalItem(fields[0], fields[1], fields[2]));
        }
        return result;
    }

    // ================================
    // 画像関連
    // ================================
    private String encodeImageToBase64(Uri uri) {
        Bitmap bm = getScaledBitmapFromUri(uri, 2000);
        if (bm == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
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

    @Override
    protected void onResume() {
        super.onResume();
        // 編集/削除後の再読み込み
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);
    }
}
