package com.santi.mucom88

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private var pending: ((Uri) -> Unit)? = null

    fun openDocument(mimes: Array<String>, write: Boolean = false, cb: (Uri) -> Unit) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimes)
        if (write) {
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        pending = cb
        startActivityForResult(i, REQ_FILE)
    }

    fun createDocument(name: String, cb: (Uri) -> Unit) {
        val i = Intent(Intent.ACTION_CREATE_DOCUMENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "application/octet-stream"
        i.putExtra(Intent.EXTRA_TITLE, name)
        pending = cb
        startActivityForResult(i, REQ_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_FILE) {
            val cb = pending
            pending = null
            val uri = data?.data
            if (resultCode == RESULT_OK && uri != null && cb != null) cb(uri)
        }
    }

    fun readUri(uri: Uri): ByteArray =
        contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("No se pudo abrir el archivo")

    fun writeUri(uri: Uri, bytes: ByteArray) {
        val out = contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("No se pudo guardar el archivo")
        out.use { it.write(bytes) }
    }

    fun fileName(uri: Uri): String {
        var name = "banco.bin"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) name = c.getString(idx)
            }
        } catch (e: Exception) {
        }
        return name
    }

    fun displayName(uri: Uri): String {
        var name = "muestra"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) name = c.getString(idx)
            }
        } catch (e: Exception) {
        }
        return name.substringBeforeLast('.')
    }

    override fun onDestroy() {
        AudioPreview.stop()
        super.onDestroy()
    }

    companion object {
        const val REQ_FILE = 4711
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fonts.init(this)
        val pager = ViewPager2(this)
        pager.adapter = PageAdapter()
        pager.offscreenPageLimit = 1
        setContentView(pager)
    }
}

class PageAdapter : RecyclerView.Adapter<PageAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v)

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v: View = if (viewType == 0) EditorPage(parent.context) else ToneEditorPage(parent.context)
        v.layoutParams = ViewGroup.LayoutParams(MATCH, MATCH)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {}
}
