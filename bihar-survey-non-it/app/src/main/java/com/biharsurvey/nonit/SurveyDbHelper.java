package com.biharsurvey.nonit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class SurveyDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="bihar_survey.db";
    private static final int DB_VERSION=2;
    public static final String TABLE="entries";
    public SurveyDbHelper(Context c){super(c,DB_NAME,null,DB_VERSION);}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE entries ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "category TEXT NOT NULL,"+
                "unique_id TEXT NOT NULL,"+
                "office TEXT,"+
                "section TEXT,"+
                "surveyor TEXT,"+
                "item_type TEXT,"+
                "photo_path TEXT,"+
                "latitude REAL,"+
                "longitude REAL,"+
                "entry_time INTEGER,"+
                "status TEXT DEFAULT 'SAVED',"+
                "payload TEXT)");
        db.execSQL("CREATE INDEX idx_entries_category ON entries(category)");
        db.execSQL("CREATE INDEX idx_entries_time ON entries(entry_time)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        if(oldVersion<2) db.execSQL("ALTER TABLE entries ADD COLUMN payload TEXT");
    }
    public long insertFull(String category,String uniqueId,String office,String section,String surveyor,String itemType,
                           String photoPath,double lat,double lon,long time,String status,String payload){
        ContentValues v=new ContentValues();
        v.put("category",category);v.put("unique_id",uniqueId);v.put("office",office);v.put("section",section);
        v.put("surveyor",surveyor);v.put("item_type",itemType);v.put("photo_path",photoPath);
        v.put("latitude",lat);v.put("longitude",lon);v.put("entry_time",time);v.put("status",status);v.put("payload",payload);
        return getWritableDatabase().insert(TABLE,null,v);
    }
    public ArrayList<EntryRow> all(){
        ArrayList<EntryRow> out=new ArrayList<>();
        Cursor c=getReadableDatabase().query(TABLE,null,null,null,null,null,"id DESC");
        try{while(c.moveToNext()){
            EntryRow e=new EntryRow();
            e.id=c.getLong(c.getColumnIndexOrThrow("id"));e.category=c.getString(c.getColumnIndexOrThrow("category"));
            e.uniqueId=c.getString(c.getColumnIndexOrThrow("unique_id"));e.office=c.getString(c.getColumnIndexOrThrow("office"));
            e.section=c.getString(c.getColumnIndexOrThrow("section"));e.surveyor=c.getString(c.getColumnIndexOrThrow("surveyor"));
            e.itemType=c.getString(c.getColumnIndexOrThrow("item_type"));e.photoPath=c.getString(c.getColumnIndexOrThrow("photo_path"));
            e.lat=c.getDouble(c.getColumnIndexOrThrow("latitude"));e.lon=c.getDouble(c.getColumnIndexOrThrow("longitude"));
            e.time=c.getLong(c.getColumnIndexOrThrow("entry_time"));e.status=c.getString(c.getColumnIndexOrThrow("status"));
            e.payload=c.getString(c.getColumnIndexOrThrow("payload"));out.add(e);
        }}finally{c.close();}return out;
    }
    public void delete(long id){getWritableDatabase().delete(TABLE,"id=?",new String[]{String.valueOf(id)});}
    public int count(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+TABLE,null);try{return c.moveToFirst()?c.getInt(0):0;}finally{c.close();}}
    public static class EntryRow{
        public long id,time;public String category,uniqueId,office,section,surveyor,itemType,photoPath,status,payload;public double lat,lon;
    }
}