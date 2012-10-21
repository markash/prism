package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views.html.appraisals._
import models._

object Appraisals extends Controller {

  def submit = Action {
    Ok(index.render(Appraisal.findById(-1).get))
  }
  
  def blank = Action {
    Ok(index.render(Appraisal.findById(-1).get))
  }
  
  def list = Action {
    Ok(index.render(Appraisal.findById(-1).get))
  }
}