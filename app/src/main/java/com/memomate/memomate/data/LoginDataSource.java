import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.memomate.memomate.data.model.LoggedInUser;

public class LoginDataSource {

    public void login(String email, String password, final LoginCallback callback) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            LoggedInUser loggedInUser = new LoggedInUser(
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail()); // Użyj adresu e-mail jako nazwy użytkownika
                            callback.onLoginSuccess(loggedInUser);
                        } else {
                            callback.onLoginError("Firebase user is null");
                        }
                    } else {
                        callback.onLoginError("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public interface LoginCallback {
        void onLoginSuccess(LoggedInUser user);
        void onLoginError(String errorMessage);
    }
}