package com.example.aasha

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aasha.databinding.SignInActivityBinding
import com.example.aasha.model.User
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SignInActivity: AppCompatActivity() {

    private lateinit var binding: SignInActivityBinding
    private lateinit var user: User

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager
    private lateinit var logInManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignInActivityBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        binding.googleLogIn.setOnClickListener { handleGoogleSignIn() }
        binding.facebookLogIn.setOnClickListener { handleFacebookSignIn() }
        binding.logInText.setOnClickListener {
            Toast.makeText(
                this,
                "Yet to be implemented :(",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun handleFacebookSignIn() {
        logInManager = LoginManager.getInstance()
        logInManager.logInWithReadPermissions(this, listOf("public_profile"))
        callbackManager = CallbackManager.Factory.create()

        logInManager.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                firebaseAuthWithFacebook(result.accessToken)
            }

            override fun onCancel() {
                Log.e(TAG, "facebook log in cancelled")
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "error authenticating with Facebook", error)
            }
        })
    }

    private fun handleGoogleSignIn() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google sign in was successful, authenticate with firebase
                val account = task.getResult(ApiException::class.java)!!
                setNewUserContent(account.displayName!!, account.photoUrl!!)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google sign in failed, update the UI accordingly
                Toast.makeText(
                    this,
                    "There was an error signing in",
                    Toast.LENGTH_LONG
                ).show()
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signWithGoogleCredential: SUCCESS")
                    addToUsersList(auth.currentUser?.uid!!)
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "There was an error signing in",
                        Toast.LENGTH_LONG
                    ).show()
                    // Empty constructor
                    user = User()
                }
            }
    }

    private fun firebaseAuthWithFacebook(idToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(idToken.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setNewUserContent(auth.currentUser?.displayName!!, auth.currentUser?.photoUrl!!)
                    addToUsersList(auth.currentUser?.uid!!)
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "There was an error signing in",
                        Toast.LENGTH_LONG
                    ).show()
                    user = User()
                }
            }
    }

    private fun setNewUserContent(displayName: String, photoUrl: Uri) {
        user = User(displayName,
                    photoUrl.toString(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
        )
    }

    private fun addToUsersList(newChildKey: String) {
        db = Firebase.database

        db.reference.child(USER_LIST_CHILD).child(newChildKey)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        db.reference.child(USER_LIST_CHILD).child(newChildKey).setValue(user)
                        Log.w(TAG, "New user inserted to database")
                    } else Log.w(TAG, "User already exists")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Inside addToUserList()", error.toException())
                }
            })
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "SignInActivity"
        const val USER_LIST_CHILD = "users"
        private const val RC_SIGN_IN = 69
    }
}