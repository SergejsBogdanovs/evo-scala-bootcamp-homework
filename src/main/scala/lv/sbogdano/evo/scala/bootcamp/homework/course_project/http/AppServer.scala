package lv.sbogdano.evo.scala.bootcamp.homework.course_project.http

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.config.{Config, ServerConfig}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.repository.db.{Database, DatabaseStorage}
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.http.routes.StationRoutes.makeRouter
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.service.StationService
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.job.JobsState
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.action.WelcomeUser
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.schedule.messages.{InputMessage, OutputMessage}
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object AppServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for (
      config     <- Config.of();
      transactor <- Database.transactor(config.dbConfig);
      _          <- Database.bootstrap(transactor);
      queue      <- Queue.unbounded[IO, InputMessage];
      topic      <- Topic[IO, OutputMessage](OutputMessage("", WelcomeUser("")));
      ref        <- Ref.of[IO, StationService](StationService(JobsState(), new DatabaseStorage(transactor)));
      exitCode   <- {
        val httpStream: Stream[IO, ExitCode] = server(config.serverConfig, ref, queue, topic)

        // Stream to process items from the queue and publish the results to the topic
        // 1. Dequeue
        // 2. apply message to state reference
        // 3. Convert resulting output messages to a stream
        // 4. Publish output messages to the publish/subscribe topic
        val processingStream: Stream[IO, Unit] =
          queue
            .dequeue
            .evalMap(inputMessage => ref.modify(_.process(inputMessage)))
            .flatMap(Stream.emits)
            .through(topic.publish)

        Stream(httpStream, processingStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
      }) yield exitCode
  }

  def server(
              serverConfig: ServerConfig,
              serviceRef: Ref[IO, StationService],
              queue: Queue[IO, InputMessage],
              topic: Topic[IO, OutputMessage]
            ): Stream[IO, ExitCode] = {

    val router = makeRouter(serviceRef, queue, topic)

    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(router)
      .serve
  }

}
