package com.leiflundgren.locationlogger.dbtables;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class MyDbHelper extends SQLiteOpenHelper {

	public interface TableInfo {
		String getTableName();
		String getCreateSql();
	}

	public static final int DATABASE_VERSION = 6;
	public static final String DATABASE_NAME = "loclogger-" + DATABASE_VERSION + ".db";

	private List<TableInfo> tables = new LinkedList<MyDbHelper.TableInfo>();
	private SQLiteDatabase database;
	
	public SQLiteDatabase getDatabase() {
		return database;
	}

	public MyDbHelper(Context context, CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
		this.database = getWritableDatabase();
	}
	
	public void close() {
		if ( this.database != null ) {
			this.database.close();
			this.database = null;
		}
	}
		
	public boolean doesTableExists(String tableName) {
	    Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) {
	    	try {
	    		return cursor.getCount()>0;
	    	}
	    	finally {
	    		cursor.close();
	        }
	    }
	    return false;
	}

	public void addTable(TableInfo table) {
		tables.add(table);
		
		if ( !doesTableExists(table.getTableName()))
			createTable(database, table);
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v("MyDbHelper","creating db " + DATABASE_NAME);
		
		for ( TableInfo table : tables ) {
			createTable(db, table);
		}
	}

	private void createTable(SQLiteDatabase db, TableInfo table) {
		Log.v("MyDbHelper", "Creating table " + table.getTableName());
		db.execSQL(table.getCreateSql());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(this.getClass().getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		for ( TableInfo table : tables ) {
			db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
		}
		onCreate(db);	
	}


}
