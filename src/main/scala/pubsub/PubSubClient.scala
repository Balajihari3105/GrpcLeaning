package pubsub
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.grpc.stub.StreamObserver
import pubsub.my_service.{Message, PubSubServiceGrpc, PublishRequest, SubscribeRequest}
import pubsub.PubSubService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PubSubClient {
  def main(args: Array[String]): Unit = {
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext.build
    val client = PubSubServiceGrpc.stub(channel)

    val observer = new StreamObserver[Message] {
      override def onNext(message: Message): Unit = {
        println(s"Received message: ${message.message} on topic ${message.topic}")
      }

      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = {}
    }

    client.subscribe(SubscribeRequest("topic1"), observer)

    client.publish(PublishRequest("topic1", "Hello, world!")).onComplete {
      case scala.util.Success(response) => println(s"Publish response: $response")
      case scala.util.Failure(t) => println(s"Error publishing message: $t")
    }

    Thread.sleep(10000)
    channel.shutdown()
  }
}