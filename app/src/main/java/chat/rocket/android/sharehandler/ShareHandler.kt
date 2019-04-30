package chat.rocket.android.sharehandler

import android.content.Context
import android.content.Intent
import chat.rocket.android.util.extensions.getFileName
import chat.rocket.android.util.extensions.getFileSize
import chat.rocket.android.util.extensions.getMimeType
import java.io.InputStream


object ShareHandler {

    fun hasShare(): Boolean = hasSharedText() || hasSharedImage()

    fun hasSharedText(): Boolean = sharedText != null
    fun hasSharedImage(): Boolean = files.size > 0

    var sharedText: String? = null

    var files: ArrayList<SharedFile> = arrayListOf()

    fun handle(intent: Intent?, context: Context) {
        intent?.let {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val action = it.action
            val type = it.type

            if (type.isNullOrEmpty() || action.isNullOrEmpty())
                return@let

            if ("text/plain" == type) {
                handleSendText(intent)
            } else {
                intent.clipData?.let { data ->
                    if (data.itemCount > 0) {
                        loadFiles(intent, context)
                    }
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun loadFiles(intent: Intent, context: Context) {
        intent.clipData?.apply {
            for (pos in 0 until itemCount) {
                val uri = getItemAt(pos).uri

                context.contentResolver.apply {
                    openInputStream(getItemAt(pos).uri)?.let {
                        files.add(SharedFile(
                            it,
                            uri.getFileName(context) ?: uri.toString(),
                            uri.getMimeType(context), // TODO some mime types missing and causing crash.
                            uri.getFileSize(context)
                        ))
                    }
                }
            }
        }
    }

    fun getTextAndClear(): String {
        val text = sharedText.orEmpty()
        sharedText = null

        return text
    }

    class SharedFile(var fis: InputStream, var name: String, val mimeType: String, val size: Int)
}