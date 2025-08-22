package dev.senna.controller;

import dev.senna.repository.UserRepository;
import dev.senna.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("UserController IT tests")
class UserControllerIT {

    public record CreateUserRequest(String username, String password) {}
    public record UpdateUserDto(String username, String password, String role) {}

    private static String createdUserId;
    private static String usernameForTest;


    @BeforeAll
    static void setUp() {
        usernameForTest = "USERNAME";
    }

    @Nested
    @DisplayName("POST /user tests")
    class postUserTests {

        @Test
        @DisplayName("Should create an user and return 201")
        void shouldCreateAnUserAndReturn201() {

            var req = new CreateUserRequest(usernameForTest, "PASSWORD");

            var locationHeader = given()
                    .contentType(ContentType.JSON)
                    .body(req)
                    .when()
                    .post("/user")
                    .then()
                    .statusCode(201)
                    .header("Location", containsString("/user/"))
                    .extract()
                    .header("Location");

            assertNotNull(locationHeader);
            createdUserId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
            assertNotNull(createdUserId);
        }

        @Test
        @DisplayName("Should get fail when trying user with existent username and return 409")
        void shouldGetFailWhenTryingUserWithExistentUsernameAndReturn409() {

            var conflictingUserName = "CONFLICTING_USER_NAME";
            var req = new CreateUserRequest(conflictingUserName, "PASSWORD");

            // Primeira criação com sucesso
            given()
                    .contentType(ContentType.JSON)
                    .body(req)
                    .when()
                    .post("/user")
                    .then()
                    .statusCode(201);

            given()
                    .contentType(ContentType.JSON)
                    .body(req)
                    .when()
                    .post("/user")
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("GET /user/{userId}")
    class getUserByIdTests {

        @Test
        @DisplayName("Should find user by id and return 200")
        void shouldFindUserByIdAndReturn200() {

            assertNotNull(createdUserId);

            given()
                    .when()
                    .get("/user/{id}", createdUserId)
                    .then()
                    .statusCode(200)
                    .body("username", equalTo(usernameForTest))
                    .body("role", equalTo("OFFICER"));
        }

        @Test
        @DisplayName("Should return 404 when trying to find user with non-existent id")
        void shouldReturn404WhenTryingToFindUserWithNonExistentId() {

            var nonExistentId = UUID.randomUUID();

            given()
                    .when()
                    .get("/user/{id}", nonExistentId)
                    .then()
                    .statusCode(404);
        }

        @Nested
        @DisplayName("GET /users tests")
        class getUsersTests {

            @Test
            @DisplayName("Should list all users and return 200")
            void shouldListAllUsersAndReturn200() {

                given()
                        .when()
                        .get("/user")
                        .then()
                        .statusCode(200)
                        .body("$", hasSize(greaterThanOrEqualTo(1)))
                        .body("userName", hasItem(usernameForTest));
            }
        }

        @Nested
        @DisplayName("PATCH /user/{id}")
        class patchUserTests {

            @Test
            @DisplayName("Should update an user and return 204")
            void shouldUpdateAnUserAndReturn204() {

                assertNotNull(createdUserId);
                var updatedUsername = "UPDATED_USERNAME";
                var updateReq = new UpdateUserDto(updatedUsername, "UPDATE_PASSWORD", null);

                given()
                        .contentType(ContentType.JSON)
                        .body(updateReq)
                        .when()
                        .patch("/user/{id}", createdUserId)
                        .then()
                        .statusCode(204);

                given()
                        .when()
                        .get("/user/{id}", createdUserId)
                        .then()
                        .statusCode(200)
                        .body("username", equalTo(updatedUsername));
            }
        }

        @Nested
        @DisplayName("DELETE /user/{id}")
        class deleteUserTests {

            @Test
            @DisplayName("Should delete an user and return 204")
            void shouldDeleteAnUserAndReturn204() {

                assertNotNull(createdUserId);

                given()
                        .when()
                        .delete("/user/{id}", createdUserId)
                        .then()
                        .statusCode(204);

                given()
                        .when()
                        .delete("/user/{id}", createdUserId)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should try delete an user and return 404")
            void shouldTryDeleteAnUserAndReturn404() {

                var nonExistentId = UUID.randomUUID();

                given()
                        .when()
                        .delete("/user/{id}", nonExistentId)
                        .then()
                        .statusCode(404);
            }
        }


    }
}