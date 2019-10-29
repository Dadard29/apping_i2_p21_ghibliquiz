package fr.epita.android.ghibliquizz.Models

class FilmObject(
    val id: String,
    val title: String,
    val description: String,
    val director: String,
    val producer: String,
    val release_date: String,
    val rt_score: String,
    val people: ArrayList<String>,
    val species: ArrayList<String>,
    val locations: ArrayList<String>,
    val vehicules: ArrayList<String>,
    val url: String
)