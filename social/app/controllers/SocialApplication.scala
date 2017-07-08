package controllers

import javax.inject._

import actors.SocialWebSocketHandlerActor
import akka.actor.ActorSystem
import com.crawler.core.runners.{CrawlerAgent, CrawlerMaster}
import crawler.SocialApp
import datasource.KafkaStream
import play.api.Logger
import play.api.Play.current
import play.api.mvc._

@Singleton
class SocialApplication @Inject() (implicit system: ActorSystem) extends Controller {
  val logger = Logger.logger

  logger.info("ActorSystem : "+system.name)

  CrawlerMaster.main(Array())
  CrawlerAgent.main(Array())
  CrawlerAgent.main(Array())

  val dbStream = new KafkaStream(system)

  dbStream.startKafkaStream()
  SocialApp.startCrawlerClient(dbStream)

  def index = Action { req =>
    logger.info(s"Got request from ${req.domain}")
    Ok(views.html.index(getCurrentIp()))
  }

  def scroll = Action {
    Ok(views.html.scroll())
  }

  def reactive = WebSocket.acceptWithActor[String, String] { request => out =>
    SocialWebSocketHandlerActor.props(out,request,dbStream)
  }

  def getCurrentIp(): String = {
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
