package org.piepmeyer.gauguin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private val activityUtils: ActivityUtils by inject()

    private lateinit var binding: ActivityAboutBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        activityUtils.configureTheme(this)

        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityUtils.configureFullscreen(this)

        binding.aboutClose.setOnClickListener {
            finishAfterTransition()
        }

        binding.aboutShareApplicationLog.setOnClickListener {
            val reversedLines =
                Runtime
                    .getRuntime()
                    .exec("logcat -d")
                    .inputStream
                    .bufferedReader()
                    .readLines()
                    .reversed()
                    .toMutableList()

            var logLength = 0
            val logLines = mutableListOf<String>()

            while (reversedLines.isNotEmpty() && logLength < 100_000) {
                logLines += reversedLines.first()
                logLength += reversedLines.first().length

                reversedLines.removeAt(0)
            }

            val logText = logLines.reversed().joinToString(separator = System.lineSeparator())

            val sendIntent: Intent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, logText)
                    type = "text/plain"
                }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }
}
