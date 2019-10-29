package fr.epita.android.ghibliquizz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import fr.epita.android.ghibliquizz.Interfaces.GhibliApiInterface
import fr.epita.android.ghibliquizz.Models.FilmObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://ghibliapi.herokuapp.com/"
    private var filmList: ArrayList<FilmObject> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // set the caller service
        val jsonConverter = GsonConverterFactory.create(GsonBuilder().create())
        val retrofit = Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(jsonConverter)
            .build()

        val service: GhibliApiInterface = retrofit.create(GhibliApiInterface::class.java)

        // set the callback for listing the films
        val callback = object : Callback<ArrayList<FilmObject>> {
            override fun onFailure(call: Call<ArrayList<FilmObject>>, t: Throwable) {
                Log.d("HTTP_ERROR", "failed to list the films")
            }

            override fun onResponse(
                call: Call<ArrayList<FilmObject>>,
                response: Response<ArrayList<FilmObject>>
            ) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        filmList = response.body()!!

                        Log.d("HTTP_SUCCESS", "retrieve film list")
                    } else {
                        Log.d("HTTP_ERROR", "empty response")
                    }
                } else {
                    Log.d("HTTP_ERROR", "bad response code received: $rCode")
                }
            }
        }
    }
}
