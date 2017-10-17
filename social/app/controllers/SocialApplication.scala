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

  val stream = new KafkaStream(system)

  stream.startKafkaStream()
  SocialApp.startCrawlerClient(stream)

  def index = Action { req =>
    logger.info(s"Got request from ${req.domain}")
    Ok(views.html.index())
  }


  def local = Action { req =>
    logger.info(s"Got request from ${req.domain}")
    Ok(views.html.localIndex())
  }


  def scrollable = Action {
    Ok(views.html.scrollablePage())
  }
  def board = Action {
    Ok(views.html.horizontalScrollablePage())
  }

  def reactive = WebSocket.acceptWithActor[String, String] { request => out =>
    SocialWebSocketHandlerActor.props(out,request,stream)
  }

}
