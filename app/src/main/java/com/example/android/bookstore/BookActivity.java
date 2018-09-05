package com.example.android.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.bookstore.data.BookContract.BookEntry;
import com.example.android.bookstore.data.BookDbHelper;

public class BookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = BookActivity.class.getSimpleName();
    private BookDbHelper mDbHelper;
    private BookCursorAdapter bookAdapter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookActivity.this, AddBookActivity.class);
                startActivity(intent);
            }
        });

        ListView listViewBook = findViewById(R.id.book_list_view);
        View emptyBookView = findViewById(R.id.book_empty_view);
        listViewBook.setEmptyView(emptyBookView);

        bookAdapter = new BookCursorAdapter(this, cursor);
        listViewBook.setAdapter(bookAdapter);

        listViewBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BookActivity.this, AddBookActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });
        //Add a Loader to the LoaderManager with unique ID for this loader, optional bundle  and loadercallbacks Interface
        getLoaderManager().initLoader(0, null, this);
    }

    //LoaderManager calls this method when initLoader is called for the first time.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_BOOK_PRICE, BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_CATEGORY, BookEntry.COLUMN_BOOK_SUPPLIERNAME, BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER, BookEntry.COLUMN_BOOK_IMAGE_URI};
        String selection = null;
        String[] selectionArgs = null;
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    //Update UI based on the query results
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.swapCursor(data);
    }

    //Release any resources so that loader can free them.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.swapCursor(null);
    }

}

