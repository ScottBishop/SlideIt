package za.co.riggaroo.motioncamera

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.InputStream

@GlideModule
class MotionSensorAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.DEBUG)

        val calculator = MemorySizeCalculator.Builder(context)
                .build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
                .setBitmapPool(LruBitmapPool(calculator.bitmapPoolSize.toLong()))
                .setDefaultRequestOptions(RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .disallowHardwareConfig())
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(StorageReference::class.java, InputStream::class.java, FirebaseImageLoader.Factory())
    }

    override fun isManifestParsingEnabled(): Boolean = false
}
