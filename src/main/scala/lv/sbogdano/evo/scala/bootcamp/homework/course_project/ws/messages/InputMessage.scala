package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{DisconnectUser, InputAction, InvalidInput, UserAction}

import java.time.Instant


case class InputMessage private (userLogin: String, action: InputAction)

object InputMessage {

  def from(userLogin: String, text: String): InputMessage = {
    //
//
//   // {"time":"2020-12-11T09:24:12.348760Z","action":{"FindJobsByUser":{}}}
//    val t1 = UserAction(Instant.now, FindJobsByUser).asJson.noSpaces
//
//    // {"time":"2020-12-11T09:24:12.390684Z","action":{"FindJobsByUserAndStatus":{"status":"pending"}}}
//    val t2 = UserAction(Instant.now, FindJobsByUserAndStatus(status = Pending)).asJson.noSpaces
//
//    // {"time":"2020-12-11T09:24:12.440810Z","action":{"FindJobsByUserAndStatus":{"status":"completed"}}}
//    val t3 = UserAction(Instant.now, FindJobsByUserAndStatus(status = Completed)).asJson.noSpaces
//
//    // {"time":"2020-12-11T09:24:12.482167Z","action":{"AddJobToSchedule":{"job":{"id":1,"userLogin":"sergejs","status":"pending","priority":{"Normal":{"value":2}},"station":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}}
//    val t4 = UserAction(Instant.now, AddJobToSchedule(job1)).asJson.noSpaces
//      // {"time":"2020-12-11T09:24:12.553816Z","action":{"AddJobToSchedule":{"job":{"id":2,"userLogin":"sergejs","status":"pending","priority":{"Normal":{"value":2}},"station":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}}
//    val t41 = UserAction(Instant.now, AddJobToSchedule(job2)).asJson.noSpaces
//
//    // {"time":"2020-12-14T14:53:11.798595Z","action":{"UpdateJobStatus":{"jobId":1,"newStatus":"completed"}}}
//    val t5 = UserAction(Instant.now, UpdateJobStatus(1L, Completed)).asJson.noSpaces
//
//    // {"time":"2020-12-14T14:53:11.850225Z","action":{"UpdateJobPriority":{"jobId":2,"newPriority":{"High":{"value":1}}}}}
//    val t6 = UserAction(Instant.now, UpdateJobPriority(2L, High())).asJson.noSpaces
//
//    // {"time":"2020-12-11T09:24:12.723735Z","action":{"DeleteJobFromSchedule":{"job":{"id":2,"userLogin":"sergejs","status":"pending","priority":{"High":{"value":1}},"station":{"uniqueName":"Riga_AS110","stationAddress":"Dammes 5","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"as110","cityRegion":"Riga","latitude":123.0,"longitude":568.0,"zoneOfResponsibility":"Latgale"}}}}}
//    val t7 = UserAction(Instant.now, DeleteJobFromSchedule(job2)).asJson.noSpaces
//
//    // {"time":"2020-12-11T09:24:12.783566Z","action":{"InvalidInput":{}}}
//    val t8 = UserAction(Instant.now, InvalidInput).asJson.noSpaces

//    // {"time":"2020-12-16T12:08:53.893754Z","action":{"DisconnectUser":{}}}
    val t9 = UserAction(Instant.now, DisconnectUser).asJson.noSpaces

    parser.decode[UserAction](text).fold(
      _       => InputMessage(userLogin, InvalidInput),
      message => InputMessage(userLogin, message.action)
    )
  }
}
