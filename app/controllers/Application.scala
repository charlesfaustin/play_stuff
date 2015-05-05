package controllers

import java.io.File
import sys.process._

import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import play.filters.csrf._
import models._

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
    Ok(views.html.index())

             }
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



  def ownFileName = Action {

    if (XDB.query[Music].fetch().toList.length > 0 ){


        /*  simple code to ping actor here */

        //OGING INTO ACTOR
        val musics = XDB.query[Music].fetch()
     
        val fileList = musics.toList.map(i => i.filepath).mkString(" ")

        val fileUuid = java.util.UUID.randomUUID.toString
        val shellCmd=  s"sox $fileList  /tmp/results/$fileUuid.mp3"
        println(shellCmd)
        val output = shellCmd.!
        val c = new java.io.File(s"/tmp/results/$fileUuid.mp3")

        //maybe change middle value to fileUuid
        val createdFile = models.CrtdFile(s"$fileUuid.mp3",  java.util.UUID.randomUUID.toString, s"/tmp/results/$fileUuid.mp3") 
        val crtdid = models.CrtdFile.create(createdFile)
        //END OF ACTOR STUFF
    
        Ok.sendFile(

          content = c,
          fileName = _ => "generated_file.mp3"
          )
        }
        else 
        {
            Redirect(routes.Application.index)
        }



  }




}



