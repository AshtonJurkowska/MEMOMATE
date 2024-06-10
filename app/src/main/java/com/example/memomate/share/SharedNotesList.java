package com.example.memomate.share;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memomate.R;
import com.example.memomate.auth.Login;
import com.example.memomate.notes.Note;
import com.example.memomate.notes.NotesAdapter;
import com.example.memomate.notes.NotesList;
import com.example.memomate.notes.OnItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SharedNotesList extends AppCompatActivity implements OnItemClickListener {

    private FirebaseFirestore db;
    private final List<Note> notes = new ArrayList<>();
    private NotesAdapter adapter;
    private String sharingEmail;
    private String sharingKey;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_notes_list);

        Toolbar myToolbar = findViewById(R.id.sharedNotesListToolbar);
        myToolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(myToolbar);

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(notes, this);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        boolean dataChanged = false;
        if (intent.getStringExtra("sharingEmail") != null) {
            sharingEmail = intent.getStringExtra("sharingEmail");
            dataChanged = true;
        }
        if (intent.getStringExtra("sharingKey") != null) {
            sharingKey = intent.getStringExtra("sharingKey");
            dataChanged = true;
        }
        if (dataChanged) {
            notes.clear();
            getNotes();
            adapter.notifyDataSetChanged();
        }
    }

    private void getNotes() {

        db.collection("notes")
                .whereEqualTo("createdBy", sharingEmail)
                .whereEqualTo("sharingKey", sharingKey)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Note note = document.toObject(Note.class);
                                note.setId(document.getId());
                                notes.add(note);
                            }
                            adapter.updateNotesList(notes);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, SharedNoteDetails.class);
        intent.putExtra("noteId", notes.get(position).getId());
        intent.putExtra("sharingEmail", sharingEmail);
        intent.putExtra("sharingKey", sharingKey);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("My notes");
        menu.add("Change sharing query");
        menu.add("Logout");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("My notes")) {
            Intent intent = new Intent(this, NotesList.class);
            startActivity(intent);
            finish();
        } else if (item.getTitle().equals("Change sharing query")) {
            Intent intent = new Intent(this, SharingQuery.class);
            if (sharingEmail != null && !sharingEmail.isEmpty()) {
                intent.putExtra("sharingEmail", sharingEmail);
            }
            if (sharingKey != null && !sharingKey.isEmpty()) {
                intent.putExtra("sharingKey", sharingKey);
            }
            startActivity(intent);
            finish();
        } else if (item.getTitle().equals("Logout")) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}