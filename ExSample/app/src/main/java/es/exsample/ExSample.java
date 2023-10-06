package es.exsample;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExSample extends AppCompatActivity {
    TextView tt_select, tt_date, tt_info;
    Button bt_add, bt_delete;
    private SampleDBHelper dbhelper;
    private static SQLiteDatabase db;
    private boolean isUniversitySelected;
    private EditText et;
    ListView lv1, lv2;
    ArrayList<String> arrayList1 = new ArrayList<>();
    ArrayList<String> arrayList2 = new ArrayList<>();
    private TextView tv_todo;
    ArrayAdapter<String> ad1, ad2;
    int date = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("20yyMMdd")));
    int index = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabexsample);

        TabHost th = (TabHost)findViewById(android.R.id.tabhost);  //TabHostオブジェクト取得
        th.setup();  //TabHostのセットアップ

        TabSpec tab1 = th.newTabSpec("tab1");  //tab1のセットアップ
        tab1.setIndicator("カレンダー");  //タブ名の設定
        tab1.setContent(R.id.tab1);   //タブに使うリニアレイアウトの指定
        th.addTab(tab1);  //タブホストにタブを追加

        TabSpec tab2 = th.newTabSpec("tab2");  //tab2のセットアップ
        tab2.setIndicator("ToDo");
        tab2.setContent(R.id.tab2);
        th.addTab(tab2);

        th.setCurrentTab(0); //最初のタブをtab1に設定

        // タブ1画面
        // カレンダー
        CalendarView cl = (CalendarView) findViewById(R.id.cl);
        cl.setOnDateChangeListener(new ExSample.DateChangeListener());
        // テキストビュー
        tt_date = (TextView) findViewById(R.id.tt_date);

        // リストビュー
        ad1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList1);
        lv1 = (ListView) findViewById(R.id.lv1);
        lv1.setAdapter(ad1);  //リストビュー1にアレイアダプターを登録

        // ラジオボタン
        tt_select = (TextView) findViewById(R.id.text_select);
        RadioButton rb_university = (RadioButton) findViewById(R.id.radioButton_university);
        rb_university.setChecked(true);
        isUniversitySelected = true;
        RadioButton rb_jobHunt = (RadioButton)findViewById(R.id.radioButton_jobhunt);
        rb_university.setOnClickListener(new ExSample.RadioClickListener());
        rb_jobHunt.setOnClickListener(new ExSample.RadioClickListener());

        // 予定追加ボタン
        bt_add = (Button) findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new ExSample.ButtonClickListener());

        // エディットテキスト
        et = (EditText) findViewById(R.id.ti);

        // タブ2画面
        tv_todo = (TextView) findViewById(R.id.textView_todo);
        tt_info = (TextView) findViewById(R.id.tt_info);
        // リストビュー
        ad2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList2);
        lv2 = (ListView) findViewById(R.id.lv2);
        lv2.setAdapter(ad2);  //リストビュー2にアレイアダプターを登録
        lv2.setOnItemClickListener(new ExSample.ListItemClickListener());

        // 削除ボタン
        bt_delete = (Button) findViewById(R.id.bt_delete);
        bt_delete.setOnClickListener(new ExSample.ButtonClickListener());

        // データベース
        dbhelper = new SampleDBHelper(this);  //データベースヘルパークラスの生成
        db = dbhelper.getWritableDatabase();  //データベースの作成 or オープン
        db.delete("taskTable", null, null);  //データベースのリセット
    }

    class DateChangeListener implements CalendarView.OnDateChangeListener {
        // カレンダー
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            // 選択された日付をCalendarオブジェクトとして生成し、年、月、日を設定する
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            // 年、月、日を抽出する
            int selectedYear = selectedCalendar.get(Calendar.YEAR);
            int selectedMonth = selectedCalendar.get(Calendar.MONTH); // 注意：月は0から始まるため (0から11)
            int selectedDayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            // 現在の年、月、日を"20xx-yy-mm"の形式で表示する
            date = Integer.parseInt(String.format("%04d%02d%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth));
            tt_date.setText(String.format("　%02d月%02d日", selectedMonth + 1, selectedDayOfMonth));

            Cursor cr1 = db.query("taskTable", new String[]{"_id", "name", "date", "isUniversity"}, "date = ?", new String[]{String.valueOf(date)}, null, null, null);
            arrayList1.clear();
            ad1.notifyDataSetChanged();
            while (cr1.moveToNext()) {  //カーソルを一つづつ動かしてデータ取得
                String name = cr1.getString(cr1.getColumnIndex("name"));
                arrayList1.add(name); // リストに取得した名前を追加
            }
            cr1.close();
        }
    }

    class RadioClickListener implements View.OnClickListener {
        // ラジオボタンの操作
        public void onClick(View v) {
            RadioButton tmp = (RadioButton) v;
            tt_select.setText(tmp.getText() + "を選択しています。");

            // 選択されたラジオボタンに基づいて選択状態を更新
            if (v.getId() == R.id.radioButton_university) {
                isUniversitySelected = true;
            } else if (v.getId() == R.id.radioButton_jobhunt) {
                isUniversitySelected = false;
            }
        }
    }

    class ButtonClickListener implements OnClickListener {
        // 予定追加ボタンの操作
        public void onClick(View v) {
            ContentValues values = new ContentValues();  //データベースに入力するデータを保存するためのオブジェクトの生成
            if(v == bt_add) {
                if (!arrayList2.contains(et.getText().toString())) {
                    values.put("name", et.getText().toString());  //エディットテキストからデータベースに入力する値を取得
                    values.put("date", date);
                    if (isUniversitySelected) {
                        values.put("isUniversity", 1);
                    } else {
                        values.put("isUniversity", 0);
                    }
                    db.insert("taskTable", null, values);  //データベースに値を挿入
                    // ダイアログ表示
                    new AlertDialog.Builder(ExSample.this).setTitle("予定を追加")  //ダイアログのタイトル、メッセージ、「Yes」ボタンの設定とダイアログの表示
                            .setMessage(et.getText().toString() + "をToDoに追加しました。").setPositiveButton("OK", null).show();

                    Cursor cr1 = db.query("taskTable", new String[]{"_id", "name", "date", "isUniversity"}, "date = ?", new String[]{String.valueOf(date)}, null, null, null);
                    arrayList1.clear();
                    ad1.notifyDataSetChanged();
                    while (cr1.moveToNext()) {  //カーソルを一つづつ動かしてデータ取得
                        String name = cr1.getString(cr1.getColumnIndex("name"));
                        arrayList1.add(name); // リストに取得した名前を追加
                    }
                    cr1.close();
                } else {
                    Toast.makeText(ExSample.this, "すでに追加されている予定名は使えません。", Toast.LENGTH_SHORT).show();
                }
            } else if (v == bt_delete) {
                // ListViewでアイテムが選択されているかをチェック
                if (index != -1) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ExSample.this);
                    dialog.setTitle("警告");
                    dialog.setMessage("本当に削除しますか？");
                    dialog.setPositiveButton("はい", new ExSample.DialogClickListener());
                    dialog.setNegativeButton("いいえ", null).show();

                    Cursor cr1 = db.query("taskTable", new String[]{"_id", "name", "date", "isUniversity"}, "date = ?", new String[]{String.valueOf(date)}, null, null, null);
                    arrayList1.clear();
                    ad1.notifyDataSetChanged();
                    while (cr1.moveToNext()) {  //カーソルを一つづつ動かしてデータ取得
                        String name = cr1.getString(cr1.getColumnIndex("name"));
                        arrayList1.add(name); // リストに取得した名前を追加
                    }
                    cr1.close();
                } else {
                    // アイテムが選択されていない場合は、Toastなどのフィードバックを表示する
                    Toast.makeText(ExSample.this, "削除する予定を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }

            Cursor cr2 = db.query("taskTable", new String[]{"_id", "name", "date", "isUniversity"}, null, null, null, null, "date ASC");  //クエリ結果をカーソルで取得
            arrayList2.clear();
            ad2.notifyDataSetChanged();
            while (cr2.moveToNext()) {  //カーソルを一つづつ動かしてデータ取得
                String taskName = cr2.getString(cr2.getColumnIndex("name"));
                arrayList2.add(taskName); // リストに取得した名前を追加
            }
            cr2.close();
        }
    }

    class ListItemClickListener implements AdapterView.OnItemClickListener {
        //リストビューのアイテムクリック時のイベント処理
        public void onItemClick(AdapterView<?>v, View iv, int pos, long id){
            TextView tmp = (TextView) iv;
            tv_todo.setText(tmp.getText() + "を選択しています。");
            index = pos;

            // データベースから選択されたアイテムの情報を取得
            String selectedTask = arrayList2.get(index);
            Cursor cr3 = db.query("taskTable", new String[]{"_id", "name", "date", "isUniversity"}, "name = ?", new String[]{selectedTask}, null, null, null);

            if (cr3.moveToFirst()) {
                // "date" カラムと "isUniversity" カラムの値を取得
                int selectedDate = cr3.getInt(cr3.getColumnIndex("date"));
                int isUniversity = cr3.getInt(cr3.getColumnIndex("isUniversity"));

                // "date" の値を "yyyy-MM-dd" の形式に変換
                String formattedDate = formatDate(selectedDate);

                // TextViewに情報を表示
                String information = (isUniversity == 1 ? "大学関連" : "就活関連") + "\n" +  "予定日: " + formattedDate;
                tt_info.setText(information);
            }
            cr3.close();
        }
        private String formatDate(int date) {
            String dateString = String.valueOf(date);
            String year = dateString.substring(0, 4);
            String month = dateString.substring(4, 6);
            String day = dateString.substring(6, 8);
            return year + "-" + month + "-" + day;
        }
    }

    class DialogClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface d, int w){
            // データベースから選択された要素を削除する
            String selectedTask = arrayList2.get(index);
            db.delete("taskTable", "name = ?", new String[]{selectedTask});

            // 選択された要素をリストビューからも削除する
            arrayList2.remove(index);
            ad2.notifyDataSetChanged();

            tv_todo.setText("予定一覧");
            tt_info.setText("予定をタップすると詳細が表示されます。");
            index = -1;
        }
    }
}