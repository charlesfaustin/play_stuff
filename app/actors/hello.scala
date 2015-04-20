package actors

import akka.actor._
import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

case class Message(msg: String)


class HelloActor(myname: String) extends Actor {

	context.system.scheduler.scheduleOnce(1000.microsecond) {
  println("u wot m8")

}

/*can specify this outside of an actor, and 
replace 'self' with the actor name, 
I dont see the point tho */
//val scheduledSweep = context.system.scheduler.schedule(0 second, 1 second, self, AnotherSched())
  def receive = {
	case "hello" => println("hello back at you")
	case  Message(msg) => println(msg)
	case AnotherSched() =>
	    println("scheduled thing.")
	case _ => println("que?")
  }
}



sealed trait SchedMessage


case class AnotherSched() extends SchedMessage