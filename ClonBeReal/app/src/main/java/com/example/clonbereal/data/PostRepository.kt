package com.example.clonbereal.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class PostRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("bereal_posts", Context.MODE_PRIVATE)

    /** Guarda un Bitmap como archivo JPG en el almacenamiento interno y devuelve su ruta. */
    fun saveBitmap(bitmap: Bitmap, name: String): String {
        val file = File(context.filesDir, "$name.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /** Carga un Bitmap desde una ruta. Devuelve null si el archivo ya no existe. */
    fun loadBitmap(path: String): Bitmap? {
        return if (File(path).exists()) BitmapFactory.decodeFile(path) else null
    }

    /** Guarda toda la lista de posts como JSON. */
    fun savePosts(posts: List<BeRealPost>) {
        val array = JSONArray()
        for (p in posts) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("userAvatarResId", p.userAvatarResId)
            obj.put("username", p.username)
            obj.put("timePosted", p.timePosted)
            obj.put("mainImagePath", p.mainImagePath)
            obj.put("selfieImagePath", p.selfieImagePath)
            obj.put("reactionCount", p.reactionCount)
            obj.put("commentCount", p.commentCount)
            array.put(obj)
        }
        prefs.edit().putString("posts_json", array.toString()).apply()
    }

    /** Carga la lista de posts guardada. */
    fun loadPosts(): List<BeRealPost> {
        val json = prefs.getString("posts_json", null) ?: return emptyList()
        val array = JSONArray(json)
        val list = mutableListOf<BeRealPost>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                BeRealPost(
                    id = obj.getString("id"),
                    userAvatarResId = obj.getInt("userAvatarResId"),
                    username = obj.getString("username"),
                    timePosted = obj.getString("timePosted"),
                    mainImagePath = obj.getString("mainImagePath"),
                    selfieImagePath = obj.getString("selfieImagePath"),
                    reactionCount = obj.getInt("reactionCount"),
                    commentCount = obj.getInt("commentCount")
                )
            )
        }
        return list
    }
}