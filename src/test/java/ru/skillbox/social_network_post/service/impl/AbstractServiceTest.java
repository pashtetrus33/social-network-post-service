package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.skillbox.social_network_post.SocialNetworkPostApplication;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.ReactionRepository;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.KafkaService;

import java.util.UUID;

@SpringBootTest(classes = SocialNetworkPostApplication.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
public abstract class AbstractServiceTest {

    private static final Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16.2-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withNetwork(network);

    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
            .withNetwork(network);

    static {
        postgresContainer.start();
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }


    @MockBean
    protected AccountServiceClient accountServiceClient;

    @MockBean
    protected FriendServiceClient friendServiceClient;

    @MockBean
    protected KafkaService kafkaService;

    @Autowired
    protected PostServiceImpl postService;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected CommentServiceImpl commentService;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected ReactionServiceImpl reactionService;

    @Autowired
    protected ReactionRepository reactionRepository;


    @BeforeEach
    void setUp() {
        // Общая логика настройки для всех тестов
        setUpAuthentication();
        clearRepositoryData();
    }

    private void setUpAuthentication() {
        // Создаем тестовую аутентификацию
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UUID testAccountId = UUID.randomUUID();

        var authentication = new UsernamePasswordAuthenticationToken(testAccountId, null);
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);
    }


    protected void clearRepositoryData() {
        // Очистка данных перед каждым тестом
        postRepository.deleteAll();
        commentRepository.deleteAll();
        reactionRepository.deleteAll();
    }
}