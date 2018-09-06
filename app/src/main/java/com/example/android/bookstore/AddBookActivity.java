package com.example.android.bookstore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;
import com.example.android.bookstore.data.BookDbHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddBookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AddBookActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private BookDbHelper mDbHelper;
    private boolean Error;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private Spinner mCategorySpinner;
    private Button mIncrease;
    private Button mDecrease;
    private Button mContactSupplier;
    private ImageView mBookImage;

    private int mCategory = 0;
    private Uri currentBookUri;
    private Uri imageUri;
    private boolean mBookHasChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);

        mNameEditText = findViewById(R.id.edit_book_name);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_book_supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.edit_book_supplier_phone_number);
        mCategorySpinner = findViewById(R.id.spinner_book_category);
        mBookImage = findViewById(R.id.book_image);
        mIncrease = findViewById(R.id.increase_quantity);
        mDecrease = findViewById(R.id.decrease_quantity);
        mContactSupplier = findViewById(R.id.contact_supplier);

        Intent intent = getIntent();
        currentBookUri = intent.getData();
        if (currentBookUri != null) {
            setTitle(getString(R.string.Edit_Book_Activity_Title));
            getLoaderManager().initLoader(0, null, this);
        } else {
            mContactSupplier.setVisibility(View.GONE);
            setTitle(getString(R.string.Add_Book_Activity_Title));
        }

        mNameEditText.setOnTouchListener(onTouchListener);
        mPriceEditText.setOnTouchListener(onTouchListener);
        mQuantityEditText.setOnTouchListener(onTouchListener);
        mSupplierNameEditText.setOnTouchListener(onTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(onTouchListener);
        mBookImage.setOnTouchListener(onTouchListener);
        mCategorySpinner.setOnTouchListener(onTouchListener);
        mIncrease.setOnTouchListener(onTouchListener);
        mDecrease.setOnTouchListener(onTouchListener);

        if (checkPermission_MANAGE_DOCUMENT()) {
            mBookImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;

                    if (Build.VERSION.SDK_INT < 19) {
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                    } else {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                    }

                    File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = publicDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);

                    intent.setDataAndType(data, "image/*");
                    startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
                }
            });
        }
        mDbHelper = new BookDbHelper(this);
        setupSpinner();
    }

    private boolean checkPermission_MANAGE_DOCUMENT() {
        if (Build.VERSION.SDK_INT < 19) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return true;
            } else {
                return false;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.MANAGE_DOCUMENTS},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                imageUri = data.getData();
                mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri imageUri) {
        if (imageUri == null || imageUri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mBookImage.getWidth();
        int targetH = mBookImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(imageUri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image. " + fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image. " + e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "IOException " + ioe);
            }
        }
    }

    private void setupSpinner() {
        //Create an ArrayAdaptar from the String array
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.book_categories, android.R.layout.simple_spinner_item);

        //Set the view for the Drop down list
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        //Set the array adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);
        //Attach the listener to the spinner
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);

                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.book_category_fiction))) {
                        mCategory = BookEntry.CATEGORY_FICTION;
                    } else if (selection.equals(getString(R.string.book_category_history))) {
                        mCategory = BookEntry.CATEGORY_HISTORY;
                    } else if (selection.equals(getString(R.string.book_category_technology))) {

                        mCategory = BookEntry.CATEGORY_TECHNOLOGY;
                    } else {
                        mCategory = BookEntry.CATEGORY_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = 0; //Select unknown
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //Create AlertDialog to create the alert box interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Insert screen data to SQLite books database.
    private void saveBook() {
        Error = false;
        String bookName = mNameEditText.getText().toString().trim();
        String bookSupplierName = mSupplierNameEditText.getText().toString().trim();
        String bookPrice = mPriceEditText.getText().toString().trim();
        String bookQuantity = mQuantityEditText.getText().toString().trim();
        String bookSupplierPhoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
        String bookImageUri = "";

        if (imageUri != null) {
            bookImageUri = imageUri.toString();
        }

        if (currentBookUri == null && TextUtils.isEmpty(bookName) && TextUtils.isEmpty(bookSupplierName)
                && TextUtils.isEmpty(bookPrice) && TextUtils.isEmpty(bookQuantity) && TextUtils.isEmpty(bookSupplierPhoneNumber)
                && mCategory == BookEntry.CATEGORY_UNKNOWN) {
            return;
        }

        //Create a ContentValues object to insert data into database table.
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(mNameEditText.getText())) {
            values.put(BookEntry.COLUMN_BOOK_NAME, mNameEditText.getText().toString());
        } else {
            mNameEditText.setError("Name required");
            Error = true;
            return;
        }

        int price = 0;
        if (!TextUtils.isEmpty(bookPrice)) {
            price = Integer.parseInt(bookPrice);
        } else {
            mPriceEditText.setError("Price required.");
            Error = true;
            return;
        }
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        int quantity = 0;
        if (!TextUtils.isEmpty(bookQuantity)) {
            quantity = Integer.parseInt(bookQuantity);
        }
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_CATEGORY, mCategory);

        if (!TextUtils.isEmpty(bookSupplierName)) {
            values.put(BookEntry.COLUMN_BOOK_SUPPLIERNAME, mSupplierNameEditText.getText().toString());
        } else {
            mSupplierNameEditText.setError("Supplier Name required");
            Error = true;
            return;
        }
        if (!TextUtils.isEmpty(bookSupplierPhoneNumber)) {
            values.put(BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER, mSupplierPhoneNumberEditText.getText().toString());
        }else{
            mSupplierPhoneNumberEditText.setError("Supplier Number required");
            Error = true;
            return;
        }
        if (!TextUtils.isEmpty(bookImageUri)) {
            values.put(BookEntry.COLUMN_BOOK_IMAGE_URI, bookImageUri);
        }
        if (currentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(getApplicationContext(), R.string.insert_book_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.insert_book_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            String selection = BookEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(ContentUris.parseId(currentBookUri))};
            int rowsUpdated = getContentResolver().update(BookEntry.CONTENT_URI, values, selection, selectionArgs);
            if (rowsUpdated == 0) {
                Toast.makeText(getApplicationContext(), R.string.update_book_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.update_book_successful, rowsUpdated), Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Create options menu for AddBookActivity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options. This adds menu items to the app bar.

        if (currentBookUri == null) {
            setTitle(getString(R.string.Add_Book_Activity_Title));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.Edit_Book_Activity_Title));
        }
        getMenuInflater().inflate(R.menu.menu_addbook, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                if (!Error) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                deleteBook();
                finish();
                return true;
            case android.R.id.home:
                //Navigate back to parent activity.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddBookActivity.this);
                    return true;
                }
                //Otherwise if there are unsaved changes, setup a dialog to warn the user.
                //Create a click listener to handle the user confirming that
                //changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //User clicked  "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(AddBookActivity.this);
                            }
                        };
                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Delete data from SQLite database
    private void deleteBook() {
        String selection = BookEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(ContentUris.parseId(currentBookUri))};
        int rowDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, selection, selectionArgs);
        if (rowDeleted == 0) {
            Toast.makeText(getApplicationContext(), R.string.delete_book_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.delete_book_successful, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_BOOK_PRICE, BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_CATEGORY, BookEntry.COLUMN_BOOK_SUPPLIERNAME, BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER, BookEntry.COLUMN_BOOK_IMAGE_URI};
        String selection = BookEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(currentBookUri))};
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_NAME)));
            mPriceEditText.setText(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_PRICE)));
            mCategorySpinner.setSelection(data.getInt(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_CATEGORY)));
            mQuantityEditText.setText(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_QUANTITY)));
            mSupplierNameEditText.setText(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_SUPPLIERNAME)));
            mSupplierPhoneNumberEditText.setText(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_SUPPLIERPHONENUMBER)));
            if (data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_IMAGE_URI)) != null) {
                imageUri = Uri.parse(data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_IMAGE_URI)));
                mBookImage.setImageBitmap(getBitmapFromUri(imageUri));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mPriceEditText.setText(null);
        mCategorySpinner.setSelection(BookEntry.CATEGORY_UNKNOWN);
        mQuantityEditText.setText(null);
        mSupplierNameEditText.setText(null);
        mSupplierPhoneNumberEditText.setText(null);
        mBookImage.setImageBitmap(null);
    }

    public void increment(View view) {

        String bookQuantity = mQuantityEditText.getText().toString();
        if (TextUtils.isEmpty(bookQuantity)) {
            bookQuantity = "0";
        }
        int quantity = Integer.parseInt(bookQuantity) + 1;
        displayQuantity(quantity);

    }

    public void decrement(View view) {
        String bookQuantity = mQuantityEditText.getText().toString();
        if (TextUtils.isEmpty(bookQuantity)) {
            bookQuantity = "0";
        }
        if (Integer.parseInt(bookQuantity) > 0) {
            int quantity = Integer.parseInt(bookQuantity) - 1;
            displayQuantity(quantity);
        }
    }

    private void displayQuantity(int quantity) {
        EditText quantityEditView = findViewById(R.id.edit_book_quantity);
        quantityEditView.setText("" + quantity);

    }

    public void ContactSupplier(View view) {
        if (!TextUtils.isEmpty(mSupplierPhoneNumberEditText.getText())) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            Uri uri = Uri.parse("tel:" + mSupplierPhoneNumberEditText.getText());
            callIntent.setData(uri);
            if (ActivityCompat.checkSelfPermission(AddBookActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(AddBookActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                startActivity(callIntent);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Supplier doesn't have a contact number in the system.", Toast.LENGTH_SHORT).show();
        }
    }
}
