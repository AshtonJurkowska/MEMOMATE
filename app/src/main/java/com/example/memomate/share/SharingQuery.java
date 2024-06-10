package com.example.memomate.share;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.memomate.R;

public class SharingQuery extends AppCompatActivity {
    EditText emailEditText;
    EditText sharingKeyEditText;
    Button changeSharingQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sharing_query);

        emailEditText = findViewById(R.id.editTextSharingEmail);
        sharingKeyEditText = findViewById(R.id.editTextSharingSharingKey);
        changeSharingQuery = findViewById(R.id.buttonChangeSharingQuery);

        Intent intent = getIntent();
        if (intent.getStringExtra("sharingEmail") != null) {
            String sharingEmail = intent.getStringExtra("sharingEmail");
            emailEditText.setText(sharingEmail);
        }
        if (intent.getStringExtra("sharingKey") != null) {
            String sharingKey = intent.getStringExtra("sharingKey");
            sharingKeyEditText.setText(sharingKey);
        }


        changeSharingQuery.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String sharingKey = sharingKeyEditText.getText().toString();

            Intent newIntent = new Intent(SharingQuery.this, SharedNotesList.class);
            newIntent.putExtra("sharingEmail", email);
            newIntent.putExtra("sharingKey", sharingKey);
            startActivity(newIntent);
            finish();
        });
    }
}