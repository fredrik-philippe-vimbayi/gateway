## Gateway Service

This is a gateway service that is built using Spring Cloud Gateway. It redirects requests to microservice 
containers which are registered to Consul service registration. 


### Endpoints

| HTTP | Path                | Information            | Status Code | Response Body      |
|------|---------------------|------------------------|-------------|--------------------|
| POST | /sign-up            | Register a new user    | 201         | -                  |
| POST | /login              | Authenticate a user    | 200         | JWT token          |
| GET  | /url/short          | Get all short urls     | 200         | List of short-urls |
| POST | /url/short          | Create a new short url | 201         | Short-url          |
| GET  | /urls/:short-url-id | Navigate to a url      | 302         | Web page           |


### Credit to the following microservices:
- [authentication](https://github.com/fredrik-philippe-vimbayi/auth-microservice)
- [url-shortener](https://github.com/DarkendHall/url_shortener)
- [image-service](https://github.com/Patlenlix/image-storage)
