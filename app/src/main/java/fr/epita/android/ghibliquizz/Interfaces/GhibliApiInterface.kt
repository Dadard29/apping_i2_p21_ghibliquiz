package fr.epita.android.ghibliquizz.Interfaces

import fr.epita.android.ghibliquizz.Models.FilmObject
import fr.epita.android.ghibliquizz.Models.PeopleObject
import retrofit2.Call
import retrofit2.http.GET

interface GhibliApiInterface {

    @GET("films")
    fun listFilms() : Call<ArrayList<FilmObject>>

    @GET("people")
    fun listPeople() : Call<ArrayList<PeopleObject>>
}