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


class FileServeActor extends Actor {

  def receive = {
	case ServeMessage(songList) => {
        println("starting...")

        /* val hehe = songList.map(i => XDB.query[CrtdFile].whereEqual("idstring", i).fetchOne().get )
        println(hehe) */


        if (songList.nonEmpty ){


          val hehe = songList.map(i => XDB.query[Music].whereEqual("idstring", i).fetchOne().get )

          val fileList = hehe.toList.map(i => i.filepath).mkString(" ")

          val fileUuid = java.util.UUID.randomUUID.toString
          val shellCmd=  s"sox $fileList  /Users/Charles/seven/hey/public/crtd/$fileUuid.mp3"

          val output = shellCmd.!
          val c = new java.io.File(s"/Users/Charles/seven/hey/public/crtd/$fileUuid.mp3")


          val createdFile = models.CrtdFile(s"$fileUuid.mp3",  fileUuid, s"/Users/Charles/seven/hey/public/crtd/$fileUuid.mp3") 
          val crtdid = models.CrtdFile.create(createdFile)
          sender ! DoneMessage(s""" { "filename" : "$fileUuid.mp3" }""")


          } else {
            sender ! NoFilesMessage
            println("no files")
        }

        
	 

  }
  case newServeMessage(songList) => {
    println("new serve message")
    val x = songList.map(i => XDB.query[Music].whereEqual("idstring", i).fetchOne().get )

    //download mp3 files here
    //http://alvinalexander.com/scala/scala-how-to-download-url-contents-to-string-file
    //import scala.concurrent.impl.Promise.DefaultPromise

    /*
    http://docs.scala-lang.org/overviews/core/futures.html
    http://alvinalexander.com/scala/concurrency-with-scala-futures-tutorials-examples
    http://stackoverflow.com/a/16257414
    http://alvinalexander.com/scala/scala-how-to-download-url-contents-to-string-file


    */
    val y = x.toList.map(i => Future { IOUtils.copy(s3helper.amazonS3Client.getObject(s3helper.bucketName, i.objkey).getObjectContent(), new FileOutputStream(new File("/Users/Charles/seven/hey/public/utils/" + i.objkey)))  })

     //val df = s3helper.amazonS3Client.getObject(s3helper.bucketName, objkey)

     //IOUtils.copy(s3helper.amazonS3Client.getObject(s3helper.bucketName, objkey).getObjectContent(), new FileOutputStream(new File("/Users/Charles/seven/hey/public/utils/" + objkey)));


    //do procedurally before turning into function

    val uu = s3helper.allSucceed(y)

    uu onSuccess {

      case bloopp => {
        val filex = x.toList.map(i => "/Users/Charles/seven/hey/public/utils/" + i.objkey).mkString(" ")
        println(filex)


      }

    }

    uu onFailure {
      case gg => println("xxxxxx")
    }




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
case class ServeMessage(tunes: Seq[String])
case class DoneMessage(msg: String)
case object NoFilesMessage






