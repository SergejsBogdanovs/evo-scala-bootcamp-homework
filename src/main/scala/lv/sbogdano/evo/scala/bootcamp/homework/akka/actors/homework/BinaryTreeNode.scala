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
  private var allElements = Map[Int, ActorRef]()

  override def receive: Receive = {
    case insert: Insert     => {
      removed = false
      doInsert(insert)
    }
    case contains: Contains => doContains(contains)
    case remove: Remove     => doRemove(remove)
    case _ => ""
  }

  private def doInsert(m: Insert): Unit = {
    if (!allElements.contains(m.elem)) {
      val actorRef = context.actorOf(BinaryTreeNode.props(m.elem, removed))
      if (m.elem > elem) subtrees += Right -> actorRef else subtrees += Left -> actorRef
      allElements += m.elem -> actorRef
    }
    m.requester ! OperationFinished(m.id)
  }

  private def doContains(m: Contains): Unit = {
    m.requester ! ContainsResult(m.id, allElements.contains(m.elem))
  }

  private def doRemove(m: Remove): Unit = {
    if (allElements.contains(m.elem)) {
      val actor = allElements(m.elem)
      subtrees = subtrees.filter {
        case (_, ref) => ref != actor
      }
      allElements -= m.elem
    }
    m.requester ! OperationFinished(m.id)
  }
}
