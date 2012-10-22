package models

import java.util.{Date};
import play.api.db._;
import play.api.Play.current;

import anorm._;
import anorm.SqlParser._;

case class NavMenu(sections: List[NavMenuSection])
case class NavMenuSection(cssClass: String, title: String, text: String, items: List[NavMenuItem])
case class NavMenuItem(id: String, title: String, text: String, uri: String, cssClass: String)

case class AppraisalPeriod (id: Pk[Long] = NotAssigned, starts: Option[Date], ends: Option[Date], closed: Option[Date])
case class Appraisal (id: Pk[Long], periodId: Option[Long], completed: Option[Date], items: List[AppraisalItem])
case class AppraisalItem (id: Pk[Long], appraisalId: Option[Long], description: String, score: Option[Double])

	
/* Helper for pagination */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object AppraisalPeriod {
	/* ResultSet row parser */
	val simple = {
	  get[Pk[Long]]("appraisal_period.id") ~
	  get[Option[Date]]("appraisal_period.starts") ~
	  get[Option[Date]]("appraisal_period.ends") ~
	  get[Option[Date]]("appraisal_period.closed") map {
	    case id~starts~ends~closed=>AppraisalPeriod(id, starts, ends, closed)
	  }
	}
	def list(): List[AppraisalPeriod] = {
	  DB.withConnection { implicit connection =>
	    SQL("select id, starts, ends, closed from appraisal_period").as(AppraisalPeriod.simple *)
	  }
	}
	
	def findById(id: Long): Option[AppraisalPeriod] = {
	  DB.withConnection { implicit connection =>
	    SQL("select id, starts, ends, closed from appraisal_period where id = {id}").on('id -> id).as(AppraisalPeriod.simple.singleOpt)
	  }
	}
	def options: Seq[(String, String)] = DB.withConnection { implicit connection =>
		SQL("select id, starts, ends, closed from appraisal_period order by ends desc")
			.as(AppraisalPeriod.simple *)
			.map(c => c.id.toString -> (c.starts.formatted("yyyy-MM-dd") + " : " + c.ends.formatted("yyyy-MM-dd")))
	}
	/* Update */
	def update(id: Long, period: AppraisalPeriod) = {
		DB.withConnection { implicit connection =>
			SQL(
			  """
					update appraisal_period
					set 
						starts = {starts}, 
						ends = {ends},
						closed = {closed}
					where id = {id}
			  """
			).on(
		        'id -> id,
		        'starts -> period.starts,
		        'ends -> period.ends,
		        'closed -> period.closed
			).executeUpdate()
		}
	}
  
  /* Insert */
  def insert(period: AppraisalPeriod): AppraisalPeriod = {
    DB.withConnection { implicit connection =>
      val id:Long = SQL("SELECT NEXTVAL('APPRAISAL_PERIOD_SEQ') as ID").apply().head[Long]("ID")
      
      val count: Long = SQL(
        """
          insert into appraisal_period (
    		  id, starts, ends, closed
          ) values (
    		  {id}, 
    		  {starts}, 
    		  {ends},
    		  {closed}
          )
        """
      ).on(
        'id -> id,
        'starts -> period.starts,
        'ends -> period.ends,
        'closed-> period.closed
      ).executeUpdate()
      
      period.copy(id = new Id(id))
    }
  }
  
  /* Delete */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from appraisal_period where id = {id}").on('id -> id).executeUpdate()
    }
  }
}

object Appraisal {
	/* ResultSet row parser */
	val simple = {
	  get[Pk[Long]]("appraisal.id") ~
	  get[Option[Long]]("appraisal.period_id") ~
	  get[Option[Date]]("appraisal.completed") map {
	    case id~periodId~completed=>Appraisal(id, periodId, completed, AppraisalItem.findForAppraisal(id.get))
	  }
	}
	def findById(id: Long): Option[Appraisal] = {
	  DB.withConnection { implicit connection =>
	    SQL("select id, period_id, completed from appraisal where id = {id}").on('id -> id).as(Appraisal.simple.singleOpt)
	  }
	}
	/* Parse a (Appraisal,AppraisalPeriod) from a ResultSet */
	val withAppraisalPeriod = Appraisal.simple ~ (AppraisalPeriod.simple ?) map {
		case appraisal~appraisal_period => (appraisal,appraisal_period)
	}
	/* Update */
	def update(id: Long, appraisal: Appraisal) = {
		DB.withConnection { implicit connection =>
			SQL(
			  """
					update appraisal
					set 
						completed = {completed}, 
						period_id = {period_id}
					where id = {id}
			  """
			).on(
		        'id -> id,
		        'completed -> appraisal.completed,
		        'period_id -> appraisal.periodId
			).executeUpdate()
    }
  }
  
  /* Insert */
  def insert(appraisal: Appraisal) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into appraisal (
    		  id, completed, period_id
          ) values (
    		  (select next value for appraisal_seq), 
    		  {completed}, 
    		  {period_id}
          )
        """
      ).on(
        'completed -> appraisal.completed,
        'period_id -> appraisal.periodId
      ).executeUpdate()
    }
  }
  
  /* Delete */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from appraisal where id = {id}").on('id -> id).executeUpdate()
    }
  }
}

object AppraisalItem {
  /* ResultSet row parser */
  val simple = {
	  get[Pk[Long]]("appraisal_item.id") ~
	  get[Option[Long]]("appraisal_item.appraisal_id") ~
	  get[String]("appraisal_item.description") ~
	  get[Option[Double]]("appraisal_item.score") map {
	    case id~appraisalId~description~score=>AppraisalItem(id, appraisalId, description, score)
	  }
	}

  val withAppraisal = AppraisalItem.simple ~ (Appraisal.simple ?) map {
    case item~appraisal => (item,appraisal)
  }
  
	def findForAppraisal(id: Long): List[AppraisalItem] = {
	  DB.withConnection { implicit connection =>
	    SQL("select id, appraisal_id, description, score from appraisal_item where appraisal_id = {id}").on('id -> id).as(AppraisalItem.simple.+)
	  }
	}
}