package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.models.User;
import lombok.Getter;

@Getter
public class SessionManager {
    private static SessionManager instance;

    private final AuthService authService;

    private SessionManager() {
        this.authService = new AuthService();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Initialize session on app startup.
     * Attempts to restore previous session automatically.
     *
     * @return SessionState indicating what the app should do next
     */
    public SessionState initializeSession() {
        try {
            // Try to restore session from stored refresh token
            if (authService.hasStoredSession()) {
                if (authService.restoreSession()) {
                    // Session restored successfully
                    return checkUserProfile();
                }
                // Stored session invalid, need fresh login
            }

            // No stored session or restore failed
            return SessionState.NEEDS_LOGIN;

        } catch (Exception e) {
            e.printStackTrace();
            // On any error, require fresh login
            return SessionState.NEEDS_LOGIN;
        }
    }
    
    private SessionState checkUserProfile() {
        if (authService.hasProfile()) {
            return SessionState.AUTHENTICATED_WITH_PROFILE;
        } else {
            return SessionState.AUTHENTICATED_NEEDS_PROFILE;
        }
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    public String getAccessToken() {
        return authService.getValidAccessToken();
    }


    public void signOut() {
        authService.signOut();
    }

    public boolean refreshTokenIfNeeded() {
        if (authService.needsTokenRefresh()) {
            return authService.refreshToken();
        }
        return true; // No refresh needed
    }

    public enum SessionState {
        NEEDS_LOGIN,                    // User needs to log in
        AUTHENTICATED_NEEDS_PROFILE,    // User is logged in but needs to complete profile
        AUTHENTICATED_WITH_PROFILE      // User is fully authenticated and has profile
    }
}
