package datasource

import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by vipmax on 03.07.17.
  */
class KafkaStream(actorSystem: ActorSystem) {
  val logger = Logger.logger

  val topicsToUsers = mutable.Map[String, mutable.HashSet[ActorRef]]()
  val dataKeys = mutable.LinkedHashSet[String]()

  val key = "topics"

  def addTopic(topic: String, userChannel:ActorRef) = synchronized {
    val users = topicsToUsers.getOrElse(topic, mutable.HashSet())
    users += userChannel
    topicsToUsers(topic) = users
  }

  def removeTopic(topic: String, userChannel:ActorRef) = synchronized {
    if(topicsToUsers.containsKey(topic)) {
      val users = topicsToUsers(topic)
      users -= userChannel
      if(users.isEmpty) topicsToUsers -= topic
    }
  }

  implicit val system = actorSystem
  implicit var materializer = ActorMaterializer()

  var consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("social")
    .withMaxWakeups(10)

  def startKafkaStream() {
    Consumer.committableSource(consumerSettings, Subscriptions.topics("posts"))
      .runForeach{msg =>
        val dbo = JSON.parse(msg.record.value()).asInstanceOf[BasicDBObject]
        val data = util.Util.convert(dbo)

        val isNewPost =  synchronized { dataKeys.add(data.getString("key")) }
        if(!isNewPost){
          if(dataKeys.size >= 1000)
            dataKeys -= dataKeys.head

          logger.debug(s"old data: ${data.getString("post_url")} dataKeys size: ${dataKeys.size}")
        }
        else {
          logger.debug(s"new data: ${data.getString("post_url")}")

          val topic = data.getString("topic")
          synchronized {
            if(topicsToUsers.contains(topic)){
              topicsToUsers(topic).foreach(u => u ! data.toJson)
            }
          }
        }
      }(materializer)
      .onFailure{
        case e:Exception =>
          println("stream exception ")
          e.printStackTrace()
      }(system.dispatcher)
  }
}





