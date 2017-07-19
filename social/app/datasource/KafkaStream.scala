package datasource

import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import org.apache.kafka.common.serialization.StringDeserializer
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by vipmax on 03.07.17.
  */
class KafkaStream(actorSystem: ActorSystem) {
  val logger = Logger.logger

  val topicsData = mutable.Map[String, TopicData]()
  val dataKeys = mutable.LinkedHashSet[String]()

  val key = "topics"

  case class TopicTimeStatistic(var timestamp: Long, var count:Long)
  case class RelatedTopicStatistic(var relatedTopic: String, var count:Long)

  case class TopicData(name: String,
                       users: mutable.HashSet[ActorRef] = mutable.HashSet[ActorRef](),
                       timeStatistics: mutable.LinkedHashMap[Long, TopicTimeStatistic] = mutable.LinkedHashMap[Long, TopicTimeStatistic]()
                      )

  def addTopic(topic: String, userChannel:ActorRef) = synchronized {
    val topicData = topicsData.getOrElse(topic, TopicData(topic))
    val users = topicData.users
    users += userChannel
    topicsData(topic) = topicData
  }

  def removeTopic(topic: String, userChannel:ActorRef) = synchronized {
    if(topicsData.containsKey(topic)) {
      val users = topicsData(topic).users
      users -= userChannel
      if(users.isEmpty) topicsData -= topic
    }
  }

  implicit val system = actorSystem
  implicit var materializer = ActorMaterializer()

  var consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("social")
    .withMaxWakeups(10)

  def updateStatistic(topic:String, newdata:BasicDBObject) = {
    if (topicsData.containsKey(topic)){
      val topicData = topicsData(topic)

      val time = DateTime.now().withSecondOfMinute(0).getMillis / 1000
      topicData.timeStatistics.getOrElseUpdate(time,TopicTimeStatistic(time, 0)).count += 1

    }
  }


  def startKafkaStream() {
    Consumer.committableSource(consumerSettings, Subscriptions.topics("posts"))
      .runForeach { msg =>
        val dbo = JSON.parse(msg.record.value()).asInstanceOf[BasicDBObject]
        val data = util.Util.convert(dbo)

        val isNewPost =  synchronized { dataKeys.add(data.getString("key")) }
        if(!isNewPost){
          if(dataKeys.size >= 1000)
            dataKeys -= dataKeys.head
        }
        else {

          val topic = data.getString("topic")
          synchronized {
            if(topicsData.contains(topic)){
//              updateStatistic(topic,data)
              logger.debug(s"new data: ${data.getString("post_url")}")

              val users = topicsData(topic).users
              data.put("topicStatistic", new BasicDBObject("usersCount", users.size))
              users.foreach(u => u ! data.toJson)
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





