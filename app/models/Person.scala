package models

import play.api.libs.json._
import java.util.Date

case class Person(name: String,  email: String, age: Int, eligible: Option[Boolean])//, adate: Date)

object Person {

  def create(person: Person){
    DB.save(person)
  }
  
  implicit val personFormat = Json.format[Person]
}