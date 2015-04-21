package controllers
import akka.actor.PoisonPill
//new model fields, of diff data types
//diff form field validation, optional fields etc, check the docs
//new page to show all the persons with standard play loop templating //val persons = DB.query[Person].fetch(), ~line 90
//new page to show persons filtered by url params, will involve db querying, look up sorm docs
//leave existing controller funcs+ views, write new ones
//maybe a diff db backend, postgres, sqlite etc
//authentication
//lets create some sort of person database app, use it as a vehicle
//aysnchronous stuff with akka, like sending emails or something
//other crud stuff, update, delete etc
//http://www.fdmtech.org/2012/03/a-better-example-of-play-framework-2-0-with-mybatis-for-scala-beta/
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Akka
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import java.nio.channels.ClosedChannelException

import play.api.libs.iteratee._
import scala.concurrent.ExecutionContext.Implicits.global
import play.twirl.api.Html
import play.api.libs.Comet
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.util.Date
import actors._

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import play.filters.csrf.CSRF
import play.filters.csrf._


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

    // for def jsonvalidate further below
    //doesnt seem to be needed tho :|
     implicit val rds = (
    (__ \ 'name).read[String] and
    (__ \ 'age).read[Int] and
    (__ \ 'email).read[String] and
    (__ \ 'eligible).read[String]

  ) tupled 

  val system = ActorSystem("HelloWorldSystem")
  val helloActor = system.actorOf(Props(new HelloActor("dave")), name="helloactor")

  helloActor ! Message("okdemdem")





  def xmlRequest = Action { request =>
    request.body.asXml.map { xml =>
      (xml \\ "name" headOption).map(_.text).map { name =>
        Ok("Hello " + name)

      }.getOrElse {
        BadRequest("Missing paramater [name]")
      }

    }.getOrElse{
      BadRequest("expecting xml data")
    }

  }





//seems the actors have to be specified elsewhere and imported
//http://typesafe.com/activator/template/play-akka-angular-websocket
//https://github.com/gigiigig/play-akka-angular-websocket/blob/master/app/controllers/AppController.scala
//^^ those 2 go together, come back to actors later anyway

//https://github.com/ap78/alertme/blob/0ae41b209fe619b8ee67822d876cf5fedd64665a/app/controllers.scala

  def index = CSRFAddToken {

    Action {  implicit request =>
 
    Ok(views.html.index(personForm))

    /* .withSession(
  "connected" -> "user@gmail.com") */
             }
     }

  val personForm: Form[Person] = Form {
  	mapping(
      //"name" -> text,
      "name" -> default(nonEmptyText,"Mikey"),
      "email" -> email,
      //"age" -> number
      "age" -> number(min=0, max=100),
      //"age" -> default(number, 34) //works, but not shown in html, just saved to db
      "eligible" -> optional(checked("eligible"))
      //"adate" -> date("yyyy-MM-dd")
  	)(Person.apply)(Person.unapply)
  }



  def addPerson = CSRFCheck {
      // https://github.com/adamnfish/csrftest csrf example that helped
      // this kinda helped too https://www.playframework.com/documentation/2.3.x/ScalaCsrf
      // and http://nickcarroll.me/2013/02/11/protect-your-play-application-with-the-csrf-filter/

        Action(parse.multipartFormData) { implicit request =>

    // implicit val token = CSRF.getToken(request).get
    //val csrfToken = CSRF.getToken(request).getOrElse(CSRF.Token(""))
  	//val person = personForm.bindFromRequest.get

    val yyy = request.body.file("fileUpload")==None


    request.body.file("fileUpload").map { file =>
      import java.io.File
      val filename = file.filename
      val contentType = file.contentType 
      /* ^^ prolly same as mimetype for s3 upload
       http://stackoverflow.com/questions/3452381/whats-the-difference-of-contenttype-and-mimetype
       */
      file.ref.moveTo(new File(s"/tmp/picture/$filename"))
      Ok("File uploaded")
    }

    //println(request.body.asFormUrlEncoded.get("adate")) //prints a particular post data value
    
    personForm.bindFromRequest.fold(
      formWithErrors => {
      // binding failure, you retrieve the form containing errors:
      BadRequest(views.html.index(formWithErrors))
    },
    person => {

      /* binding success, you get the actual value. */
      val eligible = if (person.eligible==None) Option(false) else Option(true)

      /* prepare new person object, but dont save */
      val newPerson = models.Person(person.name, person.email, person.age, eligible)
      
      if (yyy==true){ 
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
       } else {

      /* save new person object */
      val id = models.Person.create(newPerson)
      //add on a flashing success message later
      Redirect(routes.Application.index)}

          }
      )


      }
     // here
   }



  def getPersons = Action {
  	val persons = XDB.query[Person].fetch()
    println(persons.toList)

  	Ok(Json.toJson(persons))
  }

  def returnFile = Action {
    val file = new java.io.File("/Users/Charles/use_your_head.gif")
    val fileContent: Enumerator[Array[Byte]] = Enumerator.fromFile(file)
    Result(header = ResponseHeader(200, Map(CONTENT_LENGTH ->file.length.toString)),
      body = fileContent)
  }

  /*
  sendFile better than returnFile, sendfile intitiates
   download protocol in firefox, returnFile just 
   opens it in the  browser. also if you save the url 
   has a hyperlink and right click save as, returnfile
   doesnt keep the file extension type, sendFile does
   */
  def sendFile = Action {
    Ok.sendFile(new java.io.File("/Users/Charles/use_your_head.gif"))
  }

  /* renames file */
  def ownFileName = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      fileName = _ => "newfilename.gif"
      )
  }
  //val name = (jsValue \ "name")
   //    val age = (jsValue \ "age")


  
  //curl  --header "Content-type: application/json" --request POST --data '{"name": "Toto","email":"yeah@sdf.com" , "age": 32, "eligible": "true"}' http://localhost:9000/jsonvalidate
  
   def jsonvalidate = Action { request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    // Expecting text body
    jsonBody.map { jsValue =>

      jsValue.validate[(String, Int, String, String)].map{ 
        case (name, age, email, eligible) => 

            val elg = eligible match{
              case "true" => true
              case _ => false
            }
          
            val newPerson = models.Person(name, email, age, Option(elg))
            val new_id = models.Person.create(newPerson)
          Ok(Json.obj("status" ->"OK", "message" -> 
                       ("Hello " + name + ", you're "+ age + "and your email is " + email  + "\n") ))
        }.recoverTotal{
          e => BadRequest(Json.obj("status" ->"BAD", "message" ->
                                    ("Detected error: "+ JsError.toFlatJson(e) + "\n") ))
          }
        }.getOrElse {
          BadRequest("bad request. make sure response is in json and in the right format")  
        }

      } 



  def inlineFile = Action {
    Ok.sendFile(
      content = new java.io.File("/Users/Charles/use_your_head.gif"),
      inline = true
      )
  }

  def bakka = Action {
  Ok.chunked(
    Enumerator("kiki", "foo", "bar").andThen(Enumerator.eof)
    )
  }

  def comet = Action {
    val events = Enumerator(
      """<script>console.log('when')</script>""",
      """<script>console.log('I')</script>""",
      """<script>console.log('say')</script>"""
      )
    Ok.chunked(events).as(HTML)
  }

  /* events &> toCometMessage  is another way 
  of writing events.through(toCometMessage) */

  val toCometMessage = Enumeratee.map[String] { data =>
    Html("""<script>console.log('""" + data +"""')</script>""")
  }

  def comet2 = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.chunked(events &> toCometMessage)
  }


  /* the callback var is the javascript func to wrap the messages in */
  def comet3 = Action {
    val events = Enumerator("buh", "buuh", "bang")
    Ok.chunked(events &> Comet(callback = "console.log"))
  }


  def comet4 = Action {
    val events = Enumerator("comet", "4", "js")
    Ok.chunked(events &> Comet(callback = "parent.cometMessage"))

  }


  def socket = WebSocket.using[String] { request =>

      //log events to scala console
      val in = Iteratee.foreach[String](println).map { _ =>
        println("Disconnected")
      }

      //send a single hello! message
      val out = Enumerator("hello!")
      //http://stackoverflow.com/questions/11768221/firefox-websocket-security-issue

      (in, out)

  }


  def socket2 = WebSocket.using[String] {request =>
    //Concurrent.broadcast returns (Enumerator, Concurrent.Channel)

    //request.session.get("connected").foreach(println)
    val (out, channel) = Concurrent.broadcast[String]

    channel push("come at me bro")

    //log message to stdout and send response back to client
    val in = Iteratee.foreach[String] {
      msg => println(msg)
      /* the Enumerator returned by Concurrent.broadcast 
      subscribes to the channel and will
       receive the pushed messages */
       channel push("I received your message: " + msg)
       /* while (true){

       
         channel push("I received your message: " + msg)
         Thread.sleep(1000) 
       } */
    }
    (in,out)

  }

  //actor socket
  def socket3 = WebSocket.acceptWithActor[String,String] { request =>  out =>
    MyWebSocketActor.props(out)
  }

  object MyWebSocketActor{
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {

    /* 
    scheduler, could be used to retrieve messages from database
    tjeres also a scheduleOnce method
    */
    //val scheduledSweep = context.system.scheduler.schedule(0 second, 1 second, self, UpdateTime())

    def receive = {
      case msg: String =>
      
      //sending stop kills actor and closes websocket
        if (msg=="stop"){
          println("killing actor")
          self ! PoisonPill //this is harmful
        }

      
        out ! ("I received your message: " + msg)
      case UpdateTime() =>
        out ! ("I received your message: at " + System.currentTimeMillis)
        Thread.sleep(2000)


    }



  }
  //end of actor socket




}

//https://github.com/Rhinofly/play-s3 
//^ hook file upload to s3 bucket
/* would have to create a link to the uploaded media,
 maybe a unique string field in the same person class that 
 corresponds to the location of the file in the s3 bucket */
//Some(FilePart(fileUpload,full.png,Some(image/png),TemporaryFile(/var/folders/c7/pgn3b2bn07xc21qsrd33b47m0000gn/T/multipartBody8168371746653103972asTemporaryFile)))


sealed trait SocketMessage


case class UpdateTime() extends SocketMessage


