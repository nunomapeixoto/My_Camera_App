package com.example.mycameraapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.Transition
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.photo_item.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), PhotosAdapter.PhotoAdapterListener {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1
    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_SELECT_PHOTO = 2

    private lateinit var speedDial: SpeedDialView

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTv: TextView
    private lateinit var adapter: PhotosAdapter

    private lateinit var photoViewModelFactory: PhotoViewModelFactory
    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    private var currentUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        photoViewModelFactory =
            Injection.providePhotoViewModelFactory(applicationContext)
        photoViewModel = ViewModelProvider(this, photoViewModelFactory).get(PhotoViewModel::class.java)



        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(photoViewModel.getPhotos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { photos ->
                adapter = PhotosAdapter(photos, this@MainActivity)
                recyclerView.adapter = adapter
                setViewsVisibility()
            })

        recyclerView = findViewById(R.id.recycler_view)
        emptyTv = findViewById(R.id.sem_fotos_tv)


        speedDial = findViewById(R.id.fab_speed_dial)

        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.select_photo, R.drawable.ic_photo_album_white_24dp)
                .setLabelBackgroundColor(ContextCompat.getColor(this, R.color.colorPreferencesItem))
                .setLabelColor(Color.WHITE)
                .setLabel(getString(R.string.select_photo))
                .create())

        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.take_photo, R.drawable.ic_camera_alt_white_24dp)
                .setLabelBackgroundColor(ContextCompat.getColor(this, R.color.colorPreferencesItem))
                .setLabelColor(Color.WHITE)
                .setLabel(getString(R.string.take_photo))
                .create())

        speedDial.setOnActionSelectedListener { actionItem ->
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
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

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


    private fun setViewsVisibility() {
        emptyTv.visibility = if (adapter.itemCount > 0) View.GONE else View.VISIBLE

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
            photoViewModel.insertPhoto(Photo(
                nameEdit.text.toString(),
                SimpleDateFormat("dd-MM-yyyy").format(Date()),
                photoUri))
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
