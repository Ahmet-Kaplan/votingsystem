package org.votingsystem.android.contentprovider;

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
import android.text.TextUtils;
import android.util.Log;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
public class UserContentProvider extends ContentProvider {

    public static final String TAG = "UserContentProvider";

    //from http://www.buzzingandroid.com/2013/01/sqlite-insert-or-replace-through-contentprovider/
    public static final String SQL_INSERT_OR_REPLACE = "__sql_insert_or_replace__";

    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "voting_system_representatives.db";
    private static final String TABLE_NAME = "representatives";
    public static final String AUTHORITY = "votingsystem.org.representative";

    public static final String ID_COL                  = "_id";
    public static final String URL_COL                 = "url";
    public static final String NIF_COL                 = "nif";
    public static final String TYPE_COL                = "type";
    public static final String NUM_REPRESENTATIONS_COL = "numRepresentations";
    public static final String SERIALIZED_OBJECT_COL   = "serializedObject";
    public static final String FULL_NAME_COL           = "fullName";
    public static final String TIMESTAMP_CREATED_COL   = "timestampCreated";
    public static final String TIMESTAMP_UPDATED_COL   = "timestampUpdated";
    public static final String DEFAULT_SORT_ORDER      = ID_COL + " DESC";

    private SQLiteDatabase database;

    private static final int ALL_ITEMS     = 1;
    private static final int SPECIFIC_ITEM = 2;

    private static final String BASE_PATH = "representative";

    private static Long numTotalRepresentatives = null;

    private static final UriMatcher URI_MATCHER;
    static{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, ALL_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#",
                SPECIFIC_ITEM);
    }

    // Here's the public URI used to query for representative items.
    public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH);

    public static Uri getRepresentativeURI(Long representativeId) {
        return Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH + "/" + representativeId);
    }

    @Override public boolean onCreate() {
        //Delete previous session database
        getContext().deleteDatabase(DB_NAME);
        // If database file isn't found this will throw a  FileNotFoundException, and we will
        // then create the database.
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        try{
            database = databaseHelper.getWritableDatabase();
        } catch (Exception ex) {
            return false;
        }
        if(database == null) return false;
        else return true;
    }

    // Convert the URI into a custom MIME type. Our UriMatcher will parse the URI to decide
    // whether the URI is for a single item or a list.
    @Override public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case ALL_ITEMS:
                return "vnd.android.cursor.dir/representative"; // List of items.
            case SPECIFIC_ITEM:
                return "vnd.android.cursor.item/representative"; // Specific item.
            default:
                return null;
        }
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // We won't bother checking the validity of params here, but you should!
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(TABLE_NAME);
        if((URI_MATCHER.match(uri)) == SPECIFIC_ITEM){
            qBuilder.appendWhere(ID_COL + "=" + ContentUris.parseId(uri));
        }
        if(TextUtils.isEmpty(sortOrder)) sortOrder = DEFAULT_SORT_ORDER;
        Cursor cursor = qBuilder.query(database, projection, selection, selectionArgs,
                groupBy, having, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        // NOTE Argument checking code omitted. Check your parameters!
        int updateCount = database.update(TABLE_NAME, values,
                whereClause, whereArgs);
        // Notify any listeners and return the updated row count.
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    @Override public Uri insert(Uri requestUri, ContentValues values) {
        // NOTE Argument checking code omitted. Check your parameters! Check that
        // your row addition request succeeded!
        long rowId = -1;
        boolean replace = false;
        if (values.containsKey(SQL_INSERT_OR_REPLACE)) {
            replace = values.getAsBoolean(SQL_INSERT_OR_REPLACE);
            // Clone the values object, so we don't modify the original.
            // This is not strictly necessary, but depends on your needs
            //values = new ContentValues( values );
            // Remove the key, so we don't pass that on to db.insert() or db.replace()
            values.remove( SQL_INSERT_OR_REPLACE );
        }
        if ( replace ) {
            rowId = database.replace(TABLE_NAME, null, values);
        } else {
            rowId = database.insert(TABLE_NAME, null, values);
        }
        Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        // Notify any listeners and return the URI of the new row.
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        return newUri;
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        // NOTE Argument checking code omitted. Check your parameters!
        int rowCount = database.delete(TABLE_NAME, ID_COL + " = ?",
                new String[]{String.valueOf(ContentUris.parseId(uri))});
        // Notify any listeners and return the deleted row count.
        getContext().getContentResolver().notifyChange(uri, null);
        return rowCount;
    }

    public static Long getNumTotalRepresentatives() {
        return numTotalRepresentatives;
    }

    public static void setNumTotalRepresentatives(Long numTotal) {
        numTotalRepresentatives = numTotal;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                ID_COL                  + " INTEGER PRIMARY KEY, " +
                URL_COL                 + " TEXT," +
                NIF_COL                 + " TEXT," +
                NUM_REPRESENTATIONS_COL + " INTEGER DEFAULT 1, " +
                FULL_NAME_COL           + " TEXT, " +
                TYPE_COL                + " TEXT," +
                SERIALIZED_OBJECT_COL   + " blob, " +
                TIMESTAMP_UPDATED_COL   + " INTEGER DEFAULT 0, " +
                TIMESTAMP_CREATED_COL   + " INTEGER DEFAULT 0);";

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            //File dbFile = context.getDatabasePath(DB_NAME);
            //Log.d(TAG + ".DatabaseHelper(...)", "dbFile.getAbsolutePath(): " + dbFile.getAbsolutePath());
        }

        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Don't have any upgrades yet, so if this gets called for some reason we'll
            // just drop the existing table, and recreate the database with the
            // standard method.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        }

        @Override public void onCreate(SQLiteDatabase db){
            try{
                db.execSQL(DATABASE_CREATE);
                Log.d(TAG + ".DatabaseHelper.onCreate(...)", "Database created");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
