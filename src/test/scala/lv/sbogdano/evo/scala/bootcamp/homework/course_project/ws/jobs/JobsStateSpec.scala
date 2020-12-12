package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.InputAction._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.{UserJobSchedule, _}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage, SendToUser}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobsStateSpec extends AnyFlatSpec with Matchers {

  "JobState process EnterJobSchedule input message" should "return Seq(WelcomeUser) as output message" in new Scope {
    jobsStateEmpty.process(enterJobSchedule)._2 shouldBe Seq(welcomeUser)
  }

  "JobState process FindJobsByUser input message" should "return Seq(FindJobsError) as output message when jobSchedule is Empty"  in new Scope{
    jobsStateEmpty.process(findJobsByUser)._2 shouldBe Seq(findJobsError)
  }

  "JobState process FindJobsByUser input message" should "return Seq(UserJobSchedule) as output message when jobSchedule is NOT Empty"  in new Scope{
    jobsStateFull.process(findJobsByUser)._2 shouldBe Seq(userJobSchedule)
  }

  "JobState process FindJobsByUserAndStatus input message" should "return Seq(FindJobsError) as output message when jobSchedule is Empty"  in new Scope{
    jobsStateEmpty.process(findJobsByUserAndStatus)._2 shouldBe Seq(findJobsError)
  }

  "JobState process FindJobsByUserAndStatus input message" should "return Seq(UserJobSchedule) as output message when jobSchedule is NOT Empty"  in new Scope{
    jobsStateFull.process(findJobsByUserAndStatus)._2 shouldBe Seq(userJobSchedule)
  }

  trait Scope {

    val stationEntity1: StationEntity = StationEntity(
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

    val stationEntity2: StationEntity = StationEntity(
      uniqueName = "Riga_AS110",
      stationAddress = "Krasta 5",
      construction = "outdoor",
      yearOfManufacture = 1990,
      inServiceFrom = 1991,
      name = "AS110",
      cityRegion = "Daugavpils",
      latitude = 356.45,
      longitude = 876.89,
      zoneOfResponsibility = "Latgale"
    )

    val job1: Job = Job(
      1L,
      "user1",
      Pending,
      Normal(),
      stationEntity1
    )

    val job2: Job = Job(
      2L,
      "user1",
      Pending,
      Normal(),
      stationEntity2
    )

    val job3: Job = Job(
      3L,
      "user2",
      Completed,
      High(),
      stationEntity2
    )

    val jobs = List(job1, job2, job3)
    val jobsStateEmpty: JobsState = JobsState(StationService(CacheStorage()))
    val jobsStateFull: JobsState = JobsState(StationService(CacheStorage(jobsSchedule = jobs)))

    val userLogin = "user1"

    val enterJobSchedule: InputMessage = InputMessage(userLogin, EnterJobSchedule)
    val findJobsByUser: InputMessage = InputMessage(userLogin, FindJobsByUser)
    val findJobsByUserAndStatus: InputMessage = InputMessage(userLogin, FindJobsByUserAndStatus(Pending))

    val welcomeUser: OutputMessage = SendToUser(userLogin, WelcomeUser(s"Welcome, ${userLogin.capitalize}! Today is another great day for work."))
    val findJobsError: OutputMessage = SendToUser(userLogin, FindJobsError("Can not find any jobs"))
    val userJobSchedule: OutputMessage = SendToUser(userLogin, UserJobSchedule(List(job1, job2)))
  }
}

