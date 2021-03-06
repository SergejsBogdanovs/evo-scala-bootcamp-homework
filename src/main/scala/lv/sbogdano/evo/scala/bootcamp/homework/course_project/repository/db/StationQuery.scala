package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import doobie.implicits.toSqlInterpolator
import doobie.util.fragment.Fragment
import doobie.util.update.Update
import doobie.util.{Read, Write}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState.UserLogin
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.{Job, _}

object StationQuery {

  def createDb: doobie.Update0 = {
    sql"""
         |CREATE DATABASE IF NOT EXIST sbogdanovs
         |""".update
  }

  val createTableStations: String = {
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

  val createTableSchedule: String =
    """CREATE TABLE IF NOT EXISTS schedule (
         |  id INTEGER AUTO_INCREMENT PRIMARY KEY,
         |  userLogin VARCHAR(100),
         |  status VARCHAR(100),
         |  priority VARCHAR(100),
         |  station VARCHAR(100),
         |  FOREIGN KEY (station) REFERENCES stations(uniqueName)
         |  ON UPDATE CASCADE
         |  ON DELETE CASCADE
         |  );""".stripMargin

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
         sch.id,
         sch.userLogin,
         sch.status,
         sch.priority,
         st.uniqueName,
         st.stationAddress,
         st.construction,
         st.yearOfManufacture,
         st.inServiceFrom,
         st.name,
         st.cityRegion,
         st.latitude,
         st.longitude,
         st.zoneOfResponsibility
         FROM schedule sch
         INNER JOIN stations st
         ON sch.station = st.uniqueName"""

  val populateStations: String =
    s"""
       |INSERT INTO stations (uniqueName, stationAddress, construction, yearOfManufacture, inServiceFrom, name, cityRegion, latitude, longitude, zoneOfResponsibility)
       |VALUES ('Riga_AS130', 'Dammes 6', 'outdoor', 2010, 2011, 'as130', 'Riga', 45.6123, 12.3456, 'Latgale');
       |""".stripMargin

  val populateSchedule: String =
    s"""
       |INSERT INTO schedule (id, userLogin, status, priority, station)
       |VALUES (1, 'sergejs', 'pending', 'high', 'Riga_AS130');
       |""".stripMargin

  def finsJobsByUser(userLogin: UserLogin): doobie.ConnectionIO[List[Job]] =
    (fetchScheduleAndStations ++ fr"WHERE sch.userLogin = $userLogin AND sch.status <> 'rejected';").query[Job].to[List]

  def insertManyStations(stations: List[StationEntity]): doobie.ConnectionIO[Int] = {
    val sql1 = "REPLACE INTO stations (uniqueName, stationAddress, construction, yearOfManufacture, inServiceFrom, name, cityRegion, latitude, longitude, zoneOfResponsibility) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    Update[StationEntity](sql1).updateMany(stations)
  }

  def insertManyJobs(jobs: List[JobEntity]): doobie.ConnectionIO[Int] = {
    val sql2 =
      """REPLACE INTO schedule (id, userLogin, status, priority, station) VALUES (?, ?, ?, ?, ?)""".stripMargin
    Update[JobEntity](sql2).updateMany(jobs)
  }

  def deleteJobFromSchedule(job: Job): doobie.Update0 =
    sql"""DELETE FROM schedule WHERE userLogin = ${job.userLogin} AND id = ${job.id}""".stripMargin.update

}
