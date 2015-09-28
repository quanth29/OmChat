package in.ohmama.omchat.debug;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.OmApplication;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DaoMaster;
import in.ohmama.omchat.model.DbCore;
import in.ohmama.omchat.ui.activity.BaseActivity;

public class DatabaseShowActivity extends BaseActivity implements View.OnClickListener {

    HorizontalScrollView container;
    LinearLayout btnContainer;
    Button btnClear;
    Button btnUpdate;
    int currentView = 0;
    String currentTable;
    static final int TABLE_LIST_VIEW = 0;
    static final int TABLE_VIEW = 1;
    SQLiteDatabase mDataBase = DbCore.getDb();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_show);


        initView();
        addTablesToView();
    }

    // 加入所有表
    void addTablesToView() {
        currentTable = "";
        container.removeAllViews();
        btnContainer.setVisibility(View.INVISIBLE);
        currentView = TABLE_LIST_VIEW;
        List<String> tables = queryAllTables();
        LinearLayout mmLinear = new LinearLayout(this);
        FrameLayout.LayoutParams sparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mmLinear.setOrientation(LinearLayout.VERTICAL);
        mmLinear.setLayoutParams(sparams);


        for (final String name : tables) {
            TextView tvTable = new TextView(this);
            tvTable.setClickable(true);
            tvTable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addDatasToView(name);
                }
            });
            tvTable.setText(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvTable.setLayoutParams(params);
            mmLinear.addView(tvTable);
        }
        container.addView(mmLinear);
    }

    // 查看表内容
    private void addDatasToView(String tableName) {
        btnContainer.setVisibility(View.VISIBLE);
        container.removeAllViews();
        currentView = TABLE_VIEW;
        currentTable = tableName;

        Cursor dbCursor = mDataBase.query(tableName, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();

        // tablelayout
        TableLayout tableLayout = new TableLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tableLayout.setLayoutParams(params);
        TableRow.LayoutParams textParam = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
//        TableRow.LayoutParams textParam = new TableRow.LayoutParams(50, 100);

        // header
        TableRow rowheader = new TableRow(this);
        for (String c : columnNames) {
            TextView tv = new TextView(this);
            tv.setText(c);
            tv.setLayoutParams(textParam);
            tv.setTextSize(10);
            rowheader.addView(tv);
        }
        tableLayout.addView(rowheader);


        // 表头 column name
        if (dbCursor.moveToFirst()) {
            while (!dbCursor.isAfterLast()) {
                TableRow row = new TableRow(this);
                for (String n : columnNames) {
                    int index = dbCursor.getColumnIndex(n);
                    String dataStr = dbCursor.getString(index);
                    if (dataStr != null) {
                        if (dataStr.length() > 10)
                            dataStr = dataStr.substring(0, 10) + "..";
                    }
                    TextView tv = new TextView(this);
                    tv.setTextSize(10);
                    tv.setPadding(2, 2, 2, 2);
                    tv.setText(dataStr);
                    tv.setLayoutParams(textParam);
                    row.addView(tv);
                }
                dbCursor.moveToNext();
                tableLayout.addView(row);
            }
        }
        if (tableLayout.getChildCount() == 1) {
            TableRow row = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText("空");
            row.addView(tv);
            tableLayout.addView(row);
        }
        container.addView(tableLayout);
    }

    // 删除表
    void deleteTableContents(String tableName) {
        int count = mDataBase.delete(tableName, "1", null);
    }

    private void addTableToRow(String[] columns) {
        TableRow row = new TableRow(this);
        for (String c : columns) {
            TextView tv = new TextView(this);
            tv.setText(c);
            row.addView(tv);
        }
    }

    // 请求所有表
    private List<String> queryAllTables() {
        List<String> tables = new ArrayList<>();
        Cursor c = mDataBase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String name = c.getString(c.getColumnIndex("name"));
                tables.add(name);
                c.moveToNext();
            }
        }
        return tables;
    }

    private void initView() {
        container = (HorizontalScrollView) findViewById(R.id.db_container);
        btnContainer = (LinearLayout) findViewById(R.id.btnContainer);
        btnClear = (Button) findViewById(R.id.clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTableContents(currentTable);
                addTablesToView();
            }
        });
        btnUpdate = (Button) findViewById(R.id.update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DaoMaster.dropAllTables(mDataBase, true);
                DaoMaster.createAllTables(mDataBase, true);
                addTablesToView();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                deleteTableContents(currentTable);
                addTablesToView();
                break;
            default:
                break;
        }
    }


}
