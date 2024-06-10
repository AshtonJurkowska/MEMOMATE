package com.example.memomate.share;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.memomate.R;
import com.example.memomate.notes.Note;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SharedNoteDetails extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSave;
    private Button buttonCancel;
    private FirebaseFirestore db;
    private Note currentNote;
    private String sharingEmail;
    private String sharingKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_note_details);

        Toolbar myToolbar = findViewById(R.id.sharedNotesListToolbar);
        myToolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(myToolbar);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String noteId = intent.getStringExtra("noteId");
        sharingEmail = intent.getStringExtra("sharingEmail");
        sharingKey = intent.getStringExtra("sharingKey");
        this.fetchDocumentById(noteId);

        buttonSave.setOnClickListener(v -> saveNote());

        buttonCancel.setOnClickListener(v -> {
            Intent intent1 = new Intent(SharedNoteDetails.this, SharedNotesList.class);
            startActivity(intent1);
            finish();
        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please enter both title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("content", content);

        db.collection("notes").document(this.currentNote.getId()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, SharedNotesList.class);
                    intent.putExtra("sharingEmail", sharingEmail);
                    intent.putExtra("sharingKey", sharingKey);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Note update failed", Toast.LENGTH_SHORT).show());
    }

    private void fetchDocumentById(String documentId) {
        DocumentReference docRef = db.collection("notes").document(documentId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    currentNote = document.toObject(Note.class);
                    currentNote.setId(documentId);
                    editTextTitle.setText(currentNote.getTitle());
                    editTextContent.setText(currentNote.getContent());
                } else {
                    Toast.makeText(SharedNoteDetails.this, "No such document", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SharedNoteDetails.this, "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}