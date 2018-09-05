package com.example.android.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.bookstore";
    public static final String PATH_BOOKS = "books";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static abstract class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "books";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_QUANTITY = "quantity";
        public static final String COLUMN_BOOK_SUPPLIERNAME = "suppliername";
        public static final String COLUMN_BOOK_SUPPLIERPHONENUMBER = "supplierphonenumber";
        public static final String COLUMN_BOOK_CATEGORY = "category";
        public static final String COLUMN_BOOK_IMAGE_URI = "bookuri";

        //Possible values for book category.
        public static final int CATEGORY_UNKNOWN = 0;
        public static final int CATEGORY_FICTION = 1;
        public static final int CATEGORY_HISTORY = 2;
        public static final int CATEGORY_TECHNOLOGY = 3;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //Returns whether book category is valid or not.
        public static boolean isValidCategory(int category) {
            if (category == CATEGORY_FICTION || category == CATEGORY_HISTORY ||
                    category == CATEGORY_TECHNOLOGY || category == CATEGORY_UNKNOWN) {
                return true;
            }
            return false;
        }

    }
}
