<?php
include "koneksi.php";
session_start();
if ($_SESSION['status'] <> "sukses") {
    header('location:logout.php');
    exit();
}

$role = $_SESSION['role'];
$username = $_SESSION['username'];

if (isset($_POST['submit'])) {
    $tanggal = $_POST['tanggal'];
    $keterangan = $_POST['keterangan'];
    $nama_siswa = $role == 'guru' ? $_POST['nama_siswa'] : $username;  // Jika guru, gunakan nama siswa dari combobox

    // File Upload
    $gambar = $_FILES['gambar']['name'];
    $target_dir = 'uploads/';
    $target_files = $target_dir . basename($gambar);

    if (move_uploaded_file($_FILES['gambar']['tmp_name'], $target_files)) {
        // Insert data
        $query = "INSERT INTO jurnal(tanggal, keterangan, nama_siswa, gambar) VALUES ('$tanggal','$keterangan','$nama_siswa','$gambar')";
        if (mysqli_query($koneksi, $query)) {
            echo "<script>alert('Data Berhasil di Tambahkan')</script>";
        } else {
            echo "<script>alert('Error : " . mysqli_error($koneksi) . " ')</script>";
        }
    } else {
        echo "<script>alert('Error Upload File')</script>";
    }
}

// Query untuk mengambil data jurnal sesuai role
if ($role == 'guru') {
    $selected_siswa = isset($_POST['filter_siswa']) ? $_POST['filter_siswa'] : '';
    $data_siswa_query = "SELECT * FROM jurnal";
    if ($selected_siswa) {
        $data_siswa_query .= " WHERE nama_siswa = '$selected_siswa'";
    }
    $data_siswa = mysqli_query($koneksi, $data_siswa_query);

    // Query untuk mengambil daftar siswa
    $daftar_siswa = mysqli_query($koneksi, "SELECT username FROM user WHERE role = 'siswa'");
} else {
    $data_siswa = mysqli_query($koneksi, "SELECT * FROM jurnal WHERE nama_siswa = '$username'");
}
?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jurnal</title>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="container">
    <h1 class="mt-4">Welcome, <?php echo $username; ?></h1>
    <h2 class="mt-4"></h2>
    <?php
    if ($role == 'siswa') {
        include 'form.php';
    }
    ?>

    <?php if ($role == 'guru'): ?>
        <h2 class="mt-5">Filter Berdasarkan Siswa</h2>
        <form action="" method="POST">
            <div class="form-group">
                <label for="filter_siswa">Nama Siswa:</label>
                <select name="filter_siswa" class="form-control">
                    <option value="">Semua Siswa</option>
                    <?php
                    mysqli_data_seek($daftar_siswa, 0);  // Reset pointer ke awal
                    while ($siswa = mysqli_fetch_assoc($daftar_siswa)): ?>
                        <option value="<?php echo $siswa['username']; ?>" <?php if ($selected_siswa == $siswa['username']) echo 'selected'; ?>>
                            <?php echo $siswa['username']; ?>
                        </option>
                    <?php endwhile; ?>
                </select>
            </div>
            <button type="submit" name="filter" class="btn btn-secondary">Filter</button>
        </form>
    <?php endif; ?>

    <h2 class="mt-5">Data Jurnal</h2>
    <table class="table table-bordered mt-3">
        <thead>
            <tr>
                <td>Tanggal</td>
                <td>Keterangan</td>
                <td>Nama Siswa</td>
                <td>Gambar</td>
            </tr>
        </thead>
        <tbody>
            <?php while ($row = mysqli_fetch_assoc($data_siswa)): ?>
                <tr>
                    <td><?php echo $row['tanggal']; ?></td>
                    <td><?php echo $row['keterangan']; ?></td>
                    <td><?php echo $row['nama_siswa']; ?></td>
                    <td>
                        <?php if ($row['gambar']) : ?>
                            <img src="uploads/<?php echo $row['gambar']; ?>" alt="Gambar" width="100">
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endwhile; ?>
        </tbody>
    </table>
    <a href="logout.php"><button class="btn btn-danger mt-4">LogOut</button></a>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.3/dist/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
</body>

</html>
