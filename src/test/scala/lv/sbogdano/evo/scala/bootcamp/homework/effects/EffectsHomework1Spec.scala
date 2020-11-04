package lv.sbogdano.evo.scala.bootcamp.homework.effects

import lv.sbogdano.evo.scala.bootcamp.homework.effects.EffectsHomework1.IO
import org.scalatest.flatspec.AnyFlatSpec

class EffectsHomework1Spec extends AnyFlatSpec {

  "IO.fromEither passing Right" should "return IO(1)" in {
    assert(IO.fromEither(Right(1)) == IO(1))
  }

  it should "return "

}
