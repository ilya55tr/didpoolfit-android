package by.ilyatr.didpoolfit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    // Только цифры и спецсимволы (ASCII-пунктуация), ровно 8 символов
    static final String PASSWORD_REGEX = "^[0-9\\p{Punct}]{8}$";
    // +7 и 10 цифр номера
    static final int PHONE_DIGITS = 11;

    TextInputEditText phoneInput;
    TextInputLayout nameLayout;
    TextInputLayout phoneLayout;
    TextInputLayout emailLayout;
    TextInputLayout passwordLayout;
    TextInputLayout repeatPasswordLayout;
    MaterialCheckBox termsCheck;
    Button registerButton;
    TextView loginTextView;
    SharedPreferences userData;
    SharedPreferences.Editor userEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        phoneInput = findViewById(R.id.phoneInput);
        new Thread(() -> {
            PhoneNumberFormattingTextWatcher watcher = new PhoneNumberFormattingTextWatcher("RU");
            runOnUiThread(() -> phoneInput.addTextChangedListener(watcher));
        }).start();
        phoneInput.setOnFocusChangeListener((v, hasFocus) -> {
            String text = phoneInput.getText().toString();
            if (hasFocus && text.isEmpty()) {
                phoneInput.setText("+7");
                phoneInput.setSelection(phoneInput.getText().length());
            } else if (!hasFocus && text.replaceAll("\\D", "").equals("7")) {
                phoneInput.setText("");
            }
            if (!hasFocus && !getText(phoneLayout).isEmpty()) {
                validateInput(phoneLayout, true);
            }
        });

        nameLayout = findViewById(R.id.nameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        repeatPasswordLayout = findViewById(R.id.repeatPasswordLayout);
        termsCheck = findViewById(R.id.termsCheck);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        userData = getSharedPreferences("user", MODE_PRIVATE);
        userEdit = userData.edit();

        clearErrorOnInput(nameLayout);
        clearErrorOnInput(phoneLayout);
        clearErrorOnInput(emailLayout);
        clearErrorOnInput(passwordLayout);
        clearErrorOnInput(repeatPasswordLayout);
        termsCheck.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) termsCheck.setErrorShown(false);
            updateRegistrationSteps();
        });

        // У телефона уже есть обработчик фокуса для префикса +7.
        for (TextInputLayout layout : new TextInputLayout[]{
                nameLayout, emailLayout, passwordLayout, repeatPasswordLayout}) {
            layout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && !getText(layout).isEmpty()) {
                    validateInput(layout, true);
                }
            });
        }
        updateRegistrationSteps();
        registerButton.setOnClickListener(v -> {
            if (validate()) {
                userEdit.putString("fullName", getText(nameLayout));
                userEdit.putString("phoneNumber", getText(phoneLayout));
                userEdit.putString("email", getText(emailLayout));
                userEdit.putString("password", getText(passwordLayout));
                userEdit.apply();
                startActivity(new Intent(this, RegisterSecondActivity.class));
            }
        });

        loginTextView.setOnClickListener(v -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        });
        loginTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private boolean validate() {
        boolean valid = true;
        for (TextInputLayout layout : new TextInputLayout[]{
                nameLayout, phoneLayout, emailLayout, passwordLayout, repeatPasswordLayout}) {
            valid &= validateInput(layout, true);
        }

        // Соглашение
        if (!termsCheck.isChecked()) {
            termsCheck.setErrorShown(true);
            termsCheck.setErrorAccessibilityLabel(getString(R.string.error_terms));
            valid = false;
        }

        return valid;
    }

    // Одни правила используются и для открытия следующего шага, и перед сохранением.
    private boolean validateInput(TextInputLayout layout, boolean showError) {
        String text = getText(layout);
        int error = 0;
        if (text.isEmpty()) {
            error = R.string.error_empty;
        } else if (layout == phoneLayout) {
            String digits = text.replaceAll("\\D", "");
            if (digits.length() <= 1) {
                error = R.string.error_empty;
            } else if (digits.length() != PHONE_DIGITS) {
                error = R.string.error_phone;
            } else if (userData.getBoolean("registered", false)
                    && digits.equals(userData.getString("phoneNumber", "").replaceAll("\\D", ""))) {
                error = R.string.error_phone_exists;
            }
        } else if (layout == emailLayout) {
            if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                error = R.string.error_email;
            } else if (userData.getBoolean("registered", false)
                    && text.equalsIgnoreCase(userData.getString("email", ""))) {
                error = R.string.error_email_exists;
            }
        } else if (layout == passwordLayout && !text.matches(PASSWORD_REGEX)) {
            error = R.string.error_password;
        } else if (layout == repeatPasswordLayout && !text.equals(getText(passwordLayout))) {
            error = R.string.error_repeat_password;
        }
        if (showError) {
            layout.setError(error == 0 ? null : getString(error));
        }
        return error == 0;
    }

    private void updateRegistrationSteps() {
        boolean previousValid = true;
        for (TextInputLayout layout : new TextInputLayout[]{
                nameLayout, phoneLayout, emailLayout, passwordLayout, repeatPasswordLayout}) {
            // Не вызываем setEnabled повторно: это может мешать фокусу при вводе.
            if (layout.isEnabled() != previousValid) {
                layout.setEnabled(previousValid);
            }
            if (!previousValid) layout.setError(null);
            previousValid = previousValid && validateInput(layout, false);
        }
        termsCheck.setEnabled(previousValid);
        registerButton.setEnabled(previousValid && termsCheck.isChecked());
        registerButton.setAlpha(registerButton.isEnabled() ? 1f : 0.5f);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateRegistrationSteps();
    }

    // Пароли не обрезаем: пробел — тоже символ пароля
    private String getText(TextInputLayout layout) {
        String text = String.valueOf(layout.getEditText().getText());
        boolean isPassword = layout == passwordLayout || layout == repeatPasswordLayout;
        return isPassword ? text : text.trim();
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
                updateRegistrationSteps();
            }
        });
    }
}
