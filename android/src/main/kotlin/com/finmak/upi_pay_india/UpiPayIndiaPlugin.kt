package com.finmak.upi_pay_india

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
//import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.ByteArrayOutputStream
import com.finmak.upi_pay_india.Util.Companion.convertAppToMap
import com.finmak.upi_pay_india.Util.Companion.getPackageManager


/** UpiPayIndiaPlugin */

class UpiPayIndiaPlugin: MethodCallHandler, FlutterPlugin, ActivityAware {
  companion object {

    var context: Context? = null

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      context = registrar.context()
      register(registrar.messenger())
    }

    @JvmStatic
    fun register(messenger: BinaryMessenger) {
      val channel = MethodChannel(messenger, "upi_pay_india")
      channel.setMethodCallHandler(UpiPayIndiaPlugin())
    }
  }
  private var result: Result? = null
  private var requestCodeNumber = 201119

  var hasResponded = false

  override fun onMethodCall(call: MethodCall, result: Result) {
    hasResponded = false

    this.result = result

    when (call.method) {
      "initiateTransaction" -> this.initiateTransaction(call)
      "getInstalledUpiApps" -> this.getInstalledUpiApps()
      else -> result.notImplemented()
    }
  }

  private fun initiateTransaction(call: MethodCall) {
    val app: String? = call.argument("app")
    val pa: String? = call.argument("pa")
    val pn: String? = call.argument("pn")
    val mc: String? = call.argument("mc")
    val tr: String? = call.argument("tr")
    val tn: String? = call.argument("tn")
    val am: String? = call.argument("am")
    val cu: String? = call.argument("cu")
    val url: String? = call.argument("url")

    try {
      var uriStr: String? = "upi://pay?pa=" + pa +
              "&pn=" + Uri.encode(pn) +
              "&tr=" + Uri.encode(tr) +
              "&am=" + Uri.encode(am) +
              "&cu=" + Uri.encode(cu)
      if(url != null) {
        uriStr += ("&url=" + Uri.encode(url))
      }
      if(mc != null) {
        uriStr += ("&mc=" + Uri.encode(mc))
      }
      if(tn != null) {
        uriStr += ("&tn=" + Uri.encode(tn))
      }
      uriStr += "&mode=05" // &orgid=000000"
      uriStr += "&orgid=000000"

      val ecode = Base64.encodeToString(uriStr.toString().toByteArray(), Base64.DEFAULT)
      if(ecode !=null){
        uriStr += ("&sign=" + Uri.encode(ecode))
      }

      val uri = Uri.parse(uriStr)

      Log.d("upi_pay", "initiateTransaction URI: $uri")

      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.setPackage(app)

//      if (activity?.let { intent.resolveActivity(it.packageManager) } == null) {
//        this.success("activity_unavailable")
//        return
//      }
      //startApp(intent.`package`)

      context!!.startActivity(intent)

//      if(context!!.let { intent.resolveActivity(it.packageManager) } == null) {
//        this.success("activity_unavailable")
//        return
//      }
//
//      val launchIntent = getPackageManager(context!!).getLaunchIntentForPackage(intent.`package`.toString())
//      context!!.startActivity(launchIntent)

      //activity.startActivityForResult(intent, requestCodeNumber)
    } catch (ex: Exception) {
      Log.e("upi_pay", ex.toString())
      this.success("failed_to_open_app")
    }
  }

  private fun startApp(packageName: String?): Boolean {
    if (packageName.isNullOrBlank()) return false
    return try {
      val launchIntent = getPackageManager(context!!).getLaunchIntentForPackage(packageName)
      context!!.startActivity(launchIntent)
      true
    } catch (e: Exception) {
      print(e)
      false
    }
  }

  private fun getInstalledUpiApps() {
    val uriBuilder = Uri.Builder()
    uriBuilder.scheme("upi").authority("pay")

    val uri = uriBuilder.build()
    val intent = Intent(Intent.ACTION_VIEW, uri)

    //val packageManager = activity?.packageManager
    val packageManager = getPackageManager(context!!)


    try {
      //val acti = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
      val activities = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

      val activityResponse = activities.map {
        val packageName = it.packageName
        val drawable = packageManager.getApplicationIcon(packageName)

        val bitmap = drawable.let { it1 -> getBitmapFromDrawable(it1) }
        val icon = if (bitmap != null) {
          encodeToBase64(bitmap)
        } else {
          null
        }

        mapOf(
          "packageName" to packageName,
          "icon" to icon,
          "priority" to it.flags,
          "preferredOrder" to it.flags
        )
      }

      result?.success(activityResponse)
    } catch (ex: Exception) {
      Log.e("upi_pay_india", ex.toString())
      result?.error("getInstalledUpiApps", "exception", ex)
    }
  }

  private fun encodeToBase64(image: Bitmap): String? {
    val byteArrayOS = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
    return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP)
  }

  private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
    val bmp: Bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bmp
  }

  private fun success(o: String) {
    if (!hasResponded) {
      hasResponded = true
      result?.success(o)
    }
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    UpiPayIndiaPlugin.register(binding.getBinaryMessenger())
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
    context = activityPluginBinding.getActivity()
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
    context = activityPluginBinding.getActivity()
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }

}
