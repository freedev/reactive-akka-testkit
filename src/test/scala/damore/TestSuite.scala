package damore

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll

class TestSuite
  extends Step1
//    with Tools
    with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("KVStoreSuite")

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

