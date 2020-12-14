package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import doobie.implicits.toSqlInterpolator
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity

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
         |  stationUniqueName VARCHAR(100),
         |  FOREIGN KEY (stationUniqueName) REFERENCES stations(uniqueName));""".stripMargin

  // insert query
  def insert(stationEntity: StationEntity): doobie.Update0 = {
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

  // update query
  def update(stationEntity: StationEntity): doobie.Update0 = {
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

  // search stations
  def searchWithName(name: String): doobie.Query0[StationEntity] = {
    sql"""
         |SELECT * FROM stations
         |WHERE name = $name
       """.stripMargin
      .query[StationEntity]
  }

  // delete query
  def delete(uniqueName: String): doobie.Update0 = {
    sql"""
         |DELETE FROM stations
         |WHERE uniqueName = $uniqueName
       """.stripMargin
      .update
  }
}
