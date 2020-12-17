package com.homingos.sdk.upload

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.homingos.sdk.R

internal class UploadDialog private constructor(context: Context) :
    Dialog(context, R.style.HomingosDialog) {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnCancel: TextView

    companion object {
        private lateinit var onCancelled: () -> Unit

        fun getInstance(context: Context, onCancelled: () -> Unit): UploadDialog {
            this.onCancelled = onCancelled
            return UploadDialog(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_upload)

        init()
    }

    private fun init() {
        setupViews()
    }

    private fun setupViews() {
        setCancelable(false)
        setTitle(context.getString(R.string.uploading))
        setCanceledOnTouchOutside(false)
        progressBar = findViewById(R.id.pbUpload)
        btnCancel = findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            onCancelled.invoke()
            dismiss()
        }
    }

    fun setProgress(percentage: Int) {
        progressBar.progress = percentage
    }

}