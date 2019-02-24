package net.rakusei.robot.felio.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;

import net.rakusei.robot.felio.R;
import net.rakusei.robot.felio.task.LoginTask;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.login).setOnClickListener((v)->{
            Log.d("LoginActivity","Clicked!");
            String email = ((EditText)findViewById(R.id.username)).getText().toString();
            String password = ((EditText)findViewById(R.id.password)).getText().toString();
            new LoginTask(LoginActivity.this).execute(email,password);
        });
    }
}
