package by.ilyatr.didpoolfit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class LoginActivity extends AppCompatActivity {
  static final String EXTRA_EMAIL = "login_email";
  TextView register;
  TextView forgetLogin;
  Button login;
  TextInputLayout emailLayout;
  TextInputLayout passwordLayout;
  UserStorage storage;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    emailLayout = findViewById(R.id.emailLayout);
    passwordLayout = findViewById(R.id.passwordLayout);
    if (savedInstanceState == null) {
      emailLayout.getEditText().setText(getIntent().getStringExtra(EXTRA_EMAIL));
    }


    forgetLogin = findViewById(R.id.forgetPassword);
    register = findViewById(R.id.registerTextView);

    login = findViewById(R.id.loginButton);

    register.setOnClickListener(v ->{
      Intent registerIntent = new Intent(this, RegisterActivity.class);
      registerIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(registerIntent);
    });
    forgetLogin.setOnClickListener(v -> {
      Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    });
    clearErrorOnInput(emailLayout);
    clearErrorOnInput(passwordLayout);
    storage = new UserStorage(this);
    login.setOnClickListener(v -> {
      if(validate()){
        String email = getText(emailLayout);
        String password = String.valueOf(passwordLayout.getEditText().getText());
        login.setEnabled(false);
        UserStorage.WRITES.execute(() -> {
          try {
            boolean success = storage.login(email, password);
            runOnUiThread(() -> {
              if (isFinishing() || isDestroyed()) return;
              login.setEnabled(true);
              if (success) {
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
                finish();
              } else {
                passwordLayout.setError(getString(R.string.error_credentials));
              }
            });
          } catch (UserStorage.StorageException e) {
            runOnUiThread(() -> {
              if (isFinishing() || isDestroyed()) return;
              login.setEnabled(true);
              Toast.makeText(this, R.string.error_storage, Toast.LENGTH_LONG).show();
            });
          }
        });
      }
    });
  }

  private boolean validate() {
    boolean valid = true;
    // 1. Пустые поля
    valid &= checkNotEmpty(emailLayout);
    valid &= checkNotEmpty(passwordLayout);
    String email = getText(emailLayout);
    if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailLayout.setError(getString(R.string.error_email));
      valid = false;
    }

    return valid;
  }


  private void clearErrorOnInput(TextInputLayout layout) {
    layout.getEditText().addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {
        layout.setError(null);
      }
    });
  }

  private String getText(TextInputLayout layout) {
    String text = String.valueOf(layout.getEditText().getText());
    return text.trim();
  }

  private boolean checkNotEmpty(TextInputLayout layout) {
    if (getText(layout).isEmpty()) {
      layout.setError(getString(R.string.error_empty));
      return false;
    }
    return true;
  }

}
