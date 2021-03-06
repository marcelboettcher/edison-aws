package de.otto.edison.aws.dynamo.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Optional;

import static de.otto.edison.aws.dynamo.jobs.JobInfoConverter.*;
import static de.otto.edison.aws.dynamo.jobs.testsupport.JobInfoMother.jobInfo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = DynamoDbTestConfiguration.class)
@SpringBootTest
public class DynamoJobRepositoryTest {

    public static final String TABLE_NAME = "someTestTable";

    @Autowired
    private DynamoDBClient dynamoDBClient;
    private DynamoJobRepository dynamoJobRepository;

    @Before
    public void before() {
        createTestTable();
        dynamoJobRepository = new DynamoJobRepository(dynamoDBClient, new DynamoJobRepoProperties(true, TABLE_NAME));
    }

    @After
    public void after() {
        deleteTestTable();
    }

    private void createTestTable() {
        dynamoDBClient.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder().attributeName(ID).keyType(KeyType.HASH).build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName(HOSTNAME).attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName(JOB_TYPE).attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName(JOB_STATUS).attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName(STARTED).attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName(STOPPED).attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName(LAST_UPDATED).attributeType(ScalarAttributeType.S).build()
                        // TODO: Job messages as List
                )
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build());
    }

    private void deleteTestTable() {
        dynamoDBClient.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
    }

    @Test
    @Ignore
    public void shouldWriteAndReadJobInfo() throws Exception {
        // given
        JobInfo jobInfo = jobInfo("someJobId").build();

        JobInfo createdJobInfo = dynamoJobRepository.createOrUpdate(jobInfo);

        // when
        Optional<JobInfo> jobInfoFromDb = dynamoJobRepository.findOne("someJobId");

        // then
        assertThat(jobInfoFromDb.get(), is(jobInfo));
    }

}
