package lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages

import io.circe.generic.auto._
import io.circe.parser
import lv.sbogdano.evo.scala.bootcamp.homework.course_project.ws.messages.action.{InputAction, InvalidInput, UserAction}

case class InputMessage private (userLogin: String, action: InputAction)

object InputMessage {

  def from(userLogin: String, text: String): InputMessage = {

    parser.decode[UserAction](text).fold(
      _       => InputMessage(userLogin, InvalidInput),
      message => InputMessage(userLogin, message.action)
    )
  }
}
