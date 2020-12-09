package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.InputAction._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.Status._

import java.time.Instant


case class InputMessage private (userLogin: String, action: InputAction)

object InputMessage {

  def from(userLogin: String, text: String): InputMessage = {

    val stationEntity = StationEntity(
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

    // {"time":"2020-12-08T13:08:24.229762Z","action":{"ListJobsInputAction":{"status":{"All":{}}}}}
    //val t1 = UserAction(Instant.now, ListJobsInputAction(status = All())).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.272262Z","action":{"ListJobsInputAction":{"status":{"Pending":{}}}}}
    //val t2 = UserAction(Instant.now, ListJobsInputAction(status = Pending())).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.316905Z","action":{"ListJobsInputAction":{"status":{"Completed":{}}}}}
    //val t3 = UserAction(Instant.now, ListJobsInputAction(status = Completed())).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.362470Z","action":{"AddJobsInputAction":{"toUser":"sergejs","stationEntities":[{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}]}}}
    //val t4 = UserAction(Instant.now, AddJobInputAction("sergejs", stationEntities = List(stationEntity))).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.362470Z","action":{"AddJobsInputAction":{"toUser":"sergejs","stationEntities":[{"uniqueName":"Riga_AS131","stationAddress":"Dammes 1","construction":"outdoor","yearOfManufacture":2012,"inServiceFrom":2021,"name":"AS131","cityRegion":"Dagda","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Latgale"}]}}}
    //val t4 = UserAction(Instant.now, AddJobInputAction("sergejs", stationEntities = List(stationEntity))).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.399354Z","action":{"MarkJobAsCompletedInputAction":{"stationEntity":{"uniqueName":"Riga_AS130","stationAddress":"Stadiona 1","construction":"indoor","yearOfManufacture":2011,"inServiceFrom":2020,"name":"AS130","cityRegion":"Riga","latitude":123.45,"longitude":567.89,"zoneOfResponsibility":"Vidzeme"}}}}
    //val t5 = UserAction(Instant.now, MarkJobAsCompletedInputAction(stationEntity)).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.436520Z","action":{"InvalidInputInputAction":{}}}
    //val t6 = UserAction(Instant.now, InvalidInputInputAction()).asJson.noSpaces

    // {"time":"2020-12-08T13:08:24.464492Z","action":{"EnterJobScheduleInputAction":{}}}
    //val t7 = UserAction(Instant.now, DisconnectInputAction()).asJson.noSpaces

    parser.decode[UserAction](text).fold(
      _       => InputMessage(userLogin, InvalidInputInputAction()),
      message => InputMessage(userLogin, message.action)
    )
  }
}
