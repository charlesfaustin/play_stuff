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
      val filenamez = file.filename
      val contentType = file.contentType 

      file.ref.moveTo(new File(s"/tmp/picture/$filenamez"))

      val newMusic = models.Music(filenamez,  java.util.UUID.randomUUID.toString) 
      val id = models.Music.create(newMusic)
      Ok("File uploaded")
    }


       Redirect(routes.Application.index)
    
   

      }

   }



  def getMusics = Action {
  	val musics = XDB.query[Music].fetch()
    println(musics.toList)

  	Ok(Json.toJson(musics))
  }



  def sendFile = Action {
    Ok.sendFile(new java.io.File("/Users/Charles/use_your_head.gif"))
  }

  /* renames file */
  def ownFileName = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      fileName = _ => "newfilename.gif"
      )
  }



  def inlineFile = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      inline = true
      )
  }


}



