package com.example.outkeeper

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

import java.util.*

// Create a new person!
class AddNewPersonActivity : ComponentActivity(),APIService {
    private lateinit var photoPickerLauncher: ActivityResultLauncher<String>
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        var id = 0;
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_person)
        val textView = findViewById<TextView>(R.id.editTextTextPersonName)
        println("Size "+textView.text.toString().length)


        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris != null) {
                for (uri in uris) {
                    // Handle the selected photo here
                    if (uri != null) {
                        id += 1
                        val file = createTempFile("dasd", ".jpg")
                        val inputStream = contentResolver.openInputStream(uri)
                        val f = inputStream?.readBytes()
                        if (f != null) {
                            file.writeBytes(f)
                            runBlocking {
                                //Checks name length
                                if (textView.text.toString().length<20) {
                                    putMethod(file, textView.text.toString() + "/photo-" + id)
                                }
                                else {
                                    Toast.makeText(this@AddNewPersonActivity,"Maximum characters is 20", Toast.LENGTH_LONG).show();
                                    recreate()
                                }
                            }
                        }
                        val intent = Intent()
                        println("New person photos added")
                        setResult(Activity.RESULT_OK, intent)

                        finish()
                    }
                }
            }
        }
    }

   fun addNewPerson(v:View){
        photoPickerLauncher.launch("image/*")
       println("New person added")
    }

    override suspend fun createPhoto(api_endpoint: String): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getPhotoMetadata(api_endpoint: String): Void {
        TODO("Not yet implemented")
    }


    override suspend fun uploadPhoto(
        employeeId: String,
        requestBody: RequestBody
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getPhoto(): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePhoto(employeeId: String): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

}

