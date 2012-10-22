package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views.html._
import views.html.periods._
import models._


object AppraisalPeriods extends Controller with Secured {

  val inputForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "starts" -> optional(date("yyyy-MM-dd")),
      "ends" -> optional(date("yyyy-MM-dd")),
      "closed" -> optional(date("yyyy-MM-dd"))
    )(AppraisalPeriod.apply)(AppraisalPeriod.unapply)
  )
  
  def list = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(periods.list(AppraisalPeriod.list(), user))
    }.getOrElse(Forbidden)
  }
  
  def create = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(periods.create(inputForm, user))
    }.getOrElse(Forbidden)
  }
  
  def save = IsAuthenticated { username => implicit request =>
    inputForm.bindFromRequest.fold(
      errors => BadRequest,
      period => {
        Ok(viewForm.render(AppraisalPeriod.insert(period)))
      }
    ) 
  }
  

  def edit(id: Long) = Action {
    AppraisalPeriod.findById(id).map { period =>
      Ok(editForm.render(id, inputForm.fill(period)))
    }.getOrElse(NotFound)
  }
  
  def update(id: Long) = Action { implicit request =>
    inputForm.bindFromRequest.fold(
      formWithErrors => BadRequest(editForm.render(id, formWithErrors)),
      period => {
        AppraisalPeriod.update(id, period)
        //Home.flashing("success" -> "Appraisal %s has been updated".format(period.id))
        Ok(viewForm.render(period))
      }
    )
  }
  
  def delete(id: Long) = Action {
    AppraisalPeriod.delete(id)
    //Home.flashing("success" -> "Computer has been deleted")
    Ok(viewForm.render(AppraisalPeriod.findById(-1).get))
  }
}