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

public class CalculusActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 1;

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

    private ImageButton imageButton;
    private TextView tvImageHint;
    private Spinner spinner;
    private EditText editTextField;
    private Button btnAdd;
    private Spinner searchSpinner;
    private LinearLayout dynamicContainer;

    private Bitmap selectedBitmap = null;
    private boolean isSpinnerSelected = false;
    private String selectedSpinnerItem;

    private List<CalItem> itemList = new ArrayList<>();

    private static final String PREF_NAME = "CalculusPrefs";
    private static final String KEY_ITEM_LIST = "CAL_ITEM_LIST";
    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";

    private List<String> calcTitles;
    private List<String> searchSpinnerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculus);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // ImageButton + hint
        imageButton = findViewById(R.id.image_button);
        tvImageHint = findViewById(R.id.tv_image_button_hint);

        spinner = findViewById(R.id.spinner);
        editTextField = findViewById(R.id.edit_text);
        btnAdd = findViewById(R.id.btn_add);
        searchSpinner = findViewById(R.id.search_spinner);
        dynamicContainer = findViewById(R.id.dynamic_table_container);

        // Spinner
        String[] fromRes = getResources().getStringArray(R.array.calculus_menu);
        calcTitles = new ArrayList<>();
        calcTitles.add("タイトル選択");
        for (String s : fromRes) {
            calcTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, calcTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedSpinnerItem = null;
                    isSpinnerSelected = false;
                } else {
                    selectedSpinnerItem = calcTitles.get(position);
                    isSpinnerSelected = true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ImageButton -> ギャラリー
        imageButton.setOnClickListener(v -> openGallery());

        btnAdd.setOnClickListener(v -> {
            String textVal = editTextField.getText().toString().trim();
            if (selectedBitmap == null) {
                Toast.makeText(this, "画像を選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isSpinnerSelected) {
                Toast.makeText(this, "タイトルを選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (textVal.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            String base64 = encodeBitmapToBase64(selectedBitmap);
            if (base64 == null) {
                Toast.makeText(this, "画像エンコードに失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }

            CalItem item = new CalItem(base64, selectedSpinnerItem, textVal);
            itemList.add(item);
            reloadDynamicViews(itemList);
            saveItemListToPrefs(itemList);

            clearInputFields();
        });

        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);

        setupSearchSpinner();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQUEST_GALLERY && res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Bitmap bmp = decodeUriToBitmap(uri, 600);
            if (bmp != null) {
                selectedBitmap = bmp;
                imageButton.setImageBitmap(bmp);
                tvImageHint.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSearchSpinner() {
        searchSpinnerItems = new ArrayList<>();
        searchSpinnerItems.add("すべて");
        String[] fromRes = getResources().getStringArray(R.array.calculus_menu);
        for (String s : fromRes) {
            searchSpinnerItems.add(s);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, searchSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(adapter);

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

        ImageView iv = new ImageView(this);
        TableRow.LayoutParams ivParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 1f
        );
        iv.setLayoutParams(ivParams);

        Bitmap small = decodeBase64ToBitmap(item.base64Image, 300);
        if (small != null) {
            iv.setImageBitmap(small);
        } else {
            iv.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams txtParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 2f
        );
        textLayout.setLayoutParams(txtParams);

        TextView tvSpin = new TextView(this);
        tvSpin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        tvSpin.setText(item.spinnerText);

        TextView tvEdt = new TextView(this);
        tvEdt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f));
        tvEdt.setText(item.editText);

        textLayout.addView(tvSpin);
        textLayout.addView(tvEdt);

        row.addView(iv);
        row.addView(textLayout);

        // 拡大表示
        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, CExpansionActivity.class);
            intent.putExtra("INDEX", position);
            intent.putExtra("ACTIVITY_TITLE", "微分積分");
            startActivity(intent);
        });

        dynamicContainer.addView(row);
    }

    private void clearInputFields() {
        selectedBitmap = null;
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        tvImageHint.setVisibility(View.VISIBLE);

        spinner.setSelection(0);
        isSpinnerSelected = false;
        selectedSpinnerItem = null;

        editTextField.setText("");
    }

    private void saveItemListToPrefs(List<CalItem> list) {
        StringBuilder sb = new StringBuilder();
        for (CalItem c : list) {
            sb.append(c.base64Image).append(FIELD_DELIMITER)
                    .append(c.spinnerText).append(FIELD_DELIMITER)
                    .append(c.editText).append(ITEM_DELIMITER);
        }
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEM_LIST, sb.toString())
                .apply();
    }

    private List<CalItem> loadItemListFromPrefs() {
        String s = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_ITEM_LIST, "");
        List<CalItem> result = new ArrayList<>();
        if (s.isEmpty()) return result;

        String[] items = s.split(ITEM_DELIMITER);
        for (String it : items) {
            if (it.trim().isEmpty()) continue;
            String[] f = it.split(FIELD_DELIMITER);
            if (f.length < 3) continue;
            result.add(new CalItem(f[0], f[1], f[2]));
        }
        return result;
    }

    // 画像処理
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
            while ((w / inSampleSize) > maxSize || (h / inSampleSize) > maxSize) {
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
        float r = (float) w / (float) h;
        if (r > 1f) {
            w = maxSize;
            h = (int)(maxSize / r);
        } else {
            h = maxSize;
            w = (int)(maxSize * r);
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

    @Override
    protected void onResume() {
        super.onResume();
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);
    }
}
