package ru.raid.smartdiary

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

abstract class PermissionHelperFragment<Tag: Enum<Tag>>(private val allTags: Array<out Tag>) : Fragment() {
    private var code: Int = 0
    private val requests = mutableMapOf<Int, Request>()
    private val json = Json(JsonConfiguration.Stable)
    private val requestSerializer = Request.serializer()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val request = requests.remove(requestCode)
            ?: return super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            onPermissionsResult(allTags[request.tag], true)
        } else {
            val activateInSettings = permissions.any { !shouldShowRequestPermissionRationale(it) }
            if (request.rationale != null && !activateInSettings) {
                Toast.makeText(context, request.rationale, Toast.LENGTH_SHORT).show()
            }
            if (request.rationaleInSettings != null && activateInSettings) {
                Toast.makeText(context, request.rationaleInSettings, Toast.LENGTH_SHORT).show()
            }

            onPermissionsResult(allTags[request.tag], false)
        }
    }

    protected fun withPermissions(
        permissions: Array<out String>,
        rationale: Int? = null,
        rationaleSettings: Int? = null,
        tag: Tag
    ) {
        val context = checkNotNull(context) { "No context" }

        val notYetGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()
        if (notYetGranted.isEmpty()) {
            onPermissionsResult(tag, true)
            return
        }

        val requestCode = code++
        requests[requestCode] = Request(tag.ordinal, rationale, rationaleSettings)
        requestPermissions(notYetGranted, requestCode)
    }

    protected abstract fun onPermissionsResult(tag: Tag, granted: Boolean)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val (keys, values) = requests.map { Pair(it.key, it.value) }.unzip()
        val dumpedValues = values.map { json.stringify(requestSerializer, it) }

        outState.putIntArray(KEY_CODES, keys.toIntArray())
        outState.putStringArray(KEY_REQUESTS, dumpedValues.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            return

        val keys = savedInstanceState.getIntArray(KEY_CODES) ?: return
        val dumpedValues = savedInstanceState.getStringArray(KEY_REQUESTS) ?: return
        val values = dumpedValues.map { json.parse(requestSerializer, it) }

        requests.putAll(keys zip values)
        code = keys.max() ?: code
    }

    @Serializable
    private class Request(
        val tag: Int,
        val rationale: Int?,
        val rationaleInSettings: Int?
    )

    companion object {
        private const val KEY_CODES = "PermissionHelperFragment.codes"
        private const val KEY_REQUESTS = "PermissionHelperFragment.requests"
    }
}
