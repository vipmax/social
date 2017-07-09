package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import datasource.KafkaStream
import play.api.Logger
import play.api.mvc.RequestHeader

import scala.collection.mutable


object SocialWebSocketHandlerActor {
  def props(outputChannel: ActorRef, initialRequest:RequestHeader, stream:KafkaStream) = {
    Props(new SocialWebSocketHandlerActor(outputChannel, initialRequest,stream))
  }
}

class SocialWebSocketHandlerActor(outputChannel: ActorRef, initialRequest:RequestHeader, stream:KafkaStream) extends Actor with ActorLogging {
  val userTopics = mutable.Set[String]("питер")

  override def preStart() {
    Logger.info(s"New web-socket connection created from ${initialRequest.remoteAddress}")
    userTopics.foreach(t => stream.addTopic(t, outputChannel))
  }

    override def postStop(){
      Logger.info("Web-socket connection closed")
      userTopics.foreach(t => stream.removeTopic(t, outputChannel))
    }

    def receive = {
      case message: String =>
        Logger.info(s"Received message: $message")
        message match {
          case t if t.startsWith("addTopic:") =>
            log.info(s"addTopic: $t" )
            val topic = t.substring("addTopic:".length).toLowerCase()
            userTopics += topic
            stream.addTopic(topic, outputChannel)

          case t if t.startsWith("removeTopic:") =>
            log.info(s"removeTopic: $t" )
            val topic = t.substring("removeTopic:".length).toLowerCase()
            userTopics -= topic
            stream.removeTopic(topic, outputChannel)
        }
    }
}

