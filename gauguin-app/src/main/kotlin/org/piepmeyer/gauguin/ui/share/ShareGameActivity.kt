package org.piepmeyer.gauguin.ui.share

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivitySharegameBinding
import org.piepmeyer.gauguin.game.GameLifecycle
import org.piepmeyer.gauguin.ui.ActivityUtils

class ShareGameActivity : AppCompatActivity() {
    private val gameLifecycle: GameLifecycle by inject()
    private val activityUtils: ActivityUtils by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val binding = ActivitySharegameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityUtils.configureFullscreen(this)

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap("Test of QR code generation.", BarcodeFormat.QR_CODE, 400, 400)
            binding.qrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
        }
    }
}
