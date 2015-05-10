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

case class Message(msg: String)


class FileServeActor extends Actor {

  def receive = {
	case ServeMessage => {
        println("starting...")


        if (XDB.query[Music].fetch().toList.length > 0 ){
		  val musics = XDB.query[Music].fetch()
          val fileList = musics.toList.map(i => i.filepath).mkString(" ")

          val fileUuid = java.util.UUID.randomUUID.toString
          val shellCmd=  s"sox $fileList  /tmp/results/$fileUuid.mp3"

          val output = shellCmd.!
          val c = new java.io.File(s"/tmp/results/$fileUuid.mp3")

          //maybe change middle value to fileUuid
          val createdFile = models.CrtdFile(s"$fileUuid.mp3",  fileUuid, s"/tmp/results/$fileUuid.mp3") 
          val crtdid = models.CrtdFile.create(createdFile)
          sender ! DoneMessage(s""" { "idstring" : "$fileUuid" }""")
          //add string field to done message, create text that can be converted into json

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
 
   val FileActor = Akka.system.actorOf(Props[FileServeActor], name = "fileactor")


    def receive = {
      case "serve" =>      
        //out ! ("I received your message")

        /*^^ will send out json parsable objct
        to let user know the data is being processed*/
        FileActor ! ServeMessage
      case DoneMessage(msg) =>
        println("pingback")
        println(msg)
        out ! msg
        // add out ! stuff to update browser of file creation finising
      case NoFilesMessage =>
        println("no files")
        //add outstuff to handle no files being available to use
        //should prolly add server side validation to save server resources
      case _ =>
        println("ok")


    }

  }


case object ServeMessage
case class DoneMessage(msg: String)
case object NoFilesMessage






