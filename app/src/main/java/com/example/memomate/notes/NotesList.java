package com.example.memomate.notes;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memomate.R;
import com.example.memomate.auth.Login;
import com.example.memomate.share.SharedNotesList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesList extends AppCompatActivity implements OnItemClickListener {

    private FirebaseFirestore db;
    private final List<Note> notes = new ArrayList<>();
    private NotesAdapter adapter;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_list);

        Toolbar myToolbar = findViewById(R.id.notesListToolbar);
        myToolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(myToolbar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userEmail = mAuth.getCurrentUser().getEmail();

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(notes, this);
        recyclerView.setAdapter(adapter);
        getNotes();

        Button createNoteButton = findViewById(R.id.createNoteButton);
        createNoteButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(this, NoteDetails.class);
            newIntent.putExtra("mode", NoteMode.CREATE);
            startActivity(newIntent);
            finish();
        });
    }

    private void getNotes() {
        db.collection("notes")
                .whereEqualTo("createdBy", userEmail)
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
        Intent intent = new Intent(this, NoteDetails.class);
        intent.putExtra("noteId", notes.get(position).getId());
        intent.putExtra("mode", NoteMode.EDIT);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Create note");
        menu.add("Shared with me");
        menu.add("Logout");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Create note")) {
            Intent intent = new Intent(this, NoteDetails.class);
            intent.putExtra("mode", NoteMode.CREATE);
            startActivity(intent);
            finish();
        } else if (item.getTitle().equals("Shared with me")) {
            Intent intent = new Intent(this, SharedNotesList.class);
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