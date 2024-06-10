package com.example.memomate.notes;

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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NoteDetails extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private EditText editTextSharingKey;
    private Button buttonSave;
    private Button buttonCancel;
    private FirebaseFirestore db;
    private Note currentNote;

    private NoteMode mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        Toolbar myToolbar = findViewById(R.id.notesListToolbar);
        myToolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(myToolbar);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        editTextSharingKey = findViewById(R.id.editTextSharingKey);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String noteId = intent.getStringExtra("noteId");
        mode = (NoteMode) intent.getSerializableExtra("mode");

        if (mode == NoteMode.EDIT) {
            this.fetchDocumentById(noteId);
        }

        buttonSave.setOnClickListener(v -> {
            if (mode == NoteMode.CREATE) {
                createNote();
            } else if (mode == NoteMode.EDIT) {
                saveNote();
            }
        });

        buttonCancel.setOnClickListener(v -> {
            Intent intent1 = new Intent(NoteDetails.this, NotesList.class);
            startActivity(intent1);
            finish();
        });
    }

    private void createNote() {

        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String sharingKey = editTextSharingKey.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please enter both title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        Note newnote = new Note();
        newnote.setTitle(title);
        newnote.setContent(content);
        newnote.setCreatedDate(Timestamp.now());
        newnote.setCreatedBy(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        if (sharingKey.isEmpty()) {
            newnote.setSharingKey(null);
        } else {
            newnote.setSharingKey(sharingKey);
        }

        db.collection("notes")
                .add(newnote)
                .addOnSuccessListener(documentReference -> {
                    String newId = documentReference.getId();
                    Toast.makeText(this, "Note created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, NotesList.class);
                    intent.putExtra("newId", newId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating note", Toast.LENGTH_SHORT).show());
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String sharingKey = editTextSharingKey.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please enter both title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("content", content);
        if (!sharingKey.isEmpty()) {
            updates.put("sharingKey", sharingKey);
        }

        db.collection("notes").document(this.currentNote.getId()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, NotesList.class);
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
                    editTextSharingKey.setText(currentNote.getSharingKey());
                } else {
                    Toast.makeText(NoteDetails.this, "No such document", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NoteDetails.this, "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}