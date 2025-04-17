package pubsub
import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import pubsub.my_service.{Message, PubSubServiceGrpc, PublishRequest, PublishResponse, SubscribeRequest}
import pubsub.PubSubService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future

object PubSubServer {
  def main(args: Array[String]): Unit = {
    val server = ServerBuilder.forPort(50051)
      .addService(PubSubServiceGrpc.bindService(new PubSubService(), scala.concurrent.ExecutionContext.Implicits.global))
      .build
      .start

    println("Server started. Listening on port 50051")

    server.awaitTermination()
  }
}

class PubSubService extends PubSubServiceGrpc.PubSubService {
  val subscriptions: mutable.Map[String, StreamObserver[Message]] = mutable.Map.empty

  override def subscribe(request: SubscribeRequest): StreamObserver[Message] = {
    val observer = new StreamObserver[Message] {
      override def onNext(message: Message): Unit = {}

      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = {}
    }

    subscriptions(request.topic) = observer
    observer
  }

  override def publish(request: PublishRequest): Future[PublishResponse] = {
    subscriptions.get(request.topic) match {
      case Some(observer) =>
        observer.onNext(Message(request.topic, request.message))
        Future.successful(PublishResponse(true))
      case None =>
        Future.successful(PublishResponse(false))
    }
  }
}