<h2 class="mt-5">Data Jurnal</h2>
    <form action="" method="POST" enctype="multipart/form-data">
        <!-- <?php if ($role == 'guru'): ?>
            <div class="form-group">
                <label for="nama_siswa">Nama Siswa:</label>
                <select name="nama_siswa" class="form-control" required>
                    <?php while ($siswa = mysqli_fetch_assoc($daftar_siswa)): ?>
                        <option value="<?php echo $siswa['username']; ?>"><?php echo $siswa['username']; ?></option>
                    <?php endwhile; ?>
                </select>
            </div>
        <?php endif; ?> -->
        <div class="form-group">
            <label for="tanggal">Tanggal:</label>
            <input type="date" name="tanggal" class="form-control" required>
        </div>
        <div class="form-group">
            <label for="keterangan">Keterangan:</label>
            <input type="text" name="keterangan" class="form-control" required>
        </div>
        <div class="form-group">
            <label for="gambar">Upload Gambar:</label>
            <input type="file" name="gambar" class="form-control-file" required>
        </div>
        <button type="submit" name="submit" class="btn btn-primary">Simpan</button>
    </form>