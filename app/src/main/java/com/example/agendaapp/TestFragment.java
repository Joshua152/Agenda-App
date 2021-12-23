package com.example.agendaapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.agendaapp.Platforms.GoogleClassroom;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

public class TestFragment extends Fragment {

    private AuthState authState;
    private AuthorizationService authService;

    private ActivityResultLauncher<Intent> launcher;

    private Button btnOAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_test, container, false);

        init(view);

        initCallbacks(view);

        return view;
    }

    private void init(View v) {
        btnOAuth = v.findViewById(R.id.btn_oauth);

        authState = null;

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                uri -> {
                    if(uri.getResultCode() == Activity.RESULT_OK) {
                        Intent data = uri.getData();

                        AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                        AuthorizationException ex = AuthorizationException.fromIntent(data);

                        authState.update(resp, ex);

                        authService.performTokenRequest(resp.createTokenExchangeRequest(),
                                // onTokenRequestCompleted(TokenResponse, AuthorizationException)
                                (r, ex1) -> {
                                    if(ex1 != null) {
                                        Log.e("[TEST APP] oauth", "failed to complete token request: " + ex1);
                                        return;
                                    }

                                    authState.update(r, ex1);

                                    authState.performActionWithFreshTokens(authService,
                                            (accessToken, idToken, ex2) -> {
                                                if(ex2 != null) {
                                                    Log.e("[TEST APP] oauth", "failed to use accessToken: " + ex2);
                                                    return;
                                                }

                                                System.out.println("id token: " + idToken);
                                                System.out.println("access token: " + accessToken);

                                                GoogleClassroom gc = new GoogleClassroom(getActivity());
                                                gc.setAuthToken(accessToken);
                                                gc.getCourses(System.out::println);
                                            });
                                });
                    }
                });
    }

    private void initCallbacks(View v) {
        btnOAuth.setOnClickListener(view -> {
            AuthorizationServiceConfiguration.fetchFromUrl(
                    //  Uri.parse("https://classroom.googleapis.com/$discovery/rest?version=v1"),
                    Uri.parse("https://accounts.google.com/.well-known/openid-configuration"),
                    // AuthorizationServiceConfiguration, AuthorizationException
                    (serviceConfig, e) -> {
                        if(e != null) {
                            Log.e("[TEST APP] oauth", "failed to fetch config");
                            return;
                        }

                        authState = new AuthState(serviceConfig);

                        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                                serviceConfig,
                                "1074224198824-450n5qj3n6nruuleinoh55b5b0kqkrjb.apps.googleusercontent.com",
                                ResponseTypeValues.CODE,
                                Uri.parse("com.example.agendaapp:/")
                        );

                        AuthorizationRequest req = authRequestBuilder
                                .setScope("https://www.googleapis.com/auth/userinfo.profile " +
                                        "https://www.googleapis.com/auth/classroom.courses.readonly " +
                                        "https://www.googleapis.com/auth/classroom.coursework.me.readonly")
                                .build();

                        authService = new AuthorizationService(getContext());
                        Intent authIntent = authService.getAuthorizationRequestIntent(req);

                        launcher.launch(authIntent);
                    }
            );
        });
    }
}
