package com.example.outkeeper


import PersonAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*


class ShowPeopleActivity : Activity(),APIService{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_people_layout)
        var raw_json_response = "response"
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        runBlocking {
            raw_json_response = getMethod()
        }
        println(raw_json_response)
        // Lists all bucket photos from GET resposne
        val all_photos = findPhotosInResponse(raw_json_response)
        var names = listOf<String>()
        for (photo in all_photos)
        {
            // Extracts person name (bucket folder) from photo path
            val name = photo.substringBefore("/")
            if (!names.contains(name)) {
                names += name
            }
        }
        println(names)
        var people = listOf<Person>(
            )
        for(name in names)
        {
            // Create person from each person name
            val person = Person(name, listOf())
            for (photo in all_photos)
            {
                val name = photo.substringBefore("/")
                if (name == person.name)
                {
                    // Adds photos to Person
                    person.photos += "https://1ulwsnilg3.execute-api.eu-central-1.amazonaws.com/v1/s3/?key=my-facerecognizerbucket-84808/$photo"
                }
            }
            people += person
        }
        val adapter = PersonAdapter(people,this)
        recyclerView.adapter = adapter
    }

    fun addNewPerson(view:View)
    {
        val intent = Intent(this, AddNewPersonActivity::class.java)
        startActivityForResult(intent,1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // For adding a new person
            recreate()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }
   fun train_recognizer(view: View)
    {
        runBlocking {
        try {
            postMethod(
                "train_recognizer"
            )
            Toast.makeText(applicationContext, "Succesfully trained recognizer", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception)
        {
            // Training takes longer than 30 seconds
            Toast.makeText(applicationContext, "Training recognizer is taking too long. It will continue running on the server side and may take up to two extra minutes.", Toast.LENGTH_LONG).show()
        }
        }

    }
    fun findPhotosInResponse(response:String): List<String> {
        var response = response
        var list = listOf<String>() //found photos
        while(true)
        {
            var new_item = "" //new photo
            new_item = response.substringAfter("<Key>").substringBefore("</Key>")
            if(new_item .length > 30)
            {
                break
            }

            if (new_item.substringAfter("/").isNotEmpty()) {
                // To ensure photo name is not empty
                list += new_item
                println("New item: "+new_item)
            }
            var s1 = response.substring(response.indexOf("</Key>") + 6);
            s1.trim()
            response = s1

        }
        println("Photos list: "+list.toString())
        return list
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
