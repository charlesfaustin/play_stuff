import play.api._
import play.api.mvc._
import play.filters.csrf._



object Global extends GlobalSettings {

	override def onStart(app: Application) {
    Logger.info("xxxxxxApplication has started")
  } 


   override def onStop(app: Application) {
    Logger.info("xxxxxxApplication shutdown...")
  }  


}

