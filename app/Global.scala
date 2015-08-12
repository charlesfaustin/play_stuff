import play.api._
import play.api.mvc._
import play.filters.csrf._
import play.api.Logger


object Global extends GlobalSettings {

	override def onStart(app: Application) {
    Logger.info("xxxxxxApplication has started")
  } 


   override def onStop(app: Application) {
    Logger.info("xxxxxxApplication shutdown...")
  }  

  // found   : Unit ,  required: scala.concurrent.Future[play.api.mvc.Result]

  /*override def onError(request: RequestHeader, ex: Throwable) = {
      println("error")

      Logger.info("some error...")

  } */ 

}

