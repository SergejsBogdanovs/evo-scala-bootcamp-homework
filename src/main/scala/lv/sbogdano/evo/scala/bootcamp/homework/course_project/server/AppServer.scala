package lv.sbogdano.evo.scala.bootcamp.homework.course_project.server

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.{Config, ServerConfig}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.Storage
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db.{Database, DatabaseStorage}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.server.routes.StationRoutes.makeRouter
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.jobs.WorkerJobsState
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.{InputMessage, OutputMessage, SendToWorker}
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object AppServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for (
      config     <- Config.of();
      transactor <- Database.transactor(config.dbConfig);
      _          <- Database.bootstrap(transactor);
      queue      <- Queue.unbounded[IO, InputMessage];
      topic      <- Topic[IO, OutputMessage](SendToWorker("", ""));
      ref        <- Ref.of[IO, WorkerJobsState](WorkerJobsState());
      exitCode   <- {
        val httpStream = server(transactor, config.serverConfig, ref, queue, topic)

        // Stream to process items from the queue and publish the results to the topic
        // 1. Dequeue
        // 2. apply message to state reference
        // 3. Convert resulting output messages to a stream
        // 4. Publish output messages to the publish/subscribe topic
        val processingStream =
          queue
            .dequeue
            .evalMap(msg => ref.modify(_.process(msg)))
            .flatMap(Stream.emits)
            .through(topic.publish)

        Stream(httpStream, processingStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
      }) yield exitCode
  }

  def server(
              transactor: Transactor[IO],
              serverConfig: ServerConfig,
              jobsScheduleState: Ref[IO, WorkerJobsState],
              queue: Queue[IO, InputMessage],
              topic: Topic[IO, OutputMessage]
            ): Stream[IO, ExitCode] = {

    val storage: Storage = new DatabaseStorage(transactor)
    val router = makeRouter(storage, jobsScheduleState, queue, topic)

    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(router)
      .serve
  }

}
