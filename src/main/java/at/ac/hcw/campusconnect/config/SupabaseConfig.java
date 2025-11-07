package at.ac.hcw.campusconnect.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;

public class SupabaseConfig {
    private static String supabaseUrl;
    @Getter
    private static String supabaseKey;

    public static void initialize() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        supabaseUrl = dotenv.get("SUPABASE_URL");
        supabaseKey = dotenv.get("SUPABASE_ANON_KEY");

        if (supabaseUrl == null || supabaseKey == null) {
            System.out.println("Warning: Supabase configuration not found. Using demo mode.");
            throw new IllegalStateException("SUPABASE_URL and SUPABASE_ANON_KEY must be set in .env file");
        }
    }

    public static String getAuthUrl() {
        return supabaseUrl + "/auth/v1";
    }

    public static String getRestUrl() {
        return supabaseUrl + "/rest/v1";
    }

    public static String getStorageUrl() {
        return supabaseUrl + "/storage/v1";
    }
}
