package com.example.database_gevyam_ex_3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class WorkerDetails extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {



String first_st, last_st, id_st, company_st, phone_st, active_st, active_DB;
int key;
boolean revOrder, curr_reversed, AtoZ, ZtoA, onlyActive, onlyInactive;

Spinner spin;
String [] sorts, listofFilters,selectionArgs;


AlertDialog.Builder adbFilter;
AlertDialog adFilter;

SQLiteDatabase db;
HelperDB hlp;
Cursor crsr;

ListView listofWorkers;
ArrayList<String> tbl, tbl2;
ArrayList<String[]> tblfull;
ArrayAdapter adp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        AtoZ = false;
        ZtoA = false;
        onlyActive = false;
        onlyInactive = false;

        hlp = new HelperDB(this);



        listofWorkers = (ListView) findViewById(R.id.listOfWorkers);
        listofWorkers.setOnItemClickListener(this);
        listofWorkers.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        sorts = new String[]{"First added","Last added","Last name: A-Z","Last name: Z-A"};
        spin = (Spinner) findViewById(R.id.sortSpinnerWorker);
        spin.setOnItemSelectedListener(this);
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,sorts);
        spin.setAdapter(adp);

        listofFilters = new String[]{"Active", "Inactive", "All workers"};

        adbFilter = new AlertDialog.Builder(this);
        adbFilter.setTitle("Filter Workers");
        adbFilter.setCancelable(false);
        adbFilter.setItems(listofFilters, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selection = null;
                if (i == 0){
                    selection = Worker.WORKER_ACTIVE + "=?";
                    selectionArgs = new String[]{"1"};
                    onlyActive = true;
                    onlyInactive = false;

                }
                if (i == 1){
                    selection = Worker.WORKER_ACTIVE + "=?";
                    selectionArgs = new String[]{"0"};

                    onlyActive = false;
                    onlyInactive = true;

                }
                if (i == 2){
                    selection =null;
                    selectionArgs = null;

                    onlyActive = false;
                    onlyInactive = false;

                }
                db=hlp.getReadableDatabase();
                crsr = db.query(Worker.TABLE_WORKER, null, selection, selectionArgs, null,null, null, null);
                if (AtoZ)  crsr = db.query(Worker.TABLE_WORKER, null, Worker.WORKER_ACTIVE+ "=?", selectionArgs, null,null, Worker.LAST_NAME, null);
                else if (ZtoA) crsr = db.query(Worker.TABLE_WORKER, null, Worker.WORKER_ACTIVE+ "=?", selectionArgs, null,null, Worker.LAST_NAME + " DESC", null);
                else  if (curr_reversed) revOrder = true;
                Reader(crsr);
                crsr.close();
                db.close();


            }
        });

        adbFilter.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        adFilter = adbFilter.create();

        revOrder = false;
        curr_reversed = false;



    }

    @Override
    protected void onResume() {
        super.onResume();
        spin.setSelection(0);
        db=hlp.getReadableDatabase();
        crsr = db.query(Worker.TABLE_WORKER, null, null, null, null, null, null, null);
        Reader(crsr);
        crsr.close();
        db.close();

    }

    public ArrayList<String> reverse_tbl(){
        tbl2 = new ArrayList<>();
        for (int i = tbl.size()-1; i>=0; i--){
            tbl2.add(tbl.get(i));
        }
        return tbl2;
    }

    public void Reader(Cursor cursr){
        db=hlp.getWritableDatabase();
        tbl = new ArrayList<>();
        tblfull = new ArrayList<>();
        int col_key = cursr.getColumnIndex(Worker.KEY_NUMBER);
        int col_first = cursr.getColumnIndex(Worker.FIRST_NAME);
        int col_last = cursr.getColumnIndex(Worker.LAST_NAME);
        int col_id = cursr.getColumnIndex(Worker.ID_NUMBER);
        int col_company = cursr.getColumnIndex(Worker.WORK_COMPANY);
        int col_phone = cursr.getColumnIndex(Worker.PHONE_NUMBER);
        int col_active = cursr.getColumnIndex(Worker.WORKER_ACTIVE);
        cursr.moveToFirst();
        while (!cursr.isAfterLast()){
            key = cursr.getInt(col_key);
            first_st = cursr.getString(col_first);
            last_st = cursr.getString(col_last);
            id_st = cursr.getString(col_id);
            company_st = cursr.getString(col_company);
            phone_st = cursr.getString(col_phone);
            active_DB = cursr.getString(col_active);
            if (active_DB.equals("0")) active_st = "INACTIVE";
            else active_st = "ACTIVE";
            String tmp = "" + key + ": " + first_st + ", " + last_st + ", " + company_st + ", " + active_st;
            String[] tmp2 = new String[]{String.valueOf(key), first_st, last_st, id_st, company_st, phone_st, active_st} ;
            tbl.add(tmp);
            tblfull.add(tmp2);
            cursr.moveToNext();
        }
        cursr.close();
        db.close();

        if (revOrder) tbl = reverse_tbl();
        adp = new ArrayAdapter<String>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, tbl);
        listofWorkers.setAdapter(adp);
        revOrder = false;



    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String[] workerDetails;
        if (!curr_reversed) workerDetails = tblfull.get(i);
        else  workerDetails = tblfull.get(tblfull.size()-1-i);

        Intent si = new Intent(this, WorkerEdit.class);
        si.putExtra("KEY", workerDetails[0]);
        si.putExtra("First", workerDetails[1]);
        si.putExtra("Last", workerDetails[2]);
        si.putExtra("ID", workerDetails[3]);
        si.putExtra("Company", workerDetails[4]);
        si.putExtra("Phone", workerDetails[5]);
        si.putExtra("ActiveNum", workerDetails[6]);
        startActivity(si);




    }


    public void filterSelect(View view){
        adFilter.show();
    }

    public void goBack(View view){
        finish();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String[] columns = null;
        String groupBy  = null;
        String having  = null;
        String orderBy = null;
        String limit  = null;

        if (i==0){
            AtoZ = false;
            ZtoA = false;
            curr_reversed = false;
        }

        else if (i==1){
            revOrder = true;
            curr_reversed = true;
            AtoZ = false;
            ZtoA = false;

        }

        else if (i==2){
            orderBy = Worker.LAST_NAME;
            AtoZ = true;
            ZtoA = false;

        }
        else if (i==3){
            orderBy = Worker.LAST_NAME + " DESC";
            ZtoA = true;
            AtoZ = false;
        }
        db=hlp.getReadableDatabase();
        if (onlyActive || onlyInactive) crsr = db.query(Worker.TABLE_WORKER, columns, Worker.WORKER_ACTIVE+ "=?", selectionArgs, groupBy, having, orderBy, limit);
        else crsr = db.query(Worker.TABLE_WORKER, columns, null, null, groupBy, having, orderBy, limit);
        Reader(crsr);
        crsr.close();
        db.close();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}