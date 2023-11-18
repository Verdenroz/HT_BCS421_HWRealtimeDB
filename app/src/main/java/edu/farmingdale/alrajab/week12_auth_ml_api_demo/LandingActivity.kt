package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLandingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

//request code for loading image from gallery
const val REQ_PICTURE = 0
class LandingActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        binding.logoutBtn.setOnClickListener { logout() }

        var userName = ""
        if(user?.displayName != null){
            userName = user.displayName!!
        }
        //Welcomes the user with their name
        binding.welcomeUser.text = "Hello ${userName}"

        binding.loadIamgeBtn.setOnClickListener {
            loadImageFromWeb(URL(binding.imageUrlField.text.toString()))
        }

        binding.loadGalleryBtn.setOnClickListener {
            loadImageFromGallery()
        }
        //if user has a profile pic, this will load an ImageView next to welcome user text
        if(user?.photoUrl != null){
            val userImageURL = URL(firebaseAuth.currentUser!!.photoUrl.toString())
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val bitmap = BitmapFactory.decodeStream(userImageURL.openConnection().getInputStream())
                withContext(Dispatchers.Main){
                    binding.userImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
            startActivity(Intent(this@LandingActivity,LoginActivity::class.java))

    }

    /**
     *This loads an image with a valid URL
     * Try "https://www.thegoandroid.com/wp-content/uploads/2021/05/Untitled-10.png"
     */
    private fun loadImageFromWeb(url: URL){
        try {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                withContext(Dispatchers.Main){
                    binding.imageHolder.setImageBitmap(bitmap)
                }
            }
        }
        catch (e: Exception){
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Will send an Intent to load an image from Gallery
     */
    private fun loadImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_PICTURE)
    }

    /**
     * Will receive result from loadImageFromGallery()
     * This will set the ImageView to selected Image from Gallery
     * You will need an image in either Google Drive or the emulator's gallery to actually choose an Image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_PICTURE){
            if(data != null){
                binding.imageHolder.setImageURI(data.data)
            }
        }
    }
}


