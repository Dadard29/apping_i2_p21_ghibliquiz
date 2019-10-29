package fr.epita.android.ghibliquizz.Interfaces

import fr.epita.android.ghibliquizz.Models.FilmObject
import fr.epita.android.ghibliquizz.Models.PeopleObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GhibliApiInterface {

    @GET("films")
    fun listFilms() : Call<ArrayList<FilmObject>>

    @GET("films/{id}")
    fun getFilmDetail(@Path("id") id: String) : Call<FilmObject>

    @GET("people")
    fun listPeople() : Call<ArrayList<PeopleObject>>
}