package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.Logger

import scala.collection.mutable


object WebSocketReceiverActor {
  def apply(outappendChannel: ActorRef): WebSocketReceiverActor = new WebSocketReceiverActor(outappendChannel)
  def props(outputChannel: ActorRef) = Props(WebSocketReceiverActor(outputChannel))
}

class WebSocketReceiverActor(outputChannel: ActorRef) extends Actor with ActorLogging {
  val userTopics = mutable.Set[String]()

  override def preStart() {
    Logger.info("New web-socket connection created")
    userTopics.foreach(t => datasource.DbStream.addTopic(t, outputChannel))
  }

    override def postStop(){
      Logger.info("Web-socket connection closed")
      userTopics.foreach(t => datasource.DbStream.removeTopic(t, outputChannel))
    }

    def receive = {
      case message: String =>
        Logger.info(s"Received message: $message")
        message match {
          case t if t.startsWith("addTopic:") =>
            log.info(s"addTopic: $t" )
            val topic = t.substring("addTopic:".length).toLowerCase()
            userTopics += topic
            datasource.DbStream.addTopic(topic, outputChannel)

          case t if t.startsWith("removeTopic:") =>
            log.info(s"removeTopic: $t" )
            val topic = t.substring("removeTopic:".length).toLowerCase()
            userTopics -= topic
            datasource.DbStream.removeTopic(topic, outputChannel)
        }
    }
}

