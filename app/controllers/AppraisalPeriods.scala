package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views.html._
import views.html.periods._
import models._


object AppraisalPeriods extends Controller {

  val inputForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "starts" -> optional(date("yyyy-MM-dd")),
      "ends" -> optional(date("yyyy-MM-dd")),
      "closed" -> optional(date("yyyy-MM-dd"))
    )(AppraisalPeriod.apply)(AppraisalPeriod.unapply)
  )
  
  def create = Action {
    Ok(createForm.render(inputForm))
  }
  
  def save = Action { implicit request =>
    inputForm.bindFromRequest.fold(
      formWithErrors => BadRequest(createForm.render(formWithErrors)),
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