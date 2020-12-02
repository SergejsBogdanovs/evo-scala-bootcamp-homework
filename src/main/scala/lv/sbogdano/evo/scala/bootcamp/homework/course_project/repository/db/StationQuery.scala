package lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db

import doobie.implicits.toSqlInterpolator
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity

object StationQuery {

  def createDb = {
    sql"""
         |CREATE DATABASE IF NOT EXIST sbogdanovs
         |""".update
  }

  def createTable = {
    sql"""
         |CREATE TABLE IF NOT EXISTS stations (
         |  uniqueName VARCHAR(100) PRIMARY KEY,
         |  stationAddress VARCHAR(100),
         |  construction VARCHAR(100),
         |  yearOfManufacture INTEGER,
         |  inServiceFrom INTEGER,
         |  name VARCHAR(100),
         |  cityRegion VARCHAR(100),
         |  latitude NUMERIC,
         |  longitude NUMERIC,
         |  zoneOfResponsibility VARCHAR(100)
         |  )
       """.stripMargin
      .update
  }

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
