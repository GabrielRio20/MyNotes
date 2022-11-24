package com.example.mynotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.mynotes.database.Note;
import com.example.mynotes.database.NoteDao;
import com.example.mynotes.database.NoteRoomDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private NoteDao noteDao;
    private ExecutorService executorService;
    private ListView listView;
    private Button btnAddNote, btnEditNote, btnClearField;
    private EditText edtTitle, edtDesc, edtDate;
    private int idItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.lv_notes);
        btnAddNote = findViewById(R.id.btn_add_note);
        btnEditNote = findViewById(R.id.btn_update_note);
        btnClearField = findViewById(R.id.btn_clear_field);

        edtTitle = findViewById(R.id.edt_title);
        edtDesc = findViewById(R.id.edt_description);
        edtDate = findViewById(R.id.edt_date);

        //untuk menjalankan di background
        executorService = Executors.newSingleThreadExecutor();

        NoteRoomDatabase db = NoteRoomDatabase.getDatabase(this);
        noteDao = db.noteDao();

        btnClearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmptyField();
            }
        });

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String desc = edtDesc.getText().toString();
                String date = edtDate.getText().toString();
                insertData(new Note(title, desc, date));
                setEmptyField();
            }
        });
        getAllNotes();

        btnEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String desc = edtDesc.getText().toString();
                String date = edtDate.getText().toString();
                updateData(new Note(idItem, title, desc, date));
                setEmptyField();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note item = (Note) parent.getAdapter().getItem(position);
                idItem = item.getId();
                edtTitle.setText(item.getTitle());
                edtDesc.setText(item.getDescription());
                edtDate.setText(item.getDate());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Note item = (Note) parent.getAdapter().getItem(position);
                deleteData(item);
                return true;
            }
        });
    }

    private void setEmptyField(){
        edtTitle.setText("");
        edtDesc.setText("");
        edtDate.setText("");
    }

    //function mendapatkan semua data notes di database
    private void getAllNotes(){
        noteDao.getAllNotes().observe(this, notes -> {
            ArrayAdapter<Note> adapter = new ArrayAdapter<Note>(this,
                    android.R.layout.simple_list_item_1, notes);
            listView.setAdapter(adapter);
        });
    }

    //function insert data ke room
    private void insertData(Note note){
        executorService.execute(() -> noteDao.insert(note));
    }

    //function update data
    private void updateData(Note note){
        executorService.execute(() -> noteDao.update(note));
    }

    //function hapus data
    private void deleteData(Note note){
        executorService.execute(() -> noteDao.delete(note));
    }
}