package fr.epita.android.ghibliquizz

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import fr.epita.android.ghibliquizz.Interfaces.GhibliApiInterface
import fr.epita.android.ghibliquizz.Models.FilmObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_people_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PeopleDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_details)

        // retrieve the intent infos
        val originIntent = intent
        val baseUrl = originIntent.getStringExtra("FILM_BASE_URL")
        val filmId = originIntent.getStringExtra("FILM_ID")
        val correctValue = originIntent.getBooleanExtra("CORRECT", false)
        val peopleName = originIntent.getStringExtra("CHARACTER_NAME")

        if (correctValue) {
            correct.text = "RIGHT!"
            correct.setTextColor(Color.GREEN)
        } else {
            correct.text = "WRONG!"
            correct.setTextColor(Color.RED)
        }

        charactersView.text = peopleName

        // set the caller service
        val jsonConverter = GsonConverterFactory.create(GsonBuilder().create())
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .build()

        val service: GhibliApiInterface = retrofit.create(GhibliApiInterface::class.java)

        // set the callback for getting the chosen film
        val callbackFilm = object : Callback<FilmObject> {
            override fun onFailure(call: Call<FilmObject>, t: Throwable) {
                Log.d("HTTP_ERROR", "failed to get the film")
                Log.d("HTTP_ERROR", t.message)
            }

            override fun onResponse(call: Call<FilmObject>, response: Response<FilmObject>) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        val chosenFilm = response.body()!!

                        titleView.text = chosenFilm.title
                        synopsisView.text = chosenFilm.description
                        directorView.text = chosenFilm.director
                        yearView.text = chosenFilm.release_date



                        Log.d("HTTP_SUCCESS", "retrieved the film")
                    } else {
                        Log.d("HTTP_ERROR", "empty response")
                    }
                } else {
                    Log.d("HTTP_ERROR", "bad response code received: $rCode")
                }
            }
        }

        service.getFilmDetail(filmId).enqueue(callbackFilm)
    }
}
