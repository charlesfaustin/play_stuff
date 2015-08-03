package actors

import akka.actor._
import akka.actor.Actor
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import java.io.File
import java.io.FileOutputStream
import sys.process._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import play.api.libs.json._
import scala.collection.mutable.ListBuffer
import scala.concurrent._

import org.apache.commons.io.IOUtils

import sys.process._
import java.net.URL
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import s3._

case class Message(msg: String)

/*https://www.digitalocean.com/community/tutorials/how-to-set-up-http-authentication-with-nginx-on-ubuntu-12-10
NGINX PASSWORD ACCESS FOR HTTP


https://www.playframework.com/documentation/2.3.x/Production
HOW TO DEPLOY IN PRODUCTION
*/
class FileServeActor extends Actor {

  def receive = {

    case newServeMessage(songList) => {
      println("new serve message")
      val x = songList.map(i => XDB.query[Music].whereEqual("idstring", i).fetchOne().get )
      val y = x.toList.map(i => Future { IOUtils.copy(s3helper.amazonS3Client.getObject(s3helper.bucketName, i.objkey).getObjectContent(), new FileOutputStream(new File(current.path + "/public/utils/" + i.objkey)))  })

      val newSender = sender
      val uu = s3helper.allSucceed(y)

      val objectKey = java.util.UUID.randomUUID.toString + ".mp3"
      val s3filePath = "https://s3-%s.amazonaws.com/%s/%s/%s".format(s3helper.s3region, s3helper.bucketName, "combined",objectKey)
     

      uu onSuccess {

        case bloopp => {
          val filex = x.toList.map(i => current.path + "/public/utils/" + i.objkey).mkString(" ")
          val fileUuid = java.util.UUID.randomUUID.toString
          val shellCmd=  "sox %s  %s/public/crtd/%s.mp3".format(filex, current.path, fileUuid )
          val output = shellCmd.!
          val newFilePath = s"%s/public/crtd/%s.mp3".format(current.path, fileUuid)
          val c = new java.io.File(newFilePath)
          val createdFile = models.CrtdFile(filename = s"$fileUuid.mp3",  idstring = fileUuid, filepath = s3filePath, objkey= objectKey)
          s3helper.amazonS3Client.putObject(s3helper.bucketName , "combined/"+ objectKey, c)

          val id = models.CrtdFile.create(createdFile)
          val shellCmd2=  s"rm $newFilePath"
          val output2 = shellCmd2.!
        
          println("done")



        }

          //s3filepath had to be put outside of the above case match so it was in scope
         newSender ! DoneMessage(s""" { "filename" : "$s3filePath" }""")
      }

      uu onFailure {
        case gg => println("xxxxxx")
      }

     //sender ! DoneMessage(s""" { "filename" : "jhhh" }""")


    }
	    case _ =>
        println("que?")
      
    }
}



object MyWebSocketActor{
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
 
   val FileActor = Akka.system.actorOf(Props[FileServeActor])


    override def postStop() = {
      //this is to test the actor stops if the websocket on the client side is closed
      println("its stopped, no mem leak")
      }

    def receive = {

      case DoneMessage(msg) =>
        out ! msg
        println("serverside done")

      case NoFilesMessage =>
        println("no files")
        //add outstuff to handle no files being available to use
        //should prolly add server side validation to save server resources
      case jsonstring: String =>
        val json: JsValue = Json.parse(jsonstring)
        val buf = (json \ "music" \\ "song").map(_.as[String])
        //FileActor ! ServeMessage(buf)
        FileActor ! newServeMessage(buf)

      case _ =>
        println("ok")


    }

  }

case class newServeMessage(tunes: Seq[String])
case class DoneMessage(msg: String)
case object NoFilesMessage






