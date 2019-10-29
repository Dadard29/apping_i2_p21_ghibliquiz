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

    private fun checkIfCorrectPeople(peopleName: String): Boolean {
        return peopleName == goodPeople.name
    }

    // intent to go to the film detail activity
    private fun goToDetails(peopleName: String) {
        val explicitIntent = Intent(this, PeopleDetails::class.java)

        explicitIntent.putExtra("FILM_ID", chosenFilm.id)
        explicitIntent.putExtra("CHARACTER_NAME", goodPeople.name)
        explicitIntent.putExtra("CORRECT", checkIfCorrectPeople(peopleName))
        explicitIntent.putExtra("FILM_BASE_URL", this.baseUrl)

        startActivity(explicitIntent)
    }

    // set the recycler view
    fun initListWithAnswers(answsers: ArrayList<PeopleObject>) {
        val itemClickListener = View.OnClickListener {
            val peopleName = it.tag as String
            Log.d("TEST", "clicked on row $peopleName")

            goToDetails(peopleName)
        }

        answersView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))

        answersView.setHasFixedSize(true)
        answersView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        answersView.adapter = AnswerListAdapter(this, answsers, itemClickListener)
    }

    // get the answers from the peopleList
    fun getAnswers(peopleList: ArrayList<PeopleObject>, chosenFilmId: String): ArrayList<PeopleObject> {
        val answers: ArrayList<PeopleObject> = arrayListOf()

        var foundGoodCharacter = false
        for (p in peopleList) {
            // already found one, now need to add non compliants ones
            if (foundGoodCharacter) {
                if (! p.films.contains("https://ghibliapi.herokuapp.com/films/$chosenFilmId") && answers.size < 6) {
                    answers.add(p)
                }
            } else {
                // not found yet
                if (p.films.contains("https://ghibliapi.herokuapp.com/films/$chosenFilmId") && answers.size < 6) {
                    answers.add(p)
                    foundGoodCharacter = true
                }

            }
        }
        answers.forEach { Log.d("APP", it.name) }

        return answers
    }

    // chose the film from the list
    fun getChosenFilmId(peopleList: ArrayList<PeopleObject>): String {
        val r = Random().nextInt(peopleList.size)
        val goodAnswer = peopleList[r].films[0]
        goodPeople = peopleList[r]
        Log.d("APP", "set the good character as ${goodPeople.name}")
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
                Log.d("APP", "failed to get the film")
                Log.d("APP", t.message)
            }

            override fun onResponse(call: Call<FilmObject>, response: Response<FilmObject>) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        chosenFilm = response.body()!!

                        // set the film in the question
                        question.text = "Which one of these characters can be found in the movie ${chosenFilm.title} ?"

                        Log.d("APP", "retrieved the film")
                    } else {
                        Log.d("APP", "empty response")
                    }
                } else {
                    Log.d("APP", "bad response code received: $rCode")
                }
            }
        }

        // set the callback for setting up the people
        val callbackPeople = object : Callback<ArrayList<PeopleObject>> {
            override fun onFailure(call: Call<ArrayList<PeopleObject>>, t: Throwable) {
                Log.d("APP", "failed to list the people")
                Log.d("APP", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<PeopleObject>>,
                response: Response<ArrayList<PeopleObject>>
            ) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        val peopleList = response.body()!!
                        Log.d("APP", "retrieved people list")

                        val chosenFilmId: String = getChosenFilmId(peopleList)

                        val answers: ArrayList<PeopleObject> = getAnswers(peopleList, chosenFilmId)

                        initListWithAnswers(answers)

                        Log.d("APP", "chosen film $chosenFilmId")

                        // requests the film of the chosen people
                        Log.d("APP", "requesting the film...")
                        service.getFilmDetail(chosenFilmId).enqueue(callbackFilm)


                    } else {
                        Log.d("APP", "empty response")
                    }
                } else {
                    Log.d("APP", "bad response code received: $rCode")
                }
            }
        }

        // request people and select the possible answers
        Log.d("APP", "requesting people...")
        service.listPeople().enqueue(callbackPeople)
    }
}
