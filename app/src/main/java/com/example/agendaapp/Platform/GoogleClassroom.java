/**
 * Defines the methods for the Google Classroom method.
 */

package com.example.agendaapp.Platform;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agendaapp.Data.Platform;
import com.example.agendaapp.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoogleClassroom extends Platform {

    private final String ID;

    private Activity activity;
    private Context context;

    private ActivityResultRegistry registry;
    private ActivityResultLauncher<Intent> launcher;
    private ActivityResultLauncher<Intent> credentialsLauncher;

    private RequestQueue queue;
    private JsonObjectRequest photoRequest;

    private String authToken;


    /**
     * Constructor, pass in the activity or fragment's parent activity, and a registry by calling
     * requireActivity().getActivityResultRegistry()
     * @param activity The activity or fragment's parent activity
     * @param registry The registry to get the callback when the user signs in
     */
    public GoogleClassroom(Activity activity, ActivityResultRegistry registry) {
        super(ResourcesCompat.getDrawable(activity.getBaseContext().getResources(), R.drawable.ic_google_classroom_32dp, null),
                activity.getBaseContext().getString(R.string.google_classroom), new SignInButton(activity.getBaseContext()));

        ID = UUID.randomUUID().toString();

        this.activity = activity;
        this.context = activity.getBaseContext();

        this.registry = registry;
        launcher = null;
        credentialsLauncher = null;

        queue = Volley.newRequestQueue(activity.getApplicationContext());
        photoRequest = null;

        authToken = "";

        initListeners();
    }

    @Override
    public void onCreate(LifecycleOwner owner) {
        launcher = registry.register("Google Classroom" + ID, owner,
            new ActivityResultContracts.StartActivityForResult(),
            (uri) -> {
                if(uri.getResultCode() == Activity.RESULT_OK)
                    getAuthToken(uri.getData());
            });

        credentialsLauncher = registry.register("Google Classroom Credentials " + ID, owner,
                new ActivityResultContracts.StartActivityForResult(),
                (uri) -> {
                    if(uri.getResultCode() == Activity.RESULT_OK)
                        getAuthToken(uri.getData());
                });
    }

    /*
     * Inits listeners (onClick, etc.)
     */
    public void initListeners() {
        photoRequest = new JsonObjectRequest(Request.Method.GET,
                "https://people.googleapis.com/v1/people/me?personFields=photos",
                null,
                // response is a JSONObject
                response -> {
                    try {
                        String photoURL = response.getJSONArray("photos").getJSONObject(0).getString("url");

                        setAccountIcon(photoURL);

                        callSignInListeners();
                    } catch(JSONException e) {
                        Log.e("IMPORT ERROR", e.toString());
                        Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 401 error = expired token; call AccountManager.invalidateAuthToken() and get auth again
                    try {
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("IMPORT ERROR", new String(htmlBodyBytes), error);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(context, R.string.import_info_error, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                params.put("authorization", "Bearer " + authToken);

                return params;
            }
        };
    }

    /**
     * Makes the call to AccountManager.getAuthToken()
     * @param data Intent data for the account
     */
    private void getAuthToken(Intent data) {
        AccountManager manager = AccountManager.get(context);
        Bundle options = new Bundle();

        manager.getAuthToken(
                new Account(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)),
                "oauth2:https://www.googleapis.com/auth/classroom.coursework.me.readonly https://www.googleapis.com/auth/userinfo.profile",
                options,
                activity,
                // on token acquired
                // AccountManagerCallback<Bundle>
                (AccountManagerFuture<Bundle> result) -> {
                    try {
                        Bundle bundle = result.getResult();

                        // insufficient credentials
                        Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);

                        if(launch != null)
                            credentialsLauncher.launch(launch);

                        authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                        queue.add(photoRequest);
                    } catch(AuthenticatorException | IOException | OperationCanceledException e) {
                        Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();
                    }
                },
                new Handler(Looper.myLooper(), (msg) -> {
                    Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show();

                    return false;
                })
        );
    }

    @Override
    public void onClickSignIn() {
        Intent signInIntent = AccountManager.newChooseAccountIntent(
                null,
                null,
                new String[] {"com.google"},
                false,
                null,
                null,
                null,
                null
        );

        launcher.launch(signInIntent);
    }

    @Override
    public void onClickSignOut() {
        AccountManager.get(context).invalidateAuthToken("com.google", authToken);

        clearData();
    }
}
