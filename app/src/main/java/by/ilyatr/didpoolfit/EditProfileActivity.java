package by.ilyatr.didpoolfit;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;

import java.io.File;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private UserStorage storage;
    private UserStorage.User original;
    private EditProfileAdapter adapter;
    private RecyclerView list;
    private Button save;
    String photoName = "";
    private String cameraName = "";
    private boolean importing, saving, saved;

    private final ActivityResultLauncher<String> gallery = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> { if (uri != null) importPhoto(uri); });
    private final ActivityResultLauncher<Uri> camera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                File file = cameraFile();
                if (Boolean.TRUE.equals(success) && file != null) importPhoto(Uri.fromFile(file));
                else clearCamera();
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_edit_profile);
        ProfileUi.insets(this);
        storage = new UserStorage(this);
        original = ProfileUi.requireUser(this, storage);
        if (original == null) return;
        photoName = state == null ? original.photoFileName : state.getString("photoName", original.photoFileName);
        cameraName = state == null ? "" : state.getString("cameraName", "");
        ((TextView) findViewById(R.id.profile_title)).setText(R.string.edit_profile_title);
        findViewById(R.id.profile_more).setVisibility(View.INVISIBLE);
        findViewById(R.id.profile_back).setOnClickListener(v -> confirmExit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { confirmExit(); }
        });
        list = findViewById(R.id.edit_profile_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EditProfileAdapter(this, new EditProfileAdapter.Draft(original, state));
        list.setAdapter(adapter);
        save = findViewById(R.id.save_profile);
        save.setOnClickListener(v -> confirmSave());
        androidx.fragment.app.Fragment restored = getSupportFragmentManager().findFragmentByTag("editBirthDate");
        if (restored instanceof MaterialDatePicker<?>) {
            @SuppressWarnings("unchecked") MaterialDatePicker<Long> picker = (MaterialDatePicker<Long>) restored;
            attachBirthDate(picker);
        }
    }

    void chooseBirthDate() {
        if (saving || importing || getSupportFragmentManager().findFragmentByTag("editBirthDate") != null) return;
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.field_birth_date).setSelection(adapter.draft.birthDate)
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now()).build()).build();
        attachBirthDate(picker);
        picker.show(getSupportFragmentManager(), "editBirthDate");
    }

    private void attachBirthDate(MaterialDatePicker<Long> picker) {
        picker.clearOnPositiveButtonClickListeners();
        picker.addOnPositiveButtonClickListener(value -> {
            adapter.draft.setBirthDate(value);
            adapter.draft.touched[EditProfileAdapter.BIRTH] = true;
            adapter.notifyItemChanged(EditProfileAdapter.BIRTH + 1);
        });
    }

    private void confirmSave() {
        if (saving || importing) return;
        adapter.draft.submitted = true;
        adapter.refreshErrors();
        int error = adapter.draft.firstError();
        if (error >= 0) { list.smoothScrollToPosition(error + 1); return; }
        new MaterialAlertDialogBuilder(this).setTitle(R.string.confirm_save_title)
                .setMessage(R.string.confirm_save_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save_changes_action, (dialog, which) -> saveProfile()).show();
    }

    private void confirmExit() {
        if (saving || importing) {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this).setTitle(R.string.confirm_leave_title)
                .setMessage(R.string.confirm_leave_message)
                .setNegativeButton(R.string.keep_editing, null)
                .setPositiveButton(R.string.leave, (dialog, which) -> finish()).show();
    }

    void choosePhoto() {
        if (importing || saving) return;
        new MaterialAlertDialogBuilder(this).setTitle(R.string.change_photo)
                .setItems(new String[]{getString(R.string.photo_gallery), getString(R.string.photo_camera), getString(R.string.photo_default)}, (dialog, which) -> {
                    if (which == 0) {
                        try { gallery.launch("image/*"); }
                        catch (ActivityNotFoundException e) { showError(R.string.error_photo); }
                    } else if (which == 1) takePhoto();
                    else {
                        discardDraftPhoto();
                        photoName = "";
                        adapter.notifyItemChanged(0);
                    }
                }).show();
    }

    private File cameraFile() {
        if (!cameraName.matches("camera_[0-9]+\\.jpg")) return null;
        return new File(new File(getCacheDir(), "profile_camera"), cameraName);
    }

    private void takePhoto() {
        clearCamera();
        try {
            File directory = new File(getCacheDir(), "profile_camera");
            if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException();
            File file = File.createTempFile("camera_", ".jpg", directory);
            cameraName = file.getName();
            camera.launch(FileProvider.getUriForFile(this, getPackageName() + ".profile.files", file));
        } catch (IOException | ActivityNotFoundException | SecurityException e) {
            clearCamera();
            showError(R.string.error_photo);
        }
    }

    private void importPhoto(Uri uri) {
        importing = true;
        save.setEnabled(false);
        UserStorage.WRITES.execute(() -> {
            try {
                String imported = ProfilePhotos.importPhoto(this, uri);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) { ProfilePhotos.delete(this, imported); return; }
                    discardDraftPhoto();
                    photoName = imported;
                    importing = false;
                    save.setEnabled(true);
                    adapter.notifyItemChanged(0);
                });
            } catch (IOException | SecurityException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    importing = false;
                    save.setEnabled(true);
                    showError(R.string.error_photo);
                });
            } finally {
                clearCamera();
            }
        });
    }

    private void saveProfile() {
        if (importing || saving) return;
        EditProfileAdapter.Draft draft = adapter.draft;
        draft.submitted = true;
        adapter.refreshErrors();
        int error = draft.firstError();
        if (error >= 0) { list.smoothScrollToPosition(error + 1); return; }
        String name = draft.values[EditProfileAdapter.NAME];
        String gender = draft.values[EditProfileAdapter.GENDER];
        float weight = draft.weight();
        int height = draft.height();
        String current = draft.values[EditProfileAdapter.CURRENT];
        String password = draft.values[EditProfileAdapter.NEW];
        String photo = photoName;
        long birthDate = draft.birthDate;
        saving = true;
        save.setEnabled(false);
        UserStorage.WRITES.execute(() -> {
            try {
                UserStorage.UpdateResult result = storage.updateProfile(original.email, name, gender, birthDate, weight, height, photo, current, password);
                if (result == UserStorage.UpdateResult.SUCCESS && !photo.equals(original.photoFileName)) {
                    ProfilePhotos.delete(this, original.photoFileName);
                }
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    saving = false;
                    save.setEnabled(true);
                    if (result == UserStorage.UpdateResult.SUCCESS) {
                        saved = true;
                        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (result == UserStorage.UpdateResult.WRONG_PASSWORD) {
                        draft.passwordError = R.string.invalid_password;
                        adapter.refreshErrors();
                        list.smoothScrollToPosition(EditProfileAdapter.CURRENT + 1);
                    } else showError(result == UserStorage.UpdateResult.NO_SESSION ? R.string.error_session : R.string.error_profile);
                });
            } catch (UserStorage.StorageException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    saving = false;
                    save.setEnabled(true);
                    showError(R.string.error_storage);
                });
            }
        });
    }

    private void showError(int text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }

    private void discardDraftPhoto() {
        if (original == null || photoName.equals(original.photoFileName)) return;
        // После поворота фоновое сохранение предыдущего экземпляра могло уже завершиться.
        // Фотографию, на которую ссылается сохранённый профиль, не удаляем.
        try {
            UserStorage.User current = storage.getCurrentUser();
            if (current != null && photoName.equals(current.photoFileName)) return;
        } catch (UserStorage.StorageException e) {
            return;
        }
        ProfilePhotos.delete(this, photoName);
    }

    private void clearCamera() {
        File file = cameraFile();
        if (file != null && file.isFile()) file.delete();
        cameraName = "";
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        if (adapter != null) adapter.draft.save(state);
        state.putString("photoName", photoName);
        state.putString("cameraName", cameraName);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (isFinishing() && !saving && !saved) { discardDraftPhoto(); clearCamera(); }
        super.onDestroy();
    }
}
