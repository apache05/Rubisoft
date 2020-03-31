package com.rubisoft.bisexradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentForm;
import com.explorestack.consent.ConsentFormListener;
import com.explorestack.consent.ConsentInfoUpdateListener;
import com.explorestack.consent.ConsentManager;
import com.explorestack.consent.exception.ConsentManagerException;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rubisoft.bisexradar.Classes.Usuario;
import com.rubisoft.bisexradar.R;
import com.rubisoft.bisexradar.tools.utils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Activity_Inicio extends AppCompatActivity {
    private static final int REQUEST_GOOGLE_SIGN_IN = 3;
	private static final String CONSENT = "consent";
    private SharedPreferences perfil_usuario;
    private ProgressBar mProgressBar;
    private TextView TextView_titulo_principal;
    private Button Signin_button;
    private Button Signup_button;
    private Button Forgot_password_button;
    private LoginButton facebook_button;
    private SignInButton google_plus_button;
    private EditText mEmailField;
    private EditText mPasswordField;
    private String Token_socialauth;
    private AuthCredential credential;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
	private FirebaseFirestore db;

	private GoogleSignInClient mSignInClient;
	private ConsentForm consentForm;

	private void puente(){
		perfil_usuario = this.getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
		editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "facebook_935122549895970");
		editor_perfil_usuario.apply();

		Intent mIntent = new Intent(this, Activity_Principal.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(mIntent);
		this.finish();
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        super.onCreate(savedInstanceState);

        try {
			//puente();
			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                this.configura_facebook_login();
				initialize_firebase();
				if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo_mensaje")!=null) {
					procesa_extras();
				}else {

					resolveUserConsent();
					setContentView(R.layout.layout_inicio);
					perfil_usuario = this.getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
					setup_Views();  //Inicializa las Views
					setTypeFace_TextViews(); //ponemos la tipografía
					disable_all_buttons(); //para que no pulsen antes de hora
					empezar();
				}
            }
			else {
                //si no hay internet nos vamos a Activity_Sin_Conexion como siempre
                Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(mIntent);
                this.finish();
            }
        } catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de inicio");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        utils.gestiona_menu(item.getItemId(), this);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
			if (requestCode == REQUEST_GOOGLE_SIGN_IN) {  //Venimos de loggearnos con Google
				Task<GoogleSignInAccount> task =
						GoogleSignIn.getSignedInAccountFromIntent(data);
				if (task.isSuccessful()) {
					// Sign in succeeded, proceed with account
					GoogleSignInAccount acct = task.getResult();
					Token_socialauth = "googleplus_" + acct.getId();
					credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
					this.signin_with_firebaseAuth();
				} else {
					Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.ACTIVITY_INICIO_ERROR_LOGGIN), Toast.LENGTH_LONG).show();
				}
			}else{
				mCallbackManager.onActivityResult(requestCode, resultCode, data);
			}
        } catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de inicio");
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
    }

	private static Intent getIntent(Context context, boolean consent) {
		Intent intent = new Intent(context, Activity_Inicio.class);
		intent.putExtra(CONSENT, consent);
		return intent;
	}

    private void configura_facebook_login() {
        try {
            mCallbackManager = CallbackManager.Factory.create();
        } catch (Exception e) {
			utils.registra_error(e.toString(), "configura_facebook_login de inicio");
        }
    }

    private void setup_Views() {
        try {
            mProgressBar = this.findViewById(R.id.mProgressBar);
            TextView_titulo_principal = this.findViewById(R.id.Layout_inicio_TextView_nombre_app);
            //TextView_Choose_signin_method = (TextView) this.findViewById(R.id.Layout_inicio_TextView_Choose_signin_method);
            //TextView_Or = (TextView) this.findViewById(R.id.Layout_inicio_TextView_or);


            facebook_button = this.findViewById(R.id.Layout_inicio_AppCompatImageView_facebook);
            //twitter_button = this.findViewById(R.id.Layout_inicio_AppCompatImageView_twitter);
            google_plus_button = this.findViewById(R.id.Layout_inicio_AppCompatImageView_google_plus);

            Signin_button = this.findViewById(R.id.Layout_inicio_Button_signin);
            Signup_button = this.findViewById(R.id.Layout_inicio_Button_signup);

            Forgot_password_button = this.findViewById(R.id.Layout_inicio_Button_forgot_password);
			this.mEmailField = this.findViewById(R.id.Layout_inicio_EditText_Email);
			this.mPasswordField = this.findViewById(R.id.Layout_inicio_EditText_Password);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_Views de inicio");
        }
    }

    private void setTypeFace_TextViews() {
        try {
            Typeface typeFace_roboto_regular = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Regular.ttf");
            Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
            Typeface typeFace_roboto_bold = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Bold.ttf");

            TextView_titulo_principal.setTypeface(typeFace_roboto_regular);
            this.mEmailField.setTypeface(typeFace_roboto_light);
            this.mPasswordField.setTypeface(typeFace_roboto_light);
            this.Signin_button.setTypeface(typeFace_roboto_bold);
            this.Signup_button.setTypeface(typeFace_roboto_bold);

            this.Forgot_password_button.setTypeface(typeFace_roboto_bold);
        } catch (Exception e) {
			utils.registra_error(e.toString(), "setTypeFace_TextViews de inicio");
        }
    }

    private void setup_signin_with_email_y_password() {
        try {
            this.Signin_button.setOnClickListener(v -> {
				String email = mEmailField.getText().toString();
				String password = mPasswordField.getText().toString();
				if (validate_SignIn_Form(email, password)) {
					signIn_with_email_y_password(email, password);
				}
			});
        } catch (Exception e) {
			utils.registra_error(e.toString(), "setup_signin_with_email_y_password de inicio");
        }
    }

    private void setup_signup_with_email_y_password() {
        this.Signup_button.setOnClickListener(v -> {
			try {
				new MaterialDialog.Builder(Activity_Inicio.this)
						.theme(Theme.LIGHT)
						.title(getResources().getString(R.string.ACTIVITY_INICIO_SIGNUP))
						.customView(R.layout.dialog_signup, true)
						.positiveText(R.string.ok)
						.negativeText(R.string.Cancelar)
						.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
						.onPositive((dialog, which) -> {
							View view = dialog.getCustomView();

							EditText EditText_email = view.findViewById(R.id.Dialog_signup_email);
							EditText EditText_password1 = view.findViewById(R.id.Dialog_signup_password1);
							EditText EditText_password2 = view.findViewById(R.id.Dialog_signup_password2);

							String email = EditText_email.getText().toString();
							String password1 = EditText_password1.getText().toString();
							String password2 = EditText_password2.getText().toString();

							if (validate_SignUp_Form(email, password1)) {
								if (password1.equals(password2)) {
									createAccount_with_email_y_password(email, password1);
								} else {
									Toast.makeText(Activity_Inicio.this, getResources().getString(R.string.ACTIVITY_INICIO_PASSWORDS_DOESNT_MATCH), Toast.LENGTH_LONG).show();
								}
							}
						})
						.onNegative((dialog, which) -> dialog.show())
						.show();

			} catch (Exception e) {
				utils.registra_error(e.toString(), "setup_signup_with_email_y_password de inicio");
			}
		});
    }

    private void setup_google_sign_in() {
        try {
			GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
			mSignInClient = GoogleSignIn.getClient(this, options);

			google_plus_button.setOnClickListener(v -> {
				Intent intent = mSignInClient.getSignInIntent();
				startActivityForResult(intent, REQUEST_GOOGLE_SIGN_IN);
			});

        }catch(IllegalArgumentException | AndroidRuntimeException ignored) {
		}catch (Exception e) {
			utils.registra_error(e.toString(), "setup_google_sign_in de inicio");
        }
    }

    private void setup_facebook_sign_in() {
        AppEventsLogger.activateApp(this.getApplication());//para estadisticas
        // Initialize Facebook Login button
        this.facebook_button.setPermissions("email", "public_profile");
        facebook_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    if (!isFinishing() && !isDestroyed()) { //puede que ya no estemos en la activity
                        Token_socialauth = "facebook_" + loginResult.getAccessToken().getUserId();
                        credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                        LoginManager.getInstance().logOut();
                        signin_with_firebaseAuth();
                    }
                } catch (Exception e) {
					utils.registra_error(e.toString(), "setup_facebook_sign_in de inicio");
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Activity_Inicio.this, "Error  setup_facebook_sign_in" + error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setup_forgot_password() {
        try {
            this.Forgot_password_button.setOnClickListener(v -> {
				EditText edittext = new EditText(Activity_Inicio.this);
				edittext.setHint(getResources().getString(R.string.ACTIVITY_INICIO_HINT_EMAIL));
				edittext.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
				InputFilter[] filters = new InputFilter[1];
				filters[0] = new InputFilter.LengthFilter(140); //Filter to 140 characters
				edittext.setFilters(filters);

				new MaterialDialog.Builder(Activity_Inicio.this)
						.theme(Theme.LIGHT)
						.title(getResources().getString(R.string.ACTIVITY_INICIO_FORGOT_RESET_PASSWORD))
						.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
						.customView(edittext, true)
						.negativeText(R.string.Cancelar)
						.positiveText(R.string.ACTIVITY_INICIO_FORGOT_RESET)
						.onPositive((dialog, which) -> {
							try {
								EditText mDialog_reset_password_EditText_Escribe_Email = (EditText) dialog.getCustomView();

								String emailAddress = mDialog_reset_password_EditText_Escribe_Email.getText().toString();
								if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
									mAuth.sendPasswordResetEmail(emailAddress)
											.addOnCompleteListener(task -> {
												if (task.isSuccessful()) {
													Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_INICIO_EMAIL_SENT), Toast.LENGTH_LONG).show();
												} else {
													Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
												}
											});
								} else {
									Toast.makeText(getApplicationContext(), getString(R.string.ACTIVITY_INICIO_BAD_EMAIL), Toast.LENGTH_LONG).show();
								}
							} catch (Exception e) {
								utils.registra_error(e.toString(), "setup_forgot_password (MaterialDialog) de inicio");
							}
						})
						.onNegative((dialog, which) -> dialog.dismiss())
						.show();
			});
        } catch (Exception e) {
			utils.registra_error(e.toString(), "setup_forgot_password de inicio");
        }
    }

    private void disable_all_buttons() {
        facebook_button.setEnabled(false);
        //twitter_button.setEnabled(false);
        google_plus_button.setEnabled(false);
        Signin_button.setEnabled(false);
        Forgot_password_button.setEnabled(false);
        Signup_button.setEnabled(false);
    }

    private void enable_all_buttons() {
        facebook_button.setEnabled(true);
        //twitter_button.setEnabled(true);
        google_plus_button.setEnabled(true);
        Signin_button.setEnabled(true);
        Forgot_password_button.setEnabled(true);
        Signup_button.setEnabled(true);

    }

    private void empezar() {
        try {
            // A continuación intentamos cargar el Token_socialauth del usuario desde Sharedpreferences.
            String Token_SocialAuth = perfil_usuario.getString(this.getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
        
            if (Token_SocialAuth.isEmpty() /*|| (user == null)*/) {
                this.setup_google_sign_in();
                this.setup_facebook_sign_in();
                this.setup_signin_with_email_y_password();
                this.setup_signup_with_email_y_password();
                this.setup_forgot_password();
                this.enable_all_buttons();
            } else {
				suscribir_a_grupo();
				//Si sí que encontramos un Token_socialauth, quiere decir que la sesión se quedó abierta y podemos entrar
                // directamente.
                Intent mIntent1 = new Intent(this, Activity_Principal.class);
                mIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                this.startActivity(mIntent1);
                this.finish();
            }
        } catch (Exception e) {
			utils.registra_error(e.toString(), "empezar de inicio");
        }
    }

    private void createAccount_with_email_y_password(String email, String password) {
        try {
            if (mAuth!=null) {
                utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password).addOnFailureListener(this, e -> {
					Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show();
					utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);
				}).addOnCompleteListener(this, task -> {
					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);

					if (task.isSuccessful()) {
						signIn_with_email_y_password(email, password);
					} else {
						Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
            }else{
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                FirebaseApp.initializeApp(this);
                mAuth = FirebaseAuth.getInstance();
            }
        } catch (Exception e) {
			utils.registra_error(e.toString(), "createAccount_with_email_y_password de inicio");
        }
    }

    private void signIn_with_email_y_password(String email, String password) {
        try {
            if (mAuth!=null) {
                utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
					try {
						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);

						if (task.isSuccessful()) {


							String user_uid = task.getResult().getUser().getUid();
							credential = EmailAuthProvider.getCredential(email, password);
							Token_socialauth = email.substring(email.indexOf('@') + 1) + '_' + user_uid;
							utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);
							disable_all_buttons();
							get_mi_perfil(Token_socialauth);
							// new AsyncTask_login().execute();
						} else {
							Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				}).addOnFailureListener(this, e -> Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show());
            }else{
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                FirebaseApp.initializeApp(this);
                mAuth = FirebaseAuth.getInstance();
            }
            // [END sign_in_with_email]
        } catch (Exception e) {
			utils.registra_error(e.toString(), "signIn_with_email_y_password de inicio");
        }
    }

	private void signin_with_firebaseAuth() {

		if (mAuth!=null) {
			try {
				utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);

				mAuth.signInWithCredential(this.credential).addOnCompleteListener(this, task -> {
					try {
						if (!isFinishing() && !isDestroyed()) { //puede que ya no estemos en la activity
							// If sign in fails, display a message to the user. If sign in succeeds
							// the auth state listener will be notified and logic to handle the
							// signed in user can be handled in the listener.
							if (task.isSuccessful()) {
								try {
									utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);
									disable_all_buttons();
									get_mi_perfil(Token_socialauth);
								}catch (Exception e2){
									Toast.makeText(getApplicationContext(), "Error2: " + e2.getMessage(), Toast.LENGTH_LONG).show();
									utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);

								}
								// new AsyncTask_login().execute();
							} else {
								Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
								utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);
							}
						}
					} catch (Exception e) {
						utils.registra_error(e.toString(), "onCreate de inicio");
					}
				}).addOnFailureListener(this, e -> Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show());
			} catch (Exception e) {
				utils.registra_error(e.toString(), "signInWithCredential1 de inicio");
			}
		}else{
			try {
				Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
				FirebaseApp.initializeApp(this);
				mAuth = FirebaseAuth.getInstance();
			} catch (Exception e) {
				utils.registra_error(e.toString(), "signInWithCredential2 de inicio");
			}
		}

	}

    private boolean validate_SignIn_Form(String email, String password) {

        boolean valid = true;
        try {
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                this.mEmailField.setError("Required.");
                Toast.makeText(getApplicationContext(), getString(R.string.ACTIVITY_INICIO_BAD_EMAIL), Toast.LENGTH_LONG).show();
                valid = false;
            }

            if (TextUtils.isEmpty(password)) {
                this.mPasswordField.setError("Required.");
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
			utils.registra_error(e.toString(), "validate_SignIn_Form de inicio");
        }
        return valid;
    }

    private boolean validate_SignUp_Form(String email, String password) {

        boolean valid = true;
        try {
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getApplicationContext(), getString(R.string.ACTIVITY_INICIO_BAD_EMAIL), Toast.LENGTH_LONG).show();
                valid = false;
            }

            if (TextUtils.isEmpty(password)) {
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
			utils.registra_error(e.toString(), "validate_SignUp_Form de inicio");
        }
        return valid;
    }

    private void get_mi_perfil(String document){
    	try {
			DocumentReference docRef = db.collection(getResources().getString(R.string.USUARIOS)).document(document);
			docRef.get().addOnCompleteListener(task -> {
				utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);

				if (task.isSuccessful()) {
					DocumentSnapshot document1 = task.getResult();
					if (document1.exists()) {
						//Document exists  --> entramos
						login_succesful(document1.getId(), document1.getData());
					} else {
						//Document doesn't exist
						ir_a_registro();
					}
				} else {
					enable_all_buttons();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_INICIO_ERROR_LOGGIN), Toast.LENGTH_LONG).show();
				}
			});
		}catch(Exception e){
			utils.registra_error(e.toString(), "get_mi_perfil de inicio");

		}

	}

	private void login_succesful(String token_socialauth,Map<String,Object> perfil_encontrado){
		actualiza_token_FCM();
		Usuario un_perfil= new Usuario(perfil_encontrado);
		if (!un_perfil.getEs_premium()) {
			utils.actualizacion_semanal_de_estrellas(this, token_socialauth, un_perfil.getFecha_cobro_estrellas());
		}
		inicializa_sharedpreferences_perfil_usuario( token_socialauth,un_perfil);
		inicializa_sharedpreferences_busqueda(un_perfil);
		suscribir_a_grupo();
		Intent mIntent1 = new Intent(this, Activity_Principal.class);
		mIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(mIntent1);
		finish();
	}

	private void suscribir_a_grupo(){
		SharedPreferences perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
		FirebaseMessaging.getInstance().subscribeToTopic(grupo_al_que_pertenece);
	}

	private void ir_a_registro(){
		Intent mIntent2 = new Intent(this, Activity_Registro_Persona.class);
		mIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Bundle mBundle = new Bundle();
		mBundle.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), Token_socialauth);
		mIntent2.putExtras(mBundle);
		startActivity(mIntent2);
		finish();
    }

	private void inicializa_sharedpreferences_busqueda(@NonNull Usuario un_usuario) {

		SharedPreferences busqueda_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_BUSQUEDAS_USUARIO), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor_busqueda_usuario = busqueda_usuario.edit();
		if ((un_usuario.getSexo()== getResources().getInteger(R.integer.HOMBRE)) &&(un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.HETERO))) {
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.HETERO));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),1);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) &&(un_usuario.getOrientacion().intValue()==getResources().getInteger(R.integer.HETERO))){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA),  getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.HETERO));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),0);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()==getResources().getInteger(R.integer.HOMBRE)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.GAY)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.GAY));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),2);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.LESBIANA)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.LESBIANA));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),3);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.BISEX)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.BISEX));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),4);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.HOMBRE)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.BISEX)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.BISEX));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),5);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}

		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));

		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));

		Set default_razas = new HashSet();
		for (int i = 0; i < getResources().getStringArray(R.array.razas).length; i++) {
			default_razas.add(i);
		}
		//por defecto ponemos todas las razas activadas
		editor_busqueda_usuario.putStringSet(getResources().getString(R.string.BUSQUEDA_PERSONAS_RAZA), default_razas);

		int radio_de_busqueda;
		radio_de_busqueda =getResources().getInteger(R.integer.DEFAULT_RADIO) / 2;
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda);
		//editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_SEA_PROFESIONAL), false);
		editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_ESTE_ONLINE), false);

		editor_busqueda_usuario.apply();
	}

	private void inicializa_sharedpreferences_perfil_usuario(String token_socialauth,@NonNull Usuario un_perfil) {
		try {
			SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();

			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_PAIS), un_perfil.getPais());
			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_NICK), un_perfil.getNick());
			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), token_socialauth);
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), un_perfil.getOrientacion());
			if (un_perfil.getEstrellas() == null) {
				un_perfil.setEstrellas(0L);
			}
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), un_perfil.getEstrellas());
			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), un_perfil.getQuiero_dejar_claro_que());
			/*//editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PROFESIONAL), un_perfil.getProfesional());*/
			editor_perfil_usuario.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LATITUD), un_perfil.getLatitud().floatValue());
			editor_perfil_usuario.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LONGITUD), un_perfil.getLongitud().floatValue());
			editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), un_perfil.getEs_premium());

			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_SEXO), un_perfil.getSexo());
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_FECHA_NACIMIENTO), un_perfil.getFecha_nacimiento());
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_PESO), un_perfil.getPeso());
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ALTURA), un_perfil.getAltura());
		/*editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_TIENE_MOTO), un_perfil.getTiene_moto());
		editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_TIENE_COCHE), un_perfil.getTiene_coche());
		editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_TIENE_CASA), un_perfil.getTiene_casa());*/
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_RAZA), un_perfil.getRaza());

			Calendar hoy = Calendar.getInstance();

			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_FECHA_ULTIMO_ACCESO), hoy.getTimeInMillis());

			editor_perfil_usuario.apply();
		}catch (Exception e){
			utils.registra_error(e.toString(), "inicializa_sharedpreferences de inicio");

		}
	}

	private void initialize_firebase(){
		FirebaseApp.initializeApp(this);
		FirebaseAnalytics.getInstance(this);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();

	}

	private void actualiza_token_FCM() {
		try {
			String token_FCM= perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_FCM),"");
			if (!token_FCM.isEmpty()){
				db.collection(getResources().getString(R.string.USUARIOS)).document(Token_socialauth).update(getResources().getString(R.string.USUARIO_TOKEN_FCM), token_FCM);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "actualiza_token_FCM de inicio");
		}
	}

	private void procesa_extras(){
		try {
			Bundle data = getIntent().getExtras();
			String tipo_mensaje = (String) data.get("tipo_mensaje");
			if (tipo_mensaje.equals(getResources().getString(R.string.TIPO_MENSAJE_INDIVIDUAL))) {
				Intent mIntent = new Intent(this, Activity_Mensajes.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(mIntent);
				this.finish();
			} else if (tipo_mensaje.equals(getResources().getString(R.string.TIPO_MENSAJE_GRUPAL))) {
				String nick_de_quien_lo_dijo = (String) data.get("nick_de_quien_lo_dijo");
				String quien_lo_dijo = (String) data.get("quien_lo_dijo");
				String que_dijo = (String) data.get("que_dijo");
				Long hora = Calendar.getInstance().getTimeInMillis();
				utils.acomodar_conversacion_en_sharedpreferences(getApplicationContext(), quien_lo_dijo, que_dijo, hora, nick_de_quien_lo_dijo);

				Intent mIntent = new Intent(this, Activity_Chat_General.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(mIntent);
				this.finish();
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "procesa_extras de inicio");
		}
	}

	// Requesting Consent from European Users using Stack ConsentManager (https://wiki.appodeal.com/en/android/consent-manager).
	private void resolveUserConsent() {
		SharedPreferences sharedPreferences = getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_GDPR), Context.MODE_PRIVATE);
		if (!sharedPreferences.contains(getString(R.string.PREFERENCIAS_GDPR_CONSENT))) {
			ConsentManager consentManager = ConsentManager.getInstance(this);
			// Requesting Consent info update
			consentManager.requestConsentInfoUpdate(
					getResources().getString(R.string.APPODEAL_APP_KEY),
					new ConsentInfoUpdateListener() {
						@Override
						public void onConsentInfoUpdated(Consent consent) {
							Consent.ShouldShow consentShouldShow = consentManager.shouldShowConsentDialog();
							// If ConsentManager return Consent.ShouldShow.TRUE, than we should show consent form
							if (consentShouldShow == Consent.ShouldShow.TRUE) {
								showConsentForm();
							} else {
								// Start our main activity with default Consent value
								startMainActivity(false);
							}
						}

						@Override
						public void onFailedToUpdateConsentInfo(ConsentManagerException e) {
							// Start our main activity with default Consent value
							SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_GDPR), Context.MODE_PRIVATE);
							sharedPreferences.edit()
									.putBoolean(getString(R.string.PREFERENCIAS_GDPR_CONSENT), false)
									.apply();
							startMainActivity(false);
						}
					});
		}
	}

	// Displaying ConsentManger Consent request form
	private void showConsentForm() {
		if (consentForm == null) {
			consentForm = new ConsentForm.Builder(this)
					.withListener(new ConsentFormListener() {
						@Override
						public void onConsentFormLoaded() {
							// Show ConsentManager Consent request form
							consentForm.showAsActivity();
						}

						@Override
						public void onConsentFormError(ConsentManagerException error) {
							// Start our main activity with default Consent value
							SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_GDPR), Context.MODE_PRIVATE);
							sharedPreferences.edit()
									.putBoolean(getString(R.string.PREFERENCIAS_GDPR_CONSENT), false)
									.apply();
							startMainActivity(false);
						}

						@Override
						public void onConsentFormOpened() {
							//ignore
						}

						@Override
						public void onConsentFormClosed(Consent consent) {
							boolean hasConsent = consent.getStatus() == Consent.Status.PERSONALIZED && consent.getStatus() != Consent.Status.NON_PERSONALIZED;
							SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_GDPR), Context.MODE_PRIVATE);
							sharedPreferences.edit()
									.putBoolean(getString(R.string.PREFERENCIAS_GDPR_CONSENT), hasConsent)
									.apply();
							// Start our main activity with resolved Consent value
							startMainActivity(hasConsent);
						}
					}).build();
		}
		// If Consent request form is already loaded, then we can display it, otherwise, we should load it first
		if (consentForm.isLoaded()) {
			consentForm.showAsActivity();
		} else {
			consentForm.load();
		}
	}

	// Start our main activity with resolved Consent value
	private void startMainActivity(boolean hasConsent) {
		startActivity(getIntent(this, hasConsent));
		finish();
	}
}