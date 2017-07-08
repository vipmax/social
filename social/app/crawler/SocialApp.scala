package crawler

import akka.actor.{ActorLogging, ActorSystem, Props}
import com.crawler.core.runners.{CrawlerClient, CrawlerConfig}
import com.crawler.dao.KafkaUniqueSaverInfo
import com.crawler.osn.common.TaskDataResponse
import com.crawler.osn.instagram.{InstagramNewGeoPostsSearchTask, InstagramNewGeoPostsSearchTaskFailureResponse}
import com.crawler.util.Util
import com.mongodb.BasicDBObject
import datasource.KafkaStream

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
  * Created by max on 08.07.17.
  */
object SocialApp {
  var isStarted = false

  def startCrawlerClient(dbStream: KafkaStream) {
    if(isStarted) return

    val masterIp = Util.getCurrentIp() // TODO: add parameter to setttings file
    val myIp = Util.getCurrentIp()

    val system = ActorSystem(CrawlerConfig.clusterName, CrawlerConfig.getConfig(masterIp, myIp, classOf[SocialApp].getSimpleName))
    system.actorOf(Props(new SocialApp(dbStream)), classOf[SocialApp].getSimpleName)
    isStarted = true
  }
}

class SocialApp (dbStream: KafkaStream) extends CrawlerClient with ActorLogging {
  implicit val name = this.getClass.getSimpleName

  override def afterBalancerWakeUp() {
    context.system.scheduler.schedule(
      0 seconds, 5 seconds, self, "instagram"
    )(context.dispatcher)
  }

  override def receiveMassage(massage: Any): Unit = massage match {
    case "instagram" =>
      dbStream.topicsToUsers.keys.foreach { topic =>
        val task = InstagramNewGeoPostsSearchTask(
          query = topic,
          saverInfo = KafkaUniqueSaverInfo("localhost:9092", "localhost", "posts"),
          responseActor = self
        )
        log.info(s"Sending task $task to master")
        send(task)
      }
  }

  override def handleTaskDataResponse(tr: TaskDataResponse) = tr match {
    case InstagramNewGeoPostsSearchTaskFailureResponse(task, resultData, exception) =>
      log.error(s"wrongTopic ${task.query}")

      val wrongTopic = task.query
      if(dbStream.topicsToUsers.containsKey(wrongTopic)) {
        val json = new BasicDBObject("wrongTopic", wrongTopic).toJson
        dbStream.topicsToUsers(wrongTopic).foreach(user => user ! json)
        dbStream.topicsToUsers -= wrongTopic
      }

    case any: TaskDataResponse =>
      println(s"any response = ${any.getClass.getSimpleName}")
  }
}