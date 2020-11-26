package lv.sbogdano.evo.scala.bootcamp.homework.akka.actors.homework

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import lv.sbogdano.evo.scala.bootcamp.homework.akka.actors.homework.BinaryTreeSet.OperationReply.{ContainsResult, OperationFinished}

import scala.collection.mutable.ListBuffer

object BinaryTreeNode {
  private sealed trait Position

  private case object Left extends Position
  private case object Right extends Position

  def props(elem: Int, initiallyRemoved: Boolean): Props = Props(new BinaryTreeNode(elem, initiallyRemoved))
}

final class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet.Operation._

  private var subtrees = Map[Position, ActorRef]()
  private var removed = initiallyRemoved

  override def receive: Receive = {
    case insert: Insert     => doInsert(insert)
    case contains: Contains => doContains(contains)
    case remove: Remove     => doRemove(remove)
  }

  private def doInsert(m: Insert): Unit = {
    if (m.elem != elem) {
      val position = if (m.elem > elem) Right else Left
      subtrees.get(position) match {
        case Some(value) => value ! m
        case None => {
          val actor = context.actorOf(BinaryTreeNode.props(m.elem, initiallyRemoved = false))
          subtrees = subtrees + (position -> actor)
          m.requester ! OperationFinished(m.id)
        }
      }
    } else {
      m.requester ! OperationFinished(m.id)
    }
  }

  private def doContains(m: Contains): Unit = {
    if (m.elem == elem) {
      if (removed) m.requester ! ContainsResult(m.id, false) else m.requester ! ContainsResult(m.id, true)
    } else if (subtrees.nonEmpty) {
      subtrees.values.foreach(a => a ! m)
    } else {
      m.requester ! ContainsResult(m.id, false)
    }
  }

  private def doRemove(m: Remove): Unit = {
    if (m.elem == elem) {
      removed = true
      m.requester ! OperationFinished(m.id)
    } else if (subtrees.nonEmpty) {
      subtrees.values.foreach(a => a ! m)
    } else {
      m.requester ! OperationFinished(m.id)
    }

  }
}
