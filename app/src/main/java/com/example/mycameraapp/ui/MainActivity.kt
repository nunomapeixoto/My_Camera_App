package com.example.mycameraapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycameraapp.*
import com.example.mycameraapp.model.FirebaseModel
import com.example.mycameraapp.view_model.FirebaseViewModel
import com.example.mycameraapp.view_model.FirebaseViewModelFactory
import com.example.mycameraapp.view_model.PhotoViewModel
import com.example.mycameraapp.view_model.PhotoViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.leinardi.android.speeddial.SpeedDialActionItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    PhotosAdapter.PhotoAdapterListener {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1
    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_SELECT_PHOTO = 2

    private var adapter: PhotosAdapter =
        PhotosAdapter(mutableListOf(), this)

    private lateinit var firebaseViewModelFactory: FirebaseViewModelFactory
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var photoViewModelFactory: PhotoViewModelFactory
    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    private var currentUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        supportActionBar?.subtitle = auth.currentUser?.email

        upload_progress_bar.hide()
        setViewsVisibility(true)

        photoViewModelFactory =
            Injection.providePhotoViewModelFactory(
                applicationContext
            )
        photoViewModel = ViewModelProvider(this, photoViewModelFactory).get(PhotoViewModel::class.java)

        firebaseViewModelFactory =
            Injection.provideFirebaseViewModelFactory()
        firebaseViewModel = ViewModelProvider(this, firebaseViewModelFactory).get(FirebaseViewModel::class.java)

        compositeDisposable = CompositeDisposable()
//        compositeDisposable.add(photoViewModel.getPhotos()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { photos ->
//                adapter = PhotosAdapter(photos, this@MainActivity)
//                recyclerView.adapter = adapter
//                setViewsVisibility()
//            })

        fab_speed_dial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.select_photo,
                R.drawable.ic_photo_album_white_24dp
            )
                .setLabelBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorPreferencesItem
                ))
                .setLabelColor(Color.WHITE)
                .setLabel(getString(R.string.select_photo))
                .create())

        fab_speed_dial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.take_photo,
                R.drawable.ic_camera_alt_white_24dp
            )
                .setLabelBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorPreferencesItem
                ))
                .setLabelColor(Color.WHITE)
                .setLabel(getString(R.string.take_photo))
                .create())

        fab_speed_dial.setOnActionSelectedListener { actionItem ->
            when (actionItem?.id) {
                R.id.select_photo -> {
                    checkForPermissions()
                }
                R.id.take_photo -> {
                    dispatchTakePictureIntent()
                }
            }
            false
        }

        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        recycler_view.adapter = adapter

        setObservers()

        firebaseViewModel.getPhotosList()


    }


    private fun setObservers() {
        compositeDisposable.add(firebaseViewModel.getPhotos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.setList(it.toMutableList())
                setViewsVisibility(false)
            })

        compositeDisposable.add(firebaseViewModel.getNewUploadedPhoto()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.addPhoto(it)
                upload_progress_bar.hide()
                setViewsVisibility(false)
            })

        compositeDisposable.add(firebaseViewModel.getUploadProgress()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                upload_progress_bar.progress = it
            })

        compositeDisposable.add(firebaseViewModel.getStorageResults()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when(it) {
                    FirebaseModel.LISTING_FAILED -> {
                        Toast.makeText(
                            this,
                            "Não foi possível sincronizar com o firebase.",
                            Toast.LENGTH_SHORT).show()
                        setViewsVisibility(false)
                    }
                    FirebaseModel.UPLOAD_FAILED -> {
                        Toast.makeText(
                            this,
                            "Não foi possível guardar a foto no firebase.",
                            Toast.LENGTH_SHORT).show()
                        upload_progress_bar.hide()
                        setViewsVisibility(false)
                    }
                }
            })
    }

    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE)

        }
        else {
            dispatchSelectPictureIntent()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                dispatchSelectPictureIntent()
            } else {
                //sem permissao
            }
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }

    private fun dispatchSelectPictureIntent() {
        Intent(Intent.ACTION_PICK).also { selectPictureIntent ->
            selectPictureIntent.resolveActivity(packageManager)?.also {
                selectPictureIntent.type = "image/*"
                startActivityForResult(selectPictureIntent, REQUEST_SELECT_PHOTO)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    currentUri = FileProvider.getUriForFile(
                        this,
                        "com.example.mycameraapp.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentUri)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            showEditNameDialog(currentUri.toString())
        }
        else if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            if (data.data != null)
                showEditNameDialog(data.data.toString())
        }
    }

    /**
     * @return devolve o ficheiro criado para guardar a foto recebida do intent da camera
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }


    private fun setViewsVisibility(isLoading : Boolean) {

        sem_fotos_tv.visibility = if (isLoading || adapter.itemCount > 0) View.GONE else View.VISIBLE
        if (isLoading) content_progress_bar.show() else content_progress_bar.hide()

    }

    /**
     * @param photoUri uri da foto a guardar
     * Mostra um dialogo para editar o nome da foto guardar
     */
    private fun showEditNameDialog(photoUri: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Adicionar foto")
        val rootView = LayoutInflater.from(this).inflate(R.layout.dialog_add_photo, null)
        val nameEdit = rootView.findViewById<EditText>(R.id.name_edit)
        builder.setView(rootView)
        builder.setNegativeButton("Cancelar", null)
        builder.setPositiveButton("Adicionar") { _, _ ->
//            photoViewModel.insertPhoto(Photo(
//                nameEdit.text.toString(),
//                SimpleDateFormat("dd-MM-yyyy").format(Date()),
//                photoUri))

            firebaseViewModel.uploadPhoto(
                Photo(
                    nameEdit.text.toString(),
                    SimpleDateFormat("dd-MM-yyyy").format(Date()),
                    photoUri
                )
            )
            upload_progress_bar.show()
        }
        builder.create().show()
    }

    /**
     * @param photoUri uri da foto clicada na lista
     * Mostra um dialog fullscreen onde é carregada a foto a partir do uri
     */
    override fun onPhotoClick(photoUri: String) {
        val photoDialog = PhotoDialog()
        val args = Bundle()
        args.putString(PhotoDialog.PHOTO_URI, photoUri)
        photoDialog.arguments = args
        photoDialog.show(supportFragmentManager, "")
    }



}
