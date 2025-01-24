package es.exsample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 線形代数画面
 * - 画像+Spinner+テキストを追加し、下部にリスト表示
 * - SharedPreferences によるデータ永続化
 * - SearchView による検索機能 (Spinner項目を基準に絞り込み)
 */
public class LinearAlgebraActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // 保存用キーなど
    private static final String PREFS_NAME = "LinearAlgebraPrefs";
    private static final String KEY_ITEMS = "SavedItems";

    // UI部品
    private ImageButton imageButton;
    private Spinner spinner;
    private EditText editText;
    private Button btnAdd;
    private LinearLayout dynamicContainer;
    private SearchView searchView;

    // 選択された一時データ
    private Uri selectedImageUri;
    private String selectedSpinnerItem;
    private String enteredText;

    // 全アイテムのリスト (SharedPreferences で保存)
    private List<AlgebraItem> itemList = new ArrayList<>();

    // 表示用にフィルタされたリスト
    private List<AlgebraItem> filteredList = new ArrayList<>();

    // Itemデータを表す内部クラス
    // 画像URI文字列, Spinner値, EditText値を保持
    static class AlgebraItem {
        String imageUri;
        String spinnerValue;
        String textValue;

        AlgebraItem(String uri, String spinner, String text) {
            this.imageUri = uri;
            this.spinnerValue = spinner;
            this.textValue = text;
        }
    }

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

        // ImageButton クリックリスナー（ギャラリーを開く）
        imageButton.setOnClickListener(v -> openGallery());

        // Spinner の選択リスナー
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

        // 「追加」ボタン
        btnAdd.setOnClickListener(v -> {
            enteredText = editText.getText().toString();
            if (selectedImageUri != null && selectedSpinnerItem != null && !enteredText.isEmpty()) {
                // itemList に追加 & 保存
                AlgebraItem newItem = new AlgebraItem(
                        selectedImageUri.toString(),
                        selectedSpinnerItem,
                        enteredText
                );
                itemList.add(newItem);
                saveItemsToPrefs();  // 永続化

                // 表示用リストも更新（フィルタ中であってもとりあえず再度フィルタ）
                filterItems(searchView.getQuery().toString());

                // 入力内容をリセット
                selectedImageUri = null;
                selectedSpinnerItem = null;
                enteredText = null;

                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
                spinner.setSelection(0);
                editText.setText("");
            } else {
                Toast.makeText(this, "すべての項目を入力してください", Toast.LENGTH_SHORT).show();
            }
        });

        // SharedPreferences からアイテムを読み込む
        loadItemsFromPrefs();

        // 初期表示: 全件表示
        filteredList.clear();
        filteredList.addAll(itemList);
        drawAllItems(filteredList);

        // SearchView リスナー (Spinner項目で絞り込み)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // ソフトキーボードの検索ボタンが押された時
                filterItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // テキストが変更されるたびに呼ばれる
                filterItems(newText);
                return true;
            }
        });

        // SearchView の「✖」を押した時など閉じる動作
        // （機種・テーマによっては自動呼び出しされる場合もあります）
        searchView.setOnCloseListener(() -> {
            // 全アイテム表示に戻す
            filterItems("");
            return false;
        });
    }

    /**
     * ギャラリーを開く (画像選択用)
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
            // リサイズしたビットマップを取得して、ImageButtonに表示
            Bitmap scaledBitmap = getScaledBitmapFromUri(selectedImageUri, 300);
            if (scaledBitmap != null) {
                imageButton.setImageBitmap(scaledBitmap);
            } else {
                imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }

    /**
     * itemListをSharedPreferencesに保存 (JSON形式)
     */
    private void saveItemsToPrefs() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (AlgebraItem item : itemList) {
                JSONObject obj = new JSONObject();
                obj.put("imageUri", item.imageUri);
                obj.put("spinnerValue", item.spinnerValue);
                obj.put("textValue", item.textValue);
                jsonArray.put(obj);
            }
            String jsonString = jsonArray.toString();
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_ITEMS, jsonString).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SharedPreferences から itemList を復元 (JSON形式)
     */
    private void loadItemsFromPrefs() {
        itemList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_ITEMS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String imageUriStr = obj.getString("imageUri");
                String spinnerValue = obj.getString("spinnerValue");
                String textValue = obj.getString("textValue");
                AlgebraItem item = new AlgebraItem(imageUriStr, spinnerValue, textValue);
                itemList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定のリストを dynamicContainer に描画し直す
     */
    private void drawAllItems(List<AlgebraItem> list) {
        dynamicContainer.removeAllViews();
        for (AlgebraItem item : list) {
            Uri uri = Uri.parse(item.imageUri);
            addDynamicItem(dynamicContainer, uri, item.spinnerValue, item.textValue);
        }
    }

    /**
     * 検索キーワード (query) に合致するアイテムだけを filteredList に入れて再描画
     * -> 今回は「Spinnerで選択した項目が query と一致(部分一致 or 完全一致)するか」を基準
     *    要件通りなら完全一致にするが、必要に応じて contains に変えても可
     */
    private void filterItems(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            // 検索キーワードなし => 全件
            filteredList.addAll(itemList);
        } else {
            // 大文字小文字を無視して比較
            String lowerQuery = query.toLowerCase();
            for (AlgebraItem item : itemList) {
                if (item.spinnerValue.toLowerCase().equals(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        drawAllItems(filteredList);
    }

    /**
     * 動的にTableRowを追加 (画像＋Spinnerテキスト＋EditTextテキスト)
     */
    private void addDynamicItem(LinearLayout container, Uri imageUri, String spinnerText, String editTextContent) {
        // 新しい TableRow を作成
        TableRow newRow = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);
        newRow.setPadding(8, 8, 8, 8);

        // 左側の ImageView (weight=1)
        ImageView imageView = new ImageView(this);
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        );
        imageView.setLayoutParams(imageParams);

        // リサイズビットマップを取得
        Bitmap scaledBitmap = getScaledBitmapFromUri(imageUri, 300);
        if (scaledBitmap != null) {
            imageView.setImageBitmap(scaledBitmap);
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 右側の LinearLayout (縦並び, weight=2)
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams textLayoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                2f
        );
        textLayout.setLayoutParams(textLayoutParams);

        // 上段: Spinnerテキスト
        TextView spinnerTextView = new TextView(this);
        LinearLayout.LayoutParams spinnerTextViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        spinnerTextView.setLayoutParams(spinnerTextViewParams);
        spinnerTextView.setText(spinnerText);

        // 下段: EditTextテキスト
        TextView editTextView = new TextView(this);
        LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                2f
        );
        editTextView.setLayoutParams(editTextViewParams);
        editTextView.setText(editTextContent);

        textLayout.addView(spinnerTextView);
        textLayout.addView(editTextView);

        newRow.addView(imageView);
        newRow.addView(textLayout);

        // タップ時に拡大表示を行うリスナー
        newRow.setOnClickListener(v -> openExpansionActivity(imageUri, spinnerText, editTextContent));

        container.addView(newRow);
    }

    /**
     * リスト項目タップで拡大表示用アクティビティを開く
     */
    private void openExpansionActivity(Uri imageUri, String spinnerText, String editTextContent) {
        Intent intent = new Intent(this, LAExpansionActivity.class);
        intent.putExtra("IMAGE_URI", imageUri.toString());
        intent.putExtra("SPINNER_TEXT", spinnerText);
        intent.putExtra("EDIT_TEXT", editTextContent);
        intent.putExtra("ACTIVITY_TITLE", "線形代数");
        startActivity(intent);
    }

    /**
     * Uri からアスペクト比を保ちつつ最大 maxSize に収まる Bitmap を生成するユーティリティ
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

            // 2) inSampleSizeを決定
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

            // 4) 長辺が maxSize を超えないようにリサイズ (アスペクト比維持)
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
}
