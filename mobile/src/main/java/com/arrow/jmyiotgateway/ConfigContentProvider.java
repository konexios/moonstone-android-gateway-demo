package com.arrow.jmyiotgateway;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.ADDED_DEVICES;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.API_SECURITY_KEY;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.APPLICATION_HID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.CODE;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.EMAIL;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.EXTERNAL_ID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.GATEWAY_HID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.GATEWAY_UID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.IS_ACTIVE;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.PASSWORD;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.PROFILE_NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.SELECTED_EVENT;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.SERVER__ENVIRONMENT;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.ZONE_SYSTEM_NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.TABLE_NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.USER_ID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry._ID;

/**
 * Created by osminin on 25.10.2016.
 */

public final class ConfigContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.arrow.jmyiotgateway.provider";
    public static final String SCHEME = "content://";

    // URIs
    public static final String ACCOUNTS = SCHEME + AUTHORITY + "/accounts";
    public static final Uri URI_ACCOUNTS = Uri.parse(ACCOUNTS);
    static final int ACCOUNTS_CODE = 1;
    static final int ACCOUNT_ID_CODE = 2;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String UNIQUE = " UNIQUE";
    private static final String ON_CONFLICT_REPLACE = " ON CONFLICT REPLACE";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EMAIL + TEXT_TYPE + COMMA_SEP +
                    GATEWAY_HID + TEXT_TYPE + COMMA_SEP +
                    GATEWAY_UID + TEXT_TYPE + COMMA_SEP +
                    PASSWORD + TEXT_TYPE + COMMA_SEP +
                    USER_ID + TEXT_TYPE + COMMA_SEP +
                    NAME + TEXT_TYPE + COMMA_SEP +
                    API_SECURITY_KEY + TEXT_TYPE + COMMA_SEP +
                    CODE + TEXT_TYPE + COMMA_SEP +
                    SELECTED_EVENT + TEXT_TYPE + COMMA_SEP +
                    ZONE_SYSTEM_NAME + TEXT_TYPE + COMMA_SEP +
                    APPLICATION_HID + TEXT_TYPE + COMMA_SEP +
                    EXTERNAL_ID + TEXT_TYPE  + COMMA_SEP +
                    SERVER__ENVIRONMENT + TEXT_TYPE  + COMMA_SEP +
                    ADDED_DEVICES + TEXT_TYPE  + COMMA_SEP +
                    IS_ACTIVE + INT_TYPE + COMMA_SEP +
                    PROFILE_NAME + TEXT_TYPE + COMMA_SEP +
                    UNIQUE + " (" + USER_ID + COMMA_SEP + APPLICATION_HID + COMMA_SEP + SERVER__ENVIRONMENT +")" + ON_CONFLICT_REPLACE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private DatabaseHelper dbHelper;

    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "accounts", ACCOUNTS_CODE);
        uriMatcher.addURI(AUTHORITY, "accounts/#", ACCOUNT_ID_CODE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DatabaseHelper(context);
        return true;
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        final SQLiteDatabase dbConnection = dbHelper.getReadableDatabase();

        switch (getUriMatcher().match(uri)) {
            case ACCOUNTS_CODE:
                queryBuilder.setTables(TABLE_NAME);
                break;
            case ACCOUNT_ID_CODE:
                queryBuilder.appendWhere(_ID + "=" + uri.getLastPathSegment());
                break;
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = "_id";
        }
        Cursor cursor = queryBuilder.query(dbConnection, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (getUriMatcher().match(uri)) {
            case ACCOUNTS_CODE:
                return "vnd.android.cursor.dir/vnd.arrow.accounts";
            case ACCOUNT_ID_CODE:
                return "vnd.android.cursor.item/vnd.arrow.accounts";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID = dbHelper.getWritableDatabase().insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(URI_ACCOUNTS, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    static final class ConfigContentContract {
        static final String TABLE_NAME = "accounts";
        private ConfigContentContract() {
        }

        /* Inner class that defines the table contents */
        static class ConfigEntry implements BaseColumns {
            static final String _ID = "_id";
            static final String EMAIL = "email";
            static final String GATEWAY_HID = "gateway_hid";
            static final String USER_ID = "user_id";
            static final String NAME = "name";
            static final String API_SECURITY_KEY = "api_security_key";
            static final String CODE = "code";
            static final String APPLICATION_HID = "application_hid";
            static final String EXTERNAL_ID = "external_id";
            static final String SERVER__ENVIRONMENT = "server_environment";
            static final String ADDED_DEVICES = "added_devices";
            static final String IS_ACTIVE = "is_active";
            static final String PROFILE_NAME = "profile_name";
            static final String GATEWAY_UID = "gateway_uid";
            static final String PASSWORD = "password";
            static final String SELECTED_EVENT = "selected_event_code";
            static final String ZONE_SYSTEM_NAME = "zone_system_name";
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        static final String DATABASE_NAME = "Jmyiotgateway.db";
        static final int DATABASE_VERSION = 8;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion == 7 && oldVersion == 6) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SELECTED_EVENT + TEXT_TYPE + " DEFAULT 'none'");
            }
            if (newVersion == 8 && oldVersion == 7) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ZONE_SYSTEM_NAME + TEXT_TYPE + " DEFAULT 'a01'");
            }
            if (newVersion == 8 && oldVersion == 6) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SELECTED_EVENT + TEXT_TYPE + " DEFAULT 'none'");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ZONE_SYSTEM_NAME + TEXT_TYPE + " DEFAULT 'a01'");
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
