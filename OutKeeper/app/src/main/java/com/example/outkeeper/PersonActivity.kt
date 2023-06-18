package com.example.outkeeper

import PhotoAdapter
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


// Add photos for existing person
class PersonActivity : ComponentActivity() {
    private lateinit var photoPickerLauncher: ActivityResultLauncher<String>
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.person)
        var person_label = findViewById<TextView>(R.id.textView_person)
        val person = intent.getSerializableExtra("person") as? Person
        if (person != null) {
            person_label.text = person.name
        }
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (person != null) {
            println(person.photos)
        }
        // Fills adapters with person photos
        val adapter = person?.let { PhotoAdapter(it.photos) }
        var photos_count = person?.photos?.count()
        recyclerView.adapter = adapter
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            // Add photos for existing person
            if (uris!=null) {
                for (uri in uris)
                {
                    photos_count = photos_count?.plus(1)
                    val file = createTempFile("dasd", ".jpg")
                    val inputStream = contentResolver.openInputStream(uri)
                    val f = inputStream?.readBytes()
                    if (f != null) {
                        println("not null")
                        file.writeBytes(f)
                        if (person != null) {
                            if (photos_count != null) {
                                println(person.name + "/photo-" + (photos_count).toString())
                                runBlocking {
                                    putMethod(
                                        file,
                                        person.name + "/photo-" + (photos_count).toString()
                                    )
                                }
                            }
                        }
                    }
                    if (person != null) {
                        println(person.photos)
                    }
                    println("new photo added")
                }
            }
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
}

    fun pickPhotos(v: View)
    {
        photoPickerLauncher.launch("image/*")
    }
    fun removePhoto(v:View)
    {
        deleteMethod("Tomas" + "/photo-2")
    }
}

