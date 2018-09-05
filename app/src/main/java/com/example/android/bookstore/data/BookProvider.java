package com.example.android.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {
    private static final String LOG_TAG = BookProvider.class.getSimpleName();
    private BookDbHelper mDbHelper;
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 1001;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Static initializer. This is run the first time anything is called from this class.
    static {
        mUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        mUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        int match = mUriMatcher.match(uri);
        Cursor cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //Return the MIME type of data for the content URI.
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = mUriMatcher.match(uri);

        //Notify all listeners that the data has changed for the Book content URI.
        getContext().getContentResolver().notifyChange(uri, null);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insert is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {

        checkContentValues(values);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long newRowId = database.insert(BookEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            Toast.makeText(getContext(), "Failed to insert row for " + uri, Toast.LENGTH_SHORT).show();
            return null;
        } else {
            return ContentUris.withAppendedId(uri, newRowId);
        }
    }

    private void checkContentValues(ContentValues values) {
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book name is required.");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_CATEGORY)) {
            Integer category = values.getAsInteger(BookEntry.COLUMN_BOOK_CATEGORY);
            if (category == null || !BookEntry.isValidCategory(category)) {
                throw new IllegalArgumentException("Book category cannot be null");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Book price is invalid. ");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book quantity is invalid. ");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIERNAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIERNAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Book supplier name is required.");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER)) {
            String supplierPhone = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Book supplier phone is invalid. ");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_IMAGE_URI)) {
            String bookImageUri = values.getAsString(BookEntry.COLUMN_BOOK_IMAGE_URI);
            if (bookImageUri == null) {
                throw new IllegalArgumentException("Book image is invalid. ");
            }
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                return deleteBook(uri, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteBook(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int deleteBook(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
        if (rowDeleted != 0) {
            //Notify all listeners that the data has changed for the pet content URI.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        checkContentValues(values);
        //Check if there are values to update.
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowUpdated != 0) {
            //Notify all listeners that the data has changed for the pet content URI.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }
}
