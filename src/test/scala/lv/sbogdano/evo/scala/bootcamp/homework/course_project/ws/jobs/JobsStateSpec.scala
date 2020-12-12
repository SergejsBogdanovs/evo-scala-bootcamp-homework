package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs

import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.StationEntity
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.cache.CacheStorage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.InputAction._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputAction.{UserJobSchedule, _}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.OutputActionError._
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobsStateSpec extends AnyFlatSpec with Matchers {

  "JobState process EnterJobSchedule input message" should "return Seq(WelcomeUser) as output message" in new Scope {
    jobsStateEmpty.process(enterJobSchedule)._2 shouldBe Seq(welcomeUser)
  }

  "JobState process FindJobsByUser input message" should "return Seq(FindJobsError) as output message when jobSchedule is Empty" in new Scope{
    jobsStateEmpty.process(findJobsByUser)._2 shouldBe Seq(findJobsError)
  }

  "JobState process FindJobsByUser input message" should "return Seq(UserJobSchedule) as output message when jobSchedule is NOT Empty" in new Scope{
    jobsStateFull.process(findJobsByUser)._2 shouldBe Seq(userJobSchedule)
  }

  "JobState process FindJobsByUserAndStatus input message" should "return Seq(FindJobsError) as output message when jobSchedule is Empty" in new Scope{
    jobsStateEmpty.process(findJobsByUserAndStatus)._2 shouldBe Seq(findJobsError)
  }

  "JobState process FindJobsByUserAndStatus input message" should "return Seq(UserJobSchedule) as output message when jobSchedule is NOT Empty" in new Scope{
    jobsStateFull.process(findJobsByUserAndStatus)._2 shouldBe Seq(userJobSchedule)
  }

  "JobState process AddJobToSchedule input message" should "return Seq(UserJobSchedule) with added element as output message" in new Scope {
    jobsStateFull.process(addJobToSchedule)._2 shouldBe Seq(userJobScheduleAdd)
  }

  "JobState process AddJobToSchedule input message" should "return Seq(AddJobError) when job already exist" in new Scope {
    jobsStateFull.process(addJobToScheduleExist)._2 shouldBe Seq(addJobError)
  }

  "JobState process UpdateJobStatus input message" should "return Seq(UserJobSchedule) with updated status as output message" in new Scope {
    jobsStateFull.process(updateJobStatus)._2 shouldBe Seq(userJobScheduleUpdatedStatus)
  }

  "JobState process UpdateJobStatus input message" should "return Seq(UpdateJobError) when user not found" in new Scope {
    jobsStateFull.process(updateJobStatusInvalidUser)._2 shouldBe Seq(updateJobStatusInvalidUserError)
  }

  "JobState process UpdateJobStatus input message" should "return Seq(UpdateJobError) when job id not found" in new Scope {
    jobsStateFull.process(updateJobStatusInvalidJobId)._2 shouldBe Seq(updateJobStatusInvalidJobIdError)
  }

  "JobState process UpdateJobPriority input message" should "return Seq(UserJobSchedule) with updated priority as output message" in new Scope {
    jobsStateFull.process(updateJobStatus)._2 shouldBe Seq(userJobScheduleUpdatedStatus)
  }

  "JobState process UpdateJobPriority input message" should "return Seq(UpdateJobError) when user not found" in new Scope {
    jobsStateFull.process(updateJobPriorityInvalidUser)._2 shouldBe Seq(updateJobPriorityInvalidUserError)
  }

  "JobState process UpdateJobPriority input message" should "return Seq(UpdateJobError) when job id not found" in new Scope {
    jobsStateFull.process(updateJobPriorityInvalidJobId)._2 shouldBe Seq(updateJobPriorityInvalidJobIdError)
  }

  "JobState process DeleteJobFromSchedule input message" should "return Seq(UserJobSchedule) without deleted job" in new Scope {
    jobsStateFull.process(deleteJobFromSchedule)._2 shouldBe Seq(deleteJobResult)
  }

  "JobState process DeleteJobFromSchedule input message" should "return Seq(DeleteJobError) when job not found" in new Scope {
    jobsStateFull.process(deleteNotFoundJobFromSchedule)._2 shouldBe Seq(deleteJobError)
  }

  "JobState process InvalidInput input message" should "return Seq(InvalidInputError) when invalid input provided" in new Scope {
    jobsStateFull.process(invalidInput)._2 shouldBe Seq(invalidInputError)
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

    val job4: Job = Job(
      4L,
      "user3",
      Pending,
      Low(),
      stationEntity2
    )

    val userLogin = "user1"
    val invalidLogin = "invalid"

    val jobs = List(job1, job2, job3)
    val jobsStateEmpty: JobsState = JobsState(StationService(CacheStorage()))
    val jobsStateFull: JobsState = JobsState(StationService(CacheStorage(jobsSchedule = jobs)))

    val enterJobSchedule: InputMessage = InputMessage(userLogin, EnterJobSchedule)
    val findJobsByUser: InputMessage = InputMessage(userLogin, FindJobsByUser)
    val findJobsByUserAndStatus: InputMessage = InputMessage(userLogin, FindJobsByUserAndStatus(Pending))
    val addJobToSchedule: InputMessage = InputMessage(userLogin, AddJobToSchedule(job = job4))
    val addJobToScheduleExist: InputMessage = InputMessage(userLogin, AddJobToSchedule(job = job3))
    val updateJobStatus: InputMessage = InputMessage(userLogin, UpdateJobStatus(jobId = 1L, newStatus = Completed))
    val updateJobStatusInvalidUser: InputMessage = InputMessage(invalidLogin, UpdateJobStatus(jobId = 1L, newStatus = Completed))
    val updateJobStatusInvalidJobId: InputMessage = InputMessage(userLogin, UpdateJobStatus(jobId = 100L, newStatus = Completed))
    val updateJobPriority: InputMessage = InputMessage(userLogin, UpdateJobPriority(jobId = 1L, newPriority = High()))
    val updateJobPriorityInvalidUser: InputMessage = InputMessage(invalidLogin, UpdateJobPriority(jobId = 1L, newPriority = High()))
    val updateJobPriorityInvalidJobId: InputMessage = InputMessage(userLogin, UpdateJobPriority(jobId = 100L, newPriority = High()))
    val deleteJobFromSchedule: InputMessage = InputMessage(userLogin, DeleteJobFromSchedule(job1))
    val deleteNotFoundJobFromSchedule: InputMessage = InputMessage(userLogin, DeleteJobFromSchedule(job4))
    val invalidInput: InputMessage = InputMessage(userLogin, InvalidInput)

    val welcomeUser: OutputMessage = OutputMessage(userLogin, WelcomeUser(s"Welcome, ${userLogin.capitalize}! Today is another great day for work."))
    val findJobsError: OutputMessage = OutputMessage(userLogin, FindJobsError("Can not find any jobs"))
    val userJobSchedule: OutputMessage = OutputMessage(userLogin, UserJobSchedule(List(job1, job2)))
    val userJobScheduleAdd: OutputMessage = OutputMessage(userLogin, UserJobSchedule(List(job1, job2, job3, job4)))
    val userJobScheduleUpdatedStatus: OutputMessage = OutputMessage(userLogin, UserJobSchedule(List(job1.copy(status = Completed), job2, job3)))
    val addJobError: OutputMessage = OutputMessage(userLogin, AddJobError("Already exist"))
    val updateJobStatusInvalidUserError: OutputMessage = OutputMessage(invalidLogin, UpdateJobError("Couldn't find job to update status by provided user and/or job id"))
    val updateJobStatusInvalidJobIdError: OutputMessage = OutputMessage(userLogin, UpdateJobError("Couldn't find job to update status by provided user and/or job id"))
    val updateJobPriorityInvalidUserError: OutputMessage = OutputMessage(invalidLogin, UpdateJobError("Couldn't find job to update priority by provided user and/or job id"))
    val updateJobPriorityInvalidJobIdError: OutputMessage = OutputMessage(userLogin, UpdateJobError("Couldn't find job to update priority by provided user and/or job id"))
    val deleteJobResult: OutputMessage = OutputMessage(userLogin, UserJobSchedule(List(job2, job3)))
    val deleteJobError: OutputMessage = OutputMessage(userLogin, DeleteJobError("Couldn't find job to delete"))
    val invalidInputError: OutputMessage = OutputMessage(userLogin, InvalidInputError("Invalid input"))
  }
}

