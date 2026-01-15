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

            return SessionState.NEEDS_LOGIN;

        } catch (Exception e) {
            e.printStackTrace();
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
        NEEDS_LOGIN,                    
        AUTHENTICATED_NEEDS_PROFILE,   
        AUTHENTICATED_WITH_PROFILE     
    }
}
