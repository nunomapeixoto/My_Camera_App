package com.example.mycameraapp.model


import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycameraapp.Photo
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.roundToInt


class FirebaseModel {

    var firebaseUser : FirebaseUser? = null
    var uploadProgressObservable: BehaviorSubject<Int> = BehaviorSubject.create()
    var authResults: BehaviorSubject<Int> = BehaviorSubject.create()
    var storageResults: BehaviorSubject<Int> = BehaviorSubject.create()
    var photosObservableList: BehaviorSubject<List<Photo>> = BehaviorSubject.create()
    var photoObservable: BehaviorSubject<Photo> = BehaviorSubject.create()

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun listPhotos() {
        val listRef = storage.reference.child("images/${auth.currentUser?.uid}")

        val photosList = mutableListOf<Photo>()

        listRef.listAll()
            .addOnSuccessListener { listResult ->

                listResult.items.forEach { item ->

                    val uri = Uri.parse(item.path)
                    val lastSegment = uri.lastPathSegment
                    val splits = lastSegment?.split("_")
                    val name = splits?.get(0)
                    val date = splits?.get(1)?.split(".")?.get(0)

                    photosList.add(
                        Photo(
                            name.toString(),
                            date.toString(),
                            item.path
                        )
                    )

                }

                photosObservableList.onNext(photosList)
            }
            .addOnFailureListener {

                storageResults.onNext(LISTING_FAILED)

            }
    }

    fun uploadPhoto(photo: Photo) {

        val storageRef = storage.reference

        val imagesRef = storageRef.child("images/${auth.currentUser?.uid}/${photo.name}_${photo.date}.jpg")

        val file = Uri.parse(photo.uri)
        val uploadTask = imagesRef.putFile(file)
        uploadTask.addOnProgressListener {

            uploadProgressObservable.onNext((100.0 * it.bytesTransferred / it.totalByteCount).roundToInt())

        }

        uploadTask.addOnFailureListener {
            storageResults.onNext(UPLOAD_FAILED)
        }
        uploadTask.addOnSuccessListener {
            if (it.task.isSuccessful) {

                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    photoObservable.onNext(
                        Photo(
                            photo.name,
                            photo.date,
                            storage.getReferenceFromUrl(uri.toString()).path
                        )
                    )
                }
            } else {
                storageResults.onNext(UPLOAD_FAILED)
            }
        }
    }

    fun login(user: String, pwd: String, activity: AppCompatActivity) {

        auth.signInWithEmailAndPassword(user, pwd)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    firebaseUser = auth.currentUser
                    authResults.onNext(LOGIN_SUCCESS)

                } else {
                    throwException(task.exception!!, activity)
                    authResults.onNext(LOGIN_FAILED)
                }
            }

    }

    fun register(user: String, pwd: String, activity: AppCompatActivity){

        auth.createUserWithEmailAndPassword(user, pwd)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    firebaseUser = auth.currentUser
                    authResults.onNext(REGISTER_SUCCESS)
                } else {
                    throwException(task.exception!!, activity)

                    authResults.onNext(REGISTER_FAILED)

                }

            }
    }

    private fun throwException(exception: Exception, activity: AppCompatActivity) {
        try {
            throw exception
        } catch (e: FirebaseAuthWeakPasswordException) {
            Toast.makeText(activity, "Password tem de ter pelo menos 6 caracteres.",
                Toast.LENGTH_SHORT).show()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(activity, "Email e/ou password inválidos.",
                Toast.LENGTH_SHORT).show()
        } catch (e: FirebaseAuthUserCollisionException) {
            Toast.makeText(activity, "Email já em uso.",
                Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Register Exception", e.message)
        }
    }

    companion object {

        const val LOGIN_SUCCESS = 1
        const val LOGIN_FAILED = 2
        const val REGISTER_SUCCESS = 3
        const val REGISTER_FAILED = 4

        const val UPLOAD_FAILED = 5
        const val LISTING_FAILED = 6

        @Volatile
        private var instance: FirebaseModel? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance
                    ?: FirebaseModel().also { instance = it }
            }
    }

}