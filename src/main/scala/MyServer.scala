/*import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import my.service.my_service.*

object MyServer {
  def main(args: Array[String]): Unit = {
    val server = ServerBuilder.forPort(9000)
      .addService(MyServiceGrpc.bindService(new MyService , scala.concurrent.ExecutionContext.Implicits.global))
      .build
      .start

    println("Server started. Listening on port 50051")

    server.awaitTermination()
  }
}

class MyService extends MyServiceGrpc.MyService {
  override def myStreamingMethod(responseObserver: StreamObserver[MyResponse]): StreamObserver[MyRequest] = {
    new StreamObserver[MyRequest] {
      override def onNext(request: MyRequest): Unit = {
        val response = MyResponse.of(s"Received messages: ${request.message}")
        responseObserver.onNext(response)
      }

      override def onError(t: Throwable): Unit = {
        responseObserver.onError(t)
      }

      override def onCompleted(): Unit = {
        responseObserver.onCompleted()
      }
    }
  }
}*/