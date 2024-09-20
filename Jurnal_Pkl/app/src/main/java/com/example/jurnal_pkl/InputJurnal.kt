package com.example.jurnal_pkl

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class InputJurnal : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var username: String
    private lateinit var keterangan: EditText
    private lateinit var tanggal: EditText
    private lateinit var upload_gambar: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var simpan_btn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_jurnal)

        selectedImageView = findViewById(R.id.selectedImageView)

        // Meminta izin jika belum diberikan
        username = intent.getStringExtra("username").toString()

        keterangan = findViewById(R.id.keterangan_et)
        tanggal = findViewById(R.id.editTextTanggal)
        upload_gambar = findViewById(R.id.buttonSelectImage)
        simpan_btn = findViewById(R.id.simpan_btn)
        findViewById<TextView>(R.id.welcome_tv).text = "Selamat datang $username"



        upload_gambar.setOnClickListener {
            ImagePicker()
        }

        simpan_btn.setOnClickListener {
            simpan()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            // Display the selected image in the ImageView
            selectedImageUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageView.setImageBitmap(bitmap)
            }

            Toast.makeText(
                this,
                "File yang dipilih: ${selectedImageUri?.lastPathSegment}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun simpan() {
        val keterangan = keterangan.text.toString()
        val tanggal = tanggal.text.toString()

        if (selectedImageUri == null) {
            Toast.makeText(this, "Mohon gambarnya dipilih terlebih dahulu", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Thread {
            try {
                val url = URL("http://localhost:5000/Save")
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"
                )
                con.doOutput = true

                val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
                val lineEnd = "\r\n"
                val twoHyphens = "--"

                val outputStream = DataOutputStream(con.outputStream)

                // Bagian File
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"${
                        File(
                            getRealPathFromURI(selectedImageUri)
                        ).name
                    }\"" + lineEnd
                )
                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd)
                outputStream.writeBytes(lineEnd)

                val fileInputStream = FileInputStream(File(getRealPathFromURI(selectedImageUri)))
                val buffer = ByteArray(1024)
                var bytesRead = fileInputStream.read(buffer)
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = fileInputStream.read(buffer)
                }
                fileInputStream.close()
                outputStream.writeBytes(lineEnd)

                // Bagian JSON
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"data\"" + lineEnd)
                outputStream.writeBytes("Content-Type: application/json" + lineEnd)
                outputStream.writeBytes(lineEnd)

                val json = """
                    {
                        "tanggal": "$tanggal",
                        "keterangan": "$keterangan",
                        "nama_siswa": "$username"
                    }
                """
                outputStream.writeBytes(json)
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                outputStream.flush()
                outputStream.close()

                val responseCode = con.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread {
                        Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Data gagal disimpan: ${con.responseMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                con.disconnect()

            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d("Upload Cek", e.message.toString())
                }
            }
        }.start()
    }

    private fun getRealPathFromURI(uri: Uri?): String {
        val cursor = contentResolver.query(uri!!, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        return cursor?.getString(index!!) ?: ""
    }
}
