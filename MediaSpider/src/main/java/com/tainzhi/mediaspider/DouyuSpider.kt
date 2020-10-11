package com.tainzhi.mediaspider

import com.google.gson.JsonObject
import org.jsoup.Jsoup
import java.lang.System.currentTimeMillis
import java.security.MessageDigest

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/6/9 13:32
 * @description: 使用[KRequest] 爬取斗鱼信息
 **/

class DouyuSpider {

    fun getRoomCircuitId(rid: String): String {
        val timeSeconds = currentTimeMillis() / 1000
        val url = "https://playweb.douyucdn.cn/lapi/live/hlsH5Preview/$rid"
        val auth = (rid + timeSeconds).toMD5()
        val header = mutableMapOf<String, String>(
                "Content-type" to "application/x-www-form-urlencoded",
                "rid" to rid,
                "time" to timeSeconds.toString(),
                "auth" to auth
        )

        val postData = mutableMapOf(
                "rid" to rid,
                "did" to "10000000000000000000000000001501"
        )

        lateinit var roomLiveUrl: String
        lateinit var response: JsonObject
        try {
            response = KRequest().apply {
                mapData = postData
                headers = header
            }.post(url)
                    .json()

            response.let { it ->
                if (it.get("error").asInt == 0) {
                    val dataObject = it.getAsJsonObject("data")
                    val rtmpLive = dataObject.get("rtmp_live").asString
                    if (rtmpLive.contains("mix=1")) {
                        throw NotFoundException("circuit not found; PKing")
                    } else {
                        // val regex = Regex(pattern = "[0-9a-zA-Z]*_")
                        val regex = Regex(pattern = "[0-9a-zA-Z]*")
                        val found = regex.find(rtmpLive)
                        if (found != null) {
                            roomLiveUrl = found.value
                        } else {
                            throw NotFoundException("not found circuit id")
                        }
                    }
                } else {
                    val msg = it.get("msg").asString
                    throw  NotFoundException("not found circuit id", cause = Throwable(msg))
                }

            }
        } catch (e: Exception) {
            throw NotFoundException("not found circuit id", e.cause)
        }
        return roomLiveUrl
    }

    /*
    <li class="layout-Classify-item"><a class="layout-Classify-card secondCateCard" href="/g_jdqs" target="_blank"><i class="secondCateCard-icon">
        <div class="LazyLoad is-visible DyImg secondCateCard-img">
          <img src="https://sta-op.douyucdn.cn/dycatr/08dcd21b98a6b71bb1d37b0c30376734.png?x-oss-process=image/format,webp/quality,q_75" class="DyImg-content is-normal " />

         </div></i><strong>绝地求生</strong>

         fixme
         获取的图片是默认的, 因为douyu懒加载了图片, 还需要找到图片加载地址
     */
    fun getAllRoom() {
        val response = KRequest().get("https://www.douyu.com/directory")

        val doc = Jsoup.parse(response)
        doc.select("li.layout-Classify-item a.layout-Classify-card").forEach { item ->
            if (item.attr("href").isNotEmpty()) {
                val type = item.attr("href")
                val href = item.select("img[src]").attr("src")
                val name = item.select("strong").text()
            }
        }

    }

    companion object {
        @Volatile
        private var instance: DouyuSpider? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DouyuSpider().also { instance = it }
        }
    }
}

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.toHex()
}

fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}