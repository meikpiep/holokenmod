package org.piepmeyer.gauguin.ui.share

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivitySharegameBinding
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.game.save.SavedGrid
import org.piepmeyer.gauguin.ui.ActivityUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ShareGameActivity : AppCompatActivity() {
    private val game: Game by inject()
    private val activityUtils: ActivityUtils by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val binding = ActivitySharegameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityUtils.configureFullscreen(this)

        val serializedGrid = Json.encodeToString(SavedGrid.fromGrid(game.grid))

        println("${serializedGrid.length}")

        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(serializedGrid) }

        val compressedGrid = String(bos.toByteArray())

        println("${compressedGrid.length}")

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(compressedGrid, BarcodeFormat.QR_CODE, 400, 400)
            binding.qrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
        }
    }
}
