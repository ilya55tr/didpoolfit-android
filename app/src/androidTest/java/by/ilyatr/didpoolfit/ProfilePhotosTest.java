package by.ilyatr.didpoolfit;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfilePhotosTest {
    @Test public void importedImageIsOwnedResizedAndRejectsOutsidePaths() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNull(ProfilePhotos.file(context, "../outside.jpg"));
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.avatar_default_male);
        String name = ProfilePhotos.importPhoto(context, uri);
        try {
            assertTrue(ProfilePhotos.file(context, name).isFile());
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(ProfilePhotos.file(context, name).getPath(), bounds);
            assertTrue(bounds.outWidth > 0 && bounds.outWidth <= 1024);
            assertTrue(bounds.outHeight > 0 && bounds.outHeight <= 1024);
        } finally {
            ProfilePhotos.delete(context, name);
        }
        assertFalse(ProfilePhotos.file(context, name).exists());
    }

    @Test public void draftValidationSurvivesRebindingAndDoesNotSavePasswords() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String prefix = "draft_test_" + java.util.UUID.randomUUID();
        android.content.SharedPreferences accounts = context.getSharedPreferences(prefix, Context.MODE_PRIVATE);
        android.content.SharedPreferences legacy = context.getSharedPreferences(prefix + "_old", Context.MODE_PRIVATE);
        try {
            UserStorage storage = new UserStorage(accounts);
            storage.register("Alice", "79991112233", "alice@example.com", "12345!@#", "Female", 946684800000L, 60, 170);
            storage.login("alice@example.com", "12345!@#");
            EditProfileAdapter.Draft draft = new EditProfileAdapter.Draft(storage.getCurrentUser(), null);
            draft.values[EditProfileAdapter.NAME] = "Alice New";
            draft.setBirthDate(978307200000L);
            draft.values[EditProfileAdapter.WEIGHT] = "62,5";
            draft.values[EditProfileAdapter.NEW] = "1234567!";
            assertEquals(R.string.error_empty, draft.error(EditProfileAdapter.CURRENT));
            assertEquals(R.string.error_repeat_password, draft.error(EditProfileAdapter.REPEAT));
            draft.values[EditProfileAdapter.CURRENT] = "12345!@#";
            draft.values[EditProfileAdapter.REPEAT] = "1234567!";
            assertEquals(-1, draft.firstError());
            android.os.Bundle state = new android.os.Bundle();
            draft.save(state);
            EditProfileAdapter.Draft restored = new EditProfileAdapter.Draft(storage.getCurrentUser(), state);
            assertEquals("Alice New", restored.values[EditProfileAdapter.NAME]);
            assertEquals(978307200000L, restored.birthDate);
            restored.setBirthDate(System.currentTimeMillis() + 86400000L);
            assertEquals(R.string.error_birth_date, restored.error(EditProfileAdapter.BIRTH));
            restored.setBirthDate(978307200000L);
            assertEquals(62.5f, restored.weight(), 0.01f);
            assertEquals("", restored.values[EditProfileAdapter.NEW]);
            assertEquals("", restored.values[EditProfileAdapter.CURRENT]);
            assertEquals(-1, restored.firstError());
        } finally {
            accounts.edit().clear().commit();
            legacy.edit().clear().commit();
        }
    }
}
