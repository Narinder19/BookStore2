package com.example.android.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = BookDbHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "books.db";
    public static final String CREATE_BOOKS_DATABASE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
            + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
            + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, "
            + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER, "
            + BookEntry.COLUMN_BOOK_CATEGORY + " INTEGER NOT NULL DEFAULT 0, "
            + BookEntry.COLUMN_BOOK_SUPPLIERNAME + " TEXT NOT NULL, "
            + BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER + " TEXT, "
            + BookEntry.COLUMN_BOOK_IMAGE_URI + " TEXT "
            + ");";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create table books if it doesn't exist.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOKS_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
