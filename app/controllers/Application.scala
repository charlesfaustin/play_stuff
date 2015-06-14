package controllers

import java.io.File
import sys.process._

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials

import play.api.libs.json.Json
import play.api.mvc.{Controller, Action, WebSocket}
import play.filters.csrf._
import models._
import actors._
import s3._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props

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




  def index =  CSRFAddToken  {

    Action {  implicit request =>

    //write down in the actor code the steps to make the changes
    //look at future stuff, and waiting for all of them to finish
    //just do initial file upload first

    Ok(views.html.index())

             }
     }


 def socket = WebSocket.acceptWithActor[String,String] { request =>  out =>
    println(out)
    MyWebSocketActor.props(out)
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
        
        val localFilename = s3helper.fileRename(file.filename)
        val newFilePath = s3helper.placeToMoveFile + localFilename
        file.ref.moveTo(new File(newFilePath))

        val filenames = java.util.UUID.randomUUID.toString + ".mp3"
        val s3filePath = "https://s3-%s.amazonaws.com/%s/%s".format(s3helper.s3region, s3helper.bucketName, filenames)

        val newMusic = models.Music(localFilename,  java.util.UUID.randomUUID.toString, s3filePath)

        //http://havecamerawilltravel.com/photographer/how-allow-public-access-amazon-bucket

        val newFile = new File(newFilePath)
        s3helper.amazonS3Client.putObject(s3helper.bucketName, filenames, newFile)

        val id = models.Music.create(newMusic)
        
        //delete moved localfile
        val shellCmd=  s"rm $newFilePath"
        val output = shellCmd.!

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



