package controllers

import actors.WebSocketReceiverActor
import akka.actor.ActorRef
import com.crawler.core.runners.{CrawlerAgent, CrawlerMaster}
import play.api.Play.current
import play.api.mvc._

class ReactiveApplication /*@Inject()(ws: WSClient)*/ extends Controller {


  CrawlerMaster.main(Array())
  CrawlerAgent.main(Array())
  CrawlerAgent.main(Array())

  def index = Action {

    Ok(views.html.index(getCurrentIp()))
  }

  def scroll = Action {
    Ok(views.html.scroll())
  }

  def reactive = WebSocket.acceptWithActor[String, String] {
    request => WebSocketReceiverActor.props
  }

  def getCurrentIp(): String = {
    import java.net.NetworkInterface
    import collection.JavaConversions._

    NetworkInterface.getNetworkInterfaces.foreach{ ee =>
      ee.getInetAddresses.foreach { i =>
        if (i.getHostAddress.startsWith("192.168"))
          return i.getHostAddress
      }
    }

    return "127.0.0.1"
  }

}
