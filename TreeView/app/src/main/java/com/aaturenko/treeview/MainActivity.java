package com.aaturenko.treeview;

        import android.database.Cursor;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.os.AsyncTask;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ListView;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;



public class MainActivity extends AppCompatActivity{

    //для работы с бд
    DatabaseHelper sqlHelper;
    //массив элементов ListView
    ArrayList<Item> items;
    ListAdapter adapter;
    //вспомогательный массив категорий первого уровня в ирархии
    ArrayList<String> parents;
    ListView mList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqlHelper = new DatabaseHelper(this);
        parents = new ArrayList<>();
        items = new ArrayList<>();
        mList = (ListView) this.findViewById(R.id.listView);

        //sqlHelper.upgradeDataBase();
        if (sqlHelper.getRowsCount() == 0) {
            //экземпляр класса - асинсхронное действие, в которой просходит получение данных в сервера, обработка json и вызов функции, строящей дерево
            new ParseTask().execute();
        } else {
            buildTree();
            setAdapter();
        }



        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.clickOnItem(position);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //upgradeTree();

    }

    public void setAdapter() {
        adapter = new ListAdapter(this, items);
        mList.setAdapter(adapter);
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        int level = 0;

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {

                URL url = new URL("https://money.yandex.ru/api/categories-list");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;

        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            //разбираем json на составляющие части
          try {
                JSONArray categories = new JSONArray(strJson);
              //установлен параметр default, для категорий первого уровня
              parseJsonArray(categories, "Default");

            } catch (JSONException e) {
              e.printStackTrace();
            }

            Log.d("LOG_bd", "after update");
            sqlHelper.printDataBaseInLog();

            //строим дерево
            buildTree();
           //adapter.notifyDataSetChanged();
            //устанавливаем адаптер
            setAdapter();

        }

        //вспомогательная рекурсивная функция для выборки категорий нижних уровней
        protected void parseJsonArray(JSONArray arr, String parent) {
            try {
                level++;
                for (int i = 0; i < arr.length(); i++) {

                    JSONObject item = arr.getJSONObject(i);
                    try {
                        sqlHelper.addItemToTable(item.getInt("id"), item.getString("title"), parent, level);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sqlHelper.addItemToTable(item.getString("title"), parent, level);
                    }

                    try {
                        JSONArray array = item.getJSONArray("subs");
                        parseJsonArray(array, item.getString("title"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            level--;
        }
    }

    private void buildTree() {

        Log.d("LOG_tree", "is building");

        //запрос на выбор названий всех категорий, входящих в таблицу
        Cursor currentTitle = sqlHelper.getTitles();

        if (currentTitle.moveToFirst()) {
            do {
                 parents.add(currentTitle.getString(0));
            } while (currentTitle.moveToNext());
        }

        for (int i = 0; i<parents.size(); i++) {
            items.add(buildTreeNode(parents.get(i)));
        }

        parents.clear();

    }

    private ListItem buildTreeNode(String title) {

        ListItem treeNode = new ListItem(title);
        //запрос на выбор подкатегорий заданной родительской категории
        Cursor currentChild = sqlHelper.getTitlesOfChildren(title);

        if (currentChild.moveToFirst()) {
            do {
                parents.remove(currentChild.getString(0));
                treeNode.addChild(buildTreeNode(currentChild.getString(0)));

            } while (currentChild.moveToNext());
        }

        return treeNode;
    }

    private void upgradeTree() {

        items.clear();
        sqlHelper.upgradeDataBase();
        //sqlHelper.onUpgrade(sqlHelper.getReadableDatabase(), sqlHelper.getDataBaseVersion(), sqlHelper.getDataBaseVersion() + 1);
        new ParseTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            upgradeTree();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
