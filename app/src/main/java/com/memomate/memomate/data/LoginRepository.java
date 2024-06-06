import com.memomate.memomate.data.model.LoggedInUser;

public class LoginRepository {

    private static volatile LoginRepository instance;
    private LoginDataSource dataSource;

    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void logout() {
        dataSource.logout();
    }

    public void login(String email, String password, final LoginDataSource.LoginCallback callback) {
        dataSource.login(email, password, callback);
    }
}