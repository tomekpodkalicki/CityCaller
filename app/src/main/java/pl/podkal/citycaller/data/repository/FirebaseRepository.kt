package pl.podkal.citycaller.data.repository

import android.net.Uri
import android.util.Log
import androidx.privacysandbox.ads.adservices.adid.AdId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.data.models.UserModel
import java.io.File

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUser() = auth.currentUser


    suspend fun loginUser(email: String, password: String):
            FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, password)
            .await()
            .user
    }

    suspend fun registerNewUserByEmail(
        email: String,
        password: String,
        name: String
    ):
            FirebaseUser? {

        val firebaseUser = auth
            .createUserWithEmailAndPassword(email, password)
            .await()
            .user

        addNewUserToDatabase(
            firebaseUser, name
        )

        return firebaseUser

    }

    private suspend fun addNewUserToDatabase(
        firebaseUser:
        FirebaseUser?,
        name: String
    ) {
        firebaseUser ?: throw Exception("User cannot be null")

        val user = UserModel(
            id = firebaseUser.uid,
            name = name,
            email = firebaseUser.email ?: ""
        )

        db.collection("users")
            .add(user)
            .await()
    }

    suspend fun getAllIncidents(): List<IncidentModel> {
        return db.collection("incidents")
            .get()
            .await()
            .toObjects(IncidentModel::class.java)
            .toList()


    }

    suspend fun getUserIncidents(userId: String?): List<IncidentModel> {
        userId?: return emptyList()

        return db.collection("incidents")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(IncidentModel::class.java)
            .toList()
    }

    suspend fun uploadIncidentData(
        incidentModel: IncidentModel,
        file: File
    ) {
        val fileUri = Uri.fromFile(file)
        val refe = "images/${incidentModel.userId}/${fileUri.lastPathSegment}"
        val photoRefe = storage.reference
            .child(refe)

        photoRefe.putFile(fileUri)
            .await()
        
        insertIncident(incidentModel, refe)

    }

    private suspend fun insertIncident(incidentModel: IncidentModel, refe: String) {

        val downloadUrl = storage
            .getReference(refe)
            .downloadUrl
            .await()
            .toString()

        incidentModel.imageUrl = downloadUrl

        db.collection("incidents")
            .document()
            .also {
                incidentModel.apply {
                    id = it.id
                }
                it.set(incidentModel)
            }
    }

    suspend fun updateIncident(incidentModel: IncidentModel) = withContext(Dispatchers.IO) {
        db
            .collection("incidents")
            .document(incidentModel.id ?: throw Exception("Incydent bez id"))
            .set(incidentModel)
    }

    fun singOut() {
        auth.signOut()
    }


}