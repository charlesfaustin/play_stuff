package actors

import akka.actor._
import akka.actor.Actor
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import java.io.File
import sys.process._
case class Message(msg: String)


class FileServeActor extends Actor {

  def receive = {
	case  Message(msg) => {

		val musics = XDB.query[Music].fetch()
        val fileList = musics.toList.map(i => i.filepath).mkString(" ")

        val fileUuid = java.util.UUID.randomUUID.toString
        val shellCmd=  s"sox $fileList  /tmp/results/$fileUuid.mp3"

        val output = shellCmd.!
        val c = new java.io.File(s"/tmp/results/$fileUuid.mp3")

        //maybe change middle value to fileUuid
        val createdFile = models.CrtdFile(s"$fileUuid.mp3",  java.util.UUID.randomUUID.toString, s"/tmp/results/$fileUuid.mp3") 
        val crtdid = models.CrtdFile.create(createdFile)
	}
	case _ => println("que?")
  }
}







