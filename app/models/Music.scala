package models

import play.api.libs.json._
import java.util.Date

//add uuid val
case class Music(filename: String,  idstring: String) 

object Music {

  def create(music: Music){
    XDB.save(music)
  }
  
  implicit val musicFormat = Json.format[Music]
}