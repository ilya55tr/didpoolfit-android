package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterSecondActivity extends AppCompatActivity {

  static final float MIN_WEIGHT = 20;
  static final float MAX_WEIGHT = 300;
  static final int MIN_HEIGHT = 50;
  static final int MAX_HEIGHT = 250;

  TextInputLayout genderLayout;

  TextInputLayout dateOfBirthLayout;
  TextInputEditText dateOfBirthInput;

  TextInputLayout weightLayout;
  TextInputEditText weightInput;

  TextInputLayout heightLayout;
  Button nextButton;
  Long birthDateMillis = null;
  // Заполняются в validate(), сохраняются по Next
  float weight;
  int height;

  SharedPreferences userData;
  SharedPreferences.Editor userEdit;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_register_second);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    genderLayout = findViewById(R.id.genderLayout);

    dateOfBirthLayout = findViewById(R.id.dateOfBirthLayout);
    dateOfBirthInput = findViewById(R.id.dateOfBirthInput);

    weightLayout = findViewById(R.id.weightLayout);
    weightInput = findViewById(R.id.weightInput);

    heightLayout = findViewById(R.id.heightLayout);
    nextButton = findViewById(R.id.nextButton);


    dateOfBirthInput.setOnClickListener(v -> {
      CalendarConstraints constraints = new CalendarConstraints.Builder()
              .setValidator(DateValidatorPointBackward.now())
              .build();

      MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
              .datePicker()
              .setTitleText("Date of Birth")
              .setCalendarConstraints(constraints)
              .setSelection(birthDateMillis)
              .build();

      picker.addOnPositiveButtonClickListener(selection -> {
        birthDateMillis = selection;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateOfBirthInput.setText(format.format(new Date(selection)));
        dateOfBirthLayout.setError(null);
      });

      picker.show(getSupportFragmentManager(), "dateOfBirth");
    });

    weightInput.setFilters(new InputFilter[]{
            (source, start, end, dest, dstart, dend) ->{
              String result = dest.subSequence(0, dstart)
                      + source.subSequence(start, end).toString()
                      + dest.subSequence(dend, dest.length());
              return result.matches("\\d{0,3}([.,]\\d?)?") ? null : "";
            }
    });

    clearErrorOnInput(genderLayout);
    clearErrorOnInput(dateOfBirthLayout);
    clearErrorOnInput(weightLayout);
    clearErrorOnInput(heightLayout);

    userData = getSharedPreferences("user", MODE_PRIVATE);
    userEdit = userData.edit();
    nextButton.setOnClickListener(v -> {
      if (validate()){
        userEdit.putString("gender", getText(genderLayout));
        userEdit.putLong("dateOfBirth", birthDateMillis);
        userEdit.putFloat("weight", weight);
        userEdit.putInt("height", height);
        userEdit.putBoolean("registered", true);
        userEdit.apply();
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
      }
    });
  }

  private boolean validate(){
    boolean valid = true;
    valid &= checkNotEmpty(genderLayout);
    valid &= checkNotEmpty(dateOfBirthLayout);
    valid &= checkNotEmpty(weightLayout);
    valid &= checkNotEmpty(heightLayout);

    // Вес: запятую меняем на точку, иначе parseFloat не поймёт "72,5"
    String weightText = getText(weightLayout).replace(',', '.');
    if (!weightText.isEmpty()) {
      weight = parseOrZero(weightText);
      if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
        weightLayout.setError(getString(R.string.error_weight));
        valid = false;
      }
    }

    String heightText = getText(heightLayout);
    if (!heightText.isEmpty()) {
      height = (int) parseOrZero(heightText);
      if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
        heightLayout.setError(getString(R.string.error_height));
        valid = false;
      }
    }

    return valid;
  }

  // "." или "," без цифр — не число, считаем нулём, и проверка диапазона выдаст ошибку
  private float parseOrZero(String text) {
    try {
      return Float.parseFloat(text);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
  private boolean checkNotEmpty(TextInputLayout layout) {
    if (getText(layout).isEmpty()) {
      layout.setError(getString(R.string.error_empty));
      return false;
    }
    return true;
  }

  private String getText(TextInputLayout layout) {
    String text = String.valueOf(layout.getEditText().getText());
    return text.trim();
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
}