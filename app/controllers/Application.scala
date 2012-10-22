package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._

object Application extends Controller with Secured {
  
  def menu = {
      
	  NavMenu(
	      List(
	          NavMenuSection(
	              "projects",
	              "Appraisal Periods", 
	              "Appraisal Periods",
	              List (
	                  NavMenuItem("item01", "List appraisal periods", "List", "/appraisal/periods", ""),
	                  NavMenuItem("item02", "Create appraisal period", "Create", "/appraisal/period/new", "")
	              )
	          ),
	          NavMenuSection(
	              "events",
	              "Appraisals", 
	              "Appraisals",
	              List (
	                  NavMenuItem("item11", "List appraisals", "List", "/appraisal/items", ""),
	                  NavMenuItem("item12", "Create appraisal", "Create", "/appraisal/item/new", "")
	              )
	          )
	      )
	  ) 
  }
  
  def title(): String = {"Prism"}
  def demo(): User = {User("demo", "demo", "demo", "demo")}
  
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
    })
  )
  
  /** Index page */
  def index = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.index(user))
    }.getOrElse(Ok(html.index(Application.demo)))
  }
  
  /** Login page. */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /** Handle login form submission. */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.AppraisalPeriods.list()).withSession("email" -> user._1)
    )
  }

  /** Logout and clean the session. */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

  /**
   * Check if the connected user is a member of this project.
   
  def IsMemberOf(project: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Project.isMember(project, user)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  */
  
  /**
   * Check if the connected user is a owner of this task.
   
  def IsOwnerOf(task: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Task.isOwner(task, user)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  */

}