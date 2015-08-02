package models

import play.api.libs.json._
import java.util.Date

//add uuid val
case class Music(filename: String,  idstring: String, filepath: String, objkey: String) 

object Music {

  def create(music: Music){
    XDB.save(music)
  }
  
  implicit val musicFormat = Json.format[Music]
}


case class CrtdFile(filename: String,  idstring: String, filepath: String, objkey: String)

object CrtdFile {

  def create(crtdfile: CrtdFile){
    XDB.save(crtdfile)
  }
  
  implicit val crtdfileFormat = Json.format[CrtdFile]
}