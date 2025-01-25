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
 * 線形代数画面。
 * - 検索は SearchView ではなく、検索用Spinner (search_spinner) で絞り込み
 * - ImageButtonに「画像選択」メッセージを重ね表示
 * - データ入力用Spinnerに「タイトル選択」を最初( index=0 )に挿入
 * - 「編集」で画像も変更可能 (LAEditActivityにて)
 */
public class LinearAlgebraActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // ================================
    // データ保持用クラス
    // ================================
    public static class LAItem {
        public String base64Image;  // 画像をBase64 (PNG, 100%)
        public String spinnerText;  // タイトル
        public String editText;     // ユーザーのテキスト

        public LAItem(String base64Image, String spinnerText, String editText) {
            this.base64Image = base64Image;
            this.spinnerText = spinnerText;
            this.editText = editText;
        }
    }

    // ================================
    // フィールド変数
    // ================================
    private Uri selectedImageUri;
    private boolean hasSelectedImage = false; // 画像を選択済みかどうか
    private String selectedSpinnerItem;
    private boolean isSpinnerTitleSelected = false; // 「タイトル選択」以外を選んだかどうか
    private String enteredText;

    private FrameLayout frameImageButton;
    private ImageButton imageButton;
    private TextView tvImageButtonHint;

    private Spinner spinner;           // データ入力用 (タイトル)
    private EditText editText;
    private Button btnAdd;
    private LinearLayout dynamicContainer;

    private Spinner searchSpinner;     // 検索用
    private List<String> searchSpinnerItems; // 検索候補: "すべて" + ...?

    // メモリ上のリスト（アプリ起動中はこちらを参照）
    private List<LAItem> itemList = new ArrayList<>();

    // ================================
    // 定数
    // ================================
    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";

    // 区切り文字 (アイテム間 & フィールド間)
    private static final String ITEM_DELIMITER = "@@@";
    private static final String FIELD_DELIMITER = "###";

    // データ入力用 Spinner の「タイトル選択」(index=0) 以外
    private List<String> algebraTitles; // strings.xml などから読み込む想定

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_algebra);

        // 各ビューの取得
        Button btnBack = findViewById(R.id.btn_back);
        frameImageButton = findViewById(R.id.image_button).getParent() instanceof FrameLayout
                ? (FrameLayout) findViewById(R.id.image_button).getParent() : null;
        imageButton = findViewById(R.id.image_button);
        tvImageButtonHint = findViewById(R.id.tv_image_button_hint);

        spinner = findViewById(R.id.spinner);
        editText = findViewById(R.id.edit_text);
        btnAdd = findViewById(R.id.btn_add);
        dynamicContainer = findViewById(R.id.dynamic_table_container);
        searchSpinner = findViewById(R.id.search_spinner);

        // 戻るボタン
        btnBack.setOnClickListener(v -> finish());

        // --- データ入力用 Spinner に項目セット (index=0に「タイトル選択」)
        //    例として線形代数用のメニューを、res/values/arrays.xml から取得
        //    ここでは仮に "linear_algebra_menu" に ["行列","ベクトル","固有値"] などが定義されているとする
        String[] fromResource = getResources().getStringArray(R.array.linear_algebra_menu);
        algebraTitles = new ArrayList<>();
        algebraTitles.add("タイトル選択"); // index=0
        for (String s : fromResource) {
            algebraTitles.add(s);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algebraTitles);
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
                    selectedSpinnerItem = algebraTitles.get(position);
                    isSpinnerTitleSelected = true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ImageButton + 「画像選択」メッセージ
        imageButton.setOnClickListener(v -> openGallery());
        // 初期状態: 画像未選択 -> hasSelectedImage = false

        // 追加ボタン
        btnAdd.setOnClickListener(v -> {
            enteredText = editText.getText().toString().trim();

            // バリデーション
            // 1) 画像を選択済み? 2) Spinner が (「タイトル選択」でない) or (要望によってはOKにする?)
            // 3) テキスト入力済み?
            // 今回の要望どおりにすると「タイトル選択」でも追加OKなのかどうかが曖昧ですが、
            // 一般的には「タイトル選択」は未選択扱いにするケースが多いので、以下では
            // "isSpinnerTitleSelected" が true かどうかを判定に含めます。
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

            // ここまで来れば全項目OKとして追加
            // 画像をBase64に変換
            String base64 = encodeImageToBase64(selectedImageUri);
            if (base64 == null) {
                Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }
            LAItem item = new LAItem(base64, selectedSpinnerItem, enteredText);
            itemList.add(item);

            // 表示を更新
            reloadDynamicViews(itemList);
            // 保存
            saveItemListToPrefs(itemList);
            // 入力リセット
            clearInputFields();
        });

        // SharedPreferences からデータを読み込み
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);

        // 検索用 Spinner の設定
        setupSearchSpinner();

    }

    private void setupSearchSpinner() {
        // 「すべて」 + resource array
        // 例: "linear_algebra_menu" が ["行列","ベクトル","固有値"] 等
        searchSpinnerItems = new ArrayList<>();
        searchSpinnerItems.add("すべて");
        String[] fromResource = getResources().getStringArray(R.array.linear_algebra_menu);
        for (String s : fromResource) {
            searchSpinnerItems.add(s);
        }

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, searchSpinnerItems);
        searchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(searchAdapter);

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = searchSpinnerItems.get(position);
                if (selected.equals("すべて")) {
                    reloadDynamicViews(itemList);
                } else {
                    // 絞り込み
                    List<LAItem> filtered = new ArrayList<>();
                    for (LAItem it : itemList) {
                        if (it.spinnerText.equals(selected)) {
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
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            hasSelectedImage = true;

            // サムネイル表示用
            Bitmap scaledBitmap = getScaledBitmapFromUri(selectedImageUri, 300);
            if (scaledBitmap != null) {
                imageButton.setImageBitmap(scaledBitmap);
                tvImageButtonHint.setVisibility(View.GONE); // 「画像選択」メッセージ非表示
            } else {
                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
                tvImageButtonHint.setVisibility(View.VISIBLE);
                hasSelectedImage = false;
            }
        }
    }

    private void clearInputFields() {
        // 画像関連
        selectedImageUri = null;
        hasSelectedImage = false;
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        tvImageButtonHint.setVisibility(View.VISIBLE);

        // Spinner: index=0 (「タイトル選択」) に戻す
        spinner.setSelection(0);
        isSpinnerTitleSelected = false;
        selectedSpinnerItem = null;

        // EditText
        editText.setText("");
    }

    private void reloadDynamicViews(List<LAItem> list) {
        LinearLayout container = dynamicContainer;
        container.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            LAItem item = list.get(i);
            addDynamicItem(container, item, i);
        }
    }

    private void addDynamicItem(LinearLayout container, LAItem item, int position) {
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        newRow.setPadding(8, 8, 8, 8);

        // 左: ImageView (weight=1), 小サイズで表示
        ImageView imageView = new ImageView(this);
        TableRow.LayoutParams imgParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT, 1f);
        imageView.setLayoutParams(imgParams);

        Bitmap smallBitmap = decodeBase64ToBitmap(item.base64Image, 300);
        if (smallBitmap != null) {
            imageView.setImageBitmap(smallBitmap);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 右: 縦レイアウト (weight=2)
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams txtParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT, 2f);
        textLayout.setLayoutParams(txtParams);

        // (上) Spinnerタイトル相当
        TextView spinnerTextView = new TextView(this);
        spinnerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        spinnerTextView.setText(item.spinnerText);

        // (下) EditText内容
        TextView editTextView = new TextView(this);
        editTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f));
        editTextView.setText(item.editText);

        textLayout.addView(spinnerTextView);
        textLayout.addView(editTextView);

        newRow.addView(imageView);
        newRow.addView(textLayout);

        // タップで拡大表示
        newRow.setOnClickListener(v -> {
            Intent intent = new Intent(this, LAExpansionActivity.class);
            intent.putExtra("INDEX", position);
            intent.putExtra("ACTIVITY_TITLE", "線形代数");
            startActivity(intent);
        });

        container.addView(newRow);
    }

    // =====================================================================
    // SharedPreferences保存/読み込み
    // =====================================================================
    private void saveItemListToPrefs(List<LAItem> list) {
        StringBuilder sb = new StringBuilder();
        for (LAItem item : list) {
            sb.append(item.base64Image).append(FIELD_DELIMITER)
                    .append(item.spinnerText).append(FIELD_DELIMITER)
                    .append(item.editText).append(ITEM_DELIMITER);
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEM_LIST, sb.toString()).apply();
    }

    private List<LAItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<LAItem> result = new ArrayList<>();
        if (serialized.isEmpty()) return result;

        String[] chunks = serialized.split(ITEM_DELIMITER);
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty()) continue;
            String[] fields = chunk.split(FIELD_DELIMITER);
            if (fields.length < 3) continue;
            String base64 = fields[0];
            String spn = fields[1];
            String edt = fields[2];
            result.add(new LAItem(base64, spn, edt));
        }
        return result;
    }

    // =====================================================================
    // 画像処理 (PNG, 100%)
    // =====================================================================
    private String encodeImageToBase64(Uri uri) {
        Bitmap bitmap = getScaledBitmapFromUri(uri, 2000);
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Base64->Bitmap後、maxSizeでリサイズ
     */
    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (raw == null) return null;
            int w = raw.getWidth();
            int h = raw.getHeight();
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream in = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int w = options.outWidth;
            int h = options.outHeight;

            int inSampleSize = 1;
            while ((w / inSampleSize) > maxSize || (h / inSampleSize) > maxSize) {
                inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            in = getContentResolver().openInputStream(uri);
            Bitmap sampled = BitmapFactory.decodeStream(in, null, options);
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

    // =====================================================================
    // 画面復帰
    // =====================================================================
    @Override
    protected void onResume() {
        super.onResume();
        // 他画面(編集/削除)から戻った場合にリスト再読み込み
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);
    }
}
