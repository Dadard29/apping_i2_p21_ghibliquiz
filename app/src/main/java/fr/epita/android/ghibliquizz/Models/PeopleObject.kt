package fr.epita.android.ghibliquizz.Models

class PeopleObject(
    val id: String,
    val name: String,
    val gender: String,
    val age: Int,
    val eye_color: String,
    val hair_color: String,
    val films: ArrayList<FilmObject>,
    val species: String,
    val url: String
)