https://viblo.asia/p/spring-boot-xay-dung-ung-dung-reactive-voi-spring-5-webflux-924lJd6mKPM


Reactive Streams API
Đầu tiên chúng ta hiểu cơ chế hoạt động của Reactive Streams API.

Reactive Stream API được tạo bởi các kỹ sư từ Netflix, Pivotal, Lightbend, RedHat, Twitter, and Oracle và bây giờ là một phần của Java 9. Nó định nghĩa 4 interface:

Publisher: Phát ra một chuỗi các sự kiện đến subscriber theo yêu cầu của người mà subscriber đến nó. Một Publisher có thể phục vụ nhiều subscriber. Interface này chỉ có một phương thức:

Publisher.java

public interface Publisher<T>
{
    public void subscribe(Subscriber<? super T> s);
}
Subscriber: Nhận và xử lý sự kiện được phát ra bởi Publisher. Chú ý rằng không có gì xảy ra cho tới khi Subscription – nó được gọi là báo hiệu yêu cầu cho Publisher.

Subscriber.java

public interface Subscriber<T>
{
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
}
Subscription: Định nghĩa mỗi quan hệ 1-1 giữa Publisher và Subscriber. Nó chỉ có thể được sử dụng bởi một Subsriber duy nhất và được sử dụng để báo hiệu yêu cầu (request) hoặc hủy (cancel) data.

Subscription.java

public interface Subscription<T>
{
    public void request(long n);
    public void cancel();
}
Processor: Đại diện cho giai đoạn xử lý gồm cả Publisher và Subscriber đồng thời tuân thủ nguyên tắc của cả 2.

Processor.java

public interface Processor<T, R> extends Subscriber<T>, Publisher<R>
{
}
Bản chất, một Subscriber tạo một Subscription tới Publisher, sau đó Publisher gửi một sự kiện cho Subsriber với một luồng các phần tử.

<img src="https://imgur.com/jnsTggE" height="305" alt="kafka">
<br/>


#----------------------
-   **Update User**
    -   PUT <http://localhost:9000/user/v1/users/{0}>
    
    
curl --location --request PUT 'http://localhost:9000/user/v1/users/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id":1,
    "firstName": "Nguyen001",
    "lastName": "Lack Phuc"
}'


#-----------Run docker ENV Develop-----------------
docker-compose -f docker-compose-develop.yml up --build


#------/kafka-administration-and-monitoring-ui-tools--------

https://dzone.com/articles/kafka-administration-and-monitoring-ui-tools
https://github.com/obsidiandynamics/kafdrop

https://towardsdatascience.com/overview-of-ui-monitoring-tools-for-apache-kafka-clusters-9ca516c165bd

So sánh và kết luận

Nếu bạn không đủ tiền mua giấy phép thương mại thì các lựa chọn của bạn là Yahoo Kafka Manager, LinkedIn Burrow, KafDrop và Kafka Tool.
 Theo tôi, giải pháp trước đây là một giải pháp toàn diện nên phù hợp với hầu hết các trường hợp sử dụng.

Nếu bạn đang điều hành Kafka Clusters tương đối lớn, thì bạn nên trả tiền cho một giấy phép thương mại.
 Confluent và Lenses cung cấp nhiều chức năng phong phú hơn so với các công cụ giám sát khác mà chúng 
 tôi đã thấy trong bài đăng này và tôi thực sự khuyên bạn nên sử dụng cả hai công cụ này.

#------Kafka Monitor--------
docker run -d --rm -p 2000:9000 \
    -e KAFKA_BROKERCONNECT=localhost:29092 \
    -e JVM_OPTS="-Xms32M -Xmx64M" \
    -e SERVER_SERVLET_CONTEXTPATH="/" \
    obsidiandynamics/kafdrop