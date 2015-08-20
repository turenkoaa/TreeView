package com.aaturenko.treeview;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.util.Log;



public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATA_BASE_NAME = "data_base_tree_view.db"; // название бд
    private static int DATA_BASE_VERSION = 25;// версия базы данных
    static final String TABLE = "goods_and_services"; // название таблицы с

    // названия столбцов
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PARENT = "parent_id";
    public static final String COLUMN_LEVEL_HIERARCHY = "level_of_hierarchy";

    public DatabaseHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    /* SQLiteDatabase db;
        try {
            db = getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = getReadableDatabase();
        } */

    }

    public int getDataBaseVersion() {
        return DATA_BASE_VERSION;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE + "("
                + COLUMN_NUMBER + " INTEGER PRIMARY KEY," + COLUMN_ID + " INTEGER," + COLUMN_TITLE + " TEXT,"
                + COLUMN_PARENT + " TEXT," + COLUMN_LEVEL_HIERARCHY + " INTEGER);");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
       // DATA_BASE_VERSION++;
        onCreate(db);

    }

    public void upgradeDataBase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("CREATE TABLE " + TABLE + "("
                + COLUMN_NUMBER + " INTEGER PRIMARY KEY," + COLUMN_ID + " INTEGER," + COLUMN_TITLE + " TEXT,"
                + COLUMN_PARENT + " TEXT," + COLUMN_LEVEL_HIERARCHY + " INTEGER);");

        db.close();
    }

    //функция собавления строки в таблицу, перегружена
    //добавление полной строки
    public void addItemToTable(int id, String title, String parentId, int level) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PARENT, parentId);
        values.put(COLUMN_LEVEL_HIERARCHY, level);

        db.insert(TABLE, null, values);
        db.close();
    }

    //добавление без поля id
    public void addItemToTable(String title, String parentId, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PARENT, parentId);

        values.put(COLUMN_LEVEL_HIERARCHY, level);

        db.insert(TABLE, null, values);
        db.close();
    }

    //запрос на все вывод всех названий категорий, содержащихся в таблице
    public Cursor getTitles() {
        // String selectQuery = "SELECT " + sqlHelper.COLUMN_TITLE + " FROM " + sqlHelper.TABLE + " WHERE " + sqlHelper.COLUMN_LEVEL_HIERARCHY + "=" + "'" + 1 + "' ORDER BY " + sqlHelper.COLUMN_NUMBER;
        String selectQuery = "SELECT " + COLUMN_TITLE + " FROM " + TABLE + " ORDER BY " + COLUMN_NUMBER;

        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(selectQuery, null);
    }

    //запрос на вывод всех подкатегорий заданной родительской категории
    public Cursor getTitlesOfChildren(String parent) {

        String selectQuery = "SELECT " + COLUMN_TITLE + " FROM " + TABLE + " WHERE " + COLUMN_PARENT + "=" + "'" + parent + "'";

        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(selectQuery, null);
    }

    public void printDataBaseInLog() {
        String selectQuery = "SELECT * FROM " + TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.d("LOG_bd", "number " + cursor.getString(0) + ", id " + cursor.getString(1) + ", title " + cursor.getString(2) + ", parent " + cursor.getString(3));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public int getRowsCount() {
        String countQuery = "SELECT  * FROM " + TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }
}
