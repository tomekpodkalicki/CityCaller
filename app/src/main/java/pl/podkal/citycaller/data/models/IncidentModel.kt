package pl.podkal.citycaller.data.models

data class IncidentModel(
    var id: String? = null,
    val userId: String? = null,
    val desc: String? = null,
    val location: LocationModel? = null,
    val imageUrl: String? = null,
    val reactions: Int? = null
    )
