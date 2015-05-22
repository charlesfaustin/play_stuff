package actors

import akka.actor._
import akka.actor.Actor
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import java.io.File
import sys.process._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import play.api.libs.json._
import scala.collection.mutable.ListBuffer

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
          val shellCmd=  s"sox $fileList  /tmp/results/$fileUuid.mp3"

          val output = shellCmd.!
          val c = new java.io.File(s"/tmp/results/$fileUuid.mp3")


          val createdFile = models.CrtdFile(s"$fileUuid.mp3",  fileUuid, s"/tmp/results/$fileUuid.mp3") 
          val crtdid = models.CrtdFile.create(createdFile)
          sender ! DoneMessage(s""" { "idstring" : "$fileUuid" }""")


          } else {
            sender ! NoFilesMessage
            println("no files")
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

      case NoFilesMessage =>
        println("no files")
        //add outstuff to handle no files being available to use
        //should prolly add server side validation to save server resources
      case jsonstring: String =>
        val json: JsValue = Json.parse(jsonstring)
        val buf = (json \ "music" \\ "song").map(_.as[String])
        FileActor ! ServeMessage(buf)

      case _ =>
        println("ok")


    }

  }


case class ServeMessage(tunes: Seq[String])
case class DoneMessage(msg: String)
case object NoFilesMessage






