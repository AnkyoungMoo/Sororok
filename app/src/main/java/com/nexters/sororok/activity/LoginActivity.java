package com.nexters.sororok.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.nexters.sororok.R;
import com.nexters.sororok.asynctask.LoginAsyncTask;
import com.nexters.sororok.asynctask.NaverTokenTask;
import com.nexters.sororok.model.LoginRequestModel;
import com.nexters.sororok.model.LoginResponseModel;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.json.JSONException;
import org.json.JSONObject;

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

    // kakao
    private SessionCallback kakaoCallback;

    // view
    ImageButton googleButton;
    ImageButton naverButton;
    ImageButton kakaoButton;

    // 변수
    static String id;
    static String loginType;
    static String uid;

    /*TODO: 카카오 중복 success 해결방안을 찾을 때까지 임시 방편 boolean*/
    boolean isSuccess;

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

        isSuccess = false;

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
                kakaoLogin();
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

    // kakao
    private void kakaoLogin() {
        kakaoCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(kakaoCallback);
        Session.getCurrentSession().checkAndImplicitOpen();
        Session.getCurrentSession().open(AuthType.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN, LoginActivity.this);
   }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            Log.d("SessionOpen" , "세션 오픈됨");
            // 사용자 정보를 가져옴, 회원가입 미가입시 자동가입 시킴
            kakaoRequestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Log.d("SessionFail" , exception.getMessage());
            }
        }
    }

    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void kakaoRequestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                int ErrorCode = errorResult.getErrorCode();
                int ClientErrorCode = -777;

                if (ErrorCode == ClientErrorCode) {
                    Toast.makeText(getApplicationContext(), "카카오톡 서버의 네트워크가 불안정합니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("KakaoFailure" , "오류로 카카오로그인 실패");
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d("SessionClosed" , "오류로 카카오로그인 실패 ");
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                //String profileUrl = userProfile.getProfileImagePath();
                /*TODO: 카카오 중복 success 다른 해결방안을 찾아보자..*/
                if (!isSuccess) {
                    isSuccess = true;
                    String userName = userProfile.getNickname();

                    Log.d("kakaoProfile: ", userProfile.getId() + "");
                    Log.d("kakaoProfile: ", userProfile.getUUID() + "");
                    Log.d("kakaoName: ", userName);

                    // kakao = 1
                    loginType = "1";
                    uid = userProfile.getId() + "";

                    callRetrofit(LoginActivity.this);

                    if (!id.equals("-1")) {
                        Intent intent = new Intent(LoginActivity.this, TestActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Intent intent = new Intent(LoginActivity.this, LoginInfoActivity.class);

                        intent.putExtra("loginType", loginType);
                        intent.putExtra("loginUid", uid);
                        // 카카오는 받아올 이메일이 없어서 빈 문자열을 넘김
                        intent.putExtra("email", "");
                        intent.putExtra("name", userName);
                        if (userProfile.getProfileImagePath() != null)
                            intent.putExtra("imageUrl", userProfile.getProfileImagePath());
                        else
                            intent.putExtra("imageUrl", "");

                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onNotSignedUp() {
                // 자동가입이 아닐경우 동의창
                Log.d("SignedUp", "");
            }
        });
    }

    // handler를 그냥 사용하면 메모리 누수가 있을 수 있으니 static으로 만들어서 사용
    private static class NaverLoginHandler extends OAuthLoginHandler {
        private final WeakReference<LoginActivity> mActivity;

        private NaverLoginHandler(LoginActivity activity) {
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

                    Log.d("naverJSON: ", token);

                    JSONObject naverJson = new JSONObject(token);

                    JSONObject naverResponseJson = naverJson.getJSONObject("response");

                    String naverId = naverResponseJson.getString("id");
                    String name = naverResponseJson.getString("name");
                    String email = naverResponseJson.getString("email");
                    String imageUrl;
                    if (naverResponseJson.getString("profile_image") != null)
                        imageUrl = naverResponseJson.getString("profile_image");
                    else
                        imageUrl = "";

                    // naver = 2
                    loginType = "2";
                    uid = naverId;

                    callRetrofit(activity);

                    if (!id.equals("-1")) {
                        Intent intent = new Intent(activity, TestActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    } else {
                        Intent intent = new Intent(activity, LoginInfoActivity.class);

                        intent.putExtra("loginType", loginType);
                        intent.putExtra("loginUid", uid);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("imageUrl", imageUrl);

                        activity.startActivity(intent);
                        activity.finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
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

        // kakao
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
            return;

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

                            Log.d("googleUID", user.getUid());

                            // google = 0
                            loginType = "0";
                            uid = user.getUid();
                            String imageUrl;

                            if (user.getPhotoUrl() != null)
                                imageUrl = user.getPhotoUrl() + "";
                            else
                                imageUrl = "";

                            callRetrofit(LoginActivity.this);

                            if (!id.equals("-1")) {
                                Intent intent = new Intent(LoginActivity.this, TestActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, LoginInfoActivity.class);
                                intent.putExtra("loginType", loginType);
                                intent.putExtra("loginUid", uid);
                                intent.putExtra("name", user.getDisplayName());
                                intent.putExtra("email", user.getEmail());
                                intent.putExtra("imageUrl", imageUrl);

                                startActivity(intent);
                                finish();
                            }
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

    // 레트로핏을 이용하여서 서버에서 기존 로그인 정보 획득
    private static void callRetrofit(LoginActivity loginActivity) {

        /* 비동기로 처리하면 id를 가져오는거보다 다른 작업이 먼저 처리되는 경우가 발생되므로
         * 동기로 처리, 네트워크 통신 문제때문에 asyncTask에서 작업
         */
        LoginAsyncTask loginTask = new LoginAsyncTask();

        loginTask.execute(new LoginRequestModel(loginType, uid));

        try {
            LoginResponseModel loginResponseModel = loginTask.get();
            id = loginResponseModel.getId();
            Log.d("checkId", id);

            WeakReference<LoginActivity> activity = new WeakReference<>(loginActivity);
            LoginActivity mActivity = activity.get();

            SharedPreferences pref = getSharedPreferences(mActivity);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("id", id);
            editor.apply();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 비동기로 처리할 때
        loginCall.enqueue(new Callback<LoginResponseModel>() {
            @Override
            public void onResponse(Call<LoginResponseModel> call, Response<LoginResponseModel> response) {
                Log.d("start!!!", "start");
                Log.d("response", response.code() + "");

                LoginResponseModel loginResponseModel = response.body();

                Log.d("loginBody: ", loginResponseModel.getId());
                id = loginResponseModel.getId();
                Log.d("loginEmail: ", loginResponseModel.getEmail());
                Log.d("phone: ", loginResponseModel.getPhone());
                Log.d("name: ", loginResponseModel.getName());
                Log.d("end!!!", "end");
            }

            @Override
            public void onFailure(Call<LoginResponseModel> call, Throwable t) {
                Log.d("Splash Retrofit: ", "Fail");
            }
        });
        */
    }

    public static SharedPreferences getSharedPreferences (Context ctxt) {
        return ctxt.getSharedPreferences("idPreference", MODE_PRIVATE);
    }
}
