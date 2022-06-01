/**
 * Helper class for OAuth to reduce repetitive boilerplate code
 *
 * MUST CALL setRegistry(ActivityResultRegistry) and setLifecycleOwner(LifecycleOwner) before
 * attempting to launch the OAuth screen
 *
 * @author Joshua Au
 */

package com.example.agendaapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.LifecycleOwner;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.agendaapp.Data.ApiCred;
import com.example.agendaapp.R;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class OAuthHelper {

    // TODO: SIGN OUT IF DELETE PLATFORM!!!!

    private final static String SHARED_PREFERENCES_KEY = "OAuthHelper Shared Preferences Key";
    private final static String AUTH_STATE_JSON_KEY = "Auth State JSON Key";

    private final String UID;

    private Context context;
    private LifecycleOwner owner;

    private static SharedPreferences sharedPreferences;

    private ApiCred apiCred;

    private ConfigListener configListener;

    private AuthState authState;
    private AuthorizationService authService;

    private ActivityResultRegistry registry;
    private ActivityResultLauncher<Intent> authLauncher;

    private String discoveryDocURL;
    private String scopes;

    private Intent authIntent;
    private OAuthCompleteListener oAuthCompleteListener;

    public OAuthHelper(Context context, String UID, String discoveryDocURL, String scopes, ConfigListener configListener) {
        this.UID = UID;

        this.context = context;
        owner = null;

        if(sharedPreferences == null)
            initEncryptedSharedPrefs(context);

        apiCred = new ApiCred();

        this.configListener = configListener;

        authState = getAuthState();
        authService = null;

        registry = null;
        authLauncher = null;

        this.discoveryDocURL = discoveryDocURL;
        this.scopes = scopes;

        authIntent = null;
        oAuthCompleteListener = null;

        init();
    }

    /**
     * Start processes for setting up OAuth
     */
    public void init() {
        AuthorizationServiceConfiguration.fetchFromUrl(
                Uri.parse(discoveryDocURL),
                // AuthorizationServiceConfiguration, AuthorizationException
                (serviceConfig, e) -> {
                    // TODO: DISABLE SIGN IN BUTTON UNTIL THIS IS RAN?
                    // TODO: SET VOLATILE?

                    new Thread(() -> {
                        if(e != null) {
                            Log.e("[AGENDA APP] oauth", "failed to fetch config");
                            Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show();

                            return;
                        }

                        if(authState == null)
                            authState = new AuthState(serviceConfig);

                        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                                serviceConfig,
                                apiCred.clientId(),
                                ResponseTypeValues.CODE,
                                Uri.parse("com.example.agendaapp:/")
                        );

                        AuthorizationRequest req = authRequestBuilder
                                .setScope(scopes)
                                .build();

                        authService = new AuthorizationService(context);
                        authIntent = authService.getAuthorizationRequestIntent(req);

                        configListener.onConfig();
                    }).start();
                }
        );
    }

    public void initLauncher() {
        authLauncher = registry.register(UUID.randomUUID().toString(), owner,
                new ActivityResultContracts.StartActivityForResult(),
                uri -> {
                    if(uri.getResultCode() == Activity.RESULT_OK) {
                        Intent data = uri.getData();

                        AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                        AuthorizationException ex = AuthorizationException.fromIntent(data);

                        authState.update(resp, ex);

                        authService.performTokenRequest(resp.createTokenExchangeRequest(),
                                // onTokenRequestCompleted(TokenResponse, AuthorizationException)
                                (resp1, ex1) -> {
                                    if(ex1 != null) {
                                        Log.e("[TEST APP] oauth", "failed to complete token request: " + ex1);
                                        Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show();

                                        return;
                                    }

                                    authState.update(resp1, ex1);

                                    writeAuthState(authState);

                                    oAuthCompleteListener.onOAuthComplete(authState);
                                });
                    }
                });

    }

    /**
     * Initialize a universal encrypted shared preferences to speed up the process of creating
     * new imported platform instances
     * @param context The context
     */
    public static void initEncryptedSharedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    SHARED_PREFERENCES_KEY,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch(GeneralSecurityException | IOException e) {
            Log.e("[AGENDA APP] OAuth", "Unable to create master key: " + e);
            Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Launches the OAuth screen. Saves the tokens through {@link #writeAuthState(AuthState)}
     * @param oAuthCompleteListener The listener for when the OAuth intent has completed
     */
    public void launchOAuth(OAuthCompleteListener oAuthCompleteListener) {
        this.oAuthCompleteListener = oAuthCompleteListener;

        try {
            authLauncher.launch(authIntent);
        } catch(Exception e) {
            Log.e("LAUNCH AUTH", e.toString());
        }
    }

    /**
     * Gets and returns valid auth tokens through the callback
     * @param listener The token listener for when when auth state returns the tokens
     */
    public void useAuthToken(OAuthCompleteListener listener) {
        if(authService == null)
            return;

        authState.performActionWithFreshTokens(authService,
                (accessToken, idToken, ex) -> {
//                    authState.update(accessToken);

//                    TokenRequest request = authState.createTokenRefreshRequest();

                    if (ex != null) {
                        Log.e("[TEST APP] oauth", "failed to use accessToken: " + ex);
//                        Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show();

                        listener.onOAuthComplete(null);

                        return;
                    }

                    writeAuthState(authState);

                    listener.onOAuthComplete(authState);
                });
    }

    /**
     * Writes the AuthState to the encrypted shared preferences (handles saving automatically)
     * @param authState The auth state to write and encrypt
     */
    private void writeAuthState(AuthState authState) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AUTH_STATE_JSON_KEY + " | " + UID, authState.jsonSerializeString()).apply();
    }

    /**
     * Returns the AppAuth AuthState
     * @return The AuthState object; contains the access and id tokens. Returns null if there is no
     * auth state (signed out)
     */
    public AuthState getAuthState() {
        if(authState != null)
            return authState;

        try {
            String authStateJSON = sharedPreferences.getString(AUTH_STATE_JSON_KEY + " | " + UID, null);

            if(authStateJSON == null)
                return null;

            AuthState authState = AuthState.jsonDeserialize(authStateJSON);

            this.authState = authState;

            return authState;
        } catch(JSONException e) {
            Log.e("[AGENDA APP] OAuth", "Unable to deserialize auth state JSON: " + e);
            Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Resets the auth state (does not use EndSessionRequest because there is no end session endpoint)
     */
    public void signOut() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove(AUTH_STATE_JSON_KEY + " | " + UID).apply();
//
//        if(authState == null)
//            return;
//
//        AuthorizationServiceConfiguration config = authState.getAuthorizationServiceConfiguration();
//
//        EndSessionRequest request = new EndSessionRequest.Builder(config)
//                .setIdTokenHint(authState.getIdToken())
//                .setPostLogoutRedirectUri(Uri.parse("com.example.agendaapp:/"))
//                .build();
//
//        Intent endSessionIntent = authService.getEndSessionRequestIntent(request);
//        endSessionLauncher.launch(endSessionIntent);

        authState = new AuthState(authState.getAuthorizationServiceConfiguration());
    }

    public void setRegistry(ActivityResultRegistry registry) {
        this.registry = registry;

        if(owner != null)
            initLauncher();
    }

    public void setLifecycleOwner(LifecycleOwner owner) {
        this.owner = owner;

        if(registry != null)
            initLauncher();
    }

    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    /**
     * Listener for when the AuthorizationServiceConfig has been initialized and calls to get the access token
     * can be made
     */
    public interface ConfigListener {
        /**
         * Callback for when the AuthorizationServiceConfig has been initialized
         */
        public void onConfig();
    }

    /**
     * Listener for when the OAuth launch has completed
     */
    public interface OAuthCompleteListener {
        /**
         * Callback for when the OAuth screen has been completed
         * @param authState The auth state after OAuth completion
         */
        public void onOAuthComplete(AuthState authState);
    }
}
