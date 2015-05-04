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

  def getCreatedFiles = Action {
    val createdfiles = XDB.query[CrtdFile].fetch()
    Ok(Json.toJson(createdfiles))
  }


  def sendFile = Action {
    Ok.sendFile(new java.io.File("/Users/Charles/use_your_head.gif"))
  }

  /* renames file */
  def ownFileName = Action {



    if (XDB.query[Music].fetch().toList.length > 0 ){


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



  def inlineFile = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      inline = true
      )
  }


}



