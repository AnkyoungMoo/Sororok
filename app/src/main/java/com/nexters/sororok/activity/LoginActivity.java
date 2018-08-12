package com.nexters.sororok.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nexters.sororok.R;
import com.nexters.sororok.asynctask.NaverTokenTask;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity{

    // google
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    //naver
    public static OAuthLogin mOAuthLoginModule;

    // view
    ImageButton googleButton;
    ImageButton naverButton;
    ImageButton kakaoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // google
        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // naver
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                LoginActivity.this,
                "mmelSUnAaeJAGWhIYNQG",
                "nVlJHMh6vf",
                "Sororok"
        );

        googleButton = findViewById(R.id.google_button);
        naverButton = findViewById(R.id.naver_button);
        kakaoButton = findViewById(R.id.kakao_button);

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        naverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOAuthLoginModule.startOauthLoginActivity(LoginActivity.this, mOAuthLoginHandler);
            }
        });

        kakaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                //finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //Log.d("StartEmail: ", currentUser.getEmail());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // naver
    private OAuthLoginHandler mOAuthLoginHandler = new NaverLoginHandler(this);

    // handler를 그냥 사용하면 메모리 누수가 있을 수 있으니 static으로 만들어서 사용
    private static class NaverLoginHandler extends OAuthLoginHandler {
        private final WeakReference<LoginActivity> mActivity;

        public NaverLoginHandler(LoginActivity activity) {
            mActivity = new WeakReference<LoginActivity>(activity);
        }


        @Override
        public void run(boolean success) {
            LoginActivity activity = mActivity.get();

            if (success) {
                String accessToken = mOAuthLoginModule.getAccessToken(activity);
                String refreshToken = mOAuthLoginModule.getRefreshToken(activity);
                long expiresAt = mOAuthLoginModule.getExpiresAt(activity);
                String tokenType = mOAuthLoginModule.getTokenType(activity);

                NaverTokenTask tokenTask = new NaverTokenTask();

                tokenTask.execute(accessToken);

                try {
                    String token = tokenTask.get();

                    Intent intent = new Intent(activity, LoginInfoActivity.class);

                    intent.putExtra("loginType", "naver");
                    intent.putExtra("naverToken", token);

                    activity.startActivity(intent);
                    activity.finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                String errorCode = mOAuthLoginModule.getLastErrorCode(activity).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(activity);
                Toast.makeText(activity, "errorCode:" + errorCode
                        + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;

                            Intent intent = new Intent(LoginActivity.this, LoginInfoActivity.class);
                            intent.putExtra("loginType", "google");
                            intent.putExtra("googleName", user.getDisplayName());
                            intent.putExtra("googleEmail", user.getEmail());
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("NullEmail: ", "");
                        }

                        // ...
                    }
                });
    }
}
