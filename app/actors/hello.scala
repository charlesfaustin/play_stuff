package actors

import akka.actor._



case class Message(msg: String)


class HelloActor(myname: String) extends Actor {
  def receive = {
	case "hello" => println("hello back at you")
	case  Message(msg) => println(msg)
	case _ => println("que?")
  }
}

