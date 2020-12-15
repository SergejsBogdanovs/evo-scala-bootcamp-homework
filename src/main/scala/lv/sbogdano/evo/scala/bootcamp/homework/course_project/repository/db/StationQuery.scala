package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import doobie.Fragment
import doobie.implicits.toSqlInterpolator
import doobie.util.{Read, Write}
import doobie.util.fragment.Fragment
import doobie.util.meta.Meta
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Completed, High, Job, Low, Normal, Pending, Priority, Rejected, Status}

object StationQuery {

  def createDb = {
    sql"""
         |CREATE DATABASE IF NOT EXIST sbogdanovs
         |""".update
  }

  val createTableStations = {
    """CREATE TABLE IF NOT EXISTS stations (
         |  uniqueName VARCHAR(100) PRIMARY KEY,
         |  stationAddress VARCHAR(100),
         |  construction VARCHAR(100),
         |  yearOfManufacture INTEGER,
         |  inServiceFrom INTEGER,
         |  name VARCHAR(100),
         |  cityRegion VARCHAR(100),
         |  latitude NUMERIC,
         |  longitude NUMERIC,
         |  zoneOfResponsibility VARCHAR(100));""".stripMargin
  }

  val createTableSchedule =
    """CREATE TABLE IF NOT EXISTS schedule (
         |  id INTEGER AUTO_INCREMENT PRIMARY KEY,
         |  userLogin VARCHAR(100),
         |  status VARCHAR(100),
         |  priority VARCHAR(100),
         |  station VARCHAR(100),
         |  FOREIGN KEY (station) REFERENCES stations(uniqueName));""".stripMargin

  def insertStation(stationEntity: StationEntity): doobie.Update0 = {
    sql"""
         |INSERT INTO stations (
         |  uniqueName,
         |  stationAddress,
         |  construction,
         |  yearOfManufacture,
         |  inServiceFrom,
         |  name,
         |  cityRegion,
         |  latitude,
         |  longitude,
         |  zoneOfResponsibility
         |)
         |VALUES (
         |  ${stationEntity.uniqueName},
         |  ${stationEntity.stationAddress},
         |  ${stationEntity.construction},
         |  ${stationEntity.yearOfManufacture},
         |  ${stationEntity.inServiceFrom},
         |  ${stationEntity.name},
         |  ${stationEntity.cityRegion},
         |  ${stationEntity.latitude},
         |  ${stationEntity.longitude},
         |  ${stationEntity.zoneOfResponsibility}
         |)
        """.stripMargin
      .update
  }

  def updateStation(stationEntity: StationEntity): doobie.Update0 = {
    sql"""
         |UPDATE stations SET
         |  stationAddress = ${stationEntity.stationAddress},
         |  construction = ${stationEntity.construction},
         |  yearOfManufacture = ${stationEntity.yearOfManufacture},
         |  inServiceFrom = ${stationEntity.inServiceFrom},
         |  name = ${stationEntity.name},
         |  cityRegion = ${stationEntity.cityRegion},
         |  latitude = ${stationEntity.latitude},
         |  longitude = ${stationEntity.longitude},
         |  zoneOfResponsibility = ${stationEntity.zoneOfResponsibility}
         |WHERE uniqueName = ${stationEntity.uniqueName}
        """.stripMargin
      .update
  }

  def searchStationByName(name: String): doobie.Query0[StationEntity] = {
    sql"""
         |SELECT * FROM stations
         |WHERE name = $name
       """.stripMargin
      .query[StationEntity]
  }

  def deleteStation(uniqueName: String): doobie.Update0 = {
    sql"""
         |DELETE FROM stations
         |WHERE uniqueName = $uniqueName
       """.stripMargin
      .update
  }

  val jobs: Fragment = fr"SELECT * FROM schedule"
  val stations: Fragment = fr"SELECT * FROM stations"

  implicit val statusRead: Read[Status] = Read[String].map {
    case "completed" => Completed
    case "pending" => Pending
    case "rejected" => Rejected
  }

  implicit val statusWrite: Write[Status] = Write[String].contramap(s => s.toString)

  implicit val priorityRead: Read[Priority] = Read[String].map {
    case "high" => High()
    case "normal" => Normal()
    case "low" => Low()
  }

  implicit val priorityWrite: Write[Priority] = Write[String].contramap(s => s.toString)

  val fetchScheduleAndStations: Fragment =
    fr"""SELECT
         st.uniqueName,
         st.stationAddress,
         st.construction,
         st.yearOfManufacture,
         st.inServiceFrom,
         st.name,
         st.cityRegion,
         st.latitude,
         st.longitude,
         st.zoneOfResponsibility,
          sch.id,
          sch.userLogin,
          sch.status,
          sch.priority
          FROM schedule sch INNER JOIN stations st ON sch.station = stations.uniqueName"""


  def finsJobsByUser(userLogin: UserLogin): doobie.ConnectionIO[List[Job]] =
    (fetchScheduleAndStations ++ fr"WHERE userLogin = $userLogin").query[Job].to[List]

  def findJobsByUserAndStatus(userLogin: UserLogin, status: Status): doobie.ConnectionIO[List[Job]] =
    (fetchScheduleAndStations ++ fr"WHERE userLogin = $userLogin AND status = ${status.toString}" ).query[Job].to[List]

  def updateJobPriority(userLogin: UserLogin, jobId: Int, priority: Priority): doobie.ConnectionIO[Int] =
    sql"""UPDATE schedule SET priority = ${priority.toString} WHERE userLogin = $userLogin AND id = $jobId""".stripMargin.update.run

  def updateJobStatus(userLogin: UserLogin, jobId: Int, status: Status): doobie.ConnectionIO[Int] =
    sql"""UPDATE schedule SET status = ${status.toString} WHERE userLogin = $userLogin AND id = $jobId""".stripMargin.update.run

  def addJobToSchedule(job: Job): doobie.ConnectionIO[Int] =
    sql"""INSERT INTO schedule (id, userLogin, status, priority, station)
         VALUES (${job.id}, ${job.userLogin}, ${job.status.toString}, ${job.priority.toString}, ${job.station.uniqueName})""".stripMargin.update.run

  def deleteJobFromSchedule(job: Job): doobie.Update0 =
    sql"""DELETE FROM schedule WHERE userLogin = ${job.userLogin} AND id = ${job.id}""".stripMargin.update

}
