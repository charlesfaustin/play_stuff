package models

import play.api.libs.json._
import java.util.Date

//add uuid val
case class Person(name: String,  email: String, age: Int, eligible: Option[Boolean]) //, adate: Date)

object Person {

  def create(person: Person){
    XDB.save(person)
  }
  
  implicit val personFormat = Json.format[Person]
}