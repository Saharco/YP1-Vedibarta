package com.technion.vedibarta.data.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.TextContainer
import com.technion.vedibarta.POJOs.Unfilled
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.RotateBitmap
import com.technion.vedibarta.utilities.VedibartaActivity
import java.io.ByteArrayOutputStream
import java.io.IOException

class ClassAddViewModel : ViewModel() {

    private val _event = MutableLiveData<ClassAddEvent>()

    var chosenClassPicture: Uri? = null
    var imageBytes: ByteArray = ByteArray(0)
    var chosenClassDescription: TextContainer = Unfilled
    var chosenClassName: TextContainer = Unfilled
    val event: LiveData<ClassAddEvent> = _event

    private fun uploadPhoto(imagePath: Uri, context: Context) {
        val resize = ImageCompressTask(context)
        resize.execute(imagePath)
    }

    private fun classWithPhoto(
        docRef: DocumentReference,
        name: String,
        description: String
    ) {
        val storageRef = FirebaseStorage.getInstance().reference.child("classes/${docRef}/photo")
        storageRef.putBytes(imageBytes)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        val photo = it.toString()
                        val clazz = Class(
                            id = docRef.id,
                            name = name,
                            teacherID = VedibartaActivity.userId!!,
                            description = description,
                            photo = photo
                        )
                        docRef.set(clazz)
                            .addOnSuccessListener {
                                chosenClassName = Unfilled
                                chosenClassDescription = Unfilled
                                chosenClassPicture = null
                                _event.value = ClassAddEvent.ClassAdded(clazz)
                            }
                            .addOnFailureListener {
                                _event.value =
                                    ClassAddEvent.DisplayError(R.string.something_went_wrong)
                            }
                    }
            }
    }

    private fun classWithoutPhoto(
        docRef: DocumentReference,
        name: String,
        description: String
    ) {
        val clazz = Class(
            id = docRef.id,
            name = name,
            teacherID = VedibartaActivity.userId!!,
            description = description
        )
        docRef.set(clazz)
            .addOnSuccessListener {
                chosenClassName = Unfilled
                chosenClassDescription = Unfilled
                chosenClassPicture = null
                _event.value = ClassAddEvent.ClassAdded(clazz)
            }
            .addOnFailureListener {
                _event.value =
                    ClassAddEvent.DisplayError(R.string.something_went_wrong)
            }
    }

    fun createClass() {
        val name = when (val name = chosenClassName) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_name); return
            }
            is Filled -> name.text
        }
        val description = when (val desc = chosenClassDescription) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_description); return
            }
            is Filled -> desc.text
        }
        val docRef = DatabaseVersioning.currentVersion.instance.collection("classes").document()

        if (chosenClassPicture == null)
            classWithoutPhoto(docRef, name, description)
        else
            classWithPhoto(docRef, name, description)
    }

    fun saveEditedClass() {
        val name = when (val name = chosenClassName) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_name); return
            }
            is Filled -> name.text
        }
        val description = when (val desc = chosenClassDescription) {
            is Unfilled -> {
                _event.value =
                    ClassAddEvent.DisplayMissingInfoDialog(R.string.teacher_class_list_missing_class_description); return
            }
            is Filled -> desc.text
        }

        val photo = chosenClassPicture?.toString()

        val clazzMap = mutableMapOf<String, String?>(
            "name" to name,
            "description" to description,
            "photo" to photo,
            "photoBytes" to imageBytes.toString()
        )
        _event.value = ClassAddEvent.ClassEdited(clazzMap)

    }


    fun uploadImage(image: Uri?, context: Context) {
        if (image != null) {
            uploadPhoto(image, context)
            chosenClassPicture = image
        }
    }

    sealed class ClassAddEvent {
        var handled = false

        class FinishPictureLoading : ClassAddEvent()
        class StartPictureLoading : ClassAddEvent()
        data class ClassAdded(val clazz: Class) : ClassAddEvent()
        data class ClassEdited(val clazzMap: Map<String, String?>) : ClassAddEvent()
        data class DisplayMissingInfoDialog(val msgResId: Int) : ClassAddEvent()
        data class DisplayError(val msgResId: Int) : ClassAddEvent()
    }

    private inner class ImageCompressTask(private val context: Context) :
        AsyncTask<Uri, Int, ByteArray>() {

        override fun onPreExecute() {
            _event.value = ClassAddEvent.StartPictureLoading()
        }

        override fun doInBackground(vararg uris: Uri): ByteArray? {
            return try {
                val rotateBitmap = RotateBitmap()
                val bitmap = rotateBitmap.handleSamplingAndRotationBitmap(context, uris[0])!!
                val bytes = getBytesFromBitmap(
                    bitmap,
                    VedibartaActivity.IMAGE_COMPRESSION_QUALITY_IN_PERCENTS
                )
                bytes
            } catch (e: IOException) {
                Log.d("sss", e.toString())
                null
            }
        }

        override fun onPostExecute(bytes: ByteArray) {
            super.onPostExecute(bytes)
            _event.value = ClassAddEvent.FinishPictureLoading()
            imageBytes = bytes
        }

        private fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return stream.toByteArray()
        }
    }
}