package damore

import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorRef, OneForOneStrategy, Props, Status}
import akka.event.LoggingReceive
import akka.util.Timeout

object ActorB {

  sealed trait Operation
  case class SendMessage() extends Operation
  case class MessageA2B() extends Operation
  case class MessageB2C() extends Operation
  case class OperationRetry(o: Operation, n:Int) extends Operation

  sealed trait OperationReply
  case class MessageA2B_Ack() extends OperationReply
  case class MessageA2B_Failed() extends OperationReply

  case class MessageB2C_Ack()  extends OperationReply

  def props(propsActorC: Props): Props = Props(classOf[ActorB], propsActorC)
}

class ActorB(propsActorC: Props) extends Actor  with ActorLogging {
  import ActorB._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy = OneForOneStrategy() {
      case _: MyRetryTimeoutException      => {
        log.info("ActorB - supervisorStrategy caught MyRetryTimeoutException - Resume")
        Stop
      }
      case _: ActorInitializationException     => {
        log.info("ActorB - supervisorStrategy caught ActorInitializationException - Restart")
          Stop
      }
      case e: Exception                => {
        log.info("ActorB - supervisorStrategy caught Exception " + e +" - Escalate")
        Escalate
      }
    }
  //#strategy

  val actorC = context.actorOf(propsActorC)

  var client :Option[ActorRef] = None

  implicit val timeout = Timeout(4.seconds)

  def receive = LoggingReceive {
    case r:MessageB2C_Ack => {
      log.info("ActorB - secondary received message: MessageB2C_Ack")
      client.foreach(c => { c ! MessageA2B_Ack() })
    }
    case r:MessageA2B => {
      client = Some(context.sender())
      log.info("ActorB received message MessageA2B from client " + client)

      val msg = MessageB2C()
      val actorChildB = context.actorOf(Props(classOf[ActorChildB], self, actorC, msg), "actorChildB")
      actorChildB ! SendMessage()
    }
    case r => {
      log.info("ActorB - received UNHANDLED message " + r)
    }
  }

}

