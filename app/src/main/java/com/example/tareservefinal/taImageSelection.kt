package com.example.tareservefinal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


/**
 * Fragment Class to Handle Uploading Profile Picture for TaInfo that the student will see
 */
class taImageSelection : Fragment() {

    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private lateinit var progressSpinner: ProgressBar
    private lateinit var currTextView: TextView
    private lateinit var model: UserViewModel
    private lateinit var imageView: ImageView
    private lateinit var officeHoursText: EditText
    private lateinit var locationText: EditText
    private lateinit var officeHoursButton: Button
    private lateinit var locationButton: Button

    private val GALLERY_REQUEST_CODE = 1889

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ta_image_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model =
            (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java] }!!)

        database = FirebaseDatabase.getInstance().reference
        imageView = view.findViewById(R.id.currentImageView)
        progressSpinner = view.findViewById(R.id.progressBar)
        currTextView = view.findViewById(R.id.taImageLoading)
        officeHoursButton = view.findViewById(R.id.officeHourUpdateButton2)
        officeHoursText = view.findViewById(R.id.officeHoursEditText2)
        locationButton = view.findViewById(R.id.locationButton2)
        locationText = view.findViewById(R.id.locationEditText2)
        storage = Firebase.storage

        loadProfilePic()
        setupOfficeHours()
        setupLocation()

        view.findViewById<Button>(R.id.finishButton).setOnClickListener {
            view.findNavController().popBackStack()
        }

        val uploadButton = view.findViewById<Button>(R.id.setNewImageButton)
        uploadButton.setOnClickListener { uploadImage() }
    }

    private fun uploadImage() {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes =
            arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    var selectedImage: Uri = data.data!!

                    // TODO: get firebase storage and upload image
                    val storageRef = storage.reference
                    val fileName = model.userId + ".JPG"
                    val mountainRef = storageRef.child(fileName)


                    imageView.setImageURI(selectedImage)
                    progressSpinner.visibility = View.VISIBLE

                    // Get the data from an ImageView as bytes

                    val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    var uploadTask = mountainRef.putBytes(data)
                    currTextView.text = "Currently Uploading New Photo..."

                    uploadTask.addOnFailureListener {
                        currTextView.text = "New Image Failed to Load"
                        progressSpinner.visibility = View.INVISIBLE
                    }.addOnSuccessListener {
                        currTextView.text = "New Image Successfully Uploaded"
                        progressSpinner.visibility = View.INVISIBLE
                    }


                }
            }
        }

    }

    private fun loadProfilePic() {
        val imageEndpoint = "gs://tareservefinal.appspot.com/" + model!!.userId + ".JPG"

        val gsReference = storage.getReferenceFromUrl(imageEndpoint)
        val ONE_MEGABYTE: Long = 1024 * 1024 * 5
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(bmp)
            progressSpinner.visibility = View.INVISIBLE
            currTextView.text = model!!.userId + ".JPG Successfully loaded"
        }.addOnFailureListener {
            currTextView.text = "A picture doesn't exist or failed to load"
            progressSpinner.visibility = View.INVISIBLE
        }
    }

    private fun setupOfficeHours() {
        val model = (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java]})

        var userRef = database.child("Users").child(model!!.userId).child("TAData").child("Schedule")

        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                officeHoursText.setText(dataSnapshot.value.toString())
            }
        })

        officeHoursButton.setOnClickListener {
            userRef.setValue(officeHoursText.text.toString())
        }
    }

    private fun setupLocation() {
        val model = (activity?.let { ViewModelProvider(activity as FragmentActivity)[UserViewModel::class.java]})

        var userRef = database.child("Users").child(model!!.userId).child("TAData").child("Description")

        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                locationText.setText(dataSnapshot.value.toString())
            }
        })

        locationButton.setOnClickListener {
            userRef.setValue(locationText.text.toString())
        }
    }

}
