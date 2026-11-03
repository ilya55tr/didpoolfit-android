package by.ilyatr.didpoolfit;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import by.ilyatr.didpoolfit.model.WorkoutHistoryItem;

@RunWith(AndroidJUnit4.class)
public class UserStorageTest {
    private SharedPreferences preferences;
    private UserStorage storage;
    private static final long BIRTH = 946684800000L;
    private static final String PASSWORD = "12345!@#";

    @Before public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferences = context.getSharedPreferences("users_test_" + System.nanoTime(), Context.MODE_PRIVATE);
        storage = new UserStorage(preferences);
    }

    @After public void tearDown() { preferences.edit().clear().commit(); }

    private UserStorage.RegistrationResult register(String name, String email, String phone, float weight) {
        return storage.register(name, phone, email, PASSWORD, "Male", BIRTH, weight, 180);
    }

    @Test public void emptyStorageHasNoSessionOrHistory() {
        assertFalse(storage.hasUsers());
        assertNull(storage.getCurrentUser());
        assertTrue(storage.getHistory().isEmpty());
        assertFalse(storage.login("missing@example.com", PASSWORD));
    }

    @Test public void twoAccountsCanSwitchWithoutLosingData() {
        assertEquals(UserStorage.RegistrationResult.SUCCESS, register("Alice", "alice@example.com", "79991112233", 60));
        assertTrue(storage.login("alice@example.com", PASSWORD));
        assertEquals(UserStorage.RegistrationResult.SUCCESS, register("Bob", "bob@example.com", "79994445566", 90));
        assertEquals("Alice", storage.getCurrentUser().fullName);
        storage.logout();
        assertTrue(storage.hasUsers());
        assertNull(storage.getCurrentUser());
        assertTrue(storage.login(" BOB@EXAMPLE.COM ", PASSWORD));
        assertEquals("Bob", storage.getCurrentUser().fullName);
        assertEquals(90, storage.getCurrentUser().weight, 0.01f);
        assertTrue(storage.login("alice@example.com", PASSWORD));
        assertEquals(60, storage.getCurrentUser().weight, 0.01f);
    }

    @Test public void duplicatesUseNormalizedEmailAndPhone() {
        register("Alice", "alice@example.com", "79991112233", 60);
        assertTrue(storage.emailExists(" ALICE@EXAMPLE.COM "));
        assertTrue(storage.phoneExists("+7 (999) 111-22-33"));
        assertEquals(UserStorage.RegistrationResult.EMAIL_EXISTS, register("Other", "ALICE@example.com", "79994445566", 80));
        assertEquals(UserStorage.RegistrationResult.PHONE_EXISTS, register("Other", "other@example.com", "+7 (999) 111-22-33", 80));
    }

    @Test public void emailIsTheKeyAndPasswordIsPlainString() throws Exception {
        register("Alice", "ALICE@example.com", "79991112233", 60);
        JSONObject users = new JSONObject(preferences.getString("list", "{}"));
        JSONObject user = users.getJSONObject("alice@example.com");
        assertEquals(PASSWORD, user.getString("password"));
        assertFalse(user.has("id"));
        assertFalse(user.has("passwordHash"));
        assertFalse(user.has("passwordSalt"));
    }

    @Test public void wrongPasswordDoesNotChangeSession() {
        register("Alice", "alice@example.com", "79991112233", 60);
        storage.login("alice@example.com", PASSWORD);
        assertFalse(storage.login("alice@example.com", "1234567!"));
        assertEquals("alice@example.com", storage.getCurrentUser().email);
    }

    @Test public void sessionAndAccountsSurviveReopening() {
        register("Alice", "alice@example.com", "79991112233", 60);
        storage.login("alice@example.com", PASSWORD);
        UserStorage reopened = new UserStorage(preferences);
        assertEquals("alice@example.com", reopened.getCurrentUser().email);
        reopened.logout();
        assertNull(new UserStorage(preferences).getCurrentUser());
        assertTrue(new UserStorage(preferences).hasUsers());
    }

    @Test public void profileAndBirthdayUpdateOnlyCurrentAccount() {
        register("Alice", "alice@example.com", "79991112233", 60);
        register("Bob", "bob@example.com", "79994445566", 90);
        storage.login("alice@example.com", PASSWORD);
        long birthday = 978307200000L;
        assertEquals(UserStorage.UpdateResult.SUCCESS,
                storage.updateProfile("alice@example.com", "Alice New", "Female", birthday, 62.5f, 172, "avatar_123.jpg", "", ""));
        assertEquals("Alice New", storage.getCurrentUser().fullName);
        assertEquals(birthday, storage.getCurrentUser().dateOfBirth);
        assertEquals("avatar_123.jpg", storage.getCurrentUser().photoFileName);
        storage.login("bob@example.com", PASSWORD);
        assertEquals("Bob", storage.getCurrentUser().fullName);
        assertEquals(BIRTH, storage.getCurrentUser().dateOfBirth);
        assertEquals(90, storage.getCurrentUser().weight, 0.01f);
    }

    @Test public void passwordChangeRequiresCorrectOldPasswordAndIsAtomic() {
        register("Alice", "alice@example.com", "79991112233", 60);
        storage.login("alice@example.com", PASSWORD);
        assertEquals(UserStorage.UpdateResult.WRONG_PASSWORD,
                storage.updateProfile("alice@example.com", "Changed", "Female", BIRTH, 80, 175, "", "wrong", "1234567!"));
        assertEquals("Alice", storage.getCurrentUser().fullName);
        assertEquals(UserStorage.UpdateResult.SUCCESS,
                storage.updateProfile("alice@example.com", "Alice", "Female", BIRTH, 60, 175, "", PASSWORD, "1234567!"));
        storage.logout();
        assertFalse(storage.login("alice@example.com", PASSWORD));
        assertTrue(storage.login("alice@example.com", "1234567!"));
    }

    @Test public void invalidBirthdayProfileAndSessionAreRejected() {
        assertEquals(UserStorage.RegistrationResult.INVALID_PROFILE,
                storage.register("Alice", "79991112233", "alice@example.com", PASSWORD, "Male", System.currentTimeMillis()+86400000, 60, 180));
        register("Alice", "alice@example.com", "79991112233", 60);
        storage.login("alice@example.com", PASSWORD);
        assertEquals(UserStorage.UpdateResult.NO_SESSION,
                storage.updateProfile("other@example.com", "Wrong", "Male", BIRTH, 60, 180, "", "", ""));
        assertEquals(UserStorage.UpdateResult.INVALID_PROFILE,
                storage.updateProfile("alice@example.com", "Wrong", "Male", BIRTH, 60, 180, "../photo.jpg", "", ""));
        assertEquals(UserStorage.UpdateResult.INVALID_PROFILE,
                storage.updateProfile("alice@example.com", "Wrong", "Male", System.currentTimeMillis()+86400000, 60, 180, "", "", ""));
        assertEquals(UserStorage.UpdateResult.INVALID_PROFILE,
                storage.updateProfile("alice@example.com", "Wrong", "Male", BIRTH, Float.NaN, 180, "", "", ""));
        assertEquals("Alice", storage.getCurrentUser().fullName);
    }

    @Test public void historyIsPerUserNewestFirstAndPersists() {
        register("Alice", "alice@example.com", "79991112233", 60);
        register("Bob", "bob@example.com", "79994445566", 90);
        storage.login("alice@example.com", PASSWORD);
        assertTrue(storage.addWorkout("alice@example.com", "Fullbody Workout", 8, 17, 123, 1000));
        assertTrue(storage.addWorkout("alice@example.com", "AB Workout", 10, 20, 456, 3000));
        assertTrue(storage.addWorkout("alice@example.com", "Lowerbody Workout", 6, 15, 789, 2000));
        List<WorkoutHistoryItem> history = new UserStorage(preferences).getHistory();
        assertEquals(3, history.size());
        assertEquals("AB Workout", history.get(0).title);
        assertEquals(3000, history.get(0).startedAt);
        assertEquals(456, history.get(0).seed);
        assertEquals(10, history.get(0).exercises);
        assertEquals(20, history.get(0).minutes);
        storage.login("bob@example.com", PASSWORD);
        assertTrue(storage.getHistory().isEmpty());
        assertFalse(storage.addWorkout("alice@example.com", "Wrong account", 8, 17, 0, 4000));
        assertTrue(storage.getHistory().isEmpty());
        storage.logout();
        assertFalse(storage.addWorkout("bob@example.com", "Logged out", 8, 17, 0, 4000));
        storage.login("alice@example.com", PASSWORD);
        assertEquals(3, storage.getHistory().size());
    }

    @Test public void profileUpdatePreservesHistoryAndEveryStartIsRecorded() {
        register("Alice", "alice@example.com", "79991112233", 60);
        storage.login("alice@example.com", PASSWORD);
        storage.addWorkout("alice@example.com", "AB Workout", 8, 20, 1, 1000);
        storage.addWorkout("alice@example.com", "AB Workout", 8, 20, 1, 1000);
        assertEquals(UserStorage.UpdateResult.SUCCESS,
                storage.updateProfile("alice@example.com", "Alice New", "Female", BIRTH, 65, 170, "", "", ""));
        assertEquals(2, storage.getHistory().size());
    }

    @Test public void passwordRuleIsUnchanged() {
        for (String password : new String[]{"1234567!", "123456!@", "12345!@#", "!1@2#345"}) assertTrue(UserStorage.isPasswordValid(password));
        for (String password : new String[]{"12345678", "1234!@#$", "12345!", "12345678!", "12345ab!", "12345 !@"}) assertFalse(UserStorage.isPasswordValid(password));
    }

    @Test public void corruptedStorageIsNotOverwritten() {
        preferences.edit().putString("list", "broken json").commit();
        try { register("Alice", "alice@example.com", "79991112233", 60); fail("Expected StorageException"); }
        catch (UserStorage.StorageException expected) { assertEquals("broken json", preferences.getString("list", null)); }
    }

    @Test public void concurrentDuplicateRegistrationsCreateOneAccount() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<UserStorage.RegistrationResult> a = executor.submit(() -> register("Alice", "alice@example.com", "79991112233", 60));
            Future<UserStorage.RegistrationResult> b = executor.submit(() -> new UserStorage(preferences)
                    .register("Other", "79994445566", "ALICE@example.com", PASSWORD, "Male", BIRTH, 80, 180));
            UserStorage.RegistrationResult first = a.get(), second = b.get();
            assertTrue(first == UserStorage.RegistrationResult.SUCCESS && second == UserStorage.RegistrationResult.EMAIL_EXISTS
                    || second == UserStorage.RegistrationResult.SUCCESS && first == UserStorage.RegistrationResult.EMAIL_EXISTS);
            assertEquals(1, new JSONObject(preferences.getString("list", "{}")).length());
        } finally { executor.shutdownNow(); }
    }
}
