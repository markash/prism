package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views.html._
import models._

object Profiles extends Controller with Secured {

  val inputForm = Form (
      mapping(
          "email" -> email,
          "name" -> nonEmptyText,
          "surname" -> nonEmptyText,
          "password" -> nonEmptyText,
          "confirmation" -> nonEmptyText
      ) 
      (UserRegistration.apply)
      (UserRegistration.unapply)
      verifying ("Passwords do not match", f => f.password == f.confirmation)
  )
  
  def view(email : String) = IsAuthenticated { username => _ =>
	  User.findByEmail(username).map { user =>
		  Ok(profile.view(User.findRegistrationByEmailForUser(email, user).get, user))
	  }.getOrElse(Forbidden)
  }
  
  def edit(email : String) = IsAuthenticated { username => implicit request =>
	  User.findByEmail(username).map { user =>
		  Ok(profile.edit(email, inputForm.fill(User.findRegistrationByEmail(email).get), user))
	  }.getOrElse(Forbidden)
  }
  
  def update(email: String) = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user => 
	    inputForm.bindFromRequest.fold(
	      formWithErrors => BadRequest(profile.edit(email, formWithErrors, user)),
	      registration => {
	        User.update(email, registration)
	        //Home.flashing("success" -> "Profile %s has been updated".format(registration.email))
	        Ok(profile.view(registration, user))
	      }
	    )
    }.getOrElse(Forbidden)
  }
}