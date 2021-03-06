package com.example.anastasia.application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.List;

//singleton pattern
public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }
    private final String create_users_table=
            "create table users(id integer primary key autoincrement, login text unique, password text);";
    private final String create_notes_table=
            "CREATE TABLE users_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER REFERENCES users (id),"+
            "date TEXT, header TEXT, raw_text TEXT, private_flag BOOLEAN);";
    private final String create_settings_table=
            "CREATE TABLE settings (user_id INTEGER REFERENCES users (id) PRIMARY KEY, status TEXT,font_style TEXT, font_size TEXT,"+
            "font_color TEXT, background_color TEXT, email TEXT, showAvatarBlock BOOLEAN, showEmailBlock BOOLEAN, avatar BLOB);";
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //таблица пользователей
        db.execSQL(create_users_table);
        //таблица записей в дневник
        db.execSQL(create_notes_table);
        //таблица настроек
        db.execSQL(create_settings_table);
    }
    public void AddUserToBD(String login, String password, String email)
    {
        ContentValues data = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        data.put("login", login);
        data.put("password", password);
        long rowID = db.insert("users", null, data);
        if(rowID==-1) throw new RuntimeException("databace users insert error");

        int user_id=selectIDFromLoginPassword(login,password);
        data=new ContentValues();
        data.put("email",email);
        data.put("user_id",user_id);
        rowID=db.insert("settings",null,data);
        if(rowID==-1) throw new RuntimeException("databace settings insert error");
    }
    public void AddNewNote(String header, String text, int user_id)
    {
        ContentValues data = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        String curStringDate = new SimpleDateFormat("dd.MM.yyyy/kk:mm:ss").format(System.currentTimeMillis());
        data.put("user_id", user_id);
        data.put("date", curStringDate);
        data.put("header", header);
        data.put("raw_text", text);
        data.put("private_flag", true);//temp stub
        long rowID = db.insert("users_notes", null, data);
        if(rowID==-1) throw new RuntimeException("databace users_notes insert error");
    }
    public void UpdateNote(String header, String text, String note_id)
    {
        ContentValues data = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        String curStringDate = new SimpleDateFormat("dd.MM.yyyy/kk:mm:ss").format(System.currentTimeMillis());
        data.put("id", note_id);
        data.put("date", curStringDate);
        data.put("header", header);
        data.put("raw_text", text);
        data.put("private_flag", true);//temp stub
        long rowID = db.update("users_notes", data,"id=?",new String[]{note_id});
        if(rowID==-1) throw new RuntimeException("databace users_notes update error");
    }
    public void SelectNotesToList(List<Note> note_list,String user_id)
    {
        SQLiteDatabase connection = getReadableDatabase();
        Cursor cursor=connection.rawQuery("select header,raw_text,date,id from users_notes where user_id=?",
                new String[]{user_id});
        Note note;
        while (cursor.moveToNext())
        {
            note=new Note(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getInt(3));
            note_list.add(note);
        }
    }
    public void DropNoteFromID(String id)
    {
        SQLiteDatabase db = getWritableDatabase();
        long rowID = db.delete("users_notes", "id=?",new String[]{id});
        if(rowID==-1) throw new RuntimeException("databace users_notes update error");
    }
    public int selectIDFromLoginPassword(String login, String password)
    {
        SQLiteDatabase connection = getReadableDatabase();
        Cursor cursor=connection.rawQuery("select id from users where login=? and password=?",
                new String[]{login,password});
        cursor.moveToFirst();
        int user_id=cursor.getInt(0);
        cursor.close();
        return user_id;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
