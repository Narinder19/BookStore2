package com.example.android.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {


    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        int position = cursor.getPosition();

        TextView textViewBookName = view.findViewById(R.id.book_name);
        TextView textViewBookPrice = view.findViewById(R.id.book_price);
        TextView textViewBookQuantity = view.findViewById(R.id.book_quantity);
        Button saleButton = view.findViewById(R.id.book_sale_button);

        if (position % 2 == 0) {
            view.setBackgroundColor(context.getResources().getColor(R.color.light_row_background));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.book_icon_background));
        }

        final int bookId = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry._ID));
        final String bookName = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_NAME));
        String bookPrice = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_PRICE));
        final String bookQuantity = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_QUANTITY));

        textViewBookName.setText(bookName);
        textViewBookPrice.setText(context.getString(R.string.currency_symbol, bookPrice));
        textViewBookQuantity.setText(context.getString(R.string.quantity_string, bookQuantity));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reduce the quantity by one when Sale button is pressed
                int quantity = Integer.parseInt(bookQuantity) - 1;
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
                context.getContentResolver().update(updateUri, values, null, null);
                Toast.makeText(context, "Sold one " + bookName , Toast.LENGTH_SHORT).show();
            }
        });

        if (Integer.parseInt(bookQuantity) > 0) {
            saleButton.setVisibility(View.VISIBLE);

        } else {
            //Hide sale button when quantity is 0
            saleButton.setVisibility(View.GONE);
        }
    }
}
