package lv.sbogdano.evo.scala.bootcamp.homework.json

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

import cats.implicits.toBifunctorOps
import cats.instances.list._
import cats.syntax.traverse._
import com.google.gson.JsonNull
import io.circe
import io.circe.generic.JsonCodec
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, HCursor, Json}
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalaj.http.Http

import scala.util.Try

class JsonHomeworkSpec extends AnyWordSpec with Matchers with EitherValues{
  import JsonHomeworkSpec._

  "NBA JSON API client" should {
    "get info about today games" in {
      val date = LocalDate.now()
      val scoreboardOrError = fetchScoreboard(date)
      val scoreboard = scoreboardOrError.getOrElse(fail(scoreboardOrError.toString))
      val allGameIds = scoreboard.games.map(_.gameId)
      val gameInfosOrError = allGameIds.map(fetchGameInfo(date, _)).sequence
      gameInfosOrError.getOrElse(fail(gameInfosOrError.toString))
      succeed
    }

    "fetch games for 14 Feb 2020" in {
      val date = LocalDate.of(2020, 2, 14)
      val scoreboardOrError = fetchScoreboard(date)
      val scoreboard = scoreboardOrError.getOrElse(fail(scoreboardOrError.toString))
      val allGameIds = scoreboard.games.map(_.gameId)
      val gameInfosOrError = allGameIds.map(fetchGameInfo(date, _)).sequence
      val gameInfos = gameInfosOrError.getOrElse(fail(gameInfosOrError.toString))
      gameInfos.size must be(1)
    }
  }
}

object JsonHomeworkSpec {

  import  io.circe.generic.extras._


  @ConfiguredJsonCodec
  final case class TeamTotals(assists: String,
                              @JsonKey("full_timeout_remaining") fullTimeoutRemaining: String,
                              plusMinus: String)

  implicit val config: Configuration = Configuration.default

//  implicit val config: Configuration = Configuration.default.copy(
//    transformMemberNames = {
//      case "fullTimeoutRemaining" => "full_timeout_remaining"
//      case other => other
//    }
//  )

  //  implicit val teamTotalsDecoder: Decoder[TeamTotals] =
//    Decoder.forProduct3("assists", "full_timeout_remaining", "plusMinus")(TeamTotals.apply)

//  implicit val decodeTeamTotals: Decoder[TeamTotals] = (c: HCursor) => for {
//    assists <- c.downField("assists").as[String]
//    fullTimeoutRemaining <- c.downField("full_timeout_remaining").as[String]
//    plusMinus <- c.downField("plusMinus").as[String]
//  } yield {
//    TeamTotals(assists, fullTimeoutRemaining, plusMinus)
//  }

  @JsonCodec final case class TeamBoxScore(totals: TeamTotals)
  @JsonCodec final case class GameStats(hTeam: TeamBoxScore, vTeam: TeamBoxScore)

  @JsonCodec final case class PrevMatchup(gameDate: LocalDate, gameId: String)
  implicit val decodeLocalDate: Decoder[LocalDate] = Decoder.decodeString.emap { str =>
    Try(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyyMMdd"))).toEither.leftMap(err => "LocalDate" + err.getMessage)
  }

  @JsonCodec final case class BoxScore(
                                        basicGameData: Game,
                                        previousMatchup: PrevMatchup,
                                        stats: Option[GameStats],
                                      )


  @JsonCodec final case class JustScore(score: String)
  @JsonCodec final case class TeamStats(
                                         linescore: List[JustScore],
                                         loss: String,
                                         score: String,
                                         teamId: String,
                                         triCode: String
                                       )
  @JsonCodec final case class GameDuration(hours: String, minutes: String)
  @JsonCodec final case class Arena(
                                     city: String,
                                     country: String,
                                     isDomestic: Boolean,
                                     name: String,
                                     stateAbbr: String
                                   )

  @ JsonCodec final case class Game(
                                     arena: Arena,
                                     attendance: String,
                                     endTimeUTC: Option[ZonedDateTime],
                                     gameDuration: GameDuration,
                                     gameId: String,
                                     gameUrlCode: String,
                                     hTeam: TeamStats,
                                     isBuzzerBeater: Boolean,
                                     startTimeUTC: ZonedDateTime,
                                     vTeam: TeamStats,
                                   )

  @JsonCodec final case class Scoreboard(games: List[Game], numGames: Int)

  private def fetchScoreboard(date: LocalDate): Either[circe.Error, Scoreboard] = {
    val dateString = date.format(DateTimeFormatter.BASIC_ISO_DATE)
    val body = Http(s"https://data.nba.net/10s/prod/v1/$dateString/scoreboard.json").asString.body
    decode[Scoreboard](body)
  }

  private def fetchGameInfo(date: LocalDate, gameId: String): Either[circe.Error, BoxScore] = {
    val dateString = date.format(DateTimeFormatter.BASIC_ISO_DATE)
    val body = Http(s"https://data.nba.net/10s/prod/v1/$dateString/${gameId}_boxscore.json").asString.body
    decode[BoxScore](body)
  }


}
