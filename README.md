## Gateway Service

This is a gateway service that is built using Spring Cloud Gateway. The gateway redirects requests to microservice 
containers which are registered to Consul service registration. Service containers send data to each other by 
publishing messages on message exchanges and using routing keys.

### Endpoints

| HTTP | Path                | Information            | Status Code | Response Body      |
|------|---------------------|------------------------|-------------|--------------------|
| POST | /sign-up            | Register a new user    | 201         | -                  |
| POST | /login              | Authenticate a user    | 200         | JWT token          |
| GET  | /url/short          | Get all short urls     | 200         | List of short-urls |
| POST | /url/short          | Create a new short url | 201         | Short-url          |
| GET  | /urls/:short-url-id | Navigate to a url      | 302         | Web page           |
| POST | /image              | Upload an image        | 201         | -                  |
| GET  | /image/:id          | Get a saved image      | 200         | Image preview      |


### Download an image of the application
   ```
    docker pull ghcr.io/fredrik-philippe-vimbayi/gateway:latest 
   ```

### Step-by-step Instructions
1. Create a network
    ```
    docker network create <network-name>
    ``` 
2. Create a database volume
   ```
   docker volume create <database-volume>
   ```
3. Run a MySQL database container on the network
   ```
   docker run -d --name mysql --network <network-name> -v <database-volume> -e MYSQL_ROOT_PASSWORD=root -e 'MYSQL_ROOT_HOST=%' -e MYSQL_DATABASE=gateway -e MYSQL_USER=user -e MYSQL_PASSWORD=password -p 3308:3306 mysql:8.0.29
   ```
4. Run a Consul container on the network
    ```
    docker run -d -p 8500:8500 -p 8600:8600/udp --name=consul --network <network-name> consul agent -server -ui -node=server-1 -bootstrap-expect=1 -client='0.0.0.0'
    ```
5. Run a RabbitMQ container on the network
   ```
   docker run -d --name rabbit --network <network-name> -p 15672:15672 -p 5672:5672 rabbitmq:3-management
   ``` 

6. Generate an RSA public - private key pair at https://mkjwk.org/ 

7. Add configurations to Consul config at http://localhost:8500 in a `data.yml` file with the given folder structure:
   1. Auth-service configurations in **/config/authentication/**
   ```
   spring:
     cloud:
       consul:
         discovery:
           register: true
           prefer-ip-address: true
           instance-id: ${spring.application.name}:${spring.cloud.client.hostname}:${random.int[1,999999]}
     host: consul
     jpa:
       hibernate:
         ddl-auto: update
     datasource:
       url: jdbc:mysql://mysql:3306/gateway?allowPublicKeyRetrieval=true&useSSL=false
       username: user
       password: password
     rabbitmq:
       host: rabbit
       port: 5672
   server:
     port: 8080
     error:
       include-message: always
   key:
     private: your-private-key
   ```
   2. Image-service configurations in **/config/image-service/**
   ```
   spring:
     servlet:
       multipart:
         max-file-size: 10MB
         max-request-size: 10MB
     cloud:
       consul:
         discovery:
            register: true
            prefer-ip-address: true
            instance-id: ${spring.application.name}:${spring.cloud.client.hostname}:${random.int[1,999999]}
         host: consul
     jpa:
       hibernate:
         ddl-auto: update
     datasource:
       url: jdbc:mysql://mysql:3306/gateway?allowPublicKeyRetrieval=true&useSSL=false
       username: user
       password: password
   server:
     port: 8080
   ```
   3. Gateway configuration in **config/application**
   ```
   key:
     public: a-public-key
   ```
   4. Url-shortener configurations in a **config/url-shortener.yml** file (Note: This is a file instead of a folder format)
   ```
   micronaut:
     server:
       port: 8080
   datasources:
     default:
       url: jdbc:mysql://mysql:3306/gateway?allowPublicKeyRetrieval=true&useSSL=false
       username: user
       password: password
       dialect: MYSQL
   jpa.default.properties.hibernate.hbm2ddl.auto: update
   netty:
     default:
       allocator:
         max-order: 3   
   ```

8. Start service containers:
   1. Authentication service
   ```
   docker run -d --network <network-name> --name authentication -p 8080:8080 ghcr.io/fredrik-philippe-vimbayi/auth-microservice:latest
   ```
   2. Url shortener service
   ```
   docker run -d --name url_shortener --network <network-name> -p 8081:8080  -e CONSUL_HOST=consul -e CONSUL_PORT=8500 ghcr.io/darkendhall/url_shortener:latest
   ```
   3. Image service
   ```
   docker volume create <file-storage>
   ```
   ```
   docker run -d --name image-service -p 8082:8080 --network=<network-name> -v=<file-storage> ghcr.io/patlenlix/image-storage:latest
   ```
   
9. Start gateway container:
   ```
   docker run -d --name gateway --network <network-name> -p 8088:8088 ghcr.io/fredrik-philippe-vimbayi/gateway:latest
   ```

### Credit to the following microservices:
- [authentication](https://github.com/fredrik-philippe-vimbayi/auth-microservice)
- [url-shortener](https://github.com/DarkendHall/url_shortener)
- [image-service](https://github.com/Patlenlix/image-storage)
