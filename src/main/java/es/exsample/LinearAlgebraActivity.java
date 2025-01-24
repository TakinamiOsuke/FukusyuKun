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
 *  - レイアウト崩れを防ぐため：リスト項目表示時は解像度を抑えたサイズで表示
 *  - 画像は PNG (100%) の Base64 で保持して、拡大表示時には大きめにデコード
 *  - 編集・削除機能は LAExpansionActivity, LAEditActivity にて行う
 */
public class LinearAlgebraActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // ================================
    // データ保持用クラス
    // ================================
    public static class LAItem {
        // 画像をBase64エンコードした文字列 (PNG, 100%)
        public String base64Image;
        // Spinnerで選択された項目
        public String spinnerText;
        // EditTextに入力されたテキスト
        public String editText;

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
    private String selectedSpinnerItem;
    private String enteredText;

    private ImageButton imageButton;
    private Spinner spinner;
    private EditText editText;
    private Button btnAdd;
    private LinearLayout dynamicContainer;
    private SearchView searchView;

    // メモリ上のリスト（アプリ起動中はこちらを参照）
    private List<LAItem> itemList = new ArrayList<>();

    // ================================
    // 定数 (SharedPreferences)
    // ================================
    private static final String PREF_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEM_LIST = "LA_ITEM_LIST";

    // 区切り文字（アイテム間とフィールド間）
    private static final String ITEM_DELIMITER = "@@@";   // アイテム同士の区切り
    private static final String FIELD_DELIMITER = "###";  // 各フィールドの区切り

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_algebra);

        // 各ビューの取得
        Button btnBack = findViewById(R.id.btn_back);
        imageButton = findViewById(R.id.image_button);
        spinner = findViewById(R.id.spinner);
        editText = findViewById(R.id.edit_text);
        btnAdd = findViewById(R.id.btn_add);
        dynamicContainer = findViewById(R.id.dynamic_table_container);
        searchView = findViewById(R.id.search_view);

        // 戻るボタン
        btnBack.setOnClickListener(v -> finish());

        // ギャラリーを開くボタン
        imageButton.setOnClickListener(v -> openGallery());

        // Spinner 選択
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpinnerItem = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSpinnerItem = null;
            }
        });

        // 追加ボタン
        btnAdd.setOnClickListener(v -> {
            enteredText = editText.getText().toString().trim();
            if (selectedImageUri != null && selectedSpinnerItem != null && !enteredText.isEmpty()) {
                // 画像をBase64に変換（PNG, 100%）してリストに追加
                String base64 = encodeImageToBase64(selectedImageUri);
                if (base64 == null) {
                    Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
                    return;
                }

                LAItem item = new LAItem(base64, selectedSpinnerItem, enteredText);
                itemList.add(item);

                // 表示を更新
                reloadDynamicViews(itemList);

                // SharedPreferences に保存
                saveItemListToPrefs(itemList);

                // 入力内容をリセット
                clearInputFields();
            } else {
                Toast.makeText(this, "すべての項目を入力してください", Toast.LENGTH_SHORT).show();
            }
        });

        // SharedPreferences からデータを読み込み
        itemList = loadItemListFromPrefs();
        // 表示を更新
        reloadDynamicViews(itemList);

        // SearchView の動作設定
        setupSearchView();
    }

    /**
     * 画像を選択するためにギャラリーを開く
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * ギャラリーから戻ってきた時の処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // リスト項目用のサムネイル表示 (300px)
            Bitmap scaledBitmap = getScaledBitmapFromUri(selectedImageUri, 300);
            if (scaledBitmap != null) {
                imageButton.setImageBitmap(scaledBitmap);
            } else {
                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }

    /**
     * 入力フィールドをリセット（画像・Spinner・テキスト）
     */
    private void clearInputFields() {
        selectedImageUri = null;
        selectedSpinnerItem = null;
        enteredText = null;

        // ImageButton を初期アイコンに戻す
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        // Spinner を先頭に戻す
        spinner.setSelection(0);
        // EditText をクリア
        editText.setText("");
    }

    /**
     * itemList の内容をダイナミックに再描画
     * 重複表示を防ぐため、一度 container をクリアしてから再生成する
     */
    private void reloadDynamicViews(List<LAItem> list) {
        dynamicContainer.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            LAItem item = list.get(i);
            addDynamicItem(dynamicContainer, item, i);
        }
    }

    /**
     * 動的に TableRow を生成して追加
     *  - 画像は小さめ (例: 300px) でデコードして表示 -> レイアウト崩れ防止
     */
    private void addDynamicItem(LinearLayout container, LAItem item, int position) {
        // 新しい TableRow を作成 (レイアウト崩れしにくい横並び)
        TableRow newRow = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);
        newRow.setPadding(8, 8, 8, 8);

        // 左側: ImageView (weight=1)
        ImageView imageView = new ImageView(this);
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        );
        imageView.setLayoutParams(imageParams);

        // Base64 → Bitmap (小さい maxSize で)
        Bitmap smallBitmap = decodeBase64ToBitmap(item.base64Image, 300);
        if (smallBitmap != null) {
            imageView.setImageBitmap(smallBitmap);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 右側: LinearLayout (縦並び, weight=2)
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams textLayoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                2f
        );
        textLayout.setLayoutParams(textLayoutParams);

        // (上) Spinner テキスト
        TextView spinnerTextView = new TextView(this);
        LinearLayout.LayoutParams spinnerTextViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        spinnerTextView.setLayoutParams(spinnerTextViewParams);
        spinnerTextView.setText(item.spinnerText);

        // (下) EditText テキスト
        TextView editTextView = new TextView(this);
        LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                2f
        );
        editTextView.setLayoutParams(editTextViewParams);
        editTextView.setText(item.editText);

        textLayout.addView(spinnerTextView);
        textLayout.addView(editTextView);

        newRow.addView(imageView);
        newRow.addView(textLayout);

        // タップ時に拡大表示 (大きいサイズで読み込む)
        newRow.setOnClickListener(v -> openExpansionActivity(position));

        container.addView(newRow);
    }

    /**
     * 拡大表示画面へ遷移
     */
    private void openExpansionActivity(int position) {
        Intent intent = new Intent(this, LAExpansionActivity.class);
        intent.putExtra("INDEX", position);
        intent.putExtra("ACTIVITY_TITLE", "線形代数");
        startActivity(intent);
    }

    // ================================
    // SharedPreferences に保存/読み込み
    // ================================

    private void saveItemListToPrefs(List<LAItem> list) {
        StringBuilder sb = new StringBuilder();
        for (LAItem item : list) {
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

    private List<LAItem> loadItemListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String serialized = prefs.getString(KEY_ITEM_LIST, "");
        List<LAItem> result = new ArrayList<>();
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
            result.add(new LAItem(fields[0], fields[1], fields[2]));
        }
        return result;
    }

    // ================================
    // 画像のエンコード/デコード関連
    // ================================

    /**
     * Uri から画像を読み込み、PNG(100%)で Base64 化
     * (一時的に大きくリサイズして保存すれば拡大表示も高解像度)
     */
    private String encodeImageToBase64(Uri uri) {
        // ストレージ上の最大サイズ -> 一定程度大きめ (例: 2000px)
        Bitmap bitmap = getScaledBitmapFromUri(uri, 2000);
        if (bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // PNGはlosslessなので quality は無視されるが、100指定しておく
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Base64文字列を Bitmap にデコードし、更に maxSize に合わせてリサイズ (アスペクト比維持)
     */
    private Bitmap decodeBase64ToBitmap(String base64, int maxSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            // デコード
            BitmapFactory.Options options = new BitmapFactory.Options();
            // まず元画像をメモリ上に展開
            Bitmap rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (rawBitmap == null) {
                return null;
            }
            // リサイズ
            int width = rawBitmap.getWidth();
            int height = rawBitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float ratio = (float) width / (float) height;
                if (ratio > 1f) {
                    // 横長
                    width = maxSize;
                    height = (int)(maxSize / ratio);
                } else {
                    // 縦長
                    height = maxSize;
                    width = (int)(maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(rawBitmap, width, height, true);
            } else {
                // そのまま
                return rawBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Uri からアスペクト比を保ちながら最大 maxSize に収まる Bitmap を生成
     */
    private Bitmap getScaledBitmapFromUri(Uri uri, int maxSize) {
        try {
            // 1) 画像の実寸法を取得
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            // 2) inSampleSize を決定
            int inSampleSize = 1;
            while ((originalWidth / inSampleSize) > maxSize || (originalHeight / inSampleSize) > maxSize) {
                inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            // 3) 実際にビットマップをデコード
            inputStream = getContentResolver().openInputStream(uri);
            Bitmap sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            if (sampledBitmap == null) {
                return null;
            }

            // 4) 長辺が maxSize を超える場合、さらにリサイズ
            int scaledWidth = sampledBitmap.getWidth();
            int scaledHeight = sampledBitmap.getHeight();
            float ratio = (float) scaledWidth / (float) scaledHeight;

            if (scaledWidth > maxSize || scaledHeight > maxSize) {
                if (ratio > 1f) {
                    // 横長
                    scaledWidth = maxSize;
                    scaledHeight = (int) (maxSize / ratio);
                } else {
                    // 縦長
                    scaledHeight = maxSize;
                    scaledWidth = (int) (maxSize * ratio);
                }
                return Bitmap.createScaledBitmap(sampledBitmap, scaledWidth, scaledHeight, true);
            } else {
                return sampledBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================
    // SearchView 検索機能
    // ================================

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterListBySpinnerText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // リアルタイムに絞り込みたいならここで呼ぶ
                // filterListBySpinnerText(newText);
                return false;
            }
        });

        // SearchView の × ボタン押下
        searchView.setOnCloseListener(() -> {
            reloadDynamicViews(itemList);
            return false;
        });
    }

    private void filterListBySpinnerText(String query) {
        if (query == null || query.trim().isEmpty()) {
            reloadDynamicViews(itemList);
            return;
        }
        List<LAItem> filtered = new ArrayList<>();
        for (LAItem item : itemList) {
            if (item.spinnerText.equals(query.trim())) {
                filtered.add(item);
            }
        }
        reloadDynamicViews(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 他の画面で編集・削除されたかもしれないので再読み込み
        itemList = loadItemListFromPrefs();
        reloadDynamicViews(itemList);
    }
}
