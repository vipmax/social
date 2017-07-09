package util

import java.util

import com.mongodb.BasicDBObject

import scala.collection.JavaConversions._

object Util {
  def convert(document: BasicDBObject) = {
    val doc = document.get("network") match {
      case "vkontakte" => vkontakteParse(document)
      case "instagram" => instagramParse(document)
    }
    doc.append("network",document.get("network"))
    doc.append("key",document.get("key"))
    doc
  }

  private def instagramParse(document: BasicDBObject) = {
    val returned = new BasicDBObject()

    try { returned.append("topic", document.get("query")) } catch {case e: Exception => }
    try { returned.append("post_url", s"https://www.instagram.com/p/${document.get("shortcode")}") } catch {case e: Exception => }
    try { returned.append("username", document.get("owner").asInstanceOf[BasicDBObject].get("username")) } catch {case e: Exception => }
    try { returned.append("user_photo_url", document.get("owner").asInstanceOf[BasicDBObject].get("profile_pic_url")) } catch {case e: Exception => }

    try {
      val location = document.get("location").asInstanceOf[BasicDBObject]
      returned.append("lat", location.get("lat"))
      returned.append("long", location.get("lng"))
    } catch {case e: Exception => }

    try {
      val photoUrl = document.get("display_url")
      returned.append("photo_url", photoUrl)
      returned.append("icon_url", photoUrl)
      val dim = document.get("dimensions").asInstanceOf[BasicDBObject]
      returned.append("width", dim.get("width"))
      returned.append("height", dim.get("height"))
    } catch {case e: Exception => }

    try {
      val username = document.get("owner").asInstanceOf[BasicDBObject].get("username")
      returned.append("user_url", s"https://www.instagram.com/$username")
    } catch {case e: Exception => }

    try {
      returned.append("text", document.get("edge_media_to_caption").asInstanceOf[BasicDBObject]
        .get("edges").asInstanceOf[util.ArrayList[BasicDBObject]].head
        .get("node").asInstanceOf[BasicDBObject]
        .getString("text"))
    } catch {case e: Exception => }

    returned
  }

  private def vkontakteParse(document: BasicDBObject) = {
    val returned = new BasicDBObject()

    try { returned.append("topic", document.get("query")) } catch {case e: Exception => }
    try { returned.put("post_url", s"https://new.vk.com/feed?w=wall${document.get("owner_id")}_${document.get("id")}") } catch {case e: Exception => }
    try { returned.append("user_photo_url", document.get("owner").asInstanceOf[BasicDBObject].get("photo_100")) } catch {case e: Exception => }
    try { returned.append("text", document.get("text")) } catch {case e: Exception => }

    try {
      val location = document.get("geo").asInstanceOf[BasicDBObject].get("coordinates").asInstanceOf[String].split(' ')
      returned.append("lat", location(0).toDouble)
      returned.append("long", location(1).toDouble)
    } catch {case e: Exception => }

    try {
      val photo = document.get("attachments").asInstanceOf[util.ArrayList[BasicDBObject]].filter(_.get("type") == "photo").head
        .get("photo").asInstanceOf[BasicDBObject]
      val photoSizes = photo.keySet().filter(_.startsWith("photo")).map { p => p.replace("photo_", "").toInt }
      returned.append("photo_url", photo.get("photo_" + photoSizes.max))
      returned.append("icon_url", photo.get("photo_" + photoSizes.min))
      returned.append("width", photo.get("width"))
      returned.append("height", photo.get("height"))
    } catch {case e: Exception => }

    try {
      val id = document.get("owner").asInstanceOf[BasicDBObject].get("id")
      returned.append("user_url", s"https://vk.com/id$id")
    } catch {case e: Exception => }

    try {
      val owner = document.get("owner").asInstanceOf[BasicDBObject]
      val username = s"${owner.get("first_name")} ${owner.get("last_name")}"
      returned.append("username", username)
    } catch {case e: Exception => }

    returned
  }

  def getServerIp(): String = {
    import java.net.NetworkInterface

    import collection.JavaConversions._

    val ip = System.getProperty("prod.ip")
    if(ip != null) {
      println("Use production mode on : " + ip)
      return ip
    }

    NetworkInterface.getNetworkInterfaces.foreach{ ee =>
      ee.getInetAddresses.foreach { i =>
        if (i.getHostAddress.startsWith("192.168"))
          return i.getHostAddress
      }
    }

    return "127.0.0.1"
  }
}
