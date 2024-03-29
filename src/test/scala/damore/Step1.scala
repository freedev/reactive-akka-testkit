/**
 * Copyright (C) 2013-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package damore

import akka.actor.{Actor, Props}
import akka.testkit.TestProbe
import damore.ActorB._
import org.scalatest.{FunSuiteLike, Matchers}

import scala.concurrent.duration._

class TestRefWrappingActor(val probe: TestProbe) extends Actor {
  def receive = { case msg => probe.ref forward msg }
}

trait Step1
  extends FunSuiteLike
        with Matchers
{ this: TestSuite =>


  test("expectNoMessage-case: actorB retries MessageB2C every 100 milliseconds") {
    val actorA = TestProbe("Actor-A")
    val actorC = TestProbe("Actor-C")
    val actorB = system.actorOf(ActorB.props(Props(classOf[TestRefWrappingActor], actorC)), "Actor-B")

    actorA.send(actorB, MessageA2B())

    actorA.expectNoMessage(100.milliseconds)

    actorC.expectMsg(MessageB2C())

    // Retries form above
    actorC.expectMsg(200.milliseconds, MessageB2C())
    actorC.expectMsg(200.milliseconds, MessageB2C())

    actorA.expectNoMessage(100.milliseconds)

    actorC.reply(MessageB2C_Ack())

    actorA.expectMsg(MessageA2B_Ack())
  }

}
