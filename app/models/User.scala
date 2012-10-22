package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class UserRegistration(email: String, name: String, surname: String, password: String, confirmation: String)

case class User(email: String, name: String, surname: String, password: String)

object User {
  
  /** Parse a User from a ResultSet */
  val simple = {
    get[String]("profile.email") ~
    get[String]("profile.name") ~
    get[String]("profile.surname") ~
    get[String]("profile.credentials") map {
      case email~name~surname~password => User(email, name, surname, password)
    }
  }
  
  /** Parse a User from a ResultSet */
  val registration = {
    get[String]("profile.email") ~
    get[String]("profile.name") ~
    get[String]("profile.surname") ~
    get[String]("profile.credentials") ~ 
    get[String]("profile.credentials") map {
      case email~name~surname~password~confirmation => UserRegistration(email, name, surname, password, confirmation)
    }
  }
  
  /** Retrieve a User from email. */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from profile where email = {email}").on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }
  
  /** Retrieve a User from email. */
  def findRegistrationByEmailForUser(email: String, user: User): Option[UserRegistration] = {
    DB.withConnection { implicit connection =>
      SQL(
          """
          select email, name, surname, credentials, credentials confirmation 
          from profile 
          where email = {email}
            and email = {userEmail}
          """
      ).on(
        'email -> email,
        'userEmail -> email
      ).as(User.registration.singleOpt)
    }
  }
  
  /** Retrieve a User from email. */
  def findRegistrationByEmail(email: String): Option[UserRegistration] = {
    DB.withConnection { implicit connection =>
      SQL(
          """
          select email, name, surname, credentials, credentials confirmation 
          from profile 
          where email = {email}
          """
      ).on(
        'email -> email
      ).as(User.registration.singleOpt)
    }
  }
  
  
  /** Retrieve all users. */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select email, name, surname, credentials from profile").as(User.simple *)
    }
  }
  
  /** Authenticate a User. */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select email, name, surname, credentials from profile 
         where 
    		  email = {email} and 
    		  credentials = {credentials}
        """
      ).on(
        'email -> email,
        'credentials -> password
      ).as(User.simple.singleOpt)
    }
  }

  def update(email: String, user: UserRegistration): UserRegistration = {
    DB.withConnection { implicit connection =>
      SQL(
          """
          update profile set
          email = {email},
          name = {name},
          surname = {surname},
          credentials = {credentials}
          where email = {old}
          """
      ).on(
          'old -> email,
          'email -> user.email,
          'name -> user.name,
          'surname -> user.surname,
          'credentials -> user.password
      ).executeUpdate()
      
      user
    }
  }
  
  /** Create a User. */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into profile (
    		  email, name, surname, credentials
          ) values (
    		  {email}, {name}, {surname}, {credentials}
          )
        """
      ).on(
        'email -> user.email,
        'name -> user.name,
        'surname -> user.surname,
        'credentials -> user.password
      ).executeUpdate()
      
      user
    }
  } 
}