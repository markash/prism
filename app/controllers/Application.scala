package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._


object Application extends Controller {
  
  def menu = {
	  val item = NavMenuItem("id1", "title", "text", "uri", "")
	  val section = NavMenuSection("tg", "title", "text", List(item))
	  NavMenu(List(section)) 
  }
}