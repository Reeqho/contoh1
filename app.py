from flask import Flask, request, jsonify
from flask_restful import Api, Resource
from flask_mysqldb import MySQL
import pymysql
import os
import json

app = Flask(__name__)
api = Api(app)

# Konfigurasi MySQL
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'root'
app.config['MYSQL_PASSWORD'] = ''
app.config['MYSQL_DB'] = 'skanet_pkl'

mysql = MySQL(app)

# Konfigurasi direktori upload
UPLOAD_FOLDER = 'C:/xampp/htdocs/test_project/uploads'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Fungsi untuk membuat koneksi ke database
def get_db_connection():
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='',
        database='skanet_pkl',
        cursorclass=pymysql.cursors.DictCursor
    )
    return connection

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({'status': 'error', 'message': 'Username dan password diperlukan'}), 400

    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            sql = "SELECT * FROM user WHERE username = %s AND password = %s"
            cursor.execute(sql, (username, password))
            user = cursor.fetchone()

            if user:
                return jsonify({'status': 'success', 'message': 'Login berhasil'}), 200
            else:
                return jsonify({'status': 'error', 'message': 'Username atau password salah'}), 401
    finally:
        connection.close()

@app.route('/Save', methods=['POST'])
def upload_and_save():
    if 'file' not in request.files:
        return jsonify({'status': 'error', 'message': 'No file part'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'status': 'error', 'message': 'No selected file'}), 400

    if file:
        filename = file.filename
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(file_path)

        data = request.form['data']
        data_json = json.loads(data)
        tanggal = data_json.get('tanggal')
        keterangan = data_json.get('keterangan')
        nama_siswa = data_json.get('nama_siswa')

        cur = mysql.connection.cursor()
        cur.execute("INSERT INTO jurnal (tanggal, keterangan, nama_siswa, gambar) VALUES (%s, %s, %s, %s)",
                    (tanggal, keterangan, nama_siswa, filename))
        mysql.connection.commit()
        cur.close()

        return jsonify({'status': 'success', 'message': 'Data and image uploaded successfully'}), 200

if __name__ == '__main__':
    app.run(debug=True)
