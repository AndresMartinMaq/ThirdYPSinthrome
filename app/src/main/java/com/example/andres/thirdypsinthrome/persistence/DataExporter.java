package com.example.andres.thirdypsinthrome.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Takes care of exporting data in a XML format to a file.
 * Based on code by stackoverflow user "Elenasys",
 * from http://stackoverflow.com/questions/2589184/easiest-way-to-export-longitude-and-latitude-data-stored-in-a-sqlite-database-to/
 */
public class DataExporter {

    private static final String EXPORT_FILE_NAME = "sinthromeDataExport.xml";
    private String filePath;

    private Context context;
    private SQLiteDatabase db;
    private Exporter exporter;

    public DataExporter(Context ctx) {
        context = ctx;
        db = DBHelper.getInstance(ctx).getWritableDatabase();
        //filePath = ctx.getApplicationInfo().dataDir +"/"+EXPORT_FILE_NAME;
        //filePath = Environment.getExternalStorageDirectory().getPath() +"/"+EXPORT_FILE_NAME;
        filePath = "/sdcard" +"/"+EXPORT_FILE_NAME;
        log("File Path: "+filePath);

        try {
            //Create a file on the sdcard to export the database contents to
            File myFile = new File( filePath );
            myFile.createNewFile();

            FileOutputStream fOut =  new FileOutputStream(myFile);
            BufferedOutputStream bos = new BufferedOutputStream( fOut );

            exporter = new Exporter( bos );
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportData() {
        log( "Exporting Data" );

        try {
            exporter.startDbExport(db.getPath());

            // get the tables out of the given sqlite database
            Cursor cur = db.rawQuery( "SELECT * FROM sqlite_master", null);
            log("Sqlite_master tables, cursor size = " + cur.getCount());
            cur.moveToFirst();

            String tableName;
            while ( cur.getPosition() < cur.getCount() ) {
                tableName = cur.getString( cur.getColumnIndex( "name" ) );
                log( "table name " + tableName );

                // don't process these two tables since they are used for metadata
                if ( ! tableName.equals( "android_metadata" ) && ! tableName.equals( "sqlite_sequence" ) && !tableName.contains("autoindex")) {
                    exportTable( tableName );
                }

                cur.moveToNext();
            }
            exporter.endDbExport();
            exporter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportTable( String tableName ) throws IOException {
        exporter.startTable(tableName);

        // get everything from the table
        String sql = "select * from " + tableName;
        Cursor cur = db.rawQuery( sql, new String[0] );
        int numcols = cur.getColumnCount();

        log( "Start exporting table " + tableName );

        cur.moveToFirst();

        // move through the table, creating rows and adding each column with name and value to the row
        while( cur.getPosition() < cur.getCount() ) {
            exporter.startRow();
            String name;
            String val;
            for( int idx = 0; idx < numcols; idx++ )
            {
                name = cur.getColumnName(idx);
                val = cur.getString( idx );
                log( "col '" + name + "' -- val '" + val + "'" );

                exporter.addColumn(name, val);
            }

            exporter.endRow();
            cur.moveToNext();
        }
        cur.close();

        exporter.endTable();
    }

    private void log( String msg ) {
        Log.d( "DatabaseAssistant", msg );
    }

    class Exporter {
        private static final String CLOSING_WITH_TICK = "'>";
        private static final String START_DB = "<export-database name='";
        private static final String END_DB = "</export-database>";
        private static final String START_TABLE = "<table name='";
        private static final String END_TABLE = "</table>";
        private static final String START_ROW = "<row>";
        private static final String END_ROW = "</row>";
        private static final String START_COL = "<col name='";
        private static final String END_COL = "</col>";

        private BufferedOutputStream bos;

        public Exporter() throws FileNotFoundException {
            this( new BufferedOutputStream(
                    context.openFileOutput( filePath,
                            Context.MODE_WORLD_READABLE ) ) );
        }

        public Exporter( BufferedOutputStream bos ) {
            this.bos = bos;
        }

        public void close() throws IOException {
            if ( bos != null ) {
                bos.close();
            }
        }

        public void startDbExport( String dbName ) throws IOException {
            String stg = START_DB + dbName + CLOSING_WITH_TICK;
            bos.write(stg.getBytes());
        }

        public void endDbExport() throws IOException {
            bos.write(END_DB.getBytes());
        }

        public void startTable( String tableName ) throws IOException {
            String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
            bos.write(stg.getBytes());
        }

        public void endTable() throws IOException {
            bos.write(END_TABLE.getBytes());
        }

        public void startRow() throws IOException {
            bos.write(START_ROW.getBytes());
        }

        public void endRow() throws IOException {
            bos.write(END_ROW.getBytes());
        }

        public void addColumn( String name, String val ) throws IOException {
            String stg = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
            bos.write(stg.getBytes());
        }
    }

}