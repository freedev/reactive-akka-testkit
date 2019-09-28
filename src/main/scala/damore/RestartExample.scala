package damore

import akka.actor._
import concurrent.duration._

object RestartExample extends App{
  case object Start
  case object MsgA2B
  case object MsgB2C
  case object MsgC2B

  val system = ActorSystem("test")
  val a = system.actorOf(Props[A])
  a ! Start

  class A extends Actor{
    println("new A docs.actor instance created")
    val b = context.actorOf(Props[B], "myb")
    import context.dispatcher

    def receive = {
      case Start =>
        b ! Start
        context.system.scheduler.scheduleOnce(3 seconds, b, MsgA2B)
    }
  }

  class B extends Actor{
    println("new B docs.actor instance created")
    val c = context.actorOf(Props[C], "myc")
    def receive = {
      case Start =>
        c ! MsgB2C
      case MsgC2B =>
        println("got a CB from C")
      case MsgA2B =>
        println("B docs.actor received MessageA2B... throw new RuntimeException")
        throw new RuntimeException("foo")
    }
  }

  class C extends Actor{
    println("new C docs.actor instance created")
    def receive = {
      case MsgB2C =>
        Thread.sleep(10000) // simulating long running behavior
        println("done with long processing...")
        sender ! MsgC2B
    }
  }
}
