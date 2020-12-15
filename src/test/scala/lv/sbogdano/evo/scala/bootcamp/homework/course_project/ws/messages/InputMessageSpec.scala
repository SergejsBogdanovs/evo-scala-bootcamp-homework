package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.{Completed, Job, Normal, Pending}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class InputMessageSpec extends AnyFlatSpec with Matchers {

  "InputMessage" should "decode user action EnterJobSchedule as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, enterJobScheduleActionText) shouldBe InputMessage(userLogin, EnterJobSchedule)
  }

  it should "decode user action FindJobsByUser as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, findJobsByUserText) shouldBe InputMessage(userLogin, FindJobsByUser)
  }

  it should "decode user action FindJobsByUserAndStatus as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, findJobsByUserAndStatusText) shouldBe InputMessage(userLogin, FindJobsByUserAndStatus(Completed))
  }

  it should "decode user action AddJobToSchedule as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, addJobToScheduleText) shouldBe InputMessage(userLogin, AddJobToSchedule(job))
  }

  it should "decode user action UpdateJobStatus as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, updateJobStatusText) shouldBe InputMessage(userLogin, UpdateJobStatus(job.id, Pending))
  }

  it should "decode user action UpdateJobPriority as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, updateJobPriorityText) shouldBe InputMessage(userLogin, UpdateJobPriority(job.id, Normal()))
  }

  it should "decode user action DeleteJobFromSchedule as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, deleteJobFromScheduleText) shouldBe InputMessage(userLogin, DeleteJobFromSchedule(job))
  }

  it should "decode user action InvalidInput as text to InputMessage entity" in new Scope {
    InputMessage.from(userLogin, invalidInputText) shouldBe InputMessage(userLogin, InvalidInput)
  }

  trait Scope {
    val userLogin = "user"
    val instant: Instant = Instant.now()

    val stationEntity: StationEntity = StationEntity(
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

    val job: Job = Job(
      1,
      "sergejs",
      Pending,
      Normal(),
      stationEntity
    )

    val enterJobScheduleActionText: String = UserAction(instant, EnterJobSchedule).asJson.noSpaces
    val findJobsByUserText: String = UserAction(instant, FindJobsByUser).asJson.noSpaces
    val findJobsByUserAndStatusText: String = UserAction(instant, FindJobsByUserAndStatus(Completed)).asJson.noSpaces
    val addJobToScheduleText: String = UserAction(instant, AddJobToSchedule(job)).asJson.noSpaces
    val updateJobStatusText: String = UserAction(instant, UpdateJobStatus(job.id, Pending)).asJson.noSpaces
    val updateJobPriorityText: String = UserAction(instant, UpdateJobPriority(job.id, Normal())).asJson.noSpaces
    val deleteJobFromScheduleText: String = UserAction(instant, DeleteJobFromSchedule(job)).asJson.noSpaces
    val invalidInputText: String = UserAction(instant, InvalidInput).asJson.noSpaces
  }
}
