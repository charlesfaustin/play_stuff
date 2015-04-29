package controllers
import akka.actor.PoisonPill

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Akka
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import java.nio.channels.ClosedChannelException

import play.api.libs.iteratee._
import scala.concurrent.ExecutionContext.Implicits.global
import play.twirl.api.Html
import play.api.libs.Comet
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.util.Date


import play.api.libs.concurrent.Execution.Implicits._
import java.io.File
import scala.concurrent.duration._
import play.filters.csrf.CSRF
import play.filters.csrf._
import sys.process._
import java.util.Date


//sort by func for scala
/*
scala> List("a", "fg", "aaa", "e", "wwwwww").sortBy(r => r.length)
res18: List[String] = List(a, e, fg, aaa, wwwwww)
*/

//concantenate strings for cat command
/*
theStrings.mkString(" plus whatever inbetween")
tail object wont have anything after than
look here too http://langref.org/scala/lists/output/join-the-elements-of-a-list-separated-by-commas
*/


//remove all tags, install with pip install eyeD3
/*
 eyeD3 --remove-all  a.mp3
*/

object Application extends Controller {
  
  def index = CSRFAddToken {

    Action {  implicit request =>


    Ok(views.html.index())

             }
     }


  def addPerson = CSRFCheck {

        Action(parse.multipartFormData) { implicit request =>


  
      request.body.file("fileUpload").map { file =>
      import java.io.File
      val filenamez = file.filename.replace(" ","_").replace("'","")
      val contentType = file.contentType 
      //println(contentType.get.getClass)//string

      file.ref.moveTo(new File(s"/tmp/picture/$filenamez"))

      val filePath = s"/tmp/picture/$filenamez" 

      val newMusic = models.Music(filenamez,  java.util.UUID.randomUUID.toString, filePath) 
      val id = models.Music.create(newMusic)
      Ok("File uploaded")
    }


       Redirect(routes.Application.index)
    
   

      }

   }



  def getMusics = Action {
  	val musics = XDB.query[Music].fetch()
    //println(musics.toList)

  	Ok(Json.toJson(musics))
  }



  def sendFile = Action {
    Ok.sendFile(new java.io.File("/Users/Charles/use_your_head.gif"))
  }

  /* renames file */
  def ownFileName = Action {



    if (XDB.query[Music].fetch().toList.length > 0 ){


     val musics = XDB.query[Music].fetch()
     
    val newDate = new Date 
    val newFile = newDate.toString.replace(" ", "_") + ".mp3"
    val fileList = musics.toList.map(i => i.filepath).mkString(" ")

    val endFile = "/tmp/results/$newFile"

    println(fileList)

    val shellCmd=  "cat $fileList > /tmp/results/k.mp3"
    val output = shellCmd.!



    lazy val c = new java.io.File("/tmp/results/k.mp3")
    
        Ok.sendFile(

          content = c,
          fileName = _ => "k"
          )
        }
          else 

          {
             Redirect(routes.Application.index)

          }



  }



  def inlineFile = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      inline = true
      )
  }


}



