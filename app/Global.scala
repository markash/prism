import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) = {
	  InitialData.insert()
  }
  
  // called when a route is found, but it was not possible to bind the request parameters
  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  } 

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = {
    InternalServerError(views.html.errorNotFound.render(throwable.getMessage()))
  }
  
  // 404 - page not found error
  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(views.html.errorNotFound.render(request.uri))
  }   
}

/**
 * Initial set of data to be imported 
 * in the sample application.
 */
object InitialData {
  
  def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)
  
  def insert() = {
    
    if(User.findAll.isEmpty) {
      
      Seq(
        User("mp.ashworth@gmail.com", "Mark", "Ashworth", "secret")
      ).foreach(User.create)
    }
  }
}  