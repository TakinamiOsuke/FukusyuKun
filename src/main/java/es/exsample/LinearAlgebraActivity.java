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
 * 線形代数画面
 *  - ImageButton に重複アイコンが出ないよう初期 src はnull
 *  - clearInputFields() 時に setImageDrawable(null) などでリセット
 *  - 画質は PNG(100%) にし、編集画面側は変更せず
 */
public class LinearAlgebraActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 1;

    public static class LAItem {
        public String base64Image;
        public String spinnerText;
        public String editText;
        public LAItem(String base64Image, String spinnerText, String editText) {
            this.base64Image = base64Image;
            this.spinnerText = spinnerText;
            this.editText = editText;
        }
    }

    private ImageButton imageButton;
    private TextView tvImageButtonHint;
    private Spinner spinner;
    private EditText editText;
    private Button btnAdd;
    private Spinner searchSpinner;
    private LinearLayout dynamicContainer;

    private Bitmap selectedBitmap = null;    // 選択した画像
    private boolean isSpinnerSelected = false;
    private String selectedSpinnerItem;

    private List<LAItem> itemList = new ArrayList<>();

    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";
    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";

    private List<String> algebraTitles;
    private List<String> searchSpinnerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_algebra);

        // ビュー取得
        Button btnBack = findViewById(R.id.btn_back);
        imageButton = findViewById(R.id.image_button);
        tvImageButtonHint = findViewById(R.id.tv_image_button_hint);
        spinner = findViewById(R.id.spinner);
        editText = findViewById(R.id.edit_text);
        btnAdd = findViewById(R.id.btn_add);
        searchSpinner = findViewById(R.id.search_spinner);
        dynamicContainer = findViewById(R.id.dynamic_table_container);

        btnBack.setOnClickListener(v -> finish());

        // データ入力用 Spinner (先頭は「タイトル選択」)
        String[] fromRes = getResources().getStringArray(R.array.linear_algebra_menu);
        algebraTitles = new ArrayList<>();
        algebraTitles.add("タイトル選択");
        for (String s : fromRes) {
            algebraTitles.add(s);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algebraTitles);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedSpinnerItem = null;
                    isSpinnerSelected = false;
                } else {
                    selectedSpinnerItem = algebraTitles.get(position);
                    isSpinnerSelected = true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ImageButton -> ギャラリー
        imageButton.setOnClickListener(v -> openGallery());

        // 追加ボタン
        btnAdd.setOnClickListener(v -> {
            String textVal = editText.getText().toString().trim();
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

            // 画像を Base64
            String base64 = encodeBitmapToBase64(selectedBitmap);
            if (base64 == null) {
                Toast.makeText(this, "画像のエンコードに失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }

            LAItem item = new LAItem(base64, selectedSpinnerItem, textVal);
            itemList.add(item);
            reloadDynamicViews(itemList);
            saveItemListToPrefs(itemList);

            // 入力リセット
            clearInputFields();
        });

        // SharedPreferences ロード
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);

        // 検索Spinner
        setupSearchSpinner();
    }

    private void setupSearchSpinner() {
        searchSpinnerItems = new ArrayList<>();
        searchSpinnerItems.add("すべて");
        String[] arr = getResources().getStringArray(R.array.linear_algebra_menu);
        for (String s : arr) {
            searchSpinnerItems.add(s);
        }

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, searchSpinnerItems);
        searchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(searchAdapter);

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sel = searchSpinnerItems.get(position);
                if ("すべて".equals(sel)) {
                    reloadDynamicViews(itemList);
                } else {
                    List<LAItem> filtered = new ArrayList<>();
                    for (LAItem it : itemList) {
                        if (it.spinnerText.equals(sel)) {
                            filtered.add(it);
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
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, @Nullable Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == REQUEST_GALLERY && resCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Bitmap bmp = decodeUriToBitmap(uri, 600);
            if (bmp != null) {
                selectedBitmap = bmp;
                // ImageButtonに表示 & テキスト非表示
                imageButton.setImageBitmap(bmp);
                tvImageButtonHint.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void reloadDynamicViews(List<LAItem> list) {
        dynamicContainer.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            addDynamicItem(list.get(i), i);
        }
    }

    private void addDynamicItem(LAItem item, int position) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        row.setPadding(8, 8, 8, 8);

        // 左: 小さめの画像
        ImageView iv = new ImageView(this);
        TableRow.LayoutParams ivParams = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.MATCH_PARENT, 1f
        );
        iv.setLayoutParams(ivParams);
        Bitmap smallBmp = decodeBase64ToBitmap(item.base64Image, 300);
        if (smallBmp != null) {
            iv.setImageBitmap(smallBmp);
        } else {
            iv.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 右: タイトル & テキスト
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

        row.setOnClickListener(v -> {
            // 拡大表示へ
            Intent intent = new Intent(this, LAExpansionActivity.class);
            intent.putExtra("INDEX", position);
            intent.putExtra("ACTIVITY_TITLE", "線形代数");
            startActivity(intent);
        });

        dynamicContainer.addView(row);
    }

    /**
     * 入力リセット -> ImageButton をアイコンなし状態にしてヒント表示
     */
    private void clearInputFields() {
        // Bitmap破棄
        selectedBitmap = null;
        // ImageButtonにアイコン or drawableをクリア
        imageButton.setImageDrawable(null);
        // テキストを再表示
        tvImageButtonHint.setVisibility(View.VISIBLE);

        spinner.setSelection(0);
        isSpinnerSelected = false;
        selectedSpinnerItem = null;

        editText.setText("");
    }

    private void saveItemListToPrefs(List<LAItem> list) {
        StringBuilder sb = new StringBuilder();
        for (LAItem it : list) {
            sb.append(it.base64Image).append(FIELD_DELIMITER)
                    .append(it.spinnerText).append(FIELD_DELIMITER)
                    .append(it.editText).append(ITEM_DELIMITER);
        }
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEM_LIST, sb.toString())
                .apply();
    }

    private List<LAItem> loadItemListFromPrefs() {
        String stored = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString(KEY_ITEM_LIST, "");
        List<LAItem> result = new ArrayList<>();
        if (stored.isEmpty()) return result;

        String[] items = stored.split(ITEM_DELIMITER);
        for (String chunk : items) {
            if (chunk.trim().isEmpty()) continue;
            String[] f = chunk.split(FIELD_DELIMITER);
            if (f.length < 3) continue;
            result.add(new LAItem(f[0], f[1], f[2]));
        }
        return result;
    }

    // ===================================
    // 画像処理
    // ===================================
    private Bitmap decodeUriToBitmap(Uri uri, int maxSize) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream in = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in, null, opts);
            in.close();

            int w = opts.outWidth, h = opts.outHeight;
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
        if (src == null) return null;
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
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
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
        // 編集画面から戻ったなどの場合、再描画
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);
    }
}
