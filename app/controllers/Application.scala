package controllers

import java.io.File
import sys.process._

import play.api.libs.json.Json
import play.api.mvc.{Controller, Action, WebSocket}
import play.filters.csrf._
import models._
import actors._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsNull,Json,JsString,JsValue}
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

val myActor = Akka.system.actorOf(Props[FileServeActor], name = "fileserveactor")


  def index =  CSRFAddToken  {

    Action {  implicit request =>
    Ok(views.html.index())

             }
     }



  def socket = WebSocket.using[JsValue] { request =>

      //log events to scala console
      val in = Iteratee.foreach[JsValue](println).map { _ =>
        println("Disconnected")
      }

      val jf: JsValue = Json.obj(
         "name" -> "Watership Down")
      //send a single hello! message
      val out = Enumerator(jf)
      //http://stackoverflow.com/questions/11768221/firefox-websocket-security-issue

      (in, out)

  }



  def backgroundCreate = Action {
    myActor ! Message("Go")
    Redirect(routes.Application.index)
  }

  def serve(idString: String) = Action {

    val artist = XDB.query[CrtdFile].whereEqual("idstring", idString).fetchOne().get
    val serveFile = artist.filepath
    val c = new java.io.File(s"$serveFile")

    Ok.sendFile(
      content = c,
      fileName = _ => "generated_file.mp3"
    )

  }


  def addPerson = CSRFCheck {

      Action(parse.multipartFormData) { implicit request =>
  
        request.body.file("fileUpload").map { file =>
        import java.io.File
        val filenamez = file.filename.replace(" ","_").replace("'","")
        //val contentType = file.contentType 
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


   // maybe abstract the two json funcs into one, taking a param
  def getMusics = Action {
  	val musics = XDB.query[Music].fetch()
    //println(musics.toList)
  	Ok(Json.toJson(musics))
  }


  def getCreatedFiles = Action {
    val createdfiles = XDB.query[CrtdFile].fetch()
    Ok(Json.toJson(createdfiles))
  }





}



