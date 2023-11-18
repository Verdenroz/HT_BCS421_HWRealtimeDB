package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLoginBinding


const val REQ_GOOGLE = 2
class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDB = FirebaseDatabase.getInstance()

        //creates the GoogleSignInOptions for when user clicks Google Login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //create Google Sign In Client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signUpTv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.loginBtn.setOnClickListener {
            login()
        }
        binding.signInButton.setOnClickListener {
            googleSignIn()
        }

    }

    /**
     * Starts the Sign in Process for Google
     */
    private fun googleSignIn(){
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, REQ_GOOGLE)
    }

    /**
     * Result is given from googleSignIn
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if requestcode is for handling google sign in
        if(requestCode == REQ_GOOGLE){
            //get account from data intent
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                //send to FireBase to authenticate
                authenticateWithFirebase(account.idToken)
                Log.d("Success", "onActivityResult")
            }
            /*
            I have found that only Emulators with Play Store can sign in with Google
            If you see this message, you must either switch to an emulator with Play Services
            Or create a new emulator with the Play Store (When creating the emulator you should see an Icon under 'Play Store' Column
             */
            catch (e: Exception){
                Toast.makeText(this, "You need an emulator with Google Play Services", Toast.LENGTH_SHORT).show()
                Log.d("Fail", e.message!!)

            }
        }
    }

    /**
     * Firebase will authenticate with google credentials
     */
    private fun authenticateWithFirebase(idToken: String?) {
        Log.d("authentication", "Firebase Authenticate")
        //creates credential from Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        //Send to firebase to authenticate with Google credential
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                val userDetails = firebaseAuth.currentUser
                if (userDetails != null) {
                    //creates data holder for signed in user
                    val user = User(userDetails?.uid, userDetails?.displayName)
                    //stores user data to DB
                    firebaseDB.getReference().child("Users").child(userDetails.uid).setValue(user)
                    //Go to Landing Activity
                    val intent = Intent(this, LandingActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Login With Google", Toast.LENGTH_SHORT).show()
            }
    }


    private fun login() {
        val email = binding.emailET.text.toString()
        val pass = binding.passET.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty()) {

            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this, LandingActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.d("EmailPass", it.exception.toString())
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                }
            }
        } else {
            Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

        }
    }


}