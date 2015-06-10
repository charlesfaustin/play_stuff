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

        /*  turn this into an aws object thatll be imported
            there will be global aws setting veriables, then a function that takes the file,
            and uploads it. much neater.

            also, separate functions to move files, global variable as the folder path to
            move them to. compose this into different sensible functions.

            The local filemove func will be in a separate object

            diff buckets for initial uploads and combined uploads, function will take vars to put files
            in appropiate bucket

            MOVE AWS CREDENTIALS INTO SCALA FILE OUTSIDE OF SOURCE CONTROL
        */

        //unix gets funny with empty spaces
        val localFilename = file.filename.replace(" ","_").replace("'","")

        //duplication of strings, tidy up into a reusable val
        file.ref.moveTo(new File(s"/Users/Charles/seven/hey/public/up/$localFilename"))
        val filenames = java.util.UUID.randomUUID.toString + ".mp3"
        val bucketName = "cfgplaytest"
        val s3region = "eu-west-1"
        val filePath = "https://s3-%s.amazonaws.com/%s/%s".format(s3region, bucketName, filenames)
        val newMusic = models.Music(localFilename,  java.util.UUID.randomUUID.toString, filePath) 

        val AWS_ACCESS_KEY = ""
        val AWS_SECRET_KEY = ""
        //MOVE AWS CREDENTIALS INTO SCALA FILE OUTSIDE OF SOURCE CONTROL

        val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
        val amazonS3Client = new AmazonS3Client(yourAWSCredentials)
        //http://havecamerawilltravel.com/photographer/how-allow-public-access-amazon-bucket

        val newFile = new File(s"/Users/Charles/seven/hey/public/up/$localFilename")
        amazonS3Client.putObject(bucketName, filenames, newFile)


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



