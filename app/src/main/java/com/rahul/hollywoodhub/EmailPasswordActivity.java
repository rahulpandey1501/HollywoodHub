package com.rahul.hollywoodhub;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailPasswordActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private Button signInButton, signUpButton;
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);
        mAuth = FirebaseAuth.getInstance();
        bindViews();
        setListeners();
    }

    private void setListeners() {
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    private void bindViews() {
        signInButton = (Button) findViewById(R.id.signin_button);
        signUpButton = (Button) findViewById(R.id.signup_button);
        usernameEditText = (EditText) findViewById(R.id.user_name_edittext);
        passwordEditText= (EditText) findViewById(R.id.password_edittext);
    }

    private void createUserWithEmailAndPassword(String email, String password) {
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            hideProgressDialog();
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithEmailAndPassword(String email, String password) {
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            hideProgressDialog();
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin_button:
                if (TextUtils.isEmpty(usernameEditText.getText()) || TextUtils.isEmpty(passwordEditText.getText())) {
                    Toast.makeText(getApplicationContext(), "Please enter username/password", Toast.LENGTH_SHORT).show();
                    return;
                }
                signInWithEmailAndPassword(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                break;

            case R.id.signup_button:
                if (TextUtils.isEmpty(usernameEditText.getText()) || TextUtils.isEmpty(passwordEditText.getText())) {
                    Toast.makeText(getApplicationContext(), "Please enter username/password", Toast.LENGTH_SHORT).show();
                    return;
                }
                createUserWithEmailAndPassword(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                break;
        }
    }
}
