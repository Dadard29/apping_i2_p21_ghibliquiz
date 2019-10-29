package fr.epita.android.ghibliquizz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import fr.epita.android.ghibliquizz.Adapters.AnswerListAdapter
import fr.epita.android.ghibliquizz.Interfaces.GhibliApiInterface
import fr.epita.android.ghibliquizz.Models.FilmObject
import fr.epita.android.ghibliquizz.Models.PeopleObject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://ghibliapi.herokuapp.com/"

    // instanciate an empty filmobject
    private var goodPeople = PeopleObject(
        "", "", "", "", "", "", ArrayList(), "", ""
    )

    private var chosenFilm = FilmObject(
        "", "", "", "", "", "", "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), ""
    )

    private fun getFilmIdFromUrl(url: String): String {
        return url.substring("https://ghibliapi.herokuapp.com/films/".length)
    }

    private fun checkIfCorrectPeople(filmId: String): Boolean {
        for (f in goodPeople.films) {
            if (getFilmIdFromUrl(f) == filmId) {
                return true
            }
        }
        return false
    }


    private fun goToDetails(filmId: String, peopleId: String) {
        val explicitIntent = Intent(this, PeopleDetails::class.java)

        explicitIntent.putExtra("FILM_ID", filmId)
        explicitIntent.putExtra("CORRECT", checkIfCorrectPeople(filmId))
        explicitIntent.putExtra("FILM_BASE_URL", this.baseUrl)

        startActivity(explicitIntent)
    }

    fun initListWithAnswers(answsers: ArrayList<PeopleObject>) {
        val itemClickListener = View.OnClickListener {
            val peopleId = it.tag as String
            Log.d("TEST", "clicked on row $peopleId")

            goToDetails(this.chosenFilm.id, peopleId)
        }

        answersView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))

        answersView.setHasFixedSize(true)
        answersView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        answersView.adapter = AnswerListAdapter(this, answsers, itemClickListener)
    }

    fun getAnswers(peopleList: ArrayList<PeopleObject>): ArrayList<PeopleObject> {
        val answers: ArrayList<PeopleObject> = arrayListOf()

        for (i in 0..4) {
            answers.add(peopleList[i])
        }
        return answers
    }

    fun getChosenFilmId(answers: ArrayList<PeopleObject>): String {
        val r = Random().nextInt(answers.size)
        val goodAnswer = answers[r].films[0]
        goodPeople = answers[r]
        return getFilmIdFromUrl(goodAnswer)
    }

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
                        chosenFilm = response.body()!!


                        // set the film in the question
                        question.text = "Which one of these characters can be found in the movie ${chosenFilm.title} ?"



                        Log.d("HTTP_SUCCESS", "retrieved the film")
                    } else {
                        Log.d("HTTP_ERROR", "empty response")
                    }
                } else {
                    Log.d("HTTP_ERROR", "bad response code received: $rCode")
                }
            }
        }

        // set the callback for setting up the people
        val callbackPeople = object : Callback<ArrayList<PeopleObject>> {
            override fun onFailure(call: Call<ArrayList<PeopleObject>>, t: Throwable) {
                Log.d("HTTP_ERROR", "failed to list the people")
                Log.d("HTTP_ERROR", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<PeopleObject>>,
                response: Response<ArrayList<PeopleObject>>
            ) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        val peopleList = response.body()!!
                        Log.d("HTTP_SUCCESS", "retrieved people list")

                        val answers: ArrayList<PeopleObject> = getAnswers(peopleList)

                        initListWithAnswers(answers)

                        val chosenFilmId: String = getChosenFilmId(answers)
                        Log.d("HTTP_SUCCESS", "chosen film $chosenFilmId")

                        // requests the film of the chosen people
                        Log.d("HTTP_SUCCESS", "requesting the film...")
                        service.getFilmDetail(chosenFilmId).enqueue(callbackFilm)


                    } else {
                        Log.d("HTTP_ERROR", "empty response")
                    }
                } else {
                    Log.d("HTTP_ERROR", "bad response code received: $rCode")
                }
            }
        }

        // request people and select the possible answers
        Log.d("HTTP", "requesting people...")
        service.listPeople().enqueue(callbackPeople)
    }
}
