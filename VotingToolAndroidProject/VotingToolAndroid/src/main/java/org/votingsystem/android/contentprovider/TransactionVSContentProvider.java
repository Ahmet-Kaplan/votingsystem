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

import org.votingsystem.android.AppContextVS;
import org.votingsystem.model.CurrencyData;
import org.votingsystem.model.CurrencyVS;
import org.votingsystem.model.TransactionVS;
import org.votingsystem.model.VicketAccount;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class TransactionVSContentProvider extends ContentProvider {

    public static final String TAG = TransactionVSContentProvider.class.getSimpleName();


    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME       = "voting_system_transactionVS.db";
    private static final String TABLE_NAME    = "transactionVS";
    public static final String AUTHORITY      = "votingsystem.org.transactionVS";

    public static final String ID_COL                    = "_id";
    public static final String URL_COL                   = "url";
    public static final String FROM_USER_COL             = "fromUserNif";
    public static final String TO_USER_COL               = "toUserNif";
    public static final String TYPE_COL                  = "type";
    public static final String SUBJECT_COL               = "subject";
    public static final String AMOUNT_COL                = "amount";
    public static final String CURRENCY_COL              = "currency";
    public static final String WEEK_LAPSE_COL            = "weekLapse";
    public static final String SERIALIZED_OBJECT_COL     = "serializedObject";
    public static final String TIMESTAMP_TRANSACTION_COL = "timestampTransaction";
    public static final String TIMESTAMP_CREATED_COL     = "timestampCreated";
    public static final String TIMESTAMP_UPDATED_COL     = "timestampUpdated";
    public static final String DEFAULT_SORT_ORDER        = ID_COL + " DESC";

    private SQLiteDatabase database;

    private static final int ALL_ITEMS     = 1;
    private static final int SPECIFIC_ITEM = 2;

    private static final String BASE_PATH = "transactionVS";


    private static final UriMatcher URI_MATCHER;
    static{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, ALL_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", SPECIFIC_ITEM);
    }

    // Here's the public URI used to query for transactionVS items.
    public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH);

    public static Uri getTransactionVSURI(Long transactionVSId) {
        return Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH + "/" + transactionVSId);
    }

    @Override public boolean onCreate() {
        //Delete previous session database
        //getContext().deleteDatabase(DB_NAME);
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
                return "vnd.android.cursor.dir/transactionVS"; // List of items.
            case SPECIFIC_ITEM:
                return "vnd.android.cursor.item/transactionVS"; // Specific item.
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // NOTE Argument checking code omitted. Check your parameters!
        values.put(ReceiptContentProvider.TIMESTAMP_UPDATED_COL, System.currentTimeMillis());
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)){
            case ALL_ITEMS:
                updateCount = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case SPECIFIC_ITEM:
                updateCount = database.update(TABLE_NAME, values, ID_COL +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        // Notify any listeners and return the updated row count.
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    @Override public Uri insert(Uri requestUri, ContentValues values) {
        // NOTE Argument checking code omitted. Check your parameters! Check that
        // your row addition request succeeded!
        long rowId = -1;
        Uri newUri = null;
        if(values != null) {
            values.put(ReceiptContentProvider.TIMESTAMP_CREATED_COL, System.currentTimeMillis());
            values.put(ReceiptContentProvider.TIMESTAMP_UPDATED_COL, System.currentTimeMillis());
            //identifies the primary key and finds a matching row to update, inserting a new one if none is found.
            rowId = database.replace(TABLE_NAME, null, values);
            newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        }
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

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                ID_COL                    + " INTEGER PRIMARY KEY, " +
                URL_COL                   + " TEXT," +
                FROM_USER_COL             + " TEXT," +
                TO_USER_COL               + " TEXT," +
                SUBJECT_COL               + " TEXT," +
                AMOUNT_COL                + " TEXT," +
                CURRENCY_COL              + " TEXT," +
                TYPE_COL                  + " TEXT," +
                WEEK_LAPSE_COL            + " TEXT," +
                SERIALIZED_OBJECT_COL     + " blob, " +
                TIMESTAMP_TRANSACTION_COL + " INTEGER DEFAULT 0, " +
                TIMESTAMP_UPDATED_COL     + " INTEGER DEFAULT 0, " +
                TIMESTAMP_CREATED_COL     + " INTEGER DEFAULT 0);";


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

    public static List<String> getTransactionWeekList(AppContextVS contextVS) {
        List<String> result = new ArrayList<String>();
        Cursor cursor = contextVS.getContentResolver().query(
                TransactionVSContentProvider.CONTENT_URI, null, null, null, null);
        if(cursor.getCount() == 0) return result;
        cursor.moveToFirst(); //ORDER -> ID_COL DESC
        int lastId =  cursor.getInt(
                cursor.getColumnIndex(TransactionVSContentProvider.ID_COL));
        Calendar lastTransactionCalendar = Calendar.getInstance();
        Long lastTransactionMillis = cursor.getLong(
                cursor.getColumnIndex(TransactionVSContentProvider.TIMESTAMP_CREATED_COL));
        lastTransactionCalendar.setTimeInMillis(lastTransactionMillis);

        cursor.moveToLast();
        int firstId =  cursor.getInt(
                cursor.getColumnIndex(TransactionVSContentProvider.ID_COL));
        Long firstTransactionMillis = cursor.getLong(
                cursor.getColumnIndex(TransactionVSContentProvider.TIMESTAMP_CREATED_COL));
        Calendar firstTransactionCalendar = Calendar.getInstance();
        firstTransactionCalendar.setTimeInMillis(firstTransactionMillis);

        int numWeeks = firstTransactionCalendar.get(Calendar.WEEK_OF_YEAR) -
                lastTransactionCalendar.get(Calendar.WEEK_OF_YEAR);
        Log.d(TAG + ".getTransactionWeekList() ", "numWeeks: " + numWeeks +
                " - firstTransactionDate: " + firstTransactionCalendar.getTime() +
                " - lastTransactionDate: " + lastTransactionCalendar.getTime());
        Calendar iteratorCalendar = (Calendar) lastTransactionCalendar.clone();
        while(iteratorCalendar.before(lastTransactionCalendar)) {
            String lapseWeekLbl = contextVS.getLapseWeekLbl(iteratorCalendar);
            result.add(lapseWeekLbl);
            Log.d(TAG + ".getTransactionWeekList() ", "lapseWeekLbl: " + lapseWeekLbl);
        }
        return result;
    }


    public static void setVicketAccount(AppContextVS contextVS, VicketAccount updatedVicketAccount) {
        Set<CurrencyVS> keySet = updatedVicketAccount.getCurrencyMap().keySet();
        for(CurrencyVS currencyVS : keySet) {
            CurrencyData currencyData = updatedVicketAccount.getCurrencyMap().get(currencyVS);
            for(TransactionVS transactionVS : currencyData.getTransactionList()) {
                addTransaction(contextVS, transactionVS,
                        DateUtils.getDirPath(updatedVicketAccount.getWeekLapse()));
            }
            currencyData.setTransactionList(null);
        }
        contextVS.updateVicketAccountLastChecked();
    }

    public static Uri addTransaction(AppContextVS contextVS, TransactionVS transactionVS,
              String weekLapse) {
        ContentValues values = populateTransactionContentValues(transactionVS);
        values.put(TransactionVSContentProvider.WEEK_LAPSE_COL, weekLapse);
        Uri uri = contextVS.getContentResolver().insert(TransactionVSContentProvider.CONTENT_URI, values);
        Log.d(TAG + ".addTransaction() ", "added uri: " + uri.toString());
        return uri;
    }

    public static int updateTransaction(AppContextVS contextVS, TransactionVS transactionVS) {
        ContentValues values = populateTransactionContentValues(transactionVS);
        return contextVS.getContentResolver().update(TransactionVSContentProvider.
                getTransactionVSURI(transactionVS.getLocalId()), values, null, null);
    }

    private static ContentValues populateTransactionContentValues(TransactionVS transactionVS) {
        ContentValues values = new ContentValues();
        values.put(TransactionVSContentProvider.ID_COL, transactionVS.getId());
        values.put(TransactionVSContentProvider.URL_COL, transactionVS.getMessageSMIMEURL());
        if(transactionVS.getFromUserVS() != null) values.put(
                TransactionVSContentProvider.FROM_USER_COL, transactionVS.getFromUserVS().getNif());
        if(transactionVS.getToUserVS() != null) values.put(
                TransactionVSContentProvider.TO_USER_COL, transactionVS.getToUserVS().getNif());
        values.put(TransactionVSContentProvider.SUBJECT_COL, transactionVS.getSubject());
        values.put(TransactionVSContentProvider.AMOUNT_COL, transactionVS.getAmount().toPlainString());
        values.put(TransactionVSContentProvider.CURRENCY_COL, transactionVS.getCurrencyVS().toString());
        values.put(TransactionVSContentProvider.TYPE_COL, transactionVS.getType().toString());
        values.put(TransactionVSContentProvider.SERIALIZED_OBJECT_COL,
                ObjectUtils.serializeObject(transactionVS));
        values.put(TransactionVSContentProvider.TIMESTAMP_TRANSACTION_COL,
                transactionVS.getDateCreated().getTime());
        return values;
    }
}
