package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Completed, High, Job, Normal, Pending}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{InputAction, UserAction}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.InputAction._

import java.time.Instant


case class InputMessage private (userLogin: String, action: InputAction)

object InputMessage {

  def from(userLogin: String, text: String): InputMessage = {

    val stationEntity1 = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Stadiona 1",
      construction = "indoor",
      yearOfManufacture = 2011,
      inServiceFrom = 2020,
      name = "AS130",
      cityRegion = "Riga",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "Vidzeme"
    )

    val stationEntity2 = StationEntity(
      uniqueName = "Riga_AS130",
      stationAddress = "Stadiona 1",
      construction = "indoor",
      yearOfManufacture = 2011,
      inServiceFrom = 2020,
      name = "AS130",
      cityRegion = "Riga",
      latitude = 123.45,
      longitude = 567.89,
      zoneOfResponsibility = "Vidzeme"
    )

    val job1 = Job(
      1L,
      "sergejs",
      Pending,
      Normal(),
      stationEntity1
    )

    val job2 = Job(
      2L,
      "sergejs",
      Pending,
      Normal(),
      stationEntity2
    )


   // {"time":"2020-12-11T09:24:12.348760Z","action":{"FindJobsByUser":{}}}
    val t1 = UserAction(Instant.now, FindJobsByUser).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.390684Z","action":{"FindJobsByUserAndStatus":{"status":"pending"}}}
    val t2 = UserAction(Instant.now, FindJobsByUserAndStatus(status = Pending)).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.440810Z","action":{"FindJobsByUserAndStatus":{"status":"completed"}}}
    val t3 = UserAction(Instant.now, FindJobsByUserAndStatus(status = Completed)).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.482167Z","action":{"AddJobToSchedule":{"job":{"id":1,"userLogin":"sergejs","status":"pending","priority":{"Normal":{"value":2}},"station":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}}
    val t4 = UserAction(Instant.now, AddJobToSchedule(job1)).asJson.noSpaces
      // {"time":"2020-12-11T09:24:12.553816Z","action":{"AddJobToSchedule":{"job":{"id":2,"userLogin":"sergejs","status":"pending","priority":{"Normal":{"value":2}},"station":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}}
    val t41 = UserAction(Instant.now, AddJobToSchedule(job2)).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.621061Z","action":{"UpdateJobStatus":{"jobId":1,"status":"completed"}}}
    val t5 = UserAction(Instant.now, UpdateJobStatus(1L, Completed)).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.666813Z","action":{"UpdateJobPriority":{"jobId":2,"priority":{"High":{"value":1}}}}}
    val t6 = UserAction(Instant.now, UpdateJobPriority(2L, High())).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.723735Z","action":{"DeleteJobFromSchedule":{"job":{"id":2,"userLogin":"sergejs","status":"pending","priority":{"Normal":{"value":2}},"station":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}}
    val t7 = UserAction(Instant.now, DeleteJobFromSchedule(job2)).asJson.noSpaces

    // {"time":"2020-12-11T09:24:12.783566Z","action":{"InvalidInput":{}}}
    val t8 = UserAction(Instant.now, InvalidInput).asJson.noSpaces

    parser.decode[UserAction](text).fold(
      _       => InputMessage(userLogin, InvalidInput),
      message => InputMessage(userLogin, message.action)
    )
  }
}
