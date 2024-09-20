package com.example.jurnal_pkl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var login: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = findViewById(R.id.usernameEditText)
        password = findViewById(R.id.passwordEditText)
        login = findViewById(R.id.loginButton)

        login.setOnClickListener() {
            val username = username.text.toString()
            val password = password.text.toString()
            login(username, password)

        }
    }

    private fun login(username: String, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("http://localhost:5000/login")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json")
            con.doOutput = true

            val inputJSON = "{\n" +
                    "    \"username\" : \"$username\",\n" +
                    "    \"password\" : \"$password\"\n" +
                    "}".trimIndent()

            val outputStream = con.outputStream
            outputStream.write(inputJSON.toByteArray())
            outputStream.flush()
            outputStream.close()
            val responsecode = con.responseCode
            runOnUiThread() {
                if (responsecode == HttpURLConnection.HTTP_OK) {
                    val intent = Intent(this@MainActivity, InputJurnal::class.java)
                    val alertdialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Pesan")
                        .setMessage("Berhasil Login")
                        .setPositiveButton("Ok") { _, _ ->
                            intent.putExtra("username", username)
                            this@MainActivity.startActivity(intent)
                        }.create()
                    alertdialog.show()

                } else {
                    val alertdialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Pesan")
                        .setMessage("Username Atau Password Salah")
                        .setPositiveButton("Ok") { _, _ ->
                            Toast.makeText(this@MainActivity, "Gagal Coy", Toast.LENGTH_SHORT).show()
                        }.create()
                    alertdialog.show()
                    con.disconnect()
                }
            }
        }
    }
}