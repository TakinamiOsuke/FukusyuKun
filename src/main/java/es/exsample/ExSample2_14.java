//ExSample2_14.java リサイクラービューに関するサンプル
package es.exsample;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class ExSample2_14  extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);  //リサイクラービューを含むレイアウトの設定

        String titles[] = { "兼六園","21世紀美術館","近江町市場", "東茶屋街","武家屋敷"};  //各項目のタイトル
        String tags[] = { "10分", "5分", "15分", "30分", "20分" };  //各項目のタグ
        String descs[] = { "金沢を代表する観光地です。", "現代美術を扱う美術館です。", "金沢の台所と呼ばれる市場です。",
                "昔の金沢の風景が楽しめる通りです。", "伝統的な家屋と庭を楽しむことができます。" };  //各項目の説明
        ArrayList<SampleListItem> data = new ArrayList<>(); //各項目を扱うためのArrayList
        for (int i = 0; i < titles.length; i++) {  //各項目に値を設定
            SampleListItem item = new SampleListItem();
            item.setId((new Random()).nextLong());
            item.setTitle(titles[i]);
            item.setTag(tags[i]);
            item.setDesc(descs[i]);
            data.add(item);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);  //リサイクラービューの取得
        rv.setHasFixedSize(true);  //リサイクラービューのサイズを固定
        LinearLayoutManager manager = new LinearLayoutManager(this); //リニアレイアウトマネージャーの取得
        manager.setOrientation(LinearLayoutManager.VERTICAL); //リニアレイアウトを縦に使用
        //GridLayoutManager manager = new GridLayoutManager(this, 2);

        rv.setLayoutManager(manager);  //リサイクラービューにレイアウトマネージャーを設定
        RecyclerView.Adapter adapter = new SampleListAdapter(data); //リサイクラービューに渡す値を準備
        rv.setAdapter(adapter);  //リサイクラービューに値を設定
    }
}
